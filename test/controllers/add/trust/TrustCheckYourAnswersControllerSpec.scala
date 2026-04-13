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

package controllers.add.trust

import base.SpecBase
import controllers.routes
import models.UserAnswers
import models.add.{InternationalAddress, TypeOfSubcontractor}
import models.contact.ContactOptions
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, verify, verifyNoMoreInteractions, when}
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.add.CheckYourAnswersSubmittedPage
import pages.add.TypeOfSubcontractorPage
import pages.add.trust.*
import play.api.inject.bind
import play.api.libs.json.*
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.SubcontractorService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class TrustCheckYourAnswersControllerSpec extends SpecBase {

  private val minUa =
    emptyUserAnswers
      .set(TypeOfSubcontractorPage, TypeOfSubcontractor.Trust)
      .success
      .value
      .set(TrustNamePage, "Test Trust")
      .success
      .value
      .set(TrustAddressYesNoPage, false)
      .success
      .value
      .set(TrustContactOptionsPage, ContactOptions.NoDetails)
      .success
      .value
      .set(TrustUtrYesNoPage, false)
      .success
      .value
      .set(TrustWorksReferenceYesNoPage, false)
      .success
      .value

  "TrustCheckYourAnswers Controller" - {

    "must return OK and the correct view for a GET when trust optionals are not present" in {

      val ua = minUa
        .set(TrustContactOptionsPage, ContactOptions.Email)
        .success
        .value
        .set(TrustEmailAddressPage, "one@two.three")
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.add.trust.routes.TrustCheckYourAnswersController.onPageLoad().url)
        val result  = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) must include("Test Trust")
      }
    }

    "must return OK and the correct view for a GET when all trust optionals are present" in {

      val address = InternationalAddress(
        addressLine1 = "1 Trust Street",
        addressLine2 = None,
        addressLine3 = "Trust Town",
        addressLine4 = None,
        postalCode = "TR1 1ST",
        country = "GB"
      )

      val ua = minUa
        .set(TrustContactOptionsPage, ContactOptions.Email)
        .success
        .value
        .set(TrustEmailAddressPage, "one@two.three")
        .success
        .value
        .set(TrustAddressYesNoPage, true)
        .success
        .value
        .set(TrustAddressPage, address)
        .success
        .value
        .set(TrustUtrYesNoPage, true)
        .success
        .value
        .set(TrustUtrPage, "1234567890")
        .success
        .value
        .set(TrustWorksReferenceYesNoPage, true)
        .success
        .value
        .set(TrustWorksReferencePage, "WRN-001")
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.add.trust.routes.TrustCheckYourAnswersController.onPageLoad().url)
        val result  = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) must include("Test Trust")
        contentAsString(result) must include("one@two.three")
        contentAsString(result) must include("1234567890")
        contentAsString(result) must include("WRN-001")
      }
    }

    "contact option validation" - {

      "must return OK when Phone is selected and a phone number is present" in {
        val ua = minUa
          .set(TrustContactOptionsPage, ContactOptions.Phone)
          .success
          .value
          .set(TrustPhoneNumberPage, "01234567890")
          .success
          .value

        val application = applicationBuilder(userAnswers = Some(ua)).build()

        running(application) {
          val request = FakeRequest(GET, controllers.add.trust.routes.TrustCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) must include("01234567890")
        }
      }

      "must return OK when Mobile is selected and a mobile number is present" in {
        val ua = minUa
          .set(TrustContactOptionsPage, ContactOptions.Mobile)
          .success
          .value
          .set(TrustMobileNumberPage, "07123456789")
          .success
          .value

        val application = applicationBuilder(userAnswers = Some(ua)).build()

        running(application) {
          val request = FakeRequest(GET, controllers.add.trust.routes.TrustCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) must include("07123456789")
        }
      }

      "must return OK when NoDetails is selected and no contact details are present" in {
        val ua = minUa

        val application = applicationBuilder(userAnswers = Some(ua)).build()

        running(application) {
          val request = FakeRequest(GET, controllers.add.trust.routes.TrustCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value

          status(result) mustEqual OK
        }
      }

      "must redirect to Journey Recovery when Email is selected but the email address is missing" in {
        val ua = minUa
          .set(TrustContactOptionsPage, ContactOptions.Email)
          .success
          .value
        // TrustEmailAddressPage deliberately omitted

        val application = applicationBuilder(userAnswers = Some(ua)).build()

        running(application) {
          val request = FakeRequest(GET, controllers.add.trust.routes.TrustCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value must include("/there-is-a-problem")
        }
      }

      "must redirect to Journey Recovery when Phone is selected but the phone number is missing" in {
        val ua = minUa
          .set(TrustContactOptionsPage, ContactOptions.Phone)
          .success
          .value
        // TrustPhoneNumberPage deliberately omitted

        val application = applicationBuilder(userAnswers = Some(ua)).build()

        running(application) {
          val request = FakeRequest(GET, controllers.add.trust.routes.TrustCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value must include("/there-is-a-problem")
        }
      }

      "must redirect to Journey Recovery when Mobile is selected but the mobile number is missing" in {
        val ua = minUa
          .set(TrustContactOptionsPage, ContactOptions.Mobile)
          .success
          .value
        // TrustMobileNumberPage deliberately omitted

        val application = applicationBuilder(userAnswers = Some(ua)).build()

        running(application) {
          val request = FakeRequest(GET, controllers.add.trust.routes.TrustCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value must include("/there-is-a-problem")
        }
      }

      "must redirect to Journey Recovery when NoDetails is selected but stale contact data remains in the session" in {
        val ua = minUa
          .set(TrustContactOptionsPage, ContactOptions.NoDetails)
          .success
          .value
          .set(TrustEmailAddressPage, "stale@email.com")
          .success
          .value

        val application = applicationBuilder(userAnswers = Some(ua)).build()

        running(application) {
          val request = FakeRequest(GET, controllers.add.trust.routes.TrustCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value must include("/there-is-a-problem")
        }
      }
    }

    "contact option change cleanup" - {

      "must return OK when switching from Email to Phone and stale email is cleaned up" in {
        val ua = minUa
          .set(TrustContactOptionsPage, ContactOptions.Email)
          .success
          .value
          .set(TrustEmailAddressPage, "old@email.com")
          .success
          .value
          .set(TrustContactOptionsPage, ContactOptions.Phone)
          .success
          .value // cleanup removes email
          .set(TrustPhoneNumberPage, "01234567890")
          .success
          .value

        val application = applicationBuilder(userAnswers = Some(ua)).build()

        running(application) {
          val request = FakeRequest(GET, controllers.add.trust.routes.TrustCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) must include("01234567890")
        }
      }

      "must return OK when switching from Phone to Mobile and stale phone number is cleaned up" in {
        val ua = minUa
          .set(TrustContactOptionsPage, ContactOptions.Phone)
          .success
          .value
          .set(TrustPhoneNumberPage, "01234567890")
          .success
          .value
          .set(TrustContactOptionsPage, ContactOptions.Mobile)
          .success
          .value // cleanup removes phone
          .set(TrustMobileNumberPage, "07123456789")
          .success
          .value

        val application = applicationBuilder(userAnswers = Some(ua)).build()

        running(application) {
          val request = FakeRequest(GET, controllers.add.trust.routes.TrustCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) must include("07123456789")
        }
      }

      "must return OK when switching from Email to NoDetails and stale email is cleaned up" in {
        val ua = minUa
          .set(TrustContactOptionsPage, ContactOptions.Email)
          .success
          .value
          .set(TrustEmailAddressPage, "old@email.com")
          .success
          .value
          .set(TrustContactOptionsPage, ContactOptions.NoDetails)
          .success
          .value // cleanup removes email

        val application = applicationBuilder(userAnswers = Some(ua)).build()

        running(application) {
          val request = FakeRequest(GET, controllers.add.trust.routes.TrustCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value

          status(result) mustEqual OK
        }
      }
    }

    "YesNo page cleanup" - {

      "must return OK when Address YesNo changes from Yes to No and stale address is cleaned up" in {
        val address = InternationalAddress(
          addressLine1 = "1 Trust Street",
          addressLine2 = None,
          addressLine3 = "Trust Town",
          addressLine4 = None,
          postalCode = "TR1 1ST",
          country = "GB"
        )

        val ua = minUa
          .set(TrustAddressYesNoPage, true)
          .success
          .value
          .set(TrustAddressPage, address)
          .success
          .value
          .set(TrustAddressYesNoPage, false)
          .success
          .value // cleanup fires, removes address

        val application = applicationBuilder(userAnswers = Some(ua)).build()

        running(application) {
          val request = FakeRequest(GET, controllers.add.trust.routes.TrustCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value

          status(result) mustEqual OK
        }
      }

      "must return OK when UtrYesNo changes from Yes to No and stale UTR value is cleaned up" in {
        val ua = minUa
          .set(TrustUtrYesNoPage, true)
          .success
          .value
          .set(TrustUtrPage, "1234567890")
          .success
          .value
          .set(TrustUtrYesNoPage, false)
          .success
          .value // cleanup fires, removes UTR

        val application = applicationBuilder(userAnswers = Some(ua)).build()

        running(application) {
          val request = FakeRequest(GET, controllers.add.trust.routes.TrustCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value

          status(result) mustEqual OK
        }
      }

      "must return OK when WorksReferenceYesNo changes from Yes to No and stale works reference is cleaned up" in {
        val ua = minUa
          .set(TrustWorksReferenceYesNoPage, true)
          .success
          .value
          .set(TrustWorksReferencePage, "WRN-001")
          .success
          .value
          .set(TrustWorksReferenceYesNoPage, false)
          .success
          .value // cleanup fires, removes works reference

        val application = applicationBuilder(userAnswers = Some(ua)).build()

        running(application) {
          val request = FakeRequest(GET, controllers.add.trust.routes.TrustCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value

          status(result) mustEqual OK
        }
      }
    }

    "must redirect to Journey Recovery for a GET if the subcontractor type is not Trust" in {
      val ua = minUa
        .set(TypeOfSubcontractorPage, TypeOfSubcontractor.Individualorsoletrader)
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.add.trust.routes.TrustCheckYourAnswersController.onPageLoad().url)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value must include("/there-is-a-problem")
      }
    }

    "must redirect to Journey Recovery for a GET when validation fails (incomplete / URL-hopped CYA)" in {

      val invalidUa =
        emptyUserAnswers
          .set(TypeOfSubcontractorPage, TypeOfSubcontractor.Trust)
          .success
          .value

      val application = applicationBuilder(userAnswers = Some(invalidUa)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.add.trust.routes.TrustCheckYourAnswersController.onPageLoad().url)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, controllers.add.trust.routes.TrustCheckYourAnswersController.onPageLoad().url)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    def withStaleValue[A](
      ua: UserAnswers,
      page: pages.QuestionPage[A],
      value: A
    )(implicit w: Writes[A]): UserAnswers = {
      val put = page.path.json.put(Json.toJson(value))

      ua.data.transform(put) match {
        case JsSuccess(updated: JsObject, _) => ua.copy(data = updated)
        case _                               => ua
      }
    }

    "must redirect to Journey Recovery when AddressYesNo is false but address value is still present (stale session)" in {

      val address = InternationalAddress(
        addressLine1 = "1 Trust Street",
        addressLine2 = None,
        addressLine3 = "Trust Town",
        addressLine4 = None,
        postalCode = "TR1 1ST",
        country = "GB"
      )

      val uaBase =
        minUa
          .set(TrustAddressYesNoPage, false)
          .success
          .value

      val ua = withStaleValue(uaBase, TrustAddressPage, address)

      val application = applicationBuilder(userAnswers = Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.add.trust.routes.TrustCheckYourAnswersController.onPageLoad().url)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value must include("/there-is-a-problem")
      }
    }

    "must redirect to Journey Recovery when UtrYesNo is false but UTR value is still present (stale session)" in {

      val uaBase =
        minUa
          .set(TrustUtrYesNoPage, false)
          .success
          .value

      val ua = withStaleValue(uaBase, TrustUtrPage, "1234567890")

      val application = applicationBuilder(userAnswers = Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.add.trust.routes.TrustCheckYourAnswersController.onPageLoad().url)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value must include("/there-is-a-problem")
      }
    }

    "must redirect to Journey Recovery when WorksReferenceYesNo is false but works reference is still present (stale session)" in {

      val uaBase =
        minUa
          .set(TrustWorksReferenceYesNoPage, false)
          .success
          .value

      val ua = withStaleValue(uaBase, TrustWorksReferencePage, "WRN-001")

      val application = applicationBuilder(userAnswers = Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.add.trust.routes.TrustCheckYourAnswersController.onPageLoad().url)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value must include("/there-is-a-problem")
      }
    }

    "must redirect to trust subcontractor added and set submitted flag when valid data is submitted" in {
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
        val request =
          FakeRequest(POST, controllers.add.trust.routes.TrustCheckYourAnswersController.onSubmit().url)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.add.routes.SubcontractorAddedController
          .trustSubcontractorAdded()
          .url
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
        val request =
          FakeRequest(POST, controllers.add.trust.routes.TrustCheckYourAnswersController.onSubmit().url)
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
          FakeRequest(POST, controllers.add.trust.routes.TrustCheckYourAnswersController.onSubmit().url)
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
        val request =
          FakeRequest(POST, controllers.add.trust.routes.TrustCheckYourAnswersController.onSubmit().url)
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
      val mockSessionRepository    = mock[SessionRepository]

      val invalidUa =
        emptyUserAnswers
          .set(TypeOfSubcontractorPage, TypeOfSubcontractor.Trust)
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
        val request =
          FakeRequest(POST, controllers.add.trust.routes.TrustCheckYourAnswersController.onSubmit().url)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }

      verify(mockSubcontractorService, never()).createAndUpdateSubcontractor(any[UserAnswers])(any[HeaderCarrier])
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }
  }
}
