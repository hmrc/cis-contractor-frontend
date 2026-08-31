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

package navigation.finalvalidation

import controllers.add.routes as soleTraderRoutes
import controllers.add.company.routes as companyRoutes
import controllers.add.partnership.routes as partnershipRoutes
import controllers.add.trust.routes as trustRoutes
import models.{FinalValidationMode, Mode, UserAnswers}
import models.finalvalidation.FinalValidationChangeTarget
import models.finalvalidation.FinalValidationChangeTarget.*
import models.TypeOfSubcontractor.*
import navigation.NavigatorForJourney
import pages.add.TypeOfSubcontractorPage
import pages.Page
import play.api.mvc.Call

import javax.inject.{Inject, Singleton}

@Singleton
class FinalValidationNavigator @Inject() extends NavigatorForJourney {

  def startPage(target: FinalValidationChangeTarget, userAnswers: UserAnswers): Call =
    userAnswers.get(TypeOfSubcontractorPage) match {
      case Some(Individualorsoletrader) => soleTraderStartPage(target)
      case Some(Limitedcompany)         => companyStartPage(target)
      case Some(Partnership)            => partnershipStartPage(target)
      case Some(Trust)                  => trustStartPage(target)
      case None                         => controllers.routes.JourneyRecoveryController.onPageLoad()
    }

  override def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call =
    controllers.finalvalidations.routes.FinalValidationCompleteController.onPageLoad()

  private def soleTraderStartPage(target: FinalValidationChangeTarget): Call =
    target match {
      case SubcontractorName         => soleTraderRoutes.SubcontractorNameController.onPageLoad(FinalValidationMode)
      case TradingName               => soleTraderRoutes.TradingNameOfSubcontractorController.onPageLoad(FinalValidationMode)
      case AddressYesNo              => soleTraderRoutes.SubAddressYesNoController.onPageLoad(FinalValidationMode)
      case Address                   =>
        soleTraderRoutes.AddressOfSubcontractorController.redirectToAddressLookup(FinalValidationMode, None)
      case ContactDetailsYesNo       =>
        soleTraderRoutes.AddIndividualContactMethodsYesNoController.onPageLoad(FinalValidationMode)
      case EmailAddress              => soleTraderRoutes.IndividualEmailAddressController.onPageLoad(FinalValidationMode)
      case PhoneNumber               => soleTraderRoutes.IndividualPhoneNumberController.onPageLoad(FinalValidationMode)
      case MobilePhoneNumber         => soleTraderRoutes.IndividualMobileNumberController.onPageLoad(FinalValidationMode)
      case UtrYesNo                  => soleTraderRoutes.UniqueTaxpayerReferenceYesNoController.onPageLoad(FinalValidationMode)
      case Utr                       => soleTraderRoutes.SubcontractorsUniqueTaxpayerReferenceController.onPageLoad(FinalValidationMode)
      case NinoYesNo                 => soleTraderRoutes.NationalInsuranceNumberYesNoController.onPageLoad(FinalValidationMode)
      case Nino                      => soleTraderRoutes.SubNationalInsuranceNumberController.onPageLoad(FinalValidationMode)
      case WorksReferenceNumberYesNo =>
        soleTraderRoutes.WorksReferenceNumberYesNoController.onPageLoad(FinalValidationMode)
      case WorksReferenceNumber      => soleTraderRoutes.WorksReferenceNumberController.onPageLoad(FinalValidationMode)
      case _                         => controllers.routes.JourneyRecoveryController.onPageLoad()
    }

  private def companyStartPage(target: FinalValidationChangeTarget): Call =
    target match {
      case TradingName               => companyRoutes.CompanyNameController.onPageLoad(FinalValidationMode)
      case AddressYesNo              => companyRoutes.CompanyAddressYesNoController.onPageLoad(FinalValidationMode)
      case Address                   => companyRoutes.CompanyAddressController.redirectToAddressLookup(FinalValidationMode, None)
      case ContactDetailsYesNo       => companyRoutes.AddCompanyContactMethodsYesNoController.onPageLoad(FinalValidationMode)
      case EmailAddress              => companyRoutes.CompanyEmailAddressController.onPageLoad(FinalValidationMode)
      case PhoneNumber               => companyRoutes.CompanyPhoneNumberController.onPageLoad(FinalValidationMode)
      case MobilePhoneNumber         => companyRoutes.CompanyMobileNumberController.onPageLoad(FinalValidationMode)
      case UtrYesNo                  => companyRoutes.CompanyUtrYesNoController.onPageLoad(FinalValidationMode)
      case Utr                       => companyRoutes.CompanyUtrController.onPageLoad(FinalValidationMode)
      case CrnYesNo                  => companyRoutes.CompanyCrnYesNoController.onPageLoad(FinalValidationMode)
      case Crn                       => companyRoutes.CompanyCrnController.onPageLoad(FinalValidationMode)
      case WorksReferenceNumberYesNo =>
        companyRoutes.CompanyWorksReferenceYesNoController.onPageLoad(FinalValidationMode)
      case WorksReferenceNumber      => companyRoutes.CompanyWorksReferenceController.onPageLoad(FinalValidationMode)
      case _                         => controllers.routes.JourneyRecoveryController.onPageLoad()
    }

  private def trustStartPage(target: FinalValidationChangeTarget): Call =
    target match {
      case TradingName               => trustRoutes.TrustNameController.onPageLoad(FinalValidationMode)
      case AddressYesNo              => trustRoutes.TrustAddressYesNoController.onPageLoad(FinalValidationMode)
      case Address                   => trustRoutes.TrustAddressController.redirectToAddressLookup(FinalValidationMode, None)
      case ContactDetailsYesNo       => trustRoutes.AddTrustContactMethodsYesNoController.onPageLoad(FinalValidationMode)
      case EmailAddress              => trustRoutes.TrustEmailAddressController.onPageLoad(FinalValidationMode)
      case PhoneNumber               => trustRoutes.TrustPhoneNumberController.onPageLoad(FinalValidationMode)
      case MobilePhoneNumber         => trustRoutes.TrustMobileNumberController.onPageLoad(FinalValidationMode)
      case UtrYesNo                  => trustRoutes.TrustUtrYesNoController.onPageLoad(FinalValidationMode)
      case Utr                       => trustRoutes.TrustUtrController.onPageLoad(FinalValidationMode)
      case WorksReferenceNumberYesNo => trustRoutes.TrustWorksReferenceYesNoController.onPageLoad(FinalValidationMode)
      case WorksReferenceNumber      => trustRoutes.TrustWorksReferenceController.onPageLoad(FinalValidationMode)
      case _                         => controllers.routes.JourneyRecoveryController.onPageLoad()
    }

  private def partnershipStartPage(target: FinalValidationChangeTarget): Call =
    target match {
      case PartnershipTradingName    => partnershipRoutes.PartnershipNameController.onPageLoad(FinalValidationMode)
      case TradingName               => partnershipRoutes.PartnershipNominatedPartnerNameController.onPageLoad(FinalValidationMode)
      case AddressYesNo              => partnershipRoutes.PartnershipAddressYesNoController.onPageLoad(FinalValidationMode)
      case Address                   => partnershipRoutes.PartnershipAddressController.redirectToAddressLookup(FinalValidationMode, None)
      case ContactDetailsYesNo       =>
        partnershipRoutes.AddPartnershipContactMethodsYesNoController.onPageLoad(FinalValidationMode)
      case EmailAddress              => partnershipRoutes.PartnershipEmailAddressController.onPageLoad(FinalValidationMode)
      case PhoneNumber               => partnershipRoutes.PartnershipPhoneNumberController.onPageLoad(FinalValidationMode)
      case MobilePhoneNumber         => partnershipRoutes.PartnershipMobileNumberController.onPageLoad(FinalValidationMode)
      case UtrYesNo                  => partnershipRoutes.PartnershipHasUtrYesNoController.onPageLoad(FinalValidationMode)
      case Utr                       => partnershipRoutes.PartnershipUniqueTaxpayerReferenceController.onPageLoad(FinalValidationMode)
      case PartnerUtrYesNo           =>
        partnershipRoutes.PartnershipNominatedPartnerUtrYesNoController.onPageLoad(FinalValidationMode)
      case PartnerUtr                => partnershipRoutes.PartnershipNominatedPartnerUtrController.onPageLoad(FinalValidationMode)
      case NinoYesNo                 => partnershipRoutes.PartnershipNominatedPartnerNinoYesNoController.onPageLoad(FinalValidationMode)
      case Nino                      => partnershipRoutes.PartnershipNominatedPartnerNinoController.onPageLoad(FinalValidationMode)
      case CrnYesNo                  => partnershipRoutes.PartnershipNominatedPartnerCrnYesNoController.onPageLoad(FinalValidationMode)
      case Crn                       => partnershipRoutes.PartnershipNominatedPartnerCrnController.onPageLoad(FinalValidationMode)
      case WorksReferenceNumberYesNo =>
        partnershipRoutes.PartnershipWorksReferenceNumberYesNoController.onPageLoad(FinalValidationMode)
      case WorksReferenceNumber      =>
        partnershipRoutes.PartnershipWorksReferenceNumberController.onPageLoad(FinalValidationMode)
      case _                         => controllers.routes.JourneyRecoveryController.onPageLoad()
    }

}
