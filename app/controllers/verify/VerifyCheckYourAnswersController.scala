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
import models.verify.ValidatedVerify
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.VerificationService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.verify.*
import viewmodels.govuk.summarylist.*
import views.html.verify.VerifyCheckYourAnswersView
import models.requests.{CreateSubmissionForVerificationRequest, VerificationToUpdate}
import queries.CisIdQuery
import pages.verify.CurrentVerificationBatchResponsePage
import pages.verify.EmailAddressPage

import javax.inject.Inject
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class VerifyCheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view: VerifyCheckYourAnswersView,
  verificationService: VerificationService
) extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val ua = request.userAnswers
    ValidatedVerify.build(ua) match {
      case Right(_)    =>
        val list = SummaryListViewModel(
          rows = Seq(
            SelectSubcontractorSummary.row(ua),
            ReverifyExistingSubcontractorsYesNoSummary.row(ua),
            SelectSubcontractorsToReverifySummary.row(ua),
            ContractorEmailConfirmationStoredSummary.row(ua),
            ContractorEmailConfirmationNotStoredSummary.row(ua),
            EmailAddressSummary.row(ua)
          ).flatten
        )
        Ok(view(list))
      case Left(error) =>
        logger.error(s"[VerifyCheckYourAnswersController.onPageLoad] Validation failed: $error")
        Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
    }
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    ValidatedVerify.build(request.userAnswers) match {
      case Right(_) =>
        buildSubmissionRequest(request.userAnswers) match {
          case Left(_) =>
            Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))

          case Right(submissionReq) =>
            verificationService
              .createSubmissionForVerification(submissionReq)
              .map(_ => Redirect(controllers.verify.routes.SubmissionSendingController.onPageLoad()))
        }

      case Left(error) =>
        logger.error(s"[VerifyCheckYourAnswersController.onSubmit] Validation failed: $error")
        Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
    }
  }

  private def buildSubmissionRequest(ua: models.UserAnswers): Either[String, CreateSubmissionForVerificationRequest] =
    for {
      instanceId <- ua.get(CisIdQuery).toRight("CisIdQuery not found")
      current    <- ua.get(CurrentVerificationBatchResponsePage).toRight("CurrentVerificationBatchResponsePage not found")

      batchId  <- current.verificationBatch.map(_.verificationBatchId).toRight("verificationBatchId missing")
      batchRef <-
        current.verificationBatch.flatMap(_.verifBatchResourceRef).toRight("verificationBatchResourceRef missing")

      email <- ua.get(EmailAddressPage).toRight("EmailAddressPage not found")
    } yield {

      val verifications: Seq[VerificationToUpdate] =
        current.verifications.flatMap { v =>
          v.verificationResourceRef.map { ref =>
            VerificationToUpdate(
              subcontractorName = "Unknown", // toDo
              verificationResourceRef = ref,
              proceedVerification = "Y" // ???
            )
          }
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
