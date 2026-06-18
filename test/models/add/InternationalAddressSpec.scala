/*
 * Copyright 2025 HM Revenue & Customs
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

package models.add

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json._

class InternationalAddressSpec extends AnyWordSpec with Matchers {

  "InternationalAddress JSON format" should {

    "serialize InternationalAddress to JSON" in {
      val address = InternationalAddress(
        addressLine1 = "Bldg 9C",
        addressLine2 = Some("Cobalt Business Park"),
        addressLine3 = "Newcastle",
        addressLine4 = Some("Tyne and Wear"),
        postalCode = "NE99 1NE",
        country = "United Kingdom"
      )

      val json = Json.toJson(address)

      json shouldBe Json.obj(
        "addressLine1" -> "Bldg 9C",
        "addressLine2" -> "Cobalt Business Park",
        "addressLine3" -> "Newcastle",
        "addressLine4" -> "Tyne and Wear",
        "postalCode"   -> "NE99 1NE",
        "country"      -> "United Kingdom"
      )
    }

    "deserialize JSON to InternationalAddress when all fields are present" in {
      val json = Json.obj(
        "addressLine1" -> "Bldg 9C",
        "addressLine2" -> "Cobalt Business Park",
        "addressLine3" -> "Newcastle",
        "addressLine4" -> "Tyne and Wear",
        "postalCode"   -> "NE99 1NE",
        "country"      -> "United Kingdom"
      )

      val result = json.validate[InternationalAddress]

      result shouldBe JsSuccess(
        InternationalAddress(
          "Bldg 9C",
          Some("Cobalt Business Park"),
          "Newcastle",
          Some("Tyne and Wear"),
          "NE99 1NE",
          "United Kingdom"
        )
      )
    }

    "deserialize JSON to InternationalAddress when optional fields are missing" in {
      val json = Json.obj(
        "addressLine1" -> "Bldg 9C",
        "addressLine3" -> "Newcastle",
        "postalCode"   -> "NE99 1NE",
        "country"      -> "United Kingdom"
      )

      val result = json.validate[InternationalAddress]

      result shouldBe JsSuccess(
        InternationalAddress(
          "Bldg 9C",
          None,
          "Newcastle",
          None,
          "NE99 1NE",
          "United Kingdom"
        )
      )
    }
  }
}
