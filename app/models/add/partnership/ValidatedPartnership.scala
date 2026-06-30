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

import models.address.Address
import models.contact.{ContactMethodOptions, ContactOptions}
import models.contact.ContactOptions.*
import models.{InvalidAnswer, TypeOfSubcontractor, UserAnswers, Validation, ValidationError}
import pages.add.TypeOfSubcontractorPage
import pages.add.partnership.*
import play.api.libs.json.*

final case class ValidatedPartnership(
  partnershipName: String,
  partnershipAddress: Option[Address],
  partnershipContactMethodOptions: Option[Set[PartnershipContactMethodOptions]],
  partnershipEmail: Option[String],
  partnershipPhone: Option[String],
  partnershipMobile: Option[String],
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
      partnershipContactMethodOptions <-
        getOptionalPageValue(answers, PartnershipContactMethodOptionsPage, AddPartnershipContactMethodsYesNoPage)
      partnershipEmail                <- getContactPageValue(
                                           answers,
                                           partnershipContactMethodOptions,
                                           PartnershipEmailAddressPage,
                                           ContactMethodOptions.Email
                                         )
      partnershipPhone                <- getContactPageValue(
                                           answers,
                                           partnershipContactMethodOptions,
                                           PartnershipPhoneNumberPage,
                                           ContactMethodOptions.Phone
                                         )
      partnershipMobile               <- getContactPageValue(
                                           answers,
                                           partnershipContactMethodOptions,
                                           PartnershipMobileNumberPage,
                                           ContactMethodOptions.Mobile
                                         )
      partnershipUtr                  <-
        getOptionalPageValue(answers, PartnershipUniqueTaxpayerReferencePage, PartnershipHasUtrYesNoPage)
      partnershipNominatedPartnerName <- getPageValue(answers, PartnershipNominatedPartnerNamePage)
      partnershipNominatedPartnerUtr  <-
        getOptionalPageValue(answers, PartnershipNominatedPartnerUtrPage, PartnershipNominatedPartnerUtrYesNoPage)
      partnershipNominatedPartnerNino <-
        getOptionalPageValue(answers, PartnershipNominatedPartnerNinoPage, PartnershipNominatedPartnerNinoYesNoPage)
      partnershipNominatedPartnerCrn  <-
        getOptionalPageValue(answers, PartnershipNominatedPartnerCrnPage, PartnershipNominatedPartnerCrnYesNoPage)
      partnershipWorkRefNumber        <-
        getOptionalPageValue(answers, PartnershipWorksReferenceNumberPage, PartnershipWorksReferenceNumberYesNoPage)

    } yield ValidatedPartnership(
      partnershipName,
      partnershipAddress,
      partnershipContactMethodOptions,
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

}
