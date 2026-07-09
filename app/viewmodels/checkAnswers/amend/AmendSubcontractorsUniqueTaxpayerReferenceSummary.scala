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

package viewmodels.checkAnswers.amend

import models.{AmendMode, UserAnswers}
import pages.add.SubcontractorsUniqueTaxpayerReferencePage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

object AmendSubcontractorsUniqueTaxpayerReferenceSummary {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(SubcontractorsUniqueTaxpayerReferencePage).map { answer =>
      SummaryListRowViewModel(
        key = "subcontractorsUniqueTaxpayerReference.checkYourAnswersLabel",
        value = ValueViewModel(answer),
        actions = Seq(
          ActionItemViewModel(
            "site.change",
            controllers.add.routes.SubcontractorsUniqueTaxpayerReferenceController.onPageLoad(AmendMode).url
          )
            .withVisuallyHiddenText(messages("subcontractorsUniqueTaxpayerReference.change.hidden"))
            .withAttribute("id" -> "subcontractors-unique-taxpayer-reference")
        )
      )
    }
}
