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

import models.{CheckMode, UserAnswers}
import pages.add.AddressOfSubcontractorPage
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

object AddressOfSubcontractorSummary {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(AddressOfSubcontractorPage).map { answer =>

      val lines: Seq[String] = Seq(
        answer.addressLine1,
        answer.addressLine2.getOrElse(""),
        answer.addressLine3,
        answer.addressLine4.getOrElse(""),
        answer.postCode
      )

      val escapedWithBreaks: String =
        lines
          .filter(_.trim.nonEmpty) // remove empty or whitespace-only lines
          .map(HtmlFormat.escape(_).toString)
          .mkString("<br/>")

      SummaryListRowViewModel(
        key = "addressOfSubcontractor.checkYourAnswersLabel",
        value = ValueViewModel(HtmlContent(escapedWithBreaks)),
        actions = Seq(
          ActionItemViewModel(
            "site.change",
            controllers.add.routes.AddressOfSubcontractorController.onPageLoad(CheckMode).url
          ).withVisuallyHiddenText(messages("addressOfSubcontractor.change.hidden"))
            .withAttribute("id" -> "address-of-subcontractor")
        )
      )
    }

}
