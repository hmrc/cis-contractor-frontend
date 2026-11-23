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
import models.{AmendMode, CheckMode, Mode, NormalMode, UserAnswers}
import models.verify.VerificationBatchReadiness
import pages.verify.{CurrentVerificationBatchResponsePage, NewestVerificationBatchResponsePage, SelectSubcontractorPage, SelectSubcontractorsToReverifyPage, VerificationBatchReadinessPage}
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CheckVerificationBatchReadinessController @Inject() (
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

  def checkVerificationBatchReadiness(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      checkReadiness(mode, None)
    }

  def checkVerificationBatchReadinessWithId(flowId: UUID, mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen multiTabAction.forVerifyJourney(flowId)).async {
      implicit request =>
        checkReadiness(mode, Some(flowId))
    }

  private def checkReadiness(mode: Mode, flowId: Option[UUID])(implicit
    request: models.requests.DataRequest[?]
  ) = {
    val id = flowId.map(_.toString)
    updateVerificationBatchReadiness(request.userAnswers, id).map {
      case Some(updatedAnswers)
          if updatedAnswers
            .get(id.map(VerificationBatchReadinessPage.apply).getOrElse(VerificationBatchReadinessPage))
            .contains(true) =>
        val redirect = mode match {
          case NormalMode =>
            nextEmailConfirmationPage(updatedAnswers, flowId)

          case CheckMode =>
            flowId
              .map(fid => controllers.verify.routes.VerifyCheckYourAnswersController.onPageLoadWithId(fid))
              .getOrElse(controllers.verify.routes.VerifyCheckYourAnswersController.onPageLoad())

          case AmendMode =>
            logger.error(
              "[CheckVerificationBatchReadinessController][checkVerificationBatchReadiness] - unexpected AmendMode"
            )
            controllers.routes.JourneyRecoveryController.onPageLoad()
        }

        Redirect(redirect)

      case Some(_) =>
        Redirect(controllers.verify.routes.ReviewInsufficientInfoSubcontractorsController.onPageLoad())

      case None =>
        Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
    }
  }

  def updateVerificationBatchReadiness(ua: UserAnswers): Future[Option[UserAnswers]] =
    updateVerificationBatchReadiness(ua, None)

  def updateVerificationBatchReadiness(ua: UserAnswers, subJourneyId: Option[String]): Future[Option[UserAnswers]] = {
    val readinessPage = subJourneyId.map(VerificationBatchReadinessPage.apply).getOrElse(VerificationBatchReadinessPage)
    batchReadiness(ua, subJourneyId) match {
      case Some(batchReady) =>
        for {
          updatedAnswers <- Future.fromTry(ua.set(readinessPage, batchReady))
          _              <- sessionRepository.set(updatedAnswers)
        } yield Some(updatedAnswers)

      case None =>
        logger.error(
          "[CheckVerificationBatchReadinessController.updateVerificationBatchReadiness] Missing selected subcontractors or verification batch response"
        )
        Future.successful(None)
    }
  }

  private def batchReadiness(ua: UserAnswers, subJourneyId: Option[String] = None): Option[Boolean] = {
    val selectPage   = subJourneyId.map(SelectSubcontractorPage.apply).getOrElse(SelectSubcontractorPage)
    val reverifyPage =
      subJourneyId.map(SelectSubcontractorsToReverifyPage.apply).getOrElse(SelectSubcontractorsToReverifyPage)
    val currentPage  =
      subJourneyId.map(CurrentVerificationBatchResponsePage.apply).getOrElse(CurrentVerificationBatchResponsePage)

    val selectedUnverifiedIds: Set[String] =
      ua.get(selectPage)
        .getOrElse(Set.empty)
        .map(_.id)

    val selectedReverifyIds: Set[String] =
      ua.get(reverifyPage)
        .getOrElse(Set.empty)
        .map(_.id)

    val selectedIds: Set[String] =
      selectedUnverifiedIds ++ selectedReverifyIds

    ua.get(currentPage)
      .filter(_ => selectedIds.nonEmpty)
      .flatMap { batchResponse =>
        val currentSubcontractorIds =
          batchResponse.verifications
            .flatMap(_.subcontractorId)
            .map(_.toString)
            .toSet

        val remainingSelectedIds =
          selectedIds.intersect(currentSubcontractorIds)

        if (batchResponse.verifications.isEmpty) {
          Some(true)
        } else {
          Option.when(remainingSelectedIds.nonEmpty) {
            VerificationBatchReadiness.isBatchReady(remainingSelectedIds, batchResponse)
          }
        }
      }
  }

  private def nextEmailConfirmationPage(ua: UserAnswers, flowId: Option[UUID]): Call = {
    val newestPage     =
      flowId
        .map(id => NewestVerificationBatchResponsePage(id.toString))
        .getOrElse(NewestVerificationBatchResponsePage)
    val hasStoredEmail = ua
      .get(newestPage)
      .flatMap(_.scheme)
      .flatMap(_.emailAddress)
      .isDefined
    if (hasStoredEmail) {
      flowId
        .map(id =>
          controllers.verify.routes.ContractorEmailConfirmationStoredController.onPageLoadWithId(id, NormalMode)
        )
        .getOrElse(controllers.verify.routes.ContractorEmailConfirmationStoredController.onPageLoad(NormalMode))
    } else {
      flowId
        .map(id =>
          controllers.verify.routes.ContractorEmailConfirmationNotStoredController.onPageLoadWithId(id, NormalMode)
        )
        .getOrElse(controllers.verify.routes.ContractorEmailConfirmationNotStoredController.onPageLoad(NormalMode))
    }
  }
}
