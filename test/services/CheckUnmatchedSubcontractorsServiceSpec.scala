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
import models.verify.ReverificationDecision
import models.{Subcontractor, Verification, VerificationLastVerification}

class CheckUnmatchedSubcontractorsServiceSpec extends SpecBase {

  private val resourceRef = 1001L

  private def verification(
    verificationNumber: Option[String],
    actionIndicator: Option[String],
    matched: Option[String],
    verificationResourceRef: Option[Long] = Some(resourceRef),
    verificationId: Long = 1L
  ): Verification =
    Verification(
      verificationId = verificationId,
      matched = matched,
      verificationNumber = verificationNumber,
      taxTreatment = None,
      verificationBatchId = None,
      subcontractorId = None,
      actionIndicator = actionIndicator,
      verificationResourceRef = verificationResourceRef
    )

  private def subcontractor(
    subbieResourceRef: Option[Long] = Some(resourceRef),
    subcontractorId: Long = 1L
  ): Subcontractor =
    Subcontractor(
      subcontractorId = subcontractorId,
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

  "CheckUnmatchedSubcontractorsService.reverificationDecisions" - {

    "must retain eligible and ineligible decisions for every verification" in {
      val eligible = verification(
        verificationNumber = None,
        actionIndicator = None,
        matched = Some("Y"),
        verificationResourceRef = Some(1001L),
        verificationId = 1L
      )

      val alreadyMatched = verification(
        verificationNumber = Some("V0000000002"),
        actionIndicator = Some("VERIFY"),
        matched = Some("Y"),
        verificationResourceRef = Some(1002L),
        verificationId = 2L
      )

      val unsupportedAction = verification(
        verificationNumber = Some("V0000000003"),
        actionIndicator = Some("UNKNOWN"),
        matched = Some("N"),
        verificationResourceRef = Some(1003L),
        verificationId = 3L
      )

      val notAssociated = verification(
        verificationNumber = None,
        actionIndicator = None,
        matched = None,
        verificationResourceRef = Some(9999L),
        verificationId = 4L
      )

      val result =
        CheckUnmatchedSubcontractorsService.reverificationDecisions(
          verifications = Seq(
            eligible,
            alreadyMatched,
            unsupportedAction,
            notAssociated
          ),
          subcontractors = Seq(
            subcontractor(Some(1001L), subcontractorId = 11L),
            subcontractor(Some(1002L), subcontractorId = 12L),
            subcontractor(Some(1003L), subcontractorId = 13L)
          )
        )

      result mustBe Seq(
        ReverificationDecision(
          verificationId = 1L,
          subcontractorId = Some(11L),
          isUnmatched = true,
          considerForReverification = true
        ),
        ReverificationDecision(
          verificationId = 2L,
          subcontractorId = Some(12L),
          isUnmatched = false,
          considerForReverification = false
        ),
        ReverificationDecision(
          verificationId = 3L,
          subcontractorId = Some(13L),
          isUnmatched = false,
          considerForReverification = false
        ),
        ReverificationDecision(
          verificationId = 4L,
          subcontractorId = None,
          isUnmatched = true,
          considerForReverification = false
        )
      )
    }

    "must return an empty decision list when no verifications exist" in {
      CheckUnmatchedSubcontractorsService.reverificationDecisions(
        verifications = Seq.empty,
        subcontractors = Seq(subcontractor())
      ) mustBe Seq.empty
    }

    "must store the correct decision for every F3a acceptance criterion" in {
      val result =
        CheckUnmatchedSubcontractorsService.reverificationDecisions(
          verifications = Seq(
            // No verification number
            verification(
              verificationNumber = None,
              actionIndicator = None,
              matched = Some("Y"),
              verificationResourceRef = Some(1001L),
              verificationId = 1L
            ),

            // EDIT is eligible regardless of matched
            verification(
              verificationNumber = Some("V0000000002"),
              actionIndicator = Some("EDIT"),
              matched = Some("Y"),
              verificationResourceRef = Some(1002L),
              verificationId = 2L
            ),

            // MATCH and not matched
            verification(
              verificationNumber = Some("V0000000003"),
              actionIndicator = Some("MATCH"),
              matched = Some("N"),
              verificationResourceRef = Some(1003L),
              verificationId = 3L
            ),

            // VERIFY and not matched
            verification(
              verificationNumber = Some("V0000000004"),
              actionIndicator = Some("VERIFY"),
              matched = Some("N"),
              verificationResourceRef = Some(1004L),
              verificationId = 4L
            ),

            // MATCH and already matched
            verification(
              verificationNumber = Some("V0000000005"),
              actionIndicator = Some("MATCH"),
              matched = Some("Y"),
              verificationResourceRef = Some(1005L),
              verificationId = 5L
            ),

            // VERIFY and already matched
            verification(
              verificationNumber = Some("V0000000006"),
              actionIndicator = Some("VERIFY"),
              matched = Some("Y"),
              verificationResourceRef = Some(1006L),
              verificationId = 6L
            ),

            // Action does not support reverification
            verification(
              verificationNumber = Some("V0000000007"),
              actionIndicator = Some("UNKNOWN"),
              matched = Some("N"),
              verificationResourceRef = Some(1007L),
              verificationId = 7L
            )
          ),
          subcontractors = Seq(
            subcontractor(Some(1001L), subcontractorId = 11L),
            subcontractor(Some(1002L), subcontractorId = 12L),
            subcontractor(Some(1003L), subcontractorId = 13L),
            subcontractor(Some(1004L), subcontractorId = 14L),
            subcontractor(Some(1005L), subcontractorId = 15L),
            subcontractor(Some(1006L), subcontractorId = 16L),
            subcontractor(Some(1007L), subcontractorId = 17L)
          )
        )

      result mustBe Seq(
        ReverificationDecision(
          verificationId = 1L,
          subcontractorId = Some(11L),
          isUnmatched = true,
          considerForReverification = true
        ),
        ReverificationDecision(
          verificationId = 2L,
          subcontractorId = Some(12L),
          isUnmatched = true,
          considerForReverification = true
        ),
        ReverificationDecision(
          verificationId = 3L,
          subcontractorId = Some(13L),
          isUnmatched = true,
          considerForReverification = true
        ),
        ReverificationDecision(
          verificationId = 4L,
          subcontractorId = Some(14L),
          isUnmatched = true,
          considerForReverification = true
        ),
        ReverificationDecision(
          verificationId = 5L,
          subcontractorId = Some(15L),
          isUnmatched = false,
          considerForReverification = false
        ),
        ReverificationDecision(
          verificationId = 6L,
          subcontractorId = Some(16L),
          isUnmatched = false,
          considerForReverification = false
        ),
        ReverificationDecision(
          verificationId = 7L,
          subcontractorId = Some(17L),
          isUnmatched = false,
          considerForReverification = false
        )
      )
    }

    "must store unmatched but not consider for reverification when no associated subcontractor exists" in {
      val result =
        CheckUnmatchedSubcontractorsService.reverificationDecisions(
          verifications = Seq(
            verification(
              verificationNumber = None,
              actionIndicator = None,
              matched = None,
              verificationResourceRef = Some(2001L),
              verificationId = 1L
            )
          ),
          subcontractors = Seq(
            subcontractor(
              subbieResourceRef = Some(1001L),
              subcontractorId = 11L
            )
          )
        )

      result mustBe Seq(
        ReverificationDecision(
          verificationId = 1L,
          subcontractorId = None,
          isUnmatched = true,
          considerForReverification = false
        )
      )
    }

    "must store unmatched but not consider for reverification when verificationResourceRef is missing" in {
      val result =
        CheckUnmatchedSubcontractorsService.reverificationDecisions(
          verifications = Seq(
            verification(
              verificationNumber = None,
              actionIndicator = None,
              matched = None,
              verificationResourceRef = None,
              verificationId = 1L
            )
          ),
          subcontractors = Seq(
            subcontractor(
              subbieResourceRef = Some(1001L),
              subcontractorId = 11L
            )
          )
        )

      result mustBe Seq(
        ReverificationDecision(
          verificationId = 1L,
          subcontractorId = None,
          isUnmatched = true,
          considerForReverification = false
        )
      )
    }

    "must normalise case and whitespace when applying the rules" in {
      val result =
        CheckUnmatchedSubcontractorsService.reverificationDecisions(
          verifications = Seq(
            verification(
              verificationNumber = Some(" V0000000001 "),
              actionIndicator = Some(" verify "),
              matched = Some(" n "),
              verificationResourceRef = Some(1001L),
              verificationId = 1L
            )
          ),
          subcontractors = Seq(
            subcontractor(
              subbieResourceRef = Some(1001L),
              subcontractorId = 11L
            )
          )
        )

      result mustBe Seq(
        ReverificationDecision(
          verificationId = 1L,
          subcontractorId = Some(11L),
          isUnmatched = true,
          considerForReverification = true
        )
      )
    }
  }

  "CheckUnmatchedSubcontractorsService.reverificationDecisionsFromLastSubmitted" - {

    "must match last-submitted verifications to live subcontractors by resource ref" in {
      val result =
        CheckUnmatchedSubcontractorsService.reverificationDecisionsFromLastSubmitted(
          verifications = Seq(
            VerificationLastVerification(
              verificationId = 1L,
              verificationBatchId = Some(10L),
              verificationResourceRef = Some(1001L),
              matched = None,
              verificationNumber = None,
              taxTreatment = None,
              subcontractorName = Some("John Smith"),
              subcontractorId = Some(22L),
              actionIndicator = Some("verify")
            )
          ),
          liveSubcontractors = Seq(
            models.response.SubcontractorListItem(22L, Some(1001L))
          )
        )

      result mustBe Seq(
        ReverificationDecision(
          verificationId = 1L,
          subcontractorId = Some(22L),
          isUnmatched = true,
          considerForReverification = true
        )
      )
    }
  }
}
