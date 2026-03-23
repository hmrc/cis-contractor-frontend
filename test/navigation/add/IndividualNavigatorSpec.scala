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
import models.add.SubcontractorName
import models.contact.ContactOptions
import models.{CheckMode, NormalMode, UserAnswers}
import pages.Page
import pages.add.*

class IndividualNavigatorSpec extends SpecBase {

  val navigator                    = new IndividualNavigator
  private lazy val journeyRecovery = routes.JourneyRecoveryController.onPageLoad()
  private lazy val CYA             = controllers.add.routes.CheckYourAnswersController.onPageLoad()

  "IndividualNavigator" - {

    "in Normal mode" - {

      "must go from a page that doesn't exist in the route map to Index" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, NormalMode, UserAnswers("id")) mustBe routes.IndexController.onPageLoad()
      }

      "must go from a SubTradingNameYesNoPage to TradingNameOfSubcontractorPage when true" in {
        navigator.nextPage(
          SubTradingNameYesNoPage,
          NormalMode,
          emptyUserAnswers.setOrException(SubTradingNameYesNoPage, true)
        ) mustBe controllers.add.routes.TradingNameOfSubcontractorController.onPageLoad(NormalMode)
      }

      "must go from a SubTradingNameYesNoPage to SubcontractorNamePage when false" in {
        navigator.nextPage(
          SubTradingNameYesNoPage,
          NormalMode,
          emptyUserAnswers.setOrException(SubTradingNameYesNoPage, false)
        ) mustBe controllers.add.routes.SubcontractorNameController.onPageLoad(NormalMode)
      }

      "must go from a SubTradingNameYesNoPage to journey recovery page when incomplete info provided" in {
        navigator.nextPage(
          SubTradingNameYesNoPage,
          NormalMode,
          emptyUserAnswers
        ) mustBe journeyRecovery
      }

      "must go from a TradingNameOfSubcontractorPage to SubAddressYesNoPage" in {
        navigator.nextPage(
          TradingNameOfSubcontractorPage,
          NormalMode,
          UserAnswers("id")
        ) mustBe controllers.add.routes.SubAddressYesNoController.onPageLoad(NormalMode)
      }

      "must go from a SubcontractorNamePage to SubAddressYesNoPage" in {
        navigator.nextPage(
          SubcontractorNamePage,
          NormalMode,
          UserAnswers("id")
        ) mustBe controllers.add.routes.SubAddressYesNoController.onPageLoad(NormalMode)
      }

      "must go from a SubAddressYesNoPage to AddressOfSubcontractorPage when true" in {
        navigator.nextPage(
          SubAddressYesNoPage,
          NormalMode,
          emptyUserAnswers.setOrException(SubAddressYesNoPage, true)
        ) mustBe controllers.add.routes.AddressOfSubcontractorController.onPageLoad(NormalMode)
      }

      "must go from a SubAddressYesNoPage to IndividualChooseContactDetailsPage when false" in {
        navigator.nextPage(
          SubAddressYesNoPage,
          NormalMode,
          emptyUserAnswers.setOrException(SubAddressYesNoPage, false)
        ) mustBe controllers.add.routes.IndividualChooseContactDetailsController.onPageLoad(NormalMode)
      }

      "must go from a SubAddressYesNoPage to journey recovery when incomplete info provided" in {
        navigator.nextPage(
          SubAddressYesNoPage,
          NormalMode,
          emptyUserAnswers
        ) mustBe journeyRecovery
      }

      "must go from a AddressOfSubcontractorPage to IndividualChooseContactDetailsPage" in {
        navigator.nextPage(
          AddressOfSubcontractorPage,
          NormalMode,
          UserAnswers("id")
        ) mustBe controllers.add.routes.IndividualChooseContactDetailsController.onPageLoad(NormalMode)
      }

      "must go from a NationalInsuranceNumberYesNoPage to SubNationalInsuranceNumberPage when true" in {
        navigator.nextPage(
          NationalInsuranceNumberYesNoPage,
          NormalMode,
          emptyUserAnswers.setOrException(NationalInsuranceNumberYesNoPage, true)
        ) mustBe controllers.add.routes.SubNationalInsuranceNumberController.onPageLoad(NormalMode)
      }

      "must go from a NationalInsuranceNumberYesNoPage to WorksReferenceNumberYesNoPage when false" in {
        navigator.nextPage(
          NationalInsuranceNumberYesNoPage,
          NormalMode,
          emptyUserAnswers.setOrException(NationalInsuranceNumberYesNoPage, false)
        ) mustBe controllers.add.routes.WorksReferenceNumberYesNoController.onPageLoad(NormalMode)
      }

      "must go from a NationalInsuranceNumberYesNoPage to journey recovery when incomplete info provided" in {
        navigator.nextPage(
          NationalInsuranceNumberYesNoPage,
          NormalMode,
          emptyUserAnswers
        ) mustBe journeyRecovery
      }

      "must go from a SubNationalInsuranceNumberPage to UniqueTaxpayerReferenceYesNoPage" in {
        navigator.nextPage(
          SubNationalInsuranceNumberPage,
          NormalMode,
          UserAnswers("id")
        ) mustBe controllers.add.routes.UniqueTaxpayerReferenceYesNoController.onPageLoad(NormalMode)
      }

      "must go from a UniqueTaxpayerReferenceYesNoPage to SubcontractorsUniqueTaxpayerReferencePage when true" in {
        navigator.nextPage(
          UniqueTaxpayerReferenceYesNoPage,
          NormalMode,
          emptyUserAnswers.setOrException(UniqueTaxpayerReferenceYesNoPage, true)
        ) mustBe controllers.add.routes.SubcontractorsUniqueTaxpayerReferenceController.onPageLoad(NormalMode)
      }

      "must go from a UniqueTaxpayerReferenceYesNoPage to NationalInsuranceNumberYesNoPage when false" in {
        navigator.nextPage(
          UniqueTaxpayerReferenceYesNoPage,
          NormalMode,
          emptyUserAnswers.setOrException(UniqueTaxpayerReferenceYesNoPage, false)
        ) mustBe controllers.add.routes.NationalInsuranceNumberYesNoController.onPageLoad(NormalMode)
      }

      "must go from a UniqueTaxpayerReferenceYesNoPage to journey recovery when incomplete info provided" in {
        navigator.nextPage(
          UniqueTaxpayerReferenceYesNoPage,
          NormalMode,
          emptyUserAnswers
        ) mustBe journeyRecovery
      }

      "must go from a SubcontractorsUniqueTaxpayerReferencePage to WorksReferenceNumberYesNoController" in {
        navigator.nextPage(
          SubcontractorsUniqueTaxpayerReferencePage,
          NormalMode,
          UserAnswers("id")
        ) mustBe controllers.add.routes.WorksReferenceNumberYesNoController.onPageLoad(NormalMode)
      }

      "must go from a WorksReferenceNumberYesNoPage to WorksReferenceNumberPage when true" in {
        navigator.nextPage(
          WorksReferenceNumberYesNoPage,
          NormalMode,
          emptyUserAnswers.setOrException(WorksReferenceNumberYesNoPage, true)
        ) mustBe controllers.add.routes.WorksReferenceNumberController.onPageLoad(NormalMode)
      }

      "must go from a WorksReferenceNumberYesNoPage to CYA when false" in {
        val ua =
          emptyUserAnswers
            .setOrException(WorksReferenceNumberYesNoPage, false)

        val navigator = new IndividualNavigator
        navigator.nextPage(
          WorksReferenceNumberYesNoPage,
          NormalMode,
          ua
        ) mustBe controllers.add.routes.CheckYourAnswersController.onPageLoad()
      }

      "must go from a WorksReferenceNumberYesNoPage to journey recovery when incomplete info provided" in {
        navigator.nextPage(
          WorksReferenceNumberYesNoPage,
          NormalMode,
          emptyUserAnswers
        ) mustBe journeyRecovery
      }

      "must go from a WorksReferenceNumberPage to CYA" in {
        navigator.nextPage(
          WorksReferenceNumberPage,
          NormalMode,
          UserAnswers("id")
        ) mustBe controllers.add.routes.CheckYourAnswersController.onPageLoad()
      }

      "must go from a SubContactDetailsPage to CYA" in {
        navigator.nextPage(
          SubContactDetailsPage,
          NormalMode,
          UserAnswers("id")
        ) mustBe controllers.add.routes.CheckYourAnswersController.onPageLoad()
      }

      "must go from IndividualMobileNumberPage to UniqueTaxpayerReferenceYesNoPage in NormalMode" in {
        navigator.nextPage(
          IndividualMobileNumberPage,
          NormalMode,
          UserAnswers("id")
        ) mustBe controllers.add.routes.UniqueTaxpayerReferenceYesNoController.onPageLoad(NormalMode)
      }

      "must go from IndividualPhoneNumberPage to UniqueTaxpayerReferenceYesNoPage in NormalMode" in {
        navigator.nextPage(
          IndividualPhoneNumberPage,
          NormalMode,
          UserAnswers("id")
        ) mustBe controllers.add.routes.UniqueTaxpayerReferenceYesNoController.onPageLoad(NormalMode)
      }

      "must go from a IndividualEmailAddressPage to IndividualEmailAddressPage" in {
        navigator.nextPage(
          IndividualEmailAddressPage,
          NormalMode,
          UserAnswers("id")
        ) mustBe controllers.add.routes.UniqueTaxpayerReferenceYesNoController.onPageLoad(NormalMode)
      }

      "must go from IndividualChooseContactDetailsPage" - {
        "to IndividualEmailAddressPage when EmailAddress is selected" in {
          navigator.nextPage(
            IndividualChooseContactDetailsPage,
            NormalMode,
            emptyUserAnswers.setOrException(
              IndividualChooseContactDetailsPage,
              ContactOptions.Email
            )
          ) mustBe controllers.add.routes.IndividualEmailAddressController.onPageLoad(NormalMode)
        }

        "to IndividualPhoneNumberPage when PhoneNumber is selected" in {
          navigator.nextPage(
            IndividualChooseContactDetailsPage,
            NormalMode,
            emptyUserAnswers.setOrException(
              IndividualChooseContactDetailsPage,
              ContactOptions.Phone
            )
          ) mustBe controllers.add.routes.IndividualPhoneNumberController.onPageLoad(NormalMode)
        }

        "to IndividualMobileNumberPage when MobileNumber is selected" in {
          navigator.nextPage(
            IndividualChooseContactDetailsPage,
            NormalMode,
            emptyUserAnswers.setOrException(
              IndividualChooseContactDetailsPage,
              ContactOptions.Mobile
            )
          ) mustBe controllers.add.routes.IndividualMobileNumberController.onPageLoad(NormalMode)
        }

        "to UniqueTaxpayerReferenceYesNoPage when NoDetails is selected" in {
          navigator.nextPage(
            IndividualChooseContactDetailsPage,
            NormalMode,
            emptyUserAnswers.setOrException(
              IndividualChooseContactDetailsPage,
              ContactOptions.NoDetails
            )
          ) mustBe controllers.add.routes.UniqueTaxpayerReferenceYesNoController.onPageLoad(NormalMode)
        }

        "to JourneyRecoveryPage when answer is not present" in {
          navigator.nextPage(
            IndividualChooseContactDetailsPage,
            NormalMode,
            emptyUserAnswers
          ) mustBe journeyRecovery
        }
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

      "must go from SubTradingNameYesNoPage to TradingNameOfSubcontractorController when true" in {
        navigator.nextPage(
          SubTradingNameYesNoPage,
          CheckMode,
          emptyUserAnswers.setOrException(SubTradingNameYesNoPage, true)
        ) mustBe controllers.add.routes.TradingNameOfSubcontractorController.onPageLoad(CheckMode)
      }

      "must go from SubTradingNameYesNoPage to SubcontractorNamePage when false" in {
        navigator.nextPage(
          SubTradingNameYesNoPage,
          CheckMode,
          emptyUserAnswers.setOrException(SubTradingNameYesNoPage, false)
        ) mustBe controllers.add.routes.SubcontractorNameController.onPageLoad(CheckMode)
      }

      "must go from SubTradingNameYesNoPage to journey recovery page when incomplete info provided" in {
        navigator.nextPage(
          SubTradingNameYesNoPage,
          CheckMode,
          emptyUserAnswers
        ) mustBe journeyRecovery
      }

      "must go from a SubAddressYesNoPage to next page when true" in {
        navigator.nextPage(
          SubAddressYesNoPage,
          CheckMode,
          emptyUserAnswers.setOrException(SubAddressYesNoPage, true)
        ) mustBe controllers.add.routes.AddressOfSubcontractorController.onPageLoad(CheckMode)
      }

      "must go from a SubAddressYesNoPage to CYA page when false" in {
        navigator.nextPage(
          SubAddressYesNoPage,
          CheckMode,
          emptyUserAnswers.setOrException(SubAddressYesNoPage, false)
        ) mustBe CYA
      }

      "must go from a SubAddressYesNoPage to journey recovery page when incomplete info provided" in {
        navigator.nextPage(
          SubAddressYesNoPage,
          CheckMode,
          emptyUserAnswers
        ) mustBe journeyRecovery
      }

      "must go from a UniqueTaxpayerReferenceYesNoPage to next page when true" in {
        navigator.nextPage(
          UniqueTaxpayerReferenceYesNoPage,
          CheckMode,
          emptyUserAnswers.setOrException(UniqueTaxpayerReferenceYesNoPage, true)
        ) mustBe controllers.add.routes.SubcontractorsUniqueTaxpayerReferenceController.onPageLoad(CheckMode)
      }

      "must go from a UniqueTaxpayerReferenceYesNoPage to CYA page when false" in {
        navigator.nextPage(
          UniqueTaxpayerReferenceYesNoPage,
          CheckMode,
          emptyUserAnswers.setOrException(UniqueTaxpayerReferenceYesNoPage, false)
        ) mustBe CYA
      }

      "must go from a AddressOfSubcontractorPage to CYA" in {
        navigator.nextPage(
          AddressOfSubcontractorPage,
          CheckMode,
          UserAnswers("id")
        ) mustBe CYA
      }

      "must go from TradingNameOfSubcontractorPage to CYA in CheckMode" in {
        navigator.nextPage(
          TradingNameOfSubcontractorPage,
          CheckMode,
          UserAnswers("id")
        ) mustBe CYA
      }

      "must go from SubcontractorNamePage to CYA in CheckMode" in {
        navigator.nextPage(
          SubcontractorNamePage,
          CheckMode,
          UserAnswers("id")
        ) mustBe CYA
      }

      "must go from SubNationalInsuranceNumberPage to CYA in CheckMode" in {
        navigator.nextPage(
          SubNationalInsuranceNumberPage,
          CheckMode,
          UserAnswers("id")
        ) mustBe CYA
      }

      "must go from SubcontractorsUniqueTaxpayerReferencePage to CYA in CheckMode" in {
        navigator.nextPage(
          SubcontractorsUniqueTaxpayerReferencePage,
          CheckMode,
          UserAnswers("id")
        ) mustBe CYA
      }

      "must go from a UniqueTaxpayerReferenceYesNoPage to journey recovery when incomplete info provided" in {
        navigator.nextPage(
          UniqueTaxpayerReferenceYesNoPage,
          CheckMode,
          emptyUserAnswers
        ) mustBe journeyRecovery
      }

      "must go from a NationalInsuranceNumberYesNoPage to next page when true" in {
        navigator.nextPage(
          NationalInsuranceNumberYesNoPage,
          CheckMode,
          emptyUserAnswers.setOrException(NationalInsuranceNumberYesNoPage, true)
        ) mustBe controllers.add.routes.SubNationalInsuranceNumberController.onPageLoad(CheckMode)
      }

      "must go from a NationalInsuranceNumberYesNoPage to CYA page when false" in {
        navigator.nextPage(
          NationalInsuranceNumberYesNoPage,
          CheckMode,
          emptyUserAnswers.setOrException(NationalInsuranceNumberYesNoPage, false)
        ) mustBe CYA
      }

      "must go from a NationalInsuranceNumberYesNoPage to journey recovery page when incomplete info provided" in {
        navigator.nextPage(
          NationalInsuranceNumberYesNoPage,
          CheckMode,
          emptyUserAnswers
        ) mustBe journeyRecovery
      }
      "must go from a WorksReferenceNumberPage  to next page in check mode" in {
        navigator.nextPage(
          WorksReferenceNumberPage,
          CheckMode,
          UserAnswers("id")
        ) mustBe controllers.add.routes.CheckYourAnswersController.onPageLoad()
      }

      "must go from a WorksReferenceNumberYesNoPage to next page when true" in {
        navigator.nextPage(
          WorksReferenceNumberYesNoPage,
          CheckMode,
          emptyUserAnswers.setOrException(WorksReferenceNumberYesNoPage, true)
        ) mustBe controllers.add.routes.WorksReferenceNumberController.onPageLoad(CheckMode)
      }

      "must go from a WorksReferenceNumberYesNoPage to CYA page when false" in {
        navigator.nextPage(
          WorksReferenceNumberYesNoPage,
          CheckMode,
          emptyUserAnswers.setOrException(WorksReferenceNumberYesNoPage, false)
        ) mustBe CYA
      }

      "must go from a WorksReferenceNumberYesNoPage to journey recovery page when incomplete info provided" in {
        navigator.nextPage(
          WorksReferenceNumberYesNoPage,
          CheckMode,
          emptyUserAnswers
        ) mustBe journeyRecovery
      }

      "must go to SubcontractorNameController when answer is No and name is missing" in {
        val ua =
          emptyUserAnswers
            .set(SubTradingNameYesNoPage, false)
            .success
            .value

        val navigator = new IndividualNavigator()
        navigator.nextPage(SubTradingNameYesNoPage, CheckMode, ua) mustBe
          controllers.add.routes.SubcontractorNameController.onPageLoad(CheckMode)
      }

      "must go to CYA when answer is No and subcontractor name already exists (Some(_))" in {
        val ua =
          emptyUserAnswers
            .set(SubTradingNameYesNoPage, false)
            .success
            .value
            .set(SubcontractorNamePage, SubcontractorName(firstName = "Jane", middleName = None, lastName = "Doe"))
            .success
            .value

        val navigator = new IndividualNavigator()
        navigator.nextPage(SubTradingNameYesNoPage, CheckMode, ua) mustBe
          controllers.add.routes.CheckYourAnswersController.onPageLoad()
      }

      "must go to TradingNameOfSubcontractorController when answer is Yes and trading name is missing" in {
        val ua =
          emptyUserAnswers
            .set(SubTradingNameYesNoPage, true)
            .success
            .value

        val navigator = new IndividualNavigator()
        navigator.nextPage(SubTradingNameYesNoPage, CheckMode, ua) mustBe
          controllers.add.routes.TradingNameOfSubcontractorController.onPageLoad(CheckMode)
      }

      "must go to CYA when answer is Yes and trading name already exists (Some(_))" in {
        val ua =
          emptyUserAnswers
            .set(SubTradingNameYesNoPage, true)
            .success
            .value
            .set(TradingNameOfSubcontractorPage, "ACME Construction")
            .success
            .value

        val navigator = new IndividualNavigator()
        navigator.nextPage(SubTradingNameYesNoPage, CheckMode, ua) mustBe
          controllers.add.routes.CheckYourAnswersController.onPageLoad()
      }

      "must route to JourneyRecovery when SubTradingNameYesNoPage answer is missing" in {
        val navigator = new IndividualNavigator()
        navigator.nextPage(SubTradingNameYesNoPage, CheckMode, emptyUserAnswers) mustBe
          routes.JourneyRecoveryController.onPageLoad()
      }

      "must go from a IndividualEmailAddressPage to IndividualEmailAddressPage" in {
        navigator.nextPage(
          IndividualEmailAddressPage,
          CheckMode,
          UserAnswers("id")
        ) mustBe controllers.add.routes.CheckYourAnswersController.onPageLoad()
      }

      "must go from IndividualChooseContactDetailsPage" - {
        "to IndividualEmailAddressPage when EmailAddress is selected" in {
          navigator.nextPage(
            IndividualChooseContactDetailsPage,
            CheckMode,
            emptyUserAnswers.setOrException(
              IndividualChooseContactDetailsPage,
              ContactOptions.Email
            )
          ) mustBe controllers.add.routes.IndividualEmailAddressController.onPageLoad(CheckMode)
        }

        "to IndividualPhoneNumberPage when PhoneNumber is selected" in {
          navigator.nextPage(
            IndividualChooseContactDetailsPage,
            CheckMode,
            emptyUserAnswers.setOrException(
              IndividualChooseContactDetailsPage,
              ContactOptions.Phone
            )
          ) mustBe controllers.add.routes.IndividualPhoneNumberController.onPageLoad(CheckMode)
        }

        "to IndividualMobileNumberPage when MobileNumber is selected" in {
          navigator.nextPage(
            IndividualChooseContactDetailsPage,
            CheckMode,
            emptyUserAnswers.setOrException(
              IndividualChooseContactDetailsPage,
              ContactOptions.Mobile
            )
          ) mustBe controllers.add.routes.IndividualMobileNumberController.onPageLoad(CheckMode)
        }

        "to CheckYourAnswersPage when NoDetails is selected" in {
          navigator.nextPage(
            IndividualChooseContactDetailsPage,
            CheckMode,
            emptyUserAnswers.setOrException(
              IndividualChooseContactDetailsPage,
              ContactOptions.NoDetails
            )
          ) mustBe controllers.add.routes.CheckYourAnswersController.onPageLoad()
        }

        "to CYA when answer is not present" in {
          navigator.nextPage(
            IndividualChooseContactDetailsPage,
            CheckMode,
            emptyUserAnswers
          ) mustBe CYA
        }
      }

      "must go from IndividualMobileNumberPage to CheckYourAnswersController in CheckMode" in {
        navigator.nextPage(
          IndividualMobileNumberPage,
          CheckMode,
          UserAnswers("id")
        ) mustBe controllers.add.routes.CheckYourAnswersController.onPageLoad()
      }

      "must go from IndividualPhoneNumberPage to CheckYourAnswersController in CheckMode" in {
        navigator.nextPage(
          IndividualPhoneNumberPage,
          CheckMode,
          UserAnswers("id")
        ) mustBe controllers.add.routes.CheckYourAnswersController.onPageLoad()
      }

    }

    "navigatorFromSubTradingNameYesNoPage in NormalMode" - {

      "must go to TradingNameOfSubcontractorController when answer is Yes" in {
        val ua        = emptyUserAnswers.set(SubTradingNameYesNoPage, true).success.value
        val navigator = new IndividualNavigator()
        navigator.nextPage(SubTradingNameYesNoPage, NormalMode, ua) mustBe
          controllers.add.routes.TradingNameOfSubcontractorController.onPageLoad(NormalMode)
      }

      "must go to SubcontractorNameController when answer is No" in {
        val ua        = emptyUserAnswers.set(SubTradingNameYesNoPage, false).success.value
        val navigator = new IndividualNavigator()
        navigator.nextPage(SubTradingNameYesNoPage, NormalMode, ua) mustBe
          controllers.add.routes.SubcontractorNameController.onPageLoad(NormalMode)
      }

      "must route to JourneyRecovery when SubTradingNameYesNoPage answer is missing" in {
        val navigator = new IndividualNavigator()
        navigator.nextPage(SubTradingNameYesNoPage, NormalMode, emptyUserAnswers) mustBe
          routes.JourneyRecoveryController.onPageLoad()
      }

      "must go from SubAddressYesNoPage to CYA when true and AddressOfSubcontractorPage is already answered" in {
        val addressSample = models.add.InternationalAddress(
          addressLine1 = "10 Example Street",
          addressLine2 = Some("Suite 2"),
          addressLine3 = "Newcastle",
          addressLine4 = Some("Tyne & Wear"),
          postalCode = "NE1 1AA",
          country = "United Kingdom"
        )

        val ua     =
          emptyUserAnswers
            .set(SubAddressYesNoPage, true)
            .success
            .value
            .set(AddressOfSubcontractorPage, addressSample)
            .success
            .value
        val result = navigator.nextPage(SubAddressYesNoPage, CheckMode, ua)
        result mustBe CYA
      }

      "must go from NationalInsuranceNumberYesNoPage to CYA when true and SubNationalInsuranceNumberPage is already answered" in {
        val ua =
          emptyUserAnswers
            .set(NationalInsuranceNumberYesNoPage, true)
            .success
            .value
            .set(SubNationalInsuranceNumberPage, "AB123456C")
            .success
            .value // sample valid NINO

        val result = navigator.nextPage(NationalInsuranceNumberYesNoPage, CheckMode, ua)
        result mustBe CYA
      }

      "must go from UniqueTaxpayerReferenceYesNoPage to CYA when true and SubcontractorsUniqueTaxpayerReferencePage is already answered" in {
        val ua =
          emptyUserAnswers
            .set(UniqueTaxpayerReferenceYesNoPage, true)
            .success
            .value
            .set(SubcontractorsUniqueTaxpayerReferencePage, "1234567890")
            .success
            .value

        val result = navigator.nextPage(UniqueTaxpayerReferenceYesNoPage, CheckMode, ua)
        result mustBe CYA
      }

      "must go from WorksReferenceNumberYesNoPage to CYA when true and WorksReferenceNumberPage is already answered" in {
        val ua =
          emptyUserAnswers
            .set(WorksReferenceNumberYesNoPage, true)
            .success
            .value
            .set(WorksReferenceNumberPage, "WRN-001")
            .success
            .value // sample WRN

        val result = navigator.nextPage(WorksReferenceNumberYesNoPage, CheckMode, ua)
        result mustBe CYA
      }

      "must go from a SubContactDetailsPage to CYA" in {
        navigator.nextPage(
          SubContactDetailsPage,
          CheckMode,
          UserAnswers("id")
        ) mustBe CYA
      }

    }

  }

}
