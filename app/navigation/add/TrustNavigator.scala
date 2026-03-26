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

import models.contact.ContactOptions.{Email, Mobile, NoDetails, Phone}
import models.{CheckMode, Mode, NormalMode, UserAnswers}
import navigation.NavigatorForJourney
import pages.Page
import pages.add.trust.TrustContactOptionsPage
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
    case _                       => _ => controllers.routes.IndexController.onPageLoad()
  }

  private val checkRouteMap: Page => UserAnswers => Call = {
    case TrustContactOptionsPage => userAnswers => navigatorFromTrustContactOptionsPage(CheckMode)(userAnswers)
    case _                       => _ => controllers.add.routes.CheckYourAnswersController.onPageLoad()
  }

  private def navigatorFromTrustContactOptionsPage(mode: Mode)(userAnswers: UserAnswers): Call =
    mode match {
      case CheckMode =>
        controllers.add.trust.routes.TrustContactOptionsController.onPageLoad(CheckMode)

      case NormalMode =>
        userAnswers.get(TrustContactOptionsPage) match {
          case Some(Email) =>
            controllers.add.trust.routes.TrustEmailAddressController.onPageLoad(NormalMode)

          case Some(Phone) =>
            controllers.add.trust.routes.TrustPhoneNumberController.onPageLoad(NormalMode)

          case Some(Mobile) =>
            controllers.add.trust.routes.TrustMobileNumberController.onPageLoad(NormalMode)

          case Some(NoDetails) =>
            controllers.add.routes.CheckYourAnswersController.onPageLoad()

          case _ =>
            controllers.routes.JourneyRecoveryController.onPageLoad()
        }
    }

}
