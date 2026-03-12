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

package viewmodels.checkAnswers.add

import models.contact.ContactOptions
import models.{CheckMode, UserAnswers}
import org.scalatest.OptionValues.convertOptionToValuable
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import pages.add.IndividualChooseContactDetailsPage
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.*

class IndividualChooseContactDetailsSummarySpec extends AnyFreeSpec with Matchers {

  implicit val messages: Messages = stubMessages()

  "IndividualChooseContactDetailsSummary.row" - {

    "must return a SummaryListRow when EmailAddress is selected" in {
      val answers = UserAnswers("test-id")
        .set(IndividualChooseContactDetailsPage, ContactOptions.Email)
        .success
        .value

      val maybeRow: Option[SummaryListRow] = IndividualChooseContactDetailsSummary.row(answers)
      maybeRow shouldBe defined

      val row = maybeRow.value

      val expectedKeyText = messages("individualChooseContactDetails.checkYourAnswersLabel")
      row.key.content.asHtml.toString should include(expectedKeyText)

      val expectedValue = messages("individualChooseContactDetails.email")
      row.value.content.asHtml.toString should include(expectedValue)

      row.actions shouldBe defined
      val actions = row.actions.value.items
      actions should have size 1

      val changeAction       = actions.head
      val expectedChangeText = messages("site.change")
      val expectedHref       = controllers.add.routes.IndividualChooseContactDetailsController.onPageLoad(CheckMode).url
      val expectedHiddenText = messages("individualChooseContactDetails.change.hidden")

      changeAction.content.asHtml.toString    should include(expectedChangeText)
      changeAction.href                     shouldBe expectedHref
      changeAction.visuallyHiddenText.value shouldBe expectedHiddenText
    }

    "must return a SummaryListRow when PhoneNumber is selected" in {
      val answers = UserAnswers("test-id")
        .set(IndividualChooseContactDetailsPage, ContactOptions.Phone)
        .success
        .value

      val maybeRow: Option[SummaryListRow] = IndividualChooseContactDetailsSummary.row(answers)
      maybeRow shouldBe defined

      val row           = maybeRow.value
      val expectedValue = messages("individualChooseContactDetails.phone")
      row.value.content.asHtml.toString should include(expectedValue)
    }

    "must return a SummaryListRow when MobileNumber is selected" in {
      val answers = UserAnswers("test-id")
        .set(IndividualChooseContactDetailsPage, ContactOptions.Mobile)
        .success
        .value

      val maybeRow: Option[SummaryListRow] = IndividualChooseContactDetailsSummary.row(answers)
      maybeRow shouldBe defined

      val row           = maybeRow.value
      val expectedValue = messages("individualChooseContactDetails.mobile")
      row.value.content.asHtml.toString should include(expectedValue)
    }

    "must return a SummaryListRow when No Option is selected" in {
      val answers = UserAnswers("test-id")
        .set(IndividualChooseContactDetailsPage, ContactOptions.NoDetails)
        .success
        .value

      val maybeRow: Option[SummaryListRow] = IndividualChooseContactDetailsSummary.row(answers)
      maybeRow shouldBe defined

      val row           = maybeRow.value
      val expectedValue = messages("individualChooseContactDetails.noDetails")
      row.value.content.asHtml.toString should include(expectedValue)
    }

    "must return None when the answer does not exist" in {
      val answers = UserAnswers("test-id")
      IndividualChooseContactDetailsSummary.row(answers) shouldBe None
    }
  }
}

