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

import base.{SpecBase, WithFakeCisTaxpayer}
import controllers.routes
import forms.add.company.CompanyAddressYesNoFormProvider
import models.{NormalMode, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.add.company.{CompanyAddressYesNoPage, CompanyNamePage}
import play.api.data.Form
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.add.company.CompanyAddressYesNoView

import scala.concurrent.Future

class CompanyAddressYesNoControllerSpec extends SpecBase with WithFakeCisTaxpayer with MockitoSugar {

  val formProvider        = new CompanyAddressYesNoFormProvider()
  val form: Form[Boolean] = formProvider()

  private val companyName = "Test Company"

  lazy val companyAddressYesNoRoute: String =
    controllers.add.company.routes.CompanyAddressYesNoController.onPageLoad(NormalMode).url

  private def uaWithName: UserAnswers =
    emptyUserAnswers.set(CompanyNamePage, companyName).success.value

  "CompanyAddressYesNo Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(uaWithName)).build()

      running(application) {
        val request = FakeRequest(GET, companyAddressYesNoRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CompanyAddressYesNoView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form,
          NormalMode,
          companyName,
          controllers.add.company.routes.CompanyAddressYesNoController.onSubmit(NormalMode)
        )(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = uaWithName.set(CompanyAddressYesNoPage, true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, companyAddressYesNoRoute)

        val view = application.injector.instanceOf[CompanyAddressYesNoView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill(true),
          NormalMode,
          companyName,
          controllers.add.company.routes.CompanyAddressYesNoController.onSubmit(NormalMode)
        )(request, messages(application)).toString
      }
    }

    "must redirect to the CompanyAddress page when valid data with value Yes is submitted" in {

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
          FakeRequest(POST, companyAddressYesNoRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.add.company.routes.CompanyAddressController
          .redirectToAddressLookup(NormalMode, None)
          .url
      }
    }

    "must redirect to the AddCompanyContactMethodsYesNo page when valid data with value No is submitted" in {

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
          FakeRequest(POST, companyAddressYesNoRoute)
            .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.add.company.routes.AddCompanyContactMethodsYesNoController
          .onPageLoad(NormalMode)
          .url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(uaWithName)).build()

      running(application) {
        val request =
          FakeRequest(POST, companyAddressYesNoRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[CompanyAddressYesNoView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        view(
          boundForm,
          NormalMode,
          companyName,
          controllers.add.company.routes.CompanyAddressYesNoController.onSubmit(NormalMode)
        )(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, companyAddressYesNoRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, companyAddressYesNoRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to JourneyRecovery if CompanyName is missing for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, companyAddressYesNoRoute)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to JourneyRecovery if CompanyName is missing for a POST" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, companyAddressYesNoRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must return BadRequest when invalid data is submitted and name is present" in {

      val application = applicationBuilder(userAnswers = Some(uaWithName)).build()

      running(application) {
        val request =
          FakeRequest(POST, companyAddressYesNoRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))
        val view      = application.injector.instanceOf[CompanyAddressYesNoView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        view(
          boundForm,
          NormalMode,
          companyName,
          controllers.add.company.routes.CompanyAddressYesNoController.onSubmit(NormalMode)
        )(request, messages(application)).toString
      }
    }

    "must return a Bad Request when no value is submitted" in {

      val application = applicationBuilder(userAnswers = Some(uaWithName)).build()

      running(application) {
        val request =
          FakeRequest(POST, companyAddressYesNoRoute)
            .withFormUrlEncodedBody()

        val boundForm = form.bind(Map.empty)
        val view      = application.injector.instanceOf[CompanyAddressYesNoView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual
          view(
            boundForm,
            NormalMode,
            companyName,
            controllers.add.company.routes.CompanyAddressYesNoController.onSubmit(NormalMode)
          )(request, messages(application)).toString
      }
    }

    "with id" - {

      val flowId = java.util.UUID.fromString("11111111-1111-1111-1111-111111111111")
      val id     = flowId.toString

      val answers = emptyUserAnswers
        .set(queries.CisIdQuery(id), "AB12")
        .success
        .value
        .set(CompanyNamePage(id), companyName)
        .success
        .value

      lazy val withIdRoute =
        controllers.add.company.routes.CompanyAddressYesNoController
          .onPageLoadWithId(flowId, NormalMode)
          .url

      "must return OK and the correct view for a GET" in {

        val application = applicationBuilder(userAnswers = Some(answers)).build()

        running(application) {
          val request = FakeRequest(GET, withIdRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[CompanyAddressYesNoView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(
            form,
            NormalMode,
            companyName,
            controllers.add.company.routes.CompanyAddressYesNoController
              .onSubmitWithId(flowId, NormalMode)
          )(request, messages(application)).toString
        }
      }

      "must populate the view correctly on a GET when the question has previously been answered" in {

        val userAnswers = answers.set(pages.add.company.CompanyAddressYesNoPage(id), true).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, withIdRoute)

          val view = application.injector.instanceOf[CompanyAddressYesNoView]

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(
            form.fill(true),
            NormalMode,
            companyName,
            controllers.add.company.routes.CompanyAddressYesNoController
              .onSubmitWithId(flowId, NormalMode)
          )(request, messages(application)).toString
        }
      }

      "must redirect to the CompanyAddress page when valid data with value Yes is submitted" in {

        val mockSessionRepository = mock[SessionRepository]

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application =
          applicationBuilder(userAnswers = Some(answers))
            .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
            .build()

        running(application) {
          val request =
            FakeRequest(
              POST,
              controllers.add.company.routes.CompanyAddressYesNoController
                .onSubmitWithId(flowId, NormalMode)
                .url
            ).withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            controllers.add.company.routes.CompanyAddressController
              .redirectToAddressLookupWithId(flowId, None)
              .url
        }
      }

      "must redirect to the AddCompanyContactMethodsYesNo page when valid data with value No is submitted" in {

        val mockSessionRepository = mock[SessionRepository]

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application =
          applicationBuilder(userAnswers = Some(answers))
            .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
            .build()

        running(application) {
          val request =
            FakeRequest(
              POST,
              controllers.add.company.routes.CompanyAddressYesNoController
                .onSubmitWithId(flowId, NormalMode)
                .url
            ).withFormUrlEncodedBody(("value", "false"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            controllers.add.company.routes.AddCompanyContactMethodsYesNoController
              .onPageLoadWithId(flowId, NormalMode)
              .url
        }
      }

      "must return a Bad Request when invalid data is submitted" in {

        val application = applicationBuilder(userAnswers = Some(answers)).build()

        running(application) {
          val request =
            FakeRequest(
              POST,
              controllers.add.company.routes.CompanyAddressYesNoController
                .onSubmitWithId(flowId, NormalMode)
                .url
            ).withFormUrlEncodedBody(("value", ""))

          val boundForm = form.bind(Map("value" -> ""))
          val view      = application.injector.instanceOf[CompanyAddressYesNoView]

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(
            boundForm,
            NormalMode,
            companyName,
            controllers.add.company.routes.CompanyAddressYesNoController
              .onSubmitWithId(flowId, NormalMode)
          )(request, messages(application)).toString
        }
      }
    }
  }
}
