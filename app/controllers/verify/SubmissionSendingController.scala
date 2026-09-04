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

import config.FrontendAppConfig
import controllers.actions.*
import models.requests.DataRequest
import models.verify.SubmissionStatus
import models.verify.SubmissionStatus.*
import pages.verify.VerificationSubmissionDetailsPage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.VerificationService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import views.html.verify.SubmissionSendingView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SubmissionSendingController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  reconcileFormpRds: FormpRdsReconcileAction,
  val controllerComponents: MessagesControllerComponents,
  appConfig: FrontendAppConfig,
  view: SubmissionSendingView,
  verificationService: VerificationService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  private val SubmitAgainErrorCode = "3000"

  private def recovery: Result =
    Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())

  def onPageLoad: Action[AnyContent] =
    (identify andThen getData andThen requireData andThen reconcileFormpRds).async { implicit request =>
      implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

      verificationService.createSubmitAndPersistVerificationSubmission
        .map(redirectForInitialSubmissionResponse)
        .recover { case ex =>
          logger.error(
            "[SubmissionSendingController.onPageLoad] Failed to create submission",
            ex
          )
          recovery
        }
    }

  def onPollAndRedirect: Action[AnyContent] =
    (identify andThen getData andThen requireData andThen reconcileFormpRds).async { implicit request =>
      implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

      request.userAnswers.get(VerificationSubmissionDetailsPage) match {
        case None =>
          Future.successful(recovery)

        case Some(submissionDetails) =>
          val pollInterval =
            submissionDetails.pollIntervalSeconds
              .getOrElse(appConfig.submissionPollDefaultIntervalSeconds)

          verificationService
            .pollStatusAndPersist(request.userAnswers, submissionDetails)
            .map(response => redirectForPollSubmissionResponse(response, pollInterval))
            .recover { case ex =>
              logger.error(
                "[SubmissionSendingController.onPollAndRedirect] Verification poll failed",
                ex
              )
              recovery
            }
      }
    }

  private def redirectForErrorStatus(
    status: SubmissionStatus,
    govTalkErrorStatus: Option[models.verify.GovTalkErrorStatus]
  ): Result =
    status match {

      case DEPARTMENTAL_ERROR if isSubmitAgainError(govTalkErrorStatus) =>
        Redirect(
          controllers.verify.routes.VerifyDepartmentalErrorSubmitAgainController
            .onPageLoad()
        )

      case DEPARTMENTAL_ERROR =>
        Redirect(
          controllers.verify.routes.VerifyDepartmentalErrorController
            .onPageLoad()
        )

      case FATAL_ERROR if isSubmitAgainError(govTalkErrorStatus) =>
        Redirect(
          controllers.verify.routes.VerifyDepartmentalErrorSubmitAgainController
            .onPageLoad()
        )

      case FATAL_ERROR =>
        Redirect(
          controllers.verify.routes.VerificationNotSubmittedWarningController
            .onPageLoad()
        )

      case _ =>
        recovery
    }

  private def redirectForInitialSubmissionResponse(
    response: ChrisSubmissionResponse
  ): Result =
    SubmissionStatus.fromString(response.status) match {

      case SubmissionStatus.PENDING | SubmissionStatus.ACCEPTED =>
        Redirect(
          controllers.verify.routes.SubmissionSendingController.onPollAndRedirect
        )

      case status @ (DEPARTMENTAL_ERROR | FATAL_ERROR) =>
        redirectForErrorStatus(status, response.govTalkErrorStatus)

      case _ =>
        recovery
    }

  private def redirectForPollSubmissionResponse(
    response: ChrisPollResponse,
    pollInterval: Int
  )(implicit request: DataRequest[_]): Result =
    response.status match {
      case SubmissionStatus.PENDING | SubmissionStatus.ACCEPTED =>
        Ok(view())
          .withHeaders("Refresh" -> pollInterval.toString)

      case SUBMITTED =>
        Redirect(
          controllers.verify.routes.VerificationRequestSubmittedController
            .onPageLoad()
        )

      case SUBMITTED_NO_RECEIPT => // TODO: matching screen not found
        recovery

      case status @ (DEPARTMENTAL_ERROR | FATAL_ERROR) =>
        redirectForErrorStatus(status, response.govTalkErrorStatus)

      case SEND_ERROR =>
        Redirect(
          controllers.verify.routes.VerifySendErrorController.onPageLoad()
        )

      case TIMED_OUT =>
        Redirect(
          controllers.verify.routes.VerificationRequestInProgressController
            .onPageLoad()
        )

      case _ =>
        recovery
    }

  private def isSubmitAgainError(
    govTalkErrorStatus: Option[models.verify.GovTalkErrorStatus]
  ): Boolean =
    govTalkErrorStatus.exists {
      case FatalError(errorCode, _) =>
        errorCode == SubmitAgainErrorCode

      case DepartmentalError(Some(errorCode), _) =>
        errorCode == SubmitAgainErrorCode

      case _ =>
        false
    }
}
