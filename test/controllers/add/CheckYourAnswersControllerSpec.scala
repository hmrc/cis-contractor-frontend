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
import models.add.{SubContactDetails, TypeOfSubcontractor, UKAddress}
import models.{CheckMode, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, verifyNoMoreInteractions, when}
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.add.*
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.SubcontractorService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class CheckYourAnswersControllerSpec extends SpecBase {

  "Check Your Answers Controller" - {
    val address = UKAddress(
      addressLine1 = "10 Downing Street",
      addressLine2 = Some("Westminster"),
      addressLine3 = "London",
      addressLine4 = Some("UK"),
      postCode = "SW1A 2AA"
    )
    val ua =
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
        .set(SubcontractorContactDetailsYesNoPage, true)
        .success
        .value
        .set(SubContactDetailsPage, SubContactDetails("test@example.com", "0123456789"))
        .success
        .value

    "must display all questions and dependent rows when answers are provided" in {



      val application = applicationBuilder(userAnswers = Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.add.routes.CheckYourAnswersController.onPageLoad().url)
        val result  = route(application, request).value

        val content = contentAsString(result)
        status(result) mustEqual OK

        content must include("Type")
        content must include("Does the subcontractor use a trading name?")
        content must include("Trading name")
        content must include("Do you want to add the subcontractor’s address?")
        content must include("Subcontractor address")
        content must include("Do you have a National Insurance number?")
        content must include("National Insurance number")
        content must include("Do you have a Unique Taxpayer Reference (UTR)?")
        content must include("Unique Taxpayer Reference")
        content must include("Do you have a works reference number?")
        content must include("Works reference number")
        content must include("Do you want to add the subcontractor’s contact details?")

        content must include("ABC Ltd")
        content must include("AB123456C")
        content must include("1234567890")
        content must include("WRN-001")

        content must include("Contact details")
        content must include("test@example.com")
        content must include("0123456789")

        content must include(controllers.add.routes.SubTradingNameYesNoController.onPageLoad(CheckMode).url)
        content must include(controllers.add.routes.SubAddressYesNoController.onPageLoad(CheckMode).url)
        content must include(controllers.add.routes.NationalInsuranceNumberYesNoController.onPageLoad(CheckMode).url)
        content must include(controllers.add.routes.UniqueTaxpayerReferenceYesNoController.onPageLoad(CheckMode).url)
        content must include(controllers.add.routes.WorksReferenceNumberYesNoController.onPageLoad(CheckMode).url)
        content must include(
          controllers.add.routes.SubcontractorContactDetailsYesNoController.onPageLoad(CheckMode).url
        )
      }
    }

    "must redirect to other (confirm page ) when valid data is submitted" in {
      val mockSubcontractorService = mock[SubcontractorService]

      when(mockSubcontractorService.createAndUpdateSubcontractor(any[UserAnswers])(any[HeaderCarrier]))
        .thenReturn(Future.successful(()))

      val application =
        applicationBuilder(userAnswers = Some(ua))
          .overrides(
            bind[SubcontractorService].toInstance(mockSubcontractorService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, controllers.add.routes.CheckYourAnswersController.onSubmit().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.add.routes.CheckYourAnswersController
          .onPageLoad()
          .url
      }

      verify(mockSubcontractorService).createAndUpdateSubcontractor(any[UserAnswers])(any[HeaderCarrier])
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

    "must redirect to Journey Recovery when service call fails (recover block)" in {
      val mockSubcontractorService = mock[SubcontractorService]

      when(mockSubcontractorService.createAndUpdateSubcontractor(any[UserAnswers])(any[HeaderCarrier]))
        .thenReturn(Future.failed(new RuntimeException("boom")))

      val application =
        applicationBuilder(userAnswers = Some(ua))
          .overrides(
            bind[SubcontractorService].toInstance(mockSubcontractorService)
          )
          .build()

      running(application) {
        val request = FakeRequest(POST, controllers.add.routes.CheckYourAnswersController.onSubmit().url)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }

      verify(mockSubcontractorService).createAndUpdateSubcontractor(any[UserAnswers])(any[HeaderCarrier])
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
  }
}
