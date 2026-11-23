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

import models.{CheckMode, UserAnswers}
import models.verify.ContractorEmailConfirmationStored.DifferentEmail
import pages.verify.{ContractorEmailConfirmationNotStoredPage, ContractorEmailConfirmationStoredPage, EmailAddressPage}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

object EmailAddressSummary {

  private def emailRowRequired(answers: UserAnswers, subJourneyId: Option[String]): Boolean = {
    val storedPage    =
      subJourneyId.map(ContractorEmailConfirmationStoredPage.apply).getOrElse(ContractorEmailConfirmationStoredPage)
    val notStoredPage = subJourneyId
      .map(ContractorEmailConfirmationNotStoredPage.apply)
      .getOrElse(ContractorEmailConfirmationNotStoredPage)
    answers.get(storedPage).contains(DifferentEmail) ||
    answers.get(notStoredPage).contains(true)
  }

  def row(answers: UserAnswers, subJourneyId: Option[String] = None)(implicit
    messages: Messages
  ): Option[SummaryListRow] = {
    val emailPage = subJourneyId.map(EmailAddressPage.apply).getOrElse(EmailAddressPage)
    Option.when(emailRowRequired(answers, subJourneyId))(answers.get(emailPage)).flatten.map { answer =>
      SummaryListRowViewModel(
        key = "verify.emailAddress.checkYourAnswersLabel",
        value = ValueViewModel(answer),
        actions = Seq(
          ActionItemViewModel(
            "site.change",
            subJourneyId
              .map(id =>
                controllers.verify.routes.EmailAddressController
                  .onPageLoadWithId(java.util.UUID.fromString(id), CheckMode)
                  .url
              )
              .getOrElse(controllers.verify.routes.EmailAddressController.onPageLoad(CheckMode).url)
          )
            .withVisuallyHiddenText(messages("verify.emailAddress.change.hidden"))
            .withAttribute("id" -> "verify-email-address")
        )
      )
    }
  }
}
