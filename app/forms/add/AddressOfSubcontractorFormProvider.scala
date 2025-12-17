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
import models.add.UKAddress
import play.api.data.{Form, Forms}

import javax.inject.Inject

class AddressOfSubcontractorFormProvider @Inject() extends Mappings {

  private val allowedAddressCharsRegex =
    """^[A-Za-z0-9"~!@#\$%*+:\;=\?\s,\.\[\]_\{\}\(\)/&'\-\^\\£€]+$"""

  private val firstCharLetterRegex =
    """^[A-Za-z].*"""

  private val ukPostcodeRegex =
    """^[A-Za-z0-9 ~!"@#\$%&'()*+,\-./:;<=>?\[\\\]^_\{\}£€]*$"""
  
  def apply(): Form[UKAddress] = Form(
    Forms.mapping(
      "addressLine1" ->
        text("addressOfSubcontractor.error.addressLine1.required")
          .transform(_.trim, identity)
          .verifying(
            firstError(
              maxLength(35, "addressOfSubcontractor.error.addressLine1.length"),
              regexp(allowedAddressCharsRegex, "addressOfSubcontractor.error.addressLine1.invalidCharacters"),
              regexp(firstCharLetterRegex, "addressOfSubcontractor.error.addressLine1.firstCharMustBeLetter")
            )
          ),
      "addressLine2" ->
        text("addressOfSubcontractor.error.addressLine2.required")
          .transform(_.trim, identity)
          .verifying(
            firstError(
              maxLength(35, "addressOfSubcontractor.error.addressLine2.length"),
              regexp(allowedAddressCharsRegex, "addressOfSubcontractor.error.addressLine2.invalidCharacters"),
              regexp(firstCharLetterRegex, "addressOfSubcontractor.error.addressLine2.firstCharMustBeLetter")
            )
          ),
      "addressLine3" ->
        text("addressOfSubcontractor.error.addressLine3.required")
          .transform(_.trim, identity)
          .verifying(
            firstError(
              maxLength(35, "addressOfSubcontractor.error.addressLine3.length"),
              regexp(allowedAddressCharsRegex, "addressOfSubcontractor.error.addressLine3.invalidCharacters"),
              regexp(firstCharLetterRegex, "addressOfSubcontractor.error.addressLine3.firstCharMustBeLetter")
            )
          ),
      "addressLine4" ->
        Forms.optional(
          Forms.text
            .transform(_.trim, identity)
            .verifying(
              firstError(
                maxLength(35, "addressOfSubcontractor.error.addressLine4.length"),
                regexp(allowedAddressCharsRegex, "addressOfSubcontractor.error.addressLine4.invalidCharacters"),
                regexp(firstCharLetterRegex, "addressOfSubcontractor.error.addressLine4.firstCharMustBeLetter")
              )
            )
        ),
      "postCode"     ->
        text("addressOfSubcontractor.error.postCode.required")
          .transform(_.trim.toUpperCase, identity)
          .verifying(
            firstError(
              maxLength(8, "addressOfSubcontractor.error.postCode.length"),
              regexp(ukPostcodeRegex, "addressOfSubcontractor.error.postCode.invalid")
            )
          )
    )((a1: String, a2: String, a3: String, a4: Option[String], pc: String) =>
      UKAddress(a1, a2, a3, a4, pc)
    )(address =>
      Some(
        (
          address.addressLine1,
          address.addressLine2,
          address.addressLine3,
          address.addressLine4,
          address.postCode
        )
      )
    )
  )
}
