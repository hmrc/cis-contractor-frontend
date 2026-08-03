package views.verify

import base.SpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import viewmodels.verify.VerificationResultsViewModel
import views.html.verify.VerificationResultsView

import java.util

class VerificationResultsViewSpec extends SpecBase {

  "VerificationResultsView" - {

    "must return the header, paragraph and table with the verification results list" in new Setup {
      val html: HtmlFormat.Appendable = view(verificationResults)
      val doc: Document = Jsoup.parse(html.body)

      doc.select("title").text() must include(messages("verify.verificationResults.title"))
      doc.select("h1").text() must include(messages("verify.verificationResults.heading"))
      doc.select("p").text() must include(messages("verify.verificationResults.paragraph"))

      val headers: util.List[String] = doc.select("thead th").eachText()

      headers mustBe util.Arrays.asList(
        messages("verify.verificationResults.name"),
        messages("verify.verificationResults.status"),
        messages("verify.verificationResults.taxTreatment"),
        messages("verify.verificationResults.verificationNumber")
      )

      val rows: Elements = doc.select("tbody tr")
      rows.size() mustBe verificationResults.size

      verificationResults.zipWithIndex.foreach { case (result, index) =>
        val cells = rows.get(index).select("td").eachText()

        cells mustBe util.Arrays.asList(
          result.name,
          result.verificationStatus,
          result.taxTreatment,
          result.verificationNumber
        )
      }

      doc.select("button").text() mustBe
        messages("verify.reviewUnmatchedSubcontractors.button")
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

    val view: VerificationResultsView =
      app.injector.instanceOf[VerificationResultsView]

    val verificationResults = Seq(
      VerificationResultsViewModel(
        "Brody, Martin",
        "Unmatched",
        "Higher rate",
        "V0004528765/A"
      ),
      VerificationResultsViewModel(
        "Hooper and Associates",
        "Verified",
        "Standard rate",
        "V0004528765"
      ),
      VerificationResultsViewModel(
        "Quint Transportation",
        "Unmatched",
        "Higher rate",
        "V0004528765/B"
      ),
      VerificationResultsViewModel(
        "The Kintner Group",
        "Unmatched",
        "Higher rate",
        "V0004528765/C"
      )
    )
  }
}
