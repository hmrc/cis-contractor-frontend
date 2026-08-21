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
import models.response.GetNewestVerificationBatchResponse
import models.verify.{ContractorEmailConfirmationStored, SelectedSubcontractors}
import models.{AmendMode, CheckMode, Mode, NormalMode, Subcontractor, SubcontractorViewModel, UserAnswers}
import pages.Page
import pages.insufficient.ProceedInsufficientSubcontractorNameYesNoPage
import pages.verify.*

class VerifyNavigatorSpec extends SpecBase {

  private val navigator = new VerifyNavigator()

  private lazy val journeyRecovery =
    routes.JourneyRecoveryController.onPageLoad()

  private lazy val cya =
    controllers.verify.routes.VerifyCheckYourAnswersController.onPageLoad()

  private def currentVerificationBatch(mode: Mode) =
    controllers.verify.routes.CurrentVerificationBatchController.onPageLoad(mode)

  private lazy val noSubcontractorsSelectedWarningCheckMode =
    controllers.verify.routes.NoSubcontractorsSelectedWarningController.onPageLoadCheckMode()

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

        "must go to VerifyCheckYourAnswerController when answer is false" in {
          val ua = emptyUserAnswers.setOrException(ContractorEmailConfirmationNotStoredPage, false)

          navigator.nextPage(
            ContractorEmailConfirmationNotStoredPage,
            NormalMode,
            ua
          ) mustBe cya
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

        "must go to ReverifyExistingSubcontractorsYesNoController in NormalMode when there are verified subcontractors" in {

          val ua = emptyUserAnswers
            .set(
              NewestVerificationBatchResponsePage,
              GetNewestVerificationBatchResponse(
                scheme = None,
                subcontractors = Seq(
                  Subcontractor(
                    subcontractorId = 1L,
                    firstName = None,
                    secondName = None,
                    surname = None,
                    tradingName = None,
                    partnershipTradingName = None,
                    verified = Some("Y"),
                    verificationNumber = None,
                    taxTreatment = None,
                    verificationDate = None,
                    lastMonthlyReturnDate = None,
                    createDate = None,
                    subcontractorType = None,
                    subbieResourceRef = None,
                    utr = None,
                    partnerUtr = None,
                    crn = None,
                    nino = None
                  )
                ),
                verificationBatch = None,
                verifications = Seq.empty,
                submission = None,
                monthlyReturn = None,
                monthlyReturnSubmission = None
              )
            )
            .success
            .value

          navigator.nextPage(SelectSubcontractorPage, NormalMode, ua) mustBe
            controllers.verify.routes.ReverifyExistingSubcontractorsYesNoController.onPageLoad(NormalMode)
        }

        "must go to CurrentVerificationBatchController in NormalMode when there are no verified subcontractors" in {

          val ua = emptyUserAnswers
            .set(
              NewestVerificationBatchResponsePage,
              GetNewestVerificationBatchResponse(
                scheme = None,
                subcontractors = Seq(
                  Subcontractor(
                    subcontractorId = 1L,
                    firstName = None,
                    secondName = None,
                    surname = None,
                    tradingName = None,
                    partnershipTradingName = None,
                    verified = Some("N"),
                    verificationNumber = None,
                    taxTreatment = None,
                    verificationDate = None,
                    lastMonthlyReturnDate = None,
                    createDate = None,
                    subcontractorType = None,
                    subbieResourceRef = None,
                    utr = None,
                    partnerUtr = None,
                    crn = None,
                    nino = None
                  )
                ),
                verificationBatch = None,
                verifications = Seq.empty,
                submission = None,
                monthlyReturn = None,
                monthlyReturnSubmission = None
              )
            )
            .success
            .value

          navigator.nextPage(SelectSubcontractorPage, NormalMode, ua) mustBe
            currentVerificationBatch(NormalMode)
        }

        "must go to CheckVerificationBatchReadinessController in CheckMode when selections exist" in {
          val ua =
            emptyUserAnswers
              .set(
                SelectSubcontractorPage,
                Set(SubcontractorViewModel("1", "Test Subcontractor"))
              )
              .success
              .value

          navigator.nextPage(SelectSubcontractorPage, CheckMode, ua) mustBe
            controllers.verify.routes.CurrentVerificationBatchController
              .onPageLoad(CheckMode)
        }

        "must go to ReverifyExistingSubcontractorsYesNoController in CheckMode when rebuildVerificationFromWarning is true" in {
          val ua =
            emptyUserAnswers
              .set(
                SelectSubcontractorPage,
                Set(SubcontractorViewModel("1", "Test Subcontractor"))
              )
              .success
              .value
              .set(RebuildVerificationFromWarningPage, true)
              .success
              .value

          navigator.nextPage(SelectSubcontractorPage, CheckMode, ua) mustBe
            controllers.verify.routes.ReverifyExistingSubcontractorsYesNoController.onPageLoad(CheckMode)
        }

        "must go to NoSubcontractorsSelectedWarningController in CheckMode when no selections exist" in {
          navigator.nextPage(SelectSubcontractorPage, CheckMode, emptyUserAnswers) mustBe
            noSubcontractorsSelectedWarningCheckMode
        }
      }

      "ReverifyExistingSubcontractorsYesNoPage" - {

        "must go to SelectSubcontractorsToReverifyController when answer is true (NormalMode)" in {

          val ua =
            emptyUserAnswers
              .set(ReverifyExistingSubcontractorsYesNoPage, true)
              .success
              .value

          navigator.nextPage(ReverifyExistingSubcontractorsYesNoPage, NormalMode, ua) mustBe
            controllers.verify.routes.SelectSubcontractorsToReverifyController.onPageLoad(NormalMode)
        }

        "must go to CurrentVerificationBatchController when answer is false and selections exist (NormalMode)" in {

          val ua =
            emptyUserAnswers
              .set(ReverifyExistingSubcontractorsYesNoPage, false)
              .success
              .value
              .set(
                SelectSubcontractorPage,
                Set(SubcontractorViewModel("1", "Test Subcontractor"))
              )
              .success
              .value

          navigator.nextPage(ReverifyExistingSubcontractorsYesNoPage, NormalMode, ua) mustBe
            currentVerificationBatch(NormalMode)
        }

        "must go to NoSubcontractorsSelectedWarningController when answer is false and no selections exist (NormalMode)" in {

          val ua =
            emptyUserAnswers
              .set(ReverifyExistingSubcontractorsYesNoPage, false)
              .success
              .value

          navigator.nextPage(ReverifyExistingSubcontractorsYesNoPage, NormalMode, ua) mustBe
            controllers.verify.routes.NoSubcontractorsSelectedWarningController.onPageLoad()
        }
      }

      "VerifyYourSubcontractorsYesNoPage" - {

        "must go to SelectSubcontractorsToReverifyController when answer is true in NormalMode" in {

          val ua =
            emptyUserAnswers
              .set(VerifyYourSubcontractorsYesNoPage, true)
              .success
              .value

          navigator.nextPage(VerifyYourSubcontractorsYesNoPage, NormalMode, ua) mustBe
            controllers.verify.routes.SelectSubcontractorsToReverifyController.onPageLoad(NormalMode)
        }

        "must go to IndexController when answer is false in NormalMode" in {

          val ua =
            emptyUserAnswers
              .set(VerifyYourSubcontractorsYesNoPage, false)
              .success
              .value

          navigator.nextPage(VerifyYourSubcontractorsYesNoPage, NormalMode, ua) mustBe
            controllers.routes.IndexController.onPageLoad()
        }
      }

      "ContractorEmailConfirmationStoredPage" - {

        "must go to VerifyCheckYourAnswersController when answer is CurrentEmail" in {
          val ua = emptyUserAnswers.setOrException(
            ContractorEmailConfirmationStoredPage,
            ContractorEmailConfirmationStored.CurrentEmail
          )

          navigator.nextPage(
            ContractorEmailConfirmationStoredPage,
            NormalMode,
            ua
          ) mustBe cya
        }

        "must go to EmailAddressController when answer is DifferentEmail" in {
          val ua = emptyUserAnswers.setOrException(
            ContractorEmailConfirmationStoredPage,
            ContractorEmailConfirmationStored.DifferentEmail
          )
          navigator.nextPage(ContractorEmailConfirmationStoredPage, NormalMode, ua) mustBe
            controllers.verify.routes.EmailAddressController.onPageLoad(NormalMode)
        }

        "must go to VerifyCheckYourAnswersController when answer is DoNotSend" in {
          val ua = emptyUserAnswers.setOrException(
            ContractorEmailConfirmationStoredPage,
            ContractorEmailConfirmationStored.DoNotSend
          )

          navigator.nextPage(
            ContractorEmailConfirmationStoredPage,
            NormalMode,
            ua
          ) mustBe cya
        }

        "must go to JourneyRecovery when answer is not present" in {
          navigator.nextPage(ContractorEmailConfirmationStoredPage, NormalMode, emptyUserAnswers) mustBe journeyRecovery
        }
      }

      "SelectSubcontractorsToReverifyPage" - {

        "must go to CurrentVerificationBatchController when selections exist in SelectSubcontractorPage (NormalMode)" in {

          val ua =
            emptyUserAnswers
              .set(
                SelectSubcontractorPage,
                Set(SubcontractorViewModel("1", "Test Subcontractor"))
              )
              .success
              .value

          navigator.nextPage(SelectSubcontractorsToReverifyPage, NormalMode, ua) mustBe
            currentVerificationBatch(NormalMode)
        }

        "must go to CurrentVerificationBatchController when selections exist in SelectSubcontractorsToReverifyPage (NormalMode)" in {

          val ua =
            emptyUserAnswers
              .set(
                SelectSubcontractorsToReverifyPage,
                Set(SelectedSubcontractors("2", "Reverify Subcontractor"))
              )
              .success
              .value

          navigator.nextPage(SelectSubcontractorsToReverifyPage, NormalMode, ua) mustBe
            currentVerificationBatch(NormalMode)
        }

        "must go to NoSubcontractorsSelectedWarningController when no selections exist (NormalMode)" in {

          navigator.nextPage(
            SelectSubcontractorsToReverifyPage,
            NormalMode,
            emptyUserAnswers
          ) mustBe controllers.verify.routes.NoSubcontractorsSelectedWarningController.onPageLoad()
        }
      }

      "must go to VerifyCheckYourAnswers from EmailAddressPage in NormalMode" in {
        val ua = emptyUserAnswers.setOrException(EmailAddressPage, "test@test.com")

        navigator.nextPage(
          EmailAddressPage,
          NormalMode,
          ua
        ) mustBe cya
      }

      "ProceedInsufficientSubcontractorNameYesNoPage" - {

        "must go to ProceedInsufficientSubcontractorNameYesNoPage when answer is true" in {

          val ua = emptyUserAnswers
            .set(ProceedInsufficientSubcontractorNameYesNoPage, true)
            .success
            .value

          navigator.nextPage(ProceedInsufficientSubcontractorNameYesNoPage, NormalMode, ua) mustBe
            controllers.verify.routes.ReviewInsufficientInfoSubcontractorsController
              .onPageLoad()
        }

        "must go to ProceedInsufficientSubcontractorNameYesNoPage when answer is false" in {

          val ua = emptyUserAnswers
            .set(ProceedInsufficientSubcontractorNameYesNoPage, false)
            .success
            .value

          navigator.nextPage(ProceedInsufficientSubcontractorNameYesNoPage, NormalMode, ua) mustBe
            controllers.verify.routes.ReviewInsufficientInfoSubcontractorsController
              .onPageLoad()
        }
      }
    }

    "in Amend mode" - {

      "must go from any page to JourneyRecovery" in {
        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, AmendMode, UserAnswers("id")) mustBe journeyRecovery
      }

      "must go from SelectSubcontractorPage to JourneyRecovery" in {
        navigator.nextPage(SelectSubcontractorPage, AmendMode, emptyUserAnswers) mustBe journeyRecovery
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

        "must go to VerifyCheckYourAnswersController when answer is false" in {
          val ua = emptyUserAnswers.setOrException(ContractorEmailConfirmationNotStoredPage, false)

          navigator.nextPage(
            ContractorEmailConfirmationNotStoredPage,
            CheckMode,
            ua
          ) mustBe cya
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

        "must go to CurrentVerificationBatchController in CheckMode when selections exist" in {
          val ua =
            emptyUserAnswers
              .set(
                SelectSubcontractorPage,
                Set(SubcontractorViewModel("1", "Test Subcontractor"))
              )
              .success
              .value

          navigator.nextPage(
            SelectSubcontractorPage,
            CheckMode,
            ua
          ) mustBe
            currentVerificationBatch(CheckMode)
        }

        "must go to NoSubcontractorsSelectedWarningController in CheckMode when no selections exist" in {
          navigator.nextPage(
            SelectSubcontractorPage,
            CheckMode,
            emptyUserAnswers
          ) mustBe
            noSubcontractorsSelectedWarningCheckMode
        }

        "must go to CurrentVerificationBatchController in CheckMode when no new subcontractors are selected but reverify selections exist" in {
          val ua =
            emptyUserAnswers
              .set(
                SelectSubcontractorsToReverifyPage,
                Set(SelectedSubcontractors("1", "Test Subcontractor"))
              )
              .success
              .value

          navigator.nextPage(
            SelectSubcontractorPage,
            CheckMode,
            ua
          ) mustBe
            currentVerificationBatch(CheckMode)
        }

        "must go to ReverifyExistingSubcontractorsYesNoController in CheckMode when subcontractor is coming through the warning" in {
          val ua =
            emptyUserAnswers
              .set(
                RebuildVerificationFromWarningPage,
                true
              )
              .success
              .value

          navigator.nextPage(
            SelectSubcontractorPage,
            CheckMode,
            ua
          ) mustBe
            controllers.verify.routes.ReverifyExistingSubcontractorsYesNoController
              .onPageLoad(CheckMode)
        }
      }

      "ReverifyExistingSubcontractorsYesNoPage" - {

        "must go to CheckVerificationBatchReadinessController when answer is true and reverify selections exist (CheckMode)" in {

          val ua =
            emptyUserAnswers
              .set(ReverifyExistingSubcontractorsYesNoPage, true)
              .success
              .value
              .set(
                SelectSubcontractorsToReverifyPage,
                Set(SelectedSubcontractors("1", "Test Subcontractor"))
              )
              .success
              .value

          navigator.nextPage(
            ReverifyExistingSubcontractorsYesNoPage,
            CheckMode,
            ua
          ) mustBe
            controllers.verify.routes.CheckVerificationBatchReadinessController
              .checkVerificationBatchReadiness(CheckMode)
        }

        "must go to SelectSubcontractorsToReverifyController when answer is true and no reverify selections exist (CheckMode)" in {

          val ua =
            emptyUserAnswers
              .set(ReverifyExistingSubcontractorsYesNoPage, true)
              .success
              .value

          navigator.nextPage(
            ReverifyExistingSubcontractorsYesNoPage,
            CheckMode,
            ua
          ) mustBe
            controllers.verify.routes.SelectSubcontractorsToReverifyController
              .onPageLoad(CheckMode)
        }

        "must go to NoSubcontractorsSelectedWarningController when answer is false and no selections exist (CheckMode)" in {

          val ua =
            emptyUserAnswers
              .set(ReverifyExistingSubcontractorsYesNoPage, false)
              .success
              .value

          navigator.nextPage(
            ReverifyExistingSubcontractorsYesNoPage,
            CheckMode,
            ua
          ) mustBe
            noSubcontractorsSelectedWarningCheckMode
        }

        "must go to CurrentVerificationBatchController when answer is false and selections exist (CheckMode)" in {

          val ua =
            emptyUserAnswers
              .set(ReverifyExistingSubcontractorsYesNoPage, false)
              .success
              .value
              .set(
                SelectSubcontractorPage,
                Set(SubcontractorViewModel("1", "Test Subcontractor"))
              )
              .success
              .value

          navigator.nextPage(
            ReverifyExistingSubcontractorsYesNoPage,
            CheckMode,
            ua
          ) mustBe
            controllers.verify.routes.CurrentVerificationBatchController
              .onPageLoad(CheckMode)
        }
      }

      "must go to VerifyCheckYourAnswers from EmailAddressPage in CheckMode" in {
        val ua = emptyUserAnswers.setOrException(EmailAddressPage, "test@test.com")

        navigator.nextPage(
          EmailAddressPage,
          CheckMode,
          ua
        ) mustBe cya
      }

      "must go to VerifyCheckYourAnswers from VerificationDeclarationPage in CheckMode" in {
        navigator.nextPage(
          VerificationDeclarationPage,
          CheckMode,
          emptyUserAnswers
        ) mustBe cya
      }

      "VerifyYourSubcontractorsYesNoPage" - {

        "must go to SelectSubcontractorsToReverifyController when answer is true in CheckMode" in {

          val ua =
            emptyUserAnswers
              .set(VerifyYourSubcontractorsYesNoPage, true)
              .success
              .value

          navigator.nextPage(VerifyYourSubcontractorsYesNoPage, CheckMode, ua) mustBe
            controllers.verify.routes.SelectSubcontractorsToReverifyController.onPageLoad(CheckMode)
        }

        "must go to NoSubcontractorsSelectedWarningController when answer is false in CheckMode" in {

          val ua =
            emptyUserAnswers
              .set(VerifyYourSubcontractorsYesNoPage, false)
              .success
              .value

          navigator.nextPage(VerifyYourSubcontractorsYesNoPage, CheckMode, ua) mustBe
            noSubcontractorsSelectedWarningCheckMode
        }
      }

      "ContractorEmailConfirmationStoredPage" - {

        "must go to VerifyCheckYourAnswersController when answer is CurrentEmail" in {
          val ua = emptyUserAnswers.setOrException(
            ContractorEmailConfirmationStoredPage,
            ContractorEmailConfirmationStored.CurrentEmail
          )

          navigator.nextPage(
            ContractorEmailConfirmationStoredPage,
            CheckMode,
            ua
          ) mustBe cya
        }

        "must go to EmailAddressController in CheckMode when answer is DifferentEmail" in {
          val ua = emptyUserAnswers.setOrException(
            ContractorEmailConfirmationStoredPage,
            ContractorEmailConfirmationStored.DifferentEmail
          )
          navigator.nextPage(ContractorEmailConfirmationStoredPage, CheckMode, ua) mustBe
            controllers.verify.routes.EmailAddressController.onPageLoad(CheckMode)
        }

        "must go to VerifyCheckYourAnswersController when answer is DoNotSend" in {
          val ua = emptyUserAnswers.setOrException(
            ContractorEmailConfirmationStoredPage,
            ContractorEmailConfirmationStored.DoNotSend
          )

          navigator.nextPage(
            ContractorEmailConfirmationStoredPage,
            CheckMode,
            ua
          ) mustBe cya
        }

        "must go to JourneyRecovery when answer is not present" in {
          navigator.nextPage(ContractorEmailConfirmationStoredPage, CheckMode, emptyUserAnswers) mustBe journeyRecovery
        }
      }

      "SelectSubcontractorsToReverifyPage" - {

        "must go to CurrentVerificationBatchController when selections exist in SelectSubcontractorsToReverifyPage (CheckMode)" in {

          val ua =
            emptyUserAnswers
              .set(
                SelectSubcontractorsToReverifyPage,
                Set(SelectedSubcontractors("1", "Test Subcontractor"))
              )
              .success
              .value

          navigator.nextPage(
            SelectSubcontractorsToReverifyPage,
            CheckMode,
            ua
          ) mustBe
            controllers.verify.routes.CurrentVerificationBatchController
              .onPageLoad(CheckMode)
        }

        "must go to CurrentVerificationBatchController when selections exist in SelectSubcontractorPage (CheckMode)" in {

          val ua =
            emptyUserAnswers
              .set(
                SelectSubcontractorPage,
                Set(SubcontractorViewModel("1", "Test Subcontractor"))
              )
              .success
              .value

          navigator.nextPage(
            SelectSubcontractorsToReverifyPage,
            CheckMode,
            ua
          ) mustBe
            controllers.verify.routes.CurrentVerificationBatchController
              .onPageLoad(CheckMode)
        }

        "must go to NoSubcontractorsSelectedWarningController when no selections exist in either page (CheckMode)" in {

          navigator.nextPage(
            SelectSubcontractorsToReverifyPage,
            CheckMode,
            emptyUserAnswers
          ) mustBe
            noSubcontractorsSelectedWarningCheckMode
        }
      }
    }
  }
}
