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
import forms.add.IndividualEmailAddressFormProvider
import models.add.SubcontractorName
import models.{NormalMode, UserAnswers}
import navigation.Navigator
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.add.{IndividualEmailAddressPage, SubcontractorNamePage}
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.add.IndividualEmailAddressView

import scala.concurrent.Future

class IndividualEmailAddressControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute: Call = Call("GET", "/foo")

  val formProvider       = new IndividualEmailAddressFormProvider()
  val form: Form[String] = formProvider()

  lazy val individualEmailAddressRoute: String =
    controllers.add.routes.IndividualEmailAddressController.onPageLoad(NormalMode).url

  private val subContractorName = SubcontractorName("John", Some("Paul"), "Smith")

  private val name = "John Smith"

  private def uaWithName: UserAnswers =
    emptyUserAnswers.set(SubcontractorNamePage, subContractorName).success.value

  "IndividualEmailAddress Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(uaWithName)).build()

      running(application) {
        val request = FakeRequest(GET, individualEmailAddressRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[IndividualEmailAddressView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, name)(
          request,
          messages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = uaWithName.set(IndividualEmailAddressPage, "abc@test.com").success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, individualEmailAddressRoute)

        val view = application.injector.instanceOf[IndividualEmailAddressView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill("abc@test.com"), NormalMode, name)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]
      val mockNavigator         = mock[Navigator]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
      when(mockNavigator.nextPage(any(), any(), any())).thenReturn(onwardRoute)

      val application =
        applicationBuilder(userAnswers = Some(uaWithName))
          .overrides(
            bind[Navigator].toInstance(mockNavigator),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, individualEmailAddressRoute)
            .withFormUrlEncodedBody(("value", "abc@xyz.com"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(uaWithName)).build()

      running(application) {
        val request =
          FakeRequest(POST, individualEmailAddressRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[IndividualEmailAddressView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, name)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, individualEmailAddressRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, individualEmailAddressRoute)
            .withFormUrlEncodedBody(("value", "abc@xyz.com"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to JourneyRecovery if SubcontractorName is missing for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, individualEmailAddressRoute)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to JourneyRecovery if SubcontractorName is missing for a POST" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, individualEmailAddressRoute)
            .withFormUrlEncodedBody(("value", "abc@xyz.com"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
