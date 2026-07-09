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

package viewmodels.checkAnswers.add.company

import models.contact.ContactMethodOptions
import models.{CheckMode, Mode, UserAnswers}
import pages.add.company.CompanyContactMethodOptionsPage
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.checkAnswers.verify.ValueViewModelHelper
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

object CompanyContactMethodOptionsSummary {

  def row(answers: UserAnswers, mode: Mode = CheckMode)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(CompanyContactMethodOptionsPage).map { selectedMethods =>
      val options =
        ContactMethodOptions
          .ordered(selectedMethods)
          .map(m => HtmlFormat.escape(messages(s"companyContactMethodOptions.$m")).toString)
      SummaryListRowViewModel(
        key = "companyContactMethodOptions.checkYourAnswersLabel",
        value = ValueViewModelHelper
          .makeGovukBulletList(options)
          .getOrElse(ValueViewModel(HtmlContent(""))),
        actions = Seq(
          ActionItemViewModel(
            "site.change",
            controllers.add.company.routes.CompanyContactMethodOptionsController.onPageLoad(mode).url
          )
            .withVisuallyHiddenText(messages("companyContactMethodOptions.change.hidden"))
            .withAttribute("id" -> "company-contact-methods")
        )
      )
    }
}
