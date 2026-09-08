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

package models.finalvalidation

import base.SpecBase
import play.api.libs.json.{JsError, Json}

class FinalValidationChangeTargetSpec extends SpecBase {

  "FinalValidationChangeTarget" - {

    "must return the correct value from its key" in {
      FinalValidationChangeTarget.values.foreach { target =>
        FinalValidationChangeTarget.fromKey(target.key) mustBe Some(target)
      }
    }

    "must return None for an unknown key" in {
      FinalValidationChangeTarget.fromKey("unknown") mustBe None
    }

    "must round trip through JSON" in {
      FinalValidationChangeTarget.values.foreach { target =>
        Json
          .toJson(target)
          .as[FinalValidationChangeTarget] mustBe target
      }
    }

    "must fail to read an invalid key" in {
      Json
        .fromJson[FinalValidationChangeTarget](
          Json.toJson("unknown")
        ) mustBe a[JsError]
    }
  }
}
