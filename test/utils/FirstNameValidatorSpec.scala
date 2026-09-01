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

class FirstNameValidatorSpec extends AnyWordSpec with Matchers {

  "FirstNameValidator - validate firstName " must {

    "return failure when the firstName is empty" in {
      val firstName = ""
      FirstNameValidator
        .validate(Some("")) mustBe Some(
        FieldValidationFailure(
          field = SubcontractorValidationField.FirstName,
          value = Some(firstName)
        )
      )
    }

    "return no failure when the firstName is None" in {
      FirstNameValidator
        .validate(None) mustBe None
    }

    "return no failure for a valid firstName" in {
      FirstNameValidator
        .validate(
          Some("John")
        ) mustBe None
    }

    "return no failure for a valid firstName - John-Douglas" in {
      FirstNameValidator
        .validate(
          Some("John-Douglas")
        ) mustBe None
    }

    "retain the original invalid firstName in the failure" in {
      val firstName =
        "12345678901234567890123456789012345678901234567890<>"

      FirstNameValidator
        .validate(
          Some(firstName)
        ) mustBe
        Some(
          FieldValidationFailure(
            field = SubcontractorValidationField.FirstName,
            value = Some(firstName)
          )
        )
    }
  }
}
