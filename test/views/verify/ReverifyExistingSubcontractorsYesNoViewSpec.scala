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

import models.NormalMode
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.data.Form
import play.api.data.Forms.*
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import play.api.mvc.Request
import play.api.test.FakeRequest
import views.html.verify.ReverifyExistingSubcontractorsYesNoView

class ReverifyExistingSubcontractorsViewYesNoSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite {

  "ReverifyExistingSubcontractorsYesNoView" should {

    "render the page with title, heading, paragraphs, bullet list, radios and continue button" in new Setup {

      val html = view(form, NormalMode)
      val doc  = Jsoup.parse(html.toString())

      doc.select("title").text() must include(
        messages("verify.reverifyExistingSubcontractorsYesNo.title")
      )

      doc.select("h1").text() must include(messages("verify.reverifyExistingSubcontractorsYesNo.heading"))

      val paragraphs = doc.select("p").eachText()
      paragraphs must contain(messages("verify.reverifyExistingSubcontractorsYesNo.p1"))
      paragraphs must contain(messages("verify.reverifyExistingSubcontractorsYesNo.p2"))

      val bullets = doc.select("ul li").eachText()
      bullets must contain(messages("verify.reverifyExistingSubcontractorsYesNo.list.l1"))
      bullets must contain(messages("verify.reverifyExistingSubcontractorsYesNo.list.l2"))
      bullets must contain(messages("verify.reverifyExistingSubcontractorsYesNo.list.l3"))

      doc.select("form").attr("action") mustBe
        controllers.verify.routes.ReverifyExistingSubcontractorsYesNoController
          .onSubmit()
          .url

      doc.select("form").attr("autocomplete") mustBe "off"

      doc.select("input[type=radio][name=value]").size() must be > 0

      val legend = doc.select("fieldset legend")
      legend.text() mustBe messages("verify.reverifyExistingSubcontractorsYesNo.subHeading")

      doc.select(".govuk-button").text() mustBe messages("site.continue")
    }

    "display error summary when no option is selected, linking to the first radio id" in new Setup {

      val errorForm =
        form.withError("value", "verify.reverifyExistingSubcontractorsYesNo.error.required")

      val html = view(errorForm, NormalMode)
      val doc  = Jsoup.parse(html.toString())

      val errorSummary = doc.select(".govuk-error-summary")
      errorSummary.size() mustBe 1

      errorSummary.text() must include(
        messages("verify.reverifyExistingSubcontractorsYesNo.error.required")
      )

      errorSummary.select("a").attr("href") mustBe "#value_0"
    }
  }

  trait Setup {

    val form: Form[String] =
      Form("value" -> nonEmptyText)

    implicit val request: Request[_] =
      FakeRequest()

    implicit val messages: Messages =
      MessagesImpl(
        Lang.defaultLang,
        app.injector.instanceOf[MessagesApi]
      )

    val view: ReverifyExistingSubcontractorsYesNoView =
      app.injector.instanceOf[ReverifyExistingSubcontractorsYesNoView]
  }
}
