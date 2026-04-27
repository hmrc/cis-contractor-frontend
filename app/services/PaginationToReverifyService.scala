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

import javax.inject.{Inject, Singleton}
import viewmodels.govuk.PaginationFluency._

@Singleton
class PaginationToReverifyService @Inject() () {

  private val defaultRecordsPerPage = 6
  private val maxVisiblePages       = 6

  case class PaginatedResult[T](
    items: Seq[T],
    pagination: PaginationViewModel,
    currentPage: Int,
    totalPages: Int,
    startIndex: Int,
    totalCount: Int
  )

  def paginate[T](
    allItems: Seq[T],
    currentPage: Int,
    recordsPerPage: Int = defaultRecordsPerPage,
    baseUrl: String,
    pageParam: String = "page"
  ): PaginatedResult[T] = {

    val totalCount = allItems.size

    val totalPages =
      math.ceil(allItems.size.toDouble / recordsPerPage).toInt.max(1)

    val page =
      currentPage.max(1).min(totalPages)

    val start = (page - 1) * recordsPerPage
    val end   = start + recordsPerPage

    val pageItems = allItems.slice(start, end)

    PaginatedResult(
      items = pageItems,
      pagination = buildPagination(page, totalPages, baseUrl, pageParam),
      currentPage = page,
      totalPages = totalPages,
      startIndex = start + 1,
      totalCount = totalCount
    )
  }

  private def buildPagination(
    page: Int,
    totalPages: Int,
    baseUrl: String,
    pageParam: String
  ): PaginationViewModel =
    if (totalPages <= 1) {
      PaginationViewModel()
    } else {

      val half = maxVisiblePages / 2
      val from = (page - half).max(1)
      val to   = (from + maxVisiblePages - 1).min(totalPages)

      PaginationViewModel()
        .withItems(
          (from to to).map { p =>
            PaginationItemViewModel(
              number = p.toString,
              href = s"$baseUrl?$pageParam=$p"
            ).withCurrent(p == page)
          }
        )
        .copy(
          previous =
            if (page > 1)
              Some(
                PaginationLinkViewModel(
                  href = s"$baseUrl?$pageParam=${page - 1}"
                ).withText("site.pagination.previous")
              )
            else None,
          next =
            if (page < totalPages)
              Some(
                PaginationLinkViewModel(
                  href = s"$baseUrl?$pageParam=${page + 1}"
                ).withText("site.pagination.next")
              )
            else None
        )
    }
}
