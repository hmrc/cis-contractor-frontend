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

@Singleton
class Navigator @Inject() () {

  private val normalRoutes: Page => UserAnswers => Call = {
    case TypeOfSubcontractorPage                      => userAnswers => navigatorFromTypeOfSubcontractorPage(NormalMode)(userAnswers)
    case SubTradingNameYesNoPage                      => userAnswers => navigatorFromSubTradingNameYesNoPage(NormalMode)(userAnswers)
    case TradingNameOfSubcontractorPage               => _ => controllers.add.routes.SubAddressYesNoController.onPageLoad(NormalMode)
    case SubcontractorNamePage                        =>  _ => controllers.add.routes.SubAddressYesNoController.onPageLoad(NormalMode)
    case SubAddressYesNoPage                          => userAnswers => navigatorFromSubAddressYesNoPage(NormalMode)(userAnswers)
    case AddressOfSubcontractorPage                   => _ => controllers.add.routes.NationalInsuranceNumberYesNoController.onPageLoad(NormalMode)
    case NationalInsuranceNumberYesNoPage             => userAnswers => navigatorFromNationalInsuranceNumberYesNoPage(NormalMode)(userAnswers)
    case SubNationalInsuranceNumberPage               => _ => controllers.add.routes.UniqueTaxpayerReferenceYesNoController.onPageLoad(NormalMode)
    case UniqueTaxpayerReferenceYesNoPage             => userAnswers => navigatorFromUniqueTaxpayerReferenceYesNoPage(NormalMode)(userAnswers)
    case SubcontractorsUniqueTaxpayerReferencePage    => _ => controllers.add.routes.WorksReferenceNumberYesNoController.onPageLoad(NormalMode)
    case WorksReferenceNumberYesNoPage                => userAnswers => navigatorFromWorksReferenceNumberYesNoPage(NormalMode)(userAnswers)
    case WorksReferenceNumberPage                     => _ => controllers.add.routes.SubcontractorContactDetailsYesNoController.onPageLoad(NormalMode)
    case SubcontractorContactDetailsYesNoPage         => userAnswers => navigatorFromSubcontractorContactDetailsYesNoPage(NormalMode)(userAnswers)
    case SubContactDetailsPage                        => _ => routes.CheckYourAnswersController.onPageLoad()
    case _                                            => _ => routes.IndexController.onPageLoad()
  }

  private val checkRouteMap: Page => UserAnswers => Call = {
    case TypeOfSubcontractorPage => userAnswers => navigatorFromTypeOfSubcontractorPage(CheckMode)(userAnswers)
    case SubTradingNameYesNoPage => userAnswers => navigatorFromSubTradingNameYesNoPage(CheckMode)(userAnswers)
    case SubAddressYesNoPage     => userAnswers => navigatorFromSubAddressYesNoPage(CheckMode)(userAnswers)
    case AddressOfSubcontractorPage => _ => routes.CheckYourAnswersController.onPageLoad()
    case NationalInsuranceNumberYesNoPage => userAnswers => navigatorFromNationalInsuranceNumberYesNoPage(CheckMode)(userAnswers)
    case UniqueTaxpayerReferenceYesNoPage => userAnswers => navigatorFromUniqueTaxpayerReferenceYesNoPage(CheckMode)(userAnswers)
    case WorksReferenceNumberPage => userAnswers => controllers.add.routes.WorksReferenceNumberController.onPageLoad(CheckMode)
    case WorksReferenceNumberYesNoPage     => userAnswers => navigatorFromWorksReferenceNumberYesNoPage(CheckMode)(userAnswers)
    case SubContactDetailsPage          => _ => routes.CheckYourAnswersController.onPageLoad()
    case SubcontractorsUniqueTaxpayerReferencePage =>
      userAnswers => controllers.add.routes.SubcontractorsUniqueTaxpayerReferenceController.onPageLoad(CheckMode)
    case SubcontractorContactDetailsYesNoPage => userAnswers => navigatorFromSubcontractorContactDetailsYesNoPage(CheckMode)(userAnswers)
    case _                       => _ => routes.CheckYourAnswersController.onPageLoad()
  }

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = mode match {
    case NormalMode =>
      normalRoutes(page)(userAnswers)
    case CheckMode  =>
      checkRouteMap(page)(userAnswers)
  }

  private def navigatorFromTypeOfSubcontractorPage(mode: Mode)(userAnswers: UserAnswers): Call =
    (userAnswers.get(TypeOfSubcontractorPage), mode) match {
      case (Some(Individualorsoletrader), NormalMode) => controllers.add.routes.SubTradingNameYesNoController.onPageLoad(NormalMode)
      case (None, _) => routes.JourneyRecoveryController.onPageLoad()
      case (_, NormalMode) => controllers.add.routes.TradingNameOfSubcontractorController.onPageLoad(mode)
      case (_, CheckMode) => routes.CheckYourAnswersController.onPageLoad()
    }

  private def navigatorFromSubTradingNameYesNoPage(mode: Mode)(userAnswers: UserAnswers): Call =
    (userAnswers.get(SubTradingNameYesNoPage), mode) match {
      case (Some(true), _)           => controllers.add.routes.TradingNameOfSubcontractorController.onPageLoad(mode)
      case (Some(false), NormalMode) => controllers.add.routes.SubcontractorNameController.onPageLoad(NormalMode)
      case (Some(false), CheckMode)  => routes.CheckYourAnswersController.onPageLoad()
      case (None, _)                 => routes.JourneyRecoveryController.onPageLoad()
    }

  private def navigatorFromSubAddressYesNoPage(mode: Mode)(userAnswers: UserAnswers): Call =
    (userAnswers.get(SubAddressYesNoPage), mode) match {
      case (Some(true), _)           => controllers.add.routes.AddressOfSubcontractorController.onPageLoad(mode)
      case (Some(false), NormalMode) => controllers.add.routes.NationalInsuranceNumberYesNoController.onPageLoad(NormalMode)
      case (Some(false), CheckMode)  => routes.CheckYourAnswersController.onPageLoad()
      case (None, _)                 => routes.JourneyRecoveryController.onPageLoad()
    }

  private def navigatorFromNationalInsuranceNumberYesNoPage(mode: Mode)(userAnswers: UserAnswers): Call =
    (userAnswers.get(NationalInsuranceNumberYesNoPage), mode) match {
      case (Some(true), _)           => controllers.add.routes.SubNationalInsuranceNumberController.onPageLoad(mode)
      case (Some(false), NormalMode) => controllers.add.routes.UniqueTaxpayerReferenceYesNoController.onPageLoad(NormalMode)
      case (Some(false), CheckMode)  => routes.CheckYourAnswersController.onPageLoad()
      case (None, _)                 => routes.JourneyRecoveryController.onPageLoad()
    }

  private def navigatorFromUniqueTaxpayerReferenceYesNoPage(mode: Mode)(userAnswers: UserAnswers): Call =
    (userAnswers.get(UniqueTaxpayerReferenceYesNoPage), mode) match {
      case (Some(true), _) => controllers.add.routes.SubcontractorsUniqueTaxpayerReferenceController.onPageLoad(mode)
      case (Some(false), NormalMode) => controllers.add.routes.WorksReferenceNumberYesNoController.onPageLoad(NormalMode)
      case (Some(false), CheckMode) => routes.CheckYourAnswersController.onPageLoad()
      case (None, _) => routes.JourneyRecoveryController.onPageLoad()
    }

  private def navigatorFromWorksReferenceNumberYesNoPage(mode: Mode)(userAnswers: UserAnswers): Call =
    (userAnswers.get(WorksReferenceNumberYesNoPage), mode) match {
      case (Some(true), _) => controllers.add.routes.WorksReferenceNumberController.onPageLoad(mode)
      case (Some(false), NormalMode) => controllers.add.routes.SubcontractorContactDetailsYesNoController.onPageLoad(NormalMode)
      case (Some(false), CheckMode) => routes.CheckYourAnswersController.onPageLoad()
      case (None, _) => routes.JourneyRecoveryController.onPageLoad()
    }

  private def navigatorFromSubcontractorContactDetailsYesNoPage(mode: Mode)(userAnswers: UserAnswers): Call =
    (userAnswers.get(SubcontractorContactDetailsYesNoPage), mode) match {
      case (Some(true), _)           => controllers.add.routes.SubContactDetailsController.onPageLoad(mode)
      case (Some(false), NormalMode) => routes.CheckYourAnswersController.onPageLoad()
      case (Some(false), CheckMode)  => routes.CheckYourAnswersController.onPageLoad()
      case (None, _)                 => routes.JourneyRecoveryController.onPageLoad()
    }
}
