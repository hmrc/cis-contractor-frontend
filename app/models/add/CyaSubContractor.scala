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

import models.{InvalidAnswer, MissingAnswer, UserAnswers, ValidationError}
import pages.add.*

final case class CyaSubContractor(
  typeOfSubcontractor: TypeOfSubcontractor,
  tradingName: Option[String],
  subcontractorName: Option[SubcontractorName], // haven't covered all fields yet
  address: Option[UKAddress], // haven't covered all fields yet
  nino: Option[String], // haven't covered all fields yet
  utr: Option[String], // haven't covered all fields yet
  workRefNumber: Option[String], // haven't covered all fields yet
  contactDetails: Option[SubContactDetails] // haven't covered all fields yet
)

object CyaSubContractor {
  def build(answers: UserAnswers): Either[ValidationError, CyaSubContractor] =
    for {
      typeOfSubcontractor <- getTypeOfSubcontractor(answers)
      tradingName         <- getTradingName(answers)
      subcontractorName   <- getSubcontractorName(answers)
    } yield CyaSubContractor(
      typeOfSubcontractor,
      tradingName,
      subcontractorName,
      None,
      None,
      None,
      None,
      None
    )

  private def getTypeOfSubcontractor(answers: UserAnswers): Either[ValidationError, TypeOfSubcontractor] =
    answers.get(TypeOfSubcontractorPage) match {
      case Some(value) => Right(value)
      case _           => Left(MissingAnswer(TypeOfSubcontractorPage))
    }

  private def getTradingName(answers: UserAnswers): Either[ValidationError, Option[String]] =
    answers.get(TradingNameOfSubcontractorPage) match {
      case Some(value) =>
        answers.get(SubTradingNameYesNoPage) match {
          case Some(true)  => Right(Some(value))
          case Some(false) => Left(InvalidAnswer(TradingNameOfSubcontractorPage))
          case None        => Left(MissingAnswer(SubTradingNameYesNoPage))
        }
      case None        =>
        answers.get(SubTradingNameYesNoPage) match {
          case Some(true)  => Left(InvalidAnswer(TradingNameOfSubcontractorPage))
          case Some(false) => Right(None)
          case None        => Left(MissingAnswer(SubTradingNameYesNoPage))
        }
    }

  private def getSubcontractorName(answers: UserAnswers): Either[ValidationError, Option[SubcontractorName]] =
    answers.get(SubcontractorNamePage) match {
      case Some(value) =>
        answers.get(SubTradingNameYesNoPage) match {
          case Some(true)  => Left(InvalidAnswer(TradingNameOfSubcontractorPage))
          case Some(false) => Right(Some(value))
          case None        => Left(MissingAnswer(SubTradingNameYesNoPage))
        }
      case None        =>
        answers.get(SubTradingNameYesNoPage) match {
          case Some(true)  => Right(None)
          case Some(false) => Left(InvalidAnswer(TradingNameOfSubcontractorPage))
          case None        => Left(MissingAnswer(SubTradingNameYesNoPage))
        }
    }
}
