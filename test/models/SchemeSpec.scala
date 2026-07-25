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
import play.api.libs.json.*

import java.time.Instant

class SchemeSpec extends SpecBase {

  "Scheme" - {

    "must read from JSON" in {
      val json = Json.obj(
        "schemeId"                 -> 1,
        "instanceId"               -> "1",
        "accountsOfficeReference"  -> "AO123",
        "taxOfficeNumber"          -> "123",
        "taxOfficeReference"       -> "AB456",
        "utr"                      -> "1234567890",
        "name"                     -> "Test Contractor",
        "emailAddress"             -> "test@test.com",
        "displayWelcomePage"       -> "Y",
        "prePopCount"              -> 1,
        "prePopSuccessful"         -> "Y",
        "subcontractorCounter"     -> 2,
        "verificationBatchCounter" -> 3,
        "createDate"               -> "2026-06-15T03:30:52Z",
        "lastUpdate"               -> "2026-06-15T03:30:54Z",
        "version"                  -> 1
      )

      val result = json.as[Scheme]

      result.schemeId mustBe 1
      result.instanceId mustBe "1"
      result.accountsOfficeReference mustBe "AO123"
      result.utr.value mustBe "1234567890"
    }

    "must write to JSON" in {
      val model = Scheme(
        schemeId = 1,
        instanceId = "1",
        accountsOfficeReference = "AO123",
        taxOfficeNumber = "123",
        taxOfficeReference = "AB456",
        utr = Some("1234567890"),
        name = Some("Test Contractor"),
        emailAddress = Some("test@test.com"),
        createDate = Some(Instant.parse("2026-06-15T03:30:52Z")),
        lastUpdate = Some(Instant.parse("2026-06-15T03:30:54Z")),
        version = Some(1)
      )

      val json = Json.toJson(model)

      (json \ "schemeId").as[Int] mustBe 1
      (json \ "instanceId").as[String] mustBe "1"
      (json \ "accountsOfficeReference").as[String] mustBe "AO123"
      (json \ "utr").as[String] mustBe "1234567890"
    }
  }
}
