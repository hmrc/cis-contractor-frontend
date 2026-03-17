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

package pages.add

import models.add.*
import models.contact.ContactOptions
import pages.add.partnership.*
import pages.behaviours.PageBehaviours

class CleanupSpec extends PageBehaviours {

  private class TestCleanup extends Cleanup
  private val cleanup = new TestCleanup

  "Cleanup trait" - {

    val taxpayerReference = "5860920998"

    val nationalInsuranceNumber = "AA123456A"

    val worksReferenceNumber = "1234567-AB"

    val crn = "AC012345"

    val utr = "5860920998"

    val email = "abc@bbb.com"

    val name = "John"

    val address = InternationalAddress(
      addressLine1 = "value 1",
      addressLine2 = Some("value 2"),
      addressLine3 = "value 3",
      addressLine4 = Some("value 4"),
      postalCode = "NX1 1AA",
      country = "United Kingdom"
    )

    val phoneNumber = "01234567"

    "removeIndividualSoleTraderSubcontractor" - {

      val soleTraderAddress = UKAddress(
        addressLine1 = "value 1",
        addressLine2 = Some("value 2"),
        addressLine3 = "value 3",
        addressLine4 = Some("value 4"),
        postCode = "NX1 1AA"
      )

      val contactDetails = SubContactDetails(
        email = "value 1",
        telephone = "value 2"
      )

      val subcontractorName = SubcontractorName(
        firstName = "John",
        middleName = Some("Paul"),
        lastName = "Smith"
      )

      "remove all IndividualSoleTraderSubcontractor journey answers" in {

        val userAnswers =
          emptyUserAnswers
            .set(AddressOfSubcontractorPage, soleTraderAddress)
            .success
            .value
            .set(NationalInsuranceNumberYesNoPage, true)
            .success
            .value
            .set(SubAddressYesNoPage, true)
            .success
            .value
            .set(SubContactDetailsPage, contactDetails)
            .success
            .value
            .set(SubcontractorNamePage, subcontractorName)
            .success
            .value
            .set(SubcontractorsUniqueTaxpayerReferencePage, taxpayerReference)
            .success
            .value
            .set(SubNationalInsuranceNumberPage, nationalInsuranceNumber)
            .success
            .value
            .set(SubTradingNameYesNoPage, true)
            .success
            .value
            .set(TradingNameOfSubcontractorPage, name)
            .success
            .value
            .set(UniqueTaxpayerReferenceYesNoPage, true)
            .success
            .value
            .set(WorksReferenceNumberPage, worksReferenceNumber)
            .success
            .value
            .set(WorksReferenceNumberYesNoPage, true)
            .success
            .value
            .set(IndividualPhoneNumberPage, phoneNumber)
            .success
            .value

        val result = cleanup.removeIndividualSoleTraderSubcontractor(userAnswers).success.value

        result.get(AddressOfSubcontractorPage) mustBe None
        result.get(NationalInsuranceNumberYesNoPage) mustBe None
        result.get(SubAddressYesNoPage) mustBe None
        result.get(SubContactDetailsPage) mustBe None
        result.get(SubcontractorNamePage) mustBe None
        result.get(SubcontractorsUniqueTaxpayerReferencePage) mustBe None
        result.get(SubNationalInsuranceNumberPage) mustBe None
        result.get(SubTradingNameYesNoPage) mustBe None
        result.get(TradingNameOfSubcontractorPage) mustBe None
        result.get(UniqueTaxpayerReferenceYesNoPage) mustBe None
        result.get(WorksReferenceNumberPage) mustBe None
        result.get(WorksReferenceNumberYesNoPage) mustBe None
        result.get(IndividualPhoneNumberPage) mustBe None
      }
    }

    "removePartnershipSubcontractor" - {

      "remove all PartnershipSubcontractor journey answers" in {

        val userAnswers =
          emptyUserAnswers
            .set(PartnershipAddressPage, address)
            .success
            .value
            .set(PartnershipAddressYesNoPage, true)
            .success
            .value
            .set(PartnershipChooseContactDetailsPage, ContactOptions.Email)
            .success
            .value
            .set(PartnershipEmailAddressPage, email)
            .success
            .value
            .set(PartnershipHasUtrYesNoPage, true)
            .success
            .value
            .set(PartnershipMobileNumberPage, phoneNumber)
            .success
            .value
            .set(PartnershipNamePage, name)
            .success
            .value
            .set(PartnershipNominatedPartnerCrnPage, crn)
            .success
            .value
            .set(PartnershipNominatedPartnerCrnYesNoPage, true)
            .success
            .value
            .set(PartnershipNominatedPartnerNamePage, "John")
            .success
            .value
            .set(PartnershipNominatedPartnerNinoPage, nationalInsuranceNumber)
            .success
            .value
            .set(PartnershipNominatedPartnerNinoYesNoPage, true)
            .success
            .value
            .set(PartnershipNominatedPartnerUtrPage, utr)
            .success
            .value
            .set(PartnershipNominatedPartnerUtrYesNoPage, true)
            .success
            .value
            .set(PartnershipPhoneNumberPage, phoneNumber)
            .success
            .value
            .set(PartnershipUniqueTaxpayerReferencePage, taxpayerReference)
            .success
            .value
            .set(PartnershipWorksReferenceNumberPage, worksReferenceNumber)
            .success
            .value
            .set(PartnershipWorksReferenceNumberYesNoPage, true)
            .success
            .value

        val result = cleanup.removePartnershipSubcontractor(userAnswers).success.value

        result.get(PartnershipAddressPage) mustBe None
        result.get(PartnershipAddressYesNoPage) mustBe None
        result.get(PartnershipChooseContactDetailsPage) mustBe None
        result.get(PartnershipEmailAddressPage) mustBe None
        result.get(PartnershipHasUtrYesNoPage) mustBe None
        result.get(PartnershipMobileNumberPage) mustBe None
        result.get(PartnershipNamePage) mustBe None
        result.get(PartnershipNominatedPartnerCrnPage) mustBe None
        result.get(PartnershipNominatedPartnerCrnYesNoPage) mustBe None
        result.get(PartnershipNominatedPartnerNamePage) mustBe None
        result.get(PartnershipNominatedPartnerNinoPage) mustBe None
        result.get(PartnershipNominatedPartnerNinoYesNoPage) mustBe None
        result.get(PartnershipNominatedPartnerUtrPage) mustBe None
        result.get(PartnershipNominatedPartnerUtrYesNoPage) mustBe None
        result.get(PartnershipPhoneNumberPage) mustBe None
        result.get(PartnershipUniqueTaxpayerReferencePage) mustBe None
        result.get(PartnershipWorksReferenceNumberPage) mustBe None
        result.get(PartnershipWorksReferenceNumberYesNoPage) mustBe None
      }
    }
  }
}
