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

package navigation.verify

import jakarta.inject.Singleton
import models.{CheckMode, Mode, NormalMode, UserAnswers}
import navigation.NavigatorForJourney
import pages.Page
import pages.verify.*
import models.verify.ContractorEmailConfirmationStored.{CurrentEmail, DifferentEmail, DoNotSend}
import play.api.mvc.Call

import javax.inject.Inject

@Singleton
class VerifyNavigator @Inject() () extends NavigatorForJourney {

  override def nextPage(page: Page, mode: Mode, userAnswers: models.UserAnswers): Call = mode match {
    case NormalMode =>
      normalRoutes(page)(userAnswers)
    case CheckMode  =>
      checkRouteMap(page)(userAnswers)
  }

  private def normalRoutes: Page => UserAnswers => Call = {

    case ContractorEmailConfirmationNotStoredPage =>
      userAnswers => navigatorFromContractorEmailConfirmationNotStoredPage(NormalMode)(userAnswers)
    case SelectSubcontractorPage                  =>
      userAnswers => navigatorFromSelectSubcontractorPage(NormalMode)(userAnswers)
    case ReverifyExistingSubcontractorsYesNoPage  =>
      userAnswers => navigatorFromReverifyExistingSubcontractorsYesNoPage(NormalMode)(userAnswers)
    case ContractorEmailConfirmationStoredPage    =>
      userAnswers => navigatorFromContractorEmailConfirmationStoredPage(NormalMode)(userAnswers)
    case SelectSubcontractorsToReverifyPage       =>
      userAnswers => navigatorFromSelectSubcontractorsToReverifyPage(NormalMode)(userAnswers)
    case EmailAddressPage                         =>
      _ => controllers.verify.routes.VerifyCheckYourAnswersController.onPageLoad()
    case _                                        => _ => controllers.routes.JourneyRecoveryController.onPageLoad()
  }

  private def checkRouteMap: Page => UserAnswers => Call = {
    case ContractorEmailConfirmationNotStoredPage =>
      userAnswers => navigatorFromContractorEmailConfirmationNotStoredPage(CheckMode)(userAnswers)
    case SelectSubcontractorPage                  =>
      userAnswers => navigatorFromSelectSubcontractorPage(CheckMode)(userAnswers)
    case ReverifyExistingSubcontractorsYesNoPage  =>
      userAnswers => navigatorFromReverifyExistingSubcontractorsYesNoPage(CheckMode)(userAnswers)
    case ContractorEmailConfirmationStoredPage    =>
      userAnswers => navigatorFromContractorEmailConfirmationStoredPage(CheckMode)(userAnswers)
    case SelectSubcontractorsToReverifyPage       =>
      userAnswers => navigatorFromSelectSubcontractorsToReverifyPage(CheckMode)(userAnswers)
    case EmailAddressPage                         =>
      _ => controllers.verify.routes.VerifyCheckYourAnswersController.onPageLoad()
    case _                                        => _ => controllers.routes.JourneyRecoveryController.onPageLoad()
  }

  private def navigatorFromContractorEmailConfirmationNotStoredPage(mode: Mode)(ua: UserAnswers): Call =
    (ua.get(ContractorEmailConfirmationNotStoredPage), mode) match {
      case (Some(true), _)  => controllers.verify.routes.EmailAddressController.onPageLoad(mode)
      case (Some(false), _) => controllers.verify.routes.VerifyCheckYourAnswersController.onPageLoad()
      case _                => controllers.routes.JourneyRecoveryController.onPageLoad()
    }

  private def navigatorFromSelectSubcontractorPage(mode: Mode)(ua: UserAnswers): Call = {
    val _ = ua
    mode match {
      case NormalMode => controllers.verify.routes.ReverifyExistingSubcontractorsYesNoController.onPageLoad(NormalMode)
      case CheckMode  => controllers.verify.routes.VerifyCheckYourAnswersController.onPageLoad()
    }
  }

  private def navigatorFromReverifyExistingSubcontractorsYesNoPage(mode: Mode)(ua: UserAnswers): Call =
    (ua.get(ReverifyExistingSubcontractorsYesNoPage), mode) match {
      case (Some(true), _)           =>
        controllers.verify.routes.SelectSubcontractorsToReverifyController.onPageLoad(mode)
      case (Some(false), NormalMode) =>
        emailConfirmationPage(ua, NormalMode)
      case (Some(false), CheckMode)  =>
        controllers.verify.routes.VerifyCheckYourAnswersController.onPageLoad()
      case _                         =>
        controllers.routes.JourneyRecoveryController.onPageLoad()
    }

  private def navigatorFromSelectSubcontractorsToReverifyPage(mode: Mode)(ua: UserAnswers): Call =
    mode match {
      case NormalMode => emailConfirmationPage(ua, NormalMode)
      case CheckMode  => controllers.verify.routes.VerifyCheckYourAnswersController.onPageLoad()
    }

  private def emailConfirmationPage(ua: UserAnswers, mode: Mode): Call = {
    val hasStoredEmail = ua
      .get(NewestVerificationBatchResponsePage)
      .flatMap(_.scheme)
      .flatMap(_.emailAddress)
      .isDefined
    if (hasStoredEmail) controllers.verify.routes.ContractorEmailConfirmationStoredController.onPageLoad(mode)
    else controllers.verify.routes.ContractorEmailConfirmationNotStoredController.onPageLoad(mode)
  }

  private def navigatorFromContractorEmailConfirmationStoredPage(mode: Mode)(ua: UserAnswers): Call =
    (ua.get(ContractorEmailConfirmationStoredPage), mode) match {
      case (Some(CurrentEmail), _)   => controllers.verify.routes.VerifyCheckYourAnswersController.onPageLoad()
      case (Some(DifferentEmail), _) => controllers.verify.routes.EmailAddressController.onPageLoad(mode)
      case (Some(DoNotSend), _)      => controllers.verify.routes.VerifyCheckYourAnswersController.onPageLoad()
      case _                         => controllers.routes.JourneyRecoveryController.onPageLoad()
    }

}
