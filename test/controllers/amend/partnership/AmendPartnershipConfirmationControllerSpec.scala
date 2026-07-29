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

package controllers.amend.partnership

import base.SpecBase
import models.amend.partnership.OriginalPartnershipAnswers
import pages.add.partnership.PartnershipNamePage
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import queries.{CisIdQuery, OriginalPartnershipAnswersQuery}
import viewmodels.amend.partnership.PartnershipAmendConfirmationViewModel
import views.html.amend.AmendConfirmationView

class AmendPartnershipConfirmationControllerSpec extends SpecBase {
  private val cisId     = "123456789"
  private val partnershipName = "ABC Partnership"
  private val original  =
    OriginalPartnershipAnswers(
      partnershipName = Some(partnershipName),
      addressYesNo = None,
      address = None,
      partnershipContactMethodsYesNo = None,
      partnershipContactMethodOptions = Set.empty,
      email = None,
      phone = None,
      mobile = None,
      utrYesNo = None,
      utr = None,
      nominatedPartnerName = none,
      nominatedPartnerNinoYesNo = None,
      nominatedPartnerNino = None,
      nominatedPartnerCrnYesNo = None,
      nominatedPartnerCrn = None,
      nominatedPartnerUtrYesNo = None,
      nominatedPartnerUtr = None,
      nominatedPartnerWorksReferenceYesNo = None,
      nominatedPartnerWorksReference = None
    )

  private def userAnswersWithOriginal =
    emptyUserAnswers
      .set(OriginalPartnershipAnswersQuery, original)
      .success
      .value
      .set(CisIdQuery, cisId)
      .success
      .value
      .set(PartnershipNamePage, partnershipName)
      .success
      .value

  private lazy val confirmationRoute =
    controllers.amend.partnership.routes.AmendPartnershipConfirmationController.onPageLoad().url

  "AmendPartnershipConfirmationController" - {

    "must return OK and the correct view for a GET" in {
      val application = applicationBuilder(userAnswers = Some(userAnswersWithOriginal)).build()

      running(application) {

        val request = FakeRequest(GET, confirmationRoute)
        val result  = route(application, request).value

        val view = application.injector.instanceOf[AmendConfirmationView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(
            PartnershipAmendConfirmationViewModel.rows(
              original,
              userAnswersWithOriginal
            )(messages(application)),
            partnershipName,
            application.injector
              .instanceOf[config.FrontendAppConfig]
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
          .set(PartnershipNamePage, partnershipName)
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
          .set(OriginalPartnershipAnswersQuery, original)
          .success
          .value
          .set(PartnershipPartnershipNamePage, partnershippartnershipName)
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
