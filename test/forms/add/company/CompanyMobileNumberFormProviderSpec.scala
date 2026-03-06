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
import forms.mappings.Constants.MaxLength35
import org.scalacheck.Gen
import play.api.data.FormError

class CompanyMobileNumberFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "companyMobileNumber.error.required"
  val lengthKey   = "companyMobileNumber.error.length"
  val invalidKey  = "companyMobileNumber.error.invalid"

  val form = new CompanyMobileNumberFormProvider()()

  ".value" - {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      Gen.oneOf(
        "+44 7700 900 999",
        "+44 7700 900 111",
        "07700 900 982"
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
      regex = Validation.mobileRegex
    )
  }
}
