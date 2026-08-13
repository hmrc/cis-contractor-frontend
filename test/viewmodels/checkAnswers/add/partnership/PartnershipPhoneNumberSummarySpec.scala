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
import pages.add.partnership.PartnershipPhoneNumberPage
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.*
import org.scalatest.matchers.must.Matchers.must
import models.TypeOfSubcontractor
import models.viewOnly.partnership.ViewOnlyPartnershipAnswers

class PartnershipPhoneNumberSummarySpec extends AnyFreeSpec with Matchers with CyaEncodingSpecHelper {
  implicit val messages: Messages = stubMessages()

  "PartnershipPhoneNumberSummary.row" - {

    "must return a SummaryListRow when the answer exists" in {
      val answers =
        UserAnswers("test-id")
          .set(PartnershipPhoneNumberPage, "0123456789")
          .success
          .value

      val maybeRow = PartnershipPhoneNumberSummary.row(answers)
      maybeRow shouldBe defined

      val row = maybeRow.value

      val expectedKeyText = messages("partnershipPhoneNumber.checkYourAnswersLabel")
      row.key.content.asHtml.toString should include(expectedKeyText)

      row.value.content.asHtml.toString should include("0123456789")

      row.actions shouldBe defined
      val actions = row.actions.value.items
      actions should have size 1

      val changeAction       = actions.head
      val expectedChangeText = messages("site.change")
      val expectedHref       = routes.PartnershipPhoneNumberController.onPageLoad(CheckMode).url
      val expectedHiddenText = messages("partnershipPhoneNumber.change.hidden")

      changeAction.content.asHtml.toString should include(expectedChangeText)
      changeAction.href                  shouldBe expectedHref

      changeAction.visuallyHiddenText.value shouldBe expectedHiddenText
      changeAction.attributes                   must contain("id" -> "partnership-phone-number")
    }

    "must return a SummaryListRow when the answer exists for Amend journey" in {
      val answers =
        UserAnswers("test-id")
          .set(PartnershipPhoneNumberPage, "0123456789")
          .success
          .value

      val maybeRow = PartnershipPhoneNumberSummary.row(answers, AmendMode)
      maybeRow shouldBe defined

      val row = maybeRow.value

      val expectedKeyText = messages("partnershipPhoneNumber.checkYourAnswersLabel")
      row.key.content.asHtml.toString should include(expectedKeyText)

      row.value.content.asHtml.toString should include("0123456789")

      row.actions shouldBe defined
      val actions = row.actions.value.items
      actions should have size 1

      val changeAction       = actions.head
      val expectedChangeText = messages("site.change")
      val expectedHref       = routes.PartnershipPhoneNumberController.onPageLoad(AmendMode).url
      val expectedHiddenText = messages("partnershipPhoneNumber.change.hidden")

      changeAction.content.asHtml.toString should include(expectedChangeText)
      changeAction.href                  shouldBe expectedHref

      changeAction.visuallyHiddenText.value shouldBe expectedHiddenText
      changeAction.attributes                   must contain("id" -> "partnership-phone-number")
    }

    "must return None when the answer does not exist" in {
      val answers = UserAnswers("test-id")
      PartnershipPhoneNumberSummary.row(answers) shouldBe None
    }

    "must return a SummaryListRow when the answer exists in Amend journey" in {
      val answers =
        UserAnswers("test-id")
          .set(PartnershipPhoneNumberPage, "0123456789")
          .success
          .value

      val maybeRow = PartnershipPhoneNumberSummary.row(answers, AmendMode)
      maybeRow shouldBe defined

      val row = maybeRow.value

      val expectedKeyText = messages("partnershipPhoneNumber.checkYourAnswersLabel")
      row.key.content.asHtml.toString should include(expectedKeyText)

      row.value.content.asHtml.toString should include("0123456789")

      row.actions shouldBe defined
      val actions = row.actions.value.items
      actions should have size 1

      val changeAction       = actions.head
      val expectedChangeText = messages("site.change")
      val expectedHref       = routes.PartnershipPhoneNumberController.onPageLoad(AmendMode).url
      val expectedHiddenText = messages("partnershipPhoneNumber.change.hidden")

      changeAction.content.asHtml.toString should include(expectedChangeText)
      changeAction.href                  shouldBe expectedHref

      changeAction.visuallyHiddenText.value shouldBe expectedHiddenText
      changeAction.attributes                   must contain("id" -> "partnership-phone-number")
    }

    "must HTML-escape special characters correctly (single encoding only)" in {

      val phone = "020 7000 1234 & ext'78"

      val answers =
        UserAnswers("id")
          .set(PartnershipPhoneNumberPage, phone)
          .success
          .value

      val row = PartnershipPhoneNumberSummary.row(answers).value

      val html = extractHtml(row)

      assertEscaped(html, "020 7000 1234 &amp; ext&#x27;78")
      assertNoDoubleEncoding(html)
    }
  }

  "PartnershipPhoneNumberSummary.row(ViewOnlyPartnershipAnswers)" - {

    def viewOnlyAnswers(
                         phone: Option[String]
                       ): ViewOnlyPartnershipAnswers =
      ViewOnlyPartnershipAnswers(
        subcontractorType = TypeOfSubcontractor.Partnership,
        showVerificationDetails = false,
        partnershipName = None,
        addressYesNo = None,
        address = None,
        partnershipContactMethodsYesNo = None,
        partnershipContactMethodOptions = Set.empty,
        email = None,
        phone = phone,
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

    "must return a SummaryListRow when the phone number exists" in {

      val answers =
        viewOnlyAnswers(Some("0123456789"))

      val maybeRow =
        PartnershipPhoneNumberSummary.row(answers)

      maybeRow shouldBe defined

      val row = maybeRow.value

      row.key.content.asHtml.toString should include(
        messages("partnershipPhoneNumber.checkYourAnswersLabel")
      )

      row.value.content.asHtml.toString should include("0123456789")

      row.actions shouldBe defined
      row.actions.value.items shouldBe empty
    }

    "must return None when the phone number does not exist" in {

      val answers =
        viewOnlyAnswers(None)

      PartnershipPhoneNumberSummary.row(answers) shouldBe None
    }

    "must HTML-escape special characters correctly in ViewOnly row" in {

      val phone = "020 7000 1234 & ext'78"

      val answers =
        viewOnlyAnswers(Some(phone))

      val row =
        PartnershipPhoneNumberSummary.row(answers).value

      val html = extractHtml(row)

      assertEscaped(html, "020 7000 1234 &amp; ext&#x27;78")
      assertNoDoubleEncoding(html)

      row.actions.value.items shouldBe empty
    }
  }

}
