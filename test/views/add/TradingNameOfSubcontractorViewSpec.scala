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

import forms.add.TradingNameOfSubcontractorFormProvider
import models.NormalMode
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import views.html.add.TradingNameOfSubcontractorView

class TradingNameOfSubcontractorViewSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite {

  "TradingNameOfSubcontractorView" should {

    "render the page with title, heading, input and submit button" in new Setup {
      val html: HtmlFormat.Appendable = view(form, NormalMode)
      val doc                         = org.jsoup.Jsoup.parse(html.toString())

      doc.select("title").text() must include(messages("tradingNameOfSubcontractor.title"))

      val heading = doc.select("label.govuk-label")
      heading.text() mustBe messages("tradingNameOfSubcontractor.heading")

      doc.select("form").attr("action") mustBe controllers.add.routes.TradingNameOfSubcontractorController
        .onSubmit(NormalMode)
        .url

      doc.select("input[name=value]").size() mustBe 1

      doc.select(".govuk-button").text() mustBe messages("site.continue")
    }

    "display error summary and inline error when no name is entered" in new Setup {
      val errorForm: Form[String] =
        form.withError("value", "tradingNameOfSubcontractor.error.required")

      val html = view(errorForm, NormalMode)
      val doc  = org.jsoup.Jsoup.parse(html.toString())

      val summary = doc.select(".govuk-error-summary")
      summary.text() must include(messages("tradingNameOfSubcontractor.error.required"))

      val linkHref = summary.select("a").attr("href")
      linkHref mustBe "#value"

      doc.select(".govuk-error-message").text() must include(messages("tradingNameOfSubcontractor.error.required"))
    }
  }

  trait Setup {
    val formProvider       = new TradingNameOfSubcontractorFormProvider()
    val form: Form[String] = formProvider()

    implicit val request: Request[_] = FakeRequest()
    implicit val messages: Messages  =
      play.api.i18n.MessagesImpl(
        play.api.i18n.Lang.defaultLang,
        app.injector.instanceOf[play.api.i18n.MessagesApi]
      )

    val view: TradingNameOfSubcontractorView = app.injector.instanceOf[TradingNameOfSubcontractorView]
  }
}
