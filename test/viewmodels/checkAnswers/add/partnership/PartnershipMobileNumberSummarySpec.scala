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
import models.{CheckMode, UserAnswers}
import org.scalatest.OptionValues.convertOptionToValuable
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import pages.add.partnership.PartnershipMobileNumberPage
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.*

class PartnershipMobileNumberSummarySpec extends AnyFreeSpec with Matchers {
  implicit val messages: Messages = stubMessages()

  "PartnershipMobileNumberSummary.row" - {

    "must return a Summary List Row when the answer exists" in {
      val answers =
        UserAnswers("test-id")
          .set(PartnershipMobileNumberPage, "0987456231")
          .success
          .value

      val maybeRow = PartnershipMobileNumberSummary.row(answers)
      maybeRow shouldBe defined

      val row = maybeRow.value

      val expectedKeyText = messages("partnershipMobileNumber.checkYourAnswersLabel")
      row.key.content.asHtml.toString should include(expectedKeyText)

      row.value.content.asHtml.toString should include("0987456231")

      row.actions shouldBe defined
      val actions = row.actions.value.items
      actions should have size 1

      val changeAction       = actions.head
      val expectedChangeText = messages("site.change")
      val expectedHref       = routes.PartnershipMobileNumberController.onPageLoad(CheckMode).url
      val expectedHiddenText = messages("partnershipMobileNumber.change.hidden")

      changeAction.content.asHtml.toString should include(expectedChangeText)
      changeAction.href                  shouldBe expectedHref

      changeAction.visuallyHiddenText.value shouldBe expectedHiddenText
    }

    "must return None when the answer does not exist" in {
      val answers = UserAnswers("test-id")
      PartnershipMobileNumberSummary.row(answers) shouldBe None
    }
  }

}