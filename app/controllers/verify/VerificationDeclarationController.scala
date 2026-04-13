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
import forms.verify.VerificationDeclarationFormProvider
import models.Mode
import navigation.Navigator
import pages.verify.VerificationDeclarationPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.verify.VerificationDeclarationView
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class VerificationDeclarationController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  formProvider: VerificationDeclarationFormProvider,
  view: VerificationDeclarationView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    Ok(view(form, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>

      val value: String =
        request.body.asFormUrlEncoded
          .flatMap(_.get("value").flatMap(_.headOption))
          .getOrElse("confirmed")

      for {
        updatedAnswers <- Future.fromTry(request.userAnswers.set(VerificationDeclarationPage, value))
        _              <- sessionRepository.set(updatedAnswers)
      } yield Redirect(navigator.nextPage(VerificationDeclarationPage, mode, updatedAnswers))

    }

}
