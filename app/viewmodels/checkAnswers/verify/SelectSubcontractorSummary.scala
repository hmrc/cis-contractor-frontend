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

import models.{CheckMode, SubcontractorViewModel, UserAnswers}
import pages.verify.SelectSubcontractorPage
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

object SelectSubcontractorSummary {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] = {
    val selectEmptyReverify = List("None selected")
    answers.get(SelectSubcontractorPage).flatMap { answers =>
      val selectNames = answers.toSeq.map(sub => HtmlFormat.escape(sub.name).toString)

      val names = if (selectNames.isEmpty) selectEmptyReverify else selectNames

      ValueViewModelHelper.makeGovukBulletList(names).map { value =>
        SummaryListRowViewModel(
          key = "verify.selectSubcontractor.checkYourAnswersLabel",
          value = value,
          actions = Seq(
            ActionItemViewModel(
              "site.change",
              controllers.verify.routes.SelectSubcontractorController.onPageLoad(CheckMode).url
            )
              .withVisuallyHiddenText(messages("verify.selectSubcontractor.change.hidden"))
              .withAttribute("id" -> "select-subcontractor")
          )
        )
      }
    }
}
}
