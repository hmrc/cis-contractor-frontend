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
import helpers.CyaEncodingSpecHelper
import models.{AmendMode, CheckMode, UserAnswers}
import org.scalatest.OptionValues.convertOptionToValuable
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import pages.add.partnership.PartnershipEmailAddressPage
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.*
import org.scalatest.matchers.must.Matchers.must
import models.TypeOfSubcontractor
import models.info.partnership.PartnershipAnswers

class PartnershipEmailAddressSummarySpec extends AnyFreeSpec with Matchers with CyaEncodingSpecHelper {

  implicit val messages: Messages = stubMessages()

  "PartnershipEmailAddressSummary.row" - {

    "must return a SummaryListRow when the answer exists" in {

      val answers =
        UserAnswers("test-id")
          .set(PartnershipEmailAddressPage, "ABC123456")
          .success
          .value

      val maybeRow =
        PartnershipEmailAddressSummary.row(answers)

      maybeRow shouldBe defined

      val row = maybeRow.value

      val expectedKeyText =
        messages("partnershipEmailAddress.checkYourAnswersLabel")

      row.key.content.asHtml.toString should include(expectedKeyText)

      row.value.content.asHtml.toString should include("ABC123456")

      row.actions shouldBe defined
      val actions = row.actions.value.items
      actions should have size 1

      val changeAction       = actions.head
      val expectedChangeText = messages("site.change")
      val expectedHref       =
        routes.PartnershipEmailAddressController
          .onPageLoad(CheckMode)
          .url
      val expectedHiddenText =
        messages("partnershipEmailAddress.change.hidden")

      changeAction.content.asHtml.toString    should include(expectedChangeText)
      changeAction.href                     shouldBe expectedHref
      changeAction.visuallyHiddenText.value shouldBe expectedHiddenText
      changeAction.attributes                   must contain("id" -> "partnership-email-address")
    }

    "must return a SummaryListRow when the answer exists for Amend journey" in {

      val answers =
        UserAnswers("test-id")
          .set(PartnershipEmailAddressPage, "ABC123456")
          .success
          .value

      val maybeRow =
        PartnershipEmailAddressSummary.row(answers, AmendMode)

      maybeRow shouldBe defined

      val row = maybeRow.value

      val expectedKeyText =
        messages("partnershipEmailAddress.checkYourAnswersLabel")

      row.key.content.asHtml.toString should include(expectedKeyText)

      row.value.content.asHtml.toString should include("ABC123456")

      row.actions shouldBe defined
      val actions = row.actions.value.items
      actions should have size 1

      val changeAction       = actions.head
      val expectedChangeText = messages("site.change")
      val expectedHref       =
        routes.PartnershipEmailAddressController
          .onPageLoad(AmendMode)
          .url
      val expectedHiddenText =
        messages("partnershipEmailAddress.change.hidden")

      changeAction.content.asHtml.toString    should include(expectedChangeText)
      changeAction.href                     shouldBe expectedHref
      changeAction.visuallyHiddenText.value shouldBe expectedHiddenText
      changeAction.attributes                   must contain("id" -> "partnership-email-address")
    }

    "must return None when the answer does not exist" in {
      val answers = UserAnswers("test-id")
      PartnershipEmailAddressSummary.row(answers) shouldBe None
    }
    "must HTML-escape special characters correctly (single encoding only)" in {

      val email = "partners+o'reilly&co@test.com"

      val answers =
        UserAnswers("id")
          .set(PartnershipEmailAddressPage, email)
          .success
          .value

      val row = PartnershipEmailAddressSummary.row(answers).value

      val html = extractHtml(row)

      assertEscaped(html, "partners+o&#x27;reilly&amp;co@test.com")
      assertNoDoubleEncoding(html)
    }
  }

  "PartnershipEmailAddressSummary.row(ViewOnlyPartnershipAnswers)" - {

    "must return a SummaryListRow when the email exists with no actions" in {

      val answers = PartnershipAnswers(
        subcontractorType = TypeOfSubcontractor.Partnership,
        showVerificationDetails = false,
        partnershipName = None,
        addressYesNo = None,
        address = None,
        partnershipContactMethodsYesNo = None,
        partnershipContactMethodOptions = Set.empty,
        email = Some("partners@test.com"),
        phone = None,
        mobile = None,
        hasUtrYesNo = None,
        utr = None,
        nominatedPartnerName = None,
        nominatedPartnerUtrYesNo = None,
        nominatedPartnerUtr = None,
        nominatedPartnerNinoYesNo = None,
        nominatedPartnerNino = None,
        nominatedPartnerCrnYesNo = None,
        nominatedPartnerCrn = None,
        nominatedPartnerWorksReferenceYesNo = None,
        nominatedPartnerWorksReference = None,
        verificationNumber = None
      )

      val maybeRow = PartnershipEmailAddressSummary.row(answers)

      maybeRow shouldBe defined

      val row = maybeRow.value

      row.key.content.asHtml.toString should include(
        messages("partnershipEmailAddress.checkYourAnswersLabel")
      )

      row.value.content.asHtml.toString should include("partners@test.com")

      row.actions             shouldBe defined
      row.actions.value.items shouldBe empty
    }

    "must return None when the email does not exist" in {

      val answers = PartnershipAnswers(
        subcontractorType = TypeOfSubcontractor.Partnership,
        showVerificationDetails = false,
        partnershipName = None,
        addressYesNo = None,
        address = None,
        partnershipContactMethodsYesNo = None,
        partnershipContactMethodOptions = Set.empty,
        email = None,
        phone = None,
        mobile = None,
        hasUtrYesNo = None,
        utr = None,
        nominatedPartnerName = None,
        nominatedPartnerUtrYesNo = None,
        nominatedPartnerUtr = None,
        nominatedPartnerNinoYesNo = None,
        nominatedPartnerNino = None,
        nominatedPartnerCrnYesNo = None,
        nominatedPartnerCrn = None,
        nominatedPartnerWorksReferenceYesNo = None,
        nominatedPartnerWorksReference = None,
        verificationNumber = None
      )

      PartnershipEmailAddressSummary.row(answers) shouldBe None
    }

    "must HTML-escape special characters correctly in ViewOnly row" in {

      val email = "partners+o'reilly&co@test.com"

      val answers = PartnershipAnswers(
        subcontractorType = TypeOfSubcontractor.Partnership,
        showVerificationDetails = false,
        partnershipName = None,
        addressYesNo = None,
        address = None,
        partnershipContactMethodsYesNo = None,
        partnershipContactMethodOptions = Set.empty,
        email = Some(email),
        phone = None,
        mobile = None,
        hasUtrYesNo = None,
        utr = None,
        nominatedPartnerName = None,
        nominatedPartnerUtrYesNo = None,
        nominatedPartnerUtr = None,
        nominatedPartnerNinoYesNo = None,
        nominatedPartnerNino = None,
        nominatedPartnerCrnYesNo = None,
        nominatedPartnerCrn = None,
        nominatedPartnerWorksReferenceYesNo = None,
        nominatedPartnerWorksReference = None,
        verificationNumber = None
      )

      val row = PartnershipEmailAddressSummary.row(answers).value

      val html = extractHtml(row)

      assertEscaped(html, "partners+o&#x27;reilly&amp;co@test.com")
      assertNoDoubleEncoding(html)

      row.actions.value.items shouldBe empty
    }
  }
}
