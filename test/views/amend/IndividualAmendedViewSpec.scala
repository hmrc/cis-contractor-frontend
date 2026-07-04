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

import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.Messages
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.TableRow
import views.html.amend.IndividualAmendedView

class IndividualAmendedViewSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite {

  "IndividualAmendedView" should {

    "render the page with panel, headings, table and links" in new Setup {

      val html: HtmlFormat.Appendable =
        view(
          rows = rows,
          subcontractorName = subcontractorName,
          manageYourSubcontractorsUrl = manageYourSubcontractorsUrl
        )

      val doc = Jsoup.parse(html.toString())

      // title
      doc.title() must include(messages("individualAmended.panel.heading"))

      // panel
      doc.select(".govuk-panel__title").text() mustBe
        messages("individualAmended.panel.heading")

      // first paragraph
      doc.select(".govuk-body").first().text() mustBe
        messages("individualAmended.p1", subcontractorName)

      // headings
      val headings = doc.select("h2")

      headings.get(0).text() mustBe
        messages("individualAmended.updatesMade.h2")

      headings.get(1).text() mustBe
        messages("individualAmended.beforeYouGo.h2")

      // table headers
      val headers = doc.select("thead th")

      headers.size() mustBe 3

      headers.get(0).text() mustBe
        messages("individualAmended.table.hdr.details")

      headers.get(1).text() mustBe
        messages("individualAmended.table.hdr.previous")

      headers.get(2).text() mustBe
        messages("individualAmended.table.hdr.updated")

      // table content
      val cells = doc.select("tbody td")

      cells.get(0).text() mustBe "Name"
      cells.get(1).text() mustBe "John Smith"
      cells.get(2).text() mustBe "Jane Smith"

      // manage subcontractors link
      val manageLink =
        doc.select("a[href='" + manageYourSubcontractorsUrl + "']")

      manageLink.size() mustBe 1
      manageLink.attr("target") mustBe "_blank"
      manageLink.attr("rel") mustBe "noopener noreferrer"
      manageLink.text() mustBe
        messages("individualAmended.yourSubcontractors")

      // survey link
      val surveyLink =
        doc.select("a[href='#']")

      surveyLink.size() mustBe 1
      surveyLink.attr("target") mustBe "_blank"
      surveyLink.attr("rel") mustBe "noopener noreferrer"
      surveyLink.text() mustBe
        messages("individualAmended.beforeYouGo.takeAShortSurvey")

      // feedback text
      doc.text() must include(
        messages("individualAmended.beforeYouGo.shareFeedback")
      )
    }
  }

  trait Setup {

    val subcontractorName = "John Smith"

    val manageYourSubcontractorsUrl =
      "/manage-your-subcontractors"

    val rows: Seq[Seq[TableRow]] =
      Seq(
        Seq(
          TableRow(content = Text("Name")),
          TableRow(content = Text("John Smith")),
          TableRow(content = Text("Jane Smith"))
        )
      )

    implicit val request: Request[_] = FakeRequest()

    implicit val messages: Messages =
      play.api.i18n.MessagesImpl(
        play.api.i18n.Lang.defaultLang,
        app.injector.instanceOf[play.api.i18n.MessagesApi]
      )

    val view: IndividualAmendedView =
      app.injector.instanceOf[IndividualAmendedView]
  }
}
