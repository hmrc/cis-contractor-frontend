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

package views.add.partnership

import forms.add.partnership.PartnershipNominatedPartnerNinoYesNoFormProvider
import models.NormalMode
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
import views.html.add.partnership.PartnershipNominatedPartnerNinoYesNoView

class PartnershipNominatedPartnerNinoYesNoViewSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite {

  "PartnershipNominatedPartnerNinoYesNoView" should {

    "render the page with title, heading, hint, radios and submit button" in new Setup {

      private val nominatedPartnerName = "Test Partner"

      val html: HtmlFormat.Appendable = view(form, NormalMode, nominatedPartnerName)
      val doc: Document               = org.jsoup.Jsoup.parse(html.toString())

      doc.select("title").text() must include(
        messages("partnershipNominatedPartnerNinoYesNo.title")
      )

      val legend: Elements = doc.select("fieldset legend")
      legend.text() mustBe messages(
        "partnershipNominatedPartnerNinoYesNo.heading",
        nominatedPartnerName
      )
      legend.hasClass("govuk-fieldset__legend--l") mustBe true

      val hint: Elements = doc.select("fieldset .govuk-hint")
      hint.text() mustBe messages("partnershipNominatedPartnerNinoYesNo.hint")

      val radioButtons: Elements = doc.select(".govuk-radios__label")
      radioButtons.size() mustBe 2
      radioButtons.get(0).text mustBe "Yes"
      radioButtons.get(1).text mustBe "No"

      doc
        .select("form")
        .attr("action") mustBe controllers.add.partnership.routes.PartnershipNominatedPartnerNinoYesNoController
        .onSubmit(NormalMode)
        .url

      doc.select("form").attr("autocomplete") mustBe "off"

      doc.select(".govuk-button").text() mustBe messages("site.continue")
    }

    "display error summary and inline error when no option is selected" in new Setup {

      val errorForm: Form[Boolean] =
        form.withError("value", "partnershipNominatedPartnerNinoYesNo.error.required")

      private val nominatedPartnerName = "Test Partner"

      val html: HtmlFormat.Appendable = view(errorForm, NormalMode, nominatedPartnerName)
      val doc: Document               = org.jsoup.Jsoup.parse(html.toString())

      val summary: Elements = doc.select(".govuk-error-summary")
      summary.text() must include(
        messages("partnershipNominatedPartnerNinoYesNo.error.required")
      )

      val linkHref: String = summary.select("a").attr("href")
      linkHref mustBe "#value"

      doc.select(".govuk-error-message").text() must include(
        messages("partnershipNominatedPartnerNinoYesNo.error.required")
      )
    }
  }

  trait Setup {
    val formProvider        = new PartnershipNominatedPartnerNinoYesNoFormProvider()
    val form: Form[Boolean] = formProvider()

    implicit val request: Request[_] = FakeRequest()
    implicit val messages: Messages  =
      play.api.i18n.MessagesImpl(
        play.api.i18n.Lang.defaultLang,
        app.injector.instanceOf[play.api.i18n.MessagesApi]
      )

    val view: PartnershipNominatedPartnerNinoYesNoView =
      app.injector.instanceOf[PartnershipNominatedPartnerNinoYesNoView]
  }
}
