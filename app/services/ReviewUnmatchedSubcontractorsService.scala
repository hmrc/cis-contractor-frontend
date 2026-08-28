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

import models.SubcontractorCurrentVerification
import models.VerificationCurrentVerification
import models.response.GetCurrentVerificationBatchResponse
import models.verify.UnmatchedBatchReadiness
import play.api.i18n.Messages
import viewmodels.verify.*

import javax.inject.{Inject, Singleton}

@Singleton
class ReviewUnmatchedSubcontractorsService @Inject() {

  // TODO: replace with real destinations once Edit / Proceed / Remove / view-details actions are built.
  private val dummyUrl = "#"

  private val noneProvidedKey = "verify.reviewUnmatched.noneProvided"

  def buildViewModel(
    batch: GetCurrentVerificationBatchResponse
  )(implicit messages: Messages): ReviewUnmatchedViewModel = {
    val batchSubs =
      batch.verifications.flatMap { verification =>
        batch.subcontractors
          .find(sub => verification.subcontractorId.contains(sub.subcontractorId))
          .map(sub => (sub, verification))
      }

    val (readySubs, unmatchedSubs) =
      batchSubs.partition { case (_, verification) =>
        UnmatchedBatchReadiness.isVerificationReady(Some(verification))
      }

    ReviewUnmatchedViewModel(
      unmatched = unmatchedSubs.map { case (sub, verification) => toUnmatchedRow(sub, Some(verification)) },
      ready = readySubs.map { case (sub, verification) => toReadyRow(sub, Some(verification)) }
    )
  }

  private def toUnmatchedRow(
    sub: SubcontractorCurrentVerification,
    verification: Option[VerificationCurrentVerification]
  )(implicit messages: Messages): MissingSubcontractorRow = {
    val name = resolveName(sub, verification)
    MissingSubcontractorRow(
      name = name,
      nameLink = LinkViewModel(dummyUrl, name),
      utr = SubcontractorDisplay.utrDisplay(sub, noneProvidedKey),
      editLink = LinkViewModel(dummyUrl, name),
      proceedLink = LinkViewModel(dummyUrl, name),
      removeLink = LinkViewModel(dummyUrl, name)
    )
  }

  private def toReadyRow(
    sub: SubcontractorCurrentVerification,
    verification: Option[VerificationCurrentVerification]
  )(implicit messages: Messages): ReadySubcontractorRow = {
    val name = resolveName(sub, verification)
    ReadySubcontractorRow(
      name = name,
      nameLink = LinkViewModel(dummyUrl, name),
      utr = SubcontractorDisplay.utrDisplay(sub, noneProvidedKey)
    )
  }

  // Name maps to VERIFICATION.SUBCONTRACTOR_NAME, falling back to the subcontractor's derived name.
  private def resolveName(
    sub: SubcontractorCurrentVerification,
    verification: Option[VerificationCurrentVerification]
  )(implicit messages: Messages): String =
    verification
      .flatMap(_.subcontractorName)
      .map(_.trim)
      .filter(_.nonEmpty)
      .getOrElse(SubcontractorDisplay.displayName(sub))
}
