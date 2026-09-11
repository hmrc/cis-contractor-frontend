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
  maxVisiblePages: Int = 2, // max number of pages visible either side of current page
  ellipsisPadding: Int = 1, // number of pages shown before/after ellipsis
  minEllipsisSpread: Int = 2 // minimum number of pages that ellipsis should replace
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

  private def buildPartialPageSeq(
    startIndex: Int,
    endIndex: Int,
    currentPage: Int
  ): IndexedSeq[PaginationItemViewModel] =
    (startIndex to endIndex)
      .map(p => PaginationItemViewModel(p.toString, "").withCurrent(p == currentPage))

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
    val paginationStart = (page - windowSize).max(config.ellipsisPadding + 1)
    val paginationEnd   = (page + windowSize).min(totalPages - (config.ellipsisPadding))

    val leftEllipsisSpread  = paginationStart - (config.ellipsisPadding + 1)
    val rightEllipsisSpread = totalPages - config.ellipsisPadding - paginationEnd

    val hasLeftGap  = paginationStart > config.ellipsisPadding + 1
    val hasRightGap = paginationEnd < (totalPages - config.ellipsisPadding)

    val showLeftEllipsis  = hasLeftGap && (leftEllipsisSpread >= config.minEllipsisSpread)
    val showRightEllipsis = hasRightGap && (rightEllipsisSpread >= config.minEllipsisSpread)

    val pages: Seq[PaginationItemViewModel] = {
      val firstPages  = buildPartialPageSeq(1, config.ellipsisPadding, page)
      val lastPages   = buildPartialPageSeq(totalPages - (config.ellipsisPadding - 1), totalPages, page)
      val middlePages = buildPartialPageSeq(paginationStart, paginationEnd, page)
      val leftFill    =
        if (showLeftEllipsis) Seq(PaginationItemViewModel.ellipsis())
        else if (hasLeftGap) buildPartialPageSeq(paginationStart - 1, paginationStart - 1, page)
        else Seq()
      val fillRight   =
        if (showRightEllipsis) Seq(PaginationItemViewModel.ellipsis())
        else if (hasRightGap) buildPartialPageSeq(paginationEnd + 1, paginationEnd + 1, page)
        else Seq()

      firstPages ++ leftFill ++ middlePages ++ fillRight ++ (if (totalPages > 1) lastPages else Seq())
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
