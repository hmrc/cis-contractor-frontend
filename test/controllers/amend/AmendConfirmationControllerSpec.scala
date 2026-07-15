package controllers.amend

import base.SpecBase
import models.amend.trust.OriginalTrustAnswers
import pages.add.trust.TrustNamePage
import play.api.test.FakeRequest
import play.api.test.Helpers.{route, *}
import queries.{CisIdQuery, OriginalTrustAnswersQuery}
import viewmodels.amend.TrustAmendConfirmationViewModel
import views.html.amend.AmendConfirmationView

class AmendConfirmationControllerSpec extends SpecBase {

  private val original =
    OriginalTrustAnswers(
      trustName = Some("ABC Trust"),
      addressYesNo = None,
      address = None,
      trustContactMethodsYesNo = None,
      trustContactMethod = Set.empty,
      email = None,
      phone = None,
      mobile = None,
      utrYesNo = None,
      utr = None,
      worksReferenceYesNo = None,
      worksReference = None
    )

  private def userAnswersWithOriginal =
    emptyUserAnswers
      .set(OriginalTrustAnswersQuery, original)
      .success
      .value
      .set(CisIdQuery, "123456789")
      .success
      .value
      .set(TrustNamePage, "ABC Trust")
      .success
      .value

  private lazy val confirmationRoute =
    controllers.amend.routes.AmendConfirmationController.trustOnPageLoad().url

  "AmendConfirmationController" - {

    "must return OK and the correct view for a GET" in {

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithOriginal)).build()

      running(application) {

        val request = FakeRequest(GET, confirmationRoute)
        val result = route(application, request).value

        val view = application.injector.instanceOf[AmendConfirmationView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(
            TrustAmendConfirmationViewModel.rows(
              original,
              userAnswersWithOriginal
            )(messages(application)),
            "ABC Trust",
            application
              .injector
              .instanceOf[config.FrontendAppConfig]
              .manageYourSubcontractorsUrl("/dummy/url")
          )(
            request,
            messages(application)
          ).toString
      }
    }

    "must redirect to Journey Recovery when the original answers are missing" in {

      val userAnswers =
        emptyUserAnswers
          .set(CisIdQuery, "123456789")
          .success
          .value
          .set(TrustNamePage, "ABC Trust")
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {

        val request = FakeRequest(GET, confirmationRoute)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery when the CIS id is missing" in {

      val userAnswers =
        emptyUserAnswers
          .set(OriginalTrustAnswersQuery, original)
          .success
          .value
          .set(TrustNamePage, "ABC Trust")
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {

        val request = FakeRequest(GET, confirmationRoute)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
