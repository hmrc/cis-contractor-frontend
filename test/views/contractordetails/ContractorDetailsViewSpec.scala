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

import base.SpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import views.html.contractordetails.ContractorDetailsView

class ContractorDetailsViewSpec extends SpecBase {

  "ContractorDetailsView" - {

    "must show Change links when values are provided" in new Setup {
      val html: HtmlFormat.Appendable =
        view(accountsOfficeReference, uniqueTaxpayerReference, schemeName, email)

      val doc: Document = Jsoup.parse(html.body)

      doc.select(".govuk-summary-list__key").text must include(
        messages("contractorDetails.table.uniqueTaxpayerReference")
      )
      doc.select(".govuk-summary-list__key").text must include(messages("contractorDetails.table.schemeName"))
      doc.select(".govuk-summary-list__key").text must include(messages("contractorDetails.table.email"))

      doc.select(".govuk-summary-list__value").text must include(uniqueTaxpayerReference)
      doc.select(".govuk-summary-list__value").text must include(schemeName.trim)
      doc.select(".govuk-summary-list__value").text must include(email)

      val links: String = doc.select(".govuk-link").text
      links must include(messages("site.change"))
      links must include(messages("contractorDetails.link"))
    }

    "must show Add details links when values are empty" in new Setup {
      val html: HtmlFormat.Appendable =
        view(accountsOfficeReference, "", "", "")

      val doc: Document = Jsoup.parse(html.body)

      doc.select(".govuk-summary-list__key").text must include(
        messages("contractorDetails.table.uniqueTaxpayerReference")
      )
      doc.select(".govuk-summary-list__key").text must include(messages("contractorDetails.table.schemeName"))
      doc.select(".govuk-summary-list__key").text must include(messages("contractorDetails.table.email"))

      doc.select(".govuk-summary-list__value").eachText().forEach { value =>
        value mustBe ""
      }

      val links: String = doc.select(".govuk-link").text
      links must include(messages("contractorDetails.table.link.addDetails"))
    }
  }

  trait Setup {
    implicit val request: Request[_] = FakeRequest()
    implicit val messages: Messages  =
      play.api.i18n.MessagesImpl(
        play.api.i18n.Lang.defaultLang,
        app.injector.instanceOf[play.api.i18n.MessagesApi]
      )

    val view: ContractorDetailsView =
      app.injector.instanceOf[ContractorDetailsView]

    val accountsOfficeReference = "123 PA 87654321"
    val uniqueTaxpayerReference = "1234444555"
    val schemeName              = "\tScheme 123"
    val email                   = "test@business.com"
  }
}
