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
import play.api.libs.json.*

class ChrisSubmissionResponseSpec extends SpecBase {

  "ChrisSubmissionResponse" - {

    "must read from JSON" in {
      val json = Json.obj(
        "submissionId"       -> "111111",
        "status"             -> "ACCEPTED",
        "hmrcMarkGenerated"  -> "hmrc-mark",
        "correlationId"      -> "123",
        "responseEndPoint"   -> Json.obj(
          "url"                 -> "http://localhost/poll",
          "pollIntervalSeconds" -> 5
        ),
        "gatewayTimestamp"   -> "2026-06-15T03:30:52",
        "acceptedTime"       -> "2026-06-15T03:30:53",
        "error"              -> JsNull,
        "govTalkErrorStatus" -> JsNull
      )

      val result = json.as[ChrisSubmissionResponse]

      result.submissionId mustBe "111111"
      result.status mustBe "ACCEPTED"
      result.hmrcMarkGenerated mustBe "hmrc-mark"
      result.correlationId.value mustBe "123"
      result.responseEndPoint.value.pollIntervalSeconds mustBe 5
    }

    "must write to JSON" in {
      val response = ChrisSubmissionResponse(
        submissionId = "111111",
        status = "ACCEPTED",
        hmrcMarkGenerated = "hmrc-mark",
        correlationId = Some("123"),
        responseEndPoint = Some(
          ResponseEndPointDto(
            url = "http://localhost/poll",
            pollIntervalSeconds = 5
          )
        )
      )

      val json = Json.toJson(response)

      (json \ "submissionId").as[String] mustBe "111111"
      (json \ "status").as[String] mustBe "ACCEPTED"
      (json \ "hmrcMarkGenerated").as[String] mustBe "hmrc-mark"
      (json \ "responseEndPoint" \ "pollIntervalSeconds").as[Int] mustBe 5
    }
  }
}
