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

package pages.add

import models.UserAnswers
import pages.add.partnership.*
import pages.add.company.*

import scala.util.Try

trait Cleanup {

  def removeIndividualSoleTraderSubcontractor(userAnswers: UserAnswers): Try[UserAnswers] = {
    userAnswers
      .remove(AddressOfSubcontractorPage)
      .flatMap(_.remove(NationalInsuranceNumberYesNoPage))
      .flatMap(_.remove(SubAddressYesNoPage))
      .flatMap(_.remove(SubContactDetailsPage))
      .flatMap(_.remove(SubcontractorContactDetailsYesNoPage))
      .flatMap(_.remove(SubcontractorNamePage))
      .flatMap(_.remove(SubcontractorsUniqueTaxpayerReferencePage))
      .flatMap(_.remove(SubNationalInsuranceNumberPage))
      .flatMap(_.remove(SubTradingNameYesNoPage))
      .flatMap(_.remove(TradingNameOfSubcontractorPage))
      .flatMap(_.remove(UniqueTaxpayerReferenceYesNoPage))
      .flatMap(_.remove(WorksReferenceNumberPage))
      .flatMap(_.remove(WorksReferenceNumberYesNoPage))
  }
  
  // TODO Add unit test for removeLimitedCompanySubcontractor when company journey is done
  def removeLimitedCompanySubcontractor(userAnswers: UserAnswers): Try[UserAnswers] = {
    userAnswers
      .remove(CompanyAddressPage)
      .flatMap(_.remove(CompanyAddressYesNoPage))
      .flatMap(_.remove(CompanyContactOptionsPage))
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
  }

  def removePartnershipSubcontractor(userAnswers: UserAnswers): Try[UserAnswers] = {
    userAnswers
      .remove(PartnershipAddressPage)
      .flatMap(_.remove(PartnershipAddressYesNoPage))
      .flatMap(_.remove(PartnershipChooseContactDetailsPage))
      .flatMap(_.remove(PartnershipContactDetailsYesNoPage))
      .flatMap(_.remove(PartnershipEmailAddressPage))
      .flatMap(_.remove(PartnershipHasUtrYesNoPage))
      .flatMap(_.remove(PartnershipNamePage))
      .flatMap(_.remove(PartnershipNominatedPartnerCrnPage))
      .flatMap(_.remove(PartnershipNominatedPartnerCrnYesNoPage))
      .flatMap(_.remove(PartnershipNominatedPartnerNamePage))
      .flatMap(_.remove(PartnershipNominatedPartnerNinoPage))
      .flatMap(_.remove(PartnershipNominatedPartnerNinoYesNoPage))
      .flatMap(_.remove(PartnershipNominatedPartnerUtrPage))
      .flatMap(_.remove(PartnershipNominatedPartnerUtrYesNoPage))
      .flatMap(_.remove(PartnershipUniqueTaxpayerReferencePage))
      .flatMap(_.remove(PartnershipWorksReferenceNumberPage))
      .flatMap(_.remove(PartnershipWorksReferenceNumberYesNoPage))
  }

//  def removeTrustSubcontractor(userAnswers: UserAnswers): Try[UserAnswers] = {
//    userAnswers
//      .remove(AddressOfSubcontractorPage)
//      .flatMap(_.remove(NationalInsuranceNumberYesNoPage))
//  }
}
