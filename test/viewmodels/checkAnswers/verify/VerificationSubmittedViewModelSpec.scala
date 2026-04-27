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

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.time.LocalDateTime

class VerificationSubmittedViewModelSpec extends AnyFreeSpec with Matchers {

  private val now = LocalDateTime.of(2026, 4, 27, 10, 30)

  "VerificationSubmittedViewModel" - {

    "showEmail" - {

      "must return true when confirmationEmail is defined" in {

        val vm =
          VerificationSubmittedViewModel(
            referenceNumber = "REF123",
            submittedAt = now,
            subcontractorsToVerify = Seq("Sub A"),
            confirmationEmail = Some("test@test.com")
          )

        vm.showEmail shouldBe true
      }

      "must return false when confirmationEmail is not defined" in {

        val vm =
          VerificationSubmittedViewModel(
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
          VerificationSubmittedViewModel(
            referenceNumber = "REF123",
            submittedAt = now,
            subcontractorsToVerify = Seq("Sub A")
          )

        vm.showVerify shouldBe true
      }

      "must return false when subcontractorsToVerify is empty" in {

        val vm =
          VerificationSubmittedViewModel(
            referenceNumber = "REF123",
            submittedAt = now,
            subcontractorsToVerify = Seq.empty
          )

        vm.showVerify shouldBe false
      }
    }

    "showReverify" - {

      "must return true when subcontractorsToReverify is non-empty" in {

        val vm =
          VerificationSubmittedViewModel(
            referenceNumber = "REF123",
            submittedAt = now,
            subcontractorsToVerify = Seq("Sub A"),
            subcontractorsToReverify = Seq("Sub B")
          )

        vm.showReverify shouldBe true
      }

      "must return false when subcontractorsToReverify is empty" in {

        val vm =
          VerificationSubmittedViewModel(
            referenceNumber = "REF123",
            submittedAt = now,
            subcontractorsToVerify = Seq("Sub A"),
            subcontractorsToReverify = Seq.empty
          )

        vm.showReverify shouldBe false
      }
    }
  }
}
