

package controllers.add.partnership

import base.SpecBase
import controllers.routes
import forms.add.partnership.PartnershipNominatedPartnerCrnFormProvider
import models.{NormalMode, UserAnswers}
import navigation.Navigator
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, verifyNoMoreInteractions, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.add.partnership.{PartnershipNominatedPartnerCrnPage, PartnershipNominatedPartnerNamePage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.SubcontractorService
import uk.gov.hmrc.http.HeaderCarrier
import views.html.add.partnership.PartnershipNominatedPartnerCrnView

import scala.concurrent.duration.*
import scala.concurrent.{Await, Future}

class PartnershipNominatedPartnerCrnControllerSpec extends SpecBase with MockitoSugar {

  private val formProvider = new PartnershipNominatedPartnerCrnFormProvider()
  private val form         = formProvider()

  private val nominatedPartnerName = "Jane Doe"

  private lazy val getUrl =
    controllers.add.partnership.routes.PartnershipNominatedPartnerCrnController
      .onPageLoad(NormalMode)
      .url

  private lazy val postUrl =
    controllers.add.partnership.routes.PartnershipNominatedPartnerCrnController
      .onSubmit(NormalMode)
      .url

  "PartnershipNominatedPartnerCrnController" - {

    "must return OK and the correct view for a GET" in {
      val ua =
        emptyUserAnswers
          .set(PartnershipNominatedPartnerNamePage, nominatedPartnerName)
          .success
          .value

      val application = applicationBuilder(userAnswers = Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, getUrl)
        val result  = route(application, request).value

        val view = application.injector.instanceOf[PartnershipNominatedPartnerCrnView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, nominatedPartnerName)(
          request,
          messages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val ua =
        UserAnswers(userAnswersId)
          .set(PartnershipNominatedPartnerNamePage, nominatedPartnerName)
          .success
          .value
          .set(PartnershipNominatedPartnerCrnPage, "AC012345")
          .success
          .value

      val application = applicationBuilder(userAnswers = Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, getUrl)
        val result  = route(application, request).value

        val view = application.injector.instanceOf[PartnershipNominatedPartnerCrnView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill("AC012345"), NormalMode, nominatedPartnerName)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect when valid data is submitted" in {
      val mockSessionRepository    = mock[SessionRepository]
      val mockSubcontractorService = mock[SubcontractorService]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
      when(mockSubcontractorService.updateSubcontractor(any[UserAnswers])(any[HeaderCarrier])) thenReturn Future.successful(())

      val application =
        applicationBuilder(
          userAnswers = Some(
            emptyUserAnswers.set(PartnershipNominatedPartnerNamePage, nominatedPartnerName).success.value
          )
        )
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[SubcontractorService].toInstance(mockSubcontractorService),
            bind[Navigator].toInstance(new Navigator())
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, postUrl)
            .withFormUrlEncodedBody("value" -> "ac 012 345")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.add.routes.CheckYourAnswersController.onPageLoad().url
      }

      verify(mockSessionRepository).set(any())
      verify(mockSubcontractorService).updateSubcontractor(any[UserAnswers])(any[HeaderCarrier])
      verifyNoMoreInteractions(mockSubcontractorService)
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      val ua =
        emptyUserAnswers
          .set(PartnershipNominatedPartnerNamePage, nominatedPartnerName)
          .success
          .value

      val application = applicationBuilder(userAnswers = Some(ua)).build()

      running(application) {
        val request =
          FakeRequest(POST, postUrl)
            .withFormUrlEncodedBody("value" -> "INVALID-CRN")

        val boundForm = form.bind(Map("value" -> "INVALID-CRN"))
        val view      = application.injector.instanceOf[PartnershipNominatedPartnerCrnView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, nominatedPartnerName)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, getUrl)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, postUrl)
            .withFormUrlEncodedBody("value" -> "AC012345")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must throw RuntimeException on a GET when nominated partner name is missing" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, getUrl)
        val result  = route(application, request).value

        val ex = intercept[RuntimeException] {
          Await.result(result, 5.seconds)
        }

        ex.getMessage mustBe "Missing nominated partner name"
      }
    }

    "must throw RuntimeException on a POST when nominated partner name is missing" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, postUrl)
            .withFormUrlEncodedBody("value" -> "AC012345")

        val result = route(application, request).value

        val ex = intercept[RuntimeException] {
          Await.result(result, 5.seconds)
        }

        ex.getMessage mustBe "Missing nominated partner name"
      }
    }
  }
}
