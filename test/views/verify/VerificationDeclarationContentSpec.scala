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
import views.html.verify.VerificationDeclarationContent

class VerificationDeclarationContentSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite {

  "VerificationDeclarationContent" should {

    "render the declaration heading" in new Setup {
      val doc = Jsoup.parse(view().toString())

      doc.select("h2").text() mustBe
        messages("verify.verificationDeclaration.heading")
    }

    "render the declaration paragraph" in new Setup {
      val doc = Jsoup.parse(view().toString())

      doc.select("p").text() must include(
        messages("verify.verificationDeclaration.p1")
      )
    }

    "render the declaration bullet list" in new Setup {
      val doc = Jsoup.parse(view().toString())

      val bullets = doc.select(".govuk-list--bullet li")

      bullets.size() mustBe 2

      bullets.get(0).text() mustBe
        messages("verify.verificationDeclaration.list.l1")

      bullets.get(1).text() mustBe
        messages("verify.verificationDeclaration.list.l2")
    }

    "render the declaration warning text" in new Setup {
      val doc = Jsoup.parse(view().toString())

      doc.text() must include(
        messages("verify.verificationDeclaration.warningText")
      )
    }
  }

  trait Setup {
    implicit val request: Request[_] = FakeRequest()

    implicit val messages: Messages =
      MessagesImpl(
        Lang.defaultLang,
        app.injector.instanceOf[MessagesApi]
      )

    val view: VerificationDeclarationContent =
      app.injector.instanceOf[VerificationDeclarationContent]
  }
}
