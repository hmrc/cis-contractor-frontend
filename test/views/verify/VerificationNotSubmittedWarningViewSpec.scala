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
import views.html.verify.VerificationNotSubmittedWarningView

class VerificationNotSubmittedWarningViewSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite {

  implicit val request: Request[_] =
    FakeRequest()

  implicit val messages: Messages =
    MessagesImpl(
      Lang.defaultLang,
      app.injector.instanceOf[MessagesApi]
    )

  implicit val appConfig: FrontendAppConfig =
    app.injector.instanceOf[FrontendAppConfig]

  private val view: VerificationNotSubmittedWarningView =
    app.injector.instanceOf[VerificationNotSubmittedWarningView]

  "VerificationNotSubmittedWarningView" should {

    "render the page with correct title, heading and both links" in {

      val manageSubcontractorsUrl = "/manage-subcontractors/T1234567"
      val contactHmrcUrl          = "https://www.gov.uk/contact-hmrc"

      val html =
        view(manageSubcontractorsUrl, contactHmrcUrl)(
          request,
          messages
        )

      val doc = Jsoup.parse(html.toString())

      doc.select("title").text() must include(
        messages("verify.verificationNotSubmittedWarning.title")
      )

      doc.select("h1").size() mustBe 1
      doc.select("h1").text() mustBe
        messages("verify.verificationNotSubmittedWarning.heading")

      val firstLink =
        doc.select(s"a[href='$contactHmrcUrl']")
      firstLink.size() mustBe 1
      firstLink.text() mustBe
        messages("verify.verificationNotSubmittedWarning.link1.text")

      val firstLinkText = firstLink.first().parent().text()
      firstLinkText must include(
        messages("verify.verificationNotSubmittedWarning.link1.suffix")
      )
      firstLinkText.trim.endsWith(".") mustBe true

      val secondLink =
        doc.select(s"a[href='$manageSubcontractorsUrl']")
      secondLink.size() mustBe 1
      secondLink.text() mustBe
        messages("verify.verificationNotSubmittedWarning.link2.text")

      val secondLinkText = secondLink.first().parent().text()
      secondLinkText must include(
        messages("verify.verificationNotSubmittedWarning.link2.prefix")
      )
      secondLinkText.trim.endsWith(".") mustBe true
    }
  }
}
