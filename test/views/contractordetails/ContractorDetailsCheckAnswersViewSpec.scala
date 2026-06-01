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
import play.api.i18n.Messages
import play.api.mvc.Request
import play.api.test.FakeRequest
import uk.gov.hmrc.govukfrontend.views.Aliases.*
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.Key
import viewmodels.checkAnswers.contractordetails.ContractorDetailsCheckAnswersViewModel
import views.html.contractordetails.ContractorDetailsCheckAnswersView

class ContractorDetailsCheckAnswersViewSpec extends SpecBase {

  "ContractorDetailsView" - {
    "must render the page with headings, summary list, save button and dashboard link" in new Setup {

      val summaryRows = Seq(
        SummaryListRow(
          key = Key(Text("Unique Taxpayer Reference")),
          value = Value(Text("1234567890")),
          actions = Some(
            Actions(items =
              Seq(
                ActionItem("#", Text("Change"), Some("Unique Taxpayer Reference"))
              )
            )
          )
        ),
        SummaryListRow(
          key = Key(Text("Scheme name")),
          value = Value(Text("Scheme ABC")),
          actions = Some(
            Actions(items =
              Seq(
                ActionItem("#", Text("Change"), Some("Scheme name"))
              )
            )
          )
        )
      )

      val html = view(contractorDetailsCheckAnswersViewModel, summaryRows)
      val doc  = Jsoup.parse(html.body)

      doc.select("h1").text must include(messages("contractordetails.contractorDetailsCheckAnswers.heading"))

      doc.select("h2").text must include(
        messages(
          "contractordetails.contractorDetailsCheckAnswers.accountsOfficeReference",
          contractorDetailsCheckAnswersViewModel.accountsOfficeReference
        )
      )

      doc.select(".govuk-summary-list__key").eachText() must contain allOf (
        "Unique Taxpayer Reference",
        "Scheme name"
      )

      doc.select(".govuk-summary-list__value").eachText() must contain allOf (
        "1234567890",
        "Scheme ABC"
      )

      doc.select(".govuk-summary-list__actions .govuk-link").text must include(messages("site.change"))
      doc.select("button.govuk-button").text mustBe messages("site.saveAndContinue")
      doc.select(".govuk-link").text                              must include(
        messages("contractordetails.contractorDetailsCheckAnswers.returnToCisDashboard.link")
      )

    }

    "must render empty values and Add details links when summary rows contain empty values" in new Setup {

      val summaryRows = Seq(
        SummaryListRow(
          key = Key(Text("Unique Taxpayer Reference")),
          value = Value(Text("")),
          actions = Some(
            Actions(items =
              Seq(
                ActionItem("#", Text("Add details"), Some("Unique Taxpayer Reference"))
              )
            )
          )
        )
      )

      val html = view(contractorDetailsCheckAnswersViewModel, summaryRows)
      val doc  = Jsoup.parse(html.body)

      doc.select(".govuk-summary-list__value").text mustBe ""
      doc.select(".govuk-link").text must include(
        messages("contractordetails.contractorDetailsCheckAnswers.table.link.addDetails")
      )
    }

    "must include visually hidden text for each action item" in new Setup {

      val summaryRows = Seq(
        SummaryListRow(
          key = Key(Text("Unique Taxpayer Reference")),
          value = Value(Text("1234567890")),
          actions = Some(
            Actions(items =
              Seq(
                ActionItem("#", Text("Change"), Some("Change Unique Taxpayer Reference"))
              )
            )
          )
        )
      )

      val html = view(contractorDetailsCheckAnswersViewModel, summaryRows)
      val doc  = Jsoup.parse(html.body)

      val hidden = doc.select(".govuk-visually-hidden").eachText()

      hidden must contain("Change Unique Taxpayer Reference")
    }

    //    "must show Change links when values are provided" in new Setup {
//      val html: HtmlFormat.Appendable = view(contractorDetailsCheckAnswersViewModel)
//
//      val doc: Document = Jsoup.parse(html.body)
//
//      doc.select(".govuk-summary-list__key").text must include(
//        messages("contractordetails.contractorDetailsCheckAnswers.table.uniqueTaxpayerReference")
//      )
//      doc.select(".govuk-summary-list__key").text must include(
//        messages("contractordetails.contractorDetailsCheckAnswers.table.schemeName")
//      )
//      doc.select(".govuk-summary-list__key").text must include(
//        messages("contractordetails.contractorDetailsCheckAnswers.table.email")
//      )
//
//      doc.select(".govuk-summary-list__value").text must include(
//        contractorDetailsCheckAnswersViewModel.uniqueTaxpayerReference
//      )
//      doc.select(".govuk-summary-list__value").text must include(contractorDetailsCheckAnswersViewModel.schemeName.trim)
//      doc.select(".govuk-summary-list__value").text must include(contractorDetailsCheckAnswersViewModel.email)
//
//      val links: String = doc.select(".govuk-link").text
//      links must include(messages("site.change"))
//      links must include(messages("contractordetails.contractorDetailsCheckAnswers.link"))
//    }

//    "must show Add details links when values are empty" in new Setup {
//      val html: HtmlFormat.Appendable =
//        view(contractorDetailsCheckAnswersViewModel.copy(uniqueTaxpayerReference = "", schemeName = "", email = ""))
//
//      val doc: Document = Jsoup.parse(html.body)
//
//      doc.select(".govuk-summary-list__key").text must include(
//        messages("contractordetails.contractorDetailsCheckAnswers.table.uniqueTaxpayerReference")
//      )
//      doc.select(".govuk-summary-list__key").text must include(
//        messages("contractordetails.contractorDetailsCheckAnswers.table.schemeName")
//      )
//      doc.select(".govuk-summary-list__key").text must include(
//        messages("contractordetails.contractorDetailsCheckAnswers.table.email")
//      )
//
//      doc.select(".govuk-summary-list__value").eachText().forEach { value =>
//        value mustBe ""
//      }
//
//      val links: String = doc.select(".govuk-link").text
//      links must include(messages("contractordetails.contractorDetailsCheckAnswers.table.link.addDetails"))
//    }

//    "must include visually hidden text for each change link" in new Setup {
//      val rows = Seq(
//        SummaryListRow(
//          key = Key(Text(messages("contractordetails.contractorDetailsCheckAnswers.table.uniqueTaxpayerReference"))),
//          value = Value(Text(contractorDetailsCheckAnswersViewModel.uniqueTaxpayerReference)),
//          actions = Some(Actions(items =
//            Seq(ActionItem("#", messages("site.change"), Some(messages("contractordetails.contractorDetailsCheckAnswers.table.uniqueTaxpayerReference"))))
//          ))
//        ),
//        SummaryListRow(
//          key = Key(Text(messages("contractordetails.contractorDetailsCheckAnswers.table.schemeName"))),
//          value = Value(Text(contractorDetailsCheckAnswersViewModel.schemeName.trim)),
//          actions = Some(Actions(items =
//            Seq(ActionItem("#", messages("site.change"), Some(messages("contractordetails.contractorDetailsCheckAnswers.table.schemeName.hidden"))))
//          ))
//        ),
//        SummaryListRow(
//          key = Key(Text(messages("contractordetails.contractorDetailsCheckAnswers.table.email"))),
//          value = Value(Text(contractorDetailsCheckAnswersViewModel.email)),
//          actions = Some(Actions(items =
//            Seq(ActionItem("#", messages("site.change"), Some(messages("contractordetails.contractorDetailsCheckAnswers.table.email.hidden"))))
//          ))
//        )
//      )
//
//      val html = view(contractorDetailsCheckAnswersViewModel, rows)
//      val doc  = Jsoup.parse(html.body)
//
//      val hiddenTexts = doc.select(".govuk-visually-hidden").eachText()
//
//      hiddenTexts must contain(
//        messages("contractordetails.contractorDetailsCheckAnswers.table.uniqueTaxpayerReference")
//      )
//      hiddenTexts must contain(messages("contractordetails.contractorDetailsCheckAnswers.table.schemeName.hidden"))
//      hiddenTexts must contain(messages("contractordetails.contractorDetailsCheckAnswers.table.email.hidden"))
//    }
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
