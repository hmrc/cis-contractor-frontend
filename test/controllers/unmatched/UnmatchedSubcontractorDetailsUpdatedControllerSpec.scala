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
import config.FrontendAppConfig
import models.unmatched.{UnmatchedSubcontractorDetailsUpdated, UnmatchedSubcontractorDetailsUpdatedReturnTo, UnmatchedSubcontractorName, UnmatchedSubcontractorUpdate}
import pages.unmatched.UnmatchedSubcontractorDetailsUpdatedPage
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import queries.CisIdQuery
import viewmodels.unmatched.UnmatchedSubcontractorDetailsUpdatedViewModel
import views.html.unmatched.UnmatchedSubcontractorDetailsUpdatedView

class UnmatchedSubcontractorDetailsUpdatedControllerSpec extends SpecBase {

  private val cisId = "12345"

  private lazy val pageUrl =
    controllers.unmatched.routes.UnmatchedSubcontractorDetailsUpdatedController
      .onPageLoad()
      .url

  private def confirmationData(
    returnTo: String
  ): UnmatchedSubcontractorDetailsUpdated =
    UnmatchedSubcontractorDetailsUpdated(
      subcontractorName = UnmatchedSubcontractorName(
        firstName = Some("Martin"),
        lastName = Some("Brody")
      ),
      updates = Seq(
        UnmatchedSubcontractorUpdate(
          detail = "Add UTR?",
          previous = Some("No"),
          updated = Some("Yes")
        ),
        UnmatchedSubcontractorUpdate(
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
        UnmatchedSubcontractorDetailsUpdatedPage,
        confirmationData(returnTo)
      )
      .success
      .value
      .set(CisIdQuery, cisId)
      .success
      .value

  "UnmatchedSubcontractorDetailsUpdatedController" - {

    "must return OK and render the correct view for Your subcontractors journey" in {

      val userAnswers =
        userAnswersWithConfirmation(
          UnmatchedSubcontractorDetailsUpdatedReturnTo.YourSubcontractors
        )

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .build()

      running(application) {

        val request = FakeRequest(GET, pageUrl)

        val result = route(application, request).value

        val view =
          application.injector.instanceOf[UnmatchedSubcontractorDetailsUpdatedView]

        val appConfig =
          application.injector.instanceOf[FrontendAppConfig]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(
            rows = UnmatchedSubcontractorDetailsUpdatedViewModel.rows(
              confirmationData(
                UnmatchedSubcontractorDetailsUpdatedReturnTo.YourSubcontractors
              )
            )(messages(application)),
            subcontractorName = "Martin Brody",
            returnUrl = appConfig.manageYourSubcontractorsUrl(cisId),
            returnTextKey = "unmatched.unmatchedSubcontractorDetailsUpdated.yourSubcontractors",
            showBeforeYouGo = true
          )(request, messages(application)).toString
      }
    }

    "must redirect to Unauthorised ORG when CIS ID is missing & User is ORG role" in {

      val userAnswers =
        emptyUserAnswers
          .set(
            UnmatchedSubcontractorDetailsUpdatedPage,
            confirmationData(
              UnmatchedSubcontractorDetailsUpdatedReturnTo.YourSubcontractors
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
          controllers.routes.UnauthorisedOrganisationAffinityController.onPageLoad().url
      }
    }

    "must redirect to Unauthorised AGENT when CIS ID is missing & User is AGENT role" in {

      val userAnswers =
        emptyUserAnswers
          .set(
            UnmatchedSubcontractorDetailsUpdatedPage,
            confirmationData(
              UnmatchedSubcontractorDetailsUpdatedReturnTo.YourSubcontractors
            )
          )
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers), isAgent = true).build()

      running(application) {

        val request = FakeRequest(GET, pageUrl)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.routes.UnauthorisedAgentAffinityController.onPageLoad().url
      }
    }

    "must return OK and render the correct view for Cannot verify all subcontractors journey" in {

      val userAnswers =
        userAnswersWithConfirmation(
          UnmatchedSubcontractorDetailsUpdatedReturnTo.CannotVerifyAllSubcontractors
        )

      val application =
        applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {

        val request = FakeRequest(GET, pageUrl)

        val result = route(application, request).value

        val view =
          application.injector.instanceOf[UnmatchedSubcontractorDetailsUpdatedView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(
            rows = UnmatchedSubcontractorDetailsUpdatedViewModel.rows(
              confirmationData(
                UnmatchedSubcontractorDetailsUpdatedReturnTo.CannotVerifyAllSubcontractors
              )
            )(messages(application)),
            subcontractorName = "Martin Brody",
            returnUrl = "#",
            returnTextKey = "unmatched.unmatchedSubcontractorDetailsUpdated.cannotVerifyAllSubcontractors",
            showBeforeYouGo = false
          )(request, messages(application)).toString
      }
    }

    "must return OK and render the correct view for Review unmatched subcontractors journey" in {

      val userAnswers =
        userAnswersWithConfirmation(
          UnmatchedSubcontractorDetailsUpdatedReturnTo.ReviewUnmatchedSubcontractors
        )

      val application =
        applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {

        val request = FakeRequest(GET, pageUrl)

        val result = route(application, request).value

        val view =
          application.injector.instanceOf[UnmatchedSubcontractorDetailsUpdatedView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(
            rows = UnmatchedSubcontractorDetailsUpdatedViewModel.rows(
              confirmationData(
                UnmatchedSubcontractorDetailsUpdatedReturnTo.ReviewUnmatchedSubcontractors
              )
            )(messages(application)),
            subcontractorName = "Martin Brody",
            returnUrl = "#",
            returnTextKey = "unmatched.unmatchedSubcontractorDetailsUpdated.reviewUnmatchedSubcontractors",
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
