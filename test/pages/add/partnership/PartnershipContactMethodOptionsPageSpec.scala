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

package pages.add.partnership

import models.add.partnership.PartnershipContactMethodOptions
import models.contact.ContactMethodOptions
import pages.behaviours.PageBehaviours

class PartnershipContactMethodOptionsPageSpec extends PageBehaviours {
  "PartnershipContactMethodOptionsPage" - {

    beRetrievable[Set[PartnershipContactMethodOptions]](PartnershipContactMethodOptionsPage)

    beSettable[Set[PartnershipContactMethodOptions]](PartnershipContactMethodOptionsPage)

    beRemovable[Set[PartnershipContactMethodOptions]](PartnershipContactMethodOptionsPage)

    "cleanup" - {

      val userAnswers = emptyUserAnswers
        .set(PartnershipEmailAddressPage, "old@email.com")
        .success
        .value
        .set(PartnershipPhoneNumberPage, "01234567890")
        .success
        .value
        .set(PartnershipMobileNumberPage, "1111111111")
        .success
        .value

      "must remove PartnershipPhoneNumberPage and PartnershipMobileNumberPage when Email is selected" in {
        val updatedUserAnswers =
          userAnswers.set(PartnershipContactMethodOptionsPage, Set(ContactMethodOptions.Email)).success.value

        updatedUserAnswers.get(PartnershipEmailAddressPage) mustBe Some("old@email.com")
        updatedUserAnswers.get(PartnershipPhoneNumberPage) mustBe None
        updatedUserAnswers.get(PartnershipMobileNumberPage) mustBe None

      }

      "must remove PartnershipEmailAddressPage and PartnershipMobileNumberPage when Phone is selected" in {
        val updatedUserAnswers =
          userAnswers.set(PartnershipContactMethodOptionsPage, Set(ContactMethodOptions.Phone)).success.value

        updatedUserAnswers.get(PartnershipEmailAddressPage) mustBe None
        updatedUserAnswers.get(PartnershipPhoneNumberPage) mustBe Some("01234567890")
        updatedUserAnswers.get(PartnershipMobileNumberPage) mustBe None
      }

      "must remove PartnershipEmailAddressPage and PartnershipPhoneNumberPage when Mobile is selected" in {
        val updatedUserAnswers =
          userAnswers.set(PartnershipContactMethodOptionsPage, Set(ContactMethodOptions.Mobile)).success.value

        updatedUserAnswers.get(PartnershipEmailAddressPage) mustBe None
        updatedUserAnswers.get(PartnershipPhoneNumberPage) mustBe None
        updatedUserAnswers.get(PartnershipMobileNumberPage) mustBe Some("1111111111")
      }

      "must remove PartnershipMobileNumberPage when Email and Phone is selected" in {
        val updatedUserAnswers =
          userAnswers
            .set(PartnershipContactMethodOptionsPage, Set(ContactMethodOptions.Email, ContactMethodOptions.Phone))
            .success
            .value

        updatedUserAnswers.get(PartnershipEmailAddressPage) mustBe Some("old@email.com")
        updatedUserAnswers.get(PartnershipPhoneNumberPage) mustBe Some("01234567890")
        updatedUserAnswers.get(PartnershipMobileNumberPage) mustBe None
      }

      "must remove PartnershipPhoneNumberPage when Email and Mobile is selected" in {
        val updatedUserAnswers =
          userAnswers
            .set(PartnershipContactMethodOptionsPage, Set(ContactMethodOptions.Email, ContactMethodOptions.Mobile))
            .success
            .value

        updatedUserAnswers.get(PartnershipEmailAddressPage) mustBe Some("old@email.com")
        updatedUserAnswers.get(PartnershipPhoneNumberPage) mustBe None
        updatedUserAnswers.get(PartnershipMobileNumberPage) mustBe Some("1111111111")
      }

      "must remove PartnershipEmailAddressPage when Phone and Mobile is selected" in {
        val updatedUserAnswers =
          userAnswers
            .set(PartnershipContactMethodOptionsPage, Set(ContactMethodOptions.Phone, ContactMethodOptions.Mobile))
            .success
            .value

        updatedUserAnswers.get(PartnershipEmailAddressPage) mustBe None
        updatedUserAnswers.get(PartnershipPhoneNumberPage) mustBe Some("01234567890")
        updatedUserAnswers.get(PartnershipMobileNumberPage) mustBe Some("1111111111")
      }
    }
  }
}
