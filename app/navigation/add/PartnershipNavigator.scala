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
import pages.add.partnership.*
import play.api.mvc.Call

import javax.inject.{Inject, Singleton}

@Singleton
class PartnershipNavigator @Inject() () extends NavigatorForJourney {

  override def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = mode match {
    case NormalMode =>
      normalRoutes(page)(userAnswers)
    case CheckMode  =>
      checkRouteMap(page)(userAnswers)
  }

  private val normalRoutes: Page => UserAnswers => Call = {
    case PartnershipNamePage                      =>
      _ => controllers.add.partnership.routes.PartnershipAddressYesNoController.onPageLoad(NormalMode)
    case PartnershipAddressYesNoPage              =>
      userAnswers => navigatorFromPartnershipAddressYesNoPage(NormalMode)(userAnswers)
    case PartnershipAddressPage                   =>
      _ => controllers.add.partnership.routes.PartnershipChooseContactDetailsController.onPageLoad(NormalMode)
    case PartnershipChooseContactDetailsPage      =>
      userAnswers => navigatorFromChooseContactDetailsPage(NormalMode)(userAnswers)
    case PartnershipEmailAddressPage              =>
      _ => controllers.add.partnership.routes.PartnershipHasUtrYesNoController.onPageLoad(NormalMode)
    case PartnershipMobileNumberPage              =>
      _ => controllers.add.partnership.routes.PartnershipHasUtrYesNoController.onPageLoad(NormalMode)
    case PartnershipPhoneNumberPage               =>
      _ => controllers.add.partnership.routes.PartnershipHasUtrYesNoController.onPageLoad(NormalMode)
    case PartnershipHasUtrYesNoPage               => userAnswers => navigatorFromPartnershipHasUtrYesNoPage(NormalMode)(userAnswers)
    case PartnershipUniqueTaxpayerReferencePage   =>
      _ => controllers.add.partnership.routes.PartnershipNominatedPartnerNameController.onPageLoad(NormalMode)
    case PartnershipNominatedPartnerNamePage      =>
      _ => controllers.add.partnership.routes.PartnershipNominatedPartnerUtrYesNoController.onPageLoad(NormalMode)
    case PartnershipWorksReferenceNumberYesNoPage =>
      userAnswers => navigatorFromPartnershipWorksReferenceNumberYesNoPage(NormalMode)(userAnswers)
    case PartnershipNominatedPartnerNinoPage      =>
      userAnswers => navigatorFromPartnershipNominatedPartnerNinoPage(NormalMode)(userAnswers)
    case PartnershipWorksReferenceNumberPage      =>
      _ => controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad()
    case PartnershipNominatedPartnerCrnPage       =>
      userAnswers => navigatorFromPartnershipNominatedPartnerCrnPage(NormalMode)(userAnswers)
    case PartnershipNominatedPartnerNinoYesNoPage =>
      userAnswers => navigatorFromPartnershipNominatedPartnerNinoYesNoPage(NormalMode)(userAnswers)
    case PartnershipNominatedPartnerCrnYesNoPage  =>
      userAnswers => navigatorFromPartnershipNominatedPartnerCrnYesNoPage(NormalMode)(userAnswers)
    case PartnershipNominatedPartnerUtrPage       =>
      _ => controllers.add.partnership.routes.PartnershipNominatedPartnerNinoYesNoController.onPageLoad(NormalMode)
    case PartnershipNominatedPartnerUtrYesNoPage  =>
      userAnswers => navigatorFromPartnershipNominatedPartnerUtrYesNoPage(NormalMode)(userAnswers)
    case _                                        => _ => routes.IndexController.onPageLoad()
  }

  private val checkRouteMap: Page => UserAnswers => Call = {
    case PartnershipHasUtrYesNoPage               => navigatorFromPartnershipHasUtrYesNoPage(CheckMode)(_)
    case PartnershipWorksReferenceNumberYesNoPage => navigatorFromPartnershipWorksReferenceNumberYesNoPage(CheckMode)(_)
    case PartnershipChooseContactDetailsPage      => navigatorFromChooseContactDetailsPage(CheckMode)(_)
    case PartnershipNominatedPartnerCrnYesNoPage  => navigatorFromPartnershipNominatedPartnerCrnYesNoPage(CheckMode)(_)
    case PartnershipNamePage                      =>
      _ => controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad()
    case PartnershipAddressYesNoPage              =>
      userAnswers => navigatorFromPartnershipAddressYesNoPage(CheckMode)(userAnswers)
    case PartnershipAddressPage                   =>
      _ => controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad()
    case PartnershipMobileNumberPage              =>
      _ => controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad()
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
    case _                                        => _ => controllers.add.routes.CheckYourAnswersController.onPageLoad()
  }

  private def navigatorFromPartnershipNominatedPartnerNinoPage(mode: Mode)(ua: UserAnswers): Call =
    (ua.get(PartnershipNominatedPartnerNinoPage), mode) match {
      case (Some(_), NormalMode) =>
        controllers.add.partnership.routes.PartnershipNominatedPartnerCrnYesNoController.onPageLoad(NormalMode)

      case (Some(_), CheckMode) =>
        controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad()

      case (None, _) =>
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

      case (Some(true), CheckMode) if ua.get(PartnershipUniqueTaxpayerReferencePage).isDefined =>
        controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad()

      case (Some(true), CheckMode) =>
        controllers.add.partnership.routes.PartnershipUniqueTaxpayerReferenceController.onPageLoad(CheckMode)

      case (Some(false), CheckMode) =>
        controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad()

      case (Some(true), NormalMode) =>
        controllers.add.partnership.routes.PartnershipUniqueTaxpayerReferenceController.onPageLoad(NormalMode)

      case (Some(false), NormalMode) =>
        controllers.add.partnership.routes.PartnershipNominatedPartnerNameController.onPageLoad(NormalMode)

      case _ =>
        routes.JourneyRecoveryController.onPageLoad()
    }

  private def navigatorFromPartnershipWorksReferenceNumberYesNoPage(mode: Mode)(ua: UserAnswers): Call =
    (ua.get(PartnershipWorksReferenceNumberYesNoPage), mode) match {

      case (Some(true), CheckMode) if ua.get(PartnershipWorksReferenceNumberPage).isDefined =>
        controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad()

      case (Some(true), CheckMode) =>
        controllers.add.partnership.routes.PartnershipWorksReferenceNumberController.onPageLoad(CheckMode)

      case (Some(false), CheckMode) =>
        controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad()

      case (Some(true), NormalMode) =>
        controllers.add.partnership.routes.PartnershipWorksReferenceNumberController.onPageLoad(NormalMode)

      case (Some(false), NormalMode) =>
        controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad()

      case _ =>
        routes.JourneyRecoveryController.onPageLoad()
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
      case (Some(Email), NormalMode)     =>
        controllers.add.partnership.routes.PartnershipEmailAddressController.onPageLoad(NormalMode)
      case (Some(Phone), NormalMode)     =>
        controllers.add.partnership.routes.PartnershipPhoneNumberController.onPageLoad(NormalMode)
      case (Some(Mobile), NormalMode)    =>
        controllers.add.partnership.routes.PartnershipMobileNumberController.onPageLoad(NormalMode)
      case (Some(NoDetails), NormalMode) =>
        controllers.add.partnership.routes.PartnershipHasUtrYesNoController.onPageLoad(NormalMode)
      case (Some(Email), CheckMode)      =>
        userAnswers
          .get(PartnershipEmailAddressPage)
          .fold(controllers.add.partnership.routes.PartnershipEmailAddressController.onPageLoad(CheckMode)) { _ =>
            controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad()
          }
      case (Some(Phone), CheckMode)      =>
        userAnswers
          .get(PartnershipPhoneNumberPage)
          .fold(controllers.add.partnership.routes.PartnershipPhoneNumberController.onPageLoad(CheckMode)) { _ =>
            controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad()
          }
      case (Some(Mobile), CheckMode)     =>
        userAnswers
          .get(PartnershipMobileNumberPage)
          .fold(controllers.add.partnership.routes.PartnershipMobileNumberController.onPageLoad(CheckMode)) { _ =>
            controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad()
          }
      case (Some(NoDetails), CheckMode)  =>
        controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad()
      case _                             => routes.JourneyRecoveryController.onPageLoad()
    }
}
