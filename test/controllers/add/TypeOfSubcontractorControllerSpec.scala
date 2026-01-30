/*
 * Copyright 2025 HM Revenue & Customs
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
import forms.add.TypeOfSubcontractorFormProvider
import models.add.TypeOfSubcontractor
import models.{NormalMode, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.add.TypeOfSubcontractorPage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.add.TypeOfSubcontractorView

import scala.concurrent.Future

class TypeOfSubcontractorControllerSpec extends SpecBase with MockitoSugar {

  lazy val subcontractorTypesRoute = controllers.add.routes.TypeOfSubcontractorController.onPageLoad(NormalMode).url

  val formProvider = new TypeOfSubcontractorFormProvider()
  val form         = formProvider()

  "TypeOfSubcontractor Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, subcontractorTypesRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[TypeOfSubcontractorView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers =
        UserAnswers(userAnswersId).set(TypeOfSubcontractorPage, TypeOfSubcontractor.values.head).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, subcontractorTypesRoute)

        val view = application.injector.instanceOf[TypeOfSubcontractorView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(TypeOfSubcontractor.values.head), NormalMode)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to the SubTradingNameYesNo page when valid data Individualorsoletrader is submitted" in {

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
          FakeRequest(POST, subcontractorTypesRoute)
            .withFormUrlEncodedBody(("value", TypeOfSubcontractor.Individualorsoletrader.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.add.routes.SubTradingNameYesNoController
          .onPageLoad(NormalMode)
          .url
      }
    }

    "must redirect to the JourneyRecovery page when valid data Limitedcompany is submitted" in {

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
          FakeRequest(POST, subcontractorTypesRoute)
            .withFormUrlEncodedBody(("value", TypeOfSubcontractor.Limitedcompany.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to the PartnershipUniqueTaxpayerReference page when valid data Partnership is submitted" in {

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
          FakeRequest(POST, subcontractorTypesRoute)
            .withFormUrlEncodedBody(("value", TypeOfSubcontractor.Partnership.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(
          result
        ).value mustEqual controllers.add.partnership.routes.PartnershipHasUtrYesNoController // TODO Update to correct page when implemented
          .onPageLoad(NormalMode)
          .url
      }
    }

    "must redirect to the JourneyRecovery page when valid data Trust is submitted" in {

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
          FakeRequest(POST, subcontractorTypesRoute)
            .withFormUrlEncodedBody(("value", TypeOfSubcontractor.Trust.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, subcontractorTypesRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[TypeOfSubcontractorView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
      }
    }

    "must return a Bad Request and errors when no value is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, subcontractorTypesRoute)
            .withFormUrlEncodedBody()

        val form      = new TypeOfSubcontractorFormProvider()()
        val boundForm = form.bind(Map.empty)

        val view = application.injector.instanceOf[TypeOfSubcontractorView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString

        contentAsString(result) must include(messages(application)("typeOfSubcontractor.error.required"))
      }
    }

  }
}
