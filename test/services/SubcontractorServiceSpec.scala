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
import models.requests.CreateAndUpdateSubcontractorPayload
import models.requests.CreateAndUpdateSubcontractorPayload.{IndividualOrSoleTraderPayload, PartnershipPayload}
import models.subcontractor.GetSubcontractorUTRsResponse
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{verify, verifyNoMoreInteractions, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.add.*
import pages.add.partnership.*
import queries.CisIdQuery
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

final class SubcontractorServiceSpec extends SpecBase with MockitoSugar {

  implicit val hc: HeaderCarrier    = HeaderCarrier()
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

  "SubcontractorService" - {

    val cisId = "200"

    "createAndUpdateSubcontractor" - {

      "should create and update subcontractor (Individualorsoletrader) when session data is present with trading name" in {
        val mockConnector: ConstructionIndustrySchemeConnector = mock[ConstructionIndustrySchemeConnector]
        val service                                            = new SubcontractorService(mockConnector)

        val userAnswers =
          emptyUserAnswers
            .set(CisIdQuery, cisId)
            .success
            .value
            .set(TypeOfSubcontractorPage, TypeOfSubcontractor.Individualorsoletrader)
            .success
            .value
            .set(TradingNameOfSubcontractorPage, "trading name")
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

        val expectedPayload: CreateAndUpdateSubcontractorPayload =
          IndividualOrSoleTraderPayload(
            cisId = cisId,
            subcontractorType = TypeOfSubcontractor.Individualorsoletrader,
            tradingName = Some("trading name"),
            addressLine1 = Some("addressLine1"),
            addressLine2 = Some("addressLine2"),
            city = Some("addressLine3"),
            county = Some("addressLine4"),
            postcode = Some("postCode"),
            nino = Some("nino"),
            utr = Some("utr"),
            worksReferenceNumber = Some("workRef"),
            emailAddress = Some("email"),
            phoneNumber = Some("phone")
          )

        when(mockConnector.createAndUpdateSubcontractor(any[CreateAndUpdateSubcontractorPayload])(any[HeaderCarrier]))
          .thenReturn(Future.successful(()))

        service.createAndUpdateSubcontractor(userAnswers).futureValue mustBe (())

        verify(mockConnector).createAndUpdateSubcontractor(eqTo(expectedPayload))(any[HeaderCarrier])
        verifyNoMoreInteractions(mockConnector)
      }

      "should create and update subcontractor (Individualorsoletrader) when session data is present with subcontractor name" in {
        val mockConnector: ConstructionIndustrySchemeConnector = mock[ConstructionIndustrySchemeConnector]
        val service                                            = new SubcontractorService(mockConnector)

        val userAnswers =
          emptyUserAnswers
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

        val expectedPayload: CreateAndUpdateSubcontractorPayload =
          IndividualOrSoleTraderPayload(
            cisId = cisId,
            subcontractorType = TypeOfSubcontractor.Individualorsoletrader,
            firstName = Some("firstname"),
            secondName = Some("middle name"),
            surname = Some("lastname"),
            addressLine1 = Some("addressLine1"),
            addressLine2 = Some("addressLine2"),
            city = Some("addressLine3"),
            county = Some("addressLine4"),
            postcode = Some("postCode"),
            nino = Some("nino"),
            utr = Some("utr"),
            worksReferenceNumber = Some("workRef"),
            emailAddress = Some("email"),
            phoneNumber = Some("phone")
          )

        when(mockConnector.createAndUpdateSubcontractor(any[CreateAndUpdateSubcontractorPayload])(any[HeaderCarrier]))
          .thenReturn(Future.successful(()))

        service.createAndUpdateSubcontractor(userAnswers).futureValue mustBe (())

        verify(mockConnector).createAndUpdateSubcontractor(eqTo(expectedPayload))(any[HeaderCarrier])
        verifyNoMoreInteractions(mockConnector)
      }

      "should create and update subcontractor (Partnership) when session data is present" in {
        val mockConnector: ConstructionIndustrySchemeConnector = mock[ConstructionIndustrySchemeConnector]
        val service                                            = new SubcontractorService(mockConnector)

        val partnershipAddress =
          models.add.PartnershipCountryAddress(
            addressLine1 = "p1",
            addressLine2 = Some("p2"),
            addressLine3 = "pCity",
            addressLine4 = Some("pCounty"),
            postalCode = "pPost",
            country = "United Kingdom"
          )

        val userAnswers =
          emptyUserAnswers
            .set(CisIdQuery, cisId)
            .success
            .value
            .set(TypeOfSubcontractorPage, TypeOfSubcontractor.Partnership)
            .success
            .value
            .set(PartnershipUniqueTaxpayerReferencePage, "1234567890")
            .success
            .value
            .set(PartnershipNamePage, "Test Partnership")
            .success
            .value
            .set(PartnershipNominatedPartnerNamePage, "Nominated Partner")
            .success
            .value
            .set(PartnershipNominatedPartnerNinoPage, "AA123456A")
            .success
            .value
            .set(PartnershipNominatedPartnerCrnPage, "AC012345")
            .success
            .value
            .set(PartnershipWorksReferenceNumberPage, "WRN-PTN")
            .success
            .value
            .set(PartnershipAddressPage, partnershipAddress)
            .success
            .value

        val expectedPayload: CreateAndUpdateSubcontractorPayload =
          PartnershipPayload(
            cisId = cisId,
            subcontractorType = TypeOfSubcontractor.Partnership,
            utr = Some("1234567890"),
            firstName = None,
            secondName = None,
            surname = None,
            tradingName = Some("Nominated Partner"),
            nino = Some("AA123456A"),
            crn = Some("AC012345"),
            partnershipTradingName = Some("Test Partnership"),
            addressLine1 = Some("p1"),
            addressLine2 = Some("p2"),
            city = Some("pCity"),
            county = Some("pCounty"),
            postcode = Some("pPost"),
            country = Some("United Kingdom"),
            emailAddress = None,
            phoneNumber = None,
            mobilePhoneNumber = None,
            worksReferenceNumber = Some("WRN-PTN")
          )

        when(mockConnector.createAndUpdateSubcontractor(any[CreateAndUpdateSubcontractorPayload])(any[HeaderCarrier]))
          .thenReturn(Future.successful(()))

        service.createAndUpdateSubcontractor(userAnswers).futureValue mustBe (())

        verify(mockConnector).createAndUpdateSubcontractor(eqTo(expectedPayload))(any[HeaderCarrier])
        verifyNoMoreInteractions(mockConnector)
      }

      "should fail when cisId not found in session data" in {
        val mockConnector: ConstructionIndustrySchemeConnector = mock[ConstructionIndustrySchemeConnector]
        val service                                            = new SubcontractorService(mockConnector)

        val exception =
          service.createAndUpdateSubcontractor(emptyUserAnswers).failed.futureValue

        exception.getMessage must include("CisIdQuery not found in session data")
        verifyNoMoreInteractions(mockConnector)
      }

      "should fail when the connector call fails" in {
        val mockConnector: ConstructionIndustrySchemeConnector = mock[ConstructionIndustrySchemeConnector]
        val service                                            = new SubcontractorService(mockConnector)

        when(mockConnector.createAndUpdateSubcontractor(any[CreateAndUpdateSubcontractorPayload])(any[HeaderCarrier]))
          .thenReturn(Future.failed(new Exception("error")))

        val userAnswers =
          emptyUserAnswers
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

        exception.getMessage must include("error")
      }
    }

    "isDuplicateUTR(" - {

      val cisId                          = "123"
      val utr                            = "1111111111"
      val subcontractorUTRs: Seq[String] = Seq("1111111111", "2222222222")

      "should return true when a duplicate exists" in {
        val mockConnector: ConstructionIndustrySchemeConnector = mock[ConstructionIndustrySchemeConnector]
        val service                                            = new SubcontractorService(mockConnector)

        val userAnswers = emptyUserAnswers.set(CisIdQuery, cisId).success.value

        when(mockConnector.getSubcontractorUTRs(eqTo(cisId.toString))(any[HeaderCarrier]))
          .thenReturn(Future.successful(GetSubcontractorUTRsResponse(subcontractorUTRs = subcontractorUTRs)))

        service.isDuplicateUTR(userAnswers, utr).futureValue mustBe true

        verify(mockConnector).getSubcontractorUTRs(eqTo(cisId.toString))(any[HeaderCarrier])
        verifyNoMoreInteractions(mockConnector)
      }

      "should return false when no duplicate exists" in {
        val mockConnector: ConstructionIndustrySchemeConnector = mock[ConstructionIndustrySchemeConnector]
        val service                                            = new SubcontractorService(mockConnector)

        val userAnswers = emptyUserAnswers.set(CisIdQuery, cisId).success.value

        when(mockConnector.getSubcontractorUTRs(eqTo(cisId.toString))(any[HeaderCarrier]))
          .thenReturn(Future.successful(GetSubcontractorUTRsResponse(subcontractorUTRs = subcontractorUTRs)))

        service.isDuplicateUTR(userAnswers, "88888888").futureValue mustBe false

        verify(mockConnector).getSubcontractorUTRs(eqTo(cisId.toString))(any[HeaderCarrier])
        verifyNoMoreInteractions(mockConnector)
      }

      "should return false when getSubcontractorUTRs return empty list" in {
        val mockConnector: ConstructionIndustrySchemeConnector = mock[ConstructionIndustrySchemeConnector]
        val service                                            = new SubcontractorService(mockConnector)

        val userAnswers = emptyUserAnswers.set(CisIdQuery, cisId).success.value

        when(mockConnector.getSubcontractorUTRs(eqTo(cisId.toString))(any[HeaderCarrier]))
          .thenReturn(Future.successful(GetSubcontractorUTRsResponse(subcontractorUTRs = Seq.empty)))

        service.isDuplicateUTR(userAnswers, utr).futureValue mustBe false

        verify(mockConnector).getSubcontractorUTRs(eqTo(cisId.toString))(any[HeaderCarrier])
        verifyNoMoreInteractions(mockConnector)
      }

      "should fail when cisId not found in session data" in {
        val mockConnector: ConstructionIndustrySchemeConnector = mock[ConstructionIndustrySchemeConnector]
        val service                                            = new SubcontractorService(mockConnector)

        val exception =
          service.isDuplicateUTR(emptyUserAnswers, utr).failed.futureValue

        exception.getMessage must include("CisIdQuery not found in session data")
        verifyNoMoreInteractions(mockConnector)
      }

      "should fail when the connector call fails" in {
        val mockConnector: ConstructionIndustrySchemeConnector = mock[ConstructionIndustrySchemeConnector]
        val service                                            = new SubcontractorService(mockConnector)

        when(mockConnector.getSubcontractorUTRs(eqTo(cisId.toString))(any[HeaderCarrier]))
          .thenReturn(Future.failed(new Exception("error")))

        val userAnswers = emptyUserAnswers.set(CisIdQuery, cisId).success.value

        val exception =
          service.isDuplicateUTR(userAnswers, utr).failed.futureValue

        exception.getMessage must include("error")

        verify(mockConnector).getSubcontractorUTRs(eqTo(cisId.toString))(any[HeaderCarrier])
        verifyNoMoreInteractions(mockConnector)
      }
    }
  }
}
