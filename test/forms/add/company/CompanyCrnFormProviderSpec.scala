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

package forms.add.company

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class CompanyCrnFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "companyCrn.error.required"
  val lengthKey   = "companyCrn.error.length"
  val invalidKey  = "companyCrn.error.invalid"

  val companyRegNumberRegex = """(?i)^(?:[A-Z]{2}\d{6}|\d{8})$"""

  val validCrn: Seq[String] = Seq(
    "AC012345",
    "ac012345",
    "AC 012 345",
    "ZZ999999",
    "00000001",
    "12345678",
    "12 34 56 78"
  )

  val invalidCrn: Seq[String] = Seq(
    "AC01-234",
    "AC01£345",
    "12AB3456"
  )

  val form = new CompanyCrnFormProvider()()

  ".value" - {

    val fieldName = "value"

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    "must bind valid crn data" in {
      validCrn.foreach { validCrn =>
        val result = form.bind(Map(fieldName -> validCrn))
        result.errors must be(empty)
      }
    }

    "must reject invalid crn formats" in {
      invalidCrn.foreach { invalidCrn =>
        val result = form.bind(Map(fieldName -> invalidCrn))
        result.errors must contain(
          FormError(fieldName, invalidKey, Seq(companyRegNumberRegex))
        )
      }
    }

    "must accept valid crn formats" in {
      validCrn.foreach { validCrn =>
        val result = form.bind(Map(fieldName -> validCrn))
        result.errors must be(empty)
        result.errors must not contain FormError(fieldName, invalidKey)
      }
    }

    "must display error when too long (more than 8 chars ignoring spaces)" in {
      val tooLongCrn = Seq(
        "123456789",
        "AB1234567",
        "12 34 56 78 9"
      )

      tooLongCrn.foreach { crn =>
        val result = form.bind(Map(fieldName -> crn))
        result.errors must contain(
          FormError(fieldName, invalidKey, Seq(companyRegNumberRegex))
        )
      }
    }

    "must display error when too short (less than 8 chars ignoring spaces)" in {
      val tooShortCrn = Seq(
        "0",
        "0123456",
        "AB",
        "A B 01234",
        "12   34 "
      )

      tooShortCrn.foreach { crn =>
        val result = form.bind(Map(fieldName -> crn))
        result.errors must contain(
          FormError(fieldName, invalidKey, Seq(companyRegNumberRegex))
        )
      }
    }

    "normalise the value by removing spaces and uppercasing" in {
      val result = form.bind(Map(fieldName -> " a  c 0123 45     "))
      result.hasErrors mustBe false
      result.value.value mustBe "AC012345"
    }
  }
}
