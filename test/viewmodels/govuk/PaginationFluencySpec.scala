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

package viewmodels.govuk

import org.scalatestplus.play.PlaySpec
import play.api.i18n.Messages
import play.api.i18n.MessagesApi
import play.api.test.FakeRequest
import play.api.test.Helpers._

class PaginationFluencySpec extends PlaySpec {

  implicit val messagesApi: MessagesApi =
    stubMessagesApi()

  implicit val messages: Messages =
    messagesApi.preferred(FakeRequest())

  "PaginationFluency PaginationViewModel" should {

    "build empty PaginationViewModel using apply()" in {
      val result = PaginationFluency.PaginationViewModel()

      result.items mustBe Seq.empty
      result.previous mustBe None
      result.next mustBe None
      result.landmarkLabel mustBe "site.pagination.landmark"
    }

    "build PaginationViewModel with items" in {
      val item = PaginationFluency.PaginationItemViewModel("1", "/test")

      val result =
        PaginationFluency.PaginationViewModel(Seq(item))

      result.items.size mustBe 1
      result.items.head.number mustBe "1"
    }

    "support withPrevious and withNext" in {
      val previous = PaginationFluency.PaginationLinkViewModel("/prev")
      val next     = PaginationFluency.PaginationLinkViewModel("/next")

      val result =
        PaginationFluency
          .PaginationViewModel()
          .withPrevious(previous)
          .withNext(next)

      result.previous.isDefined mustBe true
      result.next.isDefined mustBe true
    }

    "convert to GOV.UK Pagination model" in {
      val item =
        PaginationFluency.PaginationItemViewModel("1", "/page/1")

      val result =
        PaginationFluency.PaginationViewModel(Seq(item)).asPagination

      result.items.get.head.number mustBe Some("1")
    }
  }

  "PaginationItemViewModel" should {

    "create normal item" in {
      val item =
        PaginationFluency.PaginationItemViewModel("2", "/page/2")

      item.number mustBe "2"
      item.href mustBe "/page/2"
      item.ellipsis mustBe false
    }

    "create ellipsis item" in {
      val item =
        PaginationFluency.PaginationItemViewModel.ellipsis()

      item.ellipsis mustBe true
      item.number mustBe ""
    }

    "support withCurrent and withVisuallyHiddenText" in {
      val item =
        PaginationFluency
          .PaginationItemViewModel("1", "/page/1")
          .withCurrent(true)
          .withVisuallyHiddenText("Page 1")

      item.current mustBe true
      item.visuallyHiddenText mustBe Some("Page 1")
    }
  }

  "PaginationLinkViewModel" should {

    "build link with text and label" in {
      val link =
        PaginationFluency
          .PaginationLinkViewModel("/next")
          .withText("Next")
          .withLabelText("Go to next page")

      link.href mustBe "/next"
      link.text mustBe Some("Next")
      link.labelText mustBe Some("Go to next page")
    }

    "convert to PaginationLink" in {
      val link =
        PaginationFluency
          .PaginationLinkViewModel("/next")
          .withText("Next")

      val result =
        link.asPaginationLink

      result.href mustBe "/next"
      result.text mustBe Some("Next")
    }
  }
}
