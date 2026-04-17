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

import forms.verify.VerifyYourSubcontractorsFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.data.Form
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import play.api.mvc.Request
import play.api.test.FakeRequest
import views.html.verify.VerifyYourSubcontractorsView

class VerifyYourSubcontractorsViewSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite {

  "VerifyYourSubcontractorsView" should {

    "render the page with title, heading, paragraphs, bullet list, radios and continue button" in new Setup {

      val html = view(form, NormalMode)
      val doc  = Jsoup.parse(html.toString())

      doc.select("title").text() must include(
        messages("verifyYourSubcontractors.title")
      )

      doc.select("h1").text() mustBe
        messages("verify.verifyYourSubcontractors.heading")

      doc.select("p.govuk-body").text() must include(
        messages("verify.verifyYourSubcontractors.p1")
      )
      doc.select("p.govuk-body").text() must include(
        messages("verify.verifyYourSubcontractors.p2")
      )
      doc.select("p.govuk-body").text() must include(
        messages("verify.verifyYourSubcontractors.p3")
      )

      val bullets = doc.select("ul.govuk-list--bullet li").eachText()
      bullets must contain(messages("verify.verifyYourSubcontractors.list.l1"))
      bullets must contain(messages("verify.verifyYourSubcontractors.list.l2"))
      bullets must contain(messages("verify.verifyYourSubcontractors.list.l3"))

      doc.select(".govuk-radios__input").size() mustBe 2

      val labels = doc.select(".govuk-radios__label").eachText()
      labels must contain("Yes")
      labels must contain("No")

      doc.select("form").attr("action") mustBe
        controllers.verify.routes.VerifyYourSubcontractorsController
          .onSubmit(NormalMode)
          .url

      doc.select("form").attr("autocomplete") mustBe "off"

      doc.select(".govuk-button").text() mustBe
        messages("site.continue")
    }

    "display error summary and inline error when no option is selected" in new Setup {

      val errorForm =
        form.withError("value", "verify.verifyYourSubcontractors.error.required")

      val html = view(errorForm, NormalMode)
      val doc  = Jsoup.parse(html.toString())

      val errorSummary = doc.select(".govuk-error-summary")
      errorSummary.text() must include(
        messages("verify.verifyYourSubcontractors.error.required")
      )

      errorSummary.select("a").attr("href") mustBe "#value_0"

      doc.select(".govuk-error-message").text() must include(
        messages("verify.verifyYourSubcontractors.error.required")
      )
    }
  }

  trait Setup {

    private val formProvider =
      new VerifyYourSubcontractorsFormProvider()

    val form: Form[Boolean] =
      formProvider()

    implicit val request: Request[_] =
      FakeRequest()

    implicit val messages: Messages =
      MessagesImpl(
        Lang.defaultLang,
        app.injector.instanceOf[MessagesApi]
      )

    val view: VerifyYourSubcontractorsView =
      app.injector.instanceOf[VerifyYourSubcontractorsView]
  }
}
