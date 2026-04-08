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
import pages.add.trust.*
import play.api.mvc.Call

import javax.inject.{Inject, Singleton}

@Singleton
class TrustNavigator @Inject() () extends NavigatorForJourney {

  override def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = mode match {
    case NormalMode =>
      normalRoutes(page)(userAnswers)
    case CheckMode  =>
      checkRouteMap(page)(userAnswers)
  }

  private val normalRoutes: Page => UserAnswers => Call = {

    case TrustNamePage                => _ => controllers.add.trust.routes.TrustAddressYesNoController.onPageLoad(NormalMode)
    case TrustEmailAddressPage        => _ => controllers.add.trust.routes.TrustUtrYesNoController.onPageLoad(NormalMode)
    case TrustAddressYesNoPage        => userAnswers => navigatorFromTrustAddressYesNoPage(NormalMode)(userAnswers)
    case TrustAddressPage             => _ => controllers.add.trust.routes.TrustContactOptionsController.onPageLoad(NormalMode)
    case TrustUtrYesNoPage            => userAnswers => navigatorFromTrustUtrYesNoPage(NormalMode)(userAnswers)
    case TrustUtrPage                 => _ => controllers.add.trust.routes.TrustWorksReferenceYesNoController.onPageLoad(NormalMode)
    case TrustPhoneNumberPage         => _ => controllers.add.trust.routes.TrustUtrYesNoController.onPageLoad(NormalMode)
    case TrustMobileNumberPage        => _ => controllers.add.trust.routes.TrustUtrYesNoController.onPageLoad(NormalMode)
    case TrustContactOptionsPage      => userAnswers => navigatorFromTrustContactOptionsPage(NormalMode)(userAnswers)
    case TrustWorksReferenceYesNoPage =>
      userAnswers => navigatorFromTrustWorksReferenceYesNoPage(NormalMode)(userAnswers)
    case TrustWorksReferencePage      => _ => controllers.add.trust.routes.TrustCheckYourAnswersController.onPageLoad()
    case _                            => _ => routes.IndexController.onPageLoad()
  }

  private val checkRouteMap: Page => UserAnswers => Call = {

    case TrustNamePage                => _ => controllers.add.trust.routes.TrustCheckYourAnswersController.onPageLoad()
    case TrustEmailAddressPage        => _ => controllers.add.trust.routes.TrustCheckYourAnswersController.onPageLoad()
    case TrustAddressYesNoPage        => navigatorFromTrustAddressYesNoPage(CheckMode)(_)
    case TrustAddressPage             => _ => controllers.add.trust.routes.TrustCheckYourAnswersController.onPageLoad()
    case TrustUtrYesNoPage            => navigatorFromTrustUtrYesNoPage(CheckMode)(_)
    case TrustUtrPage                 => _ => controllers.add.trust.routes.TrustCheckYourAnswersController.onPageLoad()
    case TrustPhoneNumberPage         => _ => controllers.add.trust.routes.TrustCheckYourAnswersController.onPageLoad()
    case TrustMobileNumberPage        => _ => controllers.add.trust.routes.TrustCheckYourAnswersController.onPageLoad()
    case TrustContactOptionsPage      => userAnswers => navigatorFromTrustContactOptionsPage(CheckMode)(userAnswers)
    case TrustWorksReferenceYesNoPage => navigatorFromTrustWorksReferenceYesNoPage(CheckMode)(_)
    case TrustWorksReferencePage      => _ => controllers.add.trust.routes.TrustCheckYourAnswersController.onPageLoad()
    case _                            => _ => controllers.add.trust.routes.TrustCheckYourAnswersController.onPageLoad()
  }

  private def navigatorFromTrustUtrYesNoPage(mode: Mode)(ua: UserAnswers): Call =
    (ua.get(TrustUtrYesNoPage), mode) match {
      case (Some(true), _)           => controllers.add.trust.routes.TrustUtrController.onPageLoad(mode)
      case (Some(false), NormalMode) =>
        controllers.add.trust.routes.TrustWorksReferenceYesNoController.onPageLoad(NormalMode)

      case (Some(false), CheckMode) =>
        controllers.add.trust.routes.TrustCheckYourAnswersController.onPageLoad()

      case _ =>
        routes.JourneyRecoveryController.onPageLoad()
    }

  private def navigatorFromTrustWorksReferenceYesNoPage(mode: Mode)(ua: UserAnswers): Call =
    (ua.get(TrustWorksReferenceYesNoPage), mode) match {
      case (Some(true), _) =>
        controllers.add.trust.routes.TrustWorksReferenceController.onPageLoad(mode)

      case (Some(false), NormalMode) =>
        controllers.add.trust.routes.TrustCheckYourAnswersController.onPageLoad()

      case (Some(false), CheckMode) =>
        controllers.add.trust.routes.TrustCheckYourAnswersController.onPageLoad()

      case _ =>
        routes.JourneyRecoveryController.onPageLoad()
    }

  private def navigatorFromTrustAddressYesNoPage(mode: Mode)(ua: UserAnswers): Call =
    (ua.get(TrustAddressYesNoPage), mode) match {
      case (Some(true), NormalMode)  =>
        controllers.add.trust.routes.TrustAddressController.onPageLoad(NormalMode)
      case (Some(false), NormalMode) =>
        controllers.add.trust.routes.TrustContactOptionsController.onPageLoad(NormalMode)
      case (Some(true), CheckMode)   =>
        ua.get(TrustAddressPage)
          .fold(controllers.add.trust.routes.TrustAddressController.onPageLoad(CheckMode)) { _ =>
            controllers.add.trust.routes.TrustCheckYourAnswersController.onPageLoad()
          }
      case (Some(false), CheckMode)  =>
        controllers.add.trust.routes.TrustCheckYourAnswersController.onPageLoad()
      case _                         =>
        routes.JourneyRecoveryController.onPageLoad()
    }

  private def navigatorFromTrustContactOptionsPage(mode: Mode)(userAnswers: UserAnswers): Call =
    (userAnswers.get(TrustContactOptionsPage), mode) match {
      case (Some(Email), NormalMode) =>
        controllers.add.trust.routes.TrustEmailAddressController.onPageLoad(NormalMode)
      case (Some(Email), CheckMode)  =>
        userAnswers
          .get(TrustEmailAddressPage)
          .fold(controllers.add.trust.routes.TrustEmailAddressController.onPageLoad(CheckMode)) { _ =>
            controllers.add.trust.routes.TrustCheckYourAnswersController.onPageLoad()
          }

      case (Some(Phone), NormalMode) =>
        controllers.add.trust.routes.TrustPhoneNumberController.onPageLoad(NormalMode)
      case (Some(Phone), CheckMode)  =>
        userAnswers
          .get(TrustPhoneNumberPage)
          .fold(controllers.add.trust.routes.TrustPhoneNumberController.onPageLoad(CheckMode)) { _ =>
            controllers.add.trust.routes.TrustCheckYourAnswersController.onPageLoad()
          }

      case (Some(Mobile), NormalMode) =>
        controllers.add.trust.routes.TrustMobileNumberController.onPageLoad(NormalMode)
      case (Some(Mobile), CheckMode)  =>
        userAnswers
          .get(TrustMobileNumberPage)
          .fold(controllers.add.trust.routes.TrustMobileNumberController.onPageLoad(CheckMode)) { _ =>
            controllers.add.trust.routes.TrustCheckYourAnswersController.onPageLoad()
          }

      case (Some(NoDetails), CheckMode) =>
        controllers.add.trust.routes.TrustCheckYourAnswersController.onPageLoad()

      case (Some(_), _) =>
        controllers.add.trust.routes.TrustUtrYesNoController.onPageLoad(mode)

      case _ => controllers.routes.JourneyRecoveryController.onPageLoad()
    }

}
