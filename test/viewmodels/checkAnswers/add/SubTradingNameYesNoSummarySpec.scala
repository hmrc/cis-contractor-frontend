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
import models.{AmendMode, CheckMode, UserAnswers}
import org.scalatest.OptionValues.convertOptionToValuable
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import pages.add.SubTradingNameYesNoPage
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.*
import models.viewOnly.ViewOnlyIndividualAnswers

class SubTradingNameYesNoSummarySpec extends AnyFreeSpec with Matchers {
  implicit val messages: Messages = stubMessages()

  "SubTradingNameYesNoSummary.row" - {

    "must return a SummaryListRow with 'Yes' when the answer is true" in {
      val answers = UserAnswers("test-id")
        .set(SubTradingNameYesNoPage, true)
        .success
        .value

      val maybeRow: Option[SummaryListRow] = SubTradingNameYesNoSummary.row(answers)
      maybeRow shouldBe defined

      val row = maybeRow.value

      val expectedKeyText = messages("subTradingNameYesNo.checkYourAnswersLabel")
      row.key.content.asHtml.toString should include(expectedKeyText)

      val expectedValue = messages("site.yes")
      row.value.content.asHtml.toString should include(expectedValue)

      row.actions shouldBe defined
      val actions = row.actions.value.items
      actions should have size 1

      val changeAction       = actions.head
      val expectedChangeText = messages("site.change")
      val expectedHref       = routes.SubTradingNameYesNoController.onPageLoad(CheckMode).url
      val expectedHiddenText = messages("subTradingNameYesNo.change.hidden")

      changeAction.content.asHtml.toString    should include(expectedChangeText)
      changeAction.href                     shouldBe expectedHref
      changeAction.visuallyHiddenText.value shouldBe expectedHiddenText
    }

    "must return a SummaryListRow with 'Yes' when the answer is true in AmendMode" in {
      val answers = UserAnswers("test-id")
        .set(SubTradingNameYesNoPage, true)
        .success
        .value

      val maybeRow: Option[SummaryListRow] = SubTradingNameYesNoSummary.row(answers, AmendMode)
      maybeRow shouldBe defined

      val row =
        maybeRow.value

      val expectedKeyText = messages("subTradingNameYesNo.checkYourAnswersLabel")
      row.key.content.asHtml.toString should include(expectedKeyText)

      val expectedValue = messages("site.yes")
      row.value.content.asHtml.toString should include(expectedValue)

      row.actions shouldBe defined
      val actions = row.actions.value.items
      actions should have size 1

      val changeAction       = actions.head
      val expectedChangeText = messages("site.change")
      val expectedHref       = routes.SubTradingNameYesNoController.onPageLoad(AmendMode).url
      val expectedHiddenText = messages("subTradingNameYesNo.change.hidden")

      changeAction.content.asHtml.toString    should include(expectedChangeText)
      changeAction.href                     shouldBe expectedHref
      changeAction.visuallyHiddenText.value shouldBe expectedHiddenText
    }

    "must return a SummaryListRow with 'No' when the answer is false" in {
      val answers = UserAnswers("test-id")
        .set(SubTradingNameYesNoPage, false)
        .success
        .value

      val maybeRow: Option[SummaryListRow] = SubTradingNameYesNoSummary.row(answers)
      maybeRow shouldBe defined

      val row           = maybeRow.value
      val expectedValue = messages("site.no")
      row.value.content.asHtml.toString should include(expectedValue)
    }

    "must return None when the answer does not exist" in {
      val answers = UserAnswers("test-id")
      SubTradingNameYesNoSummary.row(answers) shouldBe None
    }
  }

  "ViewOnly - SubTradingNameYesNoSummary.row" - {

    "must return a SummaryListRow with 'Yes' when usesTradingName is true" in {

      val answers =
        ViewOnlyIndividualAnswers(
          subcontractorType = models.TypeOfSubcontractor.Individualorsoletrader,
          showVerificationDetails = false,
          usesTradingName = Some(true),
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

      val maybeRow =
        SubTradingNameYesNoSummary.row(answers)

      maybeRow shouldBe defined

      val row = maybeRow.value

      val expectedKeyText =
        messages("subTradingNameYesNo.checkYourAnswersLabel")

      row.key.content.asHtml.toString should include(expectedKeyText)

      row.value.content.asHtml.toString should include(
        messages("site.yes")
      )

      row.actions             shouldBe defined
      row.actions.value.items shouldBe empty
    }

    "must return a SummaryListRow with 'No' when usesTradingName is false" in {

      val answers =
        ViewOnlyIndividualAnswers(
          subcontractorType = models.TypeOfSubcontractor.Individualorsoletrader,
          showVerificationDetails = false,
          usesTradingName = Some(false),
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

      val maybeRow =
        SubTradingNameYesNoSummary.row(answers)

      maybeRow shouldBe defined

      val row = maybeRow.value

      row.value.content.asHtml.toString should include(
        messages("site.no")
      )

      row.actions             shouldBe defined
      row.actions.value.items shouldBe empty
    }

    "must return None when usesTradingName is not set" in {

      val answers =
        ViewOnlyIndividualAnswers(
          subcontractorType = models.TypeOfSubcontractor.Individualorsoletrader,
          showVerificationDetails = false,
          usesTradingName = None,
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

      SubTradingNameYesNoSummary.row(answers) shouldBe None
    }
  }
}
