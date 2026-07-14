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
import models.address.Address
import models.contact.ContactMethodOptions
import models.{AmendMode, CheckMode, NormalMode, UserAnswers}
import org.scalactic.Prettifier.default
import pages.Page
import pages.add.trust.*

class TrustNavigatorSpec extends SpecBase {

  val navigator                    = new TrustNavigator
  private lazy val journeyRecovery = routes.JourneyRecoveryController.onPageLoad()
  private lazy val trustCYA        = controllers.add.trust.routes.TrustCheckYourAnswersController.onPageLoad()
  private lazy val trustAmendCYA   = journeyRecovery // TODO: redirect to amend cya page when implemented

  "TrustNavigator" - {

    "in Normal mode" - {

      "must go from a page that doesn't exist in the route map to Index" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, NormalMode, UserAnswers("id")) mustBe routes.IndexController.onPageLoad()

      }

      "must go from TrustNamePage to TrustAddressYesNoPage" in {
        navigator.nextPage(TrustNamePage, NormalMode, UserAnswers("id")) mustBe
          controllers.add.trust.routes.TrustAddressYesNoController.onPageLoad(NormalMode)
      }

      "must go from a TrustAddressYesNoPage to the address lookup on-ramp when true" in {
        navigator.nextPage(
          TrustAddressYesNoPage,
          NormalMode,
          emptyUserAnswers.setOrException(TrustAddressYesNoPage, true)
        ) mustBe controllers.add.trust.routes.TrustAddressController.redirectToAddressLookup()
      }

      "must go from a TrustAddressYesNoPage to AddTrustContactMethodsYesNoController when false" in {
        navigator.nextPage(
          TrustAddressYesNoPage,
          NormalMode,
          emptyUserAnswers.setOrException(TrustAddressYesNoPage, false)
        ) mustBe controllers.add.trust.routes.AddTrustContactMethodsYesNoController.onPageLoad(NormalMode)
      }

      "must go from a TrustAddressYesNoPage to journey recovery when incomplete info provided" in {
        navigator.nextPage(
          TrustAddressYesNoPage,
          NormalMode,
          emptyUserAnswers
        ) mustBe journeyRecovery
      }

      "must go from TrustContactMethodOptions" - {
        "to TrustEmailAddress when Email is selected" in {
          navigator.nextPage(
            TrustContactMethodOptionsPage,
            NormalMode,
            emptyUserAnswers.setOrException(
              TrustContactMethodOptionsPage,
              Set(ContactMethodOptions.Email, ContactMethodOptions.Phone, ContactMethodOptions.Mobile)
            )
          ) mustBe controllers.add.trust.routes.TrustEmailAddressController
            .onPageLoad(NormalMode)
        }

        "to TrustPhoneNumberPage when Phone is selected (Email is not selected)" in {
          navigator.nextPage(
            TrustContactMethodOptionsPage,
            NormalMode,
            emptyUserAnswers.setOrException(
              TrustContactMethodOptionsPage,
              Set(ContactMethodOptions.Phone, ContactMethodOptions.Mobile)
            )
          ) mustBe controllers.add.trust.routes.TrustPhoneNumberController
            .onPageLoad(NormalMode)
        }

        "to TrustMobileNumberPage when Mobile is selected (Email and Phone are not selected)" in {
          navigator.nextPage(
            TrustContactMethodOptionsPage,
            NormalMode,
            emptyUserAnswers.setOrException(TrustContactMethodOptionsPage, Set(ContactMethodOptions.Mobile))
          ) mustBe controllers.add.trust.routes.TrustMobileNumberController
            .onPageLoad(NormalMode)
        }

        "to JourneyRecoveryPage when TrustContactMethodOptions answer is not present" in {
          navigator.nextPage(
            TrustContactMethodOptionsPage,
            NormalMode,
            UserAnswers("id")
          ) mustBe journeyRecovery
        }
      }

      "must go from TrustEmailAddressPage" - {
        "to TrustPhoneNumberPage when Phone is selected in TrustContactMethodOptions" in {
          navigator.nextPage(
            TrustEmailAddressPage,
            NormalMode,
            emptyUserAnswers
              .setOrException(
                TrustContactMethodOptionsPage,
                Set(ContactMethodOptions.Email, ContactMethodOptions.Phone, ContactMethodOptions.Mobile)
              )
          ) mustBe controllers.add.trust.routes.TrustPhoneNumberController
            .onPageLoad(NormalMode)
        }

        "to TrustMobileNumberPage when Mobile is selected in TrustContactMethodOptions" in {
          navigator.nextPage(
            TrustEmailAddressPage,
            NormalMode,
            emptyUserAnswers
              .setOrException(
                TrustContactMethodOptionsPage,
                Set(ContactMethodOptions.Email, ContactMethodOptions.Mobile)
              )
          ) mustBe controllers.add.trust.routes.TrustMobileNumberController
            .onPageLoad(NormalMode)
        }

        "to TrustUtrYesNo Page when only Email is selected in TrustContactMethodOptions" in {
          navigator.nextPage(
            TrustEmailAddressPage,
            NormalMode,
            emptyUserAnswers
              .setOrException(
                TrustContactMethodOptionsPage,
                Set(ContactMethodOptions.Email)
              )
          ) mustBe controllers.add.trust.routes.TrustUtrYesNoController
            .onPageLoad(NormalMode)
        }

        "to JourneyRecoveryPage Page when TrustContactMethodOptions answer is not present" in {
          navigator.nextPage(
            TrustEmailAddressPage,
            NormalMode,
            UserAnswers("id")
          ) mustBe journeyRecovery
        }

        "to JourneyRecoveryPage when Email is not in the selected TrustContactMethodOptions" in {
          navigator.nextPage(
            TrustEmailAddressPage,
            NormalMode,
            emptyUserAnswers
              .setOrException(
                TrustContactMethodOptionsPage,
                Set(ContactMethodOptions.Phone, ContactMethodOptions.Mobile)
              )
          ) mustBe journeyRecovery
        }
      }

      "must go from TrustPhoneNumberPage" - {
        "to TrustMobileNumberPage when Mobile is selected in TrustContactMethodOptions" in {
          navigator.nextPage(
            TrustPhoneNumberPage,
            NormalMode,
            emptyUserAnswers
              .setOrException(
                TrustContactMethodOptionsPage,
                Set(ContactMethodOptions.Email, ContactMethodOptions.Phone, ContactMethodOptions.Mobile)
              )
          ) mustBe controllers.add.trust.routes.TrustMobileNumberController
            .onPageLoad(NormalMode)
        }

        "to TrustUtrYesNo Page when Mobile is not selected in TrustContactMethodOptions" in {
          navigator.nextPage(
            TrustPhoneNumberPage,
            NormalMode,
            emptyUserAnswers
              .setOrException(
                TrustContactMethodOptionsPage,
                Set(ContactMethodOptions.Email, ContactMethodOptions.Phone)
              )
          ) mustBe controllers.add.trust.routes.TrustUtrYesNoController
            .onPageLoad(NormalMode)
        }

        "to JourneyRecoveryPage Page when TrustContactMethodOptions answer is not present" in {
          navigator.nextPage(
            TrustPhoneNumberPage,
            NormalMode,
            UserAnswers("id")
          ) mustBe journeyRecovery
        }

        "to JourneyRecoveryPage when Phone is not in the selected TrustContactMethodOptions" in {
          navigator.nextPage(
            TrustPhoneNumberPage,
            NormalMode,
            emptyUserAnswers
              .setOrException(
                TrustContactMethodOptionsPage,
                Set(ContactMethodOptions.Email, ContactMethodOptions.Mobile)
              )
          ) mustBe journeyRecovery
        }
      }

      "must go from TrustMobileNumberPage" - {
        "to TrustUtrYesNo Page when TrustContactMethodOptions is present" in {
          navigator.nextPage(
            TrustMobileNumberPage,
            NormalMode,
            emptyUserAnswers
              .setOrException(
                TrustContactMethodOptionsPage,
                Set(ContactMethodOptions.Email, ContactMethodOptions.Phone, ContactMethodOptions.Mobile)
              )
          ) mustBe controllers.add.trust.routes.TrustUtrYesNoController
            .onPageLoad(NormalMode)
        }

        "to JourneyRecoveryPage Page when TrustContactMethodOptions answer is not present" in {
          navigator.nextPage(
            TrustMobileNumberPage,
            NormalMode,
            UserAnswers("id")
          ) mustBe journeyRecovery
        }

        "to JourneyRecoveryPage when Mobile is not in the selected TrustContactMethodOptions" in {
          navigator.nextPage(
            TrustMobileNumberPage,
            NormalMode,
            emptyUserAnswers
              .setOrException(
                TrustContactMethodOptionsPage,
                Set(ContactMethodOptions.Email, ContactMethodOptions.Phone)
              )
          ) mustBe journeyRecovery
        }
      }

      "must go from TrustUtrYesNoPage to TrustUtrController when answer is true" in {
        val ua = UserAnswers("id").set(TrustUtrYesNoPage, true).success.value
        navigator.nextPage(TrustUtrYesNoPage, NormalMode, ua) mustBe
          controllers.add.trust.routes.TrustUtrController.onPageLoad(NormalMode)
      }

      "must go from TrustUtrYesNoPage to TrustWorksReferenceYesNoController when answered No" in {
        val ua = emptyUserAnswers.set(TrustUtrYesNoPage, false).success.value
        navigator.nextPage(TrustUtrYesNoPage, NormalMode, ua) mustBe
          controllers.add.trust.routes.TrustWorksReferenceYesNoController.onPageLoad(NormalMode)
      }

      "must go from TrustUtrYesNoPage to JourneyRecovery when answer is not present" in {
        navigator.nextPage(TrustUtrYesNoPage, NormalMode, emptyUserAnswers) mustBe journeyRecovery
      }

      "must go from TrustUtrPage to TrustWorksReferenceYesNoController" in {
        navigator.nextPage(TrustUtrPage, NormalMode, emptyUserAnswers) mustBe
          controllers.add.trust.routes.TrustWorksReferenceYesNoController.onPageLoad(NormalMode)
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

      "must go from a TrustWorksReferencePage to TrustCheckYourAnswerPage" in {
        navigator.nextPage(
          TrustWorksReferencePage,
          NormalMode,
          UserAnswers("id")
        ) mustBe controllers.add.trust.routes.TrustCheckYourAnswersController.onPageLoad()
      }

      "must go from TrustWorksReferenceYesNoPage to TrustWorksReferenceController when true" in {
        navigator.nextPage(
          TrustWorksReferenceYesNoPage,
          NormalMode,
          emptyUserAnswers.setOrException(TrustWorksReferenceYesNoPage, true)
        ) mustBe controllers.add.trust.routes.TrustWorksReferenceController.onPageLoad(NormalMode)
      }

      "must go from TrustWorksReferenceYesNoPage to TrustCheckYourAnswers when false" in {
        navigator.nextPage(
          TrustWorksReferenceYesNoPage,
          NormalMode,
          emptyUserAnswers.setOrException(TrustWorksReferenceYesNoPage, false)
        ) mustBe trustCYA
      }

      "must go from TrustWorksReferenceYesNoPage to JourneyRecovery when answer is not present" in {
        navigator.nextPage(
          TrustWorksReferenceYesNoPage,
          NormalMode,
          emptyUserAnswers
        ) mustBe journeyRecovery
      }

      "must go from AddTrustContactMethodsYesNo" - {
        "to TrustContactMethodOptions Page when answer is Yes" in {
          navigator.nextPage(
            AddTrustContactMethodsYesNoPage,
            NormalMode,
            emptyUserAnswers.setOrException(AddTrustContactMethodsYesNoPage, true)
          ) mustBe controllers.add.trust.routes.TrustContactMethodOptionsController
            .onPageLoad(NormalMode)
        }

        "to TrustUtrYesNo when answer is No" in {
          navigator.nextPage(
            AddTrustContactMethodsYesNoPage,
            NormalMode,
            emptyUserAnswers.setOrException(AddTrustContactMethodsYesNoPage, false)
          ) mustBe controllers.add.trust.routes.TrustUtrYesNoController
            .onPageLoad(NormalMode)
        }

        "to JourneyRecoveryPage when answer is not present" in {
          navigator.nextPage(
            AddTrustContactMethodsYesNoPage,
            NormalMode,
            emptyUserAnswers
          ) mustBe journeyRecovery
        }
      }

    }

    "in Amend mode" - {

      "must go from any page to JourneyRecovery" in {
        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, AmendMode, UserAnswers("id")) mustBe journeyRecovery
      }

      "must go from TrustNamePage to JourneyRecovery" in {
        navigator.nextPage(TrustNamePage, AmendMode, UserAnswers("id")) mustBe journeyRecovery
      }

      "must go from AddTrustContactMethodsYesNo" - {
        "to amend CYA when answer is Yes and TrustContactMethodOptions already answered" in {
          val answers = emptyUserAnswers
            .setOrException(AddTrustContactMethodsYesNoPage, true)
            .setOrException(TrustContactMethodOptionsPage, Set(ContactMethodOptions.Email, ContactMethodOptions.Phone))

          navigator.nextPage(
            AddTrustContactMethodsYesNoPage,
            AmendMode,
            answers
          ) mustBe trustAmendCYA
        }

        "to TrustContactMethodOptions page when answer is Yes and TrustContactMethodOptions not yet answered" in {
          val answers = emptyUserAnswers.setOrException(AddTrustContactMethodsYesNoPage, true)

          navigator.nextPage(
            AddTrustContactMethodsYesNoPage,
            AmendMode,
            answers
          ) mustBe controllers.add.trust.routes.TrustContactMethodOptionsController.onPageLoad(AmendMode)
        }

        "to amend CYA when answer is No" in {
          val answers = emptyUserAnswers.set(AddTrustContactMethodsYesNoPage, false).success.value

          navigator.nextPage(
            AddTrustContactMethodsYesNoPage,
            AmendMode,
            answers
          ) mustBe trustAmendCYA
        }

        "to JourneyRecovery when answer is not present" in {
          navigator.nextPage(
            AddTrustContactMethodsYesNoPage,
            AmendMode,
            emptyUserAnswers
          ) mustBe journeyRecovery
        }
      }

      "must go from TrustWorksReferenceYesNoPage to TrustWorksReferenceController when true and no work reference number exists" in {
        val ua =
          emptyUserAnswers.setOrException(TrustWorksReferenceYesNoPage, true)

        navigator.nextPage(
          TrustWorksReferenceYesNoPage,
          AmendMode,
          ua
        ) mustBe controllers.add.trust.routes.TrustWorksReferenceController.onPageLoad(AmendMode)
      }

      "must go from WorksReferenceNumberYesNoPage to amend cya page when true and work reference number already exists" in {
        val ua =
          emptyUserAnswers
            .setOrException(TrustWorksReferenceYesNoPage, true)
            .setOrException(TrustWorksReferencePage, "wrn-1")

        navigator.nextPage(
          TrustWorksReferenceYesNoPage,
          AmendMode,
          ua
        ) mustBe trustAmendCYA
      }

      "must go from WorksReferenceNumberYesNoPage to amend cya page when false" in {
        val ua =
          emptyUserAnswers.setOrException(TrustWorksReferenceYesNoPage, false)

        navigator.nextPage(
          TrustWorksReferenceYesNoPage,
          AmendMode,
          ua
        ) mustBe trustAmendCYA
      }

      "must go from WorksReferenceNumberYesNoPage to JourneyRecovery when answer is missing" in {
        navigator.nextPage(
          TrustWorksReferenceYesNoPage,
          AmendMode,
          emptyUserAnswers
        ) mustBe journeyRecovery
      }

      "must go from TrustWorksReferencePage to amend cya page" in {
        navigator.nextPage(
          TrustWorksReferencePage,
          AmendMode,
          UserAnswers("id")
        ) mustBe trustAmendCYA
      }
    }

    "in Check mode" - {

      "must go from a TrustAddressYesNoPage to the address lookup on-ramp when true and address not yet answered" in {
        navigator.nextPage(
          TrustAddressYesNoPage,
          CheckMode,
          emptyUserAnswers.setOrException(TrustAddressYesNoPage, true)
        ) mustBe controllers.add.trust.routes.TrustAddressController.redirectToAddressLookup(Some(CheckMode.toString))
      }

      "must go from a TrustAddressYesNoPage to TrustCheckYourAnswers when true and address already answered" in {
        val ua = emptyUserAnswers
          .setOrException(TrustAddressYesNoPage, true)
          .setOrException(TrustAddressPage, Address("line1", addressLine3 = Some("line3"), postcode = Some("AA1 1AA")))
        navigator.nextPage(TrustAddressYesNoPage, CheckMode, ua) mustBe trustCYA
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

      "must go from AddTrustContactMethodsYesNo" - {
        "to CYA when answer is Yes and TrustContactMethodOptions already answered" in {
          val answers = emptyUserAnswers
            .setOrException(AddTrustContactMethodsYesNoPage, true)
            .setOrException(TrustContactMethodOptionsPage, Set(ContactMethodOptions.Email, ContactMethodOptions.Phone))

          navigator.nextPage(
            AddTrustContactMethodsYesNoPage,
            CheckMode,
            answers
          ) mustBe controllers.add.trust.routes.TrustCheckYourAnswersController.onPageLoad()
        }

        "to TrustContactMethodOptions page when answer is Yes and TrustContactMethodOptions not yet answered" in {
          val answers = emptyUserAnswers.setOrException(AddTrustContactMethodsYesNoPage, true)

          navigator.nextPage(
            AddTrustContactMethodsYesNoPage,
            CheckMode,
            answers
          ) mustBe controllers.add.trust.routes.TrustContactMethodOptionsController.onPageLoad(CheckMode)
        }

        "to CYA when answer is No" in {
          val answers = emptyUserAnswers.set(AddTrustContactMethodsYesNoPage, false).success.value

          navigator.nextPage(
            AddTrustContactMethodsYesNoPage,
            CheckMode,
            answers
          ) mustBe controllers.add.trust.routes.TrustCheckYourAnswersController.onPageLoad()
        }

        "to JourneyRecovery when answer is not present" in {
          navigator.nextPage(
            AddTrustContactMethodsYesNoPage,
            CheckMode,
            emptyUserAnswers
          ) mustBe journeyRecovery
        }
      }

      "must go from TrustContactMethodOptions" - {
        "to TrustEmailAddressPage when Email is selected and Email answer not exist" in {
          val answers = emptyUserAnswers
            .set(
              TrustContactMethodOptionsPage,
              Set(ContactMethodOptions.Email)
            )
            .success
            .value

          navigator.nextPage(
            TrustContactMethodOptionsPage,
            CheckMode,
            answers
          ) mustBe controllers.add.trust.routes.TrustEmailAddressController.onPageLoad(CheckMode)
        }

        "to TrustEmailAddressPage when Email, Phone and Mobile are selected and no Email answer not exist" in {
          val answers = emptyUserAnswers
            .set(
              TrustContactMethodOptionsPage,
              Set(ContactMethodOptions.Email, ContactMethodOptions.Phone, ContactMethodOptions.Mobile)
            )
            .success
            .value

          navigator.nextPage(
            TrustContactMethodOptionsPage,
            CheckMode,
            answers
          ) mustBe controllers.add.trust.routes.TrustEmailAddressController.onPageLoad(CheckMode)
        }

        "to CYA when only Email is selected and Email answer exists" in {
          val answers = emptyUserAnswers
            .set(TrustContactMethodOptionsPage, Set(ContactMethodOptions.Email))
            .success
            .value
            .set(TrustEmailAddressPage, "test@test.com")
            .success
            .value

          navigator.nextPage(
            TrustContactMethodOptionsPage,
            CheckMode,
            answers
          ) mustBe controllers.add.trust.routes.TrustCheckYourAnswersController.onPageLoad()
        }

        "to TrustPhoneNumberPage when Phone is selected (Email is not selected) and Phone answer not exists" in {
          val answers = emptyUserAnswers
            .set(
              TrustContactMethodOptionsPage,
              Set(ContactMethodOptions.Phone)
            )
            .success
            .value

          navigator.nextPage(
            TrustContactMethodOptionsPage,
            CheckMode,
            answers
          ) mustBe controllers.add.trust.routes.TrustPhoneNumberController.onPageLoad(CheckMode)
        }

        "to CYA when only Phone is selected and Phone answer exists" in {
          val answers = emptyUserAnswers
            .set(TrustContactMethodOptionsPage, Set(ContactMethodOptions.Phone))
            .success
            .value
            .set(TrustPhoneNumberPage, "01234567890")
            .success
            .value

          navigator.nextPage(
            TrustContactMethodOptionsPage,
            CheckMode,
            answers
          ) mustBe controllers.add.trust.routes.TrustCheckYourAnswersController.onPageLoad()
        }

        "to CYA when only Mobile is selected and Mobile answer exists" in {
          val answers = emptyUserAnswers
            .set(TrustContactMethodOptionsPage, Set(ContactMethodOptions.Mobile))
            .success
            .value
            .set(TrustMobileNumberPage, "01234567890")
            .success
            .value

          navigator.nextPage(
            TrustContactMethodOptionsPage,
            CheckMode,
            answers
          ) mustBe controllers.add.trust.routes.TrustCheckYourAnswersController.onPageLoad()
        }

        "to TrustPhoneNumberPage when Email and Phone are selected and Email answer exists, Phone answer not exists" in {
          val answers = emptyUserAnswers
            .set(TrustContactMethodOptionsPage, Set(ContactMethodOptions.Email, ContactMethodOptions.Phone))
            .success
            .value
            .set(TrustEmailAddressPage, "test@test.com")
            .success
            .value

          navigator.nextPage(
            TrustContactMethodOptionsPage,
            CheckMode,
            answers
          ) mustBe controllers.add.trust.routes.TrustPhoneNumberController.onPageLoad(CheckMode)
        }

        "to TrustMobileNumberPage when Email, Phone and Mobile are selected and Email and Phone answer exists, Mobile answer not exists" in {
          val answers = emptyUserAnswers
            .set(
              TrustContactMethodOptionsPage,
              Set(ContactMethodOptions.Email, ContactMethodOptions.Phone, ContactMethodOptions.Mobile)
            )
            .success
            .value
            .set(TrustEmailAddressPage, "test@test.com")
            .success
            .value
            .set(TrustPhoneNumberPage, "01234567890")
            .success
            .value

          navigator.nextPage(
            TrustContactMethodOptionsPage,
            CheckMode,
            answers
          ) mustBe controllers.add.trust.routes.TrustMobileNumberController.onPageLoad(CheckMode)
        }

        "to JourneyRecovery when answer is not present" in {
          navigator.nextPage(
            TrustContactMethodOptionsPage,
            CheckMode,
            emptyUserAnswers
          ) mustBe journeyRecovery
        }
      }

      "must go from TrustEmailAddressPage" - {

        "to TrustCheckYourAnswers when no missing ContactMethodOptions answer" in {
          navigator.nextPage(
            TrustEmailAddressPage,
            CheckMode,
            emptyUserAnswers
              .setOrException(
                TrustContactMethodOptionsPage,
                Set(ContactMethodOptions.Email, ContactMethodOptions.Phone, ContactMethodOptions.Mobile)
              )
              .setOrException(TrustEmailAddressPage, "test@test.com")
              .setOrException(TrustPhoneNumberPage, "1234567")
              .setOrException(TrustMobileNumberPage, "1234567")
          ) mustBe controllers.add.trust.routes.TrustCheckYourAnswersController.onPageLoad()
        }

        "to TrustPhoneNumberPage when TrustPhoneNumber is missing from ContactMethodOptions answer" in {
          navigator.nextPage(
            TrustEmailAddressPage,
            CheckMode,
            emptyUserAnswers
              .setOrException(
                TrustContactMethodOptionsPage,
                Set(ContactMethodOptions.Email, ContactMethodOptions.Phone, ContactMethodOptions.Mobile)
              )
              .setOrException(TrustEmailAddressPage, "test@test.com")
              .setOrException(TrustMobileNumberPage, "1234567")
          ) mustBe controllers.add.trust.routes.TrustPhoneNumberController.onPageLoad(CheckMode)
        }

        "to TrustMobileNumberPage when TrustMobileNumber is missing from ContactMethodOptions answer" in {
          navigator.nextPage(
            TrustEmailAddressPage,
            CheckMode,
            emptyUserAnswers
              .setOrException(
                TrustContactMethodOptionsPage,
                Set(ContactMethodOptions.Email, ContactMethodOptions.Phone, ContactMethodOptions.Mobile)
              )
              .setOrException(TrustEmailAddressPage, "test@test.com")
              .setOrException(TrustPhoneNumberPage, "1234567")
          ) mustBe controllers.add.trust.routes.TrustMobileNumberController.onPageLoad(CheckMode)
        }

        "to JourneyRecovery when TrustContactMethodOptions answer is not present" in {
          navigator.nextPage(
            TrustEmailAddressPage,
            CheckMode,
            UserAnswers("id")
          ) mustBe journeyRecovery
        }

        "to JourneyRecovery when Email is not in the selected TrustContactMethodOptions" in {
          navigator.nextPage(
            TrustEmailAddressPage,
            CheckMode,
            emptyUserAnswers
              .setOrException(
                TrustContactMethodOptionsPage,
                Set(ContactMethodOptions.Phone, ContactMethodOptions.Mobile)
              )
          ) mustBe journeyRecovery
        }
      }

      "must go from TrustPhoneNumberPage" - {

        "to TrustCheckYourAnswers when no missing ContactMethodOptions answer" in {
          navigator.nextPage(
            TrustPhoneNumberPage,
            CheckMode,
            emptyUserAnswers
              .setOrException(
                TrustContactMethodOptionsPage,
                Set(ContactMethodOptions.Email, ContactMethodOptions.Phone, ContactMethodOptions.Mobile)
              )
              .setOrException(TrustEmailAddressPage, "test@test.com")
              .setOrException(TrustPhoneNumberPage, "1234567")
              .setOrException(TrustMobileNumberPage, "1234567")
          ) mustBe controllers.add.trust.routes.TrustCheckYourAnswersController.onPageLoad()
        }

        "to TrustMobileNumberPage when TrustMobileNumber is missing from ContactMethodOptions answer" in {
          navigator.nextPage(
            TrustPhoneNumberPage,
            CheckMode,
            emptyUserAnswers
              .setOrException(
                TrustContactMethodOptionsPage,
                Set(ContactMethodOptions.Email, ContactMethodOptions.Phone, ContactMethodOptions.Mobile)
              )
              .setOrException(TrustEmailAddressPage, "test@test.com")
              .setOrException(TrustPhoneNumberPage, "1234567")
          ) mustBe controllers.add.trust.routes.TrustMobileNumberController.onPageLoad(CheckMode)
        }

        "to JourneyRecovery when TrustContactMethodOptions answer is not present" in {
          navigator.nextPage(
            TrustPhoneNumberPage,
            CheckMode,
            UserAnswers("id")
          ) mustBe journeyRecovery
        }

        "to JourneyRecovery when Phone is not in the selected TrustContactMethodOptions" in {
          navigator.nextPage(
            TrustPhoneNumberPage,
            CheckMode,
            emptyUserAnswers
              .setOrException(
                TrustContactMethodOptionsPage,
                Set(ContactMethodOptions.Email, ContactMethodOptions.Mobile)
              )
          ) mustBe journeyRecovery
        }
      }

      "must go from TrustMobileNumberPage" - {

        "to TrustCheckYourAnswers" in {
          navigator.nextPage(
            TrustMobileNumberPage,
            CheckMode,
            emptyUserAnswers
              .setOrException(
                TrustContactMethodOptionsPage,
                Set(ContactMethodOptions.Email, ContactMethodOptions.Phone, ContactMethodOptions.Mobile)
              )
              .setOrException(TrustEmailAddressPage, "test@test.com")
              .setOrException(TrustPhoneNumberPage, "1234567")
              .setOrException(TrustMobileNumberPage, "1234567")
          ) mustBe controllers.add.trust.routes.TrustCheckYourAnswersController.onPageLoad()
        }

        "to JourneyRecovery when TrustContactMethodOptions answer is not present" in {
          navigator.nextPage(
            TrustMobileNumberPage,
            CheckMode,
            UserAnswers("id")
          ) mustBe journeyRecovery
        }

        "to JourneyRecovery when Mobile is not in the selected TrustContactMethodOptions" in {
          navigator.nextPage(
            TrustMobileNumberPage,
            CheckMode,
            emptyUserAnswers
              .setOrException(
                TrustContactMethodOptionsPage,
                Set(ContactMethodOptions.Email, ContactMethodOptions.Phone)
              )
          ) mustBe journeyRecovery
        }
      }

      "must go from TrustUtrYesNoPage to TrustUtrController when answer is true and UTR not yet answered" in {
        val ua = UserAnswers("id").set(TrustUtrYesNoPage, true).success.value
        navigator.nextPage(TrustUtrYesNoPage, CheckMode, ua) mustBe
          controllers.add.trust.routes.TrustUtrController.onPageLoad(CheckMode)
      }

      "must go from TrustUtrYesNoPage to TrustCheckYourAnswers when answer is true and UTR already answered" in {
        val ua = emptyUserAnswers
          .setOrException(TrustUtrYesNoPage, true)
          .setOrException(TrustUtrPage, "1234567890")
        navigator.nextPage(TrustUtrYesNoPage, CheckMode, ua) mustBe trustCYA
      }

      "must go from TrustUtrYesNoPage to TrustCheckYourAnswers when answered No" in {
        val ua = emptyUserAnswers.set(TrustUtrYesNoPage, false).success.value
        navigator.nextPage(TrustUtrYesNoPage, CheckMode, ua) mustBe
          controllers.add.trust.routes.TrustCheckYourAnswersController.onPageLoad()
      }

      "must go from TrustUtrYesNoPage to JourneyRecovery when answer is not present" in {
        navigator.nextPage(TrustUtrYesNoPage, CheckMode, emptyUserAnswers) mustBe journeyRecovery
      }

      "must go from TrustUtrPage to TrustCheckYourAnswers" in {
        navigator.nextPage(TrustUtrPage, CheckMode, emptyUserAnswers) mustBe
          controllers.add.trust.routes.TrustCheckYourAnswersController.onPageLoad()
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

      "must go from TrustWorksReferenceYesNoPage to TrustWorksReferenceController when true and works ref not yet answered" in {
        navigator.nextPage(
          TrustWorksReferenceYesNoPage,
          CheckMode,
          emptyUserAnswers.setOrException(TrustWorksReferenceYesNoPage, true)
        ) mustBe controllers.add.trust.routes.TrustWorksReferenceController.onPageLoad(CheckMode)
      }

      "must go from TrustWorksReferenceYesNoPage to TrustCheckYourAnswers when true and works ref already answered" in {
        val answers =
          emptyUserAnswers
            .setOrException(TrustWorksReferenceYesNoPage, true)
            .setOrException(TrustWorksReferencePage, "12345678")
        navigator.nextPage(TrustWorksReferenceYesNoPage, CheckMode, answers) mustBe trustCYA
      }

      "must go from TrustWorksReferenceYesNoPage to TrustCheckYourAnswers when false" in {
        navigator.nextPage(
          TrustWorksReferenceYesNoPage,
          CheckMode,
          emptyUserAnswers.setOrException(TrustWorksReferenceYesNoPage, false)
        ) mustBe trustCYA
      }

      "must go from TrustWorksReferenceYesNoPage to JourneyRecovery when answer is not present" in {
        navigator.nextPage(
          TrustWorksReferenceYesNoPage,
          CheckMode,
          emptyUserAnswers
        ) mustBe journeyRecovery
      }
    }
  }
}
