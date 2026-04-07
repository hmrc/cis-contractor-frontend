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
import forms.add.trust.TrustContactOptionsFormProvider
import models.contact.ContactOptions
import models.{CheckMode, NormalMode, UserAnswers}
import navigation.Navigator
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.add.trust.{TrustContactOptionsPage, TrustNamePage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.add.trust.TrustContactOptionsView

import scala.concurrent.Future

class TrustContactOptionsControllerSpec extends SpecBase with MockitoSugar {

  private val formProvider = new TrustContactOptionsFormProvider()
  private val form         = formProvider()

  private val trustName = "Test trustName"

  private def uaWithName: UserAnswers = emptyUserAnswers.set(TrustNamePage, trustName).success.value

  lazy val trustContactOptionsRoute: String =
    controllers.add.trust.routes.TrustContactOptionsController.onPageLoad(NormalMode).url

  lazy val trustContactOptionsCheckRoute: String =
    controllers.add.trust.routes.TrustContactOptionsController.onPageLoad(CheckMode).url

  "TrustContactOptions Controller" - {

    "must return OK and the correct view for a GET" in {
      val userAnswers =
        UserAnswers(userAnswersId).set(TrustNamePage, trustName).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, trustContactOptionsRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[TrustContactOptionsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, trustName)(
          request,
          messages(application)
        ).toString
      }
    }

    "must populate the view correctly on GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId)
        .set(TrustContactOptionsPage, ContactOptions.Email)
        .flatMap(_.set(TrustNamePage, trustName))
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, trustContactOptionsRoute)

        val view = application.injector.instanceOf[TrustContactOptionsView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill(ContactOptions.Email),
          NormalMode,
          trustName
        )(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]
      val mockNavigator         = mock[Navigator]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
      when(mockNavigator.nextPage(any(), any(), any())).thenReturn(Call("GET", "/foo"))

      val application =
        applicationBuilder(userAnswers = Some(uaWithName))
          .overrides(
            bind[Navigator].toInstance(mockNavigator),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, trustContactOptionsRoute)
            .withFormUrlEncodedBody(("value", ContactOptions.Email.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual "/foo"
      }
    }

    "must return a Bad Request and errors when no value is submitted" in {

      val application = applicationBuilder(userAnswers = Some(uaWithName)).build()

      running(application) {
        val request =
          FakeRequest(POST, trustContactOptionsRoute)
            .withFormUrlEncodedBody()

        val form      = new TrustContactOptionsFormProvider()()
        val boundForm = form.bind(Map.empty)

        val view = application.injector.instanceOf[TrustContactOptionsView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, trustName)(
          request,
          messages(application)
        ).toString

        contentAsString(result) must include(messages(application)("trustContactOptions.error.required"))
      }
    }

    "must redirect to Journey Recovery for a GET if partnership name is missing" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, trustContactOptionsRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if trust name is missing" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, trustContactOptionsRoute)
            .withFormUrlEncodedBody(("value", ContactOptions.Mobile.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "CheckMode GET must return OK and the correct view" in {
      val userAnswers =
        UserAnswers(userAnswersId).set(TrustNamePage, trustName).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, trustContactOptionsCheckRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[TrustContactOptionsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, CheckMode, trustName)(
          request,
          messages(application)
        ).toString
      }
    }

    "CheckMode GET must populate the view correctly when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId)
        .set(TrustContactOptionsPage, ContactOptions.Mobile)
        .flatMap(_.set(TrustNamePage, trustName))
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, trustContactOptionsCheckRoute)

        val view = application.injector.instanceOf[TrustContactOptionsView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill(ContactOptions.Mobile),
          CheckMode,
          trustName
        )(
          request,
          messages(application)
        ).toString
      }
    }

    "CheckMode POST must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]
      val mockNavigator         = mock[Navigator]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
      when(mockNavigator.nextPage(any(), any(), any())).thenReturn(Call("GET", "/foo"))

      val application =
        applicationBuilder(userAnswers = Some(uaWithName))
          .overrides(
            bind[Navigator].toInstance(mockNavigator),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, trustContactOptionsCheckRoute)
            .withFormUrlEncodedBody(("value", ContactOptions.Phone.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual "/foo"
      }
    }

    "CheckMode POST must redirect to Journey Recovery for a POST if trust name is missing" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, trustContactOptionsCheckRoute)
            .withFormUrlEncodedBody(("value", ContactOptions.Phone.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "CheckMode POST must redirect to Journey Recovery for a POST if partnership name is missing" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, trustContactOptionsCheckRoute)
            .withFormUrlEncodedBody(("value", ContactOptions.Phone.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

  }
}
