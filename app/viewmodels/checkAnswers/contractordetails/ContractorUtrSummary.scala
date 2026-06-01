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

package viewmodels.checkAnswers.contractordetails

import models.{CheckMode, UserAnswers}
import pages.contractordetails.ContractorUtrPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

object ContractorUtrSummary {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(ContractorUtrPage).map { answer =>
      if (answer.trim.isEmpty) {
        SummaryListRowViewModel(
          key = messages("contractordetails.contractorUtr.checkYourAnswersLabel"),
          value = ValueViewModel(""),
          actions = Seq(
            ActionItemViewModel(
              messages("contractordetails.contractorDetailsCheckAnswers.table.link.addDetails"),
              controllers.contractordetails.routes.ContractorUtrController.onPageLoad(CheckMode).url
            ).withVisuallyHiddenText(messages("contractordetails.contractorUtr.change.hidden"))
              .withAttribute("id" -> "contractor-utr")
          )
        )
      } else {
        SummaryListRowViewModel(
          key = messages("contractordetails.contractorUtr.checkYourAnswersLabel"),
          value = ValueViewModel(answer),
          actions = Seq(
            ActionItemViewModel(
              messages("site.change"),
              controllers.contractordetails.routes.ContractorUtrController.onPageLoad(CheckMode).url
            ).withVisuallyHiddenText(messages("contractordetails.contractorUtr.change.hidden"))
              .withAttribute("id" -> "contractor-utr")
          )
        )
      }
    }
}
