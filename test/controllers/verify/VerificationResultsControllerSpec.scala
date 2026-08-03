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
import viewmodels.verify.VerificationResultsViewModel
import views.html.verify.VerificationResultsView

class VerificationResultsControllerSpec extends SpecBase {

  "VerificationResults Controller" - {

    "must return OK and the correct view for a GET" in {
      val verificationResults = Seq(
        VerificationResultsViewModel(
          "Brody, Martin",
          "Unmatched",
          "Higher rate",
          "V0004528765/A"
        ),
        VerificationResultsViewModel(
          "Hooper and Associates",
          "Verified",
          "Standard rate",
          "V0004528765"
        ),
        VerificationResultsViewModel(
          "Quint Transportation",
          "Unmatched",
          "Higher rate",
          "V0004528765/B"
        ),
        VerificationResultsViewModel(
          "The Kintner Group",
          "Unmatched",
          "Higher rate",
          "V0004528765/C"
        )
      )

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.verify.routes.VerificationResultsController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[VerificationResultsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(verificationResults)(request, messages(application)).toString
      }
    }
  }
}
