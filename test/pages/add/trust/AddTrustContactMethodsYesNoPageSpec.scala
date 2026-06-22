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

import models.contact.ContactMethodOptions
import pages.behaviours.PageBehaviours

class AddTrustContactMethodsYesNoPageSpec extends PageBehaviours {
  "AddTrustContactMethodsYesNoPage" - {

    beRetrievable[Boolean](AddTrustContactMethodsYesNoPage)

    beSettable[Boolean](AddTrustContactMethodsYesNoPage)

    beRemovable[Boolean](AddTrustContactMethodsYesNoPage)

    "cleanup: must remove all methods of contact userAnswers when No is selected" in {
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
        .set(TrustContactMethodOptionsPage, Set(ContactMethodOptions.Email, ContactMethodOptions.Phone))
        .success
        .value

      val updatedUserAnswers = userAnswers.set(AddTrustContactMethodsYesNoPage, false).success.value

      updatedUserAnswers.get(TrustEmailAddressPage) mustBe None
      updatedUserAnswers.get(TrustPhoneNumberPage) mustBe None
      updatedUserAnswers.get(TrustMobileNumberPage) mustBe None
      updatedUserAnswers.get(TrustContactMethodOptionsPage) mustBe None
    }
  }
}
