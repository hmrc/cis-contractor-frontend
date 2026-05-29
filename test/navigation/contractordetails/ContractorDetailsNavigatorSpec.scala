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
import controllers.routes
import models.{CheckMode, NormalMode, UserAnswers}
import pages.Page
import pages.contractordetails.{ContractorDetailsJourney, ContractorUtrPage}

class ContractorDetailsNavigatorSpec extends SpecBase {

  val navigator = new ContractorDetailsNavigator

  "ContractorDetailsNavigator" - {

    "in Normal mode" - {

      "must go to IndexController for an unknown page" in {
        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, NormalMode, UserAnswers("id")) mustBe
          routes.IndexController.onPageLoad()
      }

      "must go to IndexController from ContractorUtrPage" in {
        navigator.nextPage(ContractorUtrPage, NormalMode, emptyUserAnswers) mustBe
          routes.IndexController.onPageLoad()
      }
    }

    "in Check mode" - {

      "must go to IndexController for an unknown page" in {
        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, CheckMode, UserAnswers("id")) mustBe
          routes.IndexController.onPageLoad()
      }

      "must go to IndexController from ContractorUtrPage" in {
        navigator.nextPage(ContractorUtrPage, CheckMode, emptyUserAnswers) mustBe
          routes.IndexController.onPageLoad()
      }
    }

    "must implement ContractorDetailsJourney" in {
      ContractorUtrPage mustBe a[ContractorDetailsJourney]
    }
  }
}
