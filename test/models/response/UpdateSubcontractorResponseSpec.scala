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
import play.api.libs.json.Json

class UpdateSubcontractorResponseSpec extends SpecBase {

  "UpdateSubcontractorResponse" - {

    "must serialise to JSON" in {
      val response =
        UpdateSubcontractorResponse(
          version = 3
        )

      Json.toJson(response) mustBe
        Json.obj(
          "version" -> 3
        )
    }

    "must deserialise from JSON" in {
      val json =
        Json.obj(
          "version" -> 3
        )

      json.as[UpdateSubcontractorResponse] mustBe
        UpdateSubcontractorResponse(
          version = 3
        )
    }

    "must round trip through JSON" in {
      val response =
        UpdateSubcontractorResponse(
          version = 3
        )

      val json =
        Json.toJson(response)

      Json.fromJson[UpdateSubcontractorResponse](json).get mustBe
        response
    }

    "must fail to deserialise when version is missing" in {
      val json =
        Json.obj()

      val result =
        Json.fromJson[UpdateSubcontractorResponse](json)

      result.isError mustBe true
    }

    "must fail to deserialise when version has the wrong type" in {
      val json =
        Json.obj(
          "version" -> "three"
        )

      val result =
        Json.fromJson[UpdateSubcontractorResponse](json)

      result.isError mustBe true
    }
  }
}
