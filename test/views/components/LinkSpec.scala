/*
 * Copyright 2025 HM Revenue & Customs
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
import org.jsoup.Jsoup
import org.jsoup.select.Elements
import org.scalatest.matchers.must.Matchers
import play.api.i18n.Messages
import play.api.test.FakeRequest
import views.html.components.Link

class LinkSpec extends SpecBase with Matchers {

  "link" - {

    "must render the correct link text in the output HTML" in new Setup {
      val html        = link(linkText = linkText, linkUrl = linkUrl, prefixTextKey = prefixText)
      val linkElement = getLinkElement(html)

      linkElement.size mustBe 1
      linkElement.text mustBe prefixText + " " + linkText
    }

    "must render the correct link text in the output HTML with an empty prefixText" in new Setup {
      val html        = link(linkText = linkText, linkUrl = linkUrl)
      val linkElement = getLinkElement(html)

      linkElement.size mustBe 1
      linkElement.text mustBe linkText
    }

    "must render the link with the correct suffix" in new Setup {
      val html        = link(
        linkText = linkText,
        linkUrl = linkUrl,
        suffixTextKey = suffixText
      )
      val linkElement = getLinkElement(html)

      linkElement.size mustBe 1
      linkElement.text mustBe linkText + " " + suffixText
    }

    "must render the link with the correct prefix and suffix" in new Setup {
      val html        = link(
        linkText = linkText,
        linkUrl = linkUrl,
        prefixTextKey = prefixText,
        suffixTextKey = suffixText
      )
      val linkElement = getLinkElement(html)

      linkElement.size mustBe 1
      linkElement.text mustBe prefixText + " " + linkText + " " + suffixText
    }

    "must render with default class when extraClasses are empty" in new Setup {
      val html        = link(
        linkText = linkText,
        linkUrl = linkUrl,
        prefixTextKey = prefixText,
        extraClasses = ""
      )
      val linkElement = getLinkElement(html)
      val classes     = linkElement.attr("class")

      classes.trim mustBe "govuk-body"
    }

    "must render with default and extra classes when extraClasses are non-empty" in new Setup {
      val html        = link(
        linkText = linkText,
        linkUrl = linkUrl,
        prefixTextKey = prefixText,
        extraClasses = extraClasses
      )
      val linkElement = getLinkElement(html)
      val classes     = linkElement.attr("class")

      classes.trim mustBe s"govuk-body $extraClasses"
    }

    "must render the correct link URL in the output HTML" in new Setup {
      val html                     = link(
        linkText = linkText,
        linkUrl = linkUrl,
        prefixTextKey = prefixText
      )
      val linkRefElement: Elements = getLinkRefElement(html)

      linkRefElement.attr("href") mustBe linkUrl
    }

    "must render the output HTML with no rel and target attributes if isNewTab is false" in new Setup {
      val html                     = link(
        linkText = linkText,
        linkUrl = linkUrl
      )
      val linkRefElement: Elements = getLinkRefElement(html)

      linkRefElement.hasAttr("rel") mustBe false
      linkRefElement.hasAttr("target") mustBe false
    }

    "must render the output HTML with rel and target attributes if isNewTab is true" in new Setup {
      val html                     = link(
        linkText = linkText,
        linkUrl = linkUrl,
        isNewTab = true,
        prefixTextKey = prefixText
      )
      val linkRefElement: Elements = getLinkRefElement(html)

      linkRefElement.hasAttr("rel") mustBe true
      linkRefElement.hasAttr("target") mustBe true

      linkRefElement.attr("rel")    must include("noreferrer noopener")
      linkRefElement.attr("target") must include("_blank")
    }

    "must render the link with the full stop if true" in new Setup {
      val html        = link(
        linkText = linkText,
        linkUrl = linkUrl,
        prefixTextKey = prefixText,
        hasFullStop = true
      )
      val linkElement = getLinkElement(html)

      linkElement.size mustBe 1
      linkElement.text mustBe prefixText + " " + linkText + "."
    }

    "must render the provided linkText" in new Setup {
      val html        = link(
        linkText = linkText,
        linkUrl = linkUrl
      )
      val linkElement = getLinkRefElement(html)

      linkElement.size mustBe 1
      linkElement.text mustBe linkText
    }

    "must render visually hidden text when visuallyHiddenText is supplied" in new Setup {
      val hiddenText = "for Client ABC"

      val html = link(
        linkText = linkText,
        linkUrl = linkUrl,
        visuallyHiddenText = hiddenText
      )

      val doc           = Jsoup.parse(html.body)
      val hiddenElement = doc.select("a .govuk-visually-hidden")

      hiddenElement.size mustBe 1
      hiddenElement.text mustBe hiddenText
    }

    "must render visible and visually hidden text together" in new Setup {
      val hiddenText = "for Client ABC"

      val html = link(
        linkText = linkText,
        linkUrl = linkUrl,
        visuallyHiddenText = hiddenText
      )

      val linkElement = getLinkRefElement(html)

      linkElement.size mustBe 1
      linkElement.text mustBe s"$linkText $hiddenText"
    }

    "must not render visually hidden text when it is empty" in new Setup {
      val html = link(
        linkText = linkText,
        linkUrl = linkUrl
      )

      val doc = Jsoup.parse(html.body)

      doc.select("a .govuk-visually-hidden").size mustBe 0
    }

    "must render link text from linkTextKey when linkText is not supplied" in new Setup {
      val html = link(
        linkTextKey = "site.continue",
        linkUrl = linkUrl
      )

      val linkElement = getLinkRefElement(html)

      linkElement.size mustBe 1
      linkElement.text mustBe messages("site.continue")
    }

    "must prefer linkText over linkTextKey when both are supplied" in new Setup {
      val html = link(
        linkTextKey = "site.continue",
        linkText = linkText,
        linkUrl = linkUrl
      )

      val linkElement = getLinkRefElement(html)

      linkElement.size mustBe 1
      linkElement.text mustBe linkText
    }

    "must escape HTML in linkText" in new Setup {
      val unsafeText = "<script>alert('test')</script>"

      val html = link(
        linkText = unsafeText,
        linkUrl = linkUrl
      )

      val doc         = Jsoup.parse(html.body)
      val linkElement = doc.select("a")

      linkElement.text mustBe unsafeText
      linkElement.select("script").size mustBe 0
    }
  }

  trait Setup {

    val app  = applicationBuilder().build()
    val link = app.injector.instanceOf[Link]

    implicit val request: play.api.mvc.Request[_] = FakeRequest()

    implicit val messages: Messages =
      play.api.i18n.MessagesImpl(
        play.api.i18n.Lang.defaultLang,
        app.injector.instanceOf[play.api.i18n.MessagesApi]
      )

    val linkText     = "link text"
    val linkUrl      = "https://www.gov.uk/find-hmrc-contacts/technical-support-with-hmrc-online-services"
    val prefixText   = "Jump to"
    val suffixText   = "After link text"
    val extraClasses = "govuk-link--inverse govuk-link--no-underline"

    def getLinkElement(html: play.twirl.api.Html): Elements = {
      val doc = Jsoup.parse(html.body)
      doc.select("p")
    }

    def getLinkRefElement(html: play.twirl.api.Html): Elements = {
      val doc = Jsoup.parse(html.body)
      doc.select("a")
    }
  }
}
