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

package viewmodels.checkAnswers.contractordetails

import controllers.contractordetails.routes
import models.{CheckMode, UserAnswers}
import org.scalatest.OptionValues.convertOptionToValuable
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import pages.contractordetails.ContractorUtrPage
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.*

class ContractorUtrSummarySpec extends AnyFreeSpec with Matchers {

  implicit val messages: Messages = stubMessages()

  "ContractorUtrSummary.row" - {

    "must return a SummaryListRow when the answer exists" in {
      val answers =
        UserAnswers("test-id")
          .set(ContractorUtrPage, "1234567890")
          .success
          .value

      val maybeRow = ContractorUtrSummary.row(answers)
      maybeRow shouldBe defined

      val row = maybeRow.value

      val expectedKeyText = messages("contractordetails.contractorUtr.checkYourAnswersLabel")
      row.key.content.asHtml.toString should include(expectedKeyText)

      row.value.content.asHtml.toString should include("1234567890")

      row.actions shouldBe defined
      val actions = row.actions.value.items
      actions should have size 1

      val changeAction       = actions.head
      val expectedChangeText = messages("site.change")
      val expectedHref       = routes.ContractorUtrController.onPageLoad(CheckMode).url
      val expectedHiddenText = messages("contractordetails.contractorUtr.change.hidden")

      changeAction.content.asHtml.toString    should include(expectedChangeText)
      changeAction.href                     shouldBe expectedHref
      changeAction.visuallyHiddenText.value shouldBe expectedHiddenText
    }

    "must return None when the answer does not exist" in {
      val answers = UserAnswers("test-id")
      ContractorUtrSummary.row(answers) shouldBe None
    }

    "must return a SummaryListRow with empty value and Add Details link when the answer is empty" in {
      val answers =
        UserAnswers("test-id")
          .set(ContractorUtrPage, "")
          .success
          .value

      val maybeRow = ContractorUtrSummary.row(answers)
      val row      = maybeRow.value

      val expectedKeyText =
        messages("contractordetails.contractorUtr.checkYourAnswersLabel")
      row.key.content.asHtml.toString should include(expectedKeyText)

      row.value.content.asHtml.toString shouldBe ""

      val actions = row.actions.value.items
      actions should have size 1

      val action = actions.head

      val expectedAddDetailsText = messages("contractordetails.contractorDetailsCheckAnswers.table.link.addDetails")

      action.content.asHtml.toString should include(expectedAddDetailsText)

      val expectedHref = routes.ContractorUtrController.onPageLoad(CheckMode).url
      action.href shouldBe expectedHref

      val expectedHiddenText = messages("contractordetails.contractorUtr.change.hidden")
      action.visuallyHiddenText.value shouldBe expectedHiddenText
    }
  }
}
