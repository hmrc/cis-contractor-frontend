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

import models.{CheckMode, UserAnswers}
import pages.add.partnership.{PartnershipHasUtrYesNoPage, PartnershipNamePage}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

object PartnershipHasUtrYesNoSummary {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(PartnershipHasUtrYesNoPage).map { answer =>
      val partnershipName = answers.get(PartnershipNamePage)
        .getOrElse(throw MissingRequiredAnswer("PartnershipNamePage"))

      val yesNoText = if (answer) messages("site.yes") else messages("site.no")

      SummaryListRowViewModel(
        key = KeyViewModel(
          Text(messages("partnershipHasUtrYesNo.checkYourAnswersLabel", partnershipName))
        ),
        value = ValueViewModel(yesNoText),
        actions = Seq(
          ActionItemViewModel(
            content = messages("site.change"),
            href = controllers.add.partnership.routes.PartnershipHasUtrYesNoController
              .onPageLoad(CheckMode)
              .url
          ).withVisuallyHiddenText(
            messages("partnershipHasUtrYesNo.change.hidden")
          )
        )
      )
    }
}
final case class MissingRequiredAnswer(page: String) extends RuntimeException(page)