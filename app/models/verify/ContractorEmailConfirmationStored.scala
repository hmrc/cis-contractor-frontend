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

import models.{Enumerable, WithName}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

sealed trait ContractorEmailConfirmationStored

object ContractorEmailConfirmationStored extends Enumerable.Implicits {

  case object CurrentEmail extends WithName("currentEmail") with ContractorEmailConfirmationStored
  case object DifferentEmail extends WithName("differentEmail") with ContractorEmailConfirmationStored
  case object DoNotSend extends WithName("doNotSend") with ContractorEmailConfirmationStored

  val values: Seq[ContractorEmailConfirmationStored] = Seq(CurrentEmail, DifferentEmail, DoNotSend)

  def options(implicit messages: Messages): Seq[RadioItem] = {
    val prefix = "verify.contractorEmailConfirmationStored"
    Seq(
      RadioItem(
        content = Text(messages(s"$prefix.currentEmail")),
        value = Some("currentEmail"),
        id = Some("value_0")
      ),
      RadioItem(
        content = Text(messages(s"$prefix.differentEmail")),
        value = Some("differentEmail"),
        id = Some("value_1")
      ),
      RadioItem(divider = Some(messages(s"$prefix.or"))),
      RadioItem(
        content = Text(messages(s"$prefix.doNotSend")),
        value = Some("doNotSend"),
        id = Some("value_2")
      )
    )
  }

  implicit val enumerable: Enumerable[ContractorEmailConfirmationStored] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
