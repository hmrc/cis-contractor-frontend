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

package views.finalvalidations

import base.SpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.api.mvc.Request
import play.api.test.FakeRequest
import viewmodels.contractordetails.{ContractorDetailsTaskViewModel, ReviewContractorDetailsViewModel}
import views.html.finalvalidations.ReviewContractorDetailsView

class ReviewContractorDetailsViewSpec extends SpecBase {

  private implicit val request: Request[?]    = FakeRequest()
  private implicit val messagesImpl: Messages =
    app.injector.instanceOf[play.api.i18n.MessagesApi].preferred(FakeRequest())

  private val view = app.injector.instanceOf[ReviewContractorDetailsView]

  private def doc(viewModel: ReviewContractorDetailsViewModel): Document =
    Jsoup.parse(view(viewModel).body)

  "ReviewContractorDetailsView" - {

    "must highlight incomplete tasks and not complete tasks" in {
      val viewModel =
        ReviewContractorDetailsViewModel(
          tasks = Seq(
            ContractorDetailsTaskViewModel(
              titleKey = "finalValidations.reviewContractorDetails.task.utr",
              statusKey = "finalValidations.reviewContractorDetails.status.incomplete",
              href = Some("/construction-industry-scheme/contractor-details/enter-contractors-utr"),
              id = "contractor-utr"
            ),
            ContractorDetailsTaskViewModel(
              titleKey = "finalValidations.reviewContractorDetails.task.schemeName",
              statusKey = "finalValidations.reviewContractorDetails.status.complete",
              href = None,
              id = "scheme-name"
            )
          ),
          finalTask = ContractorDetailsTaskViewModel(
            titleKey = "finalValidations.reviewContractorDetails.task.fileReturn",
            statusKey = "finalValidations.reviewContractorDetails.status.cannotStart",
            href = None,
            id = "final-action"
          )
        )

      val document = doc(viewModel)

      document.select("#contractor-utr-status .govuk-tag").text mustBe messagesImpl(
        "finalValidations.reviewContractorDetails.status.incomplete"
      )
      document.select("#scheme-name-status .govuk-tag").size() mustBe 0
      document.select("#scheme-name-status").text mustBe messagesImpl(
        "finalValidations.reviewContractorDetails.status.complete"
      )
      document.select("#final-action-status .govuk-tag").size() mustBe 0
    }
  }
}
