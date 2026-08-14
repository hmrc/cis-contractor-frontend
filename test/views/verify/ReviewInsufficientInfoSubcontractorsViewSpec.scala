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
import views.html.verify.ReviewInsufficientInfoSubcontractorsView

class ReviewInsufficientInfoSubcontractorsViewSpec extends SpecBase {

  private implicit val request: Request[?]    = FakeRequest()
  private implicit val messagesImpl: Messages =
    app.injector.instanceOf[play.api.i18n.MessagesApi].preferred(FakeRequest())

  private val view = app.injector.instanceOf[ReviewInsufficientInfoSubcontractorsView]

  private def link(name: String) = LinkViewModel("#", name)

  private val missingRow =
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

  private def doc(vm: ReviewInsufficientInfoViewModel): Document =
    Jsoup.parse(view(vm).body)

  "ReviewInsufficientInfoSubcontractorsView" - {

    "must render the heading, title and introductory content" in {
      val document = doc(ReviewInsufficientInfoViewModel(missing = Seq(missingRow), ready = Nil))

      document.title     must include(messagesImpl("verify.reviewInsufficientInfo.title"))
      document.select("h1").text mustBe messagesImpl("verify.reviewInsufficientInfo.heading")
      document.body.text must include(messagesImpl("verify.reviewInsufficientInfo.p1"))
      document.body.text must include(messagesImpl("verify.reviewInsufficientInfo.p2"))
      document.select("ul.govuk-list--bullet li").size() mustBe 3
    }

    "must render the 'What you'll need' link opening in a new tab" in {
      val document = doc(ReviewInsufficientInfoViewModel(missing = Seq(missingRow), ready = Nil))

      val whatYouNeed = document.select("a[href$=verify-subcontractors]")
      whatYouNeed.size() mustBe 1
      whatYouNeed.attr("target") mustBe "_blank"
      whatYouNeed.text must include(messagesImpl("verify.reviewInsufficientInfo.whatYouNeed.link"))
    }

    "must render the missing information table with Edit, Proceed and Remove actions" in {
      val document = doc(ReviewInsufficientInfoViewModel(missing = Seq(missingRow), ready = Nil))

      val table = document.getElementById("missing-information-table")
      table must not be null

      table.text must include("Brody, Martin")
      table.text must include(messagesImpl("verify.reviewInsufficientInfo.utr.noneProvided"))
      table.text must include(messagesImpl("verify.reviewInsufficientInfo.action.edit"))
      table.text must include(messagesImpl("verify.reviewInsufficientInfo.action.proceed"))
      table.text must include(messagesImpl("verify.reviewInsufficientInfo.action.remove"))
    }

    "must render the ready table with the full Unique Taxpayer Reference header" in {
      val document = doc(ReviewInsufficientInfoViewModel(missing = Nil, ready = Seq(readyRow)))

      val table = document.getElementById("ready-table")
      table must not be null

      table.select("thead th").text must include(messagesImpl("verify.reviewInsufficientInfo.utr"))
      table.text                    must include("Smith, John")
      table.text                    must include("1234567890")
    }

    "must not render the missing table when there are no missing subcontractors" in {
      val document = doc(ReviewInsufficientInfoViewModel(missing = Nil, ready = Seq(readyRow)))

      document.getElementById("missing-information-table") mustBe null
    }

    "must not render the ready table when there are no ready subcontractors" in {
      val document = doc(ReviewInsufficientInfoViewModel(missing = Seq(missingRow), ready = Nil))

      document.getElementById("ready-table") mustBe null
    }

    "must render a Continue button only when all subcontractors are ready" in {
      val allReady = doc(ReviewInsufficientInfoViewModel(missing = Nil, ready = Seq(readyRow)))
      allReady.select(".govuk-button").text must include(messagesImpl("site.continue"))

      val stillMissing = doc(ReviewInsufficientInfoViewModel(missing = Seq(missingRow), ready = Seq(readyRow)))
      stillMissing.select(".govuk-button").size() mustBe 0
    }
  }
}
