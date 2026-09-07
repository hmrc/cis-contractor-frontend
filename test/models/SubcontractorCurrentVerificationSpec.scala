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

import base.SpecBase
import models.TypeOfSubcontractor.{Individualorsoletrader, Limitedcompany, Partnership, Trust}
import org.scalatest.matchers.should.Matchers.shouldBe
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.test.FakeRequest

import java.time.LocalDateTime

class SubcontractorCurrentVerificationSpec extends SpecBase {

  private implicit val messages: Messages =
    app.injector.instanceOf[play.api.i18n.MessagesApi].preferred(FakeRequest())

  def subcontractor(
    subcontractorType: Option[String] = None,
    firstName: Option[String] = None,
    surname: Option[String] = None,
    tradingName: Option[String] = None,
    partnershipTradingName: Option[String] = None
  ): SubcontractorCurrentVerification =
    SubcontractorCurrentVerification(
      subcontractorId = 1L,
      subbieResourceRef = None,
      firstName = firstName,
      secondName = None,
      surname = surname,
      tradingName = tradingName,
      utr = None,
      nino = None,
      crn = None,
      partnerUtr = None,
      partnershipTradingName = partnershipTradingName,
      subcontractorType = None,
      addressLine1 = None,
      addressLine2 = None,
      addressLine3 = None,
      addressLine4 = None,
      country = None,
      postcode = None,
      emailAddress = None,
      phoneNumber = None,
      mobilePhoneNumber = None,
      worksReferenceNumber = None,
      matched = None,
      autoVerified = None,
      verified = None,
      verificationNumber = None,
      taxTreatment = None,
      verificationDate = None,
      version = None,
      updatedTaxTreatment = None,
      lastMonthlyReturnDate = None,
      pendingVerifications = None
    )

  "SubcontractorCurrentVerification" - {
    "serialize to JSON correctly" in {
      val subcontractors = SubcontractorCurrentVerification(
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
        subcontractorType = Some("Individual"),
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
      val json           = Json.toJson(subcontractors)

      (json \ "subcontractorId").as[Long] mustBe 1L
      (json \ "subbieResourceRef").as[Long] mustBe 10L
      (json \ "firstName").as[String] mustBe "John"
      (json \ "surname").as[String] mustBe "Smith"
      (json \ "secondName").toOption mustBe None
      (json \ "tradingName").as[String] mustBe "ACME"
      (json \ "utr").as[String] mustBe "1111111111"
      (json \ "nino").as[String] mustBe "AA123456A"
      (json \ "crn").as[String] mustBe "AC012345"
      (json \ "partnerUtr").as[String] mustBe "5860920998"
      (json \ "partnershipTradingName").as[String] mustBe "ACME trading"
      (json \ "subcontractorType").as[String] mustBe "Individual"
      (json \ "addressLine1").as[String] mustBe "1 Test Street"
      (json \ "country").as[String] mustBe "GB"
      (json \ "postcode").as[String] mustBe "AA1 1AA"
      (json \ "emailAddress").as[String] mustBe "john@test.com"
      (json \ "phoneNumber").as[String] mustBe "01911234567"
      (json \ "mobilePhoneNumber").as[String] mustBe "07123456789"
      (json \ "worksReferenceNumber").as[String] mustBe "WRN123"
      (json \ "matched").as[String] mustBe "Y"
      (json \ "autoVerified").as[String] mustBe "N"
      (json \ "verified").as[String] mustBe "Y"
      (json \ "verificationNumber").as[String] mustBe "V123456"
      (json \ "taxTreatment").as[String] mustBe "0"
      (json \ "verificationDate").as[String] mustBe "2026-07-23T10:15:30"
      (json \ "version").as[Int] mustBe 1
      (json \ "updatedTaxTreatment").as[String] mustBe "1"
      (json \ "lastMonthlyReturnDate").as[String] mustBe "2026-06-30T00:00:00"
      (json \ "pendingVerifications").as[Int] mustBe 2
    }
    "deserialize from JSON correctly" in {
      val json   = Json.parse(
        """
          |{
          |  "subcontractorId": 1,
          |  "subbieResourceRef": 10,
          |  "firstName" : "John",
          |  "surname" : "Smith",
          |  "secondName" : "Paul",
          |  "tradingName" : "ACME",
          |  "utr" : "1111111111",
          |  "nino" : "AA123456A",
          |  "crn" : "AC012345",
          |  "partnerUtr" : "5860920998",
          |  "partnershipTradingName" : "ACME trading",
          |  "subcontractorType": "Individual",
          |  "addressLine1": "1 Test Street",
          |  "country": "GB",
          |  "postcode": "AA1 1AA",
          |  "emailAddress": "john@test.com",
          |  "phoneNumber": "01911234567",
          |  "mobilePhoneNumber": "07123456789",
          |  "worksReferenceNumber": "WRN123",
          |  "matched": "Y",
          |  "autoVerified": "N",
          |  "verified": "Y",
          |  "verificationNumber": "V123456",
          |  "taxTreatment": "0",
          |  "verificationDate": "2026-07-23T10:15:30",
          |  "version": 1,
          |  "updatedTaxTreatment": "1",
          |  "lastMonthlyReturnDate": "2026-06-30T00:00:00",
          |  "pendingVerifications": 2
          |}
          |""".stripMargin
      )
      val result = json.as[SubcontractorCurrentVerification]
      result.subcontractorId mustBe 1L
      result.subbieResourceRef mustBe Some(10L)
      result.firstName mustBe Some("John")
      result.surname mustBe Some("Smith")
      result.secondName mustBe Some("Paul")
      result.tradingName mustBe Some("ACME")
      result.utr mustBe Some("1111111111")
      result.nino mustBe Some("AA123456A")
      result.crn mustBe Some("AC012345")
      result.partnerUtr mustBe Some("5860920998")
      result.partnershipTradingName mustBe Some("ACME trading")
      result.subcontractorType mustBe Some("Individual")
      result.addressLine1 mustBe Some("1 Test Street")
      result.country mustBe Some("GB")
      result.postcode mustBe Some("AA1 1AA")
      result.emailAddress mustBe Some("john@test.com")
      result.phoneNumber mustBe Some("01911234567")
      result.mobilePhoneNumber mustBe Some("07123456789")
      result.worksReferenceNumber mustBe Some("WRN123")
      result.matched mustBe Some("Y")
      result.autoVerified mustBe Some("N")
      result.verified mustBe Some("Y")
      result.verificationNumber mustBe Some("V123456")
      result.taxTreatment mustBe Some("0")
      result.verificationDate mustBe Some(LocalDateTime.parse("2026-07-23T10:15:30"))
      result.version mustBe Some(1)
      result.updatedTaxTreatment mustBe Some("1")
      result.lastMonthlyReturnDate mustBe Some(LocalDateTime.parse("2026-06-30T00:00:00"))
      result.pendingVerifications mustBe Some(2)
    }
    "round-trip serialize and deserialize correctly" in {
      val subcontractors = SubcontractorCurrentVerification(
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
        emailAddress = None,
        phoneNumber = None,
        mobilePhoneNumber = None,
        worksReferenceNumber = Some("WRN123"),
        matched = None,
        autoVerified = None,
        verified = None,
        verificationNumber = None,
        taxTreatment = None,
        verificationDate = None,
        version = None,
        updatedTaxTreatment = None,
        lastMonthlyReturnDate = None,
        pendingVerifications = None
      )
      val json           = Json.toJson(subcontractors)
      val result         = json.as[SubcontractorCurrentVerification]
      result mustBe subcontractors
    }

    "displayName" - {

      "return partnership trading name for a partnership when available" in {
        subcontractor(
          subcontractorType = Some(Partnership.toString),
          firstName = Some("John"),
          surname = Some("Smith"),
          tradingName = Some("Trading Ltd"),
          partnershipTradingName = Some("Partnership Ltd")
        ).displayName shouldBe "Partnership Ltd"
      }

      "return trading name for a partnership when partnership trading name is not available" in {
        subcontractor(
          subcontractorType = Some(Partnership.toString),
          tradingName = Some("Trading Ltd")
        ).displayName shouldBe "Trading Ltd"
      }

      "return trading name for a limited company" in {
        subcontractor(
          subcontractorType = Some(Limitedcompany.toString),
          firstName = Some("John"),
          surname = Some("Smith"),
          tradingName = Some("Trading Ltd")
        ).displayName shouldBe "Trading Ltd"
      }

      "return trading name for a trust" in {
        subcontractor(
          subcontractorType = Some(Trust.toString),
          firstName = Some("John"),
          surname = Some("Smith"),
          tradingName = Some("Trading Ltd")
        ).displayName shouldBe "Trading Ltd"
      }

      "return surname and first name for an individual or sole trader" in {
        subcontractor(
          subcontractorType = Some(Individualorsoletrader.toString),
          firstName = Some("John"),
          surname = Some("Smith")
        ).displayName shouldBe "Smith, John"
      }

      "return trading name for an individual or sole trader when no individual name is available" in {
        subcontractor(
          subcontractorType = Some(Individualorsoletrader.toString),
          tradingName = Some("Trading Ltd")
        ).displayName shouldBe "Trading Ltd"
      }

      "return the no name message when no name is available" in {
        subcontractor().displayName shouldBe messages("verify.noName")
      }
    }
  }
}
