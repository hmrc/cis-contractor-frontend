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

import models.contact.ContactMethodOptions
import pages.behaviours.PageBehaviours

class AddPartnershipContactMethodsYesNoPageSpec extends PageBehaviours {

  "AddPartnershipContactMethodsYesNoPage" - {

    beRetrievable[Boolean](AddPartnershipContactMethodsYesNoPage)

    beSettable[Boolean](AddPartnershipContactMethodsYesNoPage)

    beRemovable[Boolean](AddPartnershipContactMethodsYesNoPage)

    "cleanup: must remove all methods of contact userAnswers when No is selected" in {
      val userAnswers = emptyUserAnswers
        .set(PartnershipEmailAddressPage, "old@email.com")
        .success
        .value
        .set(PartnershipPhoneNumberPage, "01234567890")
        .success
        .value
        .set(PartnershipMobileNumberPage, "01234567890")
        .success
        .value
        .set(PartnershipContactMethodOptionsPage, Set(ContactMethodOptions.Email, ContactMethodOptions.Phone))
        .success
        .value

      val updatedUserAnswers = userAnswers.set(AddPartnershipContactMethodsYesNoPage, false).success.value

      updatedUserAnswers.get(PartnershipEmailAddressPage) mustBe None
      updatedUserAnswers.get(PartnershipPhoneNumberPage) mustBe None
      updatedUserAnswers.get(PartnershipMobileNumberPage) mustBe None
      updatedUserAnswers.get(PartnershipContactMethodOptionsPage) mustBe None
    }
  }
}
