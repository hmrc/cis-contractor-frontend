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
import pages.add.*
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
    case PartnershipNamePage                       =>
      _ => controllers.add.partnership.routes.PartnershipHasUtrYesNoController.onPageLoad(NormalMode)
    case PartnershipHasUtrYesNoPage                => userAnswers => navigatorFromPartnershipHasUtrYesNoPage(NormalMode)(userAnswers)
    case PartnershipUniqueTaxpayerReferencePage    =>
      _ => controllers.add.partnership.routes.PartnershipWorksReferenceNumberYesNoController.onPageLoad(NormalMode)
    case PartnershipWorksReferenceNumberYesNoPage  =>
      userAnswers => navigatorFromPartnershipWorksReferenceNumberYesNoPage(NormalMode)(userAnswers)
    case PartnershipWorksReferenceNumberPage       =>
      _ => controllers.add.partnership.routes.PartnershipAddressYesNoController.onPageLoad(NormalMode)
    case PartnershipAddressYesNoPage               =>
      _ => controllers.add.partnership.routes.PartnershipAddressYesNoController.onPageLoad(NormalMode)
    case PartnershipContactDetailsYesNoPage        =>
      userAnswers => navigatorFromPartnershipContactDetailsYesNoPage(NormalMode)(userAnswers)
    case PartnershipNominatedPartnerCrnPage        => _ => controllers.add.routes.CheckYourAnswersController.onPageLoad()
    case PartnershipNominatedPartnerNinoYesNoPage  =>
      _ => controllers.add.partnership.routes.PartnershipNominatedPartnerNinoYesNoController.onPageLoad(NormalMode)
    case PartnershipNominatedPartnerNamePage       =>
      _ => controllers.add.partnership.routes.PartnershipNominatedPartnerNameController.onPageLoad(NormalMode)
    case PartnershipNominatedPartnerCrnYesNoPage   =>
      userAnswers => navigatorFromPartnershipNominatedPartnerCrnYesNoPage(NormalMode)(userAnswers)
    case _                                         => _ => routes.IndexController.onPageLoad()
  }

  private val checkRouteMap: Page => UserAnswers => Call = {
    case TypeOfSubcontractorPage                  => userAnswers => navigatorFromTypeOfSubcontractorPage(CheckMode)(userAnswers)
    case SubTradingNameYesNoPage                  => userAnswers => navigatorFromSubTradingNameYesNoPage(CheckMode)(userAnswers)
    case SubAddressYesNoPage                      => userAnswers => navigatorFromSubAddressYesNoPage(CheckMode)(userAnswers)
    case AddressOfSubcontractorPage               => _ => controllers.add.routes.CheckYourAnswersController.onPageLoad()
    case NationalInsuranceNumberYesNoPage         =>
      userAnswers => navigatorFromNationalInsuranceNumberYesNoPage(CheckMode)(userAnswers)
    case UniqueTaxpayerReferenceYesNoPage         =>
      userAnswers => navigatorFromUniqueTaxpayerReferenceYesNoPage(CheckMode)(userAnswers)
    case WorksReferenceNumberYesNoPage            =>
      userAnswers => navigatorFromWorksReferenceNumberYesNoPage(CheckMode)(userAnswers)
    case SubContactDetailsPage                    => _ => controllers.add.routes.CheckYourAnswersController.onPageLoad()
    case SubcontractorContactDetailsYesNoPage     =>
      userAnswers => navigatorFromSubcontractorContactDetailsYesNoPage(CheckMode)(userAnswers)
    case PartnershipNamePage                      => _ => controllers.add.routes.CheckYourAnswersController.onPageLoad()
    case PartnershipHasUtrYesNoPage               => userAnswers => navigatorFromPartnershipHasUtrYesNoPage(CheckMode)(userAnswers)
    case PartnershipUniqueTaxpayerReferencePage   => _ => controllers.add.routes.CheckYourAnswersController.onPageLoad()
    case PartnershipWorksReferenceNumberYesNoPage =>
      userAnswers => navigatorFromPartnershipWorksReferenceNumberYesNoPage(CheckMode)(userAnswers)
    case PartnershipWorksReferenceNumberPage      => _ => controllers.add.routes.CheckYourAnswersController.onPageLoad()
    case PartnershipAddressYesNoPage              =>
      _ => controllers.add.partnership.routes.PartnershipAddressYesNoController.onPageLoad(CheckMode)
    case PartnershipContactDetailsYesNoPage       =>
      userAnswers => navigatorFromPartnershipContactDetailsYesNoPage(CheckMode)(userAnswers)
    case PartnershipNominatedPartnerNinoYesNoPage =>
      _ => controllers.add.partnership.routes.PartnershipNominatedPartnerNinoYesNoController.onPageLoad(CheckMode)
    case PartnershipNominatedPartnerNamePage      => _ => controllers.add.routes.CheckYourAnswersController.onPageLoad()
    case PartnershipNominatedPartnerCrnPage       => _ => controllers.add.routes.CheckYourAnswersController.onPageLoad()
    case PartnershipNominatedPartnerCrnYesNoPage  =>
      userAnswers => navigatorFromPartnershipNominatedPartnerCrnYesNoPage(CheckMode)(userAnswers)
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
        controllers.add.routes.CheckYourAnswersController.onPageLoad()

      case _ =>
        routes.JourneyRecoveryController.onPageLoad()
    }

  private def navigatorFromPartnershipWorksReferenceNumberYesNoPage(mode: Mode)(userAnswers: UserAnswers): Call =
    (userAnswers.get(PartnershipWorksReferenceNumberYesNoPage), mode) match {
      case (Some(true), _)           =>
        controllers.add.partnership.routes.PartnershipWorksReferenceNumberController.onPageLoad(mode)
      case (Some(false), NormalMode) =>
        controllers.add.partnership.routes.PartnershipAddressYesNoController.onPageLoad(NormalMode)
      case (Some(false), CheckMode)  => controllers.add.routes.CheckYourAnswersController.onPageLoad()
      case (None, _)                 => routes.JourneyRecoveryController.onPageLoad()
    }

  private def navigatorFromPartnershipContactDetailsYesNoPage(mode: Mode)(userAnswers: UserAnswers): Call =
    (userAnswers.get(PartnershipContactDetailsYesNoPage), mode) match {
      case (Some(true), _)           =>
        routes.JourneyRecoveryController.onPageLoad() // TODO: SL0201 - B (PTN) - Contact details for partner Controller
      case (Some(false), NormalMode) =>
        routes.JourneyRecoveryController.onPageLoad() // TODO: SL0205 - B (PTN) - Nominated partner name controller
      case (Some(false), CheckMode)  => routes.JourneyRecoveryController.onPageLoad() // TODO: Partnership CYA controller
      case (None, _)                 => routes.JourneyRecoveryController.onPageLoad()
    }

  private def navigatorFromPartnershipNominatedPartnerCrnYesNoPage(mode: Mode)(userAnswers: UserAnswers): Call =
    (userAnswers.get(PartnershipNominatedPartnerCrnYesNoPage), mode) match {
      case (Some(true), _)           =>
        controllers.add.partnership.routes.PartnershipNominatedPartnerCrnYesNoController.onPageLoad(mode)
      case (Some(false), NormalMode) =>
        controllers.add.partnership.routes.PartnershipNominatedPartnerCrnYesNoController.onPageLoad(NormalMode)
      case (Some(false), CheckMode)  => controllers.add.routes.CheckYourAnswersController.onPageLoad()
      case (None, _)                 => routes.JourneyRecoveryController.onPageLoad()
    }
}
