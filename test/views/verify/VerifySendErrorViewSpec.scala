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

import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.Messages
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import views.html.verify.VerifySendErrorView

class VerifySendErrorViewSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite {
  "VerifySendErrorView" should {

    "render the page with correct title, heading, paragraphs and both links" in new Setup {

      private val manageSubcontractorsUrl =
        "http://localhost:6996/construction-industry-scheme/management/manage-subcontractors/12345"

      val html: HtmlFormat.Appendable = view(manageSubcontractorsUrl)
      val doc: Document = org.jsoup.Jsoup.parse(html.toString())

      doc.select("title").text() must include(messages("verify.verifySendError.title"))

      doc.select("h1").text must include(messages("verify.verifySendError.heading"))

      doc.select("p").text must include(messages("verify.verifySendError.p1"))
      
      val verificationHistoryLink: Elements =
        doc.select(s"a[href='#']")
      verificationHistoryLink.size() mustBe 1
      verificationHistoryLink.text() mustBe
        messages("verify.verifySendError.verificationHistory.p1.link")

      val verificationHistoryText: String = verificationHistoryLink.first().parent().text()

      verificationHistoryText must include(
        messages("verify.verifySendError.verificationHistory.p1")
      )

      verificationHistoryText.trim.endsWith(".") mustBe true

      val manageLink: Elements =
        doc.select(s"a[href='$manageSubcontractorsUrl']")
      manageLink.size() mustBe 1
      manageLink.text() mustBe
        messages("verify.verifySendError.manageSubcontractors.p1.link")

      val manageText: String = manageLink.first().parent().text()

      manageText must include(
        messages("verify.verifySendError.manageSubcontractors.p1")
      )

      manageText.trim.endsWith(".") mustBe true
    }
  }

  trait Setup {
    implicit val request: Request[_] = FakeRequest()
    implicit val messages: Messages =
      play.api.i18n.MessagesImpl(
        play.api.i18n.Lang.defaultLang,
        app.injector.instanceOf[play.api.i18n.MessagesApi]
      )

    val view: VerifySendErrorView = app.injector.instanceOf[VerifySendErrorView]
  }

}
