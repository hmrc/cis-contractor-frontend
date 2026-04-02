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

import models.add.{InternationalAddress, TypeOfSubcontractor}
import models.contact.ContactOptions
import models.contact.ContactOptions.*
import models.{InvalidAnswer, MissingAnswer, UserAnswers, Validation, ValidationError}
import pages.QuestionPage
import pages.add.TypeOfSubcontractorPage
import pages.add.company.*
import play.api.libs.json.Reads

final case class ValidatedCompany(
  companyName: String,
  companyAddress: Option[InternationalAddress],
  companyContactDetails: ContactOptions,
  companyEmail: Option[String],
  companyPhone: Option[String],
  companyMobile: Option[String],
  companyUtr: Option[String],
  companyCrn: Option[String],
  companyWorksReferenceNumber: Option[String]
)

object ValidatedCompany extends Validation {

  def build(answers: UserAnswers): Either[ValidationError, ValidatedCompany] =
    for {
      _                    <- validateType(answers)
      companyName          <- getPageValue(answers, CompanyNamePage)
      companyAddress       <- getOptionalPageValue(answers, CompanyAddressPage, CompanyAddressYesNoPage)
      companyContactChoice <- getPageValue(answers, CompanyContactOptionsPage)

      companyEmail  <- getContactPageValue(answers, CompanyEmailAddressPage, companyContactChoice)
      companyPhone  <- getContactPageValue(answers, CompanyPhoneNumberPage, companyContactChoice)
      companyMobile <- getContactPageValue(answers, CompanyMobileNumberPage, companyContactChoice)

      companyUtr <- getOptionalPageValue(answers, CompanyUtrPage, CompanyUtrYesNoPage)
      companyCrn <- getOptionalPageValue(answers, CompanyCrnPage, CompanyCrnYesNoPage)

      companyWorksReferenceNumber <-
        getOptionalPageValue(answers, CompanyWorksReferencePage, CompanyWorksReferenceYesNoPage)

    } yield ValidatedCompany(
      companyName = companyName,
      companyAddress = companyAddress,
      companyContactDetails = companyContactChoice,
      companyEmail = companyEmail,
      companyPhone = companyPhone,
      companyMobile = companyMobile,
      companyUtr = companyUtr,
      companyCrn = companyCrn,
      companyWorksReferenceNumber = companyWorksReferenceNumber
    )

  private def validateType(answers: UserAnswers): Either[ValidationError, Unit] =
    getPageValue(answers, TypeOfSubcontractorPage).flatMap {
      case TypeOfSubcontractor.Limitedcompany => Right(())
      case _                                  => Left(InvalidAnswer(TypeOfSubcontractorPage))
    }

  private def getContactPageValue[A](
    answers: UserAnswers,
    questionPage: QuestionPage[A],
    contactOptions: ContactOptions
  )(implicit reads: Reads[A]): Either[ValidationError, Option[A]] = {

    val expectedPage: Option[QuestionPage[_]] = contactOptions match {
      case Email     => Some(CompanyEmailAddressPage)
      case Phone     => Some(CompanyPhoneNumberPage)
      case Mobile    => Some(CompanyMobileNumberPage)
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
