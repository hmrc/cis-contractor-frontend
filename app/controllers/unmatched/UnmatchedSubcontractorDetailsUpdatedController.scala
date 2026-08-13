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

package controllers.unmatched

import config.FrontendAppConfig
import controllers.actions.{CisIdRequiredAction, DataRequiredAction, DataRetrievalAction, IdentifierAction}
import controllers.routes
import models.unmatched.UnmatchedSubcontractorDetailsUpdatedReturnTo
import pages.unmatched.UnmatchedSubcontractorDetailsUpdatedPage
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.unmatched.UnmatchedSubcontractorDetailsUpdatedViewModel
import views.html.unmatched.UnmatchedSubcontractorDetailsUpdatedView

import javax.inject.Inject

class UnmatchedSubcontractorDetailsUpdatedController @Inject() (
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  cisIdRequired: CisIdRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view: UnmatchedSubcontractorDetailsUpdatedView,
  appConfig: FrontendAppConfig
) extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen cisIdRequired) { implicit request =>
      request.userAnswers.get(UnmatchedSubcontractorDetailsUpdatedPage) match {

        case Some(confirmationData) =>
          val (returnUrl, returnTextKey, showBeforeYouGo) =
            linkDetails(
              confirmationData.returnTo,
              appConfig.manageYourSubcontractorsUrl(request.cisId)
            )

          Ok(
            view(
              rows = UnmatchedSubcontractorDetailsUpdatedViewModel
                .rows(confirmationData),
              subcontractorName = confirmationData.subcontractorName.displayName,
              returnUrl = returnUrl,
              returnTextKey = returnTextKey,
              showBeforeYouGo = showBeforeYouGo
            )
          )

        case None =>
          logger.error(
            "[UnmatchedSubcontractorDetailsUpdatedController.onPageLoad] Missing UnmatchedSubcontractorDetailsUpdatedPage"
          )
          Redirect(routes.JourneyRecoveryController.onPageLoad())
      }
    }

  private def linkDetails(
    returnTo: String,
    manageYourSubcontractorsUrl: => String
  ): (String, String, Boolean) =
    returnTo match {

      case UnmatchedSubcontractorDetailsUpdatedReturnTo.YourSubcontractors =>
        (
          manageYourSubcontractorsUrl,
          "unmatchedSubcontractorDetailsUpdated.yourSubcontractors",
          true
        )

      case UnmatchedSubcontractorDetailsUpdatedReturnTo.CannotVerifyAllSubcontractors =>
        (
          "#",
          "unmatchedSubcontractorDetailsUpdated.cannotVerifyAllSubcontractors",
          false
        )

      case UnmatchedSubcontractorDetailsUpdatedReturnTo.ReviewUnmatchedSubcontractors =>
        (
          "#",
          "unmatchedSubcontractorDetailsUpdated.reviewUnmatchedSubcontractors",
          false
        )

      case _ =>
        (
          "#",
          "unmatchedSubcontractorDetailsUpdated.cannotVerifyAllSubcontractors",
          false
        )
    }
}
