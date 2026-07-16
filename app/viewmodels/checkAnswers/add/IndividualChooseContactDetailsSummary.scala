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

import models.{CheckMode, Mode, UserAnswers}
import pages.add.IndividualChooseContactDetailsPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import utils.Utils
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

object IndividualChooseContactDetailsSummary {

  def row(answers: UserAnswers, mode: Mode = CheckMode)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(IndividualChooseContactDetailsPage).map { answer =>
      val cyaMsg = Utils.findFirstMessagesValue(
        Seq(s"individualChooseContactDetails.cya.$answer", s"individualChooseContactDetails.$answer")
      )

      SummaryListRowViewModel(
        key = "individualChooseContactDetails.checkYourAnswersLabel",
        value = ValueViewModel(cyaMsg),
        actions = Seq(
          ActionItemViewModel(
            "site.change",
            controllers.add.routes.IndividualChooseContactDetailsController.onPageLoad(mode).url
          )
            .withVisuallyHiddenText(messages("individualChooseContactDetails.change.hidden"))
        )
      )
    }
}
