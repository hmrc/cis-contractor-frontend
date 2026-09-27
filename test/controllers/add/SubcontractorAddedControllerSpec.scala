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

package controllers.add

import base.SpecBase
import controllers.routes
import models.UserAnswers
import pages.add.{CheckYourAnswersSubmittedPage, TradingNameOfSubcontractorPage}
import pages.add.company.CompanyNamePage
import pages.add.partnership.PartnershipNamePage
import pages.add.trust.TrustNamePage
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import queries.CisIdQuery
import views.html.add.SubcontractorAddedView

class SubcontractorAddedControllerSpec extends SpecBase {

  private val subcontractorName = "Test subcontractor"
  private val cisId             = "12345"

  "SubcontractorAddedController.individualSubcontractorAdded" - {

    lazy val individualSubcontractorAddedRoute =
      controllers.add.routes.SubcontractorAddedController
        .individualSubcontractorAdded()
        .url

    "must return OK and the correct view for a GET when CheckYourAnswersSubmittedPage is true, and the subcontractor name and CIS ID are present" in {

      val userAnswers: UserAnswers =
        emptyUserAnswers
          .set(TradingNameOfSubcontractorPage, subcontractorName)
          .success
          .value
          .set(CheckYourAnswersSubmittedPage, true)
          .success
          .value
          .set(CisIdQuery, cisId)
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .build()

      running(application) {
        val request =
          FakeRequest(GET, individualSubcontractorAddedRoute)

        val view =
          application.injector.instanceOf[SubcontractorAddedView]

        val subcontractorTypeTitle =
          messages(application)("subcontractorAdded.individual")

        val result = route(application, request).value

        status(result) mustBe OK

        contentAsString(result) mustEqual view(
          subcontractorName,
          subcontractorTypeTitle,
          s"${applicationConfig.manageSubcontractorsUrl}/$cisId"
        )(
          request,
          messages(application)
        ).toString
      }
    }

    "must return OK when the confirmation page is refreshed" in {

      val userAnswers: UserAnswers =
        emptyUserAnswers
          .set(TradingNameOfSubcontractorPage, subcontractorName)
          .success
          .value
          .set(CheckYourAnswersSubmittedPage, true)
          .success
          .value
          .set(CisIdQuery, cisId)
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .build()

      running(application) {
        val firstRequest =
          FakeRequest(GET, individualSubcontractorAddedRoute)

        val firstResult =
          route(application, firstRequest).value

        status(firstResult) mustBe OK

        val refreshRequest =
          FakeRequest(GET, individualSubcontractorAddedRoute)

        val refreshResult =
          route(application, refreshRequest).value

        status(refreshResult) mustBe OK
      }
    }

    "must redirect to JourneyRecovery when CheckYourAnswersSubmittedPage is not in user answers" in {

      val userAnswers: UserAnswers =
        emptyUserAnswers
          .set(TradingNameOfSubcontractorPage, subcontractorName)
          .success
          .value
          .set(CisIdQuery, cisId)
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .build()

      running(application) {
        val request =
          FakeRequest(GET, individualSubcontractorAddedRoute)

        val result =
          route(application, request).value

        status(result) mustBe SEE_OTHER

        redirectLocation(result).value mustBe
          routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to JourneyRecovery when CheckYourAnswersSubmittedPage is false" in {

      val userAnswers: UserAnswers =
        emptyUserAnswers
          .set(TradingNameOfSubcontractorPage, subcontractorName)
          .success
          .value
          .set(CheckYourAnswersSubmittedPage, false)
          .success
          .value
          .set(CisIdQuery, cisId)
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .build()

      running(application) {
        val request =
          FakeRequest(GET, individualSubcontractorAddedRoute)

        val result =
          route(application, request).value

        status(result) mustBe SEE_OTHER

        redirectLocation(result).value mustBe
          routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to JourneyRecovery when the individual subcontractor name is missing" in {

      val userAnswers: UserAnswers =
        emptyUserAnswers
          .set(CheckYourAnswersSubmittedPage, true)
          .success
          .value
          .set(CisIdQuery, cisId)
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .build()

      running(application) {
        val request =
          FakeRequest(GET, individualSubcontractorAddedRoute)

        val result =
          route(application, request).value

        status(result) mustBe SEE_OTHER

        redirectLocation(result).value mustBe
          routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to JourneyRecovery when the CIS ID is missing" in {

      val userAnswers: UserAnswers =
        emptyUserAnswers
          .set(TradingNameOfSubcontractorPage, subcontractorName)
          .success
          .value
          .set(CheckYourAnswersSubmittedPage, true)
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .build()

      running(application) {
        val request =
          FakeRequest(GET, individualSubcontractorAddedRoute)

        val result =
          route(application, request).value

        status(result) mustBe SEE_OTHER

        redirectLocation(result).value mustBe
          routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }

  "SubcontractorAddedController.companySubcontractorAdded" - {

    lazy val companySubcontractorAddedRoute =
      controllers.add.routes.SubcontractorAddedController
        .companySubcontractorAdded()
        .url

    "must return OK and the correct view for a GET when CheckYourAnswersSubmittedPage is true, and the company name and CIS ID are present" in {

      val userAnswers: UserAnswers =
        emptyUserAnswers
          .set(CompanyNamePage, subcontractorName)
          .success
          .value
          .set(CheckYourAnswersSubmittedPage, true)
          .success
          .value
          .set(CisIdQuery, cisId)
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .build()

      running(application) {
        val request =
          FakeRequest(GET, companySubcontractorAddedRoute)

        val view =
          application.injector.instanceOf[SubcontractorAddedView]

        val subcontractorTypeTitle =
          messages(application)("subcontractorAdded.company")

        val result =
          route(application, request).value

        status(result) mustBe OK

        contentAsString(result) mustEqual view(
          subcontractorName,
          subcontractorTypeTitle,
          s"${applicationConfig.manageSubcontractorsUrl}/$cisId"
        )(
          request,
          messages(application)
        ).toString
      }
    }

    "must return OK when the confirmation page is refreshed" in {

      val userAnswers: UserAnswers =
        emptyUserAnswers
          .set(CompanyNamePage, subcontractorName)
          .success
          .value
          .set(CheckYourAnswersSubmittedPage, true)
          .success
          .value
          .set(CisIdQuery, cisId)
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .build()

      running(application) {
        val firstRequest =
          FakeRequest(GET, companySubcontractorAddedRoute)

        val firstResult =
          route(application, firstRequest).value

        status(firstResult) mustBe OK

        val refreshRequest =
          FakeRequest(GET, companySubcontractorAddedRoute)

        val refreshResult =
          route(application, refreshRequest).value

        status(refreshResult) mustBe OK
      }
    }

    "must redirect to JourneyRecovery when CheckYourAnswersSubmittedPage is not in user answers" in {

      val userAnswers: UserAnswers =
        emptyUserAnswers
          .set(CompanyNamePage, subcontractorName)
          .success
          .value
          .set(CisIdQuery, cisId)
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .build()

      running(application) {
        val request =
          FakeRequest(GET, companySubcontractorAddedRoute)

        val result =
          route(application, request).value

        status(result) mustBe SEE_OTHER

        redirectLocation(result).value mustBe
          routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to JourneyRecovery when CheckYourAnswersSubmittedPage is false" in {

      val userAnswers: UserAnswers =
        emptyUserAnswers
          .set(CompanyNamePage, subcontractorName)
          .success
          .value
          .set(CheckYourAnswersSubmittedPage, false)
          .success
          .value
          .set(CisIdQuery, cisId)
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .build()

      running(application) {
        val request =
          FakeRequest(GET, companySubcontractorAddedRoute)

        val result =
          route(application, request).value

        status(result) mustBe SEE_OTHER

        redirectLocation(result).value mustBe
          routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to JourneyRecovery when the company name is missing" in {

      val userAnswers: UserAnswers =
        emptyUserAnswers
          .set(CheckYourAnswersSubmittedPage, true)
          .success
          .value
          .set(CisIdQuery, cisId)
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .build()

      running(application) {
        val request =
          FakeRequest(GET, companySubcontractorAddedRoute)

        val result =
          route(application, request).value

        status(result) mustBe SEE_OTHER

        redirectLocation(result).value mustBe
          routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to JourneyRecovery when the CIS ID is missing" in {

      val userAnswers: UserAnswers =
        emptyUserAnswers
          .set(CompanyNamePage, subcontractorName)
          .success
          .value
          .set(CheckYourAnswersSubmittedPage, true)
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .build()

      running(application) {
        val request =
          FakeRequest(GET, companySubcontractorAddedRoute)

        val result =
          route(application, request).value

        status(result) mustBe SEE_OTHER

        redirectLocation(result).value mustBe
          routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }

  "SubcontractorAddedController.partnershipSubcontractorAdded" - {

    lazy val partnershipSubcontractorAddedRoute =
      controllers.add.routes.SubcontractorAddedController
        .partnershipSubcontractorAdded()
        .url

    "must return OK and the correct view for a GET when CheckYourAnswersSubmittedPage is true, and the partnership name and CIS ID are present" in {

      val userAnswers: UserAnswers =
        emptyUserAnswers
          .set(PartnershipNamePage, subcontractorName)
          .success
          .value
          .set(CheckYourAnswersSubmittedPage, true)
          .success
          .value
          .set(CisIdQuery, cisId)
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .build()

      running(application) {
        val request =
          FakeRequest(GET, partnershipSubcontractorAddedRoute)

        val view =
          application.injector.instanceOf[SubcontractorAddedView]

        val subcontractorTypeTitle =
          messages(application)("subcontractorAdded.partnership")

        val result =
          route(application, request).value

        status(result) mustBe OK

        contentAsString(result) mustEqual view(
          subcontractorName,
          subcontractorTypeTitle,
          s"${applicationConfig.manageSubcontractorsUrl}/$cisId"
        )(
          request,
          messages(application)
        ).toString
      }
    }

    "must return OK when the confirmation page is refreshed" in {

      val userAnswers: UserAnswers =
        emptyUserAnswers
          .set(PartnershipNamePage, subcontractorName)
          .success
          .value
          .set(CheckYourAnswersSubmittedPage, true)
          .success
          .value
          .set(CisIdQuery, cisId)
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .build()

      running(application) {
        val firstRequest =
          FakeRequest(GET, partnershipSubcontractorAddedRoute)

        val firstResult =
          route(application, firstRequest).value

        status(firstResult) mustBe OK

        val refreshRequest =
          FakeRequest(GET, partnershipSubcontractorAddedRoute)

        val refreshResult =
          route(application, refreshRequest).value

        status(refreshResult) mustBe OK
      }
    }

    "must redirect to JourneyRecovery when CheckYourAnswersSubmittedPage is not in user answers" in {

      val userAnswers: UserAnswers =
        emptyUserAnswers
          .set(PartnershipNamePage, subcontractorName)
          .success
          .value
          .set(CisIdQuery, cisId)
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .build()

      running(application) {
        val request =
          FakeRequest(GET, partnershipSubcontractorAddedRoute)

        val result =
          route(application, request).value

        status(result) mustBe SEE_OTHER

        redirectLocation(result).value mustBe
          routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to JourneyRecovery when CheckYourAnswersSubmittedPage is false" in {

      val userAnswers: UserAnswers =
        emptyUserAnswers
          .set(PartnershipNamePage, subcontractorName)
          .success
          .value
          .set(CheckYourAnswersSubmittedPage, false)
          .success
          .value
          .set(CisIdQuery, cisId)
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .build()

      running(application) {
        val request =
          FakeRequest(GET, partnershipSubcontractorAddedRoute)

        val result =
          route(application, request).value

        status(result) mustBe SEE_OTHER

        redirectLocation(result).value mustBe
          routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to JourneyRecovery when the partnership name is missing" in {

      val userAnswers: UserAnswers =
        emptyUserAnswers
          .set(CheckYourAnswersSubmittedPage, true)
          .success
          .value
          .set(CisIdQuery, cisId)
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .build()

      running(application) {
        val request =
          FakeRequest(GET, partnershipSubcontractorAddedRoute)

        val result =
          route(application, request).value

        status(result) mustBe SEE_OTHER

        redirectLocation(result).value mustBe
          routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to JourneyRecovery when the CIS ID is missing" in {

      val userAnswers: UserAnswers =
        emptyUserAnswers
          .set(PartnershipNamePage, subcontractorName)
          .success
          .value
          .set(CheckYourAnswersSubmittedPage, true)
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .build()

      running(application) {
        val request =
          FakeRequest(GET, partnershipSubcontractorAddedRoute)

        val result =
          route(application, request).value

        status(result) mustBe SEE_OTHER

        redirectLocation(result).value mustBe
          routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }

  "SubcontractorAddedController.trustSubcontractorAdded" - {

    lazy val trustSubcontractorAddedRoute =
      controllers.add.routes.SubcontractorAddedController
        .trustSubcontractorAdded()
        .url

    "must return OK and the correct view for a GET when CheckYourAnswersSubmittedPage is true, and the trust name and CIS ID are present" in {

      val userAnswers: UserAnswers =
        emptyUserAnswers
          .set(TrustNamePage, subcontractorName)
          .success
          .value
          .set(CheckYourAnswersSubmittedPage, true)
          .success
          .value
          .set(CisIdQuery, cisId)
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .build()

      running(application) {
        val request =
          FakeRequest(GET, trustSubcontractorAddedRoute)

        val view =
          application.injector.instanceOf[SubcontractorAddedView]

        val subcontractorTypeTitle =
          messages(application)("subcontractorAdded.trust")

        val result =
          route(application, request).value

        status(result) mustBe OK

        contentAsString(result) mustEqual view(
          subcontractorName,
          subcontractorTypeTitle,
          s"${applicationConfig.manageSubcontractorsUrl}/$cisId"
        )(
          request,
          messages(application)
        ).toString
      }
    }

    "must return OK when the confirmation page is refreshed" in {

      val userAnswers: UserAnswers =
        emptyUserAnswers
          .set(TrustNamePage, subcontractorName)
          .success
          .value
          .set(CheckYourAnswersSubmittedPage, true)
          .success
          .value
          .set(CisIdQuery, cisId)
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .build()

      running(application) {
        val firstRequest =
          FakeRequest(GET, trustSubcontractorAddedRoute)

        val firstResult =
          route(application, firstRequest).value

        status(firstResult) mustBe OK

        val refreshRequest =
          FakeRequest(GET, trustSubcontractorAddedRoute)

        val refreshResult =
          route(application, refreshRequest).value

        status(refreshResult) mustBe OK
      }
    }

    "must redirect to JourneyRecovery when CheckYourAnswersSubmittedPage is not in user answers" in {

      val userAnswers: UserAnswers =
        emptyUserAnswers
          .set(TrustNamePage, subcontractorName)
          .success
          .value
          .set(CisIdQuery, cisId)
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .build()

      running(application) {
        val request =
          FakeRequest(GET, trustSubcontractorAddedRoute)

        val result =
          route(application, request).value

        status(result) mustBe SEE_OTHER

        redirectLocation(result).value mustBe
          routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to JourneyRecovery when CheckYourAnswersSubmittedPage is false" in {

      val userAnswers: UserAnswers =
        emptyUserAnswers
          .set(TrustNamePage, subcontractorName)
          .success
          .value
          .set(CheckYourAnswersSubmittedPage, false)
          .success
          .value
          .set(CisIdQuery, cisId)
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .build()

      running(application) {
        val request =
          FakeRequest(GET, trustSubcontractorAddedRoute)

        val result =
          route(application, request).value

        status(result) mustBe SEE_OTHER

        redirectLocation(result).value mustBe
          routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to JourneyRecovery when the trust name is missing" in {

      val userAnswers: UserAnswers =
        emptyUserAnswers
          .set(CheckYourAnswersSubmittedPage, true)
          .success
          .value
          .set(CisIdQuery, cisId)
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .build()

      running(application) {
        val request =
          FakeRequest(GET, trustSubcontractorAddedRoute)

        val result =
          route(application, request).value

        status(result) mustBe SEE_OTHER

        redirectLocation(result).value mustBe
          routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to JourneyRecovery when the CIS ID is missing" in {

      val userAnswers: UserAnswers =
        emptyUserAnswers
          .set(TrustNamePage, subcontractorName)
          .success
          .value
          .set(CheckYourAnswersSubmittedPage, true)
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .build()

      running(application) {
        val request =
          FakeRequest(GET, trustSubcontractorAddedRoute)

        val result =
          route(application, request).value

        status(result) mustBe SEE_OTHER

        redirectLocation(result).value mustBe
          routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
