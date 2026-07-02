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

package controllers.add.partnership

import controllers.actions.*
import controllers.helpers.ContactGuard
import forms.add.partnership.PartnershipPhoneNumberFormProvider
import models.Mode
import models.contact.ContactMethodOptions
import navigation.Navigator
import pages.add.partnership.{PartnershipContactMethodOptionsPage, PartnershipNamePage, PartnershipPhoneNumberPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.add.partnership.PartnershipPhoneNumberView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PartnershipPhoneNumberController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: PartnershipPhoneNumberFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: PartnershipPhoneNumberView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with ContactGuard {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    requireContactMethodInSet(
      request.userAnswers.get(PartnershipNamePage),
      request.userAnswers.get(PartnershipContactMethodOptionsPage),
      ContactMethodOptions.Phone
    ) { partnershipName =>
      val preparedForm = request.userAnswers.get(PartnershipPhoneNumberPage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }
      Ok(view(preparedForm, mode, partnershipName))
    }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      (for {
        partnershipName <- request.userAnswers.get(PartnershipNamePage)
        contactMethods  <- request.userAnswers.get(PartnershipContactMethodOptionsPage)
        if contactMethods.contains(ContactMethodOptions.Phone)
      } yield form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, partnershipName))),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(PartnershipPhoneNumberPage, value))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(PartnershipPhoneNumberPage, mode, updatedAnswers))
        ))
        .getOrElse(Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))
  }
}
