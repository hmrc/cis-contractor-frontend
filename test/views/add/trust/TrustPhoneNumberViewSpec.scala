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

package views.add.trust

import forms.add.trust.TrustPhoneNumberFormProvider
import models.NormalMode
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import views.html.add.trust.TrustPhoneNumberView

class TrustPhoneNumberViewSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite {

  "TrustPhoneNumberView" should {

    "render the page with title, heading, input and submit button" in new Setup {

      val trustName = "Test Trust"

      val html: HtmlFormat.Appendable = view(form, NormalMode, trustName)
      val doc                         = org.jsoup.Jsoup.parse(html.toString())

      doc.select("title").text() must include(messages("trustPhoneNumber.title"))

      val heading = doc.select("h1")
      heading.text() mustBe messages("trustPhoneNumber.heading", trustName)

      val hint = doc.select(".govuk-hint")
      hint.text() mustBe messages("trustPhoneNumber.hint")

      doc
        .select("form")
        .attr("action") mustBe controllers.add.trust.routes.TrustPhoneNumberController
        .onSubmit(NormalMode)
        .url

      doc.select("input[name=value]").size() mustBe 1

      doc.select(".govuk-button").text() mustBe messages("site.continue")
    }

    "display error summary and inline error when no name is entered" in new Setup {

      val trustName = "Test Trust"

      val errorForm: Form[String] =
        form.withError("value", "trustPhoneNumber.error.required")

      val html = view(errorForm, NormalMode, trustName)
      val doc  = org.jsoup.Jsoup.parse(html.toString())

      val summary = doc.select(".govuk-error-summary")
      summary.text() must include(messages("trustPhoneNumber.error.required"))

      val linkHref = summary.select("a").attr("href")
      linkHref mustBe "#value"

      doc.select(".govuk-error-message").text() must include(messages("trustPhoneNumber.error.required"))
    }
  }

  trait Setup {
    val formProvider       = new TrustPhoneNumberFormProvider()
    val form: Form[String] = formProvider()

    implicit val request: Request[_] = FakeRequest()
    implicit val messages: Messages  =
      play.api.i18n.MessagesImpl(
        play.api.i18n.Lang.defaultLang,
        app.injector.instanceOf[play.api.i18n.MessagesApi]
      )

    val view: TrustPhoneNumberView = app.injector.instanceOf[TrustPhoneNumberView]
  }
}
