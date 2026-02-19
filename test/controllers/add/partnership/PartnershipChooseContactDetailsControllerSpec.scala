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

package controllers.add.partnership

import base.SpecBase
import forms.add.partnership.PartnershipChooseContactDetailsFormProvider
import models.add.partnership.PartnershipChooseContactDetails
import models.{CheckMode, NormalMode, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.add.partnership.{PartnershipChooseContactDetailsPage, PartnershipNamePage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.add.partnership.PartnershipChooseContactDetailsView

import scala.concurrent.Future

class PartnershipChooseContactDetailsControllerSpec extends SpecBase with MockitoSugar {

  private val formProvider = new PartnershipChooseContactDetailsFormProvider()
  private val form         = formProvider()

  private val partnershipName = "Some Partners LLP"

  private def uaWithName: UserAnswers =
    emptyUserAnswers.set(PartnershipNamePage, partnershipName).success.value

  lazy private val partnershipChooseContactDetailsRoute =
    controllers.add.partnership.routes.PartnershipChooseContactDetailsController.onPageLoad(NormalMode).url

  lazy private val partnershipChooseContactDetailsCheckRoute =
    controllers.add.partnership.routes.PartnershipChooseContactDetailsController.onPageLoad(CheckMode).url

  "PartnershipChooseContactDetailsController" - {

    "must return OK and the correct view for a GET" in {
      val userAnswers =
        UserAnswers(userAnswersId).set(PartnershipNamePage, partnershipName).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, partnershipChooseContactDetailsRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[PartnershipChooseContactDetailsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, partnershipName)(
          request,
          messages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId)
        .set(PartnershipChooseContactDetailsPage, PartnershipChooseContactDetails.Email)
        .flatMap(_.set(PartnershipNamePage, partnershipName))
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, partnershipChooseContactDetailsRoute)

        val view = application.injector.instanceOf[PartnershipChooseContactDetailsView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill(PartnershipChooseContactDetails.Email),
          NormalMode,
          partnershipName
        )(
          request,
          messages(application)
        ).toString
      }
    }

    "must save the answer and redirect to the next page when valid data is submitted" in {

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
          FakeRequest(POST, partnershipChooseContactDetailsRoute)
            .withFormUrlEncodedBody(("value", PartnershipChooseContactDetails.Phone.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(
          result
        ).value mustEqual controllers.add.partnership.routes.PartnershipChooseContactDetailsController
          .onPageLoad(NormalMode)
          .url
      }
    }

    "must return a Bad Request and errors when no value is submitted" in {

      val application = applicationBuilder(userAnswers = Some(uaWithName)).build()

      running(application) {
        val request =
          FakeRequest(POST, partnershipChooseContactDetailsRoute)
            .withFormUrlEncodedBody()

        val form      = new PartnershipChooseContactDetailsFormProvider()()
        val boundForm = form.bind(Map.empty)

        val view = application.injector.instanceOf[PartnershipChooseContactDetailsView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, partnershipName)(
          request,
          messages(application)
        ).toString

        contentAsString(result) must include(messages(application)("partnershipChooseContactDetails.error.required"))
      }
    }

    "must redirect to Journey Recovery for a GET if partnership name is missing" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, partnershipChooseContactDetailsRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if partnership name is missing" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, partnershipChooseContactDetailsRoute)
            .withFormUrlEncodedBody(("value", PartnershipChooseContactDetails.Mobile.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "CheckMode GET must return OK and the correct view" in {
      val userAnswers =
        UserAnswers(userAnswersId).set(PartnershipNamePage, partnershipName).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, partnershipChooseContactDetailsCheckRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[PartnershipChooseContactDetailsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, CheckMode, partnershipName)(
          request,
          messages(application)
        ).toString
      }
    }

    "CheckMode GET must populate the view correctly when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId)
        .set(PartnershipChooseContactDetailsPage, PartnershipChooseContactDetails.Mobile)
        .flatMap(_.set(PartnershipNamePage, partnershipName))
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, partnershipChooseContactDetailsCheckRoute)

        val view = application.injector.instanceOf[PartnershipChooseContactDetailsView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill(PartnershipChooseContactDetails.Mobile),
          CheckMode,
          partnershipName
        )(
          request,
          messages(application)
        ).toString
      }
    }

    "TODO: CheckMode POST must for now stay on the page when question was answered until navigation is implemented" in {

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
          FakeRequest(POST, partnershipChooseContactDetailsCheckRoute)
            .withFormUrlEncodedBody(("value", PartnershipChooseContactDetails.Email.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        // TODO: CIS ANSF PTN: Screen AS-P4 (PTN) What are the contact details for [partnership name]?
        redirectLocation(
          result
        ).value mustEqual controllers.add.partnership.routes.PartnershipChooseContactDetailsController
          .onPageLoad(CheckMode)
          .url
      }
    }

    "CheckMode POST must redirect to Journey Recovery for a GET if partnership name is missing" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, partnershipChooseContactDetailsCheckRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "CheckMode POST must redirect to Journey Recovery for a POST if partnership name is missing" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, partnershipChooseContactDetailsCheckRoute)
            .withFormUrlEncodedBody(("value", PartnershipChooseContactDetails.Phone.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
