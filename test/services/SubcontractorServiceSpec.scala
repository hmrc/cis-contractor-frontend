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
import models.{TypeOfSubcontractor, UserAnswers}
import models.add.{IndividualNamesOptions, SubcontractorName}
import models.contact.ContactMethodOptions
import models.address.{Address, Country}
import pages.add.company.*
import models.requests.CreateAndUpdateSubcontractorPayload
import models.requests.CreateAndUpdateSubcontractorPayload.{CompanyPayload, IndividualOrSoleTraderPayload, PartnershipPayload, TrustPayload}
import models.response.{GetSubcontractorResponse, GetSubcontractorUTRsResponse}
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{times, verify, verifyNoMoreInteractions, when}
import pages.add.*
import pages.add.partnership.*
import queries.CisIdQuery
import uk.gov.hmrc.http.HeaderCarrier
import org.mockito.ArgumentCaptor
import org.scalatestplus.mockito.MockitoSugar
import pages.add.trust.*
import models.requests.{SubcontractorRequest, UpdateSubcontractorRequest}
import models.response.SubcontractorResponse
import queries.{AmendSubbieResourceRefQuery, OriginalSubcontractorQuery}

import scala.concurrent.{ExecutionContext, Future}

final class SubcontractorServiceSpec extends SpecBase with MockitoSugar {

  implicit val hc: HeaderCarrier    = HeaderCarrier()
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

  "SubcontractorService" - {

    val cisId = "200"

    "createAndUpdateSubcontractor" - {

      def baseIndividualAnswers = {
        val individualAddress =
          Address(
            addressLine1 = "i1",
            addressLine2 = Some("i2"),
            addressLine3 = Some("London"),
            addressLine4 = Some("Hackney"),
            postcode = Some("N1 5AP"),
            country = Some(Country(Some("GB"), Some("United Kingdom")))
          )

        emptyUserAnswers
          .set(CisIdQuery, cisId)
          .success
          .value
          .set(TypeOfSubcontractorPage, TypeOfSubcontractor.Individualorsoletrader)
          .success
          .value
          .set(AddressOfSubcontractorPage, individualAddress)
          .success
          .value
          .set(AddIndividualContactMethodsYesNoPage, false)
          .success
          .value
          .set(SubNationalInsuranceNumberPage, "AC012345")
          .success
          .value
          .set(SubcontractorsUniqueTaxpayerReferencePage, "1234567890")
          .success
          .value
          .set(WorksReferenceNumberPage, "WRN-IND")
          .success
          .value
      }

      "should create and update subcontractor (Individualorsoletrader) when session data is present with trading name" in {
        val mockConnector: ConstructionIndustrySchemeConnector = mock[ConstructionIndustrySchemeConnector]
        val service                                            = new SubcontractorService(mockConnector)

        val userAnswers =
          baseIndividualAnswers
            .set(TradingNameOfSubcontractorPage, "trading name")
            .success
            .value
            .set(IndividualContactMethodOptionsPage, Set(ContactMethodOptions.Email))
            .success
            .value
            .set(IndividualEmailAddressPage, "i@example.com")
            .success
            .value

        val expectedPayload: CreateAndUpdateSubcontractorPayload =
          IndividualOrSoleTraderPayload(
            cisId = cisId,
            subcontractorType = TypeOfSubcontractor.Individualorsoletrader,
            tradingName = Some("trading name"),
            addressLine1 = Some("i1"),
            addressLine2 = Some("i2"),
            city = Some("London"),
            county = Some("Hackney"),
            postcode = Some("N1 5AP"),
            country = Some("United Kingdom"),
            nino = Some("AC012345"),
            utr = Some("1234567890"),
            worksReferenceNumber = Some("WRN-IND"),
            emailAddress = Some("i@example.com"),
            phoneNumber = None,
            mobilePhoneNumber = None
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
          baseIndividualAnswers
            .set(SubcontractorNamePage, SubcontractorName("firstname", Some("middle name"), "lastname"))
            .success
            .value
            .set(IndividualContactMethodOptionsPage, Set(ContactMethodOptions.Email))
            .success
            .value
            .set(IndividualEmailAddressPage, "i@example.com")
            .success
            .value

        val expectedPayload: CreateAndUpdateSubcontractorPayload =
          IndividualOrSoleTraderPayload(
            cisId = cisId,
            subcontractorType = TypeOfSubcontractor.Individualorsoletrader,
            firstName = Some("firstname"),
            secondName = Some("middle name"),
            surname = Some("lastname"),
            addressLine1 = Some("i1"),
            addressLine2 = Some("i2"),
            city = Some("London"),
            county = Some("Hackney"),
            postcode = Some("N1 5AP"),
            country = Some("United Kingdom"),
            nino = Some("AC012345"),
            utr = Some("1234567890"),
            worksReferenceNumber = Some("WRN-IND"),
            emailAddress = Some("i@example.com"),
            phoneNumber = None,
            mobilePhoneNumber = None
          )

        when(mockConnector.createAndUpdateSubcontractor(any[CreateAndUpdateSubcontractorPayload])(any[HeaderCarrier]))
          .thenReturn(Future.successful(()))

        service.createAndUpdateSubcontractor(userAnswers).futureValue mustBe (())

        verify(mockConnector).createAndUpdateSubcontractor(eqTo(expectedPayload))(any[HeaderCarrier])
        verifyNoMoreInteractions(mockConnector)
      }

      "should create and update subcontractor (Individualorsoletrader) with EMAIL contact details" in {
        val mockConnector: ConstructionIndustrySchemeConnector = mock[ConstructionIndustrySchemeConnector]
        val service                                            = new SubcontractorService(mockConnector)

        val userAnswers =
          baseIndividualAnswers
            .set(TradingNameOfSubcontractorPage, "trading name")
            .success
            .value
            .set(IndividualContactMethodOptionsPage, Set(ContactMethodOptions.Email))
            .success
            .value
            .set(IndividualEmailAddressPage, "i@example.com")
            .success
            .value

        val expectedPayload: CreateAndUpdateSubcontractorPayload =
          IndividualOrSoleTraderPayload(
            cisId = cisId,
            subcontractorType = TypeOfSubcontractor.Individualorsoletrader,
            tradingName = Some("trading name"),
            addressLine1 = Some("i1"),
            addressLine2 = Some("i2"),
            city = Some("London"),
            county = Some("Hackney"),
            postcode = Some("N1 5AP"),
            country = Some("United Kingdom"),
            nino = Some("AC012345"),
            utr = Some("1234567890"),
            worksReferenceNumber = Some("WRN-IND"),
            emailAddress = Some("i@example.com"),
            phoneNumber = None,
            mobilePhoneNumber = None
          )

        when(mockConnector.createAndUpdateSubcontractor(any[CreateAndUpdateSubcontractorPayload])(any[HeaderCarrier]))
          .thenReturn(Future.successful(()))

        service.createAndUpdateSubcontractor(userAnswers).futureValue mustBe (())

        verify(mockConnector).createAndUpdateSubcontractor(eqTo(expectedPayload))(any[HeaderCarrier])
        verifyNoMoreInteractions(mockConnector)
      }

      "should create and update subcontractor (Individualorsoletrader) with PHONE contact details" in {
        val mockConnector: ConstructionIndustrySchemeConnector = mock[ConstructionIndustrySchemeConnector]
        val service                                            = new SubcontractorService(mockConnector)

        val userAnswers =
          baseIndividualAnswers
            .set(TradingNameOfSubcontractorPage, "trading name")
            .success
            .value
            .set(IndividualContactMethodOptionsPage, Set(ContactMethodOptions.Phone))
            .success
            .value
            .set(IndividualPhoneNumberPage, "02071234567")
            .success
            .value

        val expectedPayload: CreateAndUpdateSubcontractorPayload =
          IndividualOrSoleTraderPayload(
            cisId = cisId,
            subcontractorType = TypeOfSubcontractor.Individualorsoletrader,
            tradingName = Some("trading name"),
            addressLine1 = Some("i1"),
            addressLine2 = Some("i2"),
            city = Some("London"),
            county = Some("Hackney"),
            postcode = Some("N1 5AP"),
            country = Some("United Kingdom"),
            nino = Some("AC012345"),
            utr = Some("1234567890"),
            worksReferenceNumber = Some("WRN-IND"),
            emailAddress = None,
            phoneNumber = Some("02071234567"),
            mobilePhoneNumber = None
          )

        when(mockConnector.createAndUpdateSubcontractor(any[CreateAndUpdateSubcontractorPayload])(any[HeaderCarrier]))
          .thenReturn(Future.successful(()))

        service.createAndUpdateSubcontractor(userAnswers).futureValue mustBe (())

        verify(mockConnector).createAndUpdateSubcontractor(eqTo(expectedPayload))(any[HeaderCarrier])
        verifyNoMoreInteractions(mockConnector)
      }

      "should create and update subcontractor (Individualorsoletrader) with MOBILE contact details" in {
        val mockConnector: ConstructionIndustrySchemeConnector = mock[ConstructionIndustrySchemeConnector]
        val service                                            = new SubcontractorService(mockConnector)

        val userAnswers =
          baseIndividualAnswers
            .set(TradingNameOfSubcontractorPage, "trading name")
            .success
            .value
            .set(IndividualContactMethodOptionsPage, Set(ContactMethodOptions.Mobile))
            .success
            .value
            .set(IndividualMobileNumberPage, "07123456789")
            .success
            .value

        val expectedPayload: CreateAndUpdateSubcontractorPayload =
          IndividualOrSoleTraderPayload(
            cisId = cisId,
            subcontractorType = TypeOfSubcontractor.Individualorsoletrader,
            tradingName = Some("trading name"),
            addressLine1 = Some("i1"),
            addressLine2 = Some("i2"),
            city = Some("London"),
            county = Some("Hackney"),
            postcode = Some("N1 5AP"),
            country = Some("United Kingdom"),
            nino = Some("AC012345"),
            utr = Some("1234567890"),
            worksReferenceNumber = Some("WRN-IND"),
            emailAddress = None,
            phoneNumber = None,
            mobilePhoneNumber = Some("07123456789")
          )

        when(mockConnector.createAndUpdateSubcontractor(any[CreateAndUpdateSubcontractorPayload])(any[HeaderCarrier]))
          .thenReturn(Future.successful(()))

        service.createAndUpdateSubcontractor(userAnswers).futureValue mustBe (())

        verify(mockConnector).createAndUpdateSubcontractor(eqTo(expectedPayload))(any[HeaderCarrier])
        verifyNoMoreInteractions(mockConnector)
      }

      "should create and update subcontractor (Individualorsoletrader) with NO contact details (no contact fields sent)" in {
        val mockConnector: ConstructionIndustrySchemeConnector = mock[ConstructionIndustrySchemeConnector]
        val service                                            = new SubcontractorService(mockConnector)

        val userAnswers =
          baseIndividualAnswers
            .set(TradingNameOfSubcontractorPage, "trading name")
            .success
            .value
            .set(AddIndividualContactMethodsYesNoPage, false)
            .success
            .value

        val expectedPayload: CreateAndUpdateSubcontractorPayload =
          IndividualOrSoleTraderPayload(
            cisId = cisId,
            subcontractorType = TypeOfSubcontractor.Individualorsoletrader,
            tradingName = Some("trading name"),
            addressLine1 = Some("i1"),
            addressLine2 = Some("i2"),
            city = Some("London"),
            county = Some("Hackney"),
            postcode = Some("N1 5AP"),
            country = Some("United Kingdom"),
            nino = Some("AC012345"),
            utr = Some("1234567890"),
            worksReferenceNumber = Some("WRN-IND"),
            emailAddress = None,
            phoneNumber = None,
            mobilePhoneNumber = None
          )

        when(mockConnector.createAndUpdateSubcontractor(any[CreateAndUpdateSubcontractorPayload])(any[HeaderCarrier]))
          .thenReturn(Future.successful(()))

        service.createAndUpdateSubcontractor(userAnswers).futureValue mustBe (())

        verify(mockConnector).createAndUpdateSubcontractor(eqTo(expectedPayload))(any[HeaderCarrier])
        verifyNoMoreInteractions(mockConnector)
      }

      "should create and update subcontractor (Individualorsoletrader) when contact options are missing (no contact fields sent)" in {
        val mockConnector: ConstructionIndustrySchemeConnector = mock[ConstructionIndustrySchemeConnector]
        val service                                            = new SubcontractorService(mockConnector)

        val userAnswers =
          baseIndividualAnswers
            .set(TradingNameOfSubcontractorPage, "trading name")
            .success
            .value
            .set(IndividualContactMethodOptionsPage, Set(ContactMethodOptions.Email))
            .success
            .value

        val expectedPayload: CreateAndUpdateSubcontractorPayload =
          IndividualOrSoleTraderPayload(
            cisId = cisId,
            subcontractorType = TypeOfSubcontractor.Individualorsoletrader,
            tradingName = Some("trading name"),
            addressLine1 = Some("i1"),
            addressLine2 = Some("i2"),
            city = Some("London"),
            county = Some("Hackney"),
            postcode = Some("N1 5AP"),
            country = Some("United Kingdom"),
            nino = Some("AC012345"),
            utr = Some("1234567890"),
            worksReferenceNumber = Some("WRN-IND"),
            emailAddress = None,
            phoneNumber = None,
            mobilePhoneNumber = None
          )

        when(mockConnector.createAndUpdateSubcontractor(any[CreateAndUpdateSubcontractorPayload])(any[HeaderCarrier]))
          .thenReturn(Future.successful(()))

        service.createAndUpdateSubcontractor(userAnswers).futureValue mustBe (())

        verify(mockConnector).createAndUpdateSubcontractor(eqTo(expectedPayload))(any[HeaderCarrier])
        verifyNoMoreInteractions(mockConnector)
      }

      def basePartnershipAnswers = {
        val partnershipAddress =
          Address(
            addressLine1 = "p1",
            addressLine2 = Some("p2"),
            addressLine3 = Some("London"),
            addressLine4 = Some("Hackney"),
            postcode = Some("N1 5AP"),
            country = Some(Country(Some("GB"), Some("United Kingdom")))
          )

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
      }

      "should create and update subcontractor (Partnership) with EMAIL contact details" in {
        val mockConnector = mock[ConstructionIndustrySchemeConnector]
        val service       = new SubcontractorService(mockConnector)

        val userAnswers =
          basePartnershipAnswers
            .set(AddPartnershipContactMethodsYesNoPage, true)
            .success
            .value
            .set(PartnershipContactMethodOptionsPage, Set(ContactMethodOptions.Email))
            .success
            .value
            .set(PartnershipEmailAddressPage, "p@example.com")
            .success
            .value

        val expectedPayload: CreateAndUpdateSubcontractorPayload =
          PartnershipPayload(
            cisId = cisId,
            subcontractorType = TypeOfSubcontractor.Partnership,
            utr = Some("1234567890"),
            partnerUtr = None,
            partnershipTradingName = Some("Test Partnership"),
            partnerTradingName = Some("Nominated Partner"),
            partnerNino = Some("AA123456A"),
            partnerCrn = Some("AC012345"),
            addressLine1 = Some("p1"),
            addressLine2 = Some("p2"),
            city = Some("London"),
            county = Some("Hackney"),
            postcode = Some("N1 5AP"),
            country = Some("United Kingdom"),
            emailAddress = Some("p@example.com"),
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

      "should create and update subcontractor (Partnership) with PHONE contact details" in {
        val mockConnector = mock[ConstructionIndustrySchemeConnector]
        val service       = new SubcontractorService(mockConnector)

        val userAnswers =
          basePartnershipAnswers
            .set(AddPartnershipContactMethodsYesNoPage, true)
            .success
            .value
            .set(PartnershipContactMethodOptionsPage, Set(ContactMethodOptions.Phone))
            .success
            .value
            .set(PartnershipPhoneNumberPage, "02071234567")
            .success
            .value

        val expectedPayload: CreateAndUpdateSubcontractorPayload =
          PartnershipPayload(
            cisId = cisId,
            subcontractorType = TypeOfSubcontractor.Partnership,
            utr = Some("1234567890"),
            partnerUtr = None,
            partnershipTradingName = Some("Test Partnership"),
            partnerTradingName = Some("Nominated Partner"),
            partnerNino = Some("AA123456A"),
            partnerCrn = Some("AC012345"),
            addressLine1 = Some("p1"),
            addressLine2 = Some("p2"),
            city = Some("London"),
            county = Some("Hackney"),
            postcode = Some("N1 5AP"),
            country = Some("United Kingdom"),
            emailAddress = None,
            phoneNumber = Some("02071234567"),
            mobilePhoneNumber = None,
            worksReferenceNumber = Some("WRN-PTN")
          )

        when(mockConnector.createAndUpdateSubcontractor(any[CreateAndUpdateSubcontractorPayload])(any[HeaderCarrier]))
          .thenReturn(Future.successful(()))

        service.createAndUpdateSubcontractor(userAnswers).futureValue mustBe (())

        verify(mockConnector).createAndUpdateSubcontractor(eqTo(expectedPayload))(any[HeaderCarrier])
        verifyNoMoreInteractions(mockConnector)
      }

      "should create and update subcontractor (Partnership) with MOBILE contact details" in {
        val mockConnector = mock[ConstructionIndustrySchemeConnector]
        val service       = new SubcontractorService(mockConnector)

        val userAnswers =
          basePartnershipAnswers
            .set(AddPartnershipContactMethodsYesNoPage, true)
            .success
            .value
            .set(PartnershipContactMethodOptionsPage, Set(ContactMethodOptions.Mobile))
            .success
            .value
            .set(PartnershipMobileNumberPage, "07123456789")
            .success
            .value

        val expectedPayload: CreateAndUpdateSubcontractorPayload =
          PartnershipPayload(
            cisId = cisId,
            subcontractorType = TypeOfSubcontractor.Partnership,
            utr = Some("1234567890"),
            partnerUtr = None,
            partnershipTradingName = Some("Test Partnership"),
            partnerTradingName = Some("Nominated Partner"),
            partnerNino = Some("AA123456A"),
            partnerCrn = Some("AC012345"),
            addressLine1 = Some("p1"),
            addressLine2 = Some("p2"),
            city = Some("London"),
            county = Some("Hackney"),
            postcode = Some("N1 5AP"),
            country = Some("United Kingdom"),
            emailAddress = None,
            phoneNumber = None,
            mobilePhoneNumber = Some("07123456789"),
            worksReferenceNumber = Some("WRN-PTN")
          )

        when(mockConnector.createAndUpdateSubcontractor(any[CreateAndUpdateSubcontractorPayload])(any[HeaderCarrier]))
          .thenReturn(Future.successful(()))

        service.createAndUpdateSubcontractor(userAnswers).futureValue mustBe (())

        verify(mockConnector).createAndUpdateSubcontractor(eqTo(expectedPayload))(any[HeaderCarrier])
        verifyNoMoreInteractions(mockConnector)
      }

      "should create and update subcontractor (Partnership) with NO contact details (no contact fields sent)" in {
        val mockConnector = mock[ConstructionIndustrySchemeConnector]
        val service       = new SubcontractorService(mockConnector)

        val userAnswers =
          basePartnershipAnswers
            .set(AddPartnershipContactMethodsYesNoPage, false)
            .success
            .value

        when(mockConnector.createAndUpdateSubcontractor(any[CreateAndUpdateSubcontractorPayload])(any[HeaderCarrier]))
          .thenReturn(Future.successful(()))

        service.createAndUpdateSubcontractor(userAnswers).futureValue mustBe (())

        val captor: ArgumentCaptor[CreateAndUpdateSubcontractorPayload] =
          ArgumentCaptor.forClass(classOf[CreateAndUpdateSubcontractorPayload])

        verify(mockConnector, times(1))
          .createAndUpdateSubcontractor(captor.capture())(any[HeaderCarrier])

        val sent = captor.getValue

        sent mustBe a[PartnershipPayload]

        val partnershipSent = sent.asInstanceOf[PartnershipPayload]

        partnershipSent mustBe PartnershipPayload(
          cisId = cisId,
          subcontractorType = TypeOfSubcontractor.Partnership,
          utr = Some("1234567890"),
          partnerUtr = None,
          partnershipTradingName = Some("Test Partnership"),
          partnerTradingName = Some("Nominated Partner"),
          partnerNino = Some("AA123456A"),
          partnerCrn = Some("AC012345"),
          addressLine1 = Some("p1"),
          addressLine2 = Some("p2"),
          city = Some("London"),
          county = Some("Hackney"),
          postcode = Some("N1 5AP"),
          country = Some("United Kingdom"),
          emailAddress = None,
          phoneNumber = None,
          mobilePhoneNumber = None,
          worksReferenceNumber = Some("WRN-PTN")
        )

        verifyNoMoreInteractions(mockConnector)
      }

      "should create and update subcontractor (Partnership) when contact options are missing (no contact fields sent)" in {
        val mockConnector = mock[ConstructionIndustrySchemeConnector]
        val service       = new SubcontractorService(mockConnector)

        val userAnswers =
          basePartnershipAnswers
            .set(AddPartnershipContactMethodsYesNoPage, true)
            .success
            .value
            .remove(AddPartnershipContactMethodsYesNoPage)
            .success
            .value

        when(mockConnector.createAndUpdateSubcontractor(any[CreateAndUpdateSubcontractorPayload])(any[HeaderCarrier]))
          .thenReturn(Future.successful(()))

        service.createAndUpdateSubcontractor(userAnswers).futureValue mustBe (())

        val captor: ArgumentCaptor[CreateAndUpdateSubcontractorPayload] =
          ArgumentCaptor.forClass(classOf[CreateAndUpdateSubcontractorPayload])

        verify(mockConnector, times(1))
          .createAndUpdateSubcontractor(captor.capture())(any[HeaderCarrier])

        val sent = captor.getValue

        sent mustBe a[PartnershipPayload]

        val partnershipSent = sent.asInstanceOf[PartnershipPayload]

        partnershipSent mustBe PartnershipPayload(
          cisId = cisId,
          subcontractorType = TypeOfSubcontractor.Partnership,
          utr = Some("1234567890"),
          partnerUtr = None,
          partnershipTradingName = Some("Test Partnership"),
          partnerTradingName = Some("Nominated Partner"),
          partnerNino = Some("AA123456A"),
          partnerCrn = Some("AC012345"),
          addressLine1 = Some("p1"),
          addressLine2 = Some("p2"),
          city = Some("London"),
          county = Some("Hackney"),
          postcode = Some("N1 5AP"),
          country = Some("United Kingdom"),
          emailAddress = None,
          phoneNumber = None,
          mobilePhoneNumber = None,
          worksReferenceNumber = Some("WRN-PTN")
        )

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

      def baseCompanyAnswers = {
        val companyAddress =
          Address(
            addressLine1 = "c1",
            addressLine2 = Some("c2"),
            addressLine3 = Some("London"),
            addressLine4 = Some("Hackney"),
            postcode = Some("E1 6AN"),
            country = Some(Country(Some("GB"), Some("United Kingdom")))
          )

        emptyUserAnswers
          .set(CisIdQuery, cisId)
          .success
          .value
          .set(TypeOfSubcontractorPage, TypeOfSubcontractor.Limitedcompany)
          .success
          .value
          .set(CompanyNamePage, "Test Company Ltd")
          .success
          .value
          .set(CompanyAddressYesNoPage, true)
          .success
          .value
          .set(CompanyAddressPage, companyAddress)
          .success
          .value
          .set(CompanyUtrYesNoPage, true)
          .success
          .value
          .set(CompanyUtrPage, "1234567890")
          .success
          .value
          .set(CompanyCrnYesNoPage, true)
          .success
          .value
          .set(CompanyCrnPage, "AC012345")
          .success
          .value
          .set(CompanyWorksReferenceYesNoPage, true)
          .success
          .value
          .set(CompanyWorksReferencePage, "WRN-CMP")
          .success
          .value
      }

      "should create and update subcontractor (Company) with EMAIL contact details" in {
        val mockConnector = mock[ConstructionIndustrySchemeConnector]
        val service       = new SubcontractorService(mockConnector)

        val userAnswers =
          baseCompanyAnswers
            .set(AddCompanyContactMethodsYesNoPage, true)
            .success
            .value
            .set(CompanyContactMethodOptionsPage, Set(ContactMethodOptions.Email))
            .success
            .value
            .set(CompanyEmailAddressPage, "c@example.com")
            .success
            .value

        val expectedPayload: CreateAndUpdateSubcontractorPayload =
          CompanyPayload(
            cisId = cisId,
            subcontractorType = TypeOfSubcontractor.Limitedcompany,
            utr = Some("1234567890"),
            crn = Some("AC012345"),
            tradingName = Some("Test Company Ltd"),
            addressLine1 = Some("c1"),
            addressLine2 = Some("c2"),
            city = Some("London"),
            county = Some("Hackney"),
            country = Some("United Kingdom"),
            postcode = Some("E1 6AN"),
            emailAddress = Some("c@example.com"),
            phoneNumber = None,
            mobilePhoneNumber = None,
            worksReferenceNumber = Some("WRN-CMP")
          )

        when(mockConnector.createAndUpdateSubcontractor(any[CreateAndUpdateSubcontractorPayload])(any[HeaderCarrier]))
          .thenReturn(Future.successful(()))

        service.createAndUpdateSubcontractor(userAnswers).futureValue mustBe (())

        verify(mockConnector).createAndUpdateSubcontractor(eqTo(expectedPayload))(any[HeaderCarrier])
        verifyNoMoreInteractions(mockConnector)
      }

      "should create and update subcontractor (Company) with PHONE contact details" in {
        val mockConnector = mock[ConstructionIndustrySchemeConnector]
        val service       = new SubcontractorService(mockConnector)

        val userAnswers =
          baseCompanyAnswers
            .set(AddCompanyContactMethodsYesNoPage, true)
            .success
            .value
            .set(CompanyContactMethodOptionsPage, Set(ContactMethodOptions.Phone))
            .success
            .value
            .set(CompanyPhoneNumberPage, "02071234567")
            .success
            .value

        val expectedPayload: CreateAndUpdateSubcontractorPayload =
          CompanyPayload(
            cisId = cisId,
            subcontractorType = TypeOfSubcontractor.Limitedcompany,
            utr = Some("1234567890"),
            crn = Some("AC012345"),
            tradingName = Some("Test Company Ltd"),
            addressLine1 = Some("c1"),
            addressLine2 = Some("c2"),
            city = Some("London"),
            county = Some("Hackney"),
            country = Some("United Kingdom"),
            postcode = Some("E1 6AN"),
            emailAddress = None,
            phoneNumber = Some("02071234567"),
            mobilePhoneNumber = None,
            worksReferenceNumber = Some("WRN-CMP")
          )

        when(mockConnector.createAndUpdateSubcontractor(any[CreateAndUpdateSubcontractorPayload])(any[HeaderCarrier]))
          .thenReturn(Future.successful(()))

        service.createAndUpdateSubcontractor(userAnswers).futureValue mustBe (())

        verify(mockConnector).createAndUpdateSubcontractor(eqTo(expectedPayload))(any[HeaderCarrier])
        verifyNoMoreInteractions(mockConnector)
      }

      "should create and update subcontractor (Company) with MOBILE contact details" in {
        val mockConnector = mock[ConstructionIndustrySchemeConnector]
        val service       = new SubcontractorService(mockConnector)

        val userAnswers =
          baseCompanyAnswers
            .set(AddCompanyContactMethodsYesNoPage, true)
            .success
            .value
            .set(CompanyContactMethodOptionsPage, Set(ContactMethodOptions.Mobile))
            .success
            .value
            .set(CompanyMobileNumberPage, "07123456789")
            .success
            .value

        val expectedPayload: CreateAndUpdateSubcontractorPayload =
          CompanyPayload(
            cisId = cisId,
            subcontractorType = TypeOfSubcontractor.Limitedcompany,
            utr = Some("1234567890"),
            crn = Some("AC012345"),
            tradingName = Some("Test Company Ltd"),
            addressLine1 = Some("c1"),
            addressLine2 = Some("c2"),
            city = Some("London"),
            county = Some("Hackney"),
            country = Some("United Kingdom"),
            postcode = Some("E1 6AN"),
            emailAddress = None,
            phoneNumber = None,
            mobilePhoneNumber = Some("07123456789"),
            worksReferenceNumber = Some("WRN-CMP")
          )

        when(mockConnector.createAndUpdateSubcontractor(any[CreateAndUpdateSubcontractorPayload])(any[HeaderCarrier]))
          .thenReturn(Future.successful(()))

        service.createAndUpdateSubcontractor(userAnswers).futureValue mustBe (())

        verify(mockConnector).createAndUpdateSubcontractor(eqTo(expectedPayload))(any[HeaderCarrier])
        verifyNoMoreInteractions(mockConnector)
      }

      "should create and update subcontractor (Company) with NO contact details (no contact fields sent)" in {
        val mockConnector = mock[ConstructionIndustrySchemeConnector]
        val service       = new SubcontractorService(mockConnector)

        val userAnswers =
          baseCompanyAnswers
            .set(AddCompanyContactMethodsYesNoPage, false)
            .success
            .value

        when(mockConnector.createAndUpdateSubcontractor(any[CreateAndUpdateSubcontractorPayload])(any[HeaderCarrier]))
          .thenReturn(Future.successful(()))

        service.createAndUpdateSubcontractor(userAnswers).futureValue mustBe (())

        val captor: ArgumentCaptor[CreateAndUpdateSubcontractorPayload] =
          ArgumentCaptor.forClass(classOf[CreateAndUpdateSubcontractorPayload])

        verify(mockConnector, times(1))
          .createAndUpdateSubcontractor(captor.capture())(any[HeaderCarrier])

        val sent = captor.getValue
        sent mustBe a[CompanyPayload]

        val companySent = sent.asInstanceOf[CompanyPayload]
        companySent.emailAddress mustBe None
        companySent.phoneNumber mustBe None
        companySent.mobilePhoneNumber mustBe None

        verifyNoMoreInteractions(mockConnector)
      }

      "should create and update subcontractor (Company) when contact options are missing (no contact fields sent)" in {
        val mockConnector = mock[ConstructionIndustrySchemeConnector]
        val service       = new SubcontractorService(mockConnector)

        val userAnswers =
          baseCompanyAnswers
            .set(AddCompanyContactMethodsYesNoPage, true)
            .success
            .value
            .remove(AddCompanyContactMethodsYesNoPage)
            .success
            .value

        when(mockConnector.createAndUpdateSubcontractor(any[CreateAndUpdateSubcontractorPayload])(any[HeaderCarrier]))
          .thenReturn(Future.successful(()))

        service.createAndUpdateSubcontractor(userAnswers).futureValue mustBe (())

        val captor: ArgumentCaptor[CreateAndUpdateSubcontractorPayload] =
          ArgumentCaptor.forClass(classOf[CreateAndUpdateSubcontractorPayload])

        verify(mockConnector, times(1))
          .createAndUpdateSubcontractor(captor.capture())(any[HeaderCarrier])

        val sent = captor.getValue
        sent mustBe a[CompanyPayload]

        val companySent = sent.asInstanceOf[CompanyPayload]
        companySent.emailAddress mustBe None
        companySent.phoneNumber mustBe None
        companySent.mobilePhoneNumber mustBe None

        verifyNoMoreInteractions(mockConnector)
      }

      def baseTrustAnswers = {
        val trustAddress =
          Address(
            addressLine1 = "t1",
            addressLine2 = Some("t2"),
            addressLine3 = Some("London"),
            addressLine4 = Some("Hackney"),
            postcode = Some("N1 5AP"),
            country = Some(Country(Some("GB"), Some("United Kingdom")))
          )

        emptyUserAnswers
          .set(CisIdQuery, cisId)
          .success
          .value
          .set(TypeOfSubcontractorPage, TypeOfSubcontractor.Trust)
          .success
          .value
          .set(TrustNamePage, "Test Trust")
          .success
          .value
          .set(TrustAddressYesNoPage, true)
          .success
          .value
          .set(TrustAddressPage, trustAddress)
          .success
          .value
          .set(TrustUtrYesNoPage, true)
          .success
          .value
          .set(TrustUtrPage, "1234567890")
          .success
          .value
          .set(TrustWorksReferenceYesNoPage, true)
          .success
          .value
          .set(TrustWorksReferencePage, "WRN-TRUST")
          .success
          .value
      }

      "should create and update subcontractor (Trust) with EMAIL contact details" in {
        val mockConnector = mock[ConstructionIndustrySchemeConnector]
        val service       = new SubcontractorService(mockConnector)

        val userAnswers =
          baseTrustAnswers
            .set(AddTrustContactMethodsYesNoPage, true)
            .success
            .value
            .set(TrustContactMethodOptionsPage, Set(ContactMethodOptions.Email))
            .success
            .value
            .set(TrustEmailAddressPage, "t@example.com")
            .success
            .value

        val expectedPayload: CreateAndUpdateSubcontractorPayload =
          TrustPayload(
            cisId = cisId,
            subcontractorType = TypeOfSubcontractor.Trust,
            trustTradingName = Some("Test Trust"),
            utr = Some("1234567890"),
            addressLine1 = Some("t1"),
            addressLine2 = Some("t2"),
            city = Some("London"),
            county = Some("Hackney"),
            postcode = Some("N1 5AP"),
            country = Some("United Kingdom"),
            emailAddress = Some("t@example.com"),
            phoneNumber = None,
            mobilePhoneNumber = None,
            worksReferenceNumber = Some("WRN-TRUST")
          )

        when(mockConnector.createAndUpdateSubcontractor(any[CreateAndUpdateSubcontractorPayload])(any[HeaderCarrier]))
          .thenReturn(Future.successful(()))

        service.createAndUpdateSubcontractor(userAnswers).futureValue mustBe ()

        verify(mockConnector).createAndUpdateSubcontractor(eqTo(expectedPayload))(any[HeaderCarrier])
        verifyNoMoreInteractions(mockConnector)
      }

      "should create and update subcontractor (Trust) with PHONE contact details" in {
        val mockConnector = mock[ConstructionIndustrySchemeConnector]
        val service       = new SubcontractorService(mockConnector)

        val userAnswers =
          baseTrustAnswers
            .set(AddTrustContactMethodsYesNoPage, true)
            .success
            .value
            .set(TrustContactMethodOptionsPage, Set(ContactMethodOptions.Phone))
            .success
            .value
            .set(TrustPhoneNumberPage, "02071234567")
            .success
            .value

        val expectedPayload: CreateAndUpdateSubcontractorPayload =
          TrustPayload(
            cisId = cisId,
            subcontractorType = TypeOfSubcontractor.Trust,
            trustTradingName = Some("Test Trust"),
            utr = Some("1234567890"),
            addressLine1 = Some("t1"),
            addressLine2 = Some("t2"),
            city = Some("London"),
            county = Some("Hackney"),
            postcode = Some("N1 5AP"),
            country = Some("United Kingdom"),
            emailAddress = None,
            phoneNumber = Some("02071234567"),
            mobilePhoneNumber = None,
            worksReferenceNumber = Some("WRN-TRUST")
          )

        when(mockConnector.createAndUpdateSubcontractor(any[CreateAndUpdateSubcontractorPayload])(any[HeaderCarrier]))
          .thenReturn(Future.successful(()))

        service.createAndUpdateSubcontractor(userAnswers).futureValue mustBe ()

        verify(mockConnector).createAndUpdateSubcontractor(eqTo(expectedPayload))(any[HeaderCarrier])
        verifyNoMoreInteractions(mockConnector)
      }

      "should create and update subcontractor (Trust) with MOBILE contact details" in {
        val mockConnector = mock[ConstructionIndustrySchemeConnector]
        val service       = new SubcontractorService(mockConnector)

        val userAnswers =
          baseTrustAnswers
            .set(AddTrustContactMethodsYesNoPage, true)
            .success
            .value
            .set(TrustContactMethodOptionsPage, Set(ContactMethodOptions.Mobile))
            .success
            .value
            .set(TrustMobileNumberPage, "07123456789")
            .success
            .value

        val expectedPayload: CreateAndUpdateSubcontractorPayload =
          TrustPayload(
            cisId = cisId,
            subcontractorType = TypeOfSubcontractor.Trust,
            trustTradingName = Some("Test Trust"),
            utr = Some("1234567890"),
            addressLine1 = Some("t1"),
            addressLine2 = Some("t2"),
            city = Some("London"),
            county = Some("Hackney"),
            postcode = Some("N1 5AP"),
            country = Some("United Kingdom"),
            emailAddress = None,
            phoneNumber = None,
            mobilePhoneNumber = Some("07123456789"),
            worksReferenceNumber = Some("WRN-TRUST")
          )

        when(mockConnector.createAndUpdateSubcontractor(any[CreateAndUpdateSubcontractorPayload])(any[HeaderCarrier]))
          .thenReturn(Future.successful(()))

        service.createAndUpdateSubcontractor(userAnswers).futureValue mustBe ()

        verify(mockConnector).createAndUpdateSubcontractor(eqTo(expectedPayload))(any[HeaderCarrier])
        verifyNoMoreInteractions(mockConnector)
      }

      "should create and update subcontractor (Trust) with NO contact details (no contact fields sent)" in {
        val mockConnector = mock[ConstructionIndustrySchemeConnector]
        val service       = new SubcontractorService(mockConnector)

        val userAnswers =
          baseTrustAnswers
            .set(AddTrustContactMethodsYesNoPage, false)
            .success
            .value

        when(mockConnector.createAndUpdateSubcontractor(any[CreateAndUpdateSubcontractorPayload])(any[HeaderCarrier]))
          .thenReturn(Future.successful(()))

        service.createAndUpdateSubcontractor(userAnswers).futureValue mustBe ()

        val captor: ArgumentCaptor[CreateAndUpdateSubcontractorPayload] =
          ArgumentCaptor.forClass(classOf[CreateAndUpdateSubcontractorPayload])

        verify(mockConnector, times(1))
          .createAndUpdateSubcontractor(captor.capture())(any[HeaderCarrier])

        val sent = captor.getValue
        sent mustBe a[TrustPayload]

        val trustSent = sent.asInstanceOf[TrustPayload]
        trustSent.emailAddress mustBe None
        trustSent.phoneNumber mustBe None
        trustSent.mobilePhoneNumber mustBe None

        verifyNoMoreInteractions(mockConnector)
      }

      "should create and update subcontractor (Trust) when contact options are missing (no contact fields sent)" in {
        val mockConnector = mock[ConstructionIndustrySchemeConnector]
        val service       = new SubcontractorService(mockConnector)

        val userAnswers =
          baseTrustAnswers
            .set(AddTrustContactMethodsYesNoPage, true)
            .success
            .value
            .remove(AddTrustContactMethodsYesNoPage)
            .success
            .value

        when(mockConnector.createAndUpdateSubcontractor(any[CreateAndUpdateSubcontractorPayload])(any[HeaderCarrier]))
          .thenReturn(Future.successful(()))

        service.createAndUpdateSubcontractor(userAnswers).futureValue mustBe ()

        val captor: ArgumentCaptor[CreateAndUpdateSubcontractorPayload] =
          ArgumentCaptor.forClass(classOf[CreateAndUpdateSubcontractorPayload])

        verify(mockConnector, times(1))
          .createAndUpdateSubcontractor(captor.capture())(any[HeaderCarrier])

        val sent = captor.getValue
        sent mustBe a[TrustPayload]

        val trustSent = sent.asInstanceOf[TrustPayload]
        trustSent.emailAddress mustBe None
        trustSent.phoneNumber mustBe None
        trustSent.mobilePhoneNumber mustBe None

        verifyNoMoreInteractions(mockConnector)
      }

    }

    "isDuplicateUTR(" - {

      val cisId                          = "123"
      val utr                            = "1111111111"
      val subcontractorUTRs: Seq[String] = Seq("1111111111", "2222222222")

      "should return true when a duplicate exists" in {
        val mockConnector = mock[ConstructionIndustrySchemeConnector]
        val service       = new SubcontractorService(mockConnector)

        val userAnswers = emptyUserAnswers.set(CisIdQuery, cisId).success.value

        when(mockConnector.getSubcontractorUTRs(eqTo(cisId.toString))(any[HeaderCarrier]))
          .thenReturn(Future.successful(GetSubcontractorUTRsResponse(subcontractorUTRs = subcontractorUTRs)))

        service.isDuplicateUTR(userAnswers, utr).futureValue mustBe true

        verify(mockConnector).getSubcontractorUTRs(eqTo(cisId.toString))(any[HeaderCarrier])
        verifyNoMoreInteractions(mockConnector)
      }

      "should return false when no duplicate exists" in {
        val mockConnector = mock[ConstructionIndustrySchemeConnector]
        val service       = new SubcontractorService(mockConnector)

        val userAnswers = emptyUserAnswers.set(CisIdQuery, cisId).success.value

        when(mockConnector.getSubcontractorUTRs(eqTo(cisId.toString))(any[HeaderCarrier]))
          .thenReturn(Future.successful(GetSubcontractorUTRsResponse(subcontractorUTRs = subcontractorUTRs)))

        service.isDuplicateUTR(userAnswers, "88888888").futureValue mustBe false

        verify(mockConnector).getSubcontractorUTRs(eqTo(cisId.toString))(any[HeaderCarrier])
        verifyNoMoreInteractions(mockConnector)
      }

      "should return false when getSubcontractorUTRs return empty list" in {
        val mockConnector = mock[ConstructionIndustrySchemeConnector]
        val service       = new SubcontractorService(mockConnector)

        val userAnswers = emptyUserAnswers.set(CisIdQuery, cisId).success.value

        when(mockConnector.getSubcontractorUTRs(eqTo(cisId.toString))(any[HeaderCarrier]))
          .thenReturn(Future.successful(GetSubcontractorUTRsResponse(subcontractorUTRs = Seq.empty)))

        service.isDuplicateUTR(userAnswers, utr).futureValue mustBe false

        verify(mockConnector).getSubcontractorUTRs(eqTo(cisId.toString))(any[HeaderCarrier])
        verifyNoMoreInteractions(mockConnector)
      }

      "should fail when cisId not found in session data" in {
        val mockConnector = mock[ConstructionIndustrySchemeConnector]
        val service       = new SubcontractorService(mockConnector)

        val exception =
          service.isDuplicateUTR(emptyUserAnswers, utr).failed.futureValue

        exception.getMessage must include("CisIdQuery not found in session data")
        verifyNoMoreInteractions(mockConnector)
      }

      "should fail when subcontractor type not found in session data" in {
        val mockConnector = mock[ConstructionIndustrySchemeConnector]
        val service       = new SubcontractorService(mockConnector)

        val userAnswers =
          emptyUserAnswers
            .set(CisIdQuery, cisId)
            .success
            .value

        val ex = service.createAndUpdateSubcontractor(userAnswers).failed.futureValue
        ex.getMessage must include("TypeOfSubcontractorPage not found in session data")

        verifyNoMoreInteractions(mockConnector)
      }

      "should fail when the connector call fails" in {
        val mockConnector = mock[ConstructionIndustrySchemeConnector]
        val service       = new SubcontractorService(mockConnector)

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

    "updateSubcontractor" - {

      val updateCisId = "200"

      val originalSubcontractor =
        SubcontractorResponse(
          subcontractorId = 123L,
          utr = Some("1111111111"),
          pageVisited = Some(1),
          partnerUtr = Some("2222222222"),
          crn = Some("OLD-CRN"),
          firstName = Some("Original"),
          nino = Some("AA123456A"),
          secondName = Some("Middle"),
          surname = Some("Name"),
          partnershipTradingName = Some("Original Partnership"),
          tradingName = Some("Original Trading Name"),
          subcontractorType = Some("company"),
          addressLine1 = Some("Old address 1"),
          addressLine2 = Some("Old address 2"),
          addressLine3 = Some("Old city"),
          addressLine4 = Some("Old county"),
          country = Some("United Kingdom"),
          postcode = Some("OLD 1AA"),
          emailAddress = Some("old@example.com"),
          phoneNumber = Some("02070000000"),
          mobilePhoneNumber = Some("07000000000"),
          worksReferenceNumber = Some("OLD-WRN"),
          createDate = None,
          lastUpdate = None,
          subbieResourceRef = Some(1001L),
          matched = Some("Y"),
          autoVerified = Some("N"),
          verified = Some("Y"),
          verificationNumber = Some("V123456"),
          taxTreatment = Some("Gross"),
          verificationDate = None,
          version = Some(4),
          updatedTaxTreatment = Some("Gross"),
          lastMonthlyReturnDate = None,
          pendingVerifications = Some(0)
        )

      def baseUpdateAnswers(
        subcontractorType: TypeOfSubcontractor
      ): UserAnswers =
        emptyUserAnswers
          .set(CisIdQuery, updateCisId)
          .success
          .value
          .set(TypeOfSubcontractorPage, subcontractorType)
          .success
          .value
          .set(OriginalSubcontractorQuery, originalSubcontractor)
          .success
          .value

      "should update a company using amended values while preserving original metadata" in {

        val mockConnector =
          mock[ConstructionIndustrySchemeConnector]

        val service =
          new SubcontractorService(mockConnector)

        val address =
          Address(
            addressLine1 = "New company address 1",
            addressLine2 = Some("New company address 2"),
            addressLine3 = Some("London"),
            addressLine4 = Some("Greater London"),
            postcode = Some("E1 1AA"),
            country = Some(
              Country(
                code = Some("GB"),
                name = Some("United Kingdom")
              )
            )
          )

        val userAnswers =
          baseUpdateAnswers(TypeOfSubcontractor.Limitedcompany)
            .set(CompanyNamePage, "Updated Company Ltd")
            .success
            .value
            .set(CompanyUtrPage, "9999999999")
            .success
            .value
            .set(CompanyCrnPage, "NEW-CRN")
            .success
            .value
            .set(CompanyAddressPage, address)
            .success
            .value
            .set(CompanyEmailAddressPage, "new@example.com")
            .success
            .value
            .set(CompanyPhoneNumberPage, "02071234567")
            .success
            .value
            .set(CompanyMobileNumberPage, "07123456789")
            .success
            .value
            .set(CompanyWorksReferencePage, "NEW-WRN")
            .success
            .value

        val expectedSubcontractor =
          SubcontractorRequest(
            subcontractorId = 123L,
            utr = Some("9999999999"),
            pageVisited = Some(1),
            partnerUtr = Some("2222222222"),
            crn = Some("NEW-CRN"),
            firstName = Some("Original"),
            nino = Some("AA123456A"),
            secondName = Some("Middle"),
            surname = Some("Name"),
            partnershipTradingName = Some("Original Partnership"),
            tradingName = Some("Updated Company Ltd"),
            subcontractorType = Some("company"),
            addressLine1 = Some("New company address 1"),
            addressLine2 = Some("New company address 2"),
            addressLine3 = Some("London"),
            addressLine4 = Some("Greater London"),
            country = Some("United Kingdom"),
            postcode = Some("E1 1AA"),
            emailAddress = Some("new@example.com"),
            phoneNumber = Some("02071234567"),
            mobilePhoneNumber = Some("07123456789"),
            worksReferenceNumber = Some("NEW-WRN"),
            createDate = None,
            lastUpdate = None,
            subbieResourceRef = Some(1001L),
            matched = Some("Y"),
            autoVerified = Some("N"),
            verified = Some("Y"),
            verificationNumber = Some("V123456"),
            taxTreatment = Some("Gross"),
            verificationDate = None,
            version = Some(4),
            updatedTaxTreatment = Some("Gross"),
            lastMonthlyReturnDate = None,
            pendingVerifications = Some(0)
          )

        val expectedRequest =
          UpdateSubcontractorRequest(
            cisId = updateCisId,
            subcontractor = expectedSubcontractor
          )

        when(
          mockConnector.updateSubcontractor(
            any[UpdateSubcontractorRequest]
          )(any[HeaderCarrier])
        ).thenReturn(
          Future.successful(())
        )

        service
          .updateSubcontractor(userAnswers)
          .futureValue mustBe (())

        verify(mockConnector)
          .updateSubcontractor(
            eqTo(expectedRequest)
          )(any[HeaderCarrier])

        verifyNoMoreInteractions(mockConnector)
      }

      "should update a partnership using amended values while preserving original metadata" in {

        val mockConnector =
          mock[ConstructionIndustrySchemeConnector]

        val service =
          new SubcontractorService(mockConnector)

        val originalPartnership =
          originalSubcontractor.copy(
            subcontractorType = Some("partnership")
          )

        val address =
          Address(
            addressLine1 = "New partnership address 1",
            addressLine2 = Some("New partnership address 2"),
            addressLine3 = Some("Manchester"),
            addressLine4 = Some("Greater Manchester"),
            postcode = Some("M1 1AA"),
            country = Some(
              Country(
                code = Some("GB"),
                name = Some("United Kingdom")
              )
            )
          )

        val userAnswers =
          emptyUserAnswers
            .set(CisIdQuery, updateCisId)
            .success
            .value
            .set(
              TypeOfSubcontractorPage,
              TypeOfSubcontractor.Partnership
            )
            .success
            .value
            .set(
              OriginalSubcontractorQuery,
              originalPartnership
            )
            .success
            .value
            .set(
              PartnershipUniqueTaxpayerReferencePage,
              "3333333333"
            )
            .success
            .value
            .set(
              PartnershipNominatedPartnerUtrPage,
              "4444444444"
            )
            .success
            .value
            .set(
              PartnershipNamePage,
              "Updated Partnership"
            )
            .success
            .value
            .set(
              PartnershipNominatedPartnerNamePage,
              "Updated Partner"
            )
            .success
            .value
            .set(
              PartnershipNominatedPartnerNinoPage,
              "BB123456B"
            )
            .success
            .value
            .set(
              PartnershipNominatedPartnerCrnPage,
              "PARTNER-CRN"
            )
            .success
            .value
            .set(
              PartnershipAddressPage,
              address
            )
            .success
            .value
            .set(
              PartnershipEmailAddressPage,
              "partnership@example.com"
            )
            .success
            .value
            .set(
              PartnershipPhoneNumberPage,
              "02071111111"
            )
            .success
            .value
            .set(
              PartnershipMobileNumberPage,
              "07111111111"
            )
            .success
            .value
            .set(
              PartnershipWorksReferenceNumberPage,
              "PART-WRN"
            )
            .success
            .value

        when(
          mockConnector.updateSubcontractor(
            any[UpdateSubcontractorRequest]
          )(any[HeaderCarrier])
        ).thenReturn(
          Future.successful(())
        )

        service
          .updateSubcontractor(userAnswers)
          .futureValue mustBe (())

        val captor =
          ArgumentCaptor.forClass(
            classOf[UpdateSubcontractorRequest]
          )

        verify(mockConnector)
          .updateSubcontractor(
            captor.capture()
          )(any[HeaderCarrier])

        val sent =
          captor.getValue

        sent.cisId mustBe updateCisId

        sent.subcontractor.utr mustBe
          Some("3333333333")

        sent.subcontractor.partnerUtr mustBe
          Some("4444444444")

        sent.subcontractor.partnershipTradingName mustBe
          Some("Updated Partnership")

        sent.subcontractor.tradingName mustBe
          Some("Updated Partner")

        sent.subcontractor.nino mustBe
          Some("BB123456B")

        sent.subcontractor.crn mustBe
          Some("PARTNER-CRN")

        sent.subcontractor.addressLine1 mustBe
          Some("New partnership address 1")

        sent.subcontractor.emailAddress mustBe
          Some("partnership@example.com")

        sent.subcontractor.worksReferenceNumber mustBe
          Some("PART-WRN")

        sent.subcontractor.subcontractorId mustBe 123L
        sent.subcontractor.subbieResourceRef mustBe Some(1001L)
        sent.subcontractor.verified mustBe Some("Y")
        sent.subcontractor.verificationNumber mustBe Some("V123456")
        sent.subcontractor.taxTreatment mustBe Some("Gross")
        sent.subcontractor.version mustBe Some(4)
        sent.subcontractor.matched mustBe Some("Y")
        sent.subcontractor.autoVerified mustBe Some("N")

        verifyNoMoreInteractions(mockConnector)
      }

      "should update a trust using amended values while preserving original metadata" in {

        val mockConnector =
          mock[ConstructionIndustrySchemeConnector]

        val service =
          new SubcontractorService(mockConnector)

        val originalTrust =
          originalSubcontractor.copy(
            subcontractorType = Some("trust")
          )

        val address =
          Address(
            addressLine1 = "New trust address 1",
            addressLine2 = Some("New trust address 2"),
            addressLine3 = Some("Birmingham"),
            addressLine4 = Some("West Midlands"),
            postcode = Some("B1 1AA"),
            country = Some(
              Country(
                code = Some("GB"),
                name = Some("United Kingdom")
              )
            )
          )

        val userAnswers =
          emptyUserAnswers
            .set(CisIdQuery, updateCisId)
            .success
            .value
            .set(
              TypeOfSubcontractorPage,
              TypeOfSubcontractor.Trust
            )
            .success
            .value
            .set(
              OriginalSubcontractorQuery,
              originalTrust
            )
            .success
            .value
            .set(TrustNamePage, "Updated Trust")
            .success
            .value
            .set(TrustUtrPage, "5555555555")
            .success
            .value
            .set(TrustAddressPage, address)
            .success
            .value
            .set(
              TrustEmailAddressPage,
              "trust@example.com"
            )
            .success
            .value
            .set(
              TrustPhoneNumberPage,
              "02072222222"
            )
            .success
            .value
            .set(
              TrustMobileNumberPage,
              "07222222222"
            )
            .success
            .value
            .set(
              TrustWorksReferencePage,
              "TRUST-WRN"
            )
            .success
            .value

        when(
          mockConnector.updateSubcontractor(
            any[UpdateSubcontractorRequest]
          )(any[HeaderCarrier])
        ).thenReturn(
          Future.successful(())
        )

        service
          .updateSubcontractor(userAnswers)
          .futureValue mustBe (())

        val captor =
          ArgumentCaptor.forClass(
            classOf[UpdateSubcontractorRequest]
          )

        verify(mockConnector)
          .updateSubcontractor(
            captor.capture()
          )(any[HeaderCarrier])

        val sent =
          captor.getValue

        sent.cisId mustBe updateCisId

        sent.subcontractor.utr mustBe
          Some("5555555555")

        sent.subcontractor.tradingName mustBe
          Some("Updated Trust")

        sent.subcontractor.addressLine1 mustBe
          Some("New trust address 1")

        sent.subcontractor.addressLine2 mustBe
          Some("New trust address 2")

        sent.subcontractor.addressLine3 mustBe
          Some("Birmingham")

        sent.subcontractor.addressLine4 mustBe
          Some("West Midlands")

        sent.subcontractor.postcode mustBe
          Some("B1 1AA")

        sent.subcontractor.emailAddress mustBe
          Some("trust@example.com")

        sent.subcontractor.phoneNumber mustBe
          Some("02072222222")

        sent.subcontractor.mobilePhoneNumber mustBe
          Some("07222222222")

        sent.subcontractor.worksReferenceNumber mustBe
          Some("TRUST-WRN")

        sent.subcontractor.subcontractorId mustBe 123L
        sent.subcontractor.subbieResourceRef mustBe Some(1001L)
        sent.subcontractor.verified mustBe Some("Y")
        sent.subcontractor.verificationNumber mustBe Some("V123456")
        sent.subcontractor.taxTreatment mustBe Some("Gross")
        sent.subcontractor.version mustBe Some(4)

        verifyNoMoreInteractions(mockConnector)
      }

      "should update an individual using amended values while preserving original metadata" in {

        val mockConnector =
          mock[ConstructionIndustrySchemeConnector]

        val service =
          new SubcontractorService(mockConnector)

        val originalIndividual =
          originalSubcontractor.copy(
            subcontractorType = Some("soletrader")
          )

        val address =
          Address(
            addressLine1 = "New individual address 1",
            addressLine2 = Some("New individual address 2"),
            addressLine3 = Some("Leeds"),
            addressLine4 = Some("West Yorkshire"),
            postcode = Some("LS1 1AA"),
            country = Some(
              Country(
                code = Some("GB"),
                name = Some("United Kingdom")
              )
            )
          )

        val userAnswers =
          emptyUserAnswers
            .set(CisIdQuery, updateCisId)
            .success
            .value
            .set(
              TypeOfSubcontractorPage,
              TypeOfSubcontractor.Individualorsoletrader
            )
            .success
            .value
            .set(
              OriginalSubcontractorQuery,
              originalIndividual
            )
            .success
            .value
            .set(
              SubcontractorNamePage,
              SubcontractorName(
                "Updated",
                Some("Middle"),
                "Person"
              )
            )
            .success
            .value
            .set(
              TradingNameOfSubcontractorPage,
              "Updated Trading Name"
            )
            .success
            .value
            .set(
              SubNationalInsuranceNumberPage,
              "CC123456C"
            )
            .success
            .value
            .set(
              SubcontractorsUniqueTaxpayerReferencePage,
              "6666666666"
            )
            .success
            .value
            .set(
              AddressOfSubcontractorPage,
              address
            )
            .success
            .value
            .set(
              IndividualEmailAddressPage,
              "individual@example.com"
            )
            .success
            .value
            .set(
              IndividualPhoneNumberPage,
              "02073333333"
            )
            .success
            .value
            .set(
              IndividualMobileNumberPage,
              "07333333333"
            )
            .success
            .value
            .set(
              WorksReferenceNumberPage,
              "IND-WRN"
            )
            .success
            .value

        when(
          mockConnector.updateSubcontractor(
            any[UpdateSubcontractorRequest]
          )(any[HeaderCarrier])
        ).thenReturn(
          Future.successful(())
        )

        service
          .updateSubcontractor(userAnswers)
          .futureValue mustBe (())

        val captor =
          ArgumentCaptor.forClass(
            classOf[UpdateSubcontractorRequest]
          )

        verify(mockConnector)
          .updateSubcontractor(
            captor.capture()
          )(any[HeaderCarrier])

        val sent =
          captor.getValue

        sent.subcontractor.firstName mustBe Some("Updated")
        sent.subcontractor.secondName mustBe Some("Middle")
        sent.subcontractor.surname mustBe Some("Person")

        sent.subcontractor.tradingName mustBe
          Some("Updated Trading Name")

        sent.subcontractor.nino mustBe
          Some("CC123456C")

        sent.subcontractor.utr mustBe
          Some("6666666666")

        sent.subcontractor.addressLine1 mustBe
          Some("New individual address 1")

        sent.subcontractor.emailAddress mustBe
          Some("individual@example.com")

        sent.subcontractor.worksReferenceNumber mustBe
          Some("IND-WRN")

        sent.subcontractor.subcontractorId mustBe 123L
        sent.subcontractor.subbieResourceRef mustBe Some(1001L)
        sent.subcontractor.verified mustBe Some("Y")
        sent.subcontractor.verificationNumber mustBe Some("V123456")
        sent.subcontractor.version mustBe Some(4)

        verifyNoMoreInteractions(mockConnector)
      }

      "should update an individual name and remove trading name when IndividualNamesOptionsPage only SubcontractorName is selected" in {

        val mockConnector =
          mock[ConstructionIndustrySchemeConnector]

        val service =
          new SubcontractorService(mockConnector)

        val originalIndividual =
          originalSubcontractor.copy(
            subcontractorType = Some("soletrader")
          )

        val userAnswers =
          emptyUserAnswers
            .set(CisIdQuery, updateCisId)
            .success
            .value
            .set(
              TypeOfSubcontractorPage,
              TypeOfSubcontractor.Individualorsoletrader
            )
            .success
            .value
            .set(
              OriginalSubcontractorQuery,
              originalIndividual
            )
            .success
            .value
            .set(
              SubcontractorNamePage,
              SubcontractorName(
                "Updated",
                Some("Middle"),
                "Person"
              )
            )
            .success
            .value
            .set(
              IndividualNamesOptionsPage,
              Set(IndividualNamesOptions.SubcontractorName)
            )
            .success
            .value
            .set(
              AddIndividualContactMethodsYesNoPage,
              true
            )
            .success
            .value

        when(
          mockConnector.updateSubcontractor(
            any[UpdateSubcontractorRequest]
          )(any[HeaderCarrier])
        ).thenReturn(
          Future.successful(())
        )

        service
          .updateSubcontractor(userAnswers)
          .futureValue mustBe (())

        val captor =
          ArgumentCaptor.forClass(
            classOf[UpdateSubcontractorRequest]
          )

        verify(mockConnector)
          .updateSubcontractor(
            captor.capture()
          )(any[HeaderCarrier])

        val sent =
          captor.getValue

        sent.subcontractor.firstName mustBe Some("Updated")
        sent.subcontractor.secondName mustBe Some("Middle")
        sent.subcontractor.surname mustBe Some("Person")

        sent.subcontractor.tradingName mustBe
          Some("")

        verifyNoMoreInteractions(mockConnector)
      }

      "should preserve original individual name and address when amend pages are missing" in {

        val mockConnector =
          mock[ConstructionIndustrySchemeConnector]

        val service =
          new SubcontractorService(mockConnector)

        val originalIndividual =
          originalSubcontractor.copy(
            subcontractorType = Some("soletrader")
          )

        val userAnswers =
          emptyUserAnswers
            .set(CisIdQuery, updateCisId)
            .success
            .value
            .set(
              TypeOfSubcontractorPage,
              TypeOfSubcontractor.Individualorsoletrader
            )
            .success
            .value
            .set(
              OriginalSubcontractorQuery,
              originalIndividual
            )
            .success
            .value

        when(
          mockConnector.updateSubcontractor(
            any[UpdateSubcontractorRequest]
          )(any[HeaderCarrier])
        ).thenReturn(
          Future.successful(())
        )

        service
          .updateSubcontractor(userAnswers)
          .futureValue mustBe (())

        val captor =
          ArgumentCaptor.forClass(
            classOf[UpdateSubcontractorRequest]
          )

        verify(mockConnector)
          .updateSubcontractor(
            captor.capture()
          )(any[HeaderCarrier])

        val sent =
          captor.getValue.subcontractor

        sent.firstName mustBe Some("Original")
        sent.secondName mustBe Some("Middle")
        sent.surname mustBe Some("Name")
        sent.addressLine1 mustBe Some("Old address 1")
        sent.addressLine2 mustBe Some("Old address 2")
        sent.addressLine3 mustBe Some("Old city")
        sent.addressLine4 mustBe Some("Old county")
        sent.country mustBe Some("United Kingdom")
        sent.postcode mustBe Some("OLD 1AA")

        verifyNoMoreInteractions(mockConnector)
      }

      "should clear original individual address when address has been explicitly removed" in {

        val mockConnector =
          mock[ConstructionIndustrySchemeConnector]

        val service =
          new SubcontractorService(mockConnector)

        val originalIndividual =
          originalSubcontractor.copy(
            subcontractorType = Some("soletrader")
          )

        val userAnswers =
          emptyUserAnswers
            .set(CisIdQuery, updateCisId)
            .success
            .value
            .set(
              TypeOfSubcontractorPage,
              TypeOfSubcontractor.Individualorsoletrader
            )
            .success
            .value
            .set(
              OriginalSubcontractorQuery,
              originalIndividual
            )
            .success
            .value
            .set(SubAddressYesNoPage, false)
            .success
            .value

        when(
          mockConnector.updateSubcontractor(
            any[UpdateSubcontractorRequest]
          )(any[HeaderCarrier])
        ).thenReturn(
          Future.successful(())
        )

        service
          .updateSubcontractor(userAnswers)
          .futureValue mustBe (())

        val captor =
          ArgumentCaptor.forClass(
            classOf[UpdateSubcontractorRequest]
          )

        verify(mockConnector)
          .updateSubcontractor(
            captor.capture()
          )(any[HeaderCarrier])

        val sent =
          captor.getValue.subcontractor

        sent.addressLine1 mustBe Some("")
        sent.addressLine2 mustBe Some("")
        sent.addressLine3 mustBe Some("")
        sent.addressLine4 mustBe Some("")
        sent.country mustBe Some("")
        sent.postcode mustBe Some("")

        verifyNoMoreInteractions(mockConnector)
      }

      "should send empty strings when optional company fields are explicitly removed" in {

        val mockConnector =
          mock[ConstructionIndustrySchemeConnector]

        val service =
          new SubcontractorService(mockConnector)

        val address =
          Address(
            addressLine1 = "New company address 1"
          )

        val userAnswers =
          baseUpdateAnswers(TypeOfSubcontractor.Limitedcompany)
            .set(CompanyAddressYesNoPage, true)
            .success
            .value
            .set(CompanyAddressPage, address)
            .success
            .value
            .set(CompanyUtrYesNoPage, false)
            .success
            .value
            .set(CompanyCrnYesNoPage, false)
            .success
            .value
            .set(AddCompanyContactMethodsYesNoPage, false)
            .success
            .value
            .set(CompanyWorksReferenceYesNoPage, false)
            .success
            .value

        when(
          mockConnector.updateSubcontractor(
            any[UpdateSubcontractorRequest]
          )(any[HeaderCarrier])
        ).thenReturn(
          Future.successful(())
        )

        service
          .updateSubcontractor(userAnswers)
          .futureValue mustBe (())

        val captor =
          ArgumentCaptor.forClass(
            classOf[UpdateSubcontractorRequest]
          )

        verify(mockConnector)
          .updateSubcontractor(
            captor.capture()
          )(any[HeaderCarrier])

        val sent =
          captor.getValue.subcontractor

        sent.utr mustBe Some("")
        sent.crn mustBe Some("")
        sent.tradingName mustBe Some("Original Trading Name")
        sent.addressLine1 mustBe Some("New company address 1")
        sent.addressLine2 mustBe Some("")
        sent.addressLine3 mustBe Some("")
        sent.addressLine4 mustBe Some("")
        sent.country mustBe Some("")
        sent.postcode mustBe Some("")
        sent.emailAddress mustBe Some("")
        sent.phoneNumber mustBe Some("")
        sent.mobilePhoneNumber mustBe Some("")
        sent.worksReferenceNumber mustBe Some("")

        verifyNoMoreInteractions(mockConnector)
      }

      "should update a subcontractor selected contact detail and remove unselected contact detail" in {

        val mockConnector =
          mock[ConstructionIndustrySchemeConnector]

        val service =
          new SubcontractorService(mockConnector)

        val originalIndividual =
          originalSubcontractor.copy(
            subcontractorType = Some("soletrader")
          )

        val userAnswers =
          emptyUserAnswers
            .set(CisIdQuery, updateCisId)
            .success
            .value
            .set(
              TypeOfSubcontractorPage,
              TypeOfSubcontractor.Individualorsoletrader
            )
            .success
            .value
            .set(
              OriginalSubcontractorQuery,
              originalIndividual
            )
            .success
            .value
            .set(
              AddIndividualContactMethodsYesNoPage,
              true
            )
            .success
            .value
            .set(
              IndividualContactMethodOptionsPage,
              Set(ContactMethodOptions.Email)
            )
            .success
            .value
            .set(
              IndividualEmailAddressPage,
              "update@update.com"
            )
            .success
            .value

        when(
          mockConnector.updateSubcontractor(
            any[UpdateSubcontractorRequest]
          )(any[HeaderCarrier])
        ).thenReturn(
          Future.successful(())
        )

        service
          .updateSubcontractor(userAnswers)
          .futureValue mustBe (())

        val captor =
          ArgumentCaptor.forClass(
            classOf[UpdateSubcontractorRequest]
          )

        verify(mockConnector)
          .updateSubcontractor(
            captor.capture()
          )(any[HeaderCarrier])

        val sent =
          captor.getValue

        sent.subcontractor.emailAddress mustBe Some("update@update.com")
        sent.subcontractor.phoneNumber mustBe Some("")
        sent.subcontractor.mobilePhoneNumber mustBe Some("")

        verifyNoMoreInteractions(mockConnector)
      }

      "should fail when the submitted amend ref does not match the current original subcontractor" in {

        val mockConnector =
          mock[ConstructionIndustrySchemeConnector]

        val service =
          new SubcontractorService(mockConnector)

        val userAnswers =
          baseUpdateAnswers(TypeOfSubcontractor.Limitedcompany)
            .set(AmendSubbieResourceRefQuery, 1001L)
            .success
            .value

        val exception =
          service
            .updateSubcontractor(userAnswers, Some(2002L))
            .failed
            .futureValue

        exception.getMessage mustBe
          "Stale amend session for subbieResourceRef=2002"

        verifyNoMoreInteractions(mockConnector)
      }

      "should return Unit when the connector succeeds" in {

        val mockConnector =
          mock[ConstructionIndustrySchemeConnector]

        val service =
          new SubcontractorService(mockConnector)

        val userAnswers =
          baseUpdateAnswers(TypeOfSubcontractor.Limitedcompany)
            .set(
              CompanyNamePage,
              "Updated Company"
            )
            .success
            .value

        when(
          mockConnector.updateSubcontractor(
            any[UpdateSubcontractorRequest]
          )(any[HeaderCarrier])
        ).thenReturn(
          Future.successful(())
        )

        service
          .updateSubcontractor(userAnswers)
          .futureValue mustBe (())

        verify(mockConnector, times(1))
          .updateSubcontractor(
            any[UpdateSubcontractorRequest]
          )(any[HeaderCarrier])

        verifyNoMoreInteractions(mockConnector)
      }

      "should fail when CisIdQuery is missing" in {

        val mockConnector =
          mock[ConstructionIndustrySchemeConnector]

        val service =
          new SubcontractorService(mockConnector)

        val exception =
          service
            .updateSubcontractor(emptyUserAnswers)
            .failed
            .futureValue

        exception.getMessage mustBe
          "CisIdQuery not found in session data"

        verifyNoMoreInteractions(mockConnector)
      }

      "should fail when TypeOfSubcontractorPage is missing" in {

        val mockConnector =
          mock[ConstructionIndustrySchemeConnector]

        val service =
          new SubcontractorService(mockConnector)

        val userAnswers =
          emptyUserAnswers
            .set(CisIdQuery, updateCisId)
            .success
            .value

        val exception =
          service
            .updateSubcontractor(userAnswers)
            .failed
            .futureValue

        exception.getMessage mustBe
          "TypeOfSubcontractorPage not found in session data"

        verifyNoMoreInteractions(mockConnector)
      }

      "should fail when OriginalSubcontractorQuery is missing" in {

        val mockConnector =
          mock[ConstructionIndustrySchemeConnector]

        val service =
          new SubcontractorService(mockConnector)

        val userAnswers =
          emptyUserAnswers
            .set(CisIdQuery, updateCisId)
            .success
            .value
            .set(
              TypeOfSubcontractorPage,
              TypeOfSubcontractor.Limitedcompany
            )
            .success
            .value

        val exception =
          service
            .updateSubcontractor(userAnswers)
            .failed
            .futureValue

        exception.getMessage mustBe
          "OriginalSubcontractorQuery not found in session data"

        verifyNoMoreInteractions(mockConnector)
      }

      "should propagate an exception from updateSubcontractor connector call" in {

        val mockConnector =
          mock[ConstructionIndustrySchemeConnector]

        val service =
          new SubcontractorService(mockConnector)

        val userAnswers =
          baseUpdateAnswers(TypeOfSubcontractor.Limitedcompany)
            .set(
              CompanyNamePage,
              "Updated Company"
            )
            .success
            .value

        when(
          mockConnector.updateSubcontractor(
            any[UpdateSubcontractorRequest]
          )(any[HeaderCarrier])
        ).thenReturn(
          Future.failed(
            new RuntimeException(
              "update subcontractor failed"
            )
          )
        )

        val exception =
          service
            .updateSubcontractor(userAnswers)
            .failed
            .futureValue

        exception.getMessage mustBe
          "update subcontractor failed"

        verify(mockConnector)
          .updateSubcontractor(
            any[UpdateSubcontractorRequest]
          )(any[HeaderCarrier])

        verifyNoMoreInteractions(mockConnector)
      }
    }

    "getSubcontractor" - {

      val cisId             = "INST-123"
      val subbieResourceRef = 1001L

      val expectedResponse =
        GetSubcontractorResponse(
          scheme = None,
          subcontractor = None
        )

      "should return the response from the connector" in {
        val mockConnector =
          mock[ConstructionIndustrySchemeConnector]

        val service =
          new SubcontractorService(mockConnector)

        when(
          mockConnector.getSubcontractor(
            eqTo(cisId),
            eqTo(subbieResourceRef)
          )(any[HeaderCarrier])
        ).thenReturn(
          Future.successful(expectedResponse)
        )

        val result =
          service
            .getSubcontractor(cisId, subbieResourceRef)
            .futureValue

        result mustBe expectedResponse

        verify(mockConnector, times(1))
          .getSubcontractor(
            eqTo(cisId),
            eqTo(subbieResourceRef)
          )(any[HeaderCarrier])

        verifyNoMoreInteractions(mockConnector)
      }

      "should pass the supplied cisId and subbieResourceRef to the connector" in {
        val mockConnector =
          mock[ConstructionIndustrySchemeConnector]

        val service =
          new SubcontractorService(mockConnector)

        when(
          mockConnector.getSubcontractor(
            any[String],
            any[Long]
          )(any[HeaderCarrier])
        ).thenReturn(
          Future.successful(expectedResponse)
        )

        service
          .getSubcontractor(cisId, subbieResourceRef)
          .futureValue

        verify(mockConnector)
          .getSubcontractor(
            eqTo("INST-123"),
            eqTo(1001L)
          )(any[HeaderCarrier])

        verifyNoMoreInteractions(mockConnector)
      }

      "should return a response containing a subcontractor" in {
        val mockConnector =
          mock[ConstructionIndustrySchemeConnector]

        val service =
          new SubcontractorService(mockConnector)

        val subcontractor =
          models.response.SubcontractorResponse(
            subcontractorId = 1L,
            utr = Some("1234567890"),
            pageVisited = None,
            partnerUtr = None,
            crn = None,
            firstName = Some("Martin"),
            nino = Some("AA123456A"),
            secondName = None,
            surname = Some("Brody"),
            partnershipTradingName = None,
            tradingName = None,
            subcontractorType = Some("soletrader"),
            addressLine1 = None,
            addressLine2 = None,
            addressLine3 = None,
            addressLine4 = None,
            country = None,
            postcode = None,
            emailAddress = None,
            phoneNumber = None,
            mobilePhoneNumber = None,
            worksReferenceNumber = None,
            createDate = None,
            lastUpdate = None,
            subbieResourceRef = Some(subbieResourceRef),
            matched = None,
            autoVerified = None,
            verified = None,
            verificationNumber = None,
            taxTreatment = None,
            verificationDate = None,
            version = None,
            updatedTaxTreatment = None,
            lastMonthlyReturnDate = None,
            pendingVerifications = None
          )

        val response =
          expectedResponse.copy(
            subcontractor = Some(subcontractor)
          )

        when(
          mockConnector.getSubcontractor(
            eqTo(cisId),
            eqTo(subbieResourceRef)
          )(any[HeaderCarrier])
        ).thenReturn(
          Future.successful(response)
        )

        val result =
          service
            .getSubcontractor(cisId, subbieResourceRef)
            .futureValue

        result.subcontractor mustBe Some(subcontractor)

        verify(mockConnector)
          .getSubcontractor(
            eqTo(cisId),
            eqTo(subbieResourceRef)
          )(any[HeaderCarrier])

        verifyNoMoreInteractions(mockConnector)
      }

      "should return a response when no subcontractor is found" in {
        val mockConnector =
          mock[ConstructionIndustrySchemeConnector]

        val service =
          new SubcontractorService(mockConnector)

        when(
          mockConnector.getSubcontractor(
            eqTo(cisId),
            eqTo(subbieResourceRef)
          )(any[HeaderCarrier])
        ).thenReturn(
          Future.successful(expectedResponse)
        )

        val result =
          service
            .getSubcontractor(cisId, subbieResourceRef)
            .futureValue

        result.subcontractor mustBe None
        result.scheme mustBe None

        verify(mockConnector)
          .getSubcontractor(
            eqTo(cisId),
            eqTo(subbieResourceRef)
          )(any[HeaderCarrier])

        verifyNoMoreInteractions(mockConnector)
      }

      "should propagate an exception returned by the connector" in {
        val mockConnector =
          mock[ConstructionIndustrySchemeConnector]

        val service =
          new SubcontractorService(mockConnector)

        when(
          mockConnector.getSubcontractor(
            eqTo(cisId),
            eqTo(subbieResourceRef)
          )(any[HeaderCarrier])
        ).thenReturn(
          Future.failed(
            new RuntimeException("CIS service unavailable")
          )
        )

        val exception =
          service
            .getSubcontractor(cisId, subbieResourceRef)
            .failed
            .futureValue

        exception.getMessage mustBe "CIS service unavailable"

        verify(mockConnector)
          .getSubcontractor(
            eqTo(cisId),
            eqTo(subbieResourceRef)
          )(any[HeaderCarrier])

        verifyNoMoreInteractions(mockConnector)
      }
    }

  }
}
