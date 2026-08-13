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
import models.{AmendMode, CheckMode, UserAnswers}
import models.TypeOfSubcontractor
import models.viewOnly.partnership.ViewOnlyPartnershipAnswers
import org.scalatest.OptionValues.convertOptionToValuable
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import pages.add.partnership.PartnershipNominatedPartnerUtrYesNoPage
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.*
import org.scalatest.matchers.must.Matchers.must

class PartnershipNominatedPartnerUtrYesNoSummarySpec extends AnyFreeSpec with Matchers {

  implicit val messages: Messages = stubMessages()

  "PartnershipNominatedPartnerUtrYesNoSummary.row" - {

    "must return a SummaryListRow with 'Yes' when the answer is true" in {
      val answers = UserAnswers("test-id")
        .set(PartnershipNominatedPartnerUtrYesNoPage, true)
        .success
        .value

      val maybeRow: Option[SummaryListRow] =
        PartnershipNominatedPartnerUtrYesNoSummary.row(answers)

      maybeRow shouldBe defined

      val row = maybeRow.value

      val expectedKeyText =
        messages("partnershipNominatedPartnerUtrYesNo.checkYourAnswersLabel")

      row.key.content.asHtml.toString should include(expectedKeyText)

      val expectedValue = messages("site.yes")
      row.value.content.asHtml.toString should include(expectedValue)

      row.actions shouldBe defined
      val actions = row.actions.value.items
      actions should have size 1

      val changeAction       = actions.head
      val expectedChangeText = messages("site.change")
      val expectedHref =
        routes.PartnershipNominatedPartnerUtrYesNoController
          .onPageLoad(CheckMode)
          .url
      val expectedHiddenText =
        messages("partnershipNominatedPartnerUtrYesNo.change.hidden")

      changeAction.content.asHtml.toString should include(expectedChangeText)
      changeAction.href shouldBe expectedHref
      changeAction.visuallyHiddenText.value shouldBe expectedHiddenText
      changeAction.attributes should contain("id" -> "add-nominated-partner-utr")
    }

    "must return a SummaryListRow with 'Yes' when the answer is true for Amend journey" in {
      val answers = UserAnswers("test-id")
        .set(PartnershipNominatedPartnerUtrYesNoPage, true)
        .success
        .value

      val maybeRow =
        PartnershipNominatedPartnerUtrYesNoSummary.row(answers, AmendMode)

      maybeRow shouldBe defined

      val row = maybeRow.value

      val expectedKeyText =
        messages("partnershipNominatedPartnerUtrYesNo.checkYourAnswersLabel")

      row.key.content.asHtml.toString should include(expectedKeyText)

      val expectedValue = messages("site.yes")
      row.value.content.asHtml.toString should include(expectedValue)

      row.actions shouldBe defined
      val actions = row.actions.value.items
      actions should have size 1

      val changeAction       = actions.head
      val expectedChangeText = messages("site.change")
      val expectedHref =
        routes.PartnershipNominatedPartnerUtrYesNoController
          .onPageLoad(AmendMode)
          .url
      val expectedHiddenText =
        messages("partnershipNominatedPartnerUtrYesNo.change.hidden")

      changeAction.content.asHtml.toString should include(expectedChangeText)
      changeAction.href shouldBe expectedHref
      changeAction.visuallyHiddenText.value shouldBe expectedHiddenText
      changeAction.attributes should contain("id" -> "add-nominated-partner-utr")
    }

    "must return a SummaryListRow with 'No' and change action pointing to add flow when the answer is false in AmendMode" in {
      val answers = UserAnswers("test-id")
        .set(PartnershipNominatedPartnerUtrYesNoPage, false)
        .success
        .value

      val maybeRow =
        PartnershipNominatedPartnerUtrYesNoSummary.row(answers, AmendMode)

      maybeRow shouldBe defined

      val row: SummaryListRow = maybeRow.value

      val expectedKeyText =
        messages("partnershipNominatedPartnerUtrYesNo.checkYourAnswersLabel")

      row.key.content.asHtml.toString should include(expectedKeyText)

      val expectedValue = messages("site.no")
      row.value.content.asHtml.toString should include(expectedValue)

      row.actions shouldBe defined
      val actions = row.actions.value.items
      actions should have size 1

      val changeAction       = actions.head
      val expectedHref =
        routes.PartnershipNominatedPartnerUtrYesNoController
          .onPageLoad(AmendMode)
          .url
      val expectedChangeText = messages("site.change")
      val expectedHiddenText =
        messages("partnershipNominatedPartnerUtrYesNo.change.hidden")

      changeAction.content.asHtml.toString should include(expectedChangeText)
      changeAction.href shouldBe expectedHref
      changeAction.visuallyHiddenText.value shouldBe expectedHiddenText
      changeAction.attributes should contain("id" -> "add-nominated-partner-utr")
    }

    "must return a SummaryListRow with 'No' when the answer is false" in {
      val answers = UserAnswers("test-id")
        .set(PartnershipNominatedPartnerUtrYesNoPage, false)
        .success
        .value

      val maybeRow =
        PartnershipNominatedPartnerUtrYesNoSummary.row(answers)

      maybeRow shouldBe defined

      val row = maybeRow.value

      val expectedValue = messages("site.no")
      row.value.content.asHtml.toString should include(expectedValue)
    }

    "must return None when the answer does not exist" in {
      val answers = UserAnswers("test-id")

      PartnershipNominatedPartnerUtrYesNoSummary.row(answers) shouldBe None
    }
  }

  "PartnershipNominatedPartnerUtrYesNoSummary.row(ViewOnlyPartnershipAnswers)" - {

    def viewOnlyAnswers(
                         nominatedPartnerUtrYesNo: Option[Boolean]
                       ): ViewOnlyPartnershipAnswers =
      ViewOnlyPartnershipAnswers(
        subcontractorType = TypeOfSubcontractor.Partnership,
        showVerificationDetails = false,
        partnershipName = None,
        addressYesNo = None,
        address = None,
        partnershipContactMethodsYesNo = None,
        partnershipContactMethodOptions = Set.empty,
        email = None,
        phone = None,
        mobile = None,
        hasUtrYesNo = None,
        utr = None,
        nominatedPartnerName = None,
        nominatedPartnerUtrYesNo = nominatedPartnerUtrYesNo,
        nominatedPartnerUtr = None,
        nominatedPartnerNinoYesNo = None,
        nominatedPartnerNino = None,
        nominatedPartnerCrnYesNo = None,
        nominatedPartnerCrn = None,
        nominatedPartnerWorksReferenceYesNo = None,
        nominatedPartnerWorksReference = None,
        verificationNumber = None
      )

    "must return a SummaryListRow with 'Yes' when the ViewOnly answer is true" in {

      val answers =
        viewOnlyAnswers(Some(true))

      val maybeRow =
        PartnershipNominatedPartnerUtrYesNoSummary.row(answers)

      maybeRow shouldBe defined

      val row = maybeRow.value

      row.key.content.asHtml.toString should include(
        messages("partnershipNominatedPartnerUtrYesNo.checkYourAnswersLabel")
      )

      row.value.content.asHtml.toString should include(
        messages("site.yes")
      )

      row.actions shouldBe defined
      row.actions.value.items shouldBe empty
    }

    "must return a SummaryListRow with 'No' when the ViewOnly answer is false" in {

      val answers =
        viewOnlyAnswers(Some(false))

      val maybeRow =
        PartnershipNominatedPartnerUtrYesNoSummary.row(answers)

      maybeRow shouldBe defined

      val row = maybeRow.value

      row.key.content.asHtml.toString should include(
        messages("partnershipNominatedPartnerUtrYesNo.checkYourAnswersLabel")
      )

      row.value.content.asHtml.toString should include(
        messages("site.no")
      )

      row.actions shouldBe defined
      row.actions.value.items shouldBe empty
    }

    "must return None when the ViewOnly answer does not exist" in {

      val answers =
        viewOnlyAnswers(None)

      PartnershipNominatedPartnerUtrYesNoSummary.row(answers) shouldBe None
    }
  }
}
