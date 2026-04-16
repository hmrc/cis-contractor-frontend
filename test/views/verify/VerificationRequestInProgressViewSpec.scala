package views.verify

import config.FrontendAppConfig
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import play.api.mvc.Request
import play.api.test.FakeRequest
import views.html.verify.VerificationRequestInProgressView

class VerificationRequestInProgressViewSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite {

  implicit val request: Request[_] =
    FakeRequest()

  implicit val messages: Messages =
    MessagesImpl(
      Lang.defaultLang,
      app.injector.instanceOf[MessagesApi]
    )

  implicit val appConfig: FrontendAppConfig =
    app.injector.instanceOf[FrontendAppConfig]

  val view: VerificationRequestInProgressView =
    app.injector.instanceOf[VerificationRequestInProgressView]

  "VerificationRequestInProgressView" should {

    "render the page with correct title, heading, paragraphs and links" in {

      val html = view()
      val doc  = Jsoup.parse(html.toString())

      doc.select("title").text() must include(
        messages("verificationRequestInProgress.title")
      )

      doc.select("h1").size() mustBe 1
      doc.select("h1").text() mustBe
        messages("verify.verificationRequestInProgress.heading")

      doc.select("p.govuk-body").text() must include(
        messages("verify.verificationRequestInProgress.p1")
      )

      doc.select("p.govuk-body").text() must include(
        messages("verify.verificationRequestInProgress.p2")
      )

      val serviceDeskLink =
        doc.select(s"a[href='${appConfig.hmrcOnlineServiceDeskUrl}']")
      serviceDeskLink.size() mustBe 1
      serviceDeskLink.text() mustBe
        messages("verify.verificationRequestInProgress.p3.link")

      serviceDeskLink.first().parent().text() must include(
        messages("verify.verificationRequestInProgress.p3")
      )

      val manageSubcontractorsLink =
        doc.select(s"a[href='${appConfig.manageSubcontractorsUrl}']")
      manageSubcontractorsLink.size() mustBe 1
      manageSubcontractorsLink.text() mustBe
        messages("verify.verificationRequestInProgress.p4.link")

      manageSubcontractorsLink.first().parent().text() must include(
        messages("verify.verificationRequestInProgress.p4")
      )
    }
  }
}
