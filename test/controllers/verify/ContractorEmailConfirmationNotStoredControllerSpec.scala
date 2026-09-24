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
import forms.verify.ContractorEmailConfirmationNotStoredFormProvider
import models.NormalMode
import models.UserAnswers
import models.finalvalidation.{VerifyFinalValidationContinuation, VerifyFinalValidationResult}
import models.verify.ContractorEmailConfirmationStored
import models.response.GetNewestVerificationBatchResponse
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.{never, verify, verifyNoInteractions, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.finalvalidation.VerifyFinalValidationContinuationPage
import pages.verify.{ContractorEmailConfirmationNotStoredPage, ContractorEmailConfirmationStoredPage, NewestVerificationBatchResponsePage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import queries.CisIdQuery
import repositories.SessionRepository
import services.VerifyFinalValidationService
import uk.gov.hmrc.http.HeaderCarrier
import views.html.verify.ContractorEmailConfirmationNotStoredView

import scala.concurrent.Future

class ContractorEmailConfirmationNotStoredControllerSpec extends SpecBase with MockitoSugar {

  private def onwardRoute = Call("GET", "/foo")

  private val formProvider = new ContractorEmailConfirmationNotStoredFormProvider()
  private val form         = formProvider()

  private val cisId = "123"

  private lazy val routeUrl =
    controllers.verify.routes.ContractorEmailConfirmationNotStoredController.onPageLoad(NormalMode).url

  private lazy val finalValidationCompleteRouteUrl =
    controllers.verify.routes.ContractorEmailConfirmationNotStoredController
      .onPageLoadAfterFinalValidation(NormalMode)
      .url

  private def newestResponse(email: Option[String]): GetNewestVerificationBatchResponse =
    GetNewestVerificationBatchResponse(
      scheme = Some(
        models.ContractorScheme(
          accountsOfficeReference = None,
          utr = None,
          name = None,
          emailAddress = email
        )
      ),
      subcontractors = Nil,
      verificationBatch = None,
      verifications = Nil,
      submission = None,
      monthlyReturn = None,
      monthlyReturnSubmission = None
    )

  private def verifyFinalValidationServiceWithNoErrors(): VerifyFinalValidationService = {
    val mockVerifyFinalValidationService = mock[VerifyFinalValidationService]

    val validationResult =
      VerifyFinalValidationResult(
        subcontractors = Seq.empty,
        failures = Seq.empty
      )

    when(
      mockVerifyFinalValidationService.validate(
        any[String],
        any[UserAnswers]
      )(any[HeaderCarrier])
    ) thenReturn Future.successful(validationResult)

    mockVerifyFinalValidationService
  }

  "ContractorEmailConfirmationNotStored Controller" - {

    "must redirect to ContractorEmailConfirmationStoredController on GET when stored email exists" in {

      val ua =
        emptyUserAnswers
          .set(CisIdQuery, cisId)
          .success
          .value
          .set(NewestVerificationBatchResponsePage, newestResponse(Some("stored@test.com")))
          .success
          .value

      val mockVerifyFinalValidationService = mock[VerifyFinalValidationService]

      val application =
        applicationBuilder(userAnswers = Some(ua))
          .overrides(
            bind[VerifyFinalValidationService].toInstance(mockVerifyFinalValidationService)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, routeUrl)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.verify.routes.ContractorEmailConfirmationStoredController.onPageLoad(NormalMode).url

        verifyNoInteractions(mockVerifyFinalValidationService)
      }
    }

    "must redirect to ContractorEmailConfirmationStoredController on POST when stored email exists" in {

      val ua =
        emptyUserAnswers
          .set(NewestVerificationBatchResponsePage, newestResponse(Some("stored@test.com")))
          .success
          .value

      val mockSessionRepository = mock[SessionRepository]

      val application =
        applicationBuilder(userAnswers = Some(ua))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, routeUrl)
            .withFormUrlEncodedBody("value" -> "true")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.verify.routes.ContractorEmailConfirmationStoredController.onPageLoad(NormalMode).url

        verify(mockSessionRepository, never()).set(any())
      }
    }

    "must return OK and the correct view for a GET when no stored email exists and Final Validation has no errors" in {

      val ua =
        emptyUserAnswers
          .set(CisIdQuery, cisId)
          .success
          .value
          .set(NewestVerificationBatchResponsePage, newestResponse(None))
          .success
          .value

      val mockVerifyFinalValidationService = verifyFinalValidationServiceWithNoErrors()

      val application =
        applicationBuilder(userAnswers = Some(ua))
          .overrides(
            bind[VerifyFinalValidationService].toInstance(mockVerifyFinalValidationService)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, routeUrl)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ContractorEmailConfirmationNotStoredView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered and Final Validation has no errors" in {

      val ua =
        emptyUserAnswers
          .set(CisIdQuery, cisId)
          .success
          .value
          .set(NewestVerificationBatchResponsePage, newestResponse(None))
          .success
          .value
          .set(ContractorEmailConfirmationNotStoredPage, true)
          .success
          .value

      val mockVerifyFinalValidationService = verifyFinalValidationServiceWithNoErrors()

      val application =
        applicationBuilder(userAnswers = Some(ua))
          .overrides(
            bind[VerifyFinalValidationService].toInstance(mockVerifyFinalValidationService)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, routeUrl)

        val view = application.injector.instanceOf[ContractorEmailConfirmationNotStoredView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), NormalMode)(request, messages(application)).toString
      }
    }

    "must return OK and the correct view after Final Validation when no stored email exists" in {

      val ua =
        emptyUserAnswers
          .set(NewestVerificationBatchResponsePage, newestResponse(None))
          .success
          .value
          .set(
            VerifyFinalValidationContinuationPage,
            VerifyFinalValidationContinuation.ContractorEmailConfirmationNotStored
          )
          .success
          .value

      val mockSessionRepository               = mock[SessionRepository]
      val captor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])

      when(mockSessionRepository.set(captor.capture())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(ua))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, finalValidationCompleteRouteUrl)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ContractorEmailConfirmationNotStoredView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString

        val savedAnswers = captor.getValue
        savedAnswers.get(VerifyFinalValidationContinuationPage) mustEqual None
      }
    }

    "must populate the view correctly after Final Validation when the question has previously been answered" in {

      val ua =
        emptyUserAnswers
          .set(NewestVerificationBatchResponsePage, newestResponse(None))
          .success
          .value
          .set(ContractorEmailConfirmationNotStoredPage, true)
          .success
          .value
          .set(
            VerifyFinalValidationContinuationPage,
            VerifyFinalValidationContinuation.ContractorEmailConfirmationNotStored
          )
          .success
          .value

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(ua))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, finalValidationCompleteRouteUrl)

        val view = application.injector.instanceOf[ContractorEmailConfirmationNotStoredView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to ContractorEmailConfirmationStoredController after Final Validation when stored email exists" in {

      val ua =
        emptyUserAnswers
          .set(NewestVerificationBatchResponsePage, newestResponse(Some("stored@test.com")))
          .success
          .value
          .set(
            VerifyFinalValidationContinuationPage,
            VerifyFinalValidationContinuation.ContractorEmailConfirmationNotStored
          )
          .success
          .value

      val mockSessionRepository = mock[SessionRepository]

      val application =
        applicationBuilder(userAnswers = Some(ua))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, finalValidationCompleteRouteUrl)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.verify.routes.ContractorEmailConfirmationStoredController
            .onPageLoadAfterFinalValidation(NormalMode)
            .url

        verify(mockSessionRepository, never()).set(any())
      }
    }

    "must redirect to the next page when valid data is submitted (no stored email)" in {

      val ua =
        emptyUserAnswers
          .set(NewestVerificationBatchResponsePage, newestResponse(None))
          .success
          .value

      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(ua))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, routeUrl)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must remove ContractorEmailConfirmationStoredPage from user answers when submitting (no stored email, stored already set)" in {

      val ua =
        emptyUserAnswers
          .set(NewestVerificationBatchResponsePage, newestResponse(None))
          .success
          .value
          .set(ContractorEmailConfirmationStoredPage, ContractorEmailConfirmationStored.CurrentEmail)
          .success
          .value

      val mockSessionRepository               = mock[SessionRepository]
      val captor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
      when(mockSessionRepository.set(captor.capture())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(ua))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, routeUrl)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url

        val savedAnswers = captor.getValue
        savedAnswers.get(ContractorEmailConfirmationStoredPage) mustEqual None
      }
    }

    "must return a Bad Request and errors when invalid data is submitted (no stored email)" in {

      val ua =
        emptyUserAnswers
          .set(NewestVerificationBatchResponsePage, newestResponse(None))
          .success
          .value

      val application = applicationBuilder(userAnswers = Some(ua)).build()

      running(application) {
        val request =
          FakeRequest(POST, routeUrl)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[ContractorEmailConfirmationNotStoredView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, routeUrl)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, routeUrl)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
