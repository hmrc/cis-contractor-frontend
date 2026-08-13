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
import org.scalatest.OptionValues.convertOptionToValuable
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import pages.add.partnership.PartnershipWorksReferenceNumberYesNoPage
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.*
import org.scalatest.matchers.must.Matchers.must
import models.TypeOfSubcontractor
import models.viewOnly.partnership.ViewOnlyPartnershipAnswers
import helpers.CyaEncodingSpecHelper

class PartnershipWorksReferenceNumberYesNoSummarySpec extends AnyFreeSpec with Matchers {

  implicit val messages: Messages = stubMessages()

  "PartnershipWorksReferenceNumberYesNoSummary.row" - {

    "must return a SummaryListRow with 'Yes' when the answer is true" in {
      val answers = UserAnswers("test-id")
        .set(PartnershipWorksReferenceNumberYesNoPage, true)
        .success
        .value

      val maybeRow: Option[SummaryListRow] = PartnershipWorksReferenceNumberYesNoSummary.row(answers)
      maybeRow shouldBe defined

      val row =
        maybeRow.value

      val expectedKeyText = messages("partnershipWorksReferenceNumberYesNo.checkYourAnswersLabel")
      row.key.content.asHtml.toString should include(expectedKeyText)

      val expectedValue = messages("site.yes")
      row.value.content.asHtml.toString should include(expectedValue)

      row.actions shouldBe defined
      val actions = row.actions.value.items
      actions should have size 1

      val changeAction       = actions.head
      val expectedChangeText = messages("site.change")
      val expectedHref       = routes.PartnershipWorksReferenceNumberYesNoController.onPageLoad(CheckMode).url
      val expectedHiddenText = messages("partnershipWorksReferenceNumberYesNo.change.hidden")

      changeAction.content.asHtml.toString    should include(expectedChangeText)
      changeAction.href                     shouldBe expectedHref
      changeAction.visuallyHiddenText.value shouldBe expectedHiddenText
      changeAction.attributes                   must contain("id" -> "add-partnership-works-reference-number")
    }

    "must return a SummaryListRow with 'Yes' when the answer is true in Amend journey" in {
      val answers = UserAnswers("test-id")
        .set(PartnershipWorksReferenceNumberYesNoPage, true)
        .success
        .value

      val maybeRow: Option[SummaryListRow] = PartnershipWorksReferenceNumberYesNoSummary.row(answers, AmendMode)
      maybeRow shouldBe defined

      val row =
        maybeRow.value

      val expectedKeyText = messages("partnershipWorksReferenceNumberYesNo.checkYourAnswersLabel")
      row.key.content.asHtml.toString should include(expectedKeyText)

      val expectedValue = messages("site.yes")
      row.value.content.asHtml.toString should include(expectedValue)

      row.actions shouldBe defined
      val actions = row.actions.value.items
      actions should have size 1

      val changeAction       = actions.head
      val expectedChangeText = messages("site.change")
      val expectedHref       = routes.PartnershipWorksReferenceNumberYesNoController.onPageLoad(AmendMode).url
      val expectedHiddenText = messages("partnershipWorksReferenceNumberYesNo.change.hidden")

      changeAction.content.asHtml.toString    should include(expectedChangeText)
      changeAction.href                     shouldBe expectedHref
      changeAction.visuallyHiddenText.value shouldBe expectedHiddenText
      changeAction.attributes                   must contain("id" -> "add-partnership-works-reference-number")
    }

    "return a row with key, value = no, and change action pointing to add flow when the answer is false in AmendMode" in {
      val ua = UserAnswers("test-id")
        .set(PartnershipWorksReferenceNumberYesNoPage, false)
        .success
        .value

      val maybeRow: Option[SummaryListRow] = PartnershipWorksReferenceNumberYesNoSummary.row(ua, AmendMode)
      maybeRow shouldBe defined

      val row: SummaryListRow = maybeRow.value

      val expectedKeyText = messages("partnershipWorksReferenceNumberYesNo.checkYourAnswersLabel")
      row.key.content.asHtml.toString should include(expectedKeyText)

      val expectedValue = messages("site.no")
      row.value.content.asHtml.toString should include(expectedValue)

      row.actions shouldBe defined
      val actions = row.actions.value.items
      actions should have size 1

      val changeAction       = actions.head
      val expectedHref       = controllers.add.partnership.routes.PartnershipWorksReferenceNumberYesNoController
        .onPageLoad(AmendMode)
        .url
      val expectedChangeText = messages("site.change")
      val expectedHiddenText = messages("partnershipWorksReferenceNumberYesNo.change.hidden")

      changeAction.content.asHtml.toString    should include(expectedChangeText)
      changeAction.href                     shouldBe expectedHref
      changeAction.visuallyHiddenText.value shouldBe expectedHiddenText
      changeAction.attributes                 should contain("id" -> "add-partnership-works-reference-number")
    }

    "must return a SummaryListRow with 'No' when the answer is false" in {
      val answers = UserAnswers("test-id")
        .set(PartnershipWorksReferenceNumberYesNoPage, false)
        .success
        .value

      val maybeRow: Option[SummaryListRow] = PartnershipWorksReferenceNumberYesNoSummary.row(answers)
      maybeRow shouldBe defined

      val row           = maybeRow.value
      val expectedValue = messages("site.no")
      row.value.content.asHtml.toString should include(expectedValue)
    }

    "must return None when the answer does not exist" in {
      val answers = UserAnswers("test-id")
      PartnershipWorksReferenceNumberYesNoSummary.row(answers) shouldBe None
    }
  }

  "PartnershipWorksReferenceNumberYesNoSummary.row(ViewOnlyPartnershipAnswers)" - {

    def viewOnlyAnswers(
                         nominatedPartnerWorksReferenceYesNo: Option[Boolean]
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
        nominatedPartnerUtrYesNo = None,
        nominatedPartnerUtr = None,
        nominatedPartnerNinoYesNo = None,
        nominatedPartnerNino = None,
        nominatedPartnerCrnYesNo = None,
        nominatedPartnerCrn = None,
        nominatedPartnerWorksReferenceYesNo = nominatedPartnerWorksReferenceYesNo,
        nominatedPartnerWorksReference = None,
        verificationNumber = None
      )

    "must return a SummaryListRow with 'Yes' when the nominated partner works reference answer is true" in {

      val answers =
        viewOnlyAnswers(Some(true))

      val maybeRow =
        PartnershipWorksReferenceNumberYesNoSummary.row(answers)

      maybeRow shouldBe defined

      val row = maybeRow.value

      row.key.content.asHtml.toString should include(
        messages("partnershipWorksReferenceNumberYesNo.checkYourAnswersLabel")
      )

      row.value.content.asHtml.toString should include(
        messages("site.yes")
      )

      row.actions shouldBe defined
      row.actions.value.items shouldBe empty
    }

    "must return a SummaryListRow with 'No' when the nominated partner works reference answer is false" in {

      val answers =
        viewOnlyAnswers(Some(false))

      val maybeRow =
        PartnershipWorksReferenceNumberYesNoSummary.row(answers)

      maybeRow shouldBe defined

      val row = maybeRow.value

      row.key.content.asHtml.toString should include(
        messages("partnershipWorksReferenceNumberYesNo.checkYourAnswersLabel")
      )

      row.value.content.asHtml.toString should include(
        messages("site.no")
      )

      row.actions shouldBe defined
      row.actions.value.items shouldBe empty
    }

    "must return None when the nominated partner works reference answer does not exist" in {

      val answers =
        viewOnlyAnswers(None)

      PartnershipWorksReferenceNumberYesNoSummary.row(answers) shouldBe None
    }
  }
}
