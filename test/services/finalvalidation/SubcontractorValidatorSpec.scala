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

package services.finalvalidation

import base.SpecBase
import models.SubcontractorCurrentVerification
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar.mock
import services.*

class SubcontractorValidatorSpec extends SpecBase {

  "SubcontractorValidator" - {

    "validate" - {

      "must validate subcontractors using all validators" in {

        val detailsValidator     = mock[SubcontractorDetailsValidator]
        val companyValidator     = mock[SubcontractorCompanyValidator]
        val partnershipValidator = mock[SubcontractorPartnershipValidator]
        val individualValidator  = mock[SubcontractorIndividualValidator]
        val trustValidator       = mock[SubcontractorTrustValidator]

        val subcontractors = Seq.empty[SubcontractorCurrentVerification]

        when(detailsValidator.validate(subcontractors)).thenReturn(List.empty)
        when(companyValidator.validate(subcontractors)).thenReturn(List.empty)
        when(partnershipValidator.validate(subcontractors)).thenReturn(List.empty)
        when(individualValidator.validate(subcontractors)).thenReturn(List.empty)
        when(trustValidator.validate(subcontractors)).thenReturn(List.empty)

        val validator =
          new SubcontractorValidator(
            detailsValidator,
            companyValidator,
            partnershipValidator,
            individualValidator,
            trustValidator
          )

        validator.validate(subcontractors) mustBe List.empty

        verify(detailsValidator).validate(subcontractors)
        verify(companyValidator).validate(subcontractors)
        verify(partnershipValidator).validate(subcontractors)
        verify(individualValidator).validate(subcontractors)
        verify(trustValidator).validate(subcontractors)
      }

      "must throw when a subcontractor has an invalid subcontractor type" in {

        val detailsValidator     = mock[SubcontractorDetailsValidator]
        val companyValidator     = mock[SubcontractorCompanyValidator]
        val partnershipValidator = mock[SubcontractorPartnershipValidator]
        val individualValidator  = mock[SubcontractorIndividualValidator]
        val trustValidator       = mock[SubcontractorTrustValidator]

        val validator =
          new SubcontractorValidator(
            detailsValidator,
            companyValidator,
            partnershipValidator,
            individualValidator,
            trustValidator
          )

        val subcontractor =
          SubcontractorCurrentVerification(
            subcontractorId = 1L,
            subbieResourceRef = None,
            firstName = None,
            secondName = None,
            surname = None,
            tradingName = None,
            utr = None,
            nino = None,
            crn = None,
            partnerUtr = None,
            partnershipTradingName = None,
            subcontractorType = Some("invalid"),
            addressLine1 = None,
            addressLine2 = None,
            addressLine3 = None,
            addressLine4 = None,
            country = None,
            postcode = None,
            emailAddress = None,
            phoneNumber = None,
            mobilePhoneNumber = None,
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

        val exception =
          intercept[IllegalArgumentException] {
            validator.validate(Seq(subcontractor))
          }

        exception.getMessage mustBe
          "Invalid subcontractor type: Some(invalid)"
      }

      "must throw when a subcontractor type is missing" in {

        val detailsValidator     = mock[SubcontractorDetailsValidator]
        val companyValidator     = mock[SubcontractorCompanyValidator]
        val partnershipValidator = mock[SubcontractorPartnershipValidator]
        val individualValidator  = mock[SubcontractorIndividualValidator]
        val trustValidator       = mock[SubcontractorTrustValidator]

        val validator =
          new SubcontractorValidator(
            detailsValidator,
            companyValidator,
            partnershipValidator,
            individualValidator,
            trustValidator
          )

        val subcontractor =
          SubcontractorCurrentVerification(
            subcontractorId = 1L,
            subbieResourceRef = None,
            firstName = None,
            secondName = None,
            surname = None,
            tradingName = None,
            utr = None,
            nino = None,
            crn = None,
            partnerUtr = None,
            partnershipTradingName = None,
            subcontractorType = None,
            addressLine1 = None,
            addressLine2 = None,
            addressLine3 = None,
            addressLine4 = None,
            country = None,
            postcode = None,
            emailAddress = None,
            phoneNumber = None,
            mobilePhoneNumber = None,
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

        val exception =
          intercept[IllegalArgumentException] {
            validator.validate(Seq(subcontractor))
          }

        exception.getMessage mustBe
          "Invalid subcontractor type: None"
      }
    }

    "validateFields" - {

      "must return an empty map when there are no validation failures" in {

        val detailsValidator     = mock[SubcontractorDetailsValidator]
        val companyValidator     = mock[SubcontractorCompanyValidator]
        val partnershipValidator = mock[SubcontractorPartnershipValidator]
        val individualValidator  = mock[SubcontractorIndividualValidator]
        val trustValidator       = mock[SubcontractorTrustValidator]

        val subcontractors = Seq.empty[SubcontractorCurrentVerification]

        when(detailsValidator.validate(subcontractors)).thenReturn(List.empty)
        when(companyValidator.validate(subcontractors)).thenReturn(List.empty)
        when(partnershipValidator.validate(subcontractors)).thenReturn(List.empty)
        when(individualValidator.validate(subcontractors)).thenReturn(List.empty)
        when(trustValidator.validate(subcontractors)).thenReturn(List.empty)

        val validator =
          new SubcontractorValidator(
            detailsValidator,
            companyValidator,
            partnershipValidator,
            individualValidator,
            trustValidator
          )

        validator.validateFields(subcontractors) mustBe Map.empty
      }
    }

    "validateFieldsFor" - {

      "must return an empty sequence when the subcontractor has no validation failures" in {

        val detailsValidator     = mock[SubcontractorDetailsValidator]
        val companyValidator     = mock[SubcontractorCompanyValidator]
        val partnershipValidator = mock[SubcontractorPartnershipValidator]
        val individualValidator  = mock[SubcontractorIndividualValidator]
        val trustValidator       = mock[SubcontractorTrustValidator]

        val subcontractors = Seq.empty[SubcontractorCurrentVerification]

        when(detailsValidator.validate(subcontractors)).thenReturn(List.empty)
        when(companyValidator.validate(subcontractors)).thenReturn(List.empty)
        when(partnershipValidator.validate(subcontractors)).thenReturn(List.empty)
        when(individualValidator.validate(subcontractors)).thenReturn(List.empty)
        when(trustValidator.validate(subcontractors)).thenReturn(List.empty)

        val validator =
          new SubcontractorValidator(
            detailsValidator,
            companyValidator,
            partnershipValidator,
            individualValidator,
            trustValidator
          )

        validator.validateFieldsFor(
          1L,
          subcontractors
        ) mustBe Seq.empty
      }
    }
  }
}
