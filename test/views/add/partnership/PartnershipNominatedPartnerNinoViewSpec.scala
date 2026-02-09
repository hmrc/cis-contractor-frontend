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

import forms.add.partnership.PartnershipNominatedPartnerNinoFormProvider
import models.NormalMode
import org.jsoup.Jsoup
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
import views.html.add.partnership.PartnershipNominatedPartnerNinoView

class PartnershipNominatedPartnerNinoViewSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite {

  "PartnershipNominatedPartnerNinoView" should {

    "render the page with title, heading, hint, input and submit button" in new Setup {
      private val nominatedPartnerName = "Jane Doe"

      val html: HtmlFormat.Appendable = view(form, NormalMode, nominatedPartnerName)
      val doc: Document               = Jsoup.parse(html.toString())

      doc.select("title").text() must include(
        messages("partnershipNominatedPartnerNino.title", nominatedPartnerName)
      )

      val heading: Elements = doc.select("label.govuk-label")
      heading.text() mustBe messages("partnershipNominatedPartnerNino.heading", nominatedPartnerName)

      doc.select(".govuk-hint").text() must include(messages("partnershipNominatedPartnerNino.hint"))

      doc.select("form").attr("action") mustBe
        controllers.add.partnership.routes.PartnershipNominatedPartnerNinoController
          .onSubmit(NormalMode)
          .url

      doc.select("input[name=value]").size() mustBe 1

      doc.select(".govuk-button").text() mustBe messages("site.continue")
    }

    "display error summary and inline error when the value is missing" in new Setup {
      private val nominatedPartnerName = "Jane Doe"

      val errorForm: Form[String] =
        form.withError("value", "partnershipNominatedPartnerNino.error.required")

      val html: HtmlFormat.Appendable = view(errorForm, NormalMode, nominatedPartnerName)
      val doc: Document               = Jsoup.parse(html.toString())

      val summary: Elements = doc.select(".govuk-error-summary")
      summary.text() must include(messages("partnershipNominatedPartnerNino.error.required"))

      val linkHref: String = summary.select("a").attr("href")
      linkHref mustBe "#value"

      doc.select(".govuk-error-message").text() must include(
        messages("partnershipNominatedPartnerNino.error.required")
      )
    }
  }

  trait Setup {
    val formProvider       = new PartnershipNominatedPartnerNinoFormProvider()
    val form: Form[String] = formProvider()

    implicit val request: Request[_] = FakeRequest()
    implicit val messages: Messages  =
      play.api.i18n.MessagesImpl(
        play.api.i18n.Lang.defaultLang,
        app.injector.instanceOf[play.api.i18n.MessagesApi]
      )

    val view: PartnershipNominatedPartnerNinoView =
      app.injector.instanceOf[PartnershipNominatedPartnerNinoView]
  }
}
