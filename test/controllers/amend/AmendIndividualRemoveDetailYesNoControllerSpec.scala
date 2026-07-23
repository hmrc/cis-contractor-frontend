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

package controllers.amend

import base.SpecBase
import forms.amend.AmendIndividualRemoveDetailYesNoFormProvider
import models.{AmendMode, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.add.TradingNameOfSubcontractorPage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.amend.AmendIndividualRemoveDetailYesNoView

import scala.concurrent.Future

class AmendIndividualRemoveDetailYesNoControllerSpec extends SpecBase with MockitoSugar {

  val formProvider = new AmendIndividualRemoveDetailYesNoFormProvider()
  val form         = formProvider()

  private val subcontractorName = "Test individual"

  private def uaWithName: UserAnswers =
    emptyUserAnswers.set(TradingNameOfSubcontractorPage, subcontractorName).success.value

  "AmendIndividualRemoveDetailYesNo Controller" - {
    Seq(
      ("address", "address"),
      ("contact-details", "contact-details"),
      ("unique-taxpayer-reference", "unique-taxpayer-reference"),
      ("national-insurance-number", "national-insurance-number"),
      ("works-reference-number", "works-reference-number")
    ).foreach { case (subcontractorDetail, selectedDetail) =>
      s"when subcontractorDetail is '$subcontractorDetail'" - {
        val form = formProvider()

        lazy val removeDetailYesNoRoute =
          controllers.amend.routes.AmendIndividualRemoveDetailYesNoController.onPageLoad(selectedDetail).url

        "must return OK and the correct view for a GET" in {

          val application = applicationBuilder(userAnswers = Some(uaWithName)).build()

          running(application) {
            val request = FakeRequest(GET, removeDetailYesNoRoute)

            val result = route(application, request).value

            val view = application.injector.instanceOf[AmendIndividualRemoveDetailYesNoView]

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(subcontractorName, selectedDetail, form)(
              request,
              messages(application)
            ).toString
          }
        }

        "must redirect to the next page when valid data with value Yes is submitted" in {

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
              FakeRequest(POST, removeDetailYesNoRoute)
                .withFormUrlEncodedBody(("value", "true"))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual controllers.add.routes.CheckYourAnswersController
              .onPageLoad()
              .url
          }
        }

        "must redirect to the next page when valid data with value No is submitted" in {

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
              FakeRequest(POST, removeDetailYesNoRoute)
                .withFormUrlEncodedBody(("value", "false"))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual controllers.add.routes.CheckYourAnswersController
              .onPageLoad()
              .url
          }
        }

        "must return a Bad Request and errors when invalid data is submitted" in {

          val application = applicationBuilder(userAnswers = Some(uaWithName)).build()

          running(application) {
            val request =
              FakeRequest(POST, removeDetailYesNoRoute)
                .withFormUrlEncodedBody(("value", ""))

            val boundForm = form.bind(Map("value" -> ""))

            val view = application.injector.instanceOf[AmendIndividualRemoveDetailYesNoView]

            val result = route(application, request).value

            status(result) mustEqual BAD_REQUEST
            contentAsString(result) mustEqual view(subcontractorName, selectedDetail, boundForm)(
              request,
              messages(application)
            ).toString
          }
        }

        "must redirect to Journey Recovery for a GET if no existing data is found" in {

          val application = applicationBuilder(userAnswers = None).build()

          running(application) {
            val request = FakeRequest(GET, removeDetailYesNoRoute)

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
          }
        }

        "must redirect to Journey Recovery for a POST if no existing data is found" in {

          val application = applicationBuilder(userAnswers = None).build()

          running(application) {
            val request =
              FakeRequest(POST, removeDetailYesNoRoute)
                .withFormUrlEncodedBody(("value", "true"))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
          }
        }

        "must redirect to JourneyRecovery if CompanyName is missing for a GET" in {

          val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

          running(application) {
            val request = FakeRequest(GET, removeDetailYesNoRoute)

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
          }
        }

        "must redirect to JourneyRecovery if CompanyName is missing for a POST" in {

          val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

          running(application) {
            val request =
              FakeRequest(POST, removeDetailYesNoRoute)
                .withFormUrlEncodedBody(("value", "true"))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
          }
        }

      }
    }

    "when subcontractorDetail is subcontractor-name " - {
      val form = formProvider()

      val selectedDetail = "subcontractor-name"

      lazy val removeDetailYesNoRoute =
        controllers.amend.routes.AmendIndividualRemoveDetailYesNoController.onPageLoad(selectedDetail).url

      "must return OK and the correct view for a GET" in {

        val application = applicationBuilder(userAnswers = Some(uaWithName)).build()

        running(application) {
          val request = FakeRequest(GET, removeDetailYesNoRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[AmendIndividualRemoveDetailYesNoView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(subcontractorName, selectedDetail, form)(
            request,
            messages(application)
          ).toString
        }
      }

      "must redirect to the next page when valid data with value Yes is submitted" in {

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
            FakeRequest(POST, removeDetailYesNoRoute)
              .withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.add.routes.TradingNameOfSubcontractorController
            .onPageLoad(AmendMode)
            .url
        }
      }

      "must redirect to the next page when valid data with value No is submitted" in {

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
            FakeRequest(POST, removeDetailYesNoRoute)
              .withFormUrlEncodedBody(("value", "false"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.add.routes.CheckYourAnswersController
            .onPageLoad()
            .url
        }
      }

      "must return a Bad Request and errors when invalid data is submitted" in {

        val application = applicationBuilder(userAnswers = Some(uaWithName)).build()

        running(application) {
          val request =
            FakeRequest(POST, removeDetailYesNoRoute)
              .withFormUrlEncodedBody(("value", ""))

          val boundForm = form.bind(Map("value" -> ""))

          val view = application.injector.instanceOf[AmendIndividualRemoveDetailYesNoView]

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(subcontractorName, selectedDetail, boundForm)(
            request,
            messages(application)
          ).toString
        }
      }

      "must redirect to Journey Recovery for a GET if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, removeDetailYesNoRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to Journey Recovery for a POST if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request =
            FakeRequest(POST, removeDetailYesNoRoute)
              .withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to JourneyRecovery if CompanyName is missing for a GET" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, removeDetailYesNoRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to JourneyRecovery if CompanyName is missing for a POST" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          val request =
            FakeRequest(POST, removeDetailYesNoRoute)
              .withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }

    s"when subcontractorDetail is trading-name" - {
      val form = formProvider()

      val selectedDetail = "trading-name"

      lazy val removeDetailYesNoRoute =
        controllers.amend.routes.AmendIndividualRemoveDetailYesNoController.onPageLoad(selectedDetail).url

      "must return OK and the correct view for a GET" in {

        val application = applicationBuilder(userAnswers = Some(uaWithName)).build()

        running(application) {
          val request = FakeRequest(GET, removeDetailYesNoRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[AmendIndividualRemoveDetailYesNoView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(subcontractorName, selectedDetail, form)(
            request,
            messages(application)
          ).toString
        }
      }

      "must redirect to the next page when valid data with value Yes is submitted" in {

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
            FakeRequest(POST, removeDetailYesNoRoute)
              .withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.add.routes.SubcontractorNameController
            .onPageLoad(AmendMode)
            .url
        }
      }

      "must redirect to the next page when valid data with value No is submitted" in {

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
            FakeRequest(POST, removeDetailYesNoRoute)
              .withFormUrlEncodedBody(("value", "false"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.add.routes.CheckYourAnswersController
            .onPageLoad()
            .url
        }
      }

      "must return a Bad Request and errors when invalid data is submitted" in {

        val application = applicationBuilder(userAnswers = Some(uaWithName)).build()

        running(application) {
          val request =
            FakeRequest(POST, removeDetailYesNoRoute)
              .withFormUrlEncodedBody(("value", ""))

          val boundForm = form.bind(Map("value" -> ""))

          val view = application.injector.instanceOf[AmendIndividualRemoveDetailYesNoView]

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(subcontractorName, selectedDetail, boundForm)(
            request,
            messages(application)
          ).toString
        }
      }

      "must redirect to Journey Recovery for a GET if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, removeDetailYesNoRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to Journey Recovery for a POST if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request =
            FakeRequest(POST, removeDetailYesNoRoute)
              .withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to JourneyRecovery if CompanyName is missing for a GET" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, removeDetailYesNoRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to JourneyRecovery if CompanyName is missing for a POST" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          val request =
            FakeRequest(POST, removeDetailYesNoRoute)
              .withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }

    "when subcontractorDetail is neither 'subcontractor-name', 'trading-name', 'address', 'contact-details', 'unique-taxpayer-reference' or 'works-reference-number'" - {

      "must redirect to Journey Recovery on GET" in {

        val application = applicationBuilder(userAnswers = Some(uaWithName)).build()

        running(application) {

          val request =
            FakeRequest(
              GET,
              controllers.amend.routes.AmendIndividualRemoveDetailYesNoController.onPageLoad("invalid").url
            )

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to Journey Recovery on POST" in {

        val application =
          applicationBuilder(userAnswers = Some(uaWithName)).build()

        running(application) {

          val request =
            FakeRequest(
              POST,
              controllers.amend.routes.AmendIndividualRemoveDetailYesNoController.onSubmit("invalid").url
            )
              .withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }
  }
}
