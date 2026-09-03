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

import viewmodels.govuk.PaginationFluency.*
import uk.gov.hmrc.govukfrontend.views.viewmodels.checkboxes.CheckboxItem

import javax.inject.{Inject, Singleton}

case class PaginationConfig(
  recordsPerPage: Int = 6,
  maxVisiblePages: Int = 2
)

final case class CheckboxPaginationResult(
  paginatedData: Seq[CheckboxItem],
  paginationViewModel: PaginationViewModel,
  startIndex: Int,
  totalCount: Int
)

@Singleton
class PaginationService(val config: PaginationConfig) {

  @Inject
  def this() = this(PaginationConfig())

  def paginateCheckboxItems(
    allItems: Seq[CheckboxItem],
    currentPage: Int
  ): CheckboxPaginationResult = {

    val totalPages = math.ceil(allItems.size.toDouble / config.recordsPerPage).toInt.max(1)
    val page       = currentPage.max(1).min(totalPages)

    val pageStart = (page - 1) * config.recordsPerPage
    val pageEnd   = pageStart + config.recordsPerPage
    val pageItems = allItems.slice(pageStart, pageEnd)

    val windowSize      = config.maxVisiblePages / 2
    val paginationStart = (page - windowSize).max(2)
    val paginationEnd   = (page + windowSize).min(totalPages - 1)

    val pages: Seq[PaginationItemViewModel] = {
      val firstPage     = PaginationItemViewModel("1", "").withCurrent(page == 1)
      val lastPage      = PaginationItemViewModel(totalPages.toString, "").withCurrent(page == totalPages)
      val middlePages   =
        (paginationStart to paginationEnd)
          .filter(p => p > 1 && p < totalPages)
          .map(p => PaginationItemViewModel(p.toString, "").withCurrent(p == page))
      val leftEllipsis  = if (paginationStart > 2) Seq(PaginationItemViewModel.ellipsis()) else Seq()
      val rightEllipsis = if (paginationEnd < totalPages - 1) Seq(PaginationItemViewModel.ellipsis()) else Seq()

      Seq(firstPage) ++
        leftEllipsis ++
        middlePages ++
        rightEllipsis ++
        (if (totalPages > 1) Seq(lastPage) else Seq())
    }

    val pagination =
      if (totalPages <= 1) PaginationViewModel()
      else
        PaginationViewModel()
          .withItems(pages)
          .copy(
            previous =
              if (page > 1) Some(PaginationLinkViewModel("").withText("site.pagination.previous"))
              else None,
            next =
              if (page < totalPages) Some(PaginationLinkViewModel("").withText("site.pagination.next"))
              else None
          )

    CheckboxPaginationResult(pageItems, pagination, pageStart + 1, allItems.size)
  }
}
