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

package pages

import models.add.IndividualContactMethodOptions
import models.add.IndividualContactMethodOptions.{Emailaddress, Mobilenumber, Phonenumber}
import pages.add.{IndividualContactMethodOptionsPage, IndividualEmailAddressPage, IndividualMobileNumberPage, IndividualPhoneNumberPage}
import pages.behaviours.PageBehaviours

class IndividualContactMethodOptionsPageSpec extends PageBehaviours {

  "IndividualContactMethodOptionsPage" - {

    beRetrievable[Set[IndividualContactMethodOptions]](IndividualContactMethodOptionsPage)

    beSettable[Set[IndividualContactMethodOptions]](IndividualContactMethodOptionsPage)

    beRemovable[Set[IndividualContactMethodOptions]](IndividualContactMethodOptionsPage)

    "cleanup" - {

      val userAnswers = emptyUserAnswers
        .set(IndividualEmailAddressPage, "old@email.com").success.value
        .set(IndividualPhoneNumberPage, "01234567890").success.value
        .set(IndividualMobileNumberPage, "07700900982").success.value

      "must remove IndividualPhoneNumberPage and IndividualMobileNumberPage when only Email is selected" in {
        val updated = userAnswers.set(IndividualContactMethodOptionsPage, Set(Emailaddress)).success.value

        updated.get(IndividualEmailAddressPage) mustBe Some("old@email.com")
        updated.get(IndividualPhoneNumberPage) mustBe None
        updated.get(IndividualMobileNumberPage) mustBe None
      }

      "must remove IndividualEmailAddressPage and IndividualMobileNumberPage when only Phone is selected" in {
        val updated = userAnswers.set(IndividualContactMethodOptionsPage, Set(Phonenumber)).success.value

        updated.get(IndividualEmailAddressPage) mustBe None
        updated.get(IndividualPhoneNumberPage) mustBe Some("01234567890")
        updated.get(IndividualMobileNumberPage) mustBe None
      }

      "must remove IndividualEmailAddressPage and IndividualPhoneNumberPage when only Mobile is selected" in {
        val updated = userAnswers.set(IndividualContactMethodOptionsPage, Set(Mobilenumber)).success.value

        updated.get(IndividualEmailAddressPage) mustBe None
        updated.get(IndividualPhoneNumberPage) mustBe None
        updated.get(IndividualMobileNumberPage) mustBe Some("07700900982")
      }

      "must remove IndividualMobileNumberPage when Email and Phone are selected" in {
        val updated =
          userAnswers.set(IndividualContactMethodOptionsPage, Set(Emailaddress, Phonenumber)).success.value

        updated.get(IndividualEmailAddressPage) mustBe Some("old@email.com")
        updated.get(IndividualPhoneNumberPage) mustBe Some("01234567890")
        updated.get(IndividualMobileNumberPage) mustBe None
      }

      "must remove IndividualPhoneNumberPage when Email and Mobile are selected" in {
        val updated =
          userAnswers.set(IndividualContactMethodOptionsPage, Set(Emailaddress, Mobilenumber)).success.value

        updated.get(IndividualEmailAddressPage) mustBe Some("old@email.com")
        updated.get(IndividualPhoneNumberPage) mustBe None
        updated.get(IndividualMobileNumberPage) mustBe Some("07700900982")
      }

      "must remove IndividualEmailAddressPage when Phone and Mobile are selected" in {
        val updated =
          userAnswers.set(IndividualContactMethodOptionsPage, Set(Phonenumber, Mobilenumber)).success.value

        updated.get(IndividualEmailAddressPage) mustBe None
        updated.get(IndividualPhoneNumberPage) mustBe Some("01234567890")
        updated.get(IndividualMobileNumberPage) mustBe Some("07700900982")
      }

      "must keep all contact pages when all three methods are selected" in {
        val updated =
          userAnswers
            .set(IndividualContactMethodOptionsPage, Set(Emailaddress, Phonenumber, Mobilenumber))
            .success
            .value

        updated.get(IndividualEmailAddressPage) mustBe Some("old@email.com")
        updated.get(IndividualPhoneNumberPage) mustBe Some("01234567890")
        updated.get(IndividualMobileNumberPage) mustBe Some("07700900982")
      }
    }
  }
}