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

import models.requests.{CreateSubmissionForVerificationRequest, VerificationToUpdate}
import pages.verify.CurrentVerificationBatchResponsePage
import queries.CisIdQuery
import utils.VerifyEmailResolver

object CreateSubmissionForVerificationRequestBuilder {

  def build(
    ua: models.UserAnswers
  ): Either[String, CreateSubmissionForVerificationRequest] =
    for {
      instanceId <- ua.get(CisIdQuery).toRight("CisIdQuery not found")
      current    <- ua.get(CurrentVerificationBatchResponsePage).toRight("CurrentVerificationBatchResponsePage not found")

      batchId  <- current.verificationBatch.map(_.verificationBatchId).toRight("verificationBatchId missing")
      batchRef <-
        current.verificationBatch.flatMap(_.verifBatchResourceRef).toRight("verificationBatchResourceRef missing")

      email <- VerifyEmailResolver.resolvedEmail(ua).toRight("No email resolved for submission")
    } yield {

      val verifications: Seq[VerificationToUpdate] =
        current.verifications.flatMap(_.verificationResourceRef).map { ref =>
          VerificationToUpdate(
            subcontractorName = "Unknown", // ??
            verificationResourceRef = ref,
            proceedVerification = "Y" // ??
          )
        }

      CreateSubmissionForVerificationRequest(
        instanceId = instanceId,
        verificationBatchId = batchId,
        verificationBatchResourceRef = batchRef,
        emailRecipient = email,
        irMarkGenerated = None,
        verifications = verifications,
        agentId = None
      )
    }
}
