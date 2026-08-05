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
import pages.add.company.*
import pages.add.partnership.*
import queries.{OriginalCompanyAnswersQuery, OriginalPartnershipAnswersQuery}

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

  def partnershipHasChanges(userAnswers: UserAnswers): Boolean =
    userAnswers.get(OriginalPartnershipAnswersQuery).exists { original =>
      Seq(
        original.partnershipName                     -> userAnswers.get(PartnershipNamePage),
        original.addressYesNo                        -> userAnswers.get(PartnershipAddressYesNoPage),
        original.address                             -> userAnswers.get(PartnershipAddressPage),
        original.partnershipContactMethodsYesNo      -> userAnswers.get(AddPartnershipContactMethodsYesNoPage),
        original.partnershipContactMethodOptions     -> userAnswers
          .get(PartnershipContactMethodOptionsPage)
          .getOrElse(Set.empty),
        original.email                               -> userAnswers.get(PartnershipEmailAddressPage),
        original.phone                               -> userAnswers.get(PartnershipPhoneNumberPage),
        original.mobile                              -> userAnswers.get(PartnershipMobileNumberPage),
        original.hasUtrYesNo                         -> userAnswers.get(PartnershipHasUtrYesNoPage),
        original.utr                                 -> userAnswers.get(PartnershipUniqueTaxpayerReferencePage),
        original.nominatedPartnerName                -> userAnswers.get(PartnershipNominatedPartnerNamePage),
        original.nominatedPartnerUtrYesNo            -> userAnswers.get(PartnershipNominatedPartnerUtrYesNoPage),
        original.nominatedPartnerUtr                 -> userAnswers.get(PartnershipNominatedPartnerUtrPage),
        original.nominatedPartnerNinoYesNo           -> userAnswers.get(PartnershipNominatedPartnerNinoYesNoPage),
        original.nominatedPartnerNino                -> userAnswers.get(PartnershipNominatedPartnerNinoPage),
        original.nominatedPartnerCrnYesNo            -> userAnswers.get(PartnershipNominatedPartnerCrnYesNoPage),
        original.nominatedPartnerCrn                 -> userAnswers.get(PartnershipNominatedPartnerCrnPage),
        original.nominatedPartnerWorksReferenceYesNo -> userAnswers.get(PartnershipWorksReferenceNumberYesNoPage),
        original.nominatedPartnerWorksReference      -> userAnswers.get(PartnershipWorksReferenceNumberPage)
      ).exists { case (originalValue, currentValue) =>
        originalValue != currentValue
      }
    }
}
