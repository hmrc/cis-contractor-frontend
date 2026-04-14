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

import models.add.InternationalAddress
import pages.behaviours.PageBehaviours

class TrustAddressYesNoPageSpec extends PageBehaviours {

  "TrustAddressYesNoPage" - {

    beRetrievable[Boolean](TrustAddressYesNoPage)

    beSettable[Boolean](TrustAddressYesNoPage)

    beRemovable[Boolean](TrustAddressYesNoPage)

    "cleanup: must remove TrustAddress userAnswers when No is selected" in {

      val address = InternationalAddress(
        addressLine1 = "line 1",
        addressLine2 = Some("line 2"),
        addressLine3 = "line 3",
        addressLine4 = Some("line 4"),
        postalCode = "NX1 1AA",
        country = "United Kingdom"
      )

      val userAnswers = emptyUserAnswers.set(TrustAddressPage, address).success.value

      val updatedUserAnswers = userAnswers.set(TrustAddressYesNoPage, false).success.value

      updatedUserAnswers.get(TrustAddressPage) mustBe None
    }
  }
}
