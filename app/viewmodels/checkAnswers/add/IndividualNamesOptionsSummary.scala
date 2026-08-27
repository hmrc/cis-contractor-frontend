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

package viewmodels.checkAnswers.add

import models.add.IndividualNamesOptions
import models.{CheckMode, Mode, UserAnswers}
import pages.add.IndividualNamesOptionsPage
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.checkAnswers.verify.ValueViewModelHelper
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

object IndividualNamesOptionsSummary {

  def row(answers: UserAnswers, mode: Mode = CheckMode)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(IndividualNamesOptionsPage).map { selectedMethods =>

      val options =
        if (selectedMethods.isEmpty) {
          Seq(HtmlFormat.escape(messages("individualNamesOptions.noSelection")).toString)
        } else {
          IndividualNamesOptions
            .ordered(selectedMethods)
            .map(m => HtmlFormat.escape(messages(s"individualNamesOptions.$m")).toString)
        }

      SummaryListRowViewModel(
        key = "individualNamesOptions.checkYourAnswersLabel",
        value = ValueViewModelHelper
          .makeGovukBulletList(options, false)
          .getOrElse(ValueViewModel(HtmlContent(""))),
        actions = Seq(
          ActionItemViewModel(
            "site.change",
            controllers.add.routes.IndividualNamesOptionsController.onPageLoad(mode).url
          )
            .withVisuallyHiddenText(messages("individualNamesOptions.change.hidden"))
            .withAttribute("id" -> "individual-names-options")
        )
      )
    }
}
