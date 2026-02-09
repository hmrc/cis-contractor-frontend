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
import forms.add.partnership.PartnershipHasUtrYesNoFormProvider
import models.{NormalMode, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.add.partnership.{PartnershipHasUtrYesNoPage, PartnershipNamePage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.add.partnership.PartnershipHasUtrYesNoView

import scala.concurrent.Future

class PartnershipHasUtrYesNoControllerSpec extends SpecBase with MockitoSugar {

  private val formProvider = new PartnershipHasUtrYesNoFormProvider()
  private val form         = formProvider()

  private val partnershipName = "Test Partnership"

  private lazy val routeLoad   =
    controllers.add.partnership.routes.PartnershipHasUtrYesNoController.onPageLoad(NormalMode).url
  private lazy val routeSubmit =
    controllers.add.partnership.routes.PartnershipHasUtrYesNoController.onSubmit(NormalMode).url

  private def uaWithName: UserAnswers =
    emptyUserAnswers.set(PartnershipNamePage, partnershipName).success.value

  "partnershipHasUtrYesNo Controller" - {

    "must return OK and the correct view for a GET when name is present" in {
      val application = applicationBuilder(userAnswers = Some(uaWithName)).build()

      running(application) {
        val request = FakeRequest(GET, routeLoad)

        val result = route(application, request).value
        val view   = application.injector.instanceOf[PartnershipHasUtrYesNoView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, partnershipName)(
          request,
          messages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered and name is present" in {
      val userAnswers =
        uaWithName
          .set(PartnershipHasUtrYesNoPage, true)
          .success
          .value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routeLoad)
        val view    = application.injector.instanceOf[PartnershipHasUtrYesNoView]
        val result  = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), NormalMode, partnershipName)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET when partnership name is missing (userAnswers present)" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routeLoad)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to the next page when valid data is submitted" in {
      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(uaWithName))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, routeSubmit)
            .withFormUrlEncodedBody("value" -> "true")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(
          result
        ).value mustEqual controllers.add.partnership.routes.PartnershipUniqueTaxpayerReferenceController
          .onPageLoad(NormalMode)
          .url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted (name present)" in {
      val application = applicationBuilder(userAnswers = Some(uaWithName)).build()

      running(application) {
        val request =
          FakeRequest(POST, routeSubmit)
            .withFormUrlEncodedBody("value" -> "")

        val boundForm = form.bind(Map("value" -> ""))

        val view   = application.injector.instanceOf[PartnershipHasUtrYesNoView]
        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, partnershipName)(
          request,
          messages(application)
        ).toString
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
            .withFormUrlEncodedBody("value" -> "true")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
    "must redirect to Journey Recovery for a GET if no existing data is found" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, routeLoad)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, routeSubmit)
            .withFormUrlEncodedBody("value" -> "true")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
