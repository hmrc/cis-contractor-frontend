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
import forms.add.partnership.PartnershipNominatedPartnerCrnFormProvider
import models.Mode
import navigation.Navigator
import pages.add.partnership.{PartnershipNominatedPartnerCrnPage, PartnershipNominatedPartnerNamePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.add.partnership.PartnershipNominatedPartnerCrnView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PartnershipNominatedPartnerCrnController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: PartnershipNominatedPartnerCrnFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: PartnershipNominatedPartnerCrnView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      val nominatedPartnerName =
        request.userAnswers.get(PartnershipNominatedPartnerNamePage).getOrElse {
          throw new RuntimeException("Missing nominated partner name")
        }

      val preparedForm =
        request.userAnswers.get(PartnershipNominatedPartnerCrnPage).fold(form)(form.fill)

      Ok(view(preparedForm, mode, nominatedPartnerName))
    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      val nominatedPartnerName =
        request.userAnswers.get(PartnershipNominatedPartnerNamePage).getOrElse {
          throw new RuntimeException("Missing nominated partner name")
        }

      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, nominatedPartnerName))),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(PartnershipNominatedPartnerCrnPage, value))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(PartnershipNominatedPartnerCrnPage, mode, updatedAnswers))
        )
    }
}
