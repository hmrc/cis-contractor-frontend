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

package utils

import models.{JourneyNamespace, UserAnswers}
import pages.add.*
import pages.add.partnership.*
import pages.add.company.*
import pages.add.trust.*
import pages.verify.*

import scala.util.{Success, Try}

object SubcontractorCleanup {

  def removeIndividualSoleTraderSubcontractor(userAnswers: UserAnswers): Try[UserAnswers] =
    userAnswers
      .remove(AddressOfSubcontractorPage)
      .flatMap(_.remove(AddIndividualContactMethodsYesNoPage))
      .flatMap(_.remove(IndividualNamesOptionsPage))
      .flatMap(_.remove(IndividualContactMethodOptionsPage))
      .flatMap(_.remove(IndividualEmailAddressPage))
      .flatMap(_.remove(IndividualMobileNumberPage))
      .flatMap(_.remove(IndividualPhoneNumberPage))
      .flatMap(_.remove(NationalInsuranceNumberYesNoPage))
      .flatMap(_.remove(SubAddressYesNoPage))
      .flatMap(_.remove(SubcontractorNamePage))
      .flatMap(_.remove(SubcontractorsUniqueTaxpayerReferencePage))
      .flatMap(_.remove(SubNationalInsuranceNumberPage))
      .flatMap(_.remove(TradingNameOfSubcontractorPage))
      .flatMap(_.remove(UniqueTaxpayerReferenceYesNoPage))
      .flatMap(_.remove(WorksReferenceNumberPage))
      .flatMap(_.remove(WorksReferenceNumberYesNoPage))

  // Multi-tab
  def removeIndividualSoleTraderSubcontractor(
    userAnswers: UserAnswers,
    id: String,
    namespace: JourneyNamespace
  ): Try[UserAnswers] =
    userAnswers
      .remove(AddressOfSubcontractorPage(id, namespace))
      .flatMap(_.remove(AddIndividualContactMethodsYesNoPage(id, namespace)))
      .flatMap(_.remove(IndividualNamesOptionsPage(id, namespace)))
      .flatMap(_.remove(IndividualContactMethodOptionsPage(id, namespace)))
      .flatMap(_.remove(IndividualEmailAddressPage(id, namespace)))
      .flatMap(_.remove(IndividualMobileNumberPage(id, namespace)))
      .flatMap(_.remove(IndividualPhoneNumberPage(id, namespace)))
      .flatMap(_.remove(NationalInsuranceNumberYesNoPage(id, namespace)))
      .flatMap(_.remove(SubAddressYesNoPage(id, namespace)))
      .flatMap(_.remove(SubcontractorNamePage(id, namespace)))
      .flatMap(_.remove(SubcontractorsUniqueTaxpayerReferencePage(id, namespace)))
      .flatMap(_.remove(SubNationalInsuranceNumberPage(id, namespace)))
      .flatMap(_.remove(TradingNameOfSubcontractorPage(id, namespace)))
      .flatMap(_.remove(UniqueTaxpayerReferenceYesNoPage(id, namespace)))
      .flatMap(_.remove(WorksReferenceNumberPage(id, namespace)))
      .flatMap(_.remove(WorksReferenceNumberYesNoPage(id, namespace)))
  
  def removeLimitedCompanySubcontractor(userAnswers: UserAnswers): Try[UserAnswers] =
    userAnswers
      .remove(CompanyAddressPage)
      .flatMap(_.remove(CompanyAddressYesNoPage))
      .flatMap(_.remove(AddCompanyContactMethodsYesNoPage))
      .flatMap(_.remove(CompanyContactMethodOptionsPage))
      .flatMap(_.remove(CompanyCrnPage))
      .flatMap(_.remove(CompanyCrnYesNoPage))
      .flatMap(_.remove(CompanyEmailAddressPage))
      .flatMap(_.remove(CompanyMobileNumberPage))
      .flatMap(_.remove(CompanyNamePage))
      .flatMap(_.remove(CompanyPhoneNumberPage))
      .flatMap(_.remove(CompanyUtrPage))
      .flatMap(_.remove(CompanyUtrYesNoPage))
      .flatMap(_.remove(CompanyWorksReferencePage))
      .flatMap(_.remove(CompanyWorksReferenceYesNoPage))

  def removeLimitedCompanySubcontractor(
    userAnswers: UserAnswers,
    id: String,
    namespace: JourneyNamespace
  ): Try[UserAnswers] =
    userAnswers
      .remove(CompanyAddressPage(id, namespace))
      .flatMap(_.remove(CompanyAddressYesNoPage(id, namespace)))
      .flatMap(_.remove(AddCompanyContactMethodsYesNoPage(id, namespace)))
      .flatMap(_.remove(CompanyContactMethodOptionsPage(id, namespace)))
      .flatMap(_.remove(CompanyCrnPage(id, namespace)))
      .flatMap(_.remove(CompanyCrnYesNoPage(id, namespace)))
      .flatMap(_.remove(CompanyEmailAddressPage(id, namespace)))
      .flatMap(_.remove(CompanyMobileNumberPage(id, namespace)))
      .flatMap(_.remove(CompanyNamePage(id, namespace)))
      .flatMap(_.remove(CompanyPhoneNumberPage(id, namespace)))
      .flatMap(_.remove(CompanyUtrPage(id, namespace)))
      .flatMap(_.remove(CompanyUtrYesNoPage(id, namespace)))
      .flatMap(_.remove(CompanyWorksReferencePage(id, namespace)))
      .flatMap(_.remove(CompanyWorksReferenceYesNoPage(id, namespace)))
  
  def removePartnershipSubcontractor(userAnswers: UserAnswers): Try[UserAnswers] =
    userAnswers
      .remove(PartnershipAddressPage)
      .flatMap(_.remove(PartnershipAddressYesNoPage))
      .flatMap(_.remove(AddPartnershipContactMethodsYesNoPage))
      .flatMap(_.remove(PartnershipContactMethodOptionsPage))
      .flatMap(_.remove(PartnershipEmailAddressPage))
      .flatMap(_.remove(PartnershipHasUtrYesNoPage))
      .flatMap(_.remove(PartnershipMobileNumberPage))
      .flatMap(_.remove(PartnershipNamePage))
      .flatMap(_.remove(PartnershipNominatedPartnerCrnPage))
      .flatMap(_.remove(PartnershipNominatedPartnerCrnYesNoPage))
      .flatMap(_.remove(PartnershipNominatedPartnerNamePage))
      .flatMap(_.remove(PartnershipNominatedPartnerNinoPage))
      .flatMap(_.remove(PartnershipNominatedPartnerNinoYesNoPage))
      .flatMap(_.remove(PartnershipNominatedPartnerUtrPage))
      .flatMap(_.remove(PartnershipNominatedPartnerUtrYesNoPage))
      .flatMap(_.remove(PartnershipPhoneNumberPage))
      .flatMap(_.remove(PartnershipUniqueTaxpayerReferencePage))
      .flatMap(_.remove(PartnershipWorksReferenceNumberPage))
      .flatMap(_.remove(PartnershipWorksReferenceNumberYesNoPage))

  def removePartnershipSubcontractor(
    userAnswers: UserAnswers,
    id: String,
    namespace: JourneyNamespace
  ): Try[UserAnswers] =
    userAnswers
      .remove(PartnershipAddressPage(id, namespace))
      .flatMap(_.remove(PartnershipAddressYesNoPage(id, namespace)))
      .flatMap(_.remove(AddPartnershipContactMethodsYesNoPage(id, namespace)))
      .flatMap(_.remove(PartnershipContactMethodOptionsPage(id, namespace)))
      .flatMap(_.remove(PartnershipEmailAddressPage(id, namespace)))
      .flatMap(_.remove(PartnershipHasUtrYesNoPage(id, namespace)))
      .flatMap(_.remove(PartnershipMobileNumberPage(id, namespace)))
      .flatMap(_.remove(PartnershipNamePage(id, namespace)))
      .flatMap(_.remove(PartnershipNominatedPartnerCrnPage(id, namespace)))
      .flatMap(_.remove(PartnershipNominatedPartnerCrnYesNoPage(id, namespace)))
      .flatMap(_.remove(PartnershipNominatedPartnerNamePage(id, namespace)))
      .flatMap(_.remove(PartnershipNominatedPartnerNinoPage(id, namespace)))
      .flatMap(_.remove(PartnershipNominatedPartnerNinoYesNoPage(id, namespace)))
      .flatMap(_.remove(PartnershipNominatedPartnerUtrPage(id, namespace)))
      .flatMap(_.remove(PartnershipNominatedPartnerUtrYesNoPage(id, namespace)))
      .flatMap(_.remove(PartnershipPhoneNumberPage(id, namespace)))
      .flatMap(_.remove(PartnershipUniqueTaxpayerReferencePage(id, namespace)))
      .flatMap(_.remove(PartnershipWorksReferenceNumberPage(id, namespace)))
      .flatMap(_.remove(PartnershipWorksReferenceNumberYesNoPage(id, namespace)))
      .flatMap { updatedAnswers =>
        if namespace == JourneyNamespace.AddSubcontractor then
          updatedAnswers.remove(CheckYourAnswersSubmittedPage(id))
        else
          Success(updatedAnswers)
      }

  def removeTrustSubcontractor(userAnswers: UserAnswers): Try[UserAnswers] =
    userAnswers
      .remove(TrustAddressPage)
      .flatMap(_.remove(TrustAddressYesNoPage))
      .flatMap(_.remove(AddTrustContactMethodsYesNoPage))
      .flatMap(_.remove(TrustContactMethodOptionsPage))
      .flatMap(_.remove(TrustEmailAddressPage))
      .flatMap(_.remove(TrustMobileNumberPage))
      .flatMap(_.remove(TrustNamePage))
      .flatMap(_.remove(TrustPhoneNumberPage))
      .flatMap(_.remove(TrustUtrPage))
      .flatMap(_.remove(TrustUtrYesNoPage))
      .flatMap(_.remove(TrustWorksReferencePage))
      .flatMap(_.remove(TrustWorksReferenceYesNoPage))

  def removeTrustSubcontractor(
    userAnswers: UserAnswers,
    id: String,
    namespace: JourneyNamespace
  ): Try[UserAnswers] =
    userAnswers
      .remove(TrustAddressPage(id, namespace))
      .flatMap(_.remove(TrustAddressYesNoPage(id, namespace)))
      .flatMap(_.remove(AddTrustContactMethodsYesNoPage(id, namespace)))
      .flatMap(_.remove(TrustContactMethodOptionsPage(id, namespace)))
      .flatMap(_.remove(TrustEmailAddressPage(id, namespace)))
      .flatMap(_.remove(TrustMobileNumberPage(id, namespace)))
      .flatMap(_.remove(TrustNamePage(id, namespace)))
      .flatMap(_.remove(TrustPhoneNumberPage(id, namespace)))
      .flatMap(_.remove(TrustUtrPage(id, namespace)))
      .flatMap(_.remove(TrustUtrYesNoPage(id, namespace)))
      .flatMap(_.remove(TrustWorksReferencePage(id, namespace)))
      .flatMap(_.remove(TrustWorksReferenceYesNoPage(id, namespace)))
  
  def removeAllSubcontractor(userAnswers: UserAnswers): Try[UserAnswers] =
    removeIndividualSoleTraderSubcontractor(userAnswers)
      .flatMap(removeLimitedCompanySubcontractor)
      .flatMap(removePartnershipSubcontractor)
      .flatMap(removeTrustSubcontractor)
      .flatMap(_.remove(TypeOfSubcontractorPage))

  def removeVerifyJourney(userAnswers: UserAnswers): Try[UserAnswers] =
    userAnswers
      .remove(SelectSubcontractorPage)
      .flatMap(_.remove(ReverifyExistingSubcontractorsYesNoPage))
      .flatMap(_.remove(SelectSubcontractorsToReverifyPage))
      .flatMap(_.remove(ContractorEmailConfirmationStoredPage))
      .flatMap(_.remove(ContractorEmailConfirmationNotStoredPage))
      .flatMap(_.remove(EmailAddressPage))
      .flatMap(_.remove(VerifyYourSubcontractorsYesNoPage))
      .flatMap(_.remove(VerificationDeclarationPage))
      .flatMap(_.remove(CurrentVerificationBatchResponsePage))
      .flatMap(_.remove(NewestVerificationBatchResponsePage))
      .flatMap(_.remove(VerificationBatchReadinessPage))
      .flatMap(_.remove(RebuildVerificationFromWarningPage))

}
