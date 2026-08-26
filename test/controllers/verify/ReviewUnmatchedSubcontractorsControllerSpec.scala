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
import models.{SubcontractorLastVerification, VerificationLastVerification}
import pages.verify.LastSubmittedVerificationBatchResponsePage
import play.api.test.FakeRequest
import play.api.test.Helpers.*

class ReviewUnmatchedSubcontractorsControllerSpec extends SpecBase {

  private val endpointUrl = "/subcontractor/verify/review-unmatched-subcontractors"

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

  private def subcontractor(
    subcontractorId: Long = 22L,
    subbieResourceRef: Option[Long] = Some(10L)
  ): SubcontractorLastVerification =
    SubcontractorLastVerification(
      subcontractorId = subcontractorId,
      subbieResourceRef = subbieResourceRef,
      subcontractorType = Some("soletrader"),
      utr = Some("1111111111")
    )

  private def batchResponse(
    verifications: Seq[VerificationLastVerification],
    subcontractors: Seq[SubcontractorLastVerification]
  ): GetLastSubmittedVerificationBatchResponse =
    GetLastSubmittedVerificationBatchResponse(
      scheme = None,
      subcontractors = subcontractors,
      verifications = verifications,
      verificationBatch = None,
      submission = None
    )

  "ReviewUnmatchedSubcontractorsController" - {

    "AC2: must redirect to UnmatchedSubcontractors when unmatched resource ref matches a subcontractor" in {
      val response    = batchResponse(
        verifications = Seq(verification(verificationNumber = None, verificationResourceRef = Some(10L))),
        subcontractors = Seq(subcontractor(subbieResourceRef = Some(10L)))
      )
      val userAnswers = emptyUserAnswers
        .set(LastSubmittedVerificationBatchResponsePage, response)
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val result = route(application, FakeRequest(GET, endpointUrl)).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.UnmatchedSubcontractorsController.onPageLoad().url
      }
    }

    "AC3: must redirect to NoUnmatchedSubcontractors when verificationResourceRef does not match subbieResourceRef" in {
      val response    = batchResponse(
        verifications = Seq(verification(verificationNumber = None, verificationResourceRef = Some(12345L))),
        subcontractors = Seq(subcontractor(subbieResourceRef = Some(10L)))
      )
      val userAnswers = emptyUserAnswers
        .set(LastSubmittedVerificationBatchResponsePage, response)
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val result = route(application, FakeRequest(GET, endpointUrl)).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.routes.NoUnmatchedSubcontractorsController.onPageLoad().url
      }
    }

    "must redirect to NoUnmatchedSubcontractors when unmatched verification has no resource ref" in {
      val response    = batchResponse(
        verifications = Seq(verification(verificationNumber = None, verificationResourceRef = None)),
        subcontractors = Seq(subcontractor(subbieResourceRef = Some(10L)))
      )
      val userAnswers = emptyUserAnswers
        .set(LastSubmittedVerificationBatchResponsePage, response)
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val result = route(application, FakeRequest(GET, endpointUrl)).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.routes.NoUnmatchedSubcontractorsController.onPageLoad().url
      }
    }

    "must redirect back to VerificationResults when there are no unmatched subcontractors" in {
      val response    = batchResponse(
        verifications = Seq(verification()),
        subcontractors = Seq(subcontractor())
      )
      val userAnswers = emptyUserAnswers
        .set(LastSubmittedVerificationBatchResponsePage, response)
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val result = route(application, FakeRequest(GET, endpointUrl)).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.verify.routes.VerificationResultsController.onPageLoad().url
      }
    }

    "must redirect to JourneyRecovery when session data is missing" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val result = route(application, FakeRequest(GET, endpointUrl)).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
