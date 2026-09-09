/*
 * Copyright 2025 HM Revenue & Customs
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

import models.address.{Address, Country}
import pages.behaviours.PageBehaviours

class SubAddressYesNoPageSpec extends PageBehaviours {

  "SubAddressYesNoPage" - {

    beRetrievable[Boolean](SubAddressYesNoPage)

    beSettable[Boolean](SubAddressYesNoPage)

    beRemovable[Boolean](SubAddressYesNoPage)

    "cleanup: must remove AddressOfSubcontractor userAnswers when No is selected" in {

      val testAddress = Address(
        addressLine1 = "line 1",
        addressLine2 = Some("line 2"),
        addressLine3 = Some("line 3"),
        addressLine4 = Some("line 4"),
        postcode = Some("NX1 1AA"),
        country = Some(Country(Some("GB"), Some("United Kingdom")))
      )

      val userAnswers = emptyUserAnswers.set(AddressOfSubcontractorPage, testAddress).success.value

      val updatedUserAnswers = userAnswers.set(SubAddressYesNoPage, false).success.value

      updatedUserAnswers.get(AddressOfSubcontractorPage) mustBe None
    }
  }
}
