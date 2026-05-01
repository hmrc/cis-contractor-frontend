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

package models.verify

import uk.gov.hmrc.govukfrontend.views.viewmodels.checkboxes.CheckboxItem
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import viewmodels.govuk.checkbox.CheckboxItemViewModel
import viewmodels.verify.SubcontractorReverifyRow

object SubcontractorsToReverifyViewModel extends Enumeration {

  def checkboxItems(
    rows: Seq[SubcontractorReverifyRow],
    selected: Set[String]
  ): Seq[CheckboxItem] =
    rows.zipWithIndex.map { case (row, index) =>
      CheckboxItemViewModel(
        content = Text(row.name),
        fieldId = s"value-$index",
        index = index,
        value = row.id
      )
    }

  def extractSelected(formData: Map[String, String]): Set[String] =
    formData
      .get("value")
      .toSeq
      .flatMap(_.split(","))
      .map(_.trim)
      .filter(_.nonEmpty)
      .toSet
}
