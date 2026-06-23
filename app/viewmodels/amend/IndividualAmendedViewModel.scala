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
import models.add.{InternationalAddress, SubcontractorName}
import models.amend.OriginalIndividualAnswers
import models.contact.ContactOptions
import models.contact.ContactOptions.*
import pages.add.*
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.TableRow

object IndividualAmendedViewModel {

  def rows(original: OriginalIndividualAnswers, current: UserAnswers)(implicit messages: Messages): Seq[Seq[TableRow]] = {
    val currentUsesTradingName = current.get(SubTradingNameYesNoPage)
    val currentTradingName     = current.get(TradingNameOfSubcontractorPage)
    val currentName            = current.get(SubcontractorNamePage)
    val currentAddress         = current.get(AddressOfSubcontractorPage)
    val currentContactMethod   = current.get(IndividualChooseContactDetailsPage)
    val currentContactValue    = contactValueFromAnswers(currentContactMethod, current)
    val currentUtr             = current.get(SubcontractorsUniqueTaxpayerReferencePage)
    val currentNino            = current.get(SubNationalInsuranceNumberPage)
    val currentWorksRef        = current.get(WorksReferenceNumberPage)

    Seq(
      nameRow(original, currentUsesTradingName, currentTradingName, currentName),
      addressRow(original.address, currentAddress),
      contactMethodRow(original.contactMethod, currentContactMethod),
      contactValueRow(original.contactMethod, original.contactValue, currentContactMethod, currentContactValue),
      fieldRow(
        messages("subcontractorsUniqueTaxpayerReference.checkYourAnswersLabel"),
        original.utr,
        currentUtr,
        missing = "—"
      ),
      fieldRow(
        messages("subNationalInsuranceNumber.checkYourAnswersLabel"),
        original.nino,
        currentNino,
        missing = "—"
      ),
      fieldRow(
        messages("worksReferenceNumber.checkYourAnswersLabel"),
        original.worksReference,
        currentWorksRef,
        missing = "—"
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
    if (originalDisplay == currentDisplay) None
    else {
      val label = if (currentUsesTradingName.contains(true))
        messages("tradingNameOfSubcontractor.checkYourAnswersLabel")
      else
        messages("subcontractorName.checkYourAnswersLabel")
      Some(row(label, originalDisplay.getOrElse("—"), currentDisplay.getOrElse("—")))
    }
  }

  private def originalNameDisplay(original: OriginalIndividualAnswers): Option[String] =
    if (original.usesTradingName.contains(true))
      original.tradingName
    else
      original.subcontractorName.map(formatName)

  private def currentNameDisplay(
    usesTradingName: Option[Boolean],
    tradingName: Option[String],
    name: Option[SubcontractorName]
  ): Option[String] =
    if (usesTradingName.contains(true)) tradingName
    else name.map(formatName)

  private def formatName(n: SubcontractorName): String =
    Seq(Some(n.firstName), n.middleName, Some(n.lastName)).flatten.mkString(" ")

  private def addressRow(
    originalAddress: Option[InternationalAddress],
    currentAddress: Option[InternationalAddress]
  )(implicit messages: Messages): Option[Seq[TableRow]] = {
    if (originalAddress == currentAddress) None
    else
      Some(row(
        messages("addressOfSubcontractor.checkYourAnswersLabel"),
        originalAddress.map(formatAddress).getOrElse(messages("site.none")),
        currentAddress.map(formatAddress).getOrElse(messages("site.none"))
      ))
  }

  private def formatAddress(a: InternationalAddress): String =
    Seq(
      Some(a.addressLine1),
      a.addressLine2,
      Some(a.addressLine3),
      a.addressLine4,
      Some(a.postalCode),
      Some(a.country)
    ).flatten.filter(_.trim.nonEmpty).mkString(", ")

  private def contactMethodRow(
    originalMethod: Option[ContactOptions],
    currentMethod: Option[ContactOptions]
  )(implicit messages: Messages): Option[Seq[TableRow]] = {
    if (originalMethod == currentMethod) None
    else
      Some(row(
        messages("individualChooseContactDetails.checkYourAnswersLabel"),
        displayContactMethod(originalMethod),
        displayContactMethod(currentMethod)
      ))
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
      case _ => None
    }
  }

  private def fieldRow(
    label: String,
    original: Option[String],
    current: Option[String],
    missing: String
  ): Option[Seq[TableRow]] =
    if (original == current) None
    else Some(row(label, original.getOrElse(missing), current.getOrElse(missing)))

  private def row(label: String, previous: String, updated: String): Seq[TableRow] =
    Seq(TableRow(Text(label)), TableRow(Text(previous)), TableRow(Text(updated)))
}
