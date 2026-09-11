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

import config.FrontendAppConfig
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.Messages
import play.api.mvc.Request
import play.api.test.FakeRequest
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.TableRow
import viewmodels.amend.AmendConfirmationLink
import views.html.amend.AmendConfirmationView

class AmendConfirmationViewSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite {

  "AmendConfirmationView" should {

    "render the confirmation panel, table and links for standard amend journey" in new Setup {

      val html =
        view(
          rows,
          subcontractorName,
          standardConfirmationLink
        )

      val doc =
        Jsoup.parse(html.toString())

      doc.title() must include(
        messages("amendConfirmation.panel.heading")
      )

      doc.select(".govuk-panel__title").text() mustBe
        messages("amendConfirmation.panel.heading")

      val headings =
        doc.select("h2").eachText()

      headings must contain(
        messages("amendConfirmation.updatesMade.h2")
      )

      headings must contain(
        messages("amendConfirmation.beforeYouGo.h2")
      )

      val table =
        doc.select("table")

      table.size() mustBe 1

      val tableHeaders =
        doc.select("thead th")

      tableHeaders.get(0).text() mustBe
        messages("amendConfirmation.table.hdr.details")

      tableHeaders.get(1).text() mustBe
        messages("amendConfirmation.table.hdr.previous")

      tableHeaders.get(2).text() mustBe
        messages("amendConfirmation.table.hdr.updated")

      val tableRows =
        doc.select("tbody tr")

      tableRows.size() mustBe 1

      val firstRow =
        tableRows.first()

      firstRow.select("td").get(0).text() mustBe "Trust name"
      firstRow.select("td").get(1).text() mustBe "Old Trust"
      firstRow.select("td").get(2).text() mustBe "New Trust"

      val manageYourSubcontractorsLink =
        doc.select(
          s"a[href='${appConfig.retrieveSubcontractorListUrl}']"
        )

      manageYourSubcontractorsLink.text() mustBe
        messages("amendConfirmation.yourSubcontractors")
    }

    "render the subcontractor name in the confirmation text" in new Setup {

      val html =
        view(
          rows,
          subcontractorName,
          standardConfirmationLink
        )

      val doc =
        Jsoup.parse(html.toString())

      doc.select("p.govuk-body").first().text() mustBe
        messages(
          "amendConfirmation.p1",
          subcontractorName
        )
    }

    "render the survey link for standard journey" in new Setup {

      val html =
        view(
          rows,
          subcontractorName,
          standardConfirmationLink
        )

      val doc =
        Jsoup.parse(html.toString())

      val surveyLink =
        doc.select("a[href='#']").last()

      surveyLink.text() mustBe
        messages(
          "amendConfirmation.beforeYouGo.takeAShortSurvey"
        )

      surveyLink.attr("href") mustBe "#"
      surveyLink.attr("target") mustBe "_blank"
      surveyLink.attr("rel") mustBe "noopener noreferrer"
    }

    "hide the before you go section for insufficient info journey" in new Setup {

      val html =
        view(
          rows,
          subcontractorName,
          insufficientConfirmationLink
        )

      val doc =
        Jsoup.parse(html.toString())

      val headings =
        doc.select("h2").eachText()

      headings must contain(
        messages("amendConfirmation.updatesMade.h2")
      )

      headings must not contain
        messages("amendConfirmation.beforeYouGo.h2")

      doc.text() must not include
        messages("amendConfirmation.beforeYouGo.p1")

      val link =
        doc.select("a[href='/review-insufficient']")

      link.text() mustBe
        messages(
          "insufficientSubcontractorDetailsUpdated.cannotVerifyAllSubcontractors"
        )
    }

    "hide the before you go section for unmatched journey" in new Setup {

      val html =
        view(
          rows,
          subcontractorName,
          unmatchedConfirmationLink
        )

      val doc =
        Jsoup.parse(html.toString())

      val headings =
        doc.select("h2").eachText()

      headings must contain(
        messages("amendConfirmation.updatesMade.h2")
      )

      headings must not contain
        messages("amendConfirmation.beforeYouGo.h2")

      doc.text() must not include
        messages("amendConfirmation.beforeYouGo.p1")

      val link =
        doc.select("a[href='/review-unmatched']")

      link.text() mustBe
        messages(
          "unmatched.unmatchedSubcontractorDetailsUpdated.cannotVerifyAllSubcontractors"
        )
    }

    "must not render survey link when before you go section is hidden" in new Setup {

      val html =
        view(
          rows,
          subcontractorName,
          insufficientConfirmationLink
        )

      val doc =
        Jsoup.parse(html.toString())

      doc.text() must not include
        messages(
          "amendConfirmation.beforeYouGo.takeAShortSurvey"
        )
    }
  }

  trait Setup {

    val subcontractorName = "ABC Trust"

    val rows: Seq[Seq[TableRow]] =
      Seq(
        Seq(
          TableRow(content = Text("Trust name")),
          TableRow(content = Text("Old Trust")),
          TableRow(content = Text("New Trust"))
        )
      )

    implicit val request: Request[_] =
      FakeRequest()

    implicit val messages: Messages =
      play.api.i18n.MessagesImpl(
        play.api.i18n.Lang.defaultLang,
        app.injector.instanceOf[play.api.i18n.MessagesApi]
      )

    val appConfig: FrontendAppConfig =
      app.injector.instanceOf[FrontendAppConfig]

    val view: AmendConfirmationView =
      app.injector.instanceOf[AmendConfirmationView]

    val standardConfirmationLink =
      AmendConfirmationLink(
        url = appConfig.retrieveSubcontractorListUrl,
        textKey = "amendConfirmation.yourSubcontractors",
        showBeforeYouGo = true
      )

    val insufficientConfirmationLink =
      AmendConfirmationLink(
        url = "/review-insufficient",
        textKey = "insufficientSubcontractorDetailsUpdated.cannotVerifyAllSubcontractors",
        showBeforeYouGo = false
      )

    val unmatchedConfirmationLink =
      AmendConfirmationLink(
        url = "/review-unmatched",
        textKey = "unmatched.unmatchedSubcontractorDetailsUpdated.cannotVerifyAllSubcontractors",
        showBeforeYouGo = false
      )
  }
}
