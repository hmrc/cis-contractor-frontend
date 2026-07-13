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

import base.SpecBase
import models.add.SubcontractorName
import models.address.{Address, Country}
import models.amend.OriginalIndividualAnswers
import models.contact.ContactOptions
import pages.add.*
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text

class IndividualAmendedViewModelSpec extends SpecBase {

  implicit val msgs: Messages = messages(app)

  private val original =
    OriginalIndividualAnswers(
      usesTradingName = Some(false),
      tradingName = None,
      subcontractorName = Some(
        SubcontractorName(
          firstName = "John",
          middleName = Some("A"),
          lastName = "Smith"
        )
      ),
      addressYesNo = Some(true),
      address = Some(
        Address(
          addressLine1 = "1 High Street",
          addressLine3 = Some("Leeds"),
          postcode = Some("SA1 1AA"),
          country = Some(Country(code = None, name = Some("England")))
        )
      ),
      contactMethod = Some(ContactOptions.Email),
      contactValue = Some("john@test.com"),
      utrYesNo = Some(true),
      utr = Some("1234567890"),
      ninoYesNo = Some(true),
      nino = Some("AB123456C"),
      worksReferenceYesNo = Some(true),
      worksReference = Some("WR123")
    )

  private val answersMatchingOriginal =
    emptyUserAnswers
      .set(SubTradingNameYesNoPage, false)
      .success
      .value
      .set(
        SubcontractorNamePage,
        SubcontractorName("John", Some("A"), "Smith")
      )
      .success
      .value
      .set(SubAddressYesNoPage, true)
.success
    .value
      .set(
        AddressOfSubcontractorPage,
        Address(
          addressLine1 = "1 High Street",
          addressLine3 = Some("Leeds"),
          postcode = Some("SA1 1AA"),
          country = Some(Country(code = None, name = Some("England")))
        )
      )
      .success
      .value
      .set(IndividualChooseContactDetailsPage, ContactOptions.Email)
      .success
      .value
      .set(IndividualEmailAddressPage, "john@test.com")
      .success
      .value
      .set(UniqueTaxpayerReferenceYesNoPage, true)
      .success
      .value
      .set(SubcontractorsUniqueTaxpayerReferencePage, "1234567890")
      .success
      .value
      .set(NationalInsuranceNumberYesNoPage, true)
      .success
      .value
      .set(SubNationalInsuranceNumberPage, "AB123456C")
      .success
      .value
      .set(WorksReferenceNumberYesNoPage, true)
      .success
      .value
      .set(WorksReferenceNumberPage, "WR123")
      .success
      .value

  "rows" - {

    "must return no rows when nothing has changed" in {

      val result =
        IndividualAmendedViewModel.rows(original, answersMatchingOriginal)

      result mustBe empty
    }

    "must return a name row when the name changes" in {

      val answers =
        answersMatchingOriginal
          .set(
            SubcontractorNamePage,
            SubcontractorName("Jane", None, "Smith")
          )
          .success
          .value
          .set(AmendedPagesPage,
            Set(SubcontractorNamePage)).success
          .value

      val result =
        IndividualAmendedViewModel.rows(original, answers)

      val row = result.head

      row.head.content mustBe Text(msgs("subcontractorName.checkYourAnswersLabel"))
      row(1).content mustBe Text("John A Smith")
      row(2).content mustBe Text("Jane Smith")
    }

    "must compare trading names when the original uses a trading name" in {

      val originalTrading =
        original.copy(
          usesTradingName = Some(true),
          tradingName = Some("ABC Contractors"),
          subcontractorName = None
        )

      val answers =
        answersMatchingOriginal
          .set(SubTradingNameYesNoPage, true)
          .success
          .value
          .set(TradingNameOfSubcontractorPage, "XYZ Contractors")
          .success
          .value
          .set(AmendedPagesPage,
            Set(TradingNameOfSubcontractorPage)).success
          .value


      val result =
        IndividualAmendedViewModel.rows(originalTrading, answers)

      val row = result.head

      row.head.content mustBe Text(msgs("tradingNameOfSubcontractor.checkYourAnswersLabel"))
      row(1).content mustBe Text("ABC Contractors")
      row(2).content mustBe Text("XYZ Contractors")
    }

    "must use the trading name label when the user uses a trading name" in {

      val answers =
        answersMatchingOriginal
          .set(SubTradingNameYesNoPage, true)
          .success
          .value
          .set(TradingNameOfSubcontractorPage, "ABC Contractors")
          .success
          .value
          .set(AmendedPagesPage,
            Set(TradingNameOfSubcontractorPage)).success
          .value

      val result =
        IndividualAmendedViewModel.rows(original, answers)

      val nameRow = result(1)

      nameRow.head.content mustBe Text(msgs("tradingNameOfSubcontractor.checkYourAnswersLabel"))
      nameRow(1).content mustBe Text("John A Smith")
      nameRow(2).content mustBe Text("ABC Contractors")
    }

    "must compare trading name to subcontractor name when switching from a trading  to sub contractor name" in {

      val originalTrading =
        original.copy(
          usesTradingName = Some(true),
          tradingName = Some("ABC Contractors"),
          subcontractorName = None
        )

      val answers =
        answersMatchingOriginal
          .set(SubTradingNameYesNoPage, false)
          .success
          .value
          .set(
            SubcontractorNamePage,
            SubcontractorName("John", Some("A"), "Smith")
          )
          .success
          .value
          .set(AmendedPagesPage,
            Set(SubcontractorNamePage)).success
          .value

      val result =
        IndividualAmendedViewModel.rows(originalTrading, answers)

      val nameRow = result(1)

      nameRow.head.content mustBe Text(msgs("subcontractorName.checkYourAnswersLabel"))
      nameRow(1).content mustBe Text("ABC Contractors")
      nameRow(2).content mustBe Text("John A Smith")
    }

    "must return address yes/no and address rows when address is removed" in {

      val answers =
        answersMatchingOriginal
          .set(SubAddressYesNoPage, false)
          .success
          .value
          .remove(AddressOfSubcontractorPage)
          .success
          .value

      val result =
        IndividualAmendedViewModel.rows(original, answers)

      result must have size 2

      val yesNoRow = result.head
      val addressRow = result(1)

      yesNoRow.head.content mustBe Text(msgs("subAddressYesNo.checkYourAnswersLabel"))
      yesNoRow(1).content mustBe Text(msgs("site.yes"))
      yesNoRow(2).content mustBe Text(msgs("site.no"))

      addressRow.head.content mustBe Text(msgs("addressOfSubcontractor.checkYourAnswersLabel"))
      addressRow(1).content mustBe Text("1 High Street, Leeds, SA1 1AA, England")
      addressRow(2).content mustBe Text(msgs("individualAmended.table.content.none"))
    }

    "must return an address row when the address changes" in {

      val answers =
        answersMatchingOriginal
          .set(
            AddressOfSubcontractorPage,
            Address(
              addressLine1 = "1 HIGH Street",
              addressLine3 = Some("Leeds"),
              postcode = Some("SA1 1AA"),
              country = Some(Country(code = None, name = Some("England")))
            )
          )
          .success
          .value

      val result =
        IndividualAmendedViewModel.rows(original, answers)

      val row = result.head

      row.head.content mustBe Text(msgs("addressOfSubcontractor.checkYourAnswersLabel"))
      row(1).content mustBe Text("1 High Street, Leeds, SA1 1AA, England")
      row(2).content mustBe Text("1 HIGH Street, Leeds, SA1 1AA, England")
    }

    "must return a contact method row when the contact method changes" in {

      val answers =
        answersMatchingOriginal
          .set(IndividualChooseContactDetailsPage, ContactOptions.Phone)
          .success
          .value
          .set(AmendedPagesPage,
            Set(IndividualChooseContactDetailsPage)).success
          .value

      val result =
        IndividualAmendedViewModel.rows(original, answers)

      result must have size 2

      val row = result.head

      row.head.content mustBe Text(msgs("individualChooseContactDetails.checkYourAnswersLabel"))
      row(1).content mustBe Text(msgs("individualChooseContactDetails.email"))
      row(2).content mustBe Text(msgs("individualChooseContactDetails.phone"))
    }

    "must return a contact value row when the contact value changes" in {

      val answers =
        answersMatchingOriginal
          .set(IndividualEmailAddressPage, "new@test.com")
          .success
          .value
          .set(AmendedPagesPage,
            Set(IndividualEmailAddressPage)).success
          .value

      val result =
        IndividualAmendedViewModel.rows(original, answers)

      val row = result.head

      row.head.content mustBe Text(msgs("individualEmailAddress.checkYourAnswersLabel"))
      row(1).content mustBe Text("john@test.com")
      row(2).content mustBe Text("new@test.com")
    }

    "must return contact method, email and phone rows when changing from email to phone" in {
      val answers =
        answersMatchingOriginal
          .set(IndividualChooseContactDetailsPage, ContactOptions.Phone)
          .success
          .value
          .set(IndividualPhoneNumberPage, "01131234567")
          .success
          .value
          .set(
            AmendedPagesPage,
            Set(
              IndividualChooseContactDetailsPage,
              IndividualPhoneNumberPage
            )
          )
          .success
          .value

      val result =
        IndividualAmendedViewModel.rows(original, answers)

      result must have size 3

      val methodRow = result(0)
      val oldRow    = result(1)
      val newRow    = result(2)

      methodRow.head.content mustBe Text(msgs("individualChooseContactDetails.checkYourAnswersLabel"))
      methodRow(1).content mustBe Text(msgs("individualChooseContactDetails.email"))
      methodRow(2).content mustBe Text(msgs("individualChooseContactDetails.phone"))

      oldRow.head.content mustBe Text(msgs("individualEmailAddress.checkYourAnswersLabel"))
      oldRow(1).content mustBe Text("john@test.com")
      oldRow(2).content mustBe Text(msgs("individualAmended.table.content.none"))

      newRow.head.content mustBe Text(msgs("individualPhoneNumber.checkYourAnswersLabel"))
      newRow(1).content mustBe Text(msgs("individualAmended.table.content.none"))
      newRow(2).content mustBe Text("01131234567")
    }

    "must display none when changing from email to no details" in {

      val answers =
        answersMatchingOriginal
          .set(IndividualChooseContactDetailsPage, ContactOptions.NoDetails)
          .success
          .value
          .remove(IndividualEmailAddressPage)
          .success
          .value
          .set(AmendedPagesPage,
            Set(IndividualEmailAddressPage, IndividualChooseContactDetailsPage)).success
          .value

      val result =
        IndividualAmendedViewModel.rows(original, answers)

      result must have size 2

      val methodRow = result.head
      val removedValueRow  = result(1)

      methodRow.head.content mustBe Text(msgs("individualChooseContactDetails.checkYourAnswersLabel"))
      methodRow(1).content mustBe Text(msgs("individualChooseContactDetails.email"))
      methodRow(2).content mustBe Text(msgs("individualChooseContactDetails.noDetails"))

      removedValueRow .head.content mustBe Text(msgs("individualEmailAddress.checkYourAnswersLabel"))
      removedValueRow (1).content mustBe Text("john@test.com")
      removedValueRow (2).content mustBe Text(msgs("individualAmended.table.content.none"))
    }

    "must display email address when changing from no details to email" in {

      val originalNoDetails =
        original.copy(
          contactMethod = Some(ContactOptions.NoDetails),
          contactValue = None
        )

      val answers =
        answersMatchingOriginal
          .set(IndividualChooseContactDetailsPage, ContactOptions.Email)
          .success
          .value
          .set(IndividualEmailAddressPage, "new@test.com")
          .success
          .value
          .set(AmendedPagesPage,
            Set(IndividualEmailAddressPage, IndividualChooseContactDetailsPage)).success
          .value

      val result =
        IndividualAmendedViewModel.rows(originalNoDetails, answers)

      result must have size 2

      val addedValueRow = result(1)

      addedValueRow .head.content mustBe Text(msgs("individualEmailAddress.checkYourAnswersLabel"))
      addedValueRow (1).content mustBe Text(msgs("individualAmended.table.content.none"))
      addedValueRow (2).content mustBe Text("new@test.com")
    }

    "must return a phone number row when the original contact method is phone and the phone number changes" in {

      val originalPhone =
        original.copy(
          contactMethod = Some(ContactOptions.Phone),
          contactValue = Some("01131234567")
        )

      val answers =
        answersMatchingOriginal
          .remove(IndividualChooseContactDetailsPage)
          .success
          .value
          .remove(IndividualEmailAddressPage)
          .success
          .value
          .set(IndividualChooseContactDetailsPage, ContactOptions.Phone)
          .success
          .value
          .set(IndividualPhoneNumberPage, "07700900123")
          .success
          .value
          .set(AmendedPagesPage,
            Set(IndividualPhoneNumberPage)).success
          .value

      val result =
        IndividualAmendedViewModel.rows(originalPhone, answers)

      val row = result.head

      row.head.content mustBe Text(msgs("individualPhoneNumber.checkYourAnswersLabel"))
      row(1).content mustBe Text("01131234567")
      row(2).content mustBe Text("07700900123")
    }

    "must return phone and mobile rows when changing from phone to mobile" in {
      val originalPhone =
        original.copy(
          contactMethod = Some(ContactOptions.Phone),
          contactValue = Some("01131234567")
        )

      val answers =
        answersMatchingOriginal
          .remove(IndividualEmailAddressPage)
          .success
          .value
          .set(IndividualChooseContactDetailsPage, ContactOptions.Phone)
          .success
          .value
          .set(IndividualPhoneNumberPage, "01131234567")
          .success
          .value
          .set(IndividualChooseContactDetailsPage, ContactOptions.Mobile)
          .success
          .value
          .remove(IndividualPhoneNumberPage)
          .success
          .value
          .set(IndividualMobileNumberPage, "07700900123")
          .success
          .value
          .set(
            AmendedPagesPage,
            Set(
              IndividualChooseContactDetailsPage,
              IndividualMobileNumberPage
            )
          )
          .success
          .value

      val result =
        IndividualAmendedViewModel.rows(originalPhone, answers)

      result must have size 3

      val methodRow = result(0)
      val phoneRow = result(1)
      val mobileRow = result(2)

      methodRow(1).content mustBe Text(msgs("individualChooseContactDetails.phone"))
      methodRow(2).content mustBe Text(msgs("individualChooseContactDetails.mobile"))

      phoneRow.head.content mustBe Text(msgs("individualPhoneNumber.checkYourAnswersLabel"))
      phoneRow(1).content mustBe Text("01131234567")
      phoneRow(2).content mustBe Text(msgs("individualAmended.table.content.none"))

      mobileRow.head.content mustBe Text(msgs("individualMobileNumber.checkYourAnswersLabel"))
      mobileRow(1).content mustBe Text(msgs("individualAmended.table.content.none"))
      mobileRow(2).content mustBe Text("07700900123")
    }

    "must return a UTR row when the UTR changes" in {

      val answers =
        answersMatchingOriginal
          .set(SubcontractorsUniqueTaxpayerReferencePage, "9999999999")
          .success
          .value
          .set(AmendedPagesPage,
            Set(SubcontractorsUniqueTaxpayerReferencePage)).success
          .value

      val result =
        IndividualAmendedViewModel.rows(original, answers)

      val row = result.head

      row.head.content mustBe Text(msgs("subcontractorsUniqueTaxpayerReference.checkYourAnswersLabel"))
      row(1).content mustBe Text("1234567890")
      row(2).content mustBe Text("9999999999")
    }

    "must display none when the UTR is removed" in {

      val answers =
        answersMatchingOriginal
          .remove(SubcontractorsUniqueTaxpayerReferencePage)
          .success
          .value
          .set(AmendedPagesPage,
            Set(SubcontractorsUniqueTaxpayerReferencePage)).success
          .value

      val result =
        IndividualAmendedViewModel.rows(original, answers)

      val row = result.head

      row.head.content mustBe Text(msgs("subcontractorsUniqueTaxpayerReference.checkYourAnswersLabel"))
      row(1).content mustBe Text("1234567890")
      row(2).content mustBe Text(msgs("individualAmended.table.content.none"))
    }

    "must return a NINO row when the NINO changes" in {

      val answers =
        answersMatchingOriginal
          .set(SubNationalInsuranceNumberPage, "CD123456E")
          .success
          .value
          .set(AmendedPagesPage,
            Set(SubNationalInsuranceNumberPage)).success
          .value

      val result =
        IndividualAmendedViewModel.rows(original, answers)

      val row = result.head

      row.head.content mustBe Text(msgs("subNationalInsuranceNumber.checkYourAnswersLabel"))
      row(1).content mustBe Text("AB123456C")
      row(2).content mustBe Text("CD123456E")
    }

    "must display none when the NINO is removed" in {

      val answers =
        answersMatchingOriginal
          .remove(SubNationalInsuranceNumberPage)
          .success
          .value
          .set(AmendedPagesPage,
            Set(SubNationalInsuranceNumberPage)).success
          .value

      val result =
        IndividualAmendedViewModel.rows(original, answers)

      val row = result.head

      row.head.content mustBe Text(msgs("subNationalInsuranceNumber.checkYourAnswersLabel"))
      row(1).content mustBe Text("AB123456C")
      row(2).content mustBe Text(msgs("individualAmended.table.content.none"))
    }

    "must return a works reference row when the works reference changes" in {

      val answers =
        answersMatchingOriginal
          .set(WorksReferenceNumberPage, "WR999")
          .success
          .value
    .set(AmendedPagesPage,
        Set(WorksReferenceNumberPage)).success
        .value
      val result =
        IndividualAmendedViewModel.rows(original, answers)

      val row = result.head

      row.head.content mustBe Text(msgs("worksReferenceNumber.checkYourAnswersLabel"))
      row(1).content mustBe Text("WR123")
      row(2).content mustBe Text("WR999")
    }

    "must display none when the works reference is removed" in {

      val answers =
        answersMatchingOriginal
          .remove(WorksReferenceNumberPage)
          .success
          .value
          .set(AmendedPagesPage,
            Set(WorksReferenceNumberPage)).success
          .value

      val result =
        IndividualAmendedViewModel.rows(original, answers)

      val row = result.head

      row.head.content mustBe Text(msgs("worksReferenceNumber.checkYourAnswersLabel"))
      row(1).content mustBe Text("WR123")
      row(2).content mustBe Text(msgs("individualAmended.table.content.none"))
    }

    "must return a UTR yes/no row when the answer changes" in {

      val answers =
        answersMatchingOriginal
          .set(UniqueTaxpayerReferenceYesNoPage, false)
          .success
          .value
          .set(AmendedPagesPage,
            Set(UniqueTaxpayerReferenceYesNoPage)).success
          .value

      val result =
        IndividualAmendedViewModel.rows(original, answers)

      result must have size 2

      val yesNoRow = result.head
      val utrRow   = result(1)

      yesNoRow.head.content mustBe Text(msgs("uniqueTaxpayerReferenceYesNo.checkYourAnswersLabel"))
      yesNoRow(1).content mustBe Text(msgs("site.yes"))
      yesNoRow(2).content mustBe Text(msgs("site.no"))

      utrRow.head.content mustBe Text(msgs("subcontractorsUniqueTaxpayerReference.checkYourAnswersLabel"))
      utrRow(1).content mustBe Text("1234567890")
      utrRow(2).content mustBe Text(msgs("individualAmended.table.content.none"))
    }

    "must return a NINO yes/no row when the answer changes" in {

      val answers =
        answersMatchingOriginal
          .set(NationalInsuranceNumberYesNoPage, false)
          .success
          .value
          .set(AmendedPagesPage,
            Set(NationalInsuranceNumberYesNoPage)).success
          .value

      val result =
        IndividualAmendedViewModel.rows(original, answers)

      result must have size 2

      val yesNoRow = result.head
      val ninoRow  = result(1)

      yesNoRow.head.content mustBe Text(msgs("nationalInsuranceNumberYesNo.checkYourAnswersLabel"))
      yesNoRow(1).content mustBe Text(msgs("site.yes"))
      yesNoRow(2).content mustBe Text(msgs("site.no"))

      ninoRow.head.content mustBe Text(msgs("subNationalInsuranceNumber.checkYourAnswersLabel"))
      ninoRow(1).content mustBe Text("AB123456C")
      ninoRow(2).content mustBe Text(msgs("individualAmended.table.content.none"))
    }

    "must return a works reference yes/no row when the answer changes" in {

      val answers =
        answersMatchingOriginal
          .set(WorksReferenceNumberYesNoPage, false)
          .success
          .value
          .set(AmendedPagesPage,
            Set(WorksReferenceNumberYesNoPage)).success
          .value

      val result =
        IndividualAmendedViewModel.rows(original, answers)

      result must have size 2

      val yesNoRow = result.head
      val worksRow = result(1)

      yesNoRow.head.content mustBe Text(msgs("worksReferenceNumberYesNo.checkYourAnswersLabel"))
      yesNoRow(1).content mustBe Text(msgs("site.yes"))
      yesNoRow(2).content mustBe Text(msgs("site.no"))

      worksRow.head.content mustBe Text(msgs("worksReferenceNumber.checkYourAnswersLabel"))
      worksRow(1).content mustBe Text("WR123")
      worksRow(2).content mustBe Text(msgs("individualAmended.table.content.none"))
    }
  }
}
