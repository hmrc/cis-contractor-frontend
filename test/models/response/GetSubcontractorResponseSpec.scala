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

import java.time.{Instant, LocalDateTime}

class GetSubcontractorResponseSpec extends AnyWordSpec with Matchers {

  private val scheme =
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
      createDate = Some(Instant.parse("2026-01-10T12:30:00Z")),
      lastUpdate = Some(Instant.parse("2026-01-11T13:45:00Z")),
      version = Some(3)
    )

  private val subcontractor =
    SubcontractorResponse(
      subcontractorId = 1L,
      utr = Some("9999999999"),
      pageVisited = Some(2),
      partnerUtr = None,
      crn = Some("12345678"),
      firstName = Some("Martin"),
      nino = Some("AA123456A"),
      secondName = None,
      surname = Some("Brody"),
      partnershipTradingName = None,
      tradingName = Some("Brody Trading"),
      subcontractorType = Some("soletrader"),
      addressLine1 = Some("1 Test Street"),
      addressLine2 = None,
      addressLine3 = None,
      addressLine4 = None,
      country = Some("United Kingdom"),
      postcode = Some("AA1 1AA"),
      emailAddress = Some("subcontractor@example.com"),
      phoneNumber = None,
      mobilePhoneNumber = Some("07123456789"),
      worksReferenceNumber = Some("WRN-123"),
      createDate = Some(LocalDateTime.of(2026, 1, 10, 12, 30)),
      lastUpdate = Some(LocalDateTime.of(2026, 1, 11, 13, 45)),
      subbieResourceRef = Some(1001L),
      matched = Some("Y"),
      autoVerified = Some("N"),
      verified = Some("Y"),
      verificationNumber = Some("V1234567890"),
      taxTreatment = Some("gross"),
      verificationDate = Some(LocalDateTime.of(2026, 1, 12, 9, 15)),
      version = Some(3),
      updatedTaxTreatment = None,
      lastMonthlyReturnDate = Some(LocalDateTime.of(2026, 1, 20, 0, 0)),
      pendingVerifications = Some(1)
    )

  private val otherInfo =
    GetSubcontractorOtherInfo(
      utr = "1111111111"
    )

  private val model =
    GetSubcontractorResponse(
      scheme = Some(scheme),
      subcontractor = Some(subcontractor),
      otherInfo = Seq(otherInfo)
    )

  "GetSubcontractorResponse JSON format" should {

    "write a GetSubcontractorResponse to JSON" in {
      val json = Json.toJson(model)

      (json \ "scheme").as[ContractorSchemeResponse] mustBe scheme
      (json \ "subcontractor").as[SubcontractorResponse] mustBe subcontractor
      (json \ "otherInfo").as[Seq[GetSubcontractorOtherInfo]] mustBe Seq(otherInfo)

      (json \ "subcontractor" \ "displayName").as[String] mustBe "Martin Brody"
    }

    "read JSON into a GetSubcontractorResponse" in {
      val json = Json.obj(
        "scheme" -> Json.toJson(scheme),
        "subcontractor" -> Json.toJson(subcontractor),
        "otherInfo" -> Json.arr(
          Json.toJson(otherInfo)
        )
      )

      json.validate[GetSubcontractorResponse] mustBe JsSuccess(model)
    }

    "read JSON when scheme and subcontractor are null" in {
      val json = Json.obj(
        "scheme" -> Json.toJson(Option.empty[ContractorSchemeResponse]),
        "subcontractor" -> Json.toJson(Option.empty[SubcontractorResponse]),
        "otherInfo" -> Json.arr()
      )

      json.validate[GetSubcontractorResponse] mustBe JsSuccess(
        GetSubcontractorResponse(
          scheme = None,
          subcontractor = None,
          otherInfo = Seq.empty
        )
      )
    }

    "read JSON when optional fields are missing" in {
      val json = Json.obj(
        "otherInfo" -> Json.arr()
      )

      json.validate[GetSubcontractorResponse] mustBe JsSuccess(
        GetSubcontractorResponse(
          scheme = None,
          subcontractor = None,
          otherInfo = Seq.empty
        )
      )
    }

    "round-trip without losing data" in {
      val json = Json.toJson(model)

      json.validate[GetSubcontractorResponse] mustBe JsSuccess(model)
    }
  }

  "GetSubcontractorOtherInfo JSON format" should {

    "write and read GetSubcontractorOtherInfo" in {
      val json = Json.toJson(otherInfo)

      (json \ "utr").as[String] mustBe "1111111111"
      json.validate[GetSubcontractorOtherInfo] mustBe JsSuccess(otherInfo)
    }
  }
}