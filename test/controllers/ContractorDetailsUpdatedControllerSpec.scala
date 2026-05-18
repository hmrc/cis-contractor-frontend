package controllers

import base.SpecBase
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.ContractorDetailsUpdatedView

class ContractorDetailsUpdatedControllerSpec extends SpecBase {

  "ContractorDetailsUpdated Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.ContractorDetailsUpdatedController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ContractorDetailsUpdatedView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(contractorName = "Test Contractor")(
          request,
          messages(application)
        ).toString
      }
    }
  }
}
