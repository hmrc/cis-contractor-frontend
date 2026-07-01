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

package viewmodels.checkAnswers.amend.company

import controllers.add.company.routes
import helpers.CyaEncodingSpecHelper
import models.{AmendMode, UserAnswers}
import org.scalatest.OptionValues.convertOptionToValuable
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers.must
import org.scalatest.matchers.should.Matchers
import pages.add.company.CompanyEmailAddressPage
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.*

class CompanyEmailAddressSummarySpec extends AnyFreeSpec with Matchers with CyaEncodingSpecHelper {

  implicit val messages: Messages = stubMessages()

  "CompanyEmailAddressSummary.row" - {

    "must return a SummaryListRow when the answer exists" in {

      val answers =
        UserAnswers("test-id")
          .set(CompanyEmailAddressPage, "test@example.com")
          .success
          .value

      val maybeRow = CompanyEmailAddressSummary.row(answers)

      maybeRow shouldBe defined

      val row = maybeRow.value

      val expectedKeyText = messages("companyEmailAddress.checkYourAnswersLabel")
      row.key.content.asHtml.toString should include(expectedKeyText)

      row.value.content.asHtml.toString should include("test@example.com")

      row.actions shouldBe defined
      val actions = row.actions.value.items
      actions should have size 1

      val changeAction       = actions.head
      val expectedChangeText = messages("site.change")
      val expectedHref       = routes.CompanyEmailAddressController
        .onPageLoad(AmendMode)
        .url
      val expectedHiddenText = messages("companyEmailAddress.change.hidden")

      changeAction.content.asHtml.toString    should include(expectedChangeText)
      changeAction.href                     shouldBe expectedHref
      changeAction.visuallyHiddenText.value shouldBe expectedHiddenText
      changeAction.attributes                   must contain("id" -> "company-email-address")
    }

    "must return None when the answer does not exist" in {

      val answers = UserAnswers("test-id")

      CompanyEmailAddressSummary.row(answers) shouldBe None
    }

    "must HTML-escape special characters correctly (single encoding only)" in {

      val email = "o'reilly+test&co@example.com"

      val answers =
        UserAnswers("id")
          .set(CompanyEmailAddressPage, email)
          .success
          .value

      val row = CompanyEmailAddressSummary.row(answers).value

      val html = extractHtml(row)

      assertEscaped(html, "o&#x27;reilly+test&amp;co@example.com")
      assertNoDoubleEncoding(html)
    }
  }
}
