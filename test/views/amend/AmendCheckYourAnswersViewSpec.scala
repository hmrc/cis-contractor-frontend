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
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist._
import views.html.amend.AmendCheckYourAnswersView

class AmendCheckYourAnswersViewSpec
  extends AnyWordSpec
    with Matchers
    with GuiceOneAppPerSuite {

  "AmendCheckYourAnswersView" should {

    "render the page with headings, summary lists, buttons and cancel link" in new Setup {

      val html: HtmlFormat.Appendable =
        view(informationList, detailsList, subcontractorName)

      val doc: Document =
        Jsoup.parse(html.toString())

      doc.title() must include(messages("amendCheckYourAnswers.title"))

      val h1: Elements = doc.select("h1")
      h1.text() mustBe subcontractorName

      val h2s = doc.select("h2").eachText()

      h2s must contain(messages("amendCheckYourAnswers.heading.subcontractorInformation.h2"))
      h2s must contain(messages("amendCheckYourAnswers.heading.moreDetails.h2"))
      h2s must contain(messages("amendCheckYourAnswers.confirm.h2"))

      doc.select("p").text() must include(
        messages("amendCheckYourAnswers.confirm.p1")
      )

      doc.select(".govuk-summary-list").size() mustBe 2

      doc
        .select("form")
        .attr("action") mustBe
        controllers.amend.trust.routes.AmendTrustCheckYourAnswersController
          .onSubmit()
          .url

      doc.select("form").attr("autocomplete") mustBe "off"

      doc.select(".govuk-button").text() mustBe
        messages("amendCheckYourAnswers.confirm")

      doc.select(".govuk-link").text() must include(
        messages("amendCheckYourAnswers.cancelChanges")
      )
    }
  }

  trait Setup {

    implicit val request: Request[_] = FakeRequest()

    private val messagesApi: MessagesApi =
      app.injector.instanceOf[MessagesApi]

    implicit val messages: Messages =
      MessagesImpl(Lang.defaultLang, messagesApi)

    val view: AmendCheckYourAnswersView =
      app.injector.instanceOf[AmendCheckYourAnswersView]

    val subcontractorName = "Test Trust"

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
