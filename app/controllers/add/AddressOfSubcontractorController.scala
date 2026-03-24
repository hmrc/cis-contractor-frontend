/*
 * Copyright 2025 HM Revenue & Customs
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

package controllers.add

import config.FrontendAppConfig
import controllers.actions.*
import forms.add.AddressOfSubcontractorFormProvider
import models.Mode
import models.requests.DataRequest
import navigation.Navigator
import pages.add.AddressOfSubcontractorPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.{CountryOptions, SubcontractorNameExtractor}
import views.html.add.AddressOfSubcontractorView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AddressOfSubcontractorController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  subcontractorNameExtractor: SubcontractorNameExtractor,
  formProvider: AddressOfSubcontractorFormProvider,
  countryOptions: CountryOptions,
  val controllerComponents: MessagesControllerComponents,
  view: AddressOfSubcontractorView
)(implicit ec: ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  private val form = formProvider()

  private def recoveryRedirect =
    Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())

  private def preparedForm(implicit request: DataRequest[?]) =
    request.userAnswers.get(AddressOfSubcontractorPage).fold(form)(form.fill)

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      subcontractorNameExtractor
        .getSubcontractorName(request.userAnswers)
        .fold(recoveryRedirect) { subcontractorName =>
          Ok(view(preparedForm, mode, subcontractorName, countryOptions.options()))
        }
    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      subcontractorNameExtractor
        .getSubcontractorName(request.userAnswers)
        .fold(Future.successful(recoveryRedirect)) { subcontractorName =>
          form
            .bindFromRequest()
            .fold(
              formWithErrors =>
                Future.successful(BadRequest(view(formWithErrors, mode, subcontractorName, countryOptions.options()))),
              value =>
                for {
                  updatedAnswers <- Future.fromTry(request.userAnswers.set(AddressOfSubcontractorPage, value))
                  _              <- sessionRepository.set(updatedAnswers)
                } yield Redirect(navigator.nextPage(AddressOfSubcontractorPage, mode, updatedAnswers))
            )
        }
    }
}
