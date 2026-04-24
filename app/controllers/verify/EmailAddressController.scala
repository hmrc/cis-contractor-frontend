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
import forms.verify.EmailAddressFormProvider
import models.{Mode, UserAnswers}
import models.verify.ContractorEmailConfirmationStored
import navigation.Navigator
import pages.verify.{ContractorEmailConfirmationNotStoredPage, ContractorEmailConfirmationStoredPage, EmailAddressPage}
import pages.verification.NewestVerificationBatchResponsePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.verify.EmailAddressView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class EmailAddressController @Inject() (
                                         override val messagesApi: MessagesApi,
                                         sessionRepository: SessionRepository,
                                         navigator: Navigator,
                                         identify: IdentifierAction,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         formProvider: EmailAddressFormProvider,
                                         val controllerComponents: MessagesControllerComponents,
                                         view: EmailAddressView
                                       )(implicit ec: ExecutionContext)
  extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  private def hintKey(answers: UserAnswers): String = {
    val hasStoredEmail =
      answers
        .get(NewestVerificationBatchResponsePage)
        .exists(_.scheme.exists(_.emailAddress.exists(_.nonEmpty)))

    if (hasStoredEmail) "verify.emailAddress.hint" else "verify.emailAddress.hint.notStored"
  }

  // ✅ AC3: back link depends on “page history” in UserAnswers
  private def backLink(answers: UserAnswers, mode: Mode): Call =
    answers.get(ContractorEmailConfirmationStoredPage) match {
      case Some(ContractorEmailConfirmationStored.DifferentEmail) =>
        controllers.verify.routes.ContractorEmailConfirmationStoredController.onPageLoad(mode)

      case _ =>
        answers.get(ContractorEmailConfirmationNotStoredPage) match {
          case Some(true) =>
            controllers.verify.routes.ContractorEmailConfirmationNotStoredController.onPageLoad(mode)
          case _ =>
            controllers.routes.JourneyRecoveryController.onPageLoad()
        }
    }

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val preparedForm = request.userAnswers.get(EmailAddressPage) match {
      case None        => form
      case Some(value) => form.fill(value)
    }

    Ok(view(preparedForm, mode, hintKey(request.userAnswers), backLink(request.userAnswers, mode)))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors =>
          Future.successful(
            BadRequest(view(formWithErrors, mode, hintKey(request.userAnswers), backLink(request.userAnswers, mode)))
          ),
        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(EmailAddressPage, value))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(EmailAddressPage, mode, updatedAnswers))
      )
  }
}