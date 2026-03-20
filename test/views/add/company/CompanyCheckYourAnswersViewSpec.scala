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

package views.add.company

import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Lang, Messages, MessagesImpl, MessagesApi}
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryList, SummaryListRow, Value}
import views.html.add.company.CompanyCheckYourAnswersView

class CompanyCheckYourAnswersViewSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite {

  "CompanyCheckYourAnswersView" should {

    "render the page with title, headings, summary list and submit button" in new Setup {
      val html: HtmlFormat.Appendable = view(list)
      val doc: Document               = org.jsoup.Jsoup.parse(html.toString())

      doc.select("title").text() must include(messages("companyCheckYourAnswers.title"))

      val heading: Elements = doc.select("h1")
      heading.text() mustBe messages("companyCheckYourAnswers.heading")
      
      doc.select("h2").eachText() must contain(messages("companyCheckYourAnswers.subHeading"))
      doc.select("h2").eachText() must contain(messages("companyCheckYourAnswers.trailHeading"))

      doc.select("p").text() must include(messages("companyCheckYourAnswers.trailText"))
      
      doc.select(".govuk-summary-list").size() mustBe 1

      doc
        .select("form")
        .attr("action") mustBe controllers.add.company.routes.CompanyCheckYourAnswersController
        .onSubmit()
        .url

      doc.select("form").attr("autocomplete") mustBe "off"

      doc.select(".govuk-button").text() mustBe messages("companyCheckYourAnswers.continue")
    }
  }

  trait Setup {
    implicit val request: Request[_] = FakeRequest()

    private val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
    implicit val messages: Messages      = MessagesImpl(Lang.defaultLang, messagesApi)

    val view: CompanyCheckYourAnswersView = app.injector.instanceOf[CompanyCheckYourAnswersView]
    
    val list: SummaryList =
      SummaryList(
        rows = Seq(
          SummaryListRow(
            key = Key(Text("Company name")),
            value = Value(Text("Acme Ltd"))
          )
        )
      )
  }
}