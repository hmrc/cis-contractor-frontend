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
import forms.add.partnership.PartnershipNameFormProvider
import models.Mode
import navigation.Navigator
import pages.add.partnership.PartnershipNamePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.SubcontractorService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.add.partnership.PartnershipNameView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}




class PartnershipNameController @Inject()(
                                                        override val messagesApi: MessagesApi,
                                                        sessionRepository: SessionRepository,
                                                        navigator: Navigator,
                                                        identify: IdentifierAction,
                                                        getData: DataRetrievalAction,
                                                        requireData: DataRequiredAction,
                                                        formProvider: PartnershipNameFormProvider,
                                                        subcontractorService: SubcontractorService,
                                                        val controllerComponents: MessagesControllerComponents,
                                                        view: PartnershipNameView
                                         )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      val preparedForm = request.userAnswers.get(PartnershipNamePage).fold(form)(form.fill)
      Ok(view(preparedForm, mode))
    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      form.bindFromRequest().fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(PartnershipNamePage, value))
            _              <- sessionRepository.set(updatedAnswers)
            _              <- subcontractorService.updateSubcontractor(updatedAnswers) 
          } yield Redirect(navigator.nextPage(PartnershipNamePage, mode, updatedAnswers))
      )
    }
}
