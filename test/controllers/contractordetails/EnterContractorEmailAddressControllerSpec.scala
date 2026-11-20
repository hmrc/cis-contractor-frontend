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

package controllers.contractordetails

import base.{SpecBase, WithFakeCisTaxpayer}
import controllers.routes
import forms.contractordetails.EnterContractorEmailAddressFormProvider
import models.{NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.contractordetails.{AddEmailAddressYesNoPage, EnterContractorEmailAddressPage}
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import queries.CisIdQuery
import repositories.SessionRepository
import views.html.contractordetails.EnterContractorEmailAddressView
import play.api.inject.Binding
import play.api.inject.guice.GuiceApplicationBuilder
import services.CisTaxpayerService

import java.util.UUID
import scala.concurrent.Future

class EnterContractorEmailAddressControllerSpec extends SpecBase with WithFakeCisTaxpayer with MockitoSugar {

  def onwardRoute: Call = Call("GET", "/foo")

  val formProvider       = new EnterContractorEmailAddressFormProvider()
  val form: Form[String] = formProvider()

  private val flowId =
    UUID.fromString("0f1af513-154f-4548-a24c-2d297f7b57be")

  private val cisId = "AB12"

  private def applicationWithCisTaxpayer(
    userAnswers: UserAnswers,
    additionalBindings: Seq[Binding[_]] = Nil
  ): GuiceApplicationBuilder =
    applicationBuilder(
      userAnswers = Some(userAnswers),
      additionalBindings = additionalBindings
    )

  lazy val enterContractorEmailAddressRoute: String =
    controllers.contractordetails.routes.EnterContractorEmailAddressController
      .onPageLoad(NormalMode)
      .url

  lazy val enterContractorEmailAddressRouteWithId: String =
    controllers.contractordetails.routes.EnterContractorEmailAddressController
      .onPageLoadWithId(flowId, NormalMode)
      .url

  val userAnswersForEmailPage: UserAnswers =
    emptyUserAnswers
      .set(AddEmailAddressYesNoPage, true)
      .success
      .value

  private val userAnswersWithCisIdForFlow: UserAnswers =
    emptyUserAnswers
      .set(CisIdQuery(flowId.toString), cisId)
      .success
      .value

  val userAnswersForEmailPageWithId: UserAnswers =
    userAnswersWithCisIdForFlow
      .set(AddEmailAddressYesNoPage(flowId.toString), true)
      .success
      .value

  "EnterContractorEmailAddress Controller" - {

    "must return OK and the correct view for a GET" in {
      val application =
        applicationBuilder(userAnswers = Some(userAnswersForEmailPage)).build()

      running(application) {
        val request = FakeRequest(GET, enterContractorEmailAddressRoute)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[EnterContractorEmailAddressView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual view(
          form,
          NormalMode,
          controllers.contractordetails.routes.EnterContractorEmailAddressController
            .onSubmit(NormalMode)
        )(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val userAnswers =
        userAnswersForEmailPage
          .set(EnterContractorEmailAddressPage, "test@example.com")
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, enterContractorEmailAddressRoute)
        val view    = application.injector.instanceOf[EnterContractorEmailAddressView]
        val result  = route(application, request).value

        status(result) mustEqual OK

        contentAsString(result) mustEqual view(
          form.fill("test@example.com"),
          NormalMode,
          controllers.contractordetails.routes.EnterContractorEmailAddressController
            .onSubmit(NormalMode)
        )(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {
      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, enterContractorEmailAddressRoute)
            .withFormUrlEncodedBody(("value", "test@example.com"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      val application =
        applicationWithCisTaxpayer(userAnswersForEmailPageWithId).build()

      running(application) {
        val request =
          FakeRequest(POST, enterContractorEmailAddressRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))
        val view      = application.injector.instanceOf[EnterContractorEmailAddressView]
        val result    = route(application, request).value

        status(result) mustEqual BAD_REQUEST

        contentAsString(result) mustEqual view(
          boundForm,
          NormalMode,
          controllers.contractordetails.routes.EnterContractorEmailAddressController
            .onSubmit(NormalMode)
        )(request, messages(application)).toString
      }
    }

    "must redirect to AddEmailAddressYesNo page on a GET when AddEmailAddressYesNoPage is not answered with true" in {
      val application =
        applicationWithCisTaxpayer(userAnswersWithCisIdForFlow).build()

      running(application) {
        val request = FakeRequest(GET, enterContractorEmailAddressRoute)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.contractordetails.routes.AddEmailAddressYesNoController
            .onPageLoad(NormalMode)
            .url
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {
      val application =
        applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, enterContractorEmailAddressRoute)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {
      val application =
        applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, enterContractorEmailAddressRoute)
            .withFormUrlEncodedBody(("value", "test@example.com"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must return OK and the correct view for a GET with flow id" in {
      val application =
        applicationWithCisTaxpayer(userAnswersForEmailPageWithId).build()

      running(application) {
        val request = FakeRequest(GET, enterContractorEmailAddressRouteWithId)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[EnterContractorEmailAddressView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual view(
          form,
          NormalMode,
          controllers.contractordetails.routes.EnterContractorEmailAddressController
            .onSubmitWithId(flowId, NormalMode)
        )(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET with flow id when the question has previously been answered" in {
      val userAnswers =
        userAnswersForEmailPageWithId
          .set(EnterContractorEmailAddressPage(flowId.toString), "test@example.com")
          .success
          .value

      val application =
        applicationWithCisTaxpayer(userAnswers).build()

      running(application) {
        val request = FakeRequest(GET, enterContractorEmailAddressRouteWithId)
        val view    = application.injector.instanceOf[EnterContractorEmailAddressView]
        val result  = route(application, request).value

        status(result) mustEqual OK

        contentAsString(result) mustEqual view(
          form.fill("test@example.com"),
          NormalMode,
          controllers.contractordetails.routes.EnterContractorEmailAddressController
            .onSubmitWithId(flowId, NormalMode)
        )(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted with flow id" in {
      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationWithCisTaxpayer(
          userAnswersForEmailPageWithId,
          Seq(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
        ).build()

      running(application) {
        val request =
          FakeRequest(POST, enterContractorEmailAddressRouteWithId)
            .withFormUrlEncodedBody(("value", "test@example.com"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted with flow id" in {
      val application =
        applicationBuilder(userAnswers = Some(userAnswersForEmailPageWithId)).build()

      running(application) {
        val request =
          FakeRequest(POST, enterContractorEmailAddressRouteWithId)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))
        val view      = application.injector.instanceOf[EnterContractorEmailAddressView]
        val result    = route(application, request).value

        status(result) mustEqual BAD_REQUEST

        contentAsString(result) mustEqual view(
          boundForm,
          NormalMode,
          controllers.contractordetails.routes.EnterContractorEmailAddressController
            .onSubmitWithId(flowId, NormalMode)
        )(request, messages(application)).toString
      }
    }

    "must redirect to AddEmailAddressYesNo page on a GET with flow id when AddEmailAddressYesNoPage is not answered with true" in {
      val userAnswers =
        userAnswersWithCisIdForFlow

      val application =
        applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, enterContractorEmailAddressRouteWithId)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.contractordetails.routes.AddEmailAddressYesNoController
            .onPageLoadWithId(flowId, NormalMode)
            .url
      }
    }

    "must redirect to Journey Recovery for a GET with flow id if no existing data is found" in {
      val application =
        applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, enterContractorEmailAddressRouteWithId)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST with flow id if no existing data is found" in {
      val application =
        applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, enterContractorEmailAddressRouteWithId)
            .withFormUrlEncodedBody(("value", "test@example.com"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
