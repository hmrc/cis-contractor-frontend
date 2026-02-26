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

package models.add.company

import models.{Enumerable, WithName}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

sealed trait CompanyContactDetailsOptions

object CompanyContactDetailsOptions extends Enumerable.Implicits {

  case object EmailAddress extends WithName("emailAddress") with CompanyContactDetailsOptions
  case object PhoneNumber extends WithName("phoneNumber") with CompanyContactDetailsOptions
  case object MobileNumber extends WithName("mobileNumber") with CompanyContactDetailsOptions
  case object NoDetails extends WithName("noDetails") with CompanyContactDetailsOptions

  private case object Or extends CompanyContactDetailsOptions

  private val viewValues: Seq[CompanyContactDetailsOptions] = Seq(
    EmailAddress, PhoneNumber, MobileNumber, Or, NoDetails
  )

  val values: Seq[CompanyContactDetailsOptions] = viewValues.filter(_.isInstanceOf[WithName])

  def options(implicit messages: Messages): Seq[RadioItem] = viewValues.zipWithIndex.map {
    case (value: WithName, index) =>
      RadioItem(
        content = Text(messages(s"companyContactDetailsOptions.${value.toString}")),
        value   = Some(value.toString),
        id      = Some(s"value_$index")
      )
    case (Or, _) =>
      RadioItem(divider = Some(messages("companyContactDetailsOptions.or")))
  }

  implicit val enumerable: Enumerable[CompanyContactDetailsOptions] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
