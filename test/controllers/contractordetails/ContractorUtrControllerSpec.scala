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

package controllers.contractordetails

import base.SpecBase
import controllers.routes
import forms.contractordetails.ContractorUtrFormProvider
import models.{CheckMode, NormalMode}
import pages.contractordetails.ContractorUtrPage
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.contractordetails.ContractorUtrView

class ContractorUtrControllerSpec extends SpecBase {

  private val formProvider = new ContractorUtrFormProvider()
  private val form         = formProvider()

  private val validUtr   = "5860920998"
  private val invalidUtr = "1234567890"

  private lazy val contractorUtrRoute =
    controllers.contractordetails.routes.ContractorUtrController.onPageLoad(NormalMode).url

  "ContractorUtrController" - {

    "onPageLoad" - {

      "must return OK and the correct view for a GET with no existing answer" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, contractorUtrRoute)
          val result  = route(application, request).value
          val view    = application.injector.instanceOf[ContractorUtrView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
        }
      }

      "must populate the view correctly on a GET when the question has previously been answered" in {

        val userAnswers = emptyUserAnswers.set(ContractorUtrPage, validUtr).success.value
        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, contractorUtrRoute)
          val view    = application.injector.instanceOf[ContractorUtrView]
          val result  = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form.fill(validUtr), NormalMode)(
            request,
            messages(application)
          ).toString
        }
      }

      "must redirect to Journey Recovery for a GET if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, contractorUtrRoute)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }

    "onSubmit" - {

      "must save the answer and redirect on a valid POST" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          val request =
            FakeRequest(POST, contractorUtrRoute)
              .withFormUrlEncodedBody(("value", validUtr))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
        }
      }

      "must return Bad Request and errors for an empty submission" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          val request =
            FakeRequest(POST, contractorUtrRoute)
              .withFormUrlEncodedBody(("value", ""))

          val boundForm = form.bind(Map("value" -> ""))
          val view      = application.injector.instanceOf[ContractorUtrView]
          val result    = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
        }
      }

      "must return Bad Request and errors when UTR fails the check digit algorithm" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          val request =
            FakeRequest(POST, contractorUtrRoute)
              .withFormUrlEncodedBody(("value", invalidUtr))

          val boundForm = form.bind(Map("value" -> invalidUtr))
          val view      = application.injector.instanceOf[ContractorUtrView]
          val result    = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
        }
      }

      "must return Bad Request and errors when UTR is too short" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          val request =
            FakeRequest(POST, contractorUtrRoute)
              .withFormUrlEncodedBody(("value", "12345"))

          val boundForm = form.bind(Map("value" -> "12345"))
          val view      = application.injector.instanceOf[ContractorUtrView]
          val result    = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
        }
      }

      "must return Bad Request and errors when UTR contains non-numeric characters" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          val request =
            FakeRequest(POST, contractorUtrRoute)
              .withFormUrlEncodedBody(("value", "12345abcde"))

          val boundForm = form.bind(Map("value" -> "12345abcde"))
          val view      = application.injector.instanceOf[ContractorUtrView]
          val result    = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
        }
      }

      "must redirect to Journey Recovery for a POST if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request =
            FakeRequest(POST, contractorUtrRoute)
              .withFormUrlEncodedBody(("value", validUtr))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to Journey Recovery for a POST in CheckMode if no existing data is found" in {

        val checkRoute =
          controllers.contractordetails.routes.ContractorUtrController.onPageLoad(CheckMode).url

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request =
            FakeRequest(POST, checkRoute)
              .withFormUrlEncodedBody(("value", validUtr))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }
  }
}
