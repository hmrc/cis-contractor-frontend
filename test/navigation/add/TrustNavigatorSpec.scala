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
import models.{CheckMode, NormalMode, UserAnswers}
import org.scalactic.Prettifier.default
import pages.Page
import pages.add.trust.TrustAddressYesNoPage

class TrustNavigatorSpec extends SpecBase {

  val navigator = new TrustNavigator

  "TrustNavigator" - {

    "in Normal mode" - {

      "must go from a page that doesn't exist in the route map to Index" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, NormalMode, UserAnswers("id")) mustBe routes.IndexController.onPageLoad()
      }

      "must go from a TrustAddressYesNoPage to TrustAddressYesNoPage" in {
        navigator.nextPage(
          TrustAddressYesNoPage,
          NormalMode,
          UserAnswers("id")
        ) mustBe controllers.add.trust.routes.TrustAddressYesNoController.onPageLoad(NormalMode)
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

      "must go from TrustAddressYesNoPage to TrustAddressYesNoPage in CheckMode" in {
        navigator.nextPage(
          TrustAddressYesNoPage,
          CheckMode,
          emptyUserAnswers
        ) mustBe controllers.add.trust.routes.TrustAddressYesNoController.onPageLoad(CheckMode)
      }

    }
  }
}

