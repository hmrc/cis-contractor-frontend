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

import models.{SubcontractorCurrentVerification, TypeOfSubcontractor, VerificationCurrentVerification}
import models.TypeOfSubcontractor.*
import models.response.GetCurrentVerificationBatchResponse

object VerificationBatchReadiness {

  def isBatchReady(
    selectedIds: Set[String],
    batchResponse: GetCurrentVerificationBatchResponse
  ): Boolean = {

    val allSubcontractors = batchResponse.subcontractors
    val verifications     = batchResponse.verifications

    selectedIds.nonEmpty && {
      val selectedSubs =
        selectedIds.flatMap(id => allSubcontractors.find(_.subcontractorId.toString == id))

      selectedSubs.size == selectedIds.size &&
      selectedSubs.forall { sub =>
        val verification =
          verifications.find(_.subcontractorId.contains(sub.subcontractorId))

        isSubcontractorReady(sub, verification)
      }
    }
  }

  def isSubcontractorReady(
    sub: SubcontractorCurrentVerification,
    verification: Option[VerificationCurrentVerification]
  ): Boolean =
    hasProceeded(verification) ||
      (sub.subcontractorType.flatMap(TypeOfSubcontractor.enumerable.withName) match {
        case Some(Individualorsoletrader) => isIndividualReady(sub)
        case Some(Limitedcompany)         => isCompanyReady(sub)
        case Some(Trust)                  => isTrustReady(sub)
        case Some(Partnership)            => isPartnershipReady(sub)
        case _                            => false
      })

  private def nonBlank(opt: Option[String]): Boolean = opt.exists(_.trim.nonEmpty)

  private def hasProceeded(
    verification: Option[VerificationCurrentVerification]
  ): Boolean =
    verification.exists(_.proceed.contains("Y"))

  private def isIndividualReady(sub: SubcontractorCurrentVerification): Boolean = {
    val hasName =
      nonBlank(sub.tradingName) || nonBlank(sub.surname)

    hasName && nonBlank(sub.utr)
  }

  private def isCompanyReady(sub: SubcontractorCurrentVerification): Boolean =
    nonBlank(sub.tradingName) && nonBlank(sub.utr)

  private def isTrustReady(sub: SubcontractorCurrentVerification): Boolean =
    nonBlank(sub.tradingName) && nonBlank(sub.utr)

  private def isPartnershipReady(sub: SubcontractorCurrentVerification): Boolean = {
    val hasPartnerIdentifier = nonBlank(sub.partnerUtr) || nonBlank(sub.nino) || nonBlank(sub.crn)

    hasPartnerIdentifier && nonBlank(sub.utr) && nonBlank(sub.partnershipTradingName)
  }
}
