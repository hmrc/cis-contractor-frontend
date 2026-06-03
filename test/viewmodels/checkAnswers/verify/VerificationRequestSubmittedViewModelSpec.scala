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

package viewmodels.checkAnswers.verify

import base.SpecBase
import models.UserAnswers
import models.SubcontractorViewModel
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import org.scalatest.matchers.should.Matchers._
import pages.verify.SelectSubcontractorPage
import models.{Submission, VerificationBatch}
import models.response.GetNewestVerificationBatchResponse
import pages.verify.NewestVerificationBatchResponsePage
import queries.CisIdQuery

import java.time.LocalDateTime

class VerificationRequestSubmittedViewModelSpec extends SpecBase {

  private val cisId: String = "123456789"

  private val now: LocalDateTime = LocalDateTime.now()

  private val verificationBatch =
    VerificationBatch(
      verificationBatchId = 1L,
      status = Some("Submitted"),
      verificationNumber = Some("VB00000001")
    )

  private val submission =
    Submission(
      submissionId = 1L,
      activeObjectId = Some(1L),
      submissionRequestDate = Some(now),
      status = Some("Submitted")
    )

  private val newestBatchResponse =
    GetNewestVerificationBatchResponse(
      scheme = None,
      subcontractors = Seq.empty,
      verificationBatch = Some(verificationBatch),
      verifications = Seq.empty,
      submission = Some(submission),
      monthlyReturn = None
    )

  "VerificationRequestSubmittedViewModel" - {

    "showEmail" - {

      "must return true when confirmationEmail is defined" in {

        val vm =
          VerificationRequestSubmittedViewModel(
            manageSubcontractorsUrl = s"${applicationConfig.manageSubcontractorsUrl}/$cisId",
            referenceNumber = "REF123",
            submittedAt = now,
            subcontractorsToVerify = Seq("Sub A"),
            confirmationEmail = Some("test@test.com")
          )

        vm.showEmail shouldBe true
      }

      "must return false when confirmationEmail is not defined" in {

        val vm =
          VerificationRequestSubmittedViewModel(
            manageSubcontractorsUrl = s"${applicationConfig.manageSubcontractorsUrl}/$cisId",
            referenceNumber = "REF123",
            submittedAt = now,
            subcontractorsToVerify = Seq("Sub A"),
            confirmationEmail = None
          )

        vm.showEmail shouldBe false
      }
    }

    "showVerify" - {

      "must return true when subcontractorsToVerify is non-empty" in {

        val vm =
          VerificationRequestSubmittedViewModel(
            manageSubcontractorsUrl = s"${applicationConfig.manageSubcontractorsUrl}/$cisId",
            referenceNumber = "REF123",
            submittedAt = now,
            subcontractorsToVerify = Seq("Sub A")
          )

        vm.showVerify shouldBe true
      }

      "must return false when subcontractorsToVerify is empty" in {

        val vm =
          VerificationRequestSubmittedViewModel(
            manageSubcontractorsUrl = s"${applicationConfig.manageSubcontractorsUrl}/$cisId",
            referenceNumber = "REF123",
            submittedAt = now
          )

        vm.showVerify shouldBe false
      }
    }

    "showReverify" - {

      "must return true when subcontractorsToReverify is non-empty" in {

        val vm =
          VerificationRequestSubmittedViewModel(
            manageSubcontractorsUrl = s"${applicationConfig.manageSubcontractorsUrl}/$cisId",
            referenceNumber = "REF123",
            submittedAt = now,
            subcontractorsToVerify = Seq("Sub A"),
            subcontractorsToReverify = Seq("Sub B")
          )

        vm.showReverify shouldBe true
      }

      "must return false when subcontractorsToReverify is empty" in {

        val vm =
          VerificationRequestSubmittedViewModel(
            manageSubcontractorsUrl = s"${applicationConfig.manageSubcontractorsUrl}/$cisId",
            referenceNumber = "REF123",
            submittedAt = now,
            subcontractorsToVerify = Seq("Sub A")
          )

        vm.showReverify shouldBe false
      }
    }

    "fromUserAnswers" - {

      "must map referenceNumber and submittedAt correctly" in {

        val userAnswers =
          UserAnswers("id")
            .set(CisIdQuery, cisId).success.value
            .set(NewestVerificationBatchResponsePage, newestBatchResponse).success.value

        val vm =
          VerificationRequestSubmittedViewModel.fromUserAnswers(userAnswers, applicationConfig)

        vm.referenceNumber shouldBe "VB00000001"
        vm.submittedAt shouldBe now
      }

      "must map subcontractors correctly" in {

        val subcontractors =
          Set(
            SubcontractorViewModel("ID1", "Brody, Martin"),
            SubcontractorViewModel("ID2", "Hooper And Associates")
          )

        val userAnswers =
          UserAnswers("id")
            .set(SelectSubcontractorPage, subcontractors).success.value
            .set(CisIdQuery, cisId).success.value
            .set(NewestVerificationBatchResponsePage, newestBatchResponse).success.value

        val vm =
          VerificationRequestSubmittedViewModel.fromUserAnswers(userAnswers, applicationConfig)

        vm.subcontractorsToVerify shouldBe Seq("Brody, Martin", "Hooper And Associates")
        vm.showVerify shouldBe true
      }

      "must throw when NewestVerificationBatchResponsePage is missing" in {

        val exception =
          intercept[IllegalStateException] {

            VerificationRequestSubmittedViewModel.fromUserAnswers(
              UserAnswers("id")
                .set(CisIdQuery, cisId)
                .success
                .value,
              applicationConfig
            )
          }

        exception.getMessage should include(
          "NewestVerificationBatchResponsePage missing"
        )
      }
    }
  }
}
