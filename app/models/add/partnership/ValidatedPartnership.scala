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
import models.{UserAnswers, Validation, ValidationError}
import pages.add.TypeOfSubcontractorPage
import pages.add.partnership.*
import play.api.libs.json.*

final case class ValidatedPartnership(
  typeOfSubcontractor: TypeOfSubcontractor,
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
      typeOfSubcontractor             <- getType(answers)
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
      typeOfSubcontractor,
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

  private def getType(answers: UserAnswers): Either[ValidationError, TypeOfSubcontractor] =
    getPageValue(answers, TypeOfSubcontractorPage)

}
