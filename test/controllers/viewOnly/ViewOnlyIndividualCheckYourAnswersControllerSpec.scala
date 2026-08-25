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

package controllers.viewOnly

import base.SpecBase
import models.TypeOfSubcontractor.Individualorsoletrader
import models.add.SubcontractorName
import models.address.{Address, Country}
import models.contact.ContactMethodOptions
import models.viewOnly.ViewOnlyIndividualAnswers
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.MessagesApi
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import queries.ViewOnlyIndividualAnswersQuery

class ViewOnlyIndividualCheckYourAnswersControllerSpec extends SpecBase with MockitoSugar {

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
    ViewOnlyIndividualAnswers(
      usesTradingName = Some(false),
      tradingName = None,
      subcontractorName = Some(
        SubcontractorName(
          firstName = "John",
          middleName = Some("Middle"),
          lastName = "Smith"
        )
      ),
      addressYesNo = Some(true),
      address = Some(address),
      individualContactMethodsYesNo = Some(true),
      individualContactMethod = Set(ContactMethodOptions.Email),
      email = Some("test@test.com"),
      phone = Some("02070000000"),
      mobile = Some("07123456789"),
      utrYesNo = Some(true),
      utr = Some("11111111"),
      ninoYesNo = Some(false),
      nino = None,
      worksReferenceYesNo = Some(true),
      worksReference = Some("WRN-11"),
      verificationNumber = Some("VRN123456"),
      showVerificationDetails = false,
      subcontractorType = Individualorsoletrader
    )

  private lazy val viewOnlyRoute =
    controllers.viewOnly.routes.ViewOnlyIndividualCheckYourAnswersController
      .onPageLoad()
      .url

  "ViewOnlyIndividualCheckYourAnswersController" - {

    "must render the correct summary for an unverified individual" in {

      val userAnswers =
        emptyUserAnswers
          .set(ViewOnlyIndividualAnswersQuery, answers)
          .success
          .value

      val application =
        applicationBuilder(
          userAnswers = Some(userAnswers)
        ).build()

      running(application) {

        val request =
          FakeRequest(GET, viewOnlyRoute)

        val msg =
          application.injector
            .instanceOf[MessagesApi]
            .preferred(request)

        val result =
          route(application, request).value

        status(result) mustBe OK

        val page =
          contentAsString(result)

        page must include(
          msg("typeOfSubcontractor.checkYourAnswersLabel")
        )

        page must include(
          msg("subTradingNameYesNo.checkYourAnswersLabel")
        )

        page must include(
          msg("subcontractorName.checkYourAnswersLabel")
        )

        page must include(
          msg("subAddressYesNo.checkYourAnswersLabel")
        )

        page must include(
          msg("addressOfSubcontractor.checkYourAnswersLabel")
        )

        page must include(
          msg("uniqueTaxpayerReferenceYesNo.checkYourAnswersLabel")
        )

        page must include(
          msg("subcontractorsUniqueTaxpayerReference.checkYourAnswersLabel")
        )

        page must include(
          msg("addIndividualContactMethodsYesNo.checkYourAnswersLabel")
        )

        page must include(
          msg("individualContactMethodOptions.checkYourAnswersLabel")
        )

        page must include(
          msg("individualEmailAddress.checkYourAnswersLabel")
        )

        page must include(
          msg("individualPhoneNumber.checkYourAnswersLabel")
        )

        page must include(
          msg("individualMobileNumber.checkYourAnswersLabel")
        )

        page must include(
          msg("nationalInsuranceNumberYesNo.checkYourAnswersLabel")
        )

        page must include(
          msg("worksReferenceNumberYesNo.checkYourAnswersLabel")
        )

        page must include(
          msg("worksReferenceNumber.checkYourAnswersLabel")
        )

        page must not include (
          msg("amendCheckYourAnswers.verificationNumber.label")
        )

        page must include("Individual")
        page must include("John Middle Smith")
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
      }
    }

    "must render the correct summary for a verified individual" in {

      val verifiedAnswers =
        answers.copy(
          showVerificationDetails = true
        )

      val userAnswers =
        emptyUserAnswers
          .set(ViewOnlyIndividualAnswersQuery, verifiedAnswers)
          .success
          .value

      val application =
        applicationBuilder(
          userAnswers = Some(userAnswers)
        ).build()

      running(application) {

        val request =
          FakeRequest(GET, viewOnlyRoute)

        val msg =
          application.injector
            .instanceOf[MessagesApi]
            .preferred(request)

        val result =
          route(application, request).value

        status(result) mustBe OK

        val page =
          contentAsString(result)

        page must include(
          msg("amendCheckYourAnswers.verificationNumber.label")
        )

        page must include("VRN123456")

        page must not include (
          msg("subTradingNameYesNo.checkYourAnswersLabel")
        )

        page must not include (
          msg("subcontractorName.checkYourAnswersLabel")
        )

        page must not include (
          msg("tradingNameOfSubcontractor.checkYourAnswersLabel")
        )

        page must not include (
          msg("uniqueTaxpayerReferenceYesNo.checkYourAnswersLabel")
        )

        page must include(
          msg("subcontractorsUniqueTaxpayerReference.checkYourAnswersLabel")
        )

        page must include(
          msg("typeOfSubcontractor.checkYourAnswersLabel")
        )

        page must include("Individual")

        page must include(
          msg("subAddressYesNo.checkYourAnswersLabel")
        )

        page must include(
          msg("addressOfSubcontractor.checkYourAnswersLabel")
        )

        page must include(
          msg("addIndividualContactMethodsYesNo.checkYourAnswersLabel")
        )

        page must include(
          msg("individualContactMethodOptions.checkYourAnswersLabel")
        )

        page must include(
          msg("individualEmailAddress.checkYourAnswersLabel")
        )

        page must include("test@test.com")

        page must include(
          msg("worksReferenceNumberYesNo.checkYourAnswersLabel")
        )

        page must include(
          msg("worksReferenceNumber.checkYourAnswersLabel")
        )
      }
    }

    "must redirect to Journey Recovery when ViewOnlyIndividualAnswers are missing" in {

      val application =
        applicationBuilder(
          userAnswers = Some(emptyUserAnswers)
        ).build()

      running(application) {

        val request =
          FakeRequest(GET, viewOnlyRoute)

        val result =
          route(application, request).value

        status(result) mustBe SEE_OTHER

        redirectLocation(result).value mustBe
          controllers.routes.JourneyRecoveryController
            .onPageLoad()
            .url
      }
    }
  }
}
