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
import forms.add.partnership.PartnershipNameFormProvider
import models.{NormalMode, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, verifyNoMoreInteractions, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.add.partnership.PartnershipNamePage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.SubcontractorService
import uk.gov.hmrc.http.HeaderCarrier
import views.html.add.partnership.PartnershipNameView

import scala.concurrent.Future
import scala.util.Random

class PartnershipNameControllerSpec extends SpecBase with MockitoSugar {

  private val formProvider = new PartnershipNameFormProvider()
  private val form         = formProvider()

  private lazy val partnershipNameRoute =
    controllers.add.partnership.routes.PartnershipNameController.onPageLoad(NormalMode).url

  "PartnershipNameController" - {

    "must return OK and the correct view for a GET" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, partnershipNameRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[PartnershipNameView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val userAnswers = UserAnswers(userAnswersId).set(PartnershipNamePage, "My Partnership").success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, partnershipNameRoute)

        val view = application.injector.instanceOf[PartnershipNameView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill("My Partnership"), NormalMode)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to PartnershipHasUtrYesNo page when valid data is submitted" in {
      val mockSessionRepository    = mock[SessionRepository]
      val mockSubcontractorService = mock[SubcontractorService]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
      when(mockSubcontractorService.updateSubcontractor(any[UserAnswers])(any[HeaderCarrier])) thenReturn Future
        .successful(())

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[SubcontractorService].toInstance(mockSubcontractorService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, partnershipNameRoute)
            .withFormUrlEncodedBody(("value", "My Partnership"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.add.partnership.routes.PartnershipHasUtrYesNoController
            .onPageLoad(NormalMode)
            .url
      }

      verify(mockSessionRepository).set(any())
      verifyNoMoreInteractions(mockSubcontractorService)
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, partnershipNameRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[PartnershipNameView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
      }
    }

    "must return a Bad Request and errors when invalid data is submitted with length more than 56 characters" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
      val longInput   = Random.alphanumeric.take(57).mkString

      running(application) {
        val request =
          FakeRequest(POST, partnershipNameRoute)
            .withFormUrlEncodedBody(("value", longInput))

        val boundForm = form.bind(Map("value" -> longInput))

        val view = application.injector.instanceOf[PartnershipNameView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, partnershipNameRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, partnershipNameRoute)
            .withFormUrlEncodedBody(("value", "My Partnership"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
