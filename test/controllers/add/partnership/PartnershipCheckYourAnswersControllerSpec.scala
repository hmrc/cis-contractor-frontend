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

package controllers.add.partnership

import base.SpecBase
import models.add.{InternationalAddress, TypeOfSubcontractor}
import models.contact.ContactOptions
import pages.add.TypeOfSubcontractorPage
import pages.add.partnership.*
import play.api.test.FakeRequest
import play.api.test.Helpers.*

class PartnershipCheckYourAnswersControllerSpec extends SpecBase {

  private val minUa = emptyUserAnswers
    .set(TypeOfSubcontractorPage, TypeOfSubcontractor.Partnership)
    .success
    .value
    .set(PartnershipNamePage, "Test Partnership")
    .success
    .value
    .set(PartnershipAddressYesNoPage, false)
    .success
    .value
    .set(PartnershipHasUtrYesNoPage, false)
    .success
    .value
    .set(PartnershipNominatedPartnerNamePage, "Test Nominated Partner")
    .success
    .value
    .set(PartnershipNominatedPartnerNinoYesNoPage, false)
    .success
    .value
    .set(PartnershipNominatedPartnerCrnYesNoPage, false)
    .success
    .value
    .set(PartnershipWorksReferenceNumberYesNoPage, false)
    .success
    .value

  "PartnershipCheckYourAnswers Controller" - {

    "must return OK and the correct view for a GET when partnership optionals are not present" in {

      val ua = minUa
        .set(PartnershipChooseContactDetailsPage, ContactOptions.Email)
        .success
        .value
        .set(PartnershipEmailAddressPage, "one@two.three")
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(ua)).build()

      running(application) {
        val request =
          FakeRequest(GET, controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad().url)
        val result  = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) must include("Test Partnership")
        contentAsString(result) must include("Test Nominated Partner")
      }
    }

    "must return OK and the correct view for a GET when all partnership optionals are present" in {

      val address = InternationalAddress(
        addressLine1 = "1 Test Street",
        addressLine2 = None,
        addressLine3 = "Test Town",
        addressLine4 = None,
        postalCode = "TE1 1ST",
        country = "GB"
      )

      val ua = minUa
        .set(PartnershipChooseContactDetailsPage, ContactOptions.Email)
        .success
        .value
        .set(PartnershipEmailAddressPage, "one@two.three")
        .success
        .value
        .set(PartnershipAddressYesNoPage, true)
        .success
        .value
        .set(PartnershipAddressPage, address)
        .success
        .value
        .set(PartnershipHasUtrYesNoPage, true)
        .success
        .value
        .set(PartnershipUniqueTaxpayerReferencePage, "1234567890")
        .success
        .value
        .set(PartnershipNominatedPartnerUtrPage, "9876543210")
        .success
        .value
        .set(PartnershipNominatedPartnerNinoYesNoPage, true)
        .success
        .value
        .set(PartnershipNominatedPartnerNinoPage, "AB123456C")
        .success
        .value
        .set(PartnershipNominatedPartnerCrnYesNoPage, true)
        .success
        .value
        .set(PartnershipNominatedPartnerCrnPage, "12345678")
        .success
        .value
        .set(PartnershipWorksReferenceNumberYesNoPage, true)
        .success
        .value
        .set(PartnershipWorksReferenceNumberPage, "WRN-001")
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(ua)).build()

      running(application) {
        val request =
          FakeRequest(GET, controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad().url)
        val result  = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) must include("Test Partnership")
        contentAsString(result) must include("Test Nominated Partner")
        contentAsString(result) must include("one@two.three")
        contentAsString(result) must include("1234567890")
        contentAsString(result) must include("AB123456C")
        contentAsString(result) must include("12345678")
        contentAsString(result) must include("WRN-001")
      }
    }

    "contact option validation" - {

      "must return OK when Phone is selected and a phone number is present" in {
        val ua = minUa
          .set(PartnershipChooseContactDetailsPage, ContactOptions.Phone)
          .success
          .value
          .set(PartnershipPhoneNumberPage, "01234567890")
          .success
          .value

        val application = applicationBuilder(userAnswers = Some(ua)).build()

        running(application) {
          val request =
            FakeRequest(GET, controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) must include("01234567890")
        }
      }

      "must return OK when Mobile is selected and a mobile number is present" in {
        val ua = minUa
          .set(PartnershipChooseContactDetailsPage, ContactOptions.Mobile)
          .success
          .value
          .set(PartnershipMobileNumberPage, "07123456789")
          .success
          .value

        val application = applicationBuilder(userAnswers = Some(ua)).build()

        running(application) {
          val request =
            FakeRequest(GET, controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) must include("07123456789")
        }
      }

      "must return OK when NoDetails is selected and no contact details are present" in {
        val ua = minUa
          .set(PartnershipChooseContactDetailsPage, ContactOptions.NoDetails)
          .success
          .value

        val application = applicationBuilder(userAnswers = Some(ua)).build()

        running(application) {
          val request =
            FakeRequest(GET, controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value

          status(result) mustEqual OK
        }
      }

      "must redirect to Journey Recovery when Email is selected but the email address is missing" in {
        val ua = minUa
          .set(PartnershipChooseContactDetailsPage, ContactOptions.Email)
          .success
          .value
        // PartnershipEmailAddressPage deliberately omitted

        val application = applicationBuilder(userAnswers = Some(ua)).build()

        running(application) {
          val request =
            FakeRequest(GET, controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value must include("/there-is-a-problem")
        }
      }

      "must redirect to Journey Recovery when Phone is selected but the phone number is missing" in {
        val ua = minUa
          .set(PartnershipChooseContactDetailsPage, ContactOptions.Phone)
          .success
          .value
        // PartnershipPhoneNumberPage deliberately omitted

        val application = applicationBuilder(userAnswers = Some(ua)).build()

        running(application) {
          val request =
            FakeRequest(GET, controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value must include("/there-is-a-problem")
        }
      }

      "must redirect to Journey Recovery when Mobile is selected but the mobile number is missing" in {
        val ua = minUa
          .set(PartnershipChooseContactDetailsPage, ContactOptions.Mobile)
          .success
          .value
        // PartnershipMobileNumberPage deliberately omitted

        val application = applicationBuilder(userAnswers = Some(ua)).build()

        running(application) {
          val request =
            FakeRequest(GET, controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value must include("/there-is-a-problem")
        }
      }

      "must redirect to Journey Recovery when NoDetails is selected but stale contact data remains in the session" in {
        val ua = minUa
          .set(PartnershipChooseContactDetailsPage, ContactOptions.NoDetails)
          .success
          .value
          .set(PartnershipEmailAddressPage, "stale@email.com")
          .success
          .value

        val application = applicationBuilder(userAnswers = Some(ua)).build()

        running(application) {
          val request =
            FakeRequest(GET, controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value must include("/there-is-a-problem")
        }
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(GET, controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad().url)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value must include("/there-is-a-problem")
      }
    }
  }
}
