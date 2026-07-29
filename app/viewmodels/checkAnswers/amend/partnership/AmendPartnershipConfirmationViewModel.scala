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

package viewmodels.checkAnswers.amend.partnership

import models.UserAnswers
import models.address.Address
import models.amend.partnership.OriginalPartnershipAnswers
import models.contact.ContactMethodOptions
import pages.QuestionPage
import pages.add.*
import pages.add.partnership.*
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.TableRow

object AmendPartnershipConfirmationViewModel {

  def rows(
            original: OriginalPartnershipAnswers,
            current: UserAnswers
          )(implicit messages: Messages): Seq[Seq[TableRow]] =
    nameRow(original, current) ++
      addressRows(original, current) ++
      contactRows(original, current) ++
      utrRows(original, current) ++
      nominatedPartnersUtrRows(original, current) ++
      nominatedPartnersCrnRows(original, current) ++
      nominatedPartnersNinoRows(original, current) ++
      worksReferenceRows(original, current)

  private def nameRow(
                       original: OriginalPartnershipAnswers,
                       current: UserAnswers
                     )(implicit messages: Messages): Seq[Seq[TableRow]] =
    Seq(
      fieldRow(
        PartnershipNamePage,
        messages("partnershipName.checkYourAnswersLabel"),
        original.partnershipName,
        current
      )
    ).flatten

  private def addressRows(
                           original: OriginalPartnershipAnswers,
                           current: UserAnswers
                         )(implicit messages: Messages): Seq[Seq[TableRow]] = {
    val currentAddress = current.get(PartnershipAddressPage)

    Seq(
      yesNoRow(
        PartnershipAddressYesNoPage,
        messages("partnershipAddressYesNo.checkYourAnswersLabel"),
        original.addressYesNo,
        current
      ),
      Option.when(
        original.address != currentAddress
      ) {
        row(
          messages("partnershipAddress.checkYourAnswersLabel"),
          original.address.map(formatAddress).getOrElse(missingValue),
          currentAddress.map(formatAddress).getOrElse(missingValue)
        )
      }
    ).flatten
  }

  private def contactRows(
                           original: OriginalPartnershipAnswers,
                           current: UserAnswers
                         )(implicit messages: Messages): Seq[Seq[TableRow]] = {
    val currentMethods = current.get(PartnershipContactMethodOptionsPage).getOrElse(Set.empty)

    Seq(
      yesNoRow(
        AddPartnershipContactMethodsYesNoPage,
        messages("addPartnershipContactMethodsYesNo.checkYourAnswersLabel"),
        original.partnershipContactMethodsYesNo,
        current
      ),
      Option.when(
        original.partnershipContactMethodOptions != currentMethods
      ) {
        row(
          messages("partnershipContactMethodOptions.checkYourAnswersLabel"),
          formatContactMethods(original.partnershipContactMethodOptions),
          formatContactMethods(currentMethods)
        )
      },
      fieldRow(
        PartnershipEmailAddressPage,
        messages("partnershipEmailAddress.checkYourAnswersLabel"),
        original.email,
        current
      ),
      fieldRow(
        PartnershipPhoneNumberPage,
        messages("partnershipPhoneNumber.checkYourAnswersLabel"),
        original.phone,
        current
      ),
      fieldRow(
        PartnershipMobileNumberPage,
        messages("partnershipMobileNumber.checkYourAnswersLabel"),
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
            messages("partnershipContactMethodOptions.email")
          case ContactMethodOptions.Phone  =>
            messages("partnershipContactMethodOptions.phone")
          case ContactMethodOptions.Mobile =>
            messages("partnershipContactMethodOptions.mobile")
        }
        .mkString(", ")
    }

  private def worksReferenceRows(original: OriginalPartnershipAnswers, current: UserAnswers)(implicit
                                                                                       messages: Messages
  ): Seq[Seq[TableRow]] =
    Seq(
      yesNoRow(
        PartnershipWorksReferenceNumberYesNoPage,
        messages("partnershipWorksReferenceYesNo.checkYourAnswersLabel"),
        original.nominatedPartnerWorksReferenceYesNo,
        current
      ),
      fieldRow(
        PartnershipWorksReferenceNumberPage,
        messages("partnershipWorksReference.checkYourAnswersLabel"),
        original.nominatedPartnerWorksReference,
        current
      )
    ).flatten

  private def utrRows(original: OriginalPartnershipAnswers, current: UserAnswers)(implicit
                                                                            messages: Messages
  ): Seq[Seq[TableRow]] =
    Seq(
      yesNoRow(
        PartnershipHasUtrYesNoPage,
        messages("partnershipHasUtrYesNo.checkYourAnswersLabel"),
        original.hasUtrYesNo,
        current
      ),
      fieldRow(
        PartnershipUniqueTaxpayerReferencePage,
        messages("partnershipUniqueTaxpayerReference.checkYourAnswersLabel"),
        original.utr,
        current
      )
    ).flatten

  private def nominatedPartnerUtrRows(original: OriginalPartnershipAnswers, current: UserAnswers)(implicit
                                                                                    messages: Messages
    ): Seq[Seq[TableRow]] =
      Seq(
        yesNoRow(
          PartnershipNominatedPartnerUtrYesNoPage,
          messages("partnershipNominatedPartnerUtrYesNo.checkYourAnswersLabel"),
          original.nominatedPartnerUtrYesNo,
          current
        ),
        fieldRow(
          PartnershipNominatedPartnerUtrPage,
          messages("partnershipNominatedPartnerUtr.checkYourAnswersLabel"),
          original.nominatedPartnerUtr,
          current
        )
      ).flatten

  private def nominatedPartnerCrnRows(original: OriginalPartnershipAnswers, current: UserAnswers)(implicit
                                                                                                  messages: Messages
  ): Seq[Seq[TableRow]] =
    Seq(
      yesNoRow(
        PartnershipNominatedPartnerCrnYesNoPage,
        messages("partnershipNominatedPartnerCrnYesNo.checkYourAnswersLabel"),
        original.nominatedPartnerCrnYesNo,
        current
      ),
      fieldRow(
        PartnershipNominatedPartnerCrnPage,
        messages("partnershipNominatedPartnerCrn.checkYourAnswersLabel"),
        original.nominatedPartnerCrn,
        current
      )
    ).flatten

  private def nominatedPartnerNinoRows(original: OriginalPartnershipAnswers, current: UserAnswers)(implicit
                                                                                                    messages: Messages
    ): Seq[Seq[TableRow]] =
      Seq(
        yesNoRow(
          PartnershipNominatedPartnerNinoYesNoPage,
          messages("partnershipNominatedPartnerNinoYesNo.checkYourAnswersLabel"),
          original.nominatedPartnerNinoYesNo,
          current
        ),
        fieldRow(
          PartnershipNominatedPartnerNinoPage,
          messages("partnershipNominatedPartnerNino.checkYourAnswersLabel"),
          original.nominatedPartnerNino,
          current
        )
      ).flatten

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
