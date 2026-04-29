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
import viewmodels.checkAnswers.verify.VerificationSubmittedViewModel
import views.html.verify.VerificationRequestSubmittedView

import java.time.LocalDateTime

class VerificationRequestSubmittedControllerSpec extends SpecBase {

  "VerificationRequestSubmitted Controller" - {

    "must return OK and the correct view for a GET" in {

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {

        val request =
          FakeRequest(
            GET,
            routes.VerificationRequestSubmittedController.onPageLoad().url
          )

        val result =
          route(application, request).value

        val view =
          application.injector.instanceOf[VerificationRequestSubmittedView]

        val appConfig =
          application.injector.instanceOf[config.FrontendAppConfig]

        val expectedViewModel =
          VerificationSubmittedViewModel(
            referenceNumber = "Reference number 12345",
            submittedAt = LocalDateTime.now(),
            subcontractorsToVerify = Seq(
              "Brody, Martin",
              "Hooper And Associates",
              "Quint Transportation",
              "The Kintner Group"
            ),
            subcontractorsToReverify = Seq("Grant, Alan", "InGen Research"),
            confirmationEmail = Some("test@testmail.com")
          )

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(expectedViewModel)(
            request,
            appConfig,
            messages(application)
          ).toString
      }
    }
  }
}
