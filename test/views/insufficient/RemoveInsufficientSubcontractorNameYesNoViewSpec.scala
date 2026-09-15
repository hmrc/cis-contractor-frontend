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

package views.insufficient

import forms.insufficient.RemoveInsufficientSubcontractorNameYesNoFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import org.jsoup.select.Elements
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import play.api.mvc.Request
import play.api.test.FakeRequest
import views.html.insufficient.RemoveInsufficientSubcontractorNameYesNoView

import java.util

class RemoveInsufficientSubcontractorNameYesNoViewSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite {
  private val formProvider = new RemoveInsufficientSubcontractorNameYesNoFormProvider()
  private val form         = formProvider()

  private val subcontractorName = "Test Subcontractor"

  private given Request[_]         = FakeRequest()
  private given messages: Messages = MessagesImpl(Lang.defaultLang, app.injector.instanceOf[MessagesApi])

  private val view = app.injector.instanceOf[RemoveInsufficientSubcontractorNameYesNoView]

  "RemoveInsufficientSubcontractorNameYesNoView" should {

    "render the page with title, heading, hint, yes/no radios and submit button" in {
      val html = view(form, subcontractorName)
      val doc  = Jsoup.parse(html.toString())

      doc
        .select("title")
        .text() must include(
        messages(
          "removeInsufficientSubcontractorNameYesNo.title",
          subcontractorName
        )
      )

      val legend: Elements =
        doc.select("fieldset legend")

      legend
        .text() mustBe
        messages(
          "removeInsufficientSubcontractorNameYesNo.heading",
          subcontractorName
        )

      legend
        .hasClass("govuk-fieldset__legend--l") mustBe true

      doc
        .select(".govuk-hint")
        .text() mustBe
        messages("removeInsufficientSubcontractorNameYesNo.hint")

      val radios: Elements =
        doc.select(".govuk-radios__input")

      radios
        .size() mustBe 2

      val labels: util.List[String] =
        doc.select(".govuk-radios__label").eachText()

      labels must contain("Yes")
      labels must contain("No")

      doc
        .select("form")
        .attr("action") mustBe
        controllers.insufficient.routes.RemoveInsufficientSubcontractorNameYesNoController
          .onSubmit()
          .url

      doc
        .select("form")
        .attr("autocomplete") mustBe "off"

      doc
        .select(".govuk-button")
        .text() mustBe
        messages("site.continue")
    }

    "display error summary and inline error when no option is selected" in {
      val errorForm = form.withError("value", "removeInsufficientSubcontractorNameYesNo.error.required")
      val html      = view(errorForm, subcontractorName)
      val doc       = Jsoup.parse(html.toString())

      val summary: Elements = doc.select(".govuk-error-summary")

      summary
        .text() must include(
        messages("removeInsufficientSubcontractorNameYesNo.error.required")
      )

      summary
        .select("a")
        .attr("href") mustBe "#value_0"

      doc
        .select(".govuk-error-message")
        .text() must include(
        messages("removeInsufficientSubcontractorNameYesNo.error.required")
      )
    }
  }
}
