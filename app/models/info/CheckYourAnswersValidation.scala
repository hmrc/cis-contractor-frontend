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

package models.info

import models.TypeOfSubcontractor
import models.address.Address
import models.contact.ContactMethodOptions
import models.info.company.CompanyAnswers
import models.info.partnership.PartnershipAnswers
import models.info.trust.TrustAnswers

object CheckYourAnswersValidation {

  def isValid(answers: IndividualAnswers): Boolean =
    answers.subcontractorType == TypeOfSubcontractor.Individualorsoletrader &&
      (
        if (answers.showVerificationDetails) {
          nonBlank(answers.utr)
        } else {
          answers.usesTradingName.exists {
            case true  => nonBlank(answers.tradingName)
            case false => answers.subcontractorName.exists(name => nonBlank(name.firstName) && nonBlank(name.lastName))
          }
        }
      ) &&
      optionalAnswer(answers.addressYesNo, answers.address)(isValidAddress) &&
      contactAnswers(
        answers.individualContactMethodsYesNo,
        answers.individualContactMethod,
        answers.email,
        answers.phone,
        answers.mobile
      ) &&
      optionalAnswer(answers.utrYesNo, answers.utr)(nonBlank) &&
      optionalAnswer(answers.ninoYesNo, answers.nino)(nonBlank) &&
      optionalAnswer(answers.worksReferenceYesNo, answers.worksReference)(nonBlank)

  def isValid(answers: CompanyAnswers): Boolean =
    answers.subcontractorType == TypeOfSubcontractor.Limitedcompany &&
      (answers.showVerificationDetails || nonBlank(answers.companyName)) &&
      optionalAnswer(answers.addressYesNo, answers.address)(isValidAddress) &&
      contactAnswers(
        answers.companyContactMethodsYesNo,
        answers.companyContactMethod,
        answers.email,
        answers.phone,
        answers.mobile
      ) &&
      optionalAnswer(answers.crnYesNo, answers.crn)(nonBlank) &&
      optionalAnswer(answers.utrYesNo, answers.utr)(nonBlank) &&
      optionalAnswer(answers.worksReferenceYesNo, answers.worksReference)(nonBlank) &&
      (!answers.showVerificationDetails || nonBlank(answers.utr))

  def isValid(answers: PartnershipAnswers): Boolean =
    answers.subcontractorType == TypeOfSubcontractor.Partnership &&
      (answers.showVerificationDetails || nonBlank(answers.partnershipName)) &&
      nonBlank(answers.nominatedPartnerName) &&
      optionalAnswer(answers.addressYesNo, answers.address)(isValidAddress) &&
      contactAnswers(
        answers.partnershipContactMethodsYesNo,
        answers.partnershipContactMethodOptions,
        answers.email,
        answers.phone,
        answers.mobile
      ) &&
      optionalAnswer(answers.hasUtrYesNo, answers.utr)(nonBlank) &&
      optionalAnswer(answers.nominatedPartnerUtrYesNo, answers.nominatedPartnerUtr)(nonBlank) &&
      optionalAnswer(answers.nominatedPartnerNinoYesNo, answers.nominatedPartnerNino)(nonBlank) &&
      optionalAnswer(answers.nominatedPartnerCrnYesNo, answers.nominatedPartnerCrn)(nonBlank) &&
      optionalAnswer(answers.nominatedPartnerWorksReferenceYesNo, answers.nominatedPartnerWorksReference)(nonBlank) &&
      (!answers.showVerificationDetails || nonBlank(answers.utr))

  def isValid(answers: TrustAnswers): Boolean =
    answers.subcontractorType == TypeOfSubcontractor.Trust &&
      (answers.showVerificationDetails || nonBlank(answers.trustName)) &&
      optionalAnswer(answers.addressYesNo, answers.address)(isValidAddress) &&
      contactAnswers(
        answers.trustContactMethodsYesNo,
        answers.trustContactMethod,
        answers.email,
        answers.phone,
        answers.mobile
      ) &&
      optionalAnswer(answers.utrYesNo, answers.utr)(nonBlank) &&
      optionalAnswer(answers.worksReferenceYesNo, answers.worksReference)(nonBlank) &&
      (!answers.showVerificationDetails || nonBlank(answers.utr))

  private def contactAnswers(
    yesNo: Option[Boolean],
    selectedMethods: Set[ContactMethodOptions],
    email: Option[String],
    phone: Option[String],
    mobile: Option[String]
  ): Boolean =
    yesNo.exists {
      case true =>
        selectedMethods.nonEmpty &&
        (!selectedMethods.contains(ContactMethodOptions.Email) || nonBlank(email)) &&
        (!selectedMethods.contains(ContactMethodOptions.Phone) || nonBlank(phone)) &&
        (!selectedMethods.contains(ContactMethodOptions.Mobile) || nonBlank(mobile))

      case false =>
        selectedMethods.isEmpty
    }

  private def optionalAnswer[A](
    yesNo: Option[Boolean],
    answer: Option[A]
  )(valid: A => Boolean): Boolean =
    yesNo.exists {
      case true  => answer.exists(valid)
      case false => answer.isEmpty
    }

  private def nonBlank(value: Option[String]): Boolean =
    value.exists(nonBlank)

  private def nonBlank(value: String): Boolean =
    value.trim.nonEmpty

  private def isValidAddress(address: Address): Boolean =
    nonBlank(address.addressLine1)
}
