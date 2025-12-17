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

package forms.add

import forms.add.SubContactDetailsFormProvider
import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class SubContactDetailsFormProviderSpec extends StringFieldBehaviours {

  val form = new SubContactDetailsFormProvider()()

  ".email" - {

    val fieldName = "email"
    val requiredKey = "subContactDetails.error.email.required"
    val lengthKey = "subContactDetails.error.email.length"
    val invalidKey  = "subContactDetails.error.email.invalid"
    val maxLength = 254

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
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

  ".telephone" - {

    val fieldName = "telephone"
    val requiredKey = "subContactDetails.error.telephone.required"
    val lengthKey = "subContactDetails.error.telephone.length"
    val maxLength = 35

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
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
