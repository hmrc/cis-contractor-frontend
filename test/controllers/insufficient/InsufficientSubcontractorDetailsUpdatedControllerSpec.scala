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

package controllers.insufficient

import base.SpecBase
import config.FrontendAppConfig
import models.insufficient.{InsufficientSubcontractorDetailsUpdated, InsufficientSubcontractorDetailsUpdatedReturnTo, InsufficientSubcontractorName, InsufficientSubcontractorUpdate}
import pages.insufficient.InsufficientSubcontractorDetailsUpdatedPage
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import queries.CisIdQuery
import viewmodels.insufficient.InsufficientSubcontractorDetailsUpdatedViewModel
import views.html.insufficient.InsufficientSubcontractorDetailsUpdatedView

class InsufficientSubcontractorDetailsUpdatedControllerSpec extends SpecBase {

  private val cisId = "12345"

  private lazy val pageUrl =
    controllers.insufficient.routes.InsufficientSubcontractorDetailsUpdatedController
      .onPageLoad()
      .url

  private def confirmationData(
    returnTo: String
  ): InsufficientSubcontractorDetailsUpdated =
    InsufficientSubcontractorDetailsUpdated(
      subcontractorName = InsufficientSubcontractorName(
        firstName = Some("Martin"),
        lastName = Some("Brody")
      ),
      updates = Seq(
        InsufficientSubcontractorUpdate(
          detail = "Add UTR?",
          previous = Some("No"),
          updated = Some("Yes")
        ),
        InsufficientSubcontractorUpdate(
          detail = "UTR",
          previous = None,
          updated = Some("3992651526")
        )
      ),
      returnTo = returnTo
    )

  private def userAnswersWithConfirmation(
    returnTo: String
  ) =
    emptyUserAnswers
      .set(
        InsufficientSubcontractorDetailsUpdatedPage,
        confirmationData(returnTo)
      )
      .success
      .value
      .set(CisIdQuery, cisId)
      .success
      .value

  "InsufficientSubcontractorDetailsUpdatedController" - {

    "must return OK and render the correct view for Your subcontractors journey" in {

      val userAnswers =
        userAnswersWithConfirmation(
          InsufficientSubcontractorDetailsUpdatedReturnTo.YourSubcontractors
        )

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .configure(
            "urls.manageBaseUrl" -> "http://localhost:12345"
          )
          .build()

      running(application) {

        val request = FakeRequest(GET, pageUrl)

        val result = route(application, request).value

        val view =
          application.injector.instanceOf[InsufficientSubcontractorDetailsUpdatedView]

        val appConfig =
          application.injector.instanceOf[FrontendAppConfig]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(
            rows = InsufficientSubcontractorDetailsUpdatedViewModel.rows(
              confirmationData(
                InsufficientSubcontractorDetailsUpdatedReturnTo.YourSubcontractors
              )
            )(messages(application)),
            subcontractorName = "Martin Brody",
            returnUrl = appConfig.manageYourSubcontractorsUrl(cisId),
            returnTextKey = "insufficientSubcontractorDetailsUpdated.yourSubcontractors",
            showBeforeYouGo = true
          )(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery when CIS ID is missing" in {

      val userAnswers =
        emptyUserAnswers
          .set(
            InsufficientSubcontractorDetailsUpdatedPage,
            confirmationData(
              InsufficientSubcontractorDetailsUpdatedReturnTo.YourSubcontractors
            )
          )
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {

        val request = FakeRequest(GET, pageUrl)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must return OK and render the correct view for Cannot verify all subcontractors journey" in {

      val userAnswers =
        userAnswersWithConfirmation(
          InsufficientSubcontractorDetailsUpdatedReturnTo.CannotVerifyAllSubcontractors
        )

      val application =
        applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {

        val request = FakeRequest(GET, pageUrl)

        val result = route(application, request).value

        val view =
          application.injector.instanceOf[InsufficientSubcontractorDetailsUpdatedView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(
            rows = InsufficientSubcontractorDetailsUpdatedViewModel.rows(
              confirmationData(
                InsufficientSubcontractorDetailsUpdatedReturnTo.CannotVerifyAllSubcontractors
              )
            )(messages(application)),
            subcontractorName = "Martin Brody",
            returnUrl = "#",
            returnTextKey = "insufficientSubcontractorDetailsUpdated.cannotVerifyAllSubcontractors",
            showBeforeYouGo = false
          )(request, messages(application)).toString
      }
    }

    "must return OK and render the correct view for Review unmatched subcontractors journey" in {

      val userAnswers =
        userAnswersWithConfirmation(
          InsufficientSubcontractorDetailsUpdatedReturnTo.ReviewUnmatchedSubcontractors
        )

      val application =
        applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {

        val request = FakeRequest(GET, pageUrl)

        val result = route(application, request).value

        val view =
          application.injector.instanceOf[InsufficientSubcontractorDetailsUpdatedView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(
            rows = InsufficientSubcontractorDetailsUpdatedViewModel.rows(
              confirmationData(
                InsufficientSubcontractorDetailsUpdatedReturnTo.ReviewUnmatchedSubcontractors
              )
            )(messages(application)),
            subcontractorName = "Martin Brody",
            returnUrl = "#",
            returnTextKey = "insufficientSubcontractorDetailsUpdated.reviewUnmatchedSubcontractors",
            showBeforeYouGo = false
          )(request, messages(application)).toString
      }
    }

    "must default to Cannot verify all subcontractors when returnTo is unknown" in {

      val userAnswers =
        userAnswersWithConfirmation("unknownReturnTo")

      val application =
        applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {

        val request =
          FakeRequest(GET, pageUrl)

        val result =
          route(application, request).value

        status(result) mustEqual OK

        val page =
          contentAsString(result)

        page must include("Cannot verify all subcontractors")
        page must include("""href="#"""")
        page must not include "Before you go"
        page must not include "Take a short survey"
      }
    }

    "must redirect to Journey Recovery when confirmation data is missing" in {

      val userAnswers =
        emptyUserAnswers
          .set(CisIdQuery, cisId)
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {

        val request =
          FakeRequest(GET, pageUrl)

        val result =
          route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
