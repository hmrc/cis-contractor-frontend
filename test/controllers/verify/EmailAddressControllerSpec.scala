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
import forms.verify.EmailAddressFormProvider
import models.{NormalMode, UserAnswers}
import navigation.Navigator
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.verify.{ContractorEmailConfirmationNotStoredPage, EmailAddressPage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.verify.EmailAddressView

import scala.concurrent.Future

class EmailAddressControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new EmailAddressFormProvider()
  val form         = formProvider()

  lazy val emailAddressRoute = controllers.verify.routes.EmailAddressController.onPageLoad(NormalMode).url

  private def uaWithAlternateEmail(email: String): UserAnswers =
    emptyUserAnswers
      .set(ContractorEmailConfirmationNotStoredPage, true)
      .success
      .value
      .set(EmailAddressPage, email)
      .success
      .value

  private def uaWithNoAlternateEmail: UserAnswers =
    emptyUserAnswers
      .set(ContractorEmailConfirmationNotStoredPage, false)
      .success
      .value

  "EmailAddress Controller" - {

    "must return OK and the correct view for a GET when email is NOT stored" in {

      val application =
        applicationBuilder(userAnswers = Some(uaWithNoAlternateEmail)).build()

      running(application) {
        val request = FakeRequest(GET, emailAddressRoute)
        val view    = application.injector.instanceOf[EmailAddressView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(
            form,
            NormalMode,
            "verify.emailAddress.hint.notStored"
          )(request, messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET when email IS stored (alternate email confirmation)" in {

      val application =
        applicationBuilder(
          userAnswers = Some(uaWithAlternateEmail("stored@example.com"))
        ).build()

      running(application) {
        val request = FakeRequest(GET, emailAddressRoute)
        val view    = application.injector.instanceOf[EmailAddressView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(
            form.fill("stored@example.com"),
            NormalMode,
            "verify.emailAddress.hint"
          )(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers =
        uaWithAlternateEmail("abc@test.com")

      val application =
        applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, emailAddressRoute)
        val view    = application.injector.instanceOf[EmailAddressView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(
            form.fill("abc@test.com"),
            NormalMode,
            "verify.emailAddress.hint"
          )(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]
      val mockNavigator         = mock[Navigator]

      when(mockNavigator.nextPage(any(), any(), any())).thenReturn(onwardRoute)
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(uaWithNoAlternateEmail))
          .overrides(
            bind[Navigator].toInstance(mockNavigator),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, emailAddressRoute)
            .withFormUrlEncodedBody("value" -> "abc@test.com")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted and email is NOT stored" in {

      val application =
        applicationBuilder(userAnswers = Some(uaWithNoAlternateEmail)).build()

      running(application) {
        val request =
          FakeRequest(POST, emailAddressRoute)
            .withFormUrlEncodedBody("value" -> "")

        val boundForm = form.bind(Map("value" -> ""))
        val view      = application.injector.instanceOf[EmailAddressView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual
          view(
            boundForm,
            NormalMode,
            "verify.emailAddress.hint.notStored"
          )(request, messages(application)).toString
      }
    }

    "must return a Bad Request and errors when invalid data is submitted and email IS stored" in {

      val application =
        applicationBuilder(userAnswers = Some(uaWithAlternateEmail("stored@example.com"))).build()

      running(application) {
        val request =
          FakeRequest(POST, emailAddressRoute)
            .withFormUrlEncodedBody("value" -> "")

        val boundForm = form.bind(Map("value" -> ""))
        val view      = application.injector.instanceOf[EmailAddressView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual
          view(
            boundForm,
            NormalMode,
            "verify.emailAddress.hint"
          )(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application =
        applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, emailAddressRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application =
        applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, emailAddressRoute)
            .withFormUrlEncodedBody("value" -> "abc@test.com")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
