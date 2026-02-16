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

import forms.add.partnership.PartnershipAddressFormProvider
import models.NormalMode
import models.add.PartnershipCountryAddress
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.data.Form
import play.api.i18n.{Messages, MessagesApi, MessagesImpl}
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import utils.InputOption
import config.FrontendAppConfig
import views.html.add.partnership.PartnershipAddressView

class PartnershipAddressViewSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite {

  "PartnerShipAddressView" should {
    val testName = "Test Name"
    "render the page with title, heading, inputs and submit button" in new Setup {
      val html: HtmlFormat.Appendable = view(form, NormalMode, testName, countryOptions)
      val doc                         = Jsoup.parse(html.toString())

      doc.select("title").text() must include(messages("partnershipAddress.title"))

      doc.select("h1").text() mustBe messages("partnershipAddress.heading", testName)

      doc.select("form").attr("action") mustBe
        controllers.add.partnership.routes.PartnershipAddressController
          .onSubmit(NormalMode)
          .url

      doc.select("input[name=addressLine1]").size() mustBe 1
      doc.select("input[name=addressLine2]").size() mustBe 1
      doc.select("input[name=addressLine3]").size() mustBe 1
      doc.select("input[name=addressLine4]").size() mustBe 1
      doc.select("input[name=postalCode]").size() mustBe 1
      doc.select("select[name=country]").size() mustBe 1

      doc.select(".govuk-button").text() mustBe messages("site.continue")
    }

    "display error summary and inline errors when required fields are missing" in new Setup {
      val errorForm: Form[PartnershipCountryAddress] =
        form
          .withError("addressLine1", "partnershipAddress.addressLine1.error.required")
          .withError("postalCode", "partnershipAddress.postalCode.error.required")

      val html = view(errorForm, NormalMode, testName, countryOptions)
      val doc  = Jsoup.parse(html.toString())

      val summary = doc.select(".govuk-error-summary")
      summary.text() must include(messages("partnershipAddress.addressLine1.error.required"))
      summary.text() must include(messages("partnershipAddress.postalCode.error.required"))

      summary.select("a").first().attr("href") mustBe "#addressLine1"

      doc.select("#addressLine1-error").text() must include(
        messages("partnershipAddress.addressLine1.error.required")
      )

      doc.select("#postalCode-error").text() must include(
        messages("partnershipAddress.postalCode.error.required")
      )
    }
  }

  trait Setup {
    val formProvider: PartnershipAddressFormProvider = new PartnershipAddressFormProvider()
    val form: Form[PartnershipCountryAddress]        = formProvider()

    implicit val request: Request[_]          = FakeRequest()
    implicit val messages: Messages           = MessagesImpl(
      play.api.i18n.Lang.defaultLang,
      app.injector.instanceOf[MessagesApi]
    )
    implicit val appConfig: FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]

    val countryOptions: Seq[InputOption] = Seq(
      InputOption("GB", "United Kingdom"),
      InputOption("IN", "India")
    )

    val view: PartnershipAddressView = app.injector.instanceOf[PartnershipAddressView]
  }
}
