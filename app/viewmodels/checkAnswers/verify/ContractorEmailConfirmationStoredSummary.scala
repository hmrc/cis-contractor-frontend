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

import models.verify.ContractorEmailConfirmationStored.CurrentEmail
import models.{CheckMode, UserAnswers}
import pages.verify.ContractorEmailConfirmationStoredPage
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import utils.VerifyEmailResolver
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

object ContractorEmailConfirmationStoredSummary {

  def row(answers: UserAnswers, subJourneyId: Option[String] = None)(implicit
    messages: Messages
  ): Option[SummaryListRow] = {
    val page =
      subJourneyId.map(ContractorEmailConfirmationStoredPage.apply).getOrElse(ContractorEmailConfirmationStoredPage)
    answers.get(page).map { answer =>

      val label = messages(s"verify.contractorEmailConfirmationStored.$answer")

      val displayText = answer match {
        case CurrentEmail =>
          VerifyEmailResolver.resolvedEmail(answers, subJourneyId).fold(label) { email =>
            s"$label: <strong>${HtmlFormat.escape(email)}</strong>"
          }
        case _            => HtmlFormat.escape(label).toString
      }

      val value = ValueViewModel(HtmlContent(displayText))

      SummaryListRowViewModel(
        key = "verify.contractorEmailConfirmationStored.checkYourAnswersLabel",
        value = value,
        actions = Seq(
          ActionItemViewModel(
            "site.change",
            subJourneyId
              .map(id =>
                controllers.verify.routes.ContractorEmailConfirmationStoredController
                  .onPageLoadWithId(java.util.UUID.fromString(id), CheckMode)
                  .url
              )
              .getOrElse(
                controllers.verify.routes.ContractorEmailConfirmationStoredController.onPageLoad(CheckMode).url
              )
          )
            .withVisuallyHiddenText(messages("verify.contractorEmailConfirmationStored.change.hidden"))
            .withAttribute("id" -> "contractor-email-confirmation-stored")
        )
      )
    }
  }
}
