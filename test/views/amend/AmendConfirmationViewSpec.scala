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

import models.address.Address
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.Messages
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.TableRow
import views.html.amend.AmendConfirmationView

class AmendConfirmationViewSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite {

  "AmendConfirmationView" should {

    "render the confirmation panel, table and links" in new Setup {

      val html: HtmlFormat.Appendable =
        view(rows, subcontractorName, manageYourSubcontractorsUrl)

      val doc: Document = Jsoup.parse(html.toString())

      doc.title() must include(messages("amendConfirmation.panel.heading"))

      doc.select(".govuk-panel__title").text() mustBe
        messages("amendConfirmation.panel.heading")

      doc.select("h2").first().text() mustBe
        messages("amendConfirmation.updatesMade.h2")

      doc.select("table").size() mustBe 1

      val headers: Elements = doc.select("thead th")
      headers.get(0).text() mustBe messages("amendConfirmation.table.hdr.details")
      headers.get(1).text() mustBe messages("amendConfirmation.table.hdr.previous")
      headers.get(2).text() mustBe messages("amendConfirmation.table.hdr.updated")

      doc.select("tbody tr").size() mustBe 1
      val firstRow: Element = doc.select("tbody tr").first()

      firstRow.select("td").get(0).text() mustBe "Trust name"
      firstRow.select("td").get(1).text() mustBe "Old Trust"
      firstRow.select("td").get(2).text() mustBe "New Trust"

      val backLink: Elements = doc.select("a[href='" + manageYourSubcontractorsUrl + "']")
      backLink.text() mustBe messages("amendConfirmation.yourSubcontractors")
      backLink.attr("target") mustBe "_blank"
      backLink.attr("rel") mustBe "noopener noreferrer"

      doc.select("h2").text() must include(
        messages("amendConfirmation.beforeYouGo.h2")
      )
    }

    "render the subcontractor name in the confirmation text" in new Setup {

      val html = view(rows, subcontractorName, manageYourSubcontractorsUrl)
      val doc  = Jsoup.parse(html.toString())

      doc.body().text() must include(
        messages("amendConfirmation.p1", subcontractorName)
      )
    }

    "render the survey link" in new Setup {

      val html = view(rows, subcontractorName, manageYourSubcontractorsUrl)
      val doc: Document = Jsoup.parse(html.toString())

      val surveyLink: Element = doc.select("a[href='#']").last()

      surveyLink.text() mustBe
        messages("amendConfirmation.beforeYouGo.takeAShortSurvey")
    }
  }

  trait Setup {

    val subcontractorName = "ABC Trust"

    val manageYourSubcontractorsUrl =
      "/manage-your-subcontractors"

    val rows: Seq[Seq[TableRow]] =
      Seq(
        Seq(
          TableRow(content = Text("Trust name")),
          TableRow(content = Text("Old Trust")),
          TableRow(content = Text("New Trust"))
        )
      )

    implicit val request: Request[_] = FakeRequest()

    implicit val messages: Messages =
      play.api.i18n.MessagesImpl(
        play.api.i18n.Lang.defaultLang,
        app.injector.instanceOf[play.api.i18n.MessagesApi]
      )

    val view: AmendConfirmationView =
      app.injector.instanceOf[AmendConfirmationView]
  }
}
