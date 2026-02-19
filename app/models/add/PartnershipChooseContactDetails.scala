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

import models.Enumerable
import models.WithName
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import play.api.i18n.Messages

sealed trait PartnershipChooseContactDetails

object PartnershipChooseContactDetails extends Enumerable.Implicits {

  case object Email extends WithName("email") with PartnershipChooseContactDetails
  case object Phone extends WithName("phone") with PartnershipChooseContactDetails
  case object Mobile extends WithName("mobile") with PartnershipChooseContactDetails
  case object NoDetails extends WithName("noDetails") with PartnershipChooseContactDetails

  private case object Or extends PartnershipChooseContactDetails

  private val viewValues: Seq[PartnershipChooseContactDetails] = Seq(
    Email,
    Phone,
    Mobile,
    Or,
    NoDetails
  )

  val values: Seq[PartnershipChooseContactDetails] = viewValues.filter(_.isInstanceOf[WithName])

  def options(implicit messages: Messages): Seq[RadioItem] = viewValues.zipWithIndex.map {
    case (value: WithName, index) =>
      RadioItem(
        content = Text(messages(s"partnershipChooseContactDetails.${value.toString}")),
        value = Some(value.toString),
        id = Some(s"value_$index")
      )
    case (Or, _)                  =>
      RadioItem(divider = Some(messages("partnershipChooseContactDetails.or")))
  }

  implicit val enumerable: Enumerable[PartnershipChooseContactDetails] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
