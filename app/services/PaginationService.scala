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
  maxVisiblePages: Int = 5
)

final case class CheckboxPaginationResult(
  paginatedData: Seq[CheckboxItem],
  paginationViewModel: PaginationViewModel
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

    val start = (page - 1) * config.recordsPerPage
    val end   = start + config.recordsPerPage

    val pageItems = allItems.slice(start, end)

    val pagination =
      if (totalPages <= 1) PaginationViewModel()
      else
        PaginationViewModel()
          .withItems(
            (1 to totalPages).map { p =>
              PaginationItemViewModel(number = p.toString, href = "")
                .withCurrent(p == page)
            }
          )
          .copy(
            previous =
              if (page > 1) Some(PaginationLinkViewModel("").withText("site.pagination.previous"))
              else None,
            next =
              if (page < totalPages) Some(PaginationLinkViewModel("").withText("site.pagination.next"))
              else None
          )

    CheckboxPaginationResult(pageItems, pagination)
  }
}
