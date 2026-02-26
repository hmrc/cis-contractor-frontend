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

import forms.add.company.CompanyContactDetailsOptionsFormProvider
import models.add.company.CompanyContactDetailsOptions
import models.{CheckMode, NormalMode, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.add.company.{CompanyContactDetailsOptionsPage, CompanyNamePage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.add.company.CompanyContactDetailsOptionsView

import scala.concurrent.Future

class CompanyContactDetailsOptionsControllerSpec extends SpecBase with MockitoSugar {

  val formProvider = new CompanyContactDetailsOptionsFormProvider()
  val form = formProvider()

  private val companyName = "Some Company LLP"

  private def uaWithName: UserAnswers =
    emptyUserAnswers.set(CompanyNamePage, companyName).success.value

  lazy private val companyContactDetailsOptionRoute =
    controllers.add.company.routes.CompanyContactDetailsOptionsController.onPageLoad(NormalMode).url

  lazy private val companyContactDetailsOptionCheckRoute =
    controllers.add.company.routes.CompanyContactDetailsOptionsController.onPageLoad(CheckMode).url
  

  "CompanyContactDetailsOptions Controller" - {

    "must return OK and the correct view for a GET" in {

      val userAnswers =
        UserAnswers(userAnswersId).set(CompanyNamePage, companyName).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, companyContactDetailsOptionRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CompanyContactDetailsOptionsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, companyName)(
          request,
          messages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId)
        .set(CompanyContactDetailsOptionsPage, CompanyContactDetailsOptions.EmailAddress)
        .flatMap(_.set(CompanyNamePage, companyName))
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, companyContactDetailsOptionRoute)

        val view = application.injector.instanceOf[CompanyContactDetailsOptionsView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill(CompanyContactDetailsOptions.EmailAddress),
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
          FakeRequest(POST, companyContactDetailsOptionRoute)
            .withFormUrlEncodedBody(("value", CompanyContactDetailsOptions.PhoneNumber.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(
          result
        ).value mustEqual controllers.add.company.routes.CompanyContactDetailsOptionsController
          .onPageLoad(NormalMode)
          .url
      }
    }

    "must return a Bad Request and error when no value is submitted" in {

      val application = applicationBuilder(userAnswers = Some(uaWithName)).build()

      running(application) {
        val request =
          FakeRequest(POST, companyContactDetailsOptionRoute)
            .withFormUrlEncodedBody()

        val form = new CompanyContactDetailsOptionsFormProvider()()
        val boundForm = form.bind(Map.empty)

        val view = application.injector.instanceOf[CompanyContactDetailsOptionsView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, companyName)(
          request,
          messages(application)
        ).toString

        contentAsString(result) must include(messages(application)("companyContactDetailsOptions.error.required"))
      }
    }

    "must redirect to Journey Recovery for a GET if company name is missing" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, companyContactDetailsOptionRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if company name is missing" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, companyContactDetailsOptionRoute)
            .withFormUrlEncodedBody(("value", CompanyContactDetailsOptions.MobileNumber.toString))

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
        val request = FakeRequest(GET, companyContactDetailsOptionCheckRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CompanyContactDetailsOptionsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, CheckMode, companyName)(
          request,
          messages(application)
        ).toString
      }
    }

    "CheckMode GET must populate the view correctly when the question has previously been answered for company" in {

      val userAnswers = UserAnswers(userAnswersId)
        .set(CompanyContactDetailsOptionsPage, CompanyContactDetailsOptions.MobileNumber)
        .flatMap(_.set(CompanyNamePage, companyName))
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, companyContactDetailsOptionCheckRoute)

        val view = application.injector.instanceOf[CompanyContactDetailsOptionsView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill(CompanyContactDetailsOptions.MobileNumber),
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
        val request = FakeRequest(GET, companyContactDetailsOptionCheckRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
    
    "CheckMode POST must redirect to Journey Recovery for a POST if company name is missing" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, companyContactDetailsOptionCheckRoute)
            .withFormUrlEncodedBody(("value", CompanyContactDetailsOptions.PhoneNumber.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
    
  }
}
