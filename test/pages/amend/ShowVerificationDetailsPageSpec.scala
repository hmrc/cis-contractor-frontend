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

package pages.amend

import base.SpecBase
import play.api.libs.json.JsPath

class ShowVerificationDetailsPageSpec extends SpecBase {

  "ShowVerificationDetailsPage" - {

    "must use the expected JSON path" in {
      ShowVerificationDetailsPage.path mustBe
        JsPath \ "ShowVerificationDetailsPage"
    }

    "must save and retrieve true from UserAnswers" in {
      val updatedAnswers =
        emptyUserAnswers
          .set(
            ShowVerificationDetailsPage,
            true
          )
          .success
          .value

      updatedAnswers
        .get(ShowVerificationDetailsPage)
        .value mustBe true
    }

    "must save and retrieve false from UserAnswers" in {
      val updatedAnswers =
        emptyUserAnswers
          .set(
            ShowVerificationDetailsPage,
            false
          )
          .success
          .value

      updatedAnswers
        .get(ShowVerificationDetailsPage)
        .value mustBe false
    }

    "must remove the answer from UserAnswers" in {
      val answersWithValue =
        emptyUserAnswers
          .set(
            ShowVerificationDetailsPage,
            true
          )
          .success
          .value

      val updatedAnswers =
        answersWithValue
          .remove(ShowVerificationDetailsPage)
          .success
          .value

      updatedAnswers
        .get(ShowVerificationDetailsPage) mustBe None
    }
  }
}
