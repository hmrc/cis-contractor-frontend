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

package viewmodels.checkAnswers.add.trust

import controllers.add.trust.routes
import models.contact.ContactOptions
import models.{CheckMode, UserAnswers}
import org.scalatest.OptionValues.convertOptionToValuable
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import pages.add.trust.TrustContactOptionsPage
import play.api.i18n.{DefaultMessagesApi, Lang, Messages}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.*
import org.scalatest.matchers.must.Matchers.must

class TrustContactOptionsSummarySpec extends AnyFreeSpec with Matchers {

  implicit val messages: Messages = new DefaultMessagesApi(
    Map(
      "en" -> Map(
        "trustContactOptions.checkYourAnswersLabel" -> "Method of contact",
        "trustContactOptions.email"                 -> "Email address",
        "trustContactOptions.phone"                 -> "Phone number",
        "trustContactOptions.mobile"                -> "Mobile number",
        "trustContactOptions.noDetails"             -> "Do not add any contact details for this subcontractor",
        "trustContactOptions.cya.noDetails"         -> "None",
        "trustContactOptions.change.hidden"         -> "method of contact",
        "site.change"                               -> "Change"
      )
    )
  ).preferred(Seq(Lang("en")))

  "TrustContactOptionsSummary.row" - {

    "must return a SummaryListRow when Email is selected" in {
      val answers = UserAnswers("test-id")
        .set(TrustContactOptionsPage, ContactOptions.Email)
        .success
        .value

      val maybeRow: Option[SummaryListRow] = TrustContactOptionsSummary.row(answers)
      maybeRow shouldBe defined

      val row = maybeRow.value

      val expectedKeyText = messages("trustContactOptions.checkYourAnswersLabel")
      row.key.content.asHtml.toString should include(expectedKeyText)

      val expectedValue = "Email address"
      row.value.content.asHtml.toString should include(expectedValue)

      row.actions shouldBe defined
      val actions = row.actions.value.items
      actions should have size 1

      val changeAction       = actions.head
      val expectedChangeText = messages("site.change")
      val expectedHref       = routes.TrustContactOptionsController.onPageLoad(CheckMode).url
      val expectedHiddenText = messages("trustContactOptions.change.hidden")

      changeAction.content.asHtml.toString    should include(expectedChangeText)
      changeAction.href                     shouldBe expectedHref
      changeAction.visuallyHiddenText.value shouldBe expectedHiddenText
      changeAction.attributes                   must contain("id" -> "trust-contact-details")
    }

    "must return a SummaryListRow when Phone is selected" in {
      val answers = UserAnswers("test-id")
        .set(TrustContactOptionsPage, ContactOptions.Phone)
        .success
        .value

      val maybeRow: Option[SummaryListRow] = TrustContactOptionsSummary.row(answers)
      maybeRow shouldBe defined

      val row           = maybeRow.value
      val expectedValue = "Phone number"
      row.value.content.asHtml.toString should include(expectedValue)
    }

    "must return a SummaryListRow when Mobile is selected" in {
      val answers = UserAnswers("test-id")
        .set(TrustContactOptionsPage, ContactOptions.Mobile)
        .success
        .value

      val maybeRow: Option[SummaryListRow] = TrustContactOptionsSummary.row(answers)
      maybeRow shouldBe defined

      val row           = maybeRow.value
      val expectedValue = "Mobile number"
      row.value.content.asHtml.toString should include(expectedValue)
    }

    "must return a SummaryListRow when None is selected" in {
      val answers = UserAnswers("test-id")
        .set(TrustContactOptionsPage, ContactOptions.NoDetails)
        .success
        .value

      val maybeRow: Option[SummaryListRow] = TrustContactOptionsSummary.row(answers)
      maybeRow shouldBe defined

      val row           = maybeRow.value
      val expectedValue = "None"
      row.value.content.asHtml.toString should include(expectedValue)
    }

    "must return None when the answer does not exist" in {
      val answers = UserAnswers("test-id")
      TrustContactOptionsSummary.row(answers) shouldBe None
    }
  }
}
