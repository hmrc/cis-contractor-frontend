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

import models.add.TypeOfSubcontractor
import models.add.TypeOfSubcontractor.*
import play.api.libs.json.{Json, OFormat}
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
  ): (Seq[String], Seq[SubcontractorViewModel]) =
    subcontractors.partitionMap(fromSubcontractor)

  private def fromSubcontractor(subcontractor: Subcontractor): Either[String, SubcontractorViewModel] =
    for {
      subcontractorType <- getSubcontractorType(subcontractor)
      name              <- getSubcontractorName(subcontractor, subcontractorType)
    } yield SubcontractorViewModel(
      id = subcontractor.subcontractorId.toString,
      name = name
    )

  private def getSubcontractorName(
    subcontractor: Subcontractor,
    subcontractorType: TypeOfSubcontractor
  ): Either[String, String] = {

    def nonBlank(field: Option[String]): Option[String] =
      field.map(_.trim).filter(_.nonEmpty)

    subcontractorType match {
      case Individualorsoletrader =>
        val soleTraderName: Option[String] =
          for {
            surname   <- nonBlank(subcontractor.surname)
            firstName <- nonBlank(subcontractor.firstName)
          } yield s"$surname, $firstName"

        soleTraderName
          .orElse(nonBlank(subcontractor.tradingName))
          .toRight(s"Missing name for subcontractor id ${subcontractor.subcontractorId}")

      case Limitedcompany =>
        nonBlank(subcontractor.tradingName).toRight(
          s"Missing tradingName for subcontractor id ${subcontractor.subcontractorId}"
        )

      case Partnership =>
        nonBlank(subcontractor.partnershipTradingName).toRight(
          s"Missing partnershipTradingName for subcontractor id ${subcontractor.subcontractorId}"
        )

      case Trust =>
        nonBlank(subcontractor.tradingName).toRight(
          s"Missing tradingName for subcontractor id ${subcontractor.subcontractorId}"
        )
    }
  }

  private def getSubcontractorType(subcontractor: Subcontractor): Either[String, TypeOfSubcontractor] =
    subcontractor.subcontractorType
      .flatMap(TypeOfSubcontractor.fromString)
      .toRight(s"Unknown or missing subcontractor type for subcontractor id ${subcontractor.subcontractorId}")
}
