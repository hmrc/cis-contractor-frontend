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

import pages.behaviours.PageBehaviours

class TrustUtrYesNoPageSpec extends PageBehaviours {

  "TrustUtrYesNoPage" - {

    beRetrievable[Boolean](TrustUtrYesNoPage)

    beSettable[Boolean](TrustUtrYesNoPage)

    beRemovable[Boolean](TrustUtrYesNoPage)

    "cleanup: must remove TrustUtr userAnswers when No is selected" in {
      val userAnswers = emptyUserAnswers.set(TrustUtrPage, "1234567890").success.value

      val updatedUserAnswers = userAnswers.set(TrustUtrYesNoPage, false).success.value

      updatedUserAnswers.get(TrustUtrPage) mustBe None
    }
  }
}
