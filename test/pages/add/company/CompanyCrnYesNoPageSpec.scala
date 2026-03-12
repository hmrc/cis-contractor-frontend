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

import pages.behaviours.PageBehaviours

class CompanyCrnYesNoPageSpec extends PageBehaviours {

  "CompanyCrnYesNoPage" - {

    beRetrievable[Boolean](CompanyCrnYesNoPage)

    beSettable[Boolean](CompanyCrnYesNoPage)

    beRemovable[Boolean](CompanyCrnYesNoPage)

    "cleanup: must remove CompanyReferenceNumber userAnswers when No is selected" in {
      val userAnswers = emptyUserAnswers.set(CompanyCrnPage, "ABC").success.value

      val updatedUserAnswers = userAnswers.set(CompanyCrnYesNoPage, false).success.value

      updatedUserAnswers.get(CompanyCrnPage) mustBe None
    }
  }
}
