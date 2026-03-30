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

import models.contact.ContactOptions
import models.contact.ContactOptions.*
import models.{InvalidAnswer, MissingAnswer, UserAnswers, Validation, ValidationError}
import pages.QuestionPage
import pages.add.*
import play.api.libs.json.*

final case class ValidatedSubcontractor(
  typeOfSubcontractor: TypeOfSubcontractor,
  tradingName: Option[String],
  subcontractorName: Option[SubcontractorName],
  address: Option[InternationalAddress],
  individualContactDetails: ContactOptions,
  individualEmail: Option[String],
  individualPhone: Option[String],
  individualMobile: Option[String],
  utr: Option[String],
  nino: Option[String],
  workRefNumber: Option[String]
)

object ValidatedSubcontractor extends Validation {
  def build(answers: UserAnswers): Either[ValidationError, ValidatedSubcontractor] =
    for {
      typeOfSubcontractor      <- getType(answers)
      tradingName              <- getOptionalPageValue(answers, TradingNameOfSubcontractorPage, SubTradingNameYesNoPage)
      subcontractorName        <- getOptionalNamePage(answers)
      address                  <- getOptionalPageValue(answers, AddressOfSubcontractorPage, SubAddressYesNoPage)
      individualContactDetails <- getPageValue(answers, IndividualChooseContactDetailsPage)
      individualEmail          <- getContactPageValue(answers, IndividualEmailAddressPage, individualContactDetails)
      individualPhone          <- getContactPageValue(answers, IndividualPhoneNumberPage, individualContactDetails)
      individualMobile         <- getContactPageValue(answers, IndividualMobileNumberPage, individualContactDetails)
      utr                      <- getOptionalPageValue(answers, SubcontractorsUniqueTaxpayerReferencePage, UniqueTaxpayerReferenceYesNoPage)
      nino                     <- getOptionalPageValue(answers, SubNationalInsuranceNumberPage, NationalInsuranceNumberYesNoPage)
      workReference            <- getOptionalPageValue(answers, WorksReferenceNumberPage, WorksReferenceNumberYesNoPage)
    } yield ValidatedSubcontractor(
      typeOfSubcontractor,
      tradingName,
      subcontractorName,
      address,
      individualContactDetails,
      individualEmail,
      individualPhone,
      individualMobile,
      utr,
      nino,
      workReference
    )

  private def getType(answers: UserAnswers): Either[ValidationError, TypeOfSubcontractor] =
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

  private def getContactPageValue[A](
    answers: UserAnswers,
    questionPage: QuestionPage[A],
    contactOptions: ContactOptions
  )(implicit reads: Reads[A]): Either[ValidationError, Option[A]] = {
    val expectedPage: Option[QuestionPage[_]] = contactOptions match {
      case Email     => Some(IndividualEmailAddressPage)
      case Phone     => Some(IndividualPhoneNumberPage)
      case Mobile    => Some(IndividualMobileNumberPage)
      case NoDetails => None
    }

    if (expectedPage.contains(questionPage)) {
      answers.get(questionPage).toRight(MissingAnswer(questionPage)).map(Some(_))
    } else if (expectedPage.isDefined) {
      Right(None)
    } else {
      answers
        .get(questionPage)
        .fold(Right(None): Either[ValidationError, Option[A]])(_ => Left(InvalidAnswer(questionPage)))
    }
  }

}
