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
import models.amend.trust.OriginalTrustAnswers
import pages.add.trust.TrustNamePage
import play.api.test.FakeRequest
import play.api.test.Helpers.{route, *}
import queries.{CisIdQuery, OriginalTrustAnswersQuery}
import viewmodels.amend.TrustAmendConfirmationViewModel
import views.html.amend.AmendConfirmationView

class AmendConfirmationControllerSpec extends SpecBase {

  private val original =
    OriginalTrustAnswers(
      trustName = Some("ABC Trust"),
      addressYesNo = None,
      address = None,
      trustContactMethodsYesNo = None,
      trustContactMethod = Set.empty,
      email = None,
      phone = None,
      mobile = None,
      utrYesNo = None,
      utr = None,
      worksReferenceYesNo = None,
      worksReference = None
    )

  private def userAnswersWithOriginal =
    emptyUserAnswers
      .set(OriginalTrustAnswersQuery, original)
      .success
      .value
      .set(CisIdQuery, "123456789")
      .success
      .value
      .set(TrustNamePage, "ABC Trust")
      .success
      .value

  private lazy val confirmationRoute =
    controllers.amend.routes.AmendConfirmationController.trustOnPageLoad().url

  "AmendConfirmationController" - {

    "must return OK and the correct view for a GET" in {

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithOriginal)).build()

      running(application) {

        val request = FakeRequest(GET, confirmationRoute)
        val result = route(application, request).value

        val view = application.injector.instanceOf[AmendConfirmationView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(
            TrustAmendConfirmationViewModel.rows(
              original,
              userAnswersWithOriginal
            )(messages(application)),
            "ABC Trust",
            application
              .injector
              .instanceOf[config.FrontendAppConfig]
              .manageYourSubcontractorsUrl("123456789")
          )(
            request,
            messages(application)
          ).toString
      }
    }

    "must redirect to Journey Recovery when the original answers are missing" in {

      val userAnswers =
        emptyUserAnswers
          .set(CisIdQuery, "123456789")
          .success
          .value
          .set(TrustNamePage, "ABC Trust")
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
          .set(OriginalTrustAnswersQuery, original)
          .success
          .value
          .set(TrustNamePage, "ABC Trust")
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
