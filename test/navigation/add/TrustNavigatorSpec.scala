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

import base.SpecBase
import controllers.routes
import models.contact.ContactOptions
import models.{CheckMode, NormalMode, UserAnswers}
import org.scalactic.Prettifier.default
import pages.Page
import pages.add.trust.*

class TrustNavigatorSpec extends SpecBase {

  val navigator                    = new TrustNavigator
  private lazy val journeyRecovery = routes.JourneyRecoveryController.onPageLoad()
  private lazy val trustCYA        = controllers.add.trust.routes.TrustCheckYourAnswersController.onPageLoad()

  "TrustNavigator" - {

    "in Normal mode" - {

      "must go from a page that doesn't exist in the route map to Index" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, NormalMode, UserAnswers("id")) mustBe routes.IndexController.onPageLoad()

      }

      "must go from TrustEmailAddressPage to TrustUtrYesNoController" in {
        val ua = emptyUserAnswers.set(TrustEmailAddressPage, "test@test.com").success.value
        navigator.nextPage(TrustEmailAddressPage, NormalMode, ua) mustBe
          controllers.add.trust.routes.TrustUtrYesNoController.onPageLoad(NormalMode)
      }

      "must go from TrustNamePage to TrustAddressYesNoPage" in {
        navigator.nextPage(TrustNamePage, NormalMode, UserAnswers("id")) mustBe
          controllers.add.trust.routes.TrustAddressYesNoController.onPageLoad(NormalMode)
      }

      "must go from a TrustAddressYesNoPage to TrustAddressPage when true" in {
        navigator.nextPage(
          TrustAddressYesNoPage,
          NormalMode,
          emptyUserAnswers.setOrException(TrustAddressYesNoPage, true)
        ) mustBe controllers.add.trust.routes.TrustAddressController.onPageLoad(NormalMode)
      }

      "must go from a TrustAddressYesNoPage to TrustContactOptions when false" in {
        navigator.nextPage(
          TrustAddressYesNoPage,
          NormalMode,
          emptyUserAnswers.setOrException(TrustAddressYesNoPage, false)
        ) mustBe controllers.add.trust.routes.TrustContactOptionsController.onPageLoad(NormalMode)
      }

      "must go from a TrustAddressYesNoPage to journey recovery when incomplete info provided" in {
        navigator.nextPage(
          TrustAddressYesNoPage,
          NormalMode,
          emptyUserAnswers
        ) mustBe journeyRecovery
      }

      "must go from TrustAddressPage to TrustContactOptionsController" in {
        navigator.nextPage(TrustAddressPage, NormalMode, UserAnswers("id")) mustBe
          controllers.add.trust.routes.TrustContactOptionsController.onPageLoad(NormalMode)
      }

      "must go from TrustContactOptionsPage" - {
        "to itself when Email is selected" in {
          navigator.nextPage(
            TrustContactOptionsPage,
            NormalMode,
            emptyUserAnswers.setOrException(
              TrustContactOptionsPage,
              ContactOptions.Email
            )
          ) mustBe controllers.add.trust.routes.TrustEmailAddressController.onPageLoad(NormalMode)
        }

        "to itself when Phone is selected" in {
          navigator.nextPage(
            TrustContactOptionsPage,
            NormalMode,
            emptyUserAnswers.setOrException(
              TrustContactOptionsPage,
              ContactOptions.Phone
            )
          ) mustBe controllers.add.trust.routes.TrustPhoneNumberController.onPageLoad(NormalMode)
        }

        "to itself when Mobile is selected" in {
          navigator.nextPage(
            TrustContactOptionsPage,
            NormalMode,
            emptyUserAnswers.setOrException(
              TrustContactOptionsPage,
              ContactOptions.Mobile
            )
          ) mustBe controllers.add.trust.routes.TrustMobileNumberController.onPageLoad(NormalMode)
        }

        "to itself when NoDetails is selected" in {
          navigator.nextPage(
            TrustContactOptionsPage,
            NormalMode,
            emptyUserAnswers.setOrException(
              TrustContactOptionsPage,
              ContactOptions.NoDetails
            )
          ) mustBe controllers.add.trust.routes.TrustUtrYesNoController.onPageLoad(NormalMode)
        }

        "to JourneyRecoveryPage when answer is not present" in {
          navigator.nextPage(
            TrustContactOptionsPage,
            NormalMode,
            emptyUserAnswers
          ) mustBe journeyRecovery
        }
      }

      "must go from TrustUtrYesNoPage to TrustUtrController when answer is true" in {
        val ua = UserAnswers("id").set(TrustUtrYesNoPage, true).success.value
        navigator.nextPage(TrustUtrYesNoPage, NormalMode, ua) mustBe
          controllers.add.trust.routes.TrustUtrController.onPageLoad(NormalMode)
      }

      "must go from TrustUtrYesNoPage to TrustWorksReferenceYesNoController when answer is false" in {
        val ua = UserAnswers("id").set(TrustUtrYesNoPage, false).success.value
        navigator.nextPage(TrustUtrYesNoPage, NormalMode, ua) mustBe
          controllers.add.trust.routes.TrustWorksReferenceYesNoController.onPageLoad(NormalMode)
      }

      "must go from TrustUtrYesNoPage to JourneyRecovery when no answer is present" in {
        navigator.nextPage(TrustUtrYesNoPage, NormalMode, UserAnswers("id")) mustBe
          routes.JourneyRecoveryController.onPageLoad()
      }

      "must go from a TrustPhoneNumberPage to TrustUtrYesNoPage" in {
        navigator.nextPage(
          TrustPhoneNumberPage,
          NormalMode,
          UserAnswers("id")
        ) mustBe controllers.add.trust.routes.TrustUtrYesNoController.onPageLoad(NormalMode)
      }

      "must go from TrustWorksReferenceYesNo" - {
        "to next page when answer is Yes" in {
          navigator.nextPage(
            TrustWorksReferenceYesNoPage,
            NormalMode,
            emptyUserAnswers.setOrException(TrustWorksReferenceYesNoPage, true)
          ) mustBe controllers.add.trust.routes.TrustWorksReferenceController
            .onPageLoad(NormalMode)
        }

        "to next page when answer is No" in {
          navigator.nextPage(
            TrustWorksReferenceYesNoPage,
            NormalMode,
            emptyUserAnswers.setOrException(TrustWorksReferenceYesNoPage, false)
          ) mustBe controllers.add.trust.routes.TrustCheckYourAnswersController
            .onPageLoad()
        }

        "to JourneyRecoveryPage when answer is not present" in {
          navigator.nextPage(
            TrustWorksReferenceYesNoPage,
            NormalMode,
            emptyUserAnswers
          ) mustBe routes.JourneyRecoveryController.onPageLoad()
        }
      }

      "must go from a TrustMobileNumberPage to TrustUtrYesNoPage" in {
        navigator.nextPage(
          TrustMobileNumberPage,
          NormalMode,
          UserAnswers("id")
        ) mustBe controllers.add.trust.routes.TrustUtrYesNoController.onPageLoad(NormalMode)
      }

    }

    "in Check mode" - {

      "must go from TrustEmailAddressPage to TrustCheckYourAnswers in CheckMode" in {
        val ua = emptyUserAnswers.set(TrustEmailAddressPage, "test@test.com").success.value

        navigator.nextPage(TrustEmailAddressPage, CheckMode, ua) mustBe
          controllers.add.trust.routes.TrustCheckYourAnswersController.onPageLoad()
      }

      "must go from TrustNamePage to TrustCheckYourAnswers" in {
        navigator.nextPage(TrustNamePage, CheckMode, UserAnswers("id")) mustBe
          controllers.add.trust.routes.TrustCheckYourAnswersController.onPageLoad()
      }

      "must go from a TrustAddressYesNoPage to next page when true" in {
        navigator.nextPage(
          TrustAddressYesNoPage,
          CheckMode,
          emptyUserAnswers.setOrException(TrustAddressYesNoPage, true)
        ) mustBe controllers.add.trust.routes.TrustAddressController.onPageLoad(CheckMode)
      }

      "must go from a TrustAddressYesNoPage to CYA page when false" in {
        navigator.nextPage(
          TrustAddressYesNoPage,
          CheckMode,
          emptyUserAnswers.setOrException(TrustAddressYesNoPage, false)
        ) mustBe trustCYA
      }

      "must go from a TrustAddressYesNoPage to journey recovery page when incomplete info provided" in {
        navigator.nextPage(
          TrustAddressYesNoPage,
          CheckMode,
          emptyUserAnswers
        ) mustBe journeyRecovery
      }

      "must go from TrustUtrYesNoPage to TrustUtrController when answer is true" in {
        val ua = UserAnswers("id").set(TrustUtrYesNoPage, true).success.value
        navigator.nextPage(TrustUtrYesNoPage, CheckMode, ua) mustBe
          controllers.add.trust.routes.TrustUtrController.onPageLoad(CheckMode)
      }

      "must go from TrustUtrYesNoPage to TrustCheckYourAnswers when answer is false" in {
        val ua = UserAnswers("id").set(TrustUtrYesNoPage, false).success.value
        navigator.nextPage(TrustUtrYesNoPage, CheckMode, ua) mustBe
          controllers.add.trust.routes.TrustCheckYourAnswersController.onPageLoad()
      }

      "must go from TrustUtrYesNoPage to JourneyRecovery when no answer is present" in {
        navigator.nextPage(TrustUtrYesNoPage, CheckMode, UserAnswers("id")) mustBe
          routes.JourneyRecoveryController.onPageLoad()
      }

      "must go from a page that does not exist in the edit route map to TrustCheckYourAnswers" in {

        case object UnknownPage extends Page
        navigator.nextPage(
          UnknownPage,
          CheckMode,
          UserAnswers("id")
        ) mustBe controllers.add.trust.routes.TrustCheckYourAnswersController
          .onPageLoad()
      }

      "must go from TrustPhoneNumberPage to TrustCheckYourAnswersPage in CheckMode" in {
        navigator.nextPage(
          TrustPhoneNumberPage,
          CheckMode,
          emptyUserAnswers
        ) mustBe controllers.add.trust.routes.TrustCheckYourAnswersController.onPageLoad()
      }

      "must go from TrustWorksReferenceYesNo" - {
        "to next page when answer is Yes" in {
          val answers = UserAnswers(userAnswersId).set(TrustWorksReferenceYesNoPage, true).success.value

          navigator.nextPage(
            TrustWorksReferenceYesNoPage,
            CheckMode,
            answers
          ) mustBe controllers.add.trust.routes.TrustWorksReferenceController
            .onPageLoad(CheckMode)
        }

        "to Trust CyaPage when answer is No" in {
          val answers = UserAnswers(userAnswersId).set(TrustWorksReferenceYesNoPage, false).success.value

          navigator.nextPage(
            TrustWorksReferenceYesNoPage,
            CheckMode,
            answers
          ) mustBe controllers.add.trust.routes.TrustCheckYourAnswersController
            .onPageLoad()
        }

        "to JourneyRecoveryPage when answer is not present" in {
          navigator.nextPage(
            TrustWorksReferenceYesNoPage,
            CheckMode,
            emptyUserAnswers
          ) mustBe routes.JourneyRecoveryController.onPageLoad()
        }
      }

      "must go from TrustMobileNumberPage to TrustCheckYourAnswersPage in CheckMode" in {
        navigator.nextPage(
          TrustMobileNumberPage,
          CheckMode,
          emptyUserAnswers
        ) mustBe controllers.add.trust.routes.TrustCheckYourAnswersController.onPageLoad()
      }

      "must go from TrustAddressPage to TrustCheckYourAnswersController" in {
        navigator.nextPage(TrustAddressPage, CheckMode, UserAnswers("id")) mustBe
          controllers.add.trust.routes.TrustCheckYourAnswersController.onPageLoad()
      }

      "must go from TrustContactOptionsPage" - {

        "to TrustEmailAddressPage when EmailAddress is selected" in {
          navigator.nextPage(
            TrustContactOptionsPage,
            CheckMode,
            emptyUserAnswers.setOrException(
              TrustContactOptionsPage,
              ContactOptions.Email
            )
          ) mustBe controllers.add.trust.routes.TrustEmailAddressController.onPageLoad(CheckMode)
        }

        "to CYA page when Email option is not changed in CheckMode" in {
          val answers =
            emptyUserAnswers
              .setOrException(TrustContactOptionsPage, ContactOptions.Email)
              .setOrException(TrustEmailAddressPage, "test@test.com")

          navigator.nextPage(
            TrustContactOptionsPage,
            CheckMode,
            answers
          ) mustBe controllers.add.trust.routes.TrustCheckYourAnswersController.onPageLoad()
        }

        "to TrustPhoneNumberPage when PhoneOption is selected" in {
          navigator.nextPage(
            TrustContactOptionsPage,
            CheckMode,
            emptyUserAnswers.setOrException(
              TrustContactOptionsPage,
              ContactOptions.Phone
            )
          ) mustBe controllers.add.trust.routes.TrustPhoneNumberController.onPageLoad(CheckMode)
        }

        "to CYA page when Phone option is not changed in CheckMode" in {
          val answers =
            emptyUserAnswers
              .setOrException(TrustContactOptionsPage, ContactOptions.Phone)
              .setOrException(TrustPhoneNumberPage, "0987654333")

          navigator.nextPage(
            TrustContactOptionsPage,
            CheckMode,
            answers
          ) mustBe controllers.add.trust.routes.TrustCheckYourAnswersController.onPageLoad()
        }

        "to TrustMobileNumberPage when MobileOption is selected" in {
          navigator.nextPage(
            TrustContactOptionsPage,
            CheckMode,
            emptyUserAnswers.setOrException(
              TrustContactOptionsPage,
              ContactOptions.Mobile
            )
          ) mustBe controllers.add.trust.routes.TrustMobileNumberController.onPageLoad(CheckMode)
        }

        "to CYA page when Mobile option is not changed in CheckMode" in {
          val answers =
            emptyUserAnswers
              .setOrException(TrustContactOptionsPage, ContactOptions.Mobile)
              .setOrException(TrustMobileNumberPage, "0987654499")

          navigator.nextPage(
            TrustContactOptionsPage,
            CheckMode,
            answers
          ) mustBe controllers.add.trust.routes.TrustCheckYourAnswersController.onPageLoad()
        }

        "to CYA when NoDetails is selected" in {
          navigator.nextPage(
            TrustContactOptionsPage,
            CheckMode,
            emptyUserAnswers.setOrException(
              TrustContactOptionsPage,
              ContactOptions.NoDetails
            )
          ) mustBe controllers.add.trust.routes.TrustCheckYourAnswersController.onPageLoad()
        }

        "to TrustCheckYourAnswers when answer is not present" in {
          navigator.nextPage(
            TrustContactOptionsPage,
            CheckMode,
            emptyUserAnswers
          ) mustBe journeyRecovery
        }
      }
    }
  }
}
