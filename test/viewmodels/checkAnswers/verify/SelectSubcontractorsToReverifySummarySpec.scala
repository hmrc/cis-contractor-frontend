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

import base.SpecBase
import models.{CheckMode, UserAnswers}
import org.scalatest.OptionValues._
import org.scalatest.matchers.must.Matchers
import pages.verify.SelectSubcontractorsToReverifyPage
import play.api.i18n.{Lang, Messages, MessagesImpl}
import play.api.test.Helpers.stubMessagesApi
import models.verify.SelectedSubcontractors
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist._

class SelectSubcontractorsToReverifySummarySpec extends SpecBase with Matchers {

  private val messagesApi                 = stubMessagesApi()
  private implicit val messages: Messages = MessagesImpl(Lang.defaultLang, messagesApi)

  "SelectSubcontractorsToReverifySummary.row" - {

    "must return a summary row with multiple selected subcontractors" in {

      val answers: UserAnswers =
        emptyUserAnswers
          .set(
            SelectSubcontractorsToReverifyPage,
            Set(
              SelectedSubcontractors("Grantalan", "Grant, Alan"),
              SelectedSubcontractors("Hammondhouse", "Hammond House")
            )
          )
          .success
          .value

      val result = SelectSubcontractorsToReverifySummary.row(answers)

      result mustBe defined

      val row = result.value

      row.key.content.asHtml.toString must include(
        messages("verify.selectSubcontractorsToReverify.checkYourAnswersLabel")
      )

      val valueHtml = row.value.content.asHtml.toString

      valueHtml must include("Grant, Alan")
      valueHtml must include("Hammond House")
      valueHtml must include("<br>")

      row.actions mustBe defined

      val action = row.actions.value.items.head

      action.href mustBe controllers.verify.routes.SelectSubcontractorsToReverifyController
        .onPageLoad(CheckMode)
        .url

      action.content.asHtml.toString must include(messages("site.change"))

      action.visuallyHiddenText mustBe Some(
        messages("verify.selectSubcontractorsToReverify.change.hidden")
      )

      action.attributes must contain(
        "id" -> "select-subcontractors-to-reverify"
      )
    }

    "must return a summary row with a single selected subcontractor" in {

      val answers: UserAnswers =
        emptyUserAnswers
          .set(
            SelectSubcontractorsToReverifyPage,
            Set(SelectedSubcontractors("Ingenresearch", "InGen Research"))
          )
          .success
          .value

      val result = SelectSubcontractorsToReverifySummary.row(answers)

      result mustBe defined

      val valueHtml = result.value.value.content.asHtml.toString

      valueHtml must include("InGen Research")
      valueHtml must not include "<br>"
    }

    "must return None when no subcontractors are selected" in {
      SelectSubcontractorsToReverifySummary.row(emptyUserAnswers) mustBe None
    }
  }
}
