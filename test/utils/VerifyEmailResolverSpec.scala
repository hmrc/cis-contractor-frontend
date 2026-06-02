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

package utils

import base.SpecBase
import models.ContractorScheme
import models.response.GetNewestVerificationBatchResponse
import models.verify.ContractorEmailConfirmationStored
import pages.verify.{ContractorEmailConfirmationNotStoredPage, ContractorEmailConfirmationStoredPage, EmailAddressPage, NewestVerificationBatchResponsePage}

class VerifyEmailResolverSpec extends SpecBase {

  private def batchResponseWithEmail(email: String): GetNewestVerificationBatchResponse =
    GetNewestVerificationBatchResponse(
      scheme = Some(ContractorScheme(accountsOfficeReference = None, emailAddress = Some(email))),
      subcontractors = Seq.empty,
      verificationBatch = None,
      verifications = Seq.empty,
      submission = None,
      monthlyReturn = None,
      monthlyReturnSubmission = None
    )

  private val batchResponseNoEmail: GetNewestVerificationBatchResponse =
    GetNewestVerificationBatchResponse(
      scheme = Some(ContractorScheme(accountsOfficeReference = None, emailAddress = None)),
      subcontractors = Seq.empty,
      verificationBatch = None,
      verifications = Seq.empty,
      submission = None,
      monthlyReturn = None,
      monthlyReturnSubmission = None
    )

  "VerifyEmailResolver.resolvedEmail" - {

    "stored-email journey" - {

      "CurrentEmail with a stored email returns the scheme email" in {
        val ua = emptyUserAnswers
          .setOrException(ContractorEmailConfirmationStoredPage, ContractorEmailConfirmationStored.CurrentEmail)
          .setOrException(NewestVerificationBatchResponsePage, batchResponseWithEmail("scheme@example.com"))

        VerifyEmailResolver.resolvedEmail(ua) mustBe Some("scheme@example.com")
      }

      "CurrentEmail with no stored email returns None" in {
        val ua = emptyUserAnswers
          .setOrException(ContractorEmailConfirmationStoredPage, ContractorEmailConfirmationStored.CurrentEmail)
          .setOrException(NewestVerificationBatchResponsePage, batchResponseNoEmail)

        VerifyEmailResolver.resolvedEmail(ua) mustBe None
      }

      "CurrentEmail with no batch response returns None" in {
        val ua = emptyUserAnswers
          .setOrException(ContractorEmailConfirmationStoredPage, ContractorEmailConfirmationStored.CurrentEmail)

        VerifyEmailResolver.resolvedEmail(ua) mustBe None
      }

      "DifferentEmail returns the user-entered email address" in {
        val ua = emptyUserAnswers
          .setOrException(ContractorEmailConfirmationStoredPage, ContractorEmailConfirmationStored.DifferentEmail)
          .setOrException(EmailAddressPage, "override@example.com")

        VerifyEmailResolver.resolvedEmail(ua) mustBe Some("override@example.com")
      }

      "DoNotSend returns None" in {
        val ua = emptyUserAnswers
          .setOrException(ContractorEmailConfirmationStoredPage, ContractorEmailConfirmationStored.DoNotSend)
          .setOrException(EmailAddressPage, "ignored@example.com")

        VerifyEmailResolver.resolvedEmail(ua) mustBe None
      }
    }

    "no-email journey" - {

      "true (yes, send confirmation) returns the user-entered email address" in {
        val ua = emptyUserAnswers
          .setOrException(ContractorEmailConfirmationNotStoredPage, true)
          .setOrException(EmailAddressPage, "entered@example.com")

        VerifyEmailResolver.resolvedEmail(ua) mustBe Some("entered@example.com")
      }

      "false (do not send) returns None" in {
        val ua = emptyUserAnswers
          .setOrException(ContractorEmailConfirmationNotStoredPage, false)

        VerifyEmailResolver.resolvedEmail(ua) mustBe None
      }
    }

    "no answer in session returns None" in {
      VerifyEmailResolver.resolvedEmail(emptyUserAnswers) mustBe None
    }
  }
}
