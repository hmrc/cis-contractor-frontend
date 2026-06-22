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

package views

import forms.IndividualContactMethodOptionsFormProvider
import models.NormalMode
import models.IndividualContactMethodOptions
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import views.html.IndividualContactMethodOptionsView

import java.util

class IndividualContactMethodOptionsViewSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite {
  "IndividualContactMethodOptionsView" should {

    "render the page with title, heading, radios and submit button" in new Setup {

      val subcontractorName = "Test Subcontractor"

      val html: HtmlFormat.Appendable = view(form, NormalMode, subcontractorName)
      val doc: Document               = org.jsoup.Jsoup.parse(html.toString())
      doc.select("title").text() must include(messages("individualContactMethodOptions.title"))

      val legend: Elements = doc.select("fieldset legend")
      legend.text() mustBe messages("individualContactMethodOptions.heading", subcontractorName)

      val checkboxes: Elements = doc.select(".govuk-checkboxes__item")
      checkboxes.size() mustBe IndividualContactMethodOptions.values.size

      val labels: util.List[String] = doc.select(".govuk-checkboxes__label").eachText()
      labels must contain(messages("individualContactMethodOptions.emailAddress"))
      labels must contain(messages("individualContactMethodOptions.phoneNumber"))
      labels must contain(messages("individualContactMethodOptions.mobileNumber"))

      doc.select("form").attr("action") mustBe controllers.routes.IndividualContactMethodOptionsController
        .onSubmit(NormalMode)
        .url

      doc.select("form").attr("autocomplete") mustBe "off"

      doc.select(".govuk-button").text() mustBe messages("site.continue")
    }

    "display error summary and inline error when no option is selected" in new Setup {

      val subcontractorName = "Test Subcontractor"

      val errorForm: Form[Set[IndividualContactMethodOptions]] =
        form.withError("value", "individualContactMethodOptions.error.required")

      val html: HtmlFormat.Appendable = view(errorForm, NormalMode, subcontractorName)
      val doc: Document               = org.jsoup.Jsoup.parse(html.toString())

      val summary: Elements = doc.select(".govuk-error-summary")
      summary.text() must include(messages("individualContactMethodOptions.error.required"))

      val linkHref: String = summary.select("a").attr("href")
      linkHref mustBe "#value_0"

      doc.select(".govuk-error-message").text() must include(
        messages("individualContactMethodOptions.error.required")
      )
    }
  }

  trait Setup {
    val formProvider                               = new IndividualContactMethodOptionsFormProvider()
    val form: Form[Set[IndividualContactMethodOptions]] = formProvider()

    implicit val request: Request[_] = FakeRequest()
    implicit val messages: Messages  =
      play.api.i18n.MessagesImpl(
        play.api.i18n.Lang.defaultLang,
        app.injector.instanceOf[play.api.i18n.MessagesApi]
      )

    val view: IndividualContactMethodOptionsView = app.injector.instanceOf[IndividualContactMethodOptionsView]
  }
}
