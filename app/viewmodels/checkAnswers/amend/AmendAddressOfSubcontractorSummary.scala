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

package viewmodels.checkAnswers.amend

import models.UserAnswers
import pages.add.AddressOfSubcontractorPage
import play.api.i18n.Messages
import viewmodels.govuk.summarylist.*
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow

object AmendAddressOfSubcontractorSummary {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(AddressOfSubcontractorPage).map { answer =>
      SummaryListRowViewModel.row(
        address = answer,
        key = "addressOfSubcontractor.checkYourAnswersLabel",
        changeCall = controllers.add.routes.AddressOfSubcontractorController.redirectToAmendAddressLookup().url,
        hiddenTextKey = "addressOfSubcontractor.change.hidden",
        id = "address-of-subcontractor"
      )
    }

}
