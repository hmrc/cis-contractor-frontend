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

package controllers.amend.company

import base.SpecBase
import models.UserAnswers
import models.amend.company.OriginalCompanyAnswers
import pages.add.company.CompanyNamePage
import pages.amend.AmendCheckYourAnswersSubmittedPage
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import queries.{CisIdQuery, OriginalCompanyAnswersQuery}
import viewmodels.amend.company.CompanyAmendConfirmationViewModel
import views.html.amend.AmendConfirmationView

class AmendCompanyConfirmationControllerSpec extends SpecBase {

  private val companyName = "Company Ltd"
  private val cisId       = "contractor-123"

  private val original =
    OriginalCompanyAnswers(
      companyName = Some(companyName),
      addressYesNo = None,
      address = None,
      companyContactMethodsYesNo = None,
      companyContactMethod = Set.empty,
      email = None,
      phone = None,
      mobile = None,
      utrYesNo = None,
      utr = None,
      crnYesNo = None,
      crn = None,
      worksReferenceYesNo = None,
      worksReference = None,
      verificationNumber = None
    )

  private def userAnswersWithOriginal: UserAnswers =
    emptyUserAnswers
      .set(OriginalCompanyAnswersQuery, original)
      .success
      .value
      .set(CisIdQuery, cisId)
      .success
      .value
      .set(CompanyNamePage, companyName)
      .success
      .value
      .set(AmendCheckYourAnswersSubmittedPage, true)
      .success
      .value

  private lazy val confirmationRoute =
    controllers.amend.company.routes.AmendCompanyConfirmationController
      .onPageLoad()
      .url

  "AmendCompanyConfirmationController" - {

    "must return OK and the correct view for a GET" in {

      val app =
        applicationBuilder(
          userAnswers = Some(userAnswersWithOriginal)
        ).build()

      running(app) {

        val request = FakeRequest(GET, confirmationRoute)
        val result  = route(app, request).value

        val view =
          app.injector.instanceOf[AmendConfirmationView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(
            CompanyAmendConfirmationViewModel.rows(
              original,
              userAnswersWithOriginal
            )(messages(app)),
            companyName
          )(request, messages(app)).toString
      }
    }

    "must redirect to Journey Recovery when accessed without prior CYA submission" in {

      val userAnswers =
        emptyUserAnswers
          .set(OriginalCompanyAnswersQuery, original)
          .success
          .value
          .set(CisIdQuery, cisId)
          .success
          .value
          .set(CompanyNamePage, companyName)
          .success
          .value

      val app =
        applicationBuilder(
          userAnswers = Some(userAnswers)
        ).build()

      running(app) {

        val request = FakeRequest(GET, confirmationRoute)
        val result  = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.routes.JourneyRecoveryController
            .onPageLoad()
            .url
      }
    }

    "must redirect to Journey Recovery when not submitted" in {

      val userAnswers =
        emptyUserAnswers
          .set(CisIdQuery, cisId)
          .success
          .value
          .set(CompanyNamePage, companyName)
          .success
          .value
          .set(AmendCheckYourAnswersSubmittedPage, false)
          .success
          .value

      val app =
        applicationBuilder(
          userAnswers = Some(userAnswers)
        ).build()

      running(app) {

        val request = FakeRequest(GET, confirmationRoute)
        val result  = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.routes.JourneyRecoveryController
            .onPageLoad()
            .url
      }
    }

    "must redirect to Journey Recovery when the original answers are missing" in {

      val userAnswers =
        emptyUserAnswers
          .set(CisIdQuery, cisId)
          .success
          .value
          .set(CompanyNamePage, companyName)
          .success
          .value
          .set(AmendCheckYourAnswersSubmittedPage, true)
          .success
          .value

      val app =
        applicationBuilder(
          userAnswers = Some(userAnswers)
        ).build()

      running(app) {

        val request = FakeRequest(GET, confirmationRoute)
        val result  = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.routes.JourneyRecoveryController
            .onPageLoad()
            .url
      }
    }

    "must redirect to Journey Recovery when the CIS id is missing" in {

      val userAnswers =
        emptyUserAnswers
          .set(OriginalCompanyAnswersQuery, original)
          .success
          .value
          .set(CompanyNamePage, companyName)
          .success
          .value
          .set(AmendCheckYourAnswersSubmittedPage, true)
          .success
          .value

      val app =
        applicationBuilder(
          userAnswers = Some(userAnswers)
        ).build()

      running(app) {

        val request = FakeRequest(GET, confirmationRoute)
        val result  = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.routes.JourneyRecoveryController
            .onPageLoad()
            .url
      }
    }
  }
}
