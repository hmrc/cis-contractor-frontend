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

package pages.amend.partnership

import models.address.{Address, Country}
import models.contact.ContactMethodOptions
import pages.add.partnership.*
import pages.behaviours.PageBehaviours

class AmendPartnershipRemoveDetailYesNoPageSpec extends PageBehaviours {

  "AmendPartnershipRemoveDetailYesNoPage" - {

    val amendPartnershipRemoveDetailYesNoPage =
      AmendPartnershipRemoveDetailYesNoPage("address")

    beRetrievable[Boolean](amendPartnershipRemoveDetailYesNoPage)

    beSettable[Boolean](amendPartnershipRemoveDetailYesNoPage)

    beRemovable[Boolean](amendPartnershipRemoveDetailYesNoPage)

    val address = Address(
      addressLine1 = "line 1",
      addressLine2 = Some("line 2"),
      addressLine3 = Some("line 3"),
      addressLine4 = Some("line 4"),
      postcode = Some("NX1 1AA"),
      country = Some(Country(Some("GB"), Some("United Kingdom")))
    )

    "cleanup: must remove PartnershipAddressPage and set PartnershipAddressYesNoPage to No when Yes is selected" in {

      val userAnswers = emptyUserAnswers
        .set(PartnershipAddressPage, address)
        .success
        .value
        .set(PartnershipAddressYesNoPage, true)
        .success
        .value

      val updatedUserAnswers =
        userAnswers
          .set(AmendPartnershipRemoveDetailYesNoPage("address"), true)
          .success
          .value

      updatedUserAnswers.get(PartnershipAddressPage) mustBe None
      updatedUserAnswers.get(PartnershipAddressYesNoPage) mustBe Some(false)
    }

    "cleanup: must retain PartnershipAddressPage and PartnershipAddressYesNoPage when No is selected" in {

      val userAnswers = emptyUserAnswers
        .set(PartnershipAddressPage, address)
        .success
        .value
        .set(PartnershipAddressYesNoPage, true)
        .success
        .value

      val updatedUserAnswers =
        userAnswers
          .set(AmendPartnershipRemoveDetailYesNoPage("address"), false)
          .success
          .value

      updatedUserAnswers.get(PartnershipAddressPage) mustBe Some(address)
      updatedUserAnswers.get(PartnershipAddressYesNoPage) mustBe Some(true)
    }

    "cleanup: must remove all contact details and set AddPartnershipContactMethodsYesNoPage to No when Yes is selected" in {

      val userAnswers = emptyUserAnswers
        .set(
          PartnershipContactMethodOptionsPage,
          Set(
            ContactMethodOptions.Email,
            ContactMethodOptions.Phone,
            ContactMethodOptions.Mobile
          )
        )
        .success
        .value
        .set(PartnershipEmailAddressPage, "old@email.com")
        .success
        .value
        .set(PartnershipPhoneNumberPage, "01234567890")
        .success
        .value
        .set(PartnershipMobileNumberPage, "01234567890")
        .success
        .value
        .set(AddPartnershipContactMethodsYesNoPage, true)
        .success
        .value

      val updatedUserAnswers =
        userAnswers
          .set(AmendPartnershipRemoveDetailYesNoPage("contact-details"), true)
          .success
          .value

      updatedUserAnswers.get(PartnershipContactMethodOptionsPage) mustBe None
      updatedUserAnswers.get(PartnershipEmailAddressPage) mustBe None
      updatedUserAnswers.get(PartnershipPhoneNumberPage) mustBe None
      updatedUserAnswers.get(PartnershipMobileNumberPage) mustBe None
      updatedUserAnswers.get(AddPartnershipContactMethodsYesNoPage) mustBe Some(false)
    }

    "cleanup: must retain all contact details when No is selected" in {

      val userAnswers = emptyUserAnswers
        .set(
          PartnershipContactMethodOptionsPage,
          Set(
            ContactMethodOptions.Email,
            ContactMethodOptions.Phone,
            ContactMethodOptions.Mobile
          )
        )
        .success
        .value
        .set(PartnershipEmailAddressPage, "old@email.com")
        .success
        .value
        .set(PartnershipPhoneNumberPage, "01234567890")
        .success
        .value
        .set(PartnershipMobileNumberPage, "01234567890")
        .success
        .value
        .set(AddPartnershipContactMethodsYesNoPage, true)
        .success
        .value

      val updatedUserAnswers =
        userAnswers
          .set(AmendPartnershipRemoveDetailYesNoPage("contact-details"), false)
          .success
          .value

      updatedUserAnswers.get(PartnershipContactMethodOptionsPage) mustBe Some(
        Set(
          ContactMethodOptions.Email,
          ContactMethodOptions.Phone,
          ContactMethodOptions.Mobile
        )
      )
      updatedUserAnswers.get(PartnershipEmailAddressPage) mustBe Some("old@email.com")
      updatedUserAnswers.get(PartnershipPhoneNumberPage) mustBe Some("01234567890")
      updatedUserAnswers.get(PartnershipMobileNumberPage) mustBe Some("01234567890")
      updatedUserAnswers.get(AddPartnershipContactMethodsYesNoPage) mustBe Some(true)
    }

    Seq(
      (
        "utr",
        PartnershipUniqueTaxpayerReferencePage,
        PartnershipHasUtrYesNoPage,
        "7777777777"
      ),
      (
        "works-reference-number",
        PartnershipWorksReferenceNumberPage,
        PartnershipWorksReferenceNumberYesNoPage,
        "WR-001"
      ),
      (
        "nominated-partner-utr",
        PartnershipNominatedPartnerUtrPage,
        PartnershipNominatedPartnerUtrYesNoPage,
        "7777777777"
      ),
      (
        "nominated-partner-nino",
        PartnershipNominatedPartnerNinoPage,
        PartnershipNominatedPartnerNinoYesNoPage,
        "AA123456A"
      ),
      (
        "nominated-partner-company-registration-number",
        PartnershipNominatedPartnerCrnPage,
        PartnershipNominatedPartnerCrnYesNoPage,
        "12345678"
      )
    ).foreach {
      case (detail, detailPage, yesNoPage, dummyDetail) =>

        s"cleanup: must remove $detailPage and set $yesNoPage to No when Yes is selected" in {

          val userAnswers = emptyUserAnswers
            .set(detailPage, dummyDetail)
            .success
            .value
            .set(yesNoPage, true)
            .success
            .value

          val updatedUserAnswers =
            userAnswers
              .set(AmendPartnershipRemoveDetailYesNoPage(detail), true)
              .success
              .value

          updatedUserAnswers.get(detailPage) mustBe None
          updatedUserAnswers.get(yesNoPage) mustBe Some(false)
        }

        s"cleanup: must retain $detailPage and keep $yesNoPage as Yes when No is selected" in {

          val userAnswers = emptyUserAnswers
            .set(detailPage, dummyDetail)
            .success
            .value
            .set(yesNoPage, true)
            .success
            .value

          val updatedUserAnswers =
            userAnswers
              .set(AmendPartnershipRemoveDetailYesNoPage(detail), false)
              .success
              .value

          updatedUserAnswers.get(detailPage) mustBe Some(dummyDetail)
          updatedUserAnswers.get(yesNoPage) mustBe Some(true)
        }
    }
  }
}
