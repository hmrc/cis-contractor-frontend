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
import models.contact.ContactMethodOptions
import models.{TypeOfSubcontractor, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.scalatestplus.mockito.MockitoSugar
import pages.add.TypeOfSubcontractorPage
import pages.add.partnership.*
import pages.amend.{AmendCheckYourAnswersSubmittedPage, AmendJourneyTypePage, ShowVerificationDetailsPage}
import play.api.i18n.MessagesApi
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import queries.{CisIdQuery, OriginalPartnershipAnswersQuery}
import repositories.SessionRepository
import services.{AuditService, SubcontractorService}
import uk.gov.hmrc.http.HeaderCarrier
import utils.AmendmentHelper
import config.FrontendAppConfig
import models.amend.AmendJourneyType

import scala.concurrent.Future

class AmendPartnershipCheckYourAnswersControllerSpec extends SpecBase with MockitoSugar {

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
      .set(PartnershipNominatedPartnerNinoYesNoPage, true)
      .success
      .value
      .set(PartnershipNominatedPartnerNinoPage, "AC123456")
      .success
      .value
      .set(PartnershipNominatedPartnerUtrYesNoPage, true)
      .success
      .value
      .set(PartnershipNominatedPartnerUtrPage, "11111111")
      .success
      .value
      .set(PartnershipWorksReferenceNumberYesNoPage, true)
      .success
      .value
      .set(PartnershipWorksReferenceNumberPage, "WRN-11")
      .success
      .value
      .set(ShowVerificationDetailsPage, false)
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
          nominatedPartnerNino = Some("AC123456"),
          nominatedPartnerCrnYesNo = Some(true),
          nominatedPartnerCrn = Some("12345678"),
          nominatedPartnerWorksReferenceYesNo = Some(true),
          nominatedPartnerWorksReference = Some("WRN-1"),
          verificationNumber = None
        )
      )
      .success
      .value
      .set(
        AmendJourneyTypePage,
        AmendJourneyType.Standard
      )
      .success
      .value
      .set(
        CisIdQuery,
        "cis-123"
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

        val page = contentAsString(result)

        page must include(msg("typeOfSubcontractor.checkYourAnswersLabel"))
        page must include(msg("partnershipName.checkYourAnswersLabel"))
        page must include(msg("partnershipHasUtrYesNo.checkYourAnswersLabel"))
        page must include(msg("partnershipUniqueTaxpayerReference.checkYourAnswersLabel"))
        page must include(msg("partnershipNominatedPartnerName.checkYourAnswersLabel"))
        page must include(msg("partnershipNominatedPartnerNinoYesNo.checkYourAnswersLabel"))
        page must include(msg("partnershipNominatedPartnerNino.checkYourAnswersLabel"))
        page must include(msg("partnershipNominatedPartnerCrnYesNo.checkYourAnswersLabel"))
        page must include(msg("partnershipNominatedPartnerCrn.checkYourAnswersLabel"))
        page must include(msg("partnershipNominatedPartnerUtrYesNo.checkYourAnswersLabel"))
        page must include(msg("partnershipNominatedPartnerUtr.checkYourAnswersLabel"))
        page must include(msg("partnershipWorksReferenceNumberYesNo.checkYourAnswersLabel"))
        page must include(msg("partnershipWorksReferenceNumber.checkYourAnswersLabel"))
        page must include(msg("partnershipAddressYesNo.checkYourAnswersLabel"))
        page must include(msg("partnershipAddress.checkYourAnswersLabel"))
        page must include(msg("addPartnershipContactMethodsYesNo.checkYourAnswersLabel"))
        page must include(msg("partnershipContactMethodOptions.checkYourAnswersLabel"))
        page must include(msg("partnershipEmailAddress.checkYourAnswersLabel"))

        page must not include msg("amendCheckYourAnswers.verificationNumber.label")

        page must include("Partnership")
        page must include("Test Partnership")
        page must include("12345678")
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

    "must render the correct summary for a verified partnership" in {

      val verifiedUa  =
        minUa
          .set(ShowVerificationDetailsPage, true)
          .success
          .value
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

        val page = contentAsString(result)

        page must include(msg("amendCheckYourAnswers.verificationNumber.label"))
        page must include("VRN123456")

        page must not include msg("partnershipName.checkYourAnswersLabel")
        page must not include msg("partnershipHasUtrNumberYesNo.checkYourAnswersLabel")
        page must not include msg("partnershipUniqueTaxpayerReference.change.hidden")
        page must not include msg("partnershipNominatedPartnerUtrYesNo.checkYourAnswersLabel")
        page must not include msg("partnershipNominatedPartnerUtr.change.hidden")

        page must include(msg("typeOfSubcontractor.checkYourAnswersLabel"))
        page must include("Partnership")

        page must include(msg("partnershipAddressYesNo.checkYourAnswersLabel"))
        page must include(msg("partnershipAddress.checkYourAnswersLabel"))
        page must include(msg("site.yes"))
        page must include("12 Harbor View Road")
        page must include("Amity Island")
        page must include("Bodmin")
        page must include("Cornwall")
        page must include("PL31 2HL")
        page must include("England")

        page must include(msg("addPartnershipContactMethodsYesNo.checkYourAnswersLabel"))
        page must include(msg("partnershipContactMethodOptions.checkYourAnswersLabel"))
        page must include(msg("partnershipEmailAddress.checkYourAnswersLabel"))
        page must include("test@test.com")

        page must include(msg("partnershipNominatedPartnerCrnYesNo.checkYourAnswersLabel"))
        page must include(msg("partnershipNominatedPartnerCrn.checkYourAnswersLabel"))
        page must include(msg("partnershipNominatedPartnerNinoYesNo.checkYourAnswersLabel"))
        page must include(msg("partnershipNominatedPartnerNino.checkYourAnswersLabel"))
        page must include(msg("partnershipWorksReferenceNumberYesNo.checkYourAnswersLabel"))
        page must include(msg("partnershipWorksReferenceNumber.checkYourAnswersLabel"))
        page must include(msg("site.no"))
        page must include("WRN-1")
      }
    }

    "must not render the verification number row when the partnership is pending verifications" in {

      val verifiedUa =
        minUa
          .set(ShowVerificationDetailsPage, true)
          .success
          .value
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
              verificationNumber = None
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

        val msg = application.injector.instanceOf[MessagesApi].preferred(request)

        val result = route(application, request).value

        status(result) mustEqual OK

        val page = contentAsString(result)

        page must not include msg("amendCheckYourAnswers.verificationNumber.label")
        page must not include "VRN123456"
        page must include(msg("partnershipUniqueTaxpayerReference.verified.checkYourAnswersLabel"))
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
      val mockAuditService         = mock[AuditService]
      val captor                   = ArgumentCaptor.forClass(classOf[UserAnswers])
      when(
        mockSubcontractorService.submitAmendSubcontractor(
          any[AmendJourneyType],
          any[UserAnswers],
          any[Option[Long]]
        )(any[HeaderCarrier])
      ).thenReturn(
        Future.successful(())
      )
      when(mockSessionRepository.set(any[UserAnswers])).thenReturn(Future.successful(true))
      val application              =
        applicationBuilder(userAnswers = Some(minUa))
          .overrides(
            bind[SubcontractorService].toInstance(mockSubcontractorService),
            bind[AuditService].toInstance(mockAuditService),
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
          controllers.amend.partnership.routes.AmendPartnershipConfirmationController.onPageLoad().url
      }

      verify(mockSubcontractorService)
        .submitAmendSubcontractor(
          any[AmendJourneyType],
          any[UserAnswers],
          any[Option[Long]]
        )(any[HeaderCarrier])
      verify(mockAuditService).amendSubcontractorEvent(any[UserAnswers])(any[HeaderCarrier])
      verify(mockSessionRepository).set(captor.capture())
      captor.getValue.get(AmendCheckYourAnswersSubmittedPage) mustBe Some(true)

      verifyNoMoreInteractions(mockSubcontractorService, mockSessionRepository)
    }

    "must redirect to Journey Recovery when the check your answers page has already been submitted" in {

      val ua = minUa
        .set(AmendCheckYourAnswersSubmittedPage, true)
        .success
        .value

      val mockSubcontractorService = mock[SubcontractorService]
      val mockAuditService         = mock[AuditService]

      val application =
        applicationBuilder(userAnswers = Some(ua))
          .overrides(
            bind[SubcontractorService].toInstance(mockSubcontractorService),
            bind[AuditService].toInstance(mockAuditService)
          )
          .build()

      running(application) {

        val request =
          FakeRequest(
            POST,
            controllers.amend.partnership.routes.AmendPartnershipCheckYourAnswersController.onSubmit().url
          )

        val result = route(application, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe
          controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
      verifyNoInteractions(mockSubcontractorService)
    }

    "must clear answers and redirect to Manage Your Subcontractors when no changes have been made" in {
      val cisId = "cis-123"

      val ua =
        minUa
          .set(CisIdQuery, cisId)
          .success
          .value
          .set(PartnershipWorksReferenceNumberPage, "WRN-1")
          .success
          .value

      val mockSubcontractorService = mock[SubcontractorService]
      val mockSessionRepository    = mock[SessionRepository]
      val mockAuditService         = mock[AuditService]

      when(mockSessionRepository.set(any[UserAnswers]))
        .thenReturn(Future.successful(true))

      AmendmentHelper.partnershipHasChanges(ua) mustBe false

      val application =
        applicationBuilder(userAnswers = Some(ua))
          .configure(
            "urls.manage-your-subcontractors" ->
              s"http://localhost:6996/construction-industry-scheme/management/subcontractors/$cisId/your-subcontractors"
          )
          .overrides(
            bind[SubcontractorService].toInstance(mockSubcontractorService),
            bind[AuditService].toInstance(mockAuditService),
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

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe
          "http://localhost:6996/construction-industry-scheme/management/subcontractors/cis-123/your-subcontractors"
      }

      verifyNoInteractions(mockSubcontractorService)

      val captor = ArgumentCaptor.forClass(classOf[UserAnswers])
      verify(mockSessionRepository).set(captor.capture())

      captor.getValue.id mustBe ua.id
      captor.getValue.get(CisIdQuery) mustBe None
    }

    "must clear answers and redirect to ReviewInsufficientInfoSubcontractorsController when no changes have been made in an insufficient information journey" in {

      val ua =
        minUa
          .set(
            AmendJourneyTypePage,
            AmendJourneyType.InsufficientInfo
          )
          .success
          .value
          .set(PartnershipWorksReferenceNumberPage, "WRN-1")
          .success
          .value

      val mockSubcontractorService = mock[SubcontractorService]
      val mockSessionRepository = mock[SessionRepository]
      val mockAuditService = mock[AuditService]

      when(mockSessionRepository.set(any[UserAnswers]))
        .thenReturn(Future.successful(true))

      AmendmentHelper.partnershipHasChanges(ua) mustBe false

      val application =
        applicationBuilder(userAnswers = Some(ua))
          .overrides(
            bind[SubcontractorService].toInstance(mockSubcontractorService),
            bind[AuditService].toInstance(mockAuditService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {

        val request =
          FakeRequest(
            POST,
            controllers.amend.partnership.routes.AmendPartnershipCheckYourAnswersController
              .onSubmit()
              .url
          )

        val result = route(application, request).value

        status(result) mustBe SEE_OTHER

        redirectLocation(result).value mustBe
          controllers.verify.routes
            .ReviewInsufficientInfoSubcontractorsController
            .onPageLoad()
            .url
      }

      verifyNoInteractions(mockSubcontractorService)

      val captor = ArgumentCaptor.forClass(classOf[UserAnswers])

      verify(mockSessionRepository).set(captor.capture())

      captor.getValue.id mustBe ua.id
    }

    "must clear answers and redirect to ReviewUnmatchedSubcontractorsRoutingController when no changes have been made in an unmatched journey" in {

      val ua =
        minUa
          .set(
            AmendJourneyTypePage,
            AmendJourneyType.UnmatchedInfo
          )
          .success
          .value
          .set(PartnershipWorksReferenceNumberPage, "WRN-1")
          .success
          .value

      val mockSubcontractorService = mock[SubcontractorService]
      val mockSessionRepository = mock[SessionRepository]
      val mockAuditService = mock[AuditService]

      when(mockSessionRepository.set(any[UserAnswers]))
        .thenReturn(Future.successful(true))

      AmendmentHelper.partnershipHasChanges(ua) mustBe false

      val application =
        applicationBuilder(userAnswers = Some(ua))
          .overrides(
            bind[SubcontractorService].toInstance(mockSubcontractorService),
            bind[AuditService].toInstance(mockAuditService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {

        val request =
          FakeRequest(
            POST,
            controllers.amend.partnership.routes.AmendPartnershipCheckYourAnswersController
              .onSubmit()
              .url
          )

        val result = route(application, request).value

        status(result) mustBe SEE_OTHER

        redirectLocation(result).value mustBe
          controllers.verify.routes
            .ReviewUnmatchedSubcontractorsRoutingController
            .onPageLoad()
            .url
      }

      verifyNoInteractions(mockSubcontractorService)

      val captor = ArgumentCaptor.forClass(classOf[UserAnswers])

      verify(mockSessionRepository).set(captor.capture())

      captor.getValue.id mustBe ua.id
    }

    "must redirect to Journey Recovery when the service fails" in {

      val mockSubcontractorService = mock[SubcontractorService]
      val mockSessionRepository    = mock[SessionRepository]
      val mockAuditService         = mock[AuditService]

      when(mockSessionRepository.set(any[UserAnswers]))
        .thenReturn(Future.successful(true))

      when(
        mockSubcontractorService.submitAmendSubcontractor(
          any[AmendJourneyType],
          any[UserAnswers],
          any[Option[Long]]
        )(any[HeaderCarrier])
      ).thenReturn(
        Future.failed(
          new RuntimeException("boom")
        )
      )

      val application =
        applicationBuilder(userAnswers = Some(minUa))
          .overrides(
            bind[SubcontractorService].toInstance(mockSubcontractorService),
            bind[AuditService].toInstance(mockAuditService),
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
        .submitAmendSubcontractor(
          any[AmendJourneyType],
          any[UserAnswers],
          any[Option[Long]]
        )(any[HeaderCarrier])
    }

    "must redirect to Journey Recovery and not update subcontractor when saving submitted marker fails" in {

      val mockSubcontractorService = mock[SubcontractorService]
      val mockSessionRepository    = mock[SessionRepository]

      when(mockSessionRepository.set(any[UserAnswers]))
        .thenReturn(Future.failed(new RuntimeException("session write failed")))

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

      verify(mockSessionRepository).set(any[UserAnswers])
      verifyNoInteractions(mockSubcontractorService)
    }

    "must redirect to Journey Recovery when AmendJourneyTypePage is missing on submit" in {

      val ua =
        minUa
          .remove(AmendJourneyTypePage)
          .success
          .value

      val mockSubcontractorService =
        mock[SubcontractorService]

      val application =
        applicationBuilder(userAnswers = Some(ua))
          .overrides(
            bind[SubcontractorService]
              .toInstance(mockSubcontractorService)
          )
          .build()

      running(application) {

        val request =
          FakeRequest(
            POST,
            controllers.amend.partnership.routes.AmendPartnershipCheckYourAnswersController
              .onSubmit()
              .url
          )

        val result =
          route(application, request).value

        status(result) mustBe SEE_OTHER

        redirectLocation(result).value mustBe
          routes.JourneyRecoveryController
            .onPageLoad()
            .url
      }

      verifyNoInteractions(mockSubcontractorService)
    }

    "must redirect to Journey Recovery when POST validation fails" in {

      val invalidUa =
        emptyUserAnswers
          .set(TypeOfSubcontractorPage, TypeOfSubcontractor.Partnership)
          .success
          .value
          .set(
            CisIdQuery,
            "cis-123"
          )
          .success
          .value

      val mockSubcontractorService = mock[SubcontractorService]
      val mockSessionRepository    = mock[SessionRepository]
      val mockAuditService         = mock[AuditService]

      val application =
        applicationBuilder(userAnswers = Some(invalidUa))
          .overrides(
            bind[SubcontractorService].toInstance(mockSubcontractorService),
            bind[AuditService].toInstance(mockAuditService),
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

      verify(
        mockSubcontractorService,
        never()
      ).updateSubcontractor(
        any[UserAnswers],
        any[Option[Long]]
      )(any[HeaderCarrier])
    }

    "must clear answers and redirect to Index on cancel" in {

      val ua                    =
        minUa
          .set(CisIdQuery, "cis-123")
          .success
          .value
      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any[UserAnswers]))
        .thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(ua))
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
          app.injector
            .instanceOf[FrontendAppConfig]
            .manageYourSubcontractorsUrl("cis-123")
      }

      verify(mockSessionRepository).set(any[UserAnswers])
    }
  }
}
