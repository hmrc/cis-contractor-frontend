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
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class NameValidatorSpec extends AnyWordSpec with Matchers {

  "NameValidator.validate" should {

    "return no failures when first name and surname are provided without trading name" in {
      NameValidator.validate(
        firstName = Some("John"),
        secondName = None,
        surname = Some("Smith"),
        tradingName = None
      ) shouldBe empty
    }

    "return no failures when trading name is provided without first name or surname" in {
      NameValidator.validate(
        firstName = None,
        secondName = None,
        surname = None,
        tradingName = Some("Acme Ltd")
      ) shouldBe empty
    }

    "return no failures when all names are provided" in {
      NameValidator.validate(
        firstName = Some("John"),
        secondName = Some("James"),
        surname = Some("Smith"),
        tradingName = Some("Acme Ltd")
      ) shouldBe empty
    }

    "return a first name failure when trading name and first name are blank" in {
      NameValidator.validate(
        firstName = None,
        secondName = None,
        surname = Some("Smith"),
        tradingName = None
      ) shouldBe Seq(
        FieldValidationFailure(
          field = SubcontractorValidationField.FirstName,
          value = None
        )
      )
    }

    "return a surname failure when trading name and surname are blank" in {
      NameValidator.validate(
        firstName = Some("John"),
        secondName = None,
        surname = None,
        tradingName = None
      ) shouldBe Seq(
        FieldValidationFailure(
          field = SubcontractorValidationField.Surname,
          value = None
        )
      )
    }

    "return first name and surname failures when all required names are blank" in {
      NameValidator.validate(
        firstName = None,
        secondName = None,
        surname = None,
        tradingName = None
      ) shouldBe Seq(
        FieldValidationFailure(
          field = SubcontractorValidationField.FirstName,
          value = None
        ),
        FieldValidationFailure(
          field = SubcontractorValidationField.Surname,
          value = None
        )
      )
    }

    "return a first name failure when second name is provided without first name" in {
      NameValidator.validate(
        firstName = None,
        secondName = Some("James"),
        surname = Some("Smith"),
        tradingName = Some("Acme Ltd")
      ) shouldBe Seq(
        FieldValidationFailure(
          field = SubcontractorValidationField.SecondName,
          value = Some("James")
        )
      )
    }

    "return a second name failure when second name is provided as blank and first name is blank" in {
      NameValidator.validate(
        firstName = None,
        secondName = Some("James"),
        surname = None,
        tradingName = None
      ) shouldBe Seq(
        FieldValidationFailure(
          field = SubcontractorValidationField.FirstName,
          value = None
        ),
        FieldValidationFailure(
          field = SubcontractorValidationField.Surname,
          value = None
        ),
        FieldValidationFailure(
          field = SubcontractorValidationField.SecondName,
          value = Some("James")
        )
      )
    }

    "treat blank values as missing" in {
      NameValidator.validate(
        firstName = Some(" "),
        secondName = None,
        surname = Some(""),
        tradingName = Some(" ")
      ) shouldBe Seq(
        FieldValidationFailure(
          field = SubcontractorValidationField.FirstName,
          value = Some(" ")
        ),
        FieldValidationFailure(
          field = SubcontractorValidationField.Surname,
          value = Some("")
        )
      )
    }
  }
}
