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

import forms.add.SubContactDetailsFormProvider
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
import views.html.add.SubContactDetailsView

class SubContactDetailsViewSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite {

  "SubContactDetailsView" should {

    "render the page with title, heading, two input fields and submit button" in new Setup {
      val html: HtmlFormat.Appendable = view(form, NormalMode)
      val doc = Jsoup.parse(html.toString())

      doc.select("title").text() must include(messages("subContactDetails.title"))

      val heading = doc.select("h1.govuk-heading-l").text()
      heading mustBe messages("subContactDetails.heading")

      doc.select("input[id=email]").size() mustBe 1
      doc.select("label[for=email]").text() mustBe messages("subContactDetails.email")

      doc.select("input[id=telephone]").size() mustBe 1
      doc.select("label[for=telephone]").text() mustBe messages("subContactDetails.telephone")

      doc.select("form").attr("action") mustBe controllers.add.routes.SubContactDetailsController
        .onSubmit(NormalMode)
        .url

      doc.select(".govuk-button").text() mustBe messages("site.continue")
    }

    "display error summary and inline errors when form has errors" in new Setup {
      val errorForm: Form[_] = form
        .withError("email", "subContactDetails.error.email.required")
        .withError("telephone", "subcontractorName.error.telephone.required")

      val html = view(errorForm, NormalMode)
      val doc = Jsoup.parse(html.toString())

      val summary = doc.select(".govuk-error-summary")
      summary.text() must include(messages("subContactDetails.error.email.required"))
      summary.text() must include(messages("subcontractorName.error.telephone.required"))

      summary.select("a[href=#email]").size() mustBe 1
      summary.select("a[href=#telephone]").size() mustBe 1

      doc.select("#email-error").text() must include(messages("subContactDetails.error.email.required"))
      doc.select("#telephone-error").text() must include(messages("subcontractorName.error.telephone.required"))
    }
  }

  trait Setup {
    val formProvider: SubContactDetailsFormProvider = new SubContactDetailsFormProvider()
    val form: Form[_] = formProvider()

    implicit val request: Request[_] = FakeRequest()
    implicit val messages: Messages =
      play.api.i18n.MessagesImpl(
        play.api.i18n.Lang.defaultLang,
        app.injector.instanceOf[play.api.i18n.MessagesApi]
      )

    val view: SubContactDetailsView = app.injector.instanceOf[SubContactDetailsView]
  }
}
