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

import models.UserAnswers
import pages.add.*
import pages.add.company.*
import pages.add.trust.*
import queries.{OriginalCompanyAnswersQuery, OriginalIndividualAnswersQuery, OriginalTrustAnswersQuery}

object AmendmentHelper {

  def companyHasChanges(userAnswers: UserAnswers): Boolean =
    userAnswers.get(OriginalCompanyAnswersQuery).exists { original =>
      Seq(
        original.companyName                -> userAnswers.get(CompanyNamePage),
        original.addressYesNo               -> userAnswers.get(CompanyAddressYesNoPage),
        original.address                    -> userAnswers.get(CompanyAddressPage),
        original.companyContactMethodsYesNo -> userAnswers.get(AddCompanyContactMethodsYesNoPage),
        original.companyContactMethod       -> userAnswers.get(CompanyContactMethodOptionsPage).getOrElse(Set.empty),
        original.email                      -> userAnswers.get(CompanyEmailAddressPage),
        original.phone                      -> userAnswers.get(CompanyPhoneNumberPage),
        original.mobile                     -> userAnswers.get(CompanyMobileNumberPage),
        original.utrYesNo                   -> userAnswers.get(CompanyUtrYesNoPage),
        original.utr                        -> userAnswers.get(CompanyUtrPage),
        original.crnYesNo                   -> userAnswers.get(CompanyCrnYesNoPage),
        original.crn                        -> userAnswers.get(CompanyCrnPage),
        original.worksReferenceYesNo        -> userAnswers.get(CompanyWorksReferenceYesNoPage),
        original.worksReference             -> userAnswers.get(CompanyWorksReferencePage)
      ).exists { case (originalValue, currentValue) =>
        originalValue != currentValue
      }
    }

  def trustHasChanges(userAnswers: UserAnswers): Boolean =
    userAnswers.get(OriginalTrustAnswersQuery).exists { original =>
      Seq(
        original.trustName                -> userAnswers.get(TrustNamePage),
        original.addressYesNo             -> userAnswers.get(TrustAddressYesNoPage),
        original.address                  -> userAnswers.get(TrustAddressPage),
        original.trustContactMethodsYesNo -> userAnswers.get(AddTrustContactMethodsYesNoPage),
        original.trustContactMethod       -> userAnswers.get(TrustContactMethodOptionsPage).getOrElse(Set.empty),
        original.email                    -> userAnswers.get(TrustEmailAddressPage),
        original.phone                    -> userAnswers.get(TrustPhoneNumberPage),
        original.mobile                   -> userAnswers.get(TrustMobileNumberPage),
        original.utrYesNo                 -> userAnswers.get(TrustUtrYesNoPage),
        original.utr                      -> userAnswers.get(TrustUtrPage),
        original.worksReferenceYesNo      -> userAnswers.get(TrustWorksReferenceYesNoPage),
        original.worksReference           -> userAnswers.get(TrustWorksReferencePage)
      ).exists { case (originalValue, currentValue) =>
        originalValue != currentValue
      }
    }

  def individualHasChanges(userAnswers: UserAnswers): Boolean =
    userAnswers.get(OriginalIndividualAnswersQuery).exists { original =>
      Seq(
        original.usesTradingName               -> userAnswers.get(SubTradingNameYesNoPage),
        original.tradingName                   -> userAnswers.get(TradingNameOfSubcontractorPage),
        original.subcontractorName             -> userAnswers.get(SubcontractorNamePage),
        original.addressYesNo                  -> userAnswers.get(SubAddressYesNoPage),
        original.address                       -> userAnswers.get(AddressOfSubcontractorPage),
        original.individualContactMethodsYesNo -> userAnswers.get(AddIndividualContactMethodsYesNoPage),
        original.individualContactMethod       -> userAnswers.get(IndividualContactMethodOptionsPage).getOrElse(Set.empty),
        original.email                         -> userAnswers.get(IndividualEmailAddressPage),
        original.phone                         -> userAnswers.get(IndividualPhoneNumberPage),
        original.mobile                        -> userAnswers.get(IndividualMobileNumberPage),
        original.utrYesNo                      -> userAnswers.get(UniqueTaxpayerReferenceYesNoPage),
        original.utr                           -> userAnswers.get(SubcontractorsUniqueTaxpayerReferencePage),
        original.ninoYesNo                     -> userAnswers.get(NationalInsuranceNumberYesNoPage),
        original.nino                          -> userAnswers.get(SubNationalInsuranceNumberPage),
        original.worksReferenceYesNo           -> userAnswers.get(WorksReferenceNumberYesNoPage),
        original.worksReference                -> userAnswers.get(WorksReferenceNumberPage)
      ).exists { case (originalValue, currentValue) =>
        originalValue != currentValue
      }
    }
}
