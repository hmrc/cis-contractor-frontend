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
import models.{SubcontractorCurrentVerification, UserAnswers}
import models.finalvalidation.*
import models.finalvalidation.VerifyFinalValidationSource.*
import models.response.{GetSubcontractorResponse, SubcontractorResponse}
import models.validation.SubcontractorValidationField
import org.mockito.Mockito.{verify, verifyNoInteractions, when}
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.finalvalidation.VerifyFinalValidationSourcePage
import play.api.libs.json.{JsObject, Json}
import services.finalvalidation.SubcontractorValidator
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class VerifyFinalValidationServiceSpec extends SpecBase {

  implicit val ec: ExecutionContext = ExecutionContext.global
  implicit val hc: HeaderCarrier    = HeaderCarrier()

  private def service(
    subcontractorService: SubcontractorService,
    subcontractorValidator: SubcontractorValidator
  ): VerifyFinalValidationService =
    new VerifyFinalValidationService(
      subcontractorService,
      subcontractorValidator
    )

  private def subcontractorResponse(
    subcontractorId: Long = 1L,
    subbieResourceRef: Option[Long] = Some(10L)
  ): SubcontractorResponse =
    SubcontractorResponse(
      subcontractorId = subcontractorId,
      utr = Some("1234567890"),
      pageVisited = None,
      partnerUtr = None,
      crn = None,
      firstName = Some("John"),
      nino = Some("AB123456C"),
      secondName = Some("Paul"),
      surname = Some("Smith"),
      partnershipTradingName = None,
      tradingName = Some("Smith Construction"),
      subcontractorType = Some("soletrader"),
      addressLine1 = Some("1 High Street"),
      addressLine2 = None,
      addressLine3 = None,
      addressLine4 = None,
      country = Some("UK"),
      postcode = Some("AA1 1AA"),
      emailAddress = Some("john@example.com"),
      phoneNumber = Some("01234567890"),
      mobilePhoneNumber = Some("07123456789"),
      worksReferenceNumber = Some("WR123"),
      createDate = None,
      lastUpdate = None,
      subbieResourceRef = subbieResourceRef,
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

  private def currentVerification(
    subcontractor: SubcontractorResponse
  ): SubcontractorCurrentVerification =
    SubcontractorCurrentVerification(
      subcontractorId = subcontractor.subcontractorId,
      subbieResourceRef = subcontractor.subbieResourceRef,
      firstName = subcontractor.firstName,
      secondName = subcontractor.secondName,
      surname = subcontractor.surname,
      tradingName = subcontractor.tradingName,
      utr = subcontractor.utr,
      nino = subcontractor.nino,
      crn = subcontractor.crn,
      partnerUtr = subcontractor.partnerUtr,
      partnershipTradingName = subcontractor.partnershipTradingName,
      subcontractorType = subcontractor.subcontractorType,
      addressLine1 = subcontractor.addressLine1,
      addressLine2 = subcontractor.addressLine2,
      addressLine3 = subcontractor.addressLine3,
      addressLine4 = subcontractor.addressLine4,
      country = subcontractor.country,
      postcode = subcontractor.postcode,
      emailAddress = subcontractor.emailAddress,
      phoneNumber = subcontractor.phoneNumber,
      mobilePhoneNumber = subcontractor.mobilePhoneNumber,
      worksReferenceNumber = subcontractor.worksReferenceNumber,
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

  private def selectedUserAnswers(
    selectedId: String = "1",
    availableId: Long = 1L,
    subbieResourceRef: Option[Long] = Some(10L)
  ): UserAnswers = {

    val availableSubcontractor =
      subbieResourceRef match {
        case Some(resourceRef) =>
          Json.obj(
            "subcontractorId"   -> availableId,
            "subbieResourceRef" -> resourceRef
          )

        case None =>
          Json.obj(
            "subcontractorId" -> availableId
          )
      }

    UserAnswers(
      "id",
      Json.obj(
        "finalvalidations"         -> Json.obj(
          "verifyFinalValidationSource" -> "SelectSubcontractor"
        ),
        "selectSubcontractor"      -> Json.arr(
          Json.obj(
            "id"   -> selectedId,
            "name" -> "John Smith"
          )
        ),
        "unverifiedSubcontractors" -> Json.arr(
          availableSubcontractor
        )
      )
    )
  }

  private def draft(
    proposed: JsObject
  ): FinalValidationDraft =
    Json
      .obj(
        "subcontractors" -> Json.arr(
          Json.obj(
            "subcontractorId"   -> 1L,
            "subbieResourceRef" -> 10L,
            "baseVersion"       -> 1,
            "subcontractorType" -> "soletrader",
            "displayName"       -> "John Smith",
            "base"              -> Json.obj(),
            "proposed"          -> proposed,
            "changedTargets"    -> Json.arr(),
            "issues"            -> Json.arr(),
            "readiness"         -> "Incomplete"
          )
        )
      )
      .as[FinalValidationDraft]

  private def currentVerification(
    subcontractor: FinalValidationDraftSubcontractor
  ): SubcontractorCurrentVerification = {

    val proposed =
      subcontractor.proposed

    SubcontractorCurrentVerification(
      subcontractorId = subcontractor.subcontractorId,
      subbieResourceRef = None,
      firstName = proposed.firstName,
      secondName = proposed.secondName,
      surname = proposed.surname,
      tradingName = proposed.tradingName,
      utr = proposed.utr,
      nino = proposed.nino,
      crn = proposed.crn,
      partnerUtr = proposed.partnerUtr,
      partnershipTradingName = proposed.partnershipTradingName,
      subcontractorType = subcontractor.subcontractorType,
      addressLine1 = proposed.addressLine1,
      addressLine2 = proposed.addressLine2,
      addressLine3 = proposed.addressLine3,
      addressLine4 = proposed.addressLine4,
      country = proposed.country,
      postcode = proposed.postcode,
      emailAddress = proposed.emailAddress,
      phoneNumber = proposed.phoneNumber,
      mobilePhoneNumber = proposed.mobilePhoneNumber,
      worksReferenceNumber = proposed.worksReferenceNumber,
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

  "validate" - {

    "must return the selected subcontractors when there are no validation failures" in {

      val subcontractorService   = mock[SubcontractorService]
      val subcontractorValidator = mock[SubcontractorValidator]

      val finalValidationService =
        service(
          subcontractorService,
          subcontractorValidator
        )

      val subcontractor =
        subcontractorResponse()

      val expectedCurrentVerification =
        currentVerification(subcontractor)

      when(
        subcontractorService.getSubcontractor(
          "instance-id",
          10L
        )(hc)
      ).thenReturn(
        Future.successful(
          GetSubcontractorResponse(
            scheme = None,
            subcontractor = Some(subcontractor)
          )
        )
      )

      when(
        subcontractorValidator.validateFields(
          Seq(expectedCurrentVerification)
        )
      ).thenReturn(Map.empty)

      val result =
        finalValidationService
          .validate(
            "instance-id",
            selectedUserAnswers()
          )
          .futureValue

      result mustBe
        VerifyFinalValidationResult(
          subcontractors = Seq(subcontractor),
          failures = Seq.empty
        )

      verify(subcontractorService).getSubcontractor(
        "instance-id",
        10L
      )(hc)

      verify(subcontractorValidator).validateFields(
        Seq(expectedCurrentVerification)
      )
    }

    "must return Final Validation failures for the selected subcontractor" in {

      val subcontractorService   = mock[SubcontractorService]
      val subcontractorValidator = mock[SubcontractorValidator]

      val finalValidationService =
        service(
          subcontractorService,
          subcontractorValidator
        )

      val subcontractor =
        subcontractorResponse()

      val expectedCurrentVerification =
        currentVerification(subcontractor)

      when(
        subcontractorService.getSubcontractor(
          "instance-id",
          10L
        )(hc)
      ).thenReturn(
        Future.successful(
          GetSubcontractorResponse(
            scheme = None,
            subcontractor = Some(subcontractor)
          )
        )
      )

      when(
        subcontractorValidator.validateFields(
          Seq(expectedCurrentVerification)
        )
      ).thenReturn(
        Map(
          1L -> Seq(
            SubcontractorValidationField.Utr,
            SubcontractorValidationField.Postcode,
            SubcontractorValidationField.Utr,
            SubcontractorValidationField.WorksReferenceNumber
          )
        )
      )

      val result =
        finalValidationService
          .validate(
            "instance-id",
            selectedUserAnswers()
          )
          .futureValue

      result mustBe
        VerifyFinalValidationResult(
          subcontractors = Seq(subcontractor),
          failures = Seq(
            SubcontractorFinalValidationFailure(
              subcontractorId = 1L,
              issues = Seq(
                FinalValidationIssue(
                  field = FinalValidationField.Utr,
                  value = Some("1234567890")
                ),
                FinalValidationIssue(
                  field = FinalValidationField.PostCode,
                  value = Some("AA1 1AA")
                ),
                FinalValidationIssue(
                  field = FinalValidationField.WorkReferenceNumber,
                  value = Some("WR123")
                )
              ),
              subbieResourceRef = Some(10L)
            )
          )
        )
    }

    "must fail when VerifyFinalValidationSourcePage is not present" in {

      val subcontractorService   = mock[SubcontractorService]
      val subcontractorValidator = mock[SubcontractorValidator]

      val finalValidationService =
        service(
          subcontractorService,
          subcontractorValidator
        )

      val exception =
        finalValidationService
          .validate(
            "instance-id",
            UserAnswers("id")
          )
          .failed
          .futureValue

      exception.getMessage mustBe
        "VerifyFinalValidationSourcePage not found"

      verifyNoInteractions(subcontractorService)
      verifyNoInteractions(subcontractorValidator)
    }

    "must fail when SelectSubcontractorPage is not present" in {

      val subcontractorService   = mock[SubcontractorService]
      val subcontractorValidator = mock[SubcontractorValidator]

      val finalValidationService =
        service(
          subcontractorService,
          subcontractorValidator
        )

      val userAnswers =
        UserAnswers("id")
          .set(
            VerifyFinalValidationSourcePage,
            SelectSubcontractor
          )
          .success
          .value

      val exception =
        finalValidationService
          .validate(
            "instance-id",
            userAnswers
          )
          .failed
          .futureValue

      exception.getMessage mustBe
        "SelectSubcontractorPage not found"

      verifyNoInteractions(subcontractorService)
      verifyNoInteractions(subcontractorValidator)
    }

    "must fail when UnverifiedSubcontractorsPage is not present" in {

      val subcontractorService   = mock[SubcontractorService]
      val subcontractorValidator = mock[SubcontractorValidator]

      val finalValidationService =
        service(
          subcontractorService,
          subcontractorValidator
        )

      val userAnswers =
        UserAnswers(
          "id",
          Json.obj(
            "finalvalidations"    -> Json.obj(
              "verifyFinalValidationSource" -> "SelectSubcontractor"
            ),
            "selectSubcontractor" -> Json.arr(
              Json.obj(
                "id"   -> "1",
                "name" -> "John Smith"
              )
            )
          )
        )

      val exception =
        finalValidationService
          .validate(
            "instance-id",
            userAnswers
          )
          .failed
          .futureValue

      exception.getMessage mustBe
        "UnverifiedSubcontractorsPage not found"

      verifyNoInteractions(subcontractorService)
      verifyNoInteractions(subcontractorValidator)
    }

    "must fail when a selected subcontractor id is invalid" in {

      val subcontractorService   = mock[SubcontractorService]
      val subcontractorValidator = mock[SubcontractorValidator]

      val finalValidationService =
        service(
          subcontractorService,
          subcontractorValidator
        )

      val exception =
        finalValidationService
          .validate(
            "instance-id",
            selectedUserAnswers(
              selectedId = "invalid"
            )
          )
          .failed
          .futureValue

      exception.getMessage mustBe
        "Invalid subcontractorId: invalid"

      verifyNoInteractions(subcontractorService)
      verifyNoInteractions(subcontractorValidator)
    }

    "must fail when the selected subcontractor is not available" in {

      val subcontractorService   = mock[SubcontractorService]
      val subcontractorValidator = mock[SubcontractorValidator]

      val finalValidationService =
        service(
          subcontractorService,
          subcontractorValidator
        )

      val exception =
        finalValidationService
          .validate(
            "instance-id",
            selectedUserAnswers(
              selectedId = "1",
              availableId = 2L
            )
          )
          .failed
          .futureValue

      exception.getMessage mustBe
        "Subcontractor with id 1 not found in available subcontractors"

      verifyNoInteractions(subcontractorService)
      verifyNoInteractions(subcontractorValidator)
    }

    "must fail when the selected subcontractor does not have a subbieResourceRef" in {

      val subcontractorService   = mock[SubcontractorService]
      val subcontractorValidator = mock[SubcontractorValidator]

      val finalValidationService =
        service(
          subcontractorService,
          subcontractorValidator
        )

      val exception =
        finalValidationService
          .validate(
            "instance-id",
            selectedUserAnswers(
              subbieResourceRef = None
            )
          )
          .failed
          .futureValue

      exception.getMessage mustBe
        "Subcontractor with id 1 not found in available subcontractors"

      verifyNoInteractions(subcontractorService)
      verifyNoInteractions(subcontractorValidator)
    }

    "must fail when the returned subcontractor id does not match the selected subcontractor id" in {

      val subcontractorService   = mock[SubcontractorService]
      val subcontractorValidator = mock[SubcontractorValidator]

      val finalValidationService =
        service(
          subcontractorService,
          subcontractorValidator
        )

      val subcontractor =
        subcontractorResponse(
          subcontractorId = 2L
        )

      when(
        subcontractorService.getSubcontractor(
          "instance-id",
          10L
        )(hc)
      ).thenReturn(
        Future.successful(
          GetSubcontractorResponse(
            scheme = None,
            subcontractor = Some(subcontractor)
          )
        )
      )

      val exception =
        finalValidationService
          .validate(
            "instance-id",
            selectedUserAnswers()
          )
          .failed
          .futureValue

      exception.getMessage mustBe
        "Expected subcontractorId 1 but got 2"

      verifyNoInteractions(subcontractorValidator)
    }

    "must fail when the returned response does not contain a subcontractor" in {

      val subcontractorService   = mock[SubcontractorService]
      val subcontractorValidator = mock[SubcontractorValidator]

      val finalValidationService =
        service(
          subcontractorService,
          subcontractorValidator
        )

      when(
        subcontractorService.getSubcontractor(
          "instance-id",
          10L
        )(hc)
      ).thenReturn(
        Future.successful(
          GetSubcontractorResponse(
            scheme = None,
            subcontractor = None
          )
        )
      )

      val exception =
        finalValidationService
          .validate(
            "instance-id",
            selectedUserAnswers()
          )
          .failed
          .futureValue

      exception.getMessage mustBe
        "Subcontractor not found for subbieResourceRef 10"

      verifyNoInteractions(subcontractorValidator)
    }

    "must fail when SelectSubcontractorsToReverifyPage is not present" in {

      val subcontractorService   = mock[SubcontractorService]
      val subcontractorValidator = mock[SubcontractorValidator]

      val finalValidationService =
        service(
          subcontractorService,
          subcontractorValidator
        )

      val userAnswers =
        UserAnswers("id")
          .set(
            VerifyFinalValidationSourcePage,
            SelectSubcontractorsToReverify
          )
          .success
          .value

      val exception =
        finalValidationService
          .validate(
            "instance-id",
            userAnswers
          )
          .failed
          .futureValue

      exception.getMessage mustBe
        "SelectSubcontractorsToReverifyPage not found"

      verifyNoInteractions(subcontractorService)
      verifyNoInteractions(subcontractorValidator)
    }

    "must fail for ReviewUnmatchedSubcontractors" in {

      val subcontractorService   = mock[SubcontractorService]
      val subcontractorValidator = mock[SubcontractorValidator]

      val finalValidationService =
        service(
          subcontractorService,
          subcontractorValidator
        )

      val userAnswers =
        UserAnswers("id")
          .set(
            VerifyFinalValidationSourcePage,
            ReviewUnmatchedSubcontractors
          )
          .success
          .value

      val exception =
        finalValidationService
          .validate(
            "instance-id",
            userAnswers
          )
          .failed
          .futureValue

      exception mustBe a[UnsupportedOperationException]

      exception.getMessage mustBe
        "ReviewUnmatchedSubcontractors FinalValidation is not implemented yet"

      verifyNoInteractions(subcontractorService)
      verifyNoInteractions(subcontractorValidator)
    }

    "must fail for ReviewInsufficientInfoSubcontractors" in {

      val subcontractorService   = mock[SubcontractorService]
      val subcontractorValidator = mock[SubcontractorValidator]

      val finalValidationService =
        service(
          subcontractorService,
          subcontractorValidator
        )

      val userAnswers =
        UserAnswers("id")
          .set(
            VerifyFinalValidationSourcePage,
            ReviewInsufficientInfoSubcontractors
          )
          .success
          .value

      val exception =
        finalValidationService
          .validate(
            "instance-id",
            userAnswers
          )
          .failed
          .futureValue

      exception mustBe a[UnsupportedOperationException]

      exception.getMessage mustBe
        "ReviewInsufficientInfoSubcontractors FinalValidation is not implemented yet"

      verifyNoInteractions(subcontractorService)
      verifyNoInteractions(subcontractorValidator)
    }
  }

  "validateDraftSubcontractor" - {

    "must validate the proposed subcontractor and return distinct Final Validation draft issues" in {

      val subcontractorService   = mock[SubcontractorService]
      val subcontractorValidator = mock[SubcontractorValidator]

      val finalValidationService =
        service(
          subcontractorService,
          subcontractorValidator
        )

      val finalValidationDraft =
        draft(
          Json.obj(
            "firstName"            -> "John",
            "secondName"           -> "Paul",
            "surname"              -> "Smith",
            "tradingName"          -> "Smith Construction",
            "utr"                  -> "1234567890",
            "nino"                 -> "AB123456C",
            "addressLine1"         -> "1 High Street",
            "postcode"             -> "AA1 1AA",
            "country"              -> "UK",
            "emailAddress"         -> "john@example.com",
            "phoneNumber"          -> "01234567890",
            "mobilePhoneNumber"    -> "07123456789",
            "worksReferenceNumber" -> "WR123"
          )
        )

      val subcontractor =
        finalValidationDraft
          .subcontractor(1L)
          .value

      val expectedCurrentVerification =
        currentVerification(subcontractor)

      when(
        subcontractorValidator.validateFieldsFor(
          1L,
          Seq(expectedCurrentVerification)
        )
      ).thenReturn(
        Seq(
          SubcontractorValidationField.Utr,
          SubcontractorValidationField.Postcode,
          SubcontractorValidationField.Utr,
          SubcontractorValidationField.EmailAddress,
          SubcontractorValidationField.WorksReferenceNumber
        )
      )

      val result =
        finalValidationService
          .validateDraftSubcontractor(
            finalValidationDraft,
            1L
          )
          .success
          .value

      result mustBe Seq(
        FinalValidationDraftIssue(
          fieldKey = "utr",
          value = Some("1234567890")
        ),
        FinalValidationDraftIssue(
          fieldKey = "postCode",
          value = Some("AA1 1AA")
        ),
        FinalValidationDraftIssue(
          fieldKey = "emailAddress",
          value = Some("john@example.com")
        ),
        FinalValidationDraftIssue(
          fieldKey = "workReferenceNumber",
          value = Some("WR123")
        )
      )

      verify(subcontractorValidator).validateFieldsFor(
        1L,
        Seq(expectedCurrentVerification)
      )

      verifyNoInteractions(subcontractorService)
    }

    "must return an empty sequence when there are no failed fields" in {

      val subcontractorService   = mock[SubcontractorService]
      val subcontractorValidator = mock[SubcontractorValidator]

      val finalValidationService =
        service(
          subcontractorService,
          subcontractorValidator
        )

      val finalValidationDraft =
        draft(
          Json.obj(
            "firstName" -> "John",
            "surname"   -> "Smith"
          )
        )

      val subcontractor =
        finalValidationDraft
          .subcontractor(1L)
          .value

      val expectedCurrentVerification =
        currentVerification(subcontractor)

      when(
        subcontractorValidator.validateFieldsFor(
          1L,
          Seq(expectedCurrentVerification)
        )
      ).thenReturn(Seq.empty)

      finalValidationService
        .validateDraftSubcontractor(
          finalValidationDraft,
          1L
        )
        .success
        .value mustBe Seq.empty

      verify(subcontractorValidator).validateFieldsFor(
        1L,
        Seq(expectedCurrentVerification)
      )
    }

    "must fail when the subcontractor is not present in the draft" in {

      val subcontractorService   = mock[SubcontractorService]
      val subcontractorValidator = mock[SubcontractorValidator]

      val finalValidationService =
        service(
          subcontractorService,
          subcontractorValidator
        )

      val finalValidationDraft =
        Json
          .obj(
            "subcontractors" -> Json.arr()
          )
          .as[FinalValidationDraft]

      val result =
        finalValidationService.validateDraftSubcontractor(
          finalValidationDraft,
          1L
        )

      result.failure.exception.getMessage mustBe
        "Subcontractor 1 not found in Final Validation draft"

      verifyNoInteractions(subcontractorService)
      verifyNoInteractions(subcontractorValidator)
    }
  }
}
