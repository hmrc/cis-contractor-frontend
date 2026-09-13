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

      val doc = parse(pageNavigator(pagination, page = 1))

      doc.select(".govuk-pagination").size() mustBe 1
      doc.select(".govuk-pagination__item").size() mustBe 2
    }

    "must render a previous button with value page - 1" in new Setup {
      val pagination = PaginationViewModel(
        items = Seq(
          PaginationItemViewModel("2", "/test?page=2").withCurrent(true)
        ),
        previous = Some(PaginationLinkViewModel("/test?page=1"))
      )

      val doc = parse(pageNavigator(pagination, page = 2))

      val prevButton =
        doc.select(".govuk-pagination__prev button[name=gotoPage][value=1]")

      prevButton.size() mustBe 1
      prevButton.text() must include(
        messages("site.pagination.previous")
      )
    }

    "must render visually hidden text for the previous button" in new Setup {
      val pagination = PaginationViewModel(
        items = Seq(
          PaginationItemViewModel("2", "/test?page=2").withCurrent(true)
        ),
        previous = Some(PaginationLinkViewModel("/test?page=1"))
      )

      val doc = parse(pageNavigator(pagination, page = 2))

      val hiddenText =
        doc.select(".govuk-pagination__prev .govuk-visually-hidden")

      hiddenText.size() mustBe 1
      hiddenText.text() mustBe
        messages("site.pagination.goToPrevious")
    }

    "must render previous button with the correct attributes" in new Setup {
      val pagination = PaginationViewModel(
        items = Seq(
          PaginationItemViewModel("2", "/test?page=2").withCurrent(true)
        ),
        previous = Some(PaginationLinkViewModel("/test?page=1"))
      )

      val doc = parse(pageNavigator(pagination, page = 2))

      val prevButton =
        doc.select(".govuk-pagination__prev button")

      prevButton.size() mustBe 1
      prevButton.attr("type") mustBe "submit"
      prevButton.attr("name") mustBe "gotoPage"
      prevButton.attr("value") mustBe "1"
      prevButton.attr("rel") mustBe "prev"
      prevButton.hasClass("govuk-pagination__link--button") mustBe true
    }

    "must render a next button with value page + 1" in new Setup {
      val pagination = PaginationViewModel(
        items = Seq(
          PaginationItemViewModel("1", "/test?page=1").withCurrent(true)
        ),
        next = Some(PaginationLinkViewModel("/test?page=2"))
      )

      val doc = parse(pageNavigator(pagination, page = 1))

      val nextButton =
        doc.select(".govuk-pagination__next button[name=gotoPage][value=2]")

      nextButton.size() mustBe 1
      nextButton.text() must include(
        messages("site.pagination.next")
      )
    }

    "must render visually hidden text for the next button" in new Setup {
      val pagination = PaginationViewModel(
        items = Seq(
          PaginationItemViewModel("1", "/test?page=1").withCurrent(true)
        ),
        next = Some(PaginationLinkViewModel("/test?page=2"))
      )

      val doc = parse(pageNavigator(pagination, page = 1))

      val hiddenText =
        doc.select(".govuk-pagination__next .govuk-visually-hidden")

      hiddenText.size() mustBe 1
      hiddenText.text() mustBe
        messages("site.pagination.goToNext")
    }

    "must render next button with the correct attributes" in new Setup {
      val pagination = PaginationViewModel(
        items = Seq(
          PaginationItemViewModel("1", "/test?page=1").withCurrent(true)
        ),
        next = Some(PaginationLinkViewModel("/test?page=2"))
      )

      val doc = parse(pageNavigator(pagination, page = 1))

      val nextButton =
        doc.select(".govuk-pagination__next button")

      nextButton.size() mustBe 1
      nextButton.attr("type") mustBe "submit"
      nextButton.attr("name") mustBe "gotoPage"
      nextButton.attr("value") mustBe "2"
      nextButton.attr("rel") mustBe "next"
      nextButton.hasClass("govuk-pagination__link--button") mustBe true
    }

    "must not render previous when not set" in new Setup {
      val pagination = PaginationViewModel(
        items = Seq(
          PaginationItemViewModel("1", "/test?page=1").withCurrent(true)
        )
      )

      val doc = parse(pageNavigator(pagination, page = 1))

      doc.select(".govuk-pagination__prev").size() mustBe 0
    }

    "must not render next when not set" in new Setup {
      val pagination = PaginationViewModel(
        items = Seq(
          PaginationItemViewModel("1", "/test?page=1").withCurrent(true)
        )
      )

      val doc = parse(pageNavigator(pagination, page = 1))

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

      val doc = parse(pageNavigator(pagination, page = 2))

      doc.select(".govuk-pagination__item--current").size() mustBe 1
      doc.select(".govuk-pagination__item--current button[aria-current=page]").size() mustBe 1
    }

    "must render page number buttons with correct gotoPage values" in new Setup {
      val pagination = PaginationViewModel(
        items = Seq(
          PaginationItemViewModel("1", "/test?page=1"),
          PaginationItemViewModel("2", "/test?page=2"),
          PaginationItemViewModel("3", "/test?page=3")
        )
      )

      val doc = parse(pageNavigator(pagination, page = 1))

      doc.select(".govuk-pagination__item button[name=gotoPage][value=1]").size() mustBe 1
      doc.select(".govuk-pagination__item button[name=gotoPage][value=2]").size() mustBe 1
      doc.select(".govuk-pagination__item button[name=gotoPage][value=3]").size() mustBe 1
    }

    "must render the correct aria-label for each page number button" in new Setup {
      val pagination = PaginationViewModel(
        items = Seq(
          PaginationItemViewModel("1", "/test?page=1"),
          PaginationItemViewModel("2", "/test?page=2"),
          PaginationItemViewModel("3", "/test?page=3")
        )
      )

      val doc = parse(pageNavigator(pagination, page = 1))

      val buttons =
        doc.select(".govuk-pagination__item button")

      buttons.size() mustBe 3

      buttons.get(0).attr("aria-label") mustBe
        messages("site.pagination.goToPage", "1")

      buttons.get(1).attr("aria-label") mustBe
        messages("site.pagination.goToPage", "2")

      buttons.get(2).attr("aria-label") mustBe
        messages("site.pagination.goToPage", "3")
    }

    "must render the page number as the button text" in new Setup {
      val pagination = PaginationViewModel(
        items = Seq(
          PaginationItemViewModel("1", "/test?page=1"),
          PaginationItemViewModel("2", "/test?page=2"),
          PaginationItemViewModel("3", "/test?page=3")
        )
      )

      val doc = parse(pageNavigator(pagination, page = 1))

      val buttons =
        doc.select(".govuk-pagination__item button")

      buttons.get(0).text() mustBe "1"
      buttons.get(1).text() mustBe "2"
      buttons.get(2).text() mustBe "3"
    }

    "must use the landmark label from the view model" in new Setup {
      val pagination = PaginationViewModel(
        items = Seq(
          PaginationItemViewModel("1", "/test?page=1")
        ),
        landmarkLabel = "site.pagination.landmark"
      )

      val doc = parse(pageNavigator(pagination, page = 1))

      doc.select(".govuk-pagination").attr("aria-label") mustBe
        messages("site.pagination.landmark")
    }

    "must render previous and next buttons together when both are present" in new Setup {
      val pagination = PaginationViewModel(
        items = Seq(
          PaginationItemViewModel("2", "/test?page=2").withCurrent(true)
        ),
        previous = Some(PaginationLinkViewModel("/test?page=1")),
        next = Some(PaginationLinkViewModel("/test?page=3"))
      )

      val doc = parse(pageNavigator(pagination, page = 2))

      doc.select(".govuk-pagination__prev button").size() mustBe 1
      doc.select(".govuk-pagination__next button").size() mustBe 1

      doc.select(".govuk-pagination__prev button").attr("value") mustBe "1"
      doc.select(".govuk-pagination__next button").attr("value") mustBe "3"
    }
  }

  trait Setup {
    val app           = applicationBuilder().build()
    val pageNavigator = app.injector.instanceOf[PageNavigator]

    implicit val request: FakeRequest[play.api.mvc.AnyContentAsEmpty.type] =
      FakeRequest()

    implicit val messages: Messages =
      app.injector
        .instanceOf[play.api.i18n.MessagesApi]
        .preferred(request)

    def parse(html: play.twirl.api.Html): Document =
      Jsoup.parse(html.body)
  }
}
