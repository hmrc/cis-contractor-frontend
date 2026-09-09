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
import models.UserAnswers
import models.add.{IndividualNamesOptions, SubcontractorName}
import models.amend.OriginalIndividualAnswers
import pages.add.SubcontractorNamePage
import pages.amend.AmendCheckYourAnswersSubmittedPage
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import queries.{CisIdQuery, OriginalIndividualAnswersQuery}
import viewmodels.amend.IndividualAmendedViewModel
import views.html.amend.AmendConfirmationView

class AmendIndividualConfirmationControllerSpec extends SpecBase {

  private val cisId = "123456789"

  private val subcontractorName =
    SubcontractorName(
      firstName = "John",
      middleName = Some("A"),
      lastName = "Smith"
    )

  private val displayName = "John Smith"

  private val original =
    OriginalIndividualAnswers(
      individualNamesOptions = Set(IndividualNamesOptions.SubcontractorName),
      tradingName = None,
      subcontractorName = Some(subcontractorName),
      addressYesNo = None,
      address = None,
      individualContactMethodsYesNo = None,
      individualContactMethod = Set.empty,
      email = None,
      phone = None,
      mobile = None,
      utrYesNo = None,
      utr = None,
      ninoYesNo = None,
      nino = None,
      worksReferenceYesNo = None,
      worksReference = None,
      verificationNumber = None
    )

  private def userAnswersWithOriginal: UserAnswers =
    emptyUserAnswers
      .set(OriginalIndividualAnswersQuery, original)
      .success
      .value
      .set(CisIdQuery, cisId)
      .success
      .value
      .set(SubcontractorNamePage, subcontractorName)
      .success
      .value
      .set(AmendCheckYourAnswersSubmittedPage, true)
      .success
      .value

  private lazy val confirmationRoute =
    controllers.amend.routes.AmendIndividualConfirmationController
      .onPageLoad()
      .url

  "AmendIndividualConfirmationController" - {

    "must return OK and the correct view for a GET" in {

      val app = applicationBuilder(
        userAnswers = Some(userAnswersWithOriginal)
      ).build()

      running(app) {

        val request = FakeRequest(GET, confirmationRoute)
        val result  = route(app, request).value

        val view = app.injector.instanceOf[AmendConfirmationView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(
            IndividualAmendedViewModel.rows(
              original,
              userAnswersWithOriginal
            )(messages(app)),
            displayName
          )(request, messages(app)).toString
      }
    }

    "must redirect to Journey Recovery when the original answers are missing" in {

      val userAnswers =
        emptyUserAnswers
          .set(CisIdQuery, cisId)
          .success
          .value
          .set(SubcontractorNamePage, subcontractorName)
          .success
          .value
          .set(AmendCheckYourAnswersSubmittedPage, true)
          .success
          .value

      val app = applicationBuilder(
        userAnswers = Some(userAnswers)
      ).build()

      running(app) {

        val request = FakeRequest(GET, confirmationRoute)
        val result  = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery when the CIS id is missing" in {

      val userAnswers =
        emptyUserAnswers
          .set(OriginalIndividualAnswersQuery, original)
          .success
          .value
          .set(SubcontractorNamePage, subcontractorName)
          .success
          .value
          .set(AmendCheckYourAnswersSubmittedPage, true)
          .success
          .value

      val app = applicationBuilder(
        userAnswers = Some(userAnswers)
      ).build()

      running(app) {

        val request = FakeRequest(GET, confirmationRoute)
        val result  = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery when accessed without prior CYA submission" in {

      val userAnswers =
        emptyUserAnswers
          .set(OriginalIndividualAnswersQuery, original)
          .success
          .value
          .set(CisIdQuery, cisId)
          .success
          .value
          .set(SubcontractorNamePage, subcontractorName)
          .success
          .value

      val app = applicationBuilder(
        userAnswers = Some(userAnswers)
      ).build()

      running(app) {

        val request = FakeRequest(GET, confirmationRoute)
        val result  = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
