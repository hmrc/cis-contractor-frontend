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

import base.SpecBase
import play.api.libs.json.Json

class SubcontractorNameSpec extends SpecBase {

  "SubcontractorName" - {
    "serialise to JSON correctly" in {
      val subcontractorName = SubcontractorName("Alice", Some("Chloe"), "Smith")
      val json = Json.toJson(subcontractorName)

      (json \ "firstName").as[String] mustBe "Alice"
      (json \ "middleName").as[String] mustBe "Chloe"
      (json \ "lastName").as[String] mustBe "Smith"
    }

    "deserialize from JSON correctly" in {
      val json = Json.parse(
        """
          |{
          |  "firstName": "Alice",
          |  "middleName": "Chloe",
          |  "lastName": "Smith"
          |}
          |""".stripMargin
      )
      val result = json.as[SubcontractorName]
      result.firstName mustBe "Alice"
      result.middleName mustBe Some("Chloe")
      result.lastName mustBe "Smith"
    }

    "round-trip serialize and deserialize correctly" in {
      val subcontractorName = SubcontractorName("Alice", Some("Chloe"), "Smith")
      val json = Json.toJson(subcontractorName)
      val result = json.as[SubcontractorName]
      result mustBe subcontractorName
    }
  }
}
