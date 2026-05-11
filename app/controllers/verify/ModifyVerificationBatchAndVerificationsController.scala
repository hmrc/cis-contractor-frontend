/*
 * Copyright 2026 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers.verify

import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.requests.{CreateVerifications, DeleteVerifications, ModifyVerificationsRequest}
import models.response.GetCurrentVerificationBatchResponse
import pages.verify.{CurrentVerificationBatchResponsePage, SelectSubcontractorPage, SelectSubcontractorsToReverifyPage}
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import queries.CisIdQuery
import services.VerificationService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ModifyVerificationBatchAndVerificationsController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  verificationService: VerificationService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onSubmit(): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>

      val verifyIdsRaw: Seq[String] =
        request.userAnswers
          .get(SelectSubcontractorPage)
          .map(_.toSeq.map(_.id))
          .getOrElse(Seq.empty)

      val reverifyIdsRaw: Seq[String] =
        request.userAnswers
          .get(SelectSubcontractorsToReverifyPage)
          .map(_.toSeq.map(_.id))
          .getOrElse(Seq.empty)

      val selectedIdsEither =
        for {
          verifyIds   <- parseIds("SelectSubcontractorPage", verifyIdsRaw)
          reverifyIds <- parseIds("SelectSubcontractorsToReverifyPage", reverifyIdsRaw)
        } yield (verifyIds ++ reverifyIds).distinct

      selectedIdsEither match {

        case Left(msg) =>
          logger.error(s"[ModifyVerificationBatchAndVerificationsController.onSubmit] $msg")
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))

        case Right(selectedSubcontractorIds) =>
          val instanceIdF = instanceIdFromSession(request.userAnswers)

          (for {
            instanceId <- instanceIdF

            current <- currentBatchFromSession(request.userAnswers)

            idToRef: Map[Long, Long] =
              current.subcontractors.flatMap(s => s.subbieResourceRef.map(ref => s.subcontractorId -> ref)).toMap

            selectedRefs <- selectedRefsFromIds(selectedSubcontractorIds, idToRef)

            existingRefs = current.verifications.flatMap(_.verificationResourceRef).distinct

            //  if selected exists, formP missing(current batch) => Add (createRefs)
            // if selected missing, formP exists(current batch) => Remove (deleteRefs)
            createRefs = selectedRefs.filterNot(existingRefs.contains)
            deleteRefs = existingRefs.filterNot(selectedRefs.contains)

            modifyReq = buildModifyRequest(instanceId, current, createRefs, deleteRefs)
            resultUa <-
              if (createRefs.isEmpty && deleteRefs.isEmpty) {
                Future.successful(())
              } else {
                val modifyReq = buildModifyRequest(instanceId, current, createRefs, deleteRefs)
                verificationService.modifyVerificationBatchAndVerifications(request.userAnswers, modifyReq).map(_ => ())
              }

          } yield Redirect(controllers.routes.IndexController.onPageLoad())).recover { case t =>
            logger.error(
              "[ModifyVerificationBatchAndVerificationsController.onSubmit] Failed to modify verification batch/verifications",
              t
            )
            Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
          }
      }
    }

  private def instanceIdFromSession(userAnswers: models.UserAnswers): Future[String] =
    userAnswers
      .get(CisIdQuery)
      .map(Future.successful)
      .getOrElse(Future.failed(new RuntimeException("InstanceIdQuery not found in session data")))

  private def parseIds(label: String, ids: Iterable[String]): Either[String, Seq[Long]] = {
    val parsed = ids.toSeq.distinct.map(_.trim).map(_.toLongOption)
    if (parsed.forall(_.isDefined)) Right(parsed.flatten)
    else Left(s"Invalid subcontractor id(s) found in $label")
  }

  private def currentBatchFromSession(userAnswers: models.UserAnswers): Future[GetCurrentVerificationBatchResponse] =
    userAnswers
      .get(CurrentVerificationBatchResponsePage)
      .map(Future.successful)
      .getOrElse(Future.failed(new RuntimeException("CurrentVerificationBatchResponsePage not found in session data")))

  private def selectedRefsFromIds(
    selectedSubcontractorIds: Seq[Long],
    idToRef: Map[Long, Long]
  ): Future[Seq[Long]] =
    Future.fromTry {
      scala.util.Try {
        selectedSubcontractorIds.distinct.map { id =>
          idToRef.getOrElse(
            id,
            throw new RuntimeException(
              s"Missing subbieResourceRef for subcontractorId=$id in current verification batch"
            )
          )
        }
      }
    }

  private def buildModifyRequest(
    instanceId: String,
    current: GetCurrentVerificationBatchResponse,
    createRefs: Seq[Long],
    deleteRefs: Seq[Long]
  ): ModifyVerificationsRequest = {

    val verificationBatchResourceRefOpt =
      current.verificationBatch.flatMap(_.verifBatchResourceRef)

    if (createRefs.nonEmpty && verificationBatchResourceRefOpt.isEmpty) {
      throw new RuntimeException("Missing verifBatchResourceRef in current verification batch")
    }

    ModifyVerificationsRequest(
      instanceId = instanceId,
      deleteVerifications = if (deleteRefs.nonEmpty) Some(DeleteVerifications(deleteRefs)) else None,
      createVerifications =
        if (createRefs.nonEmpty)
          Some(CreateVerifications(verificationBatchResourceRefOpt.get, createRefs))
        else None
    )
  }
}
