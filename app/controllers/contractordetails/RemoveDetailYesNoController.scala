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
import forms.contractordetails.RemoveDetailYesNoFormProvider
import pages.contractordetails.RemoveDetailYesNoPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.contractordetails.RemoveDetailYesNoView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RemoveDetailYesNoController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: RemoveDetailYesNoFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: RemoveDetailYesNoView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(contractorDetail: String): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      if (contractorDetail == "email" || contractorDetail == "scheme-name") {

        val form         = formProvider(contractorDetail)
        val preparedForm = request.userAnswers.get(RemoveDetailYesNoPage(contractorDetail)) match {
          case None        => form
          case Some(value) => form.fill(value)
        }
        Ok(view(contractorDetail, preparedForm))
      } else {
        Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      }

    }

  def onSubmit(contractorDetail: String): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>

      val form = formProvider(contractorDetail)
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(contractorDetail, formWithErrors))),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(RemoveDetailYesNoPage(contractorDetail), value))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(
              controllers.contractordetails.routes.ContractorDetailsCheckAnswersController.onPageLoad().url
            )
        )
    }
}
