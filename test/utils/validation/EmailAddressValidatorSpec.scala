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

package utils.validation

import models.validation.{
  FieldValidationFailure,
  SubcontractorValidationField
}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class EmailAddressValidatorSpec
  extends AnyWordSpec
    with Matchers {

  "EmailAddressValidator.validate" must {

    "return no failure when the email address is missing" in {
      EmailAddressValidator.validate(None) mustBe None
    }

    "return no failure when the email address is empty" in {
      EmailAddressValidator.validate(Some("")) mustBe None
    }

    "return no failure when the email address contains only whitespace" in {
      EmailAddressValidator.validate(Some("   ")) mustBe None
    }

    "return no failure for a valid email address" in {
      EmailAddressValidator
        .validate(Some("subcontractor@example.com")) mustBe None
    }

    "return no failure for a valid email address containing supported characters" in {
      EmailAddressValidator
        .validate(
          Some("subcontractor.name+cis@example.co.uk")
        ) mustBe None
    }

    "return no failure when the email address is exactly the maximum length" in {
      val emailAddress =
        s"${"a" * 242}@example.com"

      emailAddress.length mustBe 254

      EmailAddressValidator
        .validate(Some(emailAddress)) mustBe None
    }

    "return a failure when the email address exceeds the maximum length" in {
      val emailAddress =
        s"${"a" * 243}@example.com"

      EmailAddressValidator
        .validate(Some(emailAddress)) mustBe
        Some(
          FieldValidationFailure(
            field =
              SubcontractorValidationField.EmailAddress,
            value = Some(emailAddress)
          )
        )
    }

    "return a failure when the email address has no at sign" in {
      val emailAddress =
        "subcontractor.example.com"

      EmailAddressValidator
        .validate(Some(emailAddress)) mustBe
        Some(
          FieldValidationFailure(
            field =
              SubcontractorValidationField.EmailAddress,
            value = Some(emailAddress)
          )
        )
    }

    "return a failure when the email address has no domain" in {
      val emailAddress =
        "subcontractor@"

      EmailAddressValidator
        .validate(Some(emailAddress)) mustBe
        Some(
          FieldValidationFailure(
            field =
              SubcontractorValidationField.EmailAddress,
            value = Some(emailAddress)
          )
        )
    }

    "retain the original value when the email address has surrounding whitespace" in {
      val emailAddress =
        " subcontractor@example.com "

      EmailAddressValidator
        .validate(Some(emailAddress)) mustBe
        Some(
          FieldValidationFailure(
            field =
              SubcontractorValidationField.EmailAddress,
            value = Some(emailAddress)
          )
        )
    }
  }
}
