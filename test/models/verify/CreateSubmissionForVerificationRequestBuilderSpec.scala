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
import models.*
import models.requests.*
import models.response.*
import models.verify.ContractorEmailConfirmationStored.DifferentEmail
import pages.*
import pages.verify.*
import play.api.i18n.Messages
import queries.CisIdQuery

class CreateSubmissionForVerificationRequestBuilderSpec extends SpecBase {

  implicit private val msgs: Messages = messages(app)

  "CreateSubmissionForVerificationRequestBuilder" - {

    "must build request from user answers" in {
      val currentBatch = GetCurrentVerificationBatchResponse(
        subcontractors = Seq(
          SubcontractorCurrentVerification(
            subcontractorId = 10L,
            subbieResourceRef = Some(4001L),
            firstName = Some("Test"),
            secondName = None,
            surname = Some("Subcontractor"),
            tradingName = None,
            utr = Some("1234567890"),
            nino = None,
            crn = None,
            partnerUtr = None,
            partnershipTradingName = None,
            subcontractorType = Some("Individual"),
            addressLine1 = None,
            addressLine2 = None,
            addressLine3 = None,
            addressLine4 = None,
            country = None,
            postcode = None,
            emailAddress = None,
            phoneNumber = None,
            mobilePhoneNumber = None,
            worksReferenceNumber = None,
            matched = None,
            autoVerified = None,
            verified = None,
            verificationNumber = None,
            taxTreatment = None,
            verificationDate = None,
            version = None,
            updatedTaxTreatment = None,
            lastMonthlyReturnDate = None,
            pendingVerifications = None
          )
        ),
        verificationBatch = Some(
          VerificationBatchCurrentVerification(
            verificationBatchId = 1001L,
            verifBatchResourceRef = Some(2001L)
          )
        ),
        verifications = Seq(
          VerificationCurrentVerification(
            verificationId = 3001L,
            verificationBatchId = Some(1001L),
            subcontractorId = Some(10L),
            verificationResourceRef = Some(4001L),
            subcontractorName = None,
            verificationNumber = None,
            taxTreatment = None,
            actionIndicator = None,
            proceed = Some("Y"),
            matched = None
          )
        )
      )

      val ua =
        emptyUserAnswers
          .set(CisIdQuery, "1")
          .success
          .value
          .set(CurrentVerificationBatchResponsePage, currentBatch)
          .success
          .value
          .set(ContractorEmailConfirmationStoredPage, DifferentEmail)
          .success
          .value
          .set(EmailAddressPage, "test@test.com")
          .success
          .value

      val result =
        CreateSubmissionForVerificationRequestBuilder.build(ua) match {
          case Right(value) => value
          case Left(error)  => fail(s"Expected Right, but got Left($error)")
        }

      result mustBe CreateSubmissionForVerificationRequest(
        instanceId = "1",
        verificationBatchId = 1001L,
        verificationBatchResourceRef = 2001L,
        emailRecipient = Some("test@test.com"),
        irMarkGenerated = None,
        verifications = Seq(
          VerificationToUpdate(
            subcontractorName = "Subcontractor, Test",
            verificationResourceRef = 4001L,
            proceedVerification = "Y"
          )
        ),
        agentId = None
      )
    }

    "must fail when current batch is missing" in {
      val ua =
        emptyUserAnswers
          .set(CisIdQuery, "1")
          .success
          .value
          .set(ContractorEmailConfirmationStoredPage, DifferentEmail)
          .success
          .value
          .set(EmailAddressPage, "test@test.com")
          .success
          .value

      CreateSubmissionForVerificationRequestBuilder.build(ua) match {
        case Left(error) =>
          error mustBe "CurrentVerificationBatchResponsePage not found"

        case Right(value) =>
          fail(s"Expected Left, but got Right($value)")
      }
    }
  }
}
