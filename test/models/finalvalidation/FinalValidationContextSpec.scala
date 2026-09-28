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

class FinalValidationContextSpec extends SpecBase {

  import FinalValidationContext.*

  "FinalValidationContext" - {

    "must write MonthlyReturn to JSON" in {

      Json.toJson[FinalValidationContext](MonthlyReturn) mustBe
        JsString("MonthlyReturn")
    }

    "must write VerifySubcontractor to JSON" in {

      Json.toJson[FinalValidationContext](VerifySubcontractor) mustBe
        JsString("VerifySubcontractor")
    }

    "must read MonthlyReturn from JSON" in {

      Json
        .fromJson[FinalValidationContext](JsString("MonthlyReturn"))
        .get mustBe MonthlyReturn
    }

    "must read VerifySubcontractor from JSON" in {

      Json
        .fromJson[FinalValidationContext](JsString("VerifySubcontractor"))
        .get mustBe VerifySubcontractor
    }

    "must round trip MonthlyReturn through JSON" in {

      Json
        .toJson[FinalValidationContext](MonthlyReturn)
        .as[FinalValidationContext] mustBe MonthlyReturn
    }

    "must round trip VerifySubcontractor through JSON" in {

      Json
        .toJson[FinalValidationContext](VerifySubcontractor)
        .as[FinalValidationContext] mustBe VerifySubcontractor
    }

    "must fail to read an unknown context" in {

      Json
        .fromJson[FinalValidationContext](JsString("Unknown")) mustBe
        JsError("Unknown FinalValidationContext: Unknown")
    }

    "must fail to read a non-string value" in {

      Json
        .fromJson[FinalValidationContext](Json.obj("context" -> "MonthlyReturn")) mustBe
        a[JsError]
    }
  }
}
