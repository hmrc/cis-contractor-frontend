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

package pages.amend.trust

import models.address.{Address, Country}
import models.contact.ContactMethodOptions
import pages.add.trust.*
import pages.behaviours.PageBehaviours

class AmendTrustRemoveDetailYesNoPageSpec extends PageBehaviours {
  "AmendTrustRemoveDetailYesNoPage" - {

    val amendTrustRemoveDetailYesNoPage = AmendTrustRemoveDetailYesNoPage("address")

    beRetrievable[Boolean](amendTrustRemoveDetailYesNoPage)

    beSettable[Boolean](amendTrustRemoveDetailYesNoPage)

    beRemovable[Boolean](amendTrustRemoveDetailYesNoPage)

    val address = Address(
      addressLine1 = "line 1",
      addressLine2 = Some("line 2"),
      addressLine3 = Some("line 3"),
      addressLine4 = Some("line 4"),
      postcode = Some("NX1 1AA"),
      country = Some(Country(Some("GB"), Some("United Kingdom")))
    )

    "cleanup: must remove TrustAddressPage userAnswers and set TrustAddressYesNoPage to No when Yes is selected" in {
      val userAnswers = emptyUserAnswers
        .set(TrustAddressPage, address)
        .success
        .value
        .set(TrustAddressYesNoPage, true)
        .success
        .value

      val updatedUserAnswers =
        userAnswers.set(AmendTrustRemoveDetailYesNoPage("address"), true).success.value

      updatedUserAnswers.get(TrustAddressPage) mustBe None
      updatedUserAnswers.get(TrustAddressYesNoPage) mustBe Some(false)
    }

    "cleanup: must retain TrustAddressPage userAnswers and keep TrustAddressYesNoPage as Yes when No is selected" in {
      val userAnswers = emptyUserAnswers
        .set(TrustAddressPage, address)
        .success
        .value
        .set(TrustAddressYesNoPage, true)
        .success
        .value

      val updatedUserAnswers =
        userAnswers.set(AmendTrustRemoveDetailYesNoPage("address"), false).success.value

      updatedUserAnswers.get(TrustAddressPage) mustBe Some(address)
      updatedUserAnswers.get(TrustAddressYesNoPage) mustBe Some(true)
    }

    "cleanup: must remove all methods of contact userAnswers and set AddTrustContactMethodsYesNoPage to No when Yes is selected" in {
      val userAnswers = emptyUserAnswers
        .set(
          TrustContactMethodOptionsPage,
          Set(ContactMethodOptions.Email, ContactMethodOptions.Phone, ContactMethodOptions.Mobile)
        )
        .success
        .value
        .set(TrustEmailAddressPage, "old@email.com")
        .success
        .value
        .set(TrustPhoneNumberPage, "01234567890")
        .success
        .value
        .set(TrustMobileNumberPage, "01234567890")
        .success
        .value
        .set(AddTrustContactMethodsYesNoPage, true)
        .success
        .value

      val updatedUserAnswers =
        userAnswers.set(AmendTrustRemoveDetailYesNoPage("contact-details"), true).success.value

      updatedUserAnswers.get(TrustContactMethodOptionsPage) mustBe None
      updatedUserAnswers.get(TrustEmailAddressPage) mustBe None
      updatedUserAnswers.get(TrustPhoneNumberPage) mustBe None
      updatedUserAnswers.get(TrustMobileNumberPage) mustBe None
      updatedUserAnswers.get(AddTrustContactMethodsYesNoPage) mustBe Some(false)
    }

    "cleanup: must retain all methods of contact userAnswers and keep AddTrustContactMethodsYesNoPage as Yes when No is selected" in {
      val userAnswers = emptyUserAnswers
        .set(
          TrustContactMethodOptionsPage,
          Set(ContactMethodOptions.Email, ContactMethodOptions.Phone, ContactMethodOptions.Mobile)
        )
        .success
        .value
        .set(TrustEmailAddressPage, "old@email.com")
        .success
        .value
        .set(TrustPhoneNumberPage, "01234567890")
        .success
        .value
        .set(TrustMobileNumberPage, "01234567890")
        .success
        .value
        .set(AddTrustContactMethodsYesNoPage, true)
        .success
        .value

      val updatedUserAnswers =
        userAnswers.set(AmendTrustRemoveDetailYesNoPage("contact-details"), false).success.value

      updatedUserAnswers.get(TrustContactMethodOptionsPage) mustBe Some(
        Set(ContactMethodOptions.Email, ContactMethodOptions.Phone, ContactMethodOptions.Mobile)
      )
      updatedUserAnswers.get(TrustEmailAddressPage) mustBe Some("old@email.com")
      updatedUserAnswers.get(TrustPhoneNumberPage) mustBe Some("01234567890")
      updatedUserAnswers.get(TrustMobileNumberPage) mustBe Some("01234567890")
      updatedUserAnswers.get(AddTrustContactMethodsYesNoPage) mustBe Some(true)
    }

    Seq(
      ("unique-taxpayer-reference", TrustUtrPage, TrustUtrYesNoPage, "7777777777"),
      ("works-reference-number", TrustWorksReferencePage, TrustWorksReferenceYesNoPage, "WR-001")
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
            userAnswers.set(AmendTrustRemoveDetailYesNoPage(contractorDetail), true).success.value

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
            userAnswers.set(AmendTrustRemoveDetailYesNoPage(contractorDetail), false).success.value

          updatedUserAnswers.get(selectedDetailPage) mustBe Some(dummyDetail)
          updatedUserAnswers.get(screenerPage) mustBe Some(true)
        }
      }
    }
  }
}
