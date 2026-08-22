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

package viewmodels.verify

import base.SpecBase
import models.VerificationLastVerification
import models.response.GetLastSubmittedVerificationBatchResponse

class VerificationResultsViewModelSpec extends SpecBase {

  private def verification(
    matched: Option[String] = Some("Y"),
    verificationNumber: Option[String] = Some("V0000000001"),
    taxTreatment: Option[String] = Some("net"),
    subcontractorName: Option[String] = Some("John Smith"),
    actionIndicator: Option[String] = Some("verify")
  ): VerificationLastVerification =
    VerificationLastVerification(
      verificationId = 1L,
      verificationBatchId = Some(1L),
      verificationResourceRef = Some(10L),
      matched = matched,
      verificationNumber = verificationNumber,
      taxTreatment = taxTreatment,
      subcontractorName = subcontractorName,
      subcontractorId = Some(1L),
      actionIndicator = actionIndicator
    )

  private def response(verifications: VerificationLastVerification*): GetLastSubmittedVerificationBatchResponse =
    GetLastSubmittedVerificationBatchResponse(
      scheme = None,
      subcontractors = Nil,
      verifications = verifications,
      verificationBatch = None,
      submission = None
    )

  "VerificationResultsViewModel.from" - {

    "must map subcontractor name directly" in {
      val result = VerificationResultsViewModel.from(response(verification()))(messages(app)).head
      result.name mustBe "John Smith"
    }

    "must map missing name to no-name message" in {
      val result =
        VerificationResultsViewModel
          .from(response(verification(subcontractorName = None)))(messages(app))
          .head
      result.name mustBe messages(app)("verify.noName")
    }

    "AC3: must map Unmatched when verification number does not exist" in {
      val result = VerificationResultsViewModel
        .from(response(verification(verificationNumber = None, matched = Some("Y"), actionIndicator = Some("verify"))))(
          messages(app)
        )
        .head

      result.verificationStatus mustBe messages(app)("verify.verificationResults.status.unmatched")
      result.taxTreatment mustBe messages(app)("verify.verificationResults.taxTreatment.unmatched")
      result.isUnmatched mustBe true
      result.verificationNumber mustBe ""
    }

    "AC4: must map Unmatched when action indicator is edit" in {
      val result = VerificationResultsViewModel
        .from(
          response(
            verification(
              verificationNumber = Some("V1"),
              matched = Some("Y"),
              actionIndicator = Some("edit")
            )
          )
        )(messages(app))
        .head

      result.verificationStatus mustBe messages(app)("verify.verificationResults.status.unmatched")
      result.taxTreatment mustBe messages(app)("verify.verificationResults.taxTreatment.unmatched")
      result.isUnmatched mustBe true
    }

    "AC5: must map Verified when action is match or verify and matched is Y" in {
      val matchResult  = VerificationResultsViewModel
        .from(response(verification(actionIndicator = Some("match"), matched = Some("Y"), taxTreatment = Some("net"))))(
          messages(app)
        )
        .head
      val verifyResult = VerificationResultsViewModel
        .from(
          response(verification(actionIndicator = Some("VERIFY"), matched = Some("Y"), taxTreatment = Some("gross")))
        )(messages(app))
        .head

      matchResult.verificationStatus mustBe messages(app)("verify.verificationResults.status.matched")
      matchResult.taxTreatment mustBe messages(app)("verify.verificationResults.taxTreatment.net")
      matchResult.isUnmatched mustBe false

      verifyResult.verificationStatus mustBe messages(app)("verify.verificationResults.status.matched")
      verifyResult.taxTreatment mustBe messages(app)("verify.verificationResults.taxTreatment.gross")
      verifyResult.isUnmatched mustBe false
    }

    "must map Unmatched when action is match/verify but matched is not Y" in {
      val result = VerificationResultsViewModel
        .from(response(verification(actionIndicator = Some("match"), matched = Some("N"), taxTreatment = Some("net"))))(
          messages(app)
        )
        .head

      result.verificationStatus mustBe messages(app)("verify.verificationResults.status.unmatched")
      result.taxTreatment mustBe messages(app)("verify.verificationResults.taxTreatment.unmatched")
      result.isUnmatched mustBe true
    }

    "AC7: must map Higher rate tax treatment when not verified" in {
      val result = VerificationResultsViewModel
        .from(response(verification(verificationNumber = None, taxTreatment = Some("gross"))))(messages(app))
        .head

      result.taxTreatment mustBe messages(app)("verify.verificationResults.taxTreatment.unmatched")
    }

    "AC8: must map verification number directly and allow blank" in {
      val withNumber = VerificationResultsViewModel
        .from(response(verification(verificationNumber = Some("V0000000001"))))(messages(app))
        .head
      val blank      = VerificationResultsViewModel
        .from(response(verification(verificationNumber = None)))(messages(app))
        .head

      withNumber.verificationNumber mustBe "V0000000001"
      blank.verificationNumber mustBe ""
    }
  }
}
