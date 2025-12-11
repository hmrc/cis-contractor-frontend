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

import base.SpecBase
import controllers.routes
import pages.*
import models.*
import pages.add.*

class NavigatorSpec extends SpecBase {

  val navigator = new Navigator

  private lazy val journeyRecovery = routes.JourneyRecoveryController.onPageLoad()
  private lazy val CYA             = routes.CheckYourAnswersController.onPageLoad()

  "Navigator" - {

    "in Normal mode" - {

      "must go from a page that doesn't exist in the route map to Index" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, NormalMode, UserAnswers("id")) mustBe routes.IndexController.onPageLoad()
      }

      "must go from TypeOfSubcontractorPage to SubUseTradingNameController" in {
        navigator.nextPage(
          TypeOfSubcontractorPage,
          NormalMode,
          UserAnswers("id")
        ) mustBe controllers.add.routes.SubUseTradingNameController.onPageLoad(NormalMode)
      }

      "must go from SubUseTradingNamePage to TradingNameOfSubcontractorController when True" in {
        navigator.nextPage(
          SubUseTradingNamePage,
          NormalMode,
          emptyUserAnswers.setOrException(SubUseTradingNamePage, true)
        ) mustBe controllers.add.routes.TradingNameOfSubcontractorController.onPageLoad(NormalMode)
      }

      "must go from SubUseTradingNamePage to next page when false" in {
        navigator.nextPage(
          SubUseTradingNamePage,
          NormalMode,
          emptyUserAnswers.setOrException(SubUseTradingNamePage, false)
        ) mustBe controllers.add.routes.SubUseTradingNameController.onPageLoad(NormalMode)
      }

      "must go from SubUseTradingNamePage to journey recovery page when incomplete info provided" in {
        navigator.nextPage(
          SubUseTradingNamePage,
          NormalMode,
          emptyUserAnswers
        ) mustBe journeyRecovery
      }

      "must go from a TradingNameOfSubcontractorPage to next page" in {
        navigator.nextPage(
          TradingNameOfSubcontractorPage,
          NormalMode,
          UserAnswers("id")
        ) mustBe controllers.add.routes.TradingNameOfSubcontractorController.onPageLoad(NormalMode)
      }

    }

    "in Check mode" - {

      "must go from a page that doesn't exist in the edit route map to CheckYourAnswers" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, CheckMode, UserAnswers("id")) mustBe routes.CheckYourAnswersController
          .onPageLoad()
      }

      "must go from SubUseTradingNamePage to next page when true" in {
        navigator.nextPage(
          SubUseTradingNamePage,
          CheckMode,
          emptyUserAnswers.setOrException(SubUseTradingNamePage, true)
        ) mustBe controllers.add.routes.TradingNameOfSubcontractorController.onPageLoad(CheckMode)
      }

      "must go from SubUseTradingNamePage to CYA page when false" in {
        navigator.nextPage(
          SubUseTradingNamePage,
          CheckMode,
          emptyUserAnswers.setOrException(SubUseTradingNamePage, false)
        ) mustBe CYA
      }

      "must go from SubUseTradingNamePage to CYA page when incomplete info provided" in {
        navigator.nextPage(
          SubUseTradingNamePage,
          CheckMode,
          emptyUserAnswers
        ) mustBe journeyRecovery
      }

    }
  }
}
