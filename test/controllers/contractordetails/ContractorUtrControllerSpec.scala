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

import base.WithFakeCisTaxpayer
import controllers.routes
import forms.contractordetails.ContractorUtrFormProvider
import models.{CheckMode, NormalMode}
import pages.contractordetails.ContractorUtrPage
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.contractordetails.ContractorUtrView

import java.util.UUID

class ContractorUtrControllerSpec extends WithFakeCisTaxpayer {

  private val formProvider = new ContractorUtrFormProvider()
  private val form         = formProvider()

  private val validUtr   = "5860920998"
  private val invalidUtr = "1234567890"

  private val flowId =
    UUID.fromString("0f1af513-154f-4548-a24c-2d297f7b57be")

  private lazy val contractorUtrRoute =
    controllers.contractordetails.routes.ContractorUtrController
      .onPageLoad(NormalMode)
      .url

  private lazy val contractorUtrSubmitRoute =
    controllers.contractordetails.routes.ContractorUtrController
      .onSubmit(NormalMode)
      .url

  private lazy val contractorUtrWithIdRoute =
    controllers.contractordetails.routes.ContractorUtrController
      .onPageLoadWithId(flowId, NormalMode)
      .url

  private lazy val contractorUtrWithIdSubmitRoute =
    controllers.contractordetails.routes.ContractorUtrController
      .onSubmitWithId(flowId, NormalMode)
      .url

  // The CIS ID must be stored against the same flow ID used in the route.
  private def answersWithCisIdForFlow =
    emptyUserAnswers
      .set(queries.CisIdQuery(flowId.toString), "AB12")
      .success
      .value

  "ContractorUtrController" - {

    "onPageLoad" - {

      "must return OK and the correct view for a GET with no existing answer" in {

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, contractorUtrRoute)
          val result  = route(application, request).value
          val view    = application.injector.instanceOf[ContractorUtrView]

          status(result) mustEqual OK

          contentAsString(result) mustEqual view(
            form,
            NormalMode,
            controllers.contractordetails.routes.ContractorUtrController
              .onSubmit(NormalMode)
          )(
            request,
            applicationConfig,
            messages(application)
          ).toString
        }
      }

      "must populate the view correctly on a GET when the question has previously been answered" in {

        val userAnswers =
          emptyUserAnswers
            .set(ContractorUtrPage, validUtr)
            .success
            .value

        val application =
          applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, contractorUtrRoute)
          val result  = route(application, request).value
          val view    = application.injector.instanceOf[ContractorUtrView]

          status(result) mustEqual OK

          contentAsString(result) mustEqual view(
            form.fill(validUtr),
            NormalMode,
            controllers.contractordetails.routes.ContractorUtrController
              .onSubmit(NormalMode)
          )(
            request,
            applicationConfig,
            messages(application)
          ).toString
        }
      }

      "must redirect to Journey Recovery for a GET if no existing data is found" in {

        val application =
          applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, contractorUtrRoute)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual
            routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }

    "onSubmit" - {

      "must save the answer and redirect on a valid POST" in {

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          val request =
            FakeRequest(POST, contractorUtrSubmitRoute)
              .withFormUrlEncodedBody("value" -> validUtr)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
        }
      }

      "must return Bad Request and errors for an empty submission" in {

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          val request =
            FakeRequest(POST, contractorUtrSubmitRoute)
              .withFormUrlEncodedBody("value" -> "")

          val boundForm = form.bind(Map("value" -> ""))
          val result    = route(application, request).value
          val view      = application.injector.instanceOf[ContractorUtrView]

          status(result) mustEqual BAD_REQUEST

          contentAsString(result) mustEqual view(
            boundForm,
            NormalMode,
            controllers.contractordetails.routes.ContractorUtrController
              .onSubmit(NormalMode)
          )(
            request,
            applicationConfig,
            messages(application)
          ).toString
        }
      }

      "must return Bad Request and errors when UTR fails the check digit algorithm" in {

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          val request =
            FakeRequest(POST, contractorUtrSubmitRoute)
              .withFormUrlEncodedBody("value" -> invalidUtr)

          val boundForm = form.bind(Map("value" -> invalidUtr))
          val result    = route(application, request).value
          val view      = application.injector.instanceOf[ContractorUtrView]

          status(result) mustEqual BAD_REQUEST

          contentAsString(result) mustEqual view(
            boundForm,
            NormalMode,
            controllers.contractordetails.routes.ContractorUtrController
              .onSubmit(NormalMode)
          )(
            request,
            applicationConfig,
            messages(application)
          ).toString
        }
      }

      "must return Bad Request and errors when UTR is too short" in {

        val shortUtr = "12345"

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          val request =
            FakeRequest(POST, contractorUtrSubmitRoute)
              .withFormUrlEncodedBody("value" -> shortUtr)

          val boundForm = form.bind(Map("value" -> shortUtr))
          val result    = route(application, request).value
          val view      = application.injector.instanceOf[ContractorUtrView]

          status(result) mustEqual BAD_REQUEST

          contentAsString(result) mustEqual view(
            boundForm,
            NormalMode,
            controllers.contractordetails.routes.ContractorUtrController
              .onSubmit(NormalMode)
          )(
            request,
            applicationConfig,
            messages(application)
          ).toString
        }
      }

      "must return Bad Request and errors when UTR contains non-numeric characters" in {

        val nonNumericUtr = "12345abcde"

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          val request =
            FakeRequest(POST, contractorUtrSubmitRoute)
              .withFormUrlEncodedBody("value" -> nonNumericUtr)

          val boundForm = form.bind(Map("value" -> nonNumericUtr))
          val result    = route(application, request).value
          val view      = application.injector.instanceOf[ContractorUtrView]

          status(result) mustEqual BAD_REQUEST

          contentAsString(result) mustEqual view(
            boundForm,
            NormalMode,
            controllers.contractordetails.routes.ContractorUtrController
              .onSubmit(NormalMode)
          )(
            request,
            applicationConfig,
            messages(application)
          ).toString
        }
      }

      "must redirect to Journey Recovery for a POST if no existing data is found" in {

        val application =
          applicationBuilder(userAnswers = None).build()

        running(application) {
          val request =
            FakeRequest(POST, contractorUtrSubmitRoute)
              .withFormUrlEncodedBody("value" -> validUtr)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual
            routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to Journey Recovery for a POST in CheckMode if no existing data is found" in {

        val checkSubmitRoute =
          controllers.contractordetails.routes.ContractorUtrController
            .onSubmit(CheckMode)
            .url

        val application =
          applicationBuilder(userAnswers = None).build()

        running(application) {
          val request =
            FakeRequest(POST, checkSubmitRoute)
              .withFormUrlEncodedBody("value" -> validUtr)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual
            routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }

    "multitab journey" - {

      "onPageLoadWithId" - {

        "must return OK and the correct view for a GET with no existing answer" in {

          val application =
            applicationBuilder(userAnswers = Some(answersWithCisIdForFlow)).build()

          running(application) {
            val request = FakeRequest(GET, contractorUtrWithIdRoute)
            val result  = route(application, request).value
            val view    = application.injector.instanceOf[ContractorUtrView]

            status(result) mustEqual OK

            contentAsString(result) mustEqual view(
              form,
              NormalMode,
              controllers.contractordetails.routes.ContractorUtrController
                .onSubmitWithId(flowId, NormalMode)
            )(
              request,
              applicationConfig,
              messages(application)
            ).toString
          }
        }

        "must populate the view correctly when the question has previously been answered for the flow id" in {

          val userAnswers =
            answersWithCisIdForFlow
              .set(ContractorUtrPage(flowId.toString), validUtr)
              .success
              .value

          val application =
            applicationBuilder(userAnswers = Some(userAnswers)).build()

          running(application) {
            val request = FakeRequest(GET, contractorUtrWithIdRoute)
            val result  = route(application, request).value
            val view    = application.injector.instanceOf[ContractorUtrView]

            status(result) mustEqual OK

            contentAsString(result) mustEqual view(
              form.fill(validUtr),
              NormalMode,
              controllers.contractordetails.routes.ContractorUtrController
                .onSubmitWithId(flowId, NormalMode)
            )(
              request,
              applicationConfig,
              messages(application)
            ).toString
          }
        }

        "must redirect to Journey Recovery for a GET if no existing data is found" in {

          val application =
            applicationBuilder(userAnswers = None).build()

          running(application) {
            val request = FakeRequest(GET, contractorUtrWithIdRoute)
            val result  = route(application, request).value

            status(result) mustEqual SEE_OTHER

            redirectLocation(result).value mustEqual
              routes.JourneyRecoveryController.onPageLoad().url
          }
        }
      }

      "onSubmitWithId" - {

        "must save the answer and redirect on a valid POST" in {

          val application =
            applicationBuilder(userAnswers = Some(answersWithCisIdForFlow)).build()

          running(application) {
            val request =
              FakeRequest(POST, contractorUtrWithIdSubmitRoute)
                .withFormUrlEncodedBody("value" -> validUtr)

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
          }
        }

        "must return Bad Request and errors for an empty submission" in {

          val application =
            applicationBuilder(userAnswers = Some(answersWithCisIdForFlow)).build()

          running(application) {
            val request =
              FakeRequest(POST, contractorUtrWithIdSubmitRoute)
                .withFormUrlEncodedBody("value" -> "")

            val boundForm = form.bind(Map("value" -> ""))
            val result    = route(application, request).value
            val view      = application.injector.instanceOf[ContractorUtrView]

            status(result) mustEqual BAD_REQUEST

            contentAsString(result) mustEqual view(
              boundForm,
              NormalMode,
              controllers.contractordetails.routes.ContractorUtrController
                .onSubmitWithId(flowId, NormalMode)
            )(
              request,
              applicationConfig,
              messages(application)
            ).toString
          }
        }

        "must return Bad Request and errors when UTR fails the check digit algorithm" in {

          val application =
            applicationBuilder(userAnswers = Some(answersWithCisIdForFlow)).build()

          running(application) {
            val request =
              FakeRequest(POST, contractorUtrWithIdSubmitRoute)
                .withFormUrlEncodedBody("value" -> invalidUtr)

            val boundForm = form.bind(Map("value" -> invalidUtr))
            val result    = route(application, request).value
            val view      = application.injector.instanceOf[ContractorUtrView]

            status(result) mustEqual BAD_REQUEST

            contentAsString(result) mustEqual view(
              boundForm,
              NormalMode,
              controllers.contractordetails.routes.ContractorUtrController
                .onSubmitWithId(flowId, NormalMode)
            )(
              request,
              applicationConfig,
              messages(application)
            ).toString
          }
        }

        "must return Bad Request and errors when UTR is too short" in {

          val shortUtr = "12345"

          val application =
            applicationBuilder(userAnswers = Some(answersWithCisIdForFlow)).build()

          running(application) {
            val request =
              FakeRequest(POST, contractorUtrWithIdSubmitRoute)
                .withFormUrlEncodedBody("value" -> shortUtr)

            val boundForm = form.bind(Map("value" -> shortUtr))
            val result    = route(application, request).value
            val view      = application.injector.instanceOf[ContractorUtrView]

            status(result) mustEqual BAD_REQUEST

            contentAsString(result) mustEqual view(
              boundForm,
              NormalMode,
              controllers.contractordetails.routes.ContractorUtrController
                .onSubmitWithId(flowId, NormalMode)
            )(
              request,
              applicationConfig,
              messages(application)
            ).toString
          }
        }

        "must return Bad Request and errors when UTR contains non-numeric characters" in {

          val nonNumericUtr = "12345abcde"

          val application =
            applicationBuilder(userAnswers = Some(answersWithCisIdForFlow)).build()

          running(application) {
            val request =
              FakeRequest(POST, contractorUtrWithIdSubmitRoute)
                .withFormUrlEncodedBody("value" -> nonNumericUtr)

            val boundForm = form.bind(Map("value" -> nonNumericUtr))
            val result    = route(application, request).value
            val view      = application.injector.instanceOf[ContractorUtrView]

            status(result) mustEqual BAD_REQUEST

            contentAsString(result) mustEqual view(
              boundForm,
              NormalMode,
              controllers.contractordetails.routes.ContractorUtrController
                .onSubmitWithId(flowId, NormalMode)
            )(
              request,
              applicationConfig,
              messages(application)
            ).toString
          }
        }

        "must redirect to Journey Recovery for a POST if no existing data is found" in {

          val application =
            applicationBuilder(userAnswers = None).build()

          running(application) {
            val request =
              FakeRequest(POST, contractorUtrWithIdSubmitRoute)
                .withFormUrlEncodedBody("value" -> validUtr)

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER

            redirectLocation(result).value mustEqual
              routes.JourneyRecoveryController.onPageLoad().url
          }
        }

        "must redirect to Journey Recovery for a POST in CheckMode if no existing data is found" in {

          val checkModeSubmitRoute =
            controllers.contractordetails.routes.ContractorUtrController
              .onSubmitWithId(flowId, CheckMode)
              .url

          val application =
            applicationBuilder(userAnswers = None).build()

          running(application) {
            val request =
              FakeRequest(POST, checkModeSubmitRoute)
                .withFormUrlEncodedBody("value" -> validUtr)

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER

            redirectLocation(result).value mustEqual
              routes.JourneyRecoveryController.onPageLoad().url
          }
        }
      }
    }
  }
}
