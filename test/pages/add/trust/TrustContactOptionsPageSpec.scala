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

import models.add.trust.TrustContactOptions
import models.contact.ContactOptions
import pages.behaviours.PageBehaviours

class TrustContactOptionsPageSpec extends PageBehaviours {

  "TrustContactOptionsPage" - {

    beRetrievable[TrustContactOptions](TrustContactOptionsPage)

    beSettable[TrustContactOptions](TrustContactOptionsPage)

    beRemovable[TrustContactOptions](TrustContactOptionsPage)

    "cleanup" - {

      "must remove TrustPhoneNumberPage and TrustMobileNumberPage when Email is selected" in {
        val userAnswers = emptyUserAnswers
          .set(TrustPhoneNumberPage, "01234567890")
          .success
          .value
          .set(TrustMobileNumberPage, "01234567890")
          .success
          .value

        val updatedUserAnswers = userAnswers.set(TrustContactOptionsPage, ContactOptions.Email).success.value

        updatedUserAnswers.get(TrustPhoneNumberPage) mustBe None
        updatedUserAnswers.get(TrustMobileNumberPage) mustBe None
      }

      "must remove TrustEmailAddressPage and TrustMobileNumberPage when Phone is selected" in {
        val userAnswers = emptyUserAnswers
          .set(TrustEmailAddressPage, "old@email.com")
          .success
          .value
          .set(TrustMobileNumberPage, "01234567890")
          .success
          .value

        val updatedUserAnswers = userAnswers.set(TrustContactOptionsPage, ContactOptions.Phone).success.value

        updatedUserAnswers.get(TrustEmailAddressPage) mustBe None
        updatedUserAnswers.get(TrustMobileNumberPage) mustBe None
      }

      "must remove TrustEmailAddressPage and TrustPhoneNumberPage when Mobile is selected" in {
        val userAnswers = emptyUserAnswers
          .set(TrustEmailAddressPage, "old@email.com")
          .success
          .value
          .set(TrustPhoneNumberPage, "01234567890")
          .success
          .value

        val updatedUserAnswers = userAnswers.set(TrustContactOptionsPage, ContactOptions.Mobile).success.value

        updatedUserAnswers.get(TrustEmailAddressPage) mustBe None
        updatedUserAnswers.get(TrustPhoneNumberPage) mustBe None
      }

      "must remove TrustEmailAddressPage, TrustPhoneNumberPage and TrustMobileNumberPage when NoDetails is selected" in {
        val userAnswers = emptyUserAnswers
          .set(TrustEmailAddressPage, "old@email.com")
          .success
          .value
          .set(TrustPhoneNumberPage, "01234567890")
          .success
          .value
          .set(TrustMobileNumberPage, "01234567890")
          .success
          .value

        val updatedUserAnswers = userAnswers.set(TrustContactOptionsPage, ContactOptions.NoDetails).success.value

        updatedUserAnswers.get(TrustEmailAddressPage) mustBe None
        updatedUserAnswers.get(TrustPhoneNumberPage) mustBe None
        updatedUserAnswers.get(TrustMobileNumberPage) mustBe None
      }
    }
  }
}
