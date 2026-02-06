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

import forms.add.partnership.PartnershipAddressYesNoFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.data.Form
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import play.api.mvc.Request
import play.api.test.FakeRequest
import views.html.add.partnership.PartnershipAddressYesNoView

class PartnershipAddressYesNoViewSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite {

  "PartnershipAddressYesNoView" should {

    "render the page with title, heading, hint, yes/no radios and submit button" in new Setup {

      val partnershipName = "Test Partnership"

      val html = view(form, NormalMode, partnershipName)
      val doc  = Jsoup.parse(html.toString())
      
      doc.select("title").text() must include(
        messages("partnershipAddressYesNo.title")
      )
      
      val legend = doc.select("fieldset legend")
      legend.text() mustBe messages("partnershipAddressYesNo.heading", partnershipName)
      legend.hasClass("govuk-fieldset__legend--l") mustBe true
      
      doc.select(".govuk-hint").text() mustBe messages("partnershipAddressYesNo.hint")
      
      val radios = doc.select(".govuk-radios__input")
      radios.size() mustBe 2

      val labels = doc.select(".govuk-radios__label").eachText()
      labels must contain("Yes")
      labels must contain("No")
      
      doc.select("form").attr("action") mustBe
        controllers.add.partnership.routes.PartnershipAddressYesNoController
          .onSubmit(NormalMode)
          .url
      doc.select("form").attr("autocomplete") mustBe "off"
      
      doc.select(".govuk-button").text() mustBe messages("site.continue")
    }

    "display error summary and inline error when no option is selected" in new Setup {

      val partnershipName = "Test Partnership"

      val errorForm = form.withError("value", "partnershipAddressYesNo.error.required")

      val html = view(errorForm, NormalMode, partnershipName)
      val doc  = Jsoup.parse(html.toString())
      
      val summary = doc.select(".govuk-error-summary")
      summary.text() must include(messages("partnershipAddressYesNo.error.required"))
      summary.select("a").attr("href") mustBe "#value"
      
      doc.select(".govuk-error-message").text() must include(messages("partnershipAddressYesNo.error.required"))
    }
  }

  trait Setup {
    val formProvider        = new PartnershipAddressYesNoFormProvider()
    val form: Form[Boolean] = formProvider()

    implicit val request: Request[_] = FakeRequest()
    implicit val messages: Messages  = MessagesImpl(
      Lang.defaultLang,
      app.injector.instanceOf[MessagesApi]
    )

    val view: PartnershipAddressYesNoView =
      app.injector.instanceOf[PartnershipAddressYesNoView]
  }
}
