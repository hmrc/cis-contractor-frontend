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
import forms.add.IndividualChooseContactDetailsFormProvider
import models.add.{IndividualChooseContactDetails, SubcontractorName}
import models.contact.ContactOptions
import models.{CheckMode, NormalMode, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.add.{IndividualChooseContactDetailsPage, SubcontractorNamePage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.add.IndividualChooseContactDetailsView

import scala.concurrent.Future

class IndividualChooseContactDetailsControllerSpec extends SpecBase with MockitoSugar {

  private val formProvider = new IndividualChooseContactDetailsFormProvider()
  private val form         = formProvider()

  lazy private val individualChooseContactDetailsRoute =
    controllers.add.routes.IndividualChooseContactDetailsController.onPageLoad(NormalMode).url

  lazy private val individualChooseContactDetailsCheckRoute =
    controllers.add.routes.IndividualChooseContactDetailsController.onPageLoad(CheckMode).url

  private val subContractorName = SubcontractorName("John", Some("Paul"), "Smith")

  private val name = "John Smith"

  private def uaWithName: UserAnswers =
    emptyUserAnswers.set(SubcontractorNamePage, subContractorName).success.value

  "IndividualChooseContactDetails Controller" - {

    "must return OK and the correct view for a GET" in {

      val userAnswers = UserAnswers(userAnswersId).set(SubcontractorNamePage, subContractorName).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, individualChooseContactDetailsRoute)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[IndividualChooseContactDetailsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, name)(
          request,
          messages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId)
        .set(IndividualChooseContactDetailsPage, ContactOptions.Email: IndividualChooseContactDetails)
        .flatMap(_.set(SubcontractorNamePage, subContractorName))
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, individualChooseContactDetailsRoute)
        val view    = application.injector.instanceOf[IndividualChooseContactDetailsView]
        val result  = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill(ContactOptions.Email: IndividualChooseContactDetails),
          NormalMode,
          name
        )(
          request,
          messages(application)
        ).toString
      }
    }

    "must save the answer and redirect to next page when valid data is submitted" in {

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
          FakeRequest(POST, individualChooseContactDetailsRoute)
            .withFormUrlEncodedBody(("value", ContactOptions.Phone.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(
          result
        ).value mustEqual controllers.add.routes.IndividualChooseContactDetailsController
          .onPageLoad(NormalMode)
          .url
      }
    }

    "must return a Bad Request and error when no value is submitted" in {

      val application = applicationBuilder(userAnswers = Some(uaWithName)).build()

      running(application) {
        val request =
          FakeRequest(POST, individualChooseContactDetailsRoute)
            .withFormUrlEncodedBody()

        val form      = new IndividualChooseContactDetailsFormProvider()()
        val boundForm = form.bind(Map.empty)

        val view = application.injector.instanceOf[IndividualChooseContactDetailsView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, name)(
          request,
          messages(application)
        ).toString

        contentAsString(result) must include(messages(application)("individualChooseContactDetails.error.required"))
      }
    }

    "must redirect to Journey Recovery for a GET if subcontractor is missing" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, individualChooseContactDetailsRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if subcontractor is missing" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, individualChooseContactDetailsRoute)
            .withFormUrlEncodedBody(("value", ContactOptions.Mobile.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "CheckMode GET must return OK and correct view" in {
      val userAnswers =
        UserAnswers(userAnswersId).set(SubcontractorNamePage, subContractorName).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, individualChooseContactDetailsCheckRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[IndividualChooseContactDetailsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, CheckMode, name)(
          request,
          messages(application)
        ).toString
      }
    }

    "CheckMode GET must populate the view correctly when the question has previously been answered for subcontractor" in {

      val userAnswers = UserAnswers(userAnswersId)
        .set(IndividualChooseContactDetailsPage, ContactOptions.Mobile: IndividualChooseContactDetails)
        .flatMap(_.set(SubcontractorNamePage, subContractorName))
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, individualChooseContactDetailsCheckRoute)

        val view = application.injector.instanceOf[IndividualChooseContactDetailsView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill(ContactOptions.Mobile),
          CheckMode,
          name
        )(
          request,
          messages(application)
        ).toString
      }
    }

    "CheckMode POST must redirect to Journey Recovery for a GET if subcontractor name is missing" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, individualChooseContactDetailsCheckRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "CheckMode POST must redirect to Journey Recovery for a POST if subcontractor name is missing" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, individualChooseContactDetailsCheckRoute)
            .withFormUrlEncodedBody(("value", ContactOptions.Phone.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

  }
}
