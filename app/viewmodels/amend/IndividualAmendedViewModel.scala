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
import pages.add.*
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.TableRow

object IndividualAmendedViewModel {

  def rows(original: OriginalIndividualAnswers, current: UserAnswers)(implicit
    messages: Messages
  ): Seq[Seq[TableRow]] = {
    val currentUsesTradingName = current.get(SubTradingNameYesNoPage)
    val currentTradingName     = current.get(TradingNameOfSubcontractorPage)
    val currentName            = current.get(SubcontractorNamePage)
    val currentAddress         = current.get(AddressOfSubcontractorPage)
    val currentContactMethod   = current.get(IndividualChooseContactDetailsPage)
    val currentContactValue    = contactValueFromAnswers(currentContactMethod, current)
    val currentUtrYesNo        = current.get(UniqueTaxpayerReferenceYesNoPage)
    val currentUtr             = current.get(SubcontractorsUniqueTaxpayerReferencePage)
    val currentNinoYesNo       = current.get(NationalInsuranceNumberYesNoPage)
    val currentNino            = current.get(SubNationalInsuranceNumberPage)
    val currentWorksRefYesNo   = current.get(WorksReferenceNumberYesNoPage)
    val currentWorksRef        = current.get(WorksReferenceNumberPage)

    Seq(
      nameRow(original, currentUsesTradingName, currentTradingName, currentName),
      addressRow(original.address, currentAddress),
      contactMethodRow(original.contactMethod, currentContactMethod),
      contactValueRow(original.contactMethod, original.contactValue, currentContactMethod, currentContactValue),
      yesNoRow(
        messages("uniqueTaxpayerReferenceYesNo.checkYourAnswersLabel"),
        original.utrYesNo,
        currentUtrYesNo
      ),
      fieldRow(
        messages("subcontractorsUniqueTaxpayerReference.checkYourAnswersLabel"),
        original.utr,
        currentUtr,
        missing = none
      ),
      yesNoRow(
        messages("nationalInsuranceNumberYesNo.checkYourAnswersLabel"),
        original.ninoYesNo,
        currentNinoYesNo
      ),
      fieldRow(
        messages("subNationalInsuranceNumber.checkYourAnswersLabel"),
        original.nino,
        currentNino,
        missing = none
      ),
      yesNoRow(
        messages("worksReferenceNumberYesNo.checkYourAnswersLabel"),
        original.worksReferenceYesNo,
        currentWorksRefYesNo
      ),
      fieldRow(
        messages("worksReferenceNumber.checkYourAnswersLabel"),
        original.worksReference,
        currentWorksRef,
        missing = none
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

  private def nameRow(
                       original: OriginalIndividualAnswers,
                       currentUsesTradingName: Option[Boolean],
                       currentTradingName: Option[String],
                       currentName: Option[SubcontractorName]
                     )(implicit messages: Messages): Option[Seq[TableRow]] = {

    val originalDisplay = originalNameDisplay(original)
    val currentDisplay  = currentNameDisplay(currentUsesTradingName, currentTradingName, currentName)

    val label =
      messages(
        if (currentUsesTradingName.contains(true))
          "tradingNameOfSubcontractor.checkYourAnswersLabel"
        else
          "subcontractorName.checkYourAnswersLabel"
      )

    Option.when(originalDisplay != currentDisplay) {
      row(
        label,
        originalDisplay.getOrElse(none),
        currentDisplay.getOrElse(none)
      )
    }
  }

  private def originalNameDisplay(original: OriginalIndividualAnswers): Option[String] =
    original.usesTradingName match {
      case Some(true) => original.tradingName
      case _          => original.subcontractorName.map(formatName)
    }

  private def currentNameDisplay(
    usesTradingName: Option[Boolean],
    tradingName: Option[String],
    name: Option[SubcontractorName]
  ): Option[String] =
    usesTradingName match {
      case Some(true) => tradingName
      case _          => name.map(formatName)
    }

  private def formatName(n: SubcontractorName): String =
    Seq(Some(n.firstName), n.middleName, Some(n.lastName)).flatten.mkString(" ")

  private def addressRow(
    originalAddress: Option[Address],
    currentAddress: Option[Address]
  )(implicit messages: Messages): Option[Seq[TableRow]] =
    Option.when(originalAddress != currentAddress) {
      row(
        messages("addressOfSubcontractor.checkYourAnswersLabel"),
        originalAddress.map(formatAddress).getOrElse(none),
        currentAddress.map(formatAddress).getOrElse(none)
      )
    }

  private def formatAddress(a: Address): String =
    Address.normalisedSeq(a).mkString(", ")

  private def contactMethodRow(
    originalMethod: Option[ContactOptions],
    currentMethod: Option[ContactOptions]
  )(implicit messages: Messages): Option[Seq[TableRow]] =
    Option.when(originalMethod != currentMethod) {
      row(
        messages("individualChooseContactDetails.checkYourAnswersLabel"),
        displayContactMethod(originalMethod),
        displayContactMethod(currentMethod)
      )
    }

  private def displayContactMethod(method: Option[ContactOptions])(implicit messages: Messages): String =
    method match {
      case Some(Email)  => messages("individualChooseContactDetails.email")
      case Some(Phone)  => messages("individualChooseContactDetails.phone")
      case Some(Mobile) => messages("individualChooseContactDetails.mobile")
      case _            => messages("site.none")
    }

  private def contactValueRow(
    originalMethod: Option[ContactOptions],
    originalValue: Option[String],
    currentMethod: Option[ContactOptions],
    currentValue: Option[String]
  )(implicit messages: Messages): Option[Seq[TableRow]] = {
    val effectiveMethod = currentMethod.orElse(originalMethod)
    effectiveMethod match {
      case Some(Email) | Some(Phone) | Some(Mobile) if originalValue != currentValue =>
        val label = effectiveMethod match {
          case Some(Email)  => messages("individualEmailAddress.checkYourAnswersLabel")
          case Some(Phone)  => messages("individualPhoneNumber.checkYourAnswersLabel")
          case Some(Mobile) => messages("individualMobileNumber.checkYourAnswersLabel")
          case _            => ""
        }
        Some(row(label, originalValue.getOrElse("—"), currentValue.getOrElse("—")))
      case _                                                                         => None
    }
  }

  private def yesNoRow(
    label: String,
    original: Option[Boolean],
    current: Option[Boolean]
  )(implicit messages: Messages): Option[Seq[TableRow]] =
    Option.when(original != current) {
      row(label, displayYesNo(original), displayYesNo(current))
    }

  private def displayYesNo(answer: Option[Boolean])(implicit messages: Messages): String =
    answer match {
      case Some(true)  => messages("site.yes")
      case Some(false) => messages("site.no")
      case None        => messages("individualAmended.table.content.none")
    }

  private def fieldRow(
    label: String,
    original: Option[String],
    current: Option[String],
    missing: String
  ): Option[Seq[TableRow]] =
    Option.when(original != current) {
      row(label, original.getOrElse(missing), current.getOrElse(missing))
    }

  private def row(label: String, previous: String, updated: String): Seq[TableRow] =
    Seq(TableRow(Text(label)), TableRow(Text(previous)), TableRow(Text(updated)))

  private def none(implicit messages: Messages): String =
    messages("individualAmended.table.content.none")
}
