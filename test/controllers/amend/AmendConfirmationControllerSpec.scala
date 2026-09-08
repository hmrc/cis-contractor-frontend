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
import config.FrontendAppConfig
import models.UserAnswers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import utils.DefaultSubcontractorCleanupService

import scala.concurrent.Future
import scala.util.{Failure, Success}

class AmendConfirmationControllerSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {

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

  private val userAnswers =
    emptyUserAnswers

  private val cleanedUserAnswers =
    emptyUserAnswers

  private lazy val exitRoute =
    controllers.amend.routes.AmendConfirmationController
      .onExit()
      .url

  "AmendConfirmationController" - {

    "must clean the user answers, save them to the session and redirect to the subcontractor list" in {

      when(mockCleanupService.cleanAmend(any[UserAnswers]))
        .thenReturn(Success(cleanedUserAnswers))

      when(mockSessionRepository.set(any[UserAnswers]))
        .thenReturn(Future.successful(true))

      val app = application(userAnswers)

      running(app) {

        val request = FakeRequest(GET, exitRoute)
        val result  = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          app.injector
            .instanceOf[FrontendAppConfig]
            .retrieveSubcontractorListUrl

        verify(mockCleanupService).cleanAmend(userAnswers)
        verify(mockSessionRepository).set(cleanedUserAnswers)
      }
    }

    "must redirect to Journey Recovery when cleanup fails" in {

      when(mockCleanupService.cleanAmend(any[UserAnswers]))
        .thenReturn(Failure(new RuntimeException("cleanup failed")))

      val app = application(userAnswers)

      running(app) {

        val request = FakeRequest(GET, exitRoute)
        val result  = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.routes.JourneyRecoveryController
            .onPageLoad()
            .url

        verify(mockCleanupService).cleanAmend(userAnswers)
        verifyNoInteractions(mockSessionRepository)
      }
    }

    "must save the cleaned user answers before redirecting to the subcontractor list" in {

      when(mockCleanupService.cleanAmend(any[UserAnswers]))
        .thenReturn(Success(cleanedUserAnswers))

      when(mockSessionRepository.set(any[UserAnswers]))
        .thenReturn(Future.successful(true))

      val app = application(userAnswers)

      running(app) {

        val request = FakeRequest(GET, exitRoute)
        val result  = route(app, request).value

        status(result) mustEqual SEE_OTHER

        verify(mockCleanupService).cleanAmend(userAnswers)
        verify(mockSessionRepository).set(cleanedUserAnswers)
      }
    }
  }
}
