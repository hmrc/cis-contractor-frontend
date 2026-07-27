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

import models.address.Address
import models.contact.ContactMethodOptions
import models.{InvalidAnswer, TypeOfSubcontractor, UserAnswers, Validation, ValidationError}
import pages.add.TypeOfSubcontractorPage
import pages.add.trust.*
import play.api.libs.json.Reads

final case class ValidatedTrust(
  trustName: String,
  trustAddress: Option[Address],
  trustContactMethodOptions: Option[Set[TrustContactMethodOptions]],
  trustEmail: Option[String],
  trustPhone: Option[String],
  trustMobile: Option[String],
  trustUtr: Option[String],
  trustWorkRefNumber: Option[String]
)

object ValidatedTrust extends Validation {

  def build(answers: UserAnswers): Either[ValidationError, ValidatedTrust] =
    for {
      _ <- validateType(answers)

      trustName <- getPageValue(answers, TrustNamePage)

      trustAddress <- getOptionalPageValue(answers, TrustAddressPage, TrustAddressYesNoPage)

      trustContactMethodOptions <-
        getOptionalPageValue(answers, TrustContactMethodOptionsPage, AddTrustContactMethodsYesNoPage).flatMap {
          case Some(methods) if methods.nonEmpty =>
            Right(Some(methods))

          case Some(_) =>
            Left(InvalidAnswer(TrustContactMethodOptionsPage))

          case None =>
            Right(None)
        }

      trustEmail  <-
        getContactPageValue(answers, trustContactMethodOptions, TrustEmailAddressPage, ContactMethodOptions.Email)
      trustPhone  <-
        getContactPageValue(answers, trustContactMethodOptions, TrustPhoneNumberPage, ContactMethodOptions.Phone)
      trustMobile <-
        getContactPageValue(answers, trustContactMethodOptions, TrustMobileNumberPage, ContactMethodOptions.Mobile)

      trustUtr <- getOptionalPageValue(answers, TrustUtrPage, TrustUtrYesNoPage)

      trustWorkRefNumber <- getOptionalPageValue(answers, TrustWorksReferencePage, TrustWorksReferenceYesNoPage)

    } yield ValidatedTrust(
      trustName = trustName,
      trustAddress = trustAddress,
      trustContactMethodOptions = trustContactMethodOptions,
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
}
