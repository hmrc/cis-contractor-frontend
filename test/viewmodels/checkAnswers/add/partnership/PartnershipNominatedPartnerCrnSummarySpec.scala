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

package viewmodels.checkAnswers.add.partnership

import controllers.add.partnership.routes
import helpers.CyaEncodingSpecHelper
import models.{AmendMode, CheckMode, UserAnswers}
import org.scalatest.OptionValues
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import pages.add.partnership.PartnershipNominatedPartnerCrnPage
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages

class PartnershipNominatedPartnerCrnSummarySpec
    extends AnyFreeSpec
    with Matchers
    with OptionValues
    with CyaEncodingSpecHelper {

  implicit val messages: Messages = stubMessages()

  "PartnershipNominatedPartnerCrnSummary.row" - {

    "must return a SummaryListRow when the answer exists" in {

      val ua =
        UserAnswers("test-id")
          .set(PartnershipNominatedPartnerCrnPage, "AC012345")
          .success
          .value

      val row = PartnershipNominatedPartnerCrnSummary.row(ua).value

      row.key.content.asHtml.toString should include(
        messages("partnershipNominatedPartnerCrn.checkYourAnswersLabel")
      )

      row.value.content.asHtml.toString should include("AC012345")

      row.actions.value.items should have size 1
      val action = row.actions.value.items.head

      action.href shouldBe
        routes.PartnershipNominatedPartnerCrnController
          .onPageLoad(CheckMode)
          .url

      action.content.asHtml.toString should include(messages("site.change"))

      action.visuallyHiddenText.value shouldBe
        messages("partnershipNominatedPartnerCrn.change.hidden")

      action.attributes should contain("id" -> "nominated-partner-crn")
    }

    "must return a SummaryListRow when the answer exists for Amend journey" in {

      val ua =
        UserAnswers("test-id")
          .set(PartnershipNominatedPartnerCrnPage, "AC012345")
          .success
          .value

      val row = PartnershipNominatedPartnerCrnSummary.row(ua, AmendMode).value

      row.key.content.asHtml.toString should include(
        messages("partnershipNominatedPartnerCrn.checkYourAnswersLabel")
      )

      row.value.content.asHtml.toString should include("AC012345")

      row.actions.value.items should have size 1
      val action = row.actions.value.items.head

      action.href shouldBe
        routes.PartnershipNominatedPartnerCrnController
          .onPageLoad(AmendMode)
          .url

      action.content.asHtml.toString should include(messages("site.change"))

      action.visuallyHiddenText.value shouldBe
        messages("partnershipNominatedPartnerCrn.change.hidden")

      action.attributes should contain("id" -> "nominated-partner-crn")
    }

    "must return None when the answer does not exist" in {

      val ua = UserAnswers("test-id")

      PartnershipNominatedPartnerCrnSummary.row(ua) shouldBe None
    }

    "must HTML-escape special characters correctly (single encoding only)" in {

      val crn = "CRN & Co '123'"

      val answers =
        UserAnswers("id")
          .set(PartnershipNominatedPartnerCrnPage, crn)
          .success
          .value

      val row = PartnershipNominatedPartnerCrnSummary.row(answers).value

      val html = extractHtml(row)

      assertEscaped(html, "CRN &amp; Co &#x27;123&#x27;")

      assertNoDoubleEncoding(html)
    }
  }
}
