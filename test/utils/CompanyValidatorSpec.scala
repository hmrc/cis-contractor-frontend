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

import models.validation.{FieldValidationFailure, SubcontractorValidationField}
import models.SubcontractorCurrentVerification
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class CompanyValidatorSpec extends AnyWordSpec with Matchers {

  "CompanyValidator.validate" must {

    "return no failures when all common details are missing" in {
      CompanyValidator.validate(
        subcontactorToValidate = subcontractorEmpty,
        allSubcontractors = Seq(subcontractorInvalid, subcontractorEmpty, subcontractorValid)
      ) mustBe Nil
    }

    "return no failures for when all valid" in {

      CompanyValidator.validate(
        subcontactorToValidate = subcontractorValid,
        allSubcontractors = Seq(subcontractorInvalid, subcontractorEmpty, subcontractorValid)
      ) mustBe Nil
    }

    "return every failure" in {

      CompanyValidator.validate(
        subcontactorToValidate = subcontractorInvalid,
        allSubcontractors = Seq(subcontractorInvalid, subcontractorEmpty)
      ) mustBe
        List(
          FieldValidationFailure(
            field = SubcontractorValidationField.Crn,
            value = subcontractorInvalid.crn
          ),
          FieldValidationFailure(
            field = SubcontractorValidationField.WorksReferenceNumber,
            value = subcontractorInvalid.worksReferenceNumber
          ),
          FieldValidationFailure(
            field = SubcontractorValidationField.Utr,
            value = subcontractorInvalid.utr
          ),
          FieldValidationFailure(
            field = SubcontractorValidationField.TradingName,
            value = subcontractorInvalid.tradingName
          )
        )
    }

    "retain valid fields while returning only invalid fields (wrn & crn)" in {

      CompanyValidator.validate(
        subcontactorToValidate = subcontractorSomeValid,
        allSubcontractors = Seq(subcontractorSomeValid, subcontractorInvalid, subcontractorEmpty)
      ) mustBe
        List(
          FieldValidationFailure(
            field = SubcontractorValidationField.Crn,
            value = subcontractorSomeValid.crn
          ),
          FieldValidationFailure(
            field = SubcontractorValidationField.WorksReferenceNumber,
            value = subcontractorSomeValid.worksReferenceNumber
          )
        )
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
      utr = None,
      nino = None,
      crn = None,
      partnerUtr = None,
      partnershipTradingName = None,
      subcontractorType = Some("company"),
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

  private def subcontractorValid: SubcontractorCurrentVerification =
    SubcontractorCurrentVerification(
      subcontractorId = 1L,
      subbieResourceRef = Some(
        1L * 10
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
      subcontractorType = Some("company"),
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

  private def subcontractorSomeValid: SubcontractorCurrentVerification =
    SubcontractorCurrentVerification(
      subcontractorId = 1L,
      subbieResourceRef = Some(
        1L * 10
      ),
      firstName = Some("John"),
      secondName = None,
      surname = Some("Smith"),
      tradingName = Some("Test Trading Name 1234@"),
      utr = Some("5860920998"),
      nino = None,
      crn = Some("ABC5860"),
      partnerUtr = None,
      partnershipTradingName = None,
      subcontractorType = Some("company"),
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
      subcontractorType = Some("company"),
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
