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

package pages.verify

import models.UserAnswers
import pages.behaviours.PageBehaviours

class ContractorEmailConfirmationNotStoredPageSpec extends PageBehaviours {

  "ContractorEmailConfirmationNotStoredPage" - {

    beRetrievable[Boolean](ContractorEmailConfirmationNotStoredPage)
    beSettable[Boolean](ContractorEmailConfirmationNotStoredPage)
    beRemovable[Boolean](ContractorEmailConfirmationNotStoredPage)

    "cleanup" - {

      "must remove EmailAddressPage when answer is false (stale email must be cleared)" in {
        val answers =
          UserAnswers("id")
            .setOrException(EmailAddressPage, "new@test.com")
            .setOrException(ContractorEmailConfirmationNotStoredPage, true) // previous choice

        val result =
          ContractorEmailConfirmationNotStoredPage
            .cleanup(Some(false), answers)
            .success
            .value

        result.get(EmailAddressPage) mustBe None
      }

      "must keep EmailAddressPage when answer is true" in {
        val answers =
          UserAnswers("id")
            .setOrException(EmailAddressPage, "new@test.com")

        val result =
          ContractorEmailConfirmationNotStoredPage
            .cleanup(Some(true), answers)
            .success
            .value

        result.get(EmailAddressPage) mustBe Some("new@test.com")
      }

      "must keep answers unchanged when no answer is provided" in {
        val answers = UserAnswers("id")

        val result =
          ContractorEmailConfirmationNotStoredPage
            .cleanup(None, answers)
            .success
            .value

        result mustEqual answers
      }
    }
  }
}
