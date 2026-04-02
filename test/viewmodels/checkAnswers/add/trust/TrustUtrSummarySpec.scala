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

import controllers.add.trust.routes
import models.{CheckMode, UserAnswers}
import org.scalatest.OptionValues.convertOptionToValuable
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers.must
import org.scalatest.matchers.should.Matchers
import pages.add.trust.TrustUtrPage
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.*

class TrustUtrSummarySpec extends AnyFreeSpec with Matchers {

  implicit val messages: Messages = stubMessages()

  "TrustUtrSummary.row" - {

    "must return a SummaryListRow when the answer is present" in {
      val utr     = "1234567890"
      val answers = UserAnswers("test-id").set(TrustUtrPage, utr).success.value

      val maybeRow: Option[SummaryListRow] = TrustUtrSummary.row(answers)
      maybeRow shouldBe defined

      val row = maybeRow.value

      val expectedKeyText = messages("trustUtr.checkYourAnswersLabel")
      row.key.content.asHtml.toString should include(expectedKeyText)

      row.value.content.asHtml.toString should include(utr)

      row.actions shouldBe defined
      val actions = row.actions.value.items
      actions should have size 1

      val changeAction       = actions.head
      val expectedChangeText = messages("site.change")
      val expectedHref       = routes.TrustUtrController.onPageLoad(CheckMode).url
      val expectedHiddenText = messages("trustUtr.change.hidden")

      changeAction.content.asHtml.toString    should include(expectedChangeText)
      changeAction.href                     shouldBe expectedHref
      changeAction.visuallyHiddenText.value shouldBe expectedHiddenText
      changeAction.attributes                   must contain("id" -> "trust-utr")
    }

    "must return None when the answer is not present" in {
      TrustUtrSummary.row(UserAnswers("test-id")) shouldBe None
    }
  }
}
