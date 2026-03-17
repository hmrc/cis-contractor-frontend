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

import models.{InvalidAnswer, UserAnswers, Validation, ValidationError}
import pages.add.*
import play.api.libs.json.*

final case class ValidatedSubcontractor(
  typeOfSubcontractor: TypeOfSubcontractor,
  tradingName: Option[String],
  subcontractorName: Option[SubcontractorName],
  address: Option[UKAddress],
  utr: Option[String],
  nino: Option[String],
  workRefNumber: Option[String]
)

object ValidatedSubcontractor extends Validation {
  def build(answers: UserAnswers): Either[ValidationError, ValidatedSubcontractor] =
    for {
      typeOfSubcontractor <- getType(answers)
      tradingName         <- getOptionalPageValue(answers, TradingNameOfSubcontractorPage, SubTradingNameYesNoPage)
      subcontractorName   <- getOptionalNamePage(answers)
      address             <- getOptionalPageValue(answers, AddressOfSubcontractorPage, SubAddressYesNoPage)
      utr                 <- getOptionalPageValue(answers, SubcontractorsUniqueTaxpayerReferencePage, UniqueTaxpayerReferenceYesNoPage)
      nino                <- getOptionalPageValue(answers, SubNationalInsuranceNumberPage, NationalInsuranceNumberYesNoPage)
      workReference       <- getOptionalPageValue(answers, WorksReferenceNumberPage, WorksReferenceNumberYesNoPage)
    } yield ValidatedSubcontractor(
      typeOfSubcontractor,
      tradingName,
      subcontractorName,
      address,
      utr,
      nino,
      workReference
    )

  def getType(answers: UserAnswers): Either[ValidationError, TypeOfSubcontractor] =
    getPageValue(answers, TypeOfSubcontractorPage)

  // SubTradingNameYesNoPage is inverted for SubcontractorNamePage
  // Yes  -> SubcontractorNamePage not required
  // No   -> SubcontractorNamePage required
  // To be Kept separate from generic helper for clarity.
  private def getOptionalNamePage(answers: UserAnswers): Either[ValidationError, Option[SubcontractorName]] =
    (answers.get(SubcontractorNamePage), answers.get(SubTradingNameYesNoPage)) match {
      case (Some(value), Some(false)) => Right(Some(value))
      case (None, Some(true))         => Right(None)
      case _                          => Left(InvalidAnswer(SubcontractorNamePage))
    }
}
