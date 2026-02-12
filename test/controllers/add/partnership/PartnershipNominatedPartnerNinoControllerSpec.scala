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
import forms.add.partnership.PartnershipNominatedPartnerNinoFormProvider
import models.{NormalMode, UserAnswers}
import navigation.Navigator
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, verifyNoMoreInteractions, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.add.partnership.{PartnershipNominatedPartnerNamePage, PartnershipNominatedPartnerNinoPage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.SubcontractorService
import uk.gov.hmrc.http.HeaderCarrier
import views.html.add.partnership.PartnershipNominatedPartnerNinoView

import scala.concurrent.Future
import scala.concurrent.Await
import scala.concurrent.duration.*

class PartnershipNominatedPartnerNinoControllerSpec extends SpecBase with MockitoSugar {

  private val formProvider = new PartnershipNominatedPartnerNinoFormProvider()
  private val form         = formProvider()

  private val nominatedPartnerName = "Jane Doe"

  private lazy val routeUrl =
    controllers.add.partnership.routes.PartnershipNominatedPartnerNinoController
      .onPageLoad(NormalMode)
      .url

  "PartnershipNominatedPartnerNinoController" - {

    "must return OK and the correct view for a GET" in {
      val ua =
        emptyUserAnswers
          .set(PartnershipNominatedPartnerNamePage, nominatedPartnerName)
          .success
          .value

      val application = applicationBuilder(userAnswers = Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, routeUrl)

        val result = route(application, request).value

        val view = application.injector.instanceOf[PartnershipNominatedPartnerNinoView]

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
          .set(PartnershipNominatedPartnerNinoPage, "AA123456A")
          .success
          .value

      val application = applicationBuilder(userAnswers = Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, routeUrl)

        val view = application.injector.instanceOf[PartnershipNominatedPartnerNinoView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill("AA123456A"), NormalMode, nominatedPartnerName)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect when valid data is submitted" in {
      val mockSessionRepository    = mock[SessionRepository]
      val mockSubcontractorService = mock[SubcontractorService]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
      when(mockSubcontractorService.updateSubcontractor(any[UserAnswers])(any[HeaderCarrier])) thenReturn Future
        .successful(())

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
          FakeRequest(POST, routeUrl)
            .withFormUrlEncodedBody("value" -> "AA 12 34 56 A")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
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
          FakeRequest(POST, routeUrl)
            .withFormUrlEncodedBody("value" -> "INVALID")

        val boundForm = form.bind(Map("value" -> "INVALID"))
        val view      = application.injector.instanceOf[PartnershipNominatedPartnerNinoView]

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
        val request = FakeRequest(GET, routeUrl)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, routeUrl)
            .withFormUrlEncodedBody("value" -> "QQ123456C")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must throw RuntimeException on a GET when nominated partner name is missing" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routeUrl)
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
          FakeRequest(POST, routeUrl)
            .withFormUrlEncodedBody("value" -> "QQ123456C")

        val result = route(application, request).value

        val ex = intercept[RuntimeException] {
          Await.result(result, 5.seconds)
        }

        ex.getMessage mustBe "Missing nominated partner name"
      }
    }
  }
}
