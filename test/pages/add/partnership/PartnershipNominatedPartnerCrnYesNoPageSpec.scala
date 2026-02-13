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

import pages.behaviours.PageBehaviours

class PartnershipNominatedPartnerCrnYesNoPageSpec extends PageBehaviours {

  "PartnershipNominatedPartnerCrnYesNoPage" - {

    beRetrievable[Boolean](PartnershipNominatedPartnerCrnYesNoPage)

    beSettable[Boolean](PartnershipNominatedPartnerCrnYesNoPage)

    beRemovable[Boolean](PartnershipNominatedPartnerCrnYesNoPage)

    // TODO update to correct page for cleanup
    "cleanup: must remove PartnershipWorksReferenceNumber userAnswers when No is selected" in {
      val userAnswers = emptyUserAnswers.set(PartnershipWorksReferenceNumberPage, "ABC").success.value

      val updatedUserAnswers = userAnswers.set(PartnershipNominatedPartnerCrnYesNoPage, false).success.value

      updatedUserAnswers.get(PartnershipWorksReferenceNumberPage) mustBe None
    }
  }

}
