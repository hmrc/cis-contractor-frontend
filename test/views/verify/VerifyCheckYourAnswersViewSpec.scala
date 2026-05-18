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

import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import play.api.mvc.Request
import play.api.test.FakeRequest
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryList, SummaryListRow, Value}
import views.html.verify.VerifyCheckYourAnswersView

class VerifyCheckYourAnswersViewSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite {

  "VerifyCheckYourAnswersView" should {

    "render the page title, heading, confirm button and form action" in new Setup {
      val doc = Jsoup.parse(view(SummaryList()).toString())

      doc.select("title").text() must include(messages("verify.verifyCheckYourAnswers.title"))
      doc.select("h1").text() mustBe messages("verify.verifyCheckYourAnswers.heading")

      doc.select("form").attr("action") mustBe
        controllers.verify.routes.VerifyCheckYourAnswersController.onSubmit().url

      doc.select(".govuk-button").text() mustBe messages("verify.verifyCheckYourAnswers.confirm")
    }

    "not render an h2 subheading in the main content" in new Setup {
      val doc = Jsoup.parse(view(SummaryList()).toString())
      doc.select("main h2").size() mustBe 0
    }

    "render summary list rows when present" in new Setup {
      val list = SummaryList(rows =
        Seq(
          SummaryListRow(
            key = Key(Text("Subcontractors to verify")),
            value = Value(Text("Brody, Martin"))
          ),
          SummaryListRow(
            key = Key(Text("Do you want confirmation by email?")),
            value = Value(HtmlContent("Send confirmation to current email address: <strong>agent@example.com</strong>"))
          )
        )
      )

      val doc = Jsoup.parse(view(list).toString())

      val rows = doc.select(".govuk-summary-list__row")
      rows.size() mustBe 2
      rows.get(0).select(".govuk-summary-list__key").text() mustBe "Subcontractors to verify"
      rows.get(0).select(".govuk-summary-list__value").text() mustBe "Brody, Martin"
      rows.get(1).select(".govuk-summary-list__key").text() mustBe "Do you want confirmation by email?"
      rows.get(1).select(".govuk-summary-list__value").text() must include(
        "Send confirmation to current email address:"
      )
      rows.get(1).select(".govuk-summary-list__value strong").text() mustBe "agent@example.com"
    }

    "render a bullet list for multiple subcontractors" in new Setup {
      val list = SummaryList(rows =
        Seq(
          SummaryListRow(
            key = Key(Text("Subcontractors to verify")),
            value = Value(
              HtmlContent(
                """<ul class="govuk-list govuk-list--bullet"><li>Brody, Martin</li><li>Hooper And Associates</li></ul>"""
              )
            )
          )
        )
      )

      val doc = Jsoup.parse(view(list).toString())

      val bullets = doc.select(".govuk-list--bullet li")
      bullets.size() mustBe 2
      bullets.get(0).text() mustBe "Brody, Martin"
      bullets.get(1).text() mustBe "Hooper And Associates"
    }

    "render no rows for an empty summary list" in new Setup {
      val doc = Jsoup.parse(view(SummaryList()).toString())
      doc.select(".govuk-summary-list__row").size() mustBe 0
    }
  }

  trait Setup {
    implicit val request: Request[_] = FakeRequest()

    implicit val messages: Messages =
      MessagesImpl(Lang.defaultLang, app.injector.instanceOf[MessagesApi])

    val view: VerifyCheckYourAnswersView =
      app.injector.instanceOf[VerifyCheckYourAnswersView]
  }
}
