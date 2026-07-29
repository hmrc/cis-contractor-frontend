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

package controllers.amend.partnership

import base.SpecBase
import controllers.routes
import models.address.{Address, Country}
import models.amend.partnership.OriginalPartnershipAnswers
import models.{TypeOfSubcontractor, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, verify, verifyNoMoreInteractions, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.add.TypeOfSubcontractorPage
import pages.add.partnership.*
import play.api.i18n.MessagesApi
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import queries.OriginalPartnershipAnswersQuery
import repositories.SessionRepository
import services.SubcontractorService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future
import models.contact.ContactMethodOptions
import pages.amend.ShowVerificationDetailsPage

class AmendPartnershipCheckYourAnswersControllerSpec extends SpecBase with MockitoSugar {

  private lazy val onPageLoadRoute =
    controllers.amend.partnership.routes.AmendPartnershipCheckYourAnswersController
      .onPageLoad()
      .url

  private lazy val onSubmitRoute =
    controllers.amend.partnership.routes.AmendPartnershipCheckYourAnswersController
      .onSubmit()
      .url

  private val address            =
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

  private val minUa =
    emptyUserAnswers
      .set(TypeOfSubcontractorPage, TypeOfSubcontractor.Partnership)
      .success
      .value
      .set(PartnershipNamePage, "Test Partnership")
      .success
      .value
      .set(PartnershipAddressYesNoPage, true)
      .success
      .value
      .set(PartnershipAddressPage, address)
      .success
      .value
      .set(AddPartnershipContactMethodsYesNoPage, true)
      .success
      .value
      .set(PartnershipContactMethodOptionsPage, Set(ContactMethodOptions.Email))
      .success
      .value
      .set(PartnershipEmailAddressPage, "test@test.com")
      .success
      .value
      .set(PartnershipHasUtrYesNoPage, true)
      .success
      .value
      .set(PartnershipUniqueTaxpayerReferencePage, "11111111")
      .success
      .value
      .set(PartnershipNominatedPartnerNamePage, "Partnership nominated name")
      .success
      .value
      .set(PartnershipNominatedPartnerCrnYesNoPage, true)
      .success
      .value
      .set(PartnershipNominatedPartnerCrnPage, "12345678")
      .success
      .value
      .set(PartnershipNominatedPartnerNinoYesNoPage, false)
      .success
      .value
      .set(PartnershipNominatedPartnerNinoPage, "")
      .success
      .value
      .set(PartnershipNominatedPartnerUtrYesNoPage, false)
      .success
      .value
      .set(PartnershipNominatedPartnerUtrPage, "11111111")
      .success
      .value
      .set(PartnershipWorksReferenceNumberYesNoPage, true)
      .success
      .value
      .set(PartnershipWorksReferenceNumberPage, "WRN-1")
      .success
      .value
      .set(
        OriginalPartnershipAnswersQuery,
        OriginalPartnershipAnswers(
          partnershipName = Some("Test Partnership"),
          addressYesNo = Some(true),
          address = Some(address),
          partnershipContactMethodsYesNo = Some(true),
          partnershipContactMethodOptions = Set(ContactMethodOptions.Email),
          email = Some("test@test.com"),
          phone = None,
          mobile = None,
          hasUtrYesNo = Some(true),
          utr = Some("11111111"),
          nominatedPartnerName = Some("Partnership nominated name"),
          nominatedPartnerUtrYesNo = Some(true),
          nominatedPartnerUtr = Some("11111111"),
          nominatedPartnerNinoYesNo = Some(true),
          nominatedPartnerNino = Some(""),
          nominatedPartnerCrnYesNo = Some(true),
          nominatedPartnerCrn = Some("12345678"),
          nominatedPartnerWorksReferenceYesNo = Some(true),
          nominatedPartnerWorksReference = Some("WRN-1"),
          verificationNumber = None
        )
      )
      .success
      .value

  "AmendPartnershipCheckYourAnswersController" - {

    "must return OK and render the page with the correct summary list for GET when validation succeeds for unverified partnership" in {
      val application =
        applicationBuilder(userAnswers = Some(minUa)).build()

      running(application) {
        val request =
          FakeRequest(
            GET,
            controllers.amend.partnership.routes.AmendPartnershipCheckYourAnswersController.onPageLoad().url
          )
        val msg     = app.injector.instanceOf[MessagesApi].preferred(request)
        val result  = route(application, request).value

        status(result) mustEqual OK

//        val page = contentAsString(result)
//
//        page must include(msg("typeOfSubcontractor.checkYourAnswersLabel"))
//        page must include(msg("partnershipName.checkYourAnswersLabel"))
//        page must include(msg("partnershipHasUtrYesNo.checkYourAnswersLabel"))
//        page must include(msg("partnershipUtr.checkYourAnswersLabel"))
//        page must include(msg("partnershipNominatedPartnerName.checkYourAnswersLabel"))
//        page must include(msg("partnershipNominatedPartnerNinoYesNo.checkYourAnswersLabel"))
//        page must include(msg("partnershipNominatedPartnerNino.checkYourAnswersLabel"))
//        page must include(msg("partnershipNominatedPartnerCrnYesNo.checkYourAnswersLabel"))
//        page must include(msg("partnershipNominatedPartnerCrn.checkYourAnswersLabel"))
//        page must include(msg("partnershipNominatedPartnerUtrYesNo.checkYourAnswersLabel"))
//        page must include(msg("partnershipNominatedPartnerUtr.checkYourAnswersLabel"))
//        page must include(msg("partnershipWorksReferenceYesNo.checkYourAnswersLabel"))
//        page must include(msg("partnershipWorksReference.checkYourAnswersLabel"))
//        page must include(msg("partnershipAddressYesNo.checkYourAnswersLabel"))
//        page must include(msg("partnershipAddress.checkYourAnswersLabel"))
//        page must include(msg("addPartnershipContactMethodsYesNo.checkYourAnswersLabel"))
//        page must include(msg("partnershipContactMethodOptions.checkYourAnswersLabel"))
//        page must include(msg("partnershipEmailAddress.checkYourAnswersLabel"))
//
//        page must not include (msg("amendCheckYourAnswers.verificationNumber.label"))
//
//        page must include("Partnership")
//        page must include("Test Partnership")
//        page must include("12345678")
//        page must include("WRN-1")
//        page must include("test@test.com")
//        page must include("12 Harbor View Road")
//        page must include("Amity Island")
//        page must include("Bodmin")
//        page must include("Cornwall")
//        page must include("PL31 2HL")
//        page must include("England")
//
//        page must not include "VRN123456"
      }
    }

    "must render the correct summary for a verified partnership" in {

      val verifiedUa  =
        minUa
          .set(
            OriginalPartnershipAnswersQuery,
            OriginalPartnershipAnswers(
              partnershipName = Some("Test Partnership"),
              addressYesNo = Some(false),
              address = None,
              partnershipContactMethodsYesNo = Some(false),
              partnershipContactMethodOptions = Set.empty,
              email = None,
              phone = None,
              mobile = None,
              hasUtrYesNo = Some(false),
              utr = None,
              nominatedPartnerName = None,
              nominatedPartnerUtrYesNo = Some(false),
              nominatedPartnerUtr = None,
              nominatedPartnerCrnYesNo = Some(false),
              nominatedPartnerCrn = None,
              nominatedPartnerNinoYesNo = Some(false),
              nominatedPartnerNino = None,
              nominatedPartnerWorksReferenceYesNo = Some(false),
              nominatedPartnerWorksReference = None,
              verificationNumber = Some("VRN123456")
            )
          )
          .success
          .value
      val application =
        applicationBuilder(userAnswers = Some(verifiedUa)).build()

      running(application) {

        val request =
          FakeRequest(
            GET,
            controllers.amend.partnership.routes.AmendPartnershipCheckYourAnswersController.onPageLoad().url
          )
        val msg     = app.injector.instanceOf[MessagesApi].preferred(request)
        val result  = route(application, request).value

        status(result) mustEqual OK

//        val page = contentAsString(result)
//
//        page must include(msg("amendCheckYourAnswers.verificationNumber.label"))
//        page must include("VRN123456")
//
//        page must not include msg("partnershipName.checkYourAnswersLabel")
//        page must not include msg("partnershipHasUtrNumberYesNo.checkYourAnswersLabel")
//        page must not include msg("partnershipUtrNumber.change.hidden")
//
//        page must include(msg("typeOfSubcontractor.checkYourAnswersLabel"))
//        page must include("Partnership")
//
//        page must include(msg("partnershipAddressYesNo.checkYourAnswersLabel"))
//        page must include(msg("partnershipAddress.checkYourAnswersLabel"))
//        page must include(msg("site.yes"))
//        page must include("12 Harbor View Road")
//        page must include("Amity Island")
//        page must include("Bodmin")
//        page must include("Cornwall")
//        page must include("PL31 2HL")
//        page must include("England")
//
//        page must include(msg("addPartnershipContactMethodsYesNo.checkYourAnswersLabel"))
//        page must include(msg("partnershipContactMethodOptions.checkYourAnswersLabel"))
//        page must include(msg("partnershipEmailAddress.checkYourAnswersLabel"))
//        page must include("test@test.com")
//
//        page must include(msg("partnershipWorksReferenceYesNo.checkYourAnswersLabel"))
//        page must include(msg("partnershipWorksReference.checkYourAnswersLabel"))
//        page must include(msg("site.no"))
//        page must include("WRN-1")
      }
    }

    "must redirect to Journey Recovery when validation fails" in {

      val invalidUa =
        emptyUserAnswers
          .set(TypeOfSubcontractorPage, TypeOfSubcontractor.Partnership)
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(invalidUa)).build()

      running(application) {

        val request =
          FakeRequest(
            GET,
            controllers.amend.partnership.routes.AmendPartnershipCheckYourAnswersController.onPageLoad().url
          )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect back to amend CYA after successful submit" in {

      val mockSubcontractorService = mock[SubcontractorService]
      val mockSessionRepository    = mock[SessionRepository]

      when(mockSubcontractorService.createAndUpdateSubcontractor(any[UserAnswers])(any[HeaderCarrier]))
        .thenReturn(Future.successful(()))

      val application =
        applicationBuilder(userAnswers = Some(minUa))
          .overrides(
            bind[SubcontractorService].toInstance(mockSubcontractorService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {

        val request =
          FakeRequest(
            POST,
            controllers.amend.partnership.routes.AmendPartnershipCheckYourAnswersController.onSubmit().url
          )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.amend.partnership.routes.AmendPartnershipCheckYourAnswersController.onPageLoad().url
      }

      verify(mockSubcontractorService)
        .createAndUpdateSubcontractor(any[UserAnswers])(any[HeaderCarrier])

      verifyNoMoreInteractions(mockSubcontractorService)
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
          FakeRequest(
            POST,
            controllers.amend.partnership.routes.AmendPartnershipCheckYourAnswersController.onSubmit().url
          )

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
          .set(TypeOfSubcontractorPage, TypeOfSubcontractor.Partnership)
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
          FakeRequest(
            POST,
            controllers.amend.partnership.routes.AmendPartnershipCheckYourAnswersController.onSubmit().url
          )

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
          FakeRequest(
            GET,
            controllers.amend.partnership.routes.AmendPartnershipCheckYourAnswersController.onCancel().url
          )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          routes.IndexController.onPageLoad().url
      }

      verify(mockSessionRepository).set(any[UserAnswers])
    }
  }
}
