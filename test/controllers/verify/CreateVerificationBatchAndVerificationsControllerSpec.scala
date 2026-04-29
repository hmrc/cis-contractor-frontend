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
import models.{SubcontractorCurrentVerification, VerificationBatchCurrentVerification, VerificationCurrentVerification}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{never, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.verification.CurrentVerificationBatchResponsePage
import pages.verify.{ReverifySubcontractorsPage, SelectedSubcontractorsToVerifyPage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.VerificationService

import scala.concurrent.Future

class CreateVerificationBatchAndVerificationsControllerSpec extends SpecBase with MockitoSugar {

  private val emptyCurrent: GetCurrentVerificationBatchResponse =
    GetCurrentVerificationBatchResponse(
      subcontractors = Nil,
      verificationBatch = Nil,
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
          partnershipTradingName = None
        )
      ),
      verificationBatch = Seq(
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

    "must redirect to JourneyRecovery when CurrentVerificationBatchResponsePage is missing and not call service" in {
      val mockService = mock[VerificationService]

      val ua =
        emptyUserAnswers
          .set(SelectedSubcontractorsToVerifyPage, Seq(10L))
          .success
          .value

      val app =
        applicationBuilder(userAnswers = Some(ua))
          .overrides(bind[VerificationService].toInstance(mockService))
          .build()

      running(app) {
        val controller = app.injector.instanceOf[CreateVerificationBatchAndVerificationsController]

        val request = FakeRequest(POST, "/test-only")
        val result = controller.onSubmit()(request)

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.JourneyRecoveryController.onPageLoad().url

        verify(mockService, never())
          .createVerificationBatchAndVerifications(any[UserAnswers], any[Seq[Long]], any())(any())
      }
    }

    "must return OK (placeholder) when current batch exists and not call service (TODO: modify later)" in {
      val mockService = mock[VerificationService]

      val ua =
        emptyUserAnswers
          .set(CurrentVerificationBatchResponsePage, nonEmptyCurrent)
          .success
          .value
          .set(SelectedSubcontractorsToVerifyPage, Seq(10L))
          .success
          .value

      val app =
        applicationBuilder(userAnswers = Some(ua))
          .overrides(bind[VerificationService].toInstance(mockService))
          .build()

      running(app) {
        val controller = app.injector.instanceOf[CreateVerificationBatchAndVerificationsController]

        val request = FakeRequest(POST, "/test-only")
        val result = controller.onSubmit()(request)

        status(result) mustBe OK
        contentAsString(result) must include("Current verification batch exists")

        verify(mockService, never())
          .createVerificationBatchAndVerifications(any[UserAnswers], any[Seq[Long]], any())(any())
      }
    }

    "must call service with distinct combined IDs when current batch is empty and then redirect to Index" in {
      val mockService = mock[VerificationService]

      val ua =
        emptyUserAnswers
          .set(CurrentVerificationBatchResponsePage, emptyCurrent)
          .success
          .value
          .set(SelectedSubcontractorsToVerifyPage, Seq(10L, 20L, 10L))
          .success
          .value
          .set(ReverifySubcontractorsPage, Seq(30L, 20L))
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
        val result = controller.onSubmit()(request)

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.IndexController.onPageLoad().url

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
          .set(SelectedSubcontractorsToVerifyPage, Seq(10L))
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
        val result = controller.onSubmit()(request)

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}