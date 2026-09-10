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

import base.SpecBase
import models.Scheme
import org.mockito.ArgumentMatchers.{any, anyString}
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import queries.CisIdQuery
import repositories.SessionRepository
import services.ContractorDetailsService
import uk.gov.hmrc.http.HeaderCarrier
import views.html.contractordetails.ContractorDetailsUpdatedView

import scala.concurrent.Future

class ContractorDetailsUpdatedControllerSpec extends SpecBase with MockitoSugar {

  "ContractorDetailsUpdated Controller" - {

    "must refresh the scheme in session and return OK for a GET" in {

      val cisId  = "cisId"
      val scheme = Scheme(
        schemeId = 123,
        instanceId = "instanceId",
        accountsOfficeReference = "AORef",
        taxOfficeNumber = "123",
        taxOfficeReference = "456",
        utr = Some("1234567890"),
        version = Some(2)
      )

      val mockContractorDetailsService =
        mock[ContractorDetailsService]

      val mockSessionRepository =
        mock[SessionRepository]

      when(
        mockContractorDetailsService.getScheme(anyString())(any[HeaderCarrier])
      ).thenReturn(
        Future.successful(scheme)
      )

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val userAnswers =
        emptyUserAnswers
          .set(CisIdQuery, cisId)
          .success
          .value

      val application =
        applicationBuilder(
          userAnswers = Some(userAnswers),
          additionalBindings = Seq(
            inject
              .bind[ContractorDetailsService]
              .toInstance(mockContractorDetailsService),
            inject
              .bind[SessionRepository]
              .toInstance(mockSessionRepository)
          )
        ).build()

      running(application) {

        val request =
          FakeRequest(
            GET,
            routes.ContractorDetailsUpdatedController.onPageLoad().url
          )

        val result =
          route(application, request).value

        val view =
          application.injector
            .instanceOf[ContractorDetailsUpdatedView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view()(request, messages(application)).toString
      }
    }

    "must redirect to journey recovery when CisIdQuery is missing" in {

      val application =
        applicationBuilder(
          userAnswers = Some(emptyUserAnswers)
        ).build()

      running(application) {

        val request =
          FakeRequest(
            GET,
            routes.ContractorDetailsUpdatedController.onPageLoad().url
          )

        val result =
          route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.routes.JourneyRecoveryController
            .onPageLoad()
            .url
      }
    }

    "must redirect to journey recovery when getScheme fails" in {

      val cisId = "cisId"

      val mockContractorDetailsService =
        mock[ContractorDetailsService]

      val mockSessionRepository =
        mock[SessionRepository]

      when(
        mockContractorDetailsService.getScheme(anyString())(any[HeaderCarrier])
      ).thenReturn(
        Future.failed(new RuntimeException("boom"))
      )

      val userAnswers =
        emptyUserAnswers
          .set(CisIdQuery, cisId)
          .success
          .value

      val application =
        applicationBuilder(
          userAnswers = Some(userAnswers),
          additionalBindings = Seq(
            inject
              .bind[ContractorDetailsService]
              .toInstance(mockContractorDetailsService),
            inject
              .bind[SessionRepository]
              .toInstance(mockSessionRepository)
          )
        ).build()

      running(application) {

        val request =
          FakeRequest(
            GET,
            routes.ContractorDetailsUpdatedController.onPageLoad().url
          )

        val result =
          route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.routes.JourneyRecoveryController
            .onPageLoad()
            .url
      }
    }
  }
}
