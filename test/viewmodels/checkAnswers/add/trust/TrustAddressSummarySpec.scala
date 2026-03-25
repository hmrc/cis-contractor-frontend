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

import models.add.InternationalAddress
import models.{CheckMode, UserAnswers}
import org.scalatest.OptionValues.convertOptionToValuable
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import pages.add.trust.TrustAddressPage
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent

class TrustAddressSummarySpec extends AnyWordSpec with Matchers {

  implicit val messages: Messages = stubMessages()

  "TrustAddressSummary.row" should {

    "return a SummaryListRow when TrustAddressPage has an answer" in {

      val address = InternationalAddress(
        addressLine1 = "10 Downing Street",
        addressLine2 = Some("Westminster"),
        addressLine3 = "London",
        addressLine4 = Some("Greater London"),
        postalCode = "SW1A 2AA",
        country = "United Kingdom"
      )

      val userAnswers =
        UserAnswers("id").set(TrustAddressPage, address).success.value

      val result = TrustAddressSummary.row(userAnswers)

      result mustBe defined

      val row = result.value

      row.key.content.asHtml.toString must include(
        messages("trustAddress.checkYourAnswersLabel")
      )

      row.value.content mustBe HtmlContent(
        "10 Downing Street<br/>" +
          "Westminster<br/>" +
          "London<br/>" +
          "Greater London<br/>" +
          "SW1A 2AA<br/>" +
          "United Kingdom"
      )

      val action = row.actions.value.items.head

      action.href mustBe
        controllers.add.trust.routes.TrustAddressController
          .onPageLoad(CheckMode)
          .url

      action.visuallyHiddenText.value mustBe
        messages("trustAddress.change.hidden")
    }

    "return None when TrustAddressPage has no answer" in {

      val userAnswers = UserAnswers("id")

      TrustAddressSummary.row(userAnswers) mustBe None
    }
  }
}
