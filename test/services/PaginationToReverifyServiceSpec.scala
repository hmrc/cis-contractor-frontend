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

class PaginationToReverifyServiceSpec extends AnyWordSpec with Matchers {

  private val service = new PaginationToReverifyService()

  private def items(n: Int): Seq[String] =
    (1 to n).map(i => s"Item $i")

  private val baseUrl = "/test-url"

  "PaginationToReverifyService.paginate" should {

    "return empty result when no items exist" in {
      val result = service.paginate(Seq.empty[String], 1, baseUrl = baseUrl)

      result.items mustBe empty
      result.pagination.items mustBe empty
      result.pagination.previous mustBe None
      result.pagination.next mustBe None
      result.totalCount mustBe 0
    }

    "return single page when items fit within page size" in {
      val result = service.paginate(items(6), 1, baseUrl = baseUrl)

      result.items.length mustBe 6
      result.pagination.items mustBe empty
      result.pagination.previous mustBe None
      result.pagination.next mustBe None
      result.totalPages mustBe 1
    }

    "paginate correctly when more than one page exists" in {
      val result = service.paginate(items(7), 1, baseUrl = baseUrl)

      result.items.length mustBe 6
      result.items.head mustBe "Item 1"

      result.pagination.items.length mustBe 2
      result.pagination.next.isDefined mustBe true
      result.pagination.previous mustBe None
    }

    "return correct second page data" in {
      val result = service.paginate(items(12), 2, baseUrl = baseUrl)

      result.items.length mustBe 6
      result.items.head mustBe "Item 7"

      result.pagination.previous.isDefined mustBe true
      result.pagination.next mustBe None
    }

    "clamp page to minimum when page is less than 1" in {
      val result = service.paginate(items(10), 0, baseUrl = baseUrl)

      result.currentPage mustBe 1
      result.items.head mustBe "Item 1"
    }

    "clamp page to maximum when page exceeds total pages" in {
      val result = service.paginate(items(10), 99, baseUrl = baseUrl)

      result.currentPage mustBe 2
      result.items.head mustBe "Item 7"
    }

    "mark current page correctly in pagination model" in {
      val result = service.paginate(items(20), 2, baseUrl = baseUrl)

      val current = result.pagination.items.find(_.current)
      current.isDefined mustBe true
      current.get.number mustBe "2"
    }

    "handle exact multiple of page size correctly" in {
      val result = service.paginate(items(12), 2, baseUrl = baseUrl)

      result.items mustBe Seq("Item 7", "Item 8", "Item 9", "Item 10", "Item 11", "Item 12")
      result.pagination.next mustBe None
      result.pagination.previous.isDefined mustBe true
    }

    "generate correct pagination links" in {
      val result = service.paginate(items(20), 2, baseUrl = baseUrl)

      val links = result.pagination.items.map(_.href)

      links must contain("/test-url?page=1")
      links must contain("/test-url?page=2")
      links must contain("/test-url?page=3")
    }

    "generate previous and next links correctly" in {
      val result = service.paginate(items(20), 2, baseUrl = baseUrl)

      result.pagination.previous.get.href mustBe "/test-url?page=1"
      result.pagination.next.get.href mustBe "/test-url?page=3"
    }

    "calculate startIndex correctly" in {
      val result = service.paginate(items(20), 2, baseUrl = baseUrl)

      result.startIndex mustBe 7
    }

    "respect custom recordsPerPage parameter" in {
      val result = service.paginate(items(10), 1, recordsPerPage = 3, baseUrl = baseUrl)

      result.items.length mustBe 3
      result.totalPages mustBe 4
    }
  }
}
