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

package controllers.add

import controllers.actions.*
import forms.add.IndividualChooseContactDetailsFormProvider
import models.Mode
import models.requests.DataRequest
import navigation.Navigator
import pages.add.IndividualChooseContactDetailsPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.add.IndividualChooseContactDetailsView
import utils.SubcontractorNameExtractor

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class IndividualChooseContactDetailsController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: IndividualChooseContactDetailsFormProvider,
  subcontractorNameExtractor: SubcontractorNameExtractor,
  val controllerComponents: MessagesControllerComponents,
  view: IndividualChooseContactDetailsView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  private def recoveryRedirect = Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())

  private def preparedForm(implicit request: DataRequest[?]) =
    request.userAnswers.get(IndividualChooseContactDetailsPage).fold(form)(form.fill)

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    // val list = Seq("2021-2202", "2022-2203")

    subcontractorNameExtractor
      .getSubcontractorName(request.userAnswers)
      .fold(recoveryRedirect) { subcontractorName =>

        val options: Seq[String] =
          Seq("2021-2202", "2022-2023", "2023-2024", "2024-2025", "2025-2026", "noDetails")

        val radioItems: Seq[RadioItem] =
          options.zipWithIndex.flatMap {
            case (value, index) if value == "noDetails" =>
              Seq(
                // Divider before "noDetails"
                RadioItem(divider = Some("site.or")),
                RadioItem(
                  value = Some(value),
                  content = Text(s"contact.$value"),
                  id = Some(s"value_$index")
                )
              )

            case (value, index) =>
              Seq(
                RadioItem(
                  value = Some(value),
                  content = Text(s"contact.$value"),
                  id = Some(s"value_$index")
                )
              )
          }

        Ok(view(preparedForm, mode, subcontractorName, radioItems))
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      subcontractorNameExtractor
        .getSubcontractorName(request.userAnswers)
        .fold(Future.successful(recoveryRedirect)) { subcontractorName =>
          form
            .bindFromRequest()
            .fold(
              formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, subcontractorName, Seq.empty))),
              value =>
                for {
                  updatedAnswers <- Future.fromTry(request.userAnswers.set(IndividualChooseContactDetailsPage, value))
                  _              <- sessionRepository.set(updatedAnswers)
                } yield Redirect(navigator.nextPage(IndividualChooseContactDetailsPage, mode, updatedAnswers))
            )
        }
  }
}
