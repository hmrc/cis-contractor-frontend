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
import models.Subcontractor
import models.response.GetNewestVerificationBatchResponse
import pages.verify.NewestVerificationBatchResponsePage
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.ReviewInsufficientInfoService
import views.html.verify.ReviewInsufficientInfoSubcontractorsView

class ReviewInsufficientInfoSubcontractorsControllerSpec extends SpecBase {

  private val endpointUrl = "/subcontractor/verify/subcontractors-have-missing-information"

  private def mkSub(
    id: Long,
    firstName: Option[String] = None,
    surname: Option[String] = None,
    tradingName: Option[String] = None,
    subcontractorType: Option[String] = None,
    utr: Option[String] = None
  ): Subcontractor =
    Subcontractor(
      subcontractorId = id,
      firstName = firstName,
      secondName = None,
      surname = surname,
      tradingName = tradingName,
      partnershipTradingName = None,
      verified = None,
      verificationNumber = None,
      taxTreatment = None,
      verificationDate = None,
      lastMonthlyReturnDate = None,
      createDate = None,
      subcontractorType = subcontractorType,
      subbieResourceRef = None,
      utr = utr,
      partnerUtr = None,
      crn = None,
      nino = None
    )

  private def batchOf(subs: Subcontractor*): GetNewestVerificationBatchResponse =
    GetNewestVerificationBatchResponse(
      scheme = None,
      subcontractors = subs,
      verificationBatch = None,
      verifications = Nil,
      submission = None,
      monthlyReturn = None,
      monthlyReturnSubmission = None
    )

  private val missingSub =
    mkSub(
      id = 1L,
      surname = Some("Brody"),
      firstName = Some("Martin"),
      subcontractorType = Some("soletrader"),
      utr = None
    )

  private val readySub =
    mkSub(id = 2L, tradingName = Some("Acme Ltd"), subcontractorType = Some("company"), utr = Some("1234567890"))

  "ReviewInsufficientInfoSubcontractorsController" - {

    "must return OK and the correct view for a GET" in {

      val userAnswers =
        emptyUserAnswers
          .set(NewestVerificationBatchResponsePage, batchOf(missingSub, readySub))
          .success
          .value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, endpointUrl)

        val result = route(application, request).value

        val service   = application.injector.instanceOf[ReviewInsufficientInfoService]
        val view      = application.injector.instanceOf[ReviewInsufficientInfoSubcontractorsView]
        val appConfig = application.injector.instanceOf[config.FrontendAppConfig]
        val viewModel = service.buildViewModel(batchOf(missingSub, readySub))(messages(application))

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(viewModel)(request, appConfig, messages(application)).toString
      }
    }

    "must show the missing subcontractor, the ready subcontractor and 'None provided'" in {

      val userAnswers =
        emptyUserAnswers
          .set(NewestVerificationBatchResponsePage, batchOf(missingSub, readySub))
          .success
          .value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, endpointUrl)
        val result  = route(application, request).value

        status(result) mustEqual OK

        val body = contentAsString(result)
        body must include("Brody, Martin")
        body must include("Acme Ltd")
        body must include("1234567890")
        body must include(messages(application)("verify.reviewInsufficientInfo.utr.noneProvided"))
      }
    }

    "must redirect to Journey Recovery when NewestVerificationBatchResponsePage is missing" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, endpointUrl)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery when no user answers are found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, endpointUrl)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
