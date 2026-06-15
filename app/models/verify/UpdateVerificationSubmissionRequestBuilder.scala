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

import models.requests.UpdateVerificationSubmissionRequest
import models.response.{ChrisPollResponse, ChrisSubmissionResponse}
import models.UserAnswers
import pages.verify.{CurrentVerificationBatchResponsePage, EmailAddressPage}
import queries.CisIdQuery

import java.time.LocalDateTime
import scala.util.Try

object UpdateVerificationSubmissionRequestBuilder {

  final private case class BuildParams(
    status: String,
    hmrcMarkGenerated: String,
    hmrcMarkGgis: Option[String],
    acceptedTime: Option[String],
    agentReference: Option[String],
    submissionRequestDate: Option[LocalDateTime],
    govTalkErrorCode: Option[String],
    govTalkErrorType: Option[String],
    govTalkErrorMessage: Option[String]
  )

  def fromChrisSubmissionResponse(
    ua: UserAnswers,
    response: ChrisSubmissionResponse,
    agentReference: Option[String],
    now: LocalDateTime
  ): Either[String, UpdateVerificationSubmissionRequest] = {
    val submissionRequestDate = response.gatewayTimestamp
      .flatMap(timestamp => Try(LocalDateTime.parse(timestamp)).toOption)
      .orElse(Some(now))

    build(
      ua,
      BuildParams(
        status = response.status,
        hmrcMarkGenerated = response.hmrcMarkGenerated,
        hmrcMarkGgis = None,
        acceptedTime = response.acceptedTime,
        agentReference = agentReference,
        submissionRequestDate = submissionRequestDate,
        govTalkErrorCode = response.error.flatMap(js => (js \ "number").asOpt[String]),
        govTalkErrorType = response.error.flatMap(js => (js \ "type").asOpt[String]),
        govTalkErrorMessage = response.error.flatMap(js => (js \ "text").asOpt[String])
      )
    )
  }

  def fromChrisPollResponse(
    ua: UserAnswers,
    details: VerificationSubmissionDetails,
    response: ChrisPollResponse,
    agentReference: Option[String],
    now: LocalDateTime
  ): Either[String, UpdateVerificationSubmissionRequest] =
    build(
      ua,
      BuildParams(
        status = response.status.value,
        hmrcMarkGenerated = details.hmrcMarkGenerated,
        hmrcMarkGgis = response.irMarkReceived.orElse(details.hmrcMarkGgis),
        acceptedTime = response.acceptedTime,
        agentReference = agentReference,
        submissionRequestDate = Some(details.submittedAt),
        govTalkErrorCode = response.error.flatMap(js => (js \ "number").asOpt[String]),
        govTalkErrorType = response.error.flatMap(js => (js \ "type").asOpt[String]),
        govTalkErrorMessage = response.error.flatMap(js => (js \ "text").asOpt[String])
      )
    )

  private def build(
    ua: UserAnswers,
    params: BuildParams
  ): Either[String, UpdateVerificationSubmissionRequest] =
    for {
      cisId                    <- ua.get(CisIdQuery).toRight("CisIdQuery not found in session data")
      currentVerificationBatch <-
        ua.get(CurrentVerificationBatchResponsePage).toRight("Current Verification Batch missing")
      verificationBatch        <- currentVerificationBatch.verificationBatch.toRight("Verification batch missing")
      batchResourceRef         <- verificationBatch.verifBatchResourceRef.toRight("Verification batch resource ref missing")
    } yield UpdateVerificationSubmissionRequest(
      instanceId = cisId,
      verificationBatchId = verificationBatch.verificationBatchId,
      verificationBatchResourceRef = batchResourceRef,
      submittableStatus = params.status,
      hmrcMarkGenerated = params.hmrcMarkGenerated,
      hmrcMarkGgis = params.hmrcMarkGgis,
      emailRecipient = ua.get(EmailAddressPage),
      submissionRequestDate = params.submissionRequestDate,
      acceptedTime = params.acceptedTime,
      agentId = params.agentReference,
      govtalkErrorCode = params.govTalkErrorCode,
      govtalkErrorType = params.govTalkErrorType,
      govtalkErrorMessage = params.govTalkErrorMessage
    )

}
