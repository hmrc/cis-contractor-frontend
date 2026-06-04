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
import models.verify.ContractorEmailConfirmationStored
import models.{CheckMode, ContractorScheme, UserAnswers}
import models.response.GetNewestVerificationBatchResponse
import org.scalatest.OptionValues.convertOptionToValuable
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import pages.verify.{ContractorEmailConfirmationStoredPage, NewestVerificationBatchResponsePage}
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.*

class ContractorEmailConfirmationStoredSummarySpec extends AnyFreeSpec with Matchers {

  implicit val messages: Messages = stubMessages()

  private def batchResponseWithEmail(email: String): GetNewestVerificationBatchResponse =
    GetNewestVerificationBatchResponse(
      scheme = Some(ContractorScheme(accountsOfficeReference = None, emailAddress = Some(email))),
      subcontractors = Seq.empty,
      verificationBatch = None,
      verifications = Seq.empty,
      submission = None,
      monthlyReturn = None,
      monthlyReturnSubmission = None
    )

  "ContractorEmailConfirmationStoredSummary.row" - {

    "CurrentEmail" - {

      "must show the label with inline email in bold when a stored email is present" in {

        val answers = UserAnswers("test-id")
          .set(ContractorEmailConfirmationStoredPage, ContractorEmailConfirmationStored.CurrentEmail)
          .success
          .value
          .set(NewestVerificationBatchResponsePage, batchResponseWithEmail("agent@example.com"))
          .success
          .value

        val row       = ContractorEmailConfirmationStoredSummary.row(answers).value
        val valueHtml = row.value.content.asHtml.toString

        valueHtml should include(messages("verify.contractorEmailConfirmationStored.currentEmail"))
        valueHtml should include("<strong>")
        valueHtml should include("agent@example.com")
      }

      "must show only the label when no stored email is present" in {

        val answers = UserAnswers("test-id")
          .set(ContractorEmailConfirmationStoredPage, ContractorEmailConfirmationStored.CurrentEmail)
          .success
          .value

        val row       = ContractorEmailConfirmationStoredSummary.row(answers).value
        val valueHtml = row.value.content.asHtml.toString

        valueHtml should include(messages("verify.contractorEmailConfirmationStored.currentEmail"))
        valueHtml should not include "<strong>"
      }
    }

    "must show the correct label and Change link" in {

      val answers = UserAnswers("test-id")
        .set(ContractorEmailConfirmationStoredPage, ContractorEmailConfirmationStored.CurrentEmail)
        .success
        .value

      val row = ContractorEmailConfirmationStoredSummary.row(answers).value

      row.key.content.asHtml.toString should include(
        messages("verify.contractorEmailConfirmationStored.checkYourAnswersLabel")
      )

      row.actions shouldBe defined
      val changeAction = row.actions.value.items.head
      changeAction.content.asHtml.toString    should include(messages("site.change"))
      changeAction.href                     shouldBe routes.ContractorEmailConfirmationStoredController.onPageLoad(CheckMode).url
      changeAction.visuallyHiddenText.value shouldBe messages(
        "verify.contractorEmailConfirmationStored.change.hidden"
      )
    }

    "must return None when the answer does not exist" in {

      val answers = UserAnswers("test-id")
      ContractorEmailConfirmationStoredSummary.row(answers) shouldBe None
    }
  }
}
