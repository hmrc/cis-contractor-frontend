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

package views.components

import base.SpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.matchers.must.Matchers
import play.api.i18n.Messages
import play.api.test.FakeRequest
import viewmodels.govuk.PaginationFluency._
import views.html.components.PageNavigator

class PageNavigatorSpec extends SpecBase with Matchers {

  "PageNavigator" - {

    "must render nothing when there are no pagination items" in new Setup {
      val html = pageNavigator(PaginationViewModel(), page = 1)
      val doc  = Jsoup.parse(html.body)
      doc.select(".govuk-pagination").size() mustBe 0
    }

    "must render the pagination nav when items are present" in new Setup {
      val pagination = PaginationViewModel(
        items = Seq(
          PaginationItemViewModel("1", "/test?page=1").withCurrent(true),
          PaginationItemViewModel("2", "/test?page=2")
        )
      )
      val doc        = parse(pageNavigator(pagination, page = 1))
      doc.select(".govuk-pagination").size() mustBe 1
      doc.select(".govuk-pagination__item").size() mustBe 2
    }

    "must render a previous button with value page - 1" in new Setup {
      val pagination = PaginationViewModel(
        items = Seq(PaginationItemViewModel("2", "/test?page=2").withCurrent(true)),
        previous = Some(PaginationLinkViewModel("/test?page=1"))
      )
      val doc        = parse(pageNavigator(pagination, page = 2))
      val prevButton = doc.select(".govuk-pagination__prev button[name=gotoPage][value=1]")
      prevButton.size() mustBe 1
      prevButton.text() must include(messages("site.pagination.previous"))
    }

    "must render a next button with value page + 1" in new Setup {
      val pagination = PaginationViewModel(
        items = Seq(PaginationItemViewModel("1", "/test?page=1").withCurrent(true)),
        next = Some(PaginationLinkViewModel("/test?page=2"))
      )
      val doc        = parse(pageNavigator(pagination, page = 1))
      val nextButton = doc.select(".govuk-pagination__next button[name=gotoPage][value=2]")
      nextButton.size() mustBe 1
      nextButton.text() must include(messages("site.pagination.next"))
    }

    "must not render previous when not set" in new Setup {
      val pagination = PaginationViewModel(
        items = Seq(PaginationItemViewModel("1", "/test?page=1").withCurrent(true))
      )
      val doc        = parse(pageNavigator(pagination, page = 1))
      doc.select(".govuk-pagination__prev").size() mustBe 0
    }

    "must not render next when not set" in new Setup {
      val pagination = PaginationViewModel(
        items = Seq(PaginationItemViewModel("1", "/test?page=1").withCurrent(true))
      )
      val doc        = parse(pageNavigator(pagination, page = 1))
      doc.select(".govuk-pagination__next").size() mustBe 0
    }

    "must mark the current item with govuk-pagination__item--current and aria-current" in new Setup {
      val pagination = PaginationViewModel(
        items = Seq(
          PaginationItemViewModel("1", "/test?page=1"),
          PaginationItemViewModel("2", "/test?page=2").withCurrent(true),
          PaginationItemViewModel("3", "/test?page=3")
        )
      )
      val doc        = parse(pageNavigator(pagination, page = 2))
      doc.select(".govuk-pagination__item--current").size() mustBe 1
      doc.select(".govuk-pagination__item--current [aria-current=page]").size() mustBe 1
    }

    "must render page number buttons with correct gotoPage values" in new Setup {
      val pagination = PaginationViewModel(
        items = Seq(
          PaginationItemViewModel("1", "/test?page=1"),
          PaginationItemViewModel("2", "/test?page=2"),
          PaginationItemViewModel("3", "/test?page=3")
        )
      )
      val doc        = parse(pageNavigator(pagination, page = 1))
      doc.select(".govuk-pagination__item button[name=gotoPage][value=1]").size() mustBe 1
      doc.select(".govuk-pagination__item button[name=gotoPage][value=2]").size() mustBe 1
      doc.select(".govuk-pagination__item button[name=gotoPage][value=3]").size() mustBe 1
    }

    "must use the landmark label from the view model" in new Setup {
      val pagination = PaginationViewModel(
        items = Seq(PaginationItemViewModel("1", "/test?page=1")),
        landmarkLabel = "site.pagination.landmark"
      )
      val doc        = parse(pageNavigator(pagination, page = 1))
      doc.select(".govuk-pagination").attr("aria-label") mustBe messages("site.pagination.landmark")
    }
  }

  trait Setup {
    val app                                                                = applicationBuilder().build()
    val pageNavigator: PageNavigator                                       = app.injector.instanceOf[PageNavigator]
    implicit val request: FakeRequest[play.api.mvc.AnyContentAsEmpty.type] = FakeRequest()
    implicit val messages: Messages                                        = app.injector.instanceOf[play.api.i18n.MessagesApi].preferred(request)

    def parse(html: play.twirl.api.Html): Document = Jsoup.parse(html.body)
  }
}
