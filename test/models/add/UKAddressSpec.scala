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

class UKAddressSpec extends AnyWordSpec with Matchers {

  "UKAddress JSON format" should {

    "serialize UKAddress to JSON" in {
      val address = UKAddress(
        addressLine1 = "10 Downing Street",
        addressLine2 = Some("Westminster"),
        addressLine3 = "London",
        addressLine4 = Some("Greater London"),
        postCode = "SW1A 2AA"
      )

      val json = Json.toJson(address)

      json shouldBe Json.obj(
        "addressLine1" -> "10 Downing Street",
        "addressLine2" -> "Westminster",
        "addressLine3" -> "London",
        "addressLine4" -> "Greater London",
        "postCode"     -> "SW1A 2AA"
      )
    }

    "deserialize JSON to UKAddress when addressLine4 is present" in {
      val json = Json.obj(
        "addressLine1" -> "10 Downing Street",
        "addressLine2" -> "Westminster",
        "addressLine3" -> "London",
        "addressLine4" -> "Greater London",
        "postCode"     -> "SW1A 2AA"
      )

      val result = json.validate[UKAddress]

      result shouldBe JsSuccess(
        UKAddress(
          "10 Downing Street",
          Some("Westminster"),
          "London",
          Some("Greater London"),
          "SW1A 2AA"
        )
      )
    }

    "deserialize JSON to UKAddress when addressLine4 is missing" in {
      val json = Json.obj(
        "addressLine1" -> "221B Baker Street",
        "addressLine2" -> "Marylebone",
        "addressLine3" -> "London",
        "postCode"     -> "NW1 6XE"
      )

      val result = json.validate[UKAddress]

      result shouldBe JsSuccess(
        UKAddress(
          "221B Baker Street",
          Some("Marylebone"),
          "London",
          None,
          "NW1 6XE"
        )
      )
    }
  }
}
