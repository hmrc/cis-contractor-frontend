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

import models.add.company.CompanyContactOptions
import models.contact.ContactOptions
import pages.behaviours.PageBehaviours

class CompanyContactOptionsPageSpec extends PageBehaviours {

  "CompanyContactOptionsPage" - {

    beRetrievable[CompanyContactOptions](CompanyContactOptionsPage)

    beSettable[CompanyContactOptions](CompanyContactOptionsPage)

    beRemovable[CompanyContactOptions](CompanyContactOptionsPage)

    "cleanup" - {

      "must remove CompanyPhoneNumberPage, CompanyMobileNumberPage userAnswers when Email is selected" in {
        val userAnswers = emptyUserAnswers
          .set(CompanyPhoneNumberPage, "01234567890")
          .success
          .value
          .set(CompanyMobileNumberPage, "01234567890")
          .success
          .value

        val updatedUserAnswers = userAnswers.set(CompanyContactOptionsPage, ContactOptions.Email).success.value

        updatedUserAnswers.get(CompanyPhoneNumberPage) mustBe None
        updatedUserAnswers.get(CompanyMobileNumberPage) mustBe None
      }

      "must remove CompanyEmailAddressPage, CompanyMobileNumberPage userAnswers when Phone is selected" in {
        val userAnswers = emptyUserAnswers
          .set(CompanyEmailAddressPage, "old@email.com")
          .success
          .value
          .set(CompanyMobileNumberPage, "01234567890")
          .success
          .value

        val updatedUserAnswers = userAnswers.set(CompanyContactOptionsPage, ContactOptions.Phone).success.value

        updatedUserAnswers.get(CompanyEmailAddressPage) mustBe None
        updatedUserAnswers.get(CompanyMobileNumberPage) mustBe None
      }

      "must remove CompanyEmailAddressPage, CompanyPhoneNumberPage userAnswers when Mobile is selected" in {
        val userAnswers = emptyUserAnswers
          .set(CompanyEmailAddressPage, "old@email.com")
          .success
          .value
          .set(CompanyPhoneNumberPage, "01234567890")
          .success
          .value

        val updatedUserAnswers = userAnswers.set(CompanyContactOptionsPage, ContactOptions.Mobile).success.value

        updatedUserAnswers.get(CompanyEmailAddressPage) mustBe None
        updatedUserAnswers.get(CompanyPhoneNumberPage) mustBe None
      }

      "must remove CompanyEmailAddressPage, CompanyMobileNumberPage, CompanyPhoneNumberPage userAnswers when NoDetails is selected" in {
        val userAnswers = emptyUserAnswers
          .set(CompanyEmailAddressPage, "old@email.com")
          .success
          .value
          .set(CompanyPhoneNumberPage, "01234567890")
          .success
          .value
          .set(CompanyMobileNumberPage, "01234567890")
          .success
          .value

        val updatedUserAnswers = userAnswers.set(CompanyContactOptionsPage, ContactOptions.NoDetails).success.value

        updatedUserAnswers.get(CompanyEmailAddressPage) mustBe None
        updatedUserAnswers.get(CompanyPhoneNumberPage) mustBe None
        updatedUserAnswers.get(CompanyMobileNumberPage) mustBe None
      }
    }
  }
}
