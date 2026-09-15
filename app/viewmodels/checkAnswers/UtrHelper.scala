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

package viewmodels.checkAnswers

import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.Value
import viewmodels.govuk.summarylist.*

object UtrViewModel {
  def apply(utr: String): Value =
    ValueViewModel(UtrContent(utr))
}

object UtrContent {
  def apply(utr: String): HtmlContent = {
    val zwsp = "\u200B"
    val broken = utr.grouped(1).mkString(zwsp)
    HtmlContent(s"""<span>${HtmlFormat.escape(broken)}</span>""")
  }
}
