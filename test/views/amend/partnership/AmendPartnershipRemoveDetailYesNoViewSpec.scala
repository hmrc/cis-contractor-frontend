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

package views.amend.partnership

import forms.amend.partnership.AmendPartnershipRemoveDetailYesNoFormProvider
import org.jsoup.Jsoup
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.data.Form
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import play.api.mvc.Request
import play.api.test.FakeRequest
import views.html.amend.partnership.AmendPartnershipRemoveDetailYesNoView

class AmendPartnershipRemoveDetailYesNoViewSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite {

  private val formProvider =
    new AmendPartnershipRemoveDetailYesNoFormProvider()

  private val form: Form[Boolean] =
    formProvider()

  private implicit val request: Request[_] =
    FakeRequest("GET", "/")

  private implicit val messages: Messages =
    MessagesImpl(
      Lang.defaultLang,
      app.injector.instanceOf[MessagesApi]
    )

  private val view: AmendPartnershipRemoveDetailYesNoView =
    app.injector.instanceOf[AmendPartnershipRemoveDetailYesNoView]

  Seq(
    ("address", "address", "Martin Brody"),
    ("contact-details", "contact details", "Martin Brody"),
    ("utr", "UTR", "Martin Brody"),
    ("works-reference-number", "works reference number", "Martin Brody"),
    (
      "nominated-partner-utr",
      "nominated partner's UTR",
      "Juely"
    ),
    (
      "nominated-partner-nino",
      "nominated partner's National Insurance number",
      "Juely"
    ),
    (
      "nominated-partner-company-registration-number",
      "nominated partner's company registration number",
      "Juely"
    )
  ).foreach { case (detail, detailTitle, detailName) =>
    s"AmendPartnershipRemoveDetailYesNoView when detail is '$detail'" should {

      "render the page with the correct browser title and heading" in {

        val result =
          view(
            form,
            detail,
            detailTitle,
            detailName
          )

        val document =
          Jsoup.parse(result.body)

        document.title() should include(
          messages(
            "amendPartnershipRemoveDetailYesNo.title"
          )
        )

        val legend =
          document.select("fieldset legend")

        legend.text() should include(detailTitle)
        legend.text() should include(detailName)

        legend
          .hasClass("govuk-fieldset__legend--l") shouldBe true
      }

      "render two yes/no radio buttons" in {

        val result =
          view(
            form,
            detail,
            detailTitle,
            detailName
          )

        val document =
          Jsoup.parse(result.body)

        val radios =
          document.select("input[type=radio]")

        radios.size() shouldBe 2

        val labels =
          document
            .select(".govuk-radios__label")
            .eachText()

        labels should contain("Yes")
        labels should contain("No")
      }

      "render the correct form action" in {

        val result =
          view(
            form,
            detail,
            detailTitle,
            detailName
          )

        val document =
          Jsoup.parse(result.body)

        document
          .select("form")
          .attr("action") shouldBe
          controllers.amend.partnership.routes.AmendPartnershipRemoveDetailYesNoController
            .onSubmit(detail)
            .url
      }

      "set autocomplete to off" in {

        val result =
          view(
            form,
            detail,
            detailTitle,
            detailName
          )

        val document =
          Jsoup.parse(result.body)

        document
          .select("form")
          .attr("autocomplete") shouldBe "off"
      }

      "render the continue button" in {

        val result =
          view(
            form,
            detail,
            detailTitle,
            detailName
          )

        val document =
          Jsoup.parse(result.body)

        document
          .select(".govuk-button")
          .text() shouldBe messages("site.continue")
      }

      "show the error summary and inline error when no option is selected" in {

        val errorForm =
          form
            .bind(Map.empty[String, String])

        val result =
          view(
            errorForm,
            detail,
            detailTitle,
            detailName
          )

        val document =
          Jsoup.parse(result.body)

        document
          .select(".govuk-error-summary")
          .isEmpty shouldBe false

        document
          .select(".govuk-error-summary")
          .text() should include(
          messages(
            "amendPartnershipRemoveDetailYesNo.error.required"
          )
        )

        document
          .select(".govuk-error-summary a")
          .attr("href") shouldBe "#value_0"

        document
          .select(".govuk-error-message")
          .text() should include(
          messages(
            "amendPartnershipRemoveDetailYesNo.error.required"
          )
        )
      }

      "not show the error summary when the form has no errors" in {

        val result =
          view(
            form,
            detail,
            detailTitle,
            detailName
          )

        val document =
          Jsoup.parse(result.body)

        document
          .select(".govuk-error-summary")
          .isEmpty shouldBe true
      }
    }
  }
}
