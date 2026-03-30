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
import forms.mappings.Constants.MaxLength35
import org.scalacheck.Gen
import play.api.data.FormError

class TrustPhoneNumberFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "trustPhoneNumber.error.required"
  val lengthKey   = "trustPhoneNumber.error.length"
  val invalidKey  = "trustPhoneNumber.error.invalid"

  val form = new TrustPhoneNumberFormProvider()()

  ".value" - {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      Gen.oneOf(
        "07777777777",
        "+447777777777",
        "  07777 77777 ",
        "(44)77777777777",
        "44-777-777"
      )
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = MaxLength35,
      lengthError = FormError(fieldName, lengthKey, Seq(MaxLength35))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    behave like fieldWithRegex(
      form,
      fieldName,
      invalidKey,
      regex = Validation.phoneRegex
    )
  }
}
