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

package pages.verification

import base.SpecBase
import models.response.GetNewestVerificationBatchResponse
import play.api.libs.json.Json

class NewestVerificationBatchResponsePageSpec extends SpecBase {

  "NewestVerificationBatchResponsePage" - {

    "must have the correct path" in {
      NewestVerificationBatchResponsePage.path.toString mustBe "/newestVerificationBatchResponse"
    }

    "must be able to set and get a value" in {
      val model = GetNewestVerificationBatchResponse(
        scheme = Nil,
        subcontractors = Nil,
        verificationBatch = Nil,
        verifications = Nil,
        submission = Nil,
        monthlyReturn = Nil,
        mrSubmission = Nil
      )

      val uaWithValue =
        emptyUserAnswers
          .set(NewestVerificationBatchResponsePage, model)
          .success
          .value

      uaWithValue.get(NewestVerificationBatchResponsePage) mustBe Some(model)
    }

    "must be able to remove the value" in {
      val model = GetNewestVerificationBatchResponse(
        scheme = Nil,
        subcontractors = Nil,
        verificationBatch = Nil,
        verifications = Nil,
        submission = Nil,
        monthlyReturn = Nil,
        mrSubmission = Nil
      )

      val uaWithValue =
        emptyUserAnswers
          .set(NewestVerificationBatchResponsePage, model)
          .success
          .value

      val uaRemoved =
        uaWithValue
          .remove(NewestVerificationBatchResponsePage)
          .success
          .value

      uaRemoved.get(NewestVerificationBatchResponsePage) mustBe None
    }

    "must serialise to the expected JSON key" in {
      val model = GetNewestVerificationBatchResponse(
        scheme = Nil,
        subcontractors = Nil,
        verificationBatch = Nil,
        verifications = Nil,
        submission = Nil,
        monthlyReturn = Nil,
        mrSubmission = Nil
      )

      val uaWithValue =
        emptyUserAnswers
          .set(NewestVerificationBatchResponsePage, model)
          .success
          .value

      (uaWithValue.data \ "newestVerificationBatchResponse").toOption mustBe Some(Json.toJson(model))
    }
  }
}
