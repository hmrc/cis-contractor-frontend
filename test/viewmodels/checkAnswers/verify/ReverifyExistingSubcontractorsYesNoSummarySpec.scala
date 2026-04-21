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

package viewmodels.checkAnswers.verify

import controllers.verify.routes
import models.UserAnswers
import org.scalatest.OptionValues.convertOptionToValuable
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import pages.verify.ReverifyExistingSubcontractorsYesNoPage
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow

class ReverifyExistingSubcontractorsYesNoSummarySpec extends AnyFreeSpec with Matchers {

  implicit val messages: Messages = stubMessages()

  "ReverifyExistingSubcontractorsYesNoSummary.row" - {

    "must return a SummaryListRow with Yes when the answer is true" in {

      val answers =
        UserAnswers("test-id")
          .set(ReverifyExistingSubcontractorsYesNoPage, true)
          .success
          .value

      val maybeRow: Option[SummaryListRow] =
        ReverifyExistingSubcontractorsYesNoSummary.row(answers)

      maybeRow shouldBe defined

      val row = maybeRow.value

      row.key.content.asHtml.toString should include(
        messages("reverifyExistingSubcontractorsYesNo.checkYourAnswersLabel")
      )

      row.value.content.asHtml.toString should include(
        messages("site.yes")
      )

      row.actions shouldBe defined
      val actions = row.actions.value.items
      actions should have size 1

      val changeAction = actions.head
      changeAction.content.asHtml.toString    should include(
        messages("site.change")
      )
      changeAction.href                     shouldBe
        routes.ReverifyExistingSubcontractorsYesNoController.onPageLoad().url
      changeAction.visuallyHiddenText.value shouldBe
        messages("reverifyExistingSubcontractorsYesNo.change.hidden")
    }

    "must return a SummaryListRow with No when the answer is false" in {

      val answers =
        UserAnswers("test-id")
          .set(ReverifyExistingSubcontractorsYesNoPage, false)
          .success
          .value

      val row =
        ReverifyExistingSubcontractorsYesNoSummary
          .row(answers)
          .value

      row.value.content.asHtml.toString should include(
        messages("site.no")
      )
    }

    "must return None when the answer does not exist" in {

      val answers = UserAnswers("test-id")

      ReverifyExistingSubcontractorsYesNoSummary.row(answers) shouldBe None
    }
  }
}
