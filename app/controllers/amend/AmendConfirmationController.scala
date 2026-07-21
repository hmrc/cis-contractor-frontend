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

package controllers.amend

import config.FrontendAppConfig
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import controllers.routes
import models.requests.DataRequest
import pages.add.company.CompanyNamePage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Reads
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import queries.{CisIdQuery, Gettable, OriginalCompanyAnswersQuery}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.amend.CompanyAmendConfirmationViewModel
import views.html.amend.AmendConfirmationView

import javax.inject.Inject

class AmendConfirmationController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view: AmendConfirmationView,
  appConfig: FrontendAppConfig
) extends FrontendBaseController
    with I18nSupport
    with Logging {

  def companyOnPageLoad(): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      withOriginalAnswers(OriginalCompanyAnswersQuery) { (original, cisId) =>
        Ok(
          view(
            CompanyAmendConfirmationViewModel.rows(original, request.userAnswers),
            request.userAnswers.get(CompanyNamePage).getOrElse(""),
            appConfig.manageYourSubcontractorsUrl(cisId)
          )
        )
      }
    }

  private def withOriginalAnswers[A: Reads](
    query: Gettable[A]
  )(
    block: (A, String) => Result
  )(implicit request: DataRequest[AnyContent]): Result = {

    val ua = request.userAnswers

    ua.get(query) match {

      case None =>
        logger.error(s"[AmendConfirmationController] Missing ${query.toString}")
        Redirect(routes.JourneyRecoveryController.onPageLoad())

      case Some(original) =>
        ua.get(CisIdQuery) match {

          case None =>
            logger.error("[AmendConfirmationController] Missing CisIdQuery")
            Redirect(routes.JourneyRecoveryController.onPageLoad())

          case Some(cisId) =>
            block(original, cisId)
        }
    }
  }

}
