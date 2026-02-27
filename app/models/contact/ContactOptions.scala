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
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

sealed trait ContactOptions

object ContactOptions extends Enumerable.Implicits {

  case object Email    extends WithName("email")     with ContactOptions
  case object Phone    extends WithName("phone")     with ContactOptions
  case object Mobile   extends WithName("mobile")    with ContactOptions
  case object NoDetails extends WithName("noDetails") with ContactOptions

  private case object Or extends ContactOptions

  private val viewValues: Seq[ContactOptions] =
    Seq(Email, Phone, Mobile, Or, NoDetails)

  val values: Seq[ContactOptions] =
    viewValues.collect { case v: WithName => v.asInstanceOf[ContactOptions] }

  /** Provide a message prefix so callers can use different i18n keys */
  def options(messagePrefix: String)(implicit messages: Messages): Seq[RadioItem] =
    viewValues.zipWithIndex.map {
      case (value: WithName, index) =>
        RadioItem(
          content = Text(messages(s"$messagePrefix.${value.toString}")),
          value = Some(value.toString),
          id = Some(s"value_$index")
        )
      case (Or, _) =>
        RadioItem(divider = Some(messages(s"$messagePrefix.or")))
    }

  implicit val enumerable: Enumerable[ContactOptions] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
