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

import models.contact.ContactOptions.{Email, Mobile, Phone}
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
    case TrustContactOptionsPage => userAnswers => navigatorFromTrustContactOptionsPage(NormalMode)(userAnswers)
    case TrustNamePage           => _ => controllers.add.trust.routes.TrustAddressYesNoController.onPageLoad(NormalMode)
    case _                       => _ => controllers.routes.IndexController.onPageLoad()
  }

  private val checkRouteMap: Page => UserAnswers => Call = {
    case TrustContactOptionsPage => userAnswers => navigatorFromTrustContactOptionsPage(CheckMode)(userAnswers)
    case TrustNamePage           => _ => controllers.add.trust.routes.TrustCheckYourAnswersController.onPageLoad()
    case _                       => _ => controllers.add.trust.routes.TrustCheckYourAnswersController.onPageLoad()
  }

  private def navigatorFromTrustContactOptionsPage(mode: Mode)(userAnswers: UserAnswers): Call =
    (userAnswers.get(TrustContactOptionsPage), mode) match {
      case (Some(Email), _)  =>
        controllers.add.trust.routes.TrustEmailAddressController.onPageLoad(mode)
      case (Some(Phone), _)  =>
        controllers.add.trust.routes.TrustPhoneNumberController.onPageLoad(mode)
      case (Some(Mobile), _) =>
        controllers.add.trust.routes.TrustMobileNumberController.onPageLoad(mode)
      case (Some(_), _)      =>
        controllers.add.trust.routes.TrustContactOptionsController.onPageLoad(mode)
      case (_, CheckMode)    =>
        controllers.add.trust.routes.TrustCheckYourAnswersController.onPageLoad()
      case _                 => controllers.routes.JourneyRecoveryController.onPageLoad()
    }

}
