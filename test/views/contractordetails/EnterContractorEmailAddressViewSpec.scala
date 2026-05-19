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

package views.contractordetails

import forms.contractordetails.EnterContractorEmailAddressFormProvider
import models.NormalMode
import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers.should
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import views.html.contractordetails.EnterContractorEmailAddressView

class EnterContractorEmailAddressViewSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite {

  "EnterContractorEmailAddressView" should {

    "render the page with title, heading, input and submit button" in new Setup {
      val html: HtmlFormat.Appendable = view(form, NormalMode)
      val doc                         = org.jsoup.Jsoup.parse(html.toString())

      doc.select("title").text() must include(messages("contractorDetails.enterContractorEmailAddress.title"))

      val heading = doc.select("h1")
      heading.text() mustBe messages("contractorDetails.enterContractorEmailAddress.heading")

      doc
        .select("form")
        .attr("action") mustBe controllers.contractordetails.routes.EnterContractorEmailAddressController
        .onSubmit(NormalMode)
        .url

      doc.select("input[name=value]").size() mustBe 1

      doc.select(".govuk-button").text() mustBe messages("site.continue")
    }

    "display error summary and inline error when no email is entered" in new Setup {
      val errorForm: Form[String] =
        form.withError("value", "contractorDetails.enterContractorEmailAddress.error.required")

      val html = view(errorForm, NormalMode)
      val doc  = org.jsoup.Jsoup.parse(html.toString())

      val summary = doc.select(".govuk-error-summary")
      summary.text() must include(
        messages("contractorDetails.enterContractorEmailAddress.error.required")
      )

      val linkHref = summary.select("a").attr("href")
      linkHref mustBe "#value"

      doc.select(".govuk-error-message").text() must include(
        messages("contractorDetails.enterContractorEmailAddress.error.required")
      )
    }

    "display error summary and inline error when an invalid email is entered" in new Setup {
      val errorForm: Form[String] =
        form.withError("value", "contractorDetails.enterContractorEmailAddress.error.invalid")

      val html = view(errorForm, NormalMode)
      val doc  = org.jsoup.Jsoup.parse(html.toString())

      val summary = doc.select(".govuk-error-summary")
      summary.text() must include(
        messages("contractorDetails.enterContractorEmailAddress.error.invalid")
      )

      doc.select(".govuk-error-message").text() must include(
        messages("contractorDetails.enterContractorEmailAddress.error.invalid")
      )
    }
  }

  trait Setup {
    val formProvider: EnterContractorEmailAddressFormProvider = new EnterContractorEmailAddressFormProvider()
    val form: Form[String]                                    = formProvider()

    implicit val request: Request[_] = FakeRequest()
    implicit val messages: Messages  =
      play.api.i18n.MessagesImpl(
        play.api.i18n.Lang.defaultLang,
        app.injector.instanceOf[play.api.i18n.MessagesApi]
      )

    val view: EnterContractorEmailAddressView = app.injector.instanceOf[EnterContractorEmailAddressView]
  }
}
