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

package viewmodels.checkAnswers.verify

import models.{CheckMode, UserAnswers}
import pages.verify.{ReverifyExistingSubcontractorsYesNoPage, SelectSubcontractorsToReverifyPage}
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

object SelectSubcontractorsToReverifySummary {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] = {
    val selectEmptyReverify = List(messages("verify.selectSubcontractor.display.noneSelected"))
    answers
      .get(SelectSubcontractorsToReverifyPage)
      .filter(_ => answers.get(ReverifyExistingSubcontractorsYesNoPage).contains(true))
      .flatMap { selected =>
        val selectNames = selected.map(s => HtmlFormat.escape(s.name).toString).toSeq
        val names       = if (selectNames.isEmpty) selectEmptyReverify else selectNames
        ValueViewModelHelper.makeGovukBulletList(names).map { value =>
          SummaryListRowViewModel(
            key = messages("verify.selectSubcontractorsToReverify.checkYourAnswersLabel"),
            value = value,
            actions = Seq(
              ActionItemViewModel(
                content = Text(messages("site.change")),
                href = controllers.verify.routes.SelectSubcontractorsToReverifyController.onPageLoad(CheckMode).url
              ).withVisuallyHiddenText(messages("verify.selectSubcontractorsToReverify.change.hidden"))
                .withAttribute("id", "select-subcontractors-to-reverify")
            )
          )
        }
      }
    /* .getOrElse(
        ValueViewModelHelper.makeGovukBulletList(selectEmptyReverify).map { value =>
          SummaryListRowViewModel(
            key = messages("verify.selectSubcontractorsToReverify.checkYourAnswersLabel"),
            value = value,
            actions = Seq(
              ActionItemViewModel(
                content = Text(messages("site.change")),
                href = controllers.verify.routes.SelectSubcontractorsToReverifyController.onPageLoad(CheckMode).url
              ).withVisuallyHiddenText(messages("verify.selectSubcontractorsToReverify.change.hidden"))
                .withAttribute("id", "select-subcontractors-to-reverify")
            )
          )
        }
      ) */
  }
}
