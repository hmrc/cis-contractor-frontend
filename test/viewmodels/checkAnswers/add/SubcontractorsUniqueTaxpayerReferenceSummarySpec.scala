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
import pages.add.SubcontractorsUniqueTaxpayerReferencePage
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.*
import models.info.IndividualAnswers

class SubcontractorsUniqueTaxpayerReferenceSummarySpec extends AnyFreeSpec with Matchers with CyaEncodingSpecHelper {

  implicit val messages: Messages = stubMessages()

  "SubcontractorsUniqueTaxpayerReferenceSummary.row" - {

    "must return a SummaryListRow when the answer exists" in {
      val answers =
        UserAnswers("test-id")
          .set(SubcontractorsUniqueTaxpayerReferencePage, "1234567890")
          .success
          .value

      val maybeRow = SubcontractorsUniqueTaxpayerReferenceSummary.row(answers)
      maybeRow shouldBe defined

      val row = maybeRow.value

      val expectedKeyText = messages("subcontractorsUniqueTaxpayerReference.checkYourAnswersLabel")
      row.key.content.asHtml.toString should include(expectedKeyText)

      row.value.content.asHtml.toString should include("1234567890")

      row.actions shouldBe defined
      val actions = row.actions.value.items
      actions should have size 1

      val changeAction       = actions.head
      val expectedChangeText = messages("site.change")
      val expectedHref       =
        routes.SubcontractorsUniqueTaxpayerReferenceController.onPageLoad(CheckMode).url
      val expectedHiddenText = messages("subcontractorsUniqueTaxpayerReference.change.hidden")

      changeAction.content.asHtml.toString should include(expectedChangeText)
      changeAction.href                  shouldBe expectedHref

      changeAction.visuallyHiddenText.value shouldBe expectedHiddenText
    }

    "must return a SummaryListRow when the answer exists for amend journey" in {
      val answers =
        UserAnswers("test-id")
          .set(SubcontractorsUniqueTaxpayerReferencePage, "1234567890")
          .success
          .value

      val maybeRow = SubcontractorsUniqueTaxpayerReferenceSummary.row(answers, AmendMode)
      maybeRow shouldBe defined

      val row = maybeRow.value

      val expectedKeyText = messages("subcontractorsUniqueTaxpayerReference.checkYourAnswersLabel")
      row.key.content.asHtml.toString should include(expectedKeyText)

      row.value.content.asHtml.toString should include("1234567890")

      row.actions shouldBe defined
      val actions = row.actions.value.items
      actions should have size 1

      val changeAction       = actions.head
      val expectedChangeText = messages("site.change")
      val expectedHref       =
        routes.SubcontractorsUniqueTaxpayerReferenceController.onPageLoad(AmendMode).url
      val expectedHiddenText = messages("subcontractorsUniqueTaxpayerReference.change.hidden")

      changeAction.content.asHtml.toString should include(expectedChangeText)
      changeAction.href                  shouldBe expectedHref

      changeAction.visuallyHiddenText.value shouldBe expectedHiddenText
    }

    "must return None when the answer does not exist" in {
      val answers = UserAnswers("test-id")
      SubcontractorsUniqueTaxpayerReferenceSummary.row(answers) shouldBe None
    }

    "must HTML-escape special characters correctly (single encoding only)" in {

      val utr = "1234567890 & Ref'01"

      val answers =
        UserAnswers("id")
          .set(SubcontractorsUniqueTaxpayerReferencePage, utr)
          .success
          .value

      val row = SubcontractorsUniqueTaxpayerReferenceSummary.row(answers).value

      val html = extractHtml(row)

      assertEscaped(html, "1234567890 &amp; Ref&#x27;01")
      assertNoDoubleEncoding(html)
    }

    "must not include actions when showActions is false" in {
      val utr = "1234567890"

      val answers =
        UserAnswers("test-id")
          .set(SubcontractorsUniqueTaxpayerReferencePage, utr)
          .success
          .value

      val maybeRow = SubcontractorsUniqueTaxpayerReferenceSummary.row(
        answers,
        mode = CheckMode,
        showActions = false
      )

      maybeRow shouldBe defined

      val row = maybeRow.value

      row.key.content.asHtml.toString should include(
        messages("subcontractorsUniqueTaxpayerReference.checkYourAnswersLabel")
      )

      row.value.content.asHtml.toString should include(utr)

      row.actions             shouldBe defined
      row.actions.value.items shouldBe empty
    }
  }

  "ViewOnly - SubcontractorsUniqueTaxpayerReferenceSummary.row" - {

    "must return a SummaryListRow when UTR exists" in {

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
          utrYesNo = Some(true),
          utr = Some("1234567890"),
          ninoYesNo = None,
          nino = None,
          worksReferenceYesNo = None,
          worksReference = None,
          verificationNumber = None
        )

      val maybeRow =
        SubcontractorsUniqueTaxpayerReferenceSummary.row(answers)

      maybeRow shouldBe defined

      val row = maybeRow.value

      row.key.content.asHtml.toString should include(
        messages("subcontractorsUniqueTaxpayerReference.checkYourAnswersLabel")
      )

      row.value.content.asHtml.toString should include("1234567890")

      row.actions             shouldBe defined
      row.actions.value.items shouldBe empty
    }

    "must return None when UTR does not exist" in {

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
          worksReferenceYesNo = None,
          worksReference = None,
          verificationNumber = None
        )

      SubcontractorsUniqueTaxpayerReferenceSummary.row(answers) shouldBe None
    }

    "must HTML-escape special characters correctly for ViewOnly UTR" in {

      val utr = "1234567890 & Ref'01"

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
          utrYesNo = Some(true),
          utr = Some(utr),
          ninoYesNo = None,
          nino = None,
          worksReferenceYesNo = None,
          worksReference = None,
          verificationNumber = None
        )

      val maybeRow =
        SubcontractorsUniqueTaxpayerReferenceSummary.row(answers)

      maybeRow shouldBe defined

      val row = maybeRow.value

      val html = extractHtml(row)

      assertEscaped(html, "1234567890 &amp; Ref&#x27;01")
      assertNoDoubleEncoding(html)

      row.actions             shouldBe defined
      row.actions.value.items shouldBe empty
    }
  }
}
