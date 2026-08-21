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

package utils

import models.address.Address
import models.validation.{
  FieldValidationFailure,
  SubcontractorValidationField
}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class CommonDetailsValidatorSpec
  extends AnyWordSpec
    with Matchers {

  "CommonDetailsValidator.validate" must {

    "return no failures when all common details are missing" in {
      CommonDetailsValidator.validate(
        emailAddress = None,
        phoneNumber = None,
        mobilePhoneNumber = None,
        address = None
      ) mustBe Nil
    }

    "return no failures when all common details are valid" in {
      val address =
        Address(
          addressLine1 = "1 High Street",
          addressLine2 = Some("Newcastle"),
          addressLine3 = None,
          addressLine4 = None,
          addressLine5 = None,
          postcode = Some("NE1 1AA"),
          country = None,
          addressValidated = false
        )

      CommonDetailsValidator.validate(
        emailAddress =
          Some("subcontractor@example.com"),
        phoneNumber =
          Some("0191 123 4567"),
        mobilePhoneNumber =
          Some("07700 900123"),
        address = Some(address)
      ) mustBe Nil
    }

    "return the email failure" in {
      val emailAddress =
        "invalid-email"

      CommonDetailsValidator.validate(
        emailAddress = Some(emailAddress),
        phoneNumber = None,
        mobilePhoneNumber = None,
        address = None
      ) mustBe
        List(
          FieldValidationFailure(
            field =
              SubcontractorValidationField.EmailAddress,
            value = Some(emailAddress)
          )
        )
    }

    "return every common-details failure in field order" in {
      val emailAddress =
        "invalid-email"

      val phoneNumber =
        "0191 ABC 4567"

      val mobilePhoneNumber =
        "07700 MOBILE"

      val addressLine2 =
        "-Newcastle"

      val postcode =
        "ABCDEFGHI"

      val address =
        Address(
          addressLine1 = "",
          addressLine2 = Some(addressLine2),
          addressLine3 = None,
          addressLine4 = None,
          addressLine5 = None,
          postcode = Some(postcode),
          country = None,
          addressValidated = false
        )

      CommonDetailsValidator.validate(
        emailAddress = Some(emailAddress),
        phoneNumber = Some(phoneNumber),
        mobilePhoneNumber =
          Some(mobilePhoneNumber),
        address = Some(address)
      ) mustBe
        List(
          FieldValidationFailure(
            field =
              SubcontractorValidationField.EmailAddress,
            value = Some(emailAddress)
          ),
          FieldValidationFailure(
            field =
              SubcontractorValidationField.PhoneNumber,
            value = Some(phoneNumber)
          ),
          FieldValidationFailure(
            field =
              SubcontractorValidationField.MobilePhoneNumber,
            value = Some(mobilePhoneNumber)
          ),
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
              SubcontractorValidationField.Postcode,
            value = Some(postcode)
          )
        )
    }

    "retain valid fields while returning only invalid fields" in {
      val invalidPhoneNumber =
        "0191 ABC 4567"

      CommonDetailsValidator.validate(
        emailAddress =
          Some("subcontractor@example.com"),
        phoneNumber =
          Some(invalidPhoneNumber),
        mobilePhoneNumber =
          Some("07700 900123"),
        address = None
      ) mustBe
        List(
          FieldValidationFailure(
            field =
              SubcontractorValidationField.PhoneNumber,
            value = Some(invalidPhoneNumber)
          )
        )
    }
  }
}
