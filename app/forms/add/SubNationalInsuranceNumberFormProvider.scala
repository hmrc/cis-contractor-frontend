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

import forms.mappings.Mappings
import forms.Validation
import play.api.data.Form
import play.api.data.validation.{Constraint, Invalid, Valid}

import javax.inject.Inject

class SubNationalInsuranceNumberFormProvider @Inject() extends Mappings {

  private val maxLength = 9

  private def normalised(value: String): String =
    value.replaceAll("\\s", "").toUpperCase

  private val lengthConstraint: Constraint[String] =
    Constraint("constraints.nino.length") { value =>
      if (normalised(value).length <= maxLength) {
        Valid
      } else {
        Invalid("subNationalInsuranceNumber.error.length")
      }
    }

  def apply(): Form[String] =
    Form(
      "value" -> nino("subNationalInsuranceNumber.error.required")
        .verifying(
          firstError(
            lengthConstraint,
            Validation.isNinoValid("value", "subNationalInsuranceNumber.error.invalidCharacters")
          )
        )
    )
}
