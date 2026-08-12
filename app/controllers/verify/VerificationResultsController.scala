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

import config.FrontendAppConfig
import controllers.actions.*
import pages.verify.LastSubmittedVerificationBatchResponsePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import queries.CisIdQuery
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.verify.VerificationResultsViewModel
import views.html.verify.VerificationResultsView

import javax.inject.Inject

class VerificationResultsController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view: VerificationResultsView
)(implicit appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    request.userAnswers.get(LastSubmittedVerificationBatchResponsePage) match {
      case Some(response) =>
        request.userAnswers.get(CisIdQuery) match {
          case Some(cisId) =>
            val manageSubcontractorsUrl = s"${appConfig.manageSubcontractorsUrl}/$cisId"
            Ok(view(VerificationResultsViewModel.from(response), manageSubcontractorsUrl))
          case None        => Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        }
      case None           => Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
    }
  }
}
