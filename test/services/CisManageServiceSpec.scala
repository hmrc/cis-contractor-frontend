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
import models.response.CisTaxpayerResponse
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, verifyNoInteractions, verifyNoMoreInteractions, when}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.mockito.MockitoSugar.mock
import queries.CisIdQuery
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

final class CisManageServiceSpec extends SpecBase with MockitoSugar {

  implicit val hc: HeaderCarrier    = HeaderCarrier()
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

  "SubcontractorService" - {

    "ensureCisIdInUserAnswers" - {
      "should initialize CisId if it is not in session data" in {
        val mockConnector: ConstructionIndustrySchemeConnector = mock[ConstructionIndustrySchemeConnector]
        val service                                            = new CisManageService(mockConnector)

        val cisId       = 10
        val userAnswers = emptyUserAnswers

        when(mockConnector.getCisTaxpayer()(any[HeaderCarrier]))
          .thenReturn(
            Future.successful(
              CisTaxpayerResponse(
                uniqueId = "10",
                taxOfficeNumber = "taxOfficeNumber",
                taxOfficeRef = "taxOfficeRef",
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
            )
          )

        val result = service.ensureCisIdInUserAnswers(userAnswers)

        val expectedUserAnswers = userAnswers
          .set(CisIdQuery, cisId)
          .success
          .value

        result.futureValue mustBe expectedUserAnswers

        verify(mockConnector).getCisTaxpayer()(any[HeaderCarrier])
        verifyNoMoreInteractions(mockConnector)
      }

      "should return user answers when CisIs is already in session data " in {
        val mockConnector: ConstructionIndustrySchemeConnector = mock[ConstructionIndustrySchemeConnector]
        val service                                            = new CisManageService(mockConnector)

        val cisId               = 10
        val expectedUserAnswers = emptyUserAnswers
          .set(CisIdQuery, cisId)
          .success
          .value

        when(mockConnector.getCisTaxpayer()(any[HeaderCarrier]))
          .thenReturn(
            Future.successful(
              CisTaxpayerResponse(
                uniqueId = "10",
                taxOfficeNumber = "taxOfficeNumber",
                taxOfficeRef = "taxOfficeRef",
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
            )
          )

        val result = service.ensureCisIdInUserAnswers(expectedUserAnswers)

        result.futureValue mustBe expectedUserAnswers

        verifyNoInteractions(mockConnector)
      }

      "should fail when connector returns empty CisId when CisId not in session data" in {
        val mockConnector: ConstructionIndustrySchemeConnector = mock[ConstructionIndustrySchemeConnector]
        val service                                            = new CisManageService(mockConnector)

        val cisId = ""

        when(mockConnector.getCisTaxpayer()(any[HeaderCarrier]))
          .thenReturn(
            Future.successful(
              CisTaxpayerResponse(
                uniqueId = cisId,
                taxOfficeNumber = "taxOfficeNumber",
                taxOfficeRef = "taxOfficeRef",
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
            )
          )

        val exception =
          service.ensureCisIdInUserAnswers(emptyUserAnswers).failed.futureValue

        exception.getMessage must include("Empty cisId (uniqueId) returned from /cis/taxpayer")
      }

      "should fail when connector returns invalid CisId when CisId not in session data" in {
        val mockConnector: ConstructionIndustrySchemeConnector = mock[ConstructionIndustrySchemeConnector]
        val service                                            = new CisManageService(mockConnector)

        val cisId = "value is not Int"

        when(mockConnector.getCisTaxpayer()(any[HeaderCarrier]))
          .thenReturn(
            Future.successful(
              CisTaxpayerResponse(
                uniqueId = cisId,
                taxOfficeNumber = "taxOfficeNumber",
                taxOfficeRef = "taxOfficeRef",
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
            )
          )

        val exception =
          service.ensureCisIdInUserAnswers(emptyUserAnswers).failed.futureValue

        exception.getMessage must include("Invalid data format: cisId (uniqueId) returned from /cis/taxpayer")
      }

      "should fail when the connector call fails" in {
        val mockConnector: ConstructionIndustrySchemeConnector = mock[ConstructionIndustrySchemeConnector]
        val service                                            = new CisManageService(mockConnector)

        when(mockConnector.getCisTaxpayer()(any[HeaderCarrier]))
          .thenReturn(Future.failed(new Exception("bang")))

        val exception =
          service.ensureCisIdInUserAnswers(emptyUserAnswers).failed.futureValue

        exception.getMessage must include("bang")

        verify(mockConnector).getCisTaxpayer()(any[HeaderCarrier])
        verifyNoMoreInteractions(mockConnector)
      }
    }
  }
}
