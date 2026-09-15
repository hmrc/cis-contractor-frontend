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

package controllers.amend.trust

import base.SpecBase
import config.FrontendAppConfig
import models.UserAnswers
import models.amend.AmendJourneyType
import models.amend.trust.OriginalTrustAnswers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import pages.add.trust.TrustNamePage
import pages.amend.{AmendCheckYourAnswersSubmittedPage, AmendJourneyTypePage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import queries.{CisIdQuery, OriginalTrustAnswersQuery}
import repositories.SessionRepository
import services.VerificationService
import scala.concurrent.Future
import viewmodels.amend.AmendConfirmationLinks
import viewmodels.amend.trust.TrustAmendConfirmationViewModel
import views.html.amend.AmendConfirmationView

class AmendTrustConfirmationControllerSpec extends SpecBase {

  private val cisId = "123456789"

  private val trustName = "ABC Trust"

  private val original =
    OriginalTrustAnswers(
      trustName = Some(trustName),
      addressYesNo = None,
      address = None,
      trustContactMethodsYesNo = None,
      trustContactMethod = Set.empty,
      email = None,
      phone = None,
      mobile = None,
      utrYesNo = None,
      utr = None,
      worksReferenceYesNo = None,
      worksReference = None,
      verificationNumber = None
    )

  private def userAnswersWithOriginal: UserAnswers =
    emptyUserAnswers
      .set(OriginalTrustAnswersQuery, original)
      .success
      .value
      .set(CisIdQuery, cisId)
      .success
      .value
      .set(TrustNamePage, trustName)
      .success
      .value
      .set(AmendJourneyTypePage, AmendJourneyType.Standard)
      .success
      .value
      .set(AmendCheckYourAnswersSubmittedPage, true)
      .success
      .value

  private lazy val confirmationRoute =
    controllers.amend.trust.routes.AmendTrustConfirmationController
      .onPageLoad()
      .url

  private val mockSessionRepository =
    mock[SessionRepository]

  private val mockVerificationService =
    mock[VerificationService]

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockSessionRepository, mockVerificationService)
  }

  private def application(userAnswers: UserAnswers) =
    applicationBuilder(userAnswers = Some(userAnswers))
      .overrides(
        bind[SessionRepository]
          .toInstance(mockSessionRepository),
        bind[VerificationService]
          .toInstance(mockVerificationService)
      )
      .build()

  "AmendTrustConfirmationController" - {

    "must return OK and the correct view for a GET" in {

      when(mockSessionRepository.set(any[UserAnswers]))
        .thenReturn(Future.successful(true))

      val app = application(userAnswersWithOriginal)

      running(app) {

        val request = FakeRequest(GET, confirmationRoute)
        val result  = route(app, request).value

        val view =
          app.injector.instanceOf[AmendConfirmationView]

        status(result) mustEqual OK

        val confirmationLink =
          AmendConfirmationLinks.build(
            AmendJourneyType.Standard,
            cisId,
            app.injector.instanceOf[FrontendAppConfig]
          )

        contentAsString(result) mustEqual
          view(
            TrustAmendConfirmationViewModel.rows(
              original,
              userAnswersWithOriginal
            )(messages(app)),
            trustName,
            confirmationLink
          )(request, messages(app)).toString

        verify(mockSessionRepository)
          .set(any[UserAnswers])

        verifyNoInteractions(mockVerificationService)
      }
    }

    "must redirect to Journey Recovery when accessed without prior CYA submission" in {

      val userAnswers =
        emptyUserAnswers
          .set(OriginalTrustAnswersQuery, original)
          .success
          .value
          .set(CisIdQuery, cisId)
          .success
          .value
          .set(TrustNamePage, trustName)
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

        verifyNoInteractions(mockSessionRepository)
      }
    }

    "must redirect to Journey Recovery when the original answers are missing" in {

      val userAnswers =
        emptyUserAnswers
          .set(CisIdQuery, cisId)
          .success
          .value
          .set(TrustNamePage, trustName)
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

        verifyNoInteractions(mockSessionRepository)
      }
    }

    "must redirect to Journey Recovery when the CIS id is missing" in {

      val userAnswers =
        emptyUserAnswers
          .set(OriginalTrustAnswersQuery, original)
          .success
          .value
          .set(TrustNamePage, trustName)
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

        verifyNoInteractions(mockSessionRepository)
      }
    }

    "must redirect to Journey Recovery when AmendJourneyTypePage is missing" in {

      val userAnswers =
        emptyUserAnswers
          .set(OriginalTrustAnswersQuery, original)
          .success
          .value
          .set(CisIdQuery, cisId)
          .success
          .value
          .set(TrustNamePage, trustName)
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

        verifyNoInteractions(mockSessionRepository)
      }
    }

    "must refresh verification batches and render the insufficient info confirmation link" in {

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

        verifyNoInteractions(mockSessionRepository)
      }
    }

    "must refresh verification batches and render the unmatched info confirmation link" in {

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
          controllers.verify.routes.ReviewUnmatchedInfoSubcontractorsController
            .onPageLoad()
            .url
        )

        contentAsString(result) must not include
          messages(app)(
            "amendConfirmation.beforeYouGo.h2"
          )

        verify(mockVerificationService)
          .refreshVerificationBatches(any[UserAnswers])(any())

        verifyNoInteractions(mockSessionRepository)
      }
    }

    "must not refresh verification batches for standard amend journey" in {

      when(mockSessionRepository.set(any[UserAnswers]))
        .thenReturn(Future.successful(true))

      val app = application(userAnswersWithOriginal)

      running(app) {

        val request =
          FakeRequest(GET, confirmationRoute)

        val result =
          route(app, request).value

        status(result) mustBe OK

        verify(mockSessionRepository)
          .set(any[UserAnswers])

        verifyNoInteractions(mockVerificationService)
      }
    }
  }
}
