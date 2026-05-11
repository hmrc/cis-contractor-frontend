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
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import org.scalatest.matchers.should.Matchers.shouldBe
import pages.verify.{EmailAddressPage, SelectSubcontractorPage}
import queries.CisIdQuery

import java.time.LocalDateTime

class VerificationRequestSubmittedViewModelSpec extends SpecBase {

  private val now   = LocalDateTime.of(2026, 4, 27, 10, 30)
  private val cisId = "12345"

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
  }

  "VerificationRequestSubmittedViewModel.fromUserAnswers" - {

    "must map subcontractorsToVerify from SelectSubcontractorPage" in {

      val subcontractors =
        Set(
          SubcontractorViewModel(id = "ID1", name = "Brody, Martin"),
          SubcontractorViewModel(id = "ID2", name = "Hooper And Associates")
        )

      val userAnswers =
        UserAnswers("id")
          .set(SelectSubcontractorPage, subcontractors)
          .success
          .value
          .set(CisIdQuery, cisId)
          .success
          .value

      val vm =
        VerificationRequestSubmittedViewModel.fromUserAnswers(userAnswers, applicationConfig)

      vm.subcontractorsToVerify shouldBe
        Seq("Brody, Martin", "Hooper And Associates")

      vm.showVerify shouldBe true
    }

    "must map confirmationEmail from EmailAddressPage when present" in {

      val userAnswers =
        UserAnswers("id")
          .set(EmailAddressPage, "test@testmail.com")
          .success
          .value
          .set(CisIdQuery, cisId)
          .success
          .value

      val vm =
        VerificationRequestSubmittedViewModel.fromUserAnswers(userAnswers, applicationConfig)

      vm.confirmationEmail        shouldBe Some("test@testmail.com")
      vm.showEmail                shouldBe true
      vm.subcontractorsToVerify   shouldBe Seq.empty
      vm.showVerify               shouldBe false
      vm.subcontractorsToReverify shouldBe Seq.empty
      vm.showReverify             shouldBe false
    }

    "must return empty values when pages are absent" in {

      val vm =
        VerificationRequestSubmittedViewModel.fromUserAnswers(
          UserAnswers("id").set(CisIdQuery, cisId).success.value,
          applicationConfig
        )

      vm.subcontractorsToVerify   shouldBe Seq.empty
      vm.subcontractorsToReverify shouldBe Seq.empty
      vm.confirmationEmail        shouldBe None
      vm.showVerify               shouldBe false
      vm.showReverify             shouldBe false
      vm.showEmail                shouldBe false
    }
  }
}
