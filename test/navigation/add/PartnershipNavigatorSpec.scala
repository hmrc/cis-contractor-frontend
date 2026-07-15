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
import pages.add.partnership.*
import pages.Page
import pages.QuestionPage
import play.api.libs.json.JsPath

class PartnershipNavigatorSpec extends SpecBase {

  val navigator                        = new PartnershipNavigator
  private lazy val journeyRecovery     = routes.JourneyRecoveryController.onPageLoad()
  private lazy val partnershipCYA      =
    controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad()
  private lazy val partnershipAmendCYA = routes.JourneyRecoveryController
    .onPageLoad() // TODO when available   controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad()

  "PartnershipNavigator" - {

    "in Normal mode" - {

      "must go from a page that doesn't exist in the route map to Index" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, NormalMode, UserAnswers("id")) mustBe routes.IndexController.onPageLoad()
      }

      "must go from a PartnershipHasUtrYesNo to PartnershipUniqueTaxpayerReferencePage when true" in {
        navigator.nextPage(
          PartnershipHasUtrYesNoPage,
          NormalMode,
          emptyUserAnswers.setOrException(PartnershipHasUtrYesNoPage, true)
        ) mustBe controllers.add.partnership.routes.PartnershipUniqueTaxpayerReferenceController.onPageLoad(NormalMode)
      }

      "must go from a PartnershipHasUtrYesNo to PartnershipNominatedPartnerNameController when false" in {
        navigator.nextPage(
          PartnershipHasUtrYesNoPage,
          NormalMode,
          emptyUserAnswers.setOrException(PartnershipHasUtrYesNoPage, false)
        ) mustBe controllers.add.partnership.routes.PartnershipNominatedPartnerNameController
          .onPageLoad(NormalMode)
      }

      "must go from a PartnershipHasUtrYesNo to JourneyRecovery page when no answer" in {
        navigator.nextPage(
          PartnershipHasUtrYesNoPage,
          NormalMode,
          emptyUserAnswers
        ) mustBe journeyRecovery
      }

      "must go from a PartnershipHasUtrYesNo to PartnershipUniqueTaxpayerReference page when true in CheckMode" in {
        navigator.nextPage(
          PartnershipHasUtrYesNoPage,
          CheckMode,
          emptyUserAnswers.setOrException(PartnershipHasUtrYesNoPage, true)
        ) mustBe controllers.add.partnership.routes.PartnershipUniqueTaxpayerReferenceController
          .onPageLoad(CheckMode)
      }

      "must go from a PartnershipHasUtrYesNo to PartnershipCheckYourAnswers page when false in CheckMode" in {
        navigator.nextPage(
          PartnershipHasUtrYesNoPage,
          CheckMode,
          emptyUserAnswers.setOrException(PartnershipHasUtrYesNoPage, false)
        ) mustBe partnershipCYA
      }

      "must go from a PartnershipUniqueTaxpayerReference to PartnershipNominatedPartnerName page" in {
        navigator.nextPage(
          PartnershipUniqueTaxpayerReferencePage,
          NormalMode,
          emptyUserAnswers.setOrException(PartnershipUniqueTaxpayerReferencePage, "5860920998")
        ) mustBe controllers.add.partnership.routes.PartnershipNominatedPartnerNameController
          .onPageLoad(NormalMode)
      }

      "must go from a PartnershipUniqueTaxpayerReference to PartnershipCheckYourAnswers page in CheckMode" in {
        navigator.nextPage(
          PartnershipUniqueTaxpayerReferencePage,
          CheckMode,
          emptyUserAnswers.setOrException(PartnershipUniqueTaxpayerReferencePage, "5860920998")
        ) mustBe partnershipCYA
      }

      "must go from a PartnershipWorksReferenceNumberYesNoPage to PartnershipWorksReferenceNumber page when true" in {
        navigator.nextPage(
          PartnershipWorksReferenceNumberYesNoPage,
          NormalMode,
          emptyUserAnswers.setOrException(PartnershipWorksReferenceNumberYesNoPage, true)
        ) mustBe controllers.add.partnership.routes.PartnershipWorksReferenceNumberController
          .onPageLoad(NormalMode)
      }

      "must go from a PartnershipWorksReferenceNumberYesNoPage to PartnershipCheckYourAnswersController page when false" in {
        navigator.nextPage(
          PartnershipWorksReferenceNumberYesNoPage,
          NormalMode,
          emptyUserAnswers.setOrException(PartnershipWorksReferenceNumberYesNoPage, false)
        ) mustBe controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad()
      }

      "must go from a PartnershipWorksReferenceNumberYesNoPage to journey recovery when incomplete info provided" in {
        navigator.nextPage(
          PartnershipWorksReferenceNumberYesNoPage,
          NormalMode,
          emptyUserAnswers
        ) mustBe journeyRecovery
      }

      "must go from a PartnershipWorksReferenceNumberPage to PartnershipCheckYourAnswers page" in {
        navigator.nextPage(
          PartnershipWorksReferenceNumberPage,
          NormalMode,
          emptyUserAnswers.setOrException(PartnershipUniqueTaxpayerReferencePage, "UTR-123")
        ) mustBe controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad()
      }

      "must go from PartnershipUniqueTaxpayerReferencePage to PartnershipNominatedPartnerNameController in NormalMode" in {
        navigator.nextPage(
          PartnershipUniqueTaxpayerReferencePage,
          NormalMode,
          emptyUserAnswers.setOrException(PartnershipUniqueTaxpayerReferencePage, "5860920998")
        ) mustBe controllers.add.partnership.routes.PartnershipNominatedPartnerNameController.onPageLoad(NormalMode)
      }

      "must go from PartnershipAddressYesNoPage to the address lookup on-ramp when true in NormalMode" in {
        navigator.nextPage(
          PartnershipAddressYesNoPage,
          NormalMode,
          emptyUserAnswers.setOrException(PartnershipAddressYesNoPage, true)
        ) mustBe controllers.add.partnership.routes.PartnershipAddressController.redirectToAddressLookup()
      }

      "must go from PartnershipAddressYesNoPage to AddPartnershipContactMethodsYesNo Page when false in NormalMode" in {
        navigator.nextPage(
          PartnershipAddressYesNoPage,
          NormalMode,
          emptyUserAnswers.setOrException(PartnershipAddressYesNoPage, false)
        ) mustBe controllers.add.partnership.routes.AddPartnershipContactMethodsYesNoController.onPageLoad(NormalMode)
      }

      "must go from PartnershipAddressYesNoPage to JourneyRecovery when no answer in NormalMode" in {
        navigator.nextPage(
          PartnershipAddressYesNoPage,
          NormalMode,
          emptyUserAnswers
        ) mustBe journeyRecovery
      }

      "must go from PartnershipNominatedPartnerNinoYesNoPage to PartnershipNominatedPartnerNinoController when true in NormalMode" in {
        navigator.nextPage(
          PartnershipNominatedPartnerNinoYesNoPage,
          NormalMode,
          emptyUserAnswers.setOrException(PartnershipNominatedPartnerNinoYesNoPage, true)
        ) mustBe controllers.add.partnership.routes.PartnershipNominatedPartnerNinoController.onPageLoad(NormalMode)
      }

      "must go from PartnershipNominatedPartnerNinoYesNoPage to PartnershipNominatedPartnerCrnYesNoController when false in NormalMode" in {
        navigator.nextPage(
          PartnershipNominatedPartnerNinoYesNoPage,
          NormalMode,
          emptyUserAnswers.setOrException(PartnershipNominatedPartnerNinoYesNoPage, false)
        ) mustBe controllers.add.partnership.routes.PartnershipNominatedPartnerCrnYesNoController.onPageLoad(NormalMode)
      }

      "must go from PartnershipNominatedPartnerNinoYesNoPage to JourneyRecovery when no answer in NormalMode" in {
        navigator.nextPage(
          PartnershipNominatedPartnerNinoYesNoPage,
          NormalMode,
          emptyUserAnswers
        ) mustBe journeyRecovery
      }
      "must go from a PartnershipNominatedPartnerNamePage to PartnershipNominatedPartnerUtrYesNoController" in {
        navigator.nextPage(
          PartnershipNominatedPartnerNamePage,
          NormalMode,
          UserAnswers("id")
        ) mustBe controllers.add.partnership.routes.PartnershipNominatedPartnerUtrYesNoController.onPageLoad(NormalMode)
      }

      "must go from PartnershipNamePage to PartnershipAddressYesNoController in NormalMode" in {
        navigator.nextPage(
          PartnershipNamePage,
          NormalMode,
          emptyUserAnswers
        ) mustBe controllers.add.partnership.routes.PartnershipAddressYesNoController.onPageLoad(NormalMode)
      }

      "must go from PartnershipNominatedPartnerCrnYesNo" - {
        "to next page when answer is Yes" in {
          navigator.nextPage(
            PartnershipNominatedPartnerCrnYesNoPage,
            NormalMode,
            emptyUserAnswers.setOrException(PartnershipNominatedPartnerCrnYesNoPage, true)
          ) mustBe controllers.add.partnership.routes.PartnershipNominatedPartnerCrnController
            .onPageLoad(NormalMode)
        }

        "to next page when answer is No" in {
          navigator.nextPage(
            PartnershipNominatedPartnerCrnYesNoPage,
            NormalMode,
            emptyUserAnswers.setOrException(PartnershipNominatedPartnerCrnYesNoPage, false)
          ) mustBe controllers.add.partnership.routes.PartnershipWorksReferenceNumberYesNoController
            .onPageLoad(NormalMode)
        }

        "to JourneyRecoveryPage when answer is not present" in {
          navigator.nextPage(
            PartnershipNominatedPartnerCrnYesNoPage,
            NormalMode,
            emptyUserAnswers
          ) mustBe journeyRecovery
        }
      }

      "PartnershipHasUtrYesNoPage in NormalMode" - {

        "must go to PartnershipUniqueTaxpayerReferenceController when answer is true" in {
          navigator.nextPage(
            PartnershipHasUtrYesNoPage,
            NormalMode,
            emptyUserAnswers.setOrException(PartnershipHasUtrYesNoPage, true)
          ) mustBe controllers.add.partnership.routes.PartnershipUniqueTaxpayerReferenceController
            .onPageLoad(NormalMode)
        }

        "must go to PartnershipNominatedPartnerNameController when answer is false" in {
          navigator.nextPage(
            PartnershipHasUtrYesNoPage,
            NormalMode,
            emptyUserAnswers.setOrException(PartnershipHasUtrYesNoPage, false)
          ) mustBe controllers.add.partnership.routes.PartnershipNominatedPartnerNameController.onPageLoad(NormalMode)
        }

        "must go to JourneyRecovery when answer is missing" in {
          navigator.nextPage(
            PartnershipHasUtrYesNoPage,
            NormalMode,
            emptyUserAnswers
          ) mustBe journeyRecovery
        }
      }

      "must go from a PartnershipNominatedPartnerUtrYesNoPage to next page when true" in {
        navigator.nextPage(
          PartnershipNominatedPartnerUtrYesNoPage,
          NormalMode,
          emptyUserAnswers.setOrException(PartnershipNominatedPartnerUtrYesNoPage, true)
        ) mustBe controllers.add.partnership.routes.PartnershipNominatedPartnerUtrController
          .onPageLoad(NormalMode)
      }

      "must go from a PartnershipNominatedPartnerUtrYesNoPage to next page when false" in {
        navigator.nextPage(
          PartnershipNominatedPartnerUtrYesNoPage,
          NormalMode,
          emptyUserAnswers.setOrException(PartnershipNominatedPartnerUtrYesNoPage, false)
        ) mustBe controllers.add.partnership.routes.PartnershipNominatedPartnerNinoYesNoController
          .onPageLoad(NormalMode)
      }

      "must go from a PartnershipNominatedPartnerUtrYesNoPage to journey recovery when incomplete info provided" in {
        navigator.nextPage(
          PartnershipNominatedPartnerUtrYesNoPage,
          NormalMode,
          emptyUserAnswers
        ) mustBe journeyRecovery
      }

      "must go from a PartnershipNominatedPartnerUtrPage to PartnershipNominatedPartnerNinoYesNoController in NormalMode" in {
        navigator.nextPage(
          PartnershipNominatedPartnerUtrPage,
          NormalMode,
          emptyUserAnswers
        ) mustBe controllers.add.partnership.routes.PartnershipNominatedPartnerNinoYesNoController
          .onPageLoad(NormalMode)
      }

      "must go from PartnershipEmailAddressPage" - {
        "to PartnershipPhoneNumberPage when Phone is selected in PartnershipContactMethodOptions" in {
          navigator.nextPage(
            PartnershipEmailAddressPage,
            NormalMode,
            emptyUserAnswers
              .setOrException(
                PartnershipContactMethodOptionsPage,
                Set(ContactMethodOptions.Email, ContactMethodOptions.Phone, ContactMethodOptions.Mobile)
              )
          ) mustBe controllers.add.partnership.routes.PartnershipPhoneNumberController
            .onPageLoad(NormalMode)
        }

        "to PartnershipMobileNumberPage when Mobile is selected in PartnershipContactMethodOptions" in {
          navigator.nextPage(
            PartnershipEmailAddressPage,
            NormalMode,
            emptyUserAnswers
              .setOrException(
                PartnershipContactMethodOptionsPage,
                Set(ContactMethodOptions.Email, ContactMethodOptions.Mobile)
              )
          ) mustBe controllers.add.partnership.routes.PartnershipMobileNumberController
            .onPageLoad(NormalMode)
        }

        "to PartnershipHasUtrYesNo Page when only Email is selected in PartnershipContactMethodOptions" in {
          navigator.nextPage(
            PartnershipEmailAddressPage,
            NormalMode,
            emptyUserAnswers
              .setOrException(
                PartnershipContactMethodOptionsPage,
                Set(ContactMethodOptions.Email)
              )
          ) mustBe controllers.add.partnership.routes.PartnershipHasUtrYesNoController
            .onPageLoad(NormalMode)
        }

        "to JourneyRecoveryPage Page when PartnershipContactMethodOptions answer is not present" in {
          navigator.nextPage(
            PartnershipEmailAddressPage,
            NormalMode,
            UserAnswers("id")
          ) mustBe journeyRecovery
        }

        "to JourneyRecoveryPage when Email is not in the selected PartnershipContactMethodOptions" in {
          navigator.nextPage(
            PartnershipEmailAddressPage,
            NormalMode,
            emptyUserAnswers
              .setOrException(
                PartnershipContactMethodOptionsPage,
                Set(ContactMethodOptions.Phone, ContactMethodOptions.Mobile)
              )
          ) mustBe journeyRecovery
        }

      }

      "must go from PartnershipPhoneNumberPage" - {
        "to PartnershipMobileNumberPage when Mobile is selected in PartnershipContactMethodOptions" in {
          navigator.nextPage(
            PartnershipPhoneNumberPage,
            NormalMode,
            emptyUserAnswers
              .setOrException(
                PartnershipContactMethodOptionsPage,
                Set(ContactMethodOptions.Email, ContactMethodOptions.Phone, ContactMethodOptions.Mobile)
              )
          ) mustBe controllers.add.partnership.routes.PartnershipMobileNumberController
            .onPageLoad(NormalMode)
        }

        "to PartnershipHasUtrYesNo Page when Mobile is not selected in PartnershipContactMethodOptions" in {
          navigator.nextPage(
            PartnershipPhoneNumberPage,
            NormalMode,
            emptyUserAnswers
              .setOrException(
                PartnershipContactMethodOptionsPage,
                Set(ContactMethodOptions.Email, ContactMethodOptions.Phone)
              )
          ) mustBe controllers.add.partnership.routes.PartnershipHasUtrYesNoController
            .onPageLoad(NormalMode)
        }

        "to JourneyRecoveryPage Page when PartnershipContactMethodOptions answer is not present" in {
          navigator.nextPage(
            PartnershipPhoneNumberPage,
            NormalMode,
            UserAnswers("id")
          ) mustBe journeyRecovery
        }

        "to JourneyRecoveryPage when Phone is not in the selected PartnershipContactMethodOptions" in {
          navigator.nextPage(
            PartnershipPhoneNumberPage,
            NormalMode,
            emptyUserAnswers
              .setOrException(
                PartnershipContactMethodOptionsPage,
                Set(ContactMethodOptions.Email, ContactMethodOptions.Mobile)
              )
          ) mustBe journeyRecovery
        }
      }

      "must go from PartnershipMobileNumberPage" - {
        "to PartnershipHasUtrYesNo Page when PartnershipContactMethodOptions is present" in {
          navigator.nextPage(
            PartnershipMobileNumberPage,
            NormalMode,
            emptyUserAnswers
              .setOrException(
                PartnershipContactMethodOptionsPage,
                Set(ContactMethodOptions.Email, ContactMethodOptions.Phone, ContactMethodOptions.Mobile)
              )
          ) mustBe controllers.add.partnership.routes.PartnershipHasUtrYesNoController
            .onPageLoad(NormalMode)
        }

        "to JourneyRecoveryPage Page when PartnershipContactMethodOptions answer is not present" in {
          navigator.nextPage(
            PartnershipMobileNumberPage,
            NormalMode,
            UserAnswers("id")
          ) mustBe journeyRecovery
        }

        "to JourneyRecoveryPage when Mobile is not in the selected PartnershipContactMethodOptions" in {
          navigator.nextPage(
            PartnershipMobileNumberPage,
            NormalMode,
            emptyUserAnswers
              .setOrException(
                PartnershipContactMethodOptionsPage,
                Set(ContactMethodOptions.Email, ContactMethodOptions.Phone)
              )
          ) mustBe journeyRecovery
        }
      }

      "must go from PartnershipNominatedPartnerNinoPage to PartnershipNominatedPartnerCrnYesNoController in NormalMode" in {
        val answersWithNino =
          emptyUserAnswers
            .set(PartnershipNominatedPartnerNinoPage, "AA123456A")
            .success
            .value

        navigator.nextPage(
          PartnershipNominatedPartnerNinoPage,
          NormalMode,
          answersWithNino
        ) mustBe controllers.add.partnership.routes.PartnershipNominatedPartnerCrnYesNoController.onPageLoad(NormalMode)
      }

      "must go from PartnershipNominatedPartnerCrnPage to PartnershipWorksReferenceNumberYesNoController in NormalMode" in {
        navigator.nextPage(
          PartnershipNominatedPartnerCrnPage,
          NormalMode,
          emptyUserAnswers.setOrException(PartnershipNominatedPartnerCrnPage, "12345678")
        ) mustBe controllers.add.partnership.routes.PartnershipWorksReferenceNumberYesNoController
          .onPageLoad(NormalMode)
      }

      "must go from PartnershipNominatedPartnerCrnPage to PartnershipWorksReferenceNumberYesNoController when CRN exists in NormalMode" in {

        val answers =
          emptyUserAnswers
            .set(PartnershipNominatedPartnerCrnPage, "12345678")
            .success
            .value

        navigator.nextPage(
          PartnershipNominatedPartnerCrnPage,
          NormalMode,
          answers
        ) mustBe controllers.add.partnership.routes.PartnershipWorksReferenceNumberYesNoController
          .onPageLoad(NormalMode)
      }

      "must go from PartnershipHasUtrYesNoPage to JourneyRecovery when no answer is present in NormalMode" in {
        navigator.nextPage(
          PartnershipHasUtrYesNoPage,
          NormalMode,
          emptyUserAnswers
        ) mustBe routes.JourneyRecoveryController.onPageLoad()
      }

      "must go to PartnershipNominatedPartnerNameController when answer is false in NormalMode" in {

        val answers =
          emptyUserAnswers
            .set(PartnershipHasUtrYesNoPage, false)
            .success
            .value

        navigator.nextPage(
          PartnershipHasUtrYesNoPage,
          NormalMode,
          answers
        ) mustBe controllers.add.partnership.routes.PartnershipNominatedPartnerNameController
          .onPageLoad(NormalMode)
      }

      "must go to IndexController for an unknown page in NormalMode" in {
        case object UnknownPage extends QuestionPage[String] {
          override def path: JsPath = JsPath \ "unknown-page"
        }

        navigator.nextPage(
          UnknownPage,
          NormalMode,
          emptyUserAnswers
        ) mustBe controllers.routes.IndexController.onPageLoad()
      }

      "must go from AddPartnershipContactMethodsYesNo" - {
        "to PartnershipContactMethodOptionsPage when answer is Yes" in {
          navigator.nextPage(
            AddPartnershipContactMethodsYesNoPage,
            NormalMode,
            emptyUserAnswers.setOrException(AddPartnershipContactMethodsYesNoPage, true)
          ) mustBe controllers.add.partnership.routes.PartnershipContactMethodOptionsController
            .onPageLoad(NormalMode)
        }

        "to PartnershipHasUtrYesNo when answer is No" in {
          navigator.nextPage(
            AddPartnershipContactMethodsYesNoPage,
            NormalMode,
            emptyUserAnswers.setOrException(AddPartnershipContactMethodsYesNoPage, false)
          ) mustBe controllers.add.partnership.routes.PartnershipHasUtrYesNoController
            .onPageLoad(NormalMode)
        }

        "to JourneyRecoveryPage when answer is not present" in {
          navigator.nextPage(
            AddPartnershipContactMethodsYesNoPage,
            NormalMode,
            emptyUserAnswers
          ) mustBe journeyRecovery
        }
      }

      "must go from PartnershipContactMethodOptions" - {
        "to PartnershipEmailAddress when Email is selected" in {
          navigator.nextPage(
            PartnershipContactMethodOptionsPage,
            NormalMode,
            emptyUserAnswers.setOrException(
              PartnershipContactMethodOptionsPage,
              Set(ContactMethodOptions.Email, ContactMethodOptions.Phone, ContactMethodOptions.Mobile)
            )
          ) mustBe controllers.add.partnership.routes.PartnershipEmailAddressController
            .onPageLoad(NormalMode)
        }

        "to PartnershipPhoneNumberPage when Phone is selected (Email is not selected)" in {
          navigator.nextPage(
            PartnershipContactMethodOptionsPage,
            NormalMode,
            emptyUserAnswers.setOrException(
              PartnershipContactMethodOptionsPage,
              Set(ContactMethodOptions.Phone, ContactMethodOptions.Mobile)
            )
          ) mustBe controllers.add.partnership.routes.PartnershipPhoneNumberController
            .onPageLoad(NormalMode)
        }

        "to PartnershipMobileNumberPage when Mobile is selected (Email and Phone are not selected)" in {
          navigator.nextPage(
            PartnershipContactMethodOptionsPage,
            NormalMode,
            emptyUserAnswers.setOrException(PartnershipContactMethodOptionsPage, Set(ContactMethodOptions.Mobile))
          ) mustBe controllers.add.partnership.routes.PartnershipMobileNumberController
            .onPageLoad(NormalMode)
        }

        "to JourneyRecoveryPage when PartnershipContactMethodOptions answer is not present" in {
          navigator.nextPage(
            PartnershipContactMethodOptionsPage,
            NormalMode,
            UserAnswers("id")
          ) mustBe journeyRecovery
        }
      }

    }

    "in Amend mode" - {

      "must go from any page to JourneyRecovery" in {
        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, AmendMode, UserAnswers("id")) mustBe journeyRecovery
      }

      "must go from PartnershipNamePage to JourneyRecovery" in {
        navigator.nextPage(PartnershipNamePage, AmendMode, emptyUserAnswers) mustBe journeyRecovery
      }

      "must go from a PartnershipWorksReferenceNumberYesNoPage to next page when true" in {
        navigator.nextPage(
          PartnershipWorksReferenceNumberYesNoPage,
          AmendMode,
          emptyUserAnswers.setOrException(PartnershipWorksReferenceNumberYesNoPage, true)
        ) mustBe controllers.add.partnership.routes.PartnershipWorksReferenceNumberController.onPageLoad(AmendMode)
      }

      "must go from PartnershipWorksReferenceNumberYesNoPage to CYA when answer is true in AmendMode and works reference number already exists" in {
        val answers =
          emptyUserAnswers
            .set(PartnershipWorksReferenceNumberYesNoPage, true)
            .success
            .value
            .set(PartnershipWorksReferenceNumberPage, "WRN-12345")
            .success
            .value

        navigator.nextPage(
          PartnershipWorksReferenceNumberYesNoPage,
          AmendMode,
          answers
        ) mustBe partnershipAmendCYA
      }

      "must go from a PartnershipWorksReferenceNumberYesNoPage to PartnershipCheckYourAnswers page when false" in {
        navigator.nextPage(
          PartnershipWorksReferenceNumberYesNoPage,
          AmendMode,
          emptyUserAnswers.setOrException(PartnershipWorksReferenceNumberYesNoPage, false)
        ) mustBe partnershipAmendCYA
      }

      "must go from a PartnershipWorksReferenceNumberYesNoPage to journey recovery page when incomplete info provided" in {
        navigator.nextPage(
          PartnershipWorksReferenceNumberYesNoPage,
          AmendMode,
          emptyUserAnswers
        ) mustBe journeyRecovery
      }
      "must go from a PartnershipWorksReferenceNumberPage to PartnershipCheckYourAnswers page in AmendMode" in {
        navigator.nextPage(
          PartnershipWorksReferenceNumberPage,
          AmendMode,
          emptyUserAnswers.setOrException(PartnershipUniqueTaxpayerReferencePage, "UTR-123")
        ) mustBe partnershipAmendCYA
      }

      "must go from PartnershipNominatedPartnerCrnYesNo" - {
        "to next page when answer is Yes" in {
          val answers = UserAnswers(userAnswersId).set(PartnershipNominatedPartnerCrnYesNoPage, true).success.value

          navigator.nextPage(
            PartnershipNominatedPartnerCrnYesNoPage,
            AmendMode,
            answers
          ) mustBe controllers.add.partnership.routes.PartnershipNominatedPartnerCrnController
            .onPageLoad(AmendMode)
        }

        "to PartnershipCheckYourAnswers when answer is No" in {
          val answers = UserAnswers(userAnswersId).set(PartnershipNominatedPartnerCrnYesNoPage, false).success.value

          navigator.nextPage(
            PartnershipNominatedPartnerCrnYesNoPage,
            AmendMode,
            answers
          ) mustBe partnershipAmendCYA
        }

        "must go from PartnershipNominatedPartnerCrnYesNoPage to Amend CYA when answer is true in AmendMode and CRN already exists" in {
          val answers =
            emptyUserAnswers
              .set(PartnershipNominatedPartnerCrnYesNoPage, true)
              .success
              .value
              .set(PartnershipNominatedPartnerCrnPage, "12345678")
              .success
              .value

          navigator.nextPage(
            PartnershipNominatedPartnerCrnYesNoPage,
            AmendMode,
            answers
          ) mustBe partnershipAmendCYA
        }

        "to JourneyRecoveryPage when answer is not present" in {
          navigator.nextPage(
            PartnershipNominatedPartnerCrnYesNoPage,
            AmendMode,
            emptyUserAnswers
          ) mustBe routes.JourneyRecoveryController.onPageLoad()
        }
      }

      "PartnershipNominatedPartnerCrnPage in AmendMode" - {
        "must go from PartnershipNominatedPartnerCrnPage to PartnershipCheckYourAnswersController when CRN exists in AmendMode" in {

          val answers =
            emptyUserAnswers
              .set(PartnershipNominatedPartnerCrnPage, "12345678")
              .success
              .value

          navigator.nextPage(
            PartnershipNominatedPartnerCrnPage,
            AmendMode,
            answers
          ) mustBe partnershipAmendCYA
        }

        "must go to JourneyRecovery when answer is missing" in {
          navigator.nextPage(
            PartnershipNominatedPartnerCrnPage,
            AmendMode,
            emptyUserAnswers
          ) mustBe routes.JourneyRecoveryController.onPageLoad()
        }
      }

      "must go from PartnershipNominatedPartnerNino" - {
        "must go from PartnershipNominatedPartnerNinoYesNoPage to PartnershipNominatedPartnerNinoController when true in AmendMode" in {
          navigator.nextPage(
            PartnershipNominatedPartnerNinoYesNoPage,
            AmendMode,
            emptyUserAnswers.setOrException(PartnershipNominatedPartnerNinoYesNoPage, true)
          ) mustBe controllers.add.partnership.routes.PartnershipNominatedPartnerNinoController.onPageLoad(AmendMode)
        }

        "must go from PartnershipNominatedPartnerNinoYesNoPage to PartnershipCheckYourAnswers when false in AmendMode" in {
          navigator.nextPage(
            PartnershipNominatedPartnerNinoYesNoPage,
            AmendMode,
            emptyUserAnswers.setOrException(PartnershipNominatedPartnerNinoYesNoPage, false)
          ) mustBe partnershipAmendCYA
        }

        "must go from PartnershipNominatedPartnerNinoYesNoPage to JourneyRecovery when no answer in AmendMode" in {
          navigator.nextPage(
            PartnershipNominatedPartnerNinoYesNoPage,
            AmendMode,
            emptyUserAnswers
          ) mustBe journeyRecovery
        }
        "must go from PartnershipNominatedPartnerNinoYesNoPage to PartnershipCheckYourAnswersController when answer is false in AmendMode" in {
          val answers =
            emptyUserAnswers
              .set(PartnershipNominatedPartnerNinoYesNoPage, false)
              .success
              .value

          navigator.nextPage(
            PartnershipNominatedPartnerNinoYesNoPage,
            AmendMode,
            answers
          ) mustBe partnershipAmendCYA
        }

        "must go from PartnershipNominatedPartnerNinoYesNoPage to PartnershipCheckYourAnswersController when answer is true in AmendMode and NINO is already provided" in {
          val answers =
            emptyUserAnswers
              .set(PartnershipNominatedPartnerNinoYesNoPage, true)
              .success
              .value
              .set(PartnershipNominatedPartnerNinoPage, "AA123456A")
              .success
              .value

          navigator.nextPage(
            PartnershipNominatedPartnerNinoYesNoPage,
            AmendMode,
            answers
          ) mustBe partnershipAmendCYA
        }

        "must go from PartnershipNominatedPartnerNinoPage to PartnershipCheckYourAnswersController in AmendMode" in {
          val answersWithNino =
            emptyUserAnswers
              .set(PartnershipNominatedPartnerNinoPage, "AA123456A")
              .success
              .value

          navigator.nextPage(
            PartnershipNominatedPartnerNinoPage,
            AmendMode,
            answersWithNino
          ) mustBe partnershipAmendCYA
        }

        "to JourneyRecovery when answer is missing" in {
          navigator.nextPage(
            PartnershipNominatedPartnerNinoPage,
            AmendMode,
            emptyUserAnswers
          ) mustBe routes.JourneyRecoveryController.onPageLoad()
        }
      }
      "must go from a PartnershipNominatedPartnerNamePage to AmendPartnershipCheckYourAnswers in AmendMode" in {
        navigator.nextPage(
          PartnershipNominatedPartnerNamePage,
          AmendMode,
          UserAnswers("id")
        ) mustBe partnershipAmendCYA
      }

      "must go from PartnershipNamePage to AmendPartnershipCheckYourAnswers in AmendMode" in {
        navigator.nextPage(
          PartnershipNamePage,
          AmendMode,
          emptyUserAnswers
        ) mustBe partnershipAmendCYA
      }
    }

    "in Check mode" - {

      "must go from a PartnershipWorksReferenceNumberYesNoPage to next page when true" in {
        navigator.nextPage(
          PartnershipWorksReferenceNumberYesNoPage,
          CheckMode,
          emptyUserAnswers.setOrException(PartnershipWorksReferenceNumberYesNoPage, true)
        ) mustBe controllers.add.partnership.routes.PartnershipWorksReferenceNumberController.onPageLoad(CheckMode)
      }

      "must go from PartnershipWorksReferenceNumberYesNoPage to CYA when answer is true in CheckMode and works reference number already exists" in {
        val answers =
          emptyUserAnswers
            .set(PartnershipWorksReferenceNumberYesNoPage, true)
            .success
            .value
            .set(PartnershipWorksReferenceNumberPage, "WRN-12345")
            .success
            .value

        navigator.nextPage(
          PartnershipWorksReferenceNumberYesNoPage,
          CheckMode,
          answers
        ) mustBe controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad()
      }

      "must go from a PartnershipWorksReferenceNumberYesNoPage to PartnershipCheckYourAnswers page when false" in {
        navigator.nextPage(
          PartnershipWorksReferenceNumberYesNoPage,
          CheckMode,
          emptyUserAnswers.setOrException(PartnershipWorksReferenceNumberYesNoPage, false)
        ) mustBe partnershipCYA
      }

      "must go from a PartnershipWorksReferenceNumberYesNoPage to journey recovery page when incomplete info provided" in {
        navigator.nextPage(
          PartnershipWorksReferenceNumberYesNoPage,
          CheckMode,
          emptyUserAnswers
        ) mustBe journeyRecovery
      }

      "must go from PartnershipAddressYesNoPage to the address lookup on-ramp when true and address not yet filled in CheckMode" in {
        navigator.nextPage(
          PartnershipAddressYesNoPage,
          CheckMode,
          emptyUserAnswers.setOrException(PartnershipAddressYesNoPage, true)
        ) mustBe controllers.add.partnership.routes.PartnershipAddressController.redirectToAddressLookup(
          Some(CheckMode.toString)
        )
      }

      "must go from PartnershipAddressYesNoPage to PartnershipCheckYourAnswers when true and address already filled in CheckMode" in {
        val address = Address("1 Test Street", addressLine3 = Some("Town"), postcode = Some("AA1 1AA"))
        navigator.nextPage(
          PartnershipAddressYesNoPage,
          CheckMode,
          emptyUserAnswers
            .setOrException(PartnershipAddressYesNoPage, true)
            .setOrException(PartnershipAddressPage, address)
        ) mustBe partnershipCYA
      }

      "must go from PartnershipAddressYesNoPage to PartnershipCheckYourAnswers when false in CheckMode" in {
        navigator.nextPage(
          PartnershipAddressYesNoPage,
          CheckMode,
          emptyUserAnswers.setOrException(PartnershipAddressYesNoPage, false)
        ) mustBe partnershipCYA
      }

      "must go from a PartnershipWorksReferenceNumberPage to PartnershipCheckYourAnswers page in CheckMode" in {
        navigator.nextPage(
          PartnershipWorksReferenceNumberPage,
          CheckMode,
          emptyUserAnswers.setOrException(PartnershipUniqueTaxpayerReferencePage, "UTR-123")
        ) mustBe partnershipCYA
      }

      "must go from PartnershipAddressYesNoPage to JourneyRecovery when no answer in CheckMode" in {
        navigator.nextPage(
          PartnershipAddressYesNoPage,
          CheckMode,
          emptyUserAnswers
        ) mustBe journeyRecovery
      }

      "must go to PartnershipCheckYourAnswersController when answer is false in CheckMode" in {
        val answers =
          emptyUserAnswers
            .set(PartnershipHasUtrYesNoPage, false)
            .success
            .value

        navigator.nextPage(
          PartnershipHasUtrYesNoPage,
          CheckMode,
          answers
        ) mustBe controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad()
      }

      "must go from a PartnershipNominatedPartnerNamePage to PartnershipCheckYourAnswers in CheckMode" in {
        navigator.nextPage(
          PartnershipNominatedPartnerNamePage,
          CheckMode,
          UserAnswers("id")
        ) mustBe partnershipCYA
      }

      "must go from PartnershipNominatedPartnerNinoYesNoPage to PartnershipNominatedPartnerNinoController when true in CheckMode" in {
        navigator.nextPage(
          PartnershipNominatedPartnerNinoYesNoPage,
          CheckMode,
          emptyUserAnswers.setOrException(PartnershipNominatedPartnerNinoYesNoPage, true)
        ) mustBe controllers.add.partnership.routes.PartnershipNominatedPartnerNinoController.onPageLoad(CheckMode)
      }

      "must go from PartnershipHasUtrYesNoPage to JourneyRecovery when no answer is present in CheckMode" in {
        navigator.nextPage(
          PartnershipHasUtrYesNoPage,
          CheckMode,
          emptyUserAnswers
        ) mustBe routes.JourneyRecoveryController.onPageLoad()
      }

      "must go from PartnershipNominatedPartnerNinoYesNoPage to PartnershipCheckYourAnswers when false in CheckMode" in {
        navigator.nextPage(
          PartnershipNominatedPartnerNinoYesNoPage,
          CheckMode,
          emptyUserAnswers.setOrException(PartnershipNominatedPartnerNinoYesNoPage, false)
        ) mustBe partnershipCYA
      }

      "must go from PartnershipNominatedPartnerNinoYesNoPage to JourneyRecovery when no answer in CheckMode" in {
        navigator.nextPage(
          PartnershipNominatedPartnerNinoYesNoPage,
          CheckMode,
          emptyUserAnswers
        ) mustBe journeyRecovery
      }

      "must go from a PartnershipUniqueTaxpayerReference to PartnershipCheckYourAnswers page in CheckMode" in {
        navigator.nextPage(
          PartnershipUniqueTaxpayerReferencePage,
          CheckMode,
          emptyUserAnswers.setOrException(PartnershipUniqueTaxpayerReferencePage, "5860920998")
        ) mustBe partnershipCYA
      }

      "must go from PartnershipNominatedPartnerCrnYesNo" - {
        "to next page when answer is Yes" in {
          val answers = UserAnswers(userAnswersId).set(PartnershipNominatedPartnerCrnYesNoPage, true).success.value

          navigator.nextPage(
            PartnershipNominatedPartnerCrnYesNoPage,
            CheckMode,
            answers
          ) mustBe controllers.add.partnership.routes.PartnershipNominatedPartnerCrnController
            .onPageLoad(CheckMode)
        }

        "to PartnershipCheckYourAnswers when answer is No" in {
          val answers = UserAnswers(userAnswersId).set(PartnershipNominatedPartnerCrnYesNoPage, false).success.value

          navigator.nextPage(
            PartnershipNominatedPartnerCrnYesNoPage,
            CheckMode,
            answers
          ) mustBe partnershipCYA
        }

        "to JourneyRecoveryPage when answer is not present" in {
          navigator.nextPage(
            PartnershipNominatedPartnerCrnYesNoPage,
            CheckMode,
            emptyUserAnswers
          ) mustBe routes.JourneyRecoveryController.onPageLoad()
        }
      }

      "must go from a PartnershipNominatedPartnerUtrYesNoPage to next page when true" in {
        navigator.nextPage(
          PartnershipNominatedPartnerUtrYesNoPage,
          CheckMode,
          emptyUserAnswers.setOrException(PartnershipNominatedPartnerUtrYesNoPage, true)
        ) mustBe controllers.add.partnership.routes.PartnershipNominatedPartnerUtrController.onPageLoad(CheckMode)
      }

      "must go from a PartnershipNominatedPartnerUtrYesNoPage to PartnershipCheckYourAnswers page when false" in {
        navigator.nextPage(
          PartnershipNominatedPartnerUtrYesNoPage,
          CheckMode,
          emptyUserAnswers.setOrException(PartnershipNominatedPartnerUtrYesNoPage, false)
        ) mustBe partnershipCYA
      }

      "must go from a PartnershipNominatedPartnerUtrYesNoPage to journey recovery page when incomplete info provided" in {
        navigator.nextPage(
          PartnershipNominatedPartnerUtrYesNoPage,
          CheckMode,
          emptyUserAnswers
        ) mustBe journeyRecovery
      }

      "must go from PartnershipNominatedPartnerUtrYesNoPage to CYA when answer is false in CheckMode" in {
        val answers =
          emptyUserAnswers
            .set(PartnershipNominatedPartnerUtrYesNoPage, false)
            .success
            .value

        navigator.nextPage(
          PartnershipNominatedPartnerUtrYesNoPage,
          CheckMode,
          answers
        ) mustBe controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad()
      }

      "must go from PartnershipNominatedPartnerUtrYesNoPage to CYA when answer is true in CheckMode and UTR is already provided" in {
        val answers =
          emptyUserAnswers
            .set(PartnershipNominatedPartnerUtrYesNoPage, true)
            .success
            .value
            .set(PartnershipNominatedPartnerUtrPage, "1234567890")
            .success
            .value

        navigator.nextPage(
          PartnershipNominatedPartnerUtrYesNoPage,
          CheckMode,
          answers
        ) mustBe controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad()
      }

      "must go from PartnershipNominatedPartnerCrnYesNoPage to PartnershipCheckYourAnswersController when answer is false in CheckMode" in {
        val answers =
          emptyUserAnswers
            .set(PartnershipNominatedPartnerCrnYesNoPage, false)
            .success
            .value

        navigator.nextPage(
          PartnershipNominatedPartnerCrnYesNoPage,
          CheckMode,
          answers
        ) mustBe controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad()
      }

      "must go from PartnershipNominatedPartnerCrnYesNoPage to PartnershipCheckYourAnswersController when answer is true in CheckMode and CRN already exists" in {
        val answers =
          emptyUserAnswers
            .set(PartnershipNominatedPartnerCrnYesNoPage, true)
            .success
            .value
            .set(PartnershipNominatedPartnerCrnPage, "12345678")
            .success
            .value

        navigator.nextPage(
          PartnershipNominatedPartnerCrnYesNoPage,
          CheckMode,
          answers
        ) mustBe controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad()
      }

      "must go from PartnershipNominatedPartnerNinoYesNoPage to PartnershipCheckYourAnswersController when answer is false in CheckMode" in {
        val answers =
          emptyUserAnswers
            .set(PartnershipNominatedPartnerNinoYesNoPage, false)
            .success
            .value

        navigator.nextPage(
          PartnershipNominatedPartnerNinoYesNoPage,
          CheckMode,
          answers
        ) mustBe controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad()
      }

      "must go from PartnershipNominatedPartnerNinoYesNoPage to PartnershipCheckYourAnswersController when answer is true in CheckMode and NINO is already provided" in {
        val answers =
          emptyUserAnswers
            .set(PartnershipNominatedPartnerNinoYesNoPage, true)
            .success
            .value
            .set(PartnershipNominatedPartnerNinoPage, "AA123456A")
            .success
            .value

        navigator.nextPage(
          PartnershipNominatedPartnerNinoYesNoPage,
          CheckMode,
          answers
        ) mustBe controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad()
      }

      "must go from PartnershipNominatedPartnerUtrPage to PartnershipCheckYourAnswers in CheckMode" in {

        val answers =
          emptyUserAnswers
            .set(PartnershipNominatedPartnerUtrPage, "1234567890")
            .success
            .value

        navigator.nextPage(
          PartnershipNominatedPartnerUtrPage,
          CheckMode,
          answers
        ) mustBe partnershipCYA
      }

      "must go from PartnershipNamePage to PartnershipCheckYourAnswersController in CheckMode" in {
        navigator.nextPage(
          PartnershipNamePage,
          CheckMode,
          emptyUserAnswers
        ) mustBe controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad()
      }

      "PartnershipNominatedPartnerNinoPage in CheckMode" - {
        "must go from PartnershipNominatedPartnerNinoPage to PartnershipCheckYourAnswersController in CheckMode" in {
          val answersWithNino =
            emptyUserAnswers
              .set(PartnershipNominatedPartnerNinoPage, "AA123456A")
              .success
              .value

          navigator.nextPage(
            PartnershipNominatedPartnerNinoPage,
            CheckMode,
            answersWithNino
          ) mustBe controllers.add.partnership.routes.PartnershipCheckYourAnswersController
            .onPageLoad()
        }

        "to JourneyRecovery when answer is missing" in {
          navigator.nextPage(
            PartnershipNominatedPartnerNinoPage,
            CheckMode,
            emptyUserAnswers
          ) mustBe routes.JourneyRecoveryController.onPageLoad()
        }
      }

      "PartnershipHasUtrYesNoPage in CheckMode" - {

        "must go to PartnershipUniqueTaxpayerReferenceController when answer is true" in {
          navigator.nextPage(
            PartnershipHasUtrYesNoPage,
            CheckMode,
            emptyUserAnswers.setOrException(PartnershipHasUtrYesNoPage, true)
          ) mustBe controllers.add.partnership.routes.PartnershipUniqueTaxpayerReferenceController.onPageLoad(CheckMode)
        }

        "must go to PartnershipCheckYourAnswersController when answer is true in CheckMode and UTR is already provided" in {
          val answers =
            emptyUserAnswers
              .set(PartnershipHasUtrYesNoPage, true)
              .success
              .value
              .set(PartnershipUniqueTaxpayerReferencePage, "1234567890")
              .success
              .value

          navigator.nextPage(
            PartnershipHasUtrYesNoPage,
            CheckMode,
            answers
          ) mustBe controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad()
        }

        "must go to PartnershipCheckYourAnswersController when answer is false" in {
          navigator.nextPage(
            PartnershipHasUtrYesNoPage,
            CheckMode,
            emptyUserAnswers.setOrException(PartnershipHasUtrYesNoPage, false)
          ) mustBe controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad()
        }

        "must go to JourneyRecovery when answer is missing" in {
          navigator.nextPage(
            PartnershipHasUtrYesNoPage,
            CheckMode,
            emptyUserAnswers
          ) mustBe journeyRecovery
        }
      }

      "PartnershipNominatedPartnerCrnPage in CheckMode" - {
        "must go from PartnershipNominatedPartnerCrnPage to PartnershipCheckYourAnswersController when CRN exists in CheckMode" in {

          val answers =
            emptyUserAnswers
              .set(PartnershipNominatedPartnerCrnPage, "12345678")
              .success
              .value

          navigator.nextPage(
            PartnershipNominatedPartnerCrnPage,
            CheckMode,
            answers
          ) mustBe controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad()
        }

        "must go to JourneyRecovery when answer is missing" in {
          navigator.nextPage(
            PartnershipNominatedPartnerCrnPage,
            CheckMode,
            emptyUserAnswers
          ) mustBe routes.JourneyRecoveryController.onPageLoad()
        }
      }

      "must go from AddPartnershipContactMethodsYesNo" - {
        "to CYA when answer is Yes and PartnershipContactMethodOptions already answered" in {
          val answers = emptyUserAnswers
            .setOrException(AddPartnershipContactMethodsYesNoPage, true)
            .setOrException(
              PartnershipContactMethodOptionsPage,
              Set(ContactMethodOptions.Email, ContactMethodOptions.Phone)
            )

          navigator.nextPage(
            AddPartnershipContactMethodsYesNoPage,
            CheckMode,
            answers
          ) mustBe controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad()
        }

        "to PartnershipContactMethodOptions page when answer is Yes and PartnershipContactMethodOptions not yet answered" in {
          val answers = emptyUserAnswers.setOrException(AddPartnershipContactMethodsYesNoPage, true)

          navigator.nextPage(
            AddPartnershipContactMethodsYesNoPage,
            CheckMode,
            answers
          ) mustBe controllers.add.partnership.routes.PartnershipContactMethodOptionsController.onPageLoad(CheckMode)
        }

        "to CYA when answer is No" in {
          val answers = emptyUserAnswers.set(AddPartnershipContactMethodsYesNoPage, false).success.value

          navigator.nextPage(
            AddPartnershipContactMethodsYesNoPage,
            CheckMode,
            answers
          ) mustBe controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad()
        }

        "to JourneyRecovery when answer is not present" in {
          navigator.nextPage(
            AddPartnershipContactMethodsYesNoPage,
            CheckMode,
            emptyUserAnswers
          ) mustBe journeyRecovery
        }
      }

      "must go from PartnershipContactMethodOptions" - {
        "to PartnershipEmailAddressPage when Email is selected and Email answer not exist" in {
          val answers = emptyUserAnswers
            .set(
              PartnershipContactMethodOptionsPage,
              Set(ContactMethodOptions.Email)
            )
            .success
            .value

          navigator.nextPage(
            PartnershipContactMethodOptionsPage,
            CheckMode,
            answers
          ) mustBe controllers.add.partnership.routes.PartnershipEmailAddressController.onPageLoad(CheckMode)
        }

        "to PartnershipEmailAddressPage when Email, Phone and Mobile are selected and no Email answer not exist" in {
          val answers = emptyUserAnswers
            .set(
              PartnershipContactMethodOptionsPage,
              Set(ContactMethodOptions.Email, ContactMethodOptions.Phone, ContactMethodOptions.Mobile)
            )
            .success
            .value

          navigator.nextPage(
            PartnershipContactMethodOptionsPage,
            CheckMode,
            answers
          ) mustBe controllers.add.partnership.routes.PartnershipEmailAddressController.onPageLoad(CheckMode)
        }

        "to CYA when only Email is selected and Email answer exists" in {
          val answers = emptyUserAnswers
            .set(PartnershipContactMethodOptionsPage, Set(ContactMethodOptions.Email))
            .success
            .value
            .set(PartnershipEmailAddressPage, "test@test.com")
            .success
            .value

          navigator.nextPage(
            PartnershipContactMethodOptionsPage,
            CheckMode,
            answers
          ) mustBe controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad()
        }

        "to PartnershipPhoneNumberPage when Phone is selected (Email is not selected) and Phone answer not exists" in {
          val answers = emptyUserAnswers
            .set(
              PartnershipContactMethodOptionsPage,
              Set(ContactMethodOptions.Phone)
            )
            .success
            .value

          navigator.nextPage(
            PartnershipContactMethodOptionsPage,
            CheckMode,
            answers
          ) mustBe controllers.add.partnership.routes.PartnershipPhoneNumberController.onPageLoad(CheckMode)
        }

        "to CYA when only Phone is selected and Phone answer exists" in {
          val answers = emptyUserAnswers
            .set(PartnershipContactMethodOptionsPage, Set(ContactMethodOptions.Phone))
            .success
            .value
            .set(PartnershipPhoneNumberPage, "01234567890")
            .success
            .value

          navigator.nextPage(
            PartnershipContactMethodOptionsPage,
            CheckMode,
            answers
          ) mustBe controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad()
        }

        "to CYA when only Mobile is selected and Mobile answer exists" in {
          val answers = emptyUserAnswers
            .set(PartnershipContactMethodOptionsPage, Set(ContactMethodOptions.Mobile))
            .success
            .value
            .set(PartnershipMobileNumberPage, "01234567890")
            .success
            .value

          navigator.nextPage(
            PartnershipContactMethodOptionsPage,
            CheckMode,
            answers
          ) mustBe controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad()
        }

        "to PartnershipPhoneNumberPage when Email and Phone are selected and Email answer exists, Phone answer not exists" in {
          val answers = emptyUserAnswers
            .set(PartnershipContactMethodOptionsPage, Set(ContactMethodOptions.Email, ContactMethodOptions.Phone))
            .success
            .value
            .set(PartnershipEmailAddressPage, "test@test.com")
            .success
            .value

          navigator.nextPage(
            PartnershipContactMethodOptionsPage,
            CheckMode,
            answers
          ) mustBe controllers.add.partnership.routes.PartnershipPhoneNumberController.onPageLoad(CheckMode)
        }

        "to PartnershipMobileNumberPage when Email, Phone and Mobile are selected and Email and Phone answer exists, Mobile answer not exists" in {
          val answers = emptyUserAnswers
            .set(
              PartnershipContactMethodOptionsPage,
              Set(ContactMethodOptions.Email, ContactMethodOptions.Phone, ContactMethodOptions.Mobile)
            )
            .success
            .value
            .set(PartnershipEmailAddressPage, "test@test.com")
            .success
            .value
            .set(PartnershipPhoneNumberPage, "01234567890")
            .success
            .value

          navigator.nextPage(
            PartnershipContactMethodOptionsPage,
            CheckMode,
            answers
          ) mustBe controllers.add.partnership.routes.PartnershipMobileNumberController.onPageLoad(CheckMode)
        }

        "to JourneyRecovery when answer is not present" in {
          navigator.nextPage(
            PartnershipContactMethodOptionsPage,
            CheckMode,
            emptyUserAnswers
          ) mustBe journeyRecovery
        }
      }

      "must go from PartnershipEmailAddressPage" - {

        "to PartnershipCheckYourAnswers when no missing ContactMethodOptions answer" in {
          navigator.nextPage(
            PartnershipEmailAddressPage,
            CheckMode,
            emptyUserAnswers
              .setOrException(
                PartnershipContactMethodOptionsPage,
                Set(ContactMethodOptions.Email, ContactMethodOptions.Phone, ContactMethodOptions.Mobile)
              )
              .setOrException(PartnershipEmailAddressPage, "test@test.com")
              .setOrException(PartnershipPhoneNumberPage, "1234567")
              .setOrException(PartnershipMobileNumberPage, "1234567")
          ) mustBe controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad()
        }

        "to PartnershipPhoneNumberPage when PartnershipPhoneNumber is missing from ContactMethodOptions answer" in {
          navigator.nextPage(
            PartnershipEmailAddressPage,
            CheckMode,
            emptyUserAnswers
              .setOrException(
                PartnershipContactMethodOptionsPage,
                Set(ContactMethodOptions.Email, ContactMethodOptions.Phone, ContactMethodOptions.Mobile)
              )
              .setOrException(PartnershipEmailAddressPage, "test@test.com")
              .setOrException(PartnershipMobileNumberPage, "1234567")
          ) mustBe controllers.add.partnership.routes.PartnershipPhoneNumberController.onPageLoad(CheckMode)
        }

        "to PartnershipMobileNumberPage when PartnershipMobileNumber is missing from ContactMethodOptions answer" in {
          navigator.nextPage(
            PartnershipEmailAddressPage,
            CheckMode,
            emptyUserAnswers
              .setOrException(
                PartnershipContactMethodOptionsPage,
                Set(ContactMethodOptions.Email, ContactMethodOptions.Phone, ContactMethodOptions.Mobile)
              )
              .setOrException(PartnershipEmailAddressPage, "test@test.com")
              .setOrException(PartnershipPhoneNumberPage, "1234567")
          ) mustBe controllers.add.partnership.routes.PartnershipMobileNumberController.onPageLoad(CheckMode)
        }

        "to JourneyRecovery when PartnershipContactMethodOptions answer is not present" in {
          navigator.nextPage(
            PartnershipEmailAddressPage,
            CheckMode,
            UserAnswers("id")
          ) mustBe journeyRecovery
        }

        "to JourneyRecovery when Email is not in the selected PartnershipContactMethodOptions" in {
          navigator.nextPage(
            PartnershipEmailAddressPage,
            CheckMode,
            emptyUserAnswers
              .setOrException(
                PartnershipContactMethodOptionsPage,
                Set(ContactMethodOptions.Phone, ContactMethodOptions.Mobile)
              )
          ) mustBe journeyRecovery
        }
      }

      "must go from PartnershipPhoneNumberPage" - {

        "to PartnershipCheckYourAnswers when no missing ContactMethodOptions answer" in {
          navigator.nextPage(
            PartnershipPhoneNumberPage,
            CheckMode,
            emptyUserAnswers
              .setOrException(
                PartnershipContactMethodOptionsPage,
                Set(ContactMethodOptions.Email, ContactMethodOptions.Phone, ContactMethodOptions.Mobile)
              )
              .setOrException(PartnershipEmailAddressPage, "test@test.com")
              .setOrException(PartnershipPhoneNumberPage, "1234567")
              .setOrException(PartnershipMobileNumberPage, "1234567")
          ) mustBe controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad()
        }

        "to PartnershipMobileNumberPage when PartnershipMobileNumber is missing from ContactMethodOptions answer" in {
          navigator.nextPage(
            PartnershipPhoneNumberPage,
            CheckMode,
            emptyUserAnswers
              .setOrException(
                PartnershipContactMethodOptionsPage,
                Set(ContactMethodOptions.Email, ContactMethodOptions.Phone, ContactMethodOptions.Mobile)
              )
              .setOrException(PartnershipEmailAddressPage, "test@test.com")
              .setOrException(PartnershipPhoneNumberPage, "1234567")
          ) mustBe controllers.add.partnership.routes.PartnershipMobileNumberController.onPageLoad(CheckMode)
        }

        "to JourneyRecovery when PartnershipContactMethodOptions answer is not present" in {
          navigator.nextPage(
            PartnershipPhoneNumberPage,
            CheckMode,
            UserAnswers("id")
          ) mustBe journeyRecovery
        }

        "to JourneyRecovery when Phone is not in the selected PartnershipContactMethodOptions" in {
          navigator.nextPage(
            PartnershipPhoneNumberPage,
            CheckMode,
            emptyUserAnswers
              .setOrException(
                PartnershipContactMethodOptionsPage,
                Set(ContactMethodOptions.Email, ContactMethodOptions.Mobile)
              )
          ) mustBe journeyRecovery
        }
      }

      "must go from PartnershipMobileNumberPage" - {

        "to PartnershipCheckYourAnswers" in {
          navigator.nextPage(
            PartnershipMobileNumberPage,
            CheckMode,
            emptyUserAnswers
              .setOrException(
                PartnershipContactMethodOptionsPage,
                Set(ContactMethodOptions.Email, ContactMethodOptions.Phone, ContactMethodOptions.Mobile)
              )
              .setOrException(PartnershipEmailAddressPage, "test@test.com")
              .setOrException(PartnershipPhoneNumberPage, "1234567")
              .setOrException(PartnershipMobileNumberPage, "1234567")
          ) mustBe controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad()
        }

        "to JourneyRecovery when PartnershipContactMethodOptions answer is not present" in {
          navigator.nextPage(
            PartnershipMobileNumberPage,
            CheckMode,
            UserAnswers("id")
          ) mustBe journeyRecovery
        }

        "to JourneyRecovery when Mobile is not in the selected PartnershipContactMethodOptions" in {
          navigator.nextPage(
            PartnershipMobileNumberPage,
            CheckMode,
            emptyUserAnswers
              .setOrException(
                PartnershipContactMethodOptionsPage,
                Set(ContactMethodOptions.Email, ContactMethodOptions.Phone)
              )
          ) mustBe journeyRecovery
        }
      }

      "must go to CheckYourAnswersController for an unknown page in CheckMode (default case _)" in {
        case object UnknownPage extends QuestionPage[String] {
          override def path: JsPath = JsPath \ "unknown-page"
        }

        navigator.nextPage(
          UnknownPage,
          CheckMode,
          emptyUserAnswers
        ) mustBe controllers.add.routes.CheckYourAnswersController.onPageLoad()
      }

    }

  }

}
