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

import controllers.actions.*
import models.{NormalMode, UserAnswers}
import models.response.GetCurrentVerificationBatchResponse
import models.verify.SelectedSubcontractors
import pages.verify.*
import play.api.Logging
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ContinueVerificationSubmissionController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  multiTabAction: MultiTabAction,
  sessionRepository: SessionRepository,
  val controllerComponents: MessagesControllerComponents
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onSubmit(): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      continue(request.userAnswers, None)
    }

  def onSubmitWithId(flowId: UUID): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen multiTabAction.forVerifyJourney(flowId)).async {
      implicit request =>
        continue(request.userAnswers, Some(flowId))
    }

  private def continue(userAnswers: UserAnswers, flowId: Option[UUID])(implicit
    messages: Messages
  ): Future[Result] = {
    val id          = flowId.map(_.toString)
    val currentPage = id.map(CurrentVerificationBatchResponsePage.apply).getOrElse(CurrentVerificationBatchResponsePage)

    userAnswers.get(currentPage) match {
      case None =>
        logger.error(
          "[ContinueVerificationSubmissionController][onSubmit] - CurrentVerificationBatchResponsePage missing from userAnswers"
        )
        Future.successful(
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        )

      case Some(currentBatch) =>
        val currentIds  = currentBatchSubcontractorIds(currentBatch)
        val selectedIds = existingSelectedIds(userAnswers, id)

        if (currentIds.isEmpty) {
          logger.error("[ContinueVerificationSubmissionController][onSubmit] - current batch has no subcontractors")
          Future.successful(
            Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
          )
        } else if (selectedIds.nonEmpty) {
          if (selectedIds == currentIds) {
            Future.successful(Redirect(readinessCall(flowId)))
          } else {
            logger.error(
              "[ContinueVerificationSubmissionController][onSubmit] - selected subcontractors do not match current batch"
            )
            Future.successful(
              Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
            )
          }
        } else {
          val selectedSubcontractors =
            buildSelectedSubcontractors(currentBatch)

          if (selectedSubcontractors.map(_.id) != currentIds) {
            logger.error(
              "[ContinueVerificationSubmissionController][onSubmit] - selected subcontractors do not match current batch"
            )
            Future.successful(
              Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
            )
          } else {
            val selectPage    = id.map(SelectSubcontractorPage.apply).getOrElse(SelectSubcontractorPage)
            val verifyYesNo   =
              id.map(VerifyYourSubcontractorsYesNoPage.apply).getOrElse(VerifyYourSubcontractorsYesNoPage)
            val reverifyYesNo =
              id.map(ReverifyExistingSubcontractorsYesNoPage.apply).getOrElse(ReverifyExistingSubcontractorsYesNoPage)
            val reverifyPage  =
              id.map(SelectSubcontractorsToReverifyPage.apply).getOrElse(SelectSubcontractorsToReverifyPage)

            for {
              updatedAnswers <- Future.fromTry(
                                  userAnswers
                                    .remove(selectPage)
                                    .flatMap(_.remove(verifyYesNo))
                                    .flatMap(_.set(reverifyYesNo, true))
                                    .flatMap(_.set(reverifyPage, selectedSubcontractors))
                                )
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(readinessCall(flowId))
          }
        }
    }
  }

  private def readinessCall(flowId: Option[UUID]) =
    flowId
      .map(id =>
        controllers.verify.routes.CheckVerificationBatchReadinessController
          .checkVerificationBatchReadinessWithId(id, NormalMode)
      )
      .getOrElse(
        controllers.verify.routes.CheckVerificationBatchReadinessController
          .checkVerificationBatchReadiness(NormalMode)
      )

  private def existingSelectedIds(
    userAnswers: UserAnswers,
    subJourneyId: Option[String]
  ): Set[String] = {
    val selectPage   = subJourneyId.map(SelectSubcontractorPage.apply).getOrElse(SelectSubcontractorPage)
    val reverifyPage =
      subJourneyId.map(SelectSubcontractorsToReverifyPage.apply).getOrElse(SelectSubcontractorsToReverifyPage)

    val selectedToVerify =
      userAnswers
        .get(selectPage)
        .getOrElse(Set.empty)
        .map(_.id)

    val selectedToReverify =
      userAnswers
        .get(reverifyPage)
        .getOrElse(Set.empty)
        .map(_.id)

    selectedToVerify ++ selectedToReverify
  }

  private def currentBatchSubcontractorIds(
    batch: GetCurrentVerificationBatchResponse
  ): Set[String] =
    batch.verifications
      .flatMap(_.subcontractorId)
      .map(_.toString)
      .toSet

  private def buildSelectedSubcontractors(
    batch: GetCurrentVerificationBatchResponse
  )(implicit messages: Messages): Set[SelectedSubcontractors] = {

    val subcontractorsById =
      batch.subcontractors
        .map(subcontractor => subcontractor.subcontractorId -> subcontractor)
        .toMap

    batch.verifications.flatMap { verification =>
      verification.subcontractorId.flatMap { subcontractorId =>
        subcontractorsById
          .get(subcontractorId)
          .map { subcontractor =>
            SelectedSubcontractors(
              id = subcontractorId.toString,
              name = verification.subcontractorName
                .map(_.trim)
                .filter(_.nonEmpty)
                .getOrElse(subcontractor.displayName)
            )
          }
      }
    }.toSet
  }
}
