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
import models.*

import java.time.LocalDateTime

class GetCurrentVerificationBatchResponseSpec extends AnyWordSpec with Matchers {

  "GetCurrentVerificationBatchResponse Json format" should {

    "write a response to JSON" in {
      val model = GetCurrentVerificationBatchResponse(
        subcontractors = Seq(
          SubcontractorCurrentVerification(
            subcontractorId = 1L,
            subbieResourceRef = Some(10L),
            firstName = Some("John"),
            secondName = None,
            surname = Some("Smith"),
            tradingName = Some("ACME"),
            utr = Some("1111111111"),
            nino = Some("AA123456A"),
            crn = Some("AC012345"),
            partnerUtr = Some("5860920998"),
            partnershipTradingName = Some("ACME trading"),
            subcontractorType = Some("soletrader"),
            addressLine1 = Some("1 Test Street"),
            addressLine2 = None,
            addressLine3 = None,
            addressLine4 = None,
            country = Some("GB"),
            postcode = Some("AA1 1AA"),
            emailAddress = Some("john@test.com"),
            phoneNumber = Some("01911234567"),
            mobilePhoneNumber = Some("07123456789"),
            worksReferenceNumber = Some("WRN123"),
            matched = Some("Y"),
            autoVerified = Some("N"),
            verified = Some("Y"),
            verificationNumber = Some("V123456"),
            taxTreatment = Some("0"),
            verificationDate = Some(LocalDateTime.parse("2026-07-23T10:15:30")),
            version = Some(1),
            updatedTaxTreatment = Some("1"),
            lastMonthlyReturnDate = Some(LocalDateTime.parse("2026-06-30T00:00:00")),
            pendingVerifications = Some(2)
          )
        ),
        verificationBatch = Some(
          VerificationBatchCurrentVerification(
            verificationBatchId = 99L,
            verifBatchResourceRef = Some(999L)
          )
        ),
        verifications = Seq(
          VerificationCurrentVerification(
            verificationId = 1001L,
            verificationBatchId = Some(99L),
            subcontractorId = Some(1L),
            verificationResourceRef = Some(1L),
            subcontractorName = Some("John Smith"),
            verificationNumber = Some("V123456"),
            taxTreatment = Some("0"),
            actionIndicator = Some("A"),
            proceed = Some("Y"),
            matched = Some("Y")
          )
        )
      )

      val json = Json.toJson(model)

      val sub0 = (json \ "subcontractors")(0)

      (sub0 \ "subcontractorType").as[String] mustBe "soletrader"
      (sub0 \ "addressLine1").as[String] mustBe "1 Test Street"
      (sub0 \ "country").as[String] mustBe "GB"
      (sub0 \ "postcode").as[String] mustBe "AA1 1AA"
      (sub0 \ "emailAddress").as[String] mustBe "john@test.com"
      (sub0 \ "phoneNumber").as[String] mustBe "01911234567"
      (sub0 \ "mobilePhoneNumber").as[String] mustBe "07123456789"
      (sub0 \ "worksReferenceNumber").as[String] mustBe "WRN123"
      (sub0 \ "matched").as[String] mustBe "Y"
      (sub0 \ "autoVerified").as[String] mustBe "N"
      (sub0 \ "verified").as[String] mustBe "Y"
      (sub0 \ "verificationNumber").as[String] mustBe "V123456"
      (sub0 \ "taxTreatment").as[String] mustBe "0"
      (sub0 \ "verificationDate").as[String] mustBe "2026-07-23T10:15:30"
      (sub0 \ "version").as[Int] mustBe 1
      (sub0 \ "updatedTaxTreatment").as[String] mustBe "1"
      (sub0 \ "lastMonthlyReturnDate").as[String] mustBe "2026-06-30T00:00:00"
      (sub0 \ "pendingVerifications").as[Int] mustBe 2

      val vb0 = json \ "verificationBatch"

      (vb0 \ "verificationBatchId").as[Long] mustBe 99L
      (vb0 \ "verifBatchResourceRef").as[Long] mustBe 999L

      val v0 = (json \ "verifications")(0)

      (v0 \ "verificationId").as[Long] mustBe 1001L
      (v0 \ "verificationBatchId").as[Long] mustBe 99L
      (v0 \ "subcontractorId").as[Long] mustBe 1L
      (v0 \ "verificationResourceRef").as[Long] mustBe 1L
      (v0 \ "subcontractorName").as[String] mustBe "John Smith"
      (v0 \ "verificationNumber").as[String] mustBe "V123456"
      (v0 \ "taxTreatment").as[String] mustBe "0"
      (v0 \ "actionIndicator").as[String] mustBe "A"
      (v0 \ "proceed").as[String] mustBe "Y"
      (v0 \ "matched").as[String] mustBe "Y"
    }

    "round-trip (model -> json -> model) without losing data" in {
      val model = GetCurrentVerificationBatchResponse(
        subcontractors = Seq.empty,
        verificationBatch = None,
        verifications = Seq.empty
      )

      val json = Json.toJson(model)
      json.validate[GetCurrentVerificationBatchResponse] mustBe JsSuccess(model)
    }
  }
}
