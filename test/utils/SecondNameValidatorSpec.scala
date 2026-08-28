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

package utils

import models.validation.{FieldValidationFailure, SubcontractorValidationField}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class SecondNameValidatorSpec extends AnyWordSpec with Matchers {

  "SecondNameValidator - validate secondName " must {

    "return failure when the secondName is empty" in {
      val secondName = ""
      SecondNameValidator
        .validate(Some("")) mustBe Some(
        FieldValidationFailure(
          field = SubcontractorValidationField.SecondName,
          value = Some(secondName)
        )
      )
    }

    "return failure when the secondName is None" in {
      val secondName = None
      SecondNameValidator
        .validate(secondName) mustBe Some(
        FieldValidationFailure(
          field = SubcontractorValidationField.SecondName,
          value = None
        )
      )
    }

    "return no failure for a valid secondName" in {
      SecondNameValidator
        .validate(
          Some("John")
        ) mustBe None
    }

    "return no failure for a valid secondName - John-Douglas" in {
      SecondNameValidator
        .validate(
          Some("John-Douglas")
        ) mustBe None
    }

    "retain the original invalid secondName in the failure" in {
      val secondName =
        "12345678901234567890123456789012345678901234567890<>"

      SecondNameValidator
        .validate(
          Some(secondName)
        ) mustBe
        Some(
          FieldValidationFailure(
            field = SubcontractorValidationField.SecondName,
            value = Some(secondName)
          )
        )
    }
  }
}
