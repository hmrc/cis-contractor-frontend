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

package controllers.verify

import base.SpecBase
import models.UserAnswers
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.when
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import pages.verify.RebuildVerificationFromWarningPage
import play.api.http.Status
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import queries.CisIdQuery
import repositories.SessionRepository
import views.html.verify.NoSubcontractorsSelectedWarningView

import scala.concurrent.Future

class NoSubcontractorsSelectedWarningControllerSpec extends SpecBase with Matchers with MockitoSugar {

  private val cisId             = "12345"
  private val expectedCancelUrl =
    routes.NoSubcontractorsSelectedWarningController.onCancel().url

  private val expectedSelectSubcontractorsToReverifyUrl = "/subcontractor/verify/select-subcontractors-to-verify"

  "NoSubcontractorsSelectedWarningController" - {

    "must return OK and render the correct view when CisId is present" in {

      val userAnswers =
        UserAnswers(userAnswersId).set(CisIdQuery, cisId).success.value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request =
          FakeRequest(
            GET,
            routes.NoSubcontractorsSelectedWarningController.onPageLoad().url
          )

        val result = route(application, request).value

        val view =
          application.injector.instanceOf[NoSubcontractorsSelectedWarningView]

        status(result) mustBe Status.OK
        contentAsString(result) mustBe
          view(expectedCancelUrl, expectedSelectSubcontractorsToReverifyUrl)(request, messages(application)).toString
      }
    }

    "must clear verify journey data and redirect to manage subcontractors when Cancel is clicked" in {

      val userAnswers =
        UserAnswers(userAnswersId)
          .set(CisIdQuery, cisId)
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request =
          FakeRequest(
            GET,
            routes.NoSubcontractorsSelectedWarningController.onCancel().url
          )

        val result = route(application, request).value

        status(result) mustBe Status.SEE_OTHER
        redirectLocation(result).value mustBe
          s"${applicationConfig.manageSubcontractorsUrl}/$cisId"
      }
    }

    "must return OK for CheckMode when CisId is present" in {

      val userAnswers =
        UserAnswers(userAnswersId)
          .set(CisIdQuery, cisId)
          .success
          .value

      val mockSessionRepository               = mock[SessionRepository]
      val captor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
      when(mockSessionRepository.set(captor.capture())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

      running(application) {

        val request =
          FakeRequest(
            GET,
            routes.NoSubcontractorsSelectedWarningController.onPageLoadCheckMode().url
          )

        val result = route(application, request).value

        status(result) mustBe OK
        captor.getValue.get(RebuildVerificationFromWarningPage) mustBe Some(true)
      }
    }

    "must redirect to Journey Recovery from CheckMode when CisId is missing" in {

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {

        val request =
          FakeRequest(
            GET,
            routes.NoSubcontractorsSelectedWarningController.onPageLoadCheckMode().url
          )

        val result = route(application, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe
          controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery when CisId is missing" in {

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(
            GET,
            routes.NoSubcontractorsSelectedWarningController.onPageLoad().url
          )

        val result = route(application, request).value

        status(result) mustBe Status.SEE_OTHER
        redirectLocation(result).value mustBe
          controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
