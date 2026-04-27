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

import config.FrontendAppConfig
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import play.api.mvc.Request
import play.api.test.FakeRequest
import viewmodels.checkAnswers.verify.VerificationSubmittedViewModel
import views.html.verify.VerificationRequestSubmittedView

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class VerificationRequestSubmittedViewSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite {

  "VerificationRequestSubmittedView" should {

    "render the page correctly when reverify list and email are present" in new Setup {

      val doc = Jsoup.parse(html.toString())

      doc.title must include(
        messages("verify.verificationRequestSubmitted.title")
      )

      doc.select(".govuk-panel__title").text mustBe
        messages("verify.verificationRequestSubmitted.heading")

      doc.select(".govuk-panel__body").text must include(referenceNumber)

      doc.text must include(
        messages(
          "verify.verificationRequestSubmitted.submittedAt",
          submittedAt.format(
            DateTimeFormatter.ofPattern("HH:mm 'on' dd MMMM yyyy")
          )
        )
      )

      doc.select("h2").text must include(
        messages("verify.verificationRequestSubmitted.details.subHeading")
      )

      subcontractorsToVerify.foreach { name =>
        doc.select("ul.govuk-list--bullet").text must include(name)
      }

      subcontractorsToReverify.foreach { name =>
        doc.select("ul.govuk-list--bullet").text must include(name)
      }

      doc.select("p.govuk-body").text must include(email)

      val emailVerificationLink =
        doc.select(s"a[href='${appConfig.cisGeneralEnquiries}']")

      emailVerificationLink.text must include(
        messages("verify.verificationRequestSubmitted.email.verification.link")
      )

      // Inset text print section
      doc.select(".govuk-inset-text").text must include(
        messages("verify.verificationRequestSubmitted.print.text")
      )

      doc.select(".govuk-inset-text").text must include(
        messages("verify.verificationRequestSubmitted.print.link")
      )

      doc.select("h2").text must include(
        messages("verify.verificationRequestSubmitted.needHelp.subHeading")
      )

      val manageLink =
        doc.select(s"a[href='${appConfig.manageSubcontractorsUrl}']")

      manageLink.text must include(
        messages("verify.verificationRequestSubmitted.needHelp.manageSubcontractors.link")
      )

      doc.select("h2").text must include(
        messages("verify.verificationRequestSubmitted.feedback.subHeading")
      )

      val surveyLink =
        doc.select(s"a[href='${appConfig.exitSurveyUrl}']")

      surveyLink.size mustBe 1
      surveyLink.attr("target") mustBe "_blank"

      surveyLink.first.parent.text must include(
        messages("verify.verificationRequestSubmitted.feedback.p2")
      )
    }

    "not render reverify section or email paragraph when both are absent" in new Setup {

      override val viewModel: VerificationSubmittedViewModel =
        viewModel.copy(
          subcontractorsToReverify = Seq.empty,
          confirmationEmail = None
        )

      val doc = Jsoup.parse(html.toString())

      doc.text must not include
        messages("verify.verificationRequestSubmitted.subcontractorsToReverify.label")

      doc.text must not include email
    }
  }

  trait Setup {

    implicit val request: Request[_] =
      FakeRequest()

    implicit val messages: Messages =
      MessagesImpl(
        Lang.defaultLang,
        app.injector.instanceOf[MessagesApi]
      )

    implicit val appConfig: FrontendAppConfig =
      app.injector.instanceOf[FrontendAppConfig]

    val view: VerificationRequestSubmittedView =
      app.injector.instanceOf[VerificationRequestSubmittedView]

    val referenceNumber = "Reference number 12345"
    val submittedAt     = LocalDateTime.of(2026, 4, 27, 10, 30)

    val subcontractorsToVerify =
      Seq(
        "Brody, Martin",
        "Hooper And Associates",
        "Quint Transportation",
        "The Kintner Group"
      )

    val subcontractorsToReverify =
      Seq(
        "Grant, Alan",
        "InGen Research"
      )

    val email = "test@testmail.com"

    val viewModel: VerificationSubmittedViewModel =
      VerificationSubmittedViewModel(
        referenceNumber = referenceNumber,
        submittedAt = submittedAt,
        subcontractorsToVerify = subcontractorsToVerify,
        subcontractorsToReverify = subcontractorsToReverify,
        confirmationEmail = Some(email)
      )

    lazy val html =
      view(viewModel)
  }
}
