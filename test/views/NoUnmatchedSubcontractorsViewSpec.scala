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

package views

import base.SpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.Application
import play.api.i18n.Messages
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import views.html.NoUnmatchedSubcontractorsView

class NoUnmatchedSubcontractorsViewSpec extends SpecBase {

  "NoUnmatchedSubcontractorsView" - {
    "render the page with title, heading, paragraphs and link" in new Setup {
      val manageSubcontractorsUrl     = "/manage-subcontractors/1"
      val html: HtmlFormat.Appendable = view(manageSubcontractorsUrl)
      val doc: Document               = Jsoup.parse(html.body)

      doc.select("title").text must include(messages("noUnmatchedSubcontractors.title"))
      doc.select("h1").text    must include(messages("noUnmatchedSubcontractors.heading"))
      doc.select("p").text     must include(messages("noUnmatchedSubcontractors.p1"))
      doc.select("p").text     must include(messages("noUnmatchedSubcontractors.p2"))
      doc.select("p").text     must include(messages("noUnmatchedSubcontractors.backTo"))

      val manageSubcontractorsLink = doc.select(s"a[href='$manageSubcontractorsUrl']")
      manageSubcontractorsLink.size() mustBe 1
      manageSubcontractorsLink.text() mustBe
        messages("noUnmatchedSubcontractors.manageYourSubcontractors.link")
      manageSubcontractorsLink.attr("href") mustEqual manageSubcontractorsUrl
      manageSubcontractorsLink.attr("href") must not include "?"
      manageSubcontractorsLink.attr("href") must not include "#"
    }
  }

  trait Setup {
    val app: Application                          = applicationBuilder().build()
    val view: NoUnmatchedSubcontractorsView       = app.injector.instanceOf[NoUnmatchedSubcontractorsView]
    implicit val request: play.api.mvc.Request[_] = FakeRequest()
    implicit val messages: Messages               = play.api.i18n.MessagesImpl(
      play.api.i18n.Lang.defaultLang,
      app.injector.instanceOf[play.api.i18n.MessagesApi]
    )
  }
}
