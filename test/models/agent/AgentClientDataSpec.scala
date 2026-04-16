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

package models.agent

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.{JsSuccess, Json}

class AgentClientDataSpec extends AnyFreeSpec with Matchers {

  "AgentClientData JSON format" - {

    "must read from JSON when schemeName is present" in {
      val json = Json.parse("""
        {
          "uniqueId": "unique-id-123",
          "taxOfficeNumber": "123",
          "taxOfficeReference": "AB12345",
          "schemeName": "My Scheme"
        }
      """)

      json.validate[AgentClientData] mustBe JsSuccess(
        AgentClientData(
          uniqueId = "unique-id-123",
          taxOfficeNumber = "123",
          taxOfficeReference = "AB12345",
          schemeName = Some("My Scheme")
        )
      )
    }

    "must read from JSON when schemeName is missing" in {
      val json = Json.parse("""
        {
          "uniqueId": "unique-id-123",
          "taxOfficeNumber": "123",
          "taxOfficeReference": "AB12345"
        }
      """)

      json.validate[AgentClientData] mustBe JsSuccess(
        AgentClientData(
          uniqueId = "unique-id-123",
          taxOfficeNumber = "123",
          taxOfficeReference = "AB12345",
          schemeName = None
        )
      )
    }

    "must write to JSON (round-trip)" in {
      val model = AgentClientData(
        uniqueId = "unique-id-123",
        taxOfficeNumber = "123",
        taxOfficeReference = "AB12345",
        schemeName = Some("My Scheme")
      )

      val json = Json.toJson(model)

      (json \ "uniqueId").as[String] mustBe "unique-id-123"
      (json \ "taxOfficeNumber").as[String] mustBe "123"
      (json \ "taxOfficeReference").as[String] mustBe "AB12345"
      (json \ "schemeName").as[String] mustBe "My Scheme"

      json.validate[AgentClientData] mustBe JsSuccess(model)
    }
  }
}
