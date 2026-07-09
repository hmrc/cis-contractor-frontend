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
import play.api.libs.json.Json
import java.time.LocalDateTime

class VerificationSubmissionDetailsSpec extends SpecBase {

  "VerificationSubmissionDetails" - {

    "must read from JSON" in {
      val json = Json.obj(
        "submissionId"        -> "13602",
        "status"              -> "SUBMITTED",
        "hmrcMarkGenerated"   -> "hmrc-mark",
        "hmrcMarkGgis"        -> "ggis-mark",
        "correlationId"       -> "corr-id",
        "pollUrl"             -> "http://localhost/poll",
        "pollIntervalSeconds" -> 5,
        "submittedAt"         -> "2026-06-15T03:30:52",
        "lastMessageDate"     -> "2026-06-15T03:30:54",
        "timedOut"            -> false
      )

      val result = json.as[VerificationSubmissionDetails]

      result.submissionId mustBe "13602"
      result.status mustBe "SUBMITTED"
      result.submittedAt mustBe LocalDateTime.parse("2026-06-15T03:30:52")
      result.lastMessageDate.value mustBe LocalDateTime.parse("2026-06-15T03:30:54")
    }

    "must write to JSON" in {
      val model = VerificationSubmissionDetails(
        submissionId = "13602",
        status = "SUBMITTED",
        hmrcMarkGenerated = "hmrc-mark",
        hmrcMarkGgis = Some("ggis-mark"),
        correlationId = Some("corr-id"),
        pollUrl = Some("http://localhost/poll"),
        pollIntervalSeconds = Some(5),
        submittedAt = LocalDateTime.parse("2026-06-15T03:30:52"),
        lastMessageDate = Some(LocalDateTime.parse("2026-06-15T03:30:54")),
        timedOut = false
      )

      val json = Json.toJson(model)

      (json \ "submissionId").as[String] mustBe "13602"
      (json \ "status").as[String] mustBe "SUBMITTED"
      (json \ "hmrcMarkGenerated").as[String] mustBe "hmrc-mark"
      (json \ "pollIntervalSeconds").as[Int] mustBe 5
      (json \ "timedOut").as[Boolean] mustBe false
    }
  }
}
