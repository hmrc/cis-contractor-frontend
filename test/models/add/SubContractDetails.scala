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

class SubContractDetails extends SpecBase {

  "SubContractDetails" - {
    "serialise to JSON correctly" in {
      val subContactDetails = SubContactDetails("user@domain.com", "07777777777")
      val json = Json.toJson(subContactDetails)

      (json \ "email").as[String] mustBe "user@domain.com"
      (json \ "telephone").as[String] mustBe "07777777777"
    }

    "deserialize from JSON correctly" in {
      val json = Json.parse(
        """
          |{
          |  "email": "user@domain.com",
          |  "telephone": "07777777777"
          |}
          |""".stripMargin
      )
      val result = json.as[SubContactDetails]
      result.email mustBe "user@domain.com"
      result.telephone mustBe "07777777777"
    }

    "round-trip serialize and deserialize correctly" in {
      val subContactDetails = SubContactDetails("user@domain.com", "07777777777")
      val json = Json.toJson(subContactDetails)
      val result = json.as[SubContactDetails]
      result mustBe subContactDetails
    }
  }
}
