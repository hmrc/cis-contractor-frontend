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

package controllers.viewOnly.trust

import controllers.actions.*
import controllers.routes
import models.TypeOfSubcontractor
import models.viewOnly.trust.ViewOnlyTrustAnswers
import play.api.Logging
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import queries.ViewOnlyTrustAnswersQuery
import uk.gov.hmrc.govukfrontend.views.Aliases.{Key, Text, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.add.TypeOfSubcontractorSummary
import viewmodels.checkAnswers.add.trust.*
import viewmodels.govuk.summarylist.*
import views.html.viewOnly.ViewOnlyCheckYourAnswersView

import javax.inject.Inject

class ViewOnlyTrustCheckYourAnswersController @Inject() (
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view: ViewOnlyCheckYourAnswersView
) extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      request.userAnswers.get(ViewOnlyTrustAnswersQuery) match {

        case Some(answers) =>
          val subcontractorInformationList =
            SummaryListViewModel(
              rows = subcontractorInformationRows(answers).flatten
            )

          val detailsList =
            SummaryListViewModel(
              rows = detailsRows(answers).flatten
            )

          Ok(
            view(
              subcontractorInformationList,
              detailsList,
              answers.trustName.getOrElse("")
            )
          )

        case None =>
          logger.error(
            "[ViewOnlyTrustCheckYourAnswersController.onPageLoad] " +
              "ViewOnlyTrustAnswersQuery is missing"
          )

          Redirect(
            routes.JourneyRecoveryController.onPageLoad()
          )
      }
    }

  private def subcontractorInformationRows(
    answers: ViewOnlyTrustAnswers
  )(implicit messages: Messages): Seq[Option[SummaryListRow]] = {

    val verificationRows =
      if (answers.showVerificationDetails) {
        Seq(
          TrustUtrSummary.row(answers, true),
          answers.verificationNumber.map { verificationNumber =>
            SummaryListRowViewModel(
              key = Key(
                Text(messages("amendCheckYourAnswers.verificationNumber.label"))
              ),
              value = Value(
                Text(verificationNumber)
              )
            )
          }
        )
      } else {
        Nil
      }

    Seq(
      TypeOfSubcontractorSummary.row(TypeOfSubcontractor.Trust)
    ) ++ verificationRows
  }

  private def detailsRows(
    answers: ViewOnlyTrustAnswers
  )(implicit messages: Messages): Seq[Option[SummaryListRow]] = {

    val nameRows =
      if (answers.showVerificationDetails) {
        Nil
      } else {
        Seq(
          TrustNameSummary.row(answers)
        )
      }

    val utrRows =
      if (answers.showVerificationDetails) {
        Nil
      } else {
        Seq(
          TrustUtrYesNoSummary.row(answers),
          TrustUtrSummary.row(answers, false)
        )
      }

    nameRows ++
      Seq(
        TrustAddressYesNoSummary.row(answers),
        TrustAddressSummary.row(answers),
        AddTrustContactMethodsYesNoSummary.row(answers),
        TrustContactMethodOptionsSummary.row(answers),
        TrustEmailAddressSummary.row(answers),
        TrustPhoneNumberSummary.row(answers),
        TrustMobileNumberSummary.row(answers)
      ) ++
      utrRows ++
      Seq(
        TrustWorksReferenceYesNoSummary.row(answers),
        TrustWorksReferenceSummary.row(answers)
      )
  }
}
