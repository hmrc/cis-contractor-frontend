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
import connectors.ConstructionIndustrySchemeConnector
import models.EmployerReference
import models.agent.AgentClientData
import models.requests.DataRequest
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{never, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.mvc.Results.Ok
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import queries.CisIdQuery
import services.CisManageService
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class FormpRdsReconcileActionSpec extends SpecBase with MockitoSugar {

  private val cisId = "1"

  private def userAnswersWithId =
    emptyUserAnswers.set(CisIdQuery, cisId).success.value

  private def contractorRequest(implicit ua: models.UserAnswers = userAnswersWithId): DataRequest[AnyContentAsEmpty.type] =
    DataRequest(
      request = FakeRequest(),
      userId = userAnswersId,
      userAnswers = ua,
      employerReference = Some(EmployerReference("123", "AB456")),
      isAgent = false
    )

  private def agentRequest(implicit ua: models.UserAnswers = userAnswersWithId): DataRequest[AnyContentAsEmpty.type] =
    DataRequest(
      request = FakeRequest(),
      userId = userAnswersId,
      userAnswers = ua,
      employerReference = None,
      isAgent = true
    )

  private val block: DataRequest[AnyContentAsEmpty.type] => Future[Result] = _ => Future.successful(Ok)

  private def newAction(
    connector: ConstructionIndustrySchemeConnector,
    manageService: CisManageService = mock[CisManageService]
  ): FormpRdsReconcileActionImpl =
    new FormpRdsReconcileActionImpl(connector, manageService)

  "FormpRdsReconcileAction" - {

    "must call prepopulate with contractor tax office details and allow the request through on success" in {
      val connector = mock[ConstructionIndustrySchemeConnector]
      when(connector.prepopulateContractorKnownFacts(eqTo(cisId), eqTo("123"), eqTo("AB456"))(any[HeaderCarrier]))
        .thenReturn(Future.unit)

      val result = newAction(connector).invokeBlock(contractorRequest, block)

      status(result) mustBe OK
      verify(connector).prepopulateContractorKnownFacts(eqTo(cisId), eqTo("123"), eqTo("AB456"))(any[HeaderCarrier])
    }

    "must resolve agent tax office details via the agent client cache and allow the request through on success" in {
      val connector     = mock[ConstructionIndustrySchemeConnector]
      val manageService = mock[CisManageService]
      when(manageService.getAgentClient(eqTo(userAnswersId))(any[HeaderCarrier]))
        .thenReturn(Future.successful(Some(AgentClientData(cisId, "840", "MZ00064", None))))
      when(connector.prepopulateContractorKnownFacts(eqTo(cisId), eqTo("840"), eqTo("MZ00064"))(any[HeaderCarrier]))
        .thenReturn(Future.unit)

      val result = newAction(connector, manageService).invokeBlock(agentRequest, block)

      status(result) mustBe OK
      verify(connector).prepopulateContractorKnownFacts(eqTo(cisId), eqTo("840"), eqTo("MZ00064"))(any[HeaderCarrier])
    }

    "must redirect an organisation to the unauthorised organisation page when contractor data is missing (412)" in {
      val connector = mock[ConstructionIndustrySchemeConnector]
      when(connector.prepopulateContractorKnownFacts(any[String], any[String], any[String])(any[HeaderCarrier]))
        .thenReturn(Future.failed(UpstreamErrorResponse("missing", PRECONDITION_FAILED, PRECONDITION_FAILED)))

      val result = newAction(connector).invokeBlock(contractorRequest, block)

      status(result) mustBe SEE_OTHER
      redirectLocation(result).value mustBe
        controllers.routes.UnauthorisedOrganisationAffinityController.onPageLoad().url
    }

    "must redirect an organisation to the unauthorised organisation page when contractor data is missing (404)" in {
      val connector = mock[ConstructionIndustrySchemeConnector]
      when(connector.prepopulateContractorKnownFacts(any[String], any[String], any[String])(any[HeaderCarrier]))
        .thenReturn(Future.failed(UpstreamErrorResponse("missing", NOT_FOUND, NOT_FOUND)))

      val result = newAction(connector).invokeBlock(contractorRequest, block)

      status(result) mustBe SEE_OTHER
      redirectLocation(result).value mustBe
        controllers.routes.UnauthorisedOrganisationAffinityController.onPageLoad().url
    }

    "must redirect an agent to the unauthorised agent page when contractor data is missing (412)" in {
      val connector     = mock[ConstructionIndustrySchemeConnector]
      val manageService = mock[CisManageService]
      when(manageService.getAgentClient(eqTo(userAnswersId))(any[HeaderCarrier]))
        .thenReturn(Future.successful(Some(AgentClientData(cisId, "840", "MZ00064", None))))
      when(connector.prepopulateContractorKnownFacts(any[String], any[String], any[String])(any[HeaderCarrier]))
        .thenReturn(Future.failed(UpstreamErrorResponse("missing", PRECONDITION_FAILED, PRECONDITION_FAILED)))

      val result = newAction(connector, manageService).invokeBlock(agentRequest, block)

      status(result) mustBe SEE_OTHER
      redirectLocation(result).value mustBe
        controllers.routes.UnauthorisedAgentAffinityController.onPageLoad().url
    }

    "must redirect to the system error page on an unexpected failure" in {
      val connector = mock[ConstructionIndustrySchemeConnector]
      when(connector.prepopulateContractorKnownFacts(any[String], any[String], any[String])(any[HeaderCarrier]))
        .thenReturn(Future.failed(new RuntimeException("boom")))

      val result = newAction(connector).invokeBlock(contractorRequest, block)

      status(result) mustBe SEE_OTHER
      redirectLocation(result).value mustBe controllers.routes.SystemErrorController.onPageLoad().url
    }

    "must redirect to the unauthorised organisation page and not call prepopulate when the cisId is missing" in {
      val connector = mock[ConstructionIndustrySchemeConnector]

      val result = newAction(connector).invokeBlock(contractorRequest(emptyUserAnswers), block)

      status(result) mustBe SEE_OTHER
      redirectLocation(result).value mustBe
        controllers.routes.UnauthorisedOrganisationAffinityController.onPageLoad().url
      verify(connector, never).prepopulateContractorKnownFacts(any[String], any[String], any[String])(any[HeaderCarrier])
    }

    "must redirect when tax office details cannot be resolved" in {
      val connector     = mock[ConstructionIndustrySchemeConnector]
      val manageService = mock[CisManageService]
      when(manageService.getAgentClient(eqTo(userAnswersId))(any[HeaderCarrier]))
        .thenReturn(Future.successful(None))

      val result = newAction(connector, manageService).invokeBlock(agentRequest, block)

      status(result) mustBe SEE_OTHER
      redirectLocation(result).value mustBe
        controllers.routes.UnauthorisedAgentAffinityController.onPageLoad().url
      verify(connector, never).prepopulateContractorKnownFacts(any[String], any[String], any[String])(any[HeaderCarrier])
    }
  }
}
