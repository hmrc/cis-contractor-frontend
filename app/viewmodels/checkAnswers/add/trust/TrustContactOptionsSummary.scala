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

import models.{CheckMode, UserAnswers}
import pages.add.trust.TrustContactOptionsPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import utils.Utils
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

object TrustContactOptionsSummary {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(TrustContactOptionsPage).map { answer =>
      val cyaMsg = Utils.findFirstMessagesValue(Seq(s"trustContactOptions.cya.$answer", s"trustContactOptions.$answer"))
      SummaryListRowViewModel(
        key = "trustContactOptions.checkYourAnswersLabel",
        value = ValueViewModel(cyaMsg),
        actions = Seq(
          ActionItemViewModel(
            "site.change",
            controllers.add.trust.routes.TrustContactOptionsController.onPageLoad(CheckMode).url
          )
            .withVisuallyHiddenText(messages("trustContactOptions.change.hidden"))
            .withAttribute("id" -> "trust-contact-details")
        )
      )
    }
}
