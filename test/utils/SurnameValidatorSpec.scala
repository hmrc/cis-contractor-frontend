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

class SurnameValidatorSpec extends AnyWordSpec with Matchers {

  "SurnameValidator - validate surname " must {

    "return failure when the surname is empty" in {
      SurnameValidator
        .validate(Some("")) mustBe Some(
        FieldValidationFailure(
          field = SubcontractorValidationField.Surname,
          value = Some("")
        )
      )
    }

    "return no failure when the surname is None" in {
      SurnameValidator
        .validate(None) mustBe None
    }

    "return no failure for a valid surname" in {
      SurnameValidator
        .validate(
          Some("Smith")
        ) mustBe None
    }

    "return no failure for a valid surname - Smith-Jones" in {
      SurnameValidator
        .validate(
          Some("Smith-Jones")
        ) mustBe None
    }

    "retain the original invalid surname in the failure" in {
      val surname =
        "12345678901234567890123456789012345678901234567890<>"

      SurnameValidator
        .validate(
          Some(surname)
        ) mustBe
        Some(
          FieldValidationFailure(
            field = SubcontractorValidationField.Surname,
            value = Some(surname)
          )
        )
    }
  }
}
