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

import models.SubcontractorCurrentVerification
import models.validation.{FieldValidationFailure, SubcontractorValidationField}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class UtrValidatorSpec extends AnyWordSpec with Matchers {

  private val allSubcontractors = Seq(subcontractorInvalid, subcontractorEmpty)

  "UtrValidator - validate UTR " must {

    "return no failure when the UTR is missing" in {
      UtrValidator
        .validate(None, allSubcontractors) mustBe None
    }

    "return no failure when the UTR is empty" in {
      UtrValidator
        .validate(Some(""), allSubcontractors) mustBe None
    }

    "return no failure when the UTR contains only whitespace" in {
      UtrValidator
        .validate(Some("   "), allSubcontractors) mustBe None
    }

    "return no failure for a valid UTR - 5860920998" in {
      UtrValidator
        .validate(
          Some("5860920998"),
          allSubcontractors
        ) mustBe None
    }

    "return failure for a valid UTR - 5860920998 which is used by more than one subcontractor" in {
      val allSubcontractors = Seq(subcontractorInvalid, subcontractorEmpty, subcontractorDupUtr, subcontractorValid)
      UtrValidator
        .validate(
          Some("5860920998"),
          allSubcontractors
        ) mustBe Some(
        FieldValidationFailure(
          field = SubcontractorValidationField.Utr,
          value = Some("5860920998")
        )
      )
    }

    "return a failure when the UTR exceeds the maximum length" in {
      val utr = "1234567890234"

      UtrValidator
        .validate(
          Some(utr),
          allSubcontractors
        ) mustBe
        Some(
          FieldValidationFailure(
            field = SubcontractorValidationField.Utr,
            value = Some(utr)
          )
        )
    }

    "return a failure when the UTR contains incorrect format" in {
      val utr = "12345A7890"

      UtrValidator
        .validate(
          Some(utr),
          allSubcontractors
        ) mustBe
        Some(
          FieldValidationFailure(
            field = SubcontractorValidationField.Utr,
            value = Some(utr)
          )
        )
    }

    "retain the original invalid UTR in the failure" in {
      val utr =
        "invalid-number"

      UtrValidator
        .validate(
          Some(utr),
          allSubcontractors
        ) mustBe
        Some(
          FieldValidationFailure(
            field = SubcontractorValidationField.Utr,
            value = Some(utr)
          )
        )
    }

    "return a partner UTR format failure without treating it as a duplicate of another UTR" in {
      UtrValidator
        .validate(
          value = Some("12345A7890"),
          subcontractors = Seq(subcontractorValid, subcontractorDupUtr),
          field = SubcontractorValidationField.PartnerUtr,
          checkDuplicate = false
        ) mustBe
        Some(
          FieldValidationFailure(
            field = SubcontractorValidationField.PartnerUtr,
            value = Some("12345A7890")
          )
        )
    }

    "return no failure for a valid partner UTR that matches another subcontractor UTR when duplicate checks are off" in {
      UtrValidator
        .validate(
          value = Some("5860920998"),
          subcontractors = Seq(subcontractorValid, subcontractorDupUtr),
          field = SubcontractorValidationField.PartnerUtr,
          checkDuplicate = false
        ) mustBe None
    }
  }

  private def subcontractorEmpty: SubcontractorCurrentVerification =
    SubcontractorCurrentVerification(
      subcontractorId = 1L,
      subbieResourceRef = Some(
        1L * 10
      ),
      firstName = Some("John"),
      secondName = None,
      surname = Some("Smith"),
      tradingName = Some("Trading Name"),
      utr = Some("1234567890"),
      nino = None,
      crn = None,
      partnerUtr = None,
      partnershipTradingName = None,
      subcontractorType = Some("trust"),
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

  private def subcontractorInvalid: SubcontractorCurrentVerification =
    SubcontractorCurrentVerification(
      subcontractorId = 2L,
      subbieResourceRef = Some(
        2L * 10
      ),
      firstName = Some("John"),
      secondName = None,
      surname = Some("Smith"),
      tradingName = Some("12345678901234567890123456789012345678901234567890<>"),
      utr = Some("12345A7890"),
      nino = None,
      crn = Some("invalid-number"),
      partnerUtr = None,
      partnershipTradingName = None,
      subcontractorType = Some("trust"),
      addressLine1 = Some("1 High Street"),
      addressLine2 = Some("Newcastle"),
      addressLine3 = None,
      addressLine4 = None,
      country = Some("GB"),
      postcode = Some("NE1 1AA"),
      emailAddress = Some("subcontractor@example.com"),
      phoneNumber = Some("0191 123 4567"),
      mobilePhoneNumber = Some("07700 900123"),
      worksReferenceNumber = Some("A12323452345#@[]{}$%^&£~"),
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

  private def subcontractorValid: SubcontractorCurrentVerification =
    SubcontractorCurrentVerification(
      subcontractorId = 3L,
      subbieResourceRef = Some(
        3L * 10
      ),
      firstName = Some("John"),
      secondName = None,
      surname = Some("Smith"),
      tradingName = Some("Trading Name"),
      utr = Some("5860920998"),
      nino = None,
      crn = None,
      partnerUtr = None,
      partnershipTradingName = None,
      subcontractorType = Some("trust"),
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

  private def subcontractorDupUtr: SubcontractorCurrentVerification =
    SubcontractorCurrentVerification(
      subcontractorId = 4L,
      subbieResourceRef = Some(
        4L * 10
      ),
      firstName = Some("John"),
      secondName = None,
      surname = Some("Smith"),
      tradingName = Some("12345678901234567890123456789012345678901234567890<>"),
      utr = Some("5860920998"),
      nino = None,
      crn = Some("invalid-number"),
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
      worksReferenceNumber = Some("A12323452345#@[]{}$%^&£~"),
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
