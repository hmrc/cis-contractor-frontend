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
import models.add.IndividualNamesOptions
import models.{CheckMode, NormalMode, TypeOfSubcontractor, UserAnswers}
import pages.Page
import pages.add.TypeOfSubcontractorPage
import pages.add.company.{CompanyCrnYesNoPage, CompanyUtrYesNoPage, CompanyWorksReferenceYesNoPage}
import pages.add.company.{AddCompanyContactMethodsYesNoPage, CompanyAddressYesNoPage, CompanyNamePage}
import pages.add.partnership.{PartnershipNominatedPartnerNamePage, PartnershipNominatedPartnerUtrYesNoPage, PartnershipWorksReferenceNumberYesNoPage}
import pages.add.partnership.{PartnershipAddressYesNoPage, PartnershipHasUtrYesNoPage, PartnershipNamePage, PartnershipNominatedPartnerNinoYesNoPage}
import pages.add.partnership.{AddPartnershipContactMethodsYesNoPage, PartnershipNominatedPartnerCrnYesNoPage}
import pages.add.trust.{AddTrustContactMethodsYesNoPage, TrustAddressYesNoPage, TrustNamePage, TrustUtrYesNoPage, TrustWorksReferenceYesNoPage}

class SharedNavigatorSpec extends SpecBase {

  val navigator                    = new SharedNavigator
  private lazy val journeyRecovery = routes.JourneyRecoveryController.onPageLoad()
  private lazy val CYA             = controllers.add.routes.CheckYourAnswersController.onPageLoad()

  private val completeIndividual =
    emptyUserAnswers
      .setOrException(TypeOfSubcontractorPage, TypeOfSubcontractor.Individualorsoletrader)
      .setOrException(pages.add.IndividualNamesOptionsPage, Set(IndividualNamesOptions.TradingName))
      .setOrException(pages.add.TradingNameOfSubcontractorPage, "ABC Ltd")
      .setOrException(pages.add.SubAddressYesNoPage, false)
      .setOrException(pages.add.AddIndividualContactMethodsYesNoPage, false)
      .setOrException(pages.add.UniqueTaxpayerReferenceYesNoPage, false)
      .setOrException(pages.add.NationalInsuranceNumberYesNoPage, false)
      .setOrException(pages.add.WorksReferenceNumberYesNoPage, false)

  private val completeCompany =
    emptyUserAnswers
      .setOrException(TypeOfSubcontractorPage, TypeOfSubcontractor.Limitedcompany)
      .setOrException(CompanyNamePage, "Acme Ltd")
      .setOrException(CompanyAddressYesNoPage, false)
      .setOrException(AddCompanyContactMethodsYesNoPage, false)
      .setOrException(CompanyUtrYesNoPage, false)
      .setOrException(CompanyCrnYesNoPage, false)
      .setOrException(CompanyWorksReferenceYesNoPage, false)

  private val completePartnership =
    emptyUserAnswers
      .setOrException(TypeOfSubcontractorPage, TypeOfSubcontractor.Partnership)
      .setOrException(PartnershipNamePage, "Smith & Jones")
      .setOrException(PartnershipAddressYesNoPage, false)
      .setOrException(AddPartnershipContactMethodsYesNoPage, false)
      .setOrException(PartnershipHasUtrYesNoPage, false)
      .setOrException(PartnershipNominatedPartnerNamePage, "Alice Smith")
      .setOrException(PartnershipNominatedPartnerUtrYesNoPage, false)
      .setOrException(PartnershipNominatedPartnerNinoYesNoPage, false)
      .setOrException(PartnershipNominatedPartnerCrnYesNoPage, false)
      .setOrException(PartnershipWorksReferenceNumberYesNoPage, false)

  private val completeTrust =
    emptyUserAnswers
      .setOrException(TypeOfSubcontractorPage, TypeOfSubcontractor.Trust)
      .setOrException(TrustNamePage, "Smith Family Trust")
      .setOrException(TrustAddressYesNoPage, false)
      .setOrException(AddTrustContactMethodsYesNoPage, false)
      .setOrException(TrustUtrYesNoPage, false)
      .setOrException(TrustWorksReferenceYesNoPage, false)

  "SharedNavigator" - {

    "in Normal mode" - {

      "must go from a page that doesn't exist in the route map to Index" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, NormalMode, UserAnswers("id")) mustBe routes.IndexController.onPageLoad()
      }

      "must go from a TypeOfSubcontractorPage to IndividualNamesOptionsPage when Individualorsoletrader is selected" in {
        navigator.nextPage(
          TypeOfSubcontractorPage,
          NormalMode,
          emptyUserAnswers.setOrException(TypeOfSubcontractorPage, TypeOfSubcontractor.Individualorsoletrader)
        ) mustBe controllers.add.routes.IndividualNamesOptionsController.onPageLoad(NormalMode)
      }

      "must go from a TypeOfSubcontractorPage to CompanyNameController when Limitedcompany is selected" in {
        navigator.nextPage(
          TypeOfSubcontractorPage,
          NormalMode,
          emptyUserAnswers.setOrException(TypeOfSubcontractorPage, TypeOfSubcontractor.Limitedcompany)
        ) mustBe controllers.add.company.routes.CompanyNameController.onPageLoad(NormalMode)
      }

      "must go from a TypeOfSubcontractorPage to PartnershipNameController when Partnership is selected" in {
        navigator.nextPage(
          TypeOfSubcontractorPage,
          NormalMode,
          emptyUserAnswers.setOrException(TypeOfSubcontractorPage, TypeOfSubcontractor.Partnership)
        ) mustBe controllers.add.partnership.routes.PartnershipNameController.onPageLoad(NormalMode)
      }

      "must go from a TypeOfSubcontractorPage to TrustNameController when Trust is selected" in {
        navigator.nextPage(
          TypeOfSubcontractorPage,
          NormalMode,
          emptyUserAnswers.setOrException(TypeOfSubcontractorPage, TypeOfSubcontractor.Trust)
        ) mustBe controllers.add.trust.routes.TrustNameController.onPageLoad(NormalMode)
      }

      "must go from a TypeOfSubcontractorPage to journey recovery page when incomplete info provided" in {
        navigator.nextPage(
          TypeOfSubcontractorPage,
          NormalMode,
          emptyUserAnswers
        ) mustBe journeyRecovery
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

      "must go from TypeOfSubcontractorPage to individual CYA when the individual journey is complete (type unchanged)" in {
        navigator.nextPage(
          TypeOfSubcontractorPage,
          CheckMode,
          completeIndividual
        ) mustBe CYA
      }

      "must go from TypeOfSubcontractorPage to company CYA when the company journey is complete (type unchanged)" in {
        navigator.nextPage(
          TypeOfSubcontractorPage,
          CheckMode,
          completeCompany
        ) mustBe controllers.add.company.routes.CompanyCheckYourAnswersController.onPageLoad()
      }

      "must go from TypeOfSubcontractorPage to partnership CYA when the partnership journey is complete (type unchanged)" in {
        navigator.nextPage(
          TypeOfSubcontractorPage,
          CheckMode,
          completePartnership
        ) mustBe controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad()
      }

      "must go from TypeOfSubcontractorPage to trust CYA when the trust journey is complete (type unchanged)" in {
        navigator.nextPage(
          TypeOfSubcontractorPage,
          CheckMode,
          completeTrust
        ) mustBe controllers.add.trust.routes.TrustCheckYourAnswersController.onPageLoad()
      }

      "must go from TypeOfSubcontractorPage to the first journey page in Normal mode when the selected type's journey is incomplete (type changed)" in {
        navigator.nextPage(
          TypeOfSubcontractorPage,
          CheckMode,
          emptyUserAnswers.setOrException(TypeOfSubcontractorPage, TypeOfSubcontractor.Partnership)
        ) mustBe controllers.add.partnership.routes.PartnershipNameController.onPageLoad(NormalMode)
      }

      "must go from a TypeOfSubcontractorPage to journey recovery page when incomplete info provided" in {
        navigator.nextPage(
          TypeOfSubcontractorPage,
          CheckMode,
          emptyUserAnswers
        ) mustBe journeyRecovery
      }

    }

  }

}
