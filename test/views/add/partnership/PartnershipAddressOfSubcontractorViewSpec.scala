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

package views.add.partnership

import forms.add.partnership.PartnershipAddressOfSubcontractorFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import views.html.add.partnership.PartnershipAddressOfSubcontractorView

class PartnershipAddressOfSubcontractorViewSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite {

  "PartnerShipAddressOfSubcontractorView" should {
    val testName = "Test Name"
    "render the page with title, heading, inputs and submit button" in new Setup {
      val html: HtmlFormat.Appendable = view(form, NormalMode, testName)
      val doc                         = Jsoup.parse(html.toString())

      doc.select("title").text() must include(messages("partnershipAddressOfSubcontractor.title"))

      doc.select("h1").text() mustBe messages("partnershipAddressOfSubcontractor.heading", testName)

      doc.select("form").attr("action") mustBe
        controllers.add.partnership.routes.PartnershipAddressOfSubcontractorController
          .onSubmit(NormalMode)
          .url

      doc.select("input[name=addressLine1]").size() mustBe 1
      doc.select("input[name=addressLine2]").size() mustBe 1
      doc.select("input[name=addressLine3]").size() mustBe 1
      doc.select("input[name=addressLine4]").size() mustBe 1
      doc.select("input[name=postCode]").size() mustBe 1

      doc.select(".govuk-button").text() mustBe messages("site.continue")
    }

    "display error summary and inline errors when required fields are missing" in new Setup {
      val errorForm: Form[_] =
        form
          .withError("addressLine1", "partnershipAddressOfSubcontractor.addressLine1.error.required")
          .withError("postCode", "partnershipAddressOfSubcontractor.postCode.error.required")

      val html = view(errorForm, NormalMode,testName)
      val doc  = Jsoup.parse(html.toString())

      val summary = doc.select(".govuk-error-summary")
      summary.text() must include(messages("partnershipAddressOfSubcontractor.addressLine1.error.required"))
      summary.text() must include(messages("partnershipAddressOfSubcontractor.postCode.error.required"))

      summary.select("a").first().attr("href") mustBe "#addressLine1"

      doc.select("#addressLine1-error").text() must include(
        messages("partnershipAddressOfSubcontractor.addressLine1.error.required")
      )

      doc.select("#postCode-error").text() must include(
        messages("partnershipAddressOfSubcontractor.postCode.error.required")
      )
    }
  }

  trait Setup {
    val formProvider  = new PartnershipAddressOfSubcontractorFormProvider()
    val form: Form[_] = formProvider()

    implicit val request: Request[_] = FakeRequest()
    implicit val messages: Messages  =
      play.api.i18n.MessagesImpl(
        play.api.i18n.Lang.defaultLang,
        app.injector.instanceOf[play.api.i18n.MessagesApi]
      )

    val view: PartnershipAddressOfSubcontractorView =
      app.injector.instanceOf[PartnershipAddressOfSubcontractorView]
  }
}
