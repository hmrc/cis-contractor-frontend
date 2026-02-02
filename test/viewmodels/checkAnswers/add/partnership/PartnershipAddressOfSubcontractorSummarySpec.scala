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

package viewmodels.checkAnswers.add.partnership

import models.add.UKAddress
import models.{CheckMode, UserAnswers}
import org.scalatest.OptionValues.convertOptionToValuable
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import pages.add.partnership.PartnershipAddressOfSubcontractorPage
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent

class PartnershipAddressOfSubcontractorSummarySpec extends AnyWordSpec with Matchers {

  implicit val messages: Messages = stubMessages()

  "PartnershipAddressOfSubcontractorSummary.row" should {

    "return a SummaryListRow when PartnershipAddressOfSubcontractorPage has an answer" in {

      val address = UKAddress(
        addressLine1 = "10 Downing Street",
        addressLine2 = Some("Westminster"),
        addressLine3 = "London",
        addressLine4 = Some("Greater London"),
        postCode = "SW1A 2AA"
      )

      val userAnswers =
        UserAnswers("id").set(PartnershipAddressOfSubcontractorPage, address).success.value

      val result = PartnershipAddressOfSubcontractorSummary.row(userAnswers)

      result mustBe defined

      val row = result.value

      row.key.content.asHtml.toString must include(
        messages("partnershipAddressOfSubcontractor.checkYourAnswersLabel")
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
        controllers.add.routes.AddressOfSubcontractorController
          .onPageLoad(CheckMode)
          .url

      action.visuallyHiddenText.value mustBe
        messages("partnershipAddressOfSubcontractor.change.hidden")
    }

    "return None when PartnershipAddressOfSubcontractorPage has no answer" in {

      val userAnswers = UserAnswers("id")

      PartnershipAddressOfSubcontractorSummary.row(userAnswers) mustBe None
    }
  }
}
