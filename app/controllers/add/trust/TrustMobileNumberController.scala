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

package controllers.add.trust

import controllers.actions.*
import controllers.helpers.ContactGuard
import forms.add.trust.TrustMobileNumberFormProvider
import models.Mode
import models.contact.ContactMethodOptions
import navigation.Navigator
import pages.add.trust.{TrustContactMethodOptionsPage, TrustMobileNumberPage, TrustNamePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.add.trust.TrustMobileNumberView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TrustMobileNumberController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: TrustMobileNumberFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: TrustMobileNumberView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with ContactGuard {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      requireContactMethodInSet(
        request.userAnswers.get(TrustNamePage),
        request.userAnswers.get(TrustContactMethodOptionsPage),
        ContactMethodOptions.Mobile
      ) { trustName =>

        val preparedForm =
          request.userAnswers.get(TrustMobileNumberPage).fold(form)(form.fill)

        Ok(view(preparedForm, mode, trustName))
      }
    }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      (for {
        trustName      <- request.userAnswers.get(TrustNamePage)
        contactMethods <- request.userAnswers.get(TrustContactMethodOptionsPage)
        if contactMethods.contains(ContactMethodOptions.Mobile)
      } yield form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, trustName))),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(TrustMobileNumberPage, value))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(TrustMobileNumberPage, mode, updatedAnswers))
        ))
        .getOrElse(Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))
  }
}
