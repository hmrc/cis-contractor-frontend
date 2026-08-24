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

class TrustValidatorSpec extends AnyWordSpec with Matchers {

  "TrustValidator.validate" must {

    "return no failures when all common details are missing" in {
      TrustValidator.validate(
        worksReferenceNumber = None,
        tradingName = None,
        utr = None
      ) mustBe Nil
    }

    "return the works reference number failure" in {
      val worksReferenceNumber =
        "A12323452345#@[]{}$%^&£~"

      TrustValidator.validate(
        worksReferenceNumber = Some(worksReferenceNumber),
        tradingName = None,
        utr = None
      ) mustBe
        List(
          FieldValidationFailure(
            field = SubcontractorValidationField.WorksReferenceNumber,
            value = Some(worksReferenceNumber)
          )
        )
    }

    "return every failure" in {

      val worksReferenceNumber =
        "A12323452345#@[]{}$%^&£~"

      val tradingName =
        "12345678901234567890123456789012345678901234567890<>"

      val utr = "12345A7890"

      TrustValidator.validate(
        worksReferenceNumber = Some(worksReferenceNumber),
        tradingName = Some(tradingName),
        utr = Some(utr)
      ) mustBe
        List(
          FieldValidationFailure(
            field = SubcontractorValidationField.WorksReferenceNumber,
            value = Some(worksReferenceNumber)
          ),
          FieldValidationFailure(
            field = SubcontractorValidationField.Utr,
            value = Some(utr)
          ),
          FieldValidationFailure(
            field = SubcontractorValidationField.TradingName,
            value = Some(tradingName)
          )
        )
    }

    "retain valid fields while returning only invalid fields" in {
      val invalidWorksReferenceNumber =
        "A12323452345#@[]{}$%^&£~"

      TrustValidator.validate(
        worksReferenceNumber = Some(invalidWorksReferenceNumber),
        tradingName = Some("Test Trading Name 1234@"),
        utr = None
      ) mustBe
        List(
          FieldValidationFailure(
            field = SubcontractorValidationField.WorksReferenceNumber,
            value = Some(invalidWorksReferenceNumber)
          )
        )
    }
  }
}
