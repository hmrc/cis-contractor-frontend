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

package viewmodels.checkAnswers.add.company

import controllers.add.company.routes
import helpers.CyaEncodingSpecHelper
import models.{AmendMode, CheckMode, UserAnswers}
import org.scalatest.OptionValues.convertOptionToValuable
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers.must
import org.scalatest.matchers.should.Matchers
import pages.add.company.CompanyUtrPage
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.*
import models.TypeOfSubcontractor
import models.info.company.CompanyAnswers

class CompanyUtrSummarySpec extends AnyFreeSpec with Matchers with CyaEncodingSpecHelper {
  implicit val messages: Messages = stubMessages()

  "CompanyUtrSummary.row" - {

    "must return a SummaryListRow when the answer exists" in {
      val answers =
        UserAnswers("test-id")
          .set(CompanyUtrPage, "7777777777")
          .success
          .value

      val maybeRow = CompanyUtrSummary.row(answers)
      maybeRow shouldBe defined

      val row = maybeRow.value

      val expectedKeyText = messages("companyUtr.checkYourAnswersLabel")
      row.key.content.asHtml.toString should include(expectedKeyText)

      row.value.content.asHtml.toString should include("7777777777")

      row.actions shouldBe defined
      val actions = row.actions.value.items
      actions should have size 1

      val changeAction       = actions.head
      val expectedChangeText = messages("site.change")
      val expectedHref       = routes.CompanyUtrController.onPageLoad(CheckMode).url
      val expectedHiddenText = messages("companyUtr.change.hidden")

      changeAction.content.asHtml.toString should include(expectedChangeText)
      changeAction.href                  shouldBe expectedHref

      changeAction.visuallyHiddenText.value shouldBe expectedHiddenText
      changeAction.attributes                   must contain("id" -> "company-utr")
    }

    "must return a SummaryListRow when the answer exists in Amend journey" in {
      val answers =
        UserAnswers("test-id")
          .set(CompanyUtrPage, "7777777777")
          .success
          .value

      val maybeRow = CompanyUtrSummary.row(answers, AmendMode)
      maybeRow shouldBe defined

      val row = maybeRow.value

      val expectedKeyText = messages("companyUtr.checkYourAnswersLabel")
      row.key.content.asHtml.toString should include(expectedKeyText)

      row.value.content.asHtml.toString should include("7777777777")

      row.actions shouldBe defined
      val actions = row.actions.value.items
      actions should have size 1

      val changeAction       = actions.head
      val expectedChangeText = messages("site.change")
      val expectedHref       = routes.CompanyUtrController.onPageLoad(AmendMode).url
      val expectedHiddenText = messages("companyUtr.change.hidden")

      changeAction.content.asHtml.toString should include(expectedChangeText)
      changeAction.href                  shouldBe expectedHref

      changeAction.visuallyHiddenText.value shouldBe expectedHiddenText
      changeAction.attributes                   must contain("id" -> "company-utr")
    }

    "must not include actions when showActions is false" in {
      val utr = "1234567890"

      val answers =
        UserAnswers("test-id")
          .set(CompanyUtrPage, utr)
          .success
          .value

      val maybeRow = CompanyUtrSummary.row(
        answers,
        mode = CheckMode,
        showActions = false
      )

      maybeRow shouldBe defined

      val row = maybeRow.value

      row.key.content.asHtml.toString should include(
        messages("companyUtr.verified.checkYourAnswersLabel")
      )

      row.value.content.asHtml.toString should include(utr)

      row.actions             shouldBe defined
      row.actions.value.items shouldBe empty
    }

    "must return None when the answer does not exist" in {
      val answers = UserAnswers("test-id")
      CompanyUtrSummary.row(answers) shouldBe None
    }
  }

  "CompanyUtrSummary.row with ViewOnlyCompanyAnswers" - {

    def viewOnlyAnswers(
      utr: Option[String] = None
    ): CompanyAnswers =
      CompanyAnswers(
        subcontractorType = TypeOfSubcontractor.Limitedcompany,
        showVerificationDetails = false,
        companyName = None,
        addressYesNo = None,
        address = None,
        companyContactMethodsYesNo = None,
        companyContactMethod = Set.empty,
        email = None,
        phone = None,
        mobile = None,
        crnYesNo = None,
        crn = None,
        utrYesNo = None,
        utr = utr,
        worksReferenceYesNo = None,
        worksReference = None,
        verificationNumber = None
      )

    "must return a SummaryListRow when the UTR exists and is verified" in {

      val answers = viewOnlyAnswers(Some("7777777777"))

      val maybeRow = CompanyUtrSummary.row(
        answers,
        isVerified = true
      )

      maybeRow shouldBe defined

      val row = maybeRow.value

      val expectedKeyText =
        messages("companyUtr.verified.checkYourAnswersLabel")

      row.key.content.asHtml.toString   should include(expectedKeyText)
      row.value.content.asHtml.toString should include("7777777777")

      row.actions             shouldBe defined
      row.actions.value.items shouldBe empty
    }

    "must return a SummaryListRow when the UTR exists and is not verified" in {

      val answers = viewOnlyAnswers(Some("7777777777"))

      val maybeRow = CompanyUtrSummary.row(
        answers,
        isVerified = false
      )

      maybeRow shouldBe defined

      val row = maybeRow.value

      val expectedKeyText =
        messages("companyUtr.checkYourAnswersLabel")

      row.key.content.asHtml.toString   should include(expectedKeyText)
      row.value.content.asHtml.toString should include("7777777777")

      row.actions             shouldBe defined
      row.actions.value.items shouldBe empty
    }

    "must return None when the UTR does not exist" in {

      val answers = viewOnlyAnswers()

      CompanyUtrSummary.row(
        answers,
        isVerified = true
      ) shouldBe None
    }

    "must HTML-escape special characters correctly (single encoding only)" in {

      val utr = "1234&5678'90"

      val answers = viewOnlyAnswers(Some(utr))

      val maybeRow = CompanyUtrSummary.row(
        answers,
        isVerified = false
      )

      maybeRow shouldBe defined

      val row = maybeRow.value

      val html = extractHtml(row)

      assertEscaped(html, "1234&amp;5678&#x27;90")
      assertNoDoubleEncoding(html)
    }
  }
}
