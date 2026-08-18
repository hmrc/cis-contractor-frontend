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

import base.SpecBase
import play.api.libs.json.Json

class VerificationLastVerificationSpec extends SpecBase {
  "VerificationLastVerification" - {
    "serialize to JSON correctly" in {
      val verification = VerificationLastVerification(
        verificationId = 1L,
        verificationBatchId = Some(10L),
        verificationResourceRef = Some(20L),
        matched = Some("Y"),
        verificationNumber = Some("V1234567890"),
        taxTreatment = Some("net"),
        subcontractorName = Some("ABC Construction"),
        subcontractorId = Some(99L)
      )
      val json         = Json.toJson(verification)
      (json \ "verificationId").as[Long] mustBe 1L
      (json \ "verificationBatchId").as[Long] mustBe 10L
      (json \ "verificationResourceRef").as[Long] mustBe 20L
      (json \ "matched").as[String] mustBe "Y"
      (json \ "verificationNumber").as[String] mustBe "V1234567890"
      (json \ "taxTreatment").as[String] mustBe "net"
      (json \ "subcontractorName").as[String] mustBe "ABC Construction"
      (json \ "subcontractorId").as[Long] mustBe 99L
    }

    "deserialize from JSON correctly" in {
      val json   = Json.parse(
        """|{
           | "verificationId": 1,
           | "verificationBatchId": 10,
           | "verificationResourceRef": 20,
           | "matched": "Y",
           | "verificationNumber": "V1234567890",
           | "taxTreatment": "net",
           | "subcontractorName": "ABC Construction",
           | "subcontractorId": 99
           |}""".stripMargin
      )
      val result = json.as[VerificationLastVerification]
      result.verificationId mustBe 1L
      result.verificationBatchId mustBe Some(10L)
      result.verificationResourceRef mustBe Some(20L)
      result.matched mustBe Some("Y")
      result.verificationNumber mustBe Some("V1234567890")
      result.taxTreatment mustBe Some("net")
      result.subcontractorName mustBe Some("ABC Construction")
      result.subcontractorId mustBe Some(99L)
    }

    "round-trip serialize and deserialize correctly" in {
      val verification = VerificationLastVerification(
        verificationId = 1L,
        verificationBatchId = Some(10L),
        verificationResourceRef = Some(20L),
        matched = Some("Y"),
        verificationNumber = Some("V1234567890"),
        taxTreatment = Some("net"),
        subcontractorName = Some("ABC Construction"),
        subcontractorId = Some(99L)
      )
      val json         = Json.toJson(verification)
      val result       = json.as[VerificationLastVerification]
      result mustBe verification
    }
  }
}
