/*
 * Copyright 2025 HM Revenue & Customs
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

/*
 * Copyright 2025 HM Revenue & Customs
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
import models.add.UKAddress
import models.{NormalMode, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any => anyArg}
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.add.AddressOfSubcontractorPage
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.SubcontractorService
import views.html.add.AddressOfSubcontractorView

import scala.concurrent.Future

class AddressOfSubcontractorControllerSpec extends SpecBase with MockitoSugar {

  private val formProvider = new AddressOfSubcontractorFormProvider()
  private val form         = formProvider()

  private lazy val addressOfSubcontractorRoute =
    controllers.add.routes.AddressOfSubcontractorController.onPageLoad(NormalMode).url

  private val userAnswers = UserAnswers(
    userAnswersId,
    Json.obj(
      AddressOfSubcontractorPage.toString -> Json.obj(
        "addressLine1" -> "value 1",
        "addressLine2" -> "value 2",
        "addressLine3" -> "value 3",
        "addressLine4" -> "value 4",
        "postCode"     -> "NX1 1AA"
      )
    )
  )

  "AddressOfSubcontractor Controller" - {

    "must return OK and the correct view for a GET" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, addressOfSubcontractorRoute)
        val view    = application.injector.instanceOf[AddressOfSubcontractorView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, addressOfSubcontractorRoute)
        val view    = application.injector.instanceOf[AddressOfSubcontractorView]

        val result = route(application, request).value

        status(result) mustEqual OK

        val expected = UKAddress(
          addressLine1 = "value 1",
          addressLine2 = Some("value 2"),
          addressLine3 = "value 3",
          addressLine4 = Some("value 4"),
          postCode = "NX1 1AA"
        )

        contentAsString(result) mustEqual view(form.fill(expected), NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to the NationalInsuranceNumberYesNo page when valid data is submitted and call updateSubcontractor with the updated address" in {
      val mockSessionRepository    = mock[SessionRepository]
      val mockSubcontractorService = mock[SubcontractorService]

      when(mockSessionRepository.set(anyArg())) thenReturn Future.successful(true)

      when(mockSubcontractorService.updateSubcontractor(anyArg())(anyArg()))
        .thenReturn(Future.successful(()))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[SubcontractorService].toInstance(mockSubcontractorService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, addressOfSubcontractorRoute)
            .withFormUrlEncodedBody(
              "addressLine1" -> "value 1",
              "addressLine2" -> "value 2",
              "addressLine3" -> "value 3",
              "addressLine4" -> "value 4",
              "postCode"     -> "NX1 1AA"
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.add.routes.NationalInsuranceNumberYesNoController.onPageLoad(NormalMode).url

        val uaCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockSubcontractorService, times(1)).updateSubcontractor(uaCaptor.capture())(anyArg())

        val passedUa     = uaCaptor.getValue
        val savedAddress = passedUa.get(AddressOfSubcontractorPage).value

        savedAddress.addressLine1 mustBe "value 1"
        savedAddress.addressLine2 mustBe Some("value 2")
        savedAddress.addressLine3 mustBe "value 3"
        savedAddress.addressLine4 mustBe Some("value 4")
        savedAddress.postCode mustBe "NX1 1AA"

        verify(mockSessionRepository, times(1)).set(anyArg())
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, addressOfSubcontractorRoute)
            .withFormUrlEncodedBody(
              "addressLine1" -> "",
              "addressLine2" -> "value 2",
              "addressLine3" -> "value 3",
              "postCode"     -> "NX1 1AA"
            )

        val boundForm = form.bind(
          Map(
            "addressLine1" -> "",
            "addressLine2" -> "value 2",
            "addressLine3" -> "value 3",
            "postCode"     -> "NX1 1AA"
          )
        )

        val view   = application.injector.instanceOf[AddressOfSubcontractorView]
        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, addressOfSubcontractorRoute)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, addressOfSubcontractorRoute)
            .withFormUrlEncodedBody(
              "addressLine1" -> "value 1",
              "addressLine2" -> "value 2",
              "addressLine3" -> "value 3",
              "postCode"     -> "NX1 1AA"
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
