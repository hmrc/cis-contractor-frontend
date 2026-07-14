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
import forms.add.trust.TrustContactMethodOptionsFormProvider
import models.add.trust.TrustContactMethodOptions
import models.contact.ContactMethodOptions
import models.{CheckMode, NormalMode, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.add.trust.*
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.add.trust.TrustContactMethodOptionsView

import scala.concurrent.Future

class TrustContactMethodOptionsControllerSpec extends SpecBase with MockitoSugar {

  private lazy val trustContactMethodOptionsRoute =
    controllers.add.trust.routes.TrustContactMethodOptionsController.onPageLoad(NormalMode).url

  private val formProvider = new TrustContactMethodOptionsFormProvider()
  private val form         = formProvider()

  private val trustName = "Test trustName"

  private def uaWithName: UserAnswers =
    emptyUserAnswers.set(TrustNamePage, trustName).success.value

  "TrustContactMethodOptions Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(uaWithName)).build()

      running(application) {
        val request = FakeRequest(GET, trustContactMethodOptionsRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[TrustContactMethodOptionsView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual view(form, NormalMode, trustName)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers =
        uaWithName.set(TrustContactMethodOptionsPage, TrustContactMethodOptions.values.toSet).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, trustContactMethodOptionsRoute)

        val view = application.injector.instanceOf[TrustContactMethodOptionsView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill(TrustContactMethodOptions.values.toSet),
          NormalMode,
          trustName
        )(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(uaWithName))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, trustContactMethodOptionsRoute)
            .withFormUrlEncodedBody(("value[0]", TrustContactMethodOptions.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.add.trust.routes.TrustEmailAddressController
          .onPageLoad(NormalMode)
          .url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted (name present)" in {

      val application = applicationBuilder(userAnswers = Some(uaWithName)).build()

      running(application) {
        val request =
          FakeRequest(POST, trustContactMethodOptionsRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[TrustContactMethodOptionsView]

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
        val request = FakeRequest(GET, trustContactMethodOptionsRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, trustContactMethodOptionsRoute)
            .withFormUrlEncodedBody(("value[0]", TrustContactMethodOptions.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to CYA in CheckMode when all selected contact methods already have answers" in {

      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val userAnswers = uaWithName
        .set(TrustContactMethodOptionsPage, Set(ContactMethodOptions.Email, ContactMethodOptions.Phone))
        .success
        .value
        .set(TrustEmailAddressPage, "test@example.com")
        .success
        .value
        .set(TrustPhoneNumberPage, "01234567890")
        .success
        .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, controllers.add.trust.routes.TrustContactMethodOptionsController.onPageLoad(CheckMode).url)
            .withFormUrlEncodedBody(
              ("value[0]", ContactMethodOptions.Email.toString),
              ("value[1]", ContactMethodOptions.Phone.toString)
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.add.trust.routes.TrustCheckYourAnswersController
          .onPageLoad()
          .url
      }
    }

    "must redirect to the first missing contact method page in CheckMode when the first selected method has no answer" in {

      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val userAnswers = uaWithName
        .set(TrustContactMethodOptionsPage, Set(ContactMethodOptions.Email, ContactMethodOptions.Phone))
        .success
        .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, controllers.add.trust.routes.TrustContactMethodOptionsController.onPageLoad(CheckMode).url)
            .withFormUrlEncodedBody(
              ("value[0]", ContactMethodOptions.Email.toString),
              ("value[1]", ContactMethodOptions.Phone.toString)
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.add.trust.routes.TrustEmailAddressController
          .onPageLoad(CheckMode)
          .url
      }
    }

    "must redirect to the next missing contact method page in CheckMode when the first method is answered but a later one is not" in {

      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val userAnswers = uaWithName
        .set(TrustContactMethodOptionsPage, Set(ContactMethodOptions.Email, ContactMethodOptions.Mobile))
        .success
        .value
        .set(TrustEmailAddressPage, "test@example.com")
        .success
        .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, controllers.add.trust.routes.TrustContactMethodOptionsController.onPageLoad(CheckMode).url)
            .withFormUrlEncodedBody(
              ("value[0]", ContactMethodOptions.Email.toString),
              ("value[1]", ContactMethodOptions.Mobile.toString)
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.add.trust.routes.TrustMobileNumberController
          .onPageLoad(CheckMode)
          .url
      }
    }

    "must return a Bad Request and errors when no value is submitted" in {

      val application = applicationBuilder(userAnswers = Some(uaWithName)).build()

      running(application) {
        val request =
          FakeRequest(POST, trustContactMethodOptionsRoute)
            .withFormUrlEncodedBody()

        val form      = new TrustContactMethodOptionsFormProvider()()
        val boundForm = form.bind(Map.empty)

        val view = application.injector.instanceOf[TrustContactMethodOptionsView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, trustName)(
          request,
          messages(application)
        ).toString

        contentAsString(result) must include(messages(application)("trustContactMethodOptions.error.required"))
      }
    }

    "must redirect to Journey Recovery for a GET when subcontractor name is missing (userAnswers present)" in {

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, trustContactMethodOptionsRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST when subcontractor name is missing (userAnswers present)" in {

      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, trustContactMethodOptionsRoute)
            .withFormUrlEncodedBody("value" -> "true")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
