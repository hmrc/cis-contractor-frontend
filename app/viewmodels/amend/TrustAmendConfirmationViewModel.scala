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
import models.add.trust.TrustContactMethodOptions
import models.address.Address
import models.amend.OriginalIndividualAnswers
import models.amend.trust.OriginalTrustAnswers
import models.contact.{ContactMethodOptions, ContactOptions}
import models.contact.ContactOptions.*
import pages.QuestionPage
import pages.add.*
import pages.add.trust.{AddTrustContactMethodsYesNoPage, TrustAddressPage, TrustAddressYesNoPage, TrustContactMethodOptionsPage, TrustEmailAddressPage, TrustMobileNumberPage, TrustPhoneNumberPage, TrustUtrPage, TrustUtrYesNoPage, TrustWorksReferencePage, TrustWorksReferenceYesNoPage}
import pages.amend.AmendedPagesPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.TableRow

object TrustAmendConfirmationViewModel {

  def rows(
    original: OriginalTrustAnswers,
    current: UserAnswers
  )(implicit messages: Messages): Seq[Seq[TableRow]] =
    addressRows(original, current) ++
      contactRows(original, current) ++
      utrRows(original, current) ++
      worksReferenceRows(original, current)

  private def addressRows(
    original: OriginalTrustAnswers,
    current: UserAnswers
  )(implicit messages: Messages): Seq[Seq[TableRow]] =
    Seq(
      addressYesNoRow(original, current),
      addressRow(original, current)
    ).flatten

  private def addressYesNoRow(
    original: OriginalTrustAnswers,
    current: UserAnswers
  )(implicit messages: Messages): Option[Seq[TableRow]] =
    yesNoRow(
      TrustAddressYesNoPage,
      messages("trustAddressYesNo.checkYourAnswersLabel"),
      original.addressYesNo,
      current.get(TrustAddressYesNoPage),
      current
    )

  private def addressRow(
    original: OriginalTrustAnswers,
    current: UserAnswers
  )(implicit messages: Messages): Option[Seq[TableRow]] =
    val currentAddress = current.get(TrustAddressPage)
    Option.when(
      wasAmended(current, TrustAddressPage) ||
        original.address != currentAddress
    ) {
      row(
        messages("trustAddress.checkYourAnswersLabel"),
        original.address.map(formatAddress).getOrElse(missingValue),
        currentAddress.map(formatAddress).getOrElse(missingValue)
      )
    }

  private def contactRows(
    original: OriginalTrustAnswers,
    current: UserAnswers
  )(implicit messages: Messages): Seq[Seq[TableRow]] = {

    val currentContactYesNo = current.get(AddTrustContactMethodsYesNoPage)
    val currentMethods      = current.get(TrustContactMethodOptionsPage).getOrElse(Set.empty)
    val currentEmail        = current.get(TrustEmailAddressPage)
    val currentPhone        = current.get(TrustPhoneNumberPage)
    val currentMobile       = current.get(TrustMobileNumberPage)

    Seq(
      yesNoRow(
        AddTrustContactMethodsYesNoPage,
        messages("addTrustContactMethodsYesNo.checkYourAnswersLabel"),
        original.trustContactMethodsYesNo,
        currentContactYesNo,
        current
      ),
      Option.when(
        wasAmended(current, TrustContactMethodOptionsPage) ||
          original.trustContactMethod != currentMethods
      ) {
        row(
          messages("trustContactMethodOptions.checkYourAnswersLabel"),
          formatContactMethods(original.trustContactMethod),
          formatContactMethods(currentMethods)
        )
      },
      fieldRow(
        TrustEmailAddressPage,
        messages("trustEmail.checkYourAnswersLabel"),
        original.email,
        currentEmail,
        missingValue,
        current
      ),
      fieldRow(
        TrustPhoneNumberPage,
        messages("trustPhone.checkYourAnswersLabel"),
        original.phone,
        currentPhone,
        missingValue,
        current
      ),
      fieldRow(
        TrustMobileNumberPage,
        messages("trustMobile.checkYourAnswersLabel"),
        original.mobile,
        currentMobile,
        missingValue,
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

  private def worksReferenceRows(original: OriginalTrustAnswers, current: UserAnswers)(implicit
    messages: Messages
  ): Seq[Seq[TableRow]] = {
    val currentWorksRefYesNo = current.get(TrustWorksReferenceYesNoPage)
    val currentWorksRef      = current.get(TrustWorksReferencePage)

    Seq(
      yesNoRow(
        TrustWorksReferenceYesNoPage,
        messages("trustWorksReferenceYesNo.checkYourAnswersLabel"),
        original.worksReferenceYesNo,
        currentWorksRefYesNo,
        current
      ),
      fieldRow(
        TrustWorksReferencePage,
        messages("trustWorksReference.checkYourAnswersLabel"),
        original.worksReference,
        currentWorksRef,
        missingValue,
        current
      )
    ).flatten
  }

  private def utrRows(original: OriginalTrustAnswers, current: UserAnswers)(implicit
    messages: Messages
  ): Seq[Seq[TableRow]] = {
    val currentUtrYesNo = current.get(TrustUtrYesNoPage)
    val currentUtr      = current.get(TrustUtrPage)
    Seq(
      yesNoRow(
        TrustUtrYesNoPage,
        messages("trustUtrYesNo.checkYourAnswersLabel"),
        original.utrYesNo,
        currentUtrYesNo,
        current
      ),
      fieldRow(
        TrustUtrPage,
        messages("trustUtr.checkYourAnswersLabel"),
        original.utr,
        currentUtr,
        missingValue,
        current
      )
    ).flatten
  }

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
    Seq(
      TableRow(content = Text(label), classes = "govuk-!-font-weight-bold"),
      TableRow(Text(previous)),
      TableRow(Text(updated))
    )

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
