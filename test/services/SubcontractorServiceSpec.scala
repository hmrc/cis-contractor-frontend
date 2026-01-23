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
import models.add.{SubContactDetails, SubcontractorName, TypeOfSubcontractor, UKAddress}
import models.subcontractor.CreateAndUpdateSubcontractorRequest
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{verify, verifyNoMoreInteractions, when}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.add.*
import queries.CisIdQuery
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

final class SubcontractorServiceSpec extends SpecBase with MockitoSugar {

  implicit val hc: HeaderCarrier    = HeaderCarrier()
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

  "SubcontractorService" - {

    val cisId             = 200

    "createAndUpdateSubcontractor" - {

      "should create and update subcontractor when session data is present with trading name" in {
        val mockConnector: ConstructionIndustrySchemeConnector = mock[ConstructionIndustrySchemeConnector]
        val service                                            = new SubcontractorService(mockConnector)

        val userAnswers = emptyUserAnswers
          .set(CisIdQuery, cisId)
          .success
          .value
          .set(TradingNameOfSubcontractorPage, "trading name")
          .success
          .value
          .set(TypeOfSubcontractorPage, TypeOfSubcontractor.Individualorsoletrader)
          .success
          .value
          .set(
            AddressOfSubcontractorPage,
            UKAddress("addressLine1", Some("addressLine2"), "addressLine3", Some("addressLine4"), "postCode")
          )
          .success
          .value
          .set(SubNationalInsuranceNumberPage, "nino")
          .success
          .value
          .set(SubcontractorsUniqueTaxpayerReferencePage, "utr")
          .success
          .value
          .set(WorksReferenceNumberPage, "workRef")
          .success
          .value
          .set(SubContactDetailsPage, SubContactDetails("email", "phone"))
          .success
          .value

        val expectedUpdateRequest = CreateAndUpdateSubcontractorRequest(
          instanceId = cisId,
          subcontractorType = "soletrader",
          tradingName = Some("trading name"),
          addressLine1 = Some("addressLine1"),
          addressLine2 = Some("addressLine2"),
          addressLine3 = Some("addressLine3"),
          addressLine4 = Some("addressLine4"),
          postcode = Some("postCode"),
          nino = Some("nino"),
          utr = Some("utr"),
          worksReferenceNumber = Some("workRef"),
          emailAddress = Some("email"),
          phoneNumber = Some("phone")
        )

        when(mockConnector.createAndUpdateSubcontractor(any[CreateAndUpdateSubcontractorRequest])(any[HeaderCarrier]))
          .thenReturn(Future.successful(()))

        val result = service.createAndUpdateSubcontractor(userAnswers)

        result.futureValue mustBe ()

        verify(mockConnector).createAndUpdateSubcontractor(eqTo(expectedUpdateRequest))(any[HeaderCarrier])
        verifyNoMoreInteractions(mockConnector)
      }

      "should update subcontractor when session data is present with subcontractor name" in {
        val mockConnector: ConstructionIndustrySchemeConnector = mock[ConstructionIndustrySchemeConnector]
        val service                                            = new SubcontractorService(mockConnector)

        val userAnswers = emptyUserAnswers
          .set(CisIdQuery, cisId)
          .success
          .value
          .set(TypeOfSubcontractorPage, TypeOfSubcontractor.Individualorsoletrader)
          .success
          .value
          .set(SubcontractorNamePage, SubcontractorName("firstname", Some("middle name"), "lastname"))
          .success
          .value
          .set(
            AddressOfSubcontractorPage,
            UKAddress("addressLine1", Some("addressLine2"), "addressLine3", Some("addressLine4"), "postCode")
          )
          .success
          .value
          .set(SubNationalInsuranceNumberPage, "nino")
          .success
          .value
          .set(SubcontractorsUniqueTaxpayerReferencePage, "utr")
          .success
          .value
          .set(WorksReferenceNumberPage, "workRef")
          .success
          .value
          .set(SubContactDetailsPage, SubContactDetails("email", "phone"))
          .success
          .value

        val expectedUpdateRequest = CreateAndUpdateSubcontractorRequest(
          instanceId = cisId,
          subcontractorType = "soletrader",
          firstName = Some("firstname"),
          secondName = Some("middle name"),
          surname = Some("lastname"),
          addressLine1 = Some("addressLine1"),
          addressLine2 = Some("addressLine2"),
          addressLine3 = Some("addressLine3"),
          addressLine4 = Some("addressLine4"),
          postcode = Some("postCode"),
          nino = Some("nino"),
          utr = Some("utr"),
          worksReferenceNumber = Some("workRef"),
          emailAddress = Some("email"),
          phoneNumber = Some("phone")
        )

        when(mockConnector.createAndUpdateSubcontractor(any[CreateAndUpdateSubcontractorRequest])(any[HeaderCarrier]))
          .thenReturn(Future.successful(()))

        val result = service.createAndUpdateSubcontractor(userAnswers)

        result.futureValue mustBe ()

        verify(mockConnector).createAndUpdateSubcontractor(eqTo(expectedUpdateRequest))(any[HeaderCarrier])
        verifyNoMoreInteractions(mockConnector)
      }

      "should fail when cisId not found in session data" in {
        val mockConnector: ConstructionIndustrySchemeConnector = mock[ConstructionIndustrySchemeConnector]
        val service                                            = new SubcontractorService(mockConnector)

        when(mockConnector.createAndUpdateSubcontractor(any[CreateAndUpdateSubcontractorRequest])(any[HeaderCarrier]))
          .thenReturn(Future.failed(new Exception("bang")))

        val exception =
          service.createAndUpdateSubcontractor(emptyUserAnswers).failed.futureValue

        exception.getMessage must include("CisIdQuery not found in session data")
      }

      "should fail when the connector call fails" in {
        val mockConnector: ConstructionIndustrySchemeConnector = mock[ConstructionIndustrySchemeConnector]
        val service                                            = new SubcontractorService(mockConnector)

        when(mockConnector.createAndUpdateSubcontractor(any[CreateAndUpdateSubcontractorRequest])(any[HeaderCarrier]))
          .thenReturn(Future.failed(new Exception("bang")))

        val userAnswers = emptyUserAnswers
          .set(CisIdQuery, cisId)
          .success
          .value
          .set(TypeOfSubcontractorPage, TypeOfSubcontractor.Individualorsoletrader)
          .success
          .value
          .set(TradingNameOfSubcontractorPage, "trading name")
          .success
          .value

        val exception =
          service.createAndUpdateSubcontractor(userAnswers).failed.futureValue

        exception.getMessage must include("bang")

      }
    }
  }
}
