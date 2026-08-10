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

package views.verify

import base.SpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import viewmodels.verify.VerificationResultsViewModel
import views.html.verify.VerificationResultsView

import java.util

class VerificationResultsViewSpec extends SpecBase {

  "VerificationResultsView" - {

    "must display the Back to Manage your subcontractors link when all subcontractors are verified" in new Setup {
      val verificationResults         = Seq(
        VerificationResultsViewModel(
          "Brody, Martin",
          "Verified",
          "Higher rate",
          "V0004528765/A"
        ),
        VerificationResultsViewModel(
          "Hooper and Associates",
          "Verified",
          "Standard rate",
          "V0004528765"
        ),
        VerificationResultsViewModel(
          "Quint Transportation",
          "Verified",
          "Higher rate",
          "V0004528765/B"
        ),
        VerificationResultsViewModel(
          "The Kintner Group",
          "Verified",
          "Higher rate",
          "V0004528765/C"
        )
      )
      val manageSubcontractorsUrl     = "/manage-subcontractors/1"
      val html: HtmlFormat.Appendable = view(verificationResults, manageSubcontractorsUrl)
      val doc: Document               = Jsoup.parse(html.body)
      doc.select("title").text() must include(messages("verify.verificationResults.title"))
      doc.select("h1").text()    must include(messages("verify.verificationResults.heading"))
      doc.select("p").text()     must include(messages("verify.verificationResults.paragraph"))

      val headers: util.List[String] = doc.select("thead th").eachText()

      headers mustBe util.Arrays.asList(
        messages("verify.verificationResults.name"),
        messages("verify.verificationResults.status"),
        messages("verify.verificationResults.taxTreatment"),
        messages("verify.verificationResults.verificationNumber")
      )

      val rows: Elements = doc.select("tbody tr")
      rows.size() mustBe verificationResults.size

      verificationResults.zipWithIndex.foreach { case (result, index) =>
        val cells = rows.get(index).select("td").eachText()

        cells mustBe util.Arrays.asList(
          result.name,
          result.verificationStatus,
          result.taxTreatment,
          result.verificationNumber
        )
      }

      doc.select("p").text()         must include(messages("verify.verificationResults.backTo"))
      doc.select(".govuk-link").text must include(messages("verify.verificationResults.manageYourSubcontractors.link"))
    }

    "must display the Review unmatched subcontractors button when there is at least one unmatched subcontractor" in new Setup {
      val verificationResults         = Seq(
        VerificationResultsViewModel(
          "Brody, Martin",
          "Unmatched",
          "Higher rate",
          "V0004528765/A"
        ),
        VerificationResultsViewModel(
          "Hooper and Associates",
          "Verified",
          "Standard rate",
          "V0004528765"
        ),
        VerificationResultsViewModel(
          "Quint Transportation",
          "Unmatched",
          "Higher rate",
          "V0004528765/B"
        ),
        VerificationResultsViewModel(
          "The Kintner Group",
          "Verified",
          "Higher rate",
          "V0004528765/C"
        )
      )
      val manageSubcontractorsUrl     = "/manage-subcontractors/1"
      val html: HtmlFormat.Appendable = view(verificationResults, manageSubcontractorsUrl)
      val doc: Document               = Jsoup.parse(html.body)
      val headers: util.List[String]  = doc.select("thead th").eachText()

      headers mustBe util.Arrays.asList(
        messages("verify.verificationResults.name"),
        messages("verify.verificationResults.status"),
        messages("verify.verificationResults.taxTreatment"),
        messages("verify.verificationResults.verificationNumber")
      )

      val rows: Elements = doc.select("tbody tr")
      rows.size() mustBe verificationResults.size

      verificationResults.zipWithIndex.foreach { case (result, index) =>
        val cells = rows.get(index).select("td").eachText()

        cells mustBe util.Arrays.asList(
          result.name,
          result.verificationStatus,
          result.taxTreatment,
          result.verificationNumber
        )
      }

      doc.select("button").text() mustBe
        messages("verify.verificationResults.reviewUnmatchedSubcontractors.button")
    }

  }
  trait Setup {

    implicit val request: Request[_] =
      FakeRequest()

    implicit val messages: Messages =
      MessagesImpl(
        Lang.defaultLang,
        app.injector.instanceOf[MessagesApi]
      )

    val view: VerificationResultsView =
      app.injector.instanceOf[VerificationResultsView]
  }
}
