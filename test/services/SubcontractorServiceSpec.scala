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
import models.add.{InternationalAddress, SubcontractorName, TypeOfSubcontractor}
import models.contact.ContactOptions
import pages.add.company._
import models.requests.CreateAndUpdateSubcontractorPayload
import models.requests.CreateAndUpdateSubcontractorPayload.{CompanyPayload, IndividualOrSoleTraderPayload, PartnershipPayload, TrustPayload}
import models.subcontractor.GetSubcontractorUTRsResponse
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{times, verify, verifyNoMoreInteractions, when}
import pages.add.*
import pages.add.partnership.*
import queries.CisIdQuery
import uk.gov.hmrc.http.HeaderCarrier
import org.mockito.ArgumentCaptor
import org.scalatestplus.mockito.MockitoSugar
import pages.add.trust.*

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
              InternationalAddress(
                "addressLine1",
                Some("addressLine2"),
                "addressLine3",
                Some("addressLine4"),
                "postalCode",
                "United Kingdom"
              )
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

        val expectedPayload: CreateAndUpdateSubcontractorPayload =
          IndividualOrSoleTraderPayload(
            cisId = cisId,
            subcontractorType = TypeOfSubcontractor.Individualorsoletrader,
            tradingName = Some("trading name"),
            addressLine1 = Some("addressLine1"),
            addressLine2 = Some("addressLine2"),
            city = Some("addressLine3"),
            county = Some("addressLine4"),
            postcode = Some("postalCode"),
            country = Some("United Kingdom"),
            nino = Some("nino"),
            utr = Some("utr"),
            worksReferenceNumber = Some("workRef")
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
              InternationalAddress(
                "addressLine1",
                Some("addressLine2"),
                "addressLine3",
                Some("addressLine4"),
                "postalCode",
                "United Kingdom"
              )
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
            postcode = Some("postalCode"),
            country = Some("United Kingdom"),
            nino = Some("nino"),
            utr = Some("utr"),
            worksReferenceNumber = Some("workRef")
          )

        when(mockConnector.createAndUpdateSubcontractor(any[CreateAndUpdateSubcontractorPayload])(any[HeaderCarrier]))
          .thenReturn(Future.successful(()))

        service.createAndUpdateSubcontractor(userAnswers).futureValue mustBe (())

        verify(mockConnector).createAndUpdateSubcontractor(eqTo(expectedPayload))(any[HeaderCarrier])
        verifyNoMoreInteractions(mockConnector)
      }

      def basePartnershipAnswers = {
        val partnershipAddress =
          InternationalAddress(
            addressLine1 = "p1",
            addressLine2 = Some("p2"),
            addressLine3 = "London",
            addressLine4 = Some("Hackney"),
            postalCode = "N1 5AP",
            country = "United Kingdom"
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
            .set(PartnershipChooseContactDetailsPage, ContactOptions.Email)
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
            .set(PartnershipChooseContactDetailsPage, ContactOptions.Phone)
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
            .set(PartnershipChooseContactDetailsPage, ContactOptions.Mobile)
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
            .set(PartnershipChooseContactDetailsPage, ContactOptions.NoDetails)
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

      "should create and update subcontractor (Partnership) with none in contact details (no contact fields sent)" in {
        val mockConnector = mock[ConstructionIndustrySchemeConnector]
        val service       = new SubcontractorService(mockConnector)

        val userAnswers =
          basePartnershipAnswers
            .remove(PartnershipChooseContactDetailsPage)
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
          InternationalAddress(
            addressLine1 = "c1",
            addressLine2 = Some("c2"),
            addressLine3 = "London",
            addressLine4 = Some("Hackney"),
            postalCode = "E1 6AN",
            country = "United Kingdom"
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
            .set(CompanyContactOptionsPage, ContactOptions.Email)
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
            .set(CompanyContactOptionsPage, ContactOptions.Phone)
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
            .set(CompanyContactOptionsPage, ContactOptions.Mobile)
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
            .set(CompanyContactOptionsPage, ContactOptions.NoDetails)
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

      "should create and update subcontractor (Company) with none in contact details (no contact fields sent)" in {
        val mockConnector = mock[ConstructionIndustrySchemeConnector]
        val service       = new SubcontractorService(mockConnector)

        val userAnswers =
          baseCompanyAnswers
            .remove(CompanyContactOptionsPage)
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
          InternationalAddress(
            addressLine1 = "t1",
            addressLine2 = Some("t2"),
            addressLine3 = "London",
            addressLine4 = Some("Hackney"),
            postalCode = "N1 5AP",
            country = "United Kingdom"
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
            .set(TrustContactOptionsPage, ContactOptions.Email)
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
            .set(TrustContactOptionsPage, ContactOptions.Phone)
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
            .set(TrustContactOptionsPage, ContactOptions.Mobile)
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
            .set(TrustContactOptionsPage, ContactOptions.NoDetails)
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
            .remove(TrustContactOptionsPage)
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

  }
}
