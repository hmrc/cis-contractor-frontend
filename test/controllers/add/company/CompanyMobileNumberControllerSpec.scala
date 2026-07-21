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

import base.SpecBase
import controllers.routes
import forms.add.company.CompanyMobileNumberFormProvider
import models.contact.ContactMethodOptions
import models.{AmendMode, NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.add.company.{CompanyContactMethodOptionsPage, CompanyMobileNumberPage, CompanyNamePage}
import pages.amend.AmendedPagesPage
import play.api.data.Form
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.add.company.CompanyMobileNumberView

import scala.concurrent.Future

class CompanyMobileNumberControllerSpec extends SpecBase with MockitoSugar {

  val formProvider       = new CompanyMobileNumberFormProvider()
  val form: Form[String] = formProvider()

  private val companyName = "Test Company"

  lazy val companyMobileNumberRoute: String =
    controllers.add.company.routes.CompanyMobileNumberController.onPageLoad(NormalMode).url

  private def uaWithName: UserAnswers =
    emptyUserAnswers
      .set(CompanyNamePage, companyName)
      .success
      .value

  private def uaWithNameAndMobileOption: UserAnswers =
    uaWithName
      .set(CompanyContactMethodOptionsPage, Set(ContactMethodOptions.Mobile))
      .success
      .value

  "CompanyMobileNumberController" - {

    "must return OK and the correct view for a GET when Mobile is selected" in {

      val application = applicationBuilder(userAnswers = Some(uaWithNameAndMobileOption)).build()

      running(application) {
        val request = FakeRequest(GET, companyMobileNumberRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CompanyMobileNumberView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, companyName)(
          request,
          messages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when previously answered" in {

      val userAnswers =
        uaWithNameAndMobileOption
          .set(CompanyMobileNumberPage, "+44 7700 900 999")
          .success
          .value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, companyMobileNumberRoute)

        val view = application.injector.instanceOf[CompanyMobileNumberView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill("+44 7700 900 999"), NormalMode, companyName)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to the CompanyUtrYesNo page and not add the page to AmendedPagesPage when valid data is submitted in NormalMode" in {
      val captor = ArgumentCaptor.forClass(classOf[UserAnswers])
      val onwardRoute = controllers.add.company.routes.CompanyUtrYesNoController.onPageLoad(NormalMode)
      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(uaWithNameAndMobileOption))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, companyMobileNumberRoute)
            .withFormUrlEncodedBody(("value", "+44 7700 900 999"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
        verify(mockSessionRepository).set(captor.capture())

        val updatedAnswers = captor.getValue

        updatedAnswers.get(CompanyMobileNumberPage) mustBe Some("+44 7700 900 999")
        updatedAnswers.get(AmendedPagesPage) mustBe None
      }
    }

    "must add the page to AmendedPagesPage when valid data is submitted in AmendMode" in {
      val captor = ArgumentCaptor.forClass(classOf[UserAnswers])
      val onwardRoute = controllers.add.company.routes.CompanyUtrYesNoController.onPageLoad(AmendMode)
      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(uaWithNameAndMobileOption))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, controllers.add.company.routes.CompanyMobileNumberController.onPageLoad(AmendMode).url)
            .withFormUrlEncodedBody(("value", "+44 7700 900 999"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
        verify(mockSessionRepository).set(captor.capture())

        val updatedAnswers = captor.getValue

        updatedAnswers.get(CompanyMobileNumberPage) mustBe Some("+44 7700 900 999")
        updatedAnswers.get(AmendedPagesPage) mustBe Some(Set(CompanyMobileNumberPage.toString))
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(uaWithNameAndMobileOption)).build()

      running(application) {
        val request =
          FakeRequest(POST, companyMobileNumberRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[CompanyMobileNumberView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, companyName)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, companyMobileNumberRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, companyMobileNumberRoute)
            .withFormUrlEncodedBody(("value", "+44 7700 900 999"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery when company name is missing" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, companyMobileNumberRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to JourneyRecovery if CompanyName is missing for a POST" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, companyMobileNumberRoute)
            .withFormUrlEncodedBody(("value", "+44 7700 900 999"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a GET when CompanyContactMethodOptions is missing" in {

      val application = applicationBuilder(userAnswers = Some(uaWithName)).build()

      running(application) {
        val request = FakeRequest(GET, companyMobileNumberRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a GET when Mobile is not in CompanyContactMethodOptions" in {

      val userAnswers =
        uaWithName
          .set(CompanyContactMethodOptionsPage, Set(ContactMethodOptions.Phone))
          .success
          .value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, companyMobileNumberRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST when CompanyContactMethodOptions is missing" in {

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
          FakeRequest(POST, companyMobileNumberRoute)
            .withFormUrlEncodedBody(("value", "+44 7700 900 999"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST when Mobile is not in CompanyContactMethodOptions" in {

      val userAnswers =
        uaWithName
          .set(CompanyContactMethodOptionsPage, Set(ContactMethodOptions.Phone))
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
          FakeRequest(POST, companyMobileNumberRoute)
            .withFormUrlEncodedBody(("value", "+44 7700 900 999"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
