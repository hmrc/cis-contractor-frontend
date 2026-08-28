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

class NinoValidatorSpec extends AnyWordSpec with Matchers {

  "NinoValidator.validate" must {

    "return no failure when the NINO is missing" in {
      NinoValidator.validate(None) mustBe None
    }

    "return no failure when the NINO is empty" in {
      NinoValidator.validate(Some("")) mustBe None
    }

    "return no failure when the NINO contains only whitespace" in {
      NinoValidator.validate(Some("   ")) mustBe None
    }

    "return no failure for a valid NINO" in {
      NinoValidator.validate(Some("AA123456A")) mustBe None
    }

    "return no failure for a valid lowercase NINO" in {
      NinoValidator.validate(Some("aa123456a")) mustBe None
    }

    "return a failure when the NINO exceeds the maximum length" in {
      val nino = "AA123456AA"

      NinoValidator.validate(Some(nino)) mustBe
        Some(
          FieldValidationFailure(
            field = SubcontractorValidationField.Nino,
            value = Some(nino)
          )
        )
    }

    "return a failure when the first character is not allowed" in {
      val nino = "DA123456A"

      NinoValidator.validate(Some(nino)) mustBe
        Some(
          FieldValidationFailure(
            field = SubcontractorValidationField.Nino,
            value = Some(nino)
          )
        )
    }

    "retain the original invalid NINO in the failure" in {
      val nino = "invalid"

      NinoValidator.validate(Some(nino)) mustBe
        Some(
          FieldValidationFailure(
            field = SubcontractorValidationField.Nino,
            value = Some(nino)
          )
        )
    }
  }
}