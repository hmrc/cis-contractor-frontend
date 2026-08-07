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
import forms.insufficient.ProceedInsufficientSubcontractorNameYesNoFormProvider
import models.{NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.insufficient.ProceedInsufficientSubcontractorNameYesNoPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.ReviewInsufficientInfoService
import utils.SubcontractorNameExtractor
import views.html.insufficient.ProceedInsufficientSubcontractorNameYesNoView

import scala.concurrent.Future

class ProceedInsufficientSubcontractorNameYesNoControllerSpec extends SpecBase with MockitoSugar {

  private val formProvider = new ProceedInsufficientSubcontractorNameYesNoFormProvider()

  private val form = formProvider()

  private val subcontractorName = "Test Subcontractor"

  private val subcontractorId = 10

  private val mode = NormalMode

  private def onwardRoute: Call = Call("GET", "/foo")

  private lazy val proceedInsufficientSubcontractorNameYesNoRoute =
    controllers.insufficient.routes.ProceedInsufficientSubcontractorNameYesNoController
      .onPageLoad(subcontractorId)
      .url

  "ProceedInsufficientSubcontractorNameYesNo Controller" - {

    "must return OK and the correct view for a GET" in {

      val mockService = mock[ReviewInsufficientInfoService]

      //      when(
      //        mockSubcontractorNameExtractor.getSubcontractorName(any())
      //      ).thenReturn(Some(subcontractorName))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[ReviewInsufficientInfoService].toInstance(mockService))
          .build()

      running(application) {

        val request = FakeRequest(GET, proceedInsufficientSubcontractorNameYesNoRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ProceedInsufficientSubcontractorNameYesNoView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(
            form,
            mode,
            subcontractorName,
            subcontractorId
          )(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId)
        .set(ProceedInsufficientSubcontractorNameYesNoPage, true)
        .success
        .value

      val mockService = mock[ReviewInsufficientInfoService]

//      when(
//        mockSubcontractorNameExtractor.getSubcontractorName(any())
//      ).thenReturn(Some(subcontractorName))

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[ReviewInsufficientInfoService].toInstance(mockService))
          .build()

      running(application) {

        val request = FakeRequest(GET, proceedInsufficientSubcontractorNameYesNoRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ProceedInsufficientSubcontractorNameYesNoView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(
            form.fill(true),
            mode,
            subcontractorName,
            subcontractorId
          )(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      val mockService = mock[ReviewInsufficientInfoService]
//      when(
//        mockSubcontractorNameExtractor.getSubcontractorName(any())
//      ).thenReturn(Some(subcontractorName))

      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[ReviewInsufficientInfoService].toInstance(mockService)
          )
          .build()

      running(application) {

        val request =
          FakeRequest(POST, proceedInsufficientSubcontractorNameYesNoRoute).withFormUrlEncodedBody("value" -> "true")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val mockService = mock[ReviewInsufficientInfoService]

//      when(
//        mockSubcontractorNameExtractor.getSubcontractorName(any())
//      ).thenReturn(Some(subcontractorName))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[ReviewInsufficientInfoService].toInstance(mockService))
        .build()

      running(application) {

        val request =
          FakeRequest(POST, proceedInsufficientSubcontractorNameYesNoRoute).withFormUrlEncodedBody("value" -> "")

        val boundForm = form.bind(Map("value" -> ""))

        val result = route(application, request).value

        val view = application.injector.instanceOf[ProceedInsufficientSubcontractorNameYesNoView]

        status(result) mustEqual BAD_REQUEST

        contentAsString(result) mustEqual
          view(
            boundForm,
            mode,
            subcontractorName,
            subcontractorId
          )(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val mockService = mock[ReviewInsufficientInfoService]

      //      when(
      //        mockSubcontractorNameExtractor.getSubcontractorName(any())
      //      ).thenReturn(Some(subcontractorName))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[ReviewInsufficientInfoService].toInstance(mockService))
        .build()

      running(application) {

        val request = FakeRequest(GET, proceedInsufficientSubcontractorNameYesNoRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val mockService = mock[ReviewInsufficientInfoService]

      //      when(
      //        mockSubcontractorNameExtractor.getSubcontractorName(any())
      //      ).thenReturn(Some(subcontractorName))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[ReviewInsufficientInfoService].toInstance(mockService))
        .build()

      running(application) {

        val request =
          FakeRequest(POST, proceedInsufficientSubcontractorNameYesNoRoute).withFormUrlEncodedBody("value" -> "true")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          routes.JourneyRecoveryController
            .onPageLoad()
            .url
      }
    }
  }
}
