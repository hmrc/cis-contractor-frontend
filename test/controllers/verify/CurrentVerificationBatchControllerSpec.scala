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

package controllers.verify

import base.SpecBase
import controllers.routes
import models.{NormalMode, SubcontractorCurrentVerification, UserAnswers, VerificationBatchCurrentVerification, VerificationCurrentVerification}
import models.response.GetCurrentVerificationBatchResponse
import models.validation.{FieldValidationFailure, SubcontractorValidationFailure}
import models.validation.SubcontractorValidationField.EmailAddress
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, verify, verifyNoInteractions, verifyNoMoreInteractions, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.validation.SubcontractorValidationFailuresPage
import pages.verify.CurrentVerificationBatchResponsePage
import play.api.Application
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.{SubcontractorDetailsValidator, VerificationService}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class CurrentVerificationBatchControllerSpec extends SpecBase with MockitoSugar {

  private val endpointUrl =
    controllers.verify.routes.CurrentVerificationBatchController
      .onPageLoad(NormalMode)
      .url

  "CurrentVerificationBatchController" - {

    "must redirect to JourneyRecovery when CurrentVerificationBatchResponsePage is missing" in {
      val mockService =
        mock[VerificationService]

      val mockValidator =
        mock[SubcontractorDetailsValidator]

      val mockSessionRepository =
        mock[SessionRepository]

      when(
        mockService.getCurrentVerificationBatch(
          any[UserAnswers]
        )(any[HeaderCarrier])
      ).thenReturn(
        Future.successful(emptyUserAnswers)
      )

      val application =
        buildApplication(
          userAnswers = Some(emptyUserAnswers),
          verificationService = mockService,
          validator = mockValidator,
          sessionRepository = mockSessionRepository
        )

      running(application) {
        val request =
          FakeRequest(GET, endpointUrl)

        val result =
          route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          routes.JourneyRecoveryController
            .onPageLoad()
            .url
      }

      verify(mockService)
        .getCurrentVerificationBatch(
          any[UserAnswers]
        )(any[HeaderCarrier])

      verifyNoInteractions(
        mockValidator,
        mockSessionRepository
      )

      verifyNoMoreInteractions(mockService)
    }

    "must redirect to JourneyRecovery when getCurrentVerificationBatch fails" in {
      val mockService =
        mock[VerificationService]

      val mockValidator =
        mock[SubcontractorDetailsValidator]

      val mockSessionRepository =
        mock[SessionRepository]

      when(
        mockService.getCurrentVerificationBatch(
          any[UserAnswers]
        )(any[HeaderCarrier])
      ).thenReturn(
        Future.failed(
          new RuntimeException("boom")
        )
      )

      val application =
        buildApplication(
          userAnswers = Some(emptyUserAnswers),
          verificationService = mockService,
          validator = mockValidator,
          sessionRepository = mockSessionRepository
        )

      running(application) {
        val request =
          FakeRequest(GET, endpointUrl)

        val result =
          route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          routes.JourneyRecoveryController
            .onPageLoad()
            .url
      }

      verify(mockService)
        .getCurrentVerificationBatch(
          any[UserAnswers]
        )(any[HeaderCarrier])

      verifyNoInteractions(
        mockValidator,
        mockSessionRepository
      )

      verifyNoMoreInteractions(mockService)
    }

    "must redirect to JourneyRecovery when no existing data is found" in {
      val mockService =
        mock[VerificationService]

      val mockValidator =
        mock[SubcontractorDetailsValidator]

      val mockSessionRepository =
        mock[SessionRepository]

      val application =
        buildApplication(
          userAnswers = None,
          verificationService = mockService,
          validator = mockValidator,
          sessionRepository = mockSessionRepository
        )

      running(application) {
        val request =
          FakeRequest(GET, endpointUrl)

        val result =
          route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          routes.JourneyRecoveryController
            .onPageLoad()
            .url
      }

      verify(
        mockService,
        never()
      ).getCurrentVerificationBatch(
        any[UserAnswers]
      )(any[HeaderCarrier])

      verifyNoInteractions(
        mockValidator,
        mockSessionRepository
      )

      verifyNoMoreInteractions(mockService)
    }

    "must validate subcontractors and persist validation failures" in {
      val mockService =
        mock[VerificationService]

      val mockValidator =
        mock[SubcontractorDetailsValidator]

      val mockSessionRepository =
        mock[SessionRepository]

      val subcontractors =
        Seq(currentSubcontractor(1L))

      val response =
        GetCurrentVerificationBatchResponse(
          subcontractors = subcontractors,
          verificationBatch = None,
          verifications = Seq.empty
        )

      val updatedAnswers =
        emptyUserAnswers.setOrException(
          CurrentVerificationBatchResponsePage,
          response
        )

      val failures =
        List(
          SubcontractorValidationFailure(
            subcontractorId = 1L,
            failedFields = List(
              FieldValidationFailure(
                field = EmailAddress,
                value = Some("invalid-email")
              )
            )
          )
        )

      when(
        mockService.getCurrentVerificationBatch(
          any[UserAnswers]
        )(any[HeaderCarrier])
      ).thenReturn(
        Future.successful(updatedAnswers)
      )

      when(
        mockValidator.validate(subcontractors)
      ).thenReturn(failures)

      when(
        mockSessionRepository.set(
          any[UserAnswers]
        )
      ).thenReturn(Future.successful(true))

      val application =
        buildApplication(
          userAnswers = Some(emptyUserAnswers),
          verificationService = mockService,
          validator = mockValidator,
          sessionRepository = mockSessionRepository
        )

      running(application) {
        val request =
          FakeRequest(GET, endpointUrl)

        val result =
          route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.verify.routes.CreateVerificationBatchAndVerificationsController
            .onSubmit(NormalMode)
            .url
      }

      verify(mockValidator)
        .validate(subcontractors)

      val captor =
        ArgumentCaptor.forClass(
          classOf[UserAnswers]
        )

      verify(mockSessionRepository)
        .set(captor.capture())

      captor.getValue
        .get(SubcontractorValidationFailuresPage)
        .value mustBe failures
    }

    "must replace previous failures with an empty list when every subcontractor passes" in {
      val mockService =
        mock[VerificationService]

      val mockValidator =
        mock[SubcontractorDetailsValidator]

      val mockSessionRepository =
        mock[SessionRepository]

      val subcontractors =
        Seq(currentSubcontractor(1L))

      val response =
        GetCurrentVerificationBatchResponse(
          subcontractors = subcontractors,
          verificationBatch = None,
          verifications = Seq.empty
        )

      val previousFailures =
        List(
          SubcontractorValidationFailure(
            subcontractorId = 1L,
            failedFields = List(
              FieldValidationFailure(
                field = EmailAddress,
                value = Some("invalid-email")
              )
            )
          )
        )

      val updatedAnswers =
        emptyUserAnswers
          .setOrException(
            CurrentVerificationBatchResponsePage,
            response
          )
          .setOrException(
            SubcontractorValidationFailuresPage,
            previousFailures
          )

      when(
        mockService.getCurrentVerificationBatch(
          any[UserAnswers]
        )(any[HeaderCarrier])
      ).thenReturn(
        Future.successful(updatedAnswers)
      )

      when(
        mockValidator.validate(subcontractors)
      ).thenReturn(Nil)

      when(
        mockSessionRepository.set(
          any[UserAnswers]
        )
      ).thenReturn(Future.successful(true))

      val application =
        buildApplication(
          userAnswers = Some(emptyUserAnswers),
          verificationService = mockService,
          validator = mockValidator,
          sessionRepository = mockSessionRepository
        )

      running(application) {
        val request =
          FakeRequest(GET, endpointUrl)

        val result =
          route(application, request).value

        status(result) mustEqual SEE_OTHER
      }

      val captor =
        ArgumentCaptor.forClass(
          classOf[UserAnswers]
        )

      verify(mockSessionRepository)
        .set(captor.capture())

      captor.getValue
        .get(SubcontractorValidationFailuresPage)
        .value mustBe Nil
    }

    "must redirect to ModifyVerificationBatchAndVerificationsController when a current batch exists" in {
      val mockService =
        mock[VerificationService]

      val mockValidator =
        mock[SubcontractorDetailsValidator]

      val mockSessionRepository =
        mock[SessionRepository]

      val response =
        GetCurrentVerificationBatchResponse(
          subcontractors = Seq.empty,
          verificationBatch = Some(
            VerificationBatchCurrentVerification(
              verificationBatchId = 1L,
              verifBatchResourceRef = Some(123L)
            )
          ),
          verifications = Seq.empty
        )

      val updatedAnswers =
        emptyUserAnswers.setOrException(
          CurrentVerificationBatchResponsePage,
          response
        )

      when(
        mockService.getCurrentVerificationBatch(
          any[UserAnswers]
        )(any[HeaderCarrier])
      ).thenReturn(
        Future.successful(updatedAnswers)
      )

      when(
        mockValidator.validate(
          response.subcontractors
        )
      ).thenReturn(Nil)

      when(
        mockSessionRepository.set(
          any[UserAnswers]
        )
      ).thenReturn(Future.successful(true))

      val application =
        buildApplication(
          userAnswers = Some(emptyUserAnswers),
          verificationService = mockService,
          validator = mockValidator,
          sessionRepository = mockSessionRepository
        )

      running(application) {
        val request =
          FakeRequest(GET, endpointUrl)

        val result =
          route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.verify.routes.ModifyVerificationBatchAndVerificationsController
            .modifyVerificationBatch(NormalMode)
            .url
      }
    }

    "must redirect to CreateVerificationBatchAndVerificationsController when no current batch exists" in {
      val mockService =
        mock[VerificationService]

      val mockValidator =
        mock[SubcontractorDetailsValidator]

      val mockSessionRepository =
        mock[SessionRepository]

      val response =
        GetCurrentVerificationBatchResponse(
          verificationBatch = None,
          verifications = Seq.empty,
          subcontractors = Seq.empty
        )

      val updatedAnswers =
        emptyUserAnswers.setOrException(
          CurrentVerificationBatchResponsePage,
          response
        )

      when(
        mockService.getCurrentVerificationBatch(
          any[UserAnswers]
        )(any[HeaderCarrier])
      ).thenReturn(
        Future.successful(updatedAnswers)
      )

      when(
        mockValidator.validate(
          response.subcontractors
        )
      ).thenReturn(Nil)

      when(
        mockSessionRepository.set(
          any[UserAnswers]
        )
      ).thenReturn(Future.successful(true))

      val application =
        buildApplication(
          userAnswers = Some(emptyUserAnswers),
          verificationService = mockService,
          validator = mockValidator,
          sessionRepository = mockSessionRepository
        )

      running(application) {
        val request =
          FakeRequest(GET, endpointUrl)

        val result =
          route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.verify.routes.CreateVerificationBatchAndVerificationsController
            .onSubmit(NormalMode)
            .url
      }
    }

    "must redirect to ModifyVerificationBatchAndVerificationsController when verifications exist without a current batch" in {
      val mockService =
        mock[VerificationService]

      val mockValidator =
        mock[SubcontractorDetailsValidator]

      val mockSessionRepository =
        mock[SessionRepository]

      val response =
        GetCurrentVerificationBatchResponse(
          verificationBatch = None,
          verifications = Seq(
            VerificationCurrentVerification(
              verificationId = 1L,
              verificationBatchId = None,
              subcontractorId = Some(2L),
              verificationResourceRef = Some(20L),
              subcontractorName = None,
              verificationNumber = None,
              taxTreatment = None,
              actionIndicator = None,
              proceed = None,
              matched = None
            )
          ),
          subcontractors = Seq.empty
        )

      val updatedAnswers =
        emptyUserAnswers.setOrException(
          CurrentVerificationBatchResponsePage,
          response
        )

      when(
        mockService.getCurrentVerificationBatch(
          any[UserAnswers]
        )(any[HeaderCarrier])
      ).thenReturn(
        Future.successful(updatedAnswers)
      )

      when(
        mockValidator.validate(
          response.subcontractors
        )
      ).thenReturn(Nil)

      when(
        mockSessionRepository.set(
          any[UserAnswers]
        )
      ).thenReturn(Future.successful(true))

      val application =
        buildApplication(
          userAnswers = Some(emptyUserAnswers),
          verificationService = mockService,
          validator = mockValidator,
          sessionRepository = mockSessionRepository
        )

      running(application) {
        val request =
          FakeRequest(GET, endpointUrl)

        val result =
          route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.verify.routes.ModifyVerificationBatchAndVerificationsController
            .modifyVerificationBatch(NormalMode)
            .url
      }
    }

    "must redirect to JourneyRecovery when validation failures cannot be persisted" in {
      val mockService =
        mock[VerificationService]

      val mockValidator =
        mock[SubcontractorDetailsValidator]

      val mockSessionRepository =
        mock[SessionRepository]

      val response =
        GetCurrentVerificationBatchResponse(
          subcontractors = Seq.empty,
          verificationBatch = None,
          verifications = Seq.empty
        )

      val updatedAnswers =
        emptyUserAnswers.setOrException(
          CurrentVerificationBatchResponsePage,
          response
        )

      when(
        mockService.getCurrentVerificationBatch(
          any[UserAnswers]
        )(any[HeaderCarrier])
      ).thenReturn(
        Future.successful(updatedAnswers)
      )

      when(
        mockValidator.validate(
          response.subcontractors
        )
      ).thenReturn(Nil)

      when(
        mockSessionRepository.set(
          any[UserAnswers]
        )
      ).thenReturn(
        Future.failed(
          new RuntimeException("save failed")
        )
      )

      val application =
        buildApplication(
          userAnswers = Some(emptyUserAnswers),
          verificationService = mockService,
          validator = mockValidator,
          sessionRepository = mockSessionRepository
        )

      running(application) {
        val request =
          FakeRequest(GET, endpointUrl)

        val result =
          route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          routes.JourneyRecoveryController
            .onPageLoad()
            .url
      }
    }
  }

  private def buildApplication(
    userAnswers: Option[UserAnswers],
    verificationService: VerificationService,
    validator: SubcontractorDetailsValidator,
    sessionRepository: SessionRepository
  ): Application =
    applicationBuilder(
      userAnswers = userAnswers
    ).overrides(
      bind[VerificationService]
        .toInstance(verificationService),
      bind[SubcontractorDetailsValidator]
        .toInstance(validator),
      bind[SessionRepository]
        .toInstance(sessionRepository)
    ).build()

  private def currentSubcontractor(
    subcontractorId: Long
  ): SubcontractorCurrentVerification =
    SubcontractorCurrentVerification(
      subcontractorId = subcontractorId,
      subbieResourceRef = Some(
        subcontractorId * 10
      ),
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
}
