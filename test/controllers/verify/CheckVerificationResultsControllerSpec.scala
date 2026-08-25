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
import models.response.GetLastSubmittedVerificationBatchResponse
import models.{SubcontractorLastVerification, UserAnswers, VerificationBatchLastVerification, VerificationLastVerification}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.verify.LastSubmittedVerificationBatchResponsePage
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import queries.CisIdQuery
import repositories.SessionRepository
import services.{CisManageService, VerificationService}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class CheckVerificationResultsControllerSpec extends SpecBase with MockitoSugar {

  private val endpointUrl = "/subcontractor/verify/check-results"

  private def responseWithStatus(
    status: Option[String],
    batchId: Option[Long] = Some(99L)
  ): GetLastSubmittedVerificationBatchResponse =
    GetLastSubmittedVerificationBatchResponse(
      scheme = None,
      subcontractors = Seq(
        SubcontractorLastVerification(
          subcontractorId = 1L,
          subbieResourceRef = Some(10L),
          subcontractorType = Some("company"),
          utr = Some("1111111111")
        )
      ),
      verifications = Seq(
        VerificationLastVerification(
          verificationId = 1001L,
          verificationBatchId = batchId,
          verificationResourceRef = Some(12345L),
          matched = Some("Y"),
          verificationNumber = Some("V0000000001"),
          taxTreatment = Some("net"),
          subcontractorName = Some("John Smith"),
          subcontractorId = Some(22L),
          actionIndicator = Some("verify")
        )
      ),
      verificationBatch = Some(
        VerificationBatchLastVerification(
          verificationBatchId = 99L,
          verifBatchResourceRef = Some(1234567L),
          status = status
        )
      ),
      submission = None
    )

  private def appBuilder(
    mockService: VerificationService,
    userAnswers: UserAnswers
  ): GuiceApplicationBuilder = {
    val mockCisManageService = mock[CisManageService]
    val mockSessionRepo      = mock[SessionRepository]

    when(mockCisManageService.ensureCisIdInUserAnswers(any[UserAnswers])(any[HeaderCarrier]))
      .thenReturn(Future.successful(userAnswers))
    when(mockSessionRepo.set(any[UserAnswers])).thenReturn(Future.successful(true))

    applicationBuilder(userAnswers = Some(userAnswers))
      .overrides(
        bind[VerificationService].toInstance(mockService),
        bind[CisManageService].toInstance(mockCisManageService),
        bind[SessionRepository].toInstance(mockSessionRepo)
      )
  }

  "CheckVerificationResultsController" - {

    "must redirect to VerificationResults when the last submitted batch is available" in {
      val mockService = mock[VerificationService]
      val response    = responseWithStatus(Some("VALIDATED"))
      val ua          = emptyUserAnswers
        .set(CisIdQuery, "INST-123")
        .success
        .value
        .set(LastSubmittedVerificationBatchResponsePage, response)
        .success
        .value

      when(mockService.getLastSubmittedVerificationBatch(any[UserAnswers])(any[HeaderCarrier]))
        .thenReturn(Future.successful(ua))

      val application = appBuilder(mockService, ua).build()

      running(application) {
        val result = route(application, FakeRequest(GET, endpointUrl)).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.verify.routes.VerificationResultsController.onPageLoad().url

        verify(mockService).getLastSubmittedVerificationBatch(any[UserAnswers])(any[HeaderCarrier])
      }
    }

    "must redirect to NoVerificationResults when verification batch id is missing" in {
      val mockService = mock[VerificationService]
      val response    = GetLastSubmittedVerificationBatchResponse(
        scheme = None,
        subcontractors = Nil,
        verifications = Nil,
        verificationBatch = None,
        submission = None
      )
      val ua          = emptyUserAnswers
        .set(CisIdQuery, "INST-123")
        .success
        .value
        .set(LastSubmittedVerificationBatchResponsePage, response)
        .success
        .value

      when(mockService.getLastSubmittedVerificationBatch(any[UserAnswers])(any[HeaderCarrier]))
        .thenReturn(Future.successful(ua))

      val application = appBuilder(mockService, ua).build()

      running(application) {
        val result = route(application, FakeRequest(GET, endpointUrl)).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.verify.routes.NoVerificationResultsController.onPageLoad().url
      }
    }

    "must redirect to NoVerificationResults when status is PENDING" in {
      val mockService = mock[VerificationService]
      val response    = responseWithStatus(Some("PENDING"))
      val ua          = emptyUserAnswers
        .set(CisIdQuery, "INST-123")
        .success
        .value
        .set(LastSubmittedVerificationBatchResponsePage, response)
        .success
        .value

      when(mockService.getLastSubmittedVerificationBatch(any[UserAnswers])(any[HeaderCarrier]))
        .thenReturn(Future.successful(ua))

      val application = appBuilder(mockService, ua).build()

      running(application) {
        val result = route(application, FakeRequest(GET, endpointUrl)).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.verify.routes.NoVerificationResultsController.onPageLoad().url
      }
    }

    "must redirect to NoVerificationResults when status is ACCEPTED" in {
      val mockService = mock[VerificationService]
      val response    = responseWithStatus(Some("ACCEPTED"))
      val ua          = emptyUserAnswers
        .set(CisIdQuery, "INST-123")
        .success
        .value
        .set(LastSubmittedVerificationBatchResponsePage, response)
        .success
        .value

      when(mockService.getLastSubmittedVerificationBatch(any[UserAnswers])(any[HeaderCarrier]))
        .thenReturn(Future.successful(ua))

      val application = appBuilder(mockService, ua).build()

      running(application) {
        val result = route(application, FakeRequest(GET, endpointUrl)).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.verify.routes.NoVerificationResultsController.onPageLoad().url
      }
    }

    "must redirect to SystemError when the service call fails" in {
      val mockService = mock[VerificationService]
      val ua          = emptyUserAnswers.set(CisIdQuery, "INST-123").success.value

      when(mockService.getLastSubmittedVerificationBatch(any[UserAnswers])(any[HeaderCarrier]))
        .thenReturn(Future.failed(new RuntimeException("boom")))

      val application = appBuilder(mockService, ua).build()

      running(application) {
        val result = route(application, FakeRequest(GET, endpointUrl)).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.routes.SystemErrorController.onPageLoad().url

        verify(mockService).getLastSubmittedVerificationBatch(any[UserAnswers])(any[HeaderCarrier])
      }
    }
  }
}
