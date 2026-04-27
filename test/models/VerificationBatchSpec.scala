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

package models

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsSuccess, Json}

final class VerificationBatchSpec extends AnyWordSpec with Matchers {

  "VerificationBatch JSON format" should {

    "read JSON into model" in {
      val json = Json.parse(
        """
          |{
          |  "verificationBatchId": 99,
          |  "status": "STARTED",
          |  "verificationNumber": "VB00000001"
          |}
          |""".stripMargin
      )

      val out = json.as[VerificationBatch]
      out.verificationBatchId mustBe 99L
      out.status mustBe Some("STARTED")
      out.verificationNumber mustBe Some("VB00000001")
    }

    "write model to JSON" in {
      val model = VerificationBatch(
        verificationBatchId = 99L,
        status = Some("VALIDATED"),
        verificationNumber = Some("VB00000002")
      )

      val json = Json.toJson(model)
      (json \ "verificationBatchId").as[Long] mustBe 99L
      (json \ "status").as[String] mustBe "VALIDATED"
      (json \ "verificationNumber").as[String] mustBe "VB00000002"
    }

    "round-trip (model -> json -> model) without losing data" in {
      val model = VerificationBatch(1L, None, None)
      val json  = Json.toJson(model)
      json.validate[VerificationBatch] mustBe JsSuccess(model)
    }
  }
}
