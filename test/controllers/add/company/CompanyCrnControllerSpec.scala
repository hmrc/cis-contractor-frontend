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
import forms.add.company.CompanyCrnFormProvider
import models.{NormalMode, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.add.company.{CompanyCrnPage, CompanyCrnYesNoPage, CompanyNamePage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.add.company.CompanyCrnView

import scala.concurrent.Future

class CompanyCrnControllerSpec extends SpecBase with WithFakeCisTaxpayer with MockitoSugar {

  private val formProvider = new CompanyCrnFormProvider()
  private val form         = formProvider()

  private val companyName = "Test Company"

  private lazy val companyCrnRoute = controllers.add.company.routes.CompanyCrnController.onPageLoad(NormalMode).url

  private def usWithName: UserAnswers =
    emptyUserAnswers
      .set(CompanyNamePage, companyName)
      .success
      .value
      .set(CompanyCrnYesNoPage, true)
      .success
      .value

  "CompanyCrn Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(usWithName)).build()

      running(application) {
        val request = FakeRequest(GET, companyCrnRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CompanyCrnView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form,
          NormalMode,
          companyName,
          controllers.add.company.routes.CompanyCrnController.onSubmit(NormalMode)
        )(request, messages(application)).toString
      }
    }

    "must redirect to yesOrNo page when yesorno page has No for a GET" in {

      val application =
        applicationBuilder(userAnswers = Some(usWithName.set(CompanyCrnYesNoPage, false).success.value)).build()

      running(application) {
        val request = FakeRequest(GET, companyCrnRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.add.company.routes.CompanyCrnYesNoController
          .onPageLoad(NormalMode)
          .url

      }
    }

    "must redirect to journey recovery page when none for yesorno page for a GET" in {

      val companyName = "Accenture"
      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers.set(CompanyNamePage, companyName).success.value)).build()

      running(application) {
        val request = FakeRequest(GET, companyCrnRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url

      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = usWithName.set(CompanyCrnPage, "AC012345").success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, companyCrnRoute)

        val view = application.injector.instanceOf[CompanyCrnView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill("AC012345"),
          NormalMode,
          companyName,
          controllers.add.company.routes.CompanyCrnController.onSubmit(NormalMode)
        )(request, messages(application)).toString
      }
    }

    "must redirect to the CompanyWorksReferenceYesNoPage page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(usWithName))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, companyCrnRoute)
            .withFormUrlEncodedBody(("value", "AC012345"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.add.company.routes.CompanyWorksReferenceYesNoController
          .onPageLoad(NormalMode)
          .url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(usWithName)).build()

      running(application) {
        val request =
          FakeRequest(POST, companyCrnRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[CompanyCrnView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(
          boundForm,
          NormalMode,
          companyName,
          controllers.add.company.routes.CompanyCrnController.onSubmit(NormalMode)
        )(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, companyCrnRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, companyCrnRoute)
            .withFormUrlEncodedBody(("value", "answer"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a GET when company name is missing (userAnswers present)" in {

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, companyCrnRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST when company name is missing (userAnswers present)" in {

      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, companyCrnRoute)
            .withFormUrlEncodedBody("value" -> "AC012345")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "CompanyCrn Controller with id" - {

      val flowId = java.util.UUID.fromString("11111111-1111-1111-1111-111111111111")
      val id     = flowId.toString

      val minUaWithId = emptyUserAnswers
        .set(queries.CisIdQuery(id), "AB12")
        .success
        .value
        .set(CompanyNamePage(id), companyName)
        .success
        .value
        .set(CompanyCrnYesNoPage(id), true)
        .success
        .value

      "must return OK and the correct view for a GET" in {

        val repo = mockSessionRepository(Some(minUaWithId))

        val application =
          applicationBuilder(userAnswers = Some(minUaWithId))
            .overrides(bind[SessionRepository].toInstance(repo))
            .build()

        running(application) {
          val request = FakeRequest(
            GET,
            controllers.add.company.routes.CompanyCrnController
              .onPageLoadWithId(flowId, NormalMode)
              .url
          )

          val result = route(application, request).value
          val view   = application.injector.instanceOf[CompanyCrnView]

          status(result) mustEqual OK

          contentAsString(result) mustEqual view(
            form,
            NormalMode,
            companyName,
            controllers.add.company.routes.CompanyCrnController.onSubmitWithId(flowId, NormalMode)
          )(request, messages(application)).toString
        }
      }

      "must redirect when valid data is submitted" in {

        val mockSessionRepository = mock[SessionRepository]

        when(mockSessionRepository.get(any[String]))
          .thenReturn(Future.successful(Some(minUaWithId)))

        when(mockSessionRepository.set(any[UserAnswers]))
          .thenReturn(Future.successful(true))

        val application =
          applicationBuilder(userAnswers = Some(minUaWithId))
            .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
            .build()

        running(application) {
          val request =
            FakeRequest(
              POST,
              controllers.add.company.routes.CompanyCrnController
                .onSubmitWithId(flowId, NormalMode)
                .url
            ).withFormUrlEncodedBody("value" -> "AC012345")

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual
            controllers.add.company.routes.CompanyWorksReferenceYesNoController
              .onPageLoadWithId(flowId, NormalMode)
              .url
        }
      }

      "must return a Bad Request and errors when invalid data is submitted" in {

        val application =
          applicationBuilder(userAnswers = Some(minUaWithId)).build()

        running(application) {
          val request =
            FakeRequest(
              POST,
              controllers.add.company.routes.CompanyCrnController
                .onSubmitWithId(flowId, NormalMode)
                .url
            ).withFormUrlEncodedBody("value" -> "")

          val boundForm = form.bind(Map("value" -> ""))
          val view      = application.injector.instanceOf[CompanyCrnView]

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST

          contentAsString(result) mustEqual view(
            boundForm,
            NormalMode,
            companyName,
            controllers.add.company.routes.CompanyCrnController.onSubmitWithId(flowId, NormalMode)
          )(request, messages(application)).toString
        }
      }

      "must redirect to Journey Recovery for a GET if no existing data is found" in {

        val repo = mockSessionRepository(None)

        val application =
          applicationBuilder(userAnswers = None)
            .overrides(bind[SessionRepository].toInstance(repo))
            .build()

        running(application) {
          val request = FakeRequest(
            GET,
            controllers.add.company.routes.CompanyCrnController
              .onPageLoadWithId(flowId, NormalMode)
              .url
          )

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }
  }
}
