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
import models.{AmendMode, CheckMode, Mode, UserAnswers}
import pages.add.trust.TrustUtrYesNoPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

object TrustUtrYesNoSummary {

  def row(answers: UserAnswers, mode: Mode = CheckMode)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(TrustUtrYesNoPage).map { answer =>

      val value = if (answer) "site.yes" else "site.no"

      SummaryListRowViewModel(
        key = "trustUtrYesNo.checkYourAnswersLabel",
        value = ValueViewModel(value),
        actions = Seq(
          ActionItemViewModel(
            "site.change",
            if answer && mode == AmendMode then
              controllers.amend.trust.routes.AmendTrustRemoveDetailYesNoController.onPageLoad("utr").url
            else controllers.add.trust.routes.TrustUtrYesNoController.onPageLoad(mode).url
          )
            .withVisuallyHiddenText(messages("trustUtrYesNo.change.hidden"))
            .withAttribute("id" -> "add-trust-utr")
        )
      )
    }

  def row(
    answers: ViewOnlyTrustAnswers
  )(implicit messages: Messages): Option[SummaryListRow] =
    answers.utrYesNo.map { answer =>

      val value = if (answer) "site.yes" else "site.no"

      SummaryListRowViewModel(
        key = "trustUtrYesNo.checkYourAnswersLabel",
        value = ValueViewModel(value)
      )
    }
}
