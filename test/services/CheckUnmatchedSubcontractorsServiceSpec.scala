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

package services

import base.SpecBase
import models.{Subcontractor, Verification}

class CheckUnmatchedSubcontractorsServiceSpec extends SpecBase {

  private val resourceRef = 1001L

  private def verification(
    verificationNumber: Option[String],
    actionIndicator: Option[String],
    matched: Option[String],
    verificationResourceRef: Option[Long] = Some(resourceRef)
  ): Verification =
    Verification(
      verificationId = 1L,
      matched = matched,
      verificationNumber = verificationNumber,
      taxTreatment = None,
      verificationBatchId = None,
      subcontractorId = None,
      actionIndicator = actionIndicator,
      verificationResourceRef = verificationResourceRef
    )

  private def subcontractor(
    subbieResourceRef: Option[Long] = Some(resourceRef)
  ): Subcontractor =
    Subcontractor(
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
      subbieResourceRef = subbieResourceRef,
      utr = None,
      partnerUtr = None,
      crn = None,
      nino = None
    )

  private def check(
    verification: Verification,
    subcontractors: Seq[Subcontractor] = Seq(subcontractor())
  ): Boolean =
    CheckUnmatchedSubcontractorsService
      .hasAssociatedUnmatchedVerification(
        Seq(verification),
        subcontractors
      )

  "CheckUnmatchedSubcontractorsService.hasAssociatedUnmatchedVerification" - {

    "must return true when the verification number does not exist" in {
      check(
        verification(
          verificationNumber = None,
          actionIndicator = None,
          matched = Some("Y")
        )
      ) mustBe true
    }

    "must treat a blank verification number as not existing" in {
      check(
        verification(
          verificationNumber = Some("   "),
          actionIndicator = Some("MATCH"),
          matched = Some("Y")
        )
      ) mustBe true
    }

    "must return true when the action indicator is edit regardless of matched status" in {
      check(
        verification(
          verificationNumber = Some("V0000000001"),
          actionIndicator = Some("EDIT"),
          matched = Some("Y")
        )
      ) mustBe true
    }

    "must return true when the action indicator is match and matched is not Y" in {
      check(
        verification(
          verificationNumber = Some("V0000000001"),
          actionIndicator = Some("MATCH"),
          matched = Some("N")
        )
      ) mustBe true
    }

    "must return true when the action indicator is verify and matched is not Y" in {
      check(
        verification(
          verificationNumber = Some("V0000000001"),
          actionIndicator = Some(" verify "),
          matched = Some("N")
        )
      ) mustBe true
    }

    "must treat a missing matched value as not Y" in {
      check(
        verification(
          verificationNumber = Some("V0000000001"),
          actionIndicator = Some("MATCH"),
          matched = None
        )
      ) mustBe true
    }

    "must return false when the verification is verified" in {
      check(
        verification(
          verificationNumber = Some("V0000000001"),
          actionIndicator = Some("VERIFY"),
          matched = Some("Y")
        )
      ) mustBe false
    }

    "must return false when an unmatched verification has no associated subcontractor" in {
      check(
        verification(
          verificationNumber = None,
          actionIndicator = None,
          matched = None
        ),
        subcontractors = Seq(subcontractor(Some(2001L)))
      ) mustBe false
    }

    "must return false when an unmatched verification has no verification resource reference" in {
      check(
        verification(
          verificationNumber = None,
          actionIndicator = None,
          matched = None,
          verificationResourceRef = None
        )
      ) mustBe false
    }

    "must return false when the action indicator does not meet the unmatched criteria" in {
      check(
        verification(
          verificationNumber = Some("V0000000001"),
          actionIndicator = Some("UNKNOWN"),
          matched = Some("N")
        )
      ) mustBe false
    }

    "must return true when at least one verification is unmatched and associated" in {
      val verified = verification(
        verificationNumber = Some("V0000000001"),
        actionIndicator = Some("MATCH"),
        matched = Some("Y"),
        verificationResourceRef = Some(2001L)
      )

      val unmatched = verification(
        verificationNumber = Some("V0000000002"),
        actionIndicator = Some("VERIFY"),
        matched = Some("N"),
        verificationResourceRef = Some(resourceRef)
      )

      CheckUnmatchedSubcontractorsService
        .hasAssociatedUnmatchedVerification(
          Seq(verified, unmatched),
          Seq(subcontractor())
        ) mustBe true
    }

    "must return false when no verifications or subcontractors exist" in {
      CheckUnmatchedSubcontractorsService
        .hasAssociatedUnmatchedVerification(
          Seq.empty,
          Seq.empty
        ) mustBe false
    }
  }
}
