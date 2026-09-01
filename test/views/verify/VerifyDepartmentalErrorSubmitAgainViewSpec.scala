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
import play.api.test.Helpers.*
import views.html.verify.VerifyDepartmentalErrorSubmitAgainView

class VerifyDepartmentalErrorSubmitAgainViewSpec extends SpecBase {

  private val manageSubcontractorsUrl =
    "/construction-industry-scheme/manage-subcontractors/test-cis-id"

  private def renderView(): Document = {

    val application =
      applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .build()

    running(application) {

      implicit val request: Request[?] =
        FakeRequest(GET, "/")

      implicit val msgs: Messages =
        messages(application)

      val view =
        application.injector
          .instanceOf[VerifyDepartmentalErrorSubmitAgainView]

      Jsoup.parse(
        view(manageSubcontractorsUrl).toString
      )
    }
  }

  "verifyDepartmentalErrorSubmitAgainView" - {

    "must display the correct browser title" in {

      val document = renderView()

      document.title mustBe
        "There was a problem with your verification request - Construction Industry Scheme - GOV.UK"
    }

    "must display the correct heading" in {

      val document = renderView()

      document.select("h1").text mustBe
        "There was a problem with your verification request"
    }

    "must display only one h1" in {

      val document = renderView()

      document.select("h1").size mustBe 1
    }

    "must display the first paragraph" in {

      val document = renderView()

      document.select("main").text must include(
        "The subcontractors that you selected have not been verified and your request has not been saved."
      )
    }

    "must display the second paragraph" in {

      val document = renderView()

      document.select("main").text must include(
        "To verify these subcontractors, you need to start again with a new verification request."
      )
    }

    "must display the Manage your subcontractors link" in {

      val document = renderView()

      val link =
        document.select(
          s"""a[href="$manageSubcontractorsUrl"]"""
        )

      link.size mustBe 1

      link.text mustBe
        "Manage your subcontractors"
    }

    "must set the correct Manage your subcontractors link URL" in {

      val document = renderView()

      document
        .select(s"""a[href="$manageSubcontractorsUrl"]""")
        .attr("href") mustBe manageSubcontractorsUrl
    }

    "must display a full stop after the Manage your subcontractors link" in {

      val document = renderView()

      document.select("main").text must include(
        "Back to Manage your subcontractors."
      )
    }

    "must not display a back link" in {

      val document = renderView()

      document.select(".govuk-back-link").isEmpty mustBe true
    }
  }
}
