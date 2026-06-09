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

package viewmodels.checkAnswers.add

import models.address.Address
import play.api.i18n.Messages
import play.api.mvc.Call
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

/** Builds the Check Your Answers summary row for an ALF [[Address]], shared by every subcontractor type (individual,
  * company, partnership, trust). Only the message key, change link, hidden text and row id differ between journeys.
  */
object AddressSummaryRow {

  def row(address: Address, key: String, changeCall: Call, hiddenTextKey: String, id: String)(implicit
    messages: Messages
  ): SummaryListRow = {
    val addressHtml: String =
      Seq(
        address.addressLine1,
        address.addressLine2.getOrElse(""),
        address.addressLine3.getOrElse(""),
        address.addressLine4.getOrElse(""),
        address.postcode.getOrElse(""),
        address.country.flatMap(_.name).getOrElse("")
      ).filter(_.trim.nonEmpty).mkString("<br/>")

    SummaryListRowViewModel(
      key = key,
      value = ValueViewModel(HtmlContent(addressHtml)),
      actions = Seq(
        ActionItemViewModel("site.change", changeCall.url)
          .withVisuallyHiddenText(messages(hiddenTextKey))
          .withAttribute("id" -> id)
      )
    )
  }
}
