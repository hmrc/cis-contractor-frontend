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

package viewmodels.checkAnswers.verify

import controllers.verify.routes
import models.verify.ContractorEmailConfirmationStored.{CurrentEmail, DifferentEmail, DoNotSend}
import models.{CheckMode, UserAnswers}
import org.scalatest.OptionValues.convertOptionToValuable
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import pages.verify.{ContractorEmailConfirmationNotStoredPage, ContractorEmailConfirmationStoredPage, EmailAddressPage}
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.*

class EmailAddressSummarySpec extends AnyFreeSpec with Matchers {

  implicit val messages: Messages = stubMessages()

  "EmailAddressSummary.row" - {

    "must return a SummaryListRow when DifferentEmail is selected and email is present" in {

      val answers =
        UserAnswers("test-id")
          .set(ContractorEmailConfirmationStoredPage, DifferentEmail)
          .success
          .value
          .set(EmailAddressPage, "test@example.com")
          .success
          .value

      val maybeRow = EmailAddressSummary.row(answers)

      maybeRow shouldBe defined

      val row = maybeRow.value

      val expectedKeyText = messages("verify.emailAddress.checkYourAnswersLabel")
      row.key.content.asHtml.toString should include(expectedKeyText)

      row.value.content.asHtml.toString should include("test@example.com")

      row.actions shouldBe defined
      val actions = row.actions.value.items
      actions should have size 1

      val changeAction       = actions.head
      val expectedChangeText = messages("site.change")
      val expectedHref       = routes.EmailAddressController
        .onPageLoad(CheckMode)
        .url
      val expectedHiddenText = messages("verify.emailAddress.change.hidden")

      changeAction.content.asHtml.toString    should include(expectedChangeText)
      changeAction.href                     shouldBe expectedHref
      changeAction.visuallyHiddenText.value shouldBe expectedHiddenText
    }

    "must return a SummaryListRow when ContractorEmailConfirmationNotStored is true and email is present" in {

      val answers =
        UserAnswers("test-id")
          .set(ContractorEmailConfirmationNotStoredPage, true)
          .success
          .value
          .set(EmailAddressPage, "entered@example.com")
          .success
          .value

      EmailAddressSummary.row(answers) shouldBe defined
    }

    "must return None when CurrentEmail is selected (email shown inline, not as separate row)" in {

      val answers =
        UserAnswers("test-id")
          .set(ContractorEmailConfirmationStoredPage, CurrentEmail)
          .success
          .value
          .set(EmailAddressPage, "stale@example.com")
          .success
          .value

      EmailAddressSummary.row(answers) shouldBe None
    }

    "must return None when DoNotSend is selected" in {

      val answers =
        UserAnswers("test-id")
          .set(ContractorEmailConfirmationStoredPage, DoNotSend)
          .success
          .value
          .set(EmailAddressPage, "stale@example.com")
          .success
          .value

      EmailAddressSummary.row(answers) shouldBe None
    }

    "must return None when ContractorEmailConfirmationNotStored is false" in {

      val answers =
        UserAnswers("test-id")
          .set(ContractorEmailConfirmationNotStoredPage, false)
          .success
          .value

      EmailAddressSummary.row(answers) shouldBe None
    }

    "must return None when no confirmation page answer is present" in {

      EmailAddressSummary.row(UserAnswers("test-id")) shouldBe None
    }
  }
}
