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
import models.contact.ContactOptions
import models.contact.ContactOptions.*
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
    val currentUsesTrading = current.get(SubTradingNameYesNoPage).contains(true)

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
      wasAmended(current, TradingNameOfSubcontractorPage) ||
        wasAmended(current, SubcontractorNamePage)
    ) {
      row(
        label =
          if (currentUsesTradingName.contains(true))
            messages("tradingNameOfSubcontractor.checkYourAnswersLabel")
          else
            messages("subcontractorName.checkYourAnswersLabel"),
        previous = originalNameDisplay(original).getOrElse(missingValue),
        updated =
          currentNameDisplay(
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
      current.get(SubTradingNameYesNoPage),
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
      current.get(SubAddressYesNoPage),
      current
    )

  private def addressRow(
    original: OriginalIndividualAnswers,
    current: UserAnswers
  )(implicit messages: Messages): Option[Seq[TableRow]] =
    val currentAddress = current.get(AddressOfSubcontractorPage)
    Option.when(
      wasAmended(current, AddressOfSubcontractorPage) ||
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
  )(implicit messages: Messages): Seq[Seq[TableRow]] =
    Seq(
      contactMethodRow(original, current),
      contactValueRows(original, current)
    ).flatten

  private def contactMethodRow(
    original: OriginalIndividualAnswers,
    current: UserAnswers
  )(implicit messages: Messages): Option[Seq[TableRow]] =
    Option.when(wasAmended(current, IndividualChooseContactDetailsPage)) {
      row(
        label = messages("individualChooseContactDetails.checkYourAnswersLabel"),
        previous = displayContactMethod(original.contactMethod),
        updated = displayContactMethod(current.get(IndividualChooseContactDetailsPage))
      )
    }

  private def contactValueRows(
                                original: OriginalIndividualAnswers,
                                current: UserAnswers
                              )(implicit messages: Messages): Seq[Seq[TableRow]] = {

    val originalMethod = original.contactMethod
    val currentMethod  = current.get(IndividualChooseContactDetailsPage)

    val currentValue = contactValueFromAnswers(currentMethod, current)

    (originalMethod, currentMethod) match {
      case (Some(oldMethod), Some(newMethod)) if oldMethod == newMethod && oldMethod != NoDetails =>

        contactDetails(oldMethod).toSeq.flatMap { case (label, page) =>
          Option.when(
            wasAmended(current, IndividualChooseContactDetailsPage) ||
              wasAmended(current, page) ||
              original.contactValue != currentValue
          ) {
            row(
              label,
              original.contactValue.getOrElse(missingValue),
              currentValue.getOrElse(missingValue)
            )
          }
        }
      case _ =>
        Seq(
          originalMethod
            .filter(_ != NoDetails)
            .flatMap(contactDetails)
            .map { case (label, _) =>
              row(
                label,
                original.contactValue.getOrElse(missingValue),
                missingValue
              )
            },

          currentMethod
            .filter(_ != NoDetails)
            .flatMap(contactDetails)
            .map { case (label, _) =>
              row(
                label,
                missingValue,
                currentValue.getOrElse(missingValue)
              )
            }
        ).flatten
    }
  }

  private def contactDetails(
    method: ContactOptions
  )(implicit messages: Messages): Option[(String, QuestionPage[_])] =
    method match {
      case Email =>
        Some(
          messages("individualEmailAddress.checkYourAnswersLabel") ->
            IndividualEmailAddressPage
        )

      case Phone =>
        Some(
          messages("individualPhoneNumber.checkYourAnswersLabel") ->
            IndividualPhoneNumberPage
        )

      case Mobile =>
        Some(
          messages("individualMobileNumber.checkYourAnswersLabel") ->
            IndividualMobileNumberPage
        )

      case NoDetails =>
        None
    }

  private def worksReferenceRows(original: OriginalIndividualAnswers, current: UserAnswers)(implicit
    messages: Messages
  ): Seq[Seq[TableRow]] = {
    val currentWorksRefYesNo = current.get(WorksReferenceNumberYesNoPage)
    val currentWorksRef      = current.get(WorksReferenceNumberPage)

    Seq(
      yesNoRow(
        WorksReferenceNumberYesNoPage,
        messages("worksReferenceNumberYesNo.checkYourAnswersLabel"),
        original.worksReferenceYesNo,
        currentWorksRefYesNo,
        current
      ),
      fieldRow(
        WorksReferenceNumberPage,
        messages("worksReferenceNumber.checkYourAnswersLabel"),
        original.worksReference,
        currentWorksRef,
        missingValue,
        current
      )
    ).flatten
  }

  private def utrRows(original: OriginalIndividualAnswers, current: UserAnswers)(implicit
    messages: Messages
  ): Seq[Seq[TableRow]] = {
    val currentUtrYesNo = current.get(UniqueTaxpayerReferenceYesNoPage)
    val currentUtr      = current.get(SubcontractorsUniqueTaxpayerReferencePage)
    Seq(
      yesNoRow(
        UniqueTaxpayerReferenceYesNoPage,
        messages("uniqueTaxpayerReferenceYesNo.checkYourAnswersLabel"),
        original.utrYesNo,
        currentUtrYesNo,
        current
      ),
      fieldRow(
        SubcontractorsUniqueTaxpayerReferencePage,
        messages("subcontractorsUniqueTaxpayerReference.checkYourAnswersLabel"),
        original.utr,
        currentUtr,
        missingValue,
        current
      )
    ).flatten
  }

  private def ninoRows(original: OriginalIndividualAnswers, current: UserAnswers)(implicit
    messages: Messages
  ): Seq[Seq[TableRow]] = {
    val currentNinoYesNo = current.get(NationalInsuranceNumberYesNoPage)
    val currentNino      = current.get(SubNationalInsuranceNumberPage)
    Seq(
      yesNoRow(
        NationalInsuranceNumberYesNoPage,
        messages("nationalInsuranceNumberYesNo.checkYourAnswersLabel"),
        original.ninoYesNo,
        currentNinoYesNo,
        current
      ),
      fieldRow(
        SubNationalInsuranceNumberPage,
        messages("subNationalInsuranceNumber.checkYourAnswersLabel"),
        original.nino,
        currentNino,
        missingValue,
        current
      )
    ).flatten
  }

  private def contactValueFromAnswers(method: Option[ContactOptions], ua: UserAnswers): Option[String] =
    method match {
      case Some(Email)  => ua.get(IndividualEmailAddressPage)
      case Some(Phone)  => ua.get(IndividualPhoneNumberPage)
      case Some(Mobile) => ua.get(IndividualMobileNumberPage)
      case _            => None
    }

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

  private def displayContactMethod(method: Option[ContactOptions])(implicit messages: Messages): String =
    method match {
      case Some(Email)     => messages("individualChooseContactDetails.email")
      case Some(Phone)     => messages("individualChooseContactDetails.phone")
      case Some(Mobile)    => messages("individualChooseContactDetails.mobile")
      case Some(NoDetails) => messages("individualChooseContactDetails.noDetails")
      case None            => messages("site.none")
    }

  private def yesNoRow(
    page: QuestionPage[_],
    label: String,
    original: Option[Boolean],
    currentVal: Option[Boolean],
    current: UserAnswers
  )(implicit messages: Messages): Option[Seq[TableRow]] =
    Option.when(
      wasAmended(current, page) || original != currentVal
    ) {
      row(label, displayYesNo(original), displayYesNo(currentVal))
    }

  private def displayYesNo(answer: Option[Boolean])(implicit messages: Messages): String =
    answer match {
      case Some(true)  => messages("site.yes")
      case Some(false) => messages("site.no")
      case None        => missingValue
    }

  private def fieldRow(
    page: QuestionPage[_],
    label: String,
    original: Option[String],
    currentVal: Option[String],
    missing: String,
    current: UserAnswers
  ): Option[Seq[TableRow]] =
    Option.when(
      wasAmended(current, page) || original != currentVal
    ) {
      row(label, original.getOrElse(missing), currentVal.getOrElse(missing))
    }

  private def row(label: String, previous: String, updated: String): Seq[TableRow] =
    Seq(TableRow(content = Text(label), classes = "govuk-!-font-weight-bold"), TableRow(Text(previous)), TableRow(Text(updated)))

  private def missingValue(implicit messages: Messages): String =
    messages("individualAmended.table.content.none")

  private def wasAmended(
    current: UserAnswers,
    page: QuestionPage[_]
  ): Boolean =
    current
      .get(AmendedPagesPage)
      .getOrElse(Set.empty)
      .contains(page.toString)
}
