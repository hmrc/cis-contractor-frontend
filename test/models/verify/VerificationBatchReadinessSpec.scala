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

package models.verify

import models.{SubcontractorCurrentVerification, VerificationCurrentVerification}
import base.SpecBase
import models.response.GetCurrentVerificationBatchResponse

import java.time.LocalDateTime

class VerificationBatchReadinessSpec extends SpecBase {

  private def sub(
    id: Long,
    subType: Option[String],
    utr: Option[String] = None,
    tradingName: Option[String] = None,
    firstName: Option[String] = None,
    surname: Option[String] = None,
    partnershipTradingName: Option[String] = None,
    partnerUtr: Option[String] = None,
    nino: Option[String] = None,
    crn: Option[String] = None
  ): SubcontractorCurrentVerification =
    SubcontractorCurrentVerification(
      subcontractorId = id,
      subbieResourceRef = None,
      firstName = firstName,
      secondName = None,
      surname = surname,
      tradingName = tradingName,
      utr = utr,
      nino = nino,
      crn = crn,
      partnerUtr = partnerUtr,
      partnershipTradingName = partnershipTradingName,
      subcontractorType = subType,
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

  private def verification(
    subcontractorId: Long,
    proceed: Option[String] = None
  ): VerificationCurrentVerification =
    VerificationCurrentVerification(
      verificationId = subcontractorId,
      verificationBatchId = None,
      subcontractorId = Some(subcontractorId),
      verificationResourceRef = None,
      subcontractorName = None,
      verificationNumber = None,
      taxTreatment = None,
      actionIndicator = None,
      proceed = proceed,
      matched = None
    )

  private def batchResponse(
    subcontractors: Seq[SubcontractorCurrentVerification],
    verifications: Seq[VerificationCurrentVerification] = Seq.empty
  ): GetCurrentVerificationBatchResponse =
    GetCurrentVerificationBatchResponse(
      subcontractors = subcontractors,
      verificationBatch = None,
      verifications = verifications
    )

  "VerificationBatchReadiness.isSubcontractorReady" - {

    "returns true when proceed is Y even if minimum data requirements are not met" in {
      val s =
        sub(1, Some("company"), tradingName = Some("Acme Ltd"))

      VerificationBatchReadiness.isSubcontractorReady(
        s,
        Some(verification(1, Some("Y")))
      ) mustBe true
    }

    "returns false when proceed is N and minimum data requirements are not met" in {
      val s =
        sub(1, Some("company"), tradingName = Some("Acme Ltd"))

      VerificationBatchReadiness.isSubcontractorReady(
        s,
        Some(verification(1, Some("N")))
      ) mustBe false
    }
    // ─── Individual (soletrader) ────────────────────────────────────────────

    "Individual: passes when tradingName and utr are present" in {
      val s = sub(1, Some("soletrader"), utr = Some("1234567890"), tradingName = Some("Acme"))
      VerificationBatchReadiness.isSubcontractorReady(s, None) mustBe true
    }

    "Individual: passes when surname and utr are present" in {
      val s = sub(1, Some("soletrader"), utr = Some("1234567890"), firstName = Some("John"), surname = Some("Smith"))
      VerificationBatchReadiness.isSubcontractorReady(s, None) mustBe true
    }

    "Individual: fails when utr is absent" in {
      val s = sub(1, Some("soletrader"), tradingName = Some("Acme"))
      VerificationBatchReadiness.isSubcontractorReady(s, None) mustBe false
    }

    "Individual: fails when neither tradingName nor surname are present" in {
      val s = sub(1, Some("soletrader"), utr = Some("1234567890"))
      VerificationBatchReadiness.isSubcontractorReady(s, None) mustBe false
    }

    "Individual: fails when only firstName is present (surname missing)" in {
      val s = sub(1, Some("soletrader"), utr = Some("1234567890"), firstName = Some("John"))
      VerificationBatchReadiness.isSubcontractorReady(s, None) mustBe false
    }

    "Individual: fails when only surname is present (firstName missing)" in {
      val s = sub(1, Some("soletrader"), utr = Some("1234567890"), surname = Some("Smith"))
      VerificationBatchReadiness.isSubcontractorReady(s, None) mustBe true
    }

    // ─── Company ────────────────────────────────────────────────────────────

    "Company: passes when tradingName and utr are present" in {
      val s = sub(1, Some("company"), utr = Some("1234567890"), tradingName = Some("Acme Ltd"))
      VerificationBatchReadiness.isSubcontractorReady(s, None) mustBe true
    }

    "Company: fails when tradingName is absent" in {
      val s = sub(1, Some("company"), utr = Some("1234567890"))
      VerificationBatchReadiness.isSubcontractorReady(s, None) mustBe false
    }

    "Company: fails when utr is absent" in {
      val s = sub(1, Some("company"), tradingName = Some("Acme Ltd"))
      VerificationBatchReadiness.isSubcontractorReady(s, None) mustBe false
    }

    // ─── Trust ──────────────────────────────────────────────────────────────

    "Trust: passes when tradingName and utr are present" in {
      val s = sub(1, Some("trust"), utr = Some("1234567890"), tradingName = Some("Smith Family Trust"))
      VerificationBatchReadiness.isSubcontractorReady(s, None) mustBe true
    }

    "Trust: fails when tradingName is absent" in {
      val s = sub(1, Some("trust"), utr = Some("1234567890"))
      VerificationBatchReadiness.isSubcontractorReady(s, None) mustBe false
    }

    "Trust: fails when utr is absent" in {
      val s = sub(1, Some("trust"), tradingName = Some("Smith Family Trust"))
      VerificationBatchReadiness.isSubcontractorReady(s, None) mustBe false
    }

    // ─── Partnership ────────────────────────────────────────────────────────

    "Partnership: passes when utr, partnershipTradingName and partnerUtr are present" in {
      val s = sub(
        1,
        Some("partnership"),
        utr = Some("1234567890"),
        partnershipTradingName = Some("Smith & Jones"),
        partnerUtr = Some("9876543210")
      )
      VerificationBatchReadiness.isSubcontractorReady(s, None) mustBe true
    }

    "Partnership: passes when utr, partnershipTradingName and nino are present" in {
      val s = sub(
        1,
        Some("partnership"),
        utr = Some("1234567890"),
        partnershipTradingName = Some("Smith & Jones"),
        nino = Some("AB123456C")
      )
      VerificationBatchReadiness.isSubcontractorReady(s, None) mustBe true
    }

    "Partnership: passes when utr, partnershipTradingName and crn are present" in {
      val s = sub(
        1,
        Some("partnership"),
        utr = Some("1234567890"),
        partnershipTradingName = Some("Smith & Jones"),
        crn = Some("12345678")
      )
      VerificationBatchReadiness.isSubcontractorReady(s, None) mustBe true
    }

    "Partnership: fails when utr is absent" in {
      val s =
        sub(1, Some("partnership"), partnershipTradingName = Some("Smith & Jones"), partnerUtr = Some("9876543210"))
      VerificationBatchReadiness.isSubcontractorReady(s, None) mustBe false
    }

    "Partnership: fails when partnershipTradingName is absent" in {
      val s = sub(1, Some("partnership"), utr = Some("1234567890"), partnerUtr = Some("9876543210"))
      VerificationBatchReadiness.isSubcontractorReady(s, None) mustBe false
    }

    "Partnership: fails when no partner identifier is present" in {
      val s = sub(1, Some("partnership"), utr = Some("1234567890"), partnershipTradingName = Some("Smith & Jones"))
      VerificationBatchReadiness.isSubcontractorReady(s, None) mustBe false
    }

    // ─── Blank / whitespace fields ────────────────────────────────────────────

    "fails when a required field is present but blank" in {
      val s = sub(1, Some("company"), utr = Some("1234567890"), tradingName = Some(""))
      VerificationBatchReadiness.isSubcontractorReady(s, None) mustBe false
    }

    "fails when a required field is whitespace only" in {
      val s = sub(1, Some("company"), utr = Some("   "), tradingName = Some("Acme Ltd"))
      VerificationBatchReadiness.isSubcontractorReady(s, None) mustBe false
    }

    // ─── Unknown type ────────────────────────────────────────────────────────

    "fails when subcontractorType is absent" in {
      val s = sub(1, None, utr = Some("1234567890"), tradingName = Some("Acme"))
      VerificationBatchReadiness.isSubcontractorReady(s, None) mustBe false
    }

    "fails when subcontractorType is an unrecognised value" in {
      val s = sub(1, Some("unknown"), utr = Some("1234567890"), tradingName = Some("Acme"))
      VerificationBatchReadiness.isSubcontractorReady(s, None) mustBe false
    }
  }

  private val readyIndividual = sub(1, Some("soletrader"), utr = Some("1234567890"), tradingName = Some("Acme"))
  private val readyCompany    = sub(2, Some("company"), utr = Some("0987654321"), tradingName = Some("Acme Ltd"))
  private val notReadySub     = sub(3, Some("company"), tradingName = Some("Missing UTR Ltd"))

  "VerificationBatchReadiness.isBatchReady" - {
    "returns true when a selected subcontractor has proceed = Y even though it does not meet minimum requirements" in {

      val result =
        VerificationBatchReadiness.isBatchReady(
          Set("3"),
          batchResponse(
            Seq(notReadySub),
            Seq(verification(3, Some("Y")))
          )
        )

      result mustBe true
    }
    "returns true when all selected subcontractors are ready" in {
      VerificationBatchReadiness.isBatchReady(
        Set("1", "2"),
        batchResponse(
          Seq(readyIndividual, readyCompany)
        )
      ) mustBe true
    }

    "returns false when any selected subcontractor is not ready" in {
      VerificationBatchReadiness.isBatchReady(
        Set("1", "3"),
        batchResponse(Seq(readyIndividual, notReadySub))
      ) mustBe false
    }

    "returns false when a selected ID has no matching subcontractor" in {
      VerificationBatchReadiness.isBatchReady(
        Set("1", "99"),
        batchResponse(Seq(readyIndividual))
      ) mustBe false
    }

    "returns true when only one subcontractor is selected and it is ready" in {
      VerificationBatchReadiness.isBatchReady(
        Set("1"),
        batchResponse(Seq(readyIndividual, readyCompany))
      ) mustBe true
    }

    "returns false when selectedIds is empty" in {
      VerificationBatchReadiness.isBatchReady(Set.empty, batchResponse(Seq(readyIndividual))) mustBe false
    }
  }
}
