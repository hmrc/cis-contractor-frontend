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

import controllers.routes
import jakarta.inject.Singleton
import models.{CheckMode, Mode, NormalMode, UserAnswers}
import navigation.NavigatorForJourney
import pages.Page
import pages.verify.*
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
    case _                                        => _ => controllers.routes.JourneyRecoveryController.onPageLoad()
  }

  private def checkRouteMap: Page => UserAnswers => Call = {
    case ContractorEmailConfirmationNotStoredPage =>
      userAnswers => navigatorFromContractorEmailConfirmationNotStoredPage(CheckMode)(userAnswers)
    case SelectSubcontractorPage                  =>
      userAnswers => navigatorFromSelectSubcontractorPage(CheckMode)(userAnswers)
    case _                                        => _ => controllers.routes.JourneyRecoveryController.onPageLoad()

  }

  private def navigatorFromContractorEmailConfirmationNotStoredPage(mode: Mode)(ua: UserAnswers): Call =
    (ua.get(ContractorEmailConfirmationNotStoredPage), mode) match {
      case (Some(true), _) =>
        // ToDo: navigate to next page after the page is implemented
        controllers.verify.routes.ContractorEmailConfirmationNotStoredController.onPageLoad(NormalMode)

      case (Some(false), NormalMode) =>
        // ToDo: navigate to next page after the page is implemented
        controllers.verify.routes.ContractorEmailConfirmationNotStoredController.onPageLoad(NormalMode)
      case (Some(false), CheckMode)  =>
        // ToDo: navigate CYA page after the page is implemented and implement the logic to handle dependent pages
        routes.IndexController.onPageLoad()

      case _ =>
        routes.IndexController.onPageLoad()
    }

  private def navigatorFromSelectSubcontractorPage(mode: Mode)(ua: UserAnswers): Call = {
    val _ = ua
    controllers.verify.routes.SelectSubcontractorController.onPageLoad(mode)
  }

}
