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

import models.address.Address
import models.contact.ContactMethodOptions
import models.{InvalidAnswer, TypeOfSubcontractor, UserAnswers, Validation, ValidationError}
import pages.add.TypeOfSubcontractorPage
import pages.add.company.*
import play.api.libs.json.Reads

final case class ValidatedCompany(
  companyName: String,
  companyAddress: Option[Address],
  companyContactMethodOptions: Option[Set[CompanyContactMethodOptions]],
  companyEmail: Option[String],
  companyPhone: Option[String],
  companyMobile: Option[String],
  companyUtr: Option[String],
  companyCrn: Option[String],
  companyWorksReferenceNumber: Option[String]
)

object ValidatedCompany extends Validation {

  def build(answers: UserAnswers): Either[ValidationError, ValidatedCompany] =
    buildCompany(
      answers,
      getPageValue(answers, CompanyNamePage)
    )

  def build(answers: UserAnswers, id: String): Either[ValidationError, ValidatedCompany] =
    for {
      _                           <- validateType(answers, id)
      companyName                 <- getPageValue(answers, CompanyNamePage(id))
      companyAddress              <- getOptionalPageValue(
                                       answers,
                                       CompanyAddressPage(id),
                                       CompanyAddressYesNoPage(id)
                                     )
      companyContactMethodOptions <-
        getOptionalPageValue(
          answers,
          CompanyContactMethodOptionsPage(id),
          AddCompanyContactMethodsYesNoPage(id)
        ).flatMap {
          case Some(methods) if methods.nonEmpty =>
            Right(Some(methods))
          case Some(_)                           =>
            Left(InvalidAnswer(CompanyContactMethodOptionsPage(id)))
          case None                              =>
            Right(None)
        }
      companyEmail                <- getContactPageValue(
                                       answers,
                                       companyContactMethodOptions,
                                       CompanyEmailAddressPage(id),
                                       ContactMethodOptions.Email
                                     )
      companyPhone                <- getContactPageValue(
                                       answers,
                                       companyContactMethodOptions,
                                       CompanyPhoneNumberPage(id),
                                       ContactMethodOptions.Phone
                                     )
      companyMobile               <- getContactPageValue(
                                       answers,
                                       companyContactMethodOptions,
                                       CompanyMobileNumberPage(id),
                                       ContactMethodOptions.Mobile
                                     )
      companyUtr                  <-
        getOptionalPageValue(
          answers,
          CompanyUtrPage(id),
          CompanyUtrYesNoPage(id)
        )
      companyCrn                  <-
        getOptionalPageValue(
          answers,
          CompanyCrnPage(id),
          CompanyCrnYesNoPage(id)
        )
      companyWorksReferenceNumber <-
        getOptionalPageValue(
          answers,
          CompanyWorksReferencePage(id),
          CompanyWorksReferenceYesNoPage(id)
        )

    } yield ValidatedCompany(
      companyName,
      companyAddress,
      companyContactMethodOptions,
      companyEmail,
      companyPhone,
      companyMobile,
      companyUtr,
      companyCrn,
      companyWorksReferenceNumber
    )

  def buildForAmend(answers: UserAnswers): Either[ValidationError, ValidatedCompany] =
    buildCompany(
      answers,
      getAmendPageValue(answers, CompanyNamePage)
    )

  private def buildCompany(
    answers: UserAnswers,
    companyName: Either[ValidationError, String]
  ): Either[ValidationError, ValidatedCompany] =
    for {
      _                           <- validateType(answers)
      companyName                 <- companyName
      companyAddress              <- getOptionalPageValue(
                                       answers,
                                       CompanyAddressPage,
                                       CompanyAddressYesNoPage
                                     )
      companyContactMethodOptions <-
        getOptionalPageValue(
          answers,
          CompanyContactMethodOptionsPage,
          AddCompanyContactMethodsYesNoPage
        ).flatMap {
          case Some(methods) if methods.nonEmpty =>
            Right(Some(methods))
          case Some(_)                           =>
            Left(InvalidAnswer(CompanyContactMethodOptionsPage))
          case None                              =>
            Right(None)
        }
      companyEmail                <- getContactPageValue(
                                       answers,
                                       companyContactMethodOptions,
                                       CompanyEmailAddressPage,
                                       ContactMethodOptions.Email
                                     )
      companyPhone                <- getContactPageValue(
                                       answers,
                                       companyContactMethodOptions,
                                       CompanyPhoneNumberPage,
                                       ContactMethodOptions.Phone
                                     )
      companyMobile               <- getContactPageValue(
                                       answers,
                                       companyContactMethodOptions,
                                       CompanyMobileNumberPage,
                                       ContactMethodOptions.Mobile
                                     )
      companyUtr                  <-
        getOptionalPageValue(
          answers,
          CompanyUtrPage,
          CompanyUtrYesNoPage
        )
      companyCrn                  <-
        getOptionalPageValue(
          answers,
          CompanyCrnPage,
          CompanyCrnYesNoPage
        )
      companyWorksReferenceNumber <-
        getOptionalPageValue(
          answers,
          CompanyWorksReferencePage,
          CompanyWorksReferenceYesNoPage
        )

    } yield ValidatedCompany(
      companyName,
      companyAddress,
      companyContactMethodOptions,
      companyEmail,
      companyPhone,
      companyMobile,
      companyUtr,
      companyCrn,
      companyWorksReferenceNumber
    )

  private def validateType(answers: UserAnswers): Either[ValidationError, Unit] =
    getPageValue(answers, TypeOfSubcontractorPage).flatMap {
      case TypeOfSubcontractor.Limitedcompany => Right(())
      case _                                  => Left(InvalidAnswer(TypeOfSubcontractorPage))
    }

  private def validateType(answers: UserAnswers, id: String): Either[ValidationError, Unit] =
    getPageValue(answers, TypeOfSubcontractorPage(id)).flatMap {
      case TypeOfSubcontractor.Limitedcompany => Right(())
      case _                                  => Left(InvalidAnswer(TypeOfSubcontractorPage(id)))
    }
}
