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

package pages.add.trust

import models.add.trust.TrustContactMethodOptions
import models.contact.ContactMethodOptions
import pages.behaviours.PageBehaviours

class TrustContactMethodOptionsPageSpec extends PageBehaviours {
  "TrustContactMethodOptionsPage" - {

    beRetrievable[Set[TrustContactMethodOptions]](TrustContactMethodOptionsPage)

    beSettable[Set[TrustContactMethodOptions]](TrustContactMethodOptionsPage)

    beRemovable[Set[TrustContactMethodOptions]](TrustContactMethodOptionsPage)

    "cleanup" - {

      val userAnswers = emptyUserAnswers
        .set(TrustEmailAddressPage, "old@email.com")
        .success
        .value
        .set(TrustPhoneNumberPage, "01234567890")
        .success
        .value
        .set(TrustMobileNumberPage, "1111111111")
        .success
        .value

      "must remove TrustPhoneNumberPage and TrustMobileNumberPage when Email is selected" in {
        val updatedUserAnswers =
          userAnswers.set(TrustContactMethodOptionsPage, Set(ContactMethodOptions.Email)).success.value

        updatedUserAnswers.get(TrustEmailAddressPage) mustBe Some("old@email.com")
        updatedUserAnswers.get(TrustPhoneNumberPage) mustBe None
        updatedUserAnswers.get(TrustMobileNumberPage) mustBe None

      }

      "must remove TrustEmailAddressPage and TrustMobileNumberPage when Phone is selected" in {
        val updatedUserAnswers =
          userAnswers.set(TrustContactMethodOptionsPage, Set(ContactMethodOptions.Phone)).success.value

        updatedUserAnswers.get(TrustEmailAddressPage) mustBe None
        updatedUserAnswers.get(TrustPhoneNumberPage) mustBe Some("01234567890")
        updatedUserAnswers.get(TrustMobileNumberPage) mustBe None
      }

      "must remove TrustEmailAddressPage and TrustPhoneNumberPage when Mobile is selected" in {
        val updatedUserAnswers =
          userAnswers.set(TrustContactMethodOptionsPage, Set(ContactMethodOptions.Mobile)).success.value

        updatedUserAnswers.get(TrustEmailAddressPage) mustBe None
        updatedUserAnswers.get(TrustPhoneNumberPage) mustBe None
        updatedUserAnswers.get(TrustMobileNumberPage) mustBe Some("1111111111")
      }

      "must remove TrustMobileNumberPage when Email and Phone is selected" in {
        val updatedUserAnswers =
          userAnswers
            .set(TrustContactMethodOptionsPage, Set(ContactMethodOptions.Email, ContactMethodOptions.Phone))
            .success
            .value

        updatedUserAnswers.get(TrustEmailAddressPage) mustBe Some("old@email.com")
        updatedUserAnswers.get(TrustPhoneNumberPage) mustBe Some("01234567890")
        updatedUserAnswers.get(TrustMobileNumberPage) mustBe None
      }

      "must remove TrustPhoneNumberPage when Email and Mobile is selected" in {
        val updatedUserAnswers =
          userAnswers
            .set(TrustContactMethodOptionsPage, Set(ContactMethodOptions.Email, ContactMethodOptions.Mobile))
            .success
            .value

        updatedUserAnswers.get(TrustEmailAddressPage) mustBe Some("old@email.com")
        updatedUserAnswers.get(TrustPhoneNumberPage) mustBe None
        updatedUserAnswers.get(TrustMobileNumberPage) mustBe Some("1111111111")
      }

      "must remove TrustEmailAddressPage when Phone and Mobile is selected" in {
        val updatedUserAnswers =
          userAnswers
            .set(TrustContactMethodOptionsPage, Set(ContactMethodOptions.Phone, ContactMethodOptions.Mobile))
            .success
            .value

        updatedUserAnswers.get(TrustEmailAddressPage) mustBe None
        updatedUserAnswers.get(TrustPhoneNumberPage) mustBe Some("01234567890")
        updatedUserAnswers.get(TrustMobileNumberPage) mustBe Some("1111111111")
      }
    }
  }
}
