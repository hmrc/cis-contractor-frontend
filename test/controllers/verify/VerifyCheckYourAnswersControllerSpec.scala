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
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.verify.VerifyCheckYourAnswersView

class VerifyCheckYourAnswersControllerSpec extends SpecBase {

  private lazy val onPageLoadRoute = controllers.verify.routes.VerifyCheckYourAnswersController.onPageLoad().url
  private lazy val onSubmitRoute   = controllers.verify.routes.VerifyCheckYourAnswersController.onSubmit().url

  "VerifyCheckYourAnswersController" - {

    "onPageLoad" - {

      "must return OK and render the view" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
        running(application) {
          val request = FakeRequest(GET, onPageLoadRoute)
          val result  = route(application, request).value
          val view    = application.injector.instanceOf[VerifyCheckYourAnswersView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(
            viewmodels.govuk.summarylist.SummaryListViewModel(rows = Seq.empty)
          )(request, messages(application)).toString
        }
      }

      "must redirect to Journey Recovery when there is no session data" in {
        val application = applicationBuilder(userAnswers = None).build()
        running(application) {
          val request = FakeRequest(GET, onPageLoadRoute)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }

    "onSubmit" - {

      "must redirect to Submission Sending" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
        running(application) {
          val request = FakeRequest(POST, onSubmitRoute)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.verify.routes.SubmissionSendingController
            .onPageLoad()
            .url
        }
      }
    }
  }
}
