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

package models.contact

import models.{Enumerable, WithName}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.checkboxes.CheckboxItem
import viewmodels.govuk.checkbox.*

sealed trait ContactMethodOptions

object ContactMethodOptions extends Enumerable.Implicits {

  case object Email extends WithName("email") with ContactMethodOptions
  case object Phone extends WithName("phone") with ContactMethodOptions
  case object Mobile extends WithName("mobile") with ContactMethodOptions

  val values: Seq[ContactMethodOptions] = Seq(Email, Phone, Mobile)

  def ordered(selected: Set[ContactMethodOptions]): Seq[ContactMethodOptions] = values.filter(selected.contains)

  /** Provide a message prefix so callers can use different i18n keys */
  def checkboxItems(messagePrefix: String)(implicit messages: Messages): Seq[CheckboxItem] =
    values.zipWithIndex.map { case (value, index) =>
      CheckboxItemViewModel(
        content = Text(messages(s"$messagePrefix.${value.toString}")),
        fieldId = "value",
        index = index,
        value = value.toString
      )
    }

  implicit val enumerable: Enumerable[ContactMethodOptions] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
