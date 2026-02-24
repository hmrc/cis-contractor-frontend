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

import forms.mappings.Mappings
import models.add.InternationalAddress
import play.api.data.{Form, Forms}
import forms.Validation
import forms.mapping.Constants

import javax.inject.Inject

class CompanyAddressFormProvider @Inject() extends Mappings {

  def apply(): Form[InternationalAddress] = Form(
    Forms.mapping(
      "addressLine1" ->
        text("companyAddress.error.addressLine1.required")
          .transform(_.trim, identity)
          .verifying(
            firstError(
              maxLength(Constants.MaxLength35, "companyAddress.error.addressLine1.length"),
              regexp(Validation.nameRegex, "companyAddress.error.addressLine1.invalidCharacters"),
              regexp(
                Validation.firstCharLetterOrDigitRegex,
                "companyAddress.error.addressLine1.firstCharMustBeLetterOrNumber"
              )
            )
          ),
      "addressLine2" ->
        Forms.optional(
          Forms.text
            .transform(_.trim, identity)
            .verifying(
              firstError(
                maxLength(Constants.MaxLength35, "companyAddress.error.addressLine2.length"),
                regexp(
                  Validation.nameRegex,
                  "companyAddress.error.addressLine2.invalidCharacters"
                ),
                regexp(
                  Validation.firstCharLetterRegex,
                  "companyAddress.error.addressLine2.firstCharMustBeLetterOrNumber"
                )
              )
            )
        ),
      "addressLine3" ->
        text("companyAddress.error.addressLine3.required")
          .transform(_.trim, identity)
          .verifying(
            firstError(
              maxLength(Constants.MaxLength35, "companyAddress.error.addressLine3.length"),
              regexp(Validation.nameRegex, "companyAddress.error.addressLine3.invalidCharacters"),
              regexp(Validation.firstCharLetterRegex, "companyAddress.error.addressLine3.firstCharMustBeLetterOrNumber")
            )
          ),
      "addressLine4" ->
        Forms.optional(
          Forms.text
            .transform(_.trim, identity)
            .verifying(
              firstError(
                maxLength(Constants.MaxLength35, "companyAddress.error.addressLine4.length"),
                regexp(Validation.nameRegex, "companyAddress.error.addressLine4.invalidCharacters"),
                regexp(
                  Validation.firstCharLetterRegex,
                  "companyAddress.error.addressLine4.firstCharMustBeLetterOrNumber"
                )
              )
            )
        ),
      "postalCode"   ->
        text("companyAddress.error.postalCode.required")
          .transform(
            _.trim.toUpperCase.replaceAll("\\s+", " "),
            identity
          )
          .verifying(
            firstError(
              maxLength(Constants.MaxLength8, "companyAddress.error.postalCode.length"),
              regexp(Validation.ukPostcodeRegex, "companyAddress.error.postalCode.invalid")
            )
          ),
      "country"      -> text("companyAddress.country.error.required")
    )(InternationalAddress.apply)(internationalAddress => Some(Tuple.fromProductTyped(internationalAddress)))
  )
}
