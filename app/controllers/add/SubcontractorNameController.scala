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

import controllers.actions.*
import forms.add.SubcontractorNameFormProvider
import models.Mode
import navigation.Navigator
import models.add.SubcontractorName.format
import pages.add.SubcontractorNamePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.SubcontractorService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.add.SubcontractorNameView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SubcontractorNameController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: SubcontractorNameFormProvider,
  subcontractorService: SubcontractorService,
  val controllerComponents: MessagesControllerComponents,
  view: SubcontractorNameView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>

    val preparedForm = request.userAnswers.get(SubcontractorNamePage) match {
      case Some(subcontractorName) => form.fill(subcontractorName)
      case None                    => form
    }

    Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
          value =>
            for {
              updatedAnswers           <- Future.fromTry(request.userAnswers.set(SubcontractorNamePage, value))
              _                        <- sessionRepository.set(updatedAnswers)
              userAnswersWithSubbieRef <- subcontractorService.ensureSubcontractorInUserAnswers(updatedAnswers)
              _                        <- sessionRepository.set(userAnswersWithSubbieRef)
              _                        <- subcontractorService.updateSubcontractor(updatedAnswers)
            } yield Redirect(navigator.nextPage(SubcontractorNamePage, mode, updatedAnswers))
        )
  }
}
