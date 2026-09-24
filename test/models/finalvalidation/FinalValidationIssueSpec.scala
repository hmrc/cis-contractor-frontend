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

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.Json

class FinalValidationIssueSpec extends AnyFreeSpec with Matchers {

  "FinalValidationIssue" - {

    "must round trip through JSON" in {

      val issue =
        FinalValidationIssue(
          field = FinalValidationField.Utr,
          value = Some("1234567890")
        )

      Json
        .toJson(issue)
        .as[FinalValidationIssue] mustBe issue
    }

    "must write the expected JSON" in {

      val issue =
        FinalValidationIssue(
          field = FinalValidationField.Utr,
          value = Some("1234567890")
        )

      Json.toJson(issue) mustBe Json.obj(
        "field" -> "utr",
        "value" -> "1234567890"
      )
    }

    "must round trip when value is None" in {

      val issue =
        FinalValidationIssue(
          field = FinalValidationField.EmailAddress,
          value = None
        )

      Json
        .toJson(issue)
        .as[FinalValidationIssue] mustBe issue
    }
  }
}
