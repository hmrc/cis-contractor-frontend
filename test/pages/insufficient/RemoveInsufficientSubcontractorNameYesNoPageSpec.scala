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

package pages.insufficient

import pages.behaviours.PageBehaviours
import play.api.libs.json.JsPath
import org.scalacheck.Gen

class RemoveInsufficientSubcontractorNameYesNoPageSpec extends PageBehaviours {

  "RemoveInsufficientSubcontractorNameYesNoPage" - {

    val page = RemoveInsufficientSubcontractorNameYesNoPage(12345L)

    "must use the verification resource ref in its path" in {
      page.path mustBe (JsPath \ "removeInsufficientSubcontractorNameYesNo" \ "12345" \ "removed")
    }

    beRetrievable[Boolean](Gen.const(page))

    beSettable[Boolean](Gen.const(page))

    beRemovable[Boolean](Gen.const(page))
  }
}
