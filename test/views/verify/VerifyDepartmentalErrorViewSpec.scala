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
import views.html.verify.VerifyDepartmentalErrorView


class VerifyDepartmentalErrorViewSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite {
  "VerifyDepartmentalErrorView" should {

    "render the page with correct title, heading, paragraphs and both links" in new Setup {

      private val manageSubcontractorsUrl =
        "http://localhost:6996/construction-industry-scheme/management/manage-subcontractors/12345"

      private val contactHMRCURL = "https://www.gov.uk/find-hmrc-contacts/construction-industry-scheme-general-enquiries"

      val html: HtmlFormat.Appendable = view(manageSubcontractorsUrl)
      val doc: Document = org.jsoup.Jsoup.parse(html.toString())

      doc.select("title").text() must include(messages("verify.verifyDepartmentalError.title"))

      doc.select("h1").text must include(messages("verify.verifyDepartmentalError.heading"))

      doc.select("p").text must include(messages("verify.verifyDepartmentalError.p1"))

      doc.select("p").text must include(messages("verify.verifyDepartmentalError.contactHMRC.p1"))
      doc.getElementsByClass("govuk-link").text must include(messages("verify.verifyDepartmentalError.contactHMRC.p1.link"))

      val contactHMRCLink: Elements =
        doc.select(s"a[href='$contactHMRCURL']")
      contactHMRCLink.size() mustBe 1
      contactHMRCLink.text() mustBe
        messages("verify.verifyDepartmentalError.contactHMRC.p1.link")

      val contactHMRText: String = contactHMRCLink.first().parent().text()

      contactHMRText must include(
        messages("verify.verifyDepartmentalError.contactHMRC.p1")
      )

      val manageLink: Elements =
        doc.select(s"a[href='$manageSubcontractorsUrl']")
      manageLink.size() mustBe 1
      manageLink.text() mustBe
        messages("verify.verifyDepartmentalError.manageSubcontractors.p1.link")

      val manageText: String = manageLink.first().parent().text()

      manageText must include(
        messages("verify.verifyDepartmentalError.manageSubcontractors.p1")
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

    val view: VerifyDepartmentalErrorView = app.injector.instanceOf[VerifyDepartmentalErrorView]
  }
}
