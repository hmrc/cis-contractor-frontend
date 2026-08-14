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

package views.unmatched

import base.SpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import play.api.i18n.Messages
import play.api.test.FakeRequest
import play.api.test.Helpers.GET
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.TableRow
import views.html.unmatched.UnmatchedSubcontractorDetailsUpdatedView

import scala.jdk.CollectionConverters.CollectionHasAsScala

class UnmatchedSubcontractorDetailsUpdatedViewSpec extends SpecBase {

  private val subcontractorName =
    "Martin Brody"

  private val utr =
    "3992651526"

  private lazy val application =
    applicationBuilder().build()

  private implicit lazy val msgs: Messages =
    messages(application)

  private lazy val view =
    application.injector.instanceOf[UnmatchedSubcontractorDetailsUpdatedView]

  private lazy val rows =
    Seq(
      Seq(
        TableRow(
          content = Text("Add UTR?"),
          classes = "govuk-!-font-weight-bold"
        ),
        TableRow(
          content = Text("No")
        ),
        TableRow(
          content = Text("Yes")
        )
      ),
      Seq(
        TableRow(
          content = Text("UTR"),
          classes = "govuk-!-font-weight-bold"
        ),
        TableRow(
          content = Text(
            msgs("unmatched.unmatchedSubcontractorDetailsUpdated.noneProvided")
          )
        ),
        TableRow(
          content = Text(utr)
        )
      )
    )

  private def renderView(
    returnUrl: String,
    returnTextKey: String,
    showBeforeYouGo: Boolean
  ): Document = {

    implicit val request =
      FakeRequest(GET, "/")

    Jsoup.parse(
      view(
        rows = rows,
        subcontractorName = subcontractorName,
        returnUrl = returnUrl,
        returnTextKey = returnTextKey,
        showBeforeYouGo = showBeforeYouGo
      ).toString()
    )
  }

  private def linkWithText(
    document: Document,
    text: String
  ): Element =
    document
      .select("a.govuk-link")
      .asScala
      .find(_.text() == text)
      .getOrElse(
        fail(s"Could not find link with text: $text")
      )

  private def assertLinkIsNotShown(
    document: Document,
    text: String
  ): Unit =
    document
      .select("a.govuk-link")
      .asScala
      .exists(_.text() == text) mustBe false

  private def assertCommonContent(
    document: Document
  ): Unit = {

    document.select(".govuk-panel__title").text() mustEqual
      msgs("unmatched.unmatchedSubcontractorDetailsUpdated.title")

    document.text() must include(
      msgs(
        "unmatched.unmatchedSubcontractorDetailsUpdated.p1",
        subcontractorName
      )
    )

    document.text() must include(
      msgs("unmatched.unmatchedSubcontractorDetailsUpdated.updatesMade.h2")
    )

    document.select("th").text() must include(
      msgs("unmatched.unmatchedSubcontractorDetailsUpdated.table.hdr.details")
    )

    document.select("th").text() must include(
      msgs("unmatched.unmatchedSubcontractorDetailsUpdated.table.hdr.previous")
    )

    document.select("th").text() must include(
      msgs("unmatched.unmatchedSubcontractorDetailsUpdated.table.hdr.updated")
    )

    document.text() must include("Add UTR?")
    document.text() must include("No")
    document.text() must include("Yes")
    document.text() must include("UTR")
    document.text() must include(utr)

    document.text() must include(
      msgs("unmatched.unmatchedSubcontractorDetailsUpdated.noneProvided")
    )
  }

  private def assertBeforeYouGoHidden(
    document: Document
  ): Unit = {

    document.text() must not include
      msgs("unmatched.unmatchedSubcontractorDetailsUpdated.beforeYouGo.h2")

    document.text() must not include
      msgs("unmatched.unmatchedSubcontractorDetailsUpdated.beforeYouGo.p1")

    document.text() must not include
      msgs("unmatched.unmatchedSubcontractorDetailsUpdated.beforeYouGo.takeAShortSurvey")

    document.text() must not include
      msgs("unmatchedSubcontractorDetailsUpdated.beforeYouGo.shareFeedback")
  }

  "UnmatchedSubcontractorDetailsUpdatedView" - {

    "must render the Cannot verify all subcontractors journey" in {

      val document =
        renderView(
          returnUrl = "#",
          returnTextKey = "unmatched.unmatchedSubcontractorDetailsUpdated.cannotVerifyAllSubcontractors",
          showBeforeYouGo = false
        )

      assertCommonContent(document)

      val cannotVerifyLink =
        linkWithText(
          document,
          msgs("unmatched.unmatchedSubcontractorDetailsUpdated.cannotVerifyAllSubcontractors")
        )

      cannotVerifyLink.attr("href") mustEqual "#"

      assertLinkIsNotShown(
        document,
        msgs("unmatched.unmatchedSubcontractorDetailsUpdated.reviewUnmatchedSubcontractors")
      )

      assertLinkIsNotShown(
        document,
        msgs("unmatched.unmatchedSubcontractorDetailsUpdated.yourSubcontractors")
      )

      assertLinkIsNotShown(
        document,
        msgs("unmatched.unmatchedSubcontractorDetailsUpdated.beforeYouGo.takeAShortSurvey")
      )

      assertBeforeYouGoHidden(document)
    }

    "must render the Review unmatched subcontractors journey" in {

      val document =
        renderView(
          returnUrl = "#",
          returnTextKey = "unmatched.unmatchedSubcontractorDetailsUpdated.reviewUnmatchedSubcontractors",
          showBeforeYouGo = false
        )

      assertCommonContent(document)

      val reviewUnmatchedLink =
        linkWithText(
          document,
          msgs("unmatched.unmatchedSubcontractorDetailsUpdated.reviewUnmatchedSubcontractors")
        )

      reviewUnmatchedLink.attr("href") mustEqual "#"

      assertLinkIsNotShown(
        document,
        msgs("unmatched.unmatchedSubcontractorDetailsUpdated.cannotVerifyAllSubcontractors")
      )

      assertLinkIsNotShown(
        document,
        msgs("unmatched.unmatchedSubcontractorDetailsUpdated.yourSubcontractors")
      )

      assertLinkIsNotShown(
        document,
        msgs("unmatched.unmatchedSubcontractorDetailsUpdated.beforeYouGo.takeAShortSurvey")
      )

      assertBeforeYouGoHidden(document)
    }

    "must render the Your subcontractors journey" in {

      val manageUrl =
        "/construction-industry-scheme/subcontractor/manage/12345"

      val document =
        renderView(
          returnUrl = manageUrl,
          returnTextKey = "unmatched.unmatchedSubcontractorDetailsUpdated.yourSubcontractors",
          showBeforeYouGo = true
        )

      assertCommonContent(document)

      val yourSubcontractorsLink =
        linkWithText(
          document,
          msgs("unmatched.unmatchedSubcontractorDetailsUpdated.yourSubcontractors")
        )

      yourSubcontractorsLink.attr("href") mustEqual manageUrl

      val surveyLink =
        linkWithText(
          document,
          msgs("unmatched.unmatchedSubcontractorDetailsUpdated.beforeYouGo.takeAShortSurvey")
        )

      surveyLink.attr("href") mustEqual "#"

      assertLinkIsNotShown(
        document,
        msgs("unmatched.unmatchedSubcontractorDetailsUpdated.cannotVerifyAllSubcontractors")
      )

      assertLinkIsNotShown(
        document,
        msgs("unmatched.unmatchedSubcontractorDetailsUpdated.reviewUnmatchedSubcontractors")
      )

      document.text() must include(
        msgs("unmatched.unmatchedSubcontractorDetailsUpdated.beforeYouGo.h2")
      )

      document.text() must include(
        msgs("unmatched.unmatchedSubcontractorDetailsUpdated.beforeYouGo.p1")
      )

      document.text() must include(
        msgs("unmatched.unmatchedSubcontractorDetailsUpdated.beforeYouGo.shareFeedback")
      )
    }
  }
}
