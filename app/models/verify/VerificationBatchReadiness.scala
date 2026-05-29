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

import models.{Subcontractor, TypeOfSubcontractor}
import models.TypeOfSubcontractor.*

object VerificationBatchReadiness {

  // TODO(DTR-4685): ID matching uses subcontractorId — confirm this remains correct once SubcontractorSource is replaced with real backend data
  def isBatchReady(selectedIds: Set[String], allSubcontractors: Seq[Subcontractor]): Boolean =
    selectedIds.nonEmpty && {
      val selectedSubs = selectedIds.flatMap(id => allSubcontractors.find(_.subcontractorId.toString == id))
      selectedSubs.size == selectedIds.size && selectedSubs.forall(isSubcontractorReady)
    }

  def isSubcontractorReady(sub: Subcontractor): Boolean =
    sub.subcontractorType.flatMap(TypeOfSubcontractor.enumerable.withName) match {
      case Some(Individualorsoletrader) => isIndividualReady(sub)
      case Some(Limitedcompany)         => isCompanyReady(sub)
      case Some(Trust)                  => isTrustReady(sub)
      case Some(Partnership)            => isPartnershipReady(sub)
      case _                            => false
    }

  private def nonBlank(opt: Option[String]): Boolean = opt.exists(_.trim.nonEmpty)

  private def isIndividualReady(sub: Subcontractor): Boolean = {
    val hasName =
      nonBlank(sub.tradingName) || (
        nonBlank(sub.firstName) && nonBlank(sub.surname)
      )

    hasName && nonBlank(sub.utr)
  }

  private def isCompanyReady(sub: Subcontractor): Boolean =
    nonBlank(sub.tradingName) && nonBlank(sub.utr)

  private def isTrustReady(sub: Subcontractor): Boolean =
    nonBlank(sub.tradingName) && nonBlank(sub.utr)

  private def isPartnershipReady(sub: Subcontractor): Boolean = {
    val hasPartnerIdentifier = nonBlank(sub.partnerUtr) || nonBlank(sub.nino) || nonBlank(sub.crn)

    hasPartnerIdentifier && nonBlank(sub.utr) && nonBlank(sub.partnershipTradingName)
  }
}
