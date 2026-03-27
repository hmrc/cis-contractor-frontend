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

package forms.add.trust

import forms.Validation
import forms.mappings.{Constants, Mappings}
import models.add.InternationalAddress
import play.api.data.{Form, Forms, Mapping}

import javax.inject.Inject

class TrustAddressFormProvider @Inject() extends Mappings {

  private val addressLine1: Mapping[String] =
    text("trustAddress.error.addressLine1.required")
      .transform(_.trim, identity)
      .verifying(
        firstError(
          maxLength(Constants.MaxLength35, "trustAddress.error.addressLine1.length"),
          regexp(Validation.addressRegex, "trustAddress.error.addressLine1.invalidCharacters"),
          regexp(
            Validation.firstCharLetterOrDigitRegex,
            "trustAddress.error.addressLine1.firstCharMustBeLetterOrNumber"
          )
        )
      )

  private val addressLine2: Mapping[Option[String]] =
    Forms.optional(
      Forms.text
        .transform(_.trim, identity)
        .verifying(
          firstError(
            maxLength(Constants.MaxLength35, "trustAddress.error.addressLine2.length"),
            regexp(Validation.addressRegex, "trustAddress.error.addressLine2.invalidCharacters"),
            regexp(
              Validation.firstCharLetterOrDigitRegex,
              "trustAddress.error.addressLine2.firstCharMustBeLetterOrNumber"
            )
          )
        )
    )

  private val addressLine3: Mapping[String] =
    text("trustAddress.error.addressLine3.required")
      .transform(_.trim, identity)
      .verifying(
        firstError(
          maxLength(Constants.MaxLength35, "trustAddress.error.addressLine3.length"),
          regexp(Validation.addressRegex, "trustAddress.error.addressLine3.invalidCharacters"),
          regexp(
            Validation.firstCharLetterOrDigitRegex,
            "trustAddress.error.addressLine3.firstCharMustBeLetterOrNumber"
          )
        )
      )

  private val addressLine4: Mapping[Option[String]] =
    Forms.optional(
      Forms.text
        .transform(_.trim, identity)
        .verifying(
          firstError(
            maxLength(Constants.MaxLength35, "trustAddress.error.addressLine4.length"),
            regexp(Validation.addressRegex, "trustAddress.error.addressLine4.invalidCharacters"),
            regexp(
              Validation.firstCharLetterOrDigitRegex,
              "trustAddress.error.addressLine4.firstCharMustBeLetterOrNumber"
            )
          )
        )
    )

  private val postalCode: Mapping[String] =
    text("trustAddress.error.postalCode.required")
      .transform(_.trim.toUpperCase.replaceAll("\\s+", " "), identity)
      .verifying(
        firstError(
          maxLength(Constants.MaxLength8, "trustAddress.error.postalCode.length"),
          regexp(Validation.ukPostcodeRegex, "trustAddress.error.postalCode.invalid")
        )
      )

  def apply(): Form[InternationalAddress] = Form(
    Forms.mapping(
      "addressLine1" -> addressLine1,
      "addressLine2" -> addressLine2,
      "addressLine3" -> addressLine3,
      "addressLine4" -> addressLine4,
      "postalCode"   -> postalCode,
      "country"      -> text("trustAddress.country.error.required")
    )(InternationalAddress.apply)(internationalAddress => Some(Tuple.fromProductTyped(internationalAddress)))
  )
}
