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
import forms.contractordetails.AddEmailAddressYesNoFormProvider
import models.{NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.contractordetails.AddEmailAddressYesNoPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import queries.CisIdQuery
import repositories.SessionRepository
import views.html.contractordetails.AddEmailAddressYesNoView

import java.util.UUID
import scala.concurrent.Future

class AddEmailAddressYesNoControllerSpec extends WithFakeCisTaxpayer with MockitoSugar {

  private def onwardRoute: Call = Call("GET", "/foo")

  private val formProvider = new AddEmailAddressYesNoFormProvider()
  private val form         = formProvider()

  private val uuid = UUID.fromString("11111111-1111-1111-1111-111111111111")
  private val id   = uuid.toString

  private lazy val addEmailAddressYesNoRoute =
    controllers.contractordetails.routes.AddEmailAddressYesNoController
      .onPageLoad(NormalMode)
      .url

  private def answersWithCisIdForFlow: UserAnswers =
    emptyUserAnswers
      .set(CisIdQuery(id), "AB12")
      .success
      .value

  "AddEmailAddressYesNo Controller" - {

    "must return OK and the correct view for a GET" in {

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, addEmailAddressYesNoRoute)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[AddEmailAddressYesNoView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual view(
          form,
          NormalMode,
          controllers.contractordetails.routes.AddEmailAddressYesNoController
            .onSubmit(NormalMode)
        )(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers =
        emptyUserAnswers
          .set(AddEmailAddressYesNoPage, true)
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, addEmailAddressYesNoRoute)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[AddEmailAddressYesNoView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual view(
          form.fill(true),
          NormalMode,
          controllers.contractordetails.routes.AddEmailAddressYesNoController
            .onSubmit(NormalMode)
        )(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, addEmailAddressYesNoRoute)
            .withFormUrlEncodedBody("value" -> "true")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, addEmailAddressYesNoRoute)
            .withFormUrlEncodedBody("value" -> "")

        val boundForm = form.bind(Map("value" -> ""))
        val result    = route(application, request).value
        val view      = application.injector.instanceOf[AddEmailAddressYesNoView]

        status(result) mustEqual BAD_REQUEST

        contentAsString(result) mustEqual view(
          boundForm,
          NormalMode,
          controllers.contractordetails.routes.AddEmailAddressYesNoController
            .onSubmit(NormalMode)
        )(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application =
        applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, addEmailAddressYesNoRoute)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application =
        applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, addEmailAddressYesNoRoute)
            .withFormUrlEncodedBody("value" -> "true")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    // Multi-tab tests

    "must return OK for a GET with id when the subjourney has existing data" in {

      val answers =
        answersWithCisIdForFlow
          .set(AddEmailAddressYesNoPage(id), true)
          .success
          .value

      val repo = mockSessionRepository(Some(answers))

      val application =
        applicationBuilder(userAnswers = Some(answers))
          .overrides(bind[SessionRepository].toInstance(repo))
          .build()

      running(application) {
        val request = FakeRequest(
          GET,
          controllers.contractordetails.routes.AddEmailAddressYesNoController
            .onPageLoadWithId(uuid, NormalMode)
            .url
        )

        val result = route(application, request).value
        val view   = application.injector.instanceOf[AddEmailAddressYesNoView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual view(
          form.fill(true),
          NormalMode,
          controllers.contractordetails.routes.AddEmailAddressYesNoController
            .onSubmitWithId(uuid, NormalMode)
        )(request, messages(application)).toString
      }
    }

    "must return OK for a GET with id when the question has not been answered" in {

      val answers = answersWithCisIdForFlow
      val repo    = mockSessionRepository(Some(answers))

      val application =
        applicationBuilder(userAnswers = Some(answers))
          .overrides(bind[SessionRepository].toInstance(repo))
          .build()

      running(application) {
        val request = FakeRequest(
          GET,
          controllers.contractordetails.routes.AddEmailAddressYesNoController
            .onPageLoadWithId(uuid, NormalMode)
            .url
        )

        val result = route(application, request).value

        status(result) mustEqual OK
      }
    }

    "must redirect to contractor email address with id when yes is submitted" in {

      val answers = answersWithCisIdForFlow
      val repo    = mockSessionRepository(Some(answers))

      when(repo.set(any())) thenReturn Future.successful(true)

      val expectedRoute =
        controllers.contractordetails.routes.EnterContractorEmailAddressController
          .onPageLoadWithId(uuid, NormalMode)

      val application =
        applicationBuilder(userAnswers = Some(answers))
          .overrides(
            bind[SessionRepository].toInstance(repo),
            bind[Navigator].toInstance(new FakeNavigator(expectedRoute))
          )
          .build()

      running(application) {
        val request =
          FakeRequest(
            POST,
            controllers.contractordetails.routes.AddEmailAddressYesNoController
              .onSubmitWithId(uuid, NormalMode)
              .url
          ).withFormUrlEncodedBody("value" -> "true")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual expectedRoute.url
      }
    }

    "must redirect to check answers with id when no is submitted" in {

      val answers = answersWithCisIdForFlow
      val repo    = mockSessionRepository(Some(answers))

      when(repo.set(any())) thenReturn Future.successful(true)

      val expectedRoute =
        controllers.contractordetails.routes.ContractorDetailsCheckAnswersController
          .onPageLoadWithId(uuid)

      val application =
        applicationBuilder(userAnswers = Some(answers))
          .overrides(
            bind[SessionRepository].toInstance(repo),
            bind[Navigator].toInstance(new FakeNavigator(expectedRoute))
          )
          .build()

      running(application) {
        val request =
          FakeRequest(
            POST,
            controllers.contractordetails.routes.AddEmailAddressYesNoController
              .onSubmitWithId(uuid, NormalMode)
              .url
          ).withFormUrlEncodedBody("value" -> "false")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual expectedRoute.url
      }
    }
  }
}
