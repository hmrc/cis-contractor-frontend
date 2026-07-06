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
import models.{AmendMode, CheckMode, NormalMode, UserAnswers}
import pages.Page
import pages.add.company.*

class CompanyNavigatorSpec extends SpecBase {

  val navigator                    = new CompanyNavigator
  private lazy val journeyRecovery = routes.JourneyRecoveryController.onPageLoad()
  private lazy val CompanyCYA      = controllers.add.company.routes.CompanyCheckYourAnswersController.onPageLoad()
  private lazy val CompanyAmendCYA =
    routes.JourneyRecoveryController
      .onPageLoad() // TODO: when available controllers.add.company.routes.AmendCompanyCheckYourAnswersController.onPageLoad()

  "CompanyNavigator" - {

    "in Normal mode" - {

      "must go from a page that doesn't exist in the route map to Index" in {
        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, NormalMode, UserAnswers("id")) mustBe routes.IndexController.onPageLoad()
      }

      "must go from CompanyNamePage to CompanyAddressYesNo in NormalMode" in {
        navigator.nextPage(
          CompanyNamePage,
          NormalMode,
          emptyUserAnswers
        ) mustBe controllers.add.company.routes.CompanyAddressYesNoController.onPageLoad(NormalMode)
      }

      "must go from CompanyAddressYesNoPage" - {
        "to the address lookup on-ramp when answer is Yes" in {
          navigator.nextPage(
            CompanyAddressYesNoPage,
            NormalMode,
            emptyUserAnswers.setOrException(CompanyAddressYesNoPage, true)
          ) mustBe controllers.add.company.routes.CompanyAddressController.redirectToAddressLookup()
        }

        "to CompanyContactOptions page when answer is No" in {
          navigator.nextPage(
            CompanyAddressYesNoPage,
            NormalMode,
            emptyUserAnswers.setOrException(CompanyAddressYesNoPage, false)
          ) mustBe controllers.add.company.routes.CompanyContactOptionsController.onPageLoad(NormalMode)
        }

        "to JourneyRecoveryPage when answer is not present" in {
          navigator.nextPage(
            CompanyAddressYesNoPage,
            NormalMode,
            emptyUserAnswers
          ) mustBe journeyRecovery
        }
      }

      "must go from a CompanyAddressPage to CompanyContactOptions" in {
        navigator.nextPage(
          CompanyAddressPage,
          NormalMode,
          UserAnswers("id")
        ) mustBe controllers.add.company.routes.CompanyContactOptionsController.onPageLoad(NormalMode)
      }

      "must go from CompanyContactOptionsPage" - {
        "to CompanyEmailAddressPage when EmailAddress is selected" in {
          navigator.nextPage(
            CompanyContactOptionsPage,
            NormalMode,
            emptyUserAnswers.setOrException(
              CompanyContactOptionsPage,
              ContactOptions.Email
            )
          ) mustBe controllers.add.company.routes.CompanyEmailAddressController.onPageLoad(NormalMode)
        }

        "to CompanyPhoneNumberPage when PhoneNumber is selected" in {
          navigator.nextPage(
            CompanyContactOptionsPage,
            NormalMode,
            emptyUserAnswers.setOrException(
              CompanyContactOptionsPage,
              ContactOptions.Phone
            )
          ) mustBe controllers.add.company.routes.CompanyPhoneNumberController.onPageLoad(NormalMode)
        }

        "to CompanyMobileNumberPage when MobileNumber is selected" in {
          navigator.nextPage(
            CompanyContactOptionsPage,
            NormalMode,
            emptyUserAnswers.setOrException(
              CompanyContactOptionsPage,
              ContactOptions.Mobile
            )
          ) mustBe controllers.add.company.routes.CompanyMobileNumberController.onPageLoad(NormalMode)
        }

        "to CompanyUtrYesNoPage when NoDetails is selected" in {
          navigator.nextPage(
            CompanyContactOptionsPage,
            NormalMode,
            emptyUserAnswers.setOrException(
              CompanyContactOptionsPage,
              ContactOptions.NoDetails
            )
          ) mustBe controllers.add.company.routes.CompanyUtrYesNoController.onPageLoad(NormalMode)
        }

        "to JourneyRecoveryPage when answer is not present" in {
          navigator.nextPage(
            CompanyContactOptionsPage,
            NormalMode,
            emptyUserAnswers
          ) mustBe journeyRecovery
        }
      }

      "must go from CompanyEmailAddressPage to CompanyUtrYesNo in NormalMode" in {
        navigator.nextPage(
          CompanyEmailAddressPage,
          NormalMode,
          emptyUserAnswers
        ) mustBe controllers.add.company.routes.CompanyUtrYesNoController.onPageLoad(NormalMode)
      }

      "must go from a CompanyPhoneNumberPage to CompanyUtrYesNo page" in {
        navigator.nextPage(
          CompanyPhoneNumberPage,
          NormalMode,
          UserAnswers("id")
        ) mustBe controllers.add.company.routes.CompanyUtrYesNoController.onPageLoad(NormalMode)
      }

      "must go from a CompanyMobileNumberPage to CompanyUtrYesNo" in {
        navigator.nextPage(
          CompanyMobileNumberPage,
          NormalMode,
          UserAnswers("id")
        ) mustBe controllers.add.company.routes.CompanyUtrYesNoController.onPageLoad(NormalMode)
      }

      "must go from CompanyUtrYesNo" - {
        "to CompanyUtrPage when answer is Yes" in {
          navigator.nextPage(
            CompanyUtrYesNoPage,
            NormalMode,
            emptyUserAnswers.setOrException(CompanyUtrYesNoPage, true)
          ) mustBe controllers.add.company.routes.CompanyUtrController
            .onPageLoad(NormalMode)
        }

        "to CompanyCrnYesNoPage when answer is No" in {
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

      "must go from a CompanyUtrPage to CompanyCrnYesNoPage" in {
        navigator.nextPage(
          CompanyUtrPage,
          NormalMode,
          UserAnswers("id")
        ) mustBe controllers.add.company.routes.CompanyCrnYesNoController.onPageLoad(NormalMode)
      }

      "must go from CompanyCrnYesNo" - {
        "to CompanyCrnPage when answer is Yes" in {
          navigator.nextPage(
            CompanyCrnYesNoPage,
            NormalMode,
            emptyUserAnswers.setOrException(CompanyCrnYesNoPage, true)
          ) mustBe controllers.add.company.routes.CompanyCrnController.onPageLoad(NormalMode)
        }

        "to CompanyWorksReferenceYesNoPage when answer is No" in {
          navigator.nextPage(
            CompanyCrnYesNoPage,
            NormalMode,
            emptyUserAnswers.setOrException(CompanyCrnYesNoPage, false)
          ) mustBe controllers.add.company.routes.CompanyWorksReferenceYesNoController.onPageLoad(NormalMode)
        }

        "to JourneyRecoveryPage when answer is not present" in {
          navigator.nextPage(
            CompanyCrnYesNoPage,
            NormalMode,
            emptyUserAnswers
          ) mustBe journeyRecovery
        }
      }

      "must go from a CompanyCrnPage to CompanyWorksReferenceYesNo Page" in {
        navigator.nextPage(
          CompanyCrnPage,
          NormalMode,
          UserAnswers("id")
        ) mustBe controllers.add.company.routes.CompanyWorksReferenceYesNoController.onPageLoad(NormalMode)
      }

      "must go from CompanyWorksReferenceYesNo" - {
        "to CompanyWorksReferencePage when answer is Yes" in {
          navigator.nextPage(
            CompanyWorksReferenceYesNoPage,
            NormalMode,
            emptyUserAnswers.setOrException(CompanyWorksReferenceYesNoPage, true)
          ) mustBe controllers.add.company.routes.CompanyWorksReferenceController
            .onPageLoad(NormalMode)
        }

        "to Company CYA when answer is No" in {
          navigator.nextPage(
            CompanyWorksReferenceYesNoPage,
            NormalMode,
            emptyUserAnswers.setOrException(CompanyWorksReferenceYesNoPage, false)
          ) mustBe CompanyCYA
        }

        "to JourneyRecoveryPage when answer is not present" in {
          navigator.nextPage(
            CompanyWorksReferenceYesNoPage,
            NormalMode,
            emptyUserAnswers
          ) mustBe journeyRecovery
        }
      }

      "must go from a CompanyWorksReferencePage to CompanyCheckYourAnswerPage" in {
        navigator.nextPage(
          CompanyWorksReferencePage,
          NormalMode,
          UserAnswers("id")
        ) mustBe CompanyCYA
      }

      "must go from AddCompanyContactMethodsYesNo" - {
        "to AddCompanyContactMethodsYesNoPage when answer is Yes" in {
          navigator.nextPage(
            AddCompanyContactMethodsYesNoPage,
            NormalMode,
            emptyUserAnswers.setOrException(AddCompanyContactMethodsYesNoPage, true)
          ) mustBe controllers.add.company.routes.AddCompanyContactMethodsYesNoController
            .onPageLoad(NormalMode)
        }

        "to CompanyUtrYesNo when answer is No" in {
          navigator.nextPage(
            AddCompanyContactMethodsYesNoPage,
            NormalMode,
            emptyUserAnswers.setOrException(AddCompanyContactMethodsYesNoPage, false)
          ) mustBe controllers.add.company.routes.CompanyUtrYesNoController
            .onPageLoad(NormalMode)
        }

        "to JourneyRecoveryPage when answer is not present" in {
          navigator.nextPage(
            AddCompanyContactMethodsYesNoPage,
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

      "must go from CompanyNamePage to JourneyRecovery" in {
        navigator.nextPage(CompanyNamePage, AmendMode, emptyUserAnswers) mustBe journeyRecovery
      }

      "to Amend Company CYA page when EmailAddress is selected and CompanyEmailAddressPage is answered" in {
        navigator.nextPage(
          CompanyContactOptionsPage,
          AmendMode,
          emptyUserAnswers
            .setOrException(
              CompanyContactOptionsPage,
              ContactOptions.Email
            )
            .setOrException(CompanyEmailAddressPage, "old@email.com")
        ) mustBe CompanyAmendCYA
      }

      "must go from CompanyMobileNumberPage to Company CYA in AmendMode" in {
        navigator.nextPage(
          CompanyMobileNumberPage,
          AmendMode,
          emptyUserAnswers
        ) mustBe CompanyAmendCYA
      }

      "must go from a CompanyCrnPage to Company CYA in AmendMode" in {
        navigator.nextPage(
          CompanyCrnPage,
          AmendMode,
          emptyUserAnswers
        ) mustBe CompanyAmendCYA
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
        ) mustBe CompanyCYA
      }

      "must go from CompanyAddressYesNoPage" - {
        "to the address lookup on-ramp when answer is Yes and CompanyAddressPage is not answered before" in {
          val answers = emptyUserAnswers.set(CompanyAddressYesNoPage, true).success.value

          navigator.nextPage(
            CompanyAddressYesNoPage,
            CheckMode,
            answers
          ) mustBe controllers.add.company.routes.CompanyAddressController.redirectToAddressLookup(
            Some(CheckMode.toString)
          )
        }

        "to Company CYA when answer is Yes and CompanyAddressPage is answered before" in {

          val address = models.address.Address(
            addressLine1 = "10 Example Street",
            addressLine2 = Some("Suite 2"),
            addressLine3 = Some("Newcastle"),
            addressLine4 = Some("Tyne & Wear"),
            postcode = Some("NE1 1AA"),
            country = Some(models.address.Country(Some("GB"), Some("United Kingdom")))
          )

          val answers =
            emptyUserAnswers
              .set(CompanyAddressPage, address)
              .success
              .value
              .set(CompanyAddressYesNoPage, true)
              .success
              .value

          navigator.nextPage(
            CompanyAddressYesNoPage,
            CheckMode,
            answers
          ) mustBe CompanyCYA
        }

        "to Company CYA when answer is No" in {
          val answers = emptyUserAnswers.set(CompanyAddressYesNoPage, false).success.value

          navigator.nextPage(
            CompanyAddressYesNoPage,
            CheckMode,
            answers
          ) mustBe CompanyCYA
        }

        "to JourneyRecoveryPage when answer is not present" in {
          navigator.nextPage(
            CompanyAddressYesNoPage,
            CheckMode,
            emptyUserAnswers
          ) mustBe routes.JourneyRecoveryController.onPageLoad()
        }
      }

      "must go from CompanyAddressPage to Company CYA in CheckMode" in {
        navigator.nextPage(
          CompanyAddressPage,
          CheckMode,
          emptyUserAnswers
        ) mustBe CompanyCYA
      }

      "must go from CompanyContactOptionsPage" - {
        "to CompanyEmailAddress page when EmailAddress is selected and CompanyEmailAddressPage is not answered" in {
          navigator.nextPage(
            CompanyContactOptionsPage,
            CheckMode,
            emptyUserAnswers.setOrException(
              CompanyContactOptionsPage,
              ContactOptions.Email
            )
          ) mustBe controllers.add.company.routes.CompanyEmailAddressController.onPageLoad(CheckMode)
        }

        "to Company CYA page when EmailAddress is selected and CompanyEmailAddressPage is answered" in {
          navigator.nextPage(
            CompanyContactOptionsPage,
            CheckMode,
            emptyUserAnswers
              .setOrException(
                CompanyContactOptionsPage,
                ContactOptions.Email
              )
              .setOrException(CompanyEmailAddressPage, "old@email.com")
          ) mustBe CompanyCYA
        }

        "to CompanyPhoneNumberPage when PhoneNumber is selected and CompanyPhoneNumberPage is not answered" in {
          navigator.nextPage(
            CompanyContactOptionsPage,
            CheckMode,
            emptyUserAnswers.setOrException(
              CompanyContactOptionsPage,
              ContactOptions.Phone
            )
          ) mustBe controllers.add.company.routes.CompanyPhoneNumberController.onPageLoad(CheckMode)
        }

        "to Company CYA when PhoneNumber is selected and CompanyPhoneNumberPage is answered" in {
          navigator.nextPage(
            CompanyContactOptionsPage,
            CheckMode,
            emptyUserAnswers
              .setOrException(
                CompanyContactOptionsPage,
                ContactOptions.Phone
              )
              .setOrException(CompanyPhoneNumberPage, "0123456")
          ) mustBe CompanyCYA
        }

        "to CompanyMobileNumberPage when MobileNumber is selected and CompanyMobileNumberPage is not answered" in {
          navigator.nextPage(
            CompanyContactOptionsPage,
            CheckMode,
            emptyUserAnswers.setOrException(
              CompanyContactOptionsPage,
              ContactOptions.Mobile
            )
          ) mustBe controllers.add.company.routes.CompanyMobileNumberController.onPageLoad(CheckMode)
        }

        "to CompanyMobileNumberPage when MobileNumber is selected and CompanyMobileNumberPage is answered" in {
          navigator.nextPage(
            CompanyContactOptionsPage,
            CheckMode,
            emptyUserAnswers
              .setOrException(
                CompanyContactOptionsPage,
                ContactOptions.Mobile
              )
              .setOrException(CompanyMobileNumberPage, "0123456")
          ) mustBe CompanyCYA
        }

        "to company CYA when NoDetails is selected" in {
          navigator.nextPage(
            CompanyContactOptionsPage,
            CheckMode,
            emptyUserAnswers.setOrException(
              CompanyContactOptionsPage,
              ContactOptions.NoDetails
            )
          ) mustBe CompanyCYA
        }

        "to CYA when answer is not present" in {
          navigator.nextPage(
            CompanyContactOptionsPage,
            CheckMode,
            emptyUserAnswers
          ) mustBe journeyRecovery
        }
      }

      "must go from CompanyEmailAddressPage to Company CYA in CheckMode" in {
        navigator.nextPage(
          CompanyEmailAddressPage,
          CheckMode,
          emptyUserAnswers
        ) mustBe CompanyCYA
      }

      "must go from a CompanyPhoneNumberPage to Company CYA in CheckMode" in {
        navigator.nextPage(
          CompanyPhoneNumberPage,
          CheckMode,
          emptyUserAnswers
        ) mustBe CompanyCYA
      }

      "must go from CompanyMobileNumberPage to Company CYA in CheckMode" in {
        navigator.nextPage(
          CompanyMobileNumberPage,
          CheckMode,
          emptyUserAnswers
        ) mustBe CompanyCYA
      }

      "must go from CompanyUtrYesNo" - {
        "to next page when answer is Yes and CompanyUtrPage is not answered before" in {
          val answers = emptyUserAnswers.set(CompanyUtrYesNoPage, true).success.value

          navigator.nextPage(
            CompanyUtrYesNoPage,
            CheckMode,
            answers
          ) mustBe controllers.add.company.routes.CompanyUtrController
            .onPageLoad(CheckMode)
        }

        "to Company CYA when answer is Yes and CompanyUtrPage is answered before" in {
          val answers =
            emptyUserAnswers
              .set(CompanyUtrPage, "7777777777")
              .success
              .value
              .set(CompanyUtrYesNoPage, true)
              .success
              .value

          navigator.nextPage(
            CompanyUtrYesNoPage,
            CheckMode,
            answers
          ) mustBe CompanyCYA
        }

        "to Company CYA when answer is No" in {
          val answers = emptyUserAnswers.set(CompanyUtrYesNoPage, false).success.value

          navigator.nextPage(
            CompanyUtrYesNoPage,
            CheckMode,
            answers
          ) mustBe CompanyCYA
        }

        "to JourneyRecoveryPage when answer is not present" in {
          navigator.nextPage(
            CompanyUtrYesNoPage,
            CheckMode,
            emptyUserAnswers
          ) mustBe routes.JourneyRecoveryController.onPageLoad()
        }
      }

      "must go from CompanyUtrPage to CompanyCYA in CheckMode" in {
        navigator.nextPage(
          CompanyUtrPage,
          CheckMode,
          emptyUserAnswers
        ) mustBe CompanyCYA
      }

      "must go from CompanyCrnYesNoPage" - {
        "to CompanyCrnPage when answer is Yes and CompanyCrnPage is not answered before" in {
          val answers = emptyUserAnswers.set(CompanyCrnYesNoPage, true).success.value

          navigator.nextPage(
            CompanyCrnYesNoPage,
            CheckMode,
            answers
          ) mustBe controllers.add.company.routes.CompanyCrnController.onPageLoad(CheckMode)
        }

        "to Company CYA when answer is Yes and CompanyCrnPage is answered before" in {
          val answers =
            emptyUserAnswers
              .set(CompanyCrnPage, "AC012345")
              .success
              .value
              .set(CompanyCrnYesNoPage, true)
              .success
              .value

          navigator.nextPage(
            CompanyCrnYesNoPage,
            CheckMode,
            answers
          ) mustBe CompanyCYA
        }

        "to Company CYA when answer is No" in {
          val answers = emptyUserAnswers.set(CompanyCrnYesNoPage, false).success.value

          navigator.nextPage(
            CompanyCrnYesNoPage,
            CheckMode,
            answers
          ) mustBe CompanyCYA
        }

        "to JourneyRecoveryPage when answer is not present" in {
          navigator.nextPage(
            CompanyCrnYesNoPage,
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

      "must go from CompanyWorksReferenceYesNo" - {
        "to CompanyWorksReferencePage when answer is Yes and CompanyWorksReferencePage is not answered before" in {
          val answers = emptyUserAnswers.set(CompanyWorksReferenceYesNoPage, true).success.value

          navigator.nextPage(
            CompanyWorksReferenceYesNoPage,
            CheckMode,
            answers
          ) mustBe controllers.add.company.routes.CompanyWorksReferenceController
            .onPageLoad(CheckMode)
        }

        "to Company CYA when answer is Yes and CompanyWorksReferencePage is answered before" in {
          val answers =
            emptyUserAnswers
              .set(CompanyWorksReferencePage, "WR-001")
              .success
              .value
              .set(CompanyWorksReferenceYesNoPage, true)
              .success
              .value

          navigator.nextPage(
            CompanyWorksReferenceYesNoPage,
            CheckMode,
            answers
          ) mustBe CompanyCYA
        }

        "to Company CYA when answer is No" in {
          val answers = emptyUserAnswers.set(CompanyWorksReferenceYesNoPage, false).success.value

          navigator.nextPage(
            CompanyWorksReferenceYesNoPage,
            CheckMode,
            answers
          ) mustBe CompanyCYA
        }

        "to JourneyRecoveryPage when answer is not present" in {
          navigator.nextPage(
            CompanyWorksReferenceYesNoPage,
            CheckMode,
            emptyUserAnswers
          ) mustBe routes.JourneyRecoveryController.onPageLoad()
        }
      }

      "must go from CompanyWorksReferencePage to CompanyCheckYourAnswerPage in CheckMode" in {
        navigator.nextPage(
          CompanyWorksReferencePage,
          CheckMode,
          emptyUserAnswers
        ) mustBe CompanyCYA
      }

      "must go from AddCompanyContactMethodsYesNo" - {
        "to AddCompanyContactMethodsYesNo page when answer is Yes" in {
          val answers = emptyUserAnswers.set(AddCompanyContactMethodsYesNoPage, true).success.value

          navigator.nextPage(
            AddCompanyContactMethodsYesNoPage,
            CheckMode,
            answers
          ) mustBe controllers.add.company.routes.AddCompanyContactMethodsYesNoController
            .onPageLoad(CheckMode)
        }

        "to CYA when answer is No" in {
          val answers = emptyUserAnswers.set(AddCompanyContactMethodsYesNoPage, false).success.value

          navigator.nextPage(
            AddCompanyContactMethodsYesNoPage,
            CheckMode,
            answers
          ) mustBe CompanyCYA
        }

        "to JourneyRecovery when answer is not present" in {
          navigator.nextPage(
            AddCompanyContactMethodsYesNoPage,
            CheckMode,
            emptyUserAnswers
          ) mustBe journeyRecovery
        }
      }
    }
  }
}
