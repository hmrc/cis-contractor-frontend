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

package models.finalvalidation

import base.SpecBase
import models.response.SubcontractorResponse
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock

class FinalValidationDraftRequestBuilderSpec extends SpecBase {

  "FinalValidationDraftRequestBuilder" - {

    "must build a CreateFinalValidationDraftRequest from validation failures" in {

      val subcontractor = mock[SubcontractorResponse]

      when(subcontractor.subcontractorId).thenReturn(1L)
      when(subcontractor.subbieResourceRef).thenReturn(Some(100L))
      when(subcontractor.version).thenReturn(Some(2))
      when(subcontractor.subcontractorType).thenReturn(Some("Individual"))
      when(subcontractor.displayName).thenReturn("John Smith")
      when(subcontractor.firstName).thenReturn(Some("John"))
      when(subcontractor.secondName).thenReturn(Some("Paul"))
      when(subcontractor.surname).thenReturn(Some("Smith"))
      when(subcontractor.partnershipTradingName).thenReturn(None)
      when(subcontractor.tradingName).thenReturn(Some("Smith Trading"))
      when(subcontractor.addressLine1).thenReturn(Some("1 Test Street"))
      when(subcontractor.addressLine2).thenReturn(Some("Test Area"))
      when(subcontractor.addressLine3).thenReturn(Some("Test Town"))
      when(subcontractor.addressLine4).thenReturn(Some("Test County"))
      when(subcontractor.country).thenReturn(Some("GB"))
      when(subcontractor.postcode).thenReturn(Some("AA1 1AA"))
      when(subcontractor.emailAddress).thenReturn(Some("john@example.com"))
      when(subcontractor.phoneNumber).thenReturn(Some("01234567890"))
      when(subcontractor.mobilePhoneNumber).thenReturn(Some("07123456789"))
      when(subcontractor.utr).thenReturn(Some("1234567890"))
      when(subcontractor.partnerUtr).thenReturn(None)
      when(subcontractor.nino).thenReturn(Some("AB123456C"))
      when(subcontractor.crn).thenReturn(None)
      when(subcontractor.worksReferenceNumber).thenReturn(Some("WRN123"))

      val issue =
        FinalValidationIssue(
          field = FinalValidationField.Utr,
          value = Some("1234567890")
        )

      val failure =
        SubcontractorFinalValidationFailure(
          subcontractorId = 1L,
          issues = Seq(issue),
          subbieResourceRef = Some(100L)
        )

      val validation =
        VerifyFinalValidationResult(
          subcontractors = Seq(subcontractor),
          failures = Seq(failure)
        )

      val result =
        new FinalValidationDraftRequestBuilder()
          .build("instance-1", validation)
          .success
          .value

      result mustBe CreateFinalValidationDraftRequest(
        instanceId = "instance-1",
        context = "VerifySubcontractor",
        subcontractors = Seq(
          CreateFinalValidationDraftSubcontractor(
            subcontractorId = 1L,
            subbieResourceRef = 100L,
            baseVersion = Some(2),
            subcontractorType = Some("Individual"),
            displayName = "John Smith",
            details = FinalValidationSubcontractorDetails(
              firstName = Some("John"),
              secondName = Some("Paul"),
              surname = Some("Smith"),
              partnershipTradingName = None,
              tradingName = Some("Smith Trading"),
              addressLine1 = Some("1 Test Street"),
              addressLine2 = Some("Test Area"),
              addressLine3 = Some("Test Town"),
              addressLine4 = Some("Test County"),
              country = Some("GB"),
              postcode = Some("AA1 1AA"),
              emailAddress = Some("john@example.com"),
              phoneNumber = Some("01234567890"),
              mobilePhoneNumber = Some("07123456789"),
              utr = Some("1234567890"),
              partnerUtr = None,
              nino = Some("AB123456C"),
              crn = None,
              worksReferenceNumber = Some("WRN123")
            ),
            issues = Seq(
              FinalValidationDraftIssue(
                fieldKey = "utr",
                value = Some("1234567890")
              )
            )
          )
        )
      )
    }

    "must build a request with no subcontractors when there are no failures" in {

      val validation =
        VerifyFinalValidationResult(
          subcontractors = Seq.empty,
          failures = Seq.empty
        )

      val result =
        new FinalValidationDraftRequestBuilder()
          .build("instance-1", validation)
          .success
          .value

      result mustBe CreateFinalValidationDraftRequest(
        instanceId = "instance-1",
        context = "VerifySubcontractor",
        subcontractors = Seq.empty
      )
    }

    "must fail when a failed subcontractor cannot be found" in {

      val failure =
        SubcontractorFinalValidationFailure(
          subcontractorId = 1L,
          issues = Seq.empty,
          subbieResourceRef = Some(100L)
        )

      val validation =
        VerifyFinalValidationResult(
          subcontractors = Seq.empty,
          failures = Seq(failure)
        )

      val result =
        new FinalValidationDraftRequestBuilder()
          .build("instance-1", validation)

      result.failed.get.getMessage mustBe
        "Subcontractor 1 not found"
    }

    "must fail when a subcontractor has no subbieResourceRef" in {

      val subcontractor = mock[SubcontractorResponse]

      when(subcontractor.subcontractorId).thenReturn(1L)
      when(subcontractor.subbieResourceRef).thenReturn(None)

      val failure =
        SubcontractorFinalValidationFailure(
          subcontractorId = 1L,
          issues = Seq.empty,
          subbieResourceRef = None
        )

      val validation =
        VerifyFinalValidationResult(
          subcontractors = Seq(subcontractor),
          failures = Seq(failure)
        )

      val result =
        new FinalValidationDraftRequestBuilder()
          .build("instance-1", validation)

      result.failed.get.getMessage mustBe
        "Missing subbieResourceRef for subcontractor 1"
    }
  }
}
