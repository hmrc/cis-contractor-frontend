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

package controllers.amend.partnership

import config.FrontendAppConfig
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import controllers.routes
import models.UserAnswers
import pages.add.partnership.PartnershipNamePage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Reads
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import queries.{CisIdQuery, OriginalPartnershipAnswersQuery}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.amend.partnership.AmendPartnershipConfirmationViewModel
import views.html.amend.AmendConfirmationView

import javax.inject.Inject

class AmendPartnershipConfirmationController @Inject() (
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

  def onPageLoad(): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>

      val ua = request.userAnswers

      ua.get(OriginalPartnershipAnswersQuery) match {

        case None =>
          logger.error("[AmendPartnershipConfirmationController] Missing OriginalPartnershipAnswersQuery")
          Redirect(routes.JourneyRecoveryController.onPageLoad())

        case Some(originalPartnershipAnswers) =>
          ua.get(CisIdQuery) match {

            case None =>
              logger.error("[AmendPartnershipConfirmationController] Missing CisIdQuery")
              Redirect(routes.JourneyRecoveryController.onPageLoad())

            case Some(cisId) =>
              Ok(
                view(
                  AmendPartnershipConfirmationViewModel.rows(originalPartnershipAnswers, ua),
                  partnershipDisplayName(ua),
                  appConfig.manageYourSubcontractorsUrl(cisId)
                )
              )
          }
      }
    }

  private def partnershipDisplayName(ua: UserAnswers): String =
    ua.get(PartnershipNamePage).getOrElse("")
}
