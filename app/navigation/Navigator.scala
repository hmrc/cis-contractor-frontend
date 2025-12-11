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
import pages.add.{SubUseTradingNamePage, TradingNameOfSubcontractorPage, TypeOfSubcontractorPage}

@Singleton
class Navigator @Inject() () {

  private val normalRoutes: Page => UserAnswers => Call = {
    case TypeOfSubcontractorPage        => _ => controllers.add.routes.SubUseTradingNameController.onPageLoad(NormalMode)
    case SubUseTradingNamePage          => userAnswers => navigatorFromSubUseTradingNamePage(NormalMode)(userAnswers)
    case TradingNameOfSubcontractorPage =>
      _ => controllers.add.routes.TradingNameOfSubcontractorController.onPageLoad(NormalMode)
    case _                              => _ => routes.IndexController.onPageLoad()
  }

  private val checkRouteMap: Page => UserAnswers => Call = {
    case SubUseTradingNamePage => userAnswers => navigatorFromSubUseTradingNamePage(CheckMode)(userAnswers)
    case _                     => _ => routes.CheckYourAnswersController.onPageLoad()
  }

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = mode match {
    case NormalMode =>
      normalRoutes(page)(userAnswers)
    case CheckMode  =>
      checkRouteMap(page)(userAnswers)
  }

  private def navigatorFromSubUseTradingNamePage(mode: Mode)(userAnswers: UserAnswers): Call =
    (userAnswers.get(SubUseTradingNamePage), mode) match {
      case (Some(true), _)           => controllers.add.routes.TradingNameOfSubcontractorController.onPageLoad(mode)
      case (Some(false), NormalMode) => controllers.add.routes.SubUseTradingNameController.onPageLoad(NormalMode)
      case (Some(false), CheckMode)  => routes.CheckYourAnswersController.onPageLoad()
      case (None, _)                 => routes.JourneyRecoveryController.onPageLoad()
    }
}
