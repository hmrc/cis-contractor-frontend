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
import pages.Page
import pages.add.company.*

class CompanyNavigatorSpec extends SpecBase {

  val navigator                    = new CompanyNavigator
  private lazy val journeyRecovery = routes.JourneyRecoveryController.onPageLoad()
  private lazy val CYA             = controllers.add.routes.CheckYourAnswersController.onPageLoad()
  private lazy val CompanyCYA      = controllers.add.company.routes.CompanyCheckYourAnswersController.onPageLoad()

  "CompanyNavigator" - {

    "in Normal mode" - {

      "must go from a page that doesn't exist in the route map to Index" in {
        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, NormalMode, UserAnswers("id")) mustBe routes.IndexController.onPageLoad()
      }

      "must go from a CompanyAddressPage to CompanyAddressPage" in {
        navigator.nextPage(
          CompanyAddressPage,
          NormalMode,
          UserAnswers("id")
        ) mustBe controllers.add.company.routes.CompanyAddressController.onPageLoad(NormalMode)
      }

      "must go from CompanyNamePage to CompanyNameController in NormalMode" in {
        navigator.nextPage(
          CompanyNamePage,
          NormalMode,
          emptyUserAnswers
        ) mustBe controllers.add.company.routes.CompanyNameController.onPageLoad(NormalMode)
      }

      "must go from CompanyContactOptionsPage" - {
        "to itself when EmailAddress is selected" in {
          navigator.nextPage(
            CompanyContactOptionsPage,
            NormalMode,
            emptyUserAnswers.setOrException(
              CompanyContactOptionsPage,
              ContactOptions.Email
            )
          ) mustBe controllers.add.company.routes.CompanyContactOptionsController.onPageLoad(NormalMode)
        }

        "to itself when PhoneNumber is selected" in {
          navigator.nextPage(
            CompanyContactOptionsPage,
            NormalMode,
            emptyUserAnswers.setOrException(
              CompanyContactOptionsPage,
              ContactOptions.Phone
            )
          ) mustBe controllers.add.company.routes.CompanyContactOptionsController.onPageLoad(NormalMode)
        }

        "to itself when MobileNumber is selected" in {
          navigator.nextPage(
            CompanyContactOptionsPage,
            NormalMode,
            emptyUserAnswers.setOrException(
              CompanyContactOptionsPage,
              ContactOptions.Mobile
            )
          ) mustBe controllers.add.company.routes.CompanyContactOptionsController.onPageLoad(NormalMode)
        }

        "to itself when NoDetails is selected" in {
          navigator.nextPage(
            CompanyContactOptionsPage,
            NormalMode,
            emptyUserAnswers.setOrException(
              CompanyContactOptionsPage,
              ContactOptions.NoDetails
            )
          ) mustBe controllers.add.company.routes.CompanyContactOptionsController.onPageLoad(NormalMode)
        }

        "to JourneyRecoveryPage when answer is not present" in {
          navigator.nextPage(
            CompanyContactOptionsPage,
            NormalMode,
            emptyUserAnswers
          ) mustBe journeyRecovery
        }
      }

      "must go from a CompanyPhoneNumberPage to next Page" in {
        navigator.nextPage(
          CompanyPhoneNumberPage,
          NormalMode,
          UserAnswers("id")
        ) mustBe controllers.add.company.routes.CompanyPhoneNumberController.onPageLoad(NormalMode)
      }

      "must go from CompanyCrnYesNo" - {
        "to next page when answer is Yes" in {
          navigator.nextPage(
            CompanyCrnYesNoPage,
            NormalMode,
            emptyUserAnswers.setOrException(CompanyCrnYesNoPage, true)
          ) mustBe controllers.add.company.routes.CompanyCrnYesNoController.onPageLoad(NormalMode)
        }

        "to next page when answer is No" in {
          navigator.nextPage(
            CompanyCrnYesNoPage,
            NormalMode,
            emptyUserAnswers.setOrException(CompanyCrnYesNoPage, false)
          ) mustBe controllers.add.company.routes.CompanyCrnYesNoController.onPageLoad(NormalMode)
        }

        "to JourneyRecoveryPage when answer is not present" in {
          navigator.nextPage(
            CompanyCrnYesNoPage,
            NormalMode,
            emptyUserAnswers
          ) mustBe journeyRecovery
        }
      }

      "must go from a CompanyAddressYesNoPage to CompanyAddressYesNoPage" in {
        navigator.nextPage(
          CompanyAddressYesNoPage,
          NormalMode,
          UserAnswers("id")
        ) mustBe controllers.add.company.routes.CompanyAddressYesNoController.onPageLoad(NormalMode)
      }

      "must go from CompanyEmailAddressPage to CompanyEmailAddressController in NormalMode" in {
        navigator.nextPage(
          CompanyEmailAddressPage,
          NormalMode,
          emptyUserAnswers
        ) mustBe controllers.add.company.routes.CompanyEmailAddressController.onPageLoad(NormalMode)
      }

      "must go from CompanyUtrYesNo" - {
        "to next page when answer is Yes" in {
          navigator.nextPage(
            CompanyUtrYesNoPage,
            NormalMode,
            emptyUserAnswers.setOrException(CompanyUtrYesNoPage, true)
          ) mustBe controllers.add.company.routes.CompanyUtrController
            .onPageLoad(NormalMode)
        }

        "to next page when answer is No" in {
          navigator.nextPage(
            CompanyUtrYesNoPage,
            NormalMode,
            emptyUserAnswers.setOrException(CompanyUtrYesNoPage, false)
          ) mustBe controllers.add.company.routes.CompanyCrnYesNoController
            .onPageLoad(NormalMode)
        }

        "to JourneyRecoveryPage when answer is not present" in {
          navigator.nextPage(
            CompanyUtrYesNoPage,
            NormalMode,
            emptyUserAnswers
          ) mustBe journeyRecovery
        }
      }

      "must go from CompanyWorksReferenceYesNo" - {
        "to next page when answer is Yes" in {
          navigator.nextPage(
            CompanyWorksReferenceYesNoPage,
            NormalMode,
            emptyUserAnswers.setOrException(CompanyWorksReferenceYesNoPage, true)
          ) mustBe controllers.add.company.routes.CompanyWorksReferenceController
            .onPageLoad(NormalMode)
        }

        "to next page when answer is No" in {
          navigator.nextPage(
            CompanyWorksReferenceYesNoPage,
            NormalMode,
            emptyUserAnswers.setOrException(CompanyWorksReferenceYesNoPage, false)
          ) mustBe controllers.add.company.routes.CompanyCheckYourAnswersController
            .onPageLoad()
        }

        "to JourneyRecoveryPage when answer is not present" in {
          navigator.nextPage(
            CompanyWorksReferenceYesNoPage,
            NormalMode,
            emptyUserAnswers
          ) mustBe journeyRecovery
        }
      }

      "must go from a CompanyWorksReferenceYesNoPage to next Page" in {
        navigator.nextPage(
          CompanyCrnPage,
          NormalMode,
          UserAnswers("id")
        ) mustBe controllers.add.company.routes.CompanyWorksReferenceYesNoController.onPageLoad(NormalMode)
      }

      "must go from a CompanyUtrPage to CompanyCrnYesNoPage" in {
        navigator.nextPage(
          CompanyUtrPage,
          NormalMode,
          UserAnswers("id")
        ) mustBe controllers.add.company.routes.CompanyCrnYesNoController.onPageLoad(NormalMode)
      }

      "must go from a CompanyMobileNumberPage to CompanyMobileNumberPage" in {
        navigator.nextPage(
          CompanyMobileNumberPage,
          NormalMode,
          UserAnswers("id")
        ) mustBe controllers.add.company.routes.CompanyMobileNumberController.onPageLoad(NormalMode)
      }

      "must go from a CompanyWorksReferencePage to CompanyCheckYourAnswerPage" in {
        navigator.nextPage(
          CompanyWorksReferencePage,
          NormalMode,
          UserAnswers("id")
        ) mustBe controllers.add.company.routes.CompanyCheckYourAnswersController.onPageLoad()
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

      "must go from CompanyNamePage to CompanyCheckYourAnswers in CheckMode" in {
        navigator.nextPage(
          CompanyNamePage,
          CheckMode,
          emptyUserAnswers
        ) mustBe controllers.add.company.routes.CompanyCheckYourAnswersController.onPageLoad()
      }

      "must go from CompanyAddressPage to CompanyAddressPage in CheckMode" in {
        navigator.nextPage(
          CompanyAddressPage,
          CheckMode,
          emptyUserAnswers
        ) mustBe controllers.add.company.routes.CompanyAddressController.onPageLoad(CheckMode)
      }

      "must go from CompanyEmailAddressPage to CompanyEmailAddressPage in CheckMode" in {
        navigator.nextPage(
          CompanyEmailAddressPage,
          CheckMode,
          emptyUserAnswers
        ) mustBe controllers.add.company.routes.CompanyEmailAddressController.onPageLoad(CheckMode)
      }

      "must go from CompanyContactOptionsPage" - {
        "to itself when EmailAddress is selected" in {
          navigator.nextPage(
            CompanyContactOptionsPage,
            CheckMode,
            emptyUserAnswers.setOrException(
              CompanyContactOptionsPage,
              ContactOptions.Email
            )
          ) mustBe controllers.add.company.routes.CompanyContactOptionsController.onPageLoad(CheckMode)
        }

        "to itself when PhoneNumber is selected" in {
          navigator.nextPage(
            CompanyContactOptionsPage,
            CheckMode,
            emptyUserAnswers.setOrException(
              CompanyContactOptionsPage,
              ContactOptions.Phone
            )
          ) mustBe controllers.add.company.routes.CompanyContactOptionsController.onPageLoad(CheckMode)
        }

        "to itself when MobileNumber is selected" in {
          navigator.nextPage(
            CompanyContactOptionsPage,
            CheckMode,
            emptyUserAnswers.setOrException(
              CompanyContactOptionsPage,
              ContactOptions.Mobile
            )
          ) mustBe controllers.add.company.routes.CompanyContactOptionsController.onPageLoad(CheckMode)
        }

        "to itself when NoDetails is selected" in {
          navigator.nextPage(
            CompanyContactOptionsPage,
            CheckMode,
            emptyUserAnswers.setOrException(
              CompanyContactOptionsPage,
              ContactOptions.NoDetails
            )
          ) mustBe controllers.add.company.routes.CompanyContactOptionsController.onPageLoad(CheckMode)
        }

        "to CYA when answer is not present" in {
          navigator.nextPage(
            CompanyContactOptionsPage,
            CheckMode,
            emptyUserAnswers
          ) mustBe CYA
        }
      }

      "must go from a CompanyPhoneNumberPage to CompanyPhoneNumberPage in CheckMode" in {
        navigator.nextPage(
          CompanyPhoneNumberPage,
          CheckMode,
          emptyUserAnswers
        ) mustBe controllers.add.company.routes.CompanyPhoneNumberController.onPageLoad(CheckMode)
      }

      "must go from CompanyCrnYesNoPage" - {
        "to next page when answer is Yes" in {
          val answers = UserAnswers(userAnswersId).set(CompanyCrnYesNoPage, true).success.value

          navigator.nextPage(
            CompanyCrnYesNoPage,
            CheckMode,
            answers
          ) mustBe controllers.add.company.routes.CompanyCrnYesNoController.onPageLoad(CheckMode)
        }

        "to Partnership CyaPage when answer is No" in {
          val answers = UserAnswers(userAnswersId).set(CompanyCrnYesNoPage, false).success.value

          navigator.nextPage(
            CompanyCrnYesNoPage,
            CheckMode,
            answers
          ) mustBe controllers.add.routes.CheckYourAnswersController.onPageLoad()
        }

        "to JourneyRecoveryPage when answer is not present" in {
          navigator.nextPage(
            CompanyCrnYesNoPage,
            CheckMode,
            emptyUserAnswers
          ) mustBe routes.JourneyRecoveryController.onPageLoad()
        }
      }

      "must go from CompanyAddressYesNoPage to CompanyAddressYesNoPage in CheckMode" in {
        navigator.nextPage(
          CompanyAddressYesNoPage,
          CheckMode,
          emptyUserAnswers
        ) mustBe controllers.add.company.routes.CompanyAddressYesNoController.onPageLoad(CheckMode)
      }

      "must go from CompanyUtrYesNo" - {
        "to next page when answer is Yes" in {
          val answers = UserAnswers(userAnswersId).set(CompanyUtrYesNoPage, true).success.value

          navigator.nextPage(
            CompanyUtrYesNoPage,
            CheckMode,
            answers
          ) mustBe controllers.add.company.routes.CompanyUtrController
            .onPageLoad(CheckMode)
        }

        "to Company CyaPage when answer is No" in {
          val answers = UserAnswers(userAnswersId).set(CompanyUtrYesNoPage, false).success.value

          navigator.nextPage(
            CompanyUtrYesNoPage,
            CheckMode,
            answers
          ) mustBe CYA
        }

        "to JourneyRecoveryPage when answer is not present" in {
          navigator.nextPage(
            CompanyUtrYesNoPage,
            CheckMode,
            emptyUserAnswers
          ) mustBe routes.JourneyRecoveryController.onPageLoad()
        }
      }

      "must go from CompanyWorksReferenceYesNo" - {
        "to next page when answer is Yes" in {
          val answers = UserAnswers(userAnswersId).set(CompanyWorksReferenceYesNoPage, true).success.value

          navigator.nextPage(
            CompanyWorksReferenceYesNoPage,
            CheckMode,
            answers
          ) mustBe controllers.add.company.routes.CompanyWorksReferenceController
            .onPageLoad(CheckMode)
        }

        "to Company CyaPage when answer is No" in {
          val answers = UserAnswers(userAnswersId).set(CompanyWorksReferenceYesNoPage, false).success.value

          navigator.nextPage(
            CompanyWorksReferenceYesNoPage,
            CheckMode,
            answers
          ) mustBe CYA
        }

        "to JourneyRecoveryPage when answer is not present" in {
          navigator.nextPage(
            CompanyWorksReferenceYesNoPage,
            CheckMode,
            emptyUserAnswers
          ) mustBe routes.JourneyRecoveryController.onPageLoad()
        }
      }

      "must go from a CompanyCrnPage to CompanyCYA in CheckMode" in {
        navigator.nextPage(
          CompanyCrnPage,
          CheckMode,
          emptyUserAnswers
        ) mustBe CompanyCYA
      }

      "must go from CompanyUtrPage to CompanyCYA in CheckMode" in {
        navigator.nextPage(
          CompanyUtrPage,
          CheckMode,
          emptyUserAnswers
        ) mustBe CompanyCYA
      }

      "must go from CompanyMobileNumberPage to CompanyMobileNumberPage in CheckMode" in {
        navigator.nextPage(
          CompanyMobileNumberPage,
          CheckMode,
          emptyUserAnswers
        ) mustBe controllers.add.company.routes.CompanyMobileNumberController.onPageLoad(CheckMode)
      }

      "must go from CompanyWorksReferencePage to CompanyCheckYourAnswerPage in CheckMode" in {
        navigator.nextPage(
          CompanyWorksReferencePage,
          CheckMode,
          emptyUserAnswers
        ) mustBe controllers.add.company.routes.CompanyCheckYourAnswersController.onPageLoad()
      }

    }

  }

}
