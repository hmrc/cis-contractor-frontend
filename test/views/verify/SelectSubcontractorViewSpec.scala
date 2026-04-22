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

import base.SpecBase
import controllers.verify.SelectSubcontractorController
import forms.verify.SelectSubcontractorFormProvider
import models.{Mode, SubcontractorViewModel}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import org.scalatest.matchers.must.Matchers
import play.api.Application
import play.api.data.Form
import play.api.i18n.Messages
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import play.api.mvc.*
import play.api.i18n.MessagesApi
import play.api.test.Helpers.GET
import uk.gov.hmrc.govukfrontend.views.viewmodels.checkboxes.CheckboxItem
import viewmodels.govuk.PaginationFluency.*
import views.html.verify.SelectSubcontractorView

class SelectSubcontractorViewSpec extends SpecBase with Matchers {

  "SelectSubcontractorView" - {

    "must render heading, hint, checkboxes and button" in new Setup {

      val html: HtmlFormat.Appendable =
        view(form, mode, checkboxItems, PaginationViewModel(), page = 1)

      val doc: Document = Jsoup.parse(html.body)

      doc.title must include(messages("verify.selectSubcontractor.title"))

      doc.select("h1").text mustBe messages("verify.selectSubcontractor.heading")

      doc.select(".govuk-hint").text mustBe messages("verify.selectSubcontractor.hint")

      doc.select(".govuk-checkboxes__item").size() mustBe checkboxItems.size

      doc.select("button").text mustBe messages("site.continue")
    }

    "must render error summary when form has errors" in new Setup {

      val formWithError: Form[Set[String]] =
        form.bind(Map("value" -> ""))

      val html: HtmlFormat.Appendable = view(formWithError, mode, checkboxItems, PaginationViewModel(), page = 1)

      val doc: Document = Jsoup.parse(html.body)

      doc.title must startWith(messages("error.title.prefix"))
      doc.select(".govuk-error-summary").size() mustBe 1
    }

    "must render error summary link targeting the first checkbox on the current page" in new Setup {

      val formWithError: Form[Set[String]] =
        form.bind(Map("value" -> ""))

      val allItems                      = SubcontractorViewModel.checkboxItems(SelectSubcontractorController.subcontractors)
      val page1Items: Seq[CheckboxItem] = allItems.slice(0, 6)
      val page2Items: Seq[CheckboxItem] = allItems.slice(6, 9)

      val docPage1: Document = Jsoup.parse(
        view(formWithError, mode, page1Items, PaginationViewModel(), page = 1).body
      )
      docPage1.select(".govuk-error-summary__list a").attr("href") mustBe "#value_0"

      val docPage2: Document = Jsoup.parse(
        view(formWithError, mode, page2Items, PaginationViewModel(), page = 2).body
      )
      docPage2.select(".govuk-error-summary__list a").attr("href") mustBe "#value_6"
    }

    "must render pagination when multiple pages exist" in new Setup {

      val pagination: PaginationViewModel = PaginationViewModel(
        items = Seq(
          PaginationItemViewModel("1", "").withCurrent(true),
          PaginationItemViewModel("2", "")
        ),
        next = Some(PaginationLinkViewModel("").withText("site.pagination.next"))
      )

      val html: HtmlFormat.Appendable = view(form, mode, checkboxItems, pagination, page = 1)

      val doc: Document = Jsoup.parse(html.body)

      doc.select(".govuk-pagination").size() mustBe 1
      doc.select(".govuk-pagination__item").size() mustBe 2
      doc.select(".govuk-pagination__next").size() mustBe 1
    }

    "must render previous and next on middle page" in new Setup {

      val pagination: PaginationViewModel = PaginationViewModel(
        items = Seq(
          PaginationItemViewModel("1", ""),
          PaginationItemViewModel("2", "").withCurrent(true),
          PaginationItemViewModel("3", "")
        ),
        previous = Some(PaginationLinkViewModel("").withText("site.pagination.previous")),
        next = Some(PaginationLinkViewModel("").withText("site.pagination.next"))
      )

      val html: HtmlFormat.Appendable = view(form, mode, checkboxItems, pagination, page = 2)

      val doc: Document = Jsoup.parse(html.body)

      doc.select(".govuk-pagination__prev").size() mustBe 1
      doc.select(".govuk-pagination__next").size() mustBe 1
    }

    "must NOT render pagination links when only one page" in new Setup {

      val html: HtmlFormat.Appendable = view(form, mode, checkboxItems, PaginationViewModel(), page = 1)

      val doc: Document = Jsoup.parse(html.body)

      doc.select(".govuk-pagination__item").size() mustBe 0
      doc.select(".govuk-pagination__prev").size() mustBe 0
      doc.select(".govuk-pagination__next").size() mustBe 0
    }

    "must render pagination buttons as submit controls with correct gotoPage values" in new Setup {

      val pagination: PaginationViewModel = PaginationViewModel(
        items = Seq(
          PaginationItemViewModel("1", "").withCurrent(true),
          PaginationItemViewModel("2", ""),
          PaginationItemViewModel("3", "")
        ),
        previous = Some(PaginationLinkViewModel("")),
        next = Some(PaginationLinkViewModel(""))
      )

      val html: HtmlFormat.Appendable = view(form, mode, checkboxItems, pagination, page = 2)
      val doc: Document               = Jsoup.parse(html.body)

      val prevButton: Elements =
        doc.select(".govuk-pagination__prev button[name=gotoPage][value=1]")

      prevButton.size mustBe 1
      prevButton.text must include(messages("site.pagination.previous"))

      doc.select(".govuk-pagination__item button[name=gotoPage][value=1]").size mustBe 1
      doc.select(".govuk-pagination__item button[name=gotoPage][value=2]").size mustBe 1
      doc.select(".govuk-pagination__item button[name=gotoPage][value=3]").size mustBe 1

      val nextButton: Elements =
        doc.select(".govuk-pagination__next button[name=gotoPage][value=3]")

      nextButton.size mustBe 1
      nextButton.text must include(messages("site.pagination.next"))
    }

    "must mark the current pagination item with aria-current" in new Setup {

      val pagination: PaginationViewModel = PaginationViewModel(
        items = Seq(
          PaginationItemViewModel("1", ""),
          PaginationItemViewModel("2", "").withCurrent(true),
          PaginationItemViewModel("3", "")
        )
      )

      val html: HtmlFormat.Appendable = view(form, mode, checkboxItems, pagination, page = 2)
      val doc: Document               = Jsoup.parse(html.body)

      doc.select(".govuk-pagination__item--current").size mustBe 1
      doc.select(".govuk-pagination__item--current [aria-current=page]").size mustBe 1
    }
  }

  trait Setup {
    val app: Application = applicationBuilder().build()

    implicit val request: FakeRequest[AnyContentAsEmpty.type] =
      FakeRequest(GET, "/")

    implicit val messages: Messages =
      app.injector.instanceOf[MessagesApi].preferred(request)

    val view: SelectSubcontractorView =
      app.injector.instanceOf[SelectSubcontractorView]

    val formProvider: SelectSubcontractorFormProvider =
      app.injector.instanceOf[SelectSubcontractorFormProvider]

    val form: Form[Set[String]] = formProvider()

    val checkboxItems: Seq[CheckboxItem] =
      SubcontractorViewModel.checkboxItems(SelectSubcontractorController.subcontractors)

    val mode: Mode = models.NormalMode
  }
}
