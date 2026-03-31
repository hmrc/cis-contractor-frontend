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

package controllers

import base.SpecBase
import models.UserAnswers
import models.agent.AgentClientData
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{never, times, verify, verifyNoMoreInteractions, when}
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import queries.CisIdQuery
import repositories.SessionRepository
import services.CisManageService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class IndexControllerSpec extends SpecBase {

  "IndexController" - {

    "must redirect to TypeOfSubcontractor for a GET when user is an Org user" in {

      val mockSessionRepository = mock[SessionRepository]
      val mockCisManagerService = mock[CisManageService]

      val updatedUserAnswers: UserAnswers = emptyUserAnswers

      when(mockCisManagerService.ensureCisIdInUserAnswers(any[UserAnswers])(any[HeaderCarrier]))
        .thenReturn(Future.successful(updatedUserAnswers))

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application = applicationBuilder(
        userAnswers = None,
        hasAgentRef = false
      ).overrides(
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[CisManageService].toInstance(mockCisManagerService)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.IndexController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.add.routes.TypeOfSubcontractorController
          .onPageLoad(models.NormalMode)
          .url
      }

      verify(mockCisManagerService).ensureCisIdInUserAnswers(any[UserAnswers])(any[HeaderCarrier])
      verify(mockCisManagerService, never()).getAgentClient(any[String])(any[HeaderCarrier])
      verify(mockCisManagerService, never()).hasClient(any[String], any[String])(any[HeaderCarrier])

      verify(mockSessionRepository).set(eqTo(updatedUserAnswers))

      verifyNoMoreInteractions(mockCisManagerService)
      verifyNoMoreInteractions(mockSessionRepository)
    }

    "must redirect to TypeOfSubcontractor for a GET when user is an Agent and the client exists" in {

      val mockSessionRepository = mock[SessionRepository]
      val mockCisManagerService = mock[CisManageService]

      val uniqueId = "unique-id-123"
      val ton = "taxOfficeNumber"
      val tor = "taxOfficeReference"

      when(mockCisManagerService.getAgentClient(any[String])(any[HeaderCarrier]))
        .thenReturn(
          Future.successful(
            Some(
              AgentClientData(
                uniqueId = uniqueId,
                taxOfficeNumber = ton,
                taxOfficeReference = tor,
                schemeName = None
              )
            )
          )
        )

      when(mockCisManagerService.hasClient(eqTo(ton.trim), eqTo(tor.trim))(any[HeaderCarrier]))
        .thenReturn(Future.successful(true))

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application = applicationBuilder(
        userAnswers = Some(emptyUserAnswers),
        isAgent = true,
        hasEmployeeRef = false
      ).overrides(
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[CisManageService].toInstance(mockCisManagerService)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.IndexController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.add.routes.TypeOfSubcontractorController
          .onPageLoad(models.NormalMode)
          .url
      }

      verify(mockCisManagerService, never()).ensureCisIdInUserAnswers(any[UserAnswers])(any[HeaderCarrier])
      verify(mockCisManagerService).getAgentClient(any[String])(any[HeaderCarrier])
      verify(mockCisManagerService).hasClient(eqTo(ton.trim), eqTo(tor.trim))(any[HeaderCarrier])

      val captor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
      verify(mockSessionRepository, times(1)).set(captor.capture())
      captor.getValue.get(CisIdQuery).value mustEqual uniqueId

      verifyNoMoreInteractions(mockCisManagerService)
      verifyNoMoreInteractions(mockSessionRepository)
    }

    "must redirect to JourneyRecovery when user is an Agent and agentClient is None" in {

      val mockSessionRepository = mock[SessionRepository]
      val mockCisManagerService = mock[CisManageService]

      when(mockCisManagerService.getAgentClient(any[String])(any[HeaderCarrier]))
        .thenReturn(Future.successful(None))

      val application = applicationBuilder(
        userAnswers = Some(emptyUserAnswers),
        isAgent = true,
        hasEmployeeRef = false
      ).overrides(
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[CisManageService].toInstance(mockCisManagerService)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.IndexController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }

      verify(mockCisManagerService).getAgentClient(any[String])(any[HeaderCarrier])
      verify(mockCisManagerService, never()).hasClient(any[String], any[String])(any[HeaderCarrier])
      verify(mockCisManagerService, never()).ensureCisIdInUserAnswers(any[UserAnswers])(any[HeaderCarrier])

      verify(mockSessionRepository, never()).set(any())

      verifyNoMoreInteractions(mockCisManagerService)
      verifyNoMoreInteractions(mockSessionRepository)
    }

    "must redirect to JourneyRecovery when user is an Agent, agentClient is Some, and hasClient returns false" in {

      val mockSessionRepository = mock[SessionRepository]
      val mockCisManagerService = mock[CisManageService]

      val uniqueId = "unique-id-123"
      val ton = "taxOfficeNumber"
      val tor = "taxOfficeReference"

      when(mockCisManagerService.getAgentClient(any[String])(any[HeaderCarrier]))
        .thenReturn(
          Future.successful(
            Some(
              AgentClientData(
                uniqueId = uniqueId,
                taxOfficeNumber = ton,
                taxOfficeReference = tor,
                schemeName = None
              )
            )
          )
        )

      when(mockCisManagerService.hasClient(eqTo(ton.trim), eqTo(tor.trim))(any[HeaderCarrier]))
        .thenReturn(Future.successful(false))

      val application = applicationBuilder(
        userAnswers = Some(emptyUserAnswers),
        isAgent = true,
        hasEmployeeRef = false
      ).overrides(
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[CisManageService].toInstance(mockCisManagerService)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.IndexController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }

      verify(mockCisManagerService).getAgentClient(any[String])(any[HeaderCarrier])
      verify(mockCisManagerService).hasClient(eqTo(ton.trim), eqTo(tor.trim))(any[HeaderCarrier])
      verify(mockCisManagerService, never()).ensureCisIdInUserAnswers(any[UserAnswers])(any[HeaderCarrier])

      verify(mockSessionRepository, never()).set(any())

      verifyNoMoreInteractions(mockCisManagerService)
      verifyNoMoreInteractions(mockSessionRepository)
    }

    "must redirect to JourneyRecovery when user is an Agent, agentClient is Some, and hasClient fails with NonFatal exception" in {

      val mockSessionRepository = mock[SessionRepository]
      val mockCisManagerService = mock[CisManageService]

      val uniqueId = "unique-id-123"
      val ton = "taxOfficeNumber"
      val tor = "taxOfficeReference"

      when(mockCisManagerService.getAgentClient(any[String])(any[HeaderCarrier]))
        .thenReturn(
          Future.successful(
            Some(
              AgentClientData(
                uniqueId = uniqueId,
                taxOfficeNumber = ton,
                taxOfficeReference = tor,
                schemeName = None
              )
            )
          )
        )

      when(mockCisManagerService.hasClient(eqTo(ton.trim), eqTo(tor.trim))(any[HeaderCarrier]))
        .thenReturn(Future.failed(new RuntimeException("boom")))

      val application = applicationBuilder(
        userAnswers = Some(emptyUserAnswers),
        isAgent = true,
        hasEmployeeRef = false
      ).overrides(
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[CisManageService].toInstance(mockCisManagerService)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.IndexController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }

      verify(mockCisManagerService).getAgentClient(any[String])(any[HeaderCarrier])
      verify(mockCisManagerService).hasClient(eqTo(ton.trim), eqTo(tor.trim))(any[HeaderCarrier])
      verify(mockCisManagerService, never()).ensureCisIdInUserAnswers(any[UserAnswers])(any[HeaderCarrier])

      verify(mockSessionRepository, never()).set(any())

      verifyNoMoreInteractions(mockCisManagerService)
      verifyNoMoreInteractions(mockSessionRepository)
    }

    "must redirect to JourneyRecovery when user is an Agent and getAgentClient fails with NonFatal exception (outer recover)" in {

      val mockSessionRepository = mock[SessionRepository]
      val mockCisManagerService = mock[CisManageService]

      when(mockCisManagerService.getAgentClient(any[String])(any[HeaderCarrier]))
        .thenReturn(Future.failed(new RuntimeException("boom-getAgentClient")))

      val application = applicationBuilder(
        userAnswers = Some(emptyUserAnswers),
        isAgent = true,
        hasEmployeeRef = false
      ).overrides(
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[CisManageService].toInstance(mockCisManagerService)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.IndexController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }

      verify(mockCisManagerService, never()).ensureCisIdInUserAnswers(any[UserAnswers])(any[HeaderCarrier])
      verify(mockCisManagerService).getAgentClient(any[String])(any[HeaderCarrier])

      verify(mockCisManagerService, never()).hasClient(any[String], any[String])(any[HeaderCarrier])
      verify(mockSessionRepository, never()).set(any())

      verifyNoMoreInteractions(mockCisManagerService)
      verifyNoMoreInteractions(mockSessionRepository)
    }
  }
}