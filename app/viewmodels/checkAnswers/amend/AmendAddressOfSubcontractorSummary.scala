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

import models.UserAnswers
import models.address.Address
import pages.add.AddressOfSubcontractorPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

object AmendAddressOfSubcontractorSummary {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(AddressOfSubcontractorPage).map { answer =>
      SummaryListRowViewModel(
        key = "addressOfSubcontractor.checkYourAnswersLabel",
        value = ValueViewModel(HtmlContent(Address.toHtml(answer).body)),
        actions = Seq(
          ActionItemViewModel(
            "site.change",
            controllers.add.routes.AddressOfSubcontractorController.redirectToAmendAddressLookup().url
          )
            .withVisuallyHiddenText(messages("addressOfSubcontractor.change.hidden"))
            .withAttribute("id" -> "address-of-subcontractor"),
          ActionItemViewModel(
            "site.remove",
            controllers.amend.routes.RemoveIndividualAddressController.onPageLoad().url
          )
            .withVisuallyHiddenText(messages("addressOfSubcontractor.remove.hidden"))
            .withAttribute("id" -> "remove-address-of-subcontractor")
        )
      )
    }
}
