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

import controllers.actions.*
import pages.verify.LastSubmittedVerificationBatchResponsePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.CheckUnmatchedSubcontractorsService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject

class ReviewUnmatchedSubcontractorsController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents
) extends FrontendBaseController
    with I18nSupport {

  def onPageLoad: Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      request.userAnswers.get(LastSubmittedVerificationBatchResponsePage) match {
        case Some(response) =>
          val decisions = CheckUnmatchedSubcontractorsService.reverificationDecisions(response)

          if (!decisions.exists(_.isUnmatched)) {
            Redirect(controllers.verify.routes.VerificationResultsController.onPageLoad())
          } else if (decisions.exists(_.considerForReverification)) {
            Redirect(controllers.routes.UnmatchedSubcontractorsController.onPageLoad())
          } else {
            Redirect(controllers.routes.NoUnmatchedSubcontractorsController.onPageLoad())
          }

        case None =>
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      }
    }
}
