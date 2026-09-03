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

package controllers.insufficient

import base.SpecBase
import controllers.routes
import controllers.verify.CheckVerificationBatchReadinessController
import forms.insufficient.RemoveInsufficientSubcontractorNameYesNoFormProvider
import models.{NormalMode, UserAnswers}
import models.response.DeleteVerificationResponse
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{never, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.insufficient.RemoveInsufficientSubcontractorNameYesNoPage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.VerificationService
import utils.SubcontractorNameExtractor
import views.html.insufficient.RemoveInsufficientSubcontractorNameYesNoView
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class RemoveInsufficientSubcontractorNameYesNoControllerSpec extends SpecBase with MockitoSugar {

  private val formProvider =
    new RemoveInsufficientSubcontractorNameYesNoFormProvider()

  private val form =
    formProvider()

  private val subcontractorName =
    "Test Subcontractor"

  private val mode =
    NormalMode

  private val verificationResourceRef = 12345L

  private lazy val removeInsufficientSubcontractorNameYesNoRoute =
    controllers.insufficient.routes.RemoveInsufficientSubcontractorNameYesNoController
      .onPageLoad()
      .url

  "RemoveInsufficientSubcontractorNameYesNo Controller" - {

    "must return OK and the correct view for a GET" in {

      val mockSubcontractorNameExtractor =
        mock[SubcontractorNameExtractor]

      when(
        mockSubcontractorNameExtractor.getSubcontractorName(any())
      ).thenReturn(Some(subcontractorName))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[SubcontractorNameExtractor]
              .toInstance(mockSubcontractorNameExtractor)
          )
          .build()

      running(application) {

        val request =
          FakeRequest(
            GET,
            removeInsufficientSubcontractorNameYesNoRoute
          )

        val result =
          route(application, request).value

        val view =
          application.injector
            .instanceOf[RemoveInsufficientSubcontractorNameYesNoView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(
            form,
            mode,
            subcontractorName
          )(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers =
        UserAnswers(userAnswersId)
          .set(
            RemoveInsufficientSubcontractorNameYesNoPage(verificationResourceRef),
            true
          )
          .success
          .value

      val mockSubcontractorNameExtractor =
        mock[SubcontractorNameExtractor]

      when(
        mockSubcontractorNameExtractor.getSubcontractorName(any())
      ).thenReturn(Some(subcontractorName))

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[SubcontractorNameExtractor]
              .toInstance(mockSubcontractorNameExtractor)
          )
          .build()

      running(application) {

        val request =
          FakeRequest(
            GET,
            controllers.insufficient.routes.RemoveInsufficientSubcontractorNameYesNoController
              .onPageLoad(verificationResourceRef)
              .url
          )

        val result =
          route(application, request).value

        val view =
          application.injector
            .instanceOf[RemoveInsufficientSubcontractorNameYesNoView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(
            form.fill(true),
            mode,
            subcontractorName,
            verificationResourceRef
          )(request, messages(application)).toString
      }
    }

    "must not populate the view with an answer saved for a different subcontractor" in {

      val otherVerificationResourceRef = 67890L

      val userAnswers =
        UserAnswers(userAnswersId)
          .set(
            RemoveInsufficientSubcontractorNameYesNoPage(otherVerificationResourceRef),
            false
          )
          .success
          .value

      val mockSubcontractorNameExtractor =
        mock[SubcontractorNameExtractor]

      when(
        mockSubcontractorNameExtractor.getSubcontractorName(any())
      ).thenReturn(Some(subcontractorName))

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[SubcontractorNameExtractor]
              .toInstance(mockSubcontractorNameExtractor)
          )
          .build()

      running(application) {

        val request =
          FakeRequest(
            GET,
            controllers.insufficient.routes.RemoveInsufficientSubcontractorNameYesNoController
              .onPageLoad(verificationResourceRef)
              .url
          )

        val result =
          route(application, request).value

        val view =
          application.injector
            .instanceOf[RemoveInsufficientSubcontractorNameYesNoView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(
            form,
            mode,
            subcontractorName,
            verificationResourceRef
          )(request, messages(application)).toString
      }
    }

    "must call delete verification and redirect to newest verification batch when remaining insufficient subcontractors exist" in {

      val mockSessionRepository =
        mock[SessionRepository]

      val mockSubcontractorNameExtractor =
        mock[SubcontractorNameExtractor]

      val mockVerificationService =
        mock[VerificationService]

      val mockCheckVerificationBatchReadinessController =
        mock[CheckVerificationBatchReadinessController]

      when(
        mockSubcontractorNameExtractor.getSubcontractorName(any())
      ).thenReturn(Some(subcontractorName))

      when(
        mockSessionRepository.set(any())
      ).thenReturn(Future.successful(true))

      when(
        mockSessionRepository.get(eqTo(userAnswersId))
      ).thenReturn(Future.successful(Some(emptyUserAnswers)))

      when(
        mockVerificationService.deleteVerification(
          any[UserAnswers],
          eqTo(verificationResourceRef)
        )(any[HeaderCarrier])
      ).thenReturn(
        Future.successful(DeleteVerificationResponse(Some(1L)))
      )

      when(
        mockCheckVerificationBatchReadinessController.updateVerificationBatchReadiness(any[UserAnswers])
      ).thenReturn(Future.successful(Some(emptyUserAnswers)))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[SessionRepository]
              .toInstance(mockSessionRepository),
            bind[VerificationService]
              .toInstance(mockVerificationService),
            bind[CheckVerificationBatchReadinessController]
              .toInstance(mockCheckVerificationBatchReadinessController),
            bind[SubcontractorNameExtractor]
              .toInstance(mockSubcontractorNameExtractor)
          )
          .build()

      running(application) {

        val request =
          FakeRequest(
            POST,
            controllers.insufficient.routes.RemoveInsufficientSubcontractorNameYesNoController
              .onSubmit(verificationResourceRef)
              .url
          )
            .withFormUrlEncodedBody(
              "value" -> "true"
            )

        val result =
          route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.verify.routes.NewestVerificationBatchController.onPageLoad().url

        verify(mockVerificationService)
          .deleteVerification(any[UserAnswers], eqTo(verificationResourceRef))(any[HeaderCarrier])

        verify(mockCheckVerificationBatchReadinessController)
          .updateVerificationBatchReadiness(any[UserAnswers])

        val savedAnswersCaptor =
          ArgumentCaptor.forClass(classOf[UserAnswers])

        verify(mockSessionRepository)
          .set(savedAnswersCaptor.capture())

        savedAnswersCaptor.getValue
          .get(RemoveInsufficientSubcontractorNameYesNoPage(verificationResourceRef)) mustBe None
      }
    }

    "must call delete verification and redirect to newest verification batch when no insufficient subcontractors remain" in {

      val mockSessionRepository =
        mock[SessionRepository]

      val mockSubcontractorNameExtractor =
        mock[SubcontractorNameExtractor]

      val mockVerificationService =
        mock[VerificationService]

      val mockCheckVerificationBatchReadinessController =
        mock[CheckVerificationBatchReadinessController]

      when(
        mockSubcontractorNameExtractor.getSubcontractorName(any())
      ).thenReturn(Some(subcontractorName))

      when(
        mockSessionRepository.set(any())
      ).thenReturn(Future.successful(true))

      when(
        mockVerificationService.deleteVerification(
          any[UserAnswers],
          eqTo(verificationResourceRef)
        )(any[HeaderCarrier])
      ).thenReturn(
        Future.successful(DeleteVerificationResponse(Some(0L)))
      )

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[SessionRepository]
              .toInstance(mockSessionRepository),
            bind[VerificationService]
              .toInstance(mockVerificationService),
            bind[CheckVerificationBatchReadinessController]
              .toInstance(mockCheckVerificationBatchReadinessController),
            bind[SubcontractorNameExtractor]
              .toInstance(mockSubcontractorNameExtractor)
          )
          .build()

      running(application) {

        val request =
          FakeRequest(
            POST,
            controllers.insufficient.routes.RemoveInsufficientSubcontractorNameYesNoController
              .onSubmit(verificationResourceRef)
              .url
          )
            .withFormUrlEncodedBody(
              "value" -> "true"
            )

        val result =
          route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.verify.routes.NewestVerificationBatchController.onPageLoad().url

        verify(mockCheckVerificationBatchReadinessController, never())
          .updateVerificationBatchReadiness(any[UserAnswers])
      }
    }

    "must redirect back to review page without deleting when user selects no" in {

      val mockSessionRepository =
        mock[SessionRepository]

      val mockSubcontractorNameExtractor =
        mock[SubcontractorNameExtractor]

      val mockVerificationService =
        mock[VerificationService]

      when(
        mockSubcontractorNameExtractor.getSubcontractorName(any())
      ).thenReturn(Some(subcontractorName))

      when(
        mockSessionRepository.set(any())
      ).thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[SessionRepository]
              .toInstance(mockSessionRepository),
            bind[VerificationService]
              .toInstance(mockVerificationService),
            bind[SubcontractorNameExtractor]
              .toInstance(mockSubcontractorNameExtractor)
          )
          .build()

      running(application) {

        val request =
          FakeRequest(
            POST,
            controllers.insufficient.routes.RemoveInsufficientSubcontractorNameYesNoController
              .onSubmit(verificationResourceRef)
              .url
          )
            .withFormUrlEncodedBody(
              "value" -> "false"
            )

        val result =
          route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.verify.routes.ReviewInsufficientInfoSubcontractorsController.onPageLoad().url

        verify(mockVerificationService, never())
          .deleteVerification(any[UserAnswers], any[Long])(any[HeaderCarrier])
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val mockSubcontractorNameExtractor =
        mock[SubcontractorNameExtractor]

      when(
        mockSubcontractorNameExtractor.getSubcontractorName(any())
      ).thenReturn(Some(subcontractorName))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[SubcontractorNameExtractor]
              .toInstance(mockSubcontractorNameExtractor)
          )
          .build()

      running(application) {

        val request =
          FakeRequest(
            POST,
            removeInsufficientSubcontractorNameYesNoRoute
          )
            .withFormUrlEncodedBody(
              "value" -> ""
            )

        val boundForm =
          form.bind(
            Map(
              "value" -> ""
            )
          )

        val result =
          route(application, request).value

        val view =
          application.injector
            .instanceOf[RemoveInsufficientSubcontractorNameYesNoView]

        status(result) mustEqual BAD_REQUEST

        contentAsString(result) mustEqual
          view(
            boundForm,
            mode,
            subcontractorName
          )(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val mockSubcontractorNameExtractor =
        mock[SubcontractorNameExtractor]

      when(
        mockSubcontractorNameExtractor.getSubcontractorName(any())
      ).thenReturn(None)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[SubcontractorNameExtractor]
              .toInstance(mockSubcontractorNameExtractor)
          )
          .build()

      running(application) {

        val request =
          FakeRequest(
            GET,
            removeInsufficientSubcontractorNameYesNoRoute
          )

        val result =
          route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          routes.JourneyRecoveryController
            .onPageLoad()
            .url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val mockSubcontractorNameExtractor =
        mock[SubcontractorNameExtractor]

      when(
        mockSubcontractorNameExtractor.getSubcontractorName(any())
      ).thenReturn(None)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[SubcontractorNameExtractor]
              .toInstance(mockSubcontractorNameExtractor)
          )
          .build()

      running(application) {

        val request =
          FakeRequest(
            POST,
            removeInsufficientSubcontractorNameYesNoRoute
          )
            .withFormUrlEncodedBody(
              "value" -> "true"
            )

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
}
