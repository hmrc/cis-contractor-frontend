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

package services

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.must.Matchers
import uk.gov.hmrc.govukfrontend.views.viewmodels.checkboxes.CheckboxItem

class PaginationServiceSpec extends AnyWordSpec with Matchers {

  private val service = new PaginationService()
  private val baseUrl = "/test-url"

  private def checkbox(id: String, content: String): CheckboxItem =
    CheckboxItem(
      name = Some("value"),
      value = id,
      content = uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text(content)
    )

  private def items(n: Int): Seq[CheckboxItem] =
    (1 to n).map(i => checkbox(i.toString, s"Item $i"))

  "PaginationService.paginateCheckboxItems" should {

    "return single page when no items exist" in {
      val result = service.paginateCheckboxItems(Seq.empty, 1, baseUrl)

      result.paginatedData mustBe empty
      result.paginationViewModel.items mustBe empty
      result.paginationViewModel.previous mustBe None
      result.paginationViewModel.next mustBe None
    }

    "return single page when items are within page size (<= 6)" in {
      val result = service.paginateCheckboxItems(items(6), 1, baseUrl)

      result.paginatedData.length mustBe 6
      result.paginationViewModel.items mustBe empty
      result.paginationViewModel.previous mustBe None
      result.paginationViewModel.next mustBe None
    }

    "paginate 7 items into 2 pages" in {
      val result = service.paginateCheckboxItems(items(7), 1, baseUrl)

      result.paginatedData.length mustBe 6
      result.paginatedData.head.value mustBe "1"

      result.paginationViewModel.items.length mustBe 2
      result.paginationViewModel.next.isDefined mustBe true
      result.paginationViewModel.previous mustBe None
    }

    "return correct second page data" in {
      val result = service.paginateCheckboxItems(items(12), 2, baseUrl)

      result.paginatedData.length mustBe 6
      result.paginatedData.head.value mustBe "7"

      result.paginationViewModel.previous.isDefined mustBe true
      result.paginationViewModel.next.isDefined mustBe false
    }

    "clamp page to minimum (page 0 becomes page 1)" in {
      val result = service.paginateCheckboxItems(items(10), 0, baseUrl)

      result.paginatedData.head.value mustBe "1"
      result.paginationViewModel.items.exists(_.current) mustBe true
    }

    "clamp page to maximum when page too high" in {
      val result = service.paginateCheckboxItems(items(10), 99, baseUrl)

      // 10 items → 2 pages
      result.paginatedData.head.value mustBe "7"
      result.paginationViewModel.previous.isDefined mustBe true
      result.paginationViewModel.next mustBe None
    }

    "generate correct pagination links" in {
      val result = service.paginateCheckboxItems(items(20), 2, baseUrl)

      result.paginationViewModel.previous.get.href mustBe s"$baseUrl?page=1"
      result.paginationViewModel.next.get.href mustBe s"$baseUrl?page=3"

      result.paginationViewModel.items.head.href mustBe s"$baseUrl?page=1"
    }

    "mark current page correctly" in {
      val result = service.paginateCheckboxItems(items(20), 2, baseUrl)

      val current = result.paginationViewModel.items.find(_.current)
      current.isDefined mustBe true
      current.get.number mustBe "2"
    }

    "handle exact multiple of page size correctly" in {
      val result = service.paginateCheckboxItems(items(12), 2, baseUrl)

      // page 1: 1-6, page 2: 7-12
      result.paginatedData.map(_.value) mustBe Seq("7","8","9","10","11","12")
      result.paginationViewModel.next mustBe None
      result.paginationViewModel.previous.isDefined mustBe true
    }

    "ensure page size is always 6" in {
      val result = service.paginateCheckboxItems(items(100), 1, baseUrl)

      result.paginatedData.length mustBe 6
    }

    "generate full pagination structure for 3 pages" in {
      val result = service.paginateCheckboxItems(items(20), 2, baseUrl)

      result.paginationViewModel.items.length mustBe 4 // 20 items → 4 pages (6,6,6,2)

      result.paginationViewModel.items.map(_.number) must contain allOf ("1", "2", "3", "4")
    }
  }
}
