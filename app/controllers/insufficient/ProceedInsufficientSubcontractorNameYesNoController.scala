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

package controllers.insufficient

import controllers.actions.*
import forms.insufficient.ProceedInsufficientSubcontractorNameYesNoFormProvider
import models.Mode
import models.requests.DataRequest
import models.response.GetNewestVerificationBatchResponse
import navigation.Navigator
import pages.insufficient.ProceedInsufficientSubcontractorNameYesNoPage
import pages.verify.CurrentVerificationBatchResponsePage
import play.api.Logging
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import queries.CisIdQuery
import repositories.SessionRepository
import services.ReviewInsufficientInfoService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.insufficient.ProceedInsufficientSubcontractorNameYesNoView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ProceedInsufficientSubcontractorNameYesNoController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: ProceedInsufficientSubcontractorNameYesNoFormProvider,
  reviewInsufficientInfoService: ReviewInsufficientInfoService,
  val controllerComponents: MessagesControllerComponents,
  view: ProceedInsufficientSubcontractorNameYesNoView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  private val form: Form[Boolean] = formProvider()

  private def recoveryRedirect =
    Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())

  private def preparedForm(implicit request: DataRequest[?]) =
    request.userAnswers
      .get(ProceedInsufficientSubcontractorNameYesNoPage)
      .fold(form)(form.fill)

  def onPageLoad(subcontractorId: Long, mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      request.userAnswers.get(CurrentVerificationBatchResponsePage) match {
        case Some(batch) =>
          batch.subcontractors
            .find(_.subcontractorId == subcontractorId)
            .map { subcontractor =>
              Ok(
                view(
                  preparedForm,
                  mode,
                  subcontractor.displayName(),
                  subcontractorId
                )
              )
            }
            .getOrElse(recoveryRedirect)

        case None =>
          recoveryRedirect
      }
    }

  def onSubmit(subcontractorId: Long, mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>

      val result =
        (request.userAnswers.get(CisIdQuery), request.userAnswers.get(CurrentVerificationBatchResponsePage)) match {
          case (Some(cisId), Some(batch)) =>
            batch.subcontractors
              .find(_.subcontractorId == subcontractorId)
              .map { subcontractor =>
                form
                  .bindFromRequest()
                  .fold(
                    formWithErrors =>
                      Future.successful(
                        BadRequest(
                          view(
                            formWithErrors,
                            mode,
                            subcontractor.displayName,
                            subcontractorId
                          )
                        )
                      ),
                    value =>
                      for {
                        updatedAnswers <-
                          Future.fromTry(request.userAnswers.set(ProceedInsufficientSubcontractorNameYesNoPage, value))
                        _              <- sessionRepository.set(updatedAnswers)
                        _              <-
                          reviewInsufficientInfoService.proceedInsufficientVerification(cisId, subcontractorId, batch)

                      } yield Redirect(
                        navigator.nextPage(
                          ProceedInsufficientSubcontractorNameYesNoPage,
                          mode,
                          updatedAnswers
                        )
                      )
                  )
              }
              .getOrElse(Future.successful(recoveryRedirect))
          case _                          =>
            Future.successful(recoveryRedirect)
        }
      result.recover { case ex =>
        logger.error(
          s"[DeleteAmendedNilMonthlyReturnController][onSubmit] Failed to proceed insufficient verification for subcontractorId=$subcontractorId",
          ex
        )
        recoveryRedirect
      }
    }
}
