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

package models

import models.TypeOfSubcontractor.*
import play.api.libs.json.{Json, OFormat}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.checkboxes.CheckboxItem
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import viewmodels.govuk.checkbox.CheckboxItemViewModel

case class SubcontractorViewModel(id: String, name: String)

object SubcontractorViewModel {

  implicit val format: OFormat[SubcontractorViewModel] = Json.format[SubcontractorViewModel]

  def checkboxItems(subcontractors: Seq[SubcontractorViewModel]): Seq[CheckboxItem] =
    subcontractors.sortBy(_.name.toLowerCase).zipWithIndex.map { case (sub, index) =>
      CheckboxItemViewModel(
        content = Text(sub.name),
        fieldId = "value",
        index = index,
        value = sub.id
      )
    }

  def fromSubcontractors(
    subcontractors: Seq[Subcontractor]
  )(implicit messages: Messages): Seq[SubcontractorViewModel] =
    subcontractors.map(fromSubcontractor)

  private def fromSubcontractor(subcontractor: Subcontractor)(implicit messages: Messages): SubcontractorViewModel =
    val NoName = messages("verify.noName")
    val name   = getSubcontractorType(subcontractor)
      .flatMap(subcontractorType => getSubcontractorName(subcontractor, subcontractorType))
      .getOrElse(NoName)

    SubcontractorViewModel(
      id = subcontractor.subcontractorId.toString,
      name = name
    )

  private def getSubcontractorName(
    subcontractor: Subcontractor,
    subcontractorType: TypeOfSubcontractor
  ): Option[String] = {

    def nonBlank(field: Option[String]): Option[String] =
      field.map(_.trim).filter(_.nonEmpty)

    subcontractorType match {
      case Individualorsoletrader =>
        val soleTraderName: Option[String] =
          nonBlank(subcontractor.surname).map { surname =>
            nonBlank(subcontractor.firstName) match {
              case Some(firstName) => s"$surname, $firstName"
              case None            => surname
            }
          }

        soleTraderName
          .orElse(nonBlank(subcontractor.tradingName))

      case Limitedcompany =>
        nonBlank(subcontractor.tradingName)

      case Partnership =>
        nonBlank(subcontractor.partnershipTradingName)
          .orElse(nonBlank(subcontractor.tradingName))

      case Trust =>
        nonBlank(subcontractor.tradingName)
    }
  }

  private def getSubcontractorType(subcontractor: Subcontractor): Option[TypeOfSubcontractor] =
    subcontractor.subcontractorType
      .flatMap(TypeOfSubcontractor.fromString)
}
