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
import models.address.Address
import models.amend.company.OriginalCompanyAnswers
import models.contact.ContactMethodOptions
import pages.QuestionPage
import pages.add.*
import pages.add.company.*
import pages.amend.AmendedPagesPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.TableRow

object CompanyAmendConfirmationViewModel {

  def rows(
            original: OriginalCompanyAnswers,
            current: UserAnswers
          )(implicit messages: Messages): Seq[Seq[TableRow]] =
    nameRow(original, current) ++
      addressRows(original, current) ++
      contactRows(original, current) ++
      utrRows(original, current) ++
      crnRows(original, current) ++
      worksReferenceRows(original, current)

  private def nameRow(
                       original: OriginalCompanyAnswers,
                       current: UserAnswers
                     )(implicit messages: Messages): Seq[Seq[TableRow]] =
    Seq(
      fieldRow(
        CompanyNamePage,
        messages("companyName.checkYourAnswersLabel"),
        original.companyName,
        current
      )
    ).flatten

  private def addressRows(
                           original: OriginalCompanyAnswers,
                           current: UserAnswers
                         )(implicit messages: Messages): Seq[Seq[TableRow]] = {
    val currentAddress = current.get(CompanyAddressPage)

    Seq(
      yesNoRow(
        CompanyAddressYesNoPage,
        messages("companyAddressYesNo.checkYourAnswersLabel"),
        original.addressYesNo,
        current
      ),
      Option.when(
        wasAmended(current, CompanyAddressPage) ||
          original.address != currentAddress
      ) {
        row(
          messages("companyAddress.checkYourAnswersLabel"),
          original.address.map(formatAddress).getOrElse(missingValue),
          currentAddress.map(formatAddress).getOrElse(missingValue)
        )
      }
    ).flatten
  }

  private def contactRows(
                           original: OriginalCompanyAnswers,
                           current: UserAnswers
                         )(implicit messages: Messages): Seq[Seq[TableRow]] = {
    val currentMethods = current.get(CompanyContactMethodOptionsPage).getOrElse(Set.empty)

    Seq(
      yesNoRow(
        AddCompanyContactMethodsYesNoPage,
        messages("addCompanyContactMethodsYesNo.checkYourAnswersLabel"),
        original.companyContactMethodsYesNo,
        current
      ),
      Option.when(
        wasAmended(current, CompanyContactMethodOptionsPage) ||
          original.companyContactMethod != currentMethods
      ) {
        row(
          messages("companyContactMethodOptions.checkYourAnswersLabel"),
          formatContactMethods(original.companyContactMethod),
          formatContactMethods(currentMethods)
        )
      },
      fieldRow(
        CompanyEmailAddressPage,
        messages("companyEmailAddress.checkYourAnswersLabel"),
        original.email,
        current
      ),
      fieldRow(
        CompanyPhoneNumberPage,
        messages("companyPhoneNumber.checkYourAnswersLabel"),
        original.phone,
        current
      ),
      fieldRow(
        CompanyMobileNumberPage,
        messages("companyMobileNumber.checkYourAnswersLabel"),
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
            messages("companyContactMethodOptions.email")
          case ContactMethodOptions.Phone  =>
            messages("companyContactMethodOptions.phone")
          case ContactMethodOptions.Mobile =>
            messages("companyContactMethodOptions.mobile")
        }
        .mkString(", ")
    }

  private def worksReferenceRows(original: OriginalCompanyAnswers, current: UserAnswers)(implicit
                                                                                       messages: Messages
  ): Seq[Seq[TableRow]] =
    Seq(
      yesNoRow(
        CompanyWorksReferenceYesNoPage,
        messages("companyWorksReferenceYesNo.checkYourAnswersLabel"),
        original.worksReferenceYesNo,
        current
      ),
      fieldRow(
        CompanyWorksReferencePage,
        messages("companyWorksReference.checkYourAnswersLabel"),
        original.worksReference,
        current
      )
    ).flatten

  private def utrRows(original: OriginalCompanyAnswers, current: UserAnswers)(implicit
                                                                            messages: Messages
  ): Seq[Seq[TableRow]] =
    Seq(
      yesNoRow(
        CompanyUtrYesNoPage,
        messages("companyUtrYesNo.checkYourAnswersLabel"),
        original.utrYesNo,
        current
      ),
      fieldRow(
        CompanyUtrPage,
        messages("companyUtr.checkYourAnswersLabel"),
        original.utr,
        current
      )
    ).flatten

  private def crnRows(original: OriginalCompanyAnswers, current: UserAnswers)(implicit
                                                                              messages: Messages
  ): Seq[Seq[TableRow]] =
    Seq(
      yesNoRow(
        CompanyCrnYesNoPage,
        messages("companyCrnYesNo.checkYourAnswersLabel"),
        original.crnYesNo,
        current
      ),
      fieldRow(
        CompanyCrnPage,
        messages("companyCrn.checkYourAnswersLabel"),
        original.crn,
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

    Option.when(
      wasAmended(current, page) || original != currentVal
    ) {
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

    Option.when(
      wasAmended(current, page) || original != currentVal
    ) {
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

  private def wasAmended(
                          current: UserAnswers,
                          page: QuestionPage[_]
                        ): Boolean =
    current
      .get(AmendedPagesPage)
      .getOrElse(Set.empty)
      .contains(page.toString)
}
