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
import models.TypeOfSubcontractor
import models.TypeOfSubcontractor.*
import models.response.GetCurrentVerificationBatchResponse
import connectors.ConstructionIndustrySchemeConnector
import models.requests.ProceedInsufficientVerificationRequest
import models.verify.VerificationBatchReadiness
import play.api.Logging
import play.api.i18n.Messages
import uk.gov.hmrc.http.HeaderCarrier
import viewmodels.verify.*

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class ReviewInsufficientInfoService @Inject() (
  cisConnector: ConstructionIndustrySchemeConnector
) extends Logging {

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

  def proceedInsufficientVerification(cisId: String, subcontractorId: Long, batch: GetCurrentVerificationBatchResponse)(
    implicit hc: HeaderCarrier
  ): Future[Unit] = {
    val request =
      for {
        verificationBatchResourceRef <- batch.verificationBatch.flatMap(_.verifBatchResourceRef)
        verificationResourceRef      <- batch.verifications
                                          .find(_.subcontractorId.contains(subcontractorId))
                                          .flatMap(_.verificationResourceRef)
      } yield ProceedInsufficientVerificationRequest(
        instanceId = cisId,
        verificationBatchResourceRef = verificationBatchResourceRef,
        verificationResourceRef = verificationResourceRef,
        proceed = "Y"
      )
    request match {
      case Some(req) =>
        cisConnector.proceedInsufficientVerification(req)

      case None =>
        Future.failed(
          new RuntimeException(
            s"Unable to proceed insufficient verification. Missing resource refs for subcontractorId=$subcontractorId"
          )
        )
    }
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
      proceedLink = LinkViewModel(
        controllers.insufficient.routes.ProceedInsufficientSubcontractorNameYesNoController
          .onPageLoad(sub.subcontractorId)
          .url,
        name
      ),
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

    sub.subcontractorType.flatMap(TypeOfSubcontractor.enumerable.withName) match {
      case Some(Individualorsoletrader) => individualName.orElse(trading)
      case Some(Limitedcompany)         => trading
      case Some(Trust)                  => trading
      case Some(Partnership)            => partnershipTrading.orElse(trading)
      case _                            => partnershipTrading.orElse(trading).orElse(individualName)
    }
  }
}
