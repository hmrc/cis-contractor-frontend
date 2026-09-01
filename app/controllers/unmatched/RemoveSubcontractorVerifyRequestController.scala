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

package controllers.unmatched

import controllers.actions.*
import forms.unmatched.RemoveSubcontractorVerifyRequestFormProvider
import models.Mode
import pages.unmatched.RemoveSubcontractorVerifyRequestPage
import pages.verify.CurrentVerificationBatchResponsePage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.VerificationService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.unmatched.RemoveSubcontractorVerifyRequestView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RemoveSubcontractorVerifyRequestController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: RemoveSubcontractorVerifyRequestFormProvider,
  verificationService: VerificationService,
  val controllerComponents: MessagesControllerComponents,
  view: RemoveSubcontractorVerifyRequestView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  private val form = formProvider()

  private def recoveryRedirect =
    Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())

  def onPageLoad(subcontractorId: Long, mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      request.userAnswers.get(CurrentVerificationBatchResponsePage) match {
        case Some(batch) =>
          batch.subcontractors
            .find(_.subcontractorId == subcontractorId)
            .map { subcontractor =>
              if (
                request.userAnswers
                  .get(RemoveSubcontractorVerifyRequestPage(subcontractorId))
                  .contains(true)
              ) {
                Redirect(
                  controllers.verify.routes.ReviewUnmatchedSubcontractorsController.onPageLoad()
                )
              } else {
                val preparedForm =
                  request.userAnswers
                    .get(RemoveSubcontractorVerifyRequestPage(subcontractorId))
                    .fold(form)(form.fill)

                Ok(
                  view(
                    preparedForm,
                    subcontractor.displayName,
                    subcontractorId
                  )
                )
              }
            }
            .getOrElse(recoveryRedirect)

        case None =>
          recoveryRedirect
      }
    }

  def onSubmit(subcontractorId: Long, mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>

      val result =
        request.userAnswers.get(CurrentVerificationBatchResponsePage) match {
          case Some(batch) =>
            batch.subcontractors
              .find(_.subcontractorId == subcontractorId)
              .map { subcontractor =>
                form
                  .bindFromRequest()
                  .fold(
                    formWithErrors =>
                      Future.successful(BadRequest(view(formWithErrors, subcontractor.displayName, subcontractorId))),
                    value =>
                      if (value) {
                        batch.verifications
                          .find(_.subcontractorId.contains(subcontractorId))
                          .flatMap(_.verificationResourceRef) match {
                          case Some(verificationResourceRef) =>
                            for {
                              updatedAnswers <-
                                Future.fromTry(
                                  request.userAnswers.set(RemoveSubcontractorVerifyRequestPage(subcontractorId), value)
                                )
                              deleteResponse <-
                                verificationService.deleteVerification(updatedAnswers, verificationResourceRef)
                            } yield
                              if (deleteResponse.verificationsCounter.exists(_ > 0)) {
                                Redirect(
                                  controllers.verify.routes.ReviewUnmatchedSubcontractorsController.onPageLoad()
                                )
                              } else if (deleteResponse.verificationsCounter.contains(0L)) {
                                Redirect(
                                  controllers.verify.routes.CheckVerificationBatchReadinessController
                                    .checkVerificationBatchReadiness(mode)
                                )
                              } else {
                                recoveryRedirect
                              }

                          case None =>
                            Future.successful(recoveryRedirect)
                        }
                      } else {
                        for {
                          updatedAnswers <-
                            Future.fromTry(
                              request.userAnswers.set(RemoveSubcontractorVerifyRequestPage(subcontractorId), value)
                            )

                          _ <- sessionRepository.set(updatedAnswers)
                        } yield Redirect(controllers.verify.routes.ReviewUnmatchedSubcontractorsController.onPageLoad())
                      }
                  )
              }
              .getOrElse(Future.successful(recoveryRedirect))

          case None =>
            Future.successful(recoveryRedirect)
        }

      result.recover { case ex =>
        logger.error(
          s"[RemoveSubcontractorVerifyRequestController][onSubmit] " +
            s"Failed to remove verification for subcontractorId=$subcontractorId",
          ex
        )

        recoveryRedirect
      }
    }
}
