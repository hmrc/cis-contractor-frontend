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

package viewmodels.checkAnswers.add.company

import models.amend.company.AmendCompanyRemoveDetail
import models.{AmendMode, CheckMode, UserAnswers}
import org.scalatest.OptionValues.convertOptionToValuable
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers.must
import org.scalatest.matchers.should.Matchers
import pages.add.company.CompanyUtrYesNoPage
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.*
import models.TypeOfSubcontractor
import models.viewOnly.company.ViewOnlyCompanyAnswers

class CompanyUtrYesNoSummarySpec extends AnyFreeSpec with Matchers {

  implicit val messages: Messages = stubMessages()

  "CompanyUtrYesNoSummary.row" - {

    "must return a SummaryListRow with 'Yes' when the answer is true" in {

      val answers = UserAnswers("test-id")
        .set(CompanyUtrYesNoPage, true)
        .success
        .value

      val maybeRow: Option[SummaryListRow] =
        CompanyUtrYesNoSummary.row(answers)

      maybeRow shouldBe defined

      val row = maybeRow.value

      val expectedKeyText =
        messages("companyUtrYesNo.checkYourAnswersLabel")
      row.key.content.asHtml.toString should include(expectedKeyText)

      val expectedValue = messages("site.yes")
      row.value.content.asHtml.toString should include(expectedValue)

      row.actions shouldBe defined
      val actions = row.actions.value.items
      actions should have size 1

      val changeAction       = actions.head
      val expectedChangeText = messages("site.change")
      val expectedHref       =
        controllers.add.company.routes.CompanyUtrYesNoController
          .onPageLoad(CheckMode)
          .url
      val expectedHiddenText =
        messages("companyUtrYesNo.change.hidden")

      changeAction.content.asHtml.toString    should include(expectedChangeText)
      changeAction.href                     shouldBe expectedHref
      changeAction.visuallyHiddenText.value shouldBe expectedHiddenText
      changeAction.attributes                   must contain("id" -> "add-company-utr")
    }

    "must return a SummaryListRow with 'Yes' when the answer is true in Amend journey" in {

      val answers = UserAnswers("test-id")
        .set(CompanyUtrYesNoPage, true)
        .success
        .value

      val maybeRow: Option[SummaryListRow] =
        CompanyUtrYesNoSummary.row(answers, AmendMode)

      maybeRow shouldBe defined

      val row = maybeRow.value

      val expectedKeyText =
        messages("companyUtrYesNo.checkYourAnswersLabel")
      row.key.content.asHtml.toString should include(expectedKeyText)

      val expectedValue = messages("site.yes")
      row.value.content.asHtml.toString should include(expectedValue)

      row.actions shouldBe defined
      val actions = row.actions.value.items
      actions should have size 1

      val changeAction       = actions.head
      val expectedChangeText = messages("site.change")
      val expectedHref       =
        controllers.amend.company.routes.AmendCompanyRemoveDetailYesNoController
          .onPageLoad(AmendCompanyRemoveDetail.Utr.key)
          .url
      val expectedHiddenText =
        messages("companyUtrYesNo.change.hidden")

      changeAction.content.asHtml.toString    should include(expectedChangeText)
      changeAction.href                     shouldBe expectedHref
      changeAction.visuallyHiddenText.value shouldBe expectedHiddenText
      changeAction.attributes                   must contain("id" -> "add-company-utr")
    }

    "must return a SummaryListRow with 'No' when the answer is false" in {

      val answers = UserAnswers("test-id")
        .set(CompanyUtrYesNoPage, false)
        .success
        .value

      val maybeRow =
        CompanyUtrYesNoSummary.row(answers)

      maybeRow shouldBe defined

      val row           = maybeRow.value
      val expectedValue = messages("site.no")

      row.value.content.asHtml.toString should include(expectedValue)
    }

    "must return None when the answer does not exist" in {

      val answers = UserAnswers("test-id")

      CompanyUtrYesNoSummary.row(answers) shouldBe None
    }
  }

  "CompanyUtrYesNoSummary.row with ViewOnlyCompanyAnswers" - {

    def viewOnlyAnswers(
      utrYesNo: Option[Boolean] = None
    ): ViewOnlyCompanyAnswers =
      ViewOnlyCompanyAnswers(
        subcontractorType = TypeOfSubcontractor.Limitedcompany,
        showVerificationDetails = false,
        companyName = None,
        addressYesNo = None,
        address = None,
        companyContactMethodsYesNo = None,
        companyContactMethod = Set.empty,
        email = None,
        phone = None,
        mobile = None,
        crnYesNo = None,
        crn = None,
        utrYesNo = utrYesNo,
        utr = None,
        worksReferenceYesNo = None,
        worksReference = None,
        verificationNumber = None
      )

    "must return a SummaryListRow with 'Yes' when the answer is true" in {

      val answers = viewOnlyAnswers(Some(true))

      val maybeRow = CompanyUtrYesNoSummary.row(answers)

      maybeRow shouldBe defined

      val row = maybeRow.value

      val expectedKeyText =
        messages("companyUtrYesNo.checkYourAnswersLabel")

      row.key.content.asHtml.toString should include(expectedKeyText)

      val expectedValue = messages("site.yes")
      row.value.content.asHtml.toString should include(expectedValue)

      row.actions             shouldBe defined
      row.actions.value.items shouldBe empty
    }

    "must return a SummaryListRow with 'No' when the answer is false" in {

      val answers = viewOnlyAnswers(Some(false))

      val maybeRow = CompanyUtrYesNoSummary.row(answers)

      maybeRow shouldBe defined

      val row = maybeRow.value

      val expectedKeyText =
        messages("companyUtrYesNo.checkYourAnswersLabel")

      row.key.content.asHtml.toString should include(expectedKeyText)

      val expectedValue = messages("site.no")
      row.value.content.asHtml.toString should include(expectedValue)

      row.actions             shouldBe defined
      row.actions.value.items shouldBe empty
    }

    "must return None when the answer does not exist" in {

      val answers = viewOnlyAnswers()

      CompanyUtrYesNoSummary.row(answers) shouldBe None
    }
  }
}
