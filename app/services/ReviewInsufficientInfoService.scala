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

import connectors.ConstructionIndustrySchemeConnector
import models.Subcontractor
import models.requests.ProceedInsufficientVerificationRequest
import models.response.GetNewestVerificationBatchResponse
import models.verify.VerificationBatchReadiness
import play.api.Logging
import play.api.i18n.Messages
import uk.gov.hmrc.http.HeaderCarrier
import viewmodels.verify.*

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ReviewInsufficientInfoService @Inject() (
  cisConnector: ConstructionIndustrySchemeConnector
)(implicit ec: ExecutionContext)
    extends Logging {

  // TODO: replace with real destinations once Edit / Proceed / Remove / view-details actions are built.
  private val dummyUrl = "#"

  def buildViewModel(
    batch: GetNewestVerificationBatchResponse
  )(implicit messages: Messages): ReviewInsufficientInfoViewModel = {
    val (readySubs, missingSubs) =
      batch.subcontractors.partition(VerificationBatchReadiness.isSubcontractorReady)

    ReviewInsufficientInfoViewModel(
      missing = missingSubs.map(toMissingRow),
      ready = readySubs.map(toReadyRow)
    )
  }

  def proceedInsufficientVerification(cisId: String, subcontractorId: Long, batch: GetNewestVerificationBatchResponse)(
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

  private def toMissingRow(sub: Subcontractor)(implicit messages: Messages): MissingSubcontractorRow = {
    val name = sub.displayName()
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

  private def toReadyRow(sub: Subcontractor)(implicit messages: Messages): ReadySubcontractorRow = {
    val name = sub.displayName()
    ReadySubcontractorRow(
      name = name,
      nameLink = LinkViewModel(dummyUrl, name),
      utr = utrDisplay(sub)
    )
  }

  private def utrDisplay(sub: Subcontractor)(implicit messages: Messages): String =
    sub.utr
      .map(_.trim)
      .filter(_.nonEmpty)
      .getOrElse(messages("verify.reviewInsufficientInfo.utr.noneProvided"))
}
