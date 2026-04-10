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
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalatest.matchers.must.Matchers
import play.api.Application
import play.twirl.api.Html
import views.html.components.FullWidthMainContent

class FullWidthMainContentSpec extends SpecBase with Matchers with ScalaCheckPropertyChecks {


  private def expectedHtml(content: String) =
    s"""
       |<div class="govuk-grid-row">
       |    <div class="govuk-grid-column-full">
       |        $content
       |    </div>
       |</div>
       |""".stripMargin

  "FullWidthMainContent" - {
    "render as expected when given a contentBlock" in new Setup {
      val content = """<h1 class="govuk-heading-xl">Page heading</h1><p class="govuk-body">Some page content</p>"""
      val component: FullWidthMainContent = fullWidthMainContent
      component(Html(content)) mustBe Html(expectedHtml(content))
    }

    "render an empty contentBlock" in new Setup {
      val component: FullWidthMainContent = fullWidthMainContent
      component(Html("")) mustBe Html(expectedHtml(""))
    }

    "have all template methods implemented" in new Setup {

      val component: FullWidthMainContent = fullWidthMainContent
      forAll {
        (contentBlock: String) =>
          component.render(Html(contentBlock)) mustBe component.ref.f(Html(contentBlock))
      }
    }
  }

  trait Setup {
    val app: Application = applicationBuilder().build()
    val fullWidthMainContent: FullWidthMainContent = app.injector.instanceOf[FullWidthMainContent]
  }
}
