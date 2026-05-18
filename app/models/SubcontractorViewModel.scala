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

  def fromSubcontractors(subcontractors: Seq[Subcontractor]): Either[String, Seq[SubcontractorViewModel]] =
    subcontractors.foldLeft[Either[String, Seq[SubcontractorViewModel]]](Right(Seq.empty)) {
      case (Right(viewModels), subcontractor) =>
        fromSubcontractor(subcontractor).map(viewModel => viewModels :+ viewModel)

      case (left @ Left(_), _) =>
        left
    }

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
  ): Either[String, String] =
    subcontractorType match {
      case Individualorsoletrader =>
        val soleTraderName: Option[String] =
          for {
            surname   <- subcontractor.surname.map(_.trim).filter(_.nonEmpty)
            firstName <- subcontractor.firstName.map(_.trim).filter(_.nonEmpty)
          } yield s"$surname, $firstName"

        soleTraderName
          .orElse(subcontractor.tradingName.map(_.trim).filter(_.nonEmpty))
          .toRight(s"Missing name for for subcontractor id ${subcontractor.subcontractorId}")

      case Limitedcompany =>
        subcontractor.tradingName.toRight(
          s"Missing tradingName for for subcontractor id ${subcontractor.subcontractorId}"
        )

      case Partnership =>
        subcontractor.partnershipTradingName.toRight(
          s"Missing partnershipTradingName for for subcontractor id ${subcontractor.subcontractorId}"
        )

      case Trust =>
        subcontractor.tradingName.toRight(
          s"Missing tradingName for for subcontractor id ${subcontractor.subcontractorId}"
        )
    }

  private def getSubcontractorType(subcontractor: Subcontractor): Either[String, TypeOfSubcontractor] =
    subcontractor.subcontractorType
      .flatMap(TypeOfSubcontractor.fromString)
      .toRight(s"Unknown or missing subcontractor type for subcontractor id ${subcontractor.subcontractorId}")
}
