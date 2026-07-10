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

package views.add.company

import forms.add.company.CompanyContactMethodOptionsFormProvider
import models.NormalMode
import models.add.company.CompanyContactMethodOptions
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
import views.html.add.company.CompanyContactMethodOptionsView

import java.util

class CompanyContactMethodOptionsViewSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite {
  "CompanyContactMethodOptionsView" should {

    "render the page with title, heading, radios and submit button" in new Setup {

      val companyName = "Test Company"

      val html: HtmlFormat.Appendable = view(form, NormalMode, companyName)
      val doc: Document               = org.jsoup.Jsoup.parse(html.toString())
      doc.select("title").text() must include(messages("companyContactMethodOptions.title"))

      val legend: Elements = doc.select("fieldset legend")
      legend.text() mustBe messages("companyContactMethodOptions.heading", companyName)
      legend.hasClass("govuk-fieldset__legend--l") mustBe true

      doc.select(".govuk-hint").text() mustBe messages("companyContactMethodOptions.hint")

      val checkboxes: Elements = doc.select(".govuk-checkboxes__item")
      checkboxes.size() mustBe CompanyContactMethodOptions.values.size

      val labels: util.List[String] = doc.select(".govuk-checkboxes__label").eachText()
      labels must contain(messages("companyContactMethodOptions.email"))
      labels must contain(messages("companyContactMethodOptions.phone"))
      labels must contain(messages("companyContactMethodOptions.mobile"))

      doc.select("form").attr("action") mustBe controllers.add.company.routes.CompanyContactMethodOptionsController
        .onSubmit(NormalMode)
        .url

      doc.select("form").attr("autocomplete") mustBe "off"

      doc.select(".govuk-button").text() mustBe messages("site.continue")
    }

    "display error summary and inline error when no option is selected" in new Setup {

      val companyName = "Test Company"

      val errorForm: Form[Set[CompanyContactMethodOptions]] =
        form.withError("value", "companyContactMethodOptions.error.required")

      val html: HtmlFormat.Appendable = view(errorForm, NormalMode, companyName)
      val doc: Document               = org.jsoup.Jsoup.parse(html.toString())

      val summary: Elements = doc.select(".govuk-error-summary")
      summary.text() must include(messages("companyContactMethodOptions.error.required"))

      val linkHref: String = summary.select("a").attr("href")
      linkHref mustBe "#value_0"

      doc.select(".govuk-error-message").text() must include(
        messages("companyContactMethodOptions.error.required")
      )
    }
  }

  trait Setup {
    val formProvider                                 = new CompanyContactMethodOptionsFormProvider()
    val form: Form[Set[CompanyContactMethodOptions]] = formProvider()

    implicit val request: Request[_] = FakeRequest()
    implicit val messages: Messages  =
      play.api.i18n.MessagesImpl(
        play.api.i18n.Lang.defaultLang,
        app.injector.instanceOf[play.api.i18n.MessagesApi]
      )

    val view: CompanyContactMethodOptionsView = app.injector.instanceOf[CompanyContactMethodOptionsView]
  }
}
