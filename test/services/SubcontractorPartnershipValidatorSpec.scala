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
import models.validation.SubcontractorValidationField.{Nino, PartnerUtr, PartnershipTradingName, TradingName, Utr, WorksReferenceNumber}

class SubcontractorPartnershipValidatorSpec extends SpecBase {

  private val validator =
    new SubcontractorPartnershipValidator()

  "SubcontractorPartnershipValidator.validate" - {

    "return no failures for an empty subcontractor list" in {
      validator.validate(Seq.empty) mustBe Nil
    }

    "exclude a partnership when all F5 fields are valid" in {
      validator.validate(Seq(partnership(1L))) mustBe Nil
    }

    "ignore non-partnership subcontractors" in {
      validator.validate(
        Seq(
          partnership(1L).copy(
            subcontractorType = Some("company"),
            partnershipTradingName = None,
            tradingName = None
          )
        )
      ) mustBe Nil
    }

    "return a partnership containing invalid F5 fields" in {
      val invalidName =
        "12345678901234567890123456789012345678901234567890<>"
      val invalidWrn  =
        "A12323452345#@[]{}$%^&£~"

      val result =
        validator.validate(
          Seq(
            partnership(1L).copy(
              partnershipTradingName = Some(invalidName),
              tradingName = Some(invalidName),
              utr = Some("1234567890"),
              partnerUtr = Some("12345A7890"),
              nino = Some("DA123456A"),
              worksReferenceNumber = Some(invalidWrn)
            )
          )
        )

      result.head.subcontractorId mustBe 1L
      result.head.failedFields.map(_.field) must contain allOf (
        PartnershipTradingName,
        Utr,
        TradingName,
        PartnerUtr,
        Nino,
        WorksReferenceNumber
      )
    }

    "return a UTR failure when another subcontractor already uses the same UTR" in {
      val result =
        validator.validate(
          Seq(
            partnership(1L),
            partnership(2L).copy(
              subcontractorType = Some("company"),
              partnershipTradingName = None,
              tradingName = Some("ACME Ltd")
            )
          )
        )

      result mustBe
        List(
          SubcontractorValidationFailure(
            subcontractorId = 1L,
            failedFields = List(
              FieldValidationFailure(
                field = Utr,
                value = Some("5860920998")
              )
            )
          )
        )
    }
  }

  private def partnership(
    subcontractorId: Long
  ): SubcontractorCurrentVerification =
    SubcontractorCurrentVerification(
      subcontractorId = subcontractorId,
      subbieResourceRef = Some(subcontractorId * 10),
      firstName = None,
      secondName = None,
      surname = None,
      tradingName = Some("Jane Smith"),
      utr = Some("5860920998"),
      nino = Some("AA123456A"),
      crn = Some("AB5860"),
      partnerUtr = Some("2222222222"),
      partnershipTradingName = Some("Smith & Partners"),
      subcontractorType = Some("partnership"),
      addressLine1 = Some("1 High Street"),
      addressLine2 = Some("Newcastle"),
      addressLine3 = None,
      addressLine4 = None,
      country = Some("GB"),
      postcode = Some("NE1 1AA"),
      emailAddress = Some("subcontractor@example.com"),
      phoneNumber = Some("0191 123 4567"),
      mobilePhoneNumber = Some("07700 900123"),
      worksReferenceNumber = Some("Work Ref No 1234@"),
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
