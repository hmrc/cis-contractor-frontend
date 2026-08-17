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

case class VerificationResultsViewModel(
  name: String,
  verificationStatus: String,
  taxTreatment: String,
  verificationNumber: String,
  isUnmatched: Boolean
)

object VerificationResultsViewModel {

  private val ActionEdit   = "edit"
  private val ActionMatch  = "match"
  private val ActionVerify = "verify"

  def from(
    response: GetLastSubmittedVerificationBatchResponse
  )(implicit messages: Messages): Seq[VerificationResultsViewModel] =
    response.verifications.map { verification =>
      val verified = isVerified(verification)
      VerificationResultsViewModel(
        name = verification.subcontractorName.getOrElse(messages("verify.noName")),
        verificationStatus = verificationStatusFor(verified),
        taxTreatment = taxTreatmentFor(verification, verified),
        verificationNumber = verification.verificationNumber.getOrElse(""),
        isUnmatched = !verified
      )
    }

  def unmatchedSubcontractorIds(response: GetLastSubmittedVerificationBatchResponse): Set[Long] =
    response.verifications
      .collect {
        case verification if !isVerified(verification) =>
          verification.subcontractorId
      }
      .flatten
      .toSet

  private def isVerified(verification: VerificationLastVerification): Boolean = {
    val hasVerificationNumber =
      verification.verificationNumber.exists(_.trim.nonEmpty)

    if (!hasVerificationNumber) {
      false
    } else {
      val action = verification.actionIndicator.map(_.trim.toLowerCase)
      action match {
        case Some(ActionEdit)                                                          =>
          false
        case Some(ActionMatch) | Some(ActionVerify) if isMatched(verification.matched) =>
          true
        case _                                                                         =>
          false
      }
    }
  }

  private def isMatched(matched: Option[String]): Boolean =
    matched.exists { value =>
      val normalised = value.trim.toUpperCase
      normalised == "Y" || normalised == "MATCHED"
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
