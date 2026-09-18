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

package views.insufficient

import base.SpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import play.api.i18n.Messages
import play.api.test.FakeRequest
import play.api.test.Helpers.GET
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.TableRow
import views.html.insufficient.InsufficientSubcontractorDetailsUpdatedView

import scala.jdk.CollectionConverters.CollectionHasAsScala

class InsufficientSubcontractorDetailsUpdatedViewSpec extends SpecBase {

  private val subcontractorName = "Martin Brody"

  private val utr = "3992651526"

  private lazy val application =
    applicationBuilder().build()

  private implicit lazy val msgs: Messages =
    messages(application)

  private lazy val view =
    application.injector.instanceOf[InsufficientSubcontractorDetailsUpdatedView]

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
            msgs("insufficientSubcontractorDetailsUpdated.noneProvided")
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
        subcontractorName = Some(subcontractorName),
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
      msgs("insufficientSubcontractorDetailsUpdated.title")

    document.text() must include(
      msgs(
        "insufficientSubcontractorDetailsUpdated.p1",
        subcontractorName
      )
    )

    document.text() must include(
      msgs("insufficientSubcontractorDetailsUpdated.updatesMade.h2")
    )

    document.select("th").text() must include(
      msgs("insufficientSubcontractorDetailsUpdated.table.hdr.details")
    )

    document.select("th").text() must include(
      msgs("insufficientSubcontractorDetailsUpdated.table.hdr.previous")
    )

    document.select("th").text() must include(
      msgs("insufficientSubcontractorDetailsUpdated.table.hdr.updated")
    )

    document.text() must include("Add UTR?")
    document.text() must include("No")
    document.text() must include("Yes")
    document.text() must include("UTR")
    document.text() must include(utr)

    document.text() must include(
      msgs("insufficientSubcontractorDetailsUpdated.noneProvided")
    )
  }

  private def assertBeforeYouGoHidden(
    document: Document
  ): Unit = {

    document.text() must not include
      msgs("insufficientSubcontractorDetailsUpdated.beforeYouGo.h2")

    document.text() must not include
      msgs("insufficientSubcontractorDetailsUpdated.beforeYouGo.p1")

    document.text() must not include
      msgs("insufficientSubcontractorDetailsUpdated.beforeYouGo.takeAShortSurvey")

    document.text() must not include
      msgs("insufficientSubcontractorDetailsUpdated.beforeYouGo.shareFeedback")
  }

  "InsufficientSubcontractorDetailsUpdatedView" - {

    "must render the Cannot verify all subcontractors journey" in {

      val document =
        renderView(
          returnUrl = "#",
          returnTextKey = "insufficientSubcontractorDetailsUpdated.cannotVerifyAllSubcontractors",
          showBeforeYouGo = false
        )

      assertCommonContent(document)

      val cannotVerifyLink =
        linkWithText(
          document,
          msgs(
            "insufficientSubcontractorDetailsUpdated.cannotVerifyAllSubcontractors"
          )
        )

      cannotVerifyLink.attr("href") mustEqual "#"

      assertLinkIsNotShown(
        document,
        msgs(
          "insufficientSubcontractorDetailsUpdated.reviewUnmatchedSubcontractors"
        )
      )

      assertLinkIsNotShown(
        document,
        msgs(
          "insufficientSubcontractorDetailsUpdated.yourSubcontractors"
        )
      )

      assertBeforeYouGoHidden(document)
    }

    "must render the Review unmatched subcontractors journey" in {

      val reviewUnmatchedUrl =
        controllers.routes.UnmatchedSubcontractorsController
          .onPageLoad()
          .url

      val document =
        renderView(
          returnUrl = reviewUnmatchedUrl,
          returnTextKey = "insufficientSubcontractorDetailsUpdated.reviewUnmatchedSubcontractors",
          showBeforeYouGo = false
        )

      assertCommonContent(document)

      val reviewUnmatchedLink =
        linkWithText(
          document,
          msgs(
            "insufficientSubcontractorDetailsUpdated.reviewUnmatchedSubcontractors"
          )
        )

      reviewUnmatchedLink.attr("href") mustEqual reviewUnmatchedUrl

      assertLinkIsNotShown(
        document,
        msgs(
          "insufficientSubcontractorDetailsUpdated.cannotVerifyAllSubcontractors"
        )
      )

      assertLinkIsNotShown(
        document,
        msgs(
          "insufficientSubcontractorDetailsUpdated.yourSubcontractors"
        )
      )

      assertBeforeYouGoHidden(document)
    }

    "must render the Your subcontractors journey" in {

      val manageUrl =
        "/construction-industry-scheme/subcontractor/manage/12345"

      val document =
        renderView(
          returnUrl = manageUrl,
          returnTextKey = "insufficientSubcontractorDetailsUpdated.yourSubcontractors",
          showBeforeYouGo = true
        )

      assertCommonContent(document)

      val yourSubcontractorsLink =
        linkWithText(
          document,
          msgs(
            "insufficientSubcontractorDetailsUpdated.yourSubcontractors"
          )
        )

      yourSubcontractorsLink.attr("href") mustEqual manageUrl

      val surveyLink =
        linkWithText(
          document,
          msgs(
            "insufficientSubcontractorDetailsUpdated.beforeYouGo.takeAShortSurvey"
          )
        )

      surveyLink.attr("href") mustEqual "#"

      assertLinkIsNotShown(
        document,
        msgs(
          "insufficientSubcontractorDetailsUpdated.cannotVerifyAllSubcontractors"
        )
      )

      assertLinkIsNotShown(
        document,
        msgs(
          "insufficientSubcontractorDetailsUpdated.reviewUnmatchedSubcontractors"
        )
      )

      document.text() must include(
        msgs("insufficientSubcontractorDetailsUpdated.beforeYouGo.h2")
      )

      document.text() must include(
        msgs("insufficientSubcontractorDetailsUpdated.beforeYouGo.p1")
      )

      document.text() must include(
        msgs("insufficientSubcontractorDetailsUpdated.beforeYouGo.shareFeedback")
      )
    }

    "must not render updates heading or table when there are no updates" in {

      implicit val request =
        FakeRequest(GET, "/")

      val document =
        Jsoup.parse(
          view(
            rows = Seq.empty,
            subcontractorName = Some(subcontractorName),
            returnUrl = "#",
            returnTextKey = "insufficientSubcontractorDetailsUpdated.cannotVerifyAllSubcontractors",
            showBeforeYouGo = false
          ).toString()
        )

      document.text() must not include
        msgs("insufficientSubcontractorDetailsUpdated.updatesMade.h2")

      document.select("table").isEmpty mustBe true
    }

    "must render the no-name message when subcontractor name is not available" in {

      implicit val request =
        FakeRequest(GET, "/")

      val document =
        Jsoup.parse(
          view(
            rows = rows,
            subcontractorName = None,
            returnUrl = "#",
            returnTextKey = "insufficientSubcontractorDetailsUpdated.cannotVerifyAllSubcontractors",
            showBeforeYouGo = false
          ).toString()
        )

      document.text() must include(
        msgs("insufficientSubcontractorDetailsUpdated.noName")
      )

      document.text() must not include (
        msgs(
          "insufficientSubcontractorDetailsUpdated.p1",
          subcontractorName
        )
      )
    }
  }
}
