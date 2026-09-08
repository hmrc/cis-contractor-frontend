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

import models.response.GetLastSubmittedVerificationBatchResponse
import models.verify.ReverificationDecision
import models.{Subcontractor, SubcontractorLastVerification, Verification, VerificationCurrentVerification, VerificationLastVerification}

object CheckUnmatchedSubcontractorsService {

  def reverificationDecisions(
    verifications: Seq[Verification],
    subcontractors: Seq[Subcontractor]
  ): Seq[ReverificationDecision] = {
    val subcontractorsByResourceRef =
      subcontractors.flatMap { subcontractor =>
        subcontractor.subbieResourceRef.map(_ -> subcontractor)
      }.toMap

    verifications.map { verification =>
      val associatedSubcontractor =
        verification.verificationResourceRef.flatMap(
          subcontractorsByResourceRef.get
        )

      val unmatched = isUnmatched(verification)

      ReverificationDecision(
        verificationId = verification.verificationId,
        subcontractorId = associatedSubcontractor.map(_.subcontractorId),
        isUnmatched = unmatched,
        considerForReverification = unmatched && associatedSubcontractor.isDefined
      )
    }
  }

  def reverificationDecisions(
    response: GetLastSubmittedVerificationBatchResponse
  ): Seq[ReverificationDecision] =
    reverificationDecisions(
      response.verifications.map(toVerification),
      response.subcontractors.map(toSubcontractor)
    )

  def isUnmatched(verification: VerificationLastVerification): Boolean =
    isUnmatched(toVerification(verification))

  def isUnmatched(verification: VerificationCurrentVerification): Boolean =
    isUnmatched(toVerification(verification))

  def isUnmatched(verification: Verification): Boolean = {
    val verificationNumberExists =
      normalise(verification.verificationNumber).isDefined

    val actionIndicator =
      normalise(verification.actionIndicator)

    val matched =
      normalise(verification.matched)

    !verificationNumberExists ||
    actionIndicator.contains("EDIT") ||
    (
      Set("MATCH", "VERIFY").exists(actionIndicator.contains) &&
        !matched.contains("Y")
    )
  }

  private def toVerification(verification: VerificationLastVerification): Verification =
    Verification(
      verificationId = verification.verificationId,
      matched = verification.matched,
      verificationNumber = verification.verificationNumber,
      taxTreatment = verification.taxTreatment,
      verificationBatchId = verification.verificationBatchId,
      subcontractorId = verification.subcontractorId,
      actionIndicator = verification.actionIndicator,
      verificationResourceRef = verification.verificationResourceRef
    )

  private def toVerification(verification: VerificationCurrentVerification): Verification =
    Verification(
      verificationId = verification.verificationId,
      matched = verification.matched,
      verificationNumber = verification.verificationNumber,
      taxTreatment = verification.taxTreatment,
      verificationBatchId = verification.verificationBatchId,
      subcontractorId = verification.subcontractorId,
      actionIndicator = verification.actionIndicator,
      verificationResourceRef = verification.verificationResourceRef
    )

  private def toSubcontractor(subcontractor: SubcontractorLastVerification): Subcontractor =
    Subcontractor(
      subcontractorId = subcontractor.subcontractorId,
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
      subcontractorType = subcontractor.subcontractorType,
      subbieResourceRef = subcontractor.subbieResourceRef,
      utr = subcontractor.utr,
      partnerUtr = None,
      crn = None,
      nino = None
    )

  private def normalise(value: Option[String]): Option[String] =
    value.map(_.trim.toUpperCase).filter(_.nonEmpty)
}
