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

package controllers.info.partnership

import controllers.actions.*
import controllers.routes
import models.TypeOfSubcontractor
import models.info.partnership.PartnershipAnswers
import play.api.Logging
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import queries.info.PartnershipAnswersQuery
import uk.gov.hmrc.govukfrontend.views.Aliases.{Key, Text, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.add.TypeOfSubcontractorSummary
import viewmodels.checkAnswers.add.partnership.*
import viewmodels.govuk.summarylist.*
import views.html.info.CheckYourAnswersView

import javax.inject.Inject

class PartnershipCheckYourAnswersController @Inject() (
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view: CheckYourAnswersView
) extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      request.userAnswers.get(PartnershipAnswersQuery) match {

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
              answers.partnershipName.getOrElse("")
            )
          )

        case None =>
          logger.error(
            "[PartnershipCheckYourAnswersController.onPageLoad] " +
              "PartnershipAnswersQuery is missing"
          )

          Redirect(
            routes.JourneyRecoveryController.onPageLoad()
          )
      }
    }

  private def subcontractorInformationRows(
    answers: PartnershipAnswers
  )(implicit messages: Messages): Seq[Option[SummaryListRow]] = {

    val verificationRows =
      if (answers.showVerificationDetails) {
        Seq(
          PartnershipUniqueTaxpayerReferenceSummary.row(answers, true),
          PartnershipNominatedPartnerUtrSummary.row(answers, true),
          answers.verificationNumber.map { verificationNumber =>
            SummaryListRowViewModel(
              key = Key(
                Text(
                  messages("amendCheckYourAnswers.verificationNumber.label")
                )
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
      TypeOfSubcontractorSummary.row(TypeOfSubcontractor.Partnership)
    ) ++ verificationRows
  }

  private def detailsRows(
    answers: PartnershipAnswers
  )(implicit messages: Messages): Seq[Option[SummaryListRow]] = {

    val nameRows =
      if (answers.showVerificationDetails) {
        Nil
      } else {
        Seq(
          PartnershipNameSummary.row(answers)
        )
      }

    val utrRows =
      if (answers.showVerificationDetails) {
        Seq(
          PartnershipNominatedPartnerNameSummary.row(answers)
        )
      } else {
        Seq(
          PartnershipHasUtrYesNoSummary.row(answers),
          PartnershipUniqueTaxpayerReferenceSummary.row(answers, false),
          PartnershipNominatedPartnerNameSummary.row(answers),
          PartnershipNominatedPartnerUtrYesNoSummary.row(answers),
          PartnershipNominatedPartnerUtrSummary.row(answers, false)
        )
      }

    nameRows ++
      Seq(
        PartnershipAddressYesNoSummary.row(answers),
        PartnershipAddressSummary.row(answers),
        AddPartnershipContactMethodsYesNoSummary.row(answers),
        PartnershipContactMethodOptionsSummary.row(answers),
        PartnershipEmailAddressSummary.row(answers),
        PartnershipPhoneNumberSummary.row(answers),
        PartnershipMobileNumberSummary.row(answers)
      ) ++
      utrRows ++
      Seq(
        PartnershipNominatedPartnerNinoYesNoSummary.row(answers),
        PartnershipNominatedPartnerNinoSummary.row(answers),
        PartnershipNominatedPartnerCrnYesNoSummary.row(answers),
        PartnershipNominatedPartnerCrnSummary.row(answers),
        PartnershipWorksReferenceNumberYesNoSummary.row(answers),
        PartnershipWorksReferenceNumberSummary.row(answers)
      )
  }
}
