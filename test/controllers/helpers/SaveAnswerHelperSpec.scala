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

package controllers.helpers

import base.SpecBase
import models.{AmendMode, NormalMode}
import pages.add.trust.TrustNamePage

class SaveAnswerHelperSpec extends SpecBase {

  "saveAnswer" - {

    "must set and amend the answer when in AmendMode" in {

      val result =
        SaveAnswerHelper
          .saveAnswer(
            emptyUserAnswers,
            TrustNamePage,
            "ABC Trust",
            AmendMode
          )
          .success
          .value

      result.get(TrustNamePage) mustBe Some("ABC Trust")

      result
        .get(pages.amend.AmendedPagesPage)
        .value must contain(TrustNamePage.toString)
    }

    "must set the answer without amending when in NormalMode" in {

      val result =
        SaveAnswerHelper
          .saveAnswer(
            emptyUserAnswers,
            TrustNamePage,
            "ABC Trust",
            NormalMode
          )
          .success
          .value

      result.get(TrustNamePage) mustBe Some("ABC Trust")

      result.get(pages.amend.AmendedPagesPage) mustBe None
    }
  }
}
