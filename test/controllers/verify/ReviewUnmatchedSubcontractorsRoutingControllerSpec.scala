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
import models.response.GetLastSubmittedVerificationBatchResponse
import models.VerificationLastVerification
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.verify.LastSubmittedVerificationBatchResponsePage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import queries.CisIdQuery
import services.VerificationService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class ReviewUnmatchedSubcontractorsRoutingControllerSpec extends SpecBase with MockitoSugar {

  private val endpointUrl = "/subcontractor/verify/unmatched-subcontractors"

  private def verification(
    verificationNumber: Option[String] = Some("V0000000001"),
    matched: Option[String] = Some("Y"),
    actionIndicator: Option[String] = Some("verify"),
    verificationResourceRef: Option[Long] = Some(10L)
  ): VerificationLastVerification =
    VerificationLastVerification(
      verificationId = 1001L,
      verificationBatchId = Some(99L),
      verificationResourceRef = verificationResourceRef,
      matched = matched,
      verificationNumber = verificationNumber,
      taxTreatment = Some("net"),
      subcontractorName = Some("John Smith"),
      subcontractorId = Some(22L),
      actionIndicator = actionIndicator
    )

  private def batchResponse(
    verifications: VerificationLastVerification*
  ): GetLastSubmittedVerificationBatchResponse =
    GetLastSubmittedVerificationBatchResponse(
      scheme = None,
      subcontractors = Nil,
      verifications = verifications,
      verificationBatch = None,
      submission = None
    )

  "ReviewUnmatchedSubcontractorsRoutingController" - {

    "AC2: must redirect to UnmatchedSubcontractors when unmatched resource refs are still on the live list" in {
      val mockService = mock[VerificationService]
      val response    = batchResponse(verification(verificationNumber = None, verificationResourceRef = Some(10L)))
      val userAnswers = emptyUserAnswers
        .set(LastSubmittedVerificationBatchResponsePage, response)
        .success
        .value
        .set(CisIdQuery, "900063")
        .success
        .value

      when(
        mockService.anyUnmatchedResourceRefsStillPresent(
          eqTo("900063"),
          eqTo(response)
        )(any[HeaderCarrier])
      )
        .thenReturn(Future.successful(true))

      when(
        mockService.recreateCurrentBatchFromUnmatchedVerifications(
          eqTo("900063"),
          eqTo(userAnswers)
        )(any[HeaderCarrier])
      ).thenReturn(Future.successful(userAnswers))

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[VerificationService].toInstance(mockService))
        .build()

      running(application) {
        val result = route(application, FakeRequest(GET, endpointUrl)).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.UnmatchedSubcontractorsController.onPageLoad().url
        verify(mockService)
          .anyUnmatchedResourceRefsStillPresent(
            eqTo("900063"),
            eqTo(response)
          )(any[HeaderCarrier])

        verify(mockService)
          .recreateCurrentBatchFromUnmatchedVerifications(
            eqTo("900063"),
            eqTo(userAnswers)
          )(any[HeaderCarrier])
      }
    }

    "must redirect to SystemError when recreating the current batch fails" in {
      val mockService = mock[VerificationService]

      val response = batchResponse(
        verification(
          verificationNumber = None,
          verificationResourceRef = Some(10L)
        )
      )

      val userAnswers = emptyUserAnswers
        .set(LastSubmittedVerificationBatchResponsePage, response)
        .success
        .value
        .set(CisIdQuery, "900063")
        .success
        .value

      when(
        mockService.anyUnmatchedResourceRefsStillPresent(
          eqTo("900063"),
          eqTo(response)
        )(any[HeaderCarrier])
      ).thenReturn(Future.successful(true))

      when(
        mockService.recreateCurrentBatchFromUnmatchedVerifications(
          eqTo("900063"),
          eqTo(userAnswers)
        )(any[HeaderCarrier])
      ).thenReturn(Future.failed(new RuntimeException("boom")))

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[VerificationService].toInstance(mockService))
          .build()

      running(application) {
        val result =
          route(application, FakeRequest(GET, endpointUrl)).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.routes.SystemErrorController.onPageLoad().url
      }
    }

    "AC3: must redirect to NoUnmatchedSubcontractors when unmatched resource refs are not on the live list" in {
      val mockService = mock[VerificationService]
      val response    = batchResponse(verification(verificationNumber = None, verificationResourceRef = Some(10L)))
      val userAnswers = emptyUserAnswers
        .set(LastSubmittedVerificationBatchResponsePage, response)
        .success
        .value
        .set(CisIdQuery, "900063")
        .success
        .value

      when(mockService.anyUnmatchedResourceRefsStillPresent(eqTo("900063"), eqTo(response))(any[HeaderCarrier]))
        .thenReturn(Future.successful(false))

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[VerificationService].toInstance(mockService))
        .build()

      running(application) {
        val result = route(application, FakeRequest(GET, endpointUrl)).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.routes.NoUnmatchedSubcontractorsController.onPageLoad().url
      }
    }

    "must redirect to NoUnmatchedSubcontractors when unmatched has no resource ref" in {
      val mockService = mock[VerificationService]
      val response    = batchResponse(verification(verificationNumber = None, verificationResourceRef = None))
      val userAnswers = emptyUserAnswers
        .set(LastSubmittedVerificationBatchResponsePage, response)
        .success
        .value
        .set(CisIdQuery, "900063")
        .success
        .value

      when(mockService.anyUnmatchedResourceRefsStillPresent(eqTo("900063"), eqTo(response))(any[HeaderCarrier]))
        .thenReturn(Future.successful(false))

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[VerificationService].toInstance(mockService))
        .build()

      running(application) {
        val result = route(application, FakeRequest(GET, endpointUrl)).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.routes.NoUnmatchedSubcontractorsController.onPageLoad().url
      }
    }

    "must redirect back to VerificationResults when there are no unmatched subcontractors" in {
      val mockService = mock[VerificationService]
      val response    = batchResponse(verification())
      val userAnswers = emptyUserAnswers
        .set(LastSubmittedVerificationBatchResponsePage, response)
        .success
        .value
        .set(CisIdQuery, "900063")
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[VerificationService].toInstance(mockService))
        .build()

      running(application) {
        val result = route(application, FakeRequest(GET, endpointUrl)).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.verify.routes.VerificationResultsController.onPageLoad().url
      }
    }

    "must redirect to SystemError when the live subcontractor check fails" in {
      val mockService = mock[VerificationService]
      val response    = batchResponse(verification(verificationNumber = None, verificationResourceRef = Some(10L)))
      val userAnswers = emptyUserAnswers
        .set(LastSubmittedVerificationBatchResponsePage, response)
        .success
        .value
        .set(CisIdQuery, "900063")
        .success
        .value

      when(mockService.anyUnmatchedResourceRefsStillPresent(eqTo("900063"), eqTo(response))(any[HeaderCarrier]))
        .thenReturn(Future.failed(new RuntimeException("boom")))

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[VerificationService].toInstance(mockService))
        .build()

      running(application) {
        val result = route(application, FakeRequest(GET, endpointUrl)).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.SystemErrorController.onPageLoad().url
      }
    }

    "must redirect to JourneyRecovery when session data is missing" in {

      val userAnswers =
        emptyUserAnswers
          .set(CisIdQuery, "900063")
          .success
          .value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val result = route(application, FakeRequest(GET, endpointUrl)).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
