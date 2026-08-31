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
import config.FrontendAppConfig
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import queries.CisIdQuery
import views.html.verify.SubmissionUnsuccessfulView

class VerifyDepartmentalErrorSubmitAgainControllerSpec extends SpecBase {

  private lazy val onPageLoadRoute =
    controllers.verify.routes.SubmissionUnsuccessfulController
      .onPageLoad()
      .url

  "SubmissionUnsuccessfulController" - {

    "must return OK and render the view when CisIdQuery exists" in {

      val cisId = "1234567890"

      val userAnswers =
        emptyUserAnswers
          .set(CisIdQuery, cisId)
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .build()

      running(application) {

        val request =
          FakeRequest(GET, onPageLoadRoute)

        val result =
          route(application, request).value

        val view =
          application.injector
            .instanceOf[SubmissionUnsuccessfulView]

        val appConfig =
          application.injector
            .instanceOf[FrontendAppConfig]

        val manageSubcontractorsUrl =
          s"${appConfig.manageSubcontractorsUrl}/$cisId"

        status(result) mustBe OK

        contentAsString(result) mustBe
          view(manageSubcontractorsUrl)(
            request,
            messages(application)
          ).toString
      }
    }

    "must redirect to JourneyRecovery when CisIdQuery is missing" in {

      val application =
        applicationBuilder(
          userAnswers = Some(emptyUserAnswers)
        ).build()

      running(application) {

        val request =
          FakeRequest(GET, onPageLoadRoute)

        val result =
          route(application, request).value

        status(result) mustBe SEE_OTHER

        redirectLocation(result).value mustBe
          controllers.routes.JourneyRecoveryController
            .onPageLoad()
            .url
      }
    }

    "must redirect to JourneyRecovery when no user answers exist" in {

      val application =
        applicationBuilder(userAnswers = None)
          .build()

      running(application) {

        val request =
          FakeRequest(GET, onPageLoadRoute)

        val result =
          route(application, request).value

        status(result) mustBe SEE_OTHER

        redirectLocation(result).value mustBe
          controllers.routes.JourneyRecoveryController
            .onPageLoad()
            .url
      }
    }
  }
}
