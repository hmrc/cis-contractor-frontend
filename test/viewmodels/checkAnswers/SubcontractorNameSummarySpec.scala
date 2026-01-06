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

package viewmodels.checkAnswers

import controllers.add.routes
import models.add.SubcontractorName
import models.{CheckMode, UserAnswers}
import org.scalatest.OptionValues.convertOptionToValuable
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers.{convertToStringShouldWrapperForVerb, should, shouldBe}
import pages.add.SubcontractorNamePage
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.checkAnswers.add.SubcontractorNameSummary

class SubcontractorNameSummarySpec extends AnyFreeSpec with Matchers {

  implicit val messages: Messages = stubMessages()

  "SubcontractorNameSummary.row" - {

    "must return a SummaryListRow when the answer exists" in {
      val subcontractorName = SubcontractorName(
        firstName = "John",
        middleName = Some("F."),
        lastName = "Doe"
      )

      val answers = UserAnswers("test-id")
        .set(SubcontractorNamePage, subcontractorName)
        .success
        .value

      val maybeRow: Option[SummaryListRow] = SubcontractorNameSummary.row(answers)
      maybeRow shouldBe defined

      val row = maybeRow.value

      val expectedKeyText = messages("subcontractorName.checkYourAnswersLabel")
      row.key.content.asHtml.toString should include(expectedKeyText)

      val expectedFullName = Seq(Some("John"), Some("F."), Some("Doe")).flatten.mkString(" ")
      row.value.content.asHtml.toString should include(expectedFullName)

      row.actions shouldBe defined
      val actions = row.actions.value.items
      actions should have size 1

      val changeAction       = actions.head
      val expectedChangeText = messages("site.change")
      val expectedHref       = routes.SubcontractorNameController.onPageLoad(CheckMode).url
      val expectedHiddenText = messages("subcontractorName.change.hidden")

      changeAction.content.asHtml.toString    should include(expectedChangeText)
      changeAction.href                     shouldBe expectedHref
      changeAction.visuallyHiddenText.value shouldBe expectedHiddenText
    }

    "must return None when the answer does not exist" in {
      val answers = UserAnswers("test-id")
      SubcontractorNameSummary.row(answers) shouldBe None
    }
  }
}
