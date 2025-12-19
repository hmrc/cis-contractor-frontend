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

import forms.add.AddressOfSubcontractorFormProvider
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
import views.html.add.AddressOfSubcontractorView

class AddressOfSubcontractorViewSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite {

  "AddressOfSubcontractorView" should {

    "render the page with title, heading, inputs and submit button" in new Setup {
      val html: HtmlFormat.Appendable = view(form, NormalMode)
      val doc                         = Jsoup.parse(html.toString())

      doc.select("title").text() must include(messages("addressOfSubcontractor.title"))

      doc.select("h1").text() mustBe messages("addressOfSubcontractor.heading")

      doc.select("form").attr("action") mustBe
        controllers.add.routes.AddressOfSubcontractorController
          .onSubmit(NormalMode)
          .url

      doc.select("input[name=addressLine1]").size() mustBe 1
      doc.select("input[name=addressLine2]").size() mustBe 1
      doc.select("input[name=addressLine3]").size() mustBe 1
      doc.select("input[name=addressLine4]").size() mustBe 1
      doc.select("input[name=postCode]").size() mustBe 1

      doc.select(".govuk-button").text() mustBe messages("site.continue")
    }

    "display error summary and inline errors when required fields are missing" in new Setup {
      val errorForm: Form[_] =
        form
          .withError("addressLine1", "addressOfSubcontractor.addressLine1.error.required")
          .withError("postCode", "addressOfSubcontractor.postCode.error.required")

      val html = view(errorForm, NormalMode)
      val doc  = Jsoup.parse(html.toString())

      val summary = doc.select(".govuk-error-summary")
      summary.text() must include(messages("addressOfSubcontractor.addressLine1.error.required"))
      summary.text() must include(messages("addressOfSubcontractor.postCode.error.required"))

      summary.select("a").first().attr("href") mustBe "#addressLine1"

      doc.select("#addressLine1-error").text() must include(
        messages("addressOfSubcontractor.addressLine1.error.required")
      )

      doc.select("#postCode-error").text() must include(
        messages("addressOfSubcontractor.postCode.error.required")
      )
    }
  }

  trait Setup {
    val formProvider  = new AddressOfSubcontractorFormProvider()
    val form: Form[_] = formProvider()

    implicit val request: Request[_] = FakeRequest()
    implicit val messages: Messages  =
      play.api.i18n.MessagesImpl(
        play.api.i18n.Lang.defaultLang,
        app.injector.instanceOf[play.api.i18n.MessagesApi]
      )

    val view: AddressOfSubcontractorView =
      app.injector.instanceOf[AddressOfSubcontractorView]
  }
}
