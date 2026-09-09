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

package viewmodels.checkAnswers.add

import controllers.add.routes
import helpers.CyaEncodingSpecHelper
import models.{AmendMode, CheckMode, UserAnswers}
import org.scalatest.OptionValues.convertOptionToValuable
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import pages.add.WorksReferenceNumberPage
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.*
import models.info.IndividualAnswers

class WorksReferenceNumberSummarySpec extends AnyFreeSpec with Matchers with CyaEncodingSpecHelper {

  implicit val messages: Messages = stubMessages()

  "WorksReferenceNumberSummary.row" - {

    "must return a SummaryListRow when the answer exists" in {
      val answers =
        UserAnswers("test-id")
          .set(WorksReferenceNumberPage, "1234567890")
          .success
          .value

      val maybeRow = WorksReferenceNumberSummary.row(answers)
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
        routes.WorksReferenceNumberController.onPageLoad(CheckMode).url
      val expectedHiddenText = messages("worksReferenceNumber.change.hidden")

      changeAction.content.asHtml.toString should include(expectedChangeText)
      changeAction.href                  shouldBe expectedHref

      changeAction.visuallyHiddenText.value shouldBe expectedHiddenText
    }

    "must return a SummaryListRow when the answer exists in amend journey" in {
      val answers =
        UserAnswers("test-id")
          .set(WorksReferenceNumberPage, "1234567890")
          .success
          .value

      val maybeRow = WorksReferenceNumberSummary.row(answers, AmendMode)
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
    }

    "must return None when the answer does not exist" in {
      val answers = UserAnswers("test-id")
      WorksReferenceNumberSummary.row(answers) shouldBe None
    }
  }

  "ViewOnly - WorksReferenceNumberSummary.row" - {

    "must return a SummaryListRow when works reference exists" in {

      val answers =
        IndividualAnswers(
          subcontractorType = models.TypeOfSubcontractor.Individualorsoletrader,
          showVerificationDetails = false,
          individualNamesOptions = Set.empty,
          tradingName = None,
          subcontractorName = None,
          addressYesNo = None,
          address = None,
          individualContactMethodsYesNo = None,
          individualContactMethod = Set.empty,
          email = None,
          phone = None,
          mobile = None,
          utrYesNo = None,
          utr = None,
          ninoYesNo = None,
          nino = None,
          worksReferenceYesNo = Some(true),
          worksReference = Some("1234567890"),
          verificationNumber = None
        )

      val maybeRow =
        WorksReferenceNumberSummary.row(answers)

      maybeRow shouldBe defined

      val row = maybeRow.value

      row.key.content.asHtml.toString should include(
        messages("worksReferenceNumber.checkYourAnswersLabel")
      )

      row.value.content.asHtml.toString should include("1234567890")

      row.actions             shouldBe defined
      row.actions.value.items shouldBe empty
    }

    "must return None when works reference does not exist" in {

      val answers =
        IndividualAnswers(
          subcontractorType = models.TypeOfSubcontractor.Individualorsoletrader,
          showVerificationDetails = false,
          individualNamesOptions = Set.empty,
          tradingName = None,
          subcontractorName = None,
          addressYesNo = None,
          address = None,
          individualContactMethodsYesNo = None,
          individualContactMethod = Set.empty,
          email = None,
          phone = None,
          mobile = None,
          utrYesNo = None,
          utr = None,
          ninoYesNo = None,
          nino = None,
          worksReferenceYesNo = Some(false),
          worksReference = None,
          verificationNumber = None
        )

      WorksReferenceNumberSummary.row(answers) shouldBe None
    }

    "must HTML-escape special characters correctly for ViewOnly works reference" in {

      val worksReference = "WRN & Ref'01"

      val answers =
        IndividualAnswers(
          subcontractorType = models.TypeOfSubcontractor.Individualorsoletrader,
          showVerificationDetails = false,
          individualNamesOptions = Set.empty,
          tradingName = None,
          subcontractorName = None,
          addressYesNo = None,
          address = None,
          individualContactMethodsYesNo = None,
          individualContactMethod = Set.empty,
          email = None,
          phone = None,
          mobile = None,
          utrYesNo = None,
          utr = None,
          ninoYesNo = None,
          nino = None,
          worksReferenceYesNo = Some(true),
          worksReference = Some(worksReference),
          verificationNumber = None
        )

      val row =
        WorksReferenceNumberSummary.row(answers).value

      val html = extractHtml(row)

      assertEscaped(html, "WRN &amp; Ref&#x27;01")
      assertNoDoubleEncoding(html)

      row.actions             shouldBe defined
      row.actions.value.items shouldBe empty
    }
  }
}
