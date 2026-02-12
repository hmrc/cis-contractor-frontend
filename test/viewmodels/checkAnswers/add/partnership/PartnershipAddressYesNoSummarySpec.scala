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

import base.SpecBase
import models.{CheckMode, UserAnswers}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import pages.add.partnership.PartnershipAddressYesNoPage
import play.api.i18n.{Lang, Messages, MessagesImpl}
import play.api.test.Helpers.*
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.*

class PartnershipAddressYesNoSummarySpec extends SpecBase with GuiceOneAppPerSuite {

  private val messagesApi                 = stubMessagesApi()
  private implicit val messages: Messages = MessagesImpl(Lang.defaultLang, messagesApi)

  "PartnershipAddressYesNoSummary.row" - {

    "return a row with key, value = yes, and change action when the answer is true" in {
      val ua: UserAnswers =
        emptyUserAnswers
          .set(PartnershipAddressYesNoPage, true)
          .success
          .value

      val maybeRow = PartnershipAddressYesNoSummary.row(ua)
      maybeRow must not be empty

      val row: SummaryListRow = maybeRow.value

      row.key mustBe Key(content = Text(messages("partnershipAddressYesNo.checkYourAnswersLabel")))
      row.value mustBe Value(content = Text(messages("site.yes")))

      row.actions must not be empty
      val actions: Actions = row.actions.value
      actions.items must have size 1

      val action: ActionItem = actions.items.head
      action.href mustBe controllers.add.partnership.routes.PartnershipAddressYesNoController
        .onPageLoad(CheckMode)
        .url
      action.content mustBe Text(messages("site.change"))
      action.visuallyHiddenText mustBe Some(messages("partnershipAddressYesNo.change.hidden"))
    }

    "return a row with key, value = no, and change action when the answer is false" in {
      val ua: UserAnswers =
        emptyUserAnswers
          .set(PartnershipAddressYesNoPage, false)
          .success
          .value

      val maybeRow = PartnershipAddressYesNoSummary.row(ua)
      maybeRow must not be empty

      val row: SummaryListRow = maybeRow.value

      row.key mustBe Key(content = Text(messages("partnershipAddressYesNo.checkYourAnswersLabel")))
      row.value mustBe Value(content = Text(messages("site.no")))

      row.actions must not be empty
      val actions: Actions = row.actions.value
      actions.items must have size 1

      val action: ActionItem = actions.items.head
      action.href mustBe controllers.add.partnership.routes.PartnershipAddressYesNoController
        .onPageLoad(CheckMode)
        .url
      action.content mustBe Text(messages("site.change"))
      action.visuallyHiddenText mustBe Some(messages("partnershipAddressYesNo.change.hidden"))
    }

    "return None when the answer is missing" in {
      val ua: UserAnswers = emptyUserAnswers
      PartnershipAddressYesNoSummary.row(ua) mustBe None
    }
  }
}
