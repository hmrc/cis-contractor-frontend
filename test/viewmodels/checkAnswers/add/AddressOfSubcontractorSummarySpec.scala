/*
 * Copyright 2025 HM Revenue & Customs
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

import models.{CheckMode, UserAnswers}
import models.add.UKAddress
import org.scalatest.OptionValues.convertOptionToValuable
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import pages.add.AddressOfSubcontractorPage
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent

class AddressOfSubcontractorSummarySpec extends AnyWordSpec with Matchers {

  implicit val messages: Messages = stubMessages()

  "AddressOfSubcontractorSummary.row" should {

    "return a SummaryListRow when AddressOfSubcontractorPage has an answer" in {

      val address = UKAddress(
        addressLine1 = "10 Downing Street",
        addressLine2 = Some("Westminster"),
        addressLine3 = "London",
        addressLine4 = Some("Greater London"),
        postCode = "SW1A 2AA"
      )

      val userAnswers =
        UserAnswers("id").set(AddressOfSubcontractorPage, address).success.value

      val result = AddressOfSubcontractorSummary.row(userAnswers)

      result mustBe defined

      val row = result.value

      row.key.content.asHtml.toString must include(
        messages("addressOfSubcontractor.checkYourAnswersLabel")
      )

      row.value.content mustBe HtmlContent(
        "10 Downing Street<br/>" +
          "Westminster<br/>" +
          "London<br/>" +
          "Greater London<br/>" +
          "SW1A 2AA"
      )

      val action = row.actions.value.items.head

      action.href mustBe
        controllers.add.routes.SubAddressYesNoController
          .onPageLoad(CheckMode)
          .url

      action.visuallyHiddenText.value mustBe
        messages("addressOfSubcontractor.change.hidden")
    }

    "return None when AddressOfSubcontractorPage has no answer" in {

      val userAnswers = UserAnswers("id")

      AddressOfSubcontractorSummary.row(userAnswers) mustBe None
    }
  }
}
