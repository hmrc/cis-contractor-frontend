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

package controllers.add.company

import base.SpecBase
import forms.add.company.CompanyContactOptionsFormProvider
import models.add.company.CompanyContactOptions
import models.contact.ContactOptions
import models.{CheckMode, NormalMode, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.add.company.{CompanyContactOptionsPage, CompanyNamePage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.add.company.CompanyContactOptionsView

import scala.concurrent.Future

class CompanyContactOptionsControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new CompanyContactOptionsFormProvider()
  val form         = formProvider()

  private val companyName = "Some Company LLP"

  private def uaWithName: UserAnswers =
    emptyUserAnswers.set(CompanyNamePage, companyName).success.value

  lazy private val companyContactOptionsRoute =
    controllers.add.company.routes.CompanyContactOptionsController.onPageLoad(NormalMode).url

  lazy private val companyContactOptionsCheckRoute =
    controllers.add.company.routes.CompanyContactOptionsController.onPageLoad(CheckMode).url

  "CompanyContactOptions Controller" - {

    "must return OK and the correct view for a GET" in {

      val userAnswers = UserAnswers(userAnswersId).set(CompanyNamePage, companyName).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, companyContactOptionsRoute)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[CompanyContactOptionsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, companyName)(
          request,
          messages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId)
        .set(CompanyContactOptionsPage, ContactOptions.Email: CompanyContactOptions)
        .flatMap(_.set(CompanyNamePage, companyName))
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, companyContactOptionsRoute)
        val view    = application.injector.instanceOf[CompanyContactOptionsView]
        val result  = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill(ContactOptions.Email: CompanyContactOptions),
          NormalMode,
          companyName
        )(
          request,
          messages(application)
        ).toString
      }
    }

    "must save the answer and redirect to next page when valid data is submitted" in {

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
          FakeRequest(POST, companyContactOptionsRoute)
            .withFormUrlEncodedBody(("value", ContactOptions.Phone.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(
          result
        ).value mustEqual controllers.add.company.routes.CompanyContactOptionsController
          .onPageLoad(NormalMode)
          .url
      }
    }

    "must return a Bad Request and error when no value is submitted" in {

      val application = applicationBuilder(userAnswers = Some(uaWithName)).build()

      running(application) {
        val request =
          FakeRequest(POST, companyContactOptionsRoute)
            .withFormUrlEncodedBody()

        val form      = new CompanyContactOptionsFormProvider()()
        val boundForm = form.bind(Map.empty)

        val view = application.injector.instanceOf[CompanyContactOptionsView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, companyName)(
          request,
          messages(application)
        ).toString

        contentAsString(result) must include(messages(application)("companyContactOptions.error.required"))
      }
    }

    "must redirect to Journey Recovery for a GET if company name is missing" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, companyContactOptionsRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if company name is missing" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, companyContactOptionsRoute)
            .withFormUrlEncodedBody(("value", ContactOptions.Mobile.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "CheckMode GET must return OK and correct view" in {
      val userAnswers =
        UserAnswers(userAnswersId).set(CompanyNamePage, companyName).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, companyContactOptionsCheckRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CompanyContactOptionsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, CheckMode, companyName)(
          request,
          messages(application)
        ).toString
      }
    }

    "CheckMode GET must populate the view correctly when the question has previously been answered for company" in {

      val userAnswers = UserAnswers(userAnswersId)
        .set(CompanyContactOptionsPage, ContactOptions.Mobile: CompanyContactOptions)
        .flatMap(_.set(CompanyNamePage, companyName))
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, companyContactOptionsCheckRoute)

        val view = application.injector.instanceOf[CompanyContactOptionsView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill(ContactOptions.Mobile),
          CheckMode,
          companyName
        )(
          request,
          messages(application)
        ).toString
      }
    }

    "CheckMode POST must redirect to Journey Recovery for a GET if company name is missing" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, companyContactOptionsCheckRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "CheckMode POST must redirect to Journey Recovery for a POST if company name is missing" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, companyContactOptionsCheckRoute)
            .withFormUrlEncodedBody(("value", ContactOptions.Phone.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

  }
}
