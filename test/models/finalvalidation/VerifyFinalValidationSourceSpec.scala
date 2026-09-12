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
import play.api.libs.json.{JsError, JsString, Json}

class VerifyFinalValidationSourceSpec extends SpecBase {

  import VerifyFinalValidationSource.*

  "VerifyFinalValidationSource" - {

    "must write SelectSubcontractor to JSON" in {

      Json.toJson[VerifyFinalValidationSource](SelectSubcontractor) mustBe
        JsString("SelectSubcontractor")
    }

    "must write SelectSubcontractorsToReverify to JSON" in {

      Json.toJson[VerifyFinalValidationSource](SelectSubcontractorsToReverify) mustBe
        JsString("SelectSubcontractorsToReverify")
    }

    "must write ReviewUnmatchedSubcontractors to JSON" in {

      Json.toJson[VerifyFinalValidationSource](ReviewUnmatchedSubcontractors) mustBe
        JsString("ReviewUnmatchedSubcontractors")
    }

    "must write ReviewInsufficientInfoSubcontractors to JSON" in {

      Json.toJson[VerifyFinalValidationSource](ReviewInsufficientInfoSubcontractors) mustBe
        JsString("ReviewInsufficientInfoSubcontractors")
    }

    "must read valid values from JSON" in {

      Json
        .fromJson[VerifyFinalValidationSource](
          JsString("SelectSubcontractor")
        )
        .get mustBe SelectSubcontractor

      Json
        .fromJson[VerifyFinalValidationSource](
          JsString("SelectSubcontractorsToReverify")
        )
        .get mustBe SelectSubcontractorsToReverify

      Json
        .fromJson[VerifyFinalValidationSource](
          JsString("ReviewUnmatchedSubcontractors")
        )
        .get mustBe ReviewUnmatchedSubcontractors

      Json
        .fromJson[VerifyFinalValidationSource](
          JsString("ReviewInsufficientInfoSubcontractors")
        )
        .get mustBe ReviewInsufficientInfoSubcontractors
    }

    "must round trip all values through JSON" in {

      val values =
        Seq(
          SelectSubcontractor,
          SelectSubcontractorsToReverify,
          ReviewUnmatchedSubcontractors,
          ReviewInsufficientInfoSubcontractors
        )

      values.foreach { source =>
        Json
          .toJson[VerifyFinalValidationSource](source)
          .as[VerifyFinalValidationSource] mustBe source
      }
    }

    "must fail to read an unknown value" in {

      Json
        .fromJson[VerifyFinalValidationSource](JsString("Unknown")) mustBe
        JsError("Unknown VerifyFinalValidationSource: Unknown")
    }

    "must fail to read a non-string value" in {

      Json
        .fromJson[VerifyFinalValidationSource](Json.obj()) mustBe
        a[JsError]
    }
  }
}
