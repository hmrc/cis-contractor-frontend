/*
 * Copyright 2025 HM Revenue & Customs
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

package forms

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError


import org.scalacheck.Gen

class NameOfSubcontractorFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "nameOfSubcontractor.error.required"
  val lengthKey   = "nameOfSubcontractor.error.length"
  val invalidKey  = "nameOfSubcontractor.error.invalidCharacters"
  val maxLength   = 56

  private val form = new NameOfSubcontractorFormProvider()()

  ".value" - {

    val fieldName = "value"

    val validNamesGen: Gen[String] = Gen.oneOf(
      "ABC Construction LTD",
      "A&B Contractors Ltd",
      "North-East Builders (UK)",
      "Symbols ~!@#$%*+:;=?",
      "Contains £ and €",
      "Dollar $ sign"
    )

    behave like fieldThatBindsValidData(form, fieldName, validNamesGen)

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

    "trim leading and trailing spaces" in {
      val result = form.bind(Map(fieldName -> "   ABC Ltd   "))
      result.value.value mustBe "ABC Ltd"
    }

    "reject invalid characters (backtick, pipe)" in {
      val invalidNames = Seq("Backtick ` here", "Pipe | symbol")
      invalidNames.foreach { name =>
        val result = form.bind(Map(fieldName -> name))
        result.errors.map(_.message) must contain(invalidKey)
      }
    }
  }
}
