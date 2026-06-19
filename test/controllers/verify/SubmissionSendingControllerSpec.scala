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
import models.UserAnswers
import models.requests.DataRequest
import models.response.*
import models.verify.{SubmissionStatus, VerificationSubmissionDetails}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.verify.*
import play.api.inject.bind
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.VerificationService
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDateTime
import scala.concurrent.Future

class SubmissionSendingControllerSpec extends SpecBase with MockitoSugar {

  private lazy val onPageLoadRoute =
    controllers.verify.routes.SubmissionSendingController.onPageLoad().url

  private lazy val onPollRoute =
    controllers.verify.routes.SubmissionSendingController.onPollAndRedirect.url

  private val submissionDetails =
    VerificationSubmissionDetails(
      submissionId = "13602",
      status = "ACCEPTED",
      hmrcMarkGenerated = "hmrc-mark",
      hmrcMarkGgis = None,
      correlationId = Some("corr-id"),
      pollUrl = Some("http://localhost/poll"),
      pollIntervalSeconds = Some(5),
      submittedAt = LocalDateTime.parse("2026-06-15T03:30:52"),
      lastMessageDate = None,
      timedOut = false
    )

  "SubmissionSendingController.onPageLoad" - {

    "must redirect to poll page when initial submission returns ACCEPTED" in {
      val mockService = mock[VerificationService]

      when(
        mockService.createSubmitAndPersistVerificationSubmission(
          any[DataRequest[AnyContent]],
          any[HeaderCarrier]
        )
      ).thenReturn(
        Future.successful(
          ChrisSubmissionResponse(
            submissionId = "13602",
            status = "ACCEPTED",
            hmrcMarkGenerated = "hmrc-mark"
          )
        )
      )

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[VerificationService].toInstance(mockService))
          .build()

      running(application) {
        val result = route(application, FakeRequest(GET, onPageLoadRoute)).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe
          controllers.verify.routes.SubmissionSendingController.onPollAndRedirect.url

        verify(mockService).createSubmitAndPersistVerificationSubmission(
          any[DataRequest[AnyContent]],
          any[HeaderCarrier]
        )
      }
    }

    "must redirect to recovery when service fails" in {
      val mockService = mock[VerificationService]

      when(
        mockService.createSubmitAndPersistVerificationSubmission(
          any[DataRequest[AnyContent]],
          any[HeaderCarrier]
        )
      ).thenReturn(Future.failed(new RuntimeException("boom")))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[VerificationService].toInstance(mockService))
          .build()

      running(application) {
        val result = route(application, FakeRequest(GET, onPageLoadRoute)).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe
          controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }

  "SubmissionSendingController.onPollAndRedirect" - {

    "must render polling page with Refresh header when poll returns ACCEPTED" in {
      val mockService = mock[VerificationService]

      val ua =
        emptyUserAnswers
          .set(VerificationSubmissionDetailsPage, submissionDetails)
          .success
          .value

      when(
        mockService.pollStatus(
          any[UserAnswers],
          any[VerificationSubmissionDetails]
        )(any[HeaderCarrier])
      ).thenReturn(
        Future.successful(
          ChrisPollResponse(
            status = SubmissionStatus.ACCEPTED,
            correlationId = "corr-id",
            pollUrl = None,
            pollInterval = None,
            error = None,
            irMarkReceived = None,
            lastMessageDate = None,
            acceptedTime = None
          )
        )
      )

      val application =
        applicationBuilder(userAnswers = Some(ua))
          .overrides(bind[VerificationService].toInstance(mockService))
          .build()

      running(application) {
        val result = route(application, FakeRequest(GET, onPollRoute)).value

        status(result) mustBe OK
        headers(result).get("Refresh").value mustBe "5"
      }
    }

    "must redirect to submitted page when poll returns SUBMITTED" in {
      val mockService = mock[VerificationService]

      val ua =
        emptyUserAnswers
          .set(VerificationSubmissionDetailsPage, submissionDetails)
          .success
          .value

      when(
        mockService.pollStatus(
          any[UserAnswers],
          any[VerificationSubmissionDetails]
        )(any[HeaderCarrier])
      ).thenReturn(
        Future.successful(
          ChrisPollResponse(
            status = SubmissionStatus.SUBMITTED,
            correlationId = "corr-id",
            pollUrl = None,
            pollInterval = None,
            error = None,
            irMarkReceived = None,
            lastMessageDate = None,
            acceptedTime = None
          )
        )
      )

      val application =
        applicationBuilder(userAnswers = Some(ua))
          .overrides(bind[VerificationService].toInstance(mockService))
          .build()

      running(application) {
        val result = route(application, FakeRequest(GET, onPollRoute)).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe
          controllers.verify.routes.VerificationRequestSubmittedController.onPageLoad().url
      }
    }

    "must redirect to recovery when VerificationSubmissionDetailsPage is missing" in {
      val mockService = mock[VerificationService]

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[VerificationService].toInstance(mockService))
          .build()

      running(application) {
        val result = route(application, FakeRequest(GET, onPollRoute)).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe
          controllers.routes.JourneyRecoveryController.onPageLoad().url

        verify(mockService, never()).pollStatus(
          any[UserAnswers],
          any[VerificationSubmissionDetails]
        )(any[HeaderCarrier])
      }
    }
  }
}
