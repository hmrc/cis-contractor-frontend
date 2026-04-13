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

import base.SpecBase
import controllers.routes
import models.{CheckMode, NormalMode, UserAnswers}
import org.scalactic.Prettifier.default
import pages.Page
import pages.verify.ContractorEmailConfirmationNotStoredPage

class VerifyNavigatorSpec extends SpecBase {

  private val navigator = new VerifyNavigator()

  private lazy val journeyRecovery = routes.JourneyRecoveryController.onPageLoad()

  "VerifyNavigator" - {

    "in Normal mode" - {

      "must go from a page that doesn't exist in the route map to JourneyRecovery" in {
        case object UnknownPage extends Page

        navigator.nextPage(UnknownPage, NormalMode, UserAnswers("id")) mustBe journeyRecovery
      }

      "must go from ContractorEmailConfirmationNotStoredPage to ContractorEmailConfirmationNotStoredController when answer is true" in {
        val ua = emptyUserAnswers.setOrException(ContractorEmailConfirmationNotStoredPage, true)

        navigator.nextPage(ContractorEmailConfirmationNotStoredPage, NormalMode, ua) mustBe
          controllers.verify.routes.ContractorEmailConfirmationNotStoredController.onPageLoad(NormalMode)
      }

      "must go from ContractorEmailConfirmationNotStoredPage to ContractorEmailConfirmationNotStoredController when answer is false" in {
        val ua = emptyUserAnswers.setOrException(ContractorEmailConfirmationNotStoredPage, false)

        navigator.nextPage(ContractorEmailConfirmationNotStoredPage, NormalMode, ua) mustBe
          controllers.verify.routes.ContractorEmailConfirmationNotStoredController.onPageLoad(NormalMode)
      }

      "must go from ContractorEmailConfirmationNotStoredPage to Index when answer is not present" in {
        navigator.nextPage(
          ContractorEmailConfirmationNotStoredPage,
          NormalMode,
          emptyUserAnswers
        ) mustBe routes.IndexController.onPageLoad()
      }
    }

    "in Check mode" - {

      "must go from a page that doesn't exist in the route map to JourneyRecovery" in {
        case object UnknownPage extends Page

        navigator.nextPage(UnknownPage, CheckMode, UserAnswers("id")) mustBe journeyRecovery
      }

      "must go from ContractorEmailConfirmationNotStoredPage to ContractorEmailConfirmationNotStoredController when answer is true" in {
        val ua = emptyUserAnswers.setOrException(ContractorEmailConfirmationNotStoredPage, true)

        navigator.nextPage(ContractorEmailConfirmationNotStoredPage, CheckMode, ua) mustBe
          controllers.verify.routes.ContractorEmailConfirmationNotStoredController.onPageLoad(NormalMode)
      }

      "must go from ContractorEmailConfirmationNotStoredPage to Index when answer is false" in {
        val ua = emptyUserAnswers.setOrException(ContractorEmailConfirmationNotStoredPage, false)

        navigator.nextPage(ContractorEmailConfirmationNotStoredPage, CheckMode, ua) mustBe
          routes.IndexController.onPageLoad()
      }

      "must go from ContractorEmailConfirmationNotStoredPage to Index when answer is not present" in {
        navigator.nextPage(
          ContractorEmailConfirmationNotStoredPage,
          CheckMode,
          emptyUserAnswers
        ) mustBe routes.IndexController.onPageLoad()
      }
    }
  }
}
