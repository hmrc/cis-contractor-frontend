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

package viewmodels.unmatched

import models.unmatched.{UnmatchedSubcontractorDetailsUpdated, UnmatchedSubcontractorUpdate}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.TableRow

object UnmatchedSubcontractorDetailsUpdatedViewModel {

  def rows(
    confirmationData: UnmatchedSubcontractorDetailsUpdated
  )(implicit messages: Messages): Seq[Seq[TableRow]] =
    confirmationData.updates
      .filter(update => normalise(update.previous) != normalise(update.updated))
      .map(row)

  private def row(
    update: UnmatchedSubcontractorUpdate
  )(implicit messages: Messages): Seq[TableRow] =
    Seq(
      TableRow(
        content = Text(update.detail),
        classes = "govuk-!-font-weight-bold"
      ),
      TableRow(
        content = Text(displayValue(update.previous, update.missingValueKey))
      ),
      TableRow(
        content = Text(displayValue(update.updated, update.missingValueKey))
      )
    )

  private def displayValue(
    value: Option[String],
    missingValueKey: String
  )(implicit messages: Messages): String =
    value match {
      case Some(v) if v.trim.nonEmpty =>
        v

      case _ =>
        messages(missingValueKey)
    }

  private def normalise(value: Option[String]): String =
    value.map(_.trim).getOrElse("")
}
