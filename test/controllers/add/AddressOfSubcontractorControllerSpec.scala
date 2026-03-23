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

package controllers.add

import base.SpecBase
import controllers.routes
import forms.add.AddressOfSubcontractorFormProvider
import models.add.{InternationalAddress, SubcontractorName}
import utils.CountryOptions
import models.{NormalMode, UserAnswers}
import org.mockito.ArgumentCaptor
import navigation.Navigator
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify}
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.add.{AddressOfSubcontractorPage, SubcontractorNamePage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.add.AddressOfSubcontractorView
import config.FrontendAppConfig

import scala.concurrent.Future

class AddressOfSubcontractorControllerSpec extends SpecBase with MockitoSugar {

  private val formProvider = new AddressOfSubcontractorFormProvider()
  private val form         = formProvider()

  private lazy val routeLoad =
    controllers.add.routes.AddressOfSubcontractorController.onPageLoad(NormalMode).url

  private lazy val routeSubmit =
    controllers.add.routes.AddressOfSubcontractorController.onSubmit(NormalMode).url

  private val subcontractorName = SubcontractorName("John", Some("Paul"), "Smith")

  private val name = "John Smith"

  private def uaWithName: UserAnswers =
    emptyUserAnswers.set(SubcontractorNamePage, subcontractorName).success.value

  "AddressOfSubcontractor Controller" - {

    "must return OK and the correct view for a GET when subcontractor name is present and no previous answer" in {

      val application = applicationBuilder(userAnswers = Some(uaWithName)).build()

      running(application) {
        val request                               = FakeRequest(GET, routeLoad)
        val result                                = route(application, request).value
        val countryOptions                        = application.injector.instanceOf[CountryOptions]
        val appConfig                             = application.injector.instanceOf[FrontendAppConfig]
        val view                                  = application.injector.instanceOf[AddressOfSubcontractorView]
        implicit val msgs: play.api.i18n.Messages = messages(application)

        status(result) mustBe OK
        contentType(result) mustBe Some("text/html")
        charset(result) mustBe Some("utf-8")
        contentAsString(result) mustBe view(form, NormalMode, name, countryOptions.options())(
          request,
          appConfig
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered and subcontractor name is present" in {

      val expected = InternationalAddress(
        addressLine1 = "line 1",
        addressLine2 = Some("line 2"),
        addressLine3 = "line 3",
        addressLine4 = Some("line 4"),
        postalCode = "NX1 1AA",
        country = "United Kingdom"
      )

      val ua = uaWithName
        .set(AddressOfSubcontractorPage, expected)
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(ua)).build()

      running(application) {
        val request                               = FakeRequest(GET, routeLoad)
        val view                                  = application.injector.instanceOf[AddressOfSubcontractorView]
        val countryOptions                        = application.injector.instanceOf[CountryOptions]
        val appConfig                             = application.injector.instanceOf[FrontendAppConfig]
        implicit val msgs: play.api.i18n.Messages = messages(application)
        val result                                = route(application, request).value

        status(result) mustBe OK

        val expected = InternationalAddress(
          addressLine1 = "line 1",
          addressLine2 = Some("line 2"),
          addressLine3 = "line 3",
          addressLine4 = Some("line 4"),
          postalCode = "NX1 1AA",
          country = "United Kingdom"
        )

        contentType(result) mustBe Some("text/html")
        charset(result) mustBe Some("utf-8")
        contentAsString(result) mustBe view(form.fill(expected), NormalMode, name, countryOptions.options())(
          request,
          appConfig
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET when subcontractor name is missing (userAnswers present)" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routeLoad)
        val result  = route(application, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to the next page when valid data is submitted (and persist the address) when subcontractor name is present" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val mockNavigator = mock[Navigator]
      when(mockNavigator.nextPage(any(), any(), any()))
        .thenReturn(Call("GET", "/dummy-next"))

      val application =
        applicationBuilder(userAnswers = Some(uaWithName))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[Navigator].toInstance(mockNavigator)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, routeSubmit)
            .withFormUrlEncodedBody(
              "addressLine1" -> "value 1",
              "addressLine2" -> "value 2",
              "addressLine3" -> "value 3",
              "addressLine4" -> "value 4",
              "postalCode"   -> "NX1 1AA",
              "country"      -> "United Kingdom"
            )

        val result = route(application, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe "/dummy-next"

        val uaCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockSessionRepository, times(1)).set(uaCaptor.capture())

        val saved = uaCaptor.getValue.get(AddressOfSubcontractorPage).value
        saved.addressLine1 mustBe "value 1"
        saved.addressLine2 mustBe Some("value 2")
        saved.addressLine3 mustBe "value 3"
        saved.addressLine4 mustBe Some("value 4")
        saved.postalCode mustBe "NX1 1AA"
        saved.country mustBe "United Kingdom"
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(uaWithName)).build()

      running(application) {
        val request =
          FakeRequest(POST, routeSubmit)
            .withFormUrlEncodedBody(
              "addressLine1" -> "",
              "addressLine2" -> "value 2",
              "addressLine3" -> "value 3",
              "postalCode"   -> "NX1 1AA",
              "country"      -> "United Kingdom"
            )

        val boundForm = form.bind(
          Map(
            "addressLine1" -> "",
            "addressLine2" -> "value 2",
            "addressLine3" -> "value 3",
            "postalCode"   -> "NX1 1AA",
            "country"      -> "United Kingdom"
          )
        )

        val view                                  = application.injector.instanceOf[AddressOfSubcontractorView]
        val countryOptions                        = application.injector.instanceOf[CountryOptions]
        val appConfig                             = application.injector.instanceOf[FrontendAppConfig]
        implicit val msgs: play.api.i18n.Messages = messages(application)
        val result                                = route(application, request).value

        status(result) mustBe BAD_REQUEST
        contentAsString(result) mustBe view(boundForm, NormalMode, name, countryOptions.options())(
          request,
          appConfig
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, routeLoad)

        val result = route(application, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST when subcontractor name is missing (userAnswers present)" in {
      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, routeSubmit)
            .withFormUrlEncodedBody(
              "addressLine1" -> "value 1",
              "addressLine3" -> "value 3",
              "postalCode"   -> "NX1 1AA",
              "country"      -> "United Kingdom"
            )

        val result = route(application, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, routeSubmit)
            .withFormUrlEncodedBody(
              "addressLine1" -> "value 1",
              "addressLine3" -> "value 3",
              "postalCode"   -> "NX1 1AA",
              "country"      -> "United Kingdom"
            )

        val result = route(application, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must surface an error (throw) if the repository write fails after valid submission" in {
      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())) thenReturn Future.failed(new RuntimeException("db down"))

      val mockNavigator = mock[Navigator]
      when(mockNavigator.nextPage(any(), any(), any()))
        .thenReturn(Call("GET", "/dummy-next"))

      val application =
        applicationBuilder(userAnswers = Some(uaWithName))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[Navigator].toInstance(mockNavigator)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, routeSubmit)
            .withFormUrlEncodedBody(
              "addressLine1" -> "value 1",
              "addressLine3" -> "value 3",
              "postalCode"   -> "NX1 1AA",
              "country"      -> "United Kingdom"
            )

        val thrown = intercept[RuntimeException] {
          await(route(application, request).value)
        }

        thrown.getMessage mustBe "db down"
      }
    }
  }
}
