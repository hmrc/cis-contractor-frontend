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
import models.{CheckMode, SubcontractorViewModel, UserAnswers}
import org.scalatest.OptionValues._
import org.scalatest.matchers.must.Matchers
import pages.verify.SelectSubcontractorPage
import play.api.i18n.{Lang, Messages, MessagesImpl}
import play.api.test.Helpers.stubMessagesApi
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist._

class SelectSubcontractorSummarySpec extends SpecBase with Matchers {

  private val messagesApi                 = stubMessagesApi()
  private implicit val messages: Messages = MessagesImpl(Lang.defaultLang, messagesApi)

  private val brodyMartin   = SubcontractorViewModel("brodyMartin", "Brody Martin")
  private val alphaPlumbing = SubcontractorViewModel("alphaPlumbing", "Alpha Plumbing")
  private val deltaElec     = SubcontractorViewModel("deltaElectrical", "Delta Electrical")

  "SelectSubcontractorSummary.row" - {

    "must return a row with multiple selected subcontractors" in {

      val answers: UserAnswers =
        emptyUserAnswers
          .set(SelectSubcontractorPage, Set(brodyMartin, alphaPlumbing))
          .success
          .value

      val result = SelectSubcontractorSummary.row(answers)

      result mustBe defined

      val row = result.value

      row.key.content.asHtml.toString must include(messages("verify.selectSubcontractor.checkYourAnswersLabel"))

      val valueHtml = row.value.content.asHtml.toString

      valueHtml must include("Brody Martin")
      valueHtml must include("Alpha Plumbing")
      valueHtml must include("<br>")

      row.actions mustBe defined

      val actions = row.actions.value.items
      actions must have size 1

      val action = actions.head

      action.href mustBe controllers.verify.routes.SelectSubcontractorController
        .onPageLoad(CheckMode)
        .url

      action.content.asHtml.toString must include(messages("site.change"))

      action.visuallyHiddenText mustBe Some(
        messages("verify.selectSubcontractor.change.hidden")
      )

      action.attributes must contain("id" -> "select-subcontractor")
    }

    "must return a row with a single selected subcontractor" in {

      val answers: UserAnswers =
        emptyUserAnswers
          .set(SelectSubcontractorPage, Set(deltaElec))
          .success
          .value

      val result = SelectSubcontractorSummary.row(answers)

      result mustBe defined

      val valueHtml = result.value.value.content.asHtml.toString

      valueHtml must include("Delta Electrical")
      valueHtml must not include "<br>"
    }

    "must return None when no answer is present" in {
      SelectSubcontractorSummary.row(emptyUserAnswers) mustBe None
    }
  }
}
