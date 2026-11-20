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
import forms.add.company.AddCompanyContactMethodsYesNoFormProvider
import models.{NormalMode, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.add.company.{AddCompanyContactMethodsYesNoPage, CompanyNamePage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.add.company.AddCompanyContactMethodsYesNoView

import java.util.UUID
import scala.concurrent.Future

class AddCompanyContactMethodsYesNoControllerSpec extends SpecBase with WithFakeCisTaxpayer with MockitoSugar {

  private val formProvider = new AddCompanyContactMethodsYesNoFormProvider()
  private val form         = formProvider()

  private lazy val addCompanyContactMethodsYesNoRoute =
    controllers.add.company.routes.AddCompanyContactMethodsYesNoController.onPageLoad(NormalMode).url

  private val companyName = "Test Company"

  private def uaWithName: UserAnswers =
    emptyUserAnswers.set(CompanyNamePage, companyName).success.value

  private val flowId = UUID.randomUUID()
  private val id     = flowId.toString

  private lazy val addCompanyContactMethodsYesNoWithIdRoute =
    controllers.add.company.routes.AddCompanyContactMethodsYesNoController
      .onPageLoadWithId(flowId, NormalMode)
      .url

  private def uaWithNameAndId(id: String): UserAnswers =
    emptyUserAnswers
      .set(queries.CisIdQuery(id), "AB12")
      .success
      .value
      .set(CompanyNamePage(id), companyName)
      .success
      .value

  "AddCompanyContactMethodsYesNo Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(uaWithName)).build()

      running(application) {
        val request = FakeRequest(GET, addCompanyContactMethodsYesNoRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AddCompanyContactMethodsYesNoView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form,
          NormalMode,
          companyName,
          controllers.add.company.routes.AddCompanyContactMethodsYesNoController.onSubmit(NormalMode)
        )(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = uaWithName.set(AddCompanyContactMethodsYesNoPage, true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, addCompanyContactMethodsYesNoRoute)

        val view = application.injector.instanceOf[AddCompanyContactMethodsYesNoView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill(true),
          NormalMode,
          companyName,
          controllers.add.company.routes.AddCompanyContactMethodsYesNoController.onSubmit(NormalMode)
        )(request, messages(application)).toString
      }
    }

    "must redirect to the CompanyContactMethodOptions page when valid data with value Yes is submitted" in {

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
          FakeRequest(POST, addCompanyContactMethodsYesNoRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.add.company.routes.CompanyContactMethodOptionsController
          .onPageLoad(NormalMode)
          .url
      }
    }

    "must redirect to the CompanyUtrYesNo page when valid data with value No is submitted" in {

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
          FakeRequest(POST, addCompanyContactMethodsYesNoRoute)
            .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.add.company.routes.CompanyUtrYesNoController
          .onPageLoad(NormalMode)
          .url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted (name present)" in {

      val application = applicationBuilder(userAnswers = Some(uaWithName)).build()

      running(application) {
        val request =
          FakeRequest(POST, addCompanyContactMethodsYesNoRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[AddCompanyContactMethodsYesNoView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(
          boundForm,
          NormalMode,
          companyName,
          controllers.add.company.routes.AddCompanyContactMethodsYesNoController.onSubmit(NormalMode)
        )(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, addCompanyContactMethodsYesNoRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, addCompanyContactMethodsYesNoRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must return a Bad Request and errors when no value is submitted" in {

      val application = applicationBuilder(userAnswers = Some(uaWithName)).build()

      running(application) {
        val request =
          FakeRequest(POST, addCompanyContactMethodsYesNoRoute)
            .withFormUrlEncodedBody()

        val form      = new AddCompanyContactMethodsYesNoFormProvider()()
        val boundForm = form.bind(Map.empty)

        val view = application.injector.instanceOf[AddCompanyContactMethodsYesNoView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(
          boundForm,
          NormalMode,
          companyName,
          controllers.add.company.routes.AddCompanyContactMethodsYesNoController.onSubmit(NormalMode)
        )(request, messages(application)).toString

        contentAsString(result) must include(messages(application)("addCompanyContactMethodsYesNo.error.required"))
      }
    }

    "must redirect to Journey Recovery for a GET when subcontractor name is missing (userAnswers present)" in {

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, addCompanyContactMethodsYesNoRoute)

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
          FakeRequest(POST, addCompanyContactMethodsYesNoRoute)
            .withFormUrlEncodedBody("value" -> "true")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "when using multi-tab journey" - {

      "must return OK and the correct view for a GET" in {

        val application = applicationBuilder(userAnswers = Some(uaWithNameAndId(id))).build()

        running(application) {
          val request = FakeRequest(GET, addCompanyContactMethodsYesNoWithIdRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[AddCompanyContactMethodsYesNoView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(
            form,
            NormalMode,
            companyName,
            controllers.add.company.routes.AddCompanyContactMethodsYesNoController
              .onSubmitWithId(flowId, NormalMode)
          )(request, messages(application)).toString
        }
      }

      "must populate the view correctly on a GET when the question has previously been answered" in {

        val userAnswers =
          uaWithNameAndId(id).set(AddCompanyContactMethodsYesNoPage(id), true).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, addCompanyContactMethodsYesNoWithIdRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[AddCompanyContactMethodsYesNoView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(
            form.fill(true),
            NormalMode,
            companyName,
            controllers.add.company.routes.AddCompanyContactMethodsYesNoController
              .onSubmitWithId(flowId, NormalMode)
          )(request, messages(application)).toString
        }
      }

      "must redirect to the next page with the same flowId when Yes is submitted" in {

        val mockSessionRepository = mock[SessionRepository]
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application =
          applicationBuilder(userAnswers = Some(uaWithNameAndId(id)))
            .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
            .build()

        running(application) {
          val request =
            FakeRequest(
              POST,
              controllers.add.company.routes.AddCompanyContactMethodsYesNoController
                .onSubmitWithId(flowId, NormalMode)
                .url
            ).withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            controllers.add.company.routes.CompanyContactMethodOptionsController
              .onPageLoadWithId(flowId, NormalMode)
              .url
        }
      }

      "must redirect to the next page with the same flowId when No is submitted" in {

        val mockSessionRepository = mock[SessionRepository]
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application =
          applicationBuilder(userAnswers = Some(uaWithNameAndId(id)))
            .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
            .build()

        running(application) {
          val request =
            FakeRequest(
              POST,
              controllers.add.company.routes.AddCompanyContactMethodsYesNoController
                .onSubmitWithId(flowId, NormalMode)
                .url
            ).withFormUrlEncodedBody(("value", "false"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            controllers.add.company.routes.CompanyUtrYesNoController
              .onPageLoadWithId(flowId, NormalMode)
              .url
        }
      }

      "must redirect to Journey Recovery when the company name is missing for the tab" in {

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, addCompanyContactMethodsYesNoWithIdRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must return a Bad Request when invalid data is submitted" in {

        val application = applicationBuilder(userAnswers = Some(uaWithNameAndId(id))).build()

        running(application) {
          val request =
            FakeRequest(
              POST,
              controllers.add.company.routes.AddCompanyContactMethodsYesNoController
                .onSubmitWithId(flowId, NormalMode)
                .url
            ).withFormUrlEncodedBody(("value", ""))

          val result = route(application, request).value

          val view = application.injector.instanceOf[AddCompanyContactMethodsYesNoView]

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(
            form.bind(Map("value" -> "")),
            NormalMode,
            companyName,
            controllers.add.company.routes.AddCompanyContactMethodsYesNoController
              .onSubmitWithId(flowId, NormalMode)
          )(request, messages(application)).toString
        }
      }
    }
  }
}
