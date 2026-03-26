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

package controllers.add

import base.SpecBase
import controllers.routes
import models.add.{InternationalAddress, TypeOfSubcontractor}
import models.contact.ContactOptions
import models.{CheckMode, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, verify, verifyNoMoreInteractions, when}
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.add.{CheckYourAnswersSubmittedPage, *}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.SubcontractorService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class CheckYourAnswersControllerSpec extends SpecBase {

  "Check Your Answers Controller" - {

    val address = InternationalAddress(
      addressLine1 = "10 Downing Street",
      addressLine2 = Some("Westminster"),
      addressLine3 = "London",
      addressLine4 = Some("Greater London"),
      postalCode = "SW1A 2AA",
      country = "United Kingdom"
    )

    val minUa =
      emptyUserAnswers
        .set(TypeOfSubcontractorPage, TypeOfSubcontractor.Individualorsoletrader)
        .success
        .value
        .set(SubTradingNameYesNoPage, true)
        .success
        .value
        .set(TradingNameOfSubcontractorPage, "ABC Ltd")
        .success
        .value
        .set(SubAddressYesNoPage, true)
        .success
        .value
        .set(AddressOfSubcontractorPage, address)
        .success
        .value
        .set(IndividualChooseContactDetailsPage, ContactOptions.Phone)
        .success
        .value
        .set(IndividualEmailAddressPage, "abc@test.com")
        .success
        .value
        .set(IndividualPhoneNumberPage, "07123456789")
        .success
        .value
        .set(IndividualMobileNumberPage, "07987654321")
        .success
        .value
        .set(NationalInsuranceNumberYesNoPage, true)
        .success
        .value
        .set(SubNationalInsuranceNumberPage, "AB123456C")
        .success
        .value
        .set(UniqueTaxpayerReferenceYesNoPage, true)
        .success
        .value
        .set(SubcontractorsUniqueTaxpayerReferencePage, "1234567890")
        .success
        .value
        .set(WorksReferenceNumberYesNoPage, true)
        .success
        .value
        .set(WorksReferenceNumberPage, "WRN-001")
        .success
        .value

    "must display all questions and dependent rows when answers are provided" in {

      val application = applicationBuilder(userAnswers = Some(minUa)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.add.routes.CheckYourAnswersController.onPageLoad().url)
        val result  = route(application, request).value

        val content = contentAsString(result)
        status(result) mustEqual OK

        content must include("Type")
        content must include("Does subcontractor use a trading name?")
        content must include("Subcontractor trading name")
        content must include("Add subcontractor address?")
        content must include("Address")
        content must include("Method of contact")
        content must include("Phone number")
        content must include("Add UTR?")
        content must include("UTR")
        content must include("Add National Insurance number?")
        content must include("National Insurance number")
        content must include("Add works reference number?")
        content must include("Works reference number")

        content must include("ABC Ltd")
        content must include("AB123456C")
        content must include("1234567890")
        content must include("WRN-001")

        content must include(controllers.add.routes.SubTradingNameYesNoController.onPageLoad(CheckMode).url)
        content must include(controllers.add.routes.SubAddressYesNoController.onPageLoad(CheckMode).url)
        content must include(controllers.add.routes.IndividualChooseContactDetailsController.onPageLoad(CheckMode).url)
        content must include(controllers.add.routes.UniqueTaxpayerReferenceYesNoController.onPageLoad(CheckMode).url)
        content must include(controllers.add.routes.NationalInsuranceNumberYesNoController.onPageLoad(CheckMode).url)
        content must include(controllers.add.routes.WorksReferenceNumberYesNoController.onPageLoad(CheckMode).url)
      }
    }

    "must redirect to Journey Recovery on page load (GET) when validation fails (Left(error))" in {
      val invalidUserAnswers = emptyUserAnswers

      val application = applicationBuilder(userAnswers = Some(invalidUserAnswers)).build()

      running(application) {

        val request = FakeRequest(GET, controllers.add.routes.CheckYourAnswersController.onPageLoad().url)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect back to CYA and set submitted flag when valid data is submitted" in {
      val mockSubcontractorService = mock[SubcontractorService]
      val mockSessionRepository    = mock[SessionRepository]

      when(mockSubcontractorService.createAndUpdateSubcontractor(any[UserAnswers])(any[HeaderCarrier]))
        .thenReturn(Future.successful(()))

      when(mockSessionRepository.set(any[UserAnswers]))
        .thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(minUa))
          .overrides(
            bind[SubcontractorService].toInstance(mockSubcontractorService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request = FakeRequest(POST, controllers.add.routes.CheckYourAnswersController.onSubmit().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.add.routes.CheckYourAnswersController.onPageLoad().url
      }

      verify(mockSubcontractorService).createAndUpdateSubcontractor(any[UserAnswers])(any[HeaderCarrier])
      verify(mockSessionRepository).set(any[UserAnswers])
      verifyNoMoreInteractions(mockSubcontractorService)
    }

    "must redirect to Journey Recovery and not resubmit when already submitted flag is set" in {
      val submittedUa =
        minUa.set(CheckYourAnswersSubmittedPage, true).success.value

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
        val request = FakeRequest(POST, controllers.add.routes.CheckYourAnswersController.onSubmit().url)
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
        val request = FakeRequest(POST, controllers.add.routes.CheckYourAnswersController.onSubmit().url)
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
        applicationBuilder(userAnswers = Some(minUa))
          .overrides(
            bind[SubcontractorService].toInstance(mockSubcontractorService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request = FakeRequest(POST, controllers.add.routes.CheckYourAnswersController.onSubmit().url)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }

      verify(mockSubcontractorService).createAndUpdateSubcontractor(any[UserAnswers])(any[HeaderCarrier])
      verify(mockSessionRepository, never()).set(any[UserAnswers])
      verifyNoMoreInteractions(mockSubcontractorService)
    }

    "must redirect to Journey Recovery on submit when user answers are incomplete" in {

      val incompleteUserAnswers =
        emptyUserAnswers
          .set(TypeOfSubcontractorPage, TypeOfSubcontractor.Individualorsoletrader)
          .success
          .value
          .set(SubTradingNameYesNoPage, true)
          .success
          .value
          .set(TradingNameOfSubcontractorPage, "ABC Ltd")
          .success
          .value

      val application = applicationBuilder(userAnswers = Some(incompleteUserAnswers)).build()

      running(application) {
        val request = FakeRequest(POST, controllers.add.routes.CheckYourAnswersController.onSubmit().url)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "individual contact option validation" - {

      "must return OK when Email is selected and a email address is present" in {
        val ua = minUa
          .set(IndividualChooseContactDetailsPage, ContactOptions.Email)
          .success
          .value
          .set(IndividualEmailAddressPage, "abc@test.com")
          .success
          .value

        val application = applicationBuilder(userAnswers = Some(ua)).build()

        running(application) {
          val request =
            FakeRequest(GET, controllers.add.routes.CheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) must include("abc@test.com")
        }
      }

      "must return OK when Phone is selected and a phone number is present" in {
        val ua = minUa
          .set(IndividualChooseContactDetailsPage, ContactOptions.Phone)
          .success
          .value
          .set(IndividualPhoneNumberPage, "01234567890")
          .success
          .value

        val application = applicationBuilder(userAnswers = Some(ua)).build()

        running(application) {
          val request =
            FakeRequest(GET, controllers.add.routes.CheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) must include("01234567890")
        }
      }

      "must return OK when Mobile is selected and a mobile number is present" in {
        val ua = minUa
          .set(IndividualChooseContactDetailsPage, ContactOptions.Mobile)
          .success
          .value
          .set(IndividualMobileNumberPage, "07123456789")
          .success
          .value

        val application = applicationBuilder(userAnswers = Some(ua)).build()

        running(application) {
          val request =
            FakeRequest(GET, controllers.add.routes.CheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) must include("07123456789")
        }
      }

      "must return OK when NoDetails is selected and no contact details are present" in {
        val ua = minUa
          .set(IndividualChooseContactDetailsPage, ContactOptions.NoDetails)
          .success
          .value

        val application = applicationBuilder(userAnswers = Some(ua)).build()

        running(application) {
          val request =
            FakeRequest(GET, controllers.add.routes.CheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value

          status(result) mustEqual OK
        }
      }

      "must redirect to Journey Recovery when Email is selected but the email address is missing" in {
        val ua = minUa
          .set(IndividualChooseContactDetailsPage, ContactOptions.Email)
          .success
          .value
          .remove(IndividualEmailAddressPage)
          .success
          .value

        val application = applicationBuilder(userAnswers = Some(ua)).build()

        running(application) {
          val request =
            FakeRequest(GET, controllers.add.routes.CheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value must include("/there-is-a-problem")
        }
      }

      "must redirect to Journey Recovery when Phone is selected but the phone number is missing" in {
        val ua = minUa
          .set(IndividualChooseContactDetailsPage, ContactOptions.Phone)
          .success
          .value
          .remove(IndividualPhoneNumberPage)
          .success
          .value

        val application = applicationBuilder(userAnswers = Some(ua)).build()

        running(application) {
          val request =
            FakeRequest(GET, controllers.add.routes.CheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value must include("/there-is-a-problem")
        }
      }

      "must redirect to Journey Recovery when Mobile is selected but mobile number is missing" in {
        val ua = minUa
          .set(IndividualChooseContactDetailsPage, ContactOptions.Mobile)
          .success
          .value
          .remove(IndividualMobileNumberPage)
          .success
          .value

        val application = applicationBuilder(userAnswers = Some(ua)).build()

        running(application) {
          val request =
            FakeRequest(GET, controllers.add.routes.CheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value must include("/there-is-a-problem")
        }
      }

      "must redirect to Journey Recovery when NoDetails is selected but stale contact data remains in the session" in {
        val ua = minUa
          .set(IndividualChooseContactDetailsPage, ContactOptions.NoDetails)
          .success
          .value
          .set(IndividualEmailAddressPage, "stale@email.com")
          .success
          .value

        val application = applicationBuilder(userAnswers = Some(ua)).build()

        running(application) {
          val request =
            FakeRequest(GET, controllers.add.routes.CheckYourAnswersController.onPageLoad().url)
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
          .set(IndividualChooseContactDetailsPage, ContactOptions.Email)
          .success
          .value
          .set(IndividualEmailAddressPage, "old@email.com")
          .success
          .value
          .set(IndividualChooseContactDetailsPage, ContactOptions.Phone)
          .success
          .value // cleanup removes email
          .set(IndividualPhoneNumberPage, "01234567890")
          .success
          .value

        val application = applicationBuilder(userAnswers = Some(ua)).build()

        running(application) {
          val request =
            FakeRequest(GET, controllers.add.routes.CheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) must include("01234567890")
        }
      }

      "must return OK when switching from Phone to Mobile and stale phone number is cleaned up" in {
        val ua = minUa
          .set(IndividualChooseContactDetailsPage, ContactOptions.Phone)
          .success
          .value
          .set(IndividualPhoneNumberPage, "01234567890")
          .success
          .value
          .set(IndividualChooseContactDetailsPage, ContactOptions.Mobile)
          .success
          .value // cleanup removes phone
          .set(IndividualMobileNumberPage, "07123456789")
          .success
          .value

        val application = applicationBuilder(userAnswers = Some(ua)).build()

        running(application) {
          val request =
            FakeRequest(GET, controllers.add.routes.CheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) must include("07123456789")
        }
      }

      "must return OK when switching from Email to NoDetails and stale email is cleaned up" in {
        val ua = minUa
          .set(IndividualChooseContactDetailsPage, ContactOptions.Email)
          .success
          .value
          .set(IndividualEmailAddressPage, "old@email.com")
          .success
          .value
          .set(IndividualChooseContactDetailsPage, ContactOptions.NoDetails)
          .success
          .value // cleanup removes email

        val application = applicationBuilder(userAnswers = Some(ua)).build()

        running(application) {
          val request =
            FakeRequest(GET, controllers.add.routes.CheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value

          status(result) mustEqual OK
        }
      }
    }

  }
}
