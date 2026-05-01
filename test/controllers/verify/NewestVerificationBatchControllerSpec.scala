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
import models.response.GetNewestVerificationBatchResponse
import models.{NormalMode, Subcontractor, UserAnswers}
import generators.ModelGenerators
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, verify, verifyNoMoreInteractions, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.verify.NewestVerificationBatchResponsePage
import pages.verify.UnverifiedSubcontractorsPage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.VerificationService
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDateTime
import scala.concurrent.Future

class NewestVerificationBatchControllerSpec extends SpecBase with MockitoSugar with ModelGenerators {

  private val endpointUrl = "/verify/newest"

  private val verifiedSubcontractor: Subcontractor =
    arbitrarySubcontractor.arbitrary.sample.value.copy(
      subcontractorId = 1L,
      verified = Some("Y")
    )

  private val unverifiedSubcontractor: Subcontractor =
    arbitrarySubcontractor.arbitrary.sample.value.copy(
      subcontractorId = 2L,
      verified = Some("N")
    )

  private def newestBatchResponse(subcontractors: Seq[Subcontractor]) =
    GetNewestVerificationBatchResponse(
      scheme = None,
      subcontractors = subcontractors,
      verificationBatch = None,
      verifications = Seq.empty,
      submission = None,
      monthlyReturn = None
    )

  "NewestVerificationBatchController" - {

    "must redirect to NoSubcontractorsAdded when no subcontractors exist" in {
      val mockService = mock[VerificationService]

      val updatedAnswers =
        emptyUserAnswers
          .set(NewestVerificationBatchResponsePage, newestBatchResponse(Seq.empty))
          .success
          .value

      when(mockService.refreshNewestVerificationBatch(any[UserAnswers])(any[HeaderCarrier]))
        .thenReturn(Future.successful(updatedAnswers))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[VerificationService].toInstance(mockService))
          .build()

      running(application) {
        val result = route(application, FakeRequest(GET, endpointUrl)).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe
          controllers.verify.routes.NoSubcontractorsAddedController.onPageLoad().url

        verify(mockService).refreshNewestVerificationBatch(any[UserAnswers])(any[HeaderCarrier])
        verifyNoMoreInteractions(mockService)
      }
    }

    "must redirect to VerifyYourSubcontractorsYesNo when all subcontractors are verified" in {
      val mockService = mock[VerificationService]

      val updatedAnswers =
        emptyUserAnswers
          .set(
            NewestVerificationBatchResponsePage,
            newestBatchResponse(Seq(verifiedSubcontractor))
          )
          .flatMap(_.set(UnverifiedSubcontractorsPage, Seq.empty))
          .success
          .value

      when(mockService.refreshNewestVerificationBatch(any[UserAnswers])(any[HeaderCarrier]))
        .thenReturn(Future.successful(updatedAnswers))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[VerificationService].toInstance(mockService))
          .build()

      running(application) {
        val result = route(application, FakeRequest(GET, endpointUrl)).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe
          controllers.verify.routes.VerifyYourSubcontractorsYesNoController.onPageLoad.url

        verify(mockService).refreshNewestVerificationBatch(any[UserAnswers])(any[HeaderCarrier])
        verifyNoMoreInteractions(mockService)
      }
    }

    "must redirect to SelectSubcontractor when unverified subcontractors exist" in {
      val mockService = mock[VerificationService]

      val unverified = unverifiedSubcontractor

      val updatedAnswers =
        emptyUserAnswers
          .set(
            NewestVerificationBatchResponsePage,
            newestBatchResponse(Seq(unverified))
          )
          .flatMap(_.set(UnverifiedSubcontractorsPage, Seq(unverified)))
          .success
          .value

      when(mockService.refreshNewestVerificationBatch(any[UserAnswers])(any[HeaderCarrier]))
        .thenReturn(Future.successful(updatedAnswers))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[VerificationService].toInstance(mockService))
          .build()

      running(application) {
        val result = route(application, FakeRequest(GET, endpointUrl)).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe
          controllers.verify.routes.SelectSubcontractorController
            .onPageLoad(NormalMode)
            .url

        verify(mockService).refreshNewestVerificationBatch(any[UserAnswers])(any[HeaderCarrier])
        verifyNoMoreInteractions(mockService)
      }
    }

    "must redirect to JourneyRecovery when refreshNewestVerificationBatch fails" in {
      val mockService = mock[VerificationService]

      when(mockService.refreshNewestVerificationBatch(any[UserAnswers])(any[HeaderCarrier]))
        .thenReturn(Future.failed(new RuntimeException("boom")))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[VerificationService].toInstance(mockService))
          .build()

      running(application) {
        val request = FakeRequest(GET, endpointUrl)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url

        verify(mockService).refreshNewestVerificationBatch(any[UserAnswers])(any[HeaderCarrier])
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
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url

        verify(mockService, never()).refreshNewestVerificationBatch(any[UserAnswers])(any[HeaderCarrier])
        verifyNoMoreInteractions(mockService)
      }
    }
  }
}
