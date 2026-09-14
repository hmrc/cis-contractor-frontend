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
import models.add.{IndividualNamesOptions, SubcontractorName}
import models.address.Address
import models.amend.OriginalIndividualAnswers
import models.contact.ContactMethodOptions
import pages.QuestionPage
import pages.add.*
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.TableRow

object IndividualAmendedViewModel {

  def rows(
    original: OriginalIndividualAnswers,
    current: UserAnswers
  )(implicit messages: Messages): Seq[Seq[TableRow]] =
    namesRows(original, current) ++
      addressRows(original, current) ++
      contactRows(original, current) ++
      utrRows(original, current) ++
      ninoRows(original, current) ++
      worksReferenceRows(original, current)

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
      htmlRow(
        messages("addressOfSubcontractor.checkYourAnswersLabel"),
        original.address.map(formatAddress).getOrElse(missingValue),
        currentAddress.map(formatAddress).getOrElse(missingValue)
      )
    }

  private def namesRows(
    original: OriginalIndividualAnswers,
    current: UserAnswers
  )(implicit messages: Messages): Seq[Seq[TableRow]] = {
    val currentNamesOptions = current.get(IndividualNamesOptionsPage).getOrElse(Set.empty)
    Seq(
      Option.when(original.individualNamesOptions != currentNamesOptions) {
        htmlRow(
          messages("individualNamesOptions.checkYourAnswersLabel"),
          formatNameOptions(original.individualNamesOptions),
          formatNameOptions(currentNamesOptions)
        )
      },
      fieldNameRow(
        SubcontractorNamePage,
        messages("subcontractorName.checkYourAnswersLabel"),
        original.subcontractorName,
        current
      ),
      fieldRow(
        TradingNameOfSubcontractorPage,
        messages("tradingNameOfSubcontractor.checkYourAnswersLabel"),
        original.tradingName,
        current
      )
    ).flatten
  }

  private def formatNameOptions(
    namesOptions: Set[IndividualNamesOptions]
  )(implicit messages: Messages): String =
    if (namesOptions.isEmpty) {
      missingSelect
    } else {
      val individualNamesOptions = IndividualNamesOptions
        .ordered(namesOptions)
        .map {
          case IndividualNamesOptions.SubcontractorName =>
            messages("individualNamesOptions.subcontractorName")
          case IndividualNamesOptions.TradingName       =>
            messages("individualNamesOptions.tradingName")
        }

      if (individualNamesOptions.size > 1) {
        individualNamesOptions
          .map(item => s"<li>$item</li>")
          .mkString("<ul class=\"govuk-list govuk-list--bullet\">", "", "</ul>")
      } else {
        individualNamesOptions.mkString
      }
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
        htmlRow(
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
      missingSelect
    } else {
      val contactOptions = ContactMethodOptions
        .ordered(methods)
        .map {
          case ContactMethodOptions.Email  =>
            messages("individualContactMethodOptions.email")
          case ContactMethodOptions.Phone  =>
            messages("individualContactMethodOptions.phone")
          case ContactMethodOptions.Mobile =>
            messages("individualContactMethodOptions.mobile")
        }

      if (contactOptions.size > 1) {
        contactOptions
          .map(item => s"<li>$item</li>")
          .mkString("<ul class=\"govuk-list govuk-list--bullet\">", "", "</ul>")
      } else {
        contactOptions.mkString
      }

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
    ).flatten.mkString("</br>")

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
    current: UserAnswers,
    missingValue: Messages => String = missingValue
  )(implicit messages: Messages): Option[Seq[TableRow]] = {
    val currentVal = current.get(page)

    Option.when(original != currentVal) {
      row(
        label,
        original.getOrElse(missingValue(messages)),
        currentVal.getOrElse(missingValue(messages))
      )
    }
  }

  private def fieldNameRow(
    page: QuestionPage[SubcontractorName],
    label: String,
    original: Option[SubcontractorName],
    current: UserAnswers,
    missingValue: Messages => String = missingValue
  )(implicit messages: Messages): Option[Seq[TableRow]] = {
    val currentVal = current.get(page)

    Option.when(original != currentVal) {
      row(
        label,
        original.map(formatName).getOrElse(missingValue(messages)),
        currentVal.map(formatName).getOrElse(missingValue(messages))
      )
    }
  }

  private def row(label: String, previous: String, updated: String): Seq[TableRow] =
    Seq(
      TableRow(content = Text(label), classes = "govuk-!-font-weight-bold"),
      TableRow(Text(previous)),
      TableRow(Text(updated))
    )

  private def htmlRow(label: String, previous: String, updated: String): Seq[TableRow] =
    Seq(
      TableRow(content = HtmlContent(label), classes = "govuk-!-font-weight-bold"),
      TableRow(HtmlContent(previous)),
      TableRow(HtmlContent(updated))
    )

  private def missingValue(implicit messages: Messages): String =
    messages("amendConfirmation.table.content.none")

  private def missingSelect(implicit messages: Messages): String =
    messages("amendConfirmation.table.selectContent.none")
}
