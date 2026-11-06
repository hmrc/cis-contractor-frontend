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

package pages.add

import pages.behaviours.PageBehaviours
import play.api.libs.json.JsPath

class CheckYourAnswersSubmittedPageSpec extends PageBehaviours {

  "CheckYourAnswersSubmittedPage" - {

    beRetrievable[Boolean](CheckYourAnswersSubmittedPage)

    beSettable[Boolean](CheckYourAnswersSubmittedPage)

    beRemovable[Boolean](CheckYourAnswersSubmittedPage)

    "must nest the WithId path under addSubcontractor" in {
      CheckYourAnswersSubmittedPage("uuid-1").path mustBe
        (JsPath \ "addSubcontractor" \ "uuid-1" \ "checkYourAnswersSubmitted")
      CheckYourAnswersSubmittedPage.path mustBe (JsPath \ "checkYourAnswersSubmitted")
    }
  }
}
