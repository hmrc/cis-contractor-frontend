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
import models.{SubcontractorLastVerification, UserAnswers, VerificationLastVerification}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.unmatched.RemoveSubcontractorVerifyRequestPage
import pages.verify.LastSubmittedVerificationBatchResponsePage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import viewmodels.verify.VerificationResultsViewModel
import views.html.verify.VerificationResultsView

import scala.concurrent.Future

class VerificationResultsControllerSpec extends SpecBase with MockitoSugar {

  private val subcontractor = SubcontractorLastVerification(
    subcontractorId = 1L,
    subbieResourceRef = None,
    subcontractorType = Some("company"),
    utr = None
  )

  private val verification = VerificationLastVerification(
    verificationId = 1L,
    verificationBatchId = Some(1L),
    verificationResourceRef = None,
    matched = Some("Y"),
    verificationNumber = Some("V0004528765"),
    taxTreatment = Some("net"),
    subcontractorName = Some("Hooper and Associates"),
    subcontractorId = Some(22L),
    actionIndicator = Some("verify")
  )

  private val batchResponse = GetLastSubmittedVerificationBatchResponse(
    scheme = None,
    subcontractors = Seq(subcontractor),
    verifications = Seq(verification),
    verificationBatch = None,
    submission = None
  )

  "VerificationResults Controller" - {

    "must return OK and the correct view for a GET" in {
      val userAnswer = userAnswersWithCisId
        .set(LastSubmittedVerificationBatchResponsePage, batchResponse)
        .success
        .value
        .set(RemoveSubcontractorVerifyRequestPage(123L), true)
        .success
        .value

      val manageSubcontractorsUrl = s"${applicationConfig.manageSubcontractorsUrl}/1"

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application = applicationBuilder(userAnswers = Some(userAnswer))
        .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
        .build()

      running(application) {
        val request            = FakeRequest(GET, controllers.verify.routes.VerificationResultsController.onPageLoad().url)
        val result             = route(application, request).value
        val view               = application.injector.instanceOf[VerificationResultsView]
        val expectedViewModels = VerificationResultsViewModel.from(batchResponse)(messages(application))

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(expectedViewModels, manageSubcontractorsUrl)(
          request,
          messages(application)
        ).toString

        val userAnswersCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockSessionRepository).set(userAnswersCaptor.capture())

        val updatedUserAnswers = userAnswersCaptor.getValue

        updatedUserAnswers.get(RemoveSubcontractorVerifyRequestPage(123L)) mustBe None
      }
    }

    "must redirect to Journey Recovery when LastSubmittedVerificationBatchResponsePage is missing" in {
      val application = applicationBuilder(userAnswers = Some(userAnswersWithCisId)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.verify.routes.VerificationResultsController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
