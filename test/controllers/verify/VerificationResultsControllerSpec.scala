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
import models.response.GetNewestVerificationBatchResponse
import models.{Subcontractor, Verification}
import pages.verify.NewestVerificationBatchResponsePage
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import queries.CisIdQuery
import viewmodels.verify.VerificationResultsViewModel
import views.html.verify.VerificationResultsView

class VerificationResultsControllerSpec extends SpecBase {

  private val subcontractor = Subcontractor(
    subcontractorId = 1L,
    firstName = None,
    secondName = None,
    surname = None,
    tradingName = Some("Hooper and Associates"),
    partnershipTradingName = None,
    verified = Some("Y"),
    verificationNumber = Some("V0004528765"),
    taxTreatment = Some("net"),
    verificationDate = None,
    lastMonthlyReturnDate = None,
    createDate = None,
    subcontractorType = Some("company"),
    subbieResourceRef = None,
    utr = None,
    partnerUtr = None,
    crn = None,
    nino = None
  )

  private val verification = Verification(
    verificationId = 1L,
    matched = Some("matched"),
    verificationNumber = Some("V0004528765"),
    taxTreatment = Some("net"),
    verificationBatchId = Some(1L),
    subcontractorId = Some(1L)
  )

  private val batchResponse = GetNewestVerificationBatchResponse(
    scheme = None,
    subcontractors = Seq(subcontractor),
    verificationBatch = None,
    verifications = Seq(verification),
    submission = None,
    monthlyReturn = None,
    monthlyReturnSubmission = None
  )


  "VerificationResults Controller" - {

    "must return OK and the correct view for a GET" in {
      val cisId               = "1"
      val userAnswer          = emptyUserAnswers
        .set(NewestVerificationBatchResponsePage, batchResponse).success.value
        .set(CisIdQuery, cisId).success.value



      val manageSubcontractorsUrl =
        s"${applicationConfig.manageSubcontractorsUrl}/$cisId"
      val application             = applicationBuilder(userAnswers = Some(userAnswer)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.verify.routes.VerificationResultsController.onPageLoad().url)
        val result = route(application, request).value
        val view = application.injector.instanceOf[VerificationResultsView]
        val expectedViewModels = VerificationResultsViewModel.from(batchResponse)(messages(application))

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(expectedViewModels, manageSubcontractorsUrl)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery when CisId is missing" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.verify.routes.VerificationResultsController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
