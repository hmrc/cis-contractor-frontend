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
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.verify.SubmissionSendingView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SubmissionSendingController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  appConfig: FrontendAppConfig,
  view: SubmissionSendingView,
  verificationService: VerificationService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  private def recovery: Result =
    Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())

  def onPageLoad: Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

      verificationService.createSubmitAndPersistVerificationSubmission
        .map(response => redirectForInitialSubmissionStatus(response.status))
        .recover { case ex =>
          logger.error("[SubmissionSendingController.onPageLoad] Failed to create submission", ex)
          recovery
        }
    }

  def onPollAndRedirect: Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

      request.userAnswers.get(VerificationSubmissionDetailsPage) match {
        case None =>
          Future.successful(recovery)

        case Some(submissionDetails) =>
          val pollInterval =
            submissionDetails.pollIntervalSeconds.getOrElse(appConfig.submissionPollDefaultIntervalSeconds)

          verificationService
            .pollStatus(request.userAnswers, submissionDetails)
            .map(response => redirectForPollSubmissionStatus(response.status, pollInterval))
            .recover { case ex =>
              logger.error("[SubmissionSendingController.onPollAndRedirect] Verification poll failed", ex)
              recovery
            }
      }
    }

  private def redirectForInitialSubmissionStatus(status: String): Result =
    SubmissionStatus.fromString(status) match {
      case PENDING | SubmissionStatus.ACCEPTED =>
        Redirect(controllers.verify.routes.SubmissionSendingController.onPollAndRedirect)
      case FATAL_ERROR                         =>
        Redirect(controllers.verify.routes.VerificationNotSubmittedWarningController.onPageLoad())
      case SEND_ERROR                          =>
        Redirect(controllers.verify.routes.VerifySendErrorController.onPageLoad())
      case _                                   =>
        recovery
    }

  private def redirectForPollSubmissionStatus(status: SubmissionStatus, pollInterval: Int)(implicit
    request: DataRequest[_]
  ): Result =
    status match {
      case PENDING | SubmissionStatus.ACCEPTED =>
        Ok(view()).withHeaders("Refresh" -> pollInterval.toString)
      case SUBMITTED                           =>
        Redirect(controllers.verify.routes.VerificationRequestSubmittedController.onPageLoad())
      case SUBMITTED_NO_RECEIPT                => // TODO: matching screen not found
        recovery
      case DEPARTMENTAL_ERROR                  =>
        Redirect(controllers.verify.routes.VerifyDepartmentalErrorController.onPageLoad())
      case FATAL_ERROR                         =>
        Redirect(controllers.verify.routes.VerificationNotSubmittedWarningController.onPageLoad())
      case SEND_ERROR                          =>
        Redirect(controllers.verify.routes.VerifySendErrorController.onPageLoad())
      case TIMED_OUT                           =>
        Redirect(controllers.verify.routes.VerificationRequestInProgressController.onPageLoad())
      case _                                   =>
        recovery
    }
}
