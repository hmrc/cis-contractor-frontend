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
import forms.mappings.Constants.MaxLength20
import org.scalacheck.Gen
import play.api.data.FormError

class CompanyWorksReferenceFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "companyWorksReference.error.required"
  val lengthKey   = "companyWorksReference.error.length"
  val invalidKey  = "companyWorksReference.error.invalid"

  val form = new CompanyWorksReferenceFormProvider()()

  ".value" - {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      Gen.oneOf(
        "WR-001",
        "WR-0002",
        "WR-98"
      )
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = MaxLength20,
      lengthError = FormError(fieldName, lengthKey, Seq(MaxLength20))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
