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

package controllers.amend

import base.SpecBase
import controllers.routes
import models.add.SubcontractorName
import models.address.{Address, Country}
import models.amend.OriginalIndividualAnswers
import models.{TypeOfSubcontractor, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, verify, verifyNoInteractions, verifyNoMoreInteractions, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.add.TypeOfSubcontractorPage
import pages.add.*
import play.api.i18n.MessagesApi
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import queries.OriginalIndividualAnswersQuery
import repositories.SessionRepository
import services.SubcontractorService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future
import models.contact.ContactMethodOptions
import org.mockito.ArgumentCaptor
import pages.amend.{AmendCheckYourAnswersSubmittedPage, ShowVerificationDetailsPage}

class AmendIndividualCheckYourAnswersControllerSpec extends SpecBase with MockitoSugar {
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
  private val minUa   =
    emptyUserAnswers
      .set(TypeOfSubcontractorPage, TypeOfSubcontractor.Individualorsoletrader)
      .success
      .value
      .set(SubTradingNameYesNoPage, false)
      .success
      .value
      .set(
        SubcontractorNamePage,
        SubcontractorName(
          firstName = "John",
          middleName = None,
          lastName = "Smith"
        )
      )
      .success
      .value
      .set(SubAddressYesNoPage, true)
      .success
      .value
      .set(AddressOfSubcontractorPage, address)
      .success
      .value
      .set(AddIndividualContactMethodsYesNoPage, true)
      .success
      .value
      .set(IndividualContactMethodOptionsPage, Set(ContactMethodOptions.Email))
      .success
      .value
      .set(IndividualEmailAddressPage, "test@test.com")
      .success
      .value
      .set(UniqueTaxpayerReferenceYesNoPage, true)
      .success
      .value
      .set(SubcontractorsUniqueTaxpayerReferencePage, "11111111")
      .success
      .value
      .set(NationalInsuranceNumberYesNoPage, false)
      .success
      .value
      .set(WorksReferenceNumberYesNoPage, true)
      .success
      .value
      .set(WorksReferenceNumberPage, "WRN-1")
      .success
      .value
      .set(ShowVerificationDetailsPage, false)
      .success
      .value
      .set(
        OriginalIndividualAnswersQuery,
        OriginalIndividualAnswers(
          usesTradingName = Some(false),
          subcontractorName = Some(
            SubcontractorName(
              firstName = "John",
              middleName = None,
              lastName = "Smith"
            )
          ),
          tradingName = None,
          addressYesNo = Some(true),
          address = Some(address),
          individualContactMethodsYesNo = Some(true),
          individualContactMethod = Set(ContactMethodOptions.Email),
          email = Some("test@test.com"),
          phone = None,
          mobile = None,
          utrYesNo = Some(true),
          utr = Some("11111111"),
          ninoYesNo = Some(false),
          nino = None,
          worksReferenceYesNo = Some(true),
          worksReference = Some("WRN-1"),
          verificationNumber = None
        )
      )
      .success
      .value

  "AmendIndividualCheckYourAnswersController" - {

    "must return OK and render the page with the correct summary list for GET when validation succeeds for unverified individual" in {
      val application =
        applicationBuilder(userAnswers = Some(minUa)).build()

      running(application) {
        val request =
          FakeRequest(GET, controllers.amend.routes.AmendIndividualCheckYourAnswersController.onPageLoad().url)
        val msg     = app.injector.instanceOf[MessagesApi].preferred(request)
        val result  = route(application, request).value

        status(result) mustEqual OK

        val page = contentAsString(result)

        page must include(msg("typeOfSubcontractor.checkYourAnswersLabel"))
        page must include(msg("subTradingNameYesNo.checkYourAnswersLabel"))
        page must include(msg("subcontractorName.checkYourAnswersLabel"))
        page must include(msg("subAddressYesNo.checkYourAnswersLabel"))
        page must include(msg("addressOfSubcontractor.checkYourAnswersLabel"))
        page must include(msg("uniqueTaxpayerReferenceYesNo.checkYourAnswersLabel"))
        page must include(msg("subcontractorsUniqueTaxpayerReference.checkYourAnswersLabel"))
        page must include(msg("addIndividualContactMethodsYesNo.checkYourAnswersLabel"))
        page must include(msg("individualContactMethodOptions.checkYourAnswersLabel"))
        page must include(msg("individualEmailAddress.checkYourAnswersLabel"))
        page must include(msg("nationalInsuranceNumberYesNo.checkYourAnswersLabel"))
        page must include(msg("worksReferenceNumberYesNo.checkYourAnswersLabel"))
        page must include(msg("worksReferenceNumber.checkYourAnswersLabel"))

        page must not include msg("amendCheckYourAnswers.verificationNumber.label")

        page must include("Individual")
        page must include("John Smith")
        page must include("11111111")
        page must include("WRN-1")
        page must include("test@test.com")
        page must include("12 Harbor View Road")
        page must include("Amity Island")
        page must include("Bodmin")
        page must include("Cornwall")
        page must include("PL31 2HL")
        page must include("England")

        page must not include "VRN123456"
      }
    }

    "must render the correct summary for a verified individual" in {

      val verifiedUa =
        minUa
          .set(ShowVerificationDetailsPage, true)
          .success
          .value
          .set(
            OriginalIndividualAnswersQuery,
            OriginalIndividualAnswers(
              usesTradingName = Some(false),
              subcontractorName = Some(
                SubcontractorName(
                  firstName = "John",
                  middleName = None,
                  lastName = "Smith"
                )
              ),
              tradingName = None,
              addressYesNo = Some(false),
              address = None,
              individualContactMethodsYesNo = Some(false),
              individualContactMethod = Set.empty,
              email = None,
              phone = None,
              mobile = None,
              utrYesNo = Some(false),
              utr = None,
              ninoYesNo = Some(false),
              nino = None,
              worksReferenceYesNo = Some(false),
              worksReference = None,
              verificationNumber = Some("VRN123456")
            )
          )
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(verifiedUa)).build()

      running(application) {

        val request =
          FakeRequest(GET, controllers.amend.routes.AmendIndividualCheckYourAnswersController.onPageLoad().url)
        val msg     = app.injector.instanceOf[MessagesApi].preferred(request)
        val result  = route(application, request).value

        status(result) mustEqual OK

        val page = contentAsString(result)

        page must include(msg("amendCheckYourAnswers.verificationNumber.label"))
        page must include("VRN123456")

        page must not include msg("subTradingNameYesNo.checkYourAnswersLabel")
        page must not include msg("subcontractorName.checkYourAnswersLabel")
        page must not include msg("uniqueTaxpayerReferenceYesNo.checkYourAnswersLabel")

        page must include(msg("typeOfSubcontractor.checkYourAnswersLabel"))
        page must include("Individual")

        page must include(msg("subcontractorsUniqueTaxpayerReference.checkYourAnswersLabel"))
        page must include("11111111")

        page must include(msg("subAddressYesNo.checkYourAnswersLabel"))
        page must include(msg("addressOfSubcontractor.checkYourAnswersLabel"))
        page must include(msg("site.yes"))
        page must include("12 Harbor View Road")
        page must include("Amity Island")
        page must include("Bodmin")
        page must include("Cornwall")
        page must include("PL31 2HL")
        page must include("England")

        page must include(msg("addIndividualContactMethodsYesNo.checkYourAnswersLabel"))
        page must include(msg("individualContactMethodOptions.checkYourAnswersLabel"))
        page must include(msg("individualEmailAddress.checkYourAnswersLabel"))
        page must include("test@test.com")

        page must include(msg("worksReferenceNumberYesNo.checkYourAnswersLabel"))
        page must include(msg("worksReferenceNumber.checkYourAnswersLabel"))
        page must include(msg("site.no"))
        page must include("WRN-1")
      }
    }

    "must redirect to Journey Recovery when validation fails" in {

      val invalidUa =
        emptyUserAnswers
          .set(TypeOfSubcontractorPage, TypeOfSubcontractor.Individualorsoletrader)
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(invalidUa)).build()

      running(application) {

        val request =
          FakeRequest(GET, controllers.amend.routes.AmendIndividualCheckYourAnswersController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to confirmation page after successful submit" in {

      val mockSubcontractorService = mock[SubcontractorService]
      val mockSessionRepository    = mock[SessionRepository]
      val captor                   = ArgumentCaptor.forClass(classOf[UserAnswers])
      when(mockSubcontractorService.createAndUpdateSubcontractor(any[UserAnswers])(any[HeaderCarrier]))
        .thenReturn(Future.successful(()))
      when(mockSessionRepository.set(any[UserAnswers])).thenReturn(Future.successful(true))
      val application              =
        applicationBuilder(userAnswers = Some(minUa))
          .overrides(
            bind[SubcontractorService].toInstance(mockSubcontractorService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {

        val request =
          FakeRequest(POST, controllers.amend.routes.AmendIndividualCheckYourAnswersController.onSubmit().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.amend.routes.AmendIndividualCheckYourAnswersController
            .onPageLoad()
            .url // TODO: redirect to confirmation page
      }

      verify(mockSubcontractorService)
        .createAndUpdateSubcontractor(any[UserAnswers])(any[HeaderCarrier])
      verify(mockSessionRepository).set(captor.capture())

      captor.getValue.get(AmendCheckYourAnswersSubmittedPage) mustBe Some(true)
      verifyNoMoreInteractions(mockSubcontractorService)
    }

    "must redirect to Journey Recovery when the check your answers page has already been submitted" in {

      val ua = minUa
        .set(CheckYourAnswersSubmittedPage, true)
        .success
        .value

      val mockSubcontractorService = mock[SubcontractorService]

      val application =
        applicationBuilder(userAnswers = Some(ua))
          .overrides(
            bind[SubcontractorService].toInstance(mockSubcontractorService)
          )
          .build()

      running(application) {

        val request =
          FakeRequest(POST, controllers.amend.routes.AmendIndividualCheckYourAnswersController.onSubmit().url)

        val result = route(application, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe
          controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
      verifyNoInteractions(mockSubcontractorService)
    }

    "must redirect to Journey Recovery when the service fails" in {

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
          FakeRequest(POST, controllers.amend.routes.AmendIndividualCheckYourAnswersController.onSubmit().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          routes.JourneyRecoveryController.onPageLoad().url
      }

      verify(mockSubcontractorService)
        .createAndUpdateSubcontractor(any[UserAnswers])(any[HeaderCarrier])
    }

    "must redirect to Journey Recovery when POST validation fails" in {

      val invalidUa =
        emptyUserAnswers
          .set(TypeOfSubcontractorPage, TypeOfSubcontractor.Individualorsoletrader)
          .success
          .value

      val mockSubcontractorService = mock[SubcontractorService]
      val mockSessionRepository    = mock[SessionRepository]

      val application =
        applicationBuilder(userAnswers = Some(invalidUa))
          .overrides(
            bind[SubcontractorService].toInstance(mockSubcontractorService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {

        val request =
          FakeRequest(POST, controllers.amend.routes.AmendIndividualCheckYourAnswersController.onSubmit().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          routes.JourneyRecoveryController.onPageLoad().url
      }

      verify(mockSubcontractorService, never())
        .createAndUpdateSubcontractor(any[UserAnswers])(any[HeaderCarrier])
    }

    "must clear answers and redirect to Index on cancel" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any[UserAnswers]))
        .thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(minUa))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {

        val request =
          FakeRequest(GET, controllers.amend.routes.AmendIndividualCheckYourAnswersController.onCancel().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          routes.IndexController.onPageLoad().url
      }

      verify(mockSessionRepository).set(any[UserAnswers])
    }
  }
}
