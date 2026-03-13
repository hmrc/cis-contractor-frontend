/*
 * Copyright 2025 HM Revenue & Customs
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

package navigation

import javax.inject.{Inject, Singleton}
import play.api.mvc.Call
import controllers.routes
import pages.*
import models.*
import models.add.TypeOfSubcontractor.*
import models.contact.ContactOptions.*
import pages.add.*
import pages.add.company.*
import pages.add.partnership.*

@Singleton
class Navigator @Inject() () {

  private val normalRoutes: Page => UserAnswers => Call = {
    case TypeOfSubcontractorPage                   => userAnswers => navigatorFromTypeOfSubcontractorPage(NormalMode)(userAnswers)
    case SubTradingNameYesNoPage                   => userAnswers => navigatorFromSubTradingNameYesNoPage(NormalMode)(userAnswers)
    case TradingNameOfSubcontractorPage            => _ => controllers.add.routes.SubAddressYesNoController.onPageLoad(NormalMode)
    case SubcontractorNamePage                     => _ => controllers.add.routes.SubAddressYesNoController.onPageLoad(NormalMode)
    case SubAddressYesNoPage                       => userAnswers => navigatorFromSubAddressYesNoPage(NormalMode)(userAnswers)
    case AddressOfSubcontractorPage                =>
      _ => controllers.add.routes.NationalInsuranceNumberYesNoController.onPageLoad(NormalMode)
    case PartnershipAddressPage                    =>
      _ => controllers.add.partnership.routes.PartnershipChooseContactDetailsController.onPageLoad(NormalMode)
    case NationalInsuranceNumberYesNoPage          =>
      userAnswers => navigatorFromNationalInsuranceNumberYesNoPage(NormalMode)(userAnswers)
    case SubNationalInsuranceNumberPage            =>
      _ => controllers.add.routes.UniqueTaxpayerReferenceYesNoController.onPageLoad(NormalMode)
    case UniqueTaxpayerReferenceYesNoPage          =>
      userAnswers => navigatorFromUniqueTaxpayerReferenceYesNoPage(NormalMode)(userAnswers)
    case SubcontractorsUniqueTaxpayerReferencePage =>
      _ => controllers.add.routes.WorksReferenceNumberYesNoController.onPageLoad(NormalMode)
    case WorksReferenceNumberYesNoPage             =>
      userAnswers => navigatorFromWorksReferenceNumberYesNoPage(NormalMode)(userAnswers)
    case WorksReferenceNumberPage                  =>
      _ => controllers.add.routes.SubcontractorContactDetailsYesNoController.onPageLoad(NormalMode)
    case SubcontractorContactDetailsYesNoPage      =>
      userAnswers => navigatorFromSubcontractorContactDetailsYesNoPage(NormalMode)(userAnswers)
    case SubContactDetailsPage                     => _ => controllers.add.routes.CheckYourAnswersController.onPageLoad()
    case CompanyNamePage                           => _ => controllers.add.company.routes.CompanyNameController.onPageLoad(NormalMode)
    case CompanyAddressYesNoPage                   =>
      _ => controllers.add.company.routes.CompanyAddressYesNoController.onPageLoad(NormalMode)
    case CompanyContactOptionsPage                 =>
      userAnswers => navigatorFromCompanyContactOptionsPage(NormalMode)(userAnswers)
    case CompanyEmailAddressPage                   =>
      _ => controllers.add.company.routes.CompanyEmailAddressController.onPageLoad(NormalMode)
    case CompanyMobileNumberPage                   =>
      _ => controllers.add.company.routes.CompanyMobileNumberController.onPageLoad(NormalMode)
    case PartnershipNamePage                       =>
      _ => controllers.add.partnership.routes.PartnershipHasUtrYesNoController.onPageLoad(NormalMode)
    case PartnershipHasUtrYesNoPage                => userAnswers => navigatorFromPartnershipHasUtrYesNoPage(NormalMode)(userAnswers)
    case PartnershipUniqueTaxpayerReferencePage    =>
      _ => controllers.add.partnership.routes.PartnershipWorksReferenceNumberYesNoController.onPageLoad(NormalMode)
    case PartnershipWorksReferenceNumberYesNoPage  =>
      userAnswers => navigatorFromPartnershipWorksReferenceNumberYesNoPage(NormalMode)(userAnswers)
    case PartnershipNominatedPartnerNinoPage       =>
      _ => controllers.add.partnership.routes.PartnershipNominatedPartnerNinoController.onPageLoad(NormalMode)
    case PartnershipWorksReferenceNumberPage       =>
      _ => controllers.add.partnership.routes.PartnershipAddressYesNoController.onPageLoad(NormalMode)
    case PartnershipAddressYesNoPage               => userAnswers => navigatorFromPartnershipAddressYesNoPage(NormalMode)(userAnswers)
    case PartnershipNominatedPartnerCrnPage        =>
      _ => controllers.add.partnership.routes.PartnershipNominatedPartnerCrnController.onPageLoad(NormalMode)
    case PartnershipChooseContactDetailsPage       =>
      userAnswers => navigatorFromChooseContactDetailsPage(NormalMode)(userAnswers)
    case PartnershipNominatedPartnerNinoYesNoPage  =>
      userAnswers => navigatorFromPartnershipNominatedPartnerNinoYesNoPage(NormalMode)(userAnswers)
    case PartnershipNominatedPartnerNamePage       =>
      _ => controllers.add.partnership.routes.PartnershipNominatedPartnerNameController.onPageLoad(NormalMode)
    case PartnershipNominatedPartnerCrnYesNoPage   =>
      userAnswers => navigatorFromPartnershipNominatedPartnerCrnYesNoPage(NormalMode)(userAnswers)
    case PartnershipNominatedPartnerUtrPage        =>
      _ => controllers.add.partnership.routes.PartnershipNominatedPartnerUtrController.onPageLoad(NormalMode)
    case PartnershipEmailAddressPage               =>
      _ => controllers.add.partnership.routes.PartnershipEmailAddressController.onPageLoad(NormalMode)
    case PartnershipNominatedPartnerUtrYesNoPage   =>
      userAnswers => navigatorFromPartnershipNominatedPartnerUtrYesNoPage(NormalMode)(userAnswers)
    case CompanyAddressPage                        =>
      _ => controllers.add.company.routes.CompanyAddressController.onPageLoad(NormalMode)
    case CompanyUtrYesNoPage                       =>
      userAnswers => navigatorFromCompanyUtrYesNoPage(NormalMode)(userAnswers)
    case PartnershipMobileNumberPage               =>
      _ => controllers.add.partnership.routes.PartnershipMobileNumberController.onPageLoad(NormalMode)
    case PartnershipPhoneNumberPage                =>
      _ => controllers.add.partnership.routes.PartnershipPhoneNumberController.onPageLoad(NormalMode)
    case CompanyPhoneNumberPage                    =>
      _ => controllers.add.company.routes.CompanyPhoneNumberController.onPageLoad(NormalMode)
    case CompanyWorksReferenceYesNoPage            =>
      userAnswers => navigatorFromCompanyWorksReferenceYesNoPage(NormalMode)(userAnswers)
    case CompanyCrnPage                            =>
      _ => controllers.add.company.routes.CompanyWorksReferenceYesNoController.onPageLoad(NormalMode)
    case CompanyUtrPage                            =>
      _ => controllers.add.company.routes.CompanyCrnYesNoController.onPageLoad(NormalMode)
    case IndividualMobileNumberPage                =>
      _ => controllers.add.routes.UniqueTaxpayerReferenceYesNoController.onPageLoad(NormalMode)
    case CompanyCrnYesNoPage                       =>
      userAnswers => navigatorFromCompanyCrnYesNoPage(NormalMode)(userAnswers)
    case IndividualPhoneNumberPage                 =>
      _ => controllers.add.routes.UniqueTaxpayerReferenceYesNoController.onPageLoad(NormalMode)
    case CompanyWorksReferencePage                 =>
      _ => controllers.add.company.routes.CompanyCheckYourAnswersController.onPageLoad()
    case IndividualEmailAddressPage                =>
      _ => controllers.add.routes.UniqueTaxpayerReferenceYesNoController.onPageLoad(NormalMode)
    case _                                         => _ => routes.IndexController.onPageLoad()
  }

  private val checkRouteMap: Page => UserAnswers => Call = {
    case TypeOfSubcontractorPage                  => navigatorFromTypeOfSubcontractorPage(CheckMode)(_)
    case SubTradingNameYesNoPage                  => navigatorFromSubTradingNameYesNoPage(CheckMode)(_)
    case SubAddressYesNoPage                      => navigatorFromSubAddressYesNoPage(CheckMode)(_)
    case NationalInsuranceNumberYesNoPage         => navigatorFromNationalInsuranceNumberYesNoPage(CheckMode)(_)
    case UniqueTaxpayerReferenceYesNoPage         => navigatorFromUniqueTaxpayerReferenceYesNoPage(CheckMode)(_)
    case WorksReferenceNumberYesNoPage            => navigatorFromWorksReferenceNumberYesNoPage(CheckMode)(_)
    case SubcontractorContactDetailsYesNoPage     => navigatorFromSubcontractorContactDetailsYesNoPage(CheckMode)(_)
    case CompanyContactOptionsPage                => navigatorFromCompanyContactOptionsPage(CheckMode)(_)
    case CompanyAddressYesNoPage                  =>
      _ => controllers.add.company.routes.CompanyAddressYesNoController.onPageLoad(CheckMode)
    case CompanyAddressPage                       =>
      _ => controllers.add.company.routes.CompanyAddressController.onPageLoad(CheckMode)
    case CompanyEmailAddressPage                  =>
      _ => controllers.add.company.routes.CompanyEmailAddressController.onPageLoad(CheckMode)
    case CompanyPhoneNumberPage                   =>
      _ => controllers.add.company.routes.CompanyPhoneNumberController.onPageLoad(CheckMode)
    case CompanyNamePage                          =>
      _ => controllers.add.company.routes.CompanyCheckYourAnswersController.onPageLoad()
    case CompanyUtrYesNoPage                      =>
      userAnswers => navigatorFromCompanyUtrYesNoPage(CheckMode)(userAnswers)
    case PartnershipNamePage                      =>
      _ => controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad()
    case PartnershipHasUtrYesNoPage               => navigatorFromPartnershipHasUtrYesNoPage(CheckMode)(_)
    case PartnershipUniqueTaxpayerReferencePage   =>
      _ => controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad()
    case PartnershipWorksReferenceNumberYesNoPage => navigatorFromPartnershipWorksReferenceNumberYesNoPage(CheckMode)(_)
    case PartnershipWorksReferenceNumberPage      =>
      _ => controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad()
    case PartnershipChooseContactDetailsPage      => navigatorFromChooseContactDetailsPage(CheckMode)(_)
    case PartnershipAddressYesNoPage              => navigatorFromPartnershipAddressYesNoPage(CheckMode)(_)
    case PartnershipNominatedPartnerCrnYesNoPage  => navigatorFromPartnershipNominatedPartnerCrnYesNoPage(CheckMode)(_)
    case CompanyCrnYesNoPage                      => navigatorFromCompanyCrnYesNoPage(CheckMode)(_)
    case PartnershipAddressPage                   =>
      _ => controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad()
    case PartnershipEmailAddressPage              =>
      _ => controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad()
    case PartnershipMobileNumberPage              =>
      _ => controllers.add.partnership.routes.PartnershipMobileNumberController.onPageLoad(CheckMode)
    case PartnershipPhoneNumberPage               => _ => controllers.add.routes.CheckYourAnswersController.onPageLoad()
    case PartnershipNominatedPartnerNamePage      =>
      _ => controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad()
    case PartnershipNominatedPartnerUtrYesNoPage  =>
      userAnswers => navigatorFromPartnershipNominatedPartnerUtrYesNoPage(CheckMode)(userAnswers)
    case PartnershipNominatedPartnerUtrPage       =>
      _ => controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad()
    case PartnershipNominatedPartnerNinoYesNoPage => navigatorFromPartnershipNominatedPartnerNinoYesNoPage(CheckMode)(_)
    case PartnershipNominatedPartnerNinoPage      =>
      _ => controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad()
    case PartnershipNominatedPartnerCrnPage       =>
      _ => controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad()
    case CompanyWorksReferenceYesNoPage           =>
      userAnswers => navigatorFromCompanyWorksReferenceYesNoPage(CheckMode)(userAnswers)
    case CompanyCrnPage                           =>
      _ => controllers.add.company.routes.CompanyCheckYourAnswersController.onPageLoad()
    case CompanyUtrPage                           =>
      _ => controllers.add.company.routes.CompanyCheckYourAnswersController.onPageLoad()
    case CompanyWorksReferencePage                =>
      _ => controllers.add.company.routes.CompanyCheckYourAnswersController.onPageLoad()
    case IndividualMobileNumberPage               =>
      _ => controllers.add.routes.CheckYourAnswersController.onPageLoad()
    case CompanyMobileNumberPage                  =>
      _ => controllers.add.company.routes.CompanyMobileNumberController.onPageLoad(CheckMode)
      case IndividualPhoneNumberPage                =>
      _ => controllers.add.routes.CheckYourAnswersController.onPageLoad()
    case IndividualEmailAddressPage               =>
      _ => controllers.add.routes.CheckYourAnswersController.onPageLoad()
    case _                                        => _ => controllers.add.routes.CheckYourAnswersController.onPageLoad()
  }

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = mode match {
    case NormalMode =>
      normalRoutes(page)(userAnswers)
    case CheckMode  =>
      checkRouteMap(page)(userAnswers)
  }

  private def navigatorFromTypeOfSubcontractorPage(mode: Mode)(userAnswers: UserAnswers): Call =
    (userAnswers.get(TypeOfSubcontractorPage), mode) match {
      case (Some(Individualorsoletrader), NormalMode) =>
        controllers.add.routes.SubTradingNameYesNoController.onPageLoad(NormalMode)
      case (Some(Partnership), NormalMode)            =>
        controllers.add.partnership.routes.PartnershipNameController.onPageLoad(NormalMode)
      case (None, _)                                  => routes.JourneyRecoveryController.onPageLoad()
      case (_, NormalMode)                            => routes.JourneyRecoveryController.onPageLoad()
      case (_, CheckMode)                             => controllers.add.routes.CheckYourAnswersController.onPageLoad()
    }

  private def navigatorFromSubTradingNameYesNoPage(mode: Mode)(ua: UserAnswers): Call =
    (ua.get(SubTradingNameYesNoPage), mode) match {
      case (Some(true), NormalMode) =>
        controllers.add.routes.TradingNameOfSubcontractorController.onPageLoad(NormalMode)

      case (Some(false), NormalMode) =>
        controllers.add.routes.SubcontractorNameController.onPageLoad(NormalMode)

      case (Some(false), CheckMode) =>
        ua.get(SubcontractorNamePage) match {
          case None    => controllers.add.routes.SubcontractorNameController.onPageLoad(CheckMode)
          case Some(_) => controllers.add.routes.CheckYourAnswersController.onPageLoad()
        }

      case (Some(true), CheckMode) =>
        ua.get(TradingNameOfSubcontractorPage) match {
          case None    => controllers.add.routes.TradingNameOfSubcontractorController.onPageLoad(CheckMode)
          case Some(_) => controllers.add.routes.CheckYourAnswersController.onPageLoad()
        }

      case _ =>
        routes.JourneyRecoveryController.onPageLoad()
    }

  private def navigatorFromSubAddressYesNoPage(mode: Mode)(ua: UserAnswers): Call =
    (ua.get(SubAddressYesNoPage), mode) match {
      case (Some(true), NormalMode)  =>
        controllers.add.routes.AddressOfSubcontractorController.onPageLoad(NormalMode)
      case (Some(false), NormalMode) =>
        controllers.add.routes.NationalInsuranceNumberYesNoController.onPageLoad(NormalMode)
      case (Some(true), CheckMode)   =>
        ua.get(AddressOfSubcontractorPage)
          .fold(controllers.add.routes.AddressOfSubcontractorController.onPageLoad(CheckMode)) { _ =>
            controllers.add.routes.CheckYourAnswersController.onPageLoad()
          }
      case (Some(false), CheckMode)  =>
        controllers.add.routes.CheckYourAnswersController.onPageLoad()
      case _                         =>
        routes.JourneyRecoveryController.onPageLoad()
    }

  private def navigatorFromNationalInsuranceNumberYesNoPage(mode: Mode)(ua: UserAnswers): Call =
    (ua.get(NationalInsuranceNumberYesNoPage), mode) match {

      case (Some(true), NormalMode)  =>
        controllers.add.routes.SubNationalInsuranceNumberController.onPageLoad(NormalMode)
      case (Some(false), NormalMode) =>
        controllers.add.routes.UniqueTaxpayerReferenceYesNoController.onPageLoad(NormalMode)
      case (Some(true), CheckMode)   =>
        ua.get(SubNationalInsuranceNumberPage)
          .fold(controllers.add.routes.SubNationalInsuranceNumberController.onPageLoad(CheckMode)) { _ =>
            controllers.add.routes.CheckYourAnswersController.onPageLoad()
          }
      case (Some(false), CheckMode)  =>
        controllers.add.routes.CheckYourAnswersController.onPageLoad()
      case _                         =>
        routes.JourneyRecoveryController.onPageLoad()
    }

  private def navigatorFromUniqueTaxpayerReferenceYesNoPage(mode: Mode)(ua: UserAnswers): Call =
    (ua.get(UniqueTaxpayerReferenceYesNoPage), mode) match {
      case (Some(true), NormalMode) =>
        controllers.add.routes.SubcontractorsUniqueTaxpayerReferenceController.onPageLoad(NormalMode)

      case (Some(false), NormalMode) =>
        controllers.add.routes.WorksReferenceNumberYesNoController.onPageLoad(NormalMode)

      case (Some(true), CheckMode) =>
        ua.get(SubcontractorsUniqueTaxpayerReferencePage)
          .fold(controllers.add.routes.SubcontractorsUniqueTaxpayerReferenceController.onPageLoad(CheckMode)) { _ =>
            controllers.add.routes.CheckYourAnswersController.onPageLoad()
          }

      case (Some(false), CheckMode) =>
        controllers.add.routes.CheckYourAnswersController.onPageLoad()

      case _ =>
        routes.JourneyRecoveryController.onPageLoad()
    }

  private def navigatorFromWorksReferenceNumberYesNoPage(mode: Mode)(ua: UserAnswers): Call =
    (ua.get(WorksReferenceNumberYesNoPage), mode) match {

      case (Some(true), NormalMode) =>
        controllers.add.routes.WorksReferenceNumberController.onPageLoad(NormalMode)

      case (Some(false), NormalMode) =>
        controllers.add.routes.SubcontractorContactDetailsYesNoController.onPageLoad(NormalMode)
      case (Some(true), CheckMode)   =>
        ua.get(WorksReferenceNumberPage)
          .fold(controllers.add.routes.WorksReferenceNumberController.onPageLoad(CheckMode)) { _ =>
            controllers.add.routes.CheckYourAnswersController.onPageLoad()
          }

      case (Some(false), CheckMode) =>
        controllers.add.routes.CheckYourAnswersController.onPageLoad()

      case _ =>
        routes.JourneyRecoveryController.onPageLoad()
    }

  private def navigatorFromSubcontractorContactDetailsYesNoPage(mode: Mode)(ua: UserAnswers): Call =
    (ua.get(SubcontractorContactDetailsYesNoPage), mode) match {

      case (Some(true), NormalMode) =>
        controllers.add.routes.SubContactDetailsController.onPageLoad(NormalMode)

      case (Some(false), NormalMode) =>
        controllers.add.routes.CheckYourAnswersController.onPageLoad()

      case (Some(true), CheckMode) =>
        ua.get(SubContactDetailsPage)
          .fold(controllers.add.routes.SubContactDetailsController.onPageLoad(CheckMode)) { _ =>
            controllers.add.routes.CheckYourAnswersController.onPageLoad()
          }

      case (Some(false), CheckMode) =>
        controllers.add.routes.CheckYourAnswersController.onPageLoad()

      case _ =>
        routes.JourneyRecoveryController.onPageLoad()
    }

  private def navigatorFromPartnershipHasUtrYesNoPage(mode: Mode)(ua: UserAnswers): Call =
    (ua.get(PartnershipHasUtrYesNoPage), mode) match {
      case (Some(true), _) =>
        controllers.add.partnership.routes.PartnershipUniqueTaxpayerReferenceController.onPageLoad(mode)

      case (Some(false), NormalMode) =>
        controllers.add.partnership.routes.PartnershipWorksReferenceNumberYesNoController.onPageLoad(NormalMode)

      case (Some(false), CheckMode) =>
        controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad()

      case _ =>
        routes.JourneyRecoveryController.onPageLoad()
    }

  private def navigatorFromPartnershipWorksReferenceNumberYesNoPage(mode: Mode)(userAnswers: UserAnswers): Call =
    (userAnswers.get(PartnershipWorksReferenceNumberYesNoPage), mode) match {
      case (Some(true), _)           =>
        controllers.add.partnership.routes.PartnershipWorksReferenceNumberController.onPageLoad(mode)
      case (Some(false), NormalMode) =>
        controllers.add.partnership.routes.PartnershipAddressYesNoController.onPageLoad(NormalMode)
      case (Some(false), CheckMode)  =>
        controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad()
      case (None, _)                 => routes.JourneyRecoveryController.onPageLoad()
    }

  private def navigatorFromPartnershipNominatedPartnerUtrYesNoPage(mode: Mode)(userAnswers: UserAnswers): Call =
    (userAnswers.get(PartnershipNominatedPartnerUtrYesNoPage), mode) match {
      case (Some(true), _)           =>
        controllers.add.partnership.routes.PartnershipNominatedPartnerUtrYesNoController.onPageLoad(mode)
      case (Some(false), NormalMode) =>
        controllers.add.partnership.routes.PartnershipNominatedPartnerUtrYesNoController.onPageLoad(NormalMode)
      case (Some(false), CheckMode)  =>
        controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad()
      case (None, _)                 => routes.JourneyRecoveryController.onPageLoad()
    }
  private def navigatorFromCompanyUtrYesNoPage(mode: Mode)(userAnswers: UserAnswers): Call                     =
    (userAnswers.get(CompanyUtrYesNoPage), mode) match {
      case (Some(true), _)           =>
        controllers.add.company.routes.CompanyUtrController.onPageLoad(mode)
      case (Some(false), NormalMode) =>
        controllers.add.company.routes.CompanyCrnYesNoController.onPageLoad(NormalMode)
      case (Some(false), CheckMode)  => controllers.add.routes.CheckYourAnswersController.onPageLoad()
      case (None, _)                 => routes.JourneyRecoveryController.onPageLoad()
    }

  private def navigatorFromPartnershipNominatedPartnerCrnYesNoPage(mode: Mode)(userAnswers: UserAnswers): Call =
    (userAnswers.get(PartnershipNominatedPartnerCrnYesNoPage), mode) match {
      case (Some(true), _)           =>
        controllers.add.partnership.routes.PartnershipNominatedPartnerCrnYesNoController.onPageLoad(mode)
      case (Some(false), NormalMode) =>
        controllers.add.partnership.routes.PartnershipNominatedPartnerCrnYesNoController.onPageLoad(NormalMode)
      case (Some(false), CheckMode)  =>
        controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad()
      case (None, _)                 => routes.JourneyRecoveryController.onPageLoad()
    }

  private def navigatorFromCompanyWorksReferenceYesNoPage(mode: Mode)(userAnswers: UserAnswers): Call =
    (userAnswers.get(CompanyWorksReferenceYesNoPage), mode) match {
      case (Some(true), _)           =>
        controllers.add.company.routes.CompanyWorksReferenceController.onPageLoad(mode)
      case (Some(false), NormalMode) =>
        controllers.add.company.routes.CompanyCheckYourAnswersController.onPageLoad()
      case (Some(false), CheckMode)  => controllers.add.routes.CheckYourAnswersController.onPageLoad()
      case (None, _)                 => routes.JourneyRecoveryController.onPageLoad()
    }

  private def navigatorFromChooseContactDetailsPage(mode: Mode)(userAnswers: UserAnswers): Call =
    (userAnswers.get(PartnershipChooseContactDetailsPage), mode) match {
      // TODO: EMAIL       - CIS ANSF PTN: Screen AS-P4 (PTN) - What are the contact details for [partnership name]?
      case (Some(Email), _)  =>
        controllers.add.partnership.routes.PartnershipChooseContactDetailsController.onPageLoad(mode)

      // TODO: PHOME       - CIS ANSF PTN: Screen AS-P4 (PTN) - What are the contact details for [partnership name]?
      case (Some(Phone), _)  =>
        controllers.add.partnership.routes.PartnershipChooseContactDetailsController.onPageLoad(mode)
      // TODO: MOBILE      - CIS ANSF PTN: Screen AS-P4 (PTN) - What are the contact details for [partnership name]?
      case (Some(Mobile), _) =>
        controllers.add.partnership.routes.PartnershipChooseContactDetailsController.onPageLoad(mode)
      // TODO: NO DETAILS  - CIS ANSF PTN: Screen AS-P4 (PTN) - What are the contact details for [partnership name]?
      case (Some(_), _)      =>
        controllers.add.partnership.routes.PartnershipChooseContactDetailsController.onPageLoad(mode)
      case (_, CheckMode)    =>
        controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad()
      case _                 => routes.JourneyRecoveryController.onPageLoad()
    }

  private def navigatorFromPartnershipAddressYesNoPage(mode: Mode)(ua: UserAnswers): Call =
    (ua.get(PartnershipAddressYesNoPage), mode) match {
      case (Some(true), NormalMode)  =>
        controllers.add.partnership.routes.PartnershipAddressController.onPageLoad(NormalMode)
      case (Some(false), NormalMode) =>
        controllers.add.partnership.routes.PartnershipChooseContactDetailsController.onPageLoad(NormalMode)
      case (Some(true), CheckMode)   =>
        ua.get(PartnershipAddressPage)
          .fold(controllers.add.partnership.routes.PartnershipAddressController.onPageLoad(CheckMode)) { _ =>
            controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad()
          }
      case (Some(false), CheckMode)  =>
        controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad()
      case _                         => routes.JourneyRecoveryController.onPageLoad()
    }

  private def navigatorFromPartnershipNominatedPartnerNinoYesNoPage(mode: Mode)(ua: UserAnswers): Call =
    (ua.get(PartnershipNominatedPartnerNinoYesNoPage), mode) match {
      case (Some(true), _)           =>
        controllers.add.partnership.routes.PartnershipNominatedPartnerNinoController.onPageLoad(mode)
      case (Some(false), NormalMode) =>
        controllers.add.partnership.routes.PartnershipNominatedPartnerCrnYesNoController.onPageLoad(NormalMode)
      case (Some(false), CheckMode)  =>
        controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad()
      case (None, _)                 => routes.JourneyRecoveryController.onPageLoad()
    }

  private def navigatorFromCompanyContactOptionsPage(mode: Mode)(userAnswers: UserAnswers): Call =
    (userAnswers.get(CompanyContactOptionsPage), mode) match {
      case (Some(Email), _)  =>
        controllers.add.company.routes.CompanyContactOptionsController.onPageLoad(mode)
      case (Some(Phone), _)  =>
        controllers.add.company.routes.CompanyContactOptionsController.onPageLoad(mode)
      case (Some(Mobile), _) =>
        controllers.add.company.routes.CompanyContactOptionsController.onPageLoad(mode)
      case (Some(_), _)      =>
        controllers.add.company.routes.CompanyContactOptionsController.onPageLoad(mode)
      case (_, CheckMode)    => controllers.add.routes.CheckYourAnswersController.onPageLoad()
      case _                 => routes.JourneyRecoveryController.onPageLoad()
    }

  private def navigatorFromCompanyCrnYesNoPage(mode: Mode)(userAnswers: UserAnswers): Call =
    (userAnswers.get(CompanyCrnYesNoPage), mode) match {
      case (Some(true), _)           =>
        controllers.add.company.routes.CompanyCrnYesNoController.onPageLoad(mode)
      case (Some(false), NormalMode) =>
        controllers.add.company.routes.CompanyCrnYesNoController.onPageLoad(NormalMode)
      case (Some(false), CheckMode)  => controllers.add.routes.CheckYourAnswersController.onPageLoad()
      case (None, _)                 => routes.JourneyRecoveryController.onPageLoad()
    }

}
