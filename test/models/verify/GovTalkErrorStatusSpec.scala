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

package models.verify

import base.SpecBase
import models.verify.GovTalkErrorStatus.*
import play.api.libs.json.*

class GovTalkErrorStatusSpec extends SpecBase {

  "GovTalkErrorStatus" - {

    "must write FatalError to JSON" in {
      val json = Json.toJson[GovTalkErrorStatus](
        FatalError("5000", "Fatal error")
      )

      (json \ "kind").as[String] mustBe "FatalError"
      (json \ "errorCode").as[String] mustBe "5000"
      (json \ "errorText").as[String] mustBe "Fatal error"
    }

    "must read FatalError from JSON" in {
      val json = Json.obj(
        "kind"      -> "FatalError",
        "errorCode" -> "5000",
        "errorText" -> "Fatal error"
      )

      json.as[GovTalkErrorStatus] mustBe FatalError("5000", "Fatal error")
    }

    "must write and read NoResponse" in {
      val json = Json.toJson[GovTalkErrorStatus](NoResponse)

      (json \ "kind").as[String] mustBe "NoResponse"
      json.as[GovTalkErrorStatus] mustBe NoResponse
    }
  }
}
