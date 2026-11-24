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
import forms.verify.VerifyYourSubcontractorsYesNoFormProvider
import models.NormalMode
import navigation.Navigator
import config.FrontendAppConfig
import pages.verify.VerifyYourSubcontractorsYesNoPage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import queries.{CisIdQuery, VerifyCisIdQuery}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.verify.VerifyYourSubcontractorsYesNoView

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class VerifyYourSubcontractorsYesNoController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  multiTabAction: MultiTabAction,
  formProvider: VerifyYourSubcontractorsYesNoFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: VerifyYourSubcontractorsYesNoView
)(implicit ec: ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport
    with PendingVerificationRequestGuard
    with Logging {

  private val form = formProvider()

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    redirectIfVerificationRequestInProgress(request.userAnswers).getOrElse {
      val preparedForm = request.userAnswers.get(VerifyYourSubcontractorsYesNoPage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, routes.VerifyYourSubcontractorsYesNoController.onSubmit))
    }
  }

  def onSubmit: Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    redirectIfVerificationRequestInProgress(request.userAnswers) match {
      case Some(redirectResult) =>
        Future.successful(redirectResult)

      case None =>
        form
          .bindFromRequest()
          .fold(
            formWithErrors =>
              Future.successful(
                BadRequest(view(formWithErrors, routes.VerifyYourSubcontractorsYesNoController.onSubmit))
              ),
            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(VerifyYourSubcontractorsYesNoPage, value))
                _              <- sessionRepository.set(updatedAnswers)
              } yield
                if (value) {
                  Redirect(navigator.nextPage(VerifyYourSubcontractorsYesNoPage, NormalMode, updatedAnswers))
                } else {
                  updatedAnswers.get(CisIdQuery) match {
                    case Some(cisId) =>
                      Redirect(s"${appConfig.manageSubcontractorsUrl}/$cisId")

                    case None =>
                      logger.error(
                        "[VerifyYourSubcontractorsYesNoController][onSubmit] - CisIdQuery missing from userAnswers"
                      )
                      Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
                  }
                }
          )
    }
  }

  def onPageLoadWithId(flowId: UUID): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen multiTabAction.forVerifyJourney(flowId)) { implicit request =>
      val id = flowId.toString
      redirectIfVerificationRequestInProgress(request.userAnswers, Some(id)).getOrElse {
        val page         = VerifyYourSubcontractorsYesNoPage(id)
        val preparedForm = request.userAnswers.get(page).fold(form)(form.fill)
        Ok(view(preparedForm, routes.VerifyYourSubcontractorsYesNoController.onSubmitWithId(flowId)))
      }
    }

  def onSubmitWithId(flowId: UUID): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen multiTabAction.forVerifyJourney(flowId)).async {
      implicit request =>
        val id           = flowId.toString
        val submitAction = routes.VerifyYourSubcontractorsYesNoController.onSubmitWithId(flowId)
        redirectIfVerificationRequestInProgress(request.userAnswers, Some(id)) match {
          case Some(redirectResult) =>
            Future.successful(redirectResult)
          case None                 =>
            form
              .bindFromRequest()
              .fold(
                formWithErrors => Future.successful(BadRequest(view(formWithErrors, submitAction))),
                value =>
                  for {
                    updatedAnswers <-
                      Future.fromTry(request.userAnswers.set(VerifyYourSubcontractorsYesNoPage(id), value))
                    _              <- sessionRepository.set(updatedAnswers)
                  } yield
                    if (value) {
                      Redirect(navigator.nextPage(VerifyYourSubcontractorsYesNoPage(id), NormalMode, updatedAnswers))
                    } else {
                      updatedAnswers.get(VerifyCisIdQuery(id)) match {
                        case Some(cisId) =>
                          Redirect(s"${appConfig.manageSubcontractorsUrl}/$cisId")
                        case None        =>
                          logger.error(
                            "[VerifyYourSubcontractorsYesNoController][onSubmitWithId] - VerifyCisIdQuery missing from userAnswers"
                          )
                          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
                      }
                    }
              )
        }
    }
}
