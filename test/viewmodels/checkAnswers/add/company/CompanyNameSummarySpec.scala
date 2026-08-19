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
import models.viewOnly.company.ViewOnlyCompanyAnswers
import models.{AmendMode, CheckMode, TypeOfSubcontractor, UserAnswers}
import org.scalatest.OptionValues.convertOptionToValuable
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers.must
import org.scalatest.matchers.should.Matchers
import pages.add.company.CompanyNamePage
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.*

class CompanyNameSummarySpec extends AnyFreeSpec with Matchers with CyaEncodingSpecHelper {

  implicit val messages: Messages = stubMessages()

  "CompanyNameSummary.row" - {

    "must return a SummaryListRow when the answer exists" in {
      val answers =
        UserAnswers("test-id")
          .set(CompanyNamePage, "ABC Construction Ltd")
          .success
          .value

      val maybeRow = CompanyNameSummary.row(answers)
      maybeRow shouldBe defined

      val row = maybeRow.value

      val expectedKeyText = messages("companyName.checkYourAnswersLabel")
      row.key.content.asHtml.toString should include(expectedKeyText)

      row.value.content.asHtml.toString should include("ABC Construction Ltd")

      row.actions shouldBe defined
      val actions = row.actions.value.items
      actions should have size 1

      val changeAction       = actions.head
      val expectedChangeText = messages("site.change")
      val expectedHref       = routes.CompanyNameController.onPageLoad(CheckMode).url
      val expectedHiddenText = messages("companyName.change.hidden")

      changeAction.content.asHtml.toString    should include(expectedChangeText)
      changeAction.href                     shouldBe expectedHref
      changeAction.visuallyHiddenText.value shouldBe expectedHiddenText
      changeAction.attributes                   must contain("id" -> "company-name")
    }

    "must return a SummaryListRow when the answer exists in Amend journey" in {
      val answers =
        UserAnswers("test-id")
          .set(CompanyNamePage, "ABC Construction Ltd")
          .success
          .value

      val maybeRow = CompanyNameSummary.row(answers, AmendMode)
      maybeRow shouldBe defined

      val row = maybeRow.value

      val expectedKeyText = messages("companyName.checkYourAnswersLabel")
      row.key.content.asHtml.toString should include(expectedKeyText)

      row.value.content.asHtml.toString should include("ABC Construction Ltd")

      row.actions shouldBe defined
      val actions = row.actions.value.items
      actions should have size 1

      val changeAction       = actions.head
      val expectedChangeText = messages("site.change")
      val expectedHref       = routes.CompanyNameController.onPageLoad(AmendMode).url
      val expectedHiddenText = messages("companyName.change.hidden")

      changeAction.content.asHtml.toString    should include(expectedChangeText)
      changeAction.href                     shouldBe expectedHref
      changeAction.visuallyHiddenText.value shouldBe expectedHiddenText
      changeAction.attributes                   must contain("id" -> "company-name")
    }

    "must return None when the answer does not exist" in {
      val answers = UserAnswers("test-id")
      CompanyNameSummary.row(answers) shouldBe None
    }

    "must HTML-escape special characters correctly (single encoding only)" in {

      val name = "O'Reilly & Sons Ltd"

      val answers =
        UserAnswers("id")
          .set(CompanyNamePage, name)
          .success
          .value

      val row = CompanyNameSummary.row(answers).value

      val html = extractHtml(row)

      assertEscaped(html, "O&#x27;Reilly &amp; Sons Ltd")
      assertNoDoubleEncoding(html)
    }
  }

  "CompanyNameSummary.row with ViewOnlyCompanyAnswers" - {

    def viewOnlyAnswers(
      companyName: Option[String] = None
    ): ViewOnlyCompanyAnswers =
      ViewOnlyCompanyAnswers(
        subcontractorType = TypeOfSubcontractor.Limitedcompany,
        showVerificationDetails = false,
        companyName = companyName,
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
        utr = None,
        worksReferenceYesNo = None,
        worksReference = None,
        verificationNumber = None
      )

    "must return a SummaryListRow when the company name exists" in {

      val answers = viewOnlyAnswers(Some("ABC Construction Ltd"))

      val maybeRow = CompanyNameSummary.row(answers)

      maybeRow shouldBe defined

      val row = maybeRow.value

      val expectedKeyText =
        messages("companyName.checkYourAnswersLabel")

      row.key.content.asHtml.toString   should include(expectedKeyText)
      row.value.content.asHtml.toString should include("ABC Construction Ltd")

      row.actions             shouldBe defined
      row.actions.value.items shouldBe empty
    }

    "must return None when the company name does not exist" in {

      val answers = viewOnlyAnswers()

      CompanyNameSummary.row(answers) shouldBe None
    }

    "must HTML-escape special characters correctly (single encoding only)" in {

      val name = "O'Reilly & Sons Ltd"

      val answers = viewOnlyAnswers(Some(name))

      val maybeRow = CompanyNameSummary.row(answers)

      maybeRow shouldBe defined

      val row = maybeRow.value

      val html = extractHtml(row)

      assertEscaped(html, "O&#x27;Reilly &amp; Sons Ltd")
      assertNoDoubleEncoding(html)
    }
  }
}
