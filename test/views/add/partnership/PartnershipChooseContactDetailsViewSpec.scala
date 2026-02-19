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

import forms.add.partnership.PartnershipChooseContactDetailsFormProvider
import models.NormalMode
import models.add.partnership.PartnershipChooseContactDetails
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
import views.html.add.partnership.PartnershipChooseContactDetailsView

import java.util

class PartnershipChooseContactDetailsViewSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite {

  "PartnershipChooseContactDetailsView" should {

    "render the page with title, heading, radios and submit button" in new Setup {
      val html: HtmlFormat.Appendable = view(form, NormalMode, partnershipName)
      val doc: Document               = org.jsoup.Jsoup.parse(html.toString())
      doc.select("title").text() must include(messages("partnershipChooseContactDetails.title"))

      val heading: Elements = doc.select("h1")
      heading.text() mustBe messages("partnershipChooseContactDetails.heading", partnershipName)

      val paragraph: String = doc.select("p.govuk-body").first().text()
      paragraph must include(messages("partnershipChooseContactDetails.paragraph"))

      val radios: Elements = doc.select(".govuk-radios__input")
      radios.size() mustBe PartnershipChooseContactDetails.values.size

      val labels: util.List[String] = doc.select(".govuk-radios__label").eachText()
      labels must contain(messages("partnershipChooseContactDetails.email"))
      labels must contain(messages("partnershipChooseContactDetails.phone"))
      labels must contain(messages("partnershipChooseContactDetails.mobile"))
      labels must contain(messages("partnershipChooseContactDetails.noDetails"))
      labels must not(contain(messages("partnershipChooseContactDetails.or")))

      doc
        .select("form")
        .attr("action") mustBe controllers.add.partnership.routes.PartnershipChooseContactDetailsController
        .onSubmit(NormalMode)
        .url

      doc.select("form").attr("autocomplete") mustBe "off"

      doc.select(".govuk-button").text() mustBe messages("site.continue")
    }

    "display error summary and inline error when no option is selected" in new Setup {
      val errorForm: Form[PartnershipChooseContactDetails] =
        form.withError("value", "partnershipChooseContactDetails.error.required")

      val html: HtmlFormat.Appendable = view(errorForm, NormalMode, partnershipName)
      val doc: Document               = org.jsoup.Jsoup.parse(html.toString())

      val summary: Elements = doc.select(".govuk-error-summary")
      summary.text() must include(messages("partnershipChooseContactDetails.error.required"))

      val linkHref: String = summary.select("a").attr("href")
      linkHref mustBe "#value_0"

      doc.select(".govuk-error-message").text() must include(messages("partnershipChooseContactDetails.error.required"))
    }
  }

  trait Setup {
    val formProvider: PartnershipChooseContactDetailsFormProvider = new PartnershipChooseContactDetailsFormProvider()
    val form: Form[PartnershipChooseContactDetails]               = formProvider()
    val partnershipName: String                                   = "Test Partnership LLP"

    implicit val request: Request[_] = FakeRequest()
    implicit val messages: Messages  =
      play.api.i18n.MessagesImpl(
        play.api.i18n.Lang.defaultLang,
        app.injector.instanceOf[play.api.i18n.MessagesApi]
      )

    val view: PartnershipChooseContactDetailsView = app.injector.instanceOf[PartnershipChooseContactDetailsView]
  }
}
