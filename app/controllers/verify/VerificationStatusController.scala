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

import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.{Mode, NormalMode}
import pages.verify.NewestVerificationBatchResponsePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import rules.verify.ReverificationRules
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import play.api.Logging
import javax.inject.Inject
import java.time.LocalDate

class VerificationStatusController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents
) extends FrontendBaseController
    with I18nSupport
    with Logging {

  private def hasVerifiedSubcontractors(implicit request: models.requests.DataRequest[?]): Boolean =
    request.userAnswers
      .get(NewestVerificationBatchResponsePage)
      .exists(_.subcontractors.exists(_.verified.contains("Y")))

  def goToReverificationDecision(mode: Mode = NormalMode): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      if (hasVerifiedSubcontractors) {
        Redirect(controllers.verify.routes.ReverifyExistingSubcontractorsYesNoController.onPageLoad(mode))
      } else {
        Redirect(controllers.verify.routes.ContractorEmailConfirmationStoredController.onPageLoad(mode))
      }
    }

  def goToSelectSubcontractorsToReverify(mode: Mode = NormalMode): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>

      // TEMP DEBUG LOGGING (remove later)
      val today = LocalDate.now

      def isBetweenInclusive(d: LocalDate, start: LocalDate, end: LocalDate): Boolean =
        !d.isBefore(start) && !d.isAfter(end)

      request.userAnswers.get(NewestVerificationBatchResponsePage).foreach { newest =>
        val start = ReverificationRules.startDate(today)

        newest.subcontractors.filter(_.verified.contains("Y")).foreach { sub =>
          val required = ReverificationRules.reverifyRequired(sub, today)

          val betweenVerificationDate =
            sub.verificationDate
              .map(_.toLocalDate)
              .exists(d => ReverificationRules.isBetweenInclusive(d, start, today))

          val betweenLastMonthlyReturnDate =
            sub.lastMonthlyReturnDate
              .map(_.toLocalDate)
              .exists(d => isBetweenInclusive(d, start, today))

          logger.info(
            s"[VerificationStatusController.goToSelectSubcontractorsToReverify] \n" +
              s"subcontractorId=${sub.subcontractorId}, \n" +
              s"Start Date = $start \n" +
              s"Verification Date = ${sub.verificationDate.map(_.toLocalDate)} \n" +
              s"Last monthly Return Date = ${sub.lastMonthlyReturnDate.map(_.toLocalDate)} \n" +
              s"Verification date between Today and Start date = $betweenVerificationDate, \n" +
              s"Last Monthly return date is between Today and Start date = $betweenLastMonthlyReturnDate \n" +
              s"Reverification required = $required"
          )
        }
      }
      // END TEMP DEBUG LOGGING

      if (hasVerifiedSubcontractors) {
        Redirect(controllers.verify.routes.SelectSubcontractorsToReverifyController.onPageLoad(mode))
      } else {
        Redirect(controllers.verify.routes.ContractorEmailConfirmationStoredController.onPageLoad(mode))
      }
    }
}
