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
import viewmodels.checkAnswers.contractordetails.ContractorDetailsCheckAnswersViewModel
import views.html.contractordetails.ContractorDetailsCheckAnswersView

class ContractorDetailsCheckAnswersViewSpec extends SpecBase {

  "ContractorDetailsView" - {

    "must show Change links when values are provided" in new Setup {
      val html: HtmlFormat.Appendable = view(contractorDetailsCheckAnswersViewModel)

      val doc: Document = Jsoup.parse(html.body)

      doc.select(".govuk-summary-list__key").text must include(
        messages("contractordetails.contractorDetailsCheckAnswers.table.uniqueTaxpayerReference")
      )
      doc.select(".govuk-summary-list__key").text must include(
        messages("contractordetails.contractorDetailsCheckAnswers.table.schemeName")
      )
      doc.select(".govuk-summary-list__key").text must include(
        messages("contractordetails.contractorDetailsCheckAnswers.table.email")
      )

      doc.select(".govuk-summary-list__value").text must include(
        contractorDetailsCheckAnswersViewModel.uniqueTaxpayerReference
      )
      doc.select(".govuk-summary-list__value").text must include(contractorDetailsCheckAnswersViewModel.schemeName.trim)
      doc.select(".govuk-summary-list__value").text must include(contractorDetailsCheckAnswersViewModel.email)

      val links: String = doc.select(".govuk-link").text
      links must include(messages("site.change"))
      links must include(messages("contractordetails.contractorDetailsCheckAnswers.link"))
    }

    "must show Add details links when values are empty" in new Setup {
      val html: HtmlFormat.Appendable =
        view(contractorDetailsCheckAnswersViewModel.copy(uniqueTaxpayerReference = "", schemeName = "", email = ""))

      val doc: Document = Jsoup.parse(html.body)

      doc.select(".govuk-summary-list__key").text must include(
        messages("contractordetails.contractorDetailsCheckAnswers.table.uniqueTaxpayerReference")
      )
      doc.select(".govuk-summary-list__key").text must include(
        messages("contractordetails.contractorDetailsCheckAnswers.table.schemeName")
      )
      doc.select(".govuk-summary-list__key").text must include(
        messages("contractordetails.contractorDetailsCheckAnswers.table.email")
      )

      doc.select(".govuk-summary-list__value").eachText().forEach { value =>
        value mustBe ""
      }

      val links: String = doc.select(".govuk-link").text
      links must include(messages("contractordetails.contractorDetailsCheckAnswers.table.link.addDetails"))
    }

    "must include visually hidden text for each change link" in new Setup {
      val html = view(contractorDetailsCheckAnswersViewModel)
      val doc  = Jsoup.parse(html.body)

      val hiddenTexts = doc.select(".govuk-visually-hidden").eachText()

      hiddenTexts must contain(
        messages("contractordetails.contractorDetailsCheckAnswers.table.uniqueTaxpayerReference")
      )
      hiddenTexts must contain(messages("contractordetails.contractorDetailsCheckAnswers.table.schemeName.hidden"))
      hiddenTexts must contain(messages("contractordetails.contractorDetailsCheckAnswers.table.email.hidden"))
    }
  }

  trait Setup {
    implicit val request: Request[_] = FakeRequest()
    implicit val messages: Messages  =
      play.api.i18n.MessagesImpl(
        play.api.i18n.Lang.defaultLang,
        app.injector.instanceOf[play.api.i18n.MessagesApi]
      )

    val view: ContractorDetailsCheckAnswersView =
      app.injector.instanceOf[ContractorDetailsCheckAnswersView]

    val contractorDetailsCheckAnswersViewModel: ContractorDetailsCheckAnswersViewModel =
      ContractorDetailsCheckAnswersViewModel(
        accountsOfficeReference = "123 PA 87654321",
        uniqueTaxpayerReference = "1234444555",
        schemeName = "\tScheme 123",
        email = "test@business.com"
      )
  }
}
