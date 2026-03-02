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

import forms.Validation
import forms.behaviours.StringFieldBehaviours
import org.scalacheck.Gen
import play.api.data.FormError

class CompanyEmailAddressFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "companyEmailAddress.error.required"
  val lengthKey   = "companyEmailAddress.error.length"
  val invalidKey  = "companyEmailAddress.error.invalid"
  val maxLength   = 254

  val form = new CompanyEmailAddressFormProvider()()

  ".value" - {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      Gen.oneOf(
        "test@test.com",
        "user123@example.co.uk",
        "firstname.lastname@test-domain.com"
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
  }
}
