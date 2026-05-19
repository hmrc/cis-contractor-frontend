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
import org.scalatest.matchers.must.Matchers
import play.api.http.Status
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import queries.CisIdQuery
import views.html.verify.NoSubcontractorsSelectedWarningView

class NoSubcontractorsSelectedWarningControllerSpec extends SpecBase with Matchers {

  private val cisId                    = "12345"
  private val manageSubcontractorsBase = applicationConfig.manageSubcontractorsUrl
  private val expectedManageUrl        = s"$manageSubcontractorsBase/$cisId"

  private val expectedSelectSubcontractorsToReverifyUrl = "/subcontractor/verify/select-subcontractors-to-reverify"

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
          view(expectedManageUrl, expectedSelectSubcontractorsToReverifyUrl)(request, messages(application)).toString
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
