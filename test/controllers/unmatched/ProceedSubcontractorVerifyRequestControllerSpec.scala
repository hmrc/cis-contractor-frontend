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

package controllers.unmatched

import base.SpecBase
import controllers.routes
import controllers.unmatched.routes as unmatchedRoutes
import forms.unmatched.ProceedSubcontractorVerifyRequestFormProvider
import models.response.GetCurrentVerificationBatchResponse
import models.{SubcontractorCurrentVerification, UserAnswers, VerificationBatchCurrentVerification, VerificationCurrentVerification}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.unmatched.ProceedSubcontractorVerifyRequestPage
import pages.verify.CurrentVerificationBatchResponsePage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import queries.CisIdQuery
import repositories.SessionRepository
import services.VerificationService
import uk.gov.hmrc.http.HeaderCarrier
import views.html.unmatched.ProceedSubcontractorVerifyRequestView

import scala.concurrent.Future

class ProceedSubcontractorVerifyRequestControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider      = new ProceedSubcontractorVerifyRequestFormProvider()
  val form              = formProvider()
  val subcontractorName = "Test Subcontractor"

  private val subcontractorId = 10

  private val unmappedSubcontractorId = 999999L

  private lazy val proceedSubcontractorVerifyRequestRoute =
    unmatchedRoutes.ProceedSubcontractorVerifyRequestController.onPageLoad(subcontractorId).url

  private lazy val proceedSubcontractorVerifyRequestRouteUnmappedSubcontractorIdUrl =
    controllers.insufficient.routes.ProceedInsufficientSubcontractorNameYesNoController
      .onPageLoad(unmappedSubcontractorId)
      .url

  private val currentBatchResponse: GetCurrentVerificationBatchResponse =
    GetCurrentVerificationBatchResponse(
      subcontractors = Seq(
        SubcontractorCurrentVerification(
          subcontractorId = subcontractorId,
          subbieResourceRef = Some(1111L),
          firstName = None,
          secondName = None,
          surname = None,
          tradingName = Some(subcontractorName),
          utr = None,
          nino = None,
          crn = None,
          partnerUtr = None,
          partnershipTradingName = None,
          subcontractorType = None,
          addressLine1 = None,
          addressLine2 = None,
          addressLine3 = None,
          addressLine4 = None,
          country = None,
          postcode = None,
          emailAddress = None,
          phoneNumber = None,
          mobilePhoneNumber = None,
          worksReferenceNumber = None,
          matched = None,
          autoVerified = None,
          verified = None,
          verificationNumber = None,
          taxTreatment = None,
          verificationDate = None,
          version = None,
          updatedTaxTreatment = None,
          lastMonthlyReturnDate = None,
          pendingVerifications = None
        )
      ),
      verificationBatch = Some(
        VerificationBatchCurrentVerification(
          verificationBatchId = 999L,
          verifBatchResourceRef = Some(7777L)
        )
      ),
      verifications = Seq(
        VerificationCurrentVerification(
          verificationId = 1L,
          verificationBatchId = Some(999L),
          subcontractorId = Some(subcontractorId),
          verificationResourceRef = Some(1111L),
          subcontractorName = None,
          verificationNumber = None,
          taxTreatment = None,
          actionIndicator = None,
          proceed = None,
          matched = None
        )
      )
    )

  "ProceedSubcontractorVerifyRequest Controller" - {

    "must return OK and the correct view for a GET" in {

      val userAnswers = emptyUserAnswers.set(CurrentVerificationBatchResponsePage, currentBatchResponse).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, proceedSubcontractorVerifyRequestRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ProceedSubcontractorVerifyRequestView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, subcontractorName, subcontractorId)(
          request,
          messages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered NO" in {

      val userAnswers = UserAnswers(userAnswersId)
        .set(CurrentVerificationBatchResponsePage, currentBatchResponse)
        .success
        .value
        .set(ProceedSubcontractorVerifyRequestPage(subcontractorId.toString), false)
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, proceedSubcontractorVerifyRequestRoute)

        val view = application.injector.instanceOf[ProceedSubcontractorVerifyRequestView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(false), subcontractorName, subcontractorId)(
          request,
          messages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered YES" in {

      val userAnswers = UserAnswers(userAnswersId)
        .set(CurrentVerificationBatchResponsePage, currentBatchResponse)
        .success
        .value
        .set(ProceedSubcontractorVerifyRequestPage(subcontractorId.toString), true)
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, proceedSubcontractorVerifyRequestRoute)

        val result = route(application, request).value

        redirectLocation(
          result
        ).value mustEqual controllers.verify.routes.ReviewUnmatchedSubcontractorsController
          .onPageLoad()
          .url
      }
    }

    "must redirect to the next page on a POST and update CurrentVerificationBatch when valid data is submitted and answer = YES" in {

      val userAnswers = emptyUserAnswers
        .set(CisIdQuery, "1")
        .success
        .value
        .set(CurrentVerificationBatchResponsePage, currentBatchResponse)
        .success
        .value

      val mockSessionRepository = mock[SessionRepository]

      val mockBatchService = mock[VerificationService]

      when(
        mockBatchService.proceedUnmatchedVerification(any(), any(), any())(any())
      ).thenReturn(Future.successful(()))
      when(mockBatchService.getCurrentVerificationBatch(any[UserAnswers])(any[HeaderCarrier]))
        .thenReturn(Future.successful(userAnswers))
      when(mockBatchService.refreshNewestVerificationBatch(any[UserAnswers])(any[HeaderCarrier]))
        .thenReturn(Future.successful(userAnswers))

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[VerificationService].toInstance(mockBatchService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, proceedSubcontractorVerifyRequestRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(
          result
        ).value mustEqual controllers.verify.routes.ReviewUnmatchedSubcontractorsController
          .onPageLoad()
          .url
      }
    }

    "must redirect to the next page on a POST and valid data is submitted and answer = NO" in {

      val userAnswers = emptyUserAnswers
        .set(CisIdQuery, "1")
        .success
        .value
        .set(CurrentVerificationBatchResponsePage, currentBatchResponse)
        .success
        .value

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {

        val request =
          FakeRequest(POST, proceedSubcontractorVerifyRequestRoute).withFormUrlEncodedBody("value" -> "false")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(
          result
        ).value mustEqual controllers.verify.routes.ReviewUnmatchedSubcontractorsController
          .onPageLoad()
          .url
      }
    }

    "must return a Bad Request and errors on a POST  when invalid data is submitted" in {

      val userAnswers = emptyUserAnswers
        .set(CisIdQuery, "1")
        .success
        .value
        .set(CurrentVerificationBatchResponsePage, currentBatchResponse)
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, proceedSubcontractorVerifyRequestRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[ProceedSubcontractorVerifyRequestView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, subcontractorName, subcontractorId)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, proceedSubcontractorVerifyRequestRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a GET if subcontractorId is not found" in {

      val userAnswers = emptyUserAnswers.set(CurrentVerificationBatchResponsePage, currentBatchResponse).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).overrides().build()

      running(application) {

        val request = FakeRequest(GET, proceedSubcontractorVerifyRequestRouteUnmappedSubcontractorIdUrl)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, proceedSubcontractorVerifyRequestRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if subcontractorId is not found" in {

      val userAnswers = emptyUserAnswers
        .set(CisIdQuery, "1")
        .success
        .value
        .set(CurrentVerificationBatchResponsePage, currentBatchResponse)
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {

        val request =
          FakeRequest(POST, proceedSubcontractorVerifyRequestRouteUnmappedSubcontractorIdUrl).withFormUrlEncodedBody(
            "value" -> "true"
          )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          routes.JourneyRecoveryController
            .onPageLoad()
            .url
      }
    }

    "must redirect to Journey Recovery for a POST when api failed" in {

      val userAnswers = emptyUserAnswers
        .set(CisIdQuery, "1")
        .success
        .value
        .set(CurrentVerificationBatchResponsePage, currentBatchResponse)
        .success
        .value

      val mockSessionRepository = mock[SessionRepository]

      val mockBatchService = mock[VerificationService]
      when(
        mockBatchService.proceedUnmatchedVerification(any(), any(), any())(any())
      ).thenReturn(Future.successful(()))

      when(mockSessionRepository.set(any())).thenReturn(Future.failed(new RuntimeException("boom")))

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[VerificationService].toInstance(mockBatchService)
          )
          .build()

      running(application) {

        val request =
          FakeRequest(POST, proceedSubcontractorVerifyRequestRoute).withFormUrlEncodedBody("value" -> "true")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          routes.JourneyRecoveryController
            .onPageLoad()
            .url
      }
    }
  }
}
