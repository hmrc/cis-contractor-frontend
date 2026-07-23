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

package views.amend

import forms.amend.AmendIndividualRemoveDetailYesNoFormProvider
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.data.Form
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import play.api.mvc.Request
import play.api.test.FakeRequest
import views.html.amend.AmendIndividualRemoveDetailYesNoView

class AmendIndividualRemoveDetailYesNoViewSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite {

  val subcontractorName = "Test Subcontractor"

  Seq(
    ("trading-name", "amendIndividualRemoveDetailYesNo.detail.trading-name"),
    ("subcontractor-name", "amendIndividualRemoveDetailYesNo.detail.subcontractor-name"),
    ("address", "amendIndividualRemoveDetailYesNo.detail.address"),
    ("contact-details", "amendIndividualRemoveDetailYesNo.detail.contact-details"),
    ("unique-taxpayer-reference", "amendIndividualRemoveDetailYesNo.detail.unique-taxpayer-reference"),
    ("national-insurance-number", "amendIndividualRemoveDetailYesNo.detail.national-insurance-number"),
    ("works-reference-number", "amendIndividualRemoveDetailYesNo.detail.works-reference-number")
  ).foreach { case (contractorDetail, detailKey) =>
    s"AmendIndividualRemoveDetailYesNoView when contractorDetail is '$contractorDetail'" should {

      "render the page with title, heading, hint, yes/no radios and submit button" in new Setup {
        val html = view(subcontractorName, contractorDetail, form)
        val doc  = Jsoup.parse(html.toString())

        doc.select("title").text() must include(
          messages("amendIndividualRemoveDetailYesNo.title")
        )

        val legend = doc.select("fieldset legend")
        legend
          .text() mustBe messages("amendIndividualRemoveDetailYesNo.heading", subcontractorName, messages(detailKey))
        legend.hasClass("govuk-fieldset__legend--l") mustBe true

        val radios = doc.select(".govuk-radios__input")
        radios.size() mustBe 2

        val labels = doc.select(".govuk-radios__label").eachText()
        labels must contain("Yes")
        labels must contain("No")

        doc.select("form").attr("action") mustBe
          controllers.amend.routes.AmendIndividualRemoveDetailYesNoController
            .onSubmit(contractorDetail)
            .url
        doc.select("form").attr("autocomplete") mustBe "off"

        doc.select(".govuk-button").text() mustBe messages("site.continue")
      }

      "display error summary and inline error when no option is selected" in new Setup {

        val subcontractorName = "Test Subcontractor"

        val errorForm = form.withError("value", "amendIndividualRemoveDetailYesNo.error.required")

        val html = view(subcontractorName, contractorDetail, errorForm)
        val doc  = Jsoup.parse(html.toString())

        val summary = doc.select(".govuk-error-summary")
        summary.text() must include(messages("amendIndividualRemoveDetailYesNo.error.required"))
        summary.select("a").attr("href") mustBe "#value_0"

        doc.select(".govuk-error-message").text() must include(
          messages("amendIndividualRemoveDetailYesNo.error.required")
        )
      }
    }
  }

  trait Setup {
    val formProvider        = new AmendIndividualRemoveDetailYesNoFormProvider()
    val form: Form[Boolean] = formProvider()

    implicit val request: Request[_] = FakeRequest()
    implicit val messages: Messages  = MessagesImpl(
      Lang.defaultLang,
      app.injector.instanceOf[MessagesApi]
    )

    val view: AmendIndividualRemoveDetailYesNoView =
      app.injector.instanceOf[AmendIndividualRemoveDetailYesNoView]
  }
}
