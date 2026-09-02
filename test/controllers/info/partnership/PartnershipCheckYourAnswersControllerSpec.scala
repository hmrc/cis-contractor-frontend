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

package controllers.info.partnership

import base.SpecBase
import models.TypeOfSubcontractor
import models.TypeOfSubcontractor.Partnership
import models.address.{Address, Country}
import models.contact.ContactMethodOptions
import models.info.partnership.PartnershipAnswers
import org.jsoup.Jsoup
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.MessagesApi
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import queries.info.PartnershipAnswersQuery

class PartnershipCheckYourAnswersControllerSpec extends SpecBase with MockitoSugar {

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
    PartnershipAnswers(
      partnershipName = Some("Test Partnership"),
      addressYesNo = Some(true),
      address = Some(address),
      partnershipContactMethodsYesNo = Some(true),
      partnershipContactMethodOptions = Set(ContactMethodOptions.Email),
      email = Some("test@test.com"),
      phone = Some("02070000000"),
      mobile = Some("07123456789"),
      hasUtrYesNo = Some(true),
      utr = Some("11111111"),
      nominatedPartnerName = Some("Partnership nominated name"),
      nominatedPartnerUtrYesNo = Some(true),
      nominatedPartnerUtr = Some("11111111"),
      nominatedPartnerNinoYesNo = Some(true),
      nominatedPartnerNino = Some("AC123456"),
      nominatedPartnerCrnYesNo = Some(true),
      nominatedPartnerCrn = Some("12345678"),
      nominatedPartnerWorksReferenceYesNo = Some(true),
      nominatedPartnerWorksReference = Some("WRN-11"),
      verificationNumber = Some("VRN123456"),
      showVerificationDetails = false,
      subcontractorType = Partnership
    )

  private val journeyType = "insufficient"

  private lazy val insufficientRouteUrl = controllers.info.partnership.routes.PartnershipCheckYourAnswersController
    .onPageLoad(journeyType)
    .url

  "PartnershipCheckYourAnswersController" - {

    "must return OK and render the page with the correct summary list for an unverified partnership" in {

      val userAnswers =
        emptyUserAnswers
          .set(PartnershipAnswersQuery, answers)
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {

        val request =
          FakeRequest(GET, insufficientRouteUrl)

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
          msg("partnershipName.checkYourAnswersLabel")
        )

        page must include(
          msg("partnershipHasUtrYesNo.checkYourAnswersLabel")
        )

        page must include(
          msg("partnershipUniqueTaxpayerReference.checkYourAnswersLabel")
        )

        page must include(
          msg("partnershipNominatedPartnerName.checkYourAnswersLabel")
        )

        page must include(
          msg("partnershipNominatedPartnerNinoYesNo.checkYourAnswersLabel")
        )

        page must include(
          msg("partnershipNominatedPartnerNino.checkYourAnswersLabel")
        )

        page must include(
          msg("partnershipNominatedPartnerCrnYesNo.checkYourAnswersLabel")
        )

        page must include(
          msg("partnershipNominatedPartnerCrn.checkYourAnswersLabel")
        )

        page must include(
          msg("partnershipNominatedPartnerUtrYesNo.checkYourAnswersLabel")
        )

        page must include(
          msg("partnershipNominatedPartnerUtr.checkYourAnswersLabel")
        )

        page must include(
          msg("partnershipWorksReferenceNumberYesNo.checkYourAnswersLabel")
        )

        page must include(
          msg("partnershipWorksReferenceNumber.checkYourAnswersLabel")
        )

        page must include(
          msg("partnershipAddressYesNo.checkYourAnswersLabel")
        )

        page must include(
          msg("partnershipAddress.checkYourAnswersLabel")
        )

        page must include(
          msg("addPartnershipContactMethodsYesNo.checkYourAnswersLabel")
        )

        page must include(
          msg("partnershipContactMethodOptions.checkYourAnswersLabel")
        )

        page must include(
          msg("partnershipEmailAddress.checkYourAnswersLabel")
        )

        page must include(
          msg("partnershipPhoneNumber.checkYourAnswersLabel")
        )

        page must include(
          msg("partnershipMobileNumber.checkYourAnswersLabel")
        )

        page must include(
          msg("partnershipNominatedPartnerUtr.checkYourAnswersLabel")
        )

        page must not include (
          msg("amendCheckYourAnswers.verificationNumber.label")
        )

        page must include("Partnership")
        page must include("Test Partnership")
        page must include("Partnership nominated name")
        page must include("12345678")
        page must include("AC123456")
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

    "must render the correct summary for a verified partnership" in {

      val verifiedAnswers =
        answers.copy(
          showVerificationDetails = true
        )

      val userAnswers =
        emptyUserAnswers
          .set(PartnershipAnswersQuery, verifiedAnswers)
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {

        val request =
          FakeRequest(GET, insufficientRouteUrl)

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
          msg("partnershipName.checkYourAnswersLabel")
        )

        page must not include (
          msg("partnershipHasUtrYesNo.checkYourAnswersLabel")
        )

        page must not include (
          msg("partnershipNominatedPartnerUtrYesNo.checkYourAnswersLabel")
        )

        page must not include (
          msg("partnershipNominatedPartnerUtr.change.hidden")
        )

        page must include(
          msg("typeOfSubcontractor.checkYourAnswersLabel")
        )

        page must include("Partnership")

        page must include(
          msg("partnershipUniqueTaxpayerReference.checkYourAnswersLabel")
        )

        page must include("11111111")

        page must include(
          msg("partnershipNominatedPartnerName.checkYourAnswersLabel")
        )

        page must include("Partnership nominated name")

        page must include(
          msg("partnershipAddressYesNo.checkYourAnswersLabel")
        )

        page must include(
          msg("partnershipAddress.checkYourAnswersLabel")
        )

        page must include(
          msg("partnershipContactMethodOptions.checkYourAnswersLabel")
        )

        page must include(
          msg("partnershipEmailAddress.checkYourAnswersLabel")
        )

        page must include("test@test.com")

        page must include(
          msg("partnershipNominatedPartnerNinoYesNo.checkYourAnswersLabel")
        )

        page must include(
          msg("partnershipNominatedPartnerNino.checkYourAnswersLabel")
        )

        page must include("AC123456")

        page must include(
          msg("partnershipNominatedPartnerCrnYesNo.checkYourAnswersLabel")
        )

        page must include(
          msg("partnershipNominatedPartnerCrn.checkYourAnswersLabel")
        )

        page must include("12345678")

        page must include(
          msg("partnershipWorksReferenceNumberYesNo.checkYourAnswersLabel")
        )

        page must include(
          msg("partnershipWorksReferenceNumber.checkYourAnswersLabel")
        )

        page must include("WRN-11")
      }
    }

    "must not render the verification number row when the partnership is pending verification" in {

      val pendingAnswers =
        answers.copy(
          showVerificationDetails = true,
          verificationNumber = None
        )

      val userAnswers =
        emptyUserAnswers
          .set(PartnershipAnswersQuery, pendingAnswers)
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {

        val request =
          FakeRequest(GET, insufficientRouteUrl)

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
          msg("partnershipUniqueTaxpayerReference.verified.checkYourAnswersLabel")
        )
      }
    }

    "must redirect to Journey Recovery when ViewOnlyPartnershipAnswers are missing" in {

      val application =
        applicationBuilder(
          userAnswers = Some(emptyUserAnswers)
        ).build()

      running(application) {

        val request =
          FakeRequest(GET, insufficientRouteUrl)

        val result =
          route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.routes.JourneyRecoveryController
            .onPageLoad()
            .url
      }
    }

    "must render the correct back to message and Url when the journey is insufficient" in {

      val userAnswers =
        emptyUserAnswers
          .set(PartnershipAnswersQuery, answers)
          .success
          .value

      val application =
        applicationBuilder(
          userAnswers = Some(userAnswers)
        ).build()

      running(application) {

        val request =
          FakeRequest(GET, insufficientRouteUrl)

        val msg =
          application.injector
            .instanceOf[MessagesApi]
            .preferred(request)

        val result =
          route(application, request).value

        status(result) mustBe OK

        val doc  = Jsoup.parse(contentAsString(result))
        val link = doc.select("main a").first()

        link.text() mustBe msg("info.CheckYourAnswers.cannotVerifyAllSubcontractors")
        link.attr("href") mustBe
          controllers.verify.routes.ReviewInsufficientInfoSubcontractorsController
            .onPageLoad()
            .url

        link.text()       must not be msg("info.CheckYourAnswers.reviewUnmatchedSubcontractors")
        link.attr("href") must not be controllers.verify.routes.ReviewUnmatchedSubcontractorsRoutingController
          .onPageLoad()
          .url
      }
    }

    "must render the correct back to message and Url when the journey is unmatched" in {

      val journeyType = "unmatched"

      lazy val unmatchedRouteUrl = controllers.info.partnership.routes.PartnershipCheckYourAnswersController
        .onPageLoad(journeyType)
        .url

      val userAnswers =
        emptyUserAnswers
          .set(PartnershipAnswersQuery, answers)
          .success
          .value

      val application =
        applicationBuilder(
          userAnswers = Some(userAnswers)
        ).build()

      running(application) {

        val request =
          FakeRequest(GET, unmatchedRouteUrl)

        val msg =
          application.injector
            .instanceOf[MessagesApi]
            .preferred(request)

        val result =
          route(application, request).value

        status(result) mustBe OK

        val doc  = Jsoup.parse(contentAsString(result))
        val link = doc.select("main a").first()

        link.text()       must not be msg("info.CheckYourAnswers.cannotVerifyAllSubcontractors")
        link.attr("href") must not be controllers.verify.routes.ReviewInsufficientInfoSubcontractorsController
          .onPageLoad()
          .url

        link.text() mustBe msg("info.CheckYourAnswers.reviewUnmatchedSubcontractors")
        link.attr("href") mustBe controllers.verify.routes.ReviewUnmatchedSubcontractorsRoutingController
          .onPageLoad()
          .url
      }
    }

    "must redirect to Journey Recovery when when journeyType is not valid" in {

      val journeyType = "invalid"

      lazy val invalidRouteUrl = controllers.info.partnership.routes.PartnershipCheckYourAnswersController
        .onPageLoad(journeyType)
        .url

      val userAnswers =
        emptyUserAnswers
          .set(PartnershipAnswersQuery, answers)
          .success
          .value

      val application =
        applicationBuilder(
          userAnswers = Some(userAnswers)
        ).build()

      running(application) {

        val request =
          FakeRequest(GET, invalidRouteUrl)

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
