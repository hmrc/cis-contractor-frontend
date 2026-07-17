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

package views.amend.company

import forms.amend.company.AmendCompanyRemoveDetailYesNoFormProvider
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.data.Form
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import play.api.mvc.Request
import play.api.test.FakeRequest
import views.html.amend.company.AmendCompanyRemoveDetailYesNoView

class AmendCompanyRemoveDetailYesNoViewSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite {

  val companyName = "Test Company"

  Seq(
    ("address", "amendCompanyRemoveDetailYesNo.detail.address"),
    ("contact-details", "amendCompanyRemoveDetailYesNo.detail.contact-details"),
    ("unique-taxpayer-reference", "amendCompanyRemoveDetailYesNo.detail.unique-taxpayer-reference"),
    ("company-registration-number", "amendCompanyRemoveDetailYesNo.detail.company-registration-number"),
    ("works-reference-number", "amendCompanyRemoveDetailYesNo.detail.works-reference-number")
  ).foreach { case (contractorDetail, detailKey) =>
    s"AmendCompanyRemoveDetailYesNoView when contractorDetail is '$contractorDetail'" should {

      "render the page with title, heading, hint, yes/no radios and submit button" in new Setup {
        val html = view(companyName, contractorDetail, form)
        val doc  = Jsoup.parse(html.toString())

        doc.select("title").text() must include(
          messages("amendCompanyRemoveDetailYesNo.title")
        )

        val legend = doc.select("fieldset legend")
        legend.text() mustBe messages("amendCompanyRemoveDetailYesNo.heading", companyName, messages(detailKey))
        legend.hasClass("govuk-fieldset__legend--l") mustBe true

        val radios = doc.select(".govuk-radios__input")
        radios.size() mustBe 2

        val labels = doc.select(".govuk-radios__label").eachText()
        labels must contain("Yes")
        labels must contain("No")

        doc.select("form").attr("action") mustBe
          controllers.amend.company.routes.AmendCompanyRemoveDetailYesNoController
            .onSubmit(contractorDetail)
            .url
        doc.select("form").attr("autocomplete") mustBe "off"

        doc.select(".govuk-button").text() mustBe messages("site.continue")
      }

      "display error summary and inline error when no option is selected" in new Setup {

        val companyName = "Test Company"

        val errorForm = form.withError("value", "amendCompanyRemoveDetailYesNo.error.required")

        val html = view(companyName, contractorDetail, errorForm)
        val doc  = Jsoup.parse(html.toString())

        val summary = doc.select(".govuk-error-summary")
        summary.text() must include(messages("amendCompanyRemoveDetailYesNo.error.required"))
        summary.select("a").attr("href") mustBe "#value_0"

        doc.select(".govuk-error-message").text() must include(messages("amendCompanyRemoveDetailYesNo.error.required"))
      }
    }
  }

  trait Setup {
    val formProvider        = new AmendCompanyRemoveDetailYesNoFormProvider()
    val form: Form[Boolean] = formProvider()

    implicit val request: Request[_] = FakeRequest()
    implicit val messages: Messages  = MessagesImpl(
      Lang.defaultLang,
      app.injector.instanceOf[MessagesApi]
    )

    val view: AmendCompanyRemoveDetailYesNoView =
      app.injector.instanceOf[AmendCompanyRemoveDetailYesNoView]
  }
}
