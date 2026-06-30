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

package viewmodels.checkAnswers.add.partnership

import models.UserAnswers
import pages.add.partnership.PartnershipAddressPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.checkAnswers.add.AddressSummaryRow

object PartnershipAddressSummary {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(PartnershipAddressPage).map { answer =>
      AddressSummaryRow.row(
        address = answer,
        key = "partnershipAddress.checkYourAnswersLabel",
        changeCall =
          controllers.add.partnership.routes.PartnershipAddressController.redirectToAddressLookup(Some("change")),
        hiddenTextKey = "partnershipAddress.change.hidden",
        id = "address-of-partnership"
      )
    }

}
