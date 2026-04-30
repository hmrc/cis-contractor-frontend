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
import controllers.routes
import models.UserAnswers
import org.mockito.Mockito.*
import org.mockito.ArgumentMatchers.any
import play.api.inject.bind
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.verify.VerifyDepartmentalErrorView

import scala.concurrent.Future
import queries.CisIdQuery

class VerifyDepartmentalErrorControllerSpec extends SpecBase with MockitoSugar {

  private val cisId = "12345"

  "VerifyDepartmentalError Controller" - {

    "must return OK and the correct view for a GET when cisId is in ua" in {

      def ua: UserAnswers =
        emptyUserAnswers
          .set(CisIdQuery, cisId)
          .success
          .value

      val mockRepo = mock[SessionRepository]

      when(mockRepo.set(any())).thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(ua))
          .overrides(
            bind[SessionRepository].toInstance(mockRepo)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, controllers.verify.routes.VerifyDepartmentalErrorController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[VerifyDepartmentalErrorView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(s"${applicationConfig.manageSubcontractorsUrl}/$cisId")(request, messages(application)).toString
      }
    }

    "must redirect to JourneyRecovery for a GET when CisId is missing" in {
      def ua: UserAnswers = emptyUserAnswers

      val mockRepo = mock[SessionRepository]

      when(mockRepo.set(any())).thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(ua))
          .overrides(
            bind[SessionRepository].toInstance(mockRepo)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, controllers.verify.routes.VerifyDepartmentalErrorController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}