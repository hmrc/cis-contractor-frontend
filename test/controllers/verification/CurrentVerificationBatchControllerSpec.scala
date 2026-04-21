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

package controllers.verification

import base.SpecBase
import controllers.routes
import models.UserAnswers
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, verify, verifyNoMoreInteractions, when}
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.VerificationService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class CurrentVerificationBatchControllerSpec extends SpecBase with MockitoSugar {
  private val endpointUrl = "/verification/current"

  "CurrentVerificationBatchController" - {

    "must return OK with confirmation message when refreshCurrentVerificationBatch succeeds" in {
      val mockService = mock[VerificationService]

      when(mockService.getCurrentVerificationBatch(any[UserAnswers])(any[HeaderCarrier]))
        .thenReturn(Future.successful(emptyUserAnswers))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[VerificationService].toInstance(mockService))
          .build()

      running(application) {
        val request = FakeRequest(GET, endpointUrl)
        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustBe "current verification batch saved to session"

        val uaCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockService).getCurrentVerificationBatch(uaCaptor.capture())(any[HeaderCarrier])

        uaCaptor.getValue.id mustBe emptyUserAnswers.id
        uaCaptor.getValue.data mustBe emptyUserAnswers.data

        verifyNoMoreInteractions(mockService)
      }
    }

    "must redirect to JourneyRecovery when getCurrentVerificationBatch fails" in {
      val mockService = mock[VerificationService]

      when(mockService.getCurrentVerificationBatch(any[UserAnswers])(any[HeaderCarrier]))
        .thenReturn(Future.failed(new RuntimeException("boom")))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[VerificationService].toInstance(mockService))
          .build()

      running(application) {
        val request = FakeRequest(GET, endpointUrl)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url

        verify(mockService).getCurrentVerificationBatch(any[UserAnswers])(any[HeaderCarrier])
        verifyNoMoreInteractions(mockService)
      }
    }

    "must redirect to JourneyRecovery when no existing data is found (requireData fails)" in {
      val mockService = mock[VerificationService]

      val application =
        applicationBuilder(userAnswers = None)
          .overrides(bind[VerificationService].toInstance(mockService))
          .build()

      running(application) {
        val request = FakeRequest(GET, endpointUrl)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url

        verify(mockService, never()).getCurrentVerificationBatch(any[UserAnswers])(any[HeaderCarrier])
        verifyNoMoreInteractions(mockService)
      }
    }
  }
}
