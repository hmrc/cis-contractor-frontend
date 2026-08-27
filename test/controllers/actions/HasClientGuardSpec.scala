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

package controllers.actions

import base.SpecBase
import models.agent.AgentClientData
import models.requests.IdentifierRequest
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import play.api.test.Helpers.{LOCATION, SEE_OTHER}
import services.{AuditService, CisManageService}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class HasClientGuardSpec extends SpecBase with MockitoSugar {

  private given ExecutionContext = ExecutionContext.global

  private val cisManageService = mock[CisManageService]
  private val auditService     = mock[AuditService]

  private val guard =
    new HasClientGuard(cisManageService, auditService)

  private def request(isAgent: Boolean): IdentifierRequest[AnyContent] =
    IdentifierRequest(
      FakeRequest(),
      "user-id",
      None,
      if isAgent then Some("agent-ref") else None,
      isAgent
    )

  private val client =
    AgentClientData(
      "CLIENT-123",
      "163",
      "AB0063",
      Some("ABC Construction Ltd")
    )

  "HasClientGuard" - {

    "must redirect to system error when agent client data is missing" in {
      when(
        cisManageService.getAgentClient(any[String])(using any[HeaderCarrier])
      ).thenReturn(Future.successful(None))

      val result =
        guard.check(request(isAgent = true)).futureValue

      result.value.header.status mustBe SEE_OTHER
      result.value.header.headers.get(LOCATION) mustBe
        Some(controllers.routes.SystemErrorController.onPageLoad().url)
    }

    "must redirect to system error when tax office details are missing" in {
      when(
        cisManageService.getAgentClient(any[String])(using any[HeaderCarrier])
      ).thenReturn(
        Future.successful(
          Some(client.copy(taxOfficeNumber = ""))
        )
      )

      val result =
        guard.check(request(isAgent = true)).futureValue

      result.value.header.status mustBe SEE_OTHER
    }

    "must continue when hasClient returns true" in {
      when(
        cisManageService.getAgentClient(any[String])(using any[HeaderCarrier])
      ).thenReturn(Future.successful(Some(client)))

      when(
        cisManageService.hasClient(any[String], any[String])(using any[HeaderCarrier])
      ).thenReturn(Future.successful(true))

      guard.check(request(isAgent = true)).futureValue mustBe None
    }

    "must redirect to system error when hasClient returns false" in {
      when(
        cisManageService.getAgentClient(any[String])(using any[HeaderCarrier])
      ).thenReturn(Future.successful(Some(client)))

      when(
        cisManageService.hasClient(any[String], any[String])(using any[HeaderCarrier])
      ).thenReturn(Future.successful(false))

      val result =
        guard.check(request(isAgent = true)).futureValue

      result.value.header.status mustBe SEE_OTHER
      result.value.header.headers.get(LOCATION) mustBe
        Some(controllers.routes.SystemErrorController.onPageLoad().url)
    }

    "must redirect to system error when the check fails" in {
      when(
        cisManageService.getAgentClient(any[String])(using any[HeaderCarrier])
      ).thenReturn(Future.successful(Some(client)))

      when(
        cisManageService.hasClient(any[String], any[String])(using any[HeaderCarrier])
      ).thenReturn(Future.failed(new RuntimeException("boom")))

      val result =
        guard.check(request(isAgent = true)).futureValue

      result.value.header.status mustBe SEE_OTHER
      result.value.header.headers.get(LOCATION) mustBe
        Some(controllers.routes.SystemErrorController.onPageLoad().url)
    }
  }
}
