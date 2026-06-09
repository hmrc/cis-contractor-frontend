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
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

object PartnershipAddressSummary {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(PartnershipAddressPage).map { answer =>

      val lines: Seq[String] = Seq(
        answer.addressLine1,
        answer.addressLine2.getOrElse(""),
        answer.addressLine3.getOrElse(""),
        answer.addressLine4.getOrElse(""),
        answer.postcode.getOrElse(""),
        answer.country.flatMap(_.name).getOrElse("")
      )

      val addressHtml: String =
        lines
          .filter(_.trim.nonEmpty)
          .mkString("<br/>")

      SummaryListRowViewModel(
        key = "partnershipAddress.checkYourAnswersLabel",
        value = ValueViewModel(HtmlContent(addressHtml)),
        actions = Seq(
          ActionItemViewModel(
            "site.change",
            controllers.add.partnership.routes.PartnershipAddressController.redirectToAddressLookup(Some("change")).url
          ).withVisuallyHiddenText(messages("partnershipAddress.change.hidden"))
            .withAttribute("id" -> "address-of-partnership")
        )
      )
    }

}
