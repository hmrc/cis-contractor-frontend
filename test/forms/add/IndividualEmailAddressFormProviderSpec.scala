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

package forms.add

import forms.Validation
import forms.behaviours.StringFieldBehaviours
import forms.mappings.Constants.MaxLength254
import org.scalacheck.Gen
import play.api.data.FormError

class IndividualEmailAddressFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "individualEmailAddress.error.required"
  val lengthKey   = "individualEmailAddress.error.length"
  val invalidKey  = "individualEmailAddress.error.invalid"

  val form = new IndividualEmailAddressFormProvider()()

  ".value" - {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      Gen.oneOf(
        "test@test.com",
        "user123@example.co.uk",
        "firstname.lastname@test-domain.com",
        "x+tag@mail.org",
        "a@b.cd"
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

    "bind an email with an IPv4 domain" in {
      val result = form.bind(Map(fieldName -> "test@192.168.1.1")).apply(fieldName)
      result.errors mustBe empty
    }

    "not bind an email with a non-ASCII domain" in {
      val result = form.bind(Map(fieldName -> "test@münchen.de")).apply(fieldName)
      result.errors mustEqual Seq(FormError(fieldName, invalidKey, Seq(Validation.emailRegex)))
    }

    "not bind an email with a Punycode domain" in {
      val result = form.bind(Map(fieldName -> "test@xn--mnchen-3ya.de")).apply(fieldName)
      result.errors mustEqual Seq(FormError(fieldName, invalidKey))
    }
  }
}
