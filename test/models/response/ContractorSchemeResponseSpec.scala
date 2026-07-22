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

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsSuccess, Json}

import java.time.Instant

class ContractorSchemeResponseSpec extends AnyWordSpec with Matchers {

  private val createDate = Instant.parse("2026-01-10T12:30:00Z")
  private val lastUpdate = Instant.parse("2026-01-11T13:45:00Z")

  private val model =
    ContractorSchemeResponse(
      schemeId = 123,
      instanceId = "INST-123",
      accountsOfficeReference = "123PA00123456",
      taxOfficeNumber = "123",
      taxOfficeReference = "AB45678",
      utr = Some("1234567890"),
      name = Some("Test Contractor"),
      emailAddress = Some("contractor@example.com"),
      displayWelcomePage = Some("Y"),
      prePopCount = Some(10),
      prePopSuccessful = Some("Y"),
      subcontractorCounter = Some(20),
      verificationBatchCounter = Some(5),
      createDate = Some(createDate),
      lastUpdate = Some(lastUpdate),
      version = Some(3)
    )

  "ContractorSchemeResponse JSON format" should {

    "write a ContractorSchemeResponse to JSON" in {
      val json = Json.toJson(model)

      (json \ "schemeId").as[Int] mustBe 123
      (json \ "instanceId").as[String] mustBe "INST-123"
      (json \ "accountsOfficeReference").as[String] mustBe "123PA00123456"
      (json \ "taxOfficeNumber").as[String] mustBe "123"
      (json \ "taxOfficeReference").as[String] mustBe "AB45678"
      (json \ "utr").as[String] mustBe "1234567890"
      (json \ "name").as[String] mustBe "Test Contractor"
      (json \ "emailAddress").as[String] mustBe "contractor@example.com"
      (json \ "prePopCount").as[Int] mustBe 10
      (json \ "subcontractorCounter").as[Int] mustBe 20
      (json \ "verificationBatchCounter").as[Int] mustBe 5
      (json \ "createDate").as[Instant] mustBe createDate
      (json \ "lastUpdate").as[Instant] mustBe lastUpdate
      (json \ "version").as[Int] mustBe 3
    }

    "read JSON into a ContractorSchemeResponse" in {
      val json = Json.obj(
        "schemeId" -> 123,
        "instanceId" -> "INST-123",
        "accountsOfficeReference" -> "123PA00123456",
        "taxOfficeNumber" -> "123",
        "taxOfficeReference" -> "AB45678",
        "utr" -> "1234567890",
        "name" -> "Test Contractor",
        "emailAddress" -> "contractor@example.com",
        "displayWelcomePage" -> "Y",
        "prePopCount" -> 10,
        "prePopSuccessful" -> "Y",
        "subcontractorCounter" -> 20,
        "verificationBatchCounter" -> 5,
        "createDate" -> createDate,
        "lastUpdate" -> lastUpdate,
        "version" -> 3
      )

      json.validate[ContractorSchemeResponse] mustBe JsSuccess(model)
    }

    "read JSON when optional fields are missing" in {
      val json = Json.obj(
        "schemeId" -> 123,
        "instanceId" -> "INST-123",
        "accountsOfficeReference" -> "123PA00123456",
        "taxOfficeNumber" -> "123",
        "taxOfficeReference" -> "AB45678"
      )

      val result = json.validate[ContractorSchemeResponse]

      result mustBe JsSuccess(
        ContractorSchemeResponse(
          schemeId = 123,
          instanceId = "INST-123",
          accountsOfficeReference = "123PA00123456",
          taxOfficeNumber = "123",
          taxOfficeReference = "AB45678"
        )
      )
    }

    "round-trip without losing data" in {
      val json = Json.toJson(model)

      json.validate[ContractorSchemeResponse] mustBe JsSuccess(model)
    }
  }
}