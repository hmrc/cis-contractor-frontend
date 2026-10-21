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

package services

import base.SpecBase
import connectors.ConstructionIndustrySchemeConnector
import models.EmployerReference
import models.requests.IdentifierRequest
import models.response.CisTaxpayerResponse
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.FakeRequest
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CisTaxpayerServiceSpec extends SpecBase with MockitoSugar {

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

  private val organisation = IdentifierRequest(
    FakeRequest(),
    "user-1",
    Some(EmployerReference("123", "AB456")),
    None
  )

  private val agent = IdentifierRequest(
    FakeRequest(),
    "agent-1",
    None,
    Some("A123"),
    isAgent = true
  )

  "CisTaxpayerService.findForOrganisation" - {

    "returns the client from the backend" in {
      val connector = mock[ConstructionIndustrySchemeConnector]
      when(connector.getCisTaxpayer()(any[HeaderCarrier])).thenReturn(Future.successful(taxpayer))

      val result = new CisTaxpayerService(connector).findForOrganisation(using organisation).futureValue

      result mustBe taxpayer
    }

    "fails when the organisation user has no employer reference" in {
      val connector = mock[ConstructionIndustrySchemeConnector]
      val request   = IdentifierRequest(FakeRequest(), "user-1", None, None)

      val failure = new CisTaxpayerService(connector).findForOrganisation(using request).failed.futureValue

      failure mustBe a[IllegalStateException]
      verify(connector, never).getCisTaxpayer()(any[HeaderCarrier])
    }
  }

  "CisTaxpayerService.findForAgent" - {

    "returns the client from the client list" in {
      val connector = mock[ConstructionIndustrySchemeConnector]
      when(connector.getAllClients(using any[HeaderCarrier]())).thenReturn(Future.successful(List(taxpayer)))

      val result = new CisTaxpayerService(connector).findForAgent("AB12")(using agent).futureValue

      result mustBe Some(taxpayer)
    }

    "returns nothing when the client is not in the list" in {
      val connector = mock[ConstructionIndustrySchemeConnector]
      when(connector.getAllClients(using any[HeaderCarrier]())).thenReturn(Future.successful(List(taxpayer)))

      val result = new CisTaxpayerService(connector).findForAgent("ZZ99")(using agent).futureValue

      result mustBe None
    }
  }
}
