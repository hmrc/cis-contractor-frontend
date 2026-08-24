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

class UtrValidatorSpec extends AnyWordSpec with Matchers {

  "UtrValidator - validate UTR " must {

    "return no failure when the UTR is missing" in {
      UtrValidator
        .validate(None) mustBe None
    }

    "return no failure when the UTR is empty" in {
      UtrValidator
        .validate(Some("")) mustBe None
    }

    "return no failure when the UTR contains only whitespace" in {
      UtrValidator
        .validate(Some("   ")) mustBe None
    }

    "return no failure for a valid UTR - 5860920998" in {
      UtrValidator
        .validate(
          Some("5860920998")
        ) mustBe None
    }

    "return a failure when the UTR exceeds the maximum length" in {
      val utr = "1234567890"

      UtrValidator
        .validate(
          Some(utr)
        ) mustBe
        Some(
          FieldValidationFailure(
            field = SubcontractorValidationField.Utr,
            value = Some(utr)
          )
        )
    }

    "return a failure when the UTR contains incorrect format" in {
      val utr = "12345A7890"

      UtrValidator
        .validate(
          Some(utr)
        ) mustBe
        Some(
          FieldValidationFailure(
            field = SubcontractorValidationField.Utr,
            value = Some(utr)
          )
        )
    }

    "retain the original invalid UTR in the failure" in {
      val utr =
        "invalid-number"

      UtrValidator
        .validate(
          Some(utr)
        ) mustBe
        Some(
          FieldValidationFailure(
            field = SubcontractorValidationField.Utr,
            value = Some(utr)
          )
        )
    }
  }
}
