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

package models.add

import models.contact.ContactOptions
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.checkboxes.CheckboxItem
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem
import viewmodels.govuk.checkbox.CheckboxItemViewModel
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text

type IndividualContactMethodOptions = ContactOptions

object IndividualContactMethodOptions {
  val values: Seq[IndividualContactMethodOptions] = ContactOptions.values

  def options(implicit messages: Messages): Seq[RadioItem] = {
    ContactOptions.options("individualContactMethodOptions")
  }

  def checkboxItems(
                     rows: Seq[IndividualContactMethodOptions],
                     selected: Set[String]
                   ): Seq[CheckboxItem] =
    rows.zipWithIndex.map { case (row, index) =>
      CheckboxItemViewModel(
        content = Text(row.toString()),
        fieldId = s"value-$index",
        index = index,
        value = "1"
      )
    }
}
