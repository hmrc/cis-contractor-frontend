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

package controllers.contractordetails

import controllers.actions.*
import forms.contractordetails.AddSchemeNameYesNoFormProvider
import models.Mode
import navigation.Navigator
import pages.contractordetails.{AddSchemeNameYesNoPage, SchemeNamePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.contractordetails.AddSchemeNameYesNoView

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AddSchemeNameYesNoController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  multiTabAction: MultiTabAction,
  formProvider: AddSchemeNameYesNoFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: AddSchemeNameYesNoView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      val preparedForm = request.userAnswers.get(AddSchemeNameYesNoPage).fold(form)(form.fill)
      Ok(
        view(
          preparedForm,
          mode,
          routes.AddSchemeNameYesNoController.onSubmit(mode)
        )
      )
    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            Future.successful(
              BadRequest(
                view(
                  formWithErrors,
                  mode,
                  routes.AddSchemeNameYesNoController.onSubmit(mode)
                )
              )
            ),
          value =>
            for {
              withYesNo      <- Future.fromTry(request.userAnswers.set(AddSchemeNameYesNoPage, value))
              updatedAnswers <- if (!value) Future.fromTry(withYesNo.remove(SchemeNamePage))
                                else Future.successful(withYesNo)
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(AddSchemeNameYesNoPage, mode, updatedAnswers))
        )
    }

  def onPageLoadWithId(flowId: UUID, mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen multiTabAction.forJourney(flowId)) { implicit request =>
      val id           = flowId.toString
      val page         = AddSchemeNameYesNoPage(id)
      val preparedForm = request.userAnswers.get(page).fold(form)(form.fill)

      Ok(
        view(
          preparedForm,
          mode,
          routes.AddSchemeNameYesNoController.onSubmitWithId(flowId, mode)
        )
      )
    }

  def onSubmitWithId(flowId: UUID, mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen multiTabAction.forJourney(flowId)).async { implicit request =>
      val id           = flowId.toString
      val page         = AddSchemeNameYesNoPage(id)
      val submitAction = routes.AddSchemeNameYesNoController.onSubmitWithId(flowId, mode)

      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, submitAction))),
          value =>
            for {
              withYesNo      <- Future.fromTry(request.userAnswers.set(page, value))
              updatedAnswers <- if (!value) Future.fromTry(withYesNo.remove(SchemeNamePage(id)))
                                else Future.successful(withYesNo)
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(page, mode, updatedAnswers))
        )
    }
}
