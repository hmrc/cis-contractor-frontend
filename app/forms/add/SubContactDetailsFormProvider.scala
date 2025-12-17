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
import models.add.SubContactDetails
import play.api.data.Form
import play.api.data.Forms.*

import javax.inject.Inject

class SubContactDetailsFormProvider @Inject() extends Mappings {

  private val emailRegex = "^[A-Za-z0-9!#$%&*+-/=?^_`{|}~.]+@[A-Za-z0-9!#$%&*+-/=?^_`{|}~.]+$"
  private val phoneRegex = "^[0-9() \\-]{1,35}$"
  private val maxEmailLength = 254
  private val maxTelephoneLength = 35

   def apply(): Form[SubContactDetails] = Form(
     mapping(
      "email" -> text("subContactDetails.error.email.required")

        .verifying(
          firstError(
            maxLength(maxEmailLength, "subContactDetails.error.email.length"),
            regexp(emailRegex, "subContactDetails.error.email.invalid")
          )
        ),
      "telephone" -> text("subContactDetails.error.telephone.required")
        .verifying(
          firstError(
            maxLength(maxTelephoneLength, "subContactDetails.error.telephone.length"),
            regexp(phoneRegex, "subContactDetails.error.telephone.invalid")
          )
        )
    )(SubContactDetails.apply)(x => Some((x.email, x.telephone)))
   )
 }
