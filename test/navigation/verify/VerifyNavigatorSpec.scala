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
import models.verify.ContractorEmailConfirmationStored
import pages.Page
import pages.verify.*

class VerifyNavigatorSpec extends SpecBase {

  private val navigator = new VerifyNavigator()

  private lazy val journeyRecovery = routes.JourneyRecoveryController.onPageLoad()
  private lazy val cya             = controllers.verify.routes.VerifyCheckYourAnswersController.onPageLoad()

  "VerifyNavigator" - {

    "in Normal mode" - {

      "must go from a page that doesn't exist in the route map to JourneyRecovery" in {
        case object UnknownPage extends Page

        navigator.nextPage(UnknownPage, NormalMode, UserAnswers("id")) mustBe journeyRecovery
      }

      "ContractorEmailConfirmationNotStoredPage" - {

        "must go to EmailAddressController when answer is true" in {
          val ua = emptyUserAnswers.setOrException(ContractorEmailConfirmationNotStoredPage, true)
          navigator.nextPage(ContractorEmailConfirmationNotStoredPage, NormalMode, ua) mustBe
            controllers.verify.routes.EmailAddressController.onPageLoad(NormalMode)
        }

        "must go to VerifyCheckYourAnswers when answer is false" in {
          val ua = emptyUserAnswers.setOrException(ContractorEmailConfirmationNotStoredPage, false)
          navigator.nextPage(ContractorEmailConfirmationNotStoredPage, NormalMode, ua) mustBe cya
        }

        "must go to JourneyRecovery when answer is not present" in {
          navigator.nextPage(
            ContractorEmailConfirmationNotStoredPage,
            NormalMode,
            emptyUserAnswers
          ) mustBe journeyRecovery
        }
      }

      "SelectSubcontractorPage" - {

        "must go to ReverifyExistingSubcontractorsYesNoController in NormalMode" in {
          navigator.nextPage(SelectSubcontractorPage, NormalMode, emptyUserAnswers) mustBe
            controllers.verify.routes.ReverifyExistingSubcontractorsYesNoController.onPageLoad(NormalMode)
        }
      }

      "ReverifyExistingSubcontractorsYesNoPage" - {

        "must go to SelectSubcontractorsToReverifyController when answer is true" in {
          val ua = emptyUserAnswers.setOrException(ReverifyExistingSubcontractorsYesNoPage, true)
          navigator.nextPage(ReverifyExistingSubcontractorsYesNoPage, NormalMode, ua) mustBe
            controllers.verify.routes.SelectSubcontractorsToReverifyController.onPageLoad(NormalMode)
        }

        "must go to CheckVerificationBatchReadinessController when answer is false" in {
          val ua = emptyUserAnswers.setOrException(ReverifyExistingSubcontractorsYesNoPage, false)
          navigator.nextPage(ReverifyExistingSubcontractorsYesNoPage, NormalMode, ua) mustBe
            controllers.verify.routes.CheckVerificationBatchReadinessController.onPageLoad(NormalMode)
        }

        "must go to JourneyRecovery when answer is not present" in {
          navigator.nextPage(
            ReverifyExistingSubcontractorsYesNoPage,
            NormalMode,
            emptyUserAnswers
          ) mustBe journeyRecovery
        }
      }

      "ContractorEmailConfirmationStoredPage" - {

        "must go to VerifyCheckYourAnswers when answer is CurrentEmail" in {
          val ua = emptyUserAnswers.setOrException(
            ContractorEmailConfirmationStoredPage,
            ContractorEmailConfirmationStored.CurrentEmail
          )
          navigator.nextPage(ContractorEmailConfirmationStoredPage, NormalMode, ua) mustBe cya
        }

        "must go to EmailAddressController when answer is DifferentEmail" in {
          val ua = emptyUserAnswers.setOrException(
            ContractorEmailConfirmationStoredPage,
            ContractorEmailConfirmationStored.DifferentEmail
          )
          navigator.nextPage(ContractorEmailConfirmationStoredPage, NormalMode, ua) mustBe
            controllers.verify.routes.EmailAddressController.onPageLoad(NormalMode)
        }

        "must go to VerifyCheckYourAnswers when answer is DoNotSend" in {
          val ua = emptyUserAnswers.setOrException(
            ContractorEmailConfirmationStoredPage,
            ContractorEmailConfirmationStored.DoNotSend
          )
          navigator.nextPage(ContractorEmailConfirmationStoredPage, NormalMode, ua) mustBe cya
        }

        "must go to JourneyRecovery when answer is not present" in {
          navigator.nextPage(ContractorEmailConfirmationStoredPage, NormalMode, emptyUserAnswers) mustBe journeyRecovery
        }
      }

      "SelectSubcontractorsToReverifyPage" - {

        "must go to CheckVerificationBatchReadinessController in NormalMode" in {
          navigator.nextPage(SelectSubcontractorsToReverifyPage, NormalMode, emptyUserAnswers) mustBe
            controllers.verify.routes.CheckVerificationBatchReadinessController.onPageLoad(NormalMode)
        }
      }

      "must go to VerifyCheckYourAnswers from EmailAddressPage in NormalMode" in {
        val ua = emptyUserAnswers.setOrException(EmailAddressPage, "test@test.com")
        navigator.nextPage(EmailAddressPage, NormalMode, ua) mustBe cya
      }
    }

    "in Check mode" - {

      "must go from a page that doesn't exist in the route map to JourneyRecovery" in {
        case object UnknownPage extends Page

        navigator.nextPage(UnknownPage, CheckMode, UserAnswers("id")) mustBe journeyRecovery
      }

      "ContractorEmailConfirmationNotStoredPage" - {

        "must go to EmailAddressController in CheckMode when answer is true" in {
          val ua = emptyUserAnswers.setOrException(ContractorEmailConfirmationNotStoredPage, true)
          navigator.nextPage(ContractorEmailConfirmationNotStoredPage, CheckMode, ua) mustBe
            controllers.verify.routes.EmailAddressController.onPageLoad(CheckMode)
        }

        "must go to VerifyCheckYourAnswers when answer is false" in {
          val ua = emptyUserAnswers.setOrException(ContractorEmailConfirmationNotStoredPage, false)
          navigator.nextPage(ContractorEmailConfirmationNotStoredPage, CheckMode, ua) mustBe cya
        }

        "must go to JourneyRecovery when answer is not present" in {
          navigator.nextPage(
            ContractorEmailConfirmationNotStoredPage,
            CheckMode,
            emptyUserAnswers
          ) mustBe journeyRecovery
        }
      }

      "SelectSubcontractorPage" - {

        "must go to CheckVerificationBatchReadinessController in CheckMode" in {
          navigator.nextPage(SelectSubcontractorPage, CheckMode, emptyUserAnswers) mustBe
            controllers.verify.routes.CheckVerificationBatchReadinessController.onPageLoad(CheckMode)
        }
      }

      "ReverifyExistingSubcontractorsYesNoPage" - {

        "must go to SelectSubcontractorsToReverifyController in CheckMode when answer is true" in {
          val ua = emptyUserAnswers.setOrException(ReverifyExistingSubcontractorsYesNoPage, true)
          navigator.nextPage(ReverifyExistingSubcontractorsYesNoPage, CheckMode, ua) mustBe
            controllers.verify.routes.SelectSubcontractorsToReverifyController.onPageLoad(CheckMode)
        }

        "must go to VerifyCheckYourAnswers when answer is false" in {
          val ua = emptyUserAnswers.setOrException(ReverifyExistingSubcontractorsYesNoPage, false)
          navigator.nextPage(ReverifyExistingSubcontractorsYesNoPage, CheckMode, ua) mustBe cya
        }

        "must go to JourneyRecovery when answer is not present" in {
          navigator.nextPage(
            ReverifyExistingSubcontractorsYesNoPage,
            CheckMode,
            emptyUserAnswers
          ) mustBe journeyRecovery
        }
      }

      "ContractorEmailConfirmationStoredPage" - {

        "must go to VerifyCheckYourAnswers when answer is CurrentEmail" in {
          val ua = emptyUserAnswers.setOrException(
            ContractorEmailConfirmationStoredPage,
            ContractorEmailConfirmationStored.CurrentEmail
          )
          navigator.nextPage(ContractorEmailConfirmationStoredPage, CheckMode, ua) mustBe cya
        }

        "must go to EmailAddressController in CheckMode when answer is DifferentEmail" in {
          val ua = emptyUserAnswers.setOrException(
            ContractorEmailConfirmationStoredPage,
            ContractorEmailConfirmationStored.DifferentEmail
          )
          navigator.nextPage(ContractorEmailConfirmationStoredPage, CheckMode, ua) mustBe
            controllers.verify.routes.EmailAddressController.onPageLoad(CheckMode)
        }

        "must go to VerifyCheckYourAnswers when answer is DoNotSend" in {
          val ua = emptyUserAnswers.setOrException(
            ContractorEmailConfirmationStoredPage,
            ContractorEmailConfirmationStored.DoNotSend
          )
          navigator.nextPage(ContractorEmailConfirmationStoredPage, CheckMode, ua) mustBe cya
        }

        "must go to JourneyRecovery when answer is not present" in {
          navigator.nextPage(ContractorEmailConfirmationStoredPage, CheckMode, emptyUserAnswers) mustBe journeyRecovery
        }
      }

      "must go to VerifyCheckYourAnswers from SelectSubcontractorsToReverifyPage in CheckMode" in {
        navigator.nextPage(SelectSubcontractorsToReverifyPage, CheckMode, emptyUserAnswers) mustBe cya
      }

      "must go to VerifyCheckYourAnswers from EmailAddressPage in CheckMode" in {
        val ua = emptyUserAnswers.setOrException(EmailAddressPage, "test@test.com")
        navigator.nextPage(EmailAddressPage, CheckMode, ua) mustBe cya
      }
    }
  }
}
