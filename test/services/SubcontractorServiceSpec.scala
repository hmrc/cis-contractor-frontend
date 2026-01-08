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
import models.add.TypeOfSubcontractor
import models.subContractor.{SubContractorCreateRequest, SubContractorCreateResponse}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, verifyNoMoreInteractions, when}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.add.TypeOfSubcontractorPage
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class SubcontractorServiceSpec extends SpecBase with MockitoSugar {

  implicit val hc: HeaderCarrier    = HeaderCarrier()
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

  "SubcontractorService" - {
    "createSubcontractor" - {
      "should create a subcontractor if a trading name is provided" in {
        val mockConnector: ConstructionIndustrySchemeConnector = mock[ConstructionIndustrySchemeConnector]
        val service                                            = new SubcontractorService(mockConnector)

        val mockSubContractorResourceRef = 10

        when(mockConnector.createSubContractor(any[SubContractorCreateRequest])(any[HeaderCarrier]))
          .thenReturn(Future.successful(SubContractorCreateResponse(subbieResourceRef = mockSubContractorResourceRef)))

        val expectedUserAnswers = emptyUserAnswers
          .set(TypeOfSubcontractorPage, TypeOfSubcontractor.Trust)
          .success
          .value

        val result = service.createSubcontractor(1, expectedUserAnswers)

        result.futureValue mustBe SubContractorCreateResponse(mockSubContractorResourceRef)

        verify(mockConnector).createSubContractor(any[SubContractorCreateRequest])(any[HeaderCarrier])
        verifyNoMoreInteractions(mockConnector)
      }

      "should fail when Subcontractor Type not found in session data" in {
        val mockConnector: ConstructionIndustrySchemeConnector = mock[ConstructionIndustrySchemeConnector]
        val service                                            = new SubcontractorService(mockConnector)

        when(mockConnector.createSubContractor(any[SubContractorCreateRequest])(any[HeaderCarrier]))
          .thenReturn(Future.failed(new Exception("bang")))

        val exception =
          service.createSubcontractor(1, emptyUserAnswers).failed.futureValue

        exception.getMessage must include("Subcontractor Type not found in session data")
      }

      "should fail when the connector call fails" in {
        val mockConnector: ConstructionIndustrySchemeConnector = mock[ConstructionIndustrySchemeConnector]
        val service                                            = new SubcontractorService(mockConnector)

        when(mockConnector.createSubContractor(any[SubContractorCreateRequest])(any[HeaderCarrier]))
          .thenReturn(Future.failed(new Exception("bang")))

        val expectedUserAnswers = emptyUserAnswers
          .set(TypeOfSubcontractorPage, TypeOfSubcontractor.Trust)
          .success
          .value

        val exception =
          service.createSubcontractor(1, expectedUserAnswers).failed.futureValue

        exception.getMessage must include("bang")

        verify(mockConnector).createSubContractor(any[SubContractorCreateRequest])(any[HeaderCarrier])
        verifyNoMoreInteractions(mockConnector)
      }
    }
  }
}
