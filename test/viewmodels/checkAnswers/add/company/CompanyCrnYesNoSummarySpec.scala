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

import models.{AmendMode, CheckMode, UserAnswers}
import org.scalatest.OptionValues.convertOptionToValuable
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers.must
import org.scalatest.matchers.should.Matchers
import pages.add.company.CompanyCrnYesNoPage
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.*
import models.TypeOfSubcontractor
import models.viewOnly.company.ViewOnlyCompanyAnswers

class CompanyCrnYesNoSummarySpec extends AnyFreeSpec with Matchers {

  implicit val messages: Messages = stubMessages()

  "CompanyCrnYesNoSummary.row" - {

    "must return a SummaryListRow with 'Yes' when the answer is true" in {
      val answers = UserAnswers("test-id")
        .set(CompanyCrnYesNoPage, true)
        .success
        .value

      val maybeRow: Option[SummaryListRow] = CompanyCrnYesNoSummary.row(answers)
      maybeRow shouldBe defined

      val row =
        maybeRow.value

      val expectedKeyText = messages("companyCrnYesNo.checkYourAnswersLabel")
      row.key.content.asHtml.toString should include(expectedKeyText)

      val expectedValue = messages("site.yes")
      row.value.content.asHtml.toString should include(expectedValue)

      row.actions shouldBe defined
      val actions = row.actions.value.items
      actions should have size 1

      val changeAction       = actions.head
      val expectedChangeText = messages("site.change")
      val expectedHref       = controllers.add.company.routes.CompanyCrnYesNoController.onPageLoad(CheckMode).url
      val expectedHiddenText = messages("companyCrnYesNo.change.hidden")

      changeAction.content.asHtml.toString    should include(expectedChangeText)
      changeAction.href                     shouldBe expectedHref
      changeAction.visuallyHiddenText.value shouldBe expectedHiddenText
      changeAction.attributes                   must contain("id" -> "add-company-crn")
    }

    "must return a SummaryListRow with 'Yes' when the answer is true in Amend Journey" in {
      val answers = UserAnswers("test-id")
        .set(CompanyCrnYesNoPage, true)
        .success
        .value

      val maybeRow: Option[SummaryListRow] = CompanyCrnYesNoSummary.row(answers, AmendMode)
      maybeRow shouldBe defined

      val row =
        maybeRow.value

      val expectedKeyText = messages("companyCrnYesNo.checkYourAnswersLabel")
      row.key.content.asHtml.toString should include(expectedKeyText)

      val expectedValue = messages("site.yes")
      row.value.content.asHtml.toString should include(expectedValue)

      row.actions shouldBe defined
      val actions = row.actions.value.items
      actions should have size 1

      val changeAction       = actions.head
      val expectedChangeText = messages("site.change")
      val expectedHref       = controllers.add.company.routes.CompanyCrnYesNoController.onPageLoad(AmendMode).url
      val expectedHiddenText = messages("companyCrnYesNo.change.hidden")

      changeAction.content.asHtml.toString    should include(expectedChangeText)
      changeAction.href                     shouldBe expectedHref
      changeAction.visuallyHiddenText.value shouldBe expectedHiddenText
      changeAction.attributes                   must contain("id" -> "add-company-crn")
    }

    "must return a SummaryListRow with 'No' when the answer is false" in {
      val answers = UserAnswers("test-id")
        .set(CompanyCrnYesNoPage, false)
        .success
        .value

      val maybeRow: Option[SummaryListRow] = CompanyCrnYesNoSummary.row(answers)
      maybeRow shouldBe defined

      val row           = maybeRow.value
      val expectedValue = messages("site.no")
      row.value.content.asHtml.toString should include(expectedValue)
    }

    "must return None when the answer does not exist" in {
      val answers = UserAnswers("test-id")
      CompanyCrnYesNoSummary.row(answers) shouldBe None
    }
  }

  "CompanyCrnYesNoSummary.row(ViewOnlyCompanyAnswers)" - {

    def viewOnlyAnswers(
      crnYesNo: Option[Boolean]
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
        utrYesNo = None,
        utr = None,
        crnYesNo = crnYesNo,
        crn = None,
        worksReferenceYesNo = None,
        worksReference = None,
        verificationNumber = None
      )

    "must return a SummaryListRow with 'Yes' when crnYesNo is true" in {

      val answers =
        viewOnlyAnswers(Some(true))

      val maybeRow =
        CompanyCrnYesNoSummary.row(answers)

      maybeRow shouldBe defined

      val row = maybeRow.value

      row.key.content.asHtml.toString should include(
        messages("companyCrnYesNo.checkYourAnswersLabel")
      )

      row.value.content.asHtml.toString should include(
        messages("site.yes")
      )

      row.actions             shouldBe defined
      row.actions.value.items shouldBe empty
    }

    "must return a SummaryListRow with 'No' when crnYesNo is false" in {

      val answers =
        viewOnlyAnswers(Some(false))

      val maybeRow =
        CompanyCrnYesNoSummary.row(answers)

      maybeRow shouldBe defined

      val row = maybeRow.value

      row.key.content.asHtml.toString should include(
        messages("companyCrnYesNo.checkYourAnswersLabel")
      )

      row.value.content.asHtml.toString should include(
        messages("site.no")
      )

      row.actions             shouldBe defined
      row.actions.value.items shouldBe empty
    }

    "must return None when crnYesNo does not exist" in {

      val answers =
        viewOnlyAnswers(None)

      CompanyCrnYesNoSummary.row(answers) shouldBe None
    }
  }

}
