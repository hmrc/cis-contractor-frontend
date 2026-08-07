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

package viewmodels.amend

import models.UserAnswers
import models.add.SubcontractorName
import models.address.Address
import models.amend.OriginalIndividualAnswers
import models.contact.ContactMethodOptions
import pages.QuestionPage
import pages.add.*
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.TableRow

object IndividualAmendedViewModel {

  def rows(
    original: OriginalIndividualAnswers,
    current: UserAnswers
  )(implicit messages: Messages): Seq[Seq[TableRow]] =
    tradingNameRows(original, current) ++
      addressRows(original, current) ++
      contactRows(original, current) ++
      utrRows(original, current) ++
      ninoRows(original, current) ++
      worksReferenceRows(original, current)

  private def tradingNameRows(
    original: OriginalIndividualAnswers,
    current: UserAnswers
  )(implicit messages: Messages): Seq[Seq[TableRow]] = {

    val originalUsesTrading = original.usesTradingName.contains(true)
    val currentUsesTrading  = current.get(SubTradingNameYesNoPage).contains(true)

    val yesNoRows =
      Seq(
        tradingNameYesNoRow(original, current)
      ).flatten

    val nameRows =
      if (originalUsesTrading == currentUsesTrading) {
        Seq(nameRow(original, current)).flatten
      } else if (originalUsesTrading) {
        Seq(
          row(
            messages("tradingNameOfSubcontractor.checkYourAnswersLabel"),
            original.tradingName.getOrElse(missingValue),
            missingValue
          ),
          row(
            messages("subcontractorName.checkYourAnswersLabel"),
            missingValue,
            current.get(SubcontractorNamePage).map(formatName).getOrElse(missingValue)
          )
        )
      } else {
        Seq(
          row(
            messages("subcontractorName.checkYourAnswersLabel"),
            original.subcontractorName.map(formatName).getOrElse(missingValue),
            missingValue
          ),
          row(
            messages("tradingNameOfSubcontractor.checkYourAnswersLabel"),
            missingValue,
            current.get(TradingNameOfSubcontractorPage).getOrElse(missingValue)
          )
        )
      }

    yesNoRows ++ nameRows
  }

  private def nameRow(
    original: OriginalIndividualAnswers,
    current: UserAnswers
  )(implicit messages: Messages): Option[Seq[TableRow]] = {

    val currentUsesTradingName = current.get(SubTradingNameYesNoPage)
    val currentTradingName     = current.get(TradingNameOfSubcontractorPage)
    val currentName            = current.get(SubcontractorNamePage)

    Option.when(
      originalNameDisplay(original) != currentNameDisplay(
        currentUsesTradingName,
        currentTradingName,
        currentName
      )
    ) {
      row(
        label =
          if (currentUsesTradingName.contains(true))
            messages("tradingNameOfSubcontractor.checkYourAnswersLabel")
          else
            messages("subcontractorName.checkYourAnswersLabel"),
        previous = originalNameDisplay(original).getOrElse(missingValue),
        updated = currentNameDisplay(
          currentUsesTradingName,
          currentTradingName,
          currentName
        ).getOrElse(missingValue)
      )
    }
  }

  private def tradingNameYesNoRow(
    original: OriginalIndividualAnswers,
    current: UserAnswers
  )(implicit messages: Messages): Option[Seq[TableRow]] =
    yesNoRow(
      SubTradingNameYesNoPage,
      messages("subTradingNameYesNo.checkYourAnswersLabel"),
      original.usesTradingName,
      current
    )

  private def addressRows(
    original: OriginalIndividualAnswers,
    current: UserAnswers
  )(implicit messages: Messages): Seq[Seq[TableRow]] =
    Seq(
      addressYesNoRow(original, current),
      addressRow(original, current)
    ).flatten

  private def addressYesNoRow(
    original: OriginalIndividualAnswers,
    current: UserAnswers
  )(implicit messages: Messages): Option[Seq[TableRow]] =
    yesNoRow(
      SubAddressYesNoPage,
      messages("subAddressYesNo.checkYourAnswersLabel"),
      original.addressYesNo,
      current
    )

  private def addressRow(
    original: OriginalIndividualAnswers,
    current: UserAnswers
  )(implicit messages: Messages): Option[Seq[TableRow]] =
    val currentAddress = current.get(AddressOfSubcontractorPage)
    Option.when(
      original.address != currentAddress
    ) {
      row(
        messages("addressOfSubcontractor.checkYourAnswersLabel"),
        original.address.map(formatAddress).getOrElse(missingValue),
        currentAddress.map(formatAddress).getOrElse(missingValue)
      )
    }

  private def contactRows(
    original: OriginalIndividualAnswers,
    current: UserAnswers
  )(implicit messages: Messages): Seq[Seq[TableRow]] = {
    val currentMethods = current.get(IndividualContactMethodOptionsPage).getOrElse(Set.empty)

    Seq(
      yesNoRow(
        AddIndividualContactMethodsYesNoPage,
        messages("addIndividualContactMethodsYesNo.checkYourAnswersLabel"),
        original.individualContactMethodsYesNo,
        current
      ),
      Option.when(
        original.individualContactMethod != currentMethods
      ) {
        row(
          messages("individualContactMethodOptions.checkYourAnswersLabel"),
          formatContactMethods(original.individualContactMethod),
          formatContactMethods(currentMethods)
        )
      },
      fieldRow(
        IndividualEmailAddressPage,
        messages("individualEmailAddress.checkYourAnswersLabel"),
        original.email,
        current
      ),
      fieldRow(
        IndividualPhoneNumberPage,
        messages("individualPhoneNumber.checkYourAnswersLabel"),
        original.phone,
        current
      ),
      fieldRow(
        IndividualMobileNumberPage,
        messages("individualMobileNumber.checkYourAnswersLabel"),
        original.mobile,
        current
      )
    ).flatten
  }

  private def formatContactMethods(
    methods: Set[ContactMethodOptions]
  )(implicit messages: Messages): String =
    if (methods.isEmpty) {
      missingValue
    } else {
      methods.toSeq
        .sortBy(_.toString)
        .map {
          case ContactMethodOptions.Email  =>
            messages("trustContactMethodOptions.email")
          case ContactMethodOptions.Phone  =>
            messages("trustContactMethodOptions.phone")
          case ContactMethodOptions.Mobile =>
            messages("trustContactMethodOptions.mobile")
        }
        .mkString(", ")
    }

  private def worksReferenceRows(original: OriginalIndividualAnswers, current: UserAnswers)(implicit
    messages: Messages
  ): Seq[Seq[TableRow]] =
    Seq(
      yesNoRow(
        WorksReferenceNumberYesNoPage,
        messages("worksReferenceNumberYesNo.checkYourAnswersLabel"),
        original.worksReferenceYesNo,
        current
      ),
      fieldRow(
        WorksReferenceNumberPage,
        messages("worksReferenceNumber.checkYourAnswersLabel"),
        original.worksReference,
        current
      )
    ).flatten

  private def utrRows(original: OriginalIndividualAnswers, current: UserAnswers)(implicit
    messages: Messages
  ): Seq[Seq[TableRow]] =
    Seq(
      yesNoRow(
        UniqueTaxpayerReferenceYesNoPage,
        messages("uniqueTaxpayerReferenceYesNo.checkYourAnswersLabel"),
        original.utrYesNo,
        current
      ),
      fieldRow(
        SubcontractorsUniqueTaxpayerReferencePage,
        messages("subcontractorsUniqueTaxpayerReference.checkYourAnswersLabel"),
        original.utr,
        current
      )
    ).flatten

  private def ninoRows(original: OriginalIndividualAnswers, current: UserAnswers)(implicit
    messages: Messages
  ): Seq[Seq[TableRow]] =
    Seq(
      yesNoRow(
        NationalInsuranceNumberYesNoPage,
        messages("nationalInsuranceNumberYesNo.checkYourAnswersLabel"),
        original.ninoYesNo,
        current
      ),
      fieldRow(
        SubNationalInsuranceNumberPage,
        messages("subNationalInsuranceNumber.checkYourAnswersLabel"),
        original.nino,
        current
      )
    ).flatten

  private def originalNameDisplay(original: OriginalIndividualAnswers): Option[String] =
    if (original.usesTradingName.contains(true)) {
      original.tradingName
    } else {
      original.subcontractorName.map(formatName)
    }

  private def currentNameDisplay(
    currentUsesTradingName: Option[Boolean],
    currentTradingName: Option[String],
    currentName: Option[SubcontractorName]
  ): Option[String] =
    if (currentUsesTradingName.contains(true)) {
      currentTradingName
    } else {
      currentName.map(formatName)
    }

  private def formatName(n: SubcontractorName): String =
    Seq(Some(n.firstName), n.middleName, Some(n.lastName)).flatten.mkString(" ")

  private def formatAddress(a: Address): String =
    List(
      Some(a.addressLine1),
      a.addressLine2,
      a.addressLine3,
      a.addressLine4,
      a.addressLine5,
      a.postcode,
      a.country.flatMap(_.name)
    ).flatten.mkString(", ")

  private def yesNoRow(
    page: QuestionPage[Boolean],
    label: String,
    original: Option[Boolean],
    current: UserAnswers
  )(implicit messages: Messages): Option[Seq[TableRow]] = {

    val currentVal = current.get(page)

    Option.when(original != currentVal) {
      row(label, displayYesNo(original), displayYesNo(currentVal))
    }
  }

  private def displayYesNo(answer: Option[Boolean])(implicit messages: Messages): String =
    answer match {
      case Some(true)  => messages("site.yes")
      case Some(false) => messages("site.no")
      case None        => missingValue
    }

  private def fieldRow(
    page: QuestionPage[String],
    label: String,
    original: Option[String],
    current: UserAnswers
  )(implicit messages: Messages): Option[Seq[TableRow]] = {
    val currentVal = current.get(page)

    Option.when(original != currentVal) {
      row(
        label,
        original.getOrElse(missingValue),
        currentVal.getOrElse(missingValue)
      )
    }
  }

  private def row(label: String, previous: String, updated: String): Seq[TableRow] =
    Seq(
      TableRow(content = Text(label), classes = "govuk-!-font-weight-bold"),
      TableRow(Text(previous)),
      TableRow(Text(updated))
    )

  private def missingValue(implicit messages: Messages): String =
    messages("amendConfirmation.table.content.none")
}
