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

package forms.verify

import forms.Validation
import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError
import forms.mappings.Constants.MaxLength254
import org.scalacheck.Gen

class EmailAddressFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "verify.emailAddress.error.required"
  val lengthKey   = "verify.emailAddress.error.length"
  val invalidKey  = "verify.emailAddress.error.invalid"

  val form = new EmailAddressFormProvider()()

  ".value" - {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      Gen.oneOf(
        "test@example.com",
        "name.surname@test.co.uk",
        "a@b.cd",
        "user123@test-domain.com"
      )
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = MaxLength254,
      lengthError = FormError(fieldName, lengthKey, Seq(MaxLength254))
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
      generator = stringsWithMaxLength(MaxLength254)
        .suchThat(str =>
          str.nonEmpty &&
            str.length <= MaxLength254 &&
            !str.matches(Validation.emailRegex)
        ),
      error = FormError(fieldName, invalidKey, Seq(Validation.emailRegex))
    )
  }
}
