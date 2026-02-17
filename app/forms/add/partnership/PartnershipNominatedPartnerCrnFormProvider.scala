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

import forms.Validation.companyRegNumberRegex
import forms.mappings.Mappings
import play.api.data.Form
import play.api.data.Forms.single
import play.api.data.validation.{Constraint, Invalid, Valid}

class PartnershipNominatedPartnerCrnFormProvider extends Mappings {

  private val maxLength = 8

  private def normalised(value: String): String =
    value.replaceAll("\\s", "").toUpperCase

  private val lengthConstraint: Constraint[String] =
    Constraint("constraints.crn.length") { value =>
      if (value.length <= maxLength) Valid
      else Invalid("partnershipNominatedPartnerCrn.error.length")
    }

  private val formatConstraint: Constraint[String] =
    Constraint("constraints.crn.format") { value =>
      if (value.matches(companyRegNumberRegex)) Valid
      else Invalid("partnershipNominatedPartnerCrn.error.invalidCharacters")
    }
    
  private val firstErrorConstraint: Constraint[String] =
    Constraint("constraints.nino.firstError") { value =>
       lengthConstraint(value) match {
          case i: Invalid => i
          case Valid => formatConstraint(value)
       }
    } 
  
  def apply(): Form[String] =
    Form(
      single(
        "value" -> text("partnershipNominatedPartnerCrn.error.required")
          .transform(normalised, identity)
          .verifying(firstErrorConstraint)
      )
    )
}
