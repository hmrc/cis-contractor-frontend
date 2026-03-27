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

package controllers.add.company

import base.SpecBase
import controllers.routes
import models.UserAnswers
import models.add.{InternationalAddress, TypeOfSubcontractor}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, verify, verifyNoMoreInteractions, when}
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.add.CheckYourAnswersSubmittedPage
import pages.add.TypeOfSubcontractorPage
import pages.add.company.*
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.SubcontractorService
import uk.gov.hmrc.http.HeaderCarrier
import models.contact.ContactOptions

import scala.concurrent.Future

class CompanyCheckYourAnswersControllerSpec extends SpecBase {

  import play.api.libs.json.*

  private def withStaleValue[A](
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

  private val minUa: UserAnswers =
    emptyUserAnswers
      .set(TypeOfSubcontractorPage, TypeOfSubcontractor.Limitedcompany)
      .success
      .value
      .set(CompanyNamePage, "Test Company Ltd")
      .success
      .value
      .set(CompanyAddressYesNoPage, false)
      .success
      .value
      .set(CompanyUtrYesNoPage, false)
      .success
      .value
      .set(CompanyCrnYesNoPage, false)
      .success
      .value
      .set(CompanyWorksReferenceYesNoPage, false)
      .success
      .value

  private val validUaForSubmit: UserAnswers =
    minUa
      .set(CompanyContactOptionsPage, ContactOptions.NoDetails)
      .success
      .value

  "CompanyCheckYourAnswers Controller" - {

    "must return OK and the correct view for a GET when optionals are not present" in {
      val ua =
        minUa
          .set(CompanyContactOptionsPage, ContactOptions.NoDetails)
          .success
          .value

      val application = applicationBuilder(userAnswers = Some(ua)).build()

      running(application) {
        val request =
          FakeRequest(GET, controllers.add.company.routes.CompanyCheckYourAnswersController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) must include("Test Company Ltd")
      }
    }

    "must return OK and the correct view for a GET when all optionals are present" in {
      val address = InternationalAddress(
        addressLine1 = "1 Test Street",
        addressLine2 = None,
        addressLine3 = "Test Town",
        addressLine4 = None,
        postalCode = "TE1 1ST",
        country = "GB"
      )

      val ua =
        minUa
          .set(CompanyContactOptionsPage, ContactOptions.Email)
          .success
          .value
          .set(CompanyEmailAddressPage, "one@two.three")
          .success
          .value
          .set(CompanyAddressYesNoPage, true)
          .success
          .value
          .set(CompanyAddressPage, address)
          .success
          .value
          .set(CompanyUtrYesNoPage, true)
          .success
          .value
          .set(CompanyUtrPage, "1234567890")
          .success
          .value
          .set(CompanyCrnYesNoPage, true)
          .success
          .value
          .set(CompanyCrnPage, "AC012345")
          .success
          .value
          .set(CompanyWorksReferenceYesNoPage, true)
          .success
          .value
          .set(CompanyWorksReferencePage, "WRN-001")
          .success
          .value

      val application = applicationBuilder(userAnswers = Some(ua)).build()

      running(application) {
        val request =
          FakeRequest(GET, controllers.add.company.routes.CompanyCheckYourAnswersController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual OK
        val content = contentAsString(result)
        content must include("Test Company Ltd")
        content must include("one@two.three")
        content must include("1234567890")
        content must include("AC012345")
        content must include("WRN-001")
        content must include("1 Test Street")
      }
    }

    "must redirect to Journey Recovery for a GET if validation fails (incomplete / url hopped)" in {
      val invalidUa =
        emptyUserAnswers
          .set(TypeOfSubcontractorPage, TypeOfSubcontractor.Limitedcompany)
          .success
          .value

      val application = applicationBuilder(userAnswers = Some(invalidUa)).build()

      running(application) {
        val request =
          FakeRequest(GET, controllers.add.company.routes.CompanyCheckYourAnswersController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(GET, controllers.add.company.routes.CompanyCheckYourAnswersController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect back to Company CYA and set submitted flag when valid data is submitted" in {
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
          FakeRequest(POST, controllers.add.company.routes.CompanyCheckYourAnswersController.onSubmit().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.add.company.routes.CompanyCheckYourAnswersController
          .onPageLoad()
          .url
      }

      verify(mockSubcontractorService).createAndUpdateSubcontractor(any[UserAnswers])(any[HeaderCarrier])
      verify(mockSessionRepository).set(any[UserAnswers])
      verifyNoMoreInteractions(mockSubcontractorService)
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
          FakeRequest(POST, controllers.add.company.routes.CompanyCheckYourAnswersController.onSubmit().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }

      verify(mockSubcontractorService, never()).createAndUpdateSubcontractor(any[UserAnswers])(any[HeaderCarrier])
      verify(mockSessionRepository, never()).set(any[UserAnswers])
      verifyNoMoreInteractions(mockSubcontractorService)
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
          FakeRequest(POST, controllers.add.company.routes.CompanyCheckYourAnswersController.onSubmit().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }

      verify(mockSubcontractorService).createAndUpdateSubcontractor(any[UserAnswers])(any[HeaderCarrier])
      verify(mockSessionRepository, never()).set(any[UserAnswers])
      verifyNoMoreInteractions(mockSubcontractorService)
    }

    "must redirect to Journey Recovery on submit when validation fails (Left(error)) and not call service" in {
      val mockSubcontractorService = mock[SubcontractorService]
      val mockSessionRepository    = mock[SessionRepository]

      val invalidUa =
        emptyUserAnswers
          .set(TypeOfSubcontractorPage, TypeOfSubcontractor.Limitedcompany)
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
          FakeRequest(POST, controllers.add.company.routes.CompanyCheckYourAnswersController.onSubmit().url)

        val result = route(application, request).value

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
          FakeRequest(POST, controllers.add.company.routes.CompanyCheckYourAnswersController.onSubmit().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }

  "company contact option validation" - {

    "must return OK when Email is selected and a mobile number is present" in {
      val ua = minUa
        .set(CompanyContactOptionsPage, ContactOptions.Email)
        .success
        .value
        .set(CompanyEmailAddressPage, "one@two.three")
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(ua)).build()

      running(application) {
        val request =
          FakeRequest(GET, controllers.add.company.routes.CompanyCheckYourAnswersController.onPageLoad().url)
        val result  = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) must include("one@two.three")
      }
    }

    "must return OK when Phone is selected and a phone number is present" in {
      val ua = minUa
        .set(CompanyContactOptionsPage, ContactOptions.Phone)
        .success
        .value
        .set(CompanyPhoneNumberPage, "01234567890")
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(ua)).build()

      running(application) {
        val request =
          FakeRequest(GET, controllers.add.company.routes.CompanyCheckYourAnswersController.onPageLoad().url)
        val result  = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) must include("01234567890")
      }
    }

    "must return OK when Mobile is selected and a mobile number is present" in {
      val ua = minUa
        .set(CompanyContactOptionsPage, ContactOptions.Mobile)
        .success
        .value
        .set(CompanyMobileNumberPage, "07123456789")
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(ua)).build()

      running(application) {
        val request =
          FakeRequest(GET, controllers.add.company.routes.CompanyCheckYourAnswersController.onPageLoad().url)
        val result  = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) must include("07123456789")
      }
    }

    "must return OK when NoDetails is selected and no contact details are present" in {
      val ua = minUa
        .set(CompanyContactOptionsPage, ContactOptions.NoDetails)
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(ua)).build()

      running(application) {
        val request =
          FakeRequest(GET, controllers.add.company.routes.CompanyCheckYourAnswersController.onPageLoad().url)
        val result  = route(application, request).value

        status(result) mustEqual OK
      }
    }

    "must redirect to Journey Recovery when Email is selected but email address is missing" in {
      val ua =
        minUa
          .set(CompanyContactOptionsPage, ContactOptions.Email)
          .success
          .value

      val application = applicationBuilder(userAnswers = Some(ua)).build()
      running(application) {
        val request =
          FakeRequest(GET, controllers.add.company.routes.CompanyCheckYourAnswersController.onPageLoad().url)
        val result  = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value must include("/there-is-a-problem")
      }
    }

    "must redirect to Journey Recovery when Phone is selected but phone number is missing" in {
      val ua =
        minUa
          .set(CompanyContactOptionsPage, ContactOptions.Phone)
          .success
          .value

      val application = applicationBuilder(userAnswers = Some(ua)).build()
      running(application) {
        val request =
          FakeRequest(GET, controllers.add.company.routes.CompanyCheckYourAnswersController.onPageLoad().url)
        val result  = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value must include("/there-is-a-problem")
      }
    }

    "must redirect to Journey Recovery when Mobile is selected but mobile number is missing" in {
      val ua =
        minUa
          .set(CompanyContactOptionsPage, ContactOptions.Mobile)
          .success
          .value

      val application = applicationBuilder(userAnswers = Some(ua)).build()
      running(application) {
        val request =
          FakeRequest(GET, controllers.add.company.routes.CompanyCheckYourAnswersController.onPageLoad().url)
        val result  = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value must include("/there-is-a-problem")
      }
    }

    "must redirect to Journey Recovery when NoDetails is selected but stale contact data exists" in {
      val uaBase =
        minUa
          .set(CompanyContactOptionsPage, ContactOptions.NoDetails)
          .success
          .value

      val ua = withStaleValue(uaBase, CompanyEmailAddressPage, "stale@email.com")

      val application = applicationBuilder(userAnswers = Some(ua)).build()
      running(application) {
        val request =
          FakeRequest(GET, controllers.add.company.routes.CompanyCheckYourAnswersController.onPageLoad().url)
        val result  = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value must include("/there-is-a-problem")
      }
    }
  }

  "company contact option change cleanup" - {

    "must return OK when switching from Email to Phone and stale email is cleaned up" in {
      val ua =
        minUa
          .set(CompanyContactOptionsPage, ContactOptions.Email)
          .success
          .value
          .set(CompanyEmailAddressPage, "old@email.com")
          .success
          .value
          .set(CompanyContactOptionsPage, ContactOptions.Phone)
          .success
          .value
          .set(CompanyPhoneNumberPage, "01234567890")
          .success
          .value

      val application = applicationBuilder(userAnswers = Some(ua)).build()
      running(application) {
        val request =
          FakeRequest(GET, controllers.add.company.routes.CompanyCheckYourAnswersController.onPageLoad().url)
        val result  = route(application, request).value
        status(result) mustEqual OK
        contentAsString(result) must include("01234567890")
      }
    }

    "must return OK when switching from Phone to Mobile and stale phone is cleaned up" in {
      val ua =
        minUa
          .set(CompanyContactOptionsPage, ContactOptions.Phone)
          .success
          .value
          .set(CompanyPhoneNumberPage, "01234567890")
          .success
          .value
          .set(CompanyContactOptionsPage, ContactOptions.Mobile)
          .success
          .value
          .set(CompanyMobileNumberPage, "07123456789")
          .success
          .value

      val application = applicationBuilder(userAnswers = Some(ua)).build()
      running(application) {
        val request =
          FakeRequest(GET, controllers.add.company.routes.CompanyCheckYourAnswersController.onPageLoad().url)
        val result  = route(application, request).value
        status(result) mustEqual OK
        contentAsString(result) must include("07123456789")
      }
    }

    "must return OK when switching from Email to NoDetails and stale email is cleaned up" in {
      val ua =
        minUa
          .set(CompanyContactOptionsPage, ContactOptions.Email)
          .success
          .value
          .set(CompanyEmailAddressPage, "old@email.com")
          .success
          .value
          .set(CompanyContactOptionsPage, ContactOptions.NoDetails)
          .success
          .value

      val application = applicationBuilder(userAnswers = Some(ua)).build()
      running(application) {
        val request =
          FakeRequest(GET, controllers.add.company.routes.CompanyCheckYourAnswersController.onPageLoad().url)
        val result  = route(application, request).value
        status(result) mustEqual OK
      }
    }
  }

  "company YesNo stale session" - {

    "must return OK when CompanyAddressYesNoPage changes from Yes to No and stale UTR values are cleaned up" in {

      val address = InternationalAddress("1 Test Street", None, "City", None, "AA1 1AA", "GB")

      val ua = minUa
        .set(CompanyContactOptionsPage, ContactOptions.Email)
        .success
        .value
        .set(CompanyEmailAddressPage, "one@two.three")
        .success
        .value
        .set(CompanyAddressYesNoPage, true)
        .success
        .value
        .set(CompanyAddressPage, address)
        .success
        .value
        .set(CompanyAddressYesNoPage, false)
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(ua)).build()

      running(application) {
        val request =
          FakeRequest(GET, controllers.add.company.routes.CompanyCheckYourAnswersController.onPageLoad().url)
        val result  = route(application, request).value

        status(result) mustEqual OK
      }
    }

    "must return OK when CompanyUtrYesNoPage changes from Yes to No and stale UTR values are cleaned up" in {
      val ua = minUa
        .set(CompanyContactOptionsPage, ContactOptions.Email)
        .success
        .value
        .set(CompanyEmailAddressPage, "one@two.three")
        .success
        .value
        .set(CompanyUtrYesNoPage, true)
        .success
        .value
        .set(CompanyUtrPage, "1234567890")
        .success
        .value
        .set(CompanyUtrYesNoPage, false)
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(ua)).build()

      running(application) {
        val request =
          FakeRequest(GET, controllers.add.company.routes.CompanyCheckYourAnswersController.onPageLoad().url)
        val result  = route(application, request).value

        status(result) mustEqual OK
      }
    }

    "must return OK when CompanyCrnYesNoPage changes from Yes to No and stale Crn values are cleaned up" in {
      val ua = minUa
        .set(CompanyContactOptionsPage, ContactOptions.Email)
        .success
        .value
        .set(CompanyEmailAddressPage, "one@two.three")
        .success
        .value
        .set(CompanyCrnYesNoPage, true)
        .success
        .value
        .set(CompanyCrnPage, "AC012345")
        .success
        .value
        .set(CompanyCrnYesNoPage, false)
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(ua)).build()

      running(application) {
        val request =
          FakeRequest(GET, controllers.add.company.routes.CompanyCheckYourAnswersController.onPageLoad().url)
        val result  = route(application, request).value

        status(result) mustEqual OK
      }
    }

    "must return OK when CompanyWorksReferenceYesNoPage changes from Yes to No and stale WorksReference values are cleaned up" in {
      val ua = minUa
        .set(CompanyContactOptionsPage, ContactOptions.Email)
        .success
        .value
        .set(CompanyEmailAddressPage, "one@two.three")
        .success
        .value
        .set(CompanyWorksReferenceYesNoPage, true)
        .success
        .value
        .set(CompanyWorksReferencePage, "WRN-001")
        .success
        .value
        .set(CompanyWorksReferenceYesNoPage, false)
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(ua)).build()

      running(application) {
        val request =
          FakeRequest(GET, controllers.add.company.routes.CompanyCheckYourAnswersController.onPageLoad().url)
        val result  = route(application, request).value

        status(result) mustEqual OK
      }
    }

    "must redirect to Journey Recovery when AddressYesNo is false but address value is present (stale session)" in {
      val address = InternationalAddress("1 Test Street", None, "City", None, "AA1 1AA", "GB")

      val uaBase =
        minUa
          .set(CompanyContactOptionsPage, ContactOptions.NoDetails)
          .success
          .value
          .set(CompanyAddressYesNoPage, false)
          .success
          .value

      val ua = withStaleValue(uaBase, CompanyAddressPage, address)

      val application = applicationBuilder(userAnswers = Some(ua)).build()
      running(application) {
        val request =
          FakeRequest(GET, controllers.add.company.routes.CompanyCheckYourAnswersController.onPageLoad().url)
        val result  = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value must include("/there-is-a-problem")
      }
    }

    "must redirect to Journey Recovery when CompanyUtrYesNo is false but UTR value is present (stale session)" in {
      val uaBase =
        minUa
          .set(CompanyContactOptionsPage, ContactOptions.NoDetails)
          .success
          .value
          .set(CompanyUtrYesNoPage, false)
          .success
          .value

      val ua = withStaleValue(uaBase, CompanyUtrPage, "1234567890")

      val application = applicationBuilder(userAnswers = Some(ua)).build()
      running(application) {
        val request =
          FakeRequest(GET, controllers.add.company.routes.CompanyCheckYourAnswersController.onPageLoad().url)
        val result  = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value must include("/there-is-a-problem")
      }
    }

    "must redirect to Journey Recovery when CompanyCrnYesNo is false but CRN value is present (stale session)" in {
      val uaBase =
        minUa
          .set(CompanyContactOptionsPage, ContactOptions.NoDetails)
          .success
          .value
          .set(CompanyCrnYesNoPage, false)
          .success
          .value

      val ua = withStaleValue(uaBase, CompanyCrnPage, "AC012345")

      val application = applicationBuilder(userAnswers = Some(ua)).build()
      running(application) {
        val request =
          FakeRequest(GET, controllers.add.company.routes.CompanyCheckYourAnswersController.onPageLoad().url)
        val result  = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value must include("/there-is-a-problem")
      }
    }

    "must redirect to Journey Recovery when WorksReferenceYesNo is false but works ref is present (stale session)" in {
      val uaBase =
        minUa
          .set(CompanyContactOptionsPage, ContactOptions.NoDetails)
          .success
          .value
          .set(CompanyWorksReferenceYesNoPage, false)
          .success
          .value

      val ua = withStaleValue(uaBase, CompanyWorksReferencePage, "WRN-001")

      val application = applicationBuilder(userAnswers = Some(ua)).build()
      running(application) {
        val request =
          FakeRequest(GET, controllers.add.company.routes.CompanyCheckYourAnswersController.onPageLoad().url)
        val result  = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value must include("/there-is-a-problem")
      }
    }
  }
}
