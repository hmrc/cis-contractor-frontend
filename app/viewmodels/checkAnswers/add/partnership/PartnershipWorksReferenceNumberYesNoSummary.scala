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

package viewmodels.checkAnswers.add.partnership

import models.amend.partnership.AmendPartnershipRemoveDetail
import models.{AmendMode, CheckMode, Mode, UserAnswers}
import pages.add.partnership.PartnershipWorksReferenceNumberYesNoPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*
import models.info.partnership.PartnershipAnswers

object PartnershipWorksReferenceNumberYesNoSummary {

  def row(answers: UserAnswers, mode: Mode = CheckMode)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(PartnershipWorksReferenceNumberYesNoPage).map { answer =>

      val value = if (answer) "site.yes" else "site.no"

      SummaryListRowViewModel(
        key = "partnershipWorksReferenceNumberYesNo.checkYourAnswersLabel",
        value = ValueViewModel(value),
        actions = Seq(
          ActionItemViewModel(
            "site.change",
            if answer && mode == AmendMode then
              controllers.amend.partnership.routes.AmendPartnershipRemoveDetailYesNoController
                .onPageLoad(AmendPartnershipRemoveDetail.WorksReferenceNumber.key)
                .url
            else controllers.add.partnership.routes.PartnershipWorksReferenceNumberYesNoController.onPageLoad(mode).url
          )
            .withVisuallyHiddenText(messages("partnershipWorksReferenceNumberYesNo.change.hidden"))
            .withAttribute("id" -> "add-partnership-works-reference-number")
        )
      )
    }

  def row(
    answers: PartnershipAnswers
  )(implicit messages: Messages): Option[SummaryListRow] =
    answers.nominatedPartnerWorksReferenceYesNo.map { answer =>

      val value = if (answer) "site.yes" else "site.no"

      SummaryListRowViewModel(
        key = "partnershipWorksReferenceNumberYesNo.checkYourAnswersLabel",
        value = ValueViewModel(value),
        actions = Seq.empty
      )
    }
}
