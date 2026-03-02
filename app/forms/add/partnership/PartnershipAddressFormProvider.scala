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

package forms.add.partnership

import forms.mappings.Mappings
import models.add.InternationalAddress
import play.api.data.{Form, Forms}
import forms.Validation
import forms.mappings.Constants

import javax.inject.Inject

class PartnershipAddressFormProvider @Inject() extends Mappings {

  def apply(): Form[InternationalAddress] = Form(
    Forms.mapping(
      "addressLine1" ->
        text("partnershipAddress.error.addressLine1.required")
          .transform(_.trim, identity)
          .verifying(
            firstError(
              maxLength(Constants.MaxLength35, "partnershipAddress.error.addressLine1.length"),
              regexp(Validation.nameRegex, "partnershipAddress.error.addressLine1.invalidCharacters"),
              regexp(
                Validation.firstCharLetterOrDigitRegex,
                "partnershipAddress.error.addressLine1.firstCharMustBeLetterOrNumber"
              )
            )
          ),
      "addressLine2" ->
        Forms.optional(
          Forms.text
            .transform(_.trim, identity)
            .verifying(
              firstError(
                maxLength(Constants.MaxLength35, "partnershipAddress.error.addressLine2.length"),
                regexp(
                  Validation.nameRegex,
                  "partnershipAddress.error.addressLine2.invalidCharacters"
                ),
                regexp(
                  Validation.firstCharLetterRegex,
                  "partnershipAddress.error.addressLine2.firstCharMustBeLetterOrNumber"
                )
              )
            )
        ),
      "addressLine3" ->
        text("partnershipAddress.error.addressLine3.required")
          .transform(_.trim, identity)
          .verifying(
            firstError(
              maxLength(Constants.MaxLength35, "partnershipAddress.error.addressLine3.length"),
              regexp(Validation.nameRegex, "partnershipAddress.error.addressLine3.invalidCharacters"),
              regexp(
                Validation.firstCharLetterRegex,
                "partnershipAddress.error.addressLine3.firstCharMustBeLetterOrNumber"
              )
            )
          ),
      "addressLine4" ->
        Forms.optional(
          Forms.text
            .transform(_.trim, identity)
            .verifying(
              firstError(
                maxLength(Constants.MaxLength35, "partnershipAddress.error.addressLine4.length"),
                regexp(Validation.nameRegex, "partnershipAddress.error.addressLine4.invalidCharacters"),
                regexp(
                  Validation.firstCharLetterRegex,
                  "partnershipAddress.error.addressLine4.firstCharMustBeLetterOrNumber"
                )
              )
            )
        ),
      "postalCode"   ->
        text("partnershipAddress.error.postalCode.required")
          .transform(
            _.trim.toUpperCase.replaceAll("\\s+", " "),
            identity
          )
          .verifying(
            firstError(
              maxLength(Constants.MaxLength8, "partnershipAddress.error.postalCode.length"),
              regexp(Validation.ukPostcodeRegex, "partnershipAddress.error.postalCode.invalid")
            )
          ),
      "country"      -> text("partnershipAddress.country.error.required")
    )(InternationalAddress.apply)(internationalAddress => Some(Tuple.fromProductTyped(internationalAddress)))
  )
}
