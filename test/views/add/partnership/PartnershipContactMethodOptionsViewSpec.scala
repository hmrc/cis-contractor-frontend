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

import forms.add.partnership.PartnershipContactMethodOptionsFormProvider
import models.NormalMode
import models.add.partnership.PartnershipContactMethodOptions
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
import views.html.add.partnership.PartnershipContactMethodOptionsView

import java.util

class PartnershipContactMethodOptionsViewSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite {
  "PartnershipContactMethodOptionsView" should {

    "render the page with title, heading, radios and submit button" in new Setup {

      val partnershipName = "Test Name"

      val html: HtmlFormat.Appendable = view(form, NormalMode, partnershipName)
      val doc: Document               = org.jsoup.Jsoup.parse(html.toString())
      doc.select("title").text() must include(messages("partnershipContactMethodOptions.title"))

      val legend: Elements = doc.select("fieldset legend")
      legend.text() mustBe messages("partnershipContactMethodOptions.heading", partnershipName)
      legend.hasClass("govuk-fieldset__legend--l") mustBe true

      val checkboxes: Elements = doc.select(".govuk-checkboxes__item")
      checkboxes.size() mustBe PartnershipContactMethodOptions.values.size

      val labels: util.List[String] = doc.select(".govuk-checkboxes__label").eachText()
      labels must contain(messages("partnershipContactMethodOptions.email"))
      labels must contain(messages("partnershipContactMethodOptions.phone"))
      labels must contain(messages("partnershipContactMethodOptions.mobile"))

      doc
        .select("form")
        .attr("action") mustBe controllers.add.partnership.routes.PartnershipContactMethodOptionsController
        .onSubmit(NormalMode)
        .url

      doc.select("form").attr("autocomplete") mustBe "off"

      doc.select(".govuk-button").text() mustBe messages("site.continue")
    }

    "display error summary and inline error when no option is selected" in new Setup {

      val partnershipName = "Test Subcontractor"

      val errorForm: Form[Set[PartnershipContactMethodOptions]] =
        form.withError("value", "partnershipContactMethodOptions.error.required")

      val html: HtmlFormat.Appendable = view(errorForm, NormalMode, partnershipName)
      val doc: Document               = org.jsoup.Jsoup.parse(html.toString())

      val summary: Elements = doc.select(".govuk-error-summary")
      summary.text() must include(messages("partnershipContactMethodOptions.error.required"))

      val linkHref: String = summary.select("a").attr("href")
      linkHref mustBe "#value_0"

      doc.select(".govuk-error-message").text() must include(
        messages("partnershipContactMethodOptions.error.required")
      )
    }
  }

  trait Setup {
    val formProvider                                     = new PartnershipContactMethodOptionsFormProvider()
    val form: Form[Set[PartnershipContactMethodOptions]] = formProvider()

    implicit val request: Request[_] = FakeRequest()
    implicit val messages: Messages  =
      play.api.i18n.MessagesImpl(
        play.api.i18n.Lang.defaultLang,
        app.injector.instanceOf[play.api.i18n.MessagesApi]
      )

    val view: PartnershipContactMethodOptionsView = app.injector.instanceOf[PartnershipContactMethodOptionsView]
  }
}
