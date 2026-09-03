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

import models.address.Address
import models.contact.ContactMethodOptions
import models.{InvalidAnswer, MissingAnswer, TypeOfSubcontractor, UserAnswers, Validation, ValidationError}
import pages.QuestionPage
import pages.add.*
import play.api.libs.json.*

final case class ValidatedSubcontractor(
  tradingName: Option[String],
  subcontractorName: Option[SubcontractorName],
  address: Option[Address],
  IndividualContactMethodOptions: Option[Set[IndividualContactMethodOptions]],
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
      typeOfSubcontractor            <- validateType(answers)
      tradingName                    <- getNamesPageValue(
                                          answers,
                                          TradingNameOfSubcontractorPage,
                                          IndividualNamesOptions.TradingName
                                        )
      subcontractorName              <- getNamesPageValue(answers, SubcontractorNamePage, IndividualNamesOptions.SubcontractorName)
      address                        <- getOptionalPageValue(answers, AddressOfSubcontractorPage, SubAddressYesNoPage)
      individualContactMethodOptions <-
        getOptionalPageValue(answers, IndividualContactMethodOptionsPage, AddIndividualContactMethodsYesNoPage)
          .flatMap {
            case Some(methods) if methods.nonEmpty =>
              Right(Some(methods))

            case Some(_) =>
              Left(InvalidAnswer(IndividualContactMethodOptionsPage))

            case None =>
              Right(None)
          }

      individualEmail  <- getContactPageValue(
                            answers,
                            individualContactMethodOptions,
                            IndividualEmailAddressPage,
                            ContactMethodOptions.Email
                          )
      individualPhone  <- getContactPageValue(
                            answers,
                            individualContactMethodOptions,
                            IndividualPhoneNumberPage,
                            ContactMethodOptions.Phone
                          )
      individualMobile <- getContactPageValue(
                            answers,
                            individualContactMethodOptions,
                            IndividualMobileNumberPage,
                            ContactMethodOptions.Mobile
                          )
      utr              <- getOptionalPageValue(answers, SubcontractorsUniqueTaxpayerReferencePage, UniqueTaxpayerReferenceYesNoPage)
      nino             <- getOptionalPageValue(answers, SubNationalInsuranceNumberPage, NationalInsuranceNumberYesNoPage)
      workReference    <- getOptionalPageValue(answers, WorksReferenceNumberPage, WorksReferenceNumberYesNoPage)
    } yield ValidatedSubcontractor(
      tradingName,
      subcontractorName,
      address,
      individualContactMethodOptions,
      individualEmail,
      individualPhone,
      individualMobile,
      utr,
      nino,
      workReference
    )

  private def validateType(answers: UserAnswers): Either[ValidationError, Unit] =
    getPageValue(answers, TypeOfSubcontractorPage).flatMap {
      case TypeOfSubcontractor.Individualorsoletrader => Right(())
      case _                                          => Left(InvalidAnswer(TypeOfSubcontractorPage))
    }

  private def getNamesPageValue[A](
    userAnswers: UserAnswers,
    questionPage: QuestionPage[A],
    expectedNamesOption: IndividualNamesOptions
  )(implicit
    reads: Reads[A]
  ): Either[ValidationError, Option[A]] =
    (
      userAnswers.get(questionPage),
      userAnswers.get(IndividualNamesOptionsPage)
    ) match {
      case (_, None)                                                                                           =>
        Left(MissingAnswer(IndividualNamesOptionsPage))
      case (Some(value), Some(individualNamesOptions)) if individualNamesOptions.contains(expectedNamesOption) =>
        Right(Some(value))
      case (None, Some(individualNamesOptions)) if !individualNamesOptions.contains(expectedNamesOption)       =>
        Right(None)
      case _                                                                                                   =>
        Left(InvalidAnswer(questionPage))
    }
}
