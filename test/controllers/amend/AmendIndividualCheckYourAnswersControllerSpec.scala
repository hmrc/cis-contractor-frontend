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
import play.api.test.FakeRequest
import play.api.test.Helpers.*

class AmendIndividualCheckYourAnswersControllerSpec extends SpecBase {

  private lazy val onPageLoadRoute =
    controllers.amend.routes
      .AmendIndividualCheckYourAnswersController
      .onPageLoad()
      .url

  private lazy val onSubmitRoute =
    controllers.amend.routes
      .AmendIndividualCheckYourAnswersController
      .onSubmit()
      .url

  "AmendIndividualCheckYourAnswersController" - {

    "onPageLoad" - {

      "must return OK when user answers exist" in {
        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .build()

        running(application) {
          val request = FakeRequest(GET, onPageLoadRoute)
          val result  = route(application, request).value

          status(result) mustBe OK
        }
      }

      "must return the expected individual details content" in {
        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .build()

        running(application) {
          val request = FakeRequest(GET, onPageLoadRoute)
          val result  = route(application, request).value

          contentAsString(result) mustBe
            "Amend individual subcontractor details"
        }
      }

      "must redirect to JourneyRecovery when no user answers exist" in {
        val application =
          applicationBuilder(userAnswers = None)
            .build()

        running(application) {
          val request = FakeRequest(GET, onPageLoadRoute)
          val result  = route(application, request).value

          status(result) mustBe SEE_OTHER

          redirectLocation(result).value mustBe
            controllers.routes.JourneyRecoveryController
              .onPageLoad()
              .url
        }
      }
    }

    "onSubmit" - {

      "must redirect to onPageLoad when user answers exist" in {
        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .build()

        running(application) {
          val request = FakeRequest(POST, onSubmitRoute)
          val result  = route(application, request).value

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe onPageLoadRoute
        }
      }

      "must redirect to JourneyRecovery when no user answers exist" in {
        val application =
          applicationBuilder(userAnswers = None)
            .build()

        running(application) {
          val request = FakeRequest(POST, onSubmitRoute)
          val result  = route(application, request).value

          status(result) mustBe SEE_OTHER

          redirectLocation(result).value mustBe
            controllers.routes.JourneyRecoveryController
              .onPageLoad()
              .url
        }
      }
    }
  }
}