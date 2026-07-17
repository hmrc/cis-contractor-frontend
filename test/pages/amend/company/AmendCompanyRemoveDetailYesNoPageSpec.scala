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

package pages.amend.company

import models.address.{Address, Country}
import models.contact.ContactMethodOptions
import pages.add.company.*
import pages.behaviours.PageBehaviours

class AmendCompanyRemoveDetailYesNoPageSpec extends PageBehaviours {

  "AmendCompanyRemoveDetailYesNoPage" - {

    val amendCompanyRemoveDetailYesNoPage = AmendCompanyRemoveDetailYesNoPage("address")

    beRetrievable[Boolean](amendCompanyRemoveDetailYesNoPage)

    beSettable[Boolean](amendCompanyRemoveDetailYesNoPage)

    beRemovable[Boolean](amendCompanyRemoveDetailYesNoPage)

    val address = Address(
      addressLine1 = "line 1",
      addressLine2 = Some("line 2"),
      addressLine3 = Some("line 3"),
      addressLine4 = Some("line 4"),
      postcode = Some("NX1 1AA"),
      country = Some(Country(Some("GB"), Some("United Kingdom")))
    )

    "cleanup: must remove CompanyAddressPage userAnswers and set CompanyAddressYesNoPage to No when Yes is selected" in {
      val userAnswers = emptyUserAnswers
        .set(CompanyAddressPage, address)
        .success
        .value
        .set(CompanyAddressYesNoPage, true)
        .success
        .value

      val updatedUserAnswers =
        userAnswers.set(AmendCompanyRemoveDetailYesNoPage("address"), true).success.value

      updatedUserAnswers.get(CompanyAddressPage) mustBe None
      updatedUserAnswers.get(CompanyAddressYesNoPage) mustBe Some(false)
    }

    "cleanup: must retain CompanyAddressPage userAnswers and keep CompanyAddressYesNoPage as Yes when No is selected" in {
      val userAnswers = emptyUserAnswers
        .set(CompanyAddressPage, address)
        .success
        .value
        .set(CompanyAddressYesNoPage, true)
        .success
        .value

      val updatedUserAnswers =
        userAnswers.set(AmendCompanyRemoveDetailYesNoPage("address"), false).success.value

      updatedUserAnswers.get(CompanyAddressPage) mustBe Some(address)
      updatedUserAnswers.get(CompanyAddressYesNoPage) mustBe Some(true)
    }

    "cleanup: must remove all methods of contact userAnswers and set AddCompanyContactMethodsYesNoPage to No when Yes is selected" in {
      val userAnswers = emptyUserAnswers
        .set(
          CompanyContactMethodOptionsPage,
          Set(ContactMethodOptions.Email, ContactMethodOptions.Phone, ContactMethodOptions.Mobile)
        )
        .success
        .value
        .set(CompanyEmailAddressPage, "old@email.com")
        .success
        .value
        .set(CompanyPhoneNumberPage, "01234567890")
        .success
        .value
        .set(CompanyMobileNumberPage, "01234567890")
        .success
        .value
        .set(AddCompanyContactMethodsYesNoPage, true)
        .success
        .value

      val updatedUserAnswers =
        userAnswers.set(AmendCompanyRemoveDetailYesNoPage("contact-details"), true).success.value

      updatedUserAnswers.get(CompanyContactMethodOptionsPage) mustBe None
      updatedUserAnswers.get(CompanyEmailAddressPage) mustBe None
      updatedUserAnswers.get(CompanyPhoneNumberPage) mustBe None
      updatedUserAnswers.get(CompanyMobileNumberPage) mustBe None
      updatedUserAnswers.get(AddCompanyContactMethodsYesNoPage) mustBe Some(false)
    }

    "cleanup: must retain all methods of contact userAnswers and keep AddCompanyContactMethodsYesNoPage as Yes when No is selected" in {
      val userAnswers = emptyUserAnswers
        .set(
          CompanyContactMethodOptionsPage,
          Set(ContactMethodOptions.Email, ContactMethodOptions.Phone, ContactMethodOptions.Mobile)
        )
        .success
        .value
        .set(CompanyEmailAddressPage, "old@email.com")
        .success
        .value
        .set(CompanyPhoneNumberPage, "01234567890")
        .success
        .value
        .set(CompanyMobileNumberPage, "01234567890")
        .success
        .value
        .set(AddCompanyContactMethodsYesNoPage, true)
        .success
        .value

      val updatedUserAnswers =
        userAnswers.set(AmendCompanyRemoveDetailYesNoPage("contact-details"), false).success.value

      updatedUserAnswers.get(CompanyContactMethodOptionsPage) mustBe Some(
        Set(ContactMethodOptions.Email, ContactMethodOptions.Phone, ContactMethodOptions.Mobile)
      )
      updatedUserAnswers.get(CompanyEmailAddressPage) mustBe Some("old@email.com")
      updatedUserAnswers.get(CompanyPhoneNumberPage) mustBe Some("01234567890")
      updatedUserAnswers.get(CompanyMobileNumberPage) mustBe Some("01234567890")
      updatedUserAnswers.get(AddCompanyContactMethodsYesNoPage) mustBe Some(true)
    }

    Seq(
      ("unique-taxpayer-reference", CompanyUtrPage, CompanyUtrYesNoPage, "7777777777"),
      ("company-registration-number", CompanyCrnPage, CompanyCrnYesNoPage, "1234567890"),
      ("works-reference-number", CompanyWorksReferencePage, CompanyWorksReferenceYesNoPage, "WR-001")
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
            userAnswers.set(AmendCompanyRemoveDetailYesNoPage(contractorDetail), true).success.value

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
            userAnswers.set(AmendCompanyRemoveDetailYesNoPage(contractorDetail), false).success.value

          updatedUserAnswers.get(selectedDetailPage) mustBe Some(dummyDetail)
          updatedUserAnswers.get(screenerPage) mustBe Some(true)
        }
      }
    }
  }
}
