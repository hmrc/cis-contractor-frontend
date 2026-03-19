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
import pages.add.partnership.*
import pages.add.company.*

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
      _ => controllers.add.routes.CheckYourAnswersController.onPageLoad()
    case SubContactDetailsPage                     => _ => controllers.add.routes.CheckYourAnswersController.onPageLoad()
    case PartnershipNamePage                       =>
      _ => controllers.add.partnership.routes.PartnershipAddressYesNoController.onPageLoad(NormalMode)
    case PartnershipAddressYesNoPage               =>
      userAnswers => navigatorFromPartnershipAddressYesNoPage(NormalMode)(userAnswers)
    case PartnershipAddressPage                    =>
      _ => controllers.add.partnership.routes.PartnershipChooseContactDetailsController.onPageLoad(NormalMode)
    case PartnershipChooseContactDetailsPage       =>
      userAnswers => navigatorFromChooseContactDetailsPage(NormalMode)(userAnswers)
    case PartnershipEmailAddressPage               =>
      _ => controllers.add.partnership.routes.PartnershipHasUtrYesNoController.onPageLoad(NormalMode)
    case PartnershipMobileNumberPage               =>
      _ => controllers.add.partnership.routes.PartnershipHasUtrYesNoController.onPageLoad(NormalMode)
    case PartnershipPhoneNumberPage                =>
      _ => controllers.add.partnership.routes.PartnershipHasUtrYesNoController.onPageLoad(NormalMode)
    case PartnershipHasUtrYesNoPage                => userAnswers => navigatorFromPartnershipHasUtrYesNoPage(NormalMode)(userAnswers)
    case PartnershipUniqueTaxpayerReferencePage    =>
      _ => controllers.add.partnership.routes.PartnershipNominatedPartnerNameController.onPageLoad(NormalMode)
    case PartnershipNominatedPartnerNamePage       =>
      _ => controllers.add.partnership.routes.PartnershipNominatedPartnerUtrYesNoController.onPageLoad(NormalMode)
    case PartnershipWorksReferenceNumberYesNoPage  =>
      userAnswers => navigatorFromPartnershipWorksReferenceNumberYesNoPage(NormalMode)(userAnswers)
    case PartnershipNominatedPartnerNinoPage       =>
      _ => controllers.add.partnership.routes.PartnershipNominatedPartnerCrnYesNoController.onPageLoad(NormalMode)
    case PartnershipWorksReferenceNumberPage       =>
      _ => controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad()
    case PartnershipNominatedPartnerCrnPage        =>
      _ => controllers.add.partnership.routes.PartnershipWorksReferenceNumberYesNoController.onPageLoad(NormalMode)
    case PartnershipNominatedPartnerNinoYesNoPage  =>
      userAnswers => navigatorFromPartnershipNominatedPartnerNinoYesNoPage(NormalMode)(userAnswers)
    case PartnershipNominatedPartnerCrnYesNoPage   =>
      userAnswers => navigatorFromPartnershipNominatedPartnerCrnYesNoPage(NormalMode)(userAnswers)
    case PartnershipNominatedPartnerUtrPage        =>
      _ => controllers.add.partnership.routes.PartnershipNominatedPartnerNinoYesNoController.onPageLoad(NormalMode)
    case PartnershipNominatedPartnerUtrYesNoPage   =>
      userAnswers => navigatorFromPartnershipNominatedPartnerUtrYesNoPage(NormalMode)(userAnswers)
    case CompanyNamePage                           => _ => controllers.add.company.routes.CompanyNameController.onPageLoad(NormalMode)
    case CompanyAddressPage                        =>
      _ => controllers.add.company.routes.CompanyAddressController.onPageLoad(NormalMode)
    case CompanyAddressYesNoPage                   =>
      _ => controllers.add.company.routes.CompanyAddressYesNoController.onPageLoad(NormalMode)
    case CompanyContactOptionsPage                 =>
      userAnswers => navigatorFromCompanyContactOptionsPage(NormalMode)(userAnswers)
    case CompanyEmailAddressPage                   =>
      _ => controllers.add.company.routes.CompanyEmailAddressController.onPageLoad(NormalMode)
    case CompanyMobileNumberPage                   =>
      _ => controllers.add.company.routes.CompanyMobileNumberController.onPageLoad(NormalMode)
    case CompanyUtrYesNoPage                       =>
      userAnswers => navigatorFromCompanyUtrYesNoPage(NormalMode)(userAnswers)
    case CompanyPhoneNumberPage                    =>
      _ => controllers.add.company.routes.CompanyPhoneNumberController.onPageLoad(NormalMode)
    case CompanyWorksReferenceYesNoPage            =>
      userAnswers => navigatorFromCompanyWorksReferenceYesNoPage(NormalMode)(userAnswers)
    case CompanyCrnPage                            =>
      _ => controllers.add.company.routes.CompanyWorksReferenceYesNoController.onPageLoad(NormalMode)
    case CompanyUtrPage                            =>
      _ => controllers.add.company.routes.CompanyCrnYesNoController.onPageLoad(NormalMode)
    case CompanyCrnYesNoPage                       =>
      userAnswers => navigatorFromCompanyCrnYesNoPage(NormalMode)(userAnswers)
    case IndividualMobileNumberPage                =>
      _ => controllers.add.routes.UniqueTaxpayerReferenceYesNoController.onPageLoad(NormalMode)
    case IndividualPhoneNumberPage                 =>
      _ => controllers.add.routes.UniqueTaxpayerReferenceYesNoController.onPageLoad(NormalMode)
    case IndividualChooseContactDetailsPage        =>
      userAnswers => navigatorFromIndividualChooseContactDetailsPage(NormalMode)(userAnswers)
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
    case PartnershipHasUtrYesNoPage               => navigatorFromPartnershipHasUtrYesNoPage(CheckMode)(_)
    case PartnershipWorksReferenceNumberYesNoPage => navigatorFromPartnershipWorksReferenceNumberYesNoPage(CheckMode)(_)
    case PartnershipChooseContactDetailsPage      => navigatorFromChooseContactDetailsPage(CheckMode)(_)
    case PartnershipNominatedPartnerCrnYesNoPage  => navigatorFromPartnershipNominatedPartnerCrnYesNoPage(CheckMode)(_)
    case PartnershipNamePage                      =>
      _ => controllers.add.partnership.routes.PartnershipAddressYesNoController.onPageLoad(CheckMode)
    case PartnershipAddressYesNoPage              =>
      userAnswers => navigatorFromPartnershipAddressYesNoPage(CheckMode)(userAnswers)
    case PartnershipAddressPage                   =>
      _ => controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad()
    case PartnershipMobileNumberPage              =>
      _ => controllers.add.partnership.routes.PartnershipHasUtrYesNoController.onPageLoad(CheckMode)
    case PartnershipPhoneNumberPage               =>
      _ => controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad()
    case PartnershipNominatedPartnerNinoYesNoPage =>
      userAnswers => navigatorFromPartnershipNominatedPartnerNinoYesNoPage(CheckMode)(userAnswers)
    case PartnershipNominatedPartnerNinoPage      => navigatorFromPartnershipNominatedPartnerNinoPage(CheckMode)(_)
    case PartnershipNominatedPartnerNamePage      =>
      _ => controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad()
    case PartnershipEmailAddressPage              =>
      _ => controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad()
    case PartnershipNominatedPartnerUtrYesNoPage  =>
      userAnswers => navigatorFromPartnershipNominatedPartnerUtrYesNoPage(CheckMode)(userAnswers)
    case PartnershipNominatedPartnerUtrPage       =>
      _ => controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad()
    case PartnershipNominatedPartnerCrnPage       => navigatorFromPartnershipNominatedPartnerCrnPage(CheckMode)(_)
    case PartnershipWorksReferenceNumberPage      =>
      _ => controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad()
    case PartnershipUniqueTaxpayerReferencePage   =>
      _ => controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad()
    case CompanyAddressYesNoPage                  =>
      _ => controllers.add.company.routes.CompanyAddressYesNoController.onPageLoad(CheckMode)
    case CompanyContactOptionsPage                => navigatorFromCompanyContactOptionsPage(CheckMode)(_)
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
    case CompanyCrnYesNoPage                      => navigatorFromCompanyCrnYesNoPage(CheckMode)(_)
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
    case IndividualChooseContactDetailsPage       =>
      navigatorFromIndividualChooseContactDetailsPage(CheckMode)(_)
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
        controllers.add.routes.CheckYourAnswersController.onPageLoad()
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

  private def navigatorFromPartnershipNominatedPartnerNinoPage(mode: Mode)(ua: UserAnswers): Call =
    ua.get(PartnershipNominatedPartnerNinoPage) match {
      case Some(_) =>
        controllers.add.partnership.routes.PartnershipNominatedPartnerCrnYesNoController.onPageLoad(mode)
      case None    =>
        routes.JourneyRecoveryController.onPageLoad()
    }

  private def navigatorFromPartnershipNominatedPartnerCrnPage(mode: Mode)(ua: UserAnswers): Call =
    (ua.get(PartnershipNominatedPartnerCrnPage), mode) match {
      case (Some(_), NormalMode) =>
        controllers.add.partnership.routes.PartnershipWorksReferenceNumberYesNoController.onPageLoad(NormalMode)

      case (Some(_), CheckMode) =>
        controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad()

      case (None, _) =>
        routes.JourneyRecoveryController.onPageLoad()
    }

  private def navigatorFromPartnershipHasUtrYesNoPage(mode: Mode)(ua: UserAnswers): Call =
    (ua.get(PartnershipHasUtrYesNoPage), mode) match {
      case (Some(true), _) =>
        controllers.add.partnership.routes.PartnershipUniqueTaxpayerReferenceController.onPageLoad(mode)

      case (Some(false), NormalMode) =>
        controllers.add.partnership.routes.PartnershipNominatedPartnerNameController.onPageLoad(NormalMode)

      case (Some(false), CheckMode) =>
        controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad()

      case _ =>
        routes.JourneyRecoveryController.onPageLoad()
    }

  private def navigatorFromPartnershipWorksReferenceNumberYesNoPage(mode: Mode)(userAnswers: UserAnswers): Call =
    (userAnswers.get(PartnershipWorksReferenceNumberYesNoPage), mode) match {
      case (Some(true), _)  =>
        controllers.add.partnership.routes.PartnershipWorksReferenceNumberController.onPageLoad(mode)
      case (Some(false), _) =>
        controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad()
      case (None, _)        => routes.JourneyRecoveryController.onPageLoad()
    }

  private def navigatorFromPartnershipNominatedPartnerUtrYesNoPage(mode: Mode)(userAnswers: UserAnswers): Call =
    (userAnswers.get(PartnershipNominatedPartnerUtrYesNoPage), mode) match {
      case (Some(true), NormalMode) =>
        controllers.add.partnership.routes.PartnershipNominatedPartnerUtrController.onPageLoad(NormalMode)

      case (Some(false), NormalMode) =>
        controllers.add.partnership.routes.PartnershipNominatedPartnerNinoYesNoController.onPageLoad(NormalMode)

      case (Some(true), CheckMode) =>
        userAnswers
          .get(PartnershipNominatedPartnerUtrPage)
          .fold(
            controllers.add.partnership.routes.PartnershipNominatedPartnerUtrController.onPageLoad(CheckMode)
          ) { _ =>
            controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad()
          }

      case (Some(false), CheckMode) =>
        controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad()

      case (None, _) =>
        routes.JourneyRecoveryController.onPageLoad()
    }

  private def navigatorFromPartnershipNominatedPartnerCrnYesNoPage(mode: Mode)(userAnswers: UserAnswers): Call =
    (userAnswers.get(PartnershipNominatedPartnerCrnYesNoPage), mode) match {
      case (Some(true), NormalMode) =>
        controllers.add.partnership.routes.PartnershipNominatedPartnerCrnController.onPageLoad(NormalMode)

      case (Some(false), NormalMode) =>
        controllers.add.partnership.routes.PartnershipWorksReferenceNumberYesNoController.onPageLoad(NormalMode)

      case (Some(true), CheckMode) =>
        userAnswers
          .get(PartnershipNominatedPartnerCrnPage)
          .fold(
            controllers.add.partnership.routes.PartnershipNominatedPartnerCrnController.onPageLoad(CheckMode)
          ) { _ =>
            controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad()
          }

      case (Some(false), CheckMode) =>
        controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad()

      case (None, _) =>
        routes.JourneyRecoveryController.onPageLoad()
    }

  private def navigatorFromPartnershipAddressYesNoPage(mode: Mode)(userAnswers: UserAnswers): Call =
    (userAnswers.get(PartnershipAddressYesNoPage), mode) match {
      case (Some(true), NormalMode) =>
        controllers.add.partnership.routes.PartnershipAddressController.onPageLoad(NormalMode)

      case (Some(false), NormalMode) =>
        controllers.add.partnership.routes.PartnershipChooseContactDetailsController.onPageLoad(NormalMode)

      case (Some(true), CheckMode) =>
        userAnswers
          .get(PartnershipAddressPage)
          .fold(
            controllers.add.partnership.routes.PartnershipAddressController.onPageLoad(CheckMode)
          ) { _ =>
            controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad()
          }

      case (Some(false), CheckMode) =>
        controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad()

      case (None, _) =>
        routes.JourneyRecoveryController.onPageLoad()
    }

  private def navigatorFromPartnershipNominatedPartnerNinoYesNoPage(mode: Mode)(userAnswers: UserAnswers): Call =
    (userAnswers.get(PartnershipNominatedPartnerNinoYesNoPage), mode) match {
      case (Some(true), NormalMode) =>
        controllers.add.partnership.routes.PartnershipNominatedPartnerNinoController.onPageLoad(NormalMode)

      case (Some(false), NormalMode) =>
        controllers.add.partnership.routes.PartnershipNominatedPartnerCrnYesNoController.onPageLoad(NormalMode)

      case (Some(true), CheckMode) =>
        userAnswers
          .get(PartnershipNominatedPartnerNinoPage)
          .fold(
            controllers.add.partnership.routes.PartnershipNominatedPartnerNinoController.onPageLoad(CheckMode)
          ) { _ =>
            controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad()
          }

      case (Some(false), CheckMode) =>
        controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad()

      case (None, _) =>
        routes.JourneyRecoveryController.onPageLoad()
    }

  private def navigatorFromChooseContactDetailsPage(mode: Mode)(userAnswers: UserAnswers): Call =
    (userAnswers.get(PartnershipChooseContactDetailsPage), mode) match {
      // TODO: EMAIL       - CIS ANSF PTN: Screen AS-P4 (PTN) - What are the contact details for [partnership name]?
      case (Some(Email), _)  =>
        controllers.add.partnership.routes.PartnershipEmailAddressController.onPageLoad(mode)

      // TODO: PHONE       - CIS ANSF PTN: Screen AS-P4 (PTN) - What are the contact details for [partnership name]?
      case (Some(Phone), _)  =>
        controllers.add.partnership.routes.PartnershipPhoneNumberController.onPageLoad(mode)
      // TODO: MOBILE      - CIS ANSF PTN: Screen AS-P4 (PTN) - What are the contact details for [partnership name]?
      case (Some(Mobile), _) =>
        controllers.add.partnership.routes.PartnershipMobileNumberController.onPageLoad(mode)
      // TODO: NO DETAILS  - CIS ANSF PTN: Screen AS-P4 (PTN) - What are the contact details for [partnership name]?
      case (Some(_), _)      =>
        controllers.add.partnership.routes.PartnershipHasUtrYesNoController.onPageLoad(mode)
      case (_, CheckMode)    => controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad()
      case _                 => routes.JourneyRecoveryController.onPageLoad()
    }

  private def navigatorFromCompanyUtrYesNoPage(mode: Mode)(userAnswers: UserAnswers): Call =
    (userAnswers.get(CompanyUtrYesNoPage), mode) match {
      case (Some(true), _)           =>
        controllers.add.company.routes.CompanyUtrController.onPageLoad(mode)
      case (Some(false), NormalMode) =>
        controllers.add.company.routes.CompanyCrnYesNoController.onPageLoad(NormalMode)
      case (Some(false), CheckMode)  => controllers.add.routes.CheckYourAnswersController.onPageLoad()
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

  private def navigatorFromIndividualChooseContactDetailsPage(mode: Mode)(userAnswers: UserAnswers): Call =
    (userAnswers.get(IndividualChooseContactDetailsPage), mode) match {
      case (Some(Email), _)  =>
        controllers.add.routes.IndividualEmailAddressController.onPageLoad(mode)
      case (Some(Phone), _)  =>
        controllers.add.routes.IndividualPhoneNumberController.onPageLoad(mode)
      case (Some(Mobile), _) =>
        controllers.add.routes.IndividualMobileNumberController.onPageLoad(mode)
      case (Some(_), _)      =>
        controllers.add.routes.CheckYourAnswersController.onPageLoad()
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
