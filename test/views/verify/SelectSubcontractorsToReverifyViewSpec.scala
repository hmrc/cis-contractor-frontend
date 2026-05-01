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
import models.{Mode, NormalMode}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.matchers.must.Matchers
import forms.verify.SelectSubcontractorsToReverifyFormProvider
import play.api.Application
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import viewmodels.govuk.PaginationFluency._
import viewmodels.verify.{SubcontractorReverifyData, SubcontractorReverifyRow}
import views.html.verify.SelectSubcontractorsToReverifyView

class SelectSubcontractorsToReverifyViewSpec extends SpecBase with Matchers {

  "SelectSubcontractorsToReverifyView" - {

    "must render heading, hint, table, pagination and continue button" in new Setup {

      val html: HtmlFormat.Appendable =
        view(
          form,
          mode,
          rows,
          pagination,
          page = 1,
          startIndex = 1,
          totalCount = rows.size
        )

      val doc: Document = Jsoup.parse(html.body)

      doc.title must include(messages("verify.selectSubcontractorsToReverify.title"))

      doc.select("h1").text mustBe messages("verify.selectSubcontractorsToReverify.heading")

      doc.select(".govuk-hint").text mustBe messages(
        "verify.selectSubcontractorsToReverify.hint"
      )

      doc.select("#subcontractor-table tbody tr").size() must be > 0

      doc.select("input[type=checkbox]").size() mustBe rows.size

      doc.select("button").text must include(messages("site.continue"))
    }

    "must render showing results text when pagination exists" in new Setup {

      val html = view(
        form,
        mode,
        rows,
        pagination,
        page = 1,
        startIndex = 1,
        totalCount = rows.size
      )

      val doc = Jsoup.parse(html.body)

      doc.text must include(
        s"1 to ${1 + rows.size - 1} of ${rows.size}"
      )
    }

    "must NOT render showing results text when pagination is empty" in new Setup {

      val html = view(
        form,
        mode,
        rows,
        PaginationViewModel(),
        page = 1,
        startIndex = 1,
        totalCount = rows.size
      )

      val doc = Jsoup.parse(html.body)

      doc.select("p.govuk-body").text() must not include "of"
    }

    "must render pagination when items exist" in new Setup {

      val paginationWithItems =
        PaginationViewModel(
          items = Seq(
            PaginationItemViewModel("1", "").withCurrent(true),
            PaginationItemViewModel("2", "")
          ),
          next = Some(PaginationLinkViewModel("").withText("site.pagination.next"))
        )

      val html = view(form, mode, rows, paginationWithItems, 1, 1, rows.size)

      val doc = Jsoup.parse(html.body)

      doc.select(".govuk-pagination").size() mustBe 1
    }

    "must render error summary when form has errors" in new Setup {

      val formWithError =
        new SelectSubcontractorsToReverifyFormProvider()(requireSelection = true)
          .bind(Map.empty[String, String])

      val html = view(
        formWithError,
        mode,
        rows,
        pagination,
        1,
        1,
        rows.size
      )

      val doc = Jsoup.parse(html.body)

      doc.select(".govuk-error-summary").size() mustBe 1
    }
  }

  trait Setup {

    val app: Application = applicationBuilder().build()

    implicit val request: FakeRequest[_] =
      FakeRequest()

    implicit val messages: Messages =
      MessagesImpl(Lang.defaultLang, app.injector.instanceOf[MessagesApi])

    val view: SelectSubcontractorsToReverifyView =
      app.injector.instanceOf[SelectSubcontractorsToReverifyView]

    val form = new SelectSubcontractorsToReverifyFormProvider()(requireSelection = false)

    val rows: Seq[SubcontractorReverifyRow] =
      SubcontractorReverifyData.rows.take(6)

    val pagination: PaginationViewModel =
      PaginationViewModel(
        items = Seq(
          PaginationItemViewModel("1", "").withCurrent(true),
          PaginationItemViewModel("2", "")
        ),
        next = Some(PaginationLinkViewModel("").withText("site.pagination.next"))
      )

    val mode: Mode = NormalMode
  }
}
