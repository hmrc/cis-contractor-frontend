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
import models.NormalMode
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import play.api.mvc.Request
import play.api.test.FakeRequest
import views.html.verify.NoSubcontractorsSelectedWarningView

class NoSubcontractorsSelectedWarningViewSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite {

  implicit val request: Request[_] =
    FakeRequest()

  implicit val messages: Messages =
    MessagesImpl(
      Lang.defaultLang,
      app.injector.instanceOf[MessagesApi]
    )

  implicit val appConfig: FrontendAppConfig =
    app.injector.instanceOf[FrontendAppConfig]

  private val view: NoSubcontractorsSelectedWarningView =
    app.injector.instanceOf[NoSubcontractorsSelectedWarningView]

  "NoSubcontractorsSelectedWarningView" should {

    "render the page with correct title, heading, paragraph and button group links" in {

      val manageSubcontractorsUrl = "/manage-subcontractors"

      val selectSubcontractorsToReverifyUrl =
        controllers.verify.routes.SelectSubcontractorsToReverifyController
          .onPageLoad(NormalMode)
          .url

      val html =
        view(manageSubcontractorsUrl, selectSubcontractorsToReverifyUrl)(
          request,
          messages
        )

      val doc = Jsoup.parse(html.toString())

      doc.select("title").text() must include(
        messages("verify.noSubcontractorsSelectedWarning.title")
      )

      doc.select("h1").size() mustBe 1
      doc.select("h1").text() mustBe
        messages("verify.noSubcontractorsSelectedWarning.heading")

      doc.select("p.govuk-body").text() must include(
        messages("verify.noSubcontractorsSelectedWarning.p")
      )

      val buttonGroup = doc.select("div.govuk-button-group")
      buttonGroup.size() mustBe 1

      val primaryButton = buttonGroup.select("a.govuk-button")
      primaryButton.size() mustBe 1
      primaryButton.text() mustBe
        messages("verify.noSubcontractorsSelectedWarning.button")
      primaryButton.attr("href") mustBe selectSubcontractorsToReverifyUrl
      primaryButton.attr("id") mustBe "select-button"

      val cancelLink = buttonGroup.select("a.govuk-link")
      cancelLink.size() mustBe 1
      cancelLink.text() mustBe
        messages("verify.noSubcontractorsSelectedWarning.cancel")
      cancelLink.attr("href") mustBe manageSubcontractorsUrl
      cancelLink.attr("id") mustBe "cancel-link"
    }
  }
}
