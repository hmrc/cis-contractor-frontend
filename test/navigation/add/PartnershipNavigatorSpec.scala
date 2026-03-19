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
import models.add.InternationalAddress
import models.contact.ContactOptions
import models.{CheckMode, NormalMode, UserAnswers}
import pages.Page
import pages.add.partnership.*

class PartnershipNavigatorSpec extends SpecBase {

  val navigator = new PartnershipNavigator
  private lazy val journeyRecovery = routes.JourneyRecoveryController.onPageLoad()
  private lazy val CYA = controllers.add.routes.CheckYourAnswersController.onPageLoad()
  private lazy val partnershipCYA  =
    controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad()


  "PartnershipNavigator" - {

    "in Normal mode" - {

      "must go from a page that doesn't exist in the route map to Index" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, NormalMode, UserAnswers("id")) mustBe routes.IndexController.onPageLoad()
      }

      "must go from PartnershipNamePage to PartnershipHasUtrYesNoController in NormalMode" in {
        navigator.nextPage(
          PartnershipNamePage,
          NormalMode,
          emptyUserAnswers
        ) mustBe controllers.add.partnership.routes.PartnershipHasUtrYesNoController.onPageLoad(NormalMode)
      }

      "must go from a PartnershipHasUtrYesNo to PartnershipUniqueTaxpayerReferencePage when true" in {
        navigator.nextPage(
          PartnershipHasUtrYesNoPage,
          NormalMode,
          emptyUserAnswers.setOrException(PartnershipHasUtrYesNoPage, true)
        ) mustBe controllers.add.partnership.routes.PartnershipUniqueTaxpayerReferenceController.onPageLoad(NormalMode)
      }

      "must go from a PartnershipHasUtrYesNo to PartnershipWorksReferenceNumberYesNoCPage when false" in {
        navigator.nextPage(
          PartnershipHasUtrYesNoPage,
          NormalMode,
          emptyUserAnswers.setOrException(PartnershipHasUtrYesNoPage, false)
        ) mustBe controllers.add.partnership.routes.PartnershipWorksReferenceNumberYesNoController
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

      "must go from a PartnershipUniqueTaxpayerReference to PartnershipWorksReferenceNumberYesNo page" in {
        navigator.nextPage(
          PartnershipUniqueTaxpayerReferencePage,
          NormalMode,
          emptyUserAnswers.setOrException(PartnershipUniqueTaxpayerReferencePage, "5860920998")
        ) mustBe controllers.add.partnership.routes.PartnershipWorksReferenceNumberYesNoController
          .onPageLoad(NormalMode)
      }

      "must go from a PartnershipUniqueTaxpayerReference to PartnershipCheckYourAnswers page in CheckMode" in {
        navigator.nextPage(
          PartnershipUniqueTaxpayerReferencePage,
          CheckMode,
          emptyUserAnswers.setOrException(PartnershipUniqueTaxpayerReferencePage, "5860920998")
        ) mustBe partnershipCYA
      }



      "must go from a PartnershipAddressPage to PartnershipChooseContactDetailsController in NormalMode" in {
        navigator.nextPage(
          PartnershipAddressPage,
          NormalMode,
          UserAnswers("id")
        ) mustBe controllers.add.partnership.routes.PartnershipChooseContactDetailsController.onPageLoad(NormalMode)
      }



      "must go from a PartnershipWorksReferenceNumberYesNoPage to PartnershipWorksReferenceNumber page when true" in {
        navigator.nextPage(
          PartnershipWorksReferenceNumberYesNoPage,
          NormalMode,
          emptyUserAnswers.setOrException(PartnershipWorksReferenceNumberYesNoPage, true)
        ) mustBe controllers.add.partnership.routes.PartnershipWorksReferenceNumberController
          .onPageLoad(NormalMode)
      }

      "must go from a PartnershipWorksReferenceNumberYesNoPage to PartnershipAddressYesNo page when false" in {
        navigator.nextPage(
          PartnershipWorksReferenceNumberYesNoPage,
          NormalMode,
          emptyUserAnswers.setOrException(PartnershipWorksReferenceNumberYesNoPage, false)
        ) mustBe controllers.add.partnership.routes.PartnershipAddressYesNoController.onPageLoad(NormalMode)
      }

      "must go from a PartnershipWorksReferenceNumberYesNoPage to journey recovery when incomplete info provided" in {
        navigator.nextPage(
          PartnershipWorksReferenceNumberYesNoPage,
          NormalMode,
          emptyUserAnswers
        ) mustBe journeyRecovery
      }

      "must go from a PartnershipWorksReferenceNumberPage to PartnershipAddressYesNo page" in {
        navigator.nextPage(
          PartnershipWorksReferenceNumberPage,
          NormalMode,
          emptyUserAnswers.setOrException(PartnershipUniqueTaxpayerReferencePage, "UTR-123")
        ) mustBe controllers.add.partnership.routes.PartnershipAddressYesNoController.onPageLoad(NormalMode)
      }

      "must go from a PartnershipWorksReferenceNumberPage to PartnershipCheckYourAnswers page in CheckMode" in {
        navigator.nextPage(
          PartnershipWorksReferenceNumberPage,
          CheckMode,
          emptyUserAnswers.setOrException(PartnershipUniqueTaxpayerReferencePage, "UTR-123")
        ) mustBe partnershipCYA
      }

      "must go from PartnershipAddressYesNoPage to PartnershipAddressController when true in NormalMode" in {
        navigator.nextPage(
          PartnershipAddressYesNoPage,
          NormalMode,
          emptyUserAnswers.setOrException(PartnershipAddressYesNoPage, true)
        ) mustBe controllers.add.partnership.routes.PartnershipAddressController.onPageLoad(NormalMode)
      }

      "must go from PartnershipAddressYesNoPage to PartnershipChooseContactDetailsController when false in NormalMode" in {
        navigator.nextPage(
          PartnershipAddressYesNoPage,
          NormalMode,
          emptyUserAnswers.setOrException(PartnershipAddressYesNoPage, false)
        ) mustBe controllers.add.partnership.routes.PartnershipChooseContactDetailsController.onPageLoad(NormalMode)
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
      "must go from a PartnershipNominatedPartnerNamePage to PartnershipNominatedPartnerNamePage" in {
        navigator.nextPage(
          PartnershipNominatedPartnerNamePage,
          NormalMode,
          UserAnswers("id")
        ) mustBe controllers.add.partnership.routes.PartnershipNominatedPartnerNameController.onPageLoad(NormalMode)
      }

      "must go from PartnershipNominatedPartnerCrnYesNo" - {
        "to next page when answer is Yes" in {
          navigator.nextPage(
            PartnershipNominatedPartnerCrnYesNoPage,
            NormalMode,
            emptyUserAnswers.setOrException(PartnershipNominatedPartnerCrnYesNoPage, true)
          ) mustBe controllers.add.partnership.routes.PartnershipNominatedPartnerCrnYesNoController
            .onPageLoad(NormalMode)
        }

        "to next page when answer is No" in {
          navigator.nextPage(
            PartnershipNominatedPartnerCrnYesNoPage,
            NormalMode,
            emptyUserAnswers.setOrException(PartnershipNominatedPartnerCrnYesNoPage, false)
          ) mustBe controllers.add.partnership.routes.PartnershipNominatedPartnerCrnYesNoController
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

      "must go from a PartnershipEmailAddressPage to PartnershipEmailAddressPage" in {
        navigator.nextPage(
          PartnershipEmailAddressPage,
          NormalMode,
          UserAnswers("id")
        ) mustBe controllers.add.partnership.routes.PartnershipEmailAddressController.onPageLoad(NormalMode)
      }

      "must go from PartnershipChooseContactDetailsPage" - {
        "to itself when Email is selected" in {
          navigator.nextPage(
            PartnershipChooseContactDetailsPage,
            NormalMode,
            emptyUserAnswers.setOrException(
              PartnershipChooseContactDetailsPage,
              ContactOptions.Email
            )
          ) mustBe controllers.add.partnership.routes.PartnershipChooseContactDetailsController.onPageLoad(NormalMode)
        }

        "to itself when Phone is selected" in {
          navigator.nextPage(
            PartnershipChooseContactDetailsPage,
            NormalMode,
            emptyUserAnswers.setOrException(
              PartnershipChooseContactDetailsPage,
              ContactOptions.Phone
            )
          ) mustBe controllers.add.partnership.routes.PartnershipChooseContactDetailsController.onPageLoad(NormalMode)
        }

        "to itself when Mobile is selected" in {
          navigator.nextPage(
            PartnershipChooseContactDetailsPage,
            NormalMode,
            emptyUserAnswers.setOrException(
              PartnershipChooseContactDetailsPage,
              ContactOptions.Mobile
            )
          ) mustBe controllers.add.partnership.routes.PartnershipChooseContactDetailsController.onPageLoad(NormalMode)
        }

        "to itself when NoDetails is selected" in {
          navigator.nextPage(
            PartnershipChooseContactDetailsPage,
            NormalMode,
            emptyUserAnswers.setOrException(
              PartnershipChooseContactDetailsPage,
              ContactOptions.NoDetails
            )
          ) mustBe controllers.add.partnership.routes.PartnershipChooseContactDetailsController.onPageLoad(NormalMode)
        }

        "to JourneyRecoveryPage when answer is not present" in {
          navigator.nextPage(
            PartnershipChooseContactDetailsPage,
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
        ) mustBe controllers.add.partnership.routes.PartnershipNominatedPartnerUtrYesNoController
          .onPageLoad(NormalMode)
      }

      "must go from a PartnershipNominatedPartnerUtrYesNoPage to next page when false" in {
        navigator.nextPage(
          PartnershipNominatedPartnerUtrYesNoPage,
          NormalMode,
          emptyUserAnswers.setOrException(PartnershipNominatedPartnerUtrYesNoPage, false)
        ) mustBe controllers.add.partnership.routes.PartnershipNominatedPartnerUtrYesNoController
          .onPageLoad(NormalMode)
      }

      "must go from a PartnershipNominatedPartnerUtrYesNoPage to journey recovery when incomplete info provided" in {
        navigator.nextPage(
          PartnershipNominatedPartnerUtrYesNoPage,
          NormalMode,
          emptyUserAnswers
        ) mustBe journeyRecovery
      }

      "must go from a PartnershipNominatedPartnerUtrPage to PartnershipNominatedPartnerUtrController in NormalMode" in {
        navigator.nextPage(
          PartnershipNominatedPartnerUtrPage,
          NormalMode,
          emptyUserAnswers
        ) mustBe controllers.add.partnership.routes.PartnershipNominatedPartnerUtrController.onPageLoad(NormalMode)
      }

      "must go from a PartnershipPhoneNumberPage to next Page" in {
        navigator.nextPage(
          PartnershipPhoneNumberPage,
          NormalMode,
          UserAnswers("id")
        ) mustBe controllers.add.partnership.routes.PartnershipPhoneNumberController.onPageLoad(NormalMode)
      }

      "must go from a PartnershipMobileNumberPage to next Page" in {
        navigator.nextPage(
          PartnershipMobileNumberPage,
          NormalMode,
          UserAnswers("id")
        ) mustBe controllers.add.partnership.routes.PartnershipMobileNumberController.onPageLoad(NormalMode)
      }

      "must go from PartnershipNominatedPartnerNinoPage to itself" in {
        navigator.nextPage(
          PartnershipNominatedPartnerNinoPage,
          NormalMode,
          emptyUserAnswers
        ) mustBe controllers.add.partnership.routes.PartnershipNominatedPartnerNinoController.onPageLoad(NormalMode)
      }

      "must go from PartnershipNominatedPartnerCrnPage to PartnershipNominatedPartnerCrnController" in {
        navigator.nextPage(
          PartnershipNominatedPartnerCrnPage,
          NormalMode,
          emptyUserAnswers
        ) mustBe controllers.add.partnership.routes.PartnershipNominatedPartnerCrnController.onPageLoad(NormalMode)
      }

    }

    "in Check mode" - {

      "must go from a page that doesn't exist in the edit route map to CheckYourAnswers" in {

        case object UnknownPage extends Page
        navigator.nextPage(
          UnknownPage,
          CheckMode,
          UserAnswers("id")
        ) mustBe controllers.add.routes.CheckYourAnswersController
          .onPageLoad()
      }

      "must go from a PartnershipWorksReferenceNumberYesNoPage to next page when true" in {
        navigator.nextPage(
          PartnershipWorksReferenceNumberYesNoPage,
          CheckMode,
          emptyUserAnswers.setOrException(PartnershipWorksReferenceNumberYesNoPage, true)
        ) mustBe controllers.add.partnership.routes.PartnershipWorksReferenceNumberController.onPageLoad(CheckMode)
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



      "must go from PartnershipAddressYesNoPage to PartnershipAddressController when true and address not yet filled in CheckMode" in {
        navigator.nextPage(
          PartnershipAddressYesNoPage,
          CheckMode,
          emptyUserAnswers.setOrException(PartnershipAddressYesNoPage, true)
        ) mustBe controllers.add.partnership.routes.PartnershipAddressController.onPageLoad(CheckMode)
      }

      "must go from PartnershipAddressYesNoPage to PartnershipCheckYourAnswers when true and address already filled in CheckMode" in {
        val address = InternationalAddress("1 Test Street", None, "Town", None, "AA1 1AA", "GB")
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

      "must go from PartnershipAddressYesNoPage to JourneyRecovery when no answer in CheckMode" in {
        navigator.nextPage(
          PartnershipAddressYesNoPage,
          CheckMode,
          emptyUserAnswers
        ) mustBe journeyRecovery
      }

      "must go from a PartnershipAddressPage to PartnershipCheckYourAnswers in CheckMode" in {
        navigator.nextPage(
          PartnershipAddressPage,
          CheckMode,
          emptyUserAnswers
        ) mustBe partnershipCYA
      }

      "must go from PartnershipChooseContactDetailsPage" - {

        "to itself when Email is selected" in {
          navigator.nextPage(
            PartnershipChooseContactDetailsPage,
            CheckMode,
            emptyUserAnswers.setOrException(
              PartnershipChooseContactDetailsPage,
              ContactOptions.Email
            )
          ) mustBe controllers.add.partnership.routes.PartnershipChooseContactDetailsController.onPageLoad(CheckMode)
        }

        "to itself when Phone is selected" in {
          navigator.nextPage(
            PartnershipChooseContactDetailsPage,
            CheckMode,
            emptyUserAnswers.setOrException(
              PartnershipChooseContactDetailsPage,
              ContactOptions.Phone
            )
          ) mustBe controllers.add.partnership.routes.PartnershipChooseContactDetailsController.onPageLoad(CheckMode)
        }

        "to itself when Mobile is selected" in {
          navigator.nextPage(
            PartnershipChooseContactDetailsPage,
            CheckMode,
            emptyUserAnswers.setOrException(
              PartnershipChooseContactDetailsPage,
              ContactOptions.Mobile
            )
          ) mustBe controllers.add.partnership.routes.PartnershipChooseContactDetailsController.onPageLoad(CheckMode)
        }

        "to itself when NoDetails is selected" in {
          navigator.nextPage(
            PartnershipChooseContactDetailsPage,
            CheckMode,
            emptyUserAnswers.setOrException(
              PartnershipChooseContactDetailsPage,
              ContactOptions.NoDetails
            )
          ) mustBe controllers.add.partnership.routes.PartnershipChooseContactDetailsController.onPageLoad(CheckMode)
        }

        "to PartnershipCheckYourAnswers when answer is not present" in {
          navigator.nextPage(
            PartnershipChooseContactDetailsPage,
            CheckMode,
            emptyUserAnswers
          ) mustBe partnershipCYA
        }
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

      "must go from PartnershipNominatedPartnerCrnYesNo" - {
        "to next page when answer is Yes" in {
          val answers = UserAnswers(userAnswersId).set(PartnershipNominatedPartnerCrnYesNoPage, true).success.value

          navigator.nextPage(
            PartnershipNominatedPartnerCrnYesNoPage,
            CheckMode,
            answers
          ) mustBe controllers.add.partnership.routes.PartnershipNominatedPartnerCrnYesNoController
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

      "must go from PartnershipEmailAddressPage to PartnershipCheckYourAnswers in CheckMode" in {
        navigator.nextPage(
          PartnershipEmailAddressPage,
          CheckMode,
          emptyUserAnswers
        ) mustBe partnershipCYA
      }

      "must go from a PartnershipNominatedPartnerUtrYesNoPage to next page when true" in {
        navigator.nextPage(
          PartnershipNominatedPartnerUtrYesNoPage,
          CheckMode,
          emptyUserAnswers.setOrException(PartnershipNominatedPartnerUtrYesNoPage, true)
        ) mustBe controllers.add.partnership.routes.PartnershipNominatedPartnerUtrYesNoController.onPageLoad(CheckMode)
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

      "must go from PartnershipNominatedPartnerUtrPage to PartnershipCheckYourAnswers in CheckMode" in {
        navigator.nextPage(
          PartnershipNominatedPartnerUtrPage,
          CheckMode,
          emptyUserAnswers
        ) mustBe partnershipCYA
      }

      "must go from PartnershipNamePage to PartnershipCheckYourAnswers in CheckMode" in {
        navigator.nextPage(
          PartnershipNamePage,
          CheckMode,
          emptyUserAnswers
        ) mustBe partnershipCYA
      }

      "must go from a PartnershipMobileNumberPage to PartnershipMobileNumberPage in CheckMode" in {
        navigator.nextPage(
          PartnershipMobileNumberPage,
          CheckMode,
          emptyUserAnswers
        ) mustBe controllers.add.partnership.routes.PartnershipMobileNumberController.onPageLoad(CheckMode)
      }

      "must go from a PartnershipPhoneNumberPage to CYA" in {
        navigator.nextPage(
          PartnershipPhoneNumberPage,
          CheckMode,
          UserAnswers("id")
        ) mustBe CYA
      }

      "must go from PartnershipNominatedPartnerNinoPage to PartnershipCheckYourAnswers" in {
        navigator.nextPage(
          PartnershipNominatedPartnerNinoPage,
          CheckMode,
          emptyUserAnswers
        ) mustBe partnershipCYA
      }


      "must go from PartnershipNominatedPartnerCrnPage to PartnershipCheckYourAnswers" in {
        navigator.nextPage(
          PartnershipNominatedPartnerCrnPage,
          CheckMode,
          emptyUserAnswers
        ) mustBe partnershipCYA
      }

    }

  }

}