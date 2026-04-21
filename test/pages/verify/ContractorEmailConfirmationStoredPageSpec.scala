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
import models.verify.ContractorEmailConfirmationStored
import pages.behaviours.PageBehaviours

class ContractorEmailConfirmationStoredPageSpec extends PageBehaviours {

  "ContractorEmailConfirmationStoredPage" - {

    beRetrievable[ContractorEmailConfirmationStored](ContractorEmailConfirmationStoredPage)

    beSettable[ContractorEmailConfirmationStored](ContractorEmailConfirmationStoredPage)

    beRemovable[ContractorEmailConfirmationStored](ContractorEmailConfirmationStoredPage)

    "cleanup" - {

      "must retain user answers when the answer is DifferentEmail" in {
        val answers = UserAnswers("id")
          .setOrException(ContractorEmailConfirmationStoredPage, ContractorEmailConfirmationStored.DifferentEmail)

        val result = ContractorEmailConfirmationStoredPage
          .cleanup(Some(ContractorEmailConfirmationStored.DifferentEmail), answers)
          .success
          .value

        result mustEqual answers
      }

      "must retain user answers when the answer is CurrentEmail" in {
        val answers = UserAnswers("id")
          .setOrException(ContractorEmailConfirmationStoredPage, ContractorEmailConfirmationStored.CurrentEmail)

        val result = ContractorEmailConfirmationStoredPage
          .cleanup(Some(ContractorEmailConfirmationStored.CurrentEmail), answers)
          .success
          .value

        result mustEqual answers
      }

      "must retain user answers when the answer is DoNotSend" in {
        val answers = UserAnswers("id")
          .setOrException(ContractorEmailConfirmationStoredPage, ContractorEmailConfirmationStored.DoNotSend)

        val result = ContractorEmailConfirmationStoredPage
          .cleanup(Some(ContractorEmailConfirmationStored.DoNotSend), answers)
          .success
          .value

        result mustEqual answers
      }

      "must retain user answers when there is no answer" in {
        val answers = UserAnswers("id")

        val result = ContractorEmailConfirmationStoredPage
          .cleanup(None, answers)
          .success
          .value

        result mustEqual answers
      }
    }
  }
}
