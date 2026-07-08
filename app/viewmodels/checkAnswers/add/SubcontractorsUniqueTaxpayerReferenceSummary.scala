/*
 * Copyright 2025 HM Revenue & Customs
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
import pages.add.SubcontractorsUniqueTaxpayerReferencePage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

object SubcontractorsUniqueTaxpayerReferenceSummary {

  def row(answers: UserAnswers, mode: Mode = CheckMode, showActions: Boolean = true)(implicit
    messages: Messages
  ): Option[SummaryListRow] =
    answers.get(SubcontractorsUniqueTaxpayerReferencePage).map { answer =>
      val value   = ValueViewModel(answer)
      val actions =
        if (showActions) {
          Seq(
            ActionItemViewModel(
              "site.change",
              controllers.add.routes.SubcontractorsUniqueTaxpayerReferenceController.onPageLoad(mode).url
            )
              .withVisuallyHiddenText(messages("subcontractorsUniqueTaxpayerReference.change.hidden"))
              .withAttribute("id" -> "subcontractors-unique-taxpayer-reference")
          )
        } else {
          Seq.empty
        }
      SummaryListRowViewModel(
        key = "subcontractorsUniqueTaxpayerReference.checkYourAnswersLabel",
        value = value,
        actions = actions
      )
    }
}
