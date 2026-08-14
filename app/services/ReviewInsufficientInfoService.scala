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
import models.response.GetCurrentVerificationBatchResponse
import models.verify.VerificationBatchReadiness
import play.api.i18n.Messages
import viewmodels.verify.*

import javax.inject.{Inject, Singleton}

@Singleton
class ReviewInsufficientInfoService @Inject() {

  // TODO: replace with real destinations once Edit / Proceed / Remove / view-details actions are built.
  private val dummyUrl = "#"

  def buildViewModel(
    batch: GetCurrentVerificationBatchResponse
  )(implicit messages: Messages): ReviewInsufficientInfoViewModel = {
    val batchSubs =
      batch.verifications.flatMap { verification =>
        batch.subcontractors
          .find(sub => verification.subcontractorId.contains(sub.subcontractorId))
          .map(sub => (sub, verification))
      }

    val (readySubs, missingSubs) =
      batchSubs.partition { case (sub, verification) =>
        VerificationBatchReadiness.isSubcontractorReady(sub, Some(verification))
      }

    ReviewInsufficientInfoViewModel(
      missing = missingSubs.map { case (sub, _) => toMissingRow(sub) },
      ready = readySubs.map { case (sub, _) => toReadyRow(sub) }
    )
  }

  private def toMissingRow(
    sub: SubcontractorCurrentVerification
  )(implicit messages: Messages): MissingSubcontractorRow = {
    val name = displayName(sub)
    MissingSubcontractorRow(
      name = name,
      nameLink = LinkViewModel(dummyUrl, name),
      utr = utrDisplay(sub),
      editLink = LinkViewModel(dummyUrl, name),
      proceedLink = LinkViewModel(dummyUrl, name),
      removeLink = LinkViewModel(dummyUrl, name)
    )
  }

  private def toReadyRow(sub: SubcontractorCurrentVerification)(implicit messages: Messages): ReadySubcontractorRow = {
    val name = displayName(sub)
    ReadySubcontractorRow(
      name = name,
      nameLink = LinkViewModel(dummyUrl, name),
      utr = utrDisplay(sub)
    )
  }

  private def utrDisplay(sub: SubcontractorCurrentVerification)(implicit messages: Messages): String =
    sub.utr
      .map(_.trim)
      .filter(_.nonEmpty)
      .getOrElse(messages("verify.reviewInsufficientInfo.noneProvided"))

  private def displayName(sub: SubcontractorCurrentVerification)(implicit messages: Messages): String =
    nameFor(sub).getOrElse(messages("verify.noName"))

  private def nameFor(sub: SubcontractorCurrentVerification): Option[String] = {
    val first              = sub.firstName.map(_.trim).filter(_.nonEmpty)
    val surname            = sub.surname.map(_.trim).filter(_.nonEmpty)
    val trading            = sub.tradingName.map(_.trim).filter(_.nonEmpty)
    val partnershipTrading = sub.partnershipTradingName.map(_.trim).filter(_.nonEmpty)

    val individualName = surname.map { s =>
      first.map(f => s"$s, $f").getOrElse(s)
    }

    partnershipTrading.orElse(trading).orElse(individualName)
  }
}
