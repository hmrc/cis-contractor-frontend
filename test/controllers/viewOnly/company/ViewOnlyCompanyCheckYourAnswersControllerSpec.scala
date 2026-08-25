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

package controllers.viewOnly.company

import base.SpecBase
import models.TypeOfSubcontractor.Limitedcompany
import models.address.{Address, Country}
import models.contact.ContactMethodOptions
import models.viewOnly.company.ViewOnlyCompanyAnswers
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.MessagesApi
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import queries.ViewOnlyCompanyAnswersQuery

class ViewOnlyCompanyCheckYourAnswersControllerSpec extends SpecBase with MockitoSugar {

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
    ViewOnlyCompanyAnswers(
      companyName = Some("Test Company Ltd"),
      addressYesNo = Some(true),
      address = Some(address),
      companyContactMethodsYesNo = Some(true),
      companyContactMethod = Set(ContactMethodOptions.Email),
      email = Some("test@test.com"),
      phone = Some("02070000000"),
      mobile = Some("07123456789"),
      crnYesNo = Some(true),
      crn = Some("12345678"),
      utrYesNo = Some(true),
      utr = Some("11111111"),
      worksReferenceYesNo = Some(true),
      worksReference = Some("WRN-11"),
      verificationNumber = Some("VRN123456"),
      showVerificationDetails = false,
      subcontractorType = Limitedcompany
    )

  private lazy val routeUrl = controllers.viewOnly.company.routes.ViewOnlyCompanyCheckYourAnswersController
    .onPageLoad()
    .url

  "ViewOnlyCompanyCheckYourAnswersController" - {

    "must return OK and render the correct summary for an unverified company" in {

      val userAnswers =
        emptyUserAnswers
          .set(ViewOnlyCompanyAnswersQuery, answers)
          .success
          .value

      val application =
        applicationBuilder(
          userAnswers = Some(userAnswers)
        ).build()

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

        val page =
          contentAsString(result)

        page must include(
          msg("typeOfSubcontractor.checkYourAnswersLabel")
        )

        page must include(
          msg("companyName.checkYourAnswersLabel")
        )

        page must include(
          msg("companyAddressYesNo.checkYourAnswersLabel")
        )

        page must include(
          msg("companyAddress.checkYourAnswersLabel")
        )

        page must include(
          msg("addCompanyContactMethodsYesNo.checkYourAnswersLabel")
        )

        page must include(
          msg("companyContactMethodOptions.checkYourAnswersLabel")
        )

        page must include(
          msg("companyEmailAddress.checkYourAnswersLabel")
        )

        page must include(
          msg("companyPhoneNumber.checkYourAnswersLabel")
        )

        page must include(
          msg("companyMobileNumber.checkYourAnswersLabel")
        )

        page must include(
          msg("companyUtrYesNo.checkYourAnswersLabel")
        )

        page must include(
          msg("companyUtr.checkYourAnswersLabel")
        )

        page must include(
          msg("companyCrnYesNo.checkYourAnswersLabel")
        )

        page must include(
          msg("companyCrn.checkYourAnswersLabel")
        )

        page must include(
          msg("companyWorksReferenceYesNo.checkYourAnswersLabel")
        )

        page must include(
          msg("companyWorksReference.checkYourAnswersLabel")
        )

        page must not include (
          msg("amendCheckYourAnswers.verificationNumber.label")
        )

        page must include("Company")
        page must include("Test Company Ltd")
        page must include("12345678")
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

    "must render the correct summary for a verified company" in {

      val verifiedAnswers =
        answers.copy(
          showVerificationDetails = true
        )

      val userAnswers =
        emptyUserAnswers
          .set(ViewOnlyCompanyAnswersQuery, verifiedAnswers)
          .success
          .value

      val application =
        applicationBuilder(
          userAnswers = Some(userAnswers)
        ).build()

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

        val page =
          contentAsString(result)

        page must include(
          msg("amendCheckYourAnswers.verificationNumber.label")
        )

        page must include("VRN123456")

        page must include(
          msg("typeOfSubcontractor.checkYourAnswersLabel")
        )

        page must include("Company")

        page must not include (
          msg("companyName.checkYourAnswersLabel")
        )

        page must not include (
          msg("companyUtrYesNo.checkYourAnswersLabel")
        )

        page must include(
          msg("companyUtr.verified.checkYourAnswersLabel")
        )

        page must include("11111111")

        page must include(
          msg("companyAddressYesNo.checkYourAnswersLabel")
        )

        page must include(
          msg("companyAddress.checkYourAnswersLabel")
        )

        page must include(
          msg("addCompanyContactMethodsYesNo.checkYourAnswersLabel")
        )

        page must include(
          msg("companyContactMethodOptions.checkYourAnswersLabel")
        )

        page must include(
          msg("companyEmailAddress.checkYourAnswersLabel")
        )

        page must include("test@test.com")

        page must include(
          msg("companyCrnYesNo.checkYourAnswersLabel")
        )

        page must include(
          msg("companyCrn.checkYourAnswersLabel")
        )

        page must include("12345678")

        page must include(
          msg("companyWorksReferenceYesNo.checkYourAnswersLabel")
        )

        page must include(
          msg("companyWorksReference.checkYourAnswersLabel")
        )

        page must include("WRN-11")
      }
    }

    "must not render the verification number row when the company is pending verification" in {

      val pendingAnswers =
        answers.copy(
          showVerificationDetails = true,
          verificationNumber = None
        )

      val userAnswers =
        emptyUserAnswers
          .set(ViewOnlyCompanyAnswersQuery, pendingAnswers)
          .success
          .value

      val application =
        applicationBuilder(
          userAnswers = Some(userAnswers)
        ).build()

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

        val page =
          contentAsString(result)

        page must not include (
          msg("amendCheckYourAnswers.verificationNumber.label")
        )

        page must not include "VRN123456"

        page must include(
          msg("companyUtr.verified.checkYourAnswersLabel")
        )

        page must include("11111111")
      }
    }

    "must redirect to Journey Recovery when ViewOnlyCompanyAnswers are missing" in {

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
