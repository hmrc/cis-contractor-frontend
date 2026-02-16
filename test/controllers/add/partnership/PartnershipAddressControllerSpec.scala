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

package controllers.add.partnership

import base.SpecBase
import controllers.routes
import forms.add.partnership.PartnershipAddressFormProvider
import models.add.PartnershipCountryAddress
import utils.CountryOptions
import models.{NormalMode, UserAnswers}
import navigation.Navigator
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.add.partnership.{PartnershipAddressPage, PartnershipNamePage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.libs.json.Json
import repositories.SessionRepository
import views.html.add.partnership.PartnershipAddressView
import org.scalatest.matchers.must.Matchers
import config.FrontendAppConfig

import scala.concurrent.Future

class PartnershipAddressControllerSpec extends SpecBase with MockitoSugar with Matchers {

  private val formProvider = new PartnershipAddressFormProvider()
  private val form         = formProvider()

  private val partnershipName = "Test Partnership"

  private lazy val routeLoad   =
    controllers.add.partnership.routes.PartnershipAddressController.onPageLoad(NormalMode).url
  private lazy val routeSubmit =
    controllers.add.partnership.routes.PartnershipAddressController.onSubmit(NormalMode).url

  private def uaWithName: UserAnswers =
    emptyUserAnswers.set(PartnershipNamePage, partnershipName).success.value
  private val userAnswers             = UserAnswers(
    userAnswersId,
    Json.obj(
      PartnershipNamePage.toString    -> Json.toJson(partnershipName),
      PartnershipAddressPage.toString -> Json.obj(
        "addressLine1" -> "value 1",
        "addressLine2" -> "value 2",
        "addressLine3" -> "value 3",
        "addressLine4" -> "value 4",
        "postalCode"   -> "NX1 1AA",
        "country"      -> "United Kingdom"
      )
    )
  )

  "PartnershipAddressController" - {

    "must return OK and the correct view for a GET when partnership name is present and no previous answer" in {
      val application = applicationBuilder(userAnswers = Some(uaWithName)).build()

      running(application) {
        val request                               = FakeRequest(GET, routeLoad)
        val view                                  = application.injector.instanceOf[PartnershipAddressView]
        val countryOptions                        = application.injector.instanceOf[CountryOptions]
        val appConfig                             = application.injector.instanceOf[FrontendAppConfig]
        implicit val msgs: play.api.i18n.Messages = messages(application)
        val result                                = route(application, request).value

        status(result) mustBe OK
        contentType(result) mustBe Some("text/html")
        charset(result) mustBe Some("utf-8")
        contentAsString(result) mustBe
          view(form, NormalMode, partnershipName, countryOptions.options())(request, appConfig).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered and partnership name is present" in {
      val expected = PartnershipCountryAddress(
        addressLine1 = "line 1",
        addressLine2 = Some("line 2"),
        addressLine3 = "line 3",
        addressLine4 = Some("line 4"),
        postalCode = "NX1 1AA",
        country = "United Kingdom"
      )

      val ua = uaWithName
        .set(PartnershipAddressPage, expected)
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(ua)).build()

      running(application) {
        val request                               = FakeRequest(GET, routeLoad)
        val view                                  = application.injector.instanceOf[PartnershipAddressView]
        val countryOptions                        = application.injector.instanceOf[CountryOptions]
        val appConfig                             = application.injector.instanceOf[FrontendAppConfig]
        implicit val msgs: play.api.i18n.Messages = messages(application)
        val result                                = route(application, request).value

        status(result) mustBe OK

        val expected = PartnershipCountryAddress(
          addressLine1 = "line 1",
          addressLine2 = Some("line 2"),
          addressLine3 = "line 3",
          addressLine4 = Some("line 4"),
          postalCode = "NX1 1AA",
          country = "United Kingdom"
        )

        contentType(result) mustBe Some("text/html")
        charset(result) mustBe Some("utf-8")
        contentAsString(result) mustBe
          view(form.fill(expected), NormalMode, partnershipName, countryOptions.options())(request, appConfig).toString
      }
    }

    "must redirect to Journey Recovery for a GET when partnership name is missing (userAnswers present)" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routeLoad)
        val result  = route(application, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, routeLoad)
        val result  = route(application, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to the next page when valid data is submitted (and persist the address) when partnership name is present" in {
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

        val saved = uaCaptor.getValue.get(PartnershipAddressPage).value
        saved.addressLine1 mustBe "value 1"
        saved.addressLine2 mustBe Some("value 2")
        saved.addressLine3 mustBe "value 3"
        saved.addressLine4 mustBe Some("value 4")
        saved.postalCode mustBe "NX1 1AA"
        saved.country mustBe "United Kingdom"
      }
    }

    "must return a Bad Request and errors when invalid data is submitted (name present)" in {
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

        val view                                  = application.injector.instanceOf[PartnershipAddressView]
        val countryOptions                        = application.injector.instanceOf[CountryOptions]
        val appConfig                             = application.injector.instanceOf[FrontendAppConfig]
        implicit val msgs: play.api.i18n.Messages = messages(application)
        val result                                = route(application, request).value

        status(result) mustBe BAD_REQUEST
        contentAsString(result) mustBe
          view(boundForm, NormalMode, partnershipName, countryOptions.options())(request, appConfig).toString
      }
    }

    "must redirect to Journey Recovery for a POST when partnership name is missing (userAnswers present)" in {
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
