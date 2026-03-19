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
import models.contact.ContactOptions.{Email, Mobile, Phone}
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
    case CheckMode =>
      checkRouteMap(page)(userAnswers)
  }
  
  private val normalRoutes: Page => UserAnswers => Call = {
    case CompanyNamePage                           => _ => controllers.add.company.routes.CompanyNameController.onPageLoad(NormalMode)
    case CompanyAddressYesNoPage                   =>
      _ => controllers.add.company.routes.CompanyAddressYesNoController.onPageLoad(NormalMode)
    case CompanyContactOptionsPage                 =>
      userAnswers => navigatorFromCompanyContactOptionsPage(NormalMode)(userAnswers)
    case CompanyEmailAddressPage                   =>
      _ => controllers.add.company.routes.CompanyEmailAddressController.onPageLoad(NormalMode)
    case CompanyMobileNumberPage                   =>
      _ => controllers.add.company.routes.CompanyMobileNumberController.onPageLoad(NormalMode)
    case CompanyAddressPage                        =>
      _ => controllers.add.company.routes.CompanyAddressController.onPageLoad(NormalMode)
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
    case CompanyWorksReferencePage                 =>
      _ => controllers.add.company.routes.CompanyCheckYourAnswersController.onPageLoad()
    case _                                         => _ => routes.IndexController.onPageLoad()
  }

  private val checkRouteMap: Page => UserAnswers => Call = {
    case CompanyContactOptionsPage => navigatorFromCompanyContactOptionsPage(CheckMode)(_)
    case CompanyAddressYesNoPage =>
      _ => controllers.add.company.routes.CompanyAddressYesNoController.onPageLoad(CheckMode)
    case CompanyAddressPage =>
      _ => controllers.add.company.routes.CompanyAddressController.onPageLoad(CheckMode)
    case CompanyEmailAddressPage =>
      _ => controllers.add.company.routes.CompanyEmailAddressController.onPageLoad(CheckMode)
    case CompanyPhoneNumberPage =>
      _ => controllers.add.company.routes.CompanyPhoneNumberController.onPageLoad(CheckMode)
    case CompanyNamePage =>
      _ => controllers.add.company.routes.CompanyCheckYourAnswersController.onPageLoad()
    case CompanyUtrYesNoPage =>
      userAnswers => navigatorFromCompanyUtrYesNoPage(CheckMode)(userAnswers)
    case CompanyCrnYesNoPage => navigatorFromCompanyCrnYesNoPage(CheckMode)(_)
    case CompanyWorksReferenceYesNoPage =>
      userAnswers => navigatorFromCompanyWorksReferenceYesNoPage(CheckMode)(userAnswers)
    case CompanyCrnPage =>
      _ => controllers.add.company.routes.CompanyCheckYourAnswersController.onPageLoad()
    case CompanyUtrPage =>
      _ => controllers.add.company.routes.CompanyCheckYourAnswersController.onPageLoad()
    case CompanyWorksReferencePage =>
      _ => controllers.add.company.routes.CompanyCheckYourAnswersController.onPageLoad()
    case CompanyMobileNumberPage =>
      _ => controllers.add.company.routes.CompanyMobileNumberController.onPageLoad(CheckMode)
    case _ => _ => controllers.add.routes.CheckYourAnswersController.onPageLoad()
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

  private def navigatorFromCompanyUtrYesNoPage(mode: Mode)(userAnswers: UserAnswers): Call =
    (userAnswers.get(CompanyUtrYesNoPage), mode) match {
      case (Some(true), _) =>
        controllers.add.company.routes.CompanyUtrController.onPageLoad(mode)
      case (Some(false), NormalMode) =>
        controllers.add.company.routes.CompanyCrnYesNoController.onPageLoad(NormalMode)
      case (Some(false), CheckMode) => controllers.add.routes.CheckYourAnswersController.onPageLoad()
      case (None, _) => routes.JourneyRecoveryController.onPageLoad()
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
}
