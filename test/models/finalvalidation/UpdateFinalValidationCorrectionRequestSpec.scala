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

package models.finalvalidation

import base.SpecBase
import play.api.libs.json.Json

class UpdateFinalValidationCorrectionRequestSpec extends SpecBase {

  "UpdateFinalValidationCorrectionRequest" - {

    "must round trip through JSON" in {

      val request =
        UpdateFinalValidationCorrectionRequest(
          subcontractorId = 1L,
          changeTarget = "tradingName",
          patch = FinalValidationSubcontractorPatch(
            tradingName = Some("Smith Trading")
          )
        )

      Json
        .toJson(request)
        .as[UpdateFinalValidationCorrectionRequest] mustBe request
    }

    "must write the expected JSON" in {

      val request =
        UpdateFinalValidationCorrectionRequest(
          subcontractorId = 1L,
          changeTarget = "tradingName",
          patch = FinalValidationSubcontractorPatch(
            tradingName = Some("Smith Trading")
          )
        )

      Json.toJson(request) mustBe Json.obj(
        "subcontractorId" -> 1L,
        "changeTarget"    -> "tradingName",
        "patch"           -> Json.obj(
          "tradingName" -> "Smith Trading"
        )
      )
    }

    "must round trip with an empty patch" in {

      val request =
        UpdateFinalValidationCorrectionRequest(
          subcontractorId = 1L,
          changeTarget = "utr",
          patch = FinalValidationSubcontractorPatch()
        )

      Json
        .toJson(request)
        .as[UpdateFinalValidationCorrectionRequest] mustBe request
    }
  }
}
