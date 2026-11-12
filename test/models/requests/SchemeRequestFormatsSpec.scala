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

package models.requests

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.*

class SchemeRequestFormatsSpec extends AnyFreeSpec with Matchers {

  "UpdateContractorSchemeParams" - {
    "round-trip a fully populated value" in {
      val model = UpdateContractorSchemeParams(
        1,
        "INST",
        "AO123",
        "123",
        "AB456",
        Some("1234567890"),
        Some("Scheme"),
        Some("a@b.com"),
        Some("Y"),
        Some(2),
        Some("Y"),
        Some(4)
      )
      Json.toJson(model).validate[UpdateContractorSchemeParams] mustBe JsSuccess(model)
    }
    "read a minimal value with optionals as None" in {
      val json = Json.obj(
        "schemeId"                -> 1,
        "instanceId"              -> "INST",
        "accountsOfficeReference" -> "AO123",
        "taxOfficeNumber"         -> "123",
        "taxOfficeReference"      -> "AB456"
      )
      json.as[UpdateContractorSchemeParams] mustBe
        UpdateContractorSchemeParams(1, "INST", "AO123", "123", "AB456")
    }
    "omit empty optionals when writing" in {
      val json = Json.toJson(UpdateContractorSchemeParams(1, "INST", "AO123", "123", "AB456")).as[JsObject]
      json.keys mustBe Set("schemeId", "instanceId", "accountsOfficeReference", "taxOfficeNumber", "taxOfficeReference")
    }
    "fail when a required field is missing" in {
      Json.obj("schemeId" -> 1).validate[UpdateContractorSchemeParams].isError mustBe true
    }
  }

  "UpdateSchemeRequest" - {
    val model = UpdateSchemeRequest(1, "INST", "123", "AB456", "AO123", 2, "Y", "1234567890", "Scheme", "a@b.com", 4)

    "round-trip" in {
      Json.toJson(model).validate[UpdateSchemeRequest] mustBe JsSuccess(model)
    }
    "fail when a field is missing" in {
      (Json.toJson(model).as[JsObject] - "version").validate[UpdateSchemeRequest].isError mustBe true
    }
    "fail when a field has the wrong type" in {
      (Json.toJson(model).as[JsObject] + ("prePopCount" -> JsString("x"))).validate[UpdateSchemeRequest].isError mustBe true
    }
  }
}
