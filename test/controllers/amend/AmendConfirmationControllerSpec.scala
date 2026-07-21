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

package controllers.amend

import base.SpecBase
import config.FrontendAppConfig
import models.amend.company.OriginalCompanyAnswers
import pages.add.company.CompanyNamePage
import play.api.test.FakeRequest
import play.api.test.Helpers.{route, *}
import queries.{CisIdQuery, OriginalCompanyAnswersQuery}
import viewmodels.amend.CompanyAmendConfirmationViewModel
import views.html.amend.AmendConfirmationView

class AmendConfirmationControllerSpec extends SpecBase {
  private val cisId       = "123456789"
  private val companyName = "ABC Company"

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
      worksReference = None
    )

  private def userAnswersWithOriginal =
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

  private lazy val confirmationRoute =
    controllers.amend.routes.AmendConfirmationController.companyOnPageLoad().url

  "AmendConfirmationController" - {

    "must return OK and the correct view for a GET" in {
      val application = applicationBuilder(userAnswers = Some(userAnswersWithOriginal)).build()

      running(application) {

        val request = FakeRequest(GET, confirmationRoute)
        val result  = route(application, request).value

        val view = application.injector.instanceOf[AmendConfirmationView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(
            CompanyAmendConfirmationViewModel.rows(
              original,
              userAnswersWithOriginal
            )(messages(application)),
            companyName,
            application.injector
              .instanceOf[FrontendAppConfig]
              .manageYourSubcontractorsUrl(cisId)
          )(
            request,
            messages(application)
          ).toString
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

      val application =
        applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {

        val request = FakeRequest(GET, confirmationRoute)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.routes.JourneyRecoveryController.onPageLoad().url
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

      val application =
        applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {

        val request = FakeRequest(GET, confirmationRoute)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
