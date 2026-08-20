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

package controllers.add

import base.SpecBase
import controllers.routes
import forms.add.IndividualNamesOptionsFormProvider
import models.add.IndividualNamesOptions
import models.add.SubcontractorName
import models.{CheckMode, NormalMode, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.add.{IndividualNamesOptionsPage, SubcontractorNamePage, TradingNameOfSubcontractorPage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.add.IndividualNamesOptionsView

import scala.concurrent.Future

class IndividualNamesOptionsControllerSpec extends SpecBase with MockitoSugar {

  private lazy val individualNamesOptionsRoute =
    controllers.add.routes.IndividualNamesOptionsController.onPageLoad(NormalMode).url

  private val formProvider = new IndividualNamesOptionsFormProvider()
  private val form         = formProvider()

  val subcontractorName = SubcontractorName(
    firstName = "John",
    middleName = Some("Paul"),
    lastName = "Smith"
  )

  val tradingName = "Test Trading"

  "IndividualNamesOptions Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, individualNamesOptionsRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[IndividualNamesOptionsView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers =
        UserAnswers(userAnswersId).set(IndividualNamesOptionsPage, IndividualNamesOptions.values.toSet).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, individualNamesOptionsRoute)

        val view = application.injector.instanceOf[IndividualNamesOptionsView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(IndividualNamesOptions.values.toSet), NormalMode)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to the SubcontractorName Page when valid data (Only SubcontractorName is selected) is submitted  " in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, individualNamesOptionsRoute)
            .withFormUrlEncodedBody(("value[0]", IndividualNamesOptions.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.add.routes.SubcontractorNameController
          .onPageLoad(NormalMode)
          .url
      }
    }

    "must redirect to the SubcontractorName Page when valid data (Both name options are selected) is submitted  " in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, individualNamesOptionsRoute)
            .withFormUrlEncodedBody(
              ("value[0]", IndividualNamesOptions.SubcontractorName.toString),
              ("value[1]", IndividualNamesOptions.TradingName.toString)
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.add.routes.SubcontractorNameController
          .onPageLoad(NormalMode)
          .url
      }
    }

    "must redirect to the TradingNameOfSubcontractor Page when valid data (Only TradingName is selected) is submitted  " in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, individualNamesOptionsRoute)
            .withFormUrlEncodedBody(("value[0]", IndividualNamesOptions.TradingName.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.add.routes.TradingNameOfSubcontractorController
          .onPageLoad(NormalMode)
          .url
      }
    }

    "must redirect to CYA in CheckMode when all selected name options already have answers" in {

      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val userAnswers = emptyUserAnswers
        .set(
          IndividualNamesOptionsPage,
          Set(IndividualNamesOptions.SubcontractorName, IndividualNamesOptions.TradingName)
        )
        .success
        .value
        .set(SubcontractorNamePage, subcontractorName)
        .success
        .value
        .set(TradingNameOfSubcontractorPage, tradingName)
        .success
        .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

      running(application) {
        val request =
          FakeRequest(
            POST,
            controllers.add.routes.IndividualNamesOptionsController.onPageLoad(CheckMode).url
          )
            .withFormUrlEncodedBody(
              ("value[0]", IndividualNamesOptions.SubcontractorName.toString),
              ("value[1]", IndividualNamesOptions.TradingName.toString)
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.add.routes.CheckYourAnswersController
          .onPageLoad()
          .url
      }
    }

    "must redirect to the SubcontractorName page in CheckMode when SubcontractorName option has no answer and Subcontractor option is selected" in {

      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val userAnswers = emptyUserAnswers
        .set(
          IndividualNamesOptionsPage,
          Set(IndividualNamesOptions.SubcontractorName, IndividualNamesOptions.TradingName)
        )
        .success
        .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

      running(application) {
        val request =
          FakeRequest(
            POST,
            controllers.add.routes.IndividualNamesOptionsController.onPageLoad(CheckMode).url
          )
            .withFormUrlEncodedBody(
              ("value[0]", IndividualNamesOptions.SubcontractorName.toString),
              ("value[1]", IndividualNamesOptions.TradingName.toString)
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.add.routes.SubcontractorNameController
          .onPageLoad(CheckMode)
          .url
      }
    }

    "must redirect to the TradingNameOfSubcontractor page in CheckMode when both options are selected, SubcontractorName is answered, TradingName is not" in {

      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val userAnswers = emptyUserAnswers
        .set(
          IndividualNamesOptionsPage,
          Set(IndividualNamesOptions.SubcontractorName, IndividualNamesOptions.TradingName)
        )
        .success
        .value
        .set(SubcontractorNamePage, subcontractorName)
        .success
        .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

      running(application) {
        val request =
          FakeRequest(
            POST,
            controllers.add.routes.IndividualNamesOptionsController.onPageLoad(CheckMode).url
          )
            .withFormUrlEncodedBody(
              ("value[0]", IndividualNamesOptions.SubcontractorName.toString),
              ("value[1]", IndividualNamesOptions.TradingName.toString)
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.add.routes.TradingNameOfSubcontractorController
          .onPageLoad(CheckMode)
          .url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, individualNamesOptionsRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[IndividualNamesOptionsView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, individualNamesOptionsRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, individualNamesOptionsRoute)
            .withFormUrlEncodedBody(("value[0]", IndividualNamesOptions.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must return a Bad Request and errors when no value is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, individualNamesOptionsRoute)
            .withFormUrlEncodedBody()

        val form      = new IndividualNamesOptionsFormProvider()()
        val boundForm = form.bind(Map.empty)

        val view = application.injector.instanceOf[IndividualNamesOptionsView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(
          request,
          messages(application)
        ).toString

        contentAsString(result) must include(messages(application)("individualNamesOptions.error.required"))
      }
    }
  }
}
