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
import models.NormalMode
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import queries.CisIdQuery
import repositories.SessionRepository
import views.html.contractordetails.ContractorDetailsView

import java.util.UUID
import scala.concurrent.Future

class ContractorDetailsControllerSpec extends WithFakeCisTaxpayer with MockitoSugar {

  private val flowId = UUID.fromString("11111111-1111-1111-1111-111111111111")

  // Must match FakeCisTaxpayerService.taxpayer.uniqueId
  private val cisId = "AB12"

  private val userAnswersWithFlowId =
    emptyUserAnswers.set(CisIdQuery(flowId.toString), cisId).success.value

  "ContractorDetails Controller" - {

    "must return OK and the correct view for a GET" in {
      val application = applicationBuilder().build()

      running(application) {
        val request =
          FakeRequest(
            GET,
            routes.ContractorDetailsController.onPageLoad().url
          )

        val result = route(application, request).value
        val view   = application.injector.instanceOf[ContractorDetailsView]

        status(result) mustEqual OK

        val expectedView = view(
          routes.ContractorDetailsController.onSubmit()
        )(
          request,
          messages(application)
        )

        contentAsString(result) mustEqual expectedView.toString
      }
    }

    "must create user answers and redirect to enter contractor UTR on a POST" in {
      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val application = applicationBuilder(
        additionalBindings = Seq(
          bind[SessionRepository].toInstance(mockSessionRepository)
        )
      ).build()

      running(application) {
        val request =
          FakeRequest(
            POST,
            routes.ContractorDetailsController.onSubmit().url
          )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          routes.ContractorUtrController.onPageLoad(NormalMode).url
      }
    }

    "must keep existing user answers and redirect to enter contractor UTR on a POST" in {
      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val application = applicationBuilder(
        userAnswers = Some(emptyUserAnswers),
        additionalBindings = Seq(
          bind[SessionRepository].toInstance(mockSessionRepository)
        )
      ).build()

      running(application) {
        val request =
          FakeRequest(
            POST,
            routes.ContractorDetailsController.onSubmit().url
          )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          routes.ContractorUtrController.onPageLoad(NormalMode).url
      }
    }

    "must return OK for a GET with a CIS ID and use the start submit route" in {
      val application = applicationBuilder().build()

      running(application) {
        val request =
          FakeRequest(
            GET,
            routes.ContractorDetailsController.onPageLoadStart(cisId).url
          )

        val result = route(application, request).value
        val view   = application.injector.instanceOf[ContractorDetailsView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(
            routes.ContractorDetailsController.onSubmitStart(cisId)
          )(
            request,
            messages(application)
          ).toString
      }
    }

    "must create a journey and redirect to contractor UTR on a POST with a CIS ID" in {
      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val application = applicationBuilder(
        additionalBindings = Seq(
          bind[SessionRepository].toInstance(mockSessionRepository)
        )
      ).build()

      running(application) {
        val request =
          FakeRequest(
            POST,
            routes.ContractorDetailsController.onSubmitStart(cisId).url
          )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        val redirectUrl = redirectLocation(result).value

        redirectUrl must include("/contractor-details/enter-contractor-utr/")

        verify(mockSessionRepository).set(any())
      }
    }

    "must return OK for a GET with a flow ID" in {
      val application = applicationBuilder(
        userAnswers = Some(userAnswersWithFlowId)
      ).build()

      running(application) {
        val request =
          FakeRequest(
            GET,
            routes.ContractorDetailsController.onPageLoadWithId(flowId).url
          )

        val result = route(application, request).value

        status(result) mustEqual OK
      }
    }

    "must save existing answers and redirect to contractor UTR for the same flow ID on a POST" in {
      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val application = applicationBuilder(
        userAnswers = Some(userAnswersWithFlowId),
        additionalBindings = Seq(
          bind[SessionRepository].toInstance(mockSessionRepository)
        )
      ).build()

      running(application) {
        val request =
          FakeRequest(
            POST,
            routes.ContractorDetailsController.onSubmitWithId(flowId).url
          )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          routes.ContractorUtrController
            .onPageLoadWithId(flowId, NormalMode)
            .url

        verify(mockSessionRepository).set(any())
      }
    }
  }
}
