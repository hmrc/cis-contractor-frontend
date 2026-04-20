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

package forms.add.trust

import forms.Validation
import forms.behaviours.StringFieldBehaviours
import forms.mappings.Constants
import org.scalacheck.Gen
import play.api.data.FormError

class TrustEmailAddressFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "trustEmailAddress.error.required"
  val invalidKey  = "trustEmailAddress.error.invalid"
  val lengthKey   = "trustEmailAddress.error.length"
  val maxLength   = Constants.MaxLength254

  val form = new TrustEmailAddressFormProvider()()

  ".value" - {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      Gen.oneOf(
        "test@example.com",
        "name.surname@test.co.uk",
        "a@b.cd",
        "user123@test-domain.com",
        "x+tag@mail.org"
      )
    )

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

    behave like fieldWithRegexpWithGenerator(
      form,
      fieldName,
      regexp = Validation.emailRegex,
      generator = stringsWithMaxLength(maxLength)
        .suchThat(str =>
          str.nonEmpty &&
            str.length <= maxLength &&
            !str.matches(Validation.emailRegex)
        ),
      error = FormError(fieldName, invalidKey, Seq(Validation.emailRegex))
    )

    "not bind an email where domain decodes to non-letter/digit characters" in {
      val result = form.bind(Map(fieldName -> "test@xn--domain-8ia.com")).apply(fieldName)
      result.errors mustEqual Seq(FormError(fieldName, invalidKey))
    }

    "bind an email with a valid internationalised domain label" in {
      val result = form.bind(Map(fieldName -> "test@xn--mnchen-3ya.de")).apply(fieldName)
      result.errors mustBe empty
    }
  }
}
