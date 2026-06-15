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

import base.SpecBase
import models.VerificationBatchCurrentVerification
import models.requests.UpdateVerificationSubmissionRequest
import models.response.*
import models.verify.SubmissionStatus.*
import pages.verify.{CurrentVerificationBatchResponsePage, EmailAddressPage}
import queries.CisIdQuery
import play.api.libs.json.Json

import java.time.LocalDateTime

class UpdateVerificationSubmissionRequestBuilderSpec extends SpecBase {

  private val now = LocalDateTime.parse("2026-06-15T03:30:52")

  private val currentBatch = GetCurrentVerificationBatchResponse(
    subcontractors = Seq.empty,
    verificationBatch = Some(
      VerificationBatchCurrentVerification(
        verificationBatchId = 1001L,
        verifBatchResourceRef = Some(2001L)
      )
    ),
    verifications = Seq.empty
  )

  private val ua =
    emptyUserAnswers
      .set(CisIdQuery, "1")
      .success
      .value
      .set(CurrentVerificationBatchResponsePage, currentBatch)
      .success
      .value
      .set(EmailAddressPage, "test@test.com")
      .success
      .value

  "UpdateVerificationSubmissionRequestBuilder" - {

    "must build from ChrisSubmissionResponse" in {
      val response = ChrisSubmissionResponse(
        submissionId = "13602",
        status = "ACCEPTED",
        hmrcMarkGenerated = "hmrc-mark",
        correlationId = Some("corr-id"),
        gatewayTimestamp = Some("2026-06-15T03:30:50"),
        acceptedTime = Some("2026-06-15T03:30:51"),
        error = Some(
          Json.obj(
            "number" -> "2005",
            "type"   -> "fatal",
            "text"   -> "Something failed"
          )
        )
      )

      val result =
        UpdateVerificationSubmissionRequestBuilder
          .fromChrisSubmissionResponse(
            ua = ua,
            response = response,
            agentReference = Some("123456"),
            now = now
          ) match {
          case Right(value) => value
          case Left(error)  => fail(s"Expected Right, but got Left($error)")
        }

      result mustBe UpdateVerificationSubmissionRequest(
        instanceId = "1",
        verificationBatchId = 1001L,
        verificationBatchResourceRef = 2001L,
        submittableStatus = "ACCEPTED",
        hmrcMarkGenerated = "hmrc-mark",
        hmrcMarkGgis = None,
        emailRecipient = Some("test@test.com"),
        submissionRequestDate = Some(LocalDateTime.parse("2026-06-15T03:30:50")),
        acceptedTime = Some("2026-06-15T03:30:51"),
        agentId = Some("123456"),
        govtalkErrorCode = Some("2005"),
        govtalkErrorType = Some("fatal"),
        govtalkErrorMessage = Some("Something failed")
      )
    }

    "must build from ChrisPollResponse" in {
      val details = VerificationSubmissionDetails(
        submissionId = "13602",
        status = "ACCEPTED",
        hmrcMarkGenerated = "hmrc-mark",
        hmrcMarkGgis = Some("old-ggis"),
        correlationId = Some("corr-id"),
        pollUrl = Some("http://localhost/poll"),
        pollIntervalSeconds = Some(5),
        submittedAt = LocalDateTime.parse("2026-06-15T03:30:50"),
        lastMessageDate = None,
        timedOut = false
      )

      val response = ChrisPollResponse(
        status = SUBMITTED,
        correlationId = "corr-id",
        pollUrl = None,
        pollInterval = None,
        error = None,
        irMarkReceived = Some("new-ggis"),
        lastMessageDate = None,
        acceptedTime = Some("2026-06-15T03:30:55")
      )

      val result =
        UpdateVerificationSubmissionRequestBuilder
          .fromChrisPollResponse(
            ua = ua,
            details = details,
            response = response,
            agentReference = None,
            now = now
          ) match {
          case Right(value) => value
          case Left(error)  => fail(s"Expected Right, but got Left($error)")
        }

      result.submittableStatus mustBe "SUBMITTED"
      result.hmrcMarkGenerated mustBe "hmrc-mark"
      result.hmrcMarkGgis mustBe Some("new-ggis")
      result.submissionRequestDate mustBe Some(LocalDateTime.parse("2026-06-15T03:30:50"))
      result.acceptedTime mustBe Some("2026-06-15T03:30:55")
    }

    "must fail when current batch is missing" in {
      val badUa =
        emptyUserAnswers
          .set(CisIdQuery, "1")
          .success
          .value

      UpdateVerificationSubmissionRequestBuilder
        .fromChrisSubmissionResponse(
          ua = badUa,
          response = ChrisSubmissionResponse(
            submissionId = "13602",
            status = "ACCEPTED",
            hmrcMarkGenerated = "hmrc-mark"
          ),
          agentReference = None,
          now = now
        ) match {
        case Left(error) =>
          error mustBe "Current Verification Batch missing"

        case Right(value) =>
          fail(s"Expected Left, but got Right($value)")
      }
    }
  }
}
