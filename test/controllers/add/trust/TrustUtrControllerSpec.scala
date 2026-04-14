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

package controllers.add.trust

import base.SpecBase
import controllers.routes
import forms.add.trust.TrustUtrFormProvider
import models.{NormalMode, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, verifyNoMoreInteractions, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.add.trust.{TrustNamePage, TrustUtrPage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.SubcontractorService
import uk.gov.hmrc.http.HeaderCarrier
import views.html.add.trust.TrustUtrView

import scala.concurrent.Future

class TrustUtrControllerSpec extends SpecBase with MockitoSugar {

  private val formProvider = new TrustUtrFormProvider()
  private val form         = formProvider()

  private val trustName = "Test Trust"

  private lazy val trustUtrRoute: String = controllers.add.trust.routes.TrustUtrController.onPageLoad(NormalMode).url

  private def uaWithName: UserAnswers =
    emptyUserAnswers.set(TrustNamePage, trustName).success.value

  "TrustUtr Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(uaWithName)).build()

      running(application) {
        val request = FakeRequest(GET, trustUtrRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[TrustUtrView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, trustName)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = uaWithName.set(TrustUtrPage, "7777777777").success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, trustUtrRoute)

        val view = application.injector.instanceOf[TrustUtrView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill("7777777777"), NormalMode, trustName)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val validValue = "5860920998"

      val mockSubcontractorService = mock[SubcontractorService]

      when(mockSubcontractorService.isDuplicateUTR(any[UserAnswers], any[String])(any[HeaderCarrier]))
        .thenReturn(Future.successful(false))

      val application =
        applicationBuilder(userAnswers = Some(uaWithName))
          .overrides(
            bind[SubcontractorService].toInstance(mockSubcontractorService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, trustUtrRoute)
            .withFormUrlEncodedBody(("value", validValue))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
      }

      verify(mockSubcontractorService).isDuplicateUTR(any[UserAnswers], any[String])(any[HeaderCarrier])
      verifyNoMoreInteractions(mockSubcontractorService)
    }

    "must return a Bad Request and show duplicate error when UTR already exists" in {

      val duplicatedUTR = "8888888888"

      val mockSubcontractorService = mock[SubcontractorService]

      when(mockSubcontractorService.isDuplicateUTR(any[UserAnswers], any[String])(any[HeaderCarrier]))
        .thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(uaWithName))
          .overrides(
            bind[SubcontractorService].toInstance(mockSubcontractorService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, trustUtrRoute)
            .withFormUrlEncodedBody(("value", duplicatedUTR))

        val boundForm              = form.bind(Map("value" -> duplicatedUTR))
        val formWithDuplicateError = boundForm.withError("value", "trustUtr.error.duplicate")

        val view = application.injector.instanceOf[TrustUtrView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(formWithDuplicateError, NormalMode, trustName)(
          request,
          messages(application)
        ).toString
      }

      verify(mockSubcontractorService).isDuplicateUTR(any[UserAnswers], any[String])(any[HeaderCarrier])
      verifyNoMoreInteractions(mockSubcontractorService)
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(uaWithName)).build()

      running(application) {
        val request =
          FakeRequest(POST, trustUtrRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[TrustUtrView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, trustName)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, trustUtrRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, trustUtrRoute)
            .withFormUrlEncodedBody(("value", "7777777777"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a GET when trust name is missing (userAnswers present)" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, trustUtrRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST when trust name is missing (userAnswers present)" in {

      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, trustUtrRoute)
            .withFormUrlEncodedBody("value" -> "7777777777")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
