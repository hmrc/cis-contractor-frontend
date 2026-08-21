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

package models.validation

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsNumber, JsString, Json}

class SubcontractorValidationFieldSpec extends AnyWordSpec with Matchers {

  "SubcontractorValidationField JSON format" must {

    "write every supported field using its expected value" in {
      SubcontractorValidationField.values.foreach { field =>
        Json.toJson(field) mustBe
          JsString(field.value)
      }
    }

    "read every supported field" in {
      SubcontractorValidationField.values.foreach { field =>
        Json
          .fromJson[SubcontractorValidationField](
            JsString(field.value)
          )
          .get mustBe field
      }
    }

    "round-trip every supported field" in {
      SubcontractorValidationField.values.foreach { field =>
        val json =
          Json.toJson(field)

        json
          .validate[SubcontractorValidationField]
          .get mustBe field
      }
    }

    "contain a unique value for every field" in {
      val values =
        SubcontractorValidationField.values
          .map(_.value)

      values.distinct.size mustBe values.size
    }

    "fail to read an unsupported field" in {
      val result =
        Json.fromJson[SubcontractorValidationField](
          JsString("unsupportedField")
        )

      result.isError mustBe true
    }

    "fail to read a value that is not a string" in {
      val result =
        Json.fromJson[SubcontractorValidationField](
          JsNumber(1)
        )

      result.isError mustBe true
    }
  }
}
