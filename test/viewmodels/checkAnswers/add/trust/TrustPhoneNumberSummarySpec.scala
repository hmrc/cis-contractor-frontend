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

import helpers.CyaEncodingSpecHelper
import models.{AmendMode, CheckMode, UserAnswers}
import org.scalatest.OptionValues.convertOptionToValuable
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import pages.add.trust.TrustPhoneNumberPage
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages

class TrustPhoneNumberSummarySpec extends AnyFreeSpec with Matchers with CyaEncodingSpecHelper {
  implicit val messages: Messages = stubMessages()

  "TrustPhoneNumberSummary.row" - {

    "must return a Summary List Row when the answer exists" in {
      val answers =
        UserAnswers("test-id")
          .set(TrustPhoneNumberPage, "0987456231")
          .success
          .value

      val maybeRow = TrustPhoneNumberSummary.row(answers)
      maybeRow shouldBe defined

      val row = maybeRow.value

      val expectedKeyText = messages("trustPhoneNumber.checkYourAnswersLabel")
      row.key.content.asHtml.toString should include(expectedKeyText)

      row.value.content.asHtml.toString should include("0987456231")

      row.actions shouldBe defined
      val actions = row.actions.value.items
      actions should have size 1

      val changeAction       = actions.head
      val expectedChangeText = messages("site.change")
      val expectedHref       = controllers.add.trust.routes.TrustPhoneNumberController.onPageLoad(CheckMode).url
      val expectedHiddenText = messages("trustPhoneNumber.change.hidden")

      changeAction.content.asHtml.toString should include(expectedChangeText)
      changeAction.href                  shouldBe expectedHref

      changeAction.visuallyHiddenText.value shouldBe expectedHiddenText
    }

    "must return a Summary List Row when the answer exists in AmendMode" in {
      val answers =
        UserAnswers("test-id")
          .set(TrustPhoneNumberPage, "0987456231")
          .success
          .value

      val maybeRow = TrustPhoneNumberSummary.row(answers, AmendMode)
      maybeRow shouldBe defined

      val row = maybeRow.value

      val expectedKeyText = messages("trustPhoneNumber.checkYourAnswersLabel")
      row.key.content.asHtml.toString should include(expectedKeyText)

      row.value.content.asHtml.toString should include("0987456231")

      row.actions shouldBe defined
      val actions = row.actions.value.items
      actions should have size 1

      val changeAction = actions.head
      val expectedChangeText = messages("site.change")
      val expectedHref = controllers.add.trust.routes.TrustPhoneNumberController.onPageLoad(AmendMode).url
      val expectedHiddenText = messages("trustPhoneNumber.change.hidden")

      changeAction.content.asHtml.toString should include(expectedChangeText)
      changeAction.href shouldBe expectedHref

      changeAction.visuallyHiddenText.value shouldBe expectedHiddenText
    }

    "must return None when the answer does not exist" in {
      val answers = UserAnswers("test-id")
      TrustPhoneNumberSummary.row(answers) shouldBe None
    }

    "must HTML-escape special characters correctly (single encoding only)" in {

      val phone = "020 7946 0958 & ext'78"

      val answers =
        UserAnswers("id")
          .set(TrustPhoneNumberPage, phone)
          .success
          .value

      val row = TrustPhoneNumberSummary.row(answers).value

      val html = extractHtml(row)

      assertEscaped(html, "020 7946 0958 &amp; ext&#x27;78")
      assertNoDoubleEncoding(html)
    }
  }
}
