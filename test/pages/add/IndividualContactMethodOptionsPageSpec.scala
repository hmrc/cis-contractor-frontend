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

import models.add.IndividualContactMethodOptions
import models.contact.ContactMethodOptions
import pages.behaviours.PageBehaviours

class IndividualContactMethodOptionsPageSpec extends PageBehaviours {
  "IndividualContactMethodOptionsPage" - {

    beRetrievable[Set[IndividualContactMethodOptions]](IndividualContactMethodOptionsPage)

    beSettable[Set[IndividualContactMethodOptions]](IndividualContactMethodOptionsPage)

    beRemovable[Set[IndividualContactMethodOptions]](IndividualContactMethodOptionsPage)

    "cleanup" - {

      val userAnswers = emptyUserAnswers
        .set(IndividualEmailAddressPage, "old@email.com")
        .success
        .value
        .set(IndividualPhoneNumberPage, "01234567890")
        .success
        .value
        .set(IndividualMobileNumberPage, "1111111111")
        .success
        .value

      "must remove IndividualPhoneNumberPage and IndividualMobileNumberPage when Email is selected" in {
        val updatedUserAnswers =
          userAnswers.set(IndividualContactMethodOptionsPage, Set(ContactMethodOptions.Email)).success.value

        updatedUserAnswers.get(IndividualEmailAddressPage) mustBe Some("old@email.com")
        updatedUserAnswers.get(IndividualPhoneNumberPage) mustBe None
        updatedUserAnswers.get(IndividualMobileNumberPage) mustBe None

      }

      "must remove IndividualEmailAddressPage and PIndividualMobileNumberPage when Phone is selected" in {
        val updatedUserAnswers =
          userAnswers.set(IndividualContactMethodOptionsPage, Set(ContactMethodOptions.Phone)).success.value

        updatedUserAnswers.get(IndividualEmailAddressPage) mustBe None
        updatedUserAnswers.get(IndividualPhoneNumberPage) mustBe Some("01234567890")
        updatedUserAnswers.get(IndividualMobileNumberPage) mustBe None
      }

      "must remove IndividualEmailAddressPage and IndividualPhoneNumberPage when Mobile is selected" in {
        val updatedUserAnswers =
          userAnswers.set(IndividualContactMethodOptionsPage, Set(ContactMethodOptions.Mobile)).success.value

        updatedUserAnswers.get(IndividualEmailAddressPage) mustBe None
        updatedUserAnswers.get(IndividualPhoneNumberPage) mustBe None
        updatedUserAnswers.get(IndividualMobileNumberPage) mustBe Some("1111111111")
      }

      "must remove IndividualMobileNumberPage when Email and Phone is selected" in {
        val updatedUserAnswers =
          userAnswers
            .set(IndividualContactMethodOptionsPage, Set(ContactMethodOptions.Email, ContactMethodOptions.Phone))
            .success
            .value

        updatedUserAnswers.get(IndividualEmailAddressPage) mustBe Some("old@email.com")
        updatedUserAnswers.get(IndividualPhoneNumberPage) mustBe Some("01234567890")
        updatedUserAnswers.get(IndividualMobileNumberPage) mustBe None
      }

      "must remove IndividualPhoneNumberPage when Email and Mobile is selected" in {
        val updatedUserAnswers =
          userAnswers
            .set(IndividualContactMethodOptionsPage, Set(ContactMethodOptions.Email, ContactMethodOptions.Mobile))
            .success
            .value

        updatedUserAnswers.get(IndividualEmailAddressPage) mustBe Some("old@email.com")
        updatedUserAnswers.get(IndividualPhoneNumberPage) mustBe None
        updatedUserAnswers.get(IndividualMobileNumberPage) mustBe Some("1111111111")
      }

      "must remove IndividualEmailAddressPage when Phone and Mobile is selected" in {
        val updatedUserAnswers =
          userAnswers
            .set(IndividualContactMethodOptionsPage, Set(ContactMethodOptions.Phone, ContactMethodOptions.Mobile))
            .success
            .value

        updatedUserAnswers.get(IndividualEmailAddressPage) mustBe None
        updatedUserAnswers.get(IndividualPhoneNumberPage) mustBe Some("01234567890")
        updatedUserAnswers.get(IndividualMobileNumberPage) mustBe Some("1111111111")
      }
    }
  }
}
