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

import models.{AmendMode, CheckMode, Mode, UserAnswers}
import pages.add.AddressOfSubcontractorPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow

object AddressOfSubcontractorSummary {

  def row(answers: UserAnswers, mode: Mode = CheckMode)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(AddressOfSubcontractorPage).map { answer =>
      AddressSummaryRow.row(
        address = answer,
        key = "addressOfSubcontractor.checkYourAnswersLabel",
        changeCall = if (mode == AmendMode) {
          controllers.add.routes.AddressOfSubcontractorController.redirectToAmendAddressLookup()
        } else {
          controllers.add.routes.AddressOfSubcontractorController.redirectToAddressLookup(Some("change"))
        },
        hiddenTextKey = "addressOfSubcontractor.change.hidden",
        id = "address-of-subcontractor"
      )
    }

}
