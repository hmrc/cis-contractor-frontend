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
import pages.QuestionPage
import play.api.libs.json._

final case class ValidatedSubcontractor(
  typeOfSubcontractor: TypeOfSubcontractor,
  tradingName: Option[String],
  subcontractorName: Option[SubcontractorName],
  address: Option[UKAddress],
  nino: Option[String],
  utr: Option[String],
  workRefNumber: Option[String],
  contactDetails: Option[SubContactDetails]
)

object ValidatedSubcontractor {
  def build(answers: UserAnswers): Either[ValidationError, ValidatedSubcontractor] =
    for {
      typeOfSubcontractor <- getType(answers)
      tradingName         <- getOptionalPageValue(answers, TradingNameOfSubcontractorPage, SubTradingNameYesNoPage)
      subcontractorName   <- getOptionalNamePage(answers)
      address             <- getOptionalPageValue(answers, AddressOfSubcontractorPage, SubAddressYesNoPage)
      nino                <- getOptionalPageValue(answers, SubNationalInsuranceNumberPage, NationalInsuranceNumberYesNoPage)
      utr                 <- getOptionalPageValue(answers, SubcontractorsUniqueTaxpayerReferencePage, UniqueTaxpayerReferenceYesNoPage)
      workReference       <- getOptionalPageValue(answers, WorksReferenceNumberPage, WorksReferenceNumberYesNoPage)
      contactDetails      <- getOptionalPageValue(answers, SubContactDetailsPage, SubcontractorContactDetailsYesNoPage)
    } yield ValidatedSubcontractor(
      typeOfSubcontractor,
      tradingName,
      subcontractorName,
      address,
      nino,
      utr,
      workReference,
      contactDetails
    )

  private def getType(answers: UserAnswers): Either[ValidationError, TypeOfSubcontractor] =
    answers.get(TypeOfSubcontractorPage) match {
      case Some(value) => Right(value)
      case _           => Left(MissingAnswer(TypeOfSubcontractorPage))
    }

  private def getOptionalPageValue[A](
    answers: UserAnswers,
    questionPage: QuestionPage[A],
    yesNoPage: QuestionPage[Boolean]
  )(implicit reads: Reads[A]): Either[ValidationError, Option[A]] =
    (answers.get(questionPage), answers.get(yesNoPage)) match {
      case (_, None)                 => Left(MissingAnswer(yesNoPage))
      case (Some(value), Some(true)) => Right(Some(value))
      case (None, Some(false))       => Right(None)
      case _                         => Left(InvalidAnswer(questionPage))
    }

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
