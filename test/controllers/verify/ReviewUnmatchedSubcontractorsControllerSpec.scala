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
import models.finalvalidation.{FinalValidationContext, VerifyFinalValidationSource}
import models.response.GetCurrentVerificationBatchResponse
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.finalvalidation.{FinalValidationContextPage, VerifyFinalValidationSourcePage}
import pages.verify.CurrentVerificationBatchResponsePage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import models.UserAnswers

import scala.concurrent.Future

class ReviewUnmatchedSubcontractorsControllerSpec extends SpecBase with MockitoSugar {

  private val endpointUrl = "/subcontractor/verify/review-unmatched-subcontractors"

  private val currentBatchResponse: GetCurrentVerificationBatchResponse =
    GetCurrentVerificationBatchResponse(
      subcontractors = Nil,
      verificationBatch = None,
      verifications = Nil
    )

  "ReviewUnmatchedSubcontractorsController" - {

    "onPageLoad must render the review unmatched subcontractors view when the current batch is present" in {
      val userAnswers = emptyUserAnswers
        .set(CurrentVerificationBatchResponsePage, currentBatchResponse)
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val result = route(application, FakeRequest(GET, endpointUrl)).value

        status(result) mustEqual OK
      }
    }

    "onPageLoad must redirect to JourneyRecovery when the current batch is missing" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val result = route(application, FakeRequest(GET, endpointUrl)).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "onSubmit must set the final validation source and context in session and redirect to the contractor email confirmation stored page" in {

      val mockSessionRepository = mock[SessionRepository]
      val savedAnswersCaptor    = ArgumentCaptor.forClass(classOf[UserAnswers])

      when(mockSessionRepository.set(savedAnswersCaptor.capture())).thenReturn(Future.successful(true))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
        .build()

      running(application) {
        val result = route(application, FakeRequest(POST, endpointUrl)).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.verify.routes.ContractorEmailConfirmationStoredController.onPageLoad(models.NormalMode).url

        val savedAnswers = savedAnswersCaptor.getValue
        savedAnswers
          .get(VerifyFinalValidationSourcePage)
          .value mustEqual VerifyFinalValidationSource.ReviewUnmatchedSubcontractors
        savedAnswers.get(FinalValidationContextPage).value mustEqual FinalValidationContext.VerifySubcontractor
      }
    }
  }
}
