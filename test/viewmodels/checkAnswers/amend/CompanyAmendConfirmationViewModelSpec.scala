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

package viewmodels.checkAnswers.amend

import base.SpecBase
import models.address.{Address, Country}
import models.amend.company.OriginalCompanyAnswers
import models.contact.ContactMethodOptions
import pages.add.company.*
import pages.amend.AmendedPagesPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import viewmodels.amend.CompanyAmendConfirmationViewModel

class CompanyAmendConfirmationViewModelSpec extends SpecBase {

  implicit val msgs: Messages = messages(app)

  private val original =
    OriginalCompanyAnswers(
      companyName = Some("ABC Company"),
      addressYesNo = Some(true),
      address = Some(
        Address(
          addressLine1 = "1 Test Street",
          addressLine3 = Some("Newcastle"),
          postcode = Some("SA1 1AA"),
          country = Some(Country(code = None, name = Some("England")))
        )
      ),
      companyContactMethodsYesNo = Some(true),
      companyContactMethod = Set(ContactMethodOptions.Email),
      email = Some("company@test.com"),
      phone = None,
      mobile = None,
      utrYesNo = Some(true),
      utr = Some("1123456789"),
      crnYesNo = Some(true),
      crn = Some("87654321"),
      worksReferenceYesNo = Some(true),
      worksReference = Some("WR123")
    )

  private val answersMatchingOriginal =
    emptyUserAnswers
      .set(CompanyNamePage, "ABC Company")
      .success
      .value
      .set(CompanyAddressYesNoPage, true)
      .success
      .value
      .set(
        CompanyAddressPage,
        Address(
          addressLine1 = "1 Test Street",
          addressLine3 = Some("Newcastle"),
          postcode = Some("SA1 1AA"),
          country = Some(Country(code = None, name = Some("England")))
        )
      )
      .success
      .value
      .set(AddCompanyContactMethodsYesNoPage, true)
      .success
      .value
      .set(
        CompanyContactMethodOptionsPage,
        Set(ContactMethodOptions.Email)
      )
      .success
      .value
      .set(CompanyEmailAddressPage, "company@test.com")
      .success
      .value
      .set(CompanyUtrYesNoPage, true)
      .success
      .value
      .set(CompanyUtrPage, "1123456789")
      .success
      .value
      .set(CompanyCrnYesNoPage, true)
      .success
      .value
      .set(CompanyCrnPage, "87654321")
      .success
      .value
      .set(CompanyWorksReferenceYesNoPage, true)
      .success
      .value
      .set(CompanyWorksReferencePage, "WR123")
      .success
      .value

  "rows" - {

    "must return no rows when nothing has changed" in {

      val result =
        CompanyAmendConfirmationViewModel.rows(original, answersMatchingOriginal)

      result mustBe empty
    }

    "must return a ompany name row when the company name changes" in {

      val answers =
        answersMatchingOriginal
          .set(CompanyNamePage, "XYZ Company")
          .success
          .value
          .set(AmendedPagesPage, Set(CompanyNamePage.toString))
          .success
          .value

      val result =
        CompanyAmendConfirmationViewModel.rows(original, answers)

      val row = result.head

      row.head.content mustBe Text(msgs("companyName.checkYourAnswersLabel"))
      row(1).content mustBe Text("ABC Company")
      row(2).content mustBe Text("XYZ Company")
    }

    "must return a company name row when the page is amended but the value is unchanged" in {
      val answers =
        answersMatchingOriginal
          .set(AmendedPagesPage, Set(CompanyNamePage.toString))
          .success
          .value

      val result =
        CompanyAmendConfirmationViewModel.rows(original, answers)

      val row = result.head

      row.head.content mustBe Text(msgs("companyName.checkYourAnswersLabel"))
      row(1).content mustBe Text("ABC Company")
      row(2).content mustBe Text("ABC Company")
    }

    "must return address yes/no and address rows when the address is removed" in {
      val answers =
        answersMatchingOriginal
          .set(CompanyAddressYesNoPage, false)
          .success
          .value
          .remove(CompanyAddressPage)
          .success
          .value

      val result =
        CompanyAmendConfirmationViewModel.rows(original, answers)

      result must have size 2

      val yesNoRow   = result.head
      val addressRow = result(1)

      yesNoRow.head.content mustBe Text(msgs("companyAddressYesNo.checkYourAnswersLabel"))
      yesNoRow(1).content mustBe Text(msgs("site.yes"))
      yesNoRow(2).content mustBe Text(msgs("site.no"))

      addressRow.head.content mustBe Text(msgs("companyAddress.checkYourAnswersLabel"))
      addressRow(1).content mustBe Text("1 Test Street, Newcastle, SA1 1AA, England")
      addressRow(2).content mustBe Text(msgs("amendConfirmation.table.content.none"))
    }

    "must return an address row when the address changes" in {
      val answers =
        answersMatchingOriginal
          .set(
            CompanyAddressPage,
            Address(
              addressLine1 = "10 Test Street",
              addressLine3 = Some("Newcastle"),
              postcode = Some("SA1 1AA"),
              country = Some(Country(code = None, name = Some("England")))
            )
          )
          .success
          .value

      val result =
        CompanyAmendConfirmationViewModel.rows(original, answers)

      val row = result.head

      row.head.content mustBe Text(msgs("companyAddress.checkYourAnswersLabel"))
      row(1).content mustBe Text("1 Test Street, Newcastle, SA1 1AA, England")
      row(2).content mustBe Text("10 Test Street, Newcastle, SA1 1AA, England")
    }

    "must display all populated address lines when an address changes" in {
      val answers =
        answersMatchingOriginal
          .set(
            CompanyAddressPage,
            Address(
              addressLine1 = "10 Test Street",
              addressLine2 = Some("Building A"),
              addressLine3 = Some("Business Park"),
              addressLine4 = Some("Leeds"),
              addressLine5 = Some("West Yorkshire"),
              postcode = Some("LS1 1AA"),
              country = Some(Country(code = None, name = Some("England")))
            )
          )
          .success
          .value

      val result =
        CompanyAmendConfirmationViewModel.rows(original, answers)

      val row = result.head

      row(2).content mustBe Text(
        "10 Test Street, Building A, Business Park, Leeds, West Yorkshire, LS1 1AA, England"
      )
    }

    "must return contact rows when contact methods are removed" in {

      val answers =
        answersMatchingOriginal
          .set(AddCompanyContactMethodsYesNoPage, false)
          .success
          .value
          .remove(CompanyContactMethodOptionsPage)
          .success
          .value
          .remove(CompanyEmailAddressPage)
          .success
          .value
          .set(
            AmendedPagesPage,
            Set(
              AddCompanyContactMethodsYesNoPage.toString,
              CompanyContactMethodOptionsPage.toString,
              CompanyEmailAddressPage.toString
            )
          )
          .success
          .value

      val result =
        CompanyAmendConfirmationViewModel.rows(original, answers)

      result must have size 3

      val yesNoRow  = result.head
      val methodRow = result(1)
      val emailRow  = result(2)

      yesNoRow.head.content mustBe Text(msgs("addCompanyContactMethodsYesNo.checkYourAnswersLabel"))
      yesNoRow(1).content mustBe Text(msgs("site.yes"))
      yesNoRow(2).content mustBe Text(msgs("site.no"))

      methodRow.head.content mustBe Text(msgs("companyContactMethodOptions.checkYourAnswersLabel"))
      methodRow(1).content mustBe Text(msgs("companyContactMethodOptions.email"))
      methodRow(2).content mustBe Text(msgs("amendConfirmation.table.content.none"))

      emailRow.head.content mustBe Text(msgs("companyEmailAddress.checkYourAnswersLabel"))
      emailRow(1).content mustBe Text("company@test.com")
      emailRow(2).content mustBe Text(msgs("amendConfirmation.table.content.none"))
    }

    "must display contact methods in a consistent order regardless of selection order" in {
      val answers =
        answersMatchingOriginal
          .set(
            CompanyContactMethodOptionsPage,
            Set(
              ContactMethodOptions.Phone,
              ContactMethodOptions.Email
            )
          )
          .success
          .value
          .set(
            AmendedPagesPage,
            Set(CompanyContactMethodOptionsPage.toString)
          )
          .success
          .value

      val result =
        CompanyAmendConfirmationViewModel.rows(original, answers)

      result must have size 1

      val row = result.head

      row.head.content mustBe Text(msgs("companyContactMethodOptions.checkYourAnswersLabel"))
      row(1).content mustBe Text(msgs("companyContactMethodOptions.email"))
      row(2).content mustBe Text(
        s"${msgs("companyContactMethodOptions.email")}, ${msgs("companyContactMethodOptions.phone")}"
      )
    }

    "must return contact method, email and phone rows when changing from email to phone" in {

      val answers =
        answersMatchingOriginal
          .set(
            CompanyContactMethodOptionsPage,
            Set(ContactMethodOptions.Phone)
          )
          .success
          .value
          .remove(CompanyEmailAddressPage)
          .success
          .value
          .set(CompanyPhoneNumberPage, "01131234567")
          .success
          .value
          .set(
            AmendedPagesPage,
            Set(
              CompanyContactMethodOptionsPage.toString,
              CompanyEmailAddressPage.toString,
              CompanyPhoneNumberPage.toString
            )
          )
          .success
          .value

      val result =
        CompanyAmendConfirmationViewModel.rows(original, answers)

      result must have size 3

      val methodRow = result.head
      val emailRow  = result(1)
      val phoneRow  = result(2)

      methodRow.head.content mustBe Text(msgs("companyContactMethodOptions.checkYourAnswersLabel"))
      methodRow(1).content mustBe Text(msgs("companyContactMethodOptions.email"))
      methodRow(2).content mustBe Text(msgs("companyContactMethodOptions.phone"))

      emailRow.head.content mustBe Text(msgs("companyEmailAddress.checkYourAnswersLabel"))
      emailRow(1).content mustBe Text("company@test.com")
      emailRow(2).content mustBe Text(msgs("amendConfirmation.table.content.none"))

      phoneRow.head.content mustBe Text(msgs("companyPhoneNumber.checkYourAnswersLabel"))
      phoneRow(1).content mustBe Text(msgs("amendConfirmation.table.content.none"))
      phoneRow(2).content mustBe Text("01131234567")
    }

    "must return an email row when the email changes" in {

      val answers =
        answersMatchingOriginal
          .set(CompanyEmailAddressPage, "new@test.com")
          .success
          .value
          .set(AmendedPagesPage, Set(CompanyEmailAddressPage.toString))
          .success
          .value

      val result =
        CompanyAmendConfirmationViewModel.rows(original, answers)

      val row = result.head

      row.head.content mustBe Text(msgs("companyEmailAddress.checkYourAnswersLabel"))
      row(1).content mustBe Text("company@test.com")
      row(2).content mustBe Text("new@test.com")
    }

    "must return a phone row when a phone number is added" in {

      val answers =
        answersMatchingOriginal
          .set(CompanyPhoneNumberPage, "01131234567")
          .success
          .value
          .set(AmendedPagesPage, Set(CompanyPhoneNumberPage.toString))
          .success
          .value

      val result =
        CompanyAmendConfirmationViewModel.rows(original, answers)

      val row = result.head

      row.head.content mustBe Text(msgs("companyPhoneNumber.checkYourAnswersLabel"))
      row(1).content mustBe Text(msgs("amendConfirmation.table.content.none"))
      row(2).content mustBe Text("01131234567")
    }

    "must return a phone row when the phone number changes" in {

      val originalPhone =
        original.copy(
          companyContactMethod = Set(ContactMethodOptions.Phone),
          email = None,
          phone = Some("01131234567")
        )

      val answers =
        answersMatchingOriginal
          .remove(CompanyEmailAddressPage)
          .success
          .value
          .set(CompanyContactMethodOptionsPage, Set(ContactMethodOptions.Phone))
          .success
          .value
          .set(CompanyPhoneNumberPage, "07700900123")
          .success
          .value
          .set(AmendedPagesPage, Set(CompanyPhoneNumberPage.toString))
          .success
          .value

      val result =
        CompanyAmendConfirmationViewModel.rows(originalPhone, answers)

      val row = result.head

      row.head.content mustBe Text(msgs("companyPhoneNumber.checkYourAnswersLabel"))
      row(1).content mustBe Text("01131234567")
      row(2).content mustBe Text("07700900123")
    }

    "must return a mobile row when a mobile number is added" in {

      val answers =
        answersMatchingOriginal
          .set(CompanyMobileNumberPage, "07700900123")
          .success
          .value
          .set(AmendedPagesPage, Set(CompanyMobileNumberPage.toString))
          .success
          .value

      val result =
        CompanyAmendConfirmationViewModel.rows(original, answers)

      val row = result.head

      row.head.content mustBe Text(msgs("companyMobileNumber.checkYourAnswersLabel"))
      row(1).content mustBe Text(msgs("amendConfirmation.table.content.none"))
      row(2).content mustBe Text("07700900123")
    }

    "must return a mobile row when the mobile number changes" in {
      val originalMobile =
        original.copy(
          companyContactMethod = Set(ContactMethodOptions.Mobile),
          email = None,
          mobile = Some("07700900123")
        )

      val answers =
        answersMatchingOriginal
          .remove(CompanyEmailAddressPage)
          .success
          .value
          .set(CompanyContactMethodOptionsPage, Set(ContactMethodOptions.Mobile))
          .success
          .value
          .set(CompanyMobileNumberPage, "07700900456")
          .success
          .value
          .set(AmendedPagesPage, Set(CompanyMobileNumberPage.toString))
          .success
          .value

      val result =
        CompanyAmendConfirmationViewModel.rows(originalMobile, answers)

      val row = result.head

      row.head.content mustBe Text(msgs("companyMobileNumber.checkYourAnswersLabel"))
      row(1).content mustBe Text("07700900123")
      row(2).content mustBe Text("07700900456")
    }

    "must return a contact methods row when multiple contact methods are selected" in {

      val answers =
        answersMatchingOriginal
          .set(
            CompanyContactMethodOptionsPage,
            Set(
              ContactMethodOptions.Email,
              ContactMethodOptions.Phone
            )
          )
          .success
          .value
          .set(CompanyPhoneNumberPage, "01131234567")
          .success
          .value
          .set(
            AmendedPagesPage,
            Set(
              CompanyContactMethodOptionsPage.toString,
              CompanyPhoneNumberPage.toString
            )
          )
          .success
          .value

      val result =
        CompanyAmendConfirmationViewModel.rows(original, answers)

      result must have size 2

      val methodRow = result.head
      val phoneRow  = result(1)

      methodRow.head.content mustBe Text(msgs("companyContactMethodOptions.checkYourAnswersLabel"))
      methodRow(1).content mustBe Text(msgs("companyContactMethodOptions.email"))
      methodRow(2).content mustBe Text(
        s"${msgs("companyContactMethodOptions.email")}, ${msgs("companyContactMethodOptions.phone")}"
      )

      phoneRow.head.content mustBe Text(msgs("companyPhoneNumber.checkYourAnswersLabel"))
      phoneRow(1).content mustBe Text(msgs("amendConfirmation.table.content.none"))
      phoneRow(2).content mustBe Text("01131234567")
    }

    "must return a UTR row when the UTR changes" in {

      val answers =
        answersMatchingOriginal
          .set(CompanyUtrPage, "2000000000")
          .success
          .value
          .set(AmendedPagesPage, Set(CompanyUtrPage.toString))
          .success
          .value

      val result =
        CompanyAmendConfirmationViewModel.rows(original, answers)

      val row = result.head

      row.head.content mustBe Text(msgs("companyUtr.checkYourAnswersLabel"))
      row(1).content mustBe Text("1123456789")
      row(2).content mustBe Text("2000000000")
    }

    "must return a CRN yes/no row when the answer changes" in {
      val answers =
        answersMatchingOriginal
          .set(CompanyCrnYesNoPage, false)
          .success
          .value
          .set(AmendedPagesPage, Set(CompanyCrnYesNoPage.toString))
          .success
          .value

      val result =
        CompanyAmendConfirmationViewModel.rows(original, answers)

      result must have size 2

      val yesNoRow = result.head
      val crnRow   = result(1)

      yesNoRow.head.content mustBe Text(msgs("companyCrnYesNo.checkYourAnswersLabel"))
      yesNoRow(1).content mustBe Text(msgs("site.yes"))
      yesNoRow(2).content mustBe Text(msgs("site.no"))

      crnRow.head.content mustBe Text(msgs("companyCrn.checkYourAnswersLabel"))
      crnRow(1).content mustBe Text("87654321")
      crnRow(2).content mustBe Text(msgs("amendConfirmation.table.content.none"))
    }

    "must return a CRN row when the CRN changes" in {
      val answers =
        answersMatchingOriginal
          .set(CompanyCrnPage, "12345678")
          .success
          .value
          .set(AmendedPagesPage, Set(CompanyCrnPage.toString))
          .success
          .value

      val result =
        CompanyAmendConfirmationViewModel.rows(original, answers)

      val row = result.head

      row.head.content mustBe Text(msgs("companyCrn.checkYourAnswersLabel"))
      row(1).content mustBe Text("87654321")
      row(2).content mustBe Text("12345678")
    }

    "must return a works reference yes/no row when the answer changes" in {

      val answers =
        answersMatchingOriginal
          .set(CompanyWorksReferenceYesNoPage, false)
          .success
          .value
          .set(AmendedPagesPage, Set(CompanyWorksReferenceYesNoPage.toString))
          .success
          .value

      val result =
        CompanyAmendConfirmationViewModel.rows(original, answers)

      result must have size 2

      val yesNoRow = result.head
      val worksRow = result(1)

      yesNoRow.head.content mustBe Text(msgs("companyWorksReferenceYesNo.checkYourAnswersLabel"))
      yesNoRow(1).content mustBe Text(msgs("site.yes"))
      yesNoRow(2).content mustBe Text(msgs("site.no"))

      worksRow.head.content mustBe Text(msgs("companyWorksReference.checkYourAnswersLabel"))
      worksRow(1).content mustBe Text("WR123")
      worksRow(2).content mustBe Text(msgs("amendConfirmation.table.content.none"))
    }

    "must return a works reference row when the works reference changes" in {

      val answers =
        answersMatchingOriginal
          .set(CompanyWorksReferencePage, "WR999")
          .success
          .value

      val result =
        CompanyAmendConfirmationViewModel.rows(original, answers)

      val row = result.head

      row.head.content mustBe Text(msgs("companyWorksReference.checkYourAnswersLabel"))
      row(1).content mustBe Text("WR123")
      row(2).content mustBe Text("WR999")
    }

    "must return a UTR yes/no row when the answer changes" in {

      val answers =
        answersMatchingOriginal
          .set(CompanyUtrYesNoPage, false)
          .success
          .value
          .set(AmendedPagesPage, Set(CompanyUtrYesNoPage.toString))
          .success
          .value

      val result =
        CompanyAmendConfirmationViewModel.rows(original, answers)

      result must have size 2

      val yesNoRow = result.head
      val utrRow   = result(1)

      yesNoRow.head.content mustBe Text(msgs("companyUtrYesNo.checkYourAnswersLabel"))
      yesNoRow(1).content mustBe Text(msgs("site.yes"))
      yesNoRow(2).content mustBe Text(msgs("site.no"))

      utrRow.head.content mustBe Text(msgs("companyUtr.checkYourAnswersLabel"))
      utrRow(1).content mustBe Text("1123456789")
      utrRow(2).content mustBe Text(msgs("amendConfirmation.table.content.none"))
    }

    "must return rows in the expected order when multiple sections change" in {
      val answers =
        answersMatchingOriginal
          .set(CompanyNamePage, "XYZ Company")
          .success
          .value
          .set(CompanyUtrPage, "2000000000")
          .success
          .value
          .set(
            CompanyAddressPage,
            Address(
              addressLine1 = "10 Test Street",
              addressLine3 = Some("Newcastle"),
              postcode = Some("SA1 1AA"),
              country = Some(Country(code = None, name = Some("England")))
            )
          )
          .success
          .value

      val result =
        CompanyAmendConfirmationViewModel.rows(original, answers)

      result must have size 3

      result(0).head.content mustBe Text(msgs("companyName.checkYourAnswersLabel"))
      result(1).head.content mustBe Text(msgs("companyAddress.checkYourAnswersLabel"))
      result(2).head.content mustBe Text(msgs("companyUtr.checkYourAnswersLabel"))
    }
  }
}
