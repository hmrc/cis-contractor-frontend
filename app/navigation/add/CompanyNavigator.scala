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

import controllers.routes
import models.contact.ContactOptions.{Email, Mobile, NoDetails, Phone}
import models.{CheckMode, Mode, NormalMode, UserAnswers}
import navigation.NavigatorForJourney
import pages.Page
import pages.add.company.*
import play.api.mvc.Call

import javax.inject.{Inject, Singleton}

@Singleton
class CompanyNavigator @Inject() () extends NavigatorForJourney {

  override def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = mode match {
    case NormalMode =>
      normalRoutes(page)(userAnswers)
    case CheckMode  =>
      checkRouteMap(page)(userAnswers)
  }

  private val normalRoutes: Page => UserAnswers => Call = {
    case CompanyNamePage                => _ => controllers.add.company.routes.CompanyAddressYesNoController.onPageLoad(NormalMode)
    case CompanyAddressYesNoPage        =>
      userAnswers => navigatorFromCompanyAddressYesNoPage(NormalMode)(userAnswers)
    case CompanyAddressPage             =>
      _ => controllers.add.company.routes.CompanyContactOptionsController.onPageLoad(NormalMode)
    case CompanyContactOptionsPage      =>
      userAnswers => navigatorFromCompanyContactOptionsPage(NormalMode)(userAnswers)
    case CompanyEmailAddressPage        =>
      _ => controllers.add.company.routes.CompanyUtrYesNoController.onPageLoad(NormalMode)
    case CompanyPhoneNumberPage         =>
      _ => controllers.add.company.routes.CompanyUtrYesNoController.onPageLoad(NormalMode)
    case CompanyMobileNumberPage        =>
      _ => controllers.add.company.routes.CompanyUtrYesNoController.onPageLoad(NormalMode)
    case CompanyUtrYesNoPage            =>
      userAnswers => navigatorFromCompanyUtrYesNoPage(NormalMode)(userAnswers)
    case CompanyUtrPage                 =>
      _ => controllers.add.company.routes.CompanyCrnYesNoController.onPageLoad(NormalMode)
    case CompanyCrnYesNoPage            =>
      userAnswers => navigatorFromCompanyCrnYesNoPage(NormalMode)(userAnswers)
    case CompanyCrnPage                 =>
      _ => controllers.add.company.routes.CompanyWorksReferenceYesNoController.onPageLoad(NormalMode)
    case CompanyWorksReferenceYesNoPage =>
      userAnswers => navigatorFromCompanyWorksReferenceYesNoPage(NormalMode)(userAnswers)
    case CompanyWorksReferencePage      =>
      _ => controllers.add.company.routes.CompanyCheckYourAnswersController.onPageLoad()
    case _                              => _ => routes.IndexController.onPageLoad()
  }

  private val checkRouteMap: Page => UserAnswers => Call = {
    case CompanyNamePage                =>
      _ => controllers.add.company.routes.CompanyCheckYourAnswersController.onPageLoad()
    case CompanyAddressYesNoPage        => navigatorFromCompanyAddressYesNoPage(CheckMode)(_)
    case CompanyAddressPage             =>
      _ => controllers.add.company.routes.CompanyCheckYourAnswersController.onPageLoad()
    case CompanyContactOptionsPage      => navigatorFromCompanyContactOptionsPage(CheckMode)(_)
    case CompanyEmailAddressPage        =>
      _ => controllers.add.company.routes.CompanyCheckYourAnswersController.onPageLoad()
    case CompanyPhoneNumberPage         =>
      _ => controllers.add.company.routes.CompanyCheckYourAnswersController.onPageLoad()
    case CompanyMobileNumberPage        =>
      _ => controllers.add.company.routes.CompanyCheckYourAnswersController.onPageLoad()
    case CompanyUtrYesNoPage            =>
      userAnswers => navigatorFromCompanyUtrYesNoPage(CheckMode)(userAnswers)
    case CompanyUtrPage                 =>
      _ => controllers.add.company.routes.CompanyCheckYourAnswersController.onPageLoad()
    case CompanyCrnYesNoPage            => navigatorFromCompanyCrnYesNoPage(CheckMode)(_)
    case CompanyCrnPage                 =>
      _ => controllers.add.company.routes.CompanyCheckYourAnswersController.onPageLoad()
    case CompanyWorksReferenceYesNoPage =>
      userAnswers => navigatorFromCompanyWorksReferenceYesNoPage(CheckMode)(userAnswers)
    case CompanyWorksReferencePage      =>
      _ => controllers.add.company.routes.CompanyCheckYourAnswersController.onPageLoad()
    case _                              => _ => controllers.add.routes.CheckYourAnswersController.onPageLoad()
  }

  private def navigatorFromCompanyContactOptionsPage(mode: Mode)(userAnswers: UserAnswers): Call =
    (userAnswers.get(CompanyContactOptionsPage), mode) match {
      case (Some(Email), NormalMode)     =>
        controllers.add.company.routes.CompanyEmailAddressController.onPageLoad(NormalMode)
      case (Some(Phone), NormalMode)     =>
        controllers.add.company.routes.CompanyPhoneNumberController.onPageLoad(NormalMode)
      case (Some(Mobile), NormalMode)    =>
        controllers.add.company.routes.CompanyMobileNumberController.onPageLoad(NormalMode)
      case (Some(NoDetails), NormalMode) =>
        controllers.add.company.routes.CompanyUtrYesNoController.onPageLoad(NormalMode)
      case (Some(Email), CheckMode)      =>
        userAnswers
          .get(CompanyEmailAddressPage)
          .fold(controllers.add.company.routes.CompanyEmailAddressController.onPageLoad(CheckMode)) { _ =>
            controllers.add.company.routes.CompanyCheckYourAnswersController.onPageLoad()
          }
      case (Some(Phone), CheckMode)      =>
        userAnswers
          .get(CompanyPhoneNumberPage)
          .fold(controllers.add.company.routes.CompanyPhoneNumberController.onPageLoad(CheckMode)) { _ =>
            controllers.add.company.routes.CompanyCheckYourAnswersController.onPageLoad()
          }
      case (Some(Mobile), CheckMode)     =>
        userAnswers
          .get(CompanyMobileNumberPage)
          .fold(controllers.add.company.routes.CompanyMobileNumberController.onPageLoad(CheckMode)) { _ =>
            controllers.add.company.routes.CompanyCheckYourAnswersController.onPageLoad()
          }
      case (Some(NoDetails), CheckMode)  =>
        controllers.add.company.routes.CompanyCheckYourAnswersController.onPageLoad()
      case _                             => routes.JourneyRecoveryController.onPageLoad()
    }

  private def navigatorFromCompanyAddressYesNoPage(mode: Mode)(userAnswers: UserAnswers): Call =
    (userAnswers.get(CompanyAddressYesNoPage), mode) match {
      case (Some(true), NormalMode)  =>
        controllers.add.company.routes.CompanyAddressController.onPageLoad(NormalMode)
      case (Some(false), NormalMode) =>
        controllers.add.company.routes.CompanyContactOptionsController.onPageLoad(NormalMode)
      case (Some(true), CheckMode)   =>
        userAnswers
          .get(CompanyAddressPage)
          .fold(controllers.add.company.routes.CompanyAddressController.onPageLoad(CheckMode)) { _ =>
            controllers.add.company.routes.CompanyCheckYourAnswersController.onPageLoad()
          }
      case (Some(false), CheckMode)  =>
        controllers.add.company.routes.CompanyCheckYourAnswersController.onPageLoad()
      case _                         =>
        routes.JourneyRecoveryController.onPageLoad()
    }

  private def navigatorFromCompanyUtrYesNoPage(mode: Mode)(userAnswers: UserAnswers): Call =
    (userAnswers.get(CompanyUtrYesNoPage), mode) match {
      case (Some(true), NormalMode)  =>
        controllers.add.company.routes.CompanyUtrController.onPageLoad(NormalMode)
      case (Some(false), NormalMode) =>
        controllers.add.company.routes.CompanyCrnYesNoController.onPageLoad(NormalMode)
      case (Some(true), CheckMode)   =>
        userAnswers
          .get(CompanyUtrPage)
          .fold(controllers.add.company.routes.CompanyUtrController.onPageLoad(CheckMode)) { _ =>
            controllers.add.company.routes.CompanyCheckYourAnswersController.onPageLoad()
          }
      case (Some(false), CheckMode)  =>
        controllers.add.company.routes.CompanyCheckYourAnswersController.onPageLoad()
      case _                         =>
        routes.JourneyRecoveryController.onPageLoad()
    }

  private def navigatorFromCompanyCrnYesNoPage(mode: Mode)(userAnswers: UserAnswers): Call =
    (userAnswers.get(CompanyCrnYesNoPage), mode) match {
      case (Some(true), NormalMode)  =>
        controllers.add.company.routes.CompanyCrnController.onPageLoad(NormalMode)
      case (Some(false), NormalMode) =>
        controllers.add.company.routes.CompanyWorksReferenceYesNoController.onPageLoad(NormalMode)
      case (Some(true), CheckMode)   =>
        userAnswers
          .get(CompanyCrnPage)
          .fold(controllers.add.company.routes.CompanyCrnController.onPageLoad(CheckMode)) { _ =>
            controllers.add.company.routes.CompanyCheckYourAnswersController.onPageLoad()
          }
      case (Some(false), CheckMode)  =>
        controllers.add.company.routes.CompanyCheckYourAnswersController.onPageLoad()
      case _                         =>
        routes.JourneyRecoveryController.onPageLoad()
    }

  private def navigatorFromCompanyWorksReferenceYesNoPage(mode: Mode)(userAnswers: UserAnswers): Call =
    (userAnswers.get(CompanyWorksReferenceYesNoPage), mode) match {
      case (Some(true), NormalMode)  =>
        controllers.add.company.routes.CompanyWorksReferenceController.onPageLoad(NormalMode)
      case (Some(false), NormalMode) =>
        controllers.add.company.routes.CompanyCheckYourAnswersController.onPageLoad()
      case (Some(true), CheckMode)   =>
        userAnswers
          .get(CompanyWorksReferencePage)
          .fold(controllers.add.company.routes.CompanyWorksReferenceController.onPageLoad(CheckMode)) { _ =>
            controllers.add.company.routes.CompanyCheckYourAnswersController.onPageLoad()
          }
      case (Some(false), CheckMode)  =>
        controllers.add.company.routes.CompanyCheckYourAnswersController.onPageLoad()
      case _                         =>
        routes.JourneyRecoveryController.onPageLoad()
    }
}
