package models.requests

import base.SpecBase
import play.api.libs.json.Json

import java.time.LocalDateTime

class UpdateSubcontractorRequestSpec extends SpecBase {

  private val subcontractor =
    SubcontractorRequest(
      subcontractorId = 123L,
      utr = Some("1234567890"),
      pageVisited = Some(2),
      partnerUtr = Some("0987654321"),
      crn = Some("AC012345"),
      firstName = Some("Martin"),
      nino = Some("QQ123456C"),
      secondName = Some("James"),
      surname = Some("Brody"),
      partnershipTradingName = Some("Brody Partnership"),
      tradingName = Some("Brody Construction"),
      subcontractorType = Some("soletrader"),
      addressLine1 = Some("12 Harbor View Road"),
      addressLine2 = Some("Amity Island"),
      addressLine3 = Some("Bodmin"),
      addressLine4 = Some("Cornwall"),
      country = Some("England"),
      postcode = Some("PL31 2HL"),
      emailAddress = Some("martin@example.com"),
      phoneNumber = Some("02071234567"),
      mobilePhoneNumber = Some("07123456789"),
      worksReferenceNumber = Some("XLS345-MM"),
      createDate = Some(LocalDateTime.of(2026, 7, 1, 10, 30)),
      lastUpdate = Some(LocalDateTime.of(2026, 7, 2, 11, 45)),
      subbieResourceRef = Some(1001L),
      matched = Some("Y"),
      autoVerified = Some("N"),
      verified = Some("Y"),
      verificationNumber = Some("V1234567890"),
      taxTreatment = Some("gross"),
      verificationDate = Some(LocalDateTime.of(2026, 7, 3, 12, 0)),
      version = Some(3),
      updatedTaxTreatment = Some("standardRate"),
      lastMonthlyReturnDate = Some(LocalDateTime.of(2026, 6, 30, 23, 59)),
      pendingVerifications = Some(1)
    )

  private val request =
    UpdateSubcontractorRequest(
      cisId = "CIS-123",
      subcontractor = subcontractor
    )

  "UpdateSubcontractorRequest" - {

    "must serialise to JSON" in {
      val json =
        Json.toJson(request)

      (json \ "cisId").as[String] mustBe "CIS-123"

      val subcontractorJson =
        json \ "subcontractor"

      (subcontractorJson \ "subcontractorId")
        .as[Long] mustBe 123L

      (subcontractorJson \ "utr")
        .as[String] mustBe "1234567890"

      (subcontractorJson \ "subcontractorType")
        .as[String] mustBe "soletrader"

      (subcontractorJson \ "subbieResourceRef")
        .as[Long] mustBe 1001L

      (subcontractorJson \ "verified")
        .as[String] mustBe "Y"

      (subcontractorJson \ "pendingVerifications")
        .as[Int] mustBe 1
    }

    "must deserialise from JSON" in {
      val json =
        Json.toJson(request)

      json.as[UpdateSubcontractorRequest] mustBe request
    }

    "must round trip through JSON" in {
      val json =
        Json.toJson(request)

      val result =
        Json.fromJson[UpdateSubcontractorRequest](json)

      result.get mustBe request
    }

    "must handle optional fields when they are absent" in {
      val subcontractorWithoutOptionalValues =
        subcontractor.copy(
          utr = None,
          pageVisited = None,
          partnerUtr = None,
          crn = None,
          firstName = None,
          nino = None,
          secondName = None,
          surname = None,
          partnershipTradingName = None,
          tradingName = None,
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
          createDate = None,
          lastUpdate = None,
          subbieResourceRef = None,
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

      val requestWithoutOptionalValues =
        UpdateSubcontractorRequest(
          cisId = "CIS-123",
          subcontractor = subcontractorWithoutOptionalValues
        )

      val json =
        Json.toJson(requestWithoutOptionalValues)

      json.as[UpdateSubcontractorRequest] mustBe
        requestWithoutOptionalValues
    }
  }

  "SubcontractorRequest" - {

    "must serialise and deserialise successfully" in {
      val json =
        Json.toJson(subcontractor)

      json.as[SubcontractorRequest] mustBe subcontractor
    }

    "must preserve LocalDateTime fields" in {
      val json =
        Json.toJson(subcontractor)

      val result =
        json.as[SubcontractorRequest]

      result.createDate mustBe
        Some(LocalDateTime.of(2026, 7, 1, 10, 30))

      result.lastUpdate mustBe
        Some(LocalDateTime.of(2026, 7, 2, 11, 45))

      result.verificationDate mustBe
        Some(LocalDateTime.of(2026, 7, 3, 12, 0))

      result.lastMonthlyReturnDate mustBe
        Some(LocalDateTime.of(2026, 6, 30, 23, 59))
    }
  }
}
