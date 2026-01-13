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
import models.add.{SubcontractorName, TypeOfSubcontractor}
import models.response.CisTaxpayerResponse
import models.subcontractor.{CreateSubcontractorRequest, CreateSubcontractorResponse, UpdateSubcontractorRequest, UpdateSubcontractorResponse}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, verifyNoInteractions, verifyNoMoreInteractions, when}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.add.{SubcontractorNamePage, TradingNameOfSubcontractorPage, TypeOfSubcontractorPage}
import queries.{CisIdQuery, SubbieResourceRefQuery}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

final class SubcontractorServiceSpec extends SpecBase with MockitoSugar {

  implicit val hc: HeaderCarrier    = HeaderCarrier()
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

  "SubcontractorService" - {

    "initializeCisId" - {
      "should initialize CisId if it is not in session data" in {
        val mockConnector: ConstructionIndustrySchemeConnector = mock[ConstructionIndustrySchemeConnector]
        val service                                            = new SubcontractorService(mockConnector)

        val cisId       = "cisId"
        val userAnswers = emptyUserAnswers

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

        val result = service.initializeCisId(userAnswers)

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
        val service                                            = new SubcontractorService(mockConnector)

        val cisId               = "cisId"
        val expectedUserAnswers = emptyUserAnswers
          .set(CisIdQuery, cisId)
          .success
          .value

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

        val result = service.initializeCisId(expectedUserAnswers)

        result.futureValue mustBe expectedUserAnswers

        verifyNoInteractions(mockConnector)
      }

      "should fail when connector returns empty CisId when CisId not in session data" in {
        val mockConnector: ConstructionIndustrySchemeConnector = mock[ConstructionIndustrySchemeConnector]
        val service                                            = new SubcontractorService(mockConnector)

        val cisId       = ""

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
          service.initializeCisId(emptyUserAnswers).failed.futureValue

        exception.getMessage must include("Empty cisId (uniqueId) returned from /cis/taxpayer")
      }

      "should fail when the connector call fails" in {
        val mockConnector: ConstructionIndustrySchemeConnector = mock[ConstructionIndustrySchemeConnector]
        val service                                            = new SubcontractorService(mockConnector)

        when(mockConnector.getCisTaxpayer()(any[HeaderCarrier]))
          .thenReturn(Future.failed(new Exception("bang")))

        val exception =
          service.initializeCisId(emptyUserAnswers).failed.futureValue

        exception.getMessage must include("bang")

        verify(mockConnector).getCisTaxpayer()(any[HeaderCarrier])
        verifyNoMoreInteractions(mockConnector)
      }
    }

    "createSubcontractor" - {
      "should create a subcontractor when a subcontractor type is provided" in {
        val mockConnector: ConstructionIndustrySchemeConnector = mock[ConstructionIndustrySchemeConnector]
        val service                                            = new SubcontractorService(mockConnector)

        val mockSubContractorResourceRef = 10

        val userAnswers = emptyUserAnswers
          .set(CisIdQuery, "cisId")
          .success
          .value
          .set(TypeOfSubcontractorPage, TypeOfSubcontractor.Trust)
          .success
          .value

        when(mockConnector.createSubcontractor(any[CreateSubcontractorRequest])(any[HeaderCarrier]))
          .thenReturn(Future.successful(CreateSubcontractorResponse(subbieResourceRef = mockSubContractorResourceRef)))

        val result = service.createSubcontractor(userAnswers)

        val expectedUserAnswers = userAnswers
          .set(SubbieResourceRefQuery, mockSubContractorResourceRef)
          .success
          .value

        result.futureValue mustBe expectedUserAnswers

        verify(mockConnector).createSubcontractor(any[CreateSubcontractorRequest])(any[HeaderCarrier])
        verifyNoMoreInteractions(mockConnector)
      }

      "should return user answers when if subcontractor is already created " in {
        val mockConnector: ConstructionIndustrySchemeConnector = mock[ConstructionIndustrySchemeConnector]
        val service                                            = new SubcontractorService(mockConnector)

        val mockSubContractorResourceRef = 10

        val userAnswers = emptyUserAnswers
          .set(CisIdQuery, "cisId")
          .success
          .value
          .set(TypeOfSubcontractorPage, TypeOfSubcontractor.Trust)
          .success
          .value
          .set(SubbieResourceRefQuery, mockSubContractorResourceRef)
          .success
          .value

        val result = service.createSubcontractor(userAnswers)

        result.futureValue mustBe userAnswers

        verifyNoInteractions(mockConnector)
      }

      "should fail when cisId not found in session data" in {
        val mockConnector: ConstructionIndustrySchemeConnector = mock[ConstructionIndustrySchemeConnector]
        val service                                            = new SubcontractorService(mockConnector)

        when(mockConnector.createSubcontractor(any[CreateSubcontractorRequest])(any[HeaderCarrier]))
          .thenReturn(Future.failed(new Exception("bang")))

        val exception =
          service.createSubcontractor(emptyUserAnswers).failed.futureValue

        exception.getMessage must include("CisIdQuery not found in session data")
      }

      "should fail when subcontractor type not found in session data" in {
        val mockConnector: ConstructionIndustrySchemeConnector = mock[ConstructionIndustrySchemeConnector]
        val service                                            = new SubcontractorService(mockConnector)

        when(mockConnector.createSubcontractor(any[CreateSubcontractorRequest])(any[HeaderCarrier]))
          .thenReturn(Future.failed(new Exception("bang")))

        val userAnswers = emptyUserAnswers
          .set(CisIdQuery, "cisId")
          .success
          .value

        val exception =
          service.createSubcontractor(userAnswers).failed.futureValue

        exception.getMessage must include("TypeOfSubcontractorPage not found in session data")
      }

      "should fail when the connector call fails" in {
        val mockConnector: ConstructionIndustrySchemeConnector = mock[ConstructionIndustrySchemeConnector]
        val service                                            = new SubcontractorService(mockConnector)

        when(mockConnector.createSubcontractor(any[CreateSubcontractorRequest])(any[HeaderCarrier]))
          .thenReturn(Future.failed(new Exception("bang")))

        val userAnswers = emptyUserAnswers
          .set(CisIdQuery, "cisId")
          .success
          .value
          .set(TypeOfSubcontractorPage, TypeOfSubcontractor.Trust)
          .success
          .value

        val exception =
          service.createSubcontractor(userAnswers).failed.futureValue

        exception.getMessage must include("bang")

        verify(mockConnector).createSubcontractor(any[CreateSubcontractorRequest])(any[HeaderCarrier])
        verifyNoMoreInteractions(mockConnector)
      }
    }

    val subContractorResourceRef = 10
    "updateSubcontractorTradingName" - {

      "should update subcontractor when trading name is provided" in {
        val mockConnector: ConstructionIndustrySchemeConnector = mock[ConstructionIndustrySchemeConnector]
        val service                                            = new SubcontractorService(mockConnector)

        val newVersion  = 20
        val userAnswers = emptyUserAnswers
          .set(CisIdQuery, "cisId")
          .success
          .value
          .set(SubbieResourceRefQuery, subContractorResourceRef)
          .success
          .value
          .set(TradingNameOfSubcontractorPage, "trading name")
          .success
          .value

        val mockResponse = UpdateSubcontractorResponse(newVersion = newVersion)

        when(mockConnector.updateSubcontractor(any[UpdateSubcontractorRequest])(any[HeaderCarrier]))
          .thenReturn(Future.successful(mockResponse))

        val result = service.updateSubcontractorTradingName(userAnswers)

        result.futureValue mustBe mockResponse

        verify(mockConnector).updateSubcontractor(any[UpdateSubcontractorRequest])(any[HeaderCarrier])
        verifyNoMoreInteractions(mockConnector)
      }

      "should fail when cisId not found in session data" in {
        val mockConnector: ConstructionIndustrySchemeConnector = mock[ConstructionIndustrySchemeConnector]
        val service                                            = new SubcontractorService(mockConnector)

        when(mockConnector.updateSubcontractor(any[UpdateSubcontractorRequest])(any[HeaderCarrier]))
          .thenReturn(Future.failed(new Exception("bang")))

        val exception =
          service.updateSubcontractorTradingName(emptyUserAnswers).failed.futureValue

        exception.getMessage must include("CisIdQuery not found in session data")
      }

      "should fail when subcontractor trading name not found in session data" in {
        val mockConnector: ConstructionIndustrySchemeConnector = mock[ConstructionIndustrySchemeConnector]
        val service                                            = new SubcontractorService(mockConnector)

        when(mockConnector.updateSubcontractor(any[UpdateSubcontractorRequest])(any[HeaderCarrier]))
          .thenReturn(Future.failed(new Exception("bang")))

        val userAnswers = emptyUserAnswers
          .set(CisIdQuery, "cisId")
          .success
          .value
          .set(SubbieResourceRefQuery, subContractorResourceRef)
          .success
          .value

        val exception =
          service.updateSubcontractorTradingName(userAnswers).failed.futureValue

        exception.getMessage must include("TradingNameOfSubcontractorPage not found in session data")
      }

      "should fail when the connector call fails" in {
        val mockConnector: ConstructionIndustrySchemeConnector = mock[ConstructionIndustrySchemeConnector]
        val service                                            = new SubcontractorService(mockConnector)

        when(mockConnector.updateSubcontractor(any[UpdateSubcontractorRequest])(any[HeaderCarrier]))
          .thenReturn(Future.failed(new Exception("bang")))

        val userAnswers = emptyUserAnswers
          .set(CisIdQuery, "cisId")
          .success
          .value
          .set(SubbieResourceRefQuery, subContractorResourceRef)
          .success
          .value
          .set(TradingNameOfSubcontractorPage, "trading name")
          .success
          .value

        val exception =
          service.updateSubcontractorTradingName(userAnswers).failed.futureValue

        exception.getMessage must include("bang")

        verify(mockConnector).updateSubcontractor(any[UpdateSubcontractorRequest])(any[HeaderCarrier])
        verifyNoMoreInteractions(mockConnector)
      }
    }

    "updateSubcontractorName" - {

      "should update subcontractor when name is provided" in {
        val mockConnector: ConstructionIndustrySchemeConnector = mock[ConstructionIndustrySchemeConnector]
        val service                                            = new SubcontractorService(mockConnector)

        val newVersion  = 20
        val userAnswers = emptyUserAnswers
          .set(CisIdQuery, "cisId")
          .success
          .value
          .set(SubbieResourceRefQuery, subContractorResourceRef)
          .success
          .value
          .set(SubcontractorNamePage, SubcontractorName("firstname", Some("middle name"), "lastname"))
          .success
          .value

        val mockResponse = UpdateSubcontractorResponse(newVersion = newVersion)

        when(mockConnector.updateSubcontractor(any[UpdateSubcontractorRequest])(any[HeaderCarrier]))
          .thenReturn(Future.successful(mockResponse))

        val result = service.updateSubcontractorName(userAnswers)

        result.futureValue mustBe mockResponse

        verify(mockConnector).updateSubcontractor(any[UpdateSubcontractorRequest])(any[HeaderCarrier])
        verifyNoMoreInteractions(mockConnector)
      }

      "should fail when cisId not found in session data" in {
        val mockConnector: ConstructionIndustrySchemeConnector = mock[ConstructionIndustrySchemeConnector]
        val service                                            = new SubcontractorService(mockConnector)

        when(mockConnector.updateSubcontractor(any[UpdateSubcontractorRequest])(any[HeaderCarrier]))
          .thenReturn(Future.failed(new Exception("bang")))

        val exception =
          service.updateSubcontractorName(emptyUserAnswers).failed.futureValue

        exception.getMessage must include("CisIdQuery not found in session data")
      }

      "should fail when subcontractor name not found in session data" in {
        val mockConnector: ConstructionIndustrySchemeConnector = mock[ConstructionIndustrySchemeConnector]
        val service                                            = new SubcontractorService(mockConnector)

        when(mockConnector.updateSubcontractor(any[UpdateSubcontractorRequest])(any[HeaderCarrier]))
          .thenReturn(Future.failed(new Exception("bang")))

        val userAnswers = emptyUserAnswers
          .set(CisIdQuery, "cisId")
          .success
          .value
          .set(SubbieResourceRefQuery, subContractorResourceRef)
          .success
          .value

        val exception =
          service.updateSubcontractorName(userAnswers).failed.futureValue

        exception.getMessage must include("SubcontractorNamePage not found in session data")
      }

      "should fail when the connector call fails" in {
        val mockConnector: ConstructionIndustrySchemeConnector = mock[ConstructionIndustrySchemeConnector]
        val service                                            = new SubcontractorService(mockConnector)

        when(mockConnector.updateSubcontractor(any[UpdateSubcontractorRequest])(any[HeaderCarrier]))
          .thenReturn(Future.failed(new Exception("bang")))

        val userAnswers = emptyUserAnswers
          .set(CisIdQuery, "cisId")
          .success
          .value
          .set(SubbieResourceRefQuery, subContractorResourceRef)
          .success
          .value
          .set(SubcontractorNamePage, SubcontractorName("firstname", Some("middle name"), "lastname"))
          .success
          .value

        val exception =
          service.updateSubcontractorName(userAnswers).failed.futureValue

        exception.getMessage must include("bang")

        verify(mockConnector).updateSubcontractor(any[UpdateSubcontractorRequest])(any[HeaderCarrier])
        verifyNoMoreInteractions(mockConnector)
      }
    }
  }
}
