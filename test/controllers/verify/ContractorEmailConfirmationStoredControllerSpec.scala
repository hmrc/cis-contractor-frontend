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

package controllers.verify

import base.SpecBase
import controllers.routes
import forms.verify.ContractorEmailConfirmationStoredFormProvider
import models.verify.ContractorEmailConfirmationStored
import models.{ContractorScheme, NormalMode, UserAnswers}
import models.response.GetNewestVerificationBatchResponse
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.verification.NewestVerificationBatchResponsePage
import pages.verify.ContractorEmailConfirmationStoredPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.verify.ContractorEmailConfirmationStoredView

import scala.concurrent.Future

class ContractorEmailConfirmationStoredControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute: Call = Call("GET", "/foo")

  val formProvider      = new ContractorEmailConfirmationStoredFormProvider()
  private val form      = formProvider()
  private val testEmail = "test@example.com"

  private lazy val contractorEmailConfirmationStoredRoute =
    controllers.verify.routes.ContractorEmailConfirmationStoredController.onPageLoad(NormalMode).url

  private val testScheme = ContractorScheme(
    accountsOfficeReference = Some("123PA12345678"),
    utr = Some("123"),
    name = Some("peter"),
    emailAddress = Some(testEmail)
  )

  private val testResponse = GetNewestVerificationBatchResponse(
    scheme = Some(testScheme),
    subcontractors = Seq.empty,
    verificationBatch = None,
    verifications = Seq.empty,
    submission = None,
    monthlyReturn = None
  )

  private def userAnswersWithEmail: UserAnswers =
    emptyUserAnswers.set(NewestVerificationBatchResponsePage, testResponse).success.value

  "ContractorEmailConfirmationStored Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersWithEmail)).build()

      running(application) {
        val request = FakeRequest(GET, contractorEmailConfirmationStoredRoute)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[ContractorEmailConfirmationStoredView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, testEmail)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = userAnswersWithEmail
        .set(ContractorEmailConfirmationStoredPage, ContractorEmailConfirmationStored.CurrentEmail)
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, contractorEmailConfirmationStoredRoute)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[ContractorEmailConfirmationStoredView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill(ContractorEmailConfirmationStored.CurrentEmail),
          NormalMode,
          testEmail
        )(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET when the email address is absent from the scheme" in {

      val responseWithNoEmail = testResponse.copy(scheme = Some(testScheme.copy(emailAddress = None)))
      val userAnswers         = emptyUserAnswers.set(NewestVerificationBatchResponsePage, responseWithNoEmail).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, contractorEmailConfirmationStoredRoute)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a GET when no batch response is stored" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, contractorEmailConfirmationStoredRoute)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, contractorEmailConfirmationStoredRoute)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithEmail))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, contractorEmailConfirmationStoredRoute)
            .withFormUrlEncodedBody(("value", "currentEmail"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersWithEmail)).build()

      running(application) {
        val request =
          FakeRequest(POST, contractorEmailConfirmationStoredRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))
        val view      = application.injector.instanceOf[ContractorEmailConfirmationStoredView]
        val result    = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, testEmail)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a POST when the email address is absent from the scheme" in {

      val responseWithNoEmail = testResponse.copy(scheme = Some(testScheme.copy(emailAddress = None)))
      val userAnswers         = emptyUserAnswers.set(NewestVerificationBatchResponsePage, responseWithNoEmail).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, contractorEmailConfirmationStoredRoute)
            .withFormUrlEncodedBody(("value", "currentEmail"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, contractorEmailConfirmationStoredRoute)
            .withFormUrlEncodedBody(("value", "currentEmail"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
