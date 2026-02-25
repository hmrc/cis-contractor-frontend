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

package views.add.partnership

import org.jsoup.nodes.Document
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.Messages
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import views.html.add.partnership.SubcontractorAddedView

class SubcontractorAddedViewSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite {

  "SubcontractorAddedView" should {

    "render the page with the correct main content" in new Setup {

      private val partnershipName = "Test Partnership"

      val html: HtmlFormat.Appendable = view(partnershipName)
      val doc: Document               = org.jsoup.Jsoup.parse(html.toString())

      doc.title             must include(messages("subcontractorAdded.title"))
      doc.select("h1").text must include(messages("subcontractorAdded.heading"))

      doc.select("p").text must include(messages("subcontractorAdded.p1", partnershipName))

      doc.select("h2").text                     must include(messages("subcontractorAdded.nextSteps.h2"))
      doc.select("p").text                      must include(messages("subcontractorAdded.nextSteps.p1"))
      doc.select("p").text                      must include(messages("subcontractorAdded.nextSteps.p2"))
      doc.select("li").text                     must include(messages("subcontractorAdded.nextSteps.list.l1"))
      doc.select("li").text                     must include(messages("subcontractorAdded.nextSteps.list.l2"))
      doc.select("li").text                     must include(messages("subcontractorAdded.nextSteps.list.l3"))
      doc.getElementsByClass("govuk-link").text must include(messages("subcontractorAdded.nextSteps.link"))

      doc.select("h2").text                     must include(messages("subcontractorAdded.helpAndSupport.h2"))
      doc.select("p").text                      must include(messages("subcontractorAdded.helpAndSupport.p1"))
      doc.getElementsByClass("govuk-link").text must include(messages("subcontractorAdded.helpAndSupport.p1.link"))
    }
  }

  trait Setup {
    implicit val request: Request[_] = FakeRequest()
    implicit val messages: Messages  =
      play.api.i18n.MessagesImpl(
        play.api.i18n.Lang.defaultLang,
        app.injector.instanceOf[play.api.i18n.MessagesApi]
      )

    val view: SubcontractorAddedView =
      app.injector.instanceOf[SubcontractorAddedView]
  }
}
