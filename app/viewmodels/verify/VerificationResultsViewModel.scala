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

import models.response.GetNewestVerificationBatchResponse
import models.{Subcontractor, Verification}
import play.api.i18n.Messages

case class VerificationResultsViewModel(
  name: String,
  verificationStatus: String,
  taxTreatment: String,
  verificationNumber: String,
  isUnmatched: Boolean
)

object VerificationResultsViewModel {

  def from(response: GetNewestVerificationBatchResponse)(implicit messages: Messages): Seq[VerificationResultsViewModel] = {
    val subcontractorsById = response.subcontractors.map(s => s.subcontractorId -> s).toMap

    response.verifications.flatMap { verification =>
      verification.subcontractorId.flatMap { subId =>
        subcontractorsById.get(subId).map { sub =>
          VerificationResultsViewModel(
            name = Subcontractor.resolveName(sub).getOrElse(messages("verify.noName")),
            verificationStatus = verificationStatusFor(verification),
            taxTreatment = taxTreatmentFor(verification),
            verificationNumber = verification.verificationNumber.getOrElse(messages("site.unknown")),
            isUnmatched = verification.matched.contains("unmatched")
          )
        }
      }
    }
  }

  private def verificationStatusFor(verification: Verification)(implicit messages: Messages): String =
    verification.matched match {
      case Some("matched")   => messages("verify.verificationResults.status.matched")
      case Some("unmatched") => messages("verify.verificationResults.status.unmatched")
      case _                 => messages("site.unknown")
    }

  private def taxTreatmentFor(verification: Verification)(implicit messages: Messages): String =
    verification.taxTreatment match {
      case Some("net")       => messages("verify.verificationResults.taxTreatment.net")
      case Some("gross")     => messages("verify.verificationResults.taxTreatment.gross")
      case Some("unmatched") => messages("verify.verificationResults.taxTreatment.unmatched")
      case _                 => messages("site.unknown")
    }
}
