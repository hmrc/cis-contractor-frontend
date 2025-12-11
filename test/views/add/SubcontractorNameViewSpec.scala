/*
 * Copyright 2025 HM Revenue & Customs
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

package views.add

import forms.add.SubcontractorNameFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import views.html.add.SubcontractorNameView

class SubcontractorNameViewSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite {

  "SubcontractorNameView" should {

    "render the page with title, heading, three input fields and submit button" in new Setup {
      val html: HtmlFormat.Appendable = view(form, NormalMode)
      val doc                         = Jsoup.parse(html.toString())

      doc.select("title").text() must include(messages("subcontractorName.title"))

      val heading = doc.select("h1.govuk-heading-l").text()
      heading mustBe messages("subcontractorName.heading")

      doc.select("input[id=firstName]").size() mustBe 1
      doc.select("label[for=firstName]").text() mustBe messages("subcontractorName.firstName")

      doc.select("input[id=middleName]").size() mustBe 1
      doc.select("label[for=middleName]").text() mustBe messages("subcontractorName.middleName")

      doc.select("input[id=lastName]").size() mustBe 1
      doc.select("label[for=lastName]").text() mustBe messages("subcontractorName.lastName")

      doc.select("form").attr("action") mustBe controllers.add.routes.SubcontractorNameController
        .onSubmit(NormalMode)
        .url

      doc.select(".govuk-button").text() mustBe messages("site.continue")
    }

    "display error summary and inline errors when form has errors" in new Setup {
      val errorForm: Form[_] = form
        .withError("firstName", "subcontractorName.firstName.error.required")
        .withError("lastName", "subcontractorName.lastName.error.required")

      val html = view(errorForm, NormalMode)
      val doc  = Jsoup.parse(html.toString())

      val summary = doc.select(".govuk-error-summary")
      summary.text() must include(messages("subcontractorName.firstName.error.required"))
      summary.text() must include(messages("subcontractorName.lastName.error.required"))

      summary.select("a[href=#firstName]").size() mustBe 1
      summary.select("a[href=#lastName]").size() mustBe 1

      doc.select("#firstName-error").text() must include(messages("subcontractorName.firstName.error.required"))
      doc.select("#lastName-error").text()  must include(messages("subcontractorName.lastName.error.required"))
    }
  }

  trait Setup {
    val formProvider: SubcontractorNameFormProvider = new SubcontractorNameFormProvider()
    val form: Form[_]                               = formProvider()

    implicit val request: Request[_] = FakeRequest()
    implicit val messages: Messages  =
      play.api.i18n.MessagesImpl(
        play.api.i18n.Lang.defaultLang,
        app.injector.instanceOf[play.api.i18n.MessagesApi]
      )

    val view: SubcontractorNameView = app.injector.instanceOf[SubcontractorNameView]
  }
}
