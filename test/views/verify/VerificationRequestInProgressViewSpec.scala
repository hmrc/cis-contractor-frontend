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
import views.html.verify.VerificationRequestInProgressView

class VerificationRequestInProgressViewSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite {

  "VerificationRequestInProgressView" should {

    "render the page with correct title, heading, paragraphs and links" in new Setup {

      private val html = view()
      private val doc  = Jsoup.parse(html.toString())

      doc.select("title").text() must include(
        messages("verificationRequestInProgress.title")
      )

      doc.select("h1").size() mustBe 1
      doc.select("h1").text() mustBe
        messages("verify.verificationRequestInProgress.heading")

      doc.select("p.govuk-body").text() must include(
        messages("verify.verificationRequestInProgress.p1")
      )

      doc.select("p.govuk-body").text() must include(
        messages("verify.verificationRequestInProgress.p2")
      )

      private val serviceDeskLink =
        doc.select(s"a[href='${appConfig.hmrcOnlineServiceDeskUrl}']")

      serviceDeskLink.size() mustBe 1
      serviceDeskLink.text() mustBe
        messages("verify.verificationRequestInProgress.p3.link")

      serviceDeskLink.first().parent().text() must include(
        messages("verify.verificationRequestInProgress.p3")
      )

      private val manageSubcontractorsLink =
        doc.select(s"a[href='${appConfig.manageSubcontractorsUrl}']")

      manageSubcontractorsLink.size() mustBe 1
      manageSubcontractorsLink.text() mustBe
        messages("verify.verificationRequestInProgress.p4.link")

      manageSubcontractorsLink.first().parent().text() must include(
        messages("verify.verificationRequestInProgress.p4")
      )
    }
  }

  trait Setup {

    implicit val request: Request[_] =
      FakeRequest()

    implicit val messages: Messages =
      MessagesImpl(
        Lang.defaultLang,
        app.injector.instanceOf[MessagesApi]
      )

    implicit val appConfig: FrontendAppConfig =
      app.injector.instanceOf[FrontendAppConfig]

    val view: VerificationRequestInProgressView =
      app.injector.instanceOf[VerificationRequestInProgressView]
  }
}
