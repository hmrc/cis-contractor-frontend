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
import org.scalatest.OptionValues
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import pages.add.partnership.PartnershipNominatedPartnerCrnPage
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.*

class PartnershipNominatedPartnerCrnSummarySpec extends AnyFreeSpec with Matchers with OptionValues {

  implicit val messages: Messages = stubMessages()

  "PartnershipNominatedPartnerCrnSummary.row" - {

    "must return a SummaryListRow when the answer exists" in {
      val ua =
        UserAnswers("test-id")
          .set(PartnershipNominatedPartnerCrnPage, "AC012345")
          .success
          .value

      val row = PartnershipNominatedPartnerCrnSummary.row(ua).value

      row.key mustBe Key(Text(messages("partnershipNominatedPartnerCrn.checkYourAnswersLabel")))
      row.value mustBe Value(Text("AC012345"))

      row.actions.value.items must have size 1
      val action = row.actions.value.items.head

      action.href mustBe routes.PartnershipNominatedPartnerCrnController.onPageLoad(CheckMode).url
      action.content mustBe Text(messages("site.change"))
      action.visuallyHiddenText mustBe Some(messages("partnershipNominatedPartnerCrn.change.hidden"))
    }

    "must return None when the answer does not exist" in {
      val ua = UserAnswers("test-id")
      PartnershipNominatedPartnerCrnSummary.row(ua) mustBe None
    }
  }
}
