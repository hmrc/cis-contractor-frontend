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
import models.UserAnswers
import models.amend.trust.OriginalTrustAnswers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.add.trust.TrustNamePage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import queries.{CisIdQuery, OriginalTrustAnswersQuery}
import repositories.SessionRepository
import utils.DefaultSubcontractorCleanupService
import viewmodels.amend.trust.TrustAmendConfirmationViewModel
import views.html.amend.AmendConfirmationView
import pages.amend.AmendCheckYourAnswersSubmittedPage
import scala.concurrent.Future
import scala.util.{Failure, Success}

class AmendTrustConfirmationControllerSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {

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
      .set(AmendCheckYourAnswersSubmittedPage, true)
      .success
      .value

  private lazy val confirmationRoute =
    controllers.amend.trust.routes.AmendTrustConfirmationController
      .onPageLoad()
      .url

  private val mockCleanupService =
    mock[DefaultSubcontractorCleanupService]

  private val mockSessionRepository =
    mock[SessionRepository]

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockCleanupService, mockSessionRepository)
  }

  private def application(userAnswers: UserAnswers) =
    applicationBuilder(userAnswers = Some(userAnswers))
      .overrides(
        bind[DefaultSubcontractorCleanupService].toInstance(mockCleanupService),
        bind[SessionRepository].toInstance(mockSessionRepository)
      )
      .build()

  "AmendTrustConfirmationController" - {

    "must return OK and the correct view for a GET" in {

      when(mockCleanupService.cleanAmend(any[UserAnswers]))
        .thenReturn(Success(userAnswersWithOriginal))

      when(mockSessionRepository.set(any[UserAnswers]))
        .thenReturn(Future.successful(true))

      val app = application(userAnswersWithOriginal)

      running(app) {

        val request = FakeRequest(GET, confirmationRoute)
        val result  = route(app, request).value

        val view =
          app.injector.instanceOf[AmendConfirmationView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(
            TrustAmendConfirmationViewModel.rows(
              original,
              userAnswersWithOriginal
            )(messages(app)),
            trustName
          )(request, messages(app)).toString

        verify(mockCleanupService).cleanAmend(any[UserAnswers])
        verify(mockSessionRepository).set(any[UserAnswers])
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

        verifyNoInteractions(mockCleanupService)
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

        verifyNoInteractions(mockCleanupService)
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

        verifyNoInteractions(mockCleanupService)
        verifyNoInteractions(mockSessionRepository)
      }
    }

    "must redirect to Journey Recovery when cleanup fails" in {

      when(mockCleanupService.cleanAmend(any[UserAnswers]))
        .thenReturn(
          Failure(new RuntimeException("cleanup failed"))
        )

      val app = application(userAnswersWithOriginal)

      running(app) {

        val request = FakeRequest(GET, confirmationRoute)
        val result  = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.routes.JourneyRecoveryController
            .onPageLoad()
            .url

        verify(mockCleanupService).cleanAmend(any[UserAnswers])
        verify(mockSessionRepository, never()).set(any[UserAnswers])
      }
    }
  }
}
