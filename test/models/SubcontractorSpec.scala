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

package models

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.{JsSuccess, Json}
import java.time.LocalDateTime

class SubcontractorSpec extends AnyWordSpec with Matchers {

  "Subcontractor JSON format" should {

    "read JSON into model with missing optional fields" in {

      val json = Json.parse(
        """
          |{
          |  "subcontractorId": 123
          |}
          |""".stripMargin
      )

      val out = json.as[Subcontractor]

      out.subcontractorId mustBe 123L
      out.firstName mustBe None
      out.surname mustBe None
      out.verified mustBe None
      out.tradingName mustBe None
    }

    "write model to JSON" in {

      val now = LocalDateTime.now()

      val model = Subcontractor(
        subcontractorId = 123L,
        firstName = Some("John"),
        secondName = None,
        surname = Some("Doe"),
        tradingName = Some("JD Traders"),
        partnershipTradingName = None,
        verified = Some("Y"),
        verificationNumber = Some("ABC123"),
        taxTreatment = None,
        verificationDate = Some(now),
        lastMonthlyReturnDate = None,
        createDate = None,
        subcontractorType = Some("company"),
        subbieResourceRef = None,
        utr = None,
        partnerUtr = None,
        crn = None,
        nino = None
      )

      val json = Json.toJson(model)

      (json \ "subcontractorId").as[Long] mustBe 123L
      (json \ "firstName").as[String] mustBe "John"
      (json \ "surname").as[String] mustBe "Doe"
      (json \ "verified").as[String] mustBe "Y"
    }

    "round-trip (model -> json -> model) without losing data" in {

      val model = Subcontractor(
        subcontractorId = 456L,
        firstName = Some("Jane"),
        secondName = None,
        surname = Some("Smith"),
        tradingName = Some("Smith Ltd"),
        partnershipTradingName = None,
        verified = Some("N"),
        verificationNumber = None,
        taxTreatment = None,
        verificationDate = None,
        lastMonthlyReturnDate = None,
        createDate = None,
        subcontractorType = Some("soletrader"),
        subbieResourceRef = None,
        utr = None,
        partnerUtr = None,
        crn = None,
        nino = None
      )

      val json = Json.toJson(model)

      json.validate[Subcontractor] mustBe JsSuccess(model)
    }
  }

  "isVerified helper" should {

    "return true when verified is Y" in {
      val sub = Subcontractor(
        subcontractorId = 1L,
        firstName = None,
        secondName = None,
        surname = None,
        tradingName = None,
        partnershipTradingName = None,
        verified = Some("Y"),
        verificationNumber = None,
        taxTreatment = None,
        verificationDate = None,
        lastMonthlyReturnDate = None,
        createDate = None,
        subcontractorType = None,
        subbieResourceRef = None,
        utr = None,
        partnerUtr = None,
        crn = None,
        nino = None
      )

      sub.isVerified mustBe true
    }

    "return true when verified is case-insensitive y" in {
      val sub = Subcontractor(
        subcontractorId = 1L,
        firstName = None,
        secondName = None,
        surname = None,
        tradingName = None,
        partnershipTradingName = None,
        verified = Some("y"),
        verificationNumber = None,
        taxTreatment = None,
        verificationDate = None,
        lastMonthlyReturnDate = None,
        createDate = None,
        subcontractorType = None,
        subbieResourceRef = None,
        utr = None,
        partnerUtr = None,
        crn = None,
        nino = None
      )

      sub.isVerified mustBe true
    }

    "return false when verified is N" in {
      val sub = Subcontractor(
        subcontractorId = 1L,
        firstName = None,
        secondName = None,
        surname = None,
        tradingName = None,
        partnershipTradingName = None,
        verified = Some("N"),
        verificationNumber = None,
        taxTreatment = None,
        verificationDate = None,
        lastMonthlyReturnDate = None,
        createDate = None,
        subcontractorType = None,
        subbieResourceRef = None,
        utr = None,
        partnerUtr = None,
        crn = None,
        nino = None
      )

      sub.isVerified mustBe false
    }

    "return false when verified is None" in {
      val sub = Subcontractor(
        subcontractorId = 1L,
        firstName = None,
        secondName = None,
        surname = None,
        tradingName = None,
        partnershipTradingName = None,
        verified = None,
        verificationNumber = None,
        taxTreatment = None,
        verificationDate = None,
        lastMonthlyReturnDate = None,
        createDate = None,
        subcontractorType = None,
        subbieResourceRef = None,
        utr = None,
        partnerUtr = None,
        crn = None,
        nino = None
      )

      sub.isVerified mustBe false
    }
  }
}
