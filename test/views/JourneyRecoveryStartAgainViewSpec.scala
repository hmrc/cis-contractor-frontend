package views

import base.SpecBase
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers
import play.api.i18n.Messages
import play.api.test.FakeRequest
import views.html.JourneyRecoveryStartAgainView

class JourneyRecoveryStartAgainViewSpec extends SpecBase with Matchers {

  "JourneyRecoveryStartAgainView" - {

    Seq(
      ("AGENT", applicationConfig.constructionIndustryAgentAccountUrl + "1"),
      ("ORGANISATION", applicationConfig.constructionIndustryOrgAccountUrl)
    ).foreach { case (accountTypeSTR, cisAccountURL) =>
      s"when accountType is '$accountTypeSTR'" - {

        "must render the page with the correct heading and paragraph with link" in new Setup {
          val html = view(cisAccountURL)
          val doc  = Jsoup.parse(html.body)

          doc.title                                 must include(messages("journeyRecovery.startAgain.title"))
          doc.select("h1").text                     must include(messages("journeyRecovery.startAgain.heading"))
          doc.select("p").text                      must include(messages("journeyRecovery.startAgain.guidance.p1"))
          doc.select("p").text                      must include(messages("journeyRecovery.startAgain.guidance.p2"))
          doc.select("p").text                      must include(messages("journeyRecovery.startAgain.guidance.contactHMRC.suffix"))
          doc.getElementsByClass("govuk-link").text must include(
            messages("journeyRecovery.startAgain.guidance.contactHMRC.link")
          )
          doc.select("p").text                      must include(messages("journeyRecovery.startAgain.guidance.cisAccount.prefix"))
          doc.getElementsByClass("govuk-link").text must include(
            messages("journeyRecovery.startAgain.guidance.cisAccount.link")
          )
        }
      }
    }
  }

  trait Setup {
    val app                                       = applicationBuilder().build()
    val view                                      = app.injector.instanceOf[JourneyRecoveryStartAgainView]
    implicit val request: play.api.mvc.Request[_] = FakeRequest()
    implicit val messages: Messages               = play.api.i18n.MessagesImpl(
      play.api.i18n.Lang.defaultLang,
      app.injector.instanceOf[play.api.i18n.MessagesApi]
    )
  }
}