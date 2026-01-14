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
import forms.add.SubcontractorNameFormProvider
import models.add.*
import models.subcontractor.UpdateSubcontractorResponse
import models.{NormalMode, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, verifyNoMoreInteractions, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.add.SubcontractorNamePage
import play.api.inject.bind
import play.api.libs.json.{Json, OFormat}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import queries.SubbieResourceRefQuery
import repositories.SessionRepository
import services.SubcontractorService
import uk.gov.hmrc.http.HeaderCarrier
import views.html.add.SubcontractorNameView

import scala.concurrent.Future

class SubcontractorNameControllerSpec extends SpecBase with MockitoSugar {

  val formProvider = new SubcontractorNameFormProvider()
  private val form = formProvider()

  private lazy val subcontractorNameRoute =
    controllers.add.routes.SubcontractorNameController.onPageLoad(NormalMode).url

  "SubcontractorName Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, subcontractorNameRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SubcontractorNameView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      implicit val subcontractorNameFormat: OFormat[SubcontractorName] =
        Json.format[SubcontractorName]

      val validName = SubcontractorName("John", Some("Paul"), "Smith")

      val userAnswers = emptyUserAnswers.set(SubcontractorNamePage, validName).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, subcontractorNameRoute)

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) must include("value=\"John\"")
        contentAsString(result) must include("value=\"Paul\"")
        contentAsString(result) must include("value=\"Smith\"")
      }
    }

    "must redirect to the SubAddressYesNo page when valid data is submitted" in {

      val mockSessionRepository    = mock[SessionRepository]
      val mockSubcontractorService = mock[SubcontractorService]

      val newVersion      = 20
      val mockUserAnswers = emptyUserAnswers
        .set(SubbieResourceRefQuery, 2)
        .success
        .value

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
      when(mockSubcontractorService.ensureSubcontractorInUserAnswers(any[UserAnswers])(any[HeaderCarrier])).thenReturn(
        Future
          .successful(mockUserAnswers)
      )
      when(mockSubcontractorService.updateSubcontractor(any[UserAnswers])(any[HeaderCarrier])).thenReturn(
        Future
          .successful(UpdateSubcontractorResponse(newVersion = newVersion))
      )

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[SubcontractorService].toInstance(mockSubcontractorService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, subcontractorNameRoute)
            .withFormUrlEncodedBody(
              "firstName"  -> "John",
              "middleName" -> "Paul",
              "lastName"   -> "Smith"
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.add.routes.SubAddressYesNoController
          .onPageLoad(NormalMode)
          .url
      }

      verify(mockSubcontractorService).ensureSubcontractorInUserAnswers(any[UserAnswers])(any[HeaderCarrier])
      verify(mockSubcontractorService).updateSubcontractor(any[UserAnswers])(any[HeaderCarrier])
      verifyNoMoreInteractions(mockSubcontractorService)
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, subcontractorNameRoute)
            .withFormUrlEncodedBody(
              "firstName"  -> "",
              "middleName" -> "1Paul",
              "lastName"   -> ""
            )

        val boundForm = form.bind(
          Map("firstName" -> "", "middleName" -> "1Paul", "lastName" -> "")
        )

        val view = application.injector.instanceOf[SubcontractorNameView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, subcontractorNameRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, subcontractorNameRoute)
            .withFormUrlEncodedBody(
              "firstName"  -> "John",
              "middleName" -> "Paul",
              "lastName"   -> "Smith"
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
