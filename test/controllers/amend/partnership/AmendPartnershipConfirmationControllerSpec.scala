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
import config.FrontendAppConfig
import models.UserAnswers
import models.amend.AmendJourneyType
import models.amend.partnership.OriginalPartnershipAnswers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.add.partnership.PartnershipNamePage
import pages.amend.{AmendCheckYourAnswersSubmittedPage, AmendJourneyTypePage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import queries.{CisIdQuery, OriginalPartnershipAnswersQuery}
import repositories.SessionRepository
import services.VerificationService
import utils.DefaultSubcontractorCleanupService
import viewmodels.amend.AmendConfirmationLinks
import viewmodels.checkAnswers.amend.partnership.AmendPartnershipConfirmationViewModel
import views.html.amend.AmendConfirmationView

import scala.concurrent.Future
import scala.util.{Failure, Success}

class AmendPartnershipConfirmationControllerSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {
  private val cisId           = "123456789"
  private val partnershipName = "ABC Partnership"

  private val mockCleanupService =
    mock[DefaultSubcontractorCleanupService]

  private val mockSessionRepository =
    mock[SessionRepository]

  private val mockVerificationService =
    mock[VerificationService]

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockCleanupService, mockSessionRepository, mockVerificationService)
  }

  private def application(userAnswers: UserAnswers) =
    applicationBuilder(userAnswers = Some(userAnswers))
      .overrides(
        bind[DefaultSubcontractorCleanupService]
          .toInstance(mockCleanupService),
        bind[SessionRepository]
          .toInstance(mockSessionRepository),
        bind[VerificationService]
          .toInstance(mockVerificationService)
      )
      .build()

  private val original =
    OriginalPartnershipAnswers(
      partnershipName = Some(partnershipName),
      addressYesNo = None,
      address = None,
      partnershipContactMethodsYesNo = None,
      partnershipContactMethodOptions = Set.empty,
      email = None,
      phone = None,
      mobile = None,
      hasUtrYesNo = None,
      utr = None,
      nominatedPartnerName = None,
      nominatedPartnerUtrYesNo = None,
      nominatedPartnerUtr = None,
      nominatedPartnerNinoYesNo = None,
      nominatedPartnerNino = None,
      nominatedPartnerCrnYesNo = None,
      nominatedPartnerCrn = None,
      nominatedPartnerWorksReferenceYesNo = None,
      nominatedPartnerWorksReference = None,
      verificationNumber = None
    )

  private def userAnswersWithOriginal =
    emptyUserAnswers
      .set(OriginalPartnershipAnswersQuery, original)
      .success
      .value
      .set(CisIdQuery, cisId)
      .success
      .value
      .set(PartnershipNamePage, partnershipName)
      .success
      .value
      .set(AmendJourneyTypePage, AmendJourneyType.Standard)
      .success
      .value
      .set(AmendCheckYourAnswersSubmittedPage, true)
      .success
      .value
  private lazy val confirmationRoute  =
    controllers.amend.partnership.routes.AmendPartnershipConfirmationController.onPageLoad().url

  "AmendPartnershipConfirmationController" - {

    "must return OK and the correct view for a GET" in {

      when(mockCleanupService.cleanAmend(any[UserAnswers]))
        .thenReturn(Success(userAnswersWithOriginal))

      when(mockSessionRepository.set(any[UserAnswers]))
        .thenReturn(Future.successful(true))

      val app = this.application(userAnswersWithOriginal)

      running(app) {

        val request = FakeRequest(GET, confirmationRoute)
        val result  = route(app, request).value

        val view = app.injector.instanceOf[AmendConfirmationView]

        status(result) mustEqual OK

        val confirmationLink =
          AmendConfirmationLinks.build(
            AmendJourneyType.Standard,
            cisId,
            app.injector.instanceOf[FrontendAppConfig]
          )

        contentAsString(result) mustEqual
          view(
            AmendPartnershipConfirmationViewModel.rows(
              original,
              userAnswersWithOriginal
            )(messages(app)),
            partnershipName,
            confirmationLink
          )(
            request,
            messages(app)
          ).toString

        verify(mockCleanupService).cleanAmend(any())
        verify(mockSessionRepository).set(any())
      }
    }

    "must redirect to Journey Recovery when not submitted" in {

      val userAnswers =
        emptyUserAnswers
          .set(CisIdQuery, cisId)
          .success
          .value
          .set(PartnershipNamePage, partnershipName)
          .success
          .value
          .set(AmendCheckYourAnswersSubmittedPage, false)
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {

        val request = FakeRequest(GET, confirmationRoute)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery when the original answers are missing" in {

      val userAnswers =
        emptyUserAnswers
          .set(CisIdQuery, cisId)
          .success
          .value
          .set(PartnershipNamePage, partnershipName)
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {

        val request = FakeRequest(GET, confirmationRoute)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery when the CIS id is missing" in {

      val userAnswers =
        emptyUserAnswers
          .set(OriginalPartnershipAnswersQuery, original)
          .success
          .value
          .set(PartnershipNamePage, partnershipName)
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {

        val request = FakeRequest(GET, confirmationRoute)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery when cleanup fails" in {

      when(mockCleanupService.cleanAmend(any[UserAnswers]))
        .thenReturn(Failure(new RuntimeException("cleanup failed")))

      val app = application(userAnswersWithOriginal)

      running(app) {

        val request = FakeRequest(GET, confirmationRoute)
        val result  = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.routes.JourneyRecoveryController.onPageLoad().url

        verify(mockSessionRepository, never()).set(any())
      }
    }

    "must redirect to Journey Recovery when AmendJourneyTypePage is missing" in {

      val userAnswers =
        emptyUserAnswers
          .set(OriginalPartnershipAnswersQuery, original)
          .success
          .value
          .set(CisIdQuery, cisId)
          .success
          .value
          .set(PartnershipNamePage, partnershipName)
          .success
          .value
          .set(AmendCheckYourAnswersSubmittedPage, true)
          .success
          .value

      val app = application(userAnswers)

      running(app) {

        val request = FakeRequest(GET, confirmationRoute)
        val result  = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.routes.JourneyRecoveryController
            .onPageLoad()
            .url

        verifyNoInteractions(mockCleanupService)
        verifyNoInteractions(mockSessionRepository)
      }
    }

    "must refresh verification batches and render the insufficient info confirmation link" in {

      when(mockCleanupService.cleanAmend(any[UserAnswers]))
        .thenReturn(Success(userAnswersWithOriginal))

      when(
        mockVerificationService
          .refreshVerificationBatches(any[UserAnswers])(any())
      ).thenReturn(
        Future.successful(userAnswersWithOriginal)
      )

      val userAnswers =
        userAnswersWithOriginal
          .set(
            AmendJourneyTypePage,
            AmendJourneyType.InsufficientInfo
          )
          .success
          .value

      val app = application(userAnswers)

      running(app) {

        val request = FakeRequest(GET, confirmationRoute)
        val result  = route(app, request).value

        status(result) mustEqual OK

        contentAsString(result) must include(
          controllers.verify.routes.ReviewInsufficientInfoSubcontractorsController
            .onPageLoad()
            .url
        )

        contentAsString(result) must not include
          messages(app)(
            "amendConfirmation.beforeYouGo.h2"
          )

        verify(mockVerificationService)
          .refreshVerificationBatches(any[UserAnswers])(any())
      }
    }

    "must refresh verification batches and render the unmatched info confirmation link" in {

      when(mockCleanupService.cleanAmend(any[UserAnswers]))
        .thenReturn(Success(userAnswersWithOriginal))

      when(
        mockVerificationService
          .refreshVerificationBatches(any[UserAnswers])(any())
      ).thenReturn(
        Future.successful(userAnswersWithOriginal)
      )

      val userAnswers =
        userAnswersWithOriginal
          .set(
            AmendJourneyTypePage,
            AmendJourneyType.UnmatchedInfo
          )
          .success
          .value

      val app = application(userAnswers)

      running(app) {

        val request = FakeRequest(GET, confirmationRoute)
        val result  = route(app, request).value

        status(result) mustEqual OK

        contentAsString(result) must include(
          controllers.verify.routes.ReviewUnmatchedSubcontractorsRoutingController
            .onPageLoad()
            .url
        )

        contentAsString(result) must not include
          messages(app)(
            "amendConfirmation.beforeYouGo.h2"
          )

        verify(mockVerificationService)
          .refreshVerificationBatches(any[UserAnswers])(any())
      }
    }

    "must not refresh verification batches for standard amend journey" in {

      when(mockCleanupService.cleanAmend(any[UserAnswers]))
        .thenReturn(Success(userAnswersWithOriginal))

      when(mockSessionRepository.set(any[UserAnswers]))
        .thenReturn(Future.successful(true))

      val app = application(userAnswersWithOriginal)

      running(app) {

        val request = FakeRequest(GET, confirmationRoute)
        val result  = route(app, request).value

        status(result) mustBe OK

        verifyNoInteractions(mockVerificationService)
      }
    }
  }
}
