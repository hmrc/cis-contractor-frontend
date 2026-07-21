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
import forms.add.company.CompanyContactMethodOptionsFormProvider
import models.add.company.CompanyContactMethodOptions
import models.contact.ContactMethodOptions
import models.{AmendMode, CheckMode, NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.add.company.{CompanyContactMethodOptionsPage, CompanyEmailAddressPage, CompanyNamePage, CompanyPhoneNumberPage}
import pages.amend.AmendedPagesPage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.add.company.CompanyContactMethodOptionsView

import scala.concurrent.Future

class CompanyContactMethodOptionsControllerSpec extends SpecBase with MockitoSugar {

  private lazy val companyContactMethodOptionsRoute =
    controllers.add.company.routes.CompanyContactMethodOptionsController.onPageLoad(NormalMode).url

  private val formProvider = new CompanyContactMethodOptionsFormProvider()
  private val form         = formProvider()

  private val companyName = "Test Company"

  private def uaWithName: UserAnswers =
    emptyUserAnswers.set(CompanyNamePage, companyName).success.value

  "CompanyContactMethodOptions Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(uaWithName)).build()

      running(application) {
        val request = FakeRequest(GET, companyContactMethodOptionsRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CompanyContactMethodOptionsView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual view(form, NormalMode, companyName)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers =
        uaWithName.set(CompanyContactMethodOptionsPage, CompanyContactMethodOptions.values.toSet).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, companyContactMethodOptionsRoute)

        val view = application.injector.instanceOf[CompanyContactMethodOptionsView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill(CompanyContactMethodOptions.values.toSet),
          NormalMode,
          companyName
        )(request, messages(application)).toString
      }
    }

    "must redirect to the next page and not add the page to AmendedPagesPage when valid data is submitted in NormalMode" in {
      val mockSessionRepository = mock[SessionRepository]
      val captor                = ArgumentCaptor.forClass(classOf[UserAnswers])
      val onwardRoute           = controllers.add.company.routes.CompanyEmailAddressController.onPageLoad(NormalMode)
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(uaWithName))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, companyContactMethodOptionsRoute)
            .withFormUrlEncodedBody(("value[0]", CompanyContactMethodOptions.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
        verify(mockSessionRepository).set(captor.capture())

        val updatedAnswers = captor.getValue

        updatedAnswers.get(CompanyContactMethodOptionsPage) mustBe Some(Set(ContactMethodOptions.Email))
        updatedAnswers.get(AmendedPagesPage) mustBe None
      }
    }

    "must add TrustContactMethodOptionsPage to AmendedPagesPage when submitted in AmendMode" in {
      val mockSessionRepository = mock[SessionRepository]
      val captor                = ArgumentCaptor.forClass(classOf[UserAnswers])
      val onwardRoute           = controllers.add.company.routes.CompanyEmailAddressController.onPageLoad(AmendMode)

      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(uaWithName))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {

        val request =
          FakeRequest(
            POST,
            controllers.add.company.routes.CompanyContactMethodOptionsController
              .onPageLoad(AmendMode)
              .url
          ).withFormUrlEncodedBody(
            "value[0]" -> ContactMethodOptions.Email.toString
          )

        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url

        verify(mockSessionRepository).set(captor.capture())
        val updatedAnswers = captor.getValue
        updatedAnswers.get(CompanyContactMethodOptionsPage) mustBe Some(Set(ContactMethodOptions.Email))
        updatedAnswers.get(AmendedPagesPage).value must contain(CompanyContactMethodOptionsPage.toString)
      }
    }

    "must redirect to CYA in CheckMode when all selected contact methods already have answers" in {

      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val userAnswers = uaWithName
        .set(CompanyContactMethodOptionsPage, Set(ContactMethodOptions.Email, ContactMethodOptions.Phone))
        .success
        .value
        .set(CompanyEmailAddressPage, "test@example.com")
        .success
        .value
        .set(CompanyPhoneNumberPage, "01234567890")
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
            controllers.add.company.routes.CompanyContactMethodOptionsController.onPageLoad(CheckMode).url
          )
            .withFormUrlEncodedBody(
              ("value[0]", ContactMethodOptions.Email.toString),
              ("value[1]", ContactMethodOptions.Phone.toString)
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.add.company.routes.CompanyCheckYourAnswersController
          .onPageLoad()
          .url
      }
    }

    "must redirect to the first missing contact method page in CheckMode when the first selected method has no answer" in {

      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val userAnswers = uaWithName
        .set(CompanyContactMethodOptionsPage, Set(ContactMethodOptions.Email, ContactMethodOptions.Phone))
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
            controllers.add.company.routes.CompanyContactMethodOptionsController.onPageLoad(CheckMode).url
          )
            .withFormUrlEncodedBody(
              ("value[0]", ContactMethodOptions.Email.toString),
              ("value[1]", ContactMethodOptions.Phone.toString)
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.add.company.routes.CompanyEmailAddressController
          .onPageLoad(CheckMode)
          .url
      }
    }

    "must redirect to the next missing contact method page in CheckMode when the first method is answered but a later one is not" in {

      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val userAnswers = uaWithName
        .set(CompanyContactMethodOptionsPage, Set(ContactMethodOptions.Email, ContactMethodOptions.Mobile))
        .success
        .value
        .set(CompanyEmailAddressPage, "test@example.com")
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
            controllers.add.company.routes.CompanyContactMethodOptionsController.onPageLoad(CheckMode).url
          )
            .withFormUrlEncodedBody(
              ("value[0]", ContactMethodOptions.Email.toString),
              ("value[1]", ContactMethodOptions.Mobile.toString)
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.add.company.routes.CompanyMobileNumberController
          .onPageLoad(CheckMode)
          .url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted (name present)" in {

      val application = applicationBuilder(userAnswers = Some(uaWithName)).build()

      running(application) {
        val request =
          FakeRequest(POST, companyContactMethodOptionsRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[CompanyContactMethodOptionsView]

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
        val request = FakeRequest(GET, companyContactMethodOptionsRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, companyContactMethodOptionsRoute)
            .withFormUrlEncodedBody(("value[0]", CompanyContactMethodOptions.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must return a Bad Request and errors when no value is submitted" in {

      val application = applicationBuilder(userAnswers = Some(uaWithName)).build()

      running(application) {
        val request =
          FakeRequest(POST, companyContactMethodOptionsRoute)
            .withFormUrlEncodedBody()

        val form      = new CompanyContactMethodOptionsFormProvider()()
        val boundForm = form.bind(Map.empty)

        val view = application.injector.instanceOf[CompanyContactMethodOptionsView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, companyName)(
          request,
          messages(application)
        ).toString

        contentAsString(result) must include(messages(application)("companyContactMethodOptions.error.required"))
      }
    }

    "must redirect to Journey Recovery for a GET when subcontractor name is missing (userAnswers present)" in {

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, companyContactMethodOptionsRoute)

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
          FakeRequest(POST, companyContactMethodOptionsRoute)
            .withFormUrlEncodedBody("value" -> "true")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
