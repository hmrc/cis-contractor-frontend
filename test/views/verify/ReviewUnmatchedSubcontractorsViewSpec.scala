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

package views.verify

import base.SpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.api.mvc.Request
import play.api.test.FakeRequest
import viewmodels.verify.*
import views.html.verify.ReviewUnmatchedSubcontractorsView

class ReviewUnmatchedSubcontractorsViewSpec extends SpecBase {

  private implicit val request: Request[?] = FakeRequest()

  private implicit val messagesImpl: Messages =
    app.injector.instanceOf[play.api.i18n.MessagesApi].preferred(FakeRequest())

  private val view =
    app.injector.instanceOf[ReviewUnmatchedSubcontractorsView]

  private def link(name: String) = LinkViewModel("#", name)

  private val unmatchedRow =
    MissingSubcontractorRow(
      name = "Brody, Martin",
      nameLink = link("Brody, Martin"),
      utr = "None provided",
      editLink = link("Brody, Martin"),
      proceedLink = link("Brody, Martin"),
      removeLink = link("Brody, Martin")
    )

  private val readyRow =
    ReadySubcontractorRow(
      name = "Smith, John",
      nameLink = link("Smith, John"),
      utr = "1234567890"
    )

  private def doc(vm: ReviewUnmatchedViewModel): Document =
    Jsoup.parse(view(vm).body)

  "ReviewUnmatchedSubcontractorsView" - {

    "must render the heading, title and introductory content" in {
      val document =
        doc(ReviewUnmatchedViewModel(unmatched = Seq(unmatchedRow), ready = Nil))

      document.title     must include(messagesImpl("verify.reviewUnmatched.title"))
      document.select("h1").text mustBe messagesImpl("verify.reviewUnmatched.heading")
      document.body.text must include(messagesImpl("verify.reviewUnmatched.p1"))
      document.body.text must include(messagesImpl("verify.reviewUnmatched.p2"))
      document.select("ul.govuk-list--bullet li").size() mustBe 3
    }

    "must render the guidance link opening in a new tab" in {
      val document =
        doc(ReviewUnmatchedViewModel(unmatched = Seq(unmatchedRow), ready = Nil))

      val whatYouNeed =
        document.select("a[href$='what-you-must-do-as-a-cis-contractor']")

      whatYouNeed.size() mustBe 1
      whatYouNeed.attr("target") mustBe "_blank"
      whatYouNeed.text must include(
        messagesImpl("verify.reviewUnmatched.whatYouNeed.link")
      )
    }

    "must render the unmatched subcontractors table with Edit, Proceed and Remove actions" in {
      val document =
        doc(ReviewUnmatchedViewModel(unmatched = Seq(unmatchedRow), ready = Nil))

      val table = document.getElementById("unmatched-subcontractors-table")

      table      must not be null
      table.text must include("Brody, Martin")
      table.text must include(messagesImpl("verify.reviewUnmatched.noneProvided"))
      table.text must include(messagesImpl("verify.reviewUnmatched.action.edit"))
      table.text must include(messagesImpl("verify.reviewUnmatched.action.proceed"))
      table.text must include(messagesImpl("verify.reviewUnmatched.action.remove"))
    }

    "must render the subcontractor name link with visually hidden text" in {
      val document =
        doc(ReviewUnmatchedViewModel(unmatched = Seq(unmatchedRow), ready = Nil))

      val nameLink =
        document.select("#unmatched-subcontractors-table a").first()

      nameLink.text mustBe
        s"Brody, Martin ${messagesImpl("verify.reviewUnmatched.name.hidden", "Brody, Martin")}"

      val hiddenText =
        nameLink.select(".govuk-visually-hidden")

      hiddenText.size() mustBe 1
      hiddenText.text mustBe
        messagesImpl("verify.reviewUnmatched.name.hidden", "Brody, Martin")
    }

    "must render visually hidden text for each unmatched action link" in {
      val document =
        doc(ReviewUnmatchedViewModel(unmatched = Seq(unmatchedRow), ready = Nil))

      val actionLinks =
        document.select("#unmatched-subcontractors-table a")

      actionLinks.size() mustBe 4

      actionLinks.get(1).text mustBe
        s"${messagesImpl("verify.reviewUnmatched.action.edit")} " +
        messagesImpl(
          "verify.reviewUnmatched.action.edit.hidden",
          "Brody, Martin"
        )

      actionLinks.get(2).text mustBe
        s"${messagesImpl("verify.reviewUnmatched.action.proceed")} " +
        messagesImpl(
          "verify.reviewUnmatched.action.proceed.hidden",
          "Brody, Martin"
        )

      actionLinks.get(3).text mustBe
        s"${messagesImpl("verify.reviewUnmatched.action.remove")} " +
        messagesImpl(
          "verify.reviewUnmatched.action.remove.hidden",
          "Brody, Martin"
        )
    }

    "must render the correct visually hidden text inside each unmatched action link" in {
      val document =
        doc(ReviewUnmatchedViewModel(unmatched = Seq(unmatchedRow), ready = Nil))

      val actionLinks =
        document.select("#unmatched-subcontractors-table a")

      actionLinks.size() mustBe 4

      actionLinks
        .get(1)
        .select(".govuk-visually-hidden")
        .text mustBe
        messagesImpl(
          "verify.reviewUnmatched.action.edit.hidden",
          "Brody, Martin"
        )

      actionLinks
        .get(2)
        .select(".govuk-visually-hidden")
        .text mustBe
        messagesImpl(
          "verify.reviewUnmatched.action.proceed.hidden",
          "Brody, Martin"
        )

      actionLinks
        .get(3)
        .select(".govuk-visually-hidden")
        .text mustBe
        messagesImpl(
          "verify.reviewUnmatched.action.remove.hidden",
          "Brody, Martin"
        )
    }

    "must render separators between unmatched action links as aria-hidden" in {
      val document =
        doc(ReviewUnmatchedViewModel(unmatched = Seq(unmatchedRow), ready = Nil))

      val separators =
        document.select("#unmatched-subcontractors-table span[aria-hidden='true']")

      separators.size() mustBe 2
      separators.eachText() mustBe java.util.Arrays.asList("|", "|")
    }

    "must render the ready table with the full Unique Taxpayer Reference header" in {
      val document =
        doc(ReviewUnmatchedViewModel(unmatched = Nil, ready = Seq(readyRow)))

      val table = document.getElementById("ready-table")

      table                         must not be null
      table.select("thead th").text must include(
        messagesImpl("verify.reviewUnmatched.utr")
      )
      table.text                    must include("Smith, John")
      table.text                    must include("1234567890")
    }

    "must render visually hidden text for a ready subcontractor name" in {
      val document =
        doc(ReviewUnmatchedViewModel(unmatched = Nil, ready = Seq(readyRow)))

      val nameLink =
        document.select("#ready-table a").first()

      nameLink.text mustBe
        s"Smith, John ${messagesImpl("verify.reviewUnmatched.name.hidden", "Smith, John")}"

      nameLink
        .select(".govuk-visually-hidden")
        .text mustBe
        messagesImpl("verify.reviewUnmatched.name.hidden", "Smith, John")
    }

    "must not render the unmatched table when there are no unmatched subcontractors" in {
      val document =
        doc(ReviewUnmatchedViewModel(unmatched = Nil, ready = Seq(readyRow)))

      document.getElementById("unmatched-subcontractors-table") mustBe null
    }

    "must not render the ready table when there are no ready subcontractors" in {
      val document =
        doc(ReviewUnmatchedViewModel(unmatched = Seq(unmatchedRow), ready = Nil))

      document.getElementById("ready-table") mustBe null
    }

    "must render a Continue button only when all subcontractors are ready" in {
      val allReady =
        doc(ReviewUnmatchedViewModel(unmatched = Nil, ready = Seq(readyRow)))

      allReady.select(".govuk-button").text must include(
        messagesImpl("site.continue")
      )

      val stillUnmatched =
        doc(
          ReviewUnmatchedViewModel(
            unmatched = Seq(unmatchedRow),
            ready = Seq(readyRow)
          )
        )

      stillUnmatched.select(".govuk-button").size() mustBe 0
    }

    "must render the 'Back to Verification results' link" in {
      val document =
        doc(ReviewUnmatchedViewModel(unmatched = Seq(unmatchedRow), ready = Nil))

      document.body.text must include(
        messagesImpl("verify.reviewUnmatched.backToResults.link")
      )
    }

    "must render the back to results link" in {
      val document =
        doc(
          ReviewUnmatchedViewModel(
            unmatched = Seq(unmatchedRow),
            ready = Nil
          )
        )

      val backToResults =
        document.select(
          s"a[href='${controllers.verify.routes.VerificationResultsController.onPageLoad().url}']"
        )

      backToResults.size() mustBe 1
      backToResults.text() mustBe
        messagesImpl("verify.reviewUnmatched.backToResults.link")
    }
  }
}
