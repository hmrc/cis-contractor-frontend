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

package controllers.add.trust

import base.SpecBase
import controllers.routes
import forms.add.trust.TrustMobileNumberFormProvider
import models.contact.ContactMethodOptions
import models.{AmendMode, NormalMode, UserAnswers}
import navigation.Navigator
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.add.trust.{TrustContactMethodOptionsPage, TrustMobileNumberPage, TrustNamePage}
import pages.amend.AmendedPagesPage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.add.trust.TrustMobileNumberView

import scala.concurrent.Future

class TrustMobileNumberControllerSpec extends SpecBase with MockitoSugar {

  val formProvider = new TrustMobileNumberFormProvider()
  val form         = formProvider()

  private val trustName = "Test Trust"

  lazy val trustMobileNumberRoute: String =
    controllers.add.trust.routes.TrustMobileNumberController.onPageLoad(NormalMode).url

  private def uaWithName: UserAnswers =
    emptyUserAnswers
      .set(TrustNamePage, trustName)
      .success
      .value

  private def uaWithNameAndMobileOption: UserAnswers =
    uaWithName
      .set(TrustContactMethodOptionsPage, Set(ContactMethodOptions.Mobile))
      .success
      .value

  "TrustMobileNumberController" - {

    "must return OK and the correct view for a GET when Mobile is selected" in {

      val application = applicationBuilder(userAnswers = Some(uaWithNameAndMobileOption)).build()

      running(application) {
        val request = FakeRequest(GET, trustMobileNumberRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[TrustMobileNumberView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, trustName)(
          request,
          messages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when previously answered" in {

      val userAnswers =
        uaWithNameAndMobileOption
          .set(TrustMobileNumberPage, "07700 900 982")
          .success
          .value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, trustMobileNumberRoute)

        val view = application.injector.instanceOf[TrustMobileNumberView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill("07700 900 982"), NormalMode, trustName)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to the next page and not add page to AmendedPagesPage when valid data is submitted in NormalMode" in {
      val mobileNumber = "07700 900 982"

      val mockSessionRepository = mock[SessionRepository]
      val mockNavigator         = mock[Navigator]
      val captor = ArgumentCaptor.forClass(classOf[UserAnswers])

      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      when(mockNavigator.nextPage(any(), any(), any()))
        .thenReturn(
          controllers.add.trust.routes.TrustUtrYesNoController
            .onPageLoad(NormalMode)
        )

      val application =
        applicationBuilder(userAnswers = Some(uaWithNameAndMobileOption))
          .overrides(
            bind[Navigator].toInstance(mockNavigator),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {

        val request =
          FakeRequest(POST, trustMobileNumberRoute)
            .withFormUrlEncodedBody("value" -> mobileNumber)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.add.trust.routes.TrustUtrYesNoController
            .onPageLoad(NormalMode)
            .url

        verify(mockSessionRepository).set(captor.capture())

        val updatedAnswers = captor.getValue

        updatedAnswers.get(TrustMobileNumberPage) mustBe Some(mobileNumber)
        updatedAnswers.get(AmendedPagesPage) mustBe None
      }
    }

    "must add TrustMobileNumberPage to AmendedPagesPage when submitted in AmendMode" in {
      val mockSessionRepository = mock[SessionRepository]
      val mockNavigator = mock[Navigator]

      val captor = ArgumentCaptor.forClass(classOf[UserAnswers])

      when(mockSessionRepository.set(any()))
        .thenReturn(Future.successful(true))

      when(mockNavigator.nextPage(any(), any(), any()))
        .thenReturn(
          controllers.add.trust.routes.TrustUtrYesNoController
            .onPageLoad(AmendMode)
        )

      val application =
        applicationBuilder(userAnswers = Some(uaWithNameAndMobileOption))
          .overrides(
            bind[Navigator].toInstance(mockNavigator),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {

        val request =
          FakeRequest(
            POST,
            controllers.add.trust.routes.TrustMobileNumberController
              .onSubmit(AmendMode)
              .url
          ).withFormUrlEncodedBody(
            "value" -> "07700 900 982"
          )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.add.trust.routes.TrustUtrYesNoController.onPageLoad(AmendMode).url
        verify(mockSessionRepository).set(captor.capture())

        val updatedAnswers = captor.getValue

        updatedAnswers.get(TrustMobileNumberPage) mustBe Some("07700 900 982")
        updatedAnswers.get(AmendedPagesPage).value must contain(TrustMobileNumberPage.toString)
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(uaWithNameAndMobileOption)).build()

      running(application) {
        val request =
          FakeRequest(POST, trustMobileNumberRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[TrustMobileNumberView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, trustName)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, trustMobileNumberRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, trustMobileNumberRoute)
            .withFormUrlEncodedBody(("value", "answer"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a GET when trust name is missing" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, trustMobileNumberRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to JourneyRecovery if trustName is missing for a POST" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, trustMobileNumberRoute)
            .withFormUrlEncodedBody(("value", "07700 900 982"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a GET when TrustContactMethodOptions is missing" in {

      val application = applicationBuilder(userAnswers = Some(uaWithName)).build()

      running(application) {
        val request = FakeRequest(GET, trustMobileNumberRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a GET when Mobile is not in TrustContactMethodOptions" in {

      val userAnswers =
        uaWithName
          .set(TrustContactMethodOptionsPage, Set(ContactMethodOptions.Phone))
          .success
          .value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, trustMobileNumberRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST when TrustContactMethodOptions is missing" in {

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
          FakeRequest(POST, trustMobileNumberRoute)
            .withFormUrlEncodedBody(("value", "12345678"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST when Mobile is not in TrustContactMethodOptions" in {

      val userAnswers =
        uaWithName
          .set(TrustContactMethodOptionsPage, Set(ContactMethodOptions.Phone))
          .success
          .value

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, trustMobileNumberRoute)
            .withFormUrlEncodedBody(("value", "12345678"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
