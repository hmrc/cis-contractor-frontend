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

package navigation

import models.{Mode, NormalMode, UserAnswers}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import pages.Page
import play.api.mvc.Call

class NavigatorForJourneySpec extends AnyFreeSpec with Matchers {

  "NavigatorForJourney" - {
    "should allow implementations to be used via the common interface" in {
      case object TestPage extends Page

      val impl: NavigatorForJourney = new NavigatorForJourney {
        override def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call =
          Call("GET", "/ok")
      }

      impl.nextPage(TestPage, NormalMode, UserAnswers("id")) mustBe Call("GET", "/ok")
    }
  }
}
