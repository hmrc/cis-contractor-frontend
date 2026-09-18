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

package services

import base.SpecBase
import models.SubcontractorCurrentVerification
import models.validation.{FieldValidationFailure, SubcontractorValidationFailure}
import models.validation.SubcontractorValidationField.{AddressLine1, Country, EmailAddress, PhoneNumber}

class SubcontractorDetailsValidatorSpec extends SpecBase {

  private val validator =
    new SubcontractorDetailsValidator()

  "SubcontractorDetailsValidator.validate" - {

    "return no failures for an empty subcontractor list" in {
      validator.validate(Seq.empty) mustBe Nil
    }

    "exclude a subcontractor when all common details are valid" in {
      validator.validate(
        Seq(subcontractor(1L))
      ) mustBe Nil
    }

    "return a subcontractor containing an invalid email address" in {
      val invalidEmail =
        "invalid-email"

      val result =
        validator.validate(
          Seq(
            subcontractor(1L).copy(
              emailAddress = Some(invalidEmail)
            )
          )
        )

      result mustBe
        List(
          SubcontractorValidationFailure(
            subcontractorId = 1L,
            failedFields = List(
              FieldValidationFailure(
                field = EmailAddress,
                value = Some(invalidEmail)
              )
            )
          )
        )
    }

    "convert a missing address line 1 to an empty string and return its failure" in {
      val result =
        validator.validate(
          Seq(
            subcontractor(2L).copy(
              addressLine1 = None,
              addressLine2 = Some("Newcastle"),
              addressLine3 = None,
              addressLine4 = None,
              postcode = None,
              country = None
            )
          )
        )

      result mustBe
        List(
          SubcontractorValidationFailure(
            subcontractorId = 2L,
            failedFields = List(
              FieldValidationFailure(
                field = AddressLine1,
                value = None
              )
            )
          )
        )
    }

    "return no address failure when every address field is missing" in {
      val result =
        validator.validate(
          Seq(
            subcontractor(3L).copy(
              addressLine1 = None,
              addressLine2 = None,
              addressLine3 = None,
              addressLine4 = None,
              postcode = None,
              country = None
            )
          )
        )

      result mustBe Nil
    }

    "map and validate the flat country field" in {
      val invalidCountry =
        "-GB"

      val result =
        validator.validate(
          Seq(
            subcontractor(4L).copy(
              country = Some(invalidCountry)
            )
          )
        )

      result mustBe
        List(
          SubcontractorValidationFailure(
            subcontractorId = 4L,
            failedFields = List(
              FieldValidationFailure(
                field = Country,
                value = Some(invalidCountry)
              )
            )
          )
        )
    }

    "return only subcontractors containing failures and preserve their order" in {
      val invalidPhone =
        "0191 PHONE"

      val invalidEmail =
        "invalid-email"

      val result =
        validator.validate(
          Seq(
            subcontractor(1L),
            subcontractor(2L).copy(
              phoneNumber = Some(invalidPhone)
            ),
            subcontractor(3L),
            subcontractor(4L).copy(
              emailAddress = Some(invalidEmail)
            )
          )
        )

      result mustBe
        List(
          SubcontractorValidationFailure(
            subcontractorId = 2L,
            failedFields = List(
              FieldValidationFailure(
                field = PhoneNumber,
                value = Some(invalidPhone)
              )
            )
          ),
          SubcontractorValidationFailure(
            subcontractorId = 4L,
            failedFields = List(
              FieldValidationFailure(
                field = EmailAddress,
                value = Some(invalidEmail)
              )
            )
          )
        )
    }
  }

  private def subcontractor(
    subcontractorId: Long
  ): SubcontractorCurrentVerification =
    SubcontractorCurrentVerification(
      subcontractorId = subcontractorId,
      subbieResourceRef = Some(
        subcontractorId * 10
      ),
      firstName = Some("John"),
      secondName = None,
      surname = Some("Smith"),
      tradingName = None,
      utr = Some("1234567890"),
      nino = Some("AA123456A"),
      crn = None,
      partnerUtr = None,
      partnershipTradingName = None,
      subcontractorType = Some("soletrader"),
      addressLine1 = Some("1 High Street"),
      addressLine2 = Some("Newcastle"),
      addressLine3 = None,
      addressLine4 = None,
      country = Some("GB"),
      postcode = Some("NE1 1AA"),
      emailAddress = Some("subcontractor@example.com"),
      phoneNumber = Some("0191 123 4567"),
      mobilePhoneNumber = Some("07700 900123"),
      worksReferenceNumber = None,
      matched = None,
      autoVerified = None,
      verified = None,
      verificationNumber = None,
      taxTreatment = None,
      verificationDate = None,
      version = None,
      updatedTaxTreatment = None,
      lastMonthlyReturnDate = None,
      pendingVerifications = None
    )
}
