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

import base.SpecBase
import models.response.GetCurrentVerificationBatchResponse
import models.{SubcontractorCurrentVerification, VerificationCurrentVerification}

class UnmatchedBatchReadinessSpec extends SpecBase {

  private def sub(id: Long): SubcontractorCurrentVerification =
    SubcontractorCurrentVerification(
      subcontractorId = id,
      subbieResourceRef = None,
      firstName = None,
      secondName = None,
      surname = None,
      tradingName = None,
      utr = None,
      nino = None,
      crn = None,
      partnerUtr = None,
      partnershipTradingName = None,
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

  private def verification(
    subcontractorId: Long,
    actionIndicator: Option[String] = None,
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
      actionIndicator = actionIndicator,
      proceed = proceed,
      matched = None
    )

  private def batch(
    subs: Seq[SubcontractorCurrentVerification],
    verifications: Seq[VerificationCurrentVerification]
  ): GetCurrentVerificationBatchResponse =
    GetCurrentVerificationBatchResponse(
      subcontractors = subs,
      verificationBatch = None,
      verifications = verifications
    )

  "UnmatchedBatchReadiness.isVerificationReady" - {

    "must be true when the verification has been edited (actionIndicator = 'edit')" in {
      UnmatchedBatchReadiness.isVerificationReady(Some(verification(1L, actionIndicator = Some("edit")))) mustBe true
    }

    "must be true regardless of the casing of the action indicator" in {
      UnmatchedBatchReadiness.isVerificationReady(Some(verification(1L, actionIndicator = Some("EDIT")))) mustBe true
    }

    "must be true when the verification is marked to proceed (proceed = 'Y')" in {
      UnmatchedBatchReadiness.isVerificationReady(Some(verification(1L, proceed = Some("Y")))) mustBe true
    }

    "must be true regardless of the casing of the proceed flag" in {
      UnmatchedBatchReadiness.isVerificationReady(Some(verification(1L, proceed = Some("y")))) mustBe true
    }

    "must be false when neither edited nor marked to proceed" in {
      UnmatchedBatchReadiness.isVerificationReady(Some(verification(1L))) mustBe false
    }

    "must be false for another action indicator such as 'MATCH'" in {
      UnmatchedBatchReadiness.isVerificationReady(Some(verification(1L, actionIndicator = Some("MATCH")))) mustBe false
    }

    "must be false when the proceed flag is not 'Y'" in {
      UnmatchedBatchReadiness.isVerificationReady(Some(verification(1L, proceed = Some("N")))) mustBe false
    }

    "must be false when there is no verification" in {
      UnmatchedBatchReadiness.isVerificationReady(None) mustBe false
    }

    "must ignore blank values" in {
      UnmatchedBatchReadiness.isVerificationReady(
        Some(verification(1L, actionIndicator = Some("  "), proceed = Some("  ")))
      ) mustBe false
    }
  }

  "UnmatchedBatchReadiness.isBatchReady" - {

    "must be true when every subcontractor's verification is ready" in {
      val subs          = Seq(sub(1L), sub(2L))
      val verifications = Seq(
        verification(1L, actionIndicator = Some("edit")),
        verification(2L, proceed = Some("Y"))
      )

      UnmatchedBatchReadiness.isBatchReady(batch(subs, verifications)) mustBe true
    }

    "must be false when any subcontractor's verification is not ready" in {
      val subs          = Seq(sub(1L), sub(2L))
      val verifications = Seq(
        verification(1L, actionIndicator = Some("edit")),
        verification(2L)
      )

      UnmatchedBatchReadiness.isBatchReady(batch(subs, verifications)) mustBe false
    }

    "must be false when a subcontractor has no matching verification" in {
      val subs          = Seq(sub(1L), sub(2L))
      val verifications = Seq(verification(1L, proceed = Some("Y")))

      UnmatchedBatchReadiness.isBatchReady(batch(subs, verifications)) mustBe false
    }

    "must be false for an empty batch" in {
      UnmatchedBatchReadiness.isBatchReady(batch(Nil, Nil)) mustBe false
    }
  }
}
