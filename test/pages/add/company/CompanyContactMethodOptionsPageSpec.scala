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

package pages.add.company

import models.add.company.CompanyContactMethodOptions
import models.contact.ContactMethodOptions
import pages.behaviours.PageBehaviours

class CompanyContactMethodOptionsPageSpec extends PageBehaviours {
  "CompanyContactMethodOptionsPage" - {

    beRetrievable[Set[CompanyContactMethodOptions]](CompanyContactMethodOptionsPage)

    beSettable[Set[CompanyContactMethodOptions]](CompanyContactMethodOptionsPage)

    beRemovable[Set[CompanyContactMethodOptions]](CompanyContactMethodOptionsPage)

    "cleanup" - {

      val userAnswers = emptyUserAnswers
        .set(CompanyEmailAddressPage, "old@email.com")
        .success
        .value
        .set(CompanyPhoneNumberPage, "01234567890")
        .success
        .value
        .set(CompanyMobileNumberPage, "1111111111")
        .success
        .value

      "must remove CompanyPhoneNumberPage and CompanyMobileNumberPage when Email is selected" in {
        val updatedUserAnswers =
          userAnswers.set(CompanyContactMethodOptionsPage, Set(ContactMethodOptions.Email)).success.value

        updatedUserAnswers.get(CompanyEmailAddressPage) mustBe Some("old@email.com")
        updatedUserAnswers.get(CompanyPhoneNumberPage) mustBe None
        updatedUserAnswers.get(CompanyMobileNumberPage) mustBe None
      }

      "must remove CompanyEmailAddressPage and CompanyMobileNumberPage when Phone is selected" in {
        val updatedUserAnswers =
          userAnswers.set(CompanyContactMethodOptionsPage, Set(ContactMethodOptions.Phone)).success.value

        updatedUserAnswers.get(CompanyEmailAddressPage) mustBe None
        updatedUserAnswers.get(CompanyPhoneNumberPage) mustBe Some("01234567890")
        updatedUserAnswers.get(CompanyMobileNumberPage) mustBe None
      }

      "must remove CompanyEmailAddressPage and CompanyPhoneNumberPage when Mobile is selected" in {
        val updatedUserAnswers =
          userAnswers.set(CompanyContactMethodOptionsPage, Set(ContactMethodOptions.Mobile)).success.value

        updatedUserAnswers.get(CompanyEmailAddressPage) mustBe None
        updatedUserAnswers.get(CompanyPhoneNumberPage) mustBe None
        updatedUserAnswers.get(CompanyMobileNumberPage) mustBe Some("1111111111")
      }

      "must remove CompanyMobileNumberPage when Email and Phone is selected" in {
        val updatedUserAnswers =
          userAnswers
            .set(CompanyContactMethodOptionsPage, Set(ContactMethodOptions.Email, ContactMethodOptions.Phone))
            .success
            .value

        updatedUserAnswers.get(CompanyEmailAddressPage) mustBe Some("old@email.com")
        updatedUserAnswers.get(CompanyPhoneNumberPage) mustBe Some("01234567890")
        updatedUserAnswers.get(CompanyMobileNumberPage) mustBe None
      }

      "must remove CompanyPhoneNumberPage when Email and Mobile is selected" in {
        val updatedUserAnswers =
          userAnswers
            .set(CompanyContactMethodOptionsPage, Set(ContactMethodOptions.Email, ContactMethodOptions.Mobile))
            .success
            .value

        updatedUserAnswers.get(CompanyEmailAddressPage) mustBe Some("old@email.com")
        updatedUserAnswers.get(CompanyPhoneNumberPage) mustBe None
        updatedUserAnswers.get(CompanyMobileNumberPage) mustBe Some("1111111111")
      }

      "must remove CompanyEmailAddressPage when Phone and Mobile is selected" in {
        val updatedUserAnswers =
          userAnswers
            .set(CompanyContactMethodOptionsPage, Set(ContactMethodOptions.Phone, ContactMethodOptions.Mobile))
            .success
            .value

        updatedUserAnswers.get(CompanyEmailAddressPage) mustBe None
        updatedUserAnswers.get(CompanyPhoneNumberPage) mustBe Some("01234567890")
        updatedUserAnswers.get(CompanyMobileNumberPage) mustBe Some("1111111111")
      }
    }
  }
}
