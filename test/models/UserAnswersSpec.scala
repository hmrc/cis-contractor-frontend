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

package models

import base.SpecBase

class UserAnswersSpec extends SpecBase {

  "UserAnswers.initialiseSubJourney" - {

    "adds an empty folder for a new id" in {
      val answers = UserAnswers("user-1").initialiseSubJourney("org_abc").success.value

      answers.hasSubJourney("org_abc") mustBe true
    }

    "fails when the folder already exists" in {
      val answers = UserAnswers("user-1").initialiseSubJourney("org_abc").success.value

      answers.initialiseSubJourney("org_abc").isFailure mustBe true
    }
  }

  "UserAnswers.hasSubJourney" - {

    "is false when the folder is missing" in {
      UserAnswers("user-1").hasSubJourney("org_abc") mustBe false
    }
  }
}
