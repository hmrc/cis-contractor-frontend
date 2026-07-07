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

package models.response

import base.SpecBase
import models.verify.SubmissionStatus
import models.verify.SubmissionStatus.SUBMITTED
import play.api.libs.json.*

class ChrisPollResponseSpec extends SpecBase {

  "ChrisPollResponse" - {

    "must read from JSON" in {
      val json = Json.obj(
        "status"             -> "SUBMITTED",
        "correlationId"      -> "123",
        "pollUrl"            -> JsNull,
        "pollInterval"       -> JsNull,
        "error"              -> JsNull,
        "irMarkReceived"     -> JsNull,
        "lastMessageDate"    -> JsNull,
        "acceptedTime"       -> JsNull,
        "govTalkErrorStatus" -> JsNull
      )

      val result = json.as[ChrisPollResponse]

      result.status mustBe SUBMITTED
      result.correlationId mustBe "123"
    }

    "must write to JSON" in {
      val response = ChrisPollResponse(
        status = SUBMITTED,
        correlationId = "123",
        pollUrl = None,
        pollInterval = None,
        error = None,
        irMarkReceived = None,
        lastMessageDate = None,
        acceptedTime = None
      )

      val json = Json.toJson(response)

      (json \ "status").as[String] mustBe "SUBMITTED"
      (json \ "correlationId").as[String] mustBe "123"
    }
  }
}
