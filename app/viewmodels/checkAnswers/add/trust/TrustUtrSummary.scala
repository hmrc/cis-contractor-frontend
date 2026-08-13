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

import models.viewOnly.trust.ViewOnlyTrustAnswers
import models.{CheckMode, Mode, UserAnswers}
import pages.add.trust.TrustUtrPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

object TrustUtrSummary {

  def row(answers: UserAnswers, mode: Mode = CheckMode, showActions: Boolean = true)(implicit
    messages: Messages
  ): Option[SummaryListRow] =
    answers.get(TrustUtrPage).map { answer =>
      val value = ValueViewModel(answer)
      if (showActions) {
        val actions = Seq(
          ActionItemViewModel(
            "site.change",
            controllers.add.trust.routes.TrustUtrController.onPageLoad(mode).url
          )
            .withVisuallyHiddenText(messages("trustUtr.change.hidden"))
            .withAttribute("id" -> "trust-utr")
        )
        SummaryListRowViewModel(
          key = "trustUtr.checkYourAnswersLabel",
          value = value,
          actions = actions
        )
      } else {
        SummaryListRowViewModel(
          key = "trustUtr.verified.checkYourAnswersLabel",
          value = value,
          actions = Seq.empty
        )
      }
    }

  def row(
    answers: ViewOnlyTrustAnswers,
    isVerified: Boolean
  )(implicit messages: Messages): Option[SummaryListRow] =
    answers.utr.map { answer =>
      SummaryListRowViewModel(
        key = if (isVerified) {
          "trustUtr.verified.checkYourAnswersLabel"
        } else {
          "trustUtr.checkYourAnswersLabel"
        },
        value = ValueViewModel(answer)
      )
    }
}
