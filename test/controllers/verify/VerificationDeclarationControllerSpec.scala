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
import forms.verify.VerificationDeclarationFormProvider
import models.NormalMode
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.verify.VerificationDeclarationView

class VerificationDeclarationControllerSpec extends SpecBase {

  "VerificationDeclarationController" - {

    "must return OK and the correct view for a GET" in {

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {

        val mode = NormalMode

        val request =
          FakeRequest(
            GET,
            controllers.verify.routes.VerificationDeclarationController
              .onPageLoad(mode)
              .url
          )

        val result = route(application, request).value

        val view =
          application.injector.instanceOf[VerificationDeclarationView]

        val form =
          application.injector
            .instanceOf[VerificationDeclarationFormProvider]
            .apply()

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(form, mode)(request, messages(application)).toString
      }
    }

    "must redirect on POST" in {

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {

        val mode = NormalMode

        val request =
          FakeRequest(
            POST,
            controllers.verify.routes.VerificationDeclarationController
              .onSubmit(mode)
              .url
          )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
      }
    }
  }
}
