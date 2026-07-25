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
import models.UserAnswers
import models.response.GetCurrentVerificationBatchResponse
import models.{SubcontractorCurrentVerification, SubcontractorViewModel, VerificationBatchCurrentVerification, VerificationCurrentVerification}
import models.verify.{ChrisVerificationRequestBuilder, SelectedSubcontractors}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{never, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.verify.{CurrentVerificationBatchResponsePage, SelectSubcontractorPage, SelectSubcontractorsToReverifyPage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.{CisManageService, VerificationService}
import connectors.ConstructionIndustrySchemeConnector

import scala.concurrent.Future
import repositories.SessionRepository

import scala.concurrent.ExecutionContext
import uk.gov.hmrc.http.HeaderCarrier

class CreateVerificationBatchAndVerificationsControllerSpec extends SpecBase with MockitoSugar {

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global
  implicit val hc: HeaderCarrier    = HeaderCarrier()

  private val emptyCurrent: GetCurrentVerificationBatchResponse =
    GetCurrentVerificationBatchResponse(
      subcontractors = Nil,
      verificationBatch = None,
      verifications = Nil
    )

  private val nonEmptyCurrent: GetCurrentVerificationBatchResponse =
    GetCurrentVerificationBatchResponse(
      subcontractors = Seq(
        SubcontractorCurrentVerification(
          subcontractorId = 10L,
          subbieResourceRef = Some(1111L),
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
          worksReferenceNumber = None
        )
      ),
      verificationBatch = Some(
        VerificationBatchCurrentVerification(
          verificationBatchId = 999L,
          verifBatchResourceRef = Some(7777L)
        )
      ),
      verifications = Seq(
        VerificationCurrentVerification(
          verificationId = 1L,
          verificationBatchId = Some(999L),
          subcontractorId = Some(10L),
          verificationResourceRef = Some(1111L)
        )
      )
    )

  "CreateVerificationBatchAndVerificationsController.onSubmit" - {

    "must get current verification batch when CurrentVerificationBatchResponsePage is missing and redirect to JourneyRecovery when service fails" in {
      val mockService = mock[VerificationService]

      val ua =
        emptyUserAnswers
          .set(SelectSubcontractorPage, Set(SubcontractorViewModel("10", "Name 10")))
          .success
          .value

      when(mockService.getCurrentVerificationBatch(any[UserAnswers])(any()))
        .thenReturn(Future.failed(new RuntimeException("current batch failed")))

      val app =
        applicationBuilder(userAnswers = Some(ua))
          .overrides(bind[VerificationService].toInstance(mockService))
          .build()

      running(app) {
        val controller = app.injector.instanceOf[CreateVerificationBatchAndVerificationsController]

        val request = FakeRequest(POST, "/test-only")
        val result  = controller.onSubmit()(request)

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.JourneyRecoveryController.onPageLoad().url

        verify(mockService).getCurrentVerificationBatch(eqTo(ua))(any())

        verify(mockService, never())
          .createVerificationBatchAndVerifications(any[UserAnswers], any[Seq[Long]], any())(any())
      }
    }

    "must redirect to JourneyRecovery when an invalid subcontractor id is found (parseIds Left) and not call service" in {
      val mockService = mock[VerificationService]

      val ua =
        emptyUserAnswers
          .set(CurrentVerificationBatchResponsePage, emptyCurrent)
          .success
          .value
          .set(
            SelectSubcontractorPage,
            Set(SubcontractorViewModel("not-a-long", "Bad Id"))
          )
          .success
          .value

      val app =
        applicationBuilder(userAnswers = Some(ua))
          .overrides(bind[VerificationService].toInstance(mockService))
          .build()

      running(app) {
        val controller = app.injector.instanceOf[CreateVerificationBatchAndVerificationsController]

        val request = FakeRequest(POST, "/test-only")
        val result  = controller.onSubmit()(request)

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.JourneyRecoveryController.onPageLoad().url

        verify(mockService, never())
          .createVerificationBatchAndVerifications(any[UserAnswers], any[Seq[Long]], any())(any())
      }
    }

    "must redirect to ModifyVerificationBatchAndVerificationsController when current batch exists and not call service" in {
      val mockService = mock[VerificationService]

      val ua =
        emptyUserAnswers
          .set(CurrentVerificationBatchResponsePage, nonEmptyCurrent)
          .success
          .value
          .set(SelectSubcontractorPage, Set(SubcontractorViewModel("10", "Name 10")))
          .success
          .value

      val app =
        applicationBuilder(userAnswers = Some(ua))
          .overrides(bind[VerificationService].toInstance(mockService))
          .build()

      running(app) {
        val controller = app.injector.instanceOf[CreateVerificationBatchAndVerificationsController]

        val request = FakeRequest(POST, "/test-only")
        val result  = controller.onSubmit()(request)

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe
          controllers.verify.routes.ModifyVerificationBatchAndVerificationsController.modifyVerificationBatch().url

        verify(mockService, never())
          .createVerificationBatchAndVerifications(any[UserAnswers], any[Seq[Long]], any())(any())
      }
    }

    "must call service with distinct combined ids when current batch is empty and then redirect to CheckVerificationBatchReadiness" in {
      val mockService = mock[VerificationService]

      val ua =
        emptyUserAnswers
          .set(CurrentVerificationBatchResponsePage, emptyCurrent)
          .success
          .value
          .set(
            SelectSubcontractorPage,
            Set(
              SubcontractorViewModel("10", "Name 10"),
              SubcontractorViewModel("20", "Name 20"),
              SubcontractorViewModel("10", "Name 10")
            )
          )
          .success
          .value
          .set(
            SelectSubcontractorsToReverifyPage,
            Set(
              SelectedSubcontractors("30", "Name 30"),
              SelectedSubcontractors("20", "Name 20")
            )
          )
          .success
          .value

      when(
        mockService.createVerificationBatchAndVerifications(any[UserAnswers], any[Seq[Long]], eqTo(None))(any())
      ).thenReturn(Future.successful(ua))

      val app =
        applicationBuilder(userAnswers = Some(ua))
          .overrides(bind[VerificationService].toInstance(mockService))
          .build()

      running(app) {
        val controller = app.injector.instanceOf[CreateVerificationBatchAndVerificationsController]

        val request = FakeRequest(POST, "/test-only")
        val result  = controller.onSubmit()(request)

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe
          controllers.verify.routes.CheckVerificationBatchReadinessController
            .checkVerificationBatchReadiness()
            .url

        val idsCaptor = ArgumentCaptor.forClass(classOf[Seq[Long]])

        verify(mockService)
          .createVerificationBatchAndVerifications(eqTo(ua), idsCaptor.capture(), eqTo(None))(any())

        idsCaptor.getValue mustBe Seq(10L, 20L, 30L)
      }
    }

    "must redirect to JourneyRecovery when service call fails" in {
      val mockService = mock[VerificationService]

      val ua =
        emptyUserAnswers
          .set(CurrentVerificationBatchResponsePage, emptyCurrent)
          .success
          .value
          .set(SelectSubcontractorPage, Set(SubcontractorViewModel("10", "Name 10")))
          .success
          .value

      when(
        mockService.createVerificationBatchAndVerifications(any[UserAnswers], any[Seq[Long]], eqTo(None))(any())
      ).thenReturn(Future.failed(new RuntimeException("boom")))

      val app =
        applicationBuilder(userAnswers = Some(ua))
          .overrides(bind[VerificationService].toInstance(mockService))
          .build()

      running(app) {
        val controller = app.injector.instanceOf[CreateVerificationBatchAndVerificationsController]

        val request = FakeRequest(POST, "/test-only")
        val result  = controller.onSubmit()(request)

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }

  "must default missing SelectSubcontractorPage to empty (getOrElse Seq.empty) and use only reverify ids" in {
    val mockService = mock[VerificationService]

    val ua =
      emptyUserAnswers
        .set(CurrentVerificationBatchResponsePage, emptyCurrent)
        .success
        .value
        .set(
          SelectSubcontractorsToReverifyPage,
          Set(
            SelectedSubcontractors("30", "Name 30"),
            SelectedSubcontractors("20", "Name 20")
          )
        )
        .success
        .value

    when(mockService.createVerificationBatchAndVerifications(any[UserAnswers], any[Seq[Long]], eqTo(None))(any()))
      .thenReturn(Future.successful(ua))

    val app =
      applicationBuilder(userAnswers = Some(ua))
        .overrides(bind[VerificationService].toInstance(mockService))
        .build()

    running(app) {
      val controller = app.injector.instanceOf[CreateVerificationBatchAndVerificationsController]

      val request = FakeRequest(POST, "/test-only")
      val result  = controller.onSubmit()(request)

      status(result) mustBe SEE_OTHER
      redirectLocation(result).value mustBe
        controllers.verify.routes.CheckVerificationBatchReadinessController
          .checkVerificationBatchReadiness()
          .url

      val idsCaptor = ArgumentCaptor.forClass(classOf[Seq[Long]])
      verify(mockService).createVerificationBatchAndVerifications(eqTo(ua), idsCaptor.capture(), eqTo(None))(any())

      idsCaptor.getValue.sorted mustBe Seq(20L, 30L)
    }
  }

  "must default missing SelectSubcontractorsToReverifyPage to empty (getOrElse Seq.empty) and use only verify ids" in {
    val mockService = mock[VerificationService]

    val ua =
      emptyUserAnswers
        .set(CurrentVerificationBatchResponsePage, emptyCurrent)
        .success
        .value
        .set(
          SelectSubcontractorPage,
          Set(
            SubcontractorViewModel("10", "Name 10"),
            SubcontractorViewModel("20", "Name 20")
          )
        )
        .success
        .value

    when(mockService.createVerificationBatchAndVerifications(any[UserAnswers], any[Seq[Long]], eqTo(None))(any()))
      .thenReturn(Future.successful(ua))

    val app =
      applicationBuilder(userAnswers = Some(ua))
        .overrides(bind[VerificationService].toInstance(mockService))
        .build()

    running(app) {
      val controller = app.injector.instanceOf[CreateVerificationBatchAndVerificationsController]

      val request = FakeRequest(POST, "/test-only")
      val result  = controller.onSubmit()(request)

      status(result) mustBe SEE_OTHER
      redirectLocation(result).value mustBe
        controllers.verify.routes.CheckVerificationBatchReadinessController
          .checkVerificationBatchReadiness()
          .url

      val idsCaptor = ArgumentCaptor.forClass(classOf[Seq[Long]])
      verify(mockService).createVerificationBatchAndVerifications(eqTo(ua), idsCaptor.capture(), eqTo(None))(any())

      idsCaptor.getValue.sorted mustBe Seq(10L, 20L)
    }
  }

  "must fail when CisIdQuery is missing (InstanceIdQuery not found in session data) and not call connector nor repo" in {
    val mockConnector        = mock[ConstructionIndustrySchemeConnector]
    val mockCisManageService = mock[CisManageService]
    val mockBuilder          = mock[ChrisVerificationRequestBuilder]
    val mockRepo             = mock[SessionRepository]
    val service              = new VerificationService(mockConnector, mockCisManageService, mockBuilder, mockRepo)

    val currentBatchResponse =
      GetCurrentVerificationBatchResponse(
        subcontractors = Nil,
        verificationBatch = None,
        verifications = Nil
      )

    val ua =
      emptyUserAnswers
        .set(CurrentVerificationBatchResponsePage, currentBatchResponse)
        .success
        .value

    val ex =
      service
        .createVerificationBatchAndVerifications(
          userAnswers = ua,
          selectedSubcontractorIds = Seq(10L),
          actionIndicator = None
        )
        .failed
        .futureValue

    ex.getMessage must include("InstanceIdQuery not found in session data")

    verify(mockConnector, never()).createVerificationBatchAndVerifications(any())(any())
    verify(mockConnector, never()).getCurrentVerificationBatch(any[String])(any())
    verify(mockConnector, never()).getNewestVerificationBatch(any[String])(any())
    verify(mockRepo, never()).set(any())
  }
}
