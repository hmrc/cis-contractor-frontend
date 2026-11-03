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
import pages.add.TypeOfSubcontractorPage
import queries.CisIdQuery

class UserAnswersSpec extends SpecBase {

  "UserAnswers with a subjourney id" - {

    "keep two subjourneys in one login document" in {
      val answers = UserAnswers("user-1")
        .set(TypeOfSubcontractorPage("uuid-1"), TypeOfSubcontractor.Partnership)
        .flatMap(_.set(CisIdQuery("uuid-1"), "1045"))
        .flatMap(_.set(TypeOfSubcontractorPage("uuid-2"), TypeOfSubcontractor.Partnership))
        .flatMap(_.set(CisIdQuery("uuid-2"), "800"))
        .success
        .value

      answers.get(TypeOfSubcontractorPage("uuid-1")) mustBe Some(TypeOfSubcontractor.Partnership)
      answers.get(CisIdQuery("uuid-1")) mustBe Some("1045")
      answers.get(TypeOfSubcontractorPage("uuid-2")) mustBe Some(TypeOfSubcontractor.Partnership)
      answers.get(CisIdQuery("uuid-2")) mustBe Some("800")
    }

    "do not overwrite root cisId when a subjourney cisId is set" in {
      val answers = UserAnswers("user-1")
        .set(CisIdQuery, "1")
        .flatMap(_.set(CisIdQuery("uuid-1"), "1045"))
        .success
        .value

      answers.get(CisIdQuery) mustBe Some("1")
      answers.get(CisIdQuery("uuid-1")) mustBe Some("1045")
    }
  }
}
