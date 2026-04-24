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
import pages.Page
import models.verify.ContractorEmailConfirmationStored
import pages.verify.*

class VerifyNavigatorSpec extends SpecBase {

  private val navigator = new VerifyNavigator()

  private lazy val journeyRecovery = routes.JourneyRecoveryController.onPageLoad()

  "VerifyNavigator" - {

    "in Normal mode" - {

      "must go from a page that doesn't exist in the route map to JourneyRecovery" in {
        case object UnknownPage extends Page

        navigator.nextPage(UnknownPage, NormalMode, UserAnswers("id")) mustBe journeyRecovery
      }

      "must go from ContractorEmailConfirmationNotStoredPage to EmailAddressController when answer is true" in {
        val ua = emptyUserAnswers.setOrException(ContractorEmailConfirmationNotStoredPage, true)

        navigator.nextPage(ContractorEmailConfirmationNotStoredPage, NormalMode, ua) mustBe
          controllers.verify.routes.EmailAddressController.onPageLoad(NormalMode)
      }

      // updte test once VF-06 is completed SM-01a NO -> (VF-06 not implemented yet)
      "must go from ContractorEmailConfirmationNotStoredPage to JourneyRecovery when answer is false" in {
        val ua = emptyUserAnswers.setOrException(ContractorEmailConfirmationNotStoredPage, false)

        navigator.nextPage(ContractorEmailConfirmationNotStoredPage, NormalMode, ua) mustBe journeyRecovery
      }

      "must go from ContractorEmailConfirmationNotStoredPage to JourneyRecovery when answer is not present" in {
        navigator.nextPage(
          ContractorEmailConfirmationNotStoredPage,
          NormalMode,
          emptyUserAnswers
        ) mustBe journeyRecovery
      }

      "must go from ContractorEmailConfirmationStoredPage to JourneyRecovery when answer is CurrentEmail" in {
        val ua = emptyUserAnswers.setOrException(
          ContractorEmailConfirmationStoredPage,
          ContractorEmailConfirmationStored.CurrentEmail
        )

        navigator.nextPage(ContractorEmailConfirmationStoredPage, NormalMode, ua) mustBe journeyRecovery
      }

      "must go from ContractorEmailConfirmationStoredPage to EmailAddressController when answer is DifferentEmail" in {
        val ua = emptyUserAnswers.setOrException(
          ContractorEmailConfirmationStoredPage,
          ContractorEmailConfirmationStored.DifferentEmail
        )

        navigator.nextPage(ContractorEmailConfirmationStoredPage, NormalMode, ua) mustBe
          controllers.verify.routes.EmailAddressController.onPageLoad(NormalMode)
      }

      "must go from ContractorEmailConfirmationStoredPage to JourneyRecovery when answer is DoNotSend" in {
        val ua = emptyUserAnswers.setOrException(
          ContractorEmailConfirmationStoredPage,
          ContractorEmailConfirmationStored.DoNotSend
        )

        navigator.nextPage(ContractorEmailConfirmationStoredPage, NormalMode, ua) mustBe journeyRecovery
      }

      "must go from ContractorEmailConfirmationStoredPage to JourneyRecovery when answer is not present" in {
        navigator.nextPage(ContractorEmailConfirmationStoredPage, NormalMode, emptyUserAnswers) mustBe journeyRecovery
      }

      // update test once VF-06 is completed
      "must go from EmailAddressPage to EmailAddressController in NormalMode" in {
        val ua = emptyUserAnswers.set(EmailAddressPage, "test@test.com").success.value

        navigator.nextPage(EmailAddressPage, NormalMode, ua) mustBe
          controllers.verify.routes.EmailAddressController.onPageLoad(NormalMode)
      }
    }

    "in Check mode" - {

      "must go from a page that doesn't exist in the route map to JourneyRecovery" in {
        case object UnknownPage extends Page

        navigator.nextPage(UnknownPage, CheckMode, UserAnswers("id")) mustBe journeyRecovery
      }

      "must go from ContractorEmailConfirmationNotStoredPage to EmailAddressController when answer is true" in {
        val ua = emptyUserAnswers.setOrException(ContractorEmailConfirmationNotStoredPage, true)

        navigator.nextPage(ContractorEmailConfirmationNotStoredPage, CheckMode, ua) mustBe
          controllers.verify.routes.EmailAddressController.onPageLoad(CheckMode)
      }

      "must go from ContractorEmailConfirmationNotStoredPage to JourneyRecovery when answer is false" in {
        val ua = emptyUserAnswers.setOrException(ContractorEmailConfirmationNotStoredPage, false)

        navigator.nextPage(ContractorEmailConfirmationNotStoredPage, CheckMode, ua) mustBe journeyRecovery
      }

      "must go from ContractorEmailConfirmationNotStoredPage to JourneyRecovery when answer is not present" in {
        navigator.nextPage(
          ContractorEmailConfirmationNotStoredPage,
          CheckMode,
          emptyUserAnswers
        ) mustBe journeyRecovery
      }

      "must go from ContractorEmailConfirmationStoredPage to JourneyRecovery when answer is CurrentEmail" in {
        val ua = emptyUserAnswers.setOrException(
          ContractorEmailConfirmationStoredPage,
          ContractorEmailConfirmationStored.CurrentEmail
        )

        navigator.nextPage(ContractorEmailConfirmationStoredPage, CheckMode, ua) mustBe journeyRecovery
      }

      "must go from ContractorEmailConfirmationStoredPage to EmailAddressController when answer is DifferentEmail" in {
        val ua = emptyUserAnswers.setOrException(
          ContractorEmailConfirmationStoredPage,
          ContractorEmailConfirmationStored.DifferentEmail
        )

        navigator.nextPage(ContractorEmailConfirmationStoredPage, CheckMode, ua) mustBe
          controllers.verify.routes.EmailAddressController.onPageLoad(CheckMode)
      }

      "must go from ContractorEmailConfirmationStoredPage to JourneyRecovery when answer is DoNotSend" in {
        val ua = emptyUserAnswers.setOrException(
          ContractorEmailConfirmationStoredPage,
          ContractorEmailConfirmationStored.DoNotSend
        )

        navigator.nextPage(ContractorEmailConfirmationStoredPage, CheckMode, ua) mustBe journeyRecovery
      }

      "must go from ContractorEmailConfirmationStoredPage to JourneyRecovery when answer is not present" in {
        navigator.nextPage(ContractorEmailConfirmationStoredPage, CheckMode, emptyUserAnswers) mustBe journeyRecovery
      }

      // update test once VF-06 is finished
      "must go from EmailAddressPage to EmailAddressController in CheckMode" in {
        val ua = emptyUserAnswers.set(EmailAddressPage, "test@test.com").success.value

        navigator.nextPage(EmailAddressPage, CheckMode, ua) mustBe
          controllers.verify.routes.EmailAddressController.onPageLoad(CheckMode)
      }
    }
  }
}
