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

package forms.contractordetails

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class SchemeNameFormProviderSpec extends StringFieldBehaviours {

  val requiredKey            = "contractordetails.schemeName.error.required"
  val lengthKey              = "contractordetails.schemeName.error.length"
  val invalidCharactersError = "contractordetails.schemeName.error.invalidCharacters"
  val maxLength              = 56

  val form = new SchemeNameFormProvider()()

  ".value" - {

    val fieldName = "value"

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    "must bind valid data" in {
      val validSchemeNames: Seq[String] = Seq(
        "Alpha Scheme",
        "Growth+",
        "Value-Based Scheme",
        "Test: Scheme 1",
        "Name_With[Brackets]",
        "Name{Curly}Braces",
        "A&B Scheme"
      )
      validSchemeNames.foreach { validSchemeName =>
        val result = form.bind(Map(fieldName -> validSchemeName))
        result.errors must be(empty)
      }
    }
    "must not bind invalid characters" in {
      val invalidSchemeNames = Seq(
        "Hello|World",
        "Name<test",
        "Plan😅",
        "Café",
        "Value©2024"
      )
      invalidSchemeNames.foreach { invalidSchemeName =>
        val result = form.bind(Map(fieldName -> invalidSchemeName))
        result.errors.map(_.message) must contain(invalidCharactersError)
      }
    }
  }
}
