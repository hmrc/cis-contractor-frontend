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

package navigation.add

import javax.inject.{Inject, Singleton}
import navigation.NavigatorForJourney
import controllers.routes
import models.contact.ContactOptions.{Email, Mobile, Phone}
import models.{CheckMode, Mode, NormalMode, UserAnswers}
import pages.Page
import pages.add.*
import play.api.mvc.Call

@Singleton
class IndividualNavigator @Inject() () extends NavigatorForJourney {

  override def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = mode match {
    case NormalMode =>
      normalRoutes(page)(userAnswers)
    case CheckMode  =>
      checkRouteMap(page)(userAnswers)
  }

  private val normalRoutes: Page => UserAnswers => Call = {
    case SubTradingNameYesNoPage                   => userAnswers => navigatorFromSubTradingNameYesNoPage(NormalMode)(userAnswers)
    case TradingNameOfSubcontractorPage            => _ => controllers.add.routes.SubAddressYesNoController.onPageLoad(NormalMode)
    case SubcontractorNamePage                     => _ => controllers.add.routes.SubAddressYesNoController.onPageLoad(NormalMode)
    case SubAddressYesNoPage                       => userAnswers => navigatorFromSubAddressYesNoPage(NormalMode)(userAnswers)
    case AddressOfSubcontractorPage                =>
      _ => controllers.add.routes.UniqueTaxpayerReferenceYesNoController.onPageLoad(NormalMode)
    case UniqueTaxpayerReferenceYesNoPage          =>
      userAnswers => navigatorFromUniqueTaxpayerReferenceYesNoPage(NormalMode)(userAnswers)
    case SubcontractorsUniqueTaxpayerReferencePage =>
      _ => controllers.add.routes.NationalInsuranceNumberYesNoController.onPageLoad(NormalMode)
    case NationalInsuranceNumberYesNoPage          =>
      userAnswers => navigatorFromNationalInsuranceNumberYesNoPage(NormalMode)(userAnswers)
    case SubNationalInsuranceNumberPage            =>
      _ => controllers.add.routes.WorksReferenceNumberYesNoController.onPageLoad(NormalMode)
    case WorksReferenceNumberYesNoPage             =>
      userAnswers => navigatorFromWorksReferenceNumberYesNoPage(NormalMode)(userAnswers)
    case WorksReferenceNumberPage                  =>
      _ => controllers.add.routes.CheckYourAnswersController.onPageLoad()
    case IndividualMobileNumberPage                =>
      _ => controllers.add.routes.UniqueTaxpayerReferenceYesNoController.onPageLoad(NormalMode)
    case IndividualPhoneNumberPage                 =>
      _ => controllers.add.routes.UniqueTaxpayerReferenceYesNoController.onPageLoad(NormalMode)
    case IndividualChooseContactDetailsPage        =>
      userAnswers => navigatorFromIndividualChooseContactDetailsPage(NormalMode)(userAnswers)
    case IndividualEmailAddressPage                =>
      _ => controllers.add.routes.UniqueTaxpayerReferenceYesNoController.onPageLoad(NormalMode)
    case _                                         => _ => routes.IndexController.onPageLoad()
  }

  private val checkRouteMap: Page => UserAnswers => Call = {
    case SubTradingNameYesNoPage            => navigatorFromSubTradingNameYesNoPage(CheckMode)(_)
    case SubAddressYesNoPage                => navigatorFromSubAddressYesNoPage(CheckMode)(_)
    case NationalInsuranceNumberYesNoPage   => navigatorFromNationalInsuranceNumberYesNoPage(CheckMode)(_)
    case UniqueTaxpayerReferenceYesNoPage   => navigatorFromUniqueTaxpayerReferenceYesNoPage(CheckMode)(_)
    case WorksReferenceNumberYesNoPage      => navigatorFromWorksReferenceNumberYesNoPage(CheckMode)(_)
    case IndividualChooseContactDetailsPage => navigatorFromIndividualChooseContactDetailsPage(CheckMode)(_)
    case IndividualMobileNumberPage         =>
      _ => controllers.add.routes.CheckYourAnswersController.onPageLoad()
    case IndividualPhoneNumberPage          =>
      _ => controllers.add.routes.CheckYourAnswersController.onPageLoad()
    case IndividualEmailAddressPage         =>
      _ => controllers.add.routes.CheckYourAnswersController.onPageLoad()
    case _                                  => _ => controllers.add.routes.CheckYourAnswersController.onPageLoad()
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
        controllers.add.routes.UniqueTaxpayerReferenceYesNoController.onPageLoad(NormalMode)
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
        controllers.add.routes.WorksReferenceNumberYesNoController.onPageLoad(NormalMode)
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
        controllers.add.routes.NationalInsuranceNumberYesNoController.onPageLoad(NormalMode)

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

}
