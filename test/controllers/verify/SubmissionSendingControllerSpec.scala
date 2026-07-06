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
import models.ContractorScheme
import models.requests.CreateSubmissionForVerificationRequest
import models.response.{CreateSubmissionForVerificationResponse, GetCurrentVerificationBatchResponse, GetNewestVerificationBatchResponse}
import models.verify.ContractorEmailConfirmationStored.CurrentEmail
import models.{SubcontractorCurrentVerification, SubcontractorViewModel, VerificationBatchCurrentVerification, VerificationCurrentVerification}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.verify._
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import queries.CisIdQuery
import services.VerificationService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class SubmissionSendingControllerSpec extends SpecBase with MockitoSugar {

  private lazy val onPageLoadRoute =
    controllers.verify.routes.SubmissionSendingController.onPageLoad().url

  private val instanceId = "INST-123"

  private def withCisId(ua: models.UserAnswers): models.UserAnswers =
    ua.set(CisIdQuery, instanceId).success.value

  private val newestWithSchemeEmail: GetNewestVerificationBatchResponse =
    GetNewestVerificationBatchResponse(
      scheme = Some(
        ContractorScheme(
          accountsOfficeReference = None,
          emailAddress = Some("scheme@example.com")
        )
      ),
      subcontractors = Nil,
      verificationBatch = None,
      verifications = Nil,
      submission = None,
      monthlyReturn = None,
      monthlyReturnSubmission = None
    )

  private val currentBatch: GetCurrentVerificationBatchResponse =
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
          partnershipTradingName = None
        )
      ),
      verificationBatch = Some(
        VerificationBatchCurrentVerification(
          verificationBatchId = 99L,
          verifBatchResourceRef = Some(7777L)
        )
      ),
      verifications = Seq(
        VerificationCurrentVerification(
          verificationId = 1L,
          verificationBatchId = Some(99L),
          subcontractorId = Some(10L),
          verificationResourceRef = Some(111L)
        ),
        VerificationCurrentVerification(
          verificationId = 2L,
          verificationBatchId = Some(99L),
          subcontractorId = Some(10L),
          verificationResourceRef = None
        )
      )
    )

  private val selectedSubcontractor: SubcontractorViewModel =
    SubcontractorViewModel(
      id = "10",
      name = "Test Subcontractor"
    )

  private def validJourneyAnswers: models.UserAnswers =
    emptyUserAnswers
      .set(NewestVerificationBatchResponsePage, newestWithSchemeEmail)
      .success
      .value
      .set(ContractorEmailConfirmationStoredPage, CurrentEmail)
      .success
      .value
      .set(SelectSubcontractorPage, Set(selectedSubcontractor))
      .success
      .value
      .set(VerificationBatchReadinessPage, true)
      .success
      .value

  "SubmissionSendingController.onPageLoad" - {

    "must call service to create submission and return OK when answers are valid and buildSubmissionRequest succeeds CurrentEmail uses scheme email" in {
      val ua0 =
        validJourneyAnswers
          .set(CurrentVerificationBatchResponsePage, currentBatch)
          .success
          .value

      val ua = withCisId(ua0)

      val mockService = mock[VerificationService]

      when(mockService.getCurrentVerificationBatch(any[models.UserAnswers])(any[HeaderCarrier]))
        .thenReturn(Future.successful(ua))

      when(mockService.createSubmissionForVerification(any[CreateSubmissionForVerificationRequest])(any[HeaderCarrier]))
        .thenReturn(Future.successful(CreateSubmissionForVerificationResponse(12345L)))

      val application =
        applicationBuilder(userAnswers = Some(ua))
          .overrides(bind[VerificationService].toInstance(mockService))
          .build()

      running(application) {
        val result = route(application, FakeRequest(GET, onPageLoadRoute)).value

        status(result) mustBe OK

        verify(mockService).getCurrentVerificationBatch(any[models.UserAnswers])(any[HeaderCarrier])

        val captor = ArgumentCaptor.forClass(classOf[CreateSubmissionForVerificationRequest])
        verify(mockService).createSubmissionForVerification(captor.capture())(any[HeaderCarrier])

        val req = captor.getValue

        req.instanceId mustBe instanceId
        req.verificationBatchId mustBe 99L
        req.verificationBatchResourceRef mustBe 7777L
        req.emailRecipient mustBe Some("scheme@example.com")
        req.verifications.map(_.verificationResourceRef) mustBe Seq(111L)
      }
    }

    "must redirect to JourneyRecovery when CisIdQuery is missing buildSubmissionRequest fails" in {
      val ua =
        validJourneyAnswers
          .set(CurrentVerificationBatchResponsePage, currentBatch)
          .success
          .value

      val mockService = mock[VerificationService]

      when(mockService.getCurrentVerificationBatch(any[models.UserAnswers])(any[HeaderCarrier]))
        .thenReturn(Future.successful(ua))

      val application =
        applicationBuilder(userAnswers = Some(ua))
          .overrides(bind[VerificationService].toInstance(mockService))
          .build()

      running(application) {
        val result = route(application, FakeRequest(GET, onPageLoadRoute)).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe controllers.routes.JourneyRecoveryController.onPageLoad().url

        verify(mockService).getCurrentVerificationBatch(any[models.UserAnswers])(any[HeaderCarrier])
        verify(mockService, never()).createSubmissionForVerification(any())(any())
      }
    }

    "must redirect to JourneyRecovery when CurrentVerificationBatchResponsePage is missing buildSubmissionRequest fails" in {
      val ua = withCisId(validJourneyAnswers)

      val mockService = mock[VerificationService]

      when(mockService.getCurrentVerificationBatch(any[models.UserAnswers])(any[HeaderCarrier]))
        .thenReturn(Future.successful(ua))

      val application =
        applicationBuilder(userAnswers = Some(ua))
          .overrides(bind[VerificationService].toInstance(mockService))
          .build()

      running(application) {
        val result = route(application, FakeRequest(GET, onPageLoadRoute)).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe controllers.routes.JourneyRecoveryController.onPageLoad().url

        verify(mockService).getCurrentVerificationBatch(any[models.UserAnswers])(any[HeaderCarrier])
        verify(mockService, never()).createSubmissionForVerification(any())(any())
      }
    }

    "must redirect to JourneyRecovery when verificationBatchResourceRef is missing" in {
      val currentNoBatchRef =
        currentBatch.copy(
          verificationBatch = currentBatch.verificationBatch.map(_.copy(verifBatchResourceRef = None))
        )

      val ua0 =
        validJourneyAnswers
          .set(CurrentVerificationBatchResponsePage, currentNoBatchRef)
          .success
          .value

      val ua = withCisId(ua0)

      val mockService = mock[VerificationService]

      when(mockService.getCurrentVerificationBatch(any[models.UserAnswers])(any[HeaderCarrier]))
        .thenReturn(Future.successful(ua))

      val application =
        applicationBuilder(userAnswers = Some(ua))
          .overrides(bind[VerificationService].toInstance(mockService))
          .build()

      running(application) {
        val result = route(application, FakeRequest(GET, onPageLoadRoute)).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe controllers.routes.JourneyRecoveryController.onPageLoad().url

        verify(mockService).getCurrentVerificationBatch(any[models.UserAnswers])(any[HeaderCarrier])
        verify(mockService, never()).createSubmissionForVerification(any())(any())
      }
    }
  }
}
