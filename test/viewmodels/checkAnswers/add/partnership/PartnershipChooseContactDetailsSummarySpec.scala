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
import models.add.PartnershipChooseContactDetails
import models.{CheckMode, UserAnswers}
import org.scalatest.OptionValues.convertOptionToValuable
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import pages.add.partnership.PartnershipChooseContactDetailsPage
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.*

class PartnershipChooseContactDetailsSummarySpec extends AnyFreeSpec with Matchers {

  implicit val messages: Messages = stubMessages()

  "PartnershipChooseContactDetailsSummary.row" - {

    "must return a SummaryListRow when Email is selected" in {
      val answers = UserAnswers("test-id")
        .set(PartnershipChooseContactDetailsPage, PartnershipChooseContactDetails.Email)
        .success
        .value

      val maybeRow: Option[SummaryListRow] = PartnershipChooseContactDetailsSummary.row(answers)
      maybeRow shouldBe defined

      val row = maybeRow.value

      val expectedKeyText = messages("partnershipChooseContactDetails.checkYourAnswersLabel")
      row.key.content.asHtml.toString should include(expectedKeyText)

      val expectedValue = messages("partnershipChooseContactDetails.email")
      row.value.content.asHtml.toString should include(expectedValue)

      row.actions shouldBe defined
      val actions = row.actions.value.items
      actions should have size 1

      val changeAction       = actions.head
      val expectedChangeText = messages("site.change")
      val expectedHref       = routes.PartnershipChooseContactDetailsController.onPageLoad(CheckMode).url
      val expectedHiddenText = messages("partnershipChooseContactDetails.change.hidden")

      changeAction.content.asHtml.toString    should include(expectedChangeText)
      changeAction.href                     shouldBe expectedHref
      changeAction.visuallyHiddenText.value shouldBe expectedHiddenText
    }

    "must return a SummaryListRow when Phone is selected" in {
      val answers = UserAnswers("test-id")
        .set(PartnershipChooseContactDetailsPage, PartnershipChooseContactDetails.Phone)
        .success
        .value

      val maybeRow: Option[SummaryListRow] = PartnershipChooseContactDetailsSummary.row(answers)
      maybeRow shouldBe defined

      val row           = maybeRow.value
      val expectedValue = messages("partnershipChooseContactDetails.phone")
      row.value.content.asHtml.toString should include(expectedValue)
    }

    "must return a SummaryListRow when Mobile is selected" in {
      val answers = UserAnswers("test-id")
        .set(PartnershipChooseContactDetailsPage, PartnershipChooseContactDetails.Mobile)
        .success
        .value

      val maybeRow: Option[SummaryListRow] = PartnershipChooseContactDetailsSummary.row(answers)
      maybeRow shouldBe defined

      val row           = maybeRow.value
      val expectedValue = messages("partnershipChooseContactDetails.mobile")
      row.value.content.asHtml.toString should include(expectedValue)
    }

    "must return a SummaryListRow when None is selected" in {
      val answers = UserAnswers("test-id")
        .set(PartnershipChooseContactDetailsPage, PartnershipChooseContactDetails.NoDetails)
        .success
        .value

      val maybeRow: Option[SummaryListRow] = PartnershipChooseContactDetailsSummary.row(answers)
      maybeRow shouldBe defined

      val row           = maybeRow.value
      val expectedValue = messages("partnershipChooseContactDetails.noDetails")
      row.value.content.asHtml.toString should include(expectedValue)
    }

    "must return None when the answer does not exist" in {
      val answers = UserAnswers("test-id")
      PartnershipChooseContactDetailsSummary.row(answers) shouldBe None
    }
  }
}
