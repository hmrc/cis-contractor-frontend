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

class PartnershipValidatorSpec extends AnyWordSpec with Matchers {

  "PartnershipValidator.validate" must {

    "return no failures when optional fields are missing and both names are valid" in {
      PartnershipValidator.validate(
        subcontractorToValidate = subcontractorOptionalEmpty,
        allSubcontractors = Seq(subcontractorOptionalEmpty)
      ) mustBe Nil
    }

    "return no failures when all fields are valid" in {
      PartnershipValidator.validate(
        subcontractorToValidate = subcontractorValid,
        allSubcontractors = Seq(subcontractorInvalid, subcontractorValid)
      ) mustBe Nil
    }

    "return every failure" in {
      PartnershipValidator.validate(
        subcontractorToValidate = subcontractorInvalid,
        allSubcontractors = Seq(subcontractorInvalid)
      ) mustBe
        List(
          FieldValidationFailure(
            field = SubcontractorValidationField.PartnershipTradingName,
            value = subcontractorInvalid.partnershipTradingName
          ),
          FieldValidationFailure(
            field = SubcontractorValidationField.Utr,
            value = subcontractorInvalid.utr
          ),
          FieldValidationFailure(
            field = SubcontractorValidationField.TradingName,
            value = subcontractorInvalid.tradingName
          ),
          FieldValidationFailure(
            field = SubcontractorValidationField.PartnerUtr,
            value = subcontractorInvalid.partnerUtr
          ),
          FieldValidationFailure(
            field = SubcontractorValidationField.Nino,
            value = subcontractorInvalid.nino
          ),
          FieldValidationFailure(
            field = SubcontractorValidationField.Crn,
            value = subcontractorInvalid.crn
          ),
          FieldValidationFailure(
            field = SubcontractorValidationField.WorksReferenceNumber,
            value = subcontractorInvalid.worksReferenceNumber
          )
        )
    }

    "return failures when both names are missing" in {
      PartnershipValidator.validate(
        subcontractorToValidate = subcontractorMissingNames,
        allSubcontractors = Seq(subcontractorMissingNames)
      ) mustBe
        List(
          FieldValidationFailure(
            field = SubcontractorValidationField.PartnershipTradingName,
            value = None
          ),
          FieldValidationFailure(
            field = SubcontractorValidationField.TradingName,
            value = None
          )
        )
    }

    "return a UTR failure when the same UTR is used by another subcontractor" in {
      PartnershipValidator.validate(
        subcontractorToValidate = subcontractorValid,
        allSubcontractors = Seq(subcontractorValid, companyWithSameUtr)
      ) mustBe
        List(
          FieldValidationFailure(
            field = SubcontractorValidationField.Utr,
            value = subcontractorValid.utr
          )
        )
    }
  }

  private def subcontractorOptionalEmpty: SubcontractorCurrentVerification =
    baseSubcontractor(
      tradingName = Some("Jane Smith"),
      partnershipTradingName = Some("Smith & Partners")
    )

  private def subcontractorValid: SubcontractorCurrentVerification =
    baseSubcontractor(
      tradingName = Some("Jane Smith"),
      utr = Some("5860920998"),
      nino = Some("AA123456A"),
      crn = Some("AB5860"),
      partnerUtr = Some("2222222222"),
      partnershipTradingName = Some("Smith & Partners"),
      worksReferenceNumber = Some("Work Ref No 1234@")
    )

  private def subcontractorInvalid: SubcontractorCurrentVerification =
    baseSubcontractor(
      subcontractorId = 2L,
      tradingName = Some("12345678901234567890123456789012345678901234567890<>"),
      utr = Some("12345A7890"),
      nino = Some("DA123456A"),
      crn = Some("ABC5860"),
      partnerUtr = Some("12345A7890"),
      partnershipTradingName = Some("12345678901234567890123456789012345678901234567890<>"),
      worksReferenceNumber = Some("A12323452345#@[]{}$%^&£~")
    )

  private def subcontractorMissingNames: SubcontractorCurrentVerification =
    baseSubcontractor()

  private def companyWithSameUtr: SubcontractorCurrentVerification =
    baseSubcontractor(
      subcontractorId = 3L,
      tradingName = Some("ACME Ltd"),
      utr = Some("5860920998"),
      subcontractorType = Some("company")
    )

  private def baseSubcontractor(
    subcontractorId: Long = 1L,
    tradingName: Option[String] = None,
    utr: Option[String] = None,
    nino: Option[String] = None,
    crn: Option[String] = None,
    partnerUtr: Option[String] = None,
    partnershipTradingName: Option[String] = None,
    subcontractorType: Option[String] = Some("partnership"),
    worksReferenceNumber: Option[String] = None
  ): SubcontractorCurrentVerification =
    SubcontractorCurrentVerification(
      subcontractorId = subcontractorId,
      subbieResourceRef = Some(subcontractorId * 10),
      firstName = None,
      secondName = None,
      surname = None,
      tradingName = tradingName,
      utr = utr,
      nino = nino,
      crn = crn,
      partnerUtr = partnerUtr,
      partnershipTradingName = partnershipTradingName,
      subcontractorType = subcontractorType,
      addressLine1 = Some("1 High Street"),
      addressLine2 = Some("Newcastle"),
      addressLine3 = None,
      addressLine4 = None,
      country = Some("GB"),
      postcode = Some("NE1 1AA"),
      emailAddress = Some("subcontractor@example.com"),
      phoneNumber = Some("0191 123 4567"),
      mobilePhoneNumber = Some("07700 900123"),
      worksReferenceNumber = worksReferenceNumber,
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
