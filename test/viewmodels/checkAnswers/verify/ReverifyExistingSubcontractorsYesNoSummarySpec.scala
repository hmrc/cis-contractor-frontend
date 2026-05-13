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
import models.response.GetNewestVerificationBatchResponse
import models.{CheckMode, ContractorScheme, Subcontractor, UserAnswers}
import org.scalatest.OptionValues.convertOptionToValuable
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import pages.verify.{NewestVerificationBatchResponsePage, ReverifyExistingSubcontractorsYesNoPage}
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow

class ReverifyExistingSubcontractorsYesNoSummarySpec extends AnyFreeSpec with Matchers {

  implicit val messages: Messages = stubMessages()

  private val aSubcontractor = Subcontractor(
    subcontractorId = 1L,
    firstName = None,
    secondName = None,
    surname = None,
    tradingName = Some("Brody & Co"),
    partnershipTradingName = None,
    verified = None,
    verificationNumber = None,
    taxTreatment = None,
    verificationDate = None,
    lastMonthlyReturnDate = None,
    createDate = None,
    subcontractorType = None,
    subbieResourceRef = None,
    utr = None,
    partnerUtr = None,
    crn = None,
    nino = None
  )

  private def batchWithSubcontractors(subs: Subcontractor*): GetNewestVerificationBatchResponse =
    GetNewestVerificationBatchResponse(
      scheme = Some(ContractorScheme(accountsOfficeReference = None, emailAddress = None)),
      subcontractors = subs.toSeq,
      verificationBatch = None,
      verifications = Seq.empty,
      submission = None,
      monthlyReturn = None
    )

  private val emptyBatch: GetNewestVerificationBatchResponse =
    batchWithSubcontractors()

  "ReverifyExistingSubcontractorsYesNoSummary.row" - {

    "must return a SummaryListRow with Yes when the answer is true and batch has subcontractors" in {

      val answers =
        UserAnswers("test-id")
          .set(NewestVerificationBatchResponsePage, batchWithSubcontractors(aSubcontractor))
          .success
          .value
          .set(ReverifyExistingSubcontractorsYesNoPage, true)
          .success
          .value

      val maybeRow: Option[SummaryListRow] =
        ReverifyExistingSubcontractorsYesNoSummary.row(answers)

      maybeRow shouldBe defined

      val row = maybeRow.value

      row.key.content.asHtml.toString should include(
        messages("verify.reverifyExistingSubcontractorsYesNo.checkYourAnswersLabel")
      )

      row.value.content.asHtml.toString should include(
        messages("site.yes")
      )

      row.actions shouldBe defined
      val actions = row.actions.value.items
      actions should have size 1

      val changeAction = actions.head

      changeAction.content.asHtml.toString should include(
        messages("site.change")
      )

      changeAction.href shouldBe
        routes.ReverifyExistingSubcontractorsYesNoController
          .onPageLoad(CheckMode)
          .url

      changeAction.visuallyHiddenText.value shouldBe
        messages("verify.reverifyExistingSubcontractorsYesNo.change.hidden")
    }

    "must return a SummaryListRow with No when the answer is false and batch has subcontractors" in {

      val answers =
        UserAnswers("test-id")
          .set(NewestVerificationBatchResponsePage, batchWithSubcontractors(aSubcontractor))
          .success
          .value
          .set(ReverifyExistingSubcontractorsYesNoPage, false)
          .success
          .value

      val row = ReverifyExistingSubcontractorsYesNoSummary.row(answers)
      row                                   shouldBe defined
      row.value.value.content.asHtml.toString should include(messages("site.no"))
    }

    "must return None when the batch has no subcontractors (question was not presented)" in {

      val answers =
        UserAnswers("test-id")
          .set(NewestVerificationBatchResponsePage, emptyBatch)
          .success
          .value
          .set(ReverifyExistingSubcontractorsYesNoPage, true)
          .success
          .value

      ReverifyExistingSubcontractorsYesNoSummary.row(answers) shouldBe None
    }

    "must return None when the batch response is absent" in {

      val answers =
        UserAnswers("test-id")
          .set(ReverifyExistingSubcontractorsYesNoPage, true)
          .success
          .value

      ReverifyExistingSubcontractorsYesNoSummary.row(answers) shouldBe None
    }

    "must return None when the answer does not exist even if batch has subcontractors" in {

      val answers =
        UserAnswers("test-id")
          .set(NewestVerificationBatchResponsePage, batchWithSubcontractors(aSubcontractor))
          .success
          .value

      ReverifyExistingSubcontractorsYesNoSummary.row(answers) shouldBe None
    }
  }
}
