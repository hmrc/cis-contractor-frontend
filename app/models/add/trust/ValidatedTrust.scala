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

package models.add.trust

import models.add.{InternationalAddress, TypeOfSubcontractor}
import models.contact.ContactOptions
import models.contact.ContactOptions.{Email, Mobile, NoDetails, Phone}
import models.{InvalidAnswer, MissingAnswer, UserAnswers, Validation, ValidationError}
import pages.QuestionPage
import pages.add.TypeOfSubcontractorPage
import pages.add.trust.*
import play.api.libs.json.Reads

final case class ValidatedTrust(
                                 trustName: String,
                                 trustAddress: Option[InternationalAddress],
                                 trustContactDetails: TrustContactOptions,
                                 trustEmail: Option[String],
                                 trustPhone: Option[String],
                                 trustMobile: Option[String],
                                 trustUtr: Option[String],
                                 trustWorkRefNumber: Option[String]
                               )

object ValidatedTrust extends Validation {

  def build(answers: UserAnswers): Either[ValidationError, ValidatedTrust] =
    for {
      _                  <- validateType(answers)

      trustName           <- getPageValue(answers, TrustNamePage)

      trustAddress        <- getOptionalPageValue(answers, TrustAddressPage, TrustAddressYesNoPage)

      trustContactDetails <- getPageValue(answers, TrustContactOptionsPage)

      trustEmail          <- getContactPageValue(answers, TrustEmailAddressPage, trustContactDetails)
      trustPhone          <- getContactPageValue(answers, TrustPhoneNumberPage, trustContactDetails)
      trustMobile         <- getContactPageValue(answers, TrustMobileNumberPage, trustContactDetails)

      trustUtr            <- getOptionalPageValue(answers, TrustUtrPage, TrustUtrYesNoPage)

      trustWorkRefNumber  <- getOptionalPageValue(answers, TrustWorksReferencePage, TrustWorksReferenceYesNoPage)

    } yield ValidatedTrust(
      trustName = trustName,
      trustAddress = trustAddress,
      trustContactDetails = trustContactDetails,
      trustEmail = trustEmail,
      trustPhone = trustPhone,
      trustMobile = trustMobile,
      trustUtr = trustUtr,
      trustWorkRefNumber = trustWorkRefNumber
    )

  private def validateType(answers: UserAnswers): Either[ValidationError, Unit] =
    getPageValue(answers, TypeOfSubcontractorPage).flatMap {
      case TypeOfSubcontractor.Trust => Right(())
      case _                         => Left(InvalidAnswer(TypeOfSubcontractorPage))
    }

  private def getContactPageValue[A](
                                      answers: UserAnswers,
                                      questionPage: QuestionPage[A],
                                      contactOptions: TrustContactOptions
                                    )(implicit reads: Reads[A]): Either[ValidationError, Option[A]] = {

    val expectedPage: Option[QuestionPage[_]] = contactOptions match {
      case Email     => Some(TrustEmailAddressPage)
      case Phone     => Some(TrustPhoneNumberPage)
      case Mobile    => Some(TrustMobileNumberPage)
      case NoDetails => None
    }

    if (expectedPage.contains(questionPage)) {
      answers.get(questionPage).toRight(MissingAnswer(questionPage)).map(Some(_))
    }
    else if (expectedPage.isDefined) {
      Right(None)
    }
    else {
      answers
        .get(questionPage)
        .fold(Right(None): Either[ValidationError, Option[A]])(_ => Left(InvalidAnswer(questionPage)))
    }
  }
}
