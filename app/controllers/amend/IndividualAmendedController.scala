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
import controllers.actions.*
import pages.add.{SubcontractorNamePage, TradingNameOfSubcontractorPage}
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import queries.{CisIdQuery, OriginalIndividualAnswersQuery}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.amend.IndividualAmendedViewModel
import views.html.amend.IndividualAmendedView

import javax.inject.Inject

class IndividualAmendedController @Inject() (
                                              override val messagesApi: MessagesApi,
                                              identify: IdentifierAction,
                                              getData: DataRetrievalAction,
                                              requireData: DataRequiredAction,
                                              val controllerComponents: MessagesControllerComponents,
                                              view: IndividualAmendedView,
                                              appConfig: FrontendAppConfig
                                            ) extends FrontendBaseController
  with I18nSupport
  with Logging {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val ua = request.userAnswers

    ua.get(OriginalIndividualAnswersQuery) match {
      case None =>
        logger.error("[IndividualAmendedController.onPageLoad] OriginalIndividualAnswersQuery missing from session")
        Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())

      case Some(original) =>
        ua.get(CisIdQuery) match {
          case None =>
            logger.error("[IndividualAmendedController.onPageLoad] CisIdQuery missing from session")
            Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())

          case Some(cisId) =>
            val tableRows            = IndividualAmendedViewModel.rows(original, ua)
            val manageYourSubcontractors = appConfig.manageYourSubcontractorsUrl(cisId)
            Ok(view(tableRows, displayName(ua), manageYourSubcontractors))
        }
    }
  }

  private def displayName(ua: models.UserAnswers): String =
    ua.get(SubcontractorNamePage)
      .map(n => s"${n.lastName}, ${n.firstName}")
      .orElse(ua.get(TradingNameOfSubcontractorPage))
      .getOrElse("")
}
