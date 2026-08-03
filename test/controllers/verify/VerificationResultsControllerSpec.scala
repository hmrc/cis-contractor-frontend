package controllers.verify

import base.SpecBase
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import viewmodels.verify.VerificationResultsViewModel
import views.html.verify.VerificationResultsView

class VerificationResultsControllerSpec extends SpecBase {

  "VerificationResults Controller" - {

    "must return OK and the correct view for a GET" in {
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

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.verify.routes.VerificationResultsController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[VerificationResultsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(verificationResults)(request, messages(application)).toString
      }
    }
  }
}
