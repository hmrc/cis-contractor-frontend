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
import pages.verify.SelectSubcontractorsToReverifyPage
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

object SelectSubcontractorsToReverifySummary {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(SelectSubcontractorsToReverifyPage).filter(_.nonEmpty).map { selected =>

      val valueHtml =
        selected.toSeq
          .sortBy(_.id)
          .map(s => HtmlFormat.escape(s.name).toString)
          .mkString("<br>")

      SummaryListRowViewModel(
        key = messages("verify.selectSubcontractorsToReverify.checkYourAnswersLabel"),
        value = ValueViewModel(HtmlContent(valueHtml)),
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
