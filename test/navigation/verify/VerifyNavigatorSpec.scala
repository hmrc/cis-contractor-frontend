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
import models.{CheckMode, NormalMode, Subcontractor, SubcontractorViewModel, UserAnswers}
import models.verify.{ContractorEmailConfirmationStored, SelectedSubcontractors}
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

        "must go to VerificationDeclarationController when answer is false" in {
          val ua = emptyUserAnswers.setOrException(ContractorEmailConfirmationNotStoredPage, false)

          navigator.nextPage(
            ContractorEmailConfirmationNotStoredPage,
            NormalMode,
            ua
          ) mustBe controllers.verify.routes.VerificationDeclarationController.onPageLoad()
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

        "must go to CheckVerificationBatchReadinessController in NormalMode when there are no verified subcontractors" in {

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
            controllers.verify.routes.CheckVerificationBatchReadinessController.checkVerificationBatchReadiness()
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

        "must go to CheckVerificationBatchReadinessController when answer is false and selections exist (NormalMode)" in {

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
            controllers.verify.routes.CheckVerificationBatchReadinessController.checkVerificationBatchReadiness()
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

        "must go to VerificationDeclarationController when answer is CurrentEmail" in {
          val ua = emptyUserAnswers.setOrException(
            ContractorEmailConfirmationStoredPage,
            ContractorEmailConfirmationStored.CurrentEmail
          )

          navigator.nextPage(
            ContractorEmailConfirmationStoredPage,
            NormalMode,
            ua
          ) mustBe controllers.verify.routes.VerificationDeclarationController.onPageLoad()
        }

        "must go to EmailAddressController when answer is DifferentEmail" in {
          val ua = emptyUserAnswers.setOrException(
            ContractorEmailConfirmationStoredPage,
            ContractorEmailConfirmationStored.DifferentEmail
          )
          navigator.nextPage(ContractorEmailConfirmationStoredPage, NormalMode, ua) mustBe
            controllers.verify.routes.EmailAddressController.onPageLoad(NormalMode)
        }

        "must go to VerificationDeclarationController when answer is DoNotSend" in {
          val ua = emptyUserAnswers.setOrException(
            ContractorEmailConfirmationStoredPage,
            ContractorEmailConfirmationStored.DoNotSend
          )

          navigator.nextPage(
            ContractorEmailConfirmationStoredPage,
            NormalMode,
            ua
          ) mustBe controllers.verify.routes.VerificationDeclarationController.onPageLoad()
        }

        "must go to JourneyRecovery when answer is not present" in {
          navigator.nextPage(ContractorEmailConfirmationStoredPage, NormalMode, emptyUserAnswers) mustBe journeyRecovery
        }
      }

      "SelectSubcontractorsToReverifyPage" - {

        "must go to CheckVerificationBatchReadinessController when selections exist in SelectSubcontractorPage (NormalMode)" in {

          val ua =
            emptyUserAnswers
              .set(
                SelectSubcontractorPage,
                Set(SubcontractorViewModel("1", "Test Subcontractor"))
              )
              .success
              .value

          navigator.nextPage(SelectSubcontractorsToReverifyPage, NormalMode, ua) mustBe
            controllers.verify.routes.CheckVerificationBatchReadinessController.checkVerificationBatchReadiness()
        }

        "must go to CheckVerificationBatchReadinessController when selections exist in SelectSubcontractorsToReverifyPage (NormalMode)" in {

          val ua =
            emptyUserAnswers
              .set(
                SelectSubcontractorsToReverifyPage,
                Set(SelectedSubcontractors("2", "Reverify Subcontractor"))
              )
              .success
              .value

          navigator.nextPage(SelectSubcontractorsToReverifyPage, NormalMode, ua) mustBe
            controllers.verify.routes.CheckVerificationBatchReadinessController.checkVerificationBatchReadiness()
        }

        "must go to NoSubcontractorsSelectedWarningController when no selections exist (NormalMode)" in {

          navigator.nextPage(
            SelectSubcontractorsToReverifyPage,
            NormalMode,
            emptyUserAnswers
          ) mustBe controllers.verify.routes.NoSubcontractorsSelectedWarningController.onPageLoad()
        }
      }

      "must go to VerificationDeclarationController from EmailAddressPage in NormalMode" in {
        val ua = emptyUserAnswers.setOrException(EmailAddressPage, "test@test.com")

        navigator.nextPage(
          EmailAddressPage,
          NormalMode,
          ua
        ) mustBe controllers.verify.routes.VerificationDeclarationController.onPageLoad()
      }

      "must go to VerifyCheckYourAnswers from VerificationDeclarationPage in NormalMode" in {
        navigator.nextPage(
          VerificationDeclarationPage,
          NormalMode,
          emptyUserAnswers
        ) mustBe cya
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

        "must go to VerificationDeclarationController when answer is false" in {
          val ua = emptyUserAnswers.setOrException(ContractorEmailConfirmationNotStoredPage, false)

          navigator.nextPage(
            ContractorEmailConfirmationNotStoredPage,
            CheckMode,
            ua
          ) mustBe controllers.verify.routes.VerificationDeclarationController.onPageLoad()
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
            controllers.verify.routes.CheckVerificationBatchReadinessController.checkVerificationBatchReadiness()
        }
      }

      "ReverifyExistingSubcontractorsYesNoPage" - {

        "must go to SelectSubcontractorsToReverifyController when answer is true (CheckMode)" in {

          val ua =
            emptyUserAnswers
              .set(ReverifyExistingSubcontractorsYesNoPage, true)
              .success
              .value

          navigator.nextPage(ReverifyExistingSubcontractorsYesNoPage, CheckMode, ua) mustBe
            controllers.verify.routes.SelectSubcontractorsToReverifyController.onPageLoad(CheckMode)
        }

        "must go to VerifyCheckYourAnswersController when answer is false and selections exist (CheckMode)" in {

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

          navigator.nextPage(ReverifyExistingSubcontractorsYesNoPage, CheckMode, ua) mustBe
            controllers.verify.routes.VerifyCheckYourAnswersController.onPageLoad()
        }

        "must go to NoSubcontractorsSelectedWarningController when answer is false and no selections exist (CheckMode)" in {

          val ua =
            emptyUserAnswers
              .set(ReverifyExistingSubcontractorsYesNoPage, false)
              .success
              .value

          navigator.nextPage(ReverifyExistingSubcontractorsYesNoPage, CheckMode, ua) mustBe
            controllers.verify.routes.NoSubcontractorsSelectedWarningController.onPageLoad()
        }
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
            controllers.verify.routes.NoSubcontractorsSelectedWarningController.onPageLoad()
        }

      }

      "ContractorEmailConfirmationStoredPage" - {

        "must go to VerificationDeclarationController when answer is CurrentEmail" in {
          val ua = emptyUserAnswers.setOrException(
            ContractorEmailConfirmationStoredPage,
            ContractorEmailConfirmationStored.CurrentEmail
          )

          navigator.nextPage(
            ContractorEmailConfirmationStoredPage,
            CheckMode,
            ua
          ) mustBe controllers.verify.routes.VerificationDeclarationController.onPageLoad()
        }

        "must go to EmailAddressController in CheckMode when answer is DifferentEmail" in {
          val ua = emptyUserAnswers.setOrException(
            ContractorEmailConfirmationStoredPage,
            ContractorEmailConfirmationStored.DifferentEmail
          )
          navigator.nextPage(ContractorEmailConfirmationStoredPage, CheckMode, ua) mustBe
            controllers.verify.routes.EmailAddressController.onPageLoad(CheckMode)
        }

        "must go to VerificationDeclarationController when answer is DoNotSend" in {
          val ua = emptyUserAnswers.setOrException(
            ContractorEmailConfirmationStoredPage,
            ContractorEmailConfirmationStored.DoNotSend
          )

          navigator.nextPage(
            ContractorEmailConfirmationStoredPage,
            CheckMode,
            ua
          ) mustBe controllers.verify.routes.VerificationDeclarationController.onPageLoad()
        }

        "must go to JourneyRecovery when answer is not present" in {
          navigator.nextPage(ContractorEmailConfirmationStoredPage, CheckMode, emptyUserAnswers) mustBe journeyRecovery
        }
      }

      "SelectSubcontractorsToReverifyPage" - {

        "must go to VerifyCheckYourAnswersController when selections exist in SelectSubcontractorPage (CheckMode)" in {

          val ua =
            emptyUserAnswers
              .set(
                SelectSubcontractorPage,
                Set(SubcontractorViewModel("1", "Test Subcontractor"))
              )
              .success
              .value

          navigator.nextPage(SelectSubcontractorsToReverifyPage, CheckMode, ua) mustBe
            controllers.verify.routes.VerifyCheckYourAnswersController.onPageLoad()
        }

        "must go to VerifyCheckYourAnswersController when selections exist in SelectSubcontractorsToReverifyPage (CheckMode)" in {

          val ua =
            emptyUserAnswers
              .set(
                SelectSubcontractorsToReverifyPage,
                Set(SelectedSubcontractors("2", "Reverify Subcontractor"))
              )
              .success
              .value

          navigator.nextPage(SelectSubcontractorsToReverifyPage, CheckMode, ua) mustBe
            controllers.verify.routes.VerifyCheckYourAnswersController.onPageLoad()
        }

        "must go to NoSubcontractorsSelectedWarningController when no selections exist (CheckMode)" in {

          navigator.nextPage(
            SelectSubcontractorsToReverifyPage,
            CheckMode,
            emptyUserAnswers
          ) mustBe controllers.verify.routes.NoSubcontractorsSelectedWarningController.onPageLoad()
        }
      }

      "must go to VerificationDeclarationController from EmailAddressPage in CheckMode" in {
        val ua = emptyUserAnswers.setOrException(EmailAddressPage, "test@test.com")

        navigator.nextPage(
          EmailAddressPage,
          CheckMode,
          ua
        ) mustBe controllers.verify.routes.VerificationDeclarationController.onPageLoad()
      }

      "must go to VerifyCheckYourAnswers from VerificationDeclarationPage in CheckMode" in {
        navigator.nextPage(
          VerificationDeclarationPage,
          CheckMode,
          emptyUserAnswers
        ) mustBe cya
      }
    }
  }
}
