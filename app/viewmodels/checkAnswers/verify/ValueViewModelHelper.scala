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

package viewmodels.checkAnswers.verify

import uk.gov.hmrc.govukfrontend.views.viewmodels.content._
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist._

import viewmodels.govuk.summarylist.*

object ValueViewModelHelper {
  def makeGovukBulletList(values: Seq[String], shouldSort: Boolean = true): Option[Value] = {

    def sortIfNeeded(values: Seq[String]): Seq[String] =
      if (shouldSort) {
        values.sortBy(_.toLowerCase)
      } else {
        values
      }

    val valueHtml = values match {
      case Seq(single)                   => Some(single)
      case multiple if multiple.size > 1 =>
        Some(
          sortIfNeeded(multiple)
            .mkString("<ul class=\"govuk-list govuk-list--bullet\"><li>", "</li><li>", "</li></ul>")
        )
      case _                             => None
    }
    valueHtml.map(v => ValueViewModel(HtmlContent(v)))
  }
}
