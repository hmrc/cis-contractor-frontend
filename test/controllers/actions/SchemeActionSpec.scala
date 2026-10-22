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
import models.EmployerReference
import models.requests.CisPath.{CisId, CisOrg}
import models.requests.IdentifierRequest
import models.response.CisTaxpayerResponse
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verifyNoInteractions, when}
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.MessagesApi
import play.api.mvc.Results
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.CisTaxpayerService
import views.html.PageNotFoundView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SchemeActionSpec extends SpecBase with MockitoSugar {

  private val taxpayer = CisTaxpayerResponse(
    uniqueId = "AB12",
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

  private def action(service: CisTaxpayerService) =
    new SchemeActionImpl(
      messagesApi = app.injector.instanceOf[MessagesApi],
      config = applicationConfig,
      cisTaxpayerService = service,
      notFoundView = app.injector.instanceOf[PageNotFoundView]
    )

  "SchemeAction" - {

    "allows an organisation user on org" in {
      val service = mock[CisTaxpayerService]
      when(service.findForOrganisation(using any[IdentifierRequest[?]]())).thenReturn(Future.successful(taxpayer))

      val request = IdentifierRequest(
        FakeRequest("GET", "/org/journey"),
        "user-1",
        Some(EmployerReference("123", "AB456")),
        None
      )

      val result = action(service)(CisOrg).invokeBlock(request, scheme => Future.successful(Results.Ok(scheme.cisId)))

      status(result) mustBe OK
      contentAsString(result) mustBe "AB12"
    }

    "sends an organisation user who opens a client id back to org" in {
      val service = mock[CisTaxpayerService]
      val request = IdentifierRequest(
        FakeRequest("GET", "/AB12/11111111-1111-1111-1111-111111111111/add"),
        "user-1",
        Some(EmployerReference("123", "AB456")),
        None
      )

      val result = action(service)(CisId("AB12")).invokeBlock(request, _ => Future.successful(Results.Ok))

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some("/org/11111111-1111-1111-1111-111111111111/add")
      verifyNoInteractions(service)
    }

    "sends an agent who opens org to the agent account" in {
      val service = mock[CisTaxpayerService]
      val request = IdentifierRequest(
        FakeRequest(),
        "agent-1",
        None,
        Some("A123"),
        isAgent = true,
        agentCode = Some("C1")
      )

      val result = action(service)(CisOrg).invokeBlock(request, _ => Future.successful(Results.Ok))

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(applicationConfig.constructionIndustryAgentAccountUrl)
      verifyNoInteractions(service)
    }
  }
}
