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

import models.VerificationLastVerification
import models.response.GetLastSubmittedVerificationBatchResponse
import play.api.i18n.Messages
import services.CheckUnmatchedSubcontractorsService

case class VerificationResultsViewModel(
  name: String,
  verificationStatus: String,
  taxTreatment: String,
  verificationNumber: String,
  isUnmatched: Boolean
)

object VerificationResultsViewModel {

  def from(
    response: GetLastSubmittedVerificationBatchResponse
  )(implicit messages: Messages): Seq[VerificationResultsViewModel] =
    response.verifications.map { verification =>
      val unmatched = CheckUnmatchedSubcontractorsService.isUnmatched(verification)
      VerificationResultsViewModel(
        name = verification.subcontractorName.getOrElse(messages("verify.noName")),
        verificationStatus = verificationStatusFor(!unmatched),
        taxTreatment = taxTreatmentFor(verification, !unmatched),
        verificationNumber = verification.verificationNumber.getOrElse(""),
        isUnmatched = unmatched
      )
    }

  private def verificationStatusFor(verified: Boolean)(implicit messages: Messages): String =
    if (verified) {
      messages("verify.verificationResults.status.matched")
    } else {
      messages("verify.verificationResults.status.unmatched")
    }

  private def taxTreatmentFor(
    verification: VerificationLastVerification,
    verified: Boolean
  )(implicit messages: Messages): String =
    if (verified) {
      mapTaxTreatment(verification.taxTreatment)
    } else {
      messages("verify.verificationResults.taxTreatment.unmatched")
    }

  private def mapTaxTreatment(taxTreatment: Option[String])(implicit messages: Messages): String =
    taxTreatment
      .map(_.trim.toLowerCase)
      .collect {
        case "net" | "standardrate" | "standard rate"   =>
          messages("verify.verificationResults.taxTreatment.net")
        case "gross"                                    =>
          messages("verify.verificationResults.taxTreatment.gross")
        case "unmatched" | "higherrate" | "higher rate" =>
          messages("verify.verificationResults.taxTreatment.unmatched")
      }
      .getOrElse(messages("site.unknown"))
}
