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

package utils.validation

import models.address.{Address, Country}
import models.validation.{
  FieldValidationFailure,
  SubcontractorValidationField
}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class AddressDetailsValidatorSpec
  extends AnyWordSpec
    with Matchers {

  "AddressDetailsValidator.validate" must {

    "return no failures when the address is missing" in {
      AddressDetailsValidator.validate(None) mustBe Nil
    }

    "return no failures when every address field is empty" in {
      val address =
        createAddress(addressLine1 = "")

      AddressDetailsValidator
        .validate(Some(address)) mustBe Nil
    }

    "return no failures for a valid address" in {
      val address =
        createAddress(
          addressLine1 = "1 High Street",
          addressLine2 = Some("Newcastle"),
          addressLine3 = Some("Tyne and Wear"),
          postcode = Some("NE1 1AA"),
          country =
            Some(
              Country(
                code = Some("GB"),
                name = Some("United Kingdom")
              )
            )
        )

      AddressDetailsValidator
        .validate(Some(address)) mustBe Nil
    }

    "return an address-line-1 failure when another address line exists" in {
      val address =
        createAddress(
          addressLine1 = "",
          addressLine2 = Some("Newcastle")
        )

      AddressDetailsValidator
        .validate(Some(address)) mustBe
        List(
          FieldValidationFailure(
            field =
              SubcontractorValidationField.AddressLine1,
            value = None
          )
        )
    }

    "return an address-line-1 failure when only the postcode exists" in {
      val address =
        createAddress(
          addressLine1 = "",
          postcode = Some("NE1 1AA")
        )

      AddressDetailsValidator
        .validate(Some(address)) mustBe
        List(
          FieldValidationFailure(
            field =
              SubcontractorValidationField.AddressLine1,
            value = None
          )
        )
    }

    "return an address-line-1 failure when only the country exists" in {
      val address =
        createAddress(
          addressLine1 = "",
          country =
            Some(
              Country(
                code = Some("GB"),
                name = Some("United Kingdom")
              )
            )
        )

      AddressDetailsValidator
        .validate(Some(address)) mustBe
        List(
          FieldValidationFailure(
            field =
              SubcontractorValidationField.AddressLine1,
            value = None
          )
        )
    }

    "return no failure when an address line is exactly 35 characters" in {
      val address =
        createAddress(
          addressLine1 = "A" * 35
        )

      AddressDetailsValidator
        .validate(Some(address)) mustBe Nil
    }

    "return a failure when an address line exceeds 35 characters" in {
      val value =
        "A" * 36

      val address =
        createAddress(
          addressLine1 = value
        )

      AddressDetailsValidator
        .validate(Some(address)) mustBe
        List(
          FieldValidationFailure(
            field =
              SubcontractorValidationField.AddressLine1,
            value = Some(value)
          )
        )
    }

    "return a failure when an address line does not start with a letter or digit" in {
      val value =
        "-High Street"

      val address =
        createAddress(
          addressLine1 = value
        )

      AddressDetailsValidator
        .validate(Some(address)) mustBe
        List(
          FieldValidationFailure(
            field =
              SubcontractorValidationField.AddressLine1,
            value = Some(value)
          )
        )
    }

    "return failures for every invalid optional address line" in {
      val addressLine2 =
        "-Newcastle"

      val addressLine3 =
        "A" * 36

      val addressLine4 =
        "Tyne | Wear"

      val address =
        createAddress(
          addressLine1 = "1 High Street",
          addressLine2 = Some(addressLine2),
          addressLine3 = Some(addressLine3),
          addressLine4 = Some(addressLine4)
        )

      AddressDetailsValidator
        .validate(Some(address)) mustBe
        List(
          FieldValidationFailure(
            field =
              SubcontractorValidationField.AddressLine2,
            value = Some(addressLine2)
          ),
          FieldValidationFailure(
            field =
              SubcontractorValidationField.AddressLine3,
            value = Some(addressLine3)
          ),
          FieldValidationFailure(
            field =
              SubcontractorValidationField.AddressLine4,
            value = Some(addressLine4)
          )
        )
    }

    "return no failure when the postcode is exactly 8 characters" in {
      val address =
        createAddress(
          addressLine1 = "1 High Street",
          postcode = Some("NE12 3AA")
        )

      AddressDetailsValidator
        .validate(Some(address)) mustBe Nil
    }

    "return a failure when the postcode exceeds 8 characters" in {
      val postcode =
        "ABCDEFGHI"

      val address =
        createAddress(
          addressLine1 = "1 High Street",
          postcode = Some(postcode)
        )

      AddressDetailsValidator
        .validate(Some(address)) mustBe
        List(
          FieldValidationFailure(
            field =
              SubcontractorValidationField.Postcode,
            value = Some(postcode)
          )
        )
    }

    "return a failure when the postcode contains unsupported characters" in {
      val postcode =
        "NE1`1AA"

      val address =
        createAddress(
          addressLine1 = "1 High Street",
          postcode = Some(postcode)
        )

      AddressDetailsValidator
        .validate(Some(address)) mustBe
        List(
          FieldValidationFailure(
            field =
              SubcontractorValidationField.Postcode,
            value = Some(postcode)
          )
        )
    }

    "return a failure when the country exceeds 35 characters" in {
      val country =
        "A" * 36

      val address =
        createAddress(
          addressLine1 = "1 High Street",
          country =
            Some(
              Country(
                code = None,
                name = Some(country)
              )
            )
        )

      AddressDetailsValidator
        .validate(Some(address)) mustBe
        List(
          FieldValidationFailure(
            field =
              SubcontractorValidationField.Country,
            value = Some(country)
          )
        )
    }

    "return all failures in address-field order" in {
      val addressLine2 =
        "-Newcastle"

      val addressLine3 =
        "A" * 36

      val postcode =
        "ABCDEFGHI"

      val country =
        "-United Kingdom"

      val address =
        createAddress(
          addressLine1 = "",
          addressLine2 = Some(addressLine2),
          addressLine3 = Some(addressLine3),
          postcode = Some(postcode),
          country =
            Some(
              Country(
                code = None,
                name = Some(country)
              )
            )
        )

      AddressDetailsValidator
        .validate(Some(address)) mustBe
        List(
          FieldValidationFailure(
            field =
              SubcontractorValidationField.AddressLine1,
            value = None
          ),
          FieldValidationFailure(
            field =
              SubcontractorValidationField.AddressLine2,
            value = Some(addressLine2)
          ),
          FieldValidationFailure(
            field =
              SubcontractorValidationField.AddressLine3,
            value = Some(addressLine3)
          ),
          FieldValidationFailure(
            field =
              SubcontractorValidationField.Postcode,
            value = Some(postcode)
          ),
          FieldValidationFailure(
            field =
              SubcontractorValidationField.Country,
            value = Some(country)
          )
        )
    }
  }

  private def createAddress(
                             addressLine1: String,
                             addressLine2: Option[String] = None,
                             addressLine3: Option[String] = None,
                             addressLine4: Option[String] = None,
                             postcode: Option[String] = None,
                             country: Option[Country] = None
                           ): Address =
    Address(
      addressLine1 = addressLine1,
      addressLine2 = addressLine2,
      addressLine3 = addressLine3,
      addressLine4 = addressLine4,
      addressLine5 = None,
      postcode = postcode,
      country = country,
      addressValidated = false
    )
}
