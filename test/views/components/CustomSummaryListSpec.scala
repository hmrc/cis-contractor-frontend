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

package views.components

import base.SpecBase
import play.api.i18n.Messages
import views.html.components.CustomSummaryList

class CustomSummaryListSpec extends SpecBase {

  "CustomSummaryList" - {

    "must render the rows, values and action links" in {

      implicit val msgs: Messages = messages(app)

      val view = new CustomSummaryList()

      val result =
        view(
          rows = Seq(
            "Name" -> Seq(
              ("/change", "site.change", "site.change.hidden")
            )
          ),
          rowValues = Seq("John Smith")
        ).toString

      result must include("Name")
      result must include("John Smith")
      result must include("""href="/change"""")
      result must include("""class="govuk-summary-list__value"""")
      result must include("""class="govuk-link"""")
    }
  }
}
