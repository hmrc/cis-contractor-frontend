/*
 * Copyright 2025 HM Revenue & Customs
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

package navigation

import base.SpecBase
import controllers.routes
import pages.*
import models.*
import models.add.{SubcontractorName, TypeOfSubcontractor, UKAddress}
import pages.add.*
import pages.add.partnership.*
import pages.add.partnership.PartnershipNominatedPartnerNinoPage

class NavigatorSpec extends SpecBase {

  val navigator = new Navigator

  private lazy val journeyRecovery = routes.JourneyRecoveryController.onPageLoad()
  private lazy val CYA             = controllers.add.routes.CheckYourAnswersController.onPageLoad()

  "Navigator" - {

    "in Normal mode" - {

      "must go from a page that doesn't exist in the route map to Index" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, NormalMode, UserAnswers("id")) mustBe routes.IndexController.onPageLoad()
      }

      "must go from a TypeOfSubcontractorPage to SubTradingNameYesNo when Individualorsoletrader is selected" in {
        navigator.nextPage(
          TypeOfSubcontractorPage,
          NormalMode,
          emptyUserAnswers.setOrException(TypeOfSubcontractorPage, TypeOfSubcontractor.Individualorsoletrader)
        ) mustBe controllers.add.routes.SubTradingNameYesNoController.onPageLoad(NormalMode)
      }

      "must go from a TypeOfSubcontractorPage to JourneyRecovery when Limitedcompany is selected" in {
        navigator.nextPage(
          TypeOfSubcontractorPage,
          NormalMode,
          emptyUserAnswers.setOrException(TypeOfSubcontractorPage, TypeOfSubcontractor.Limitedcompany)
        ) mustBe routes.JourneyRecoveryController.onPageLoad()
      }

      "must go from a TypeOfSubcontractorPage to PartnershipNameController when Partnership is selected" in {
        navigator.nextPage(
          TypeOfSubcontractorPage,
          NormalMode,
          emptyUserAnswers.setOrException(TypeOfSubcontractorPage, TypeOfSubcontractor.Partnership)
        ) mustBe controllers.add.partnership.routes.PartnershipNameController.onPageLoad(NormalMode)
      }

      "must go from PartnershipNamePage to PartnershipHasUtrYesNoController in NormalMode" in {
        navigator.nextPage(
          PartnershipNamePage,
          NormalMode,
          emptyUserAnswers
        ) mustBe controllers.add.partnership.routes.PartnershipHasUtrYesNoController.onPageLoad(NormalMode)
      }

      "must go from a TypeOfSubcontractorPage to JourneyRecovery when Trust is selected" in {
        navigator.nextPage(
          TypeOfSubcontractorPage,
          NormalMode,
          emptyUserAnswers.setOrException(TypeOfSubcontractorPage, TypeOfSubcontractor.Trust)
        ) mustBe routes.JourneyRecoveryController.onPageLoad()
      }

      "must go from a TypeOfSubcontractorPage to journey recovery page when incomplete info provided" in {
        navigator.nextPage(
          TypeOfSubcontractorPage,
          NormalMode,
          emptyUserAnswers
        ) mustBe journeyRecovery
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

      "must go from a PartnershipHasUtrYesNo to PartnershipUniqueTaxpayerReference page when false in CheckMode" in {
        navigator.nextPage(
          PartnershipHasUtrYesNoPage,
          CheckMode,
          emptyUserAnswers.setOrException(PartnershipHasUtrYesNoPage, false)
        ) mustBe controllers.add.routes.CheckYourAnswersController.onPageLoad()
      }

      "must go from a PartnershipUniqueTaxpayerReference to PartnershipWorksReferenceNumberYesNo page" in {
        navigator.nextPage(
          PartnershipUniqueTaxpayerReferencePage,
          NormalMode,
          emptyUserAnswers.setOrException(PartnershipUniqueTaxpayerReferencePage, "5860920998")
        ) mustBe controllers.add.partnership.routes.PartnershipWorksReferenceNumberYesNoController
          .onPageLoad(NormalMode)
      }

      "must go from a PartnershipUniqueTaxpayerReference to CheckYourAnswers page in CheckMode" in {
        navigator.nextPage(
          PartnershipUniqueTaxpayerReferencePage,
          CheckMode,
          emptyUserAnswers.setOrException(PartnershipUniqueTaxpayerReferencePage, "5860920998")
        ) mustBe controllers.add.routes.CheckYourAnswersController.onPageLoad()
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

      "must go from a SubAddressYesNoPage to NationalInsuranceNumberYesNoPage when false" in {
        navigator.nextPage(
          SubAddressYesNoPage,
          NormalMode,
          emptyUserAnswers.setOrException(SubAddressYesNoPage, false)
        ) mustBe controllers.add.routes.NationalInsuranceNumberYesNoController.onPageLoad(NormalMode)
      }

      "must go from a SubAddressYesNoPage to journey recovery when incomplete info provided" in {
        navigator.nextPage(
          SubAddressYesNoPage,
          NormalMode,
          emptyUserAnswers
        ) mustBe journeyRecovery
      }

      "must go from a AddressOfSubcontractorPage to NationalInsuranceNumberYesNoPage" in {
        navigator.nextPage(
          AddressOfSubcontractorPage,
          NormalMode,
          UserAnswers("id")
        ) mustBe controllers.add.routes.NationalInsuranceNumberYesNoController.onPageLoad(NormalMode)
      }

      "must go from a PartnershipAddressPage to next page" in {
        navigator.nextPage(
          PartnershipAddressPage,
          NormalMode,
          UserAnswers("id")
        ) mustBe controllers.add.partnership.routes.PartnershipAddressController.onPageLoad(NormalMode)
      }

      "must go from a NationalInsuranceNumberYesNoPage to SubNationalInsuranceNumberPage when true" in {
        navigator.nextPage(
          NationalInsuranceNumberYesNoPage,
          NormalMode,
          emptyUserAnswers.setOrException(NationalInsuranceNumberYesNoPage, true)
        ) mustBe controllers.add.routes.SubNationalInsuranceNumberController.onPageLoad(NormalMode)
      }

      "must go from a NationalInsuranceNumberYesNoPage to UniqueTaxpayerReferenceYesNoPage when false" in {
        navigator.nextPage(
          NationalInsuranceNumberYesNoPage,
          NormalMode,
          emptyUserAnswers.setOrException(NationalInsuranceNumberYesNoPage, false)
        ) mustBe controllers.add.routes.UniqueTaxpayerReferenceYesNoController.onPageLoad(NormalMode)
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

      "must go from a UniqueTaxpayerReferenceYesNoPage to WorksReferenceNumberYesNoPage when false" in {
        navigator.nextPage(
          UniqueTaxpayerReferenceYesNoPage,
          NormalMode,
          emptyUserAnswers.setOrException(UniqueTaxpayerReferenceYesNoPage, false)
        ) mustBe controllers.add.routes.WorksReferenceNumberYesNoController.onPageLoad(NormalMode)
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

      "must go from a WorksReferenceNumberYesNoPage to SubcontractorContactDetailsYesNoPage when false" in {
        navigator.nextPage(
          WorksReferenceNumberYesNoPage,
          NormalMode,
          emptyUserAnswers.setOrException(WorksReferenceNumberYesNoPage, false)
        ) mustBe controllers.add.routes.SubcontractorContactDetailsYesNoController.onPageLoad(NormalMode)
      }

      "must go from a WorksReferenceNumberYesNoPage to journey recovery when incomplete info provided" in {
        navigator.nextPage(
          WorksReferenceNumberYesNoPage,
          NormalMode,
          emptyUserAnswers
        ) mustBe journeyRecovery
      }

      "must go from a WorksReferenceNumberPage to SubcontractorContactDetailsYesNoPage" in {
        navigator.nextPage(
          WorksReferenceNumberPage,
          NormalMode,
          UserAnswers("id")
        ) mustBe controllers.add.routes.SubcontractorContactDetailsYesNoController.onPageLoad(NormalMode)
      }

      "must go from a SubcontractorContactDetailsYesNoPage to SubContactDetailsPage when true" in {
        navigator.nextPage(
          SubcontractorContactDetailsYesNoPage,
          NormalMode,
          emptyUserAnswers.setOrException(SubcontractorContactDetailsYesNoPage, true)
        ) mustBe controllers.add.routes.SubContactDetailsController.onPageLoad(NormalMode)
      }

      "must go from SubcontractorContactDetailsYesNoPage to CYA when false" in {
        val ua =
          emptyUserAnswers
            .setOrException(SubcontractorContactDetailsYesNoPage, false)

        val navigator = new Navigator
        navigator.nextPage(
          SubcontractorContactDetailsYesNoPage,
          NormalMode,
          ua
        ) mustBe controllers.add.routes.CheckYourAnswersController.onPageLoad()
      }

      "must go from a SubcontractorContactDetailsYesNoPage to journey recovery when incomplete info provided" in {
        navigator.nextPage(
          SubcontractorContactDetailsYesNoPage,
          NormalMode,
          emptyUserAnswers
        ) mustBe journeyRecovery
      }

      "must go from a SubContactDetailsPage to CYA" in {
        navigator.nextPage(
          SubContactDetailsPage,
          NormalMode,
          UserAnswers("id")
        ) mustBe controllers.add.routes.CheckYourAnswersController.onPageLoad()
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

      "must go from a PartnershipWorksReferenceNumberPage to CheckYourAnswers Page" in {
        navigator.nextPage(
          PartnershipWorksReferenceNumberPage,
          CheckMode,
          emptyUserAnswers.setOrException(PartnershipUniqueTaxpayerReferencePage, "UTR-123")
        ) mustBe controllers.add.routes.CheckYourAnswersController.onPageLoad()
      }

      "must go from PartnershipAddressYesNoPage to PartnershipAddressYesNoController in NormalMode" in {
        navigator.nextPage(
          PartnershipAddressYesNoPage,
          NormalMode,
          emptyUserAnswers
        ) mustBe controllers.add.partnership.routes.PartnershipAddressYesNoController.onPageLoad(NormalMode)
      }

      "must go from PartnershipContactDetailsYesNoPage" - {
        "to PartnershipContactDetailsPage when answer is Yes" in {
          val answers = UserAnswers(userAnswersId).set(PartnershipContactDetailsYesNoPage, true).success.value

          // TODO: SL0201 - B (PTN) - Contact details for partner Controller
          navigator.nextPage(
            PartnershipContactDetailsYesNoPage,
            NormalMode,
            answers
          ) mustBe routes.JourneyRecoveryController.onPageLoad()
        }

        "to PartnershipNominatedPartnerNamePage when answer is No" in {
          val answers = UserAnswers(userAnswersId).set(PartnershipContactDetailsYesNoPage, false).success.value

          // TODO: SL0205 - B (PTN) - Nominated partner name controller
          navigator.nextPage(
            PartnershipContactDetailsYesNoPage,
            NormalMode,
            answers
          ) mustBe routes.JourneyRecoveryController.onPageLoad()
        }

        "to JourneyRecoveryPage when answer is not present" in {
          navigator.nextPage(
            PartnershipContactDetailsYesNoPage,
            NormalMode,
            emptyUserAnswers
          ) mustBe routes.JourneyRecoveryController.onPageLoad()
        }
      }

      "must go from PartnershipNominatedPartnerNinoYesNoPage to PartnershipNominatedPartnerNinoYesNoController in NormalMode" in {
        navigator.nextPage(
          PartnershipNominatedPartnerNinoYesNoPage,
          NormalMode,
          emptyUserAnswers
        ) mustBe controllers.add.partnership.routes.PartnershipNominatedPartnerNinoYesNoController
          .onPageLoad(NormalMode)
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

      "must go from TypeOfSubcontractorPage to CYA when valid data is submitted" in {
        navigator.nextPage(
          TypeOfSubcontractorPage,
          CheckMode,
          emptyUserAnswers.setOrException(TypeOfSubcontractorPage, TypeOfSubcontractor.values.head)
        ) mustBe CYA
      }

      "must go from a TypeOfSubcontractorPage to journey recovery page when incomplete info provided" in {
        navigator.nextPage(
          TypeOfSubcontractorPage,
          CheckMode,
          emptyUserAnswers
        ) mustBe journeyRecovery
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

      "must go from a UniqueTaxpayerReferenceYesNoPage to journey recovery when incomplete info provided" in {
        navigator.nextPage(
          UniqueTaxpayerReferenceYesNoPage,
          CheckMode,
          emptyUserAnswers
        ) mustBe journeyRecovery
      }

      "must go from a SubcontractorContactDetailsYesNoPage to next page when true" in {
        navigator.nextPage(
          SubcontractorContactDetailsYesNoPage,
          CheckMode,
          emptyUserAnswers.setOrException(SubcontractorContactDetailsYesNoPage, true)
        ) mustBe controllers.add.routes.SubContactDetailsController.onPageLoad(CheckMode)
      }

      "must go from a SubcontractorContactDetailsYesNoPage to CYA page when false" in {
        navigator.nextPage(
          SubcontractorContactDetailsYesNoPage,
          CheckMode,
          emptyUserAnswers.setOrException(SubcontractorContactDetailsYesNoPage, false)
        ) mustBe CYA
      }

      "must go from a SubcontractorContactDetailsYesNoPage to journey recovery when incomplete info provided" in {
        navigator.nextPage(
          SubcontractorContactDetailsYesNoPage,
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

      "must go from a PartnershipWorksReferenceNumberYesNoPage to next page when true" in {
        navigator.nextPage(
          PartnershipWorksReferenceNumberYesNoPage,
          CheckMode,
          emptyUserAnswers.setOrException(PartnershipWorksReferenceNumberYesNoPage, true)
        ) mustBe controllers.add.partnership.routes.PartnershipWorksReferenceNumberController.onPageLoad(CheckMode)
      }

      "must go from a PartnershipWorksReferenceNumberYesNoPage to CYA page when false" in {
        navigator.nextPage(
          PartnershipWorksReferenceNumberYesNoPage,
          CheckMode,
          emptyUserAnswers.setOrException(PartnershipWorksReferenceNumberYesNoPage, false)
        ) mustBe CYA
      }

      "must go from a PartnershipWorksReferenceNumberYesNoPage to journey recovery page when incomplete info provided" in {
        navigator.nextPage(
          PartnershipWorksReferenceNumberYesNoPage,
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

        val navigator = new Navigator()
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

        val navigator = new Navigator()
        navigator.nextPage(SubTradingNameYesNoPage, CheckMode, ua) mustBe
          controllers.add.routes.CheckYourAnswersController.onPageLoad()
      }

      "must go to TradingNameOfSubcontractorController when answer is Yes and trading name is missing" in {
        val ua =
          emptyUserAnswers
            .set(SubTradingNameYesNoPage, true)
            .success
            .value

        val navigator = new Navigator()
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

        val navigator = new Navigator()
        navigator.nextPage(SubTradingNameYesNoPage, CheckMode, ua) mustBe
          controllers.add.routes.CheckYourAnswersController.onPageLoad()
      }

      "must route to JourneyRecovery when SubTradingNameYesNoPage answer is missing" in {
        val navigator = new Navigator()
        navigator.nextPage(SubTradingNameYesNoPage, CheckMode, emptyUserAnswers) mustBe
          routes.JourneyRecoveryController.onPageLoad()
      }

      "must go from PartnershipAddressYesNoPage to PartnershipAddressYesNoController in CheckMode" in {
        navigator.nextPage(
          PartnershipAddressYesNoPage,
          CheckMode,
          emptyUserAnswers
        ) mustBe controllers.add.partnership.routes.PartnershipAddressYesNoController.onPageLoad(CheckMode)
      }

      "must go from a PartnershipAddressPage to next page" in {
        navigator.nextPage(
          PartnershipAddressPage,
          CheckMode,
          emptyUserAnswers
        ) mustBe controllers.add.partnership.routes.PartnershipAddressController.onPageLoad(CheckMode)
      }

      "must go from PartnershipContactDetailsYesNoPage" - {
        "to PartnershipContactDetailsPage when answer is Yes" in {
          val answers = UserAnswers(userAnswersId).set(PartnershipContactDetailsYesNoPage, true).success.value

          // TODO: SL0201 - B (PTN) - Contact details for partner Controller
          navigator.nextPage(
            PartnershipContactDetailsYesNoPage,
            CheckMode,
            answers
          ) mustBe routes.JourneyRecoveryController.onPageLoad()
        }

        "to Partnership CyaPage when answer is No" in {
          val answers = UserAnswers(userAnswersId).set(PartnershipContactDetailsYesNoPage, false).success.value

          // TODO: Partnership CYA controller
          navigator.nextPage(
            PartnershipContactDetailsYesNoPage,
            CheckMode,
            answers
          ) mustBe routes.JourneyRecoveryController.onPageLoad()
        }

        "to JourneyRecoveryPage when answer is not present" in {
          navigator.nextPage(
            PartnershipContactDetailsYesNoPage,
            CheckMode,
            emptyUserAnswers
          ) mustBe routes.JourneyRecoveryController.onPageLoad()
        }
      }

      "must go from a PartnershipNominatedPartnerNamePage to CYA" in {
        navigator.nextPage(
          PartnershipNominatedPartnerNamePage,
          CheckMode,
          UserAnswers("id")
        ) mustBe CYA
      }

      "must go from PartnershipNominatedPartnerNinoYesNoPage to PartnershipNominatedPartnerNinoYesNoController in CheckMode" in {
        navigator.nextPage(
          PartnershipNominatedPartnerNinoYesNoPage,
          CheckMode,
          emptyUserAnswers
        ) mustBe controllers.add.partnership.routes.PartnershipNominatedPartnerNinoYesNoController.onPageLoad(CheckMode)
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

        "to Partnership CyaPage when answer is No" in {
          val answers = UserAnswers(userAnswersId).set(PartnershipNominatedPartnerCrnYesNoPage, false).success.value

          navigator.nextPage(
            PartnershipNominatedPartnerCrnYesNoPage,
            CheckMode,
            answers
          ) mustBe controllers.add.routes.CheckYourAnswersController.onPageLoad()
        }

        "to JourneyRecoveryPage when answer is not present" in {
          navigator.nextPage(
            PartnershipNominatedPartnerCrnYesNoPage,
            CheckMode,
            emptyUserAnswers
          ) mustBe routes.JourneyRecoveryController.onPageLoad()
        }
      }
    }

    "navigatorFromSubTradingNameYesNoPage in NormalMode" - {
      "must go to TradingNameOfSubcontractorController when answer is Yes" in {
        val ua        = emptyUserAnswers.set(SubTradingNameYesNoPage, true).success.value
        val navigator = new Navigator()
        navigator.nextPage(SubTradingNameYesNoPage, NormalMode, ua) mustBe
          controllers.add.routes.TradingNameOfSubcontractorController.onPageLoad(NormalMode)
      }

      "must go to SubcontractorNameController when answer is No" in {
        val ua        = emptyUserAnswers.set(SubTradingNameYesNoPage, false).success.value
        val navigator = new Navigator()
        navigator.nextPage(SubTradingNameYesNoPage, NormalMode, ua) mustBe
          controllers.add.routes.SubcontractorNameController.onPageLoad(NormalMode)
      }

      "must route to JourneyRecovery when SubTradingNameYesNoPage answer is missing" in {
        val navigator = new Navigator()
        navigator.nextPage(SubTradingNameYesNoPage, NormalMode, emptyUserAnswers) mustBe
          routes.JourneyRecoveryController.onPageLoad()
      }

      "must go from SubAddressYesNoPage to CYA when true and AddressOfSubcontractorPage is already answered" in {
        val addressSample = models.add.UKAddress(
          addressLine1 = "10 Example Street",
          addressLine2 = Some("Suite 2"),
          addressLine3 = "Newcastle",
          addressLine4 = Some("Tyne & Wear"),
          postCode = "NE1 1AA"
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

      "must go from SubcontractorContactDetailsYesNoPage to CYA in CheckMode when true and SubContactDetailsPage is already answered" in {
        val contactDetailsSample = models.add.SubContactDetails("test@test.com", "11222121221")

        val ua =
          emptyUserAnswers
            .set(SubcontractorContactDetailsYesNoPage, true)
            .success
            .value
            .set(SubContactDetailsPage, contactDetailsSample)
            .success
            .value

        val result = navigator.nextPage(SubcontractorContactDetailsYesNoPage, CheckMode, ua)
        result mustBe CYA
      }

      "must go from a SubContactDetailsPage to CYA" in {
        navigator.nextPage(
          SubContactDetailsPage,
          CheckMode,
          UserAnswers("id")
        ) mustBe CYA
      }

      "must go from PartnershipNominatedPartnerNinoPage to itself in NormalMode" in {
        navigator.nextPage(
          PartnershipNominatedPartnerNinoPage,
          NormalMode,
          emptyUserAnswers
        ) mustBe controllers.add.partnership.routes.PartnershipNominatedPartnerNinoController.onPageLoad(NormalMode)
      }

      "must go from PartnershipNominatedPartnerNinoPage to CYA in CheckMode" in {
        navigator.nextPage(
          PartnershipNominatedPartnerNinoPage,
          CheckMode,
          emptyUserAnswers
        ) mustBe controllers.add.routes.CheckYourAnswersController.onPageLoad()
      }

    }
  }
}
