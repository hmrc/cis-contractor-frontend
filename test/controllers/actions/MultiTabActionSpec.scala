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
import models.{EmployerReference, UserAnswers}
import models.requests.{DataRequest, IdentifierRequest}
import models.response.CisTaxpayerResponse
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.Results
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import queries.CisIdQuery
import services.CisTaxpayerService

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MultiTabActionSpec extends SpecBase with MockitoSugar {

  private val cisId  = "1045"
  private val flowId = UUID.fromString("11111111-1111-1111-1111-111111111111")

  private val taxpayer = CisTaxpayerResponse(
    uniqueId = cisId,
    taxOfficeNumber = "123",
    taxOfficeRef = "AB456",
    aoDistrict = None,
    aoPayType = None,
    aoCheckCode = None,
    aoReference = None,
    validBusinessAddr = None,
    correlation = None,
    ggAgentId = None,
    employerName1 = None,
    employerName2 = None,
    agentOwnRef = None,
    schemeName = None,
    utr = None,
    enrolledSig = None
  )

  private def organisationRequest =
    IdentifierRequest(FakeRequest(), userAnswersId, Some(EmployerReference("123", "AB456")), None)

  private def agentRequest =
    IdentifierRequest(FakeRequest(), userAnswersId, None, Some("A123"), isAgent = true)

  private def journeyRequest(answers: UserAnswers) =
    DataRequest(FakeRequest(), userAnswersId, answers)

  "MultiTabAction.forStart" - {

    "allows an organisation when the cisId matches" in {
      val service = mock[CisTaxpayerService]
      when(service.findForOrganisation(using any[IdentifierRequest[?]]()))
        .thenReturn(Future.successful(taxpayer))

      val result = new MultiTabAction(service)
        .forStart(cisId)
        .invokeBlock(
          organisationRequest,
          _ => Future.successful(Results.Ok)
        )

      status(result) mustBe OK
    }

    "sends an organisation to Journey Recovery when the cisId does not match" in {
      val service = mock[CisTaxpayerService]
      when(service.findForOrganisation(using any[IdentifierRequest[?]]()))
        .thenReturn(Future.successful(taxpayer.copy(uniqueId = "other")))

      val result = new MultiTabAction(service)
        .forStart(cisId)
        .invokeBlock(
          organisationRequest,
          _ => Future.successful(Results.Ok)
        )

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.JourneyRecoveryController.onPageLoad().url)
    }

    "allows an agent when they are authorised for the client" in {
      val service = mock[CisTaxpayerService]
      when(service.findForAgent(eqTo(cisId))(using any[IdentifierRequest[?]]()))
        .thenReturn(Future.successful(Some(taxpayer)))

      val result = new MultiTabAction(service)
        .forStart(cisId)
        .invokeBlock(
          agentRequest,
          _ => Future.successful(Results.Ok)
        )

      status(result) mustBe OK
    }

    "sends an agent to Journey Recovery when they are not authorised for the client" in {
      val service = mock[CisTaxpayerService]
      when(service.findForAgent(eqTo(cisId))(using any[IdentifierRequest[?]]()))
        .thenReturn(Future.successful(None))

      val result = new MultiTabAction(service)
        .forStart(cisId)
        .invokeBlock(
          agentRequest,
          _ => Future.successful(Results.Ok)
        )

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.JourneyRecoveryController.onPageLoad().url)
    }
  }

  "MultiTabAction.forJourney" - {

    "allows the request when cisId under the flowId is authorised" in {
      val service = mock[CisTaxpayerService]
      when(service.findForOrganisation(using any[IdentifierRequest[?]]()))
        .thenReturn(Future.successful(taxpayer))

      val answers = emptyUserAnswers.set(CisIdQuery(flowId.toString), cisId).success.value

      val result = new MultiTabAction(service)
        .forJourney(flowId)
        .invokeBlock(
          journeyRequest(answers),
          _ => Future.successful(Results.Ok)
        )

      status(result) mustBe OK
    }

    "sends the user to Journey Recovery when cisId is missing under the flowId" in {
      val service = mock[CisTaxpayerService]

      val result = new MultiTabAction(service)
        .forJourney(flowId)
        .invokeBlock(
          journeyRequest(emptyUserAnswers),
          _ => Future.successful(Results.Ok)
        )

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.JourneyRecoveryController.onPageLoad().url)
    }

    "sends the user to Journey Recovery when cisId under the flowId is not authorised" in {
      val service = mock[CisTaxpayerService]
      when(service.findForOrganisation(using any[IdentifierRequest[?]]()))
        .thenReturn(Future.successful(taxpayer.copy(uniqueId = "other")))

      val answers = emptyUserAnswers.set(CisIdQuery(flowId.toString), cisId).success.value

      val result = new MultiTabAction(service)
        .forJourney(flowId)
        .invokeBlock(
          journeyRequest(answers),
          _ => Future.successful(Results.Ok)
        )

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.JourneyRecoveryController.onPageLoad().url)
    }
  }
}
