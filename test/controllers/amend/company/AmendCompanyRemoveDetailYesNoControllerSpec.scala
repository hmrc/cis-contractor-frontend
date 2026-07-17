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

package controllers.amend.company

import base.SpecBase
import controllers.routes
import forms.amend.company.AmendCompanyRemoveDetailYesNoFormProvider
import models.{NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.add.company.CompanyNamePage
import pages.amend.company.AmendCompanyRemoveDetailYesNoPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.amend.company.AmendCompanyRemoveDetailYesNoView

import scala.concurrent.Future

class AmendCompanyRemoveDetailYesNoControllerSpec extends SpecBase with MockitoSugar {

  val formProvider = new AmendCompanyRemoveDetailYesNoFormProvider()

  private val companyName = "Test Company"

  private def uaWithName: UserAnswers =
    emptyUserAnswers.set(CompanyNamePage, companyName).success.value

  "AmendCompanyRemoveDetailYesNo Controller" - {

    Seq(
      ("address", "address"),
      ("contact-details", "contact-details"),
      ("unique-taxpayer-reference", "unique-taxpayer-reference"),
      ("company-registration-number", "company-registration-number"),
      ("works-reference-number", "works-reference-number")
    ).foreach { case (contractorDetail, selectedDetail) =>
      s"when contractorDetail is '$contractorDetail'" - {
        val form = formProvider()

        lazy val removeDetailYesNoRoute =
          controllers.amend.company.routes.AmendCompanyRemoveDetailYesNoController.onPageLoad(selectedDetail).url

        "must return OK and the correct view for a GET" in {

          val application = applicationBuilder(userAnswers = Some(uaWithName)).build()

          running(application) {
            val request = FakeRequest(GET, removeDetailYesNoRoute)

            val result = route(application, request).value

            val view = application.injector.instanceOf[AmendCompanyRemoveDetailYesNoView]

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(companyName, selectedDetail, form)(
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
            redirectLocation(result).value mustEqual controllers.add.company.routes.CompanyCheckYourAnswersController
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
            redirectLocation(result).value mustEqual controllers.add.company.routes.CompanyCheckYourAnswersController
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

            val view = application.injector.instanceOf[AmendCompanyRemoveDetailYesNoView]

            val result = route(application, request).value

            status(result) mustEqual BAD_REQUEST
            contentAsString(result) mustEqual view(companyName, selectedDetail, boundForm)(
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

    "when contractorDetail is neither 'address', 'contact-details', 'unique-taxpayer-reference', 'company-registration-number' or 'works-reference-number'" - {

      "must redirect to Journey Recovery on GET" in {

        val application = applicationBuilder(userAnswers = Some(uaWithName)).build()

        running(application) {

          val request =
            FakeRequest(
              GET,
              controllers.amend.company.routes.AmendCompanyRemoveDetailYesNoController.onPageLoad("invalid").url
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
              controllers.amend.company.routes.AmendCompanyRemoveDetailYesNoController.onSubmit("invalid").url
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
