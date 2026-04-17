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
import models.add.{InternationalAddress, SubcontractorName, TypeOfSubcontractor}
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
import play.api.libs.json.*

import scala.concurrent.Future

class CheckYourAnswersControllerSpec extends SpecBase {

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
      .set(TypeOfSubcontractorPage, TypeOfSubcontractor.Individualorsoletrader)
      .success
      .value
      .set(SubTradingNameYesNoPage, true)
      .success
      .value
      .set(TradingNameOfSubcontractorPage, "ABC Ltd")
      .success
      .value
      .set(SubAddressYesNoPage, false)
      .success
      .value
      .set(NationalInsuranceNumberYesNoPage, false)
      .success
      .value
      .set(UniqueTaxpayerReferenceYesNoPage, false)
      .success
      .value
      .set(WorksReferenceNumberYesNoPage, false)
      .success
      .value

  private val validUaForSubmit: UserAnswers =
    minUa
      .set(IndividualChooseContactDetailsPage, ContactOptions.NoDetails)
      .success
      .value

  private lazy val CYARoute = controllers.add.routes.CheckYourAnswersController.onPageLoad().url

  "Check Your Answers Controller" - {

    "must return OK and the correct view for a GET when optionals are not present" in {
      val ua =
        minUa
          .set(IndividualChooseContactDetailsPage, ContactOptions.NoDetails)
          .success
          .value

      val application = applicationBuilder(userAnswers = Some(ua)).build()

      running(application) {
        val request =
          FakeRequest(GET, CYARoute)

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) must include("ABC Ltd")
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
          .set(IndividualChooseContactDetailsPage, ContactOptions.Email)
          .success
          .value
          .set(IndividualEmailAddressPage, "one@two.three")
          .success
          .value
          .set(SubAddressYesNoPage, true)
          .success
          .value
          .set(AddressOfSubcontractorPage, address)
          .success
          .value
          .set(UniqueTaxpayerReferenceYesNoPage, true)
          .success
          .value
          .set(SubcontractorsUniqueTaxpayerReferencePage, "1234567890")
          .success
          .value
          .set(NationalInsuranceNumberYesNoPage, true)
          .success
          .value
          .set(SubNationalInsuranceNumberPage, "AB123456C")
          .success
          .value
          .set(WorksReferenceNumberYesNoPage, true)
          .success
          .value
          .set(WorksReferenceNumberPage, "WRN-001")
          .success
          .value

      val application = applicationBuilder(userAnswers = Some(ua)).build()

      running(application) {
        val request =
          FakeRequest(GET, CYARoute)

        val result = route(application, request).value

        status(result) mustEqual OK

        val content = contentAsString(result)

        content must include("Type")
        content must include("Does subcontractor use a trading name?")
        content must include("Subcontractor trading name")
        content must include("Add subcontractor address?")
        content must include("Address")
        content must include("Method of contact")
        content must include("Email address")
        content must include("Add UTR?")
        content must include("UTR")
        content must include("Add National Insurance number?")
        content must include("National Insurance number")
        content must include("Add works reference number?")
        content must include("Works reference number")

        content must include("ABC Ltd")
        content must include("one@two.three")
        content must include("1234567890")
        content must include("AB123456C")
        content must include("WRN-001")
        content must include("1 Test Street")

        content must include(controllers.add.routes.SubTradingNameYesNoController.onPageLoad(CheckMode).url)
        content must include(controllers.add.routes.SubAddressYesNoController.onPageLoad(CheckMode).url)
        content must include(controllers.add.routes.IndividualChooseContactDetailsController.onPageLoad(CheckMode).url)
        content must include(controllers.add.routes.UniqueTaxpayerReferenceYesNoController.onPageLoad(CheckMode).url)
        content must include(controllers.add.routes.NationalInsuranceNumberYesNoController.onPageLoad(CheckMode).url)
        content must include(controllers.add.routes.WorksReferenceNumberYesNoController.onPageLoad(CheckMode).url)
      }
    }

    "must redirect to Journey Recovery for a GET if validation fails (incomplete / url hopped)" in {
      val invalidUa =
        emptyUserAnswers
          .set(TypeOfSubcontractorPage, TypeOfSubcontractor.Individualorsoletrader)
          .success
          .value

      val application = applicationBuilder(userAnswers = Some(invalidUa)).build()

      running(application) {
        val request =
          FakeRequest(GET, CYARoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(GET, CYARoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to SubcontractorAdded page and set submitted flag when valid data is submitted" in {
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
          FakeRequest(POST, CYARoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.add.routes.SubcontractorAddedController
          .individualSubcontractorAdded()
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
          FakeRequest(POST, CYARoute)

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
          FakeRequest(POST, CYARoute)

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
          .set(TypeOfSubcontractorPage, TypeOfSubcontractor.Individualorsoletrader)
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
          FakeRequest(POST, CYARoute)

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
          FakeRequest(POST, CYARoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "company contact option validation" - {

      "must return OK when Email is selected and a email is present" in {
        val ua = minUa
          .set(IndividualChooseContactDetailsPage, ContactOptions.Email)
          .success
          .value
          .set(IndividualEmailAddressPage, "one@two.three")
          .success
          .value

        val application = applicationBuilder(userAnswers = Some(ua)).build()

        running(application) {
          val request =
            FakeRequest(GET, CYARoute)
          val result  = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) must include("one@two.three")
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
            FakeRequest(GET, CYARoute)
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
            FakeRequest(GET, CYARoute)
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
            FakeRequest(GET, CYARoute)
          val result  = route(application, request).value

          status(result) mustEqual OK
        }
      }

      "must redirect to Journey Recovery when Email is selected but email address is missing" in {
        val ua =
          minUa
            .set(IndividualChooseContactDetailsPage, ContactOptions.Email)
            .success
            .value

        val application = applicationBuilder(userAnswers = Some(ua)).build()
        running(application) {
          val request =
            FakeRequest(GET, CYARoute)
          val result  = route(application, request).value
          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value must include("/there-is-a-problem")
        }
      }

      "must redirect to Journey Recovery when Phone is selected but phone number is missing" in {
        val ua =
          minUa
            .set(IndividualChooseContactDetailsPage, ContactOptions.Phone)
            .success
            .value

        val application = applicationBuilder(userAnswers = Some(ua)).build()
        running(application) {
          val request =
            FakeRequest(GET, CYARoute)
          val result  = route(application, request).value
          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value must include("/there-is-a-problem")
        }
      }

      "must redirect to Journey Recovery when Mobile is selected but mobile number is missing" in {
        val ua =
          minUa
            .set(IndividualChooseContactDetailsPage, ContactOptions.Mobile)
            .success
            .value

        val application = applicationBuilder(userAnswers = Some(ua)).build()
        running(application) {
          val request =
            FakeRequest(GET, CYARoute)
          val result  = route(application, request).value
          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value must include("/there-is-a-problem")
        }
      }

      "must redirect to Journey Recovery when NoDetails is selected but stale contact data exists" in {
        val uaBase =
          minUa
            .set(IndividualChooseContactDetailsPage, ContactOptions.NoDetails)
            .success
            .value

        val ua = withStaleValue(uaBase, IndividualEmailAddressPage, "stale@email.com")

        val application = applicationBuilder(userAnswers = Some(ua)).build()
        running(application) {
          val request =
            FakeRequest(GET, CYARoute)
          val result  = route(application, request).value
          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value must include("/there-is-a-problem")
        }
      }
    }

    "contact option change cleanup" - {

      "must return OK when switching from Email to Phone and stale email is cleaned up" in {
        val ua =
          minUa
            .set(IndividualChooseContactDetailsPage, ContactOptions.Email)
            .success
            .value
            .set(IndividualEmailAddressPage, "old@email.com")
            .success
            .value
            .set(IndividualChooseContactDetailsPage, ContactOptions.Phone)
            .success
            .value
            .set(IndividualPhoneNumberPage, "01234567890")
            .success
            .value

        val application = applicationBuilder(userAnswers = Some(ua)).build()
        running(application) {
          val request =
            FakeRequest(GET, CYARoute)
          val result  = route(application, request).value
          status(result) mustEqual OK
          contentAsString(result) must include("01234567890")
        }
      }

      "must return OK when switching from Phone to Mobile and stale phone is cleaned up" in {
        val ua =
          minUa
            .set(IndividualChooseContactDetailsPage, ContactOptions.Phone)
            .success
            .value
            .set(IndividualPhoneNumberPage, "01234567890")
            .success
            .value
            .set(IndividualChooseContactDetailsPage, ContactOptions.Mobile)
            .success
            .value
            .set(IndividualMobileNumberPage, "07123456789")
            .success
            .value

        val application = applicationBuilder(userAnswers = Some(ua)).build()
        running(application) {
          val request =
            FakeRequest(GET, CYARoute)
          val result  = route(application, request).value
          status(result) mustEqual OK
          contentAsString(result) must include("07123456789")
        }
      }

      "must return OK when switching from Email to NoDetails and stale email is cleaned up" in {
        val ua =
          minUa
            .set(IndividualChooseContactDetailsPage, ContactOptions.Email)
            .success
            .value
            .set(IndividualEmailAddressPage, "old@email.com")
            .success
            .value
            .set(IndividualChooseContactDetailsPage, ContactOptions.NoDetails)
            .success
            .value

        val application = applicationBuilder(userAnswers = Some(ua)).build()
        running(application) {
          val request =
            FakeRequest(GET, CYARoute)
          val result  = route(application, request).value
          status(result) mustEqual OK
        }
      }
    }

    "YesNo stale session" - {

      "must return OK when SubTradingNameYesNoPage changes from Yes to No and stale TradingNameOfSubcontractor values are cleaned up" in {

        val name = SubcontractorName("John", Some("Paul"), "Smith")

        val ua = minUa
          .set(IndividualChooseContactDetailsPage, ContactOptions.Email)
          .success
          .value
          .set(IndividualEmailAddressPage, "one@two.three")
          .success
          .value
          .set(SubTradingNameYesNoPage, true)
          .success
          .value
          .set(TradingNameOfSubcontractorPage, "ABC Ltd")
          .success
          .value
          .set(SubTradingNameYesNoPage, false)
          .success
          .value
          .set(SubcontractorNamePage, name)
          .success
          .value

        val application = applicationBuilder(userAnswers = Some(ua)).build()

        running(application) {
          val request =
            FakeRequest(GET, CYARoute)
          val result  = route(application, request).value

          status(result) mustEqual OK
        }
      }

      "must return OK when SubTradingNameYesNoPage changes from No to Yes and stale SubcontractorNamePage values are cleaned up" in {

        val name = SubcontractorName("John", Some("Paul"), "Smith")

        val ua = minUa
          .set(IndividualChooseContactDetailsPage, ContactOptions.Email)
          .success
          .value
          .set(IndividualEmailAddressPage, "one@two.three")
          .success
          .value
          .set(SubTradingNameYesNoPage, false)
          .success
          .value
          .set(SubcontractorNamePage, name)
          .success
          .value
          .set(SubTradingNameYesNoPage, true)
          .success
          .value
          .set(TradingNameOfSubcontractorPage, "ABC Ltd")
          .success
          .value

        val application = applicationBuilder(userAnswers = Some(ua)).build()

        running(application) {
          val request =
            FakeRequest(GET, CYARoute)
          val result  = route(application, request).value

          status(result) mustEqual OK
        }
      }

      "must return OK when SubAddressYesNo changes from Yes to No and stale UTR values are cleaned up" in {

        val address = InternationalAddress("1 Test Street", None, "City", None, "AA1 1AA", "GB")

        val ua = minUa
          .set(IndividualChooseContactDetailsPage, ContactOptions.Email)
          .success
          .value
          .set(IndividualEmailAddressPage, "one@two.three")
          .success
          .value
          .set(SubAddressYesNoPage, true)
          .success
          .value
          .set(AddressOfSubcontractorPage, address)
          .success
          .value
          .set(SubAddressYesNoPage, false)
          .success
          .value

        val application = applicationBuilder(userAnswers = Some(ua)).build()

        running(application) {
          val request =
            FakeRequest(GET, CYARoute)
          val result  = route(application, request).value

          status(result) mustEqual OK
        }
      }

      "must return OK when UniqueTaxpayerReferenceYesNo changes from Yes to No and stale UTR values are cleaned up" in {
        val ua = minUa
          .set(IndividualChooseContactDetailsPage, ContactOptions.Email)
          .success
          .value
          .set(IndividualEmailAddressPage, "one@two.three")
          .success
          .value
          .set(UniqueTaxpayerReferenceYesNoPage, true)
          .success
          .value
          .set(SubcontractorsUniqueTaxpayerReferencePage, "1234567890")
          .success
          .value
          .set(UniqueTaxpayerReferenceYesNoPage, false)
          .success
          .value

        val application = applicationBuilder(userAnswers = Some(ua)).build()

        running(application) {
          val request =
            FakeRequest(GET, CYARoute)
          val result  = route(application, request).value

          status(result) mustEqual OK
        }
      }

      "must return OK when NationalInsuranceNumberYesNo changes from Yes to No and stale NationalInsuranceNumber values are cleaned up" in {
        val ua = minUa
          .set(IndividualChooseContactDetailsPage, ContactOptions.Email)
          .success
          .value
          .set(IndividualEmailAddressPage, "one@two.three")
          .success
          .value
          .set(NationalInsuranceNumberYesNoPage, true)
          .success
          .value
          .set(SubNationalInsuranceNumberPage, "AC012345C")
          .success
          .value
          .set(NationalInsuranceNumberYesNoPage, false)
          .success
          .value

        val application = applicationBuilder(userAnswers = Some(ua)).build()

        running(application) {
          val request =
            FakeRequest(GET, CYARoute)
          val result  = route(application, request).value

          status(result) mustEqual OK
        }
      }

      "must return OK when WorksReferenceNumberYesNo changes from Yes to No and stale WorksReference values are cleaned up" in {
        val ua = minUa
          .set(IndividualChooseContactDetailsPage, ContactOptions.Email)
          .success
          .value
          .set(IndividualEmailAddressPage, "one@two.three")
          .success
          .value
          .set(WorksReferenceNumberYesNoPage, true)
          .success
          .value
          .set(WorksReferenceNumberPage, "WRN-001")
          .success
          .value
          .set(WorksReferenceNumberYesNoPage, false)
          .success
          .value

        val application = applicationBuilder(userAnswers = Some(ua)).build()

        running(application) {
          val request =
            FakeRequest(GET, CYARoute)
          val result  = route(application, request).value

          status(result) mustEqual OK
        }
      }

      "must redirect to Journey Recovery when SubTradingNameYesNoPage is false but TradingNameOfSubcontractor value is present (stale session)" in {

        val name = SubcontractorName("John", Some("Paul"), "Smith")

        val uaBase =
          minUa
            .set(IndividualChooseContactDetailsPage, ContactOptions.NoDetails)
            .success
            .value
            .set(SubTradingNameYesNoPage, false)
            .success
            .value
            .set(SubcontractorNamePage, name)
            .success
            .value

        val ua = withStaleValue(uaBase, TradingNameOfSubcontractorPage, "ABC Ltd")

        val application = applicationBuilder(userAnswers = Some(ua)).build()
        running(application) {
          val request =
            FakeRequest(GET, CYARoute)
          val result  = route(application, request).value
          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value must include("/there-is-a-problem")
        }
      }

      "must redirect to Journey Recovery when SubTradingNameYesNoPage is true but TradingNameOfSubcontractor value is present (stale session)" in {

        val name = SubcontractorName("John", Some("Paul"), "Smith")

        val uaBase =
          minUa
            .set(IndividualChooseContactDetailsPage, ContactOptions.NoDetails)
            .success
            .value
            .set(SubTradingNameYesNoPage, true)
            .success
            .value
            .set(TradingNameOfSubcontractorPage, "ABC Ltd")
            .success
            .value

        val ua = withStaleValue(uaBase, SubcontractorNamePage, name)

        val application = applicationBuilder(userAnswers = Some(ua)).build()
        running(application) {
          val request =
            FakeRequest(GET, CYARoute)
          val result  = route(application, request).value
          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value must include("/there-is-a-problem")
        }
      }

      "must redirect to Journey Recovery when AddressYesNo is false but address value is present (stale session)" in {
        val address = InternationalAddress("1 Test Street", None, "City", None, "AA1 1AA", "GB")

        val uaBase =
          minUa
            .set(IndividualChooseContactDetailsPage, ContactOptions.NoDetails)
            .success
            .value
            .set(SubAddressYesNoPage, false)
            .success
            .value

        val ua = withStaleValue(uaBase, AddressOfSubcontractorPage, address)

        val application = applicationBuilder(userAnswers = Some(ua)).build()
        running(application) {
          val request =
            FakeRequest(GET, CYARoute)
          val result  = route(application, request).value
          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value must include("/there-is-a-problem")
        }
      }

      "must redirect to Journey Recovery when UniqueTaxpayerReferenceYesNo is false but UTR value is present (stale session)" in {
        val uaBase =
          minUa
            .set(IndividualChooseContactDetailsPage, ContactOptions.NoDetails)
            .success
            .value
            .set(UniqueTaxpayerReferenceYesNoPage, false)
            .success
            .value

        val ua = withStaleValue(uaBase, SubcontractorsUniqueTaxpayerReferencePage, "1234567890")

        val application = applicationBuilder(userAnswers = Some(ua)).build()
        running(application) {
          val request =
            FakeRequest(GET, CYARoute)
          val result  = route(application, request).value
          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value must include("/there-is-a-problem")
        }
      }

      "must redirect to Journey Recovery when NationalInsuranceNumberYesNo is false but nino value is present (stale session)" in {
        val uaBase =
          minUa
            .set(IndividualChooseContactDetailsPage, ContactOptions.NoDetails)
            .success
            .value
            .set(NationalInsuranceNumberYesNoPage, false)
            .success
            .value

        val ua = withStaleValue(uaBase, SubNationalInsuranceNumberPage, "AC0123456C")

        val application = applicationBuilder(userAnswers = Some(ua)).build()
        running(application) {
          val request =
            FakeRequest(GET, CYARoute)
          val result  = route(application, request).value
          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value must include("/there-is-a-problem")
        }
      }

      "must redirect to Journey Recovery when WorksReferenceYesNo is false but works ref is present (stale session)" in {
        val uaBase =
          minUa
            .set(IndividualChooseContactDetailsPage, ContactOptions.NoDetails)
            .success
            .value
            .set(WorksReferenceNumberYesNoPage, false)
            .success
            .value

        val ua = withStaleValue(uaBase, WorksReferenceNumberPage, "WRN-001")

        val application = applicationBuilder(userAnswers = Some(ua)).build()
        running(application) {
          val request =
            FakeRequest(GET, CYARoute)
          val result  = route(application, request).value
          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value must include("/there-is-a-problem")
        }
      }
    }
  }
}
