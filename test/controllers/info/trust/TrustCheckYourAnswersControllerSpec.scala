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

package controllers.info.trust

import base.SpecBase
import models.TypeOfSubcontractor.Trust
import models.address.{Address, Country}
import models.contact.ContactMethodOptions
import models.info.trust.TrustAnswers
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.MessagesApi
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import queries.info.TrustAnswersQuery

class TrustCheckYourAnswersControllerSpec extends SpecBase with MockitoSugar {

  private val address =
    Address(
      addressLine1 = "12 Harbor View Road",
      addressLine2 = Some("Amity Island"),
      addressLine3 = Some("Bodmin"),
      addressLine4 = Some("Cornwall"),
      postcode = Some("PL31 2HL"),
      country = Some(
        Country(
          code = None,
          name = Some("England")
        )
      )
    )

  private val answers =
    TrustAnswers(
      trustName = Some("Test Trust"),
      addressYesNo = Some(true),
      address = Some(address),
      trustContactMethodsYesNo = Some(true),
      trustContactMethod = Set(ContactMethodOptions.Email),
      email = Some("test@test.com"),
      phone = Some("02070000000"),
      mobile = Some("07123456789"),
      utrYesNo = Some(true),
      utr = Some("11111111"),
      worksReferenceYesNo = Some(true),
      worksReference = Some("WRN-11"),
      verificationNumber = Some("VRN123456"),
      showVerificationDetails = false,
      subcontractorType = Trust
    )

  private lazy val routeUrl = controllers.info.trust.routes.TrustCheckYourAnswersController
    .onPageLoad()
    .url

  "TrustCheckYourAnswersController" - {

    "must return OK and render the page with the correct summary list for an unverified trust" in {

      val userAnswers =
        emptyUserAnswers
          .set(TrustAnswersQuery, answers)
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {

        val request =
          FakeRequest(GET, routeUrl)

        val msg =
          application.injector
            .instanceOf[MessagesApi]
            .preferred(request)

        val result =
          route(application, request).value

        status(result) mustEqual OK

        val page = contentAsString(result)

        page must include(
          msg("typeOfSubcontractor.checkYourAnswersLabel")
        )

        page must include(
          msg("trustName.checkYourAnswersLabel")
        )

        page must include(
          msg("trustUtrYesNo.checkYourAnswersLabel")
        )

        page must include(
          msg("trustUtr.checkYourAnswersLabel")
        )

        page must include(
          msg("trustWorksReferenceYesNo.checkYourAnswersLabel")
        )

        page must include(
          msg("trustWorksReference.checkYourAnswersLabel")
        )

        page must include(
          msg("trustAddressYesNo.checkYourAnswersLabel")
        )

        page must include(
          msg("trustAddress.checkYourAnswersLabel")
        )

        page must include(
          msg("addTrustContactMethodsYesNo.checkYourAnswersLabel")
        )

        page must include(
          msg("trustContactMethodOptions.checkYourAnswersLabel")
        )

        page must include(
          msg("trustEmailAddress.checkYourAnswersLabel")
        )

        page must include(
          msg("trustPhoneNumber.checkYourAnswersLabel")
        )

        page must include(
          msg("trustMobileNumber.checkYourAnswersLabel")
        )

        page must not include (
          msg("amendCheckYourAnswers.verificationNumber.label")
        )

        page must include("Trust")
        page must include("Test Trust")
        page must include("11111111")
        page must include("WRN-11")
        page must include("test@test.com")
        page must include("02070000000")
        page must include("07123456789")

        page must include("12 Harbor View Road")
        page must include("Amity Island")
        page must include("Bodmin")
        page must include("Cornwall")
        page must include("PL31 2HL")
        page must include("England")

        page must not include "VRN123456"
      }
    }

    "must render the correct summary for a verified trust" in {

      val verifiedAnswers =
        answers.copy(
          showVerificationDetails = true
        )

      val userAnswers =
        emptyUserAnswers
          .set(TrustAnswersQuery, verifiedAnswers)
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {

        val request =
          FakeRequest(GET, routeUrl)

        val msg =
          application.injector
            .instanceOf[MessagesApi]
            .preferred(request)

        val result =
          route(application, request).value

        status(result) mustEqual OK

        val page = contentAsString(result)

        page must include(
          msg("amendCheckYourAnswers.verificationNumber.label")
        )

        page must include("VRN123456")

        page must not include (
          msg("trustName.checkYourAnswersLabel")
        )

        page must not include (
          msg("trustUtrYesNo.checkYourAnswersLabel")
        )

        page must include(
          msg("trustUtr.verified.checkYourAnswersLabel")
        )

        page must include("11111111")

        page must include(
          msg("typeOfSubcontractor.checkYourAnswersLabel")
        )

        page must include("Trust")

        page must include(
          msg("trustAddressYesNo.checkYourAnswersLabel")
        )

        page must include(
          msg("trustAddress.checkYourAnswersLabel")
        )

        page must include(
          msg("trustContactMethodOptions.checkYourAnswersLabel")
        )

        page must include(
          msg("trustEmailAddress.checkYourAnswersLabel")
        )

        page must include("test@test.com")

        page must include(
          msg("trustPhoneNumber.checkYourAnswersLabel")
        )

        page must include(
          msg("trustMobileNumber.checkYourAnswersLabel")
        )

        page must include(
          msg("trustWorksReferenceYesNo.checkYourAnswersLabel")
        )

        page must include(
          msg("trustWorksReference.checkYourAnswersLabel")
        )

        page must include("WRN-11")
      }
    }

    "must not render the verification number row when the trust is pending verification" in {

      val pendingAnswers =
        answers.copy(
          showVerificationDetails = true,
          verificationNumber = None
        )

      val userAnswers =
        emptyUserAnswers
          .set(TrustAnswersQuery, pendingAnswers)
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {

        val request =
          FakeRequest(GET, routeUrl)

        val msg =
          application.injector
            .instanceOf[MessagesApi]
            .preferred(request)

        val result =
          route(application, request).value

        status(result) mustEqual OK

        val page = contentAsString(result)

        page must not include (
          msg("amendCheckYourAnswers.verificationNumber.label")
        )

        page must not include "VRN123456"

        page must include(
          msg("trustUtr.verified.checkYourAnswersLabel")
        )
      }
    }

    "must redirect to Journey Recovery when ViewOnlyTrustAnswers are missing" in {

      val application =
        applicationBuilder(
          userAnswers = Some(emptyUserAnswers)
        ).build()

      running(application) {

        val request =
          FakeRequest(GET, routeUrl)

        val result =
          route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.routes.JourneyRecoveryController
            .onPageLoad()
            .url
      }
    }
  }
}
