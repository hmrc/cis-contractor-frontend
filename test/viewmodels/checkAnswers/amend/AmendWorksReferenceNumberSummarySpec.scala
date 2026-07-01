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

package viewmodels.checkAnswers.amend

import controllers.add.routes
import helpers.CyaEncodingSpecHelper
import models.{AmendMode, UserAnswers}
import org.scalatest.OptionValues.convertOptionToValuable
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import pages.add.WorksReferenceNumberPage
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.*

class AmendWorksReferenceNumberSummarySpec extends AnyFreeSpec with Matchers with CyaEncodingSpecHelper {

  implicit val messages: Messages = stubMessages()

  "AmendWorksReferenceNumberSummary.row" - {

    "must return a SummaryListRow when the answer exists" in {
      val answers =
        UserAnswers("test-id")
          .set(WorksReferenceNumberPage, "1234567890")
          .success
          .value

      val maybeRow = AmendWorksReferenceNumberSummary.row(answers)
      maybeRow shouldBe defined

      val row = maybeRow.value

      val expectedKeyText = messages("worksReferenceNumber.checkYourAnswersLabel")
      row.key.content.asHtml.toString should include(expectedKeyText)

      row.value.content.asHtml.toString should include("1234567890")

      row.actions shouldBe defined
      val actions = row.actions.value.items
      actions should have size 1

      val changeAction       = actions.head
      val expectedChangeText = messages("site.change")
      val expectedHref       =
        routes.WorksReferenceNumberController.onPageLoad(AmendMode).url
      val expectedHiddenText = messages("worksReferenceNumber.change.hidden")

      changeAction.content.asHtml.toString should include(expectedChangeText)
      changeAction.href                  shouldBe expectedHref

      changeAction.visuallyHiddenText.value shouldBe expectedHiddenText
      changeAction.attributes                 should contain("id" -> "works-reference-number")
    }

    "must return None when the answer does not exist" in {
      val answers = UserAnswers("test-id")
      AmendWorksReferenceNumberSummary.row(answers) shouldBe None
    }
  }
}
