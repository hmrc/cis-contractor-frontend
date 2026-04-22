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

import config.FrontendAppConfig
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import play.api.mvc.Request
import play.api.test.FakeRequest
import views.html.verify.NoSubcontractorsAddedView

class NoSubcontractorsAddedViewSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite {

  implicit val request: Request[_] =
    FakeRequest()

  implicit val messages: Messages =
    MessagesImpl(
      Lang.defaultLang,
      app.injector.instanceOf[MessagesApi]
    )

  implicit val appConfig: FrontendAppConfig =
    app.injector.instanceOf[FrontendAppConfig]

  val view: NoSubcontractorsAddedView =
    app.injector.instanceOf[NoSubcontractorsAddedView]

  "NoSubcontractorsAddedView" should {

    "render the page with correct title, heading, paragraphs and both links" in {

      val addSubcontractorsUrl    = "/add-subcontractors"
      val manageSubcontractorsUrl = "/manage-subcontractors/T1234567"

      val html =
        view(addSubcontractorsUrl, manageSubcontractorsUrl)(
          request,
          messages
        )

      val doc = Jsoup.parse(html.toString())

      doc.select("title").text() must include(
        messages("verify.noSubcontractorsAdded.title")
      )

      doc.select("h1").size() mustBe 1
      doc.select("h1").text() mustBe
        messages("verify.noSubcontractorsAdded.heading")

      doc.select("p.govuk-body").text() must include(
        messages("verify.noSubcontractorsAdded.p1")
      )

      val addLink =
        doc.select(s"a[href='$addSubcontractorsUrl']")
      addLink.size() mustBe 1
      addLink.text() mustBe
        messages("verify.noSubcontractorsAdded.p2.link")

      addLink.first().parent().text() must include(
        messages("verify.noSubcontractorsAdded.p2")
      )
      addLink.first().parent().text() must include(
        messages("verify.noSubcontractorsAdded.p2.end")
      )

      val manageLink =
        doc.select(s"a[href='$manageSubcontractorsUrl']")
      manageLink.size() mustBe 1
      manageLink.text() mustBe
        messages("verify.noSubcontractorsAdded.p3.link")

      val manageText = manageLink.first().parent().text()

      manageText must include(
        messages("verify.noSubcontractorsAdded.p3")
      )

      manageText.trim.endsWith(".") mustBe true
    }
  }
}
