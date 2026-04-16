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

package views.verify

import base.SpecBase
import forms.verify.VerificationDeclarationFormProvider
import models.{Mode, NormalMode}
import org.jsoup.Jsoup
import play.api.data.Form
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.verify.VerificationDeclarationView

class VerificationDeclarationViewSpec extends SpecBase {

  "VerificationDeclarationView" - {

    "must render the expected content" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        implicit val request = FakeRequest()
        implicit val msgs    = messages(application)

        val view                = application.injector.instanceOf[VerificationDeclarationView]
        val form: Form[Boolean] = application.injector.instanceOf[VerificationDeclarationFormProvider].apply()
        val mode: Mode          = NormalMode

        val html = view(form, mode)
        val doc  = Jsoup.parse(html.toString)

        doc.title() must include(msgs("verify.verificationDeclaration.title"))

        doc.select("h1").text() mustEqual msgs("verify.verificationDeclaration.heading")

        doc.text() must include(msgs("verify.verificationDeclaration.p1"))
        doc.text() must include(msgs("verify.verificationDeclaration.list.l1"))
        doc.text() must include(msgs("verify.verificationDeclaration.list.l2"))
        doc.text() must include(msgs("verify.verificationDeclaration.warningText"))

        val formEl = doc.select("form").first()
        formEl.attr("action") mustEqual controllers.verify.routes.VerificationDeclarationController.onSubmit().url
        formEl.attr("method").toLowerCase mustEqual "post"

        doc.select("button.govuk-button").text() mustEqual msgs("verify.verificationDeclaration.confirm")
      }
    }
  }
}
