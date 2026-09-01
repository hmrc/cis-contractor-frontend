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

package controllers.info

import controllers.actions.*
import controllers.routes
import models.TypeOfSubcontractor
import models.info.{CheckYourAnswersValidation, IndividualAnswers}
import play.api.Logging
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import queries.info.IndividualAnswersQuery
import uk.gov.hmrc.govukfrontend.views.Aliases.{Key, Text, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.add.*
import viewmodels.govuk.summarylist.*
import views.html.info.CheckYourAnswersView

import javax.inject.Inject

class IndividualCheckYourAnswersController @Inject() (
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
      request.userAnswers.get(IndividualAnswersQuery) match {

        case Some(answers) if CheckYourAnswersValidation.isValid(answers) =>
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
              displayName(answers)
            )
          )

        case Some(_) | None =>
          logger.error(
            "[IndividualCheckYourAnswersController.onPageLoad] " +
              "IndividualAnswersQuery is missing or invalid"
          )

          Redirect(
            routes.JourneyRecoveryController.onPageLoad()
          )
      }
    }

  private def subcontractorInformationRows(
    answers: IndividualAnswers
  )(implicit messages: Messages): Seq[Option[SummaryListRow]] = {

    val verificationRows =
      if (answers.showVerificationDetails) {
        Seq(
          SubcontractorsUniqueTaxpayerReferenceSummary.row(answers),
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
      TypeOfSubcontractorSummary.row(
        TypeOfSubcontractor.Individualorsoletrader
      )
    ) ++ verificationRows
  }

  private def detailsRows(
    answers: IndividualAnswers
  )(implicit messages: Messages): Seq[Option[SummaryListRow]] = {

    val nameRows =
      if (!answers.showVerificationDetails) {
        Seq(
          SubTradingNameYesNoSummary.row(answers),
          SubcontractorNameSummary.row(answers),
          TradingNameOfSubcontractorSummary.row(answers)
        )
      } else {
        Nil
      }

    val addressRows =
      Seq(
        SubAddressYesNoSummary.row(answers),
        AddressOfSubcontractorSummary.row(answers)
      )

    val utrRows =
      if (!answers.showVerificationDetails) {
        Seq(
          UniqueTaxpayerReferenceYesNoSummary.row(answers),
          SubcontractorsUniqueTaxpayerReferenceSummary.row(answers)
        )
      } else {
        Nil
      }

    val contactRows =
      Seq(
        AddIndividualContactMethodsYesNoSummary.row(answers),
        IndividualContactMethodOptionsSummary.row(answers),
        IndividualEmailAddressSummary.row(answers),
        IndividualPhoneNumberSummary.row(answers),
        IndividualMobileNumberSummary.row(answers)
      )

    val additionalRows =
      Seq(
        NationalInsuranceNumberYesNoSummary.row(answers),
        SubNationalInsuranceNumberSummary.row(answers),
        WorksReferenceNumberYesNoSummary.row(answers),
        WorksReferenceNumberSummary.row(answers)
      )

    nameRows ++
      addressRows ++
      contactRows ++
      utrRows ++
      additionalRows
  }

  private def displayName(
    answers: IndividualAnswers
  ): String =
    answers.subcontractorName
      .map { name =>
        Seq(
          Some(name.firstName),
          name.middleName,
          Some(name.lastName)
        ).flatten.mkString(" ")
      }
      .orElse(answers.tradingName)
      .getOrElse("")
}
