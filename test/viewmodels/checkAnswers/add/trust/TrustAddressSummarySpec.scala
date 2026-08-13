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
import helpers.CyaEncodingSpecHelper
import models.{AmendMode, UserAnswers}
import models.address.{Address, Country}
import models.viewOnly.trust.ViewOnlyTrustAnswers
import org.scalatest.OptionValues.convertOptionToValuable
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import pages.add.trust.TrustAddressPage
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent

class TrustAddressSummarySpec extends AnyWordSpec with Matchers with CyaEncodingSpecHelper {

  implicit val messages: Messages = stubMessages()

  "TrustAddressSummary.row" should {

    "return a SummaryListRow when TrustAddressPage has an answer" in {

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
          .set(TrustAddressPage, address)
          .success
          .value

      val result = TrustAddressSummary.row(userAnswers)

      result shouldBe defined

      val row = result.value

      row.key.content.asHtml.toString should include(
        messages("trustAddress.checkYourAnswersLabel")
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
        routes.TrustAddressController
          .redirectToAddressLookup(Some("change"))
          .url

      action.visuallyHiddenText.value shouldBe
        messages("trustAddress.change.hidden")

      action.attributes should contain("id" -> "address-of-trust")
    }

    "return a SummaryListRow when TrustAddressPage has an answer in AmendMode" in {

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
          .set(TrustAddressPage, address)
          .success
          .value

      val result = TrustAddressSummary.row(userAnswers, AmendMode)

      result shouldBe defined

      val row = result.value

      row.key.content.asHtml.toString should include(
        messages("trustAddress.checkYourAnswersLabel")
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
        routes.TrustAddressController
          .redirectToAmendAddressLookup()
          .url

      action.visuallyHiddenText.value shouldBe
        messages("trustAddress.change.hidden")

      action.attributes should contain("id" -> "address-of-trust")
    }

    "return None when TrustAddressPage has no answer" in {

      val userAnswers = UserAnswers("id")

      TrustAddressSummary.row(userAnswers) shouldBe None
    }

    "must HTML-escape special characters correctly (single encoding only)" in {

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
          .set(TrustAddressPage, address)
          .success
          .value

      val row = TrustAddressSummary.row(answers).value

      val html = extractHtml(row)

      assertRaw(html, "10 O&#x27;Reilly &amp; Co")
      assertRaw(html, "Building &amp; Sons")

      assertHasBreaks(html)

      assertNoDoubleEncoding(html)
    }
  }

  "TrustAddressSummary.row for ViewOnlyTrustAnswers" should {

    "return a SummaryListRow when address exists" in {

      val address = Address(
        addressLine1 = "10 Downing Street",
        addressLine2 = Some("Westminster"),
        addressLine3 = Some("London"),
        addressLine4 = Some("Greater London"),
        postcode = Some("SW1A 2AA"),
        country = Some(Country(Some("GB"), Some("United Kingdom")))
      )

      val answers =
        ViewOnlyTrustAnswers(
          subcontractorType = models.TypeOfSubcontractor.Trust,
          showVerificationDetails = false,
          trustName = Some("Test Trust"),
          addressYesNo = Some(true),
          address = Some(address),
          trustContactMethodsYesNo = None,
          trustContactMethod = Set.empty,
          email = None,
          phone = None,
          mobile = None,
          utrYesNo = None,
          utr = None,
          worksReferenceYesNo = None,
          worksReference = None,
          verificationNumber = None
        )

      val result = TrustAddressSummary.row(answers)

      result shouldBe defined

      val row = result.value

      row.key.content.asHtml.toString should include(
        messages("trustAddress.checkYourAnswersLabel")
      )

      row.value.content shouldBe HtmlContent(
        "10 Downing Street<br/>" +
          "Westminster<br/>" +
          "London<br/>" +
          "Greater London<br/>" +
          "SW1A 2AA<br/>" +
          "United Kingdom"
      )

      row.actions             shouldBe defined
      row.actions.value.items shouldBe empty
    }

    "return None when address is missing in ViewOnlyTrustAnswers" in {

      val answers =
        ViewOnlyTrustAnswers(
          subcontractorType = models.TypeOfSubcontractor.Trust,
          showVerificationDetails = false,
          trustName = Some("Test Trust"),
          addressYesNo = Some(false),
          address = None,
          trustContactMethodsYesNo = None,
          trustContactMethod = Set.empty,
          email = None,
          phone = None,
          mobile = None,
          utrYesNo = None,
          utr = None,
          worksReferenceYesNo = None,
          worksReference = None,
          verificationNumber = None
        )

      TrustAddressSummary.row(answers) shouldBe None
    }

    "render the ViewOnly address safely without double encoding" in {

      val address = Address(
        addressLine1 = "10 O'Reilly & Co",
        addressLine2 = Some("Building & Sons"),
        addressLine3 = Some("Main Street"),
        addressLine4 = Some("London"),
        postcode = Some("AB1 2CD"),
        country = Some(Country(Some("GB"), Some("UK")))
      )

      val answers =
        ViewOnlyTrustAnswers(
          subcontractorType = models.TypeOfSubcontractor.Trust,
          showVerificationDetails = false,
          trustName = Some("Test Trust"),
          addressYesNo = Some(true),
          address = Some(address),
          trustContactMethodsYesNo = None,
          trustContactMethod = Set.empty,
          email = None,
          phone = None,
          mobile = None,
          utrYesNo = None,
          utr = None,
          worksReferenceYesNo = None,
          worksReference = None,
          verificationNumber = None
        )

      val maybeRow = TrustAddressSummary.row(answers)

      maybeRow shouldBe defined

      val row = maybeRow.value

      val html = extractHtml(row)

      assertRaw(html, "10 O&#x27;Reilly &amp; Co")
      assertRaw(html, "Building &amp; Sons")

      assertHasBreaks(html)
      assertNoDoubleEncoding(html)

      row.actions             shouldBe defined
      row.actions.value.items shouldBe empty
    }
  }
}
