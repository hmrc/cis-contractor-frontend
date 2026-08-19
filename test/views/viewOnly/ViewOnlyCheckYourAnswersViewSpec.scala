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

package views.viewOnly

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.*
import views.html.viewOnly.ViewOnlyCheckYourAnswersView

class ViewOnlyCheckYourAnswersViewSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite {

  "ViewOnlyCheckYourAnswersView" should {

    "render the page with headings, summary lists and back link" in new Setup {

      val html: HtmlFormat.Appendable =
        view(
          informationList,
          detailsList,
          subcontractorName
        )

      val doc: Document =
        Jsoup.parse(html.toString())

      doc.title() must include(
        messages("amendCheckYourAnswers.title")
      )

      val h1: Elements =
        doc.select("h1")

      h1.text() mustBe subcontractorName

      val h2s =
        doc.select("h2").eachText()

      h2s must contain(
        messages(
          "amendCheckYourAnswers.heading.subcontractorInformation.h2"
        )
      )

      h2s must contain(
        messages(
          "amendCheckYourAnswers.heading.moreDetails.h2"
        )
      )

      doc.select(".govuk-summary-list").size() mustBe 2

      val backLink: Elements =
        doc.select("a.govuk-link[href='#']")

      backLink.size() mustBe 1

      backLink.text() mustBe
        messages("viewOnlyCheckYourAnswers.cannotVerifyAllSubcontractors")

      backLink.attr("href") mustBe "#"

      val backLinkContainer =
        backLink.first().parent()

      backLinkContainer.text() mustBe
        s"${messages("viewOnlyCheckYourAnswers.backTo")} " +
        messages("viewOnlyCheckYourAnswers.cannotVerifyAllSubcontractors") +
        "."
    }
  }

  trait Setup {

    implicit val request: Request[_] =
      FakeRequest()

    private val messagesApi: MessagesApi =
      app.injector.instanceOf[MessagesApi]

    implicit val messages: Messages =
      MessagesImpl(
        Lang.defaultLang,
        messagesApi
      )

    val view: ViewOnlyCheckYourAnswersView =
      app.injector.instanceOf[ViewOnlyCheckYourAnswersView]

    val subcontractorName =
      "Test Trust"

    val informationList: SummaryList =
      SummaryList(
        rows = Seq(
          SummaryListRow(
            key = Key(Text("Type")),
            value = Value(Text("Trust"))
          )
        )
      )

    val detailsList: SummaryList =
      SummaryList(
        rows = Seq(
          SummaryListRow(
            key = Key(Text("UTR")),
            value = Value(Text("1234567890"))
          )
        )
      )
  }
}
