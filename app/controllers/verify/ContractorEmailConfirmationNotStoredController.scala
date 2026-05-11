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
import forms.verify.ContractorEmailConfirmationNotStoredFormProvider
import models.Mode
import models.UserAnswers
import navigation.Navigator
import pages.verify.{ContractorEmailConfirmationNotStoredPage, NewestVerificationBatchResponsePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.verify.ContractorEmailConfirmationNotStoredView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ContractorEmailConfirmationNotStoredController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: ContractorEmailConfirmationNotStoredFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: ContractorEmailConfirmationNotStoredView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  private def hasStoredEmail(ua: UserAnswers): Boolean =
    ua.get(NewestVerificationBatchResponsePage)
      .exists(_.scheme.exists(_.emailAddress.exists(_.nonEmpty)))

  private def redirectToStored(mode: Mode): Result =
    Redirect(controllers.verify.routes.ContractorEmailConfirmationStoredController.onPageLoad(mode))

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      if (hasStoredEmail(request.userAnswers)) {
        redirectToStored(mode)
      } else {
        val preparedForm = request.userAnswers.get(ContractorEmailConfirmationNotStoredPage) match {
          case None        => form
          case Some(value) => form.fill(value)
        }

        Ok(view(preparedForm, mode))
      }
    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      if (hasStoredEmail(request.userAnswers)) {
        Future.successful(redirectToStored(mode))
      } else {
        form
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
            value =>
              for {
                updatedAnswers <-
                  Future.fromTry(request.userAnswers.set(ContractorEmailConfirmationNotStoredPage, value))
                _              <- sessionRepository.set(updatedAnswers)
              } yield Redirect(navigator.nextPage(ContractorEmailConfirmationNotStoredPage, mode, updatedAnswers))
          )
      }
    }
}
