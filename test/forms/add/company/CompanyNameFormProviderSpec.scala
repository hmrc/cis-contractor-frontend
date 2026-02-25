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
import forms.mappings.Constants
import org.scalacheck.Gen
import play.api.data.FormError

class CompanyNameFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "companyName.error.required"
  val lengthKey   = "companyName.error.length"
  val invalidKey  = "companyName.error.invalidCharacters"

  private val form = new CompanyNameFormProvider()()

  ".value" - {

    val fieldName = "value"

    val validNamesGen: Gen[String] = Gen.oneOf(
      "ABC Construction Ltd",
      "A&B Builders",
      "North-East Builders (UK)",
      "Symbols ~!@#$%*+:;=?",
      "Contains £ and €",
      "Name with [brackets] and underscores_"
    )

    behave like fieldThatBindsValidData(form, fieldName, validNamesGen)

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = Constants.MaxLength56,
      lengthError = FormError(fieldName, lengthKey, Seq(Constants.MaxLength56))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    "trim leading and trailing spaces" in {
      val result = form.bind(Map(fieldName -> "   ABC Construction Ltd   "))
      result.value.value mustBe "ABC Construction Ltd"
    }

    "reject invalid characters (backtick, pipe, angle brackets)" in {
      val invalidNames = Seq("Backtick ` here", "Pipe | symbol", "Angle <bracket>")
      invalidNames.foreach { name =>
        val result = form.bind(Map(fieldName -> name))
        result.errors.map(_.message) must contain(invalidKey)
      }
    }
  }
}
