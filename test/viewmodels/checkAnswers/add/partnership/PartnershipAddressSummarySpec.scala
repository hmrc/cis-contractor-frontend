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

import helpers.CyaEncodingSpecHelper
import controllers.add.partnership.routes
import models.{AmendMode, UserAnswers}
import models.address.{Address, Country}
import org.scalatest.OptionValues.convertOptionToValuable
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import pages.add.partnership.PartnershipAddressPage
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent

class PartnershipAddressSummarySpec extends AnyWordSpec with Matchers with CyaEncodingSpecHelper {

  implicit val messages: Messages = stubMessages()

  "PartnershipAddressSummary.row" should {

    "return a SummaryListRow when PartnershipAddressPage has an answer" in {

      val address = Address(
        addressLine1 = "10 Downing Street",
        addressLine2 = Some("Westminster"),
        addressLine3 = Some("London"),
        addressLine4 = Some("Greater London"),
        postcode = Some("SW1A 2AA"),
        country = Some(Country(Some("GB"), Some("United Kingdom")))
      )

      val userAnswers =
        UserAnswers("id")
          .set(PartnershipAddressPage, address)
          .success
          .value

      val result = PartnershipAddressSummary.row(userAnswers)

      result shouldBe defined

      val row = result.value

      row.key.content.asHtml.toString should include(
        messages("partnershipAddress.checkYourAnswersLabel")
      )

      row.value.content shouldBe HtmlContent(
        "10 Downing Street<br/>" +
          "Westminster<br/>" +
          "London<br/>" +
          "Greater London<br/>" +
          "SW1A 2AA<br/>" +
          "United Kingdom"
      )

      val action = row.actions.value.items.head

      action.href shouldBe
        routes.PartnershipAddressController
          .redirectToAddressLookup(Some("change"))
          .url

      action.visuallyHiddenText.value shouldBe
        messages("partnershipAddress.change.hidden")

      action.attributes should contain("id" -> "address-of-partnership")
    }

    "return a SummaryListRow when PartnershipAddressPage has an answer in Amend journey" in {

      val address = Address(
        addressLine1 = "10 Downing Street",
        addressLine2 = Some("Westminster"),
        addressLine3 = Some("London"),
        addressLine4 = Some("Greater London"),
        postcode = Some("SW1A 2AA"),
        country = Some(Country(Some("GB"), Some("United Kingdom")))
      )

      val userAnswers =
        UserAnswers("id")
          .set(PartnershipAddressPage, address)
          .success
          .value

      val result = PartnershipAddressSummary.row(userAnswers, AmendMode)

      result shouldBe defined

      val row = result.value

      row.key.content.asHtml.toString should include(
        messages("partnershipAddress.checkYourAnswersLabel")
      )

      row.value.content shouldBe HtmlContent(
        "10 Downing Street<br/>" +
          "Westminster<br/>" +
          "London<br/>" +
          "Greater London<br/>" +
          "SW1A 2AA<br/>" +
          "United Kingdom"
      )

      val action = row.actions.value.items.head

      action.href shouldBe
        routes.PartnershipAddressController
          .redirectToAmendAddressLookup()
          .url

      action.visuallyHiddenText.value shouldBe
        messages("partnershipAddress.change.hidden")

      action.attributes should contain("id" -> "address-of-partnership")
    }

    "return None when PartnershipAddressPage has no answer" in {

      val userAnswers = UserAnswers("id")

      PartnershipAddressSummary.row(userAnswers) shouldBe None
    }

    "must render address safely without double encoding and preserve line breaks" in {

      val address = Address(
        addressLine1 = "10 O'Reilly & Co",
        addressLine2 = Some("Building & Sons"),
        addressLine3 = Some("Main Street"),
        addressLine4 = Some("London"),
        postcode = Some("AB1 2CD"),
        country = Some(Country(Some("GB"), Some("UK")))
      )

      val answers =
        UserAnswers("id")
          .set(PartnershipAddressPage, address)
          .success
          .value

      val row = PartnershipAddressSummary.row(answers).value

      val html = extractHtml(row)

      assertRaw(html, "10 O&#x27;Reilly &amp; Co")
      assertRaw(html, "Building &amp; Sons")

      assertHasBreaks(html)

      assertNoDoubleEncoding(html)
    }
  }
}
