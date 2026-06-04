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
import models.requests.{CreateSubmissionForVerificationRequest, VerificationToUpdate}
import models.verify.ValidatedVerify
import pages.verify.CurrentVerificationBatchResponsePage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import queries.CisIdQuery
import services.VerificationService
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
                                              view: SubmissionSendingView,
                                              verificationService: VerificationService
                                            )(implicit ec: ExecutionContext)
  extends FrontendBaseController
    with I18nSupport
    with Logging {

  private def recovery: Result =
    Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())

  def onPageLoad(): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>

      ValidatedVerify.build(request.userAnswers) match {
        case Left(error) =>
          logger.error(s"[SubmissionSendingController] Validation failed: $error")
          Future.successful(recovery)

        case Right(validated) =>
          buildSubmissionRequest(request.userAnswers, validated) match {
            case Left(msg) =>
              logger.error(s"[SubmissionSendingController] Failed to build submission request: $msg")
              Future.successful(recovery)

            case Right(submissionReq) =>
              verificationService
                .createSubmissionForVerification(submissionReq)
                .map{_ =>
                  logger.info(
                    s"[VerifyCheckYourAnswersController] Successfully created submission for verification, redirecting to submission sending"
                  )
                  Ok(view())
                }
                .recover { case t =>
                  logger.error("[SubmissionSendingController] Failed to create submission", t)
                  recovery
                }
          }
      }
    }

  private def buildSubmissionRequest(
                                      ua: models.UserAnswers,
                                      validated: ValidatedVerify
                                    ): Either[String, CreateSubmissionForVerificationRequest] =
    for {
      instanceId <- ua.get(CisIdQuery).toRight("CisIdQuery not found")
      current    <- ua.get(CurrentVerificationBatchResponsePage).toRight("CurrentVerificationBatchResponsePage not found")

      batchId  <- current.verificationBatch.map(_.verificationBatchId).toRight("verificationBatchId missing")
      batchRef <- current.verificationBatch.flatMap(_.verifBatchResourceRef).toRight("verificationBatchResourceRef missing")

      email <- validated.emailToUse.toRight("No email resolved for submission")
    } yield {

      val verifications: Seq[VerificationToUpdate] =
        current.verifications.flatMap(_.verificationResourceRef).map { ref =>
          VerificationToUpdate(
            subcontractorName = "Unknown", // ??
            verificationResourceRef = ref,
            proceedVerification = "Y"  // ??
          )
        }

      CreateSubmissionForVerificationRequest(
        instanceId = instanceId,
        verificationBatchId = batchId,
        verificationBatchResourceRef = batchRef,
        emailRecipient = email,
        irMarkGenerated = None,
        verifications = verifications,
        agentId = None
      )
    }
}