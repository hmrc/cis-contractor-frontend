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
import models.{AmendMode, CheckMode, UserAnswers}
import org.scalatest.OptionValues
import org.scalatest.TryValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import pages.add.partnership.PartnershipNominatedPartnerNinoPage
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.*
import org.scalatest.matchers.must.Matchers.must
import models.TypeOfSubcontractor
import models.viewOnly.partnership.ViewOnlyPartnershipAnswers

class PartnershipNominatedPartnerNinoSummarySpec extends AnyFreeSpec with Matchers with OptionValues with TryValues {

  implicit val messages: Messages = stubMessages()

  "PartnershipNominatedPartnerNinoSummary.row" - {

    "must return a SummaryListRow when the answer exists" in {
      val ua =
        UserAnswers("test-id")
          .set(PartnershipNominatedPartnerNinoPage, "QQ123456C")
          .success
          .value

      val row = PartnershipNominatedPartnerNinoSummary.row(ua).value

      row.key mustBe Key(Text(messages("partnershipNominatedPartnerNino.checkYourAnswersLabel")))
      row.value mustBe Value(Text("QQ123456C"))

      row.actions.value.items must have size 1
      val action = row.actions.value.items.head

      action.href mustBe routes.PartnershipNominatedPartnerNinoController.onPageLoad(CheckMode).url
      action.content mustBe Text(messages("site.change"))
      action.visuallyHiddenText mustBe Some(messages("partnershipNominatedPartnerNino.change.hidden"))
      action.attributes must contain("id" -> "nominated-partner-nino")
    }

    "must return a SummaryListRow when the answer exists for Amend journey" in {
      val ua =
        UserAnswers("test-id")
          .set(PartnershipNominatedPartnerNinoPage, "QQ123456C")
          .success
          .value

      val row = PartnershipNominatedPartnerNinoSummary.row(ua, AmendMode).value

      row.key mustBe Key(Text(messages("partnershipNominatedPartnerNino.checkYourAnswersLabel")))
      row.value mustBe Value(Text("QQ123456C"))

      row.actions.value.items must have size 1
      val action = row.actions.value.items.head

      action.href mustBe routes.PartnershipNominatedPartnerNinoController.onPageLoad(AmendMode).url
      action.content mustBe Text(messages("site.change"))
      action.visuallyHiddenText mustBe Some(messages("partnershipNominatedPartnerNino.change.hidden"))
      action.attributes must contain("id" -> "nominated-partner-nino")
    }

    "must return None when the answer does not exist" in {
      val ua = UserAnswers("test-id")
      PartnershipNominatedPartnerNinoSummary.row(ua) mustBe None
    }
  }

  "PartnershipNominatedPartnerNinoSummary.row(ViewOnlyPartnershipAnswers)" - {

    def viewOnlyAnswers(
      nominatedPartnerNino: Option[String]
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
        phone = None,
        mobile = None,
        hasUtrYesNo = None,
        utr = None,
        nominatedPartnerName = None,
        nominatedPartnerUtrYesNo = None,
        nominatedPartnerUtr = None,
        nominatedPartnerNinoYesNo = None,
        nominatedPartnerNino = nominatedPartnerNino,
        nominatedPartnerCrnYesNo = None,
        nominatedPartnerCrn = None,
        nominatedPartnerWorksReferenceYesNo = None,
        nominatedPartnerWorksReference = None,
        verificationNumber = None
      )

    "must return a SummaryListRow when the nominated partner NINO exists" in {

      val answers = viewOnlyAnswers(Some("QQ123456C"))

      val maybeRow =
        PartnershipNominatedPartnerNinoSummary.row(answers)

      maybeRow mustBe defined

      val row = maybeRow.value

      row.key.content.asHtml.toString must include(
        messages("partnershipNominatedPartnerNino.checkYourAnswersLabel")
      )

      row.value.content.asHtml.toString must include("QQ123456C")

      row.actions mustBe defined
      row.actions.value.items mustBe empty
    }

    "must return None when the nominated partner NINO does not exist" in {

      val answers = viewOnlyAnswers(None)

      PartnershipNominatedPartnerNinoSummary.row(answers) mustBe None
    }
  }
}
