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

import javax.inject.Inject

class PartnershipAddressFormProvider @Inject() extends Mappings {

  private val allowedAddressCharsRegex =
    """^[A-Za-z0-9"~!@#\$%*+:\;=\?\s,\.\[\]_\{\}\(\)/&'\-\^\\£€]+$"""

  private val firstCharLetterRegex =
    """^[A-Za-z].*"""

  private val firstCharLetterOrDigitRegex = """^[A-Za-z0-9].*"""

  private val ukPostcodeRegex =
    """^(GIR\s?0AA|(?:(?:[A-Z]{1,2}\d[A-Z\d]?|\d[A-Z]{2})\s?\d[A-Z]{2}))$"""

  def apply(): Form[InternationalAddress] = Form(
    Forms.mapping(
      "addressLine1" ->
        text("partnershipAddress.error.addressLine1.required")
          .transform(_.trim, identity)
          .verifying(
            firstError(
              maxLength(35, "partnershipAddress.error.addressLine1.length"),
              regexp(allowedAddressCharsRegex, "partnershipAddress.error.addressLine1.invalidCharacters"),
              regexp(
                firstCharLetterOrDigitRegex,
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
                maxLength(35, "partnershipAddress.error.addressLine2.length"),
                regexp(
                  allowedAddressCharsRegex,
                  "partnershipAddress.error.addressLine2.invalidCharacters"
                ),
                regexp(
                  firstCharLetterRegex,
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
              maxLength(35, "partnershipAddress.error.addressLine3.length"),
              regexp(allowedAddressCharsRegex, "partnershipAddress.error.addressLine3.invalidCharacters"),
              regexp(firstCharLetterRegex, "partnershipAddress.error.addressLine3.firstCharMustBeLetterOrNumber")
            )
          ),
      "addressLine4" ->
        Forms.optional(
          Forms.text
            .transform(_.trim, identity)
            .verifying(
              firstError(
                maxLength(35, "partnershipAddress.error.addressLine4.length"),
                regexp(allowedAddressCharsRegex, "partnershipAddress.error.addressLine4.invalidCharacters"),
                regexp(firstCharLetterRegex, "partnershipAddress.error.addressLine4.firstCharMustBeLetterOrNumber")
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
              maxLength(8, "partnershipAddress.error.postalCode.length"),
              regexp(ukPostcodeRegex, "partnershipAddress.error.postalCode.invalid")
            )
          ),
      "country"      -> text("partnershipAddress.country.error.required")
    )(InternationalAddress.apply)(internationalAddress => Some(Tuple.fromProductTyped(internationalAddress)))
  )
}
