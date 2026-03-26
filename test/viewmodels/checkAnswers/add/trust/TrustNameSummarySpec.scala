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

package viewmodels.checkAnswers.add.trust

import models.{CheckMode, UserAnswers}
import org.scalatest.OptionValues.convertOptionToValuable
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import pages.add.trust.TrustNamePage
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages

class TrustNameSummarySpec extends AnyFreeSpec with Matchers {

  implicit val messages: Messages = stubMessages()

  "TrustNameSummary.row" - {

    "must return a SummaryListRow when the answer exists" in {
      val answers =
        UserAnswers("test-id")
          .set(TrustNamePage, "Acme Trust")
          .success
          .value

      val maybeRow = TrustNameSummary.row(answers)
      maybeRow shouldBe defined

      val row = maybeRow.value

      row.key.content.asHtml.toString should include(messages("trustName.checkYourAnswersLabel"))

      row.value.content.asHtml.toString should include("Acme Trust")

      row.actions shouldBe defined
      val actions = row.actions.value.items
      actions should have size 1

      val changeAction = actions.head
      changeAction.content.asHtml.toString    should include(messages("site.change"))
      changeAction.href                     shouldBe controllers.add.trust.routes.TrustNameController.onPageLoad(CheckMode).url
      changeAction.visuallyHiddenText.value shouldBe messages("trustName.change.hidden")
    }

    "must return None when the answer does not exist" in {
      TrustNameSummary.row(UserAnswers("test-id")) shouldBe None
    }
  }
}
