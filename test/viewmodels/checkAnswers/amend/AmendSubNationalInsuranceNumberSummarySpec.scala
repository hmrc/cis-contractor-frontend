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
import pages.add.SubNationalInsuranceNumberPage
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.*

class AmendSubNationalInsuranceNumberSummarySpec extends AnyFreeSpec with Matchers with CyaEncodingSpecHelper {

  implicit val messages: Messages = stubMessages()

  "AmendSubNationalInsuranceNumberSummary.row" - {

    "must return a SummaryListRow when the answer exists" in {
      val answers =
        UserAnswers("test-id")
          .set(SubNationalInsuranceNumberPage, "AA123456A")
          .success
          .value

      val maybeRow = AmendSubNationalInsuranceNumberSummary.row(answers)
      maybeRow shouldBe defined

      val row = maybeRow.value

      val expectedKeyText = messages("subNationalInsuranceNumber.checkYourAnswersLabel")
      row.key.content.asHtml.toString should include(expectedKeyText)

      row.value.content.asHtml.toString should include("AA123456A")

      row.actions shouldBe defined
      val actions = row.actions.value.items
      actions should have size 1

      val changeAction       = actions.head
      val expectedChangeText = messages("site.change")
      val expectedHref       = routes.SubNationalInsuranceNumberController.onPageLoad(AmendMode).url
      val expectedHiddenText = messages("subNationalInsuranceNumber.change.hidden")

      changeAction.content.asHtml.toString should include(expectedChangeText)
      changeAction.href                  shouldBe expectedHref

      changeAction.visuallyHiddenText.value shouldBe expectedHiddenText
    }

    "must return None when the answer does not exist" in {
      val answers = UserAnswers("test-id")
      AmendSubNationalInsuranceNumberSummary.row(answers) shouldBe None
    }

    "must HTML-escape special characters correctly (single encoding only)" in {

      val nino = "AB123456C & Ref'01"

      val answers =
        UserAnswers("id")
          .set(SubNationalInsuranceNumberPage, nino)
          .success
          .value

      val row = AmendSubNationalInsuranceNumberSummary.row(answers).value

      val html = extractHtml(row)

      assertEscaped(html, "AB123456C &amp; Ref&#x27;01")
      assertNoDoubleEncoding(html)
    }
  }
}
