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
import controllers.routes
import models.UserAnswers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, verify, verifyNoMoreInteractions, when}
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.add.CheckYourAnswersSubmittedPage
import play.api.inject.bind
import repositories.SessionRepository
import services.SubcontractorService
import uk.gov.hmrc.http.HeaderCarrier
import scala.concurrent.Future
import play.api.libs.json._

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
    .set(PartnershipNominatedPartnerUtrYesNoPage, false)
    .success
    .value
    .set(PartnershipWorksReferenceNumberYesNoPage, false)
    .success
    .value

  private val validUaForSubmit =
    minUa
      .set(PartnershipChooseContactDetailsPage, ContactOptions.NoDetails)
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
        .set(PartnershipNominatedPartnerUtrYesNoPage, true)
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

    "contact option change cleanup" - {

      // Simulates a user who previously entered a contact value, then changed their contact option via CYA.
      // The page cleanup should clear the stale value from the old option.

      "must return OK when switching from Email to Phone and stale email is cleaned up" in {
        val ua = minUa
          .set(PartnershipChooseContactDetailsPage, ContactOptions.Email)
          .success
          .value
          .set(PartnershipEmailAddressPage, "old@email.com")
          .success
          .value
          .set(PartnershipChooseContactDetailsPage, ContactOptions.Phone)
          .success
          .value // cleanup removes email
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

      "must return OK when switching from Phone to Mobile and stale phone number is cleaned up" in {
        val ua = minUa
          .set(PartnershipChooseContactDetailsPage, ContactOptions.Phone)
          .success
          .value
          .set(PartnershipPhoneNumberPage, "01234567890")
          .success
          .value
          .set(PartnershipChooseContactDetailsPage, ContactOptions.Mobile)
          .success
          .value // cleanup removes phone
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

      "must return OK when switching from Email to NoDetails and stale email is cleaned up" in {
        val ua = minUa
          .set(PartnershipChooseContactDetailsPage, ContactOptions.Email)
          .success
          .value
          .set(PartnershipEmailAddressPage, "old@email.com")
          .success
          .value
          .set(PartnershipChooseContactDetailsPage, ContactOptions.NoDetails)
          .success
          .value // cleanup removes email

        val application = applicationBuilder(userAnswers = Some(ua)).build()

        running(application) {
          val request =
            FakeRequest(GET, controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value

          status(result) mustEqual OK
        }
      }
    }

    "YesNo page cleanup" - {

      // Simulates a user who previously answered Yes and provided a value, then changed back to No via CYA.
      // The page cleanup should clear the stale value so CYA loads correctly.

      "must return OK when CRN YesNo changes from Yes to No and stale CRN value is cleaned up" in {
        val ua = minUa
          .set(PartnershipChooseContactDetailsPage, ContactOptions.Email)
          .success
          .value
          .set(PartnershipEmailAddressPage, "one@two.three")
          .success
          .value
          .set(PartnershipNominatedPartnerCrnYesNoPage, true)
          .success
          .value
          .set(PartnershipNominatedPartnerCrnPage, "12345678")
          .success
          .value
          .set(PartnershipNominatedPartnerCrnYesNoPage, false)
          .success
          .value // cleanup fires, removes CRN value

        val application = applicationBuilder(userAnswers = Some(ua)).build()

        running(application) {
          val request =
            FakeRequest(GET, controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value

          status(result) mustEqual OK
        }
      }

      "must return OK when Nino YesNo changes from Yes to No and stale Nino value is cleaned up" in {
        val ua = minUa
          .set(PartnershipChooseContactDetailsPage, ContactOptions.Email)
          .success
          .value
          .set(PartnershipEmailAddressPage, "one@two.three")
          .success
          .value
          .set(PartnershipNominatedPartnerNinoYesNoPage, true)
          .success
          .value
          .set(PartnershipNominatedPartnerNinoPage, "AB123456C")
          .success
          .value
          .set(PartnershipNominatedPartnerNinoYesNoPage, false)
          .success
          .value // cleanup fires, removes Nino value

        val application = applicationBuilder(userAnswers = Some(ua)).build()

        running(application) {
          val request =
            FakeRequest(GET, controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value

          status(result) mustEqual OK
        }
      }

      "must return OK when HasUtr YesNo changes from Yes to No and stale UTR values are cleaned up" in {
        val ua = minUa
          .set(PartnershipChooseContactDetailsPage, ContactOptions.Email)
          .success
          .value
          .set(PartnershipEmailAddressPage, "one@two.three")
          .success
          .value
          .set(PartnershipHasUtrYesNoPage, true)
          .success
          .value
          .set(PartnershipUniqueTaxpayerReferencePage, "1234567890")
          .success
          .value
          .set(PartnershipHasUtrYesNoPage, false)
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

      "must return OK when Address YesNo changes from Yes to No and stale address is cleaned up" in {
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
          .set(PartnershipAddressYesNoPage, false)
          .success
          .value // cleanup fires, removes address

        val application = applicationBuilder(userAnswers = Some(ua)).build()

        running(application) {
          val request =
            FakeRequest(GET, controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value

          status(result) mustEqual OK
        }
      }
    }

    "must redirect to Journey Recovery for a GET if the subcontractor type is not Partnership" in {
      val ua = minUa
        .set(TypeOfSubcontractorPage, TypeOfSubcontractor.Individualorsoletrader)
        .success
        .value
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

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value must include("/there-is-a-problem")
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

    def withStaleValue[A](
      ua: models.UserAnswers,
      page: pages.QuestionPage[A],
      value: A
    )(implicit w: Writes[A]): models.UserAnswers = {
      val put = page.path.json.put(Json.toJson(value))

      ua.data.transform(put) match {
        case JsSuccess(updated: JsObject, _) => ua.copy(data = updated)
        case _                               => ua
      }
    }

    "must redirect to Journey Recovery when AddressYesNo is false but address value is still present (stale session)" in {

      val address = InternationalAddress(
        addressLine1 = "1 Test Street",
        addressLine2 = None,
        addressLine3 = "Test Town",
        addressLine4 = None,
        postalCode = "TE1 1ST",
        country = "GB"
      )

      val uaBase =
        minUa
          .set(PartnershipChooseContactDetailsPage, ContactOptions.Email)
          .success
          .value
          .set(PartnershipEmailAddressPage, "one@two.three")
          .success
          .value
          .set(PartnershipAddressYesNoPage, false)
          .success
          .value

      val ua = withStaleValue(uaBase, PartnershipAddressPage, address)

      val application = applicationBuilder(userAnswers = Some(ua)).build()

      running(application) {
        val request =
          FakeRequest(GET, controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad().url)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value must include("/there-is-a-problem")
      }
    }

    "must redirect to Journey Recovery when HasUtrYesNo is false but partnership UTR value is still present (stale session)" in {

      val uaBase =
        minUa
          .set(PartnershipChooseContactDetailsPage, ContactOptions.Email)
          .success
          .value
          .set(PartnershipEmailAddressPage, "one@two.three")
          .success
          .value
          .set(PartnershipHasUtrYesNoPage, false)
          .success
          .value

      val ua = withStaleValue(uaBase, PartnershipUniqueTaxpayerReferencePage, "1234567890")

      val application = applicationBuilder(userAnswers = Some(ua)).build()

      running(application) {
        val request =
          FakeRequest(GET, controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad().url)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value must include("/there-is-a-problem")
      }
    }

    "must redirect to Journey Recovery when NominatedPartnerNinoYesNo is false but NINO value is still present (stale session)" in {

      val uaBase =
        minUa
          .set(PartnershipChooseContactDetailsPage, ContactOptions.Email)
          .success
          .value
          .set(PartnershipEmailAddressPage, "one@two.three")
          .success
          .value
          .set(PartnershipNominatedPartnerNinoYesNoPage, false)
          .success
          .value

      val ua = withStaleValue(uaBase, PartnershipNominatedPartnerNinoPage, "AB123456C")

      val application = applicationBuilder(userAnswers = Some(ua)).build()

      running(application) {
        val request =
          FakeRequest(GET, controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad().url)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value must include("/there-is-a-problem")
      }
    }

    "must redirect to Journey Recovery when NominatedPartnerCrnYesNo is false but CRN value is still present (stale session)" in {

      val uaBase =
        minUa
          .set(PartnershipChooseContactDetailsPage, ContactOptions.Email)
          .success
          .value
          .set(PartnershipEmailAddressPage, "one@two.three")
          .success
          .value
          .set(PartnershipNominatedPartnerCrnYesNoPage, false)
          .success
          .value

      val ua = withStaleValue(uaBase, PartnershipNominatedPartnerCrnPage, "12345678")

      val application = applicationBuilder(userAnswers = Some(ua)).build()

      running(application) {
        val request =
          FakeRequest(GET, controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad().url)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value must include("/there-is-a-problem")
      }
    }

    "must redirect to Journey Recovery when WorksReferenceNumberYesNo is false but WRN value is still present (stale session)" in {

      val uaBase =
        minUa
          .set(PartnershipChooseContactDetailsPage, ContactOptions.Email)
          .success
          .value
          .set(PartnershipEmailAddressPage, "one@two.three")
          .success
          .value
          .set(PartnershipWorksReferenceNumberYesNoPage, false)
          .success
          .value

      val ua = withStaleValue(uaBase, PartnershipWorksReferenceNumberPage, "WRN-001")

      val application = applicationBuilder(userAnswers = Some(ua)).build()

      running(application) {
        val request =
          FakeRequest(GET, controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad().url)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value must include("/there-is-a-problem")
      }
    }

    "must redirect back to Partnership CYA and set submitted flag when valid data is submitted" in {
      val mockSubcontractorService = mock[SubcontractorService]
      val mockSessionRepository    = mock[SessionRepository]

      when(mockSubcontractorService.createAndUpdateSubcontractor(any[UserAnswers])(any[HeaderCarrier]))
        .thenReturn(Future.successful(()))

      when(mockSessionRepository.set(any[UserAnswers]))
        .thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(validUaForSubmit))
          .overrides(
            bind[SubcontractorService].toInstance(mockSubcontractorService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onSubmit().url)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(
          result
        ).value mustEqual controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad().url
      }

      verify(mockSubcontractorService).createAndUpdateSubcontractor(any[UserAnswers])(any[HeaderCarrier])
      verify(mockSessionRepository).set(any[UserAnswers])
      verifyNoMoreInteractions(mockSubcontractorService)
    }
  }

  "must redirect to Journey Recovery and not resubmit when already submitted flag is set" in {
    val submittedUa =
      validUaForSubmit.set(CheckYourAnswersSubmittedPage, true).success.value

    val mockSubcontractorService = mock[SubcontractorService]
    val mockSessionRepository    = mock[SessionRepository]

    val application =
      applicationBuilder(userAnswers = Some(submittedUa))
        .overrides(
          bind[SubcontractorService].toInstance(mockSubcontractorService),
          bind[SessionRepository].toInstance(mockSessionRepository)
        )
        .build()

    running(application) {
      val request =
        FakeRequest(POST, controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onSubmit().url)
      val result  = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
    }

    verify(mockSubcontractorService, never()).createAndUpdateSubcontractor(any[UserAnswers])(any[HeaderCarrier])
    verify(mockSessionRepository, never()).set(any[UserAnswers])
    verifyNoMoreInteractions(mockSubcontractorService)
  }

  "must redirect to Journey Recovery on submit (POST) if no existing data is found" in {
    val application = applicationBuilder(userAnswers = None).build()

    running(application) {
      val request =
        FakeRequest(POST, controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onSubmit().url)
      val result  = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
    }
  }

  "must redirect to Journey Recovery when service call fails (recover block) and not set submitted flag" in {
    val mockSubcontractorService = mock[SubcontractorService]
    val mockSessionRepository    = mock[SessionRepository]

    when(mockSubcontractorService.createAndUpdateSubcontractor(any[UserAnswers])(any[HeaderCarrier]))
      .thenReturn(Future.failed(new RuntimeException("boom")))

    val application =
      applicationBuilder(userAnswers = Some(validUaForSubmit))
        .overrides(
          bind[SubcontractorService].toInstance(mockSubcontractorService),
          bind[SessionRepository].toInstance(mockSessionRepository)
        )
        .build()

    running(application) {
      val request =
        FakeRequest(POST, controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onSubmit().url)
      val result  = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
    }

    verify(mockSubcontractorService).createAndUpdateSubcontractor(any[UserAnswers])(any[HeaderCarrier])
    verify(mockSessionRepository, never()).set(any[UserAnswers])
    verifyNoMoreInteractions(mockSubcontractorService)
  }


  "must redirect to Journey Recovery on submit when validation fails (Left(error))" in {
    val mockSubcontractorService = mock[SubcontractorService]
    val mockSessionRepository = mock[SessionRepository]
    
    val invalidUa =
      emptyUserAnswers
        .set(TypeOfSubcontractorPage, TypeOfSubcontractor.Partnership)
        .success
        .value

    val application =
      applicationBuilder(userAnswers = Some(invalidUa))
        .overrides(
          bind[SubcontractorService].toInstance(mockSubcontractorService),
          bind[SessionRepository].toInstance(mockSessionRepository)
        )
        .build()

    running(application) {
      val request = FakeRequest(POST, controllers.add.routes.CheckYourAnswersController.onSubmit().url)
      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
    }

    verify(mockSubcontractorService, never()).createAndUpdateSubcontractor(any[UserAnswers])(any[HeaderCarrier])
    verify(mockSessionRepository, never()).set(any[UserAnswers])
  }

}
