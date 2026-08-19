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

import base.SpecBase
import models.address.{Address, Country}
import models.amend.partnership.OriginalPartnershipAnswers
import models.contact.ContactMethodOptions
import pages.add.partnership.*
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text

class AmendPartnershipConfirmationViewModelSpec extends SpecBase {

  implicit val msgs: Messages = messages(app)

  private val original =
    OriginalPartnershipAnswers(
      partnershipName = Some("ABC Partnership"),
      addressYesNo = Some(true),
      address = Some(
        Address(
          addressLine1 = "1 Test Street",
          addressLine3 = Some("Newcastle"),
          postcode = Some("SA1 1AA"),
          country = Some(Country(code = None, name = Some("England")))
        )
      ),
      partnershipContactMethodsYesNo = Some(true),
      partnershipContactMethodOptions = Set(ContactMethodOptions.Email),
      email = Some("partnership@test.com"),
      phone = None,
      mobile = None,
      hasUtrYesNo = Some(true),
      utr = Some("1123456789"),
      nominatedPartnerCrnYesNo = Some(true),
      nominatedPartnerCrn = Some("87654321"),
      nominatedPartnerName = Some("nominated partner name"),
      nominatedPartnerUtrYesNo = Some(true),
      nominatedPartnerUtr = Some("8888888888"),
      nominatedPartnerNinoYesNo = Some(true),
      nominatedPartnerNino = Some("AB123456"),
      nominatedPartnerWorksReferenceYesNo = Some(true),
      nominatedPartnerWorksReference = Some("WR123"),
      verificationNumber = Some("V100000")
    )

  private val answersMatchingOriginal =
    emptyUserAnswers
      .set(PartnershipNamePage, "ABC Partnership")
      .success
      .value
      .set(PartnershipAddressYesNoPage, true)
      .success
      .value
      .set(
        PartnershipAddressPage,
        Address(
          addressLine1 = "1 Test Street",
          addressLine3 = Some("Newcastle"),
          postcode = Some("SA1 1AA"),
          country = Some(Country(code = None, name = Some("England")))
        )
      )
      .success
      .value
      .set(AddPartnershipContactMethodsYesNoPage, true)
      .success
      .value
      .set(
        PartnershipContactMethodOptionsPage,
        Set(ContactMethodOptions.Email)
      )
      .success
      .value
      .set(PartnershipEmailAddressPage, "partnership@test.com")
      .success
      .value
      .set(PartnershipHasUtrYesNoPage, true)
      .success
      .value
      .set(PartnershipUniqueTaxpayerReferencePage, "1123456789")
      .success
      .value
      .set(PartnershipNominatedPartnerNamePage, "nominated partner name")
      .success
      .value
      .set(PartnershipNominatedPartnerCrnYesNoPage, true)
      .success
      .value
      .set(PartnershipNominatedPartnerCrnPage, "87654321")
      .success
      .value
      .set(PartnershipNominatedPartnerUtrYesNoPage, true)
      .success
      .value
      .set(PartnershipNominatedPartnerUtrPage, "8888888888")
      .success
      .value
      .set(PartnershipNominatedPartnerNinoYesNoPage, true)
      .success
      .value
      .set(PartnershipNominatedPartnerNinoPage, "AB123456")
      .success
      .value
      .set(PartnershipWorksReferenceNumberYesNoPage, true)
      .success
      .value
      .set(PartnershipWorksReferenceNumberPage, "WR123")
      .success
      .value

  "rows" - {

    "must return no rows when nothing has changed" in {

      val result =
        AmendPartnershipConfirmationViewModel.rows(original, answersMatchingOriginal)

      result mustBe empty
    }

    "must return a partnership name row when the partnership name changes" in {

      val answers =
        answersMatchingOriginal
          .set(PartnershipNamePage, "XYZ Partnership")
          .success
          .value

      val result =
        AmendPartnershipConfirmationViewModel.rows(original, answers)

      val row = result.head

      row.head.content mustBe Text(msgs("partnershipName.checkYourAnswersLabel"))
      row(1).content mustBe Text("ABC Partnership")
      row(2).content mustBe Text("XYZ Partnership")
    }

    "must return address yes/no and address rows when the address is removed" in {
      val answers =
        answersMatchingOriginal
          .set(PartnershipAddressYesNoPage, false)
          .success
          .value
          .remove(PartnershipAddressPage)
          .success
          .value

      val result =
        AmendPartnershipConfirmationViewModel.rows(original, answers)

      result must have size 2

      val yesNoRow   = result.head
      val addressRow = result(1)

      yesNoRow.head.content mustBe Text(msgs("partnershipAddressYesNo.checkYourAnswersLabel"))
      yesNoRow(1).content mustBe Text(msgs("site.yes"))
      yesNoRow(2).content mustBe Text(msgs("site.no"))

      addressRow.head.content mustBe Text(msgs("partnershipAddress.checkYourAnswersLabel"))
      addressRow(1).content mustBe Text("1 Test Street, Newcastle, SA1 1AA, England")
      addressRow(2).content mustBe Text(msgs("amendConfirmation.table.content.none"))
    }

    "must return an address row when the address changes" in {
      val answers =
        answersMatchingOriginal
          .set(
            PartnershipAddressPage,
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
        AmendPartnershipConfirmationViewModel.rows(original, answers)

      val row = result.head

      row.head.content mustBe Text(msgs("partnershipAddress.checkYourAnswersLabel"))
      row(1).content mustBe Text("1 Test Street, Newcastle, SA1 1AA, England")
      row(2).content mustBe Text("10 Test Street, Newcastle, SA1 1AA, England")
    }

    "must display all populated address lines when an address changes" in {
      val answers =
        answersMatchingOriginal
          .set(
            PartnershipAddressPage,
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
        AmendPartnershipConfirmationViewModel.rows(original, answers)

      val row = result.head

      row(2).content mustBe Text(
        "10 Test Street, Building A, Business Park, Leeds, West Yorkshire, LS1 1AA, England"
      )
    }

    "must return contact rows when contact methods are removed" in {

      val answers =
        answersMatchingOriginal
          .set(AddPartnershipContactMethodsYesNoPage, false)
          .success
          .value
          .remove(PartnershipContactMethodOptionsPage)
          .success
          .value
          .remove(PartnershipEmailAddressPage)
          .success
          .value

      val result =
        AmendPartnershipConfirmationViewModel.rows(original, answers)

      result must have size 3

      val yesNoRow  = result.head
      val methodRow = result(1)
      val emailRow  = result(2)

      yesNoRow.head.content mustBe Text(msgs("addPartnershipContactMethodsYesNo.checkYourAnswersLabel"))
      yesNoRow(1).content mustBe Text(msgs("site.yes"))
      yesNoRow(2).content mustBe Text(msgs("site.no"))

      methodRow.head.content mustBe Text(msgs("partnershipContactMethodOptions.checkYourAnswersLabel"))
      methodRow(1).content mustBe Text(msgs("partnershipContactMethodOptions.email"))
      methodRow(2).content mustBe Text(msgs("amendConfirmation.table.selectContent.none"))

      emailRow.head.content mustBe Text(msgs("partnershipEmailAddress.checkYourAnswersLabel"))
      emailRow(1).content mustBe Text("partnership@test.com")
      emailRow(2).content mustBe Text(msgs("amendConfirmation.table.content.none"))
    }

    "must display contact methods in canonical order" in {

      val answers =
        answersMatchingOriginal
          .set(
            PartnershipContactMethodOptionsPage,
            Set(
              ContactMethodOptions.Mobile,
              ContactMethodOptions.Email,
              ContactMethodOptions.Phone
            )
          )
          .success
          .value

      val result =
        AmendPartnershipConfirmationViewModel.rows(original, answers)

      result must have size 1

      val row = result.head

      row.head.content mustBe Text(msgs("partnershipContactMethodOptions.checkYourAnswersLabel"))
      row(1).content mustBe Text(msgs("partnershipContactMethodOptions.email"))
      row(2).content mustBe Text(
        s"${msgs("partnershipContactMethodOptions.email")}, " +
          s"${msgs("partnershipContactMethodOptions.phone")}, " +
          msgs("partnershipContactMethodOptions.mobile")
      )
    }

    "must display contact methods in a consistent order regardless of selection order" in {
      val answers =
        answersMatchingOriginal
          .set(
            PartnershipContactMethodOptionsPage,
            Set(
              ContactMethodOptions.Phone,
              ContactMethodOptions.Email
            )
          )
          .success
          .value

      val result =
        AmendPartnershipConfirmationViewModel.rows(original, answers)

      result must have size 1

      val row = result.head

      row.head.content mustBe Text(msgs("partnershipContactMethodOptions.checkYourAnswersLabel"))
      row(1).content mustBe Text(msgs("partnershipContactMethodOptions.email"))
      row(2).content mustBe Text(
        s"${msgs("partnershipContactMethodOptions.email")}, ${msgs("partnershipContactMethodOptions.phone")}"
      )
    }

    "must return contact method, email and phone rows when changing from email to phone" in {

      val answers =
        answersMatchingOriginal
          .set(
            PartnershipContactMethodOptionsPage,
            Set(ContactMethodOptions.Phone)
          )
          .success
          .value
          .remove(PartnershipEmailAddressPage)
          .success
          .value
          .set(PartnershipPhoneNumberPage, "01131234567")
          .success
          .value

      val result =
        AmendPartnershipConfirmationViewModel.rows(original, answers)

      result must have size 3

      val methodRow = result.head
      val emailRow  = result(1)
      val phoneRow  = result(2)

      methodRow.head.content mustBe Text(msgs("partnershipContactMethodOptions.checkYourAnswersLabel"))
      methodRow(1).content mustBe Text(msgs("partnershipContactMethodOptions.email"))
      methodRow(2).content mustBe Text(msgs("partnershipContactMethodOptions.phone"))

      emailRow.head.content mustBe Text(msgs("partnershipEmailAddress.checkYourAnswersLabel"))
      emailRow(1).content mustBe Text("partnership@test.com")
      emailRow(2).content mustBe Text(msgs("amendConfirmation.table.content.none"))

      phoneRow.head.content mustBe Text(msgs("partnershipPhoneNumber.checkYourAnswersLabel"))
      phoneRow(1).content mustBe Text(msgs("amendConfirmation.table.content.none"))
      phoneRow(2).content mustBe Text("01131234567")
    }

    "must return an email row when the email changes" in {

      val answers =
        answersMatchingOriginal
          .set(PartnershipEmailAddressPage, "new@test.com")
          .success
          .value

      val result =
        AmendPartnershipConfirmationViewModel.rows(original, answers)

      val row = result.head

      row.head.content mustBe Text(msgs("partnershipEmailAddress.checkYourAnswersLabel"))
      row(1).content mustBe Text("partnership@test.com")
      row(2).content mustBe Text("new@test.com")
    }

    "must return a phone row when a phone number is added" in {

      val answers =
        answersMatchingOriginal
          .set(PartnershipPhoneNumberPage, "01131234567")
          .success
          .value

      val result =
        AmendPartnershipConfirmationViewModel.rows(original, answers)

      val row = result.head

      row.head.content mustBe Text(msgs("partnershipPhoneNumber.checkYourAnswersLabel"))
      row(1).content mustBe Text(msgs("amendConfirmation.table.content.none"))
      row(2).content mustBe Text("01131234567")
    }

    "must return a phone row when the phone number changes" in {

      val originalPhone =
        original.copy(
          partnershipContactMethodOptions = Set(ContactMethodOptions.Phone),
          email = None,
          phone = Some("01131234567")
        )

      val answers =
        answersMatchingOriginal
          .remove(PartnershipEmailAddressPage)
          .success
          .value
          .set(PartnershipContactMethodOptionsPage, Set(ContactMethodOptions.Phone))
          .success
          .value
          .set(PartnershipPhoneNumberPage, "07700900123")
          .success
          .value

      val result =
        AmendPartnershipConfirmationViewModel.rows(originalPhone, answers)

      val row = result.head

      row.head.content mustBe Text(msgs("partnershipPhoneNumber.checkYourAnswersLabel"))
      row(1).content mustBe Text("01131234567")
      row(2).content mustBe Text("07700900123")
    }

    "must return a mobile row when a mobile number is added" in {

      val answers =
        answersMatchingOriginal
          .set(PartnershipMobileNumberPage, "07700900123")
          .success
          .value

      val result =
        AmendPartnershipConfirmationViewModel.rows(original, answers)

      val row = result.head

      row.head.content mustBe Text(msgs("partnershipMobileNumber.checkYourAnswersLabel"))
      row(1).content mustBe Text(msgs("amendConfirmation.table.content.none"))
      row(2).content mustBe Text("07700900123")
    }

    "must return a mobile row when the mobile number changes" in {
      val originalMobile =
        original.copy(
          partnershipContactMethodOptions = Set(ContactMethodOptions.Mobile),
          email = None,
          mobile = Some("07700900123")
        )

      val answers =
        answersMatchingOriginal
          .remove(PartnershipEmailAddressPage)
          .success
          .value
          .set(PartnershipContactMethodOptionsPage, Set(ContactMethodOptions.Mobile))
          .success
          .value
          .set(PartnershipMobileNumberPage, "07700900456")
          .success
          .value

      val result =
        AmendPartnershipConfirmationViewModel.rows(originalMobile, answers)

      val row = result.head

      row.head.content mustBe Text(msgs("partnershipMobileNumber.checkYourAnswersLabel"))
      row(1).content mustBe Text("07700900123")
      row(2).content mustBe Text("07700900456")
    }

    "must return a contact methods row when multiple contact methods are selected" in {

      val answers =
        answersMatchingOriginal
          .set(
            PartnershipContactMethodOptionsPage,
            Set(
              ContactMethodOptions.Email,
              ContactMethodOptions.Phone
            )
          )
          .success
          .value
          .set(PartnershipPhoneNumberPage, "01131234567")
          .success
          .value

      val result =
        AmendPartnershipConfirmationViewModel.rows(original, answers)

      result must have size 2

      val methodRow = result.head
      val phoneRow  = result(1)

      methodRow.head.content mustBe Text(msgs("partnershipContactMethodOptions.checkYourAnswersLabel"))
      methodRow(1).content mustBe Text(msgs("partnershipContactMethodOptions.email"))
      methodRow(2).content mustBe Text(
        s"${msgs("partnershipContactMethodOptions.email")}, ${msgs("partnershipContactMethodOptions.phone")}"
      )

      phoneRow.head.content mustBe Text(msgs("partnershipPhoneNumber.checkYourAnswersLabel"))
      phoneRow(1).content mustBe Text(msgs("amendConfirmation.table.content.none"))
      phoneRow(2).content mustBe Text("01131234567")
    }

    "must return a UTR row when the UTR changes" in {

      val answers =
        answersMatchingOriginal
          .set(PartnershipUniqueTaxpayerReferencePage, "2000000000")
          .success
          .value

      val result =
        AmendPartnershipConfirmationViewModel.rows(original, answers)

      val row = result.head

      row.head.content mustBe Text(msgs("partnershipUniqueTaxpayerReference.checkYourAnswersLabel"))
      row(1).content mustBe Text("1123456789")
      row(2).content mustBe Text("2000000000")
    }

    "must return a CRN yes/no row when the answer changes" in {
      val answers =
        answersMatchingOriginal
          .set(PartnershipNominatedPartnerCrnYesNoPage, false)
          .success
          .value

      val result =
        AmendPartnershipConfirmationViewModel.rows(original, answers)

      result must have size 2

      val yesNoRow = result.head
      val crnRow   = result(1)

      yesNoRow.head.content mustBe Text(msgs("partnershipNominatedPartnerCrnYesNo.checkYourAnswersLabel"))
      yesNoRow(1).content mustBe Text(msgs("site.yes"))
      yesNoRow(2).content mustBe Text(msgs("site.no"))

      crnRow.head.content mustBe Text(msgs("partnershipNominatedPartnerCrn.checkYourAnswersLabel"))
      crnRow(1).content mustBe Text("87654321")
      crnRow(2).content mustBe Text(msgs("amendConfirmation.table.content.none"))
    }

    "must return a CRN row when the CRN changes" in {
      val answers =
        answersMatchingOriginal
          .set(PartnershipNominatedPartnerCrnPage, "12345678")
          .success
          .value

      val result =
        AmendPartnershipConfirmationViewModel.rows(original, answers)

      val row = result.head

      row.head.content mustBe Text(msgs("partnershipNominatedPartnerCrn.checkYourAnswersLabel"))
      row(1).content mustBe Text("87654321")
      row(2).content mustBe Text("12345678")
    }

    "must return a works reference yes/no row when the answer changes" in {

      val answers =
        answersMatchingOriginal
          .set(PartnershipWorksReferenceNumberYesNoPage, false)
          .success
          .value

      val result =
        AmendPartnershipConfirmationViewModel.rows(original, answers)

      result must have size 2

      val yesNoRow = result.head
      val worksRow = result(1)

      yesNoRow.head.content mustBe Text(msgs("partnershipWorksReferenceNumberYesNo.checkYourAnswersLabel"))
      yesNoRow(1).content mustBe Text(msgs("site.yes"))
      yesNoRow(2).content mustBe Text(msgs("site.no"))

      worksRow.head.content mustBe Text(msgs("partnershipWorksReferenceNumber.checkYourAnswersLabel"))
      worksRow(1).content mustBe Text("WR123")
      worksRow(2).content mustBe Text(msgs("amendConfirmation.table.content.none"))
    }

    "must return a works reference row when the works reference changes" in {

      val answers =
        answersMatchingOriginal
          .set(PartnershipWorksReferenceNumberPage, "WR999")
          .success
          .value

      val result =
        AmendPartnershipConfirmationViewModel.rows(original, answers)

      val row = result.head

      row.head.content mustBe Text(msgs("partnershipWorksReferenceNumber.checkYourAnswersLabel"))
      row(1).content mustBe Text("WR123")
      row(2).content mustBe Text("WR999")
    }

    "must return a UTR yes/no row when the answer changes" in {

      val answers =
        answersMatchingOriginal
          .set(PartnershipHasUtrYesNoPage, false)
          .success
          .value

      val result =
        AmendPartnershipConfirmationViewModel.rows(original, answers)

      result must have size 2

      val yesNoRow = result.head
      val utrRow   = result(1)

      yesNoRow.head.content mustBe Text(msgs("partnershipHasUtrYesNo.checkYourAnswersLabel"))
      yesNoRow(1).content mustBe Text(msgs("site.yes"))
      yesNoRow(2).content mustBe Text(msgs("site.no"))

      utrRow.head.content mustBe Text(msgs("partnershipUniqueTaxpayerReference.checkYourAnswersLabel"))
      utrRow(1).content mustBe Text("1123456789")
      utrRow(2).content mustBe Text(msgs("amendConfirmation.table.content.none"))
    }

    "must return a nominated UTR yes/no row when the answer changes" in {

      val answers =
        answersMatchingOriginal
          .set(PartnershipNominatedPartnerUtrYesNoPage, false)
          .success
          .value

      val result =
        AmendPartnershipConfirmationViewModel.rows(original, answers)

      result must have size 2

      val yesNoRow        = result.head
      val nominatedUtrRow = result(1)

      yesNoRow.head.content mustBe Text(msgs("partnershipNominatedPartnerUtrYesNo.checkYourAnswersLabel"))
      yesNoRow(1).content mustBe Text(msgs("site.yes"))
      yesNoRow(2).content mustBe Text(msgs("site.no"))

      nominatedUtrRow.head.content mustBe Text(msgs("partnershipNominatedPartnerUtr.checkYourAnswersLabel"))
      nominatedUtrRow(1).content mustBe Text("8888888888")
      nominatedUtrRow(2).content mustBe Text(msgs("amendConfirmation.table.content.none"))
    }

    "must return rows in the expected order when multiple sections change" in {
      val answers =
        answersMatchingOriginal
          .set(PartnershipNamePage, "XYZ Partnership")
          .success
          .value
          .set(PartnershipUniqueTaxpayerReferencePage, "2000000000")
          .success
          .value
          .set(
            PartnershipAddressPage,
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
        AmendPartnershipConfirmationViewModel.rows(original, answers)

      result must have size 3

      result(0).head.content mustBe Text(msgs("partnershipName.checkYourAnswersLabel"))
      result(1).head.content mustBe Text(msgs("partnershipAddress.checkYourAnswersLabel"))
      result(2).head.content mustBe Text(msgs("partnershipUniqueTaxpayerReference.checkYourAnswersLabel"))
    }
  }
}
