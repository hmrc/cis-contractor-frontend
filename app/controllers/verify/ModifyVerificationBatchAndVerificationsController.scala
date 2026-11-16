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

import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction, MultiTabAction}
import models.Mode
import models.requests.{CreateVerifications, DeleteVerifications, ModifyVerificationsRequest}
import models.response.GetCurrentVerificationBatchResponse
import pages.verify.{CurrentVerificationBatchResponsePage, SelectSubcontractorPage, SelectSubcontractorsToReverifyPage}
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import queries.{CisIdQuery, VerifyCisIdQuery}
import services.VerificationService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ModifyVerificationBatchAndVerificationsController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  multiTabAction: MultiTabAction,
  val controllerComponents: MessagesControllerComponents,
  verificationService: VerificationService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging
    with PendingVerificationRequestGuard {

  def modifyVerificationBatch(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      redirectIfVerificationRequestInProgress(request.userAnswers) match {
        case Some(redirectResult) => Future.successful(redirectResult)
        case None                 => runModifyLogic(mode, None)
      }
    }

  def modifyVerificationBatchWithId(flowId: UUID, mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen multiTabAction.forVerifyJourney(flowId)).async {
      implicit request =>
        val id = flowId.toString
        redirectIfVerificationRequestInProgress(request.userAnswers, Some(id)) match {
          case Some(redirectResult) => Future.successful(redirectResult)
          case None                 => runModifyLogic(mode, Some(flowId))
        }
    }

  private def runModifyLogic(mode: Mode, flowId: Option[UUID])(implicit
    request: models.requests.DataRequest[?]
  ): Future[Result] = {
    val id           = flowId.map(_.toString)
    val selectPage   = id.map(SelectSubcontractorPage.apply).getOrElse(SelectSubcontractorPage)
    val reverifyPage = id.map(SelectSubcontractorsToReverifyPage.apply).getOrElse(SelectSubcontractorsToReverifyPage)

    val verifyIdsRaw: Seq[String] =
      request.userAnswers
        .get(selectPage)
        .map(_.toSeq.map(_.id))
        .getOrElse(Seq.empty)

    val reverifyIdsRaw: Seq[String] =
      request.userAnswers
        .get(reverifyPage)
        .map(_.toSeq.map(_.id))
        .getOrElse(Seq.empty)

    val selectedIdsEither =
      for {
        verifyIds   <- parseIds("SelectSubcontractorPage", verifyIdsRaw)
        reverifyIds <- parseIds("SelectSubcontractorsToReverifyPage", reverifyIdsRaw)
      } yield (verifyIds ++ reverifyIds).distinct

    selectedIdsEither match {

      case Left(msg) =>
        logger.error(s"[ModifyVerificationBatchAndVerificationsController] $msg")
        Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))

      case Right(selectedSubcontractorIds) =>
        (for {
          instanceId <- instanceIdFromSession(request.userAnswers, id)
          current    <- currentBatchFromSession(request.userAnswers, id)

          idToRef = current.subcontractors.flatMap(s => s.subbieResourceRef.map(ref => s.subcontractorId -> ref)).toMap

          selectedRefs <- selectedRefsFromIds(selectedSubcontractorIds, idToRef)

          existingRefs = current.verifications.flatMap(_.verificationResourceRef).distinct

          createRefs = selectedRefs.filterNot(existingRefs.contains)
          deleteRefs = existingRefs.filterNot(selectedRefs.contains)

          _ <-
            if (createRefs.isEmpty && deleteRefs.isEmpty) {
              Future.successful(())
            } else {
              val modifyReq = buildModifyRequest(instanceId, current, createRefs, deleteRefs)
              id
                .fold(
                  verificationService.modifyVerificationBatchAndVerifications(request.userAnswers, modifyReq)
                )(nestedId =>
                  verificationService.modifyVerificationBatchAndVerifications(
                    request.userAnswers,
                    modifyReq,
                    Some(nestedId)
                  )
                )
                .map(_ => ())
            }
        } yield Redirect(
          flowId
            .map(fid =>
              controllers.verify.routes.CheckVerificationBatchReadinessController
                .checkVerificationBatchReadinessWithId(fid, mode)
            )
            .getOrElse(
              controllers.verify.routes.CheckVerificationBatchReadinessController.checkVerificationBatchReadiness(mode)
            )
        ))
          .recover { case t =>
            logger.error(
              "[ModifyVerificationBatchAndVerificationsController] Failed to modify verification batch/verifications",
              t
            )
            Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
          }
    }
  }

  private def instanceIdFromSession(userAnswers: models.UserAnswers, subJourneyId: Option[String]): Future[String] =
    subJourneyId
      .map(id => userAnswers.get(VerifyCisIdQuery(id)))
      .getOrElse(userAnswers.get(CisIdQuery))
      .map(Future.successful)
      .getOrElse(Future.failed(new RuntimeException("InstanceIdQuery not found in session data")))

  private def currentBatchFromSession(
    userAnswers: models.UserAnswers,
    subJourneyId: Option[String]
  ): Future[GetCurrentVerificationBatchResponse] = {
    val page =
      subJourneyId.map(CurrentVerificationBatchResponsePage.apply).getOrElse(CurrentVerificationBatchResponsePage)
    userAnswers
      .get(page)
      .map(Future.successful)
      .getOrElse(Future.failed(new RuntimeException("CurrentVerificationBatchResponsePage not found in session data")))
  }

  private def parseIds(label: String, ids: Iterable[String]): Either[String, Seq[Long]] = {
    val parsed = ids.toSeq.distinct.map(_.trim).map(_.toLongOption)
    if (parsed.forall(_.isDefined)) Right(parsed.flatten)
    else Left(s"Invalid subcontractor id(s) found in $label")
  }

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
        if (createRefs.nonEmpty) Some(CreateVerifications(verificationBatchResourceRefOpt.get, createRefs)) else None
    )
  }
}
