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

package navigation.contractordetails

import base.SpecBase
import controllers.contractordetails.{routes => contractorDetailsRoutes}
import controllers.routes
import models.{AmendMode, CheckMode, NormalMode, UserAnswers}
import pages.Page
import pages.contractordetails.*

class ContractorDetailsNavigatorSpec extends SpecBase {

  val navigator = new ContractorDetailsNavigator

  "ContractorDetailsNavigator" - {

    "in NormalMode" - {

      "must go to IndexController for an unknown page" in {
        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, NormalMode, UserAnswers("id")) mustEqual
          routes.IndexController.onPageLoad()
      }

      "must go from ContractorUtrPage to AddSchemeNameYesNo" in {
        navigator.nextPage(ContractorUtrPage, NormalMode, emptyUserAnswers) mustEqual
          contractorDetailsRoutes.AddSchemeNameYesNoController.onPageLoad(NormalMode)
      }

      "must go from AddSchemeNameYesNoPage to SchemeName when Yes" in {
        val answers = emptyUserAnswers.set(AddSchemeNameYesNoPage, true).success.value
        navigator.nextPage(AddSchemeNameYesNoPage, NormalMode, answers) mustEqual
          contractorDetailsRoutes.SchemeNameController.onPageLoad(NormalMode)
      }

      "must go from AddSchemeNameYesNoPage to AddEmailAddressYesNo when No" in {
        val answers = emptyUserAnswers.set(AddSchemeNameYesNoPage, false).success.value
        navigator.nextPage(AddSchemeNameYesNoPage, NormalMode, answers) mustEqual
          contractorDetailsRoutes.AddEmailAddressYesNoController.onPageLoad(NormalMode)
      }

      "must go to JourneyRecovery from AddSchemeNameYesNoPage when unanswered" in {
        navigator.nextPage(AddSchemeNameYesNoPage, NormalMode, emptyUserAnswers) mustEqual
          routes.JourneyRecoveryController.onPageLoad()
      }

      "must go from SchemeNamePage to AddEmailAddressYesNo" in {
        navigator.nextPage(SchemeNamePage, NormalMode, emptyUserAnswers) mustEqual
          contractorDetailsRoutes.AddEmailAddressYesNoController.onPageLoad(NormalMode)
      }

      "must go from AddEmailAddressYesNoPage to EnterContractorEmailAddress when Yes" in {
        val answers = emptyUserAnswers.set(AddEmailAddressYesNoPage, true).success.value
        navigator.nextPage(AddEmailAddressYesNoPage, NormalMode, answers) mustEqual
          contractorDetailsRoutes.EnterContractorEmailAddressController.onPageLoad(NormalMode)
      }

      "must go from AddEmailAddressYesNoPage to check answers when No" in {
        val answers = emptyUserAnswers.set(AddEmailAddressYesNoPage, false).success.value
        navigator.nextPage(AddEmailAddressYesNoPage, NormalMode, answers) mustEqual
          contractorDetailsRoutes.ContractorDetailsCheckAnswersController.onPageLoad()
      }

      "must go to JourneyRecovery from AddEmailAddressYesNoPage when unanswered" in {
        navigator.nextPage(AddEmailAddressYesNoPage, NormalMode, emptyUserAnswers) mustEqual
          routes.JourneyRecoveryController.onPageLoad()
      }

      "must go from EnterContractorEmailAddressPage to check answers" in {
        navigator.nextPage(EnterContractorEmailAddressPage, NormalMode, emptyUserAnswers) mustEqual
          contractorDetailsRoutes.ContractorDetailsCheckAnswersController.onPageLoad()
      }
    }

    "in CheckMode" - {

      "must go to IndexController for an unknown page" in {
        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, CheckMode, UserAnswers("id")) mustEqual
          routes.IndexController.onPageLoad()
      }

      "must go from ContractorUtrPage to check answers" in {
        navigator.nextPage(ContractorUtrPage, CheckMode, emptyUserAnswers) mustEqual
          contractorDetailsRoutes.ContractorDetailsCheckAnswersController.onPageLoad()
      }

      "must go from AddSchemeNameYesNoPage to SchemeName in CheckMode when Yes" in {
        val answers = emptyUserAnswers.set(AddSchemeNameYesNoPage, true).success.value
        navigator.nextPage(AddSchemeNameYesNoPage, CheckMode, answers) mustEqual
          contractorDetailsRoutes.SchemeNameController.onPageLoad(CheckMode)
      }

      "must go from AddSchemeNameYesNoPage to check answers when No" in {
        val answers = emptyUserAnswers.set(AddSchemeNameYesNoPage, false).success.value
        navigator.nextPage(AddSchemeNameYesNoPage, CheckMode, answers) mustEqual
          contractorDetailsRoutes.ContractorDetailsCheckAnswersController.onPageLoad()
      }

      "must go from SchemeNamePage to check answers" in {
        navigator.nextPage(SchemeNamePage, CheckMode, emptyUserAnswers) mustEqual
          contractorDetailsRoutes.ContractorDetailsCheckAnswersController.onPageLoad()
      }

      "must go from AddEmailAddressYesNoPage to EnterContractorEmailAddress in CheckMode when Yes" in {
        val answers = emptyUserAnswers.set(AddEmailAddressYesNoPage, true).success.value
        navigator.nextPage(AddEmailAddressYesNoPage, CheckMode, answers) mustEqual
          contractorDetailsRoutes.EnterContractorEmailAddressController.onPageLoad(CheckMode)
      }

      "must go from AddEmailAddressYesNoPage to check answers when No" in {
        val answers = emptyUserAnswers.set(AddEmailAddressYesNoPage, false).success.value
        navigator.nextPage(AddEmailAddressYesNoPage, CheckMode, answers) mustEqual
          contractorDetailsRoutes.ContractorDetailsCheckAnswersController.onPageLoad()
      }

      "must go from EnterContractorEmailAddressPage to check answers" in {
        navigator.nextPage(EnterContractorEmailAddressPage, CheckMode, emptyUserAnswers) mustEqual
          contractorDetailsRoutes.ContractorDetailsCheckAnswersController.onPageLoad()
      }
    }

    "in AmendMode" - {

      "must go from ContractorUtrPage to check answers" in {
        navigator.nextPage(ContractorUtrPage, AmendMode, emptyUserAnswers) mustEqual
          contractorDetailsRoutes.ContractorDetailsCheckAnswersController.onPageLoad()
      }

      "must go from AddSchemeNameYesNoPage to SchemeName in CheckMode when Yes" in {
        val answers = emptyUserAnswers.set(AddSchemeNameYesNoPage, true).success.value
        navigator.nextPage(AddSchemeNameYesNoPage, AmendMode, answers) mustEqual
          contractorDetailsRoutes.SchemeNameController.onPageLoad(CheckMode)
      }
    }

    "must implement ContractorDetailsJourney" in {
      ContractorUtrPage mustBe a[ContractorDetailsJourney]
    }
  }
}
