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

package controllers.insufficient

import config.FrontendAppConfig
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import controllers.routes
import queries.CisIdQuery
import models.insufficient.InsufficientSubcontractorDetailsUpdatedReturnTo
import pages.insufficient.InsufficientSubcontractorDetailsUpdatedPage
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.insufficient.InsufficientSubcontractorDetailsUpdatedViewModel
import views.html.insufficient.InsufficientSubcontractorDetailsUpdatedView

import javax.inject.Inject

class InsufficientSubcontractorDetailsUpdatedController @Inject() (
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view: InsufficientSubcontractorDetailsUpdatedView,
  appConfig: FrontendAppConfig
) extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      request.userAnswers.get(InsufficientSubcontractorDetailsUpdatedPage) match {

        case Some(confirmationData) =>
          request.userAnswers.get(CisIdQuery) match {

            case None =>
              logger.error(
                "[InsufficientSubcontractorDetailsUpdatedController.onPageLoad] Missing CisIdQuery"
              )
              Redirect(routes.JourneyRecoveryController.onPageLoad())

            case Some(cisId) =>
              val (returnUrl, returnTextKey, showBeforeYouGo) =
                linkDetails(
                  confirmationData.returnTo,
                  appConfig.manageYourSubcontractorsUrl(cisId)
                )

              Ok(
                view(
                  rows = InsufficientSubcontractorDetailsUpdatedViewModel.rows(
                    confirmationData
                  ),
                  subcontractorName = confirmationData.subcontractorName.displayName,
                  returnUrl = returnUrl,
                  returnTextKey = returnTextKey,
                  showBeforeYouGo = showBeforeYouGo
                )
              )
          }

        case None =>
          logger.error(
            "[InsufficientSubcontractorDetailsUpdatedController.onPageLoad] Missing InsufficientSubcontractorDetailsUpdatedPage"
          )
          Redirect(routes.JourneyRecoveryController.onPageLoad())
      }
    }

  private def linkDetails(
    returnTo: String,
    manageYourSubcontractorsUrl: => String
  ): (String, String, Boolean) =
    returnTo match {

      case InsufficientSubcontractorDetailsUpdatedReturnTo.YourSubcontractors =>
        (
          manageYourSubcontractorsUrl,
          "insufficientSubcontractorDetailsUpdated.yourSubcontractors",
          true
        )

      case InsufficientSubcontractorDetailsUpdatedReturnTo.CannotVerifyAllSubcontractors =>
        (
          "#",
          "insufficientSubcontractorDetailsUpdated.cannotVerifyAllSubcontractors",
          false
        )

      case InsufficientSubcontractorDetailsUpdatedReturnTo.ReviewUnmatchedSubcontractors =>
        (
          "#",
          "insufficientSubcontractorDetailsUpdated.reviewUnmatchedSubcontractors",
          false
        )

      case _ =>
        (
          "#",
          "insufficientSubcontractorDetailsUpdated.cannotVerifyAllSubcontractors",
          false
        )
    }
}
