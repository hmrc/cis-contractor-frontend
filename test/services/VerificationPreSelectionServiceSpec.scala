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
import models.*
import models.response.{GetCurrentVerificationBatchResponse, GetNewestVerificationBatchResponse}
import pages.verify.{CurrentVerificationBatchResponsePage, NewestVerificationBatchResponsePage}

class VerificationPreSelectionServiceSpec extends SpecBase {

  private val service = new VerificationPreSelectionService

  private def subcontractor(id: Long, resourceRef: Option[Long]): Subcontractor =
    Subcontractor(
      subcontractorId = id,
      firstName = None,
      secondName = None,
      surname = None,
      tradingName = Some(s"Subcontractor $id"),
      partnershipTradingName = None,
      verified = None,
      verificationNumber = None,
      taxTreatment = None,
      verificationDate = None,
      lastMonthlyReturnDate = None,
      createDate = None,
      subcontractorType = Some("company"),
      subbieResourceRef = resourceRef,
      utr = None,
      partnerUtr = None,
      crn = None,
      nino = None
    )

  private val displayedSubcontractors =
    Seq(
      subcontractor(1L, Some(1001L)),
      subcontractor(2L, Some(1002L)),
      subcontractor(3L, None)
    )

  private def newestBatch(status: Option[String]): GetNewestVerificationBatchResponse =
    GetNewestVerificationBatchResponse(
      scheme = None,
      subcontractors = displayedSubcontractors,
      verificationBatch = Some(
        VerificationBatch(verificationBatchId = 1L, status = status, verificationNumber = None)
      ),
      verifications = Nil,
      submission = None,
      monthlyReturn = None,
      monthlyReturnSubmission = None
    )

  private val currentBatch =
    GetCurrentVerificationBatchResponse(
      subcontractors = Nil,
      verificationBatch = Some(
        VerificationBatchCurrentVerification(verificationBatchId = 1L, verifBatchResourceRef = None)
      ),
      verifications = Seq(
        VerificationCurrentVerification(
          verificationId = 1L,
          verificationBatchId = Some(1L),
          subcontractorId = Some(1L),
          verificationResourceRef = Some(1001L),
          subcontractorName = Some("Subcontractor 1"),
          verificationNumber = None,
          taxTreatment = None,
          actionIndicator = None,
          proceed = None,
          matched = None
        )
      )
    )

  "VerificationPreSelectionService.preSelectedSubcontractorIds" - {

    "must select only subcontractors whose resource reference matches a current verification when batch is STARTED" in {
      val ua =
        emptyUserAnswers
          .set(NewestVerificationBatchResponsePage, newestBatch(Some("STARTED")))
          .success
          .value
          .set(CurrentVerificationBatchResponsePage, currentBatch)
          .success
          .value

      service.preSelectedSubcontractorIds(displayedSubcontractors, ua) mustBe Set("1")
    }

    "must select only subcontractors whose resource reference matches a current verification when batch is VALIDATED" in {
      val ua =
        emptyUserAnswers
          .set(NewestVerificationBatchResponsePage, newestBatch(Some("VALIDATED")))
          .success
          .value
          .set(CurrentVerificationBatchResponsePage, currentBatch)
          .success
          .value

      service.preSelectedSubcontractorIds(displayedSubcontractors, ua) mustBe Set("1")
    }

    "must select all displayed subcontractors when batch status is not STARTED or VALIDATED" in {
      val ua =
        emptyUserAnswers
          .set(NewestVerificationBatchResponsePage, newestBatch(Some("ACCEPTED")))
          .success
          .value
          .set(CurrentVerificationBatchResponsePage, currentBatch)
          .success
          .value

      service.preSelectedSubcontractorIds(displayedSubcontractors, ua) mustBe Set("1", "2", "3")
    }

    "must select all displayed subcontractors when batch status is missing" in {
      val ua =
        emptyUserAnswers
          .set(NewestVerificationBatchResponsePage, newestBatch(None))
          .success
          .value
          .set(CurrentVerificationBatchResponsePage, currentBatch)
          .success
          .value

      service.preSelectedSubcontractorIds(displayedSubcontractors, ua) mustBe Set("1", "2", "3")
    }
  }
}
