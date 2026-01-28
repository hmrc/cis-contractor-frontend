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

import pages.add.TradingNameOfSubcontractorPage
import pages.behaviours.PageBehaviours

class PartnershipWorksReferenceNumberYesNoPageSpec extends PageBehaviours {

  "PartnershipWorksReferenceNumberYesNoPage" - {

    beRetrievable[Boolean](PartnershipWorksReferenceNumberYesNoPage)

    beSettable[Boolean](PartnershipWorksReferenceNumberYesNoPage)

    beRemovable[Boolean](PartnershipWorksReferenceNumberYesNoPage)

    //Need to update to correct page!
    "cleanup: must remove TradingNameOfSubcontractor userAnswers when No is selected" in {
      val userAnswers = emptyUserAnswers.set(TradingNameOfSubcontractorPage, "ABC").success.value

      val updatedUserAnswers = userAnswers.set(PartnershipWorksReferenceNumberYesNoPage, false).success.value

      updatedUserAnswers.get(TradingNameOfSubcontractorPage) mustBe None
    }

  }
}
