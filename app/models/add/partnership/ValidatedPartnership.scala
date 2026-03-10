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

package models.add.partnership

import models.add.{InternationalAddress, TypeOfSubcontractor}
import models.contact.ContactOptions
import models.contact.ContactOptions.*
import models.{InvalidAnswer, MissingAnswer, UserAnswers, Validation, ValidationError}
import pages.QuestionPage
import pages.add.TypeOfSubcontractorPage
import pages.add.partnership.*
import play.api.libs.json.*

final case class ValidatedPartnership(
  partnershipName: String,
  partnershipAddress: Option[InternationalAddress],
  partnershipContactDetails: ContactOptions,
  partnershipEmail: Option[_],
  partnershipPhone: Option[_],
  partnershipMobile: Option[_],
  partnershipUtr: Option[String],
  partnershipNominatedPartnerName: String,
  partnershipNominatedPartnerUtr: Option[String],
  partnershipNominatedPartnerNino: Option[String],
  partnershipNominatedPartnerCrn: Option[String],
  partnershipWorkRefNumber: Option[String]
)

object ValidatedPartnership extends Validation {
  def build(answers: UserAnswers): Either[ValidationError, ValidatedPartnership] =
    for {
      _                               <- validateType(answers)
      partnershipName                 <- getPageValue(answers, PartnershipNamePage)
      partnershipAddress              <- getOptionalPageValue(answers, PartnershipAddressPage, PartnershipAddressYesNoPage)
      partnershipContactDetails       <- getPageValue(answers, PartnershipChooseContactDetailsPage)
      partnershipEmail                <- getContactPageValue(answers, PartnershipEmailAddressPage, partnershipContactDetails)
      partnershipPhone                <- getContactPageValue(answers, PartnershipPhoneNumberPage, partnershipContactDetails)
      partnershipMobile               <- getContactPageValue(answers, PartnershipMobileNumberPage, partnershipContactDetails)
      partnershipUtr                  <-
        getOptionalPageValue(answers, PartnershipUniqueTaxpayerReferencePage, PartnershipHasUtrYesNoPage)
      partnershipNominatedPartnerName <- getPageValue(answers, PartnershipNominatedPartnerNamePage)
      partnershipNominatedPartnerUtr  <-
        getOptionalPageValue(answers, PartnershipNominatedPartnerUtrPage, PartnershipHasUtrYesNoPage)
      partnershipNominatedPartnerNino <-
        getOptionalPageValue(answers, PartnershipNominatedPartnerNinoPage, PartnershipNominatedPartnerNinoYesNoPage)
      partnershipNominatedPartnerCrn  <-
        getOptionalPageValue(answers, PartnershipNominatedPartnerCrnPage, PartnershipNominatedPartnerCrnYesNoPage)
      partnershipWorkRefNumber        <-
        getOptionalPageValue(answers, PartnershipWorksReferenceNumberPage, PartnershipWorksReferenceNumberYesNoPage)

    } yield ValidatedPartnership(
      partnershipName,
      partnershipAddress,
      partnershipContactDetails,
      partnershipEmail,
      partnershipPhone,
      partnershipMobile,
      partnershipUtr,
      partnershipNominatedPartnerName,
      partnershipNominatedPartnerUtr,
      partnershipNominatedPartnerNino,
      partnershipNominatedPartnerCrn,
      partnershipWorkRefNumber
    )

  private def validateType(answers: UserAnswers): Either[ValidationError, Unit] =
    getPageValue(answers, TypeOfSubcontractorPage).flatMap {
      case TypeOfSubcontractor.Partnership => Right(())
      case _                               => Left(InvalidAnswer(TypeOfSubcontractorPage))
    }

  private def getContactPageValue[A](
    answers: UserAnswers,
    questionPage: QuestionPage[A],
    contactOptions: ContactOptions
  )(implicit reads: Reads[A]): Either[ValidationError, Option[A]] =
    (contactOptions, questionPage) match {
      case (Email, PartnershipEmailAddressPage)  =>
        answers.get(questionPage).toRight(MissingAnswer(questionPage)).map(Option(_))
      case (Phone, PartnershipPhoneNumberPage)   =>
        answers.get(questionPage).toRight(MissingAnswer(questionPage)).map(Option(_))
      case (Mobile, PartnershipMobileNumberPage) =>
        answers.get(questionPage).toRight(MissingAnswer(questionPage)).map(Option(_))
      case (Email | Phone | Mobile, _)           => Right(None)
      case (NoDetails, _)                        =>
        answers
          .get(questionPage)
          .fold(Right(None): Either[ValidationError, Option[A]])(_ => Left(InvalidAnswer(questionPage)))
      case _                                     => Left(InvalidAnswer(questionPage))
    }

}
