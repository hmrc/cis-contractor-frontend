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

import java.time.LocalDateTime

class SubcontractorResponseSpec extends AnyWordSpec with Matchers {

  private val createDate       = LocalDateTime.of(2026, 1, 10, 12, 30)
  private val lastUpdate       = LocalDateTime.of(2026, 1, 11, 13, 45)
  private val verificationDate = LocalDateTime.of(2026, 1, 12, 9, 15)
  private val lastReturnDate   = LocalDateTime.of(2026, 1, 20, 0, 0)

  private def subcontractor(
                             subcontractorType: Option[String] = Some("soletrader"),
                             firstName: Option[String] = Some("Martin"),
                             surname: Option[String] = Some("Brody"),
                             tradingName: Option[String] = Some("Brody Trading"),
                             partnershipTradingName: Option[String] = None
                           ): SubcontractorResponse =
    SubcontractorResponse(
      subcontractorId = 1L,
      utr = Some("1234567890"),
      pageVisited = Some(2),
      partnerUtr = Some("9876543210"),
      crn = Some("12345678"),
      firstName = firstName,
      nino = Some("AA123456A"),
      secondName = Some("James"),
      surname = surname,
      partnershipTradingName = partnershipTradingName,
      tradingName = tradingName,
      subcontractorType = subcontractorType,
      addressLine1 = Some("1 Test Street"),
      addressLine2 = Some("Test Area"),
      addressLine3 = Some("Test Town"),
      addressLine4 = Some("Test County"),
      country = Some("United Kingdom"),
      postcode = Some("AA1 1AA"),
      emailAddress = Some("test@example.com"),
      phoneNumber = Some("02070000000"),
      mobilePhoneNumber = Some("07123456789"),
      worksReferenceNumber = Some("WRN-123"),
      createDate = Some(createDate),
      lastUpdate = Some(lastUpdate),
      subbieResourceRef = Some(1001L),
      matched = Some("Y"),
      autoVerified = Some("N"),
      verified = Some("Y"),
      verificationNumber = Some("V1234567890"),
      taxTreatment = Some("gross"),
      verificationDate = Some(verificationDate),
      version = Some(3),
      updatedTaxTreatment = Some("net"),
      lastMonthlyReturnDate = Some(lastReturnDate),
      pendingVerifications = Some(1)
    )

  "SubcontractorResponse.displayName" should {

    "return first name and surname for a sole trader" in {
      subcontractor().displayName mustBe "Martin Brody"
    }

    "return surname when a sole trader has no first name" in {
      subcontractor(
        firstName = None,
        surname = Some("Brody")
      ).displayName mustBe "Brody"
    }

    "return trading name when a sole trader has no personal name" in {
      subcontractor(
        firstName = None,
        surname = None,
        tradingName = Some("Brody Trading")
      ).displayName mustBe "Brody Trading"
    }

    "return trading name for a company" in {
      subcontractor(
        subcontractorType = Some("company"),
        firstName = None,
        surname = None,
        tradingName = Some("Hammond House Ltd")
      ).displayName mustBe "Hammond House Ltd"
    }

    "return trading name for a trust" in {
      subcontractor(
        subcontractorType = Some("trust"),
        firstName = None,
        surname = None,
        tradingName = Some("Hammond Trust")
      ).displayName mustBe "Hammond Trust"
    }

    "return partnership trading name for a partnership when present" in {
      subcontractor(
        subcontractorType = Some("partnership"),
        firstName = None,
        surname = None,
        tradingName = Some("Fallback Partnership"),
        partnershipTradingName = Some("Brody and Hooper")
      ).displayName mustBe "Brody and Hooper"
    }

    "return trading name for a partnership when partnership trading name is missing" in {
      subcontractor(
        subcontractorType = Some("partnership"),
        firstName = None,
        surname = None,
        tradingName = Some("Fallback Partnership"),
        partnershipTradingName = None
      ).displayName mustBe "Fallback Partnership"
    }

    "return No name provided when no matching name is available" in {
      subcontractor(
        subcontractorType = None,
        firstName = None,
        surname = None,
        tradingName = None,
        partnershipTradingName = None
      ).displayName mustBe "No name provided"
    }

    "match subcontractor type without regard to case" in {
      subcontractor(
        subcontractorType = Some("COMPANY"),
        firstName = None,
        surname = None,
        tradingName = Some("Upper Case Ltd")
      ).displayName mustBe "Upper Case Ltd"
    }
  }

  "SubcontractorResponse JSON format" should {

    "write a subcontractor response to JSON including displayName" in {
      val model = subcontractor()

      val json = Json.toJson(model)

      (json \ "subcontractorId").as[Long] mustBe 1L
      (json \ "utr").as[String] mustBe "1234567890"
      (json \ "firstName").as[String] mustBe "Martin"
      (json \ "surname").as[String] mustBe "Brody"
      (json \ "createDate").as[LocalDateTime] mustBe createDate
      (json \ "verificationDate").as[LocalDateTime] mustBe verificationDate
      (json \ "displayName").as[String] mustBe "Martin Brody"
    }

    "read JSON into a SubcontractorResponse" in {
      val model = subcontractor()

      val json =
        Json.toJson(model).as[play.api.libs.json.JsObject] - "displayName"

      json.validate[SubcontractorResponse] mustBe JsSuccess(model)
    }

    "read JSON when optional fields are missing" in {
      val json = Json.obj(
        "subcontractorId" -> 1L
      )

      val result = json.validate[SubcontractorResponse]

      result mustBe a[JsSuccess[?]]

      val model = result.get

      model.subcontractorId mustBe 1L
      model.utr mustBe None
      model.firstName mustBe None
      model.surname mustBe None
      model.displayName mustBe "No name provided"
    }

    "round-trip without losing model data" in {
      val model = subcontractor()

      val jsonWithoutDisplayName =
        Json.toJson(model).as[play.api.libs.json.JsObject] - "displayName"

      jsonWithoutDisplayName.as[SubcontractorResponse] mustBe model
    }
  }
}
