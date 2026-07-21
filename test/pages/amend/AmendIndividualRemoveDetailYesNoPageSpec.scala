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

package pages.amend

import models.address.{Address, Country}
import models.contact.ContactMethodOptions
import pages.add.*
import pages.behaviours.PageBehaviours

class AmendIndividualRemoveDetailYesNoPageSpec extends PageBehaviours {

  "AmendIndividualRemoveDetailYesNoPage" - {

    val amendIndividualRemoveDetailYesNoPage = AmendIndividualRemoveDetailYesNoPage("address")

    beRetrievable[Boolean](amendIndividualRemoveDetailYesNoPage)

    beSettable[Boolean](amendIndividualRemoveDetailYesNoPage)

    beRemovable[Boolean](amendIndividualRemoveDetailYesNoPage)

    val address = Address(
      addressLine1 = "line 1",
      addressLine2 = Some("line 2"),
      addressLine3 = Some("line 3"),
      addressLine4 = Some("line 4"),
      postcode = Some("NX1 1AA"),
      country = Some(Country(Some("GB"), Some("United Kingdom")))
    )

    "cleanup: must remove AddressOfSubcontractorPage userAnswers and set SubAddressYesNoPage to No when Yes is selected" in {
      val userAnswers = emptyUserAnswers
        .set(AddressOfSubcontractorPage, address)
        .success
        .value
        .set(SubAddressYesNoPage, true)
        .success
        .value

      val updatedUserAnswers =
        userAnswers.set(AmendIndividualRemoveDetailYesNoPage("address"), true).success.value

      updatedUserAnswers.get(AddressOfSubcontractorPage) mustBe None
      updatedUserAnswers.get(SubAddressYesNoPage) mustBe Some(false)
    }

    "cleanup: must retain AddressOfSubcontractorPage userAnswers and keep SubAddressYesNoPage as Yes when No is selected" in {
      val userAnswers = emptyUserAnswers
        .set(AddressOfSubcontractorPage, address)
        .success
        .value
        .set(SubAddressYesNoPage, true)
        .success
        .value

      val updatedUserAnswers =
        userAnswers.set(AmendIndividualRemoveDetailYesNoPage("address"), false).success.value

      updatedUserAnswers.get(AddressOfSubcontractorPage) mustBe Some(address)
      updatedUserAnswers.get(SubAddressYesNoPage) mustBe Some(true)
    }

    "cleanup: must remove all methods of contact userAnswers and set AddIndividualContactMethodsYesNoPage to No when Yes is selected" in {
      val userAnswers = emptyUserAnswers
        .set(
          IndividualContactMethodOptionsPage,
          Set(ContactMethodOptions.Email, ContactMethodOptions.Phone, ContactMethodOptions.Mobile)
        )
        .success
        .value
        .set(IndividualEmailAddressPage, "old@email.com")
        .success
        .value
        .set(IndividualPhoneNumberPage, "01234567890")
        .success
        .value
        .set(IndividualMobileNumberPage, "01234567890")
        .success
        .value
        .set(AddIndividualContactMethodsYesNoPage, true)
        .success
        .value

      val updatedUserAnswers =
        userAnswers.set(AmendIndividualRemoveDetailYesNoPage("contact-details"), true).success.value

      updatedUserAnswers.get(IndividualContactMethodOptionsPage) mustBe None
      updatedUserAnswers.get(IndividualEmailAddressPage) mustBe None
      updatedUserAnswers.get(IndividualPhoneNumberPage) mustBe None
      updatedUserAnswers.get(IndividualMobileNumberPage) mustBe None
      updatedUserAnswers.get(AddIndividualContactMethodsYesNoPage) mustBe Some(false)
    }

    "cleanup: must retain all methods of contact userAnswers and keep AddIndividualContactMethodsYesNoPage as Yes when No is selected" in {
      val userAnswers = emptyUserAnswers
        .set(
          IndividualContactMethodOptionsPage,
          Set(ContactMethodOptions.Email, ContactMethodOptions.Phone, ContactMethodOptions.Mobile)
        )
        .success
        .value
        .set(IndividualEmailAddressPage, "old@email.com")
        .success
        .value
        .set(IndividualPhoneNumberPage, "01234567890")
        .success
        .value
        .set(IndividualMobileNumberPage, "01234567890")
        .success
        .value
        .set(AddIndividualContactMethodsYesNoPage, true)
        .success
        .value

      val updatedUserAnswers =
        userAnswers.set(AmendIndividualRemoveDetailYesNoPage("contact-details"), false).success.value

      updatedUserAnswers.get(IndividualContactMethodOptionsPage) mustBe Some(
        Set(ContactMethodOptions.Email, ContactMethodOptions.Phone, ContactMethodOptions.Mobile)
      )
      updatedUserAnswers.get(IndividualEmailAddressPage) mustBe Some("old@email.com")
      updatedUserAnswers.get(IndividualPhoneNumberPage) mustBe Some("01234567890")
      updatedUserAnswers.get(IndividualMobileNumberPage) mustBe Some("01234567890")
      updatedUserAnswers.get(AddIndividualContactMethodsYesNoPage) mustBe Some(true)
    }

    Seq(
      ("trading-name", TradingNameOfSubcontractorPage, SubTradingNameYesNoPage, "Test name"),
      (
        "unique-taxpayer-reference",
        SubcontractorsUniqueTaxpayerReferencePage,
        UniqueTaxpayerReferenceYesNoPage,
        "7777777777"
      ),
      ("national-insurance-number", SubNationalInsuranceNumberPage, NationalInsuranceNumberYesNoPage, "AA123456A"),
      ("works-reference-number", WorksReferenceNumberPage, WorksReferenceNumberYesNoPage, "WR-001")
    ).foreach { case (contractorDetail, selectedDetailPage, screenerPage, dummyDetail) =>
      s"when contractorDetail is '$contractorDetail'" - {

        s"cleanup: must remove '$selectedDetailPage' userAnswers and set '$screenerPage' to No when Yes is selected" in {
          val userAnswers = emptyUserAnswers
            .set(selectedDetailPage, dummyDetail)
            .success
            .value
            .set(screenerPage, true)
            .success
            .value

          val updatedUserAnswers =
            userAnswers.set(AmendIndividualRemoveDetailYesNoPage(contractorDetail), true).success.value

          updatedUserAnswers.get(selectedDetailPage) mustBe None
          updatedUserAnswers.get(screenerPage) mustBe Some(false)
        }

        s"cleanup: must retain '$selectedDetailPage' userAnswers and keep '$screenerPage' as Yes when No is selected" in {
          val userAnswers = emptyUserAnswers
            .set(selectedDetailPage, dummyDetail)
            .success
            .value
            .set(screenerPage, true)
            .success
            .value

          val updatedUserAnswers =
            userAnswers.set(AmendIndividualRemoveDetailYesNoPage(contractorDetail), false).success.value

          updatedUserAnswers.get(selectedDetailPage) mustBe Some(dummyDetail)
          updatedUserAnswers.get(screenerPage) mustBe Some(true)
        }
      }
    }
  }
}
