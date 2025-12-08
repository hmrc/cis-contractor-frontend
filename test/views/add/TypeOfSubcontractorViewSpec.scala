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

import forms.TypeOfSubcontractorFormProvider
import models.NormalMode
import models.add.TypeOfSubcontractor
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import views.html.add.TypeOfSubcontractorView

class TypeOfSubcontractorViewSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite {

  "TypeOfSubcontractorView" should {

    "render the page with title, heading, radios and submit button" in new Setup {
      val html: HtmlFormat.Appendable = view(form, NormalMode)
      val doc                         = org.jsoup.Jsoup.parse(html.toString())
      doc.select("title").text() must include(messages("typeOfSubcontractor.title"))

      val legend = doc.select("fieldset legend")
      legend.text() mustBe messages("typeOfSubcontractor.heading")
      legend.hasClass("govuk-fieldset__legend--l") mustBe true

      val radios = doc.select(".govuk-radios__input")
      radios.size() mustBe TypeOfSubcontractor.options.size

      val labels = doc.select(".govuk-radios__label").eachText()
      labels must contain(messages("typeOfSubcontractor.individualOrSoleTrader"))
      labels must contain(messages("typeOfSubcontractor.limitedCompany"))
      labels must contain(messages("typeOfSubcontractor.partnership"))
      labels must contain(messages("typeOfSubcontractor.trust"))

      doc.select("form").attr("action") mustBe controllers.add.routes.TypeOfSubcontractorController.onSubmit(NormalMode).url

      doc.select("form").attr("autocomplete") mustBe "off"

      doc.select(".govuk-button").text() mustBe messages("site.continue")
    }

    "display error summary and inline error when no option is selected" in new Setup {
      val errorForm: Form[TypeOfSubcontractor] =
        form.withError("value", "typeOfSubcontractor.error.required")

      val html = view(errorForm, NormalMode)
      val doc  = org.jsoup.Jsoup.parse(html.toString())

      val summary = doc.select(".govuk-error-summary")
      summary.text() must include(messages("typeOfSubcontractor.error.required"))

      val linkHref = summary.select("a").attr("href")
      linkHref mustBe "#value_0"

      doc.select(".govuk-error-message").text() must include(messages("typeOfSubcontractor.error.required"))
    }
  }

  trait Setup {
    val formProvider                   = new TypeOfSubcontractorFormProvider()
    val form: Form[TypeOfSubcontractor] = formProvider()

    implicit val request: Request[_] = FakeRequest()
    implicit val messages: Messages  =
      play.api.i18n.MessagesImpl(
        play.api.i18n.Lang.defaultLang,
        app.injector.instanceOf[play.api.i18n.MessagesApi]
      )

    val view: TypeOfSubcontractorView = app.injector.instanceOf[TypeOfSubcontractorView]
  }
}
