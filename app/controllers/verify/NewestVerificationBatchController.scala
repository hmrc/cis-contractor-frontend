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

import controllers.AgentClientChecks
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.NormalMode
import models.UserAnswers
import models.verify.VerificationBatchStatus
import pages.verify.NewestVerificationBatchResponsePage
import pages.verify.UnverifiedSubcontractorsPage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.CisManageService
import services.VerificationService
import services.SubmissionStatusCheckResult
import services.CheckLatestSubmissionStatusService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import java.time.LocalDateTime

class NewestVerificationBatchController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  verificationBatchService: VerificationService,
  override protected val cisManagerService: CisManageService,
  override protected val sessionRepository: SessionRepository
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with AgentClientChecks
    with Logging {

  private sealed trait InactivityStatus
  private object InactivityStatus {
    case object Active extends InactivityStatus
    case object Inactive extends InactivityStatus
    case object MissingData extends InactivityStatus
    val SixMonths: Long = 6
  }

  private def checkSchemeInactivity(response: models.response.GetNewestVerificationBatchResponse): InactivityStatus =
    response.monthlyReturn match {
      case None =>
        // No monthly return submitted - scheme is active
        InactivityStatus.Active

      case Some(monthlyReturn) if !monthlyReturn.decNoMoreSubPayments.contains("Y") =>
        InactivityStatus.Active

      case Some(_) =>
        response.monthlyReturnSubmission match {
          case None =>
            InactivityStatus.MissingData

          case Some(monthlyReturnSubmission) =>
            monthlyReturnSubmission.submissionRequestDate match {
              case Some(requestDate) =>
                val sixMonthsLater = requestDate.plusMonths(InactivityStatus.SixMonths)
                if (LocalDateTime.now().isBefore(sixMonthsLater)) InactivityStatus.Inactive else InactivityStatus.Active
              case None              =>
                InactivityStatus.MissingData
            }
        }
    }

  private def checkSubmissionStatus(
    response: models.response.GetNewestVerificationBatchResponse
  ): SubmissionStatusCheckResult = {
    val status = response.verificationBatch.flatMap(_.status).flatMap { raw =>
      val parsed = VerificationBatchStatus.from(raw)
      if (parsed.isEmpty) {
        logger.warn(
          s"[NewestVerificationBatchController.onPageLoad] Unrecognised verification batch status: $raw"
        )
      }
      parsed
    }
    CheckLatestSubmissionStatusService.check(status)
  }

  private def routeFromResponse(
    response: models.response.GetNewestVerificationBatchResponse,
    unverified: Seq[models.Subcontractor]
  ): play.api.mvc.Result =
    checkSchemeInactivity(response) match {
      case InactivityStatus.MissingData =>
        Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())

      case InactivityStatus.Inactive =>
        Redirect(controllers.verify.routes.InactiveSchemeWarningController.onPageLoad())

      case InactivityStatus.Active =>
        routeFromSubmissionStatus(response, unverified)
    }

  private def routeFromSubmissionStatus(
    response: models.response.GetNewestVerificationBatchResponse,
    unverified: Seq[models.Subcontractor]
  ): play.api.mvc.Result =
    checkSubmissionStatus(response) match {
      case SubmissionStatusCheckResult.ShowPendingVerificationWarning =>
        Redirect(controllers.verify.routes.VerificationRequestInProgressController.onPageLoad())

      case SubmissionStatusCheckResult.Continue if response.subcontractors.isEmpty =>
        Redirect(controllers.verify.routes.NoSubcontractorsAddedController.onPageLoad())

      case SubmissionStatusCheckResult.Continue if unverified.isEmpty =>
        Redirect(controllers.verify.routes.VerifyYourSubcontractorsYesNoController.onPageLoad)

      case SubmissionStatusCheckResult.Continue =>
        Redirect(controllers.verify.routes.SelectSubcontractorController.onPageLoad(NormalMode))
    }

  def onPageLoad(): Action[AnyContent] =
    (identify andThen getData).async { implicit request =>
      val userAnswers = request.userAnswers.getOrElse(UserAnswers(request.userId))

      withAgentClientChecks(request.userId, request.isAgent, userAnswers)
        .flatMap {
          case Left(redirect)        => Future.successful(redirect)
          case Right(checkedAnswers) =>
            verificationBatchService
              .refreshNewestVerificationBatch(checkedAnswers)
              .map { updatedAnswers =>

                val batch      = updatedAnswers.get(NewestVerificationBatchResponsePage)
                val unverified = updatedAnswers.get(UnverifiedSubcontractorsPage).getOrElse(Seq.empty)

                batch match {
                  case None           => Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
                  case Some(response) => routeFromResponse(response, unverified)
                }
              }
        }
        .recover { case t =>
          logger.error(
            "[NewestVerificationBatchController.onPageLoad] Failed to refresh newest verification batch",
            t
          )
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        }
    }

  def onContinue: Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      verificationBatchService
        .refreshNewestVerificationBatch(request.userAnswers)
        .map { updatedAnswers =>

          val batch      = updatedAnswers.get(NewestVerificationBatchResponsePage)
          val unverified = updatedAnswers.get(UnverifiedSubcontractorsPage).getOrElse(Seq.empty)

          batch match {
            case None           => Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
            case Some(response) => routeFromSubmissionStatus(response, unverified)
          }
        }
        .recover { case t =>
          logger.error(
            "[NewestVerificationBatchController.onContinue] Failed to refresh newest verification batch",
            t
          )
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        }
    }
}
