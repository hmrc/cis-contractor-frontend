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

package views.contractordetails

import base.SpecBase
import forms.contractordetails.ContractorUtrFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.matchers.must.Matchers
import play.api.data.Form
import play.api.i18n.Messages
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import views.html.contractordetails.ContractorUtrView

class ContractorUtrViewSpec extends SpecBase with Matchers {

  "ContractorUtrView" - {

    "must display the correct title" in new Setup {
      doc.title must include(messages("contractorDetails.contractorUtr.title"))
    }

    "must display the correct heading" in new Setup {
      doc.select("h1").text must include(messages("contractorDetails.contractorUtr.heading"))
    }

    "must display paragraph 1" in new Setup {
      doc.select("p").text must include(messages("contractorDetails.contractorUtr.p1"))
    }

    "must display a text input field" in new Setup {
      doc.select("input[name=value]").size mustBe 1
    }

    "must display the Continue button" in new Setup {
      doc.select("button").text must include(messages("site.continue"))
    }

    "must display the form with the correct action URL" in new Setup {
      doc.select("form").attr("action") mustBe
        controllers.contractordetails.routes.ContractorUtrController.onSubmit(NormalMode).url
    }

    "must display error summary and inline error when form has errors" in new ErrorSetup {
      doc.select(".govuk-error-summary").size mustBe 1
      doc.select(".govuk-error-summary").text must include(messages("contractorDetails.contractorUtr.error.required"))
      doc.select(".govuk-error-message").text must include(messages("contractorDetails.contractorUtr.error.required"))
    }

    "must display title with error prefix when form has errors" in new ErrorSetup {
      doc.title must startWith(messages("error.title.prefix"))
    }
  }

  trait Setup {
    val app                                       = applicationBuilder().build()
    val formProvider: ContractorUtrFormProvider   = new ContractorUtrFormProvider()
    val form: Form[String]                        = formProvider()
    val view: ContractorUtrView                   = app.injector.instanceOf[ContractorUtrView]
    implicit val request: play.api.mvc.Request[_] = FakeRequest()
    implicit val messages: Messages               = play.api.i18n.MessagesImpl(
      play.api.i18n.Lang.defaultLang,
      app.injector.instanceOf[play.api.i18n.MessagesApi]
    )
    lazy val html: HtmlFormat.Appendable          = view(form, NormalMode)
    lazy val doc: Document                        = Jsoup.parse(html.body)
  }

  trait ErrorSetup extends Setup {
    val errorForm: Form[String]                   = form.withError("value", "contractorDetails.contractorUtr.error.required")
    override lazy val html: HtmlFormat.Appendable = view(errorForm, NormalMode)
    override lazy val doc: Document               = Jsoup.parse(html.body)
  }
}
