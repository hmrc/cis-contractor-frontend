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

package forms.add.partnership

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError
import org.scalacheck.Gen
import forms.mappings.Constants

class PartnershipEmailAddressFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "partnershipEmailAddress.error.required"
  val invalidKey  = "partnershipEmailAddress.error.invalid"
  val lengthKey   = "partnershipEmailAddress.error.length"
  val maxLength   = Constants.MaxLength254

  val form = new PartnershipEmailAddressFormProvider()()

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
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
