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

package controllers

import base.SpecBase
import forms.IndividualContactMethodOptionsFormProvider
import models.add.SubcontractorName
import models.{IndividualContactMethodOptions, NormalMode, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.IndividualContactMethodOptionsPage
import pages.add.SubcontractorNamePage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.IndividualContactMethodOptionsView

import scala.concurrent.Future

class IndividualContactMethodOptionsControllerSpec extends SpecBase with MockitoSugar {
  private lazy val individualContactMethodOptionsRoute =
    controllers.routes.IndividualContactMethodOptionsController.onPageLoad(NormalMode).url

  private val formProvider = new IndividualContactMethodOptionsFormProvider()
  private val form = formProvider()

  private val subcontractorName = SubcontractorName("John", Some("Paul"), "Smith")

  private val name = "John Smith"

  private def uaWithName: UserAnswers =
    emptyUserAnswers.set(SubcontractorNamePage, subcontractorName).success.value

  "IndividualContactMethodOptions Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(uaWithName)).build()

      running(application) {
        val request = FakeRequest(GET, individualContactMethodOptionsRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[IndividualContactMethodOptionsView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual view(form, NormalMode, name)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers =
        uaWithName.set(IndividualContactMethodOptionsPage, IndividualContactMethodOptions.values.toSet).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, individualContactMethodOptionsRoute)

        val view = application.injector.instanceOf[IndividualContactMethodOptionsView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill(IndividualContactMethodOptions.values.toSet),
          NormalMode,
          name
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
          FakeRequest(POST, individualContactMethodOptionsRoute)
            .withFormUrlEncodedBody(("value[0]", IndividualContactMethodOptions.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.IndexController
          .onPageLoad()
          .url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted (name present)" in {

      val application = applicationBuilder(userAnswers = Some(uaWithName)).build()

      running(application) {
        val request =
          FakeRequest(POST, individualContactMethodOptionsRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[IndividualContactMethodOptionsView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, name)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, individualContactMethodOptionsRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, individualContactMethodOptionsRoute)
            .withFormUrlEncodedBody(("value[0]", IndividualContactMethodOptions.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must return a Bad Request and errors when no value is submitted" in {

      val application = applicationBuilder(userAnswers = Some(uaWithName)).build()

      running(application) {
        val request =
          FakeRequest(POST, individualContactMethodOptionsRoute)
            .withFormUrlEncodedBody()

        val form = new IndividualContactMethodOptionsFormProvider()()
        val boundForm = form.bind(Map.empty)

        val view = application.injector.instanceOf[IndividualContactMethodOptionsView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, name)(
          request,
          messages(application)
        ).toString

        contentAsString(result) must include(messages(application)("individualContactMethodOptions.error.required"))
      }
    }

    "must redirect to Journey Recovery for a GET when subcontractor name is missing (userAnswers present)" in {

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, individualContactMethodOptionsRoute)

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
          FakeRequest(POST, individualContactMethodOptionsRoute)
            .withFormUrlEncodedBody("value" -> "true")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
