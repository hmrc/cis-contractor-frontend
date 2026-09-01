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
import models.TypeOfSubcontractor
import models.add.{IndividualNamesOptions, SubcontractorName}
import models.address.{Address, Country}
import models.amend.OriginalIndividualAnswers
import models.contact.ContactMethodOptions
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.add.*
import pages.add.company.*
import pages.add.partnership.*
import pages.add.trust.*
import play.api.libs.json.JsValue
import queries.{AmendSubbieResourceRefQuery, CisIdQuery, OriginalIndividualAnswersQuery}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditConnector

import scala.concurrent.ExecutionContext.Implicits.global

class AuditServiceSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {

  private implicit val headerCarrier: HeaderCarrier = HeaderCarrier()

  private val mockAuditConnector = mock[AuditConnector]
  private val service            = new AuditService(mockAuditConnector)

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockAuditConnector)
  }

  private def captureDetail(): JsValue = {
    val captor: ArgumentCaptor[JsValue] = ArgumentCaptor.forClass(classOf[JsValue])
    verify(mockAuditConnector).sendExplicitAudit(any[String], captor.capture())(any(), any(), any())
    captor.getValue
  }

  ".addSubcontractorEvent" - {

    "must send an explicit audit event with the correct auditType and typeOfSubcontractor for minimal answers" in {
      val ua = emptyUserAnswers
        .set(TypeOfSubcontractorPage, TypeOfSubcontractor.Individualorsoletrader)
        .success
        .value

      service.addSubcontractorEvent(ua)

      val detail = captureDetail()
      (detail \ "typeOfSubcontractor").as[String] mustBe "soletrader"
      (detail \ "cisId").toOption mustBe None
      (detail \ "individualNamesOptions").toOption mustBe None
    }

    "must include all fields in the audit event when full individual answers are provided" in {
      val address = Address(
        addressLine1 = "4 Other Place",
        addressLine2 = Some("Some District"),
        addressLine3 = Some("Anytown"),
        postcode = Some("ZZ1 1ZZ"),
        country = Some(Country(Some("GB"), Some("United Kingdom"))),
        addressValidated = true
      )

      val ua = emptyUserAnswers
        .set(TypeOfSubcontractorPage, TypeOfSubcontractor.Individualorsoletrader)
        .success
        .value
        .set(
          IndividualNamesOptionsPage,
          Set(IndividualNamesOptions.SubcontractorName, IndividualNamesOptions.TradingName)
        )
        .success
        .value
        .set(TradingNameOfSubcontractorPage, "TradingName")
        .success
        .value
        .set(SubAddressYesNoPage, true)
        .success
        .value
        .set(AddressOfSubcontractorPage, address)
        .success
        .value
        .set(AddIndividualContactMethodsYesNoPage, true)
        .success
        .value
        .set(
          IndividualContactMethodOptionsPage,
          Set(ContactMethodOptions.Email, ContactMethodOptions.Phone, ContactMethodOptions.Mobile)
        )
        .success
        .value
        .set(IndividualEmailAddressPage, "test@test.com")
        .success
        .value
        .set(IndividualPhoneNumberPage, "+447960141611")
        .success
        .value
        .set(IndividualMobileNumberPage, "01912170507")
        .success
        .value
        .set(UniqueTaxpayerReferenceYesNoPage, true)
        .success
        .value
        .set(SubcontractorsUniqueTaxpayerReferencePage, "1111122222")
        .success
        .value
        .set(NationalInsuranceNumberYesNoPage, true)
        .success
        .value
        .set(SubNationalInsuranceNumberPage, "NH112233D")
        .success
        .value
        .set(WorksReferenceNumberYesNoPage, true)
        .success
        .value
        .set(WorksReferenceNumberPage, "WORKREF-001")
        .success
        .value
        .set(SubcontractorNamePage, SubcontractorName("John", Some("Paul"), "Smith"))
        .success
        .value

      service.addSubcontractorEvent(ua)

      val detail = captureDetail()
      (detail \ "typeOfSubcontractor").as[String] mustBe "soletrader"
      (detail \ "individualNamesOptions").as[Seq[String]] mustBe Seq("subcontractorName", "tradingName")
      (detail \ "firstName").as[String] mustBe "John"
      (detail \ "middleName").as[String] mustBe "Paul"
      (detail \ "surname").as[String] mustBe "Smith"
      (detail \ "tradingNameOfSubcontractor").as[String] mustBe "TradingName"
      (detail \ "subAddressYesNo").as[Boolean] mustBe true
      (detail \ "addressOfSubcontractor" \ "addressLine1").as[String] mustBe "4 Other Place"
      (detail \ "addIndividualContactMethodsYesNo").as[Boolean] mustBe true
      (detail \ "individualContactMethodOptions").as[Seq[String]] mustBe Seq("email", "phone", "mobile")
      (detail \ "individualEmailAddress").as[String] mustBe "test@test.com"
      (detail \ "individualPhoneNumber").as[String] mustBe "+447960141611"
      (detail \ "individualMobileNumber").as[String] mustBe "01912170507"
      (detail \ "uniqueTaxpayerReferenceYesNo").as[Boolean] mustBe true
      (detail \ "subcontractorsUniqueTaxpayerReference").as[String] mustBe "1111122222"
      (detail \ "nationalInsuranceNumberYesNo").as[Boolean] mustBe true
      (detail \ "subNationalInsuranceNumber").as[String] mustBe "NH112233D"
      (detail \ "worksReferenceNumberYesNo").as[Boolean] mustBe true
      (detail \ "worksReferenceNumber").as[String] mustBe "WORKREF-001"
    }

    "must build a company audit event for a limited company" in {
      val ua = emptyUserAnswers
        .set(TypeOfSubcontractorPage, TypeOfSubcontractor.Limitedcompany)
        .success
        .value
        .set(CompanyNamePage, "Test Co Ltd")
        .success
        .value

      service.addSubcontractorEvent(ua)

      val detail = captureDetail()
      (detail \ "typeOfSubcontractor").as[String] mustBe "company"
      (detail \ "companyName").as[String] mustBe "Test Co Ltd"
    }

    "must build a partnership audit event for a partnership" in {
      val ua = emptyUserAnswers
        .set(TypeOfSubcontractorPage, TypeOfSubcontractor.Partnership)
        .success
        .value
        .set(PartnershipNamePage, "Test Partnership")
        .success
        .value

      service.addSubcontractorEvent(ua)

      val detail = captureDetail()
      (detail \ "typeOfSubcontractor").as[String] mustBe "partnership"
      (detail \ "partnershipName").as[String] mustBe "Test Partnership"
    }

    "must build a trust audit event for a trust" in {
      val ua = emptyUserAnswers
        .set(TypeOfSubcontractorPage, TypeOfSubcontractor.Trust)
        .success
        .value
        .set(TrustNamePage, "Test Trust")
        .success
        .value

      service.addSubcontractorEvent(ua)

      val detail = captureDetail()
      (detail \ "typeOfSubcontractor").as[String] mustBe "trust"
      (detail \ "trustName").as[String] mustBe "Test Trust"
    }
  }

  ".amendSubcontractorEvent" - {

    "must send an individual amend event with only the changed fields diffed" in {
      val original = OriginalIndividualAnswers(
        individualNamesOptions = Set(IndividualNamesOptions.SubcontractorName),
        tradingName = None,
        subcontractorName = Some(SubcontractorName("John", None, "Smith")),
        addressYesNo = Some(false),
        address = None,
        individualContactMethodsYesNo = Some(false),
        individualContactMethod = Set.empty,
        email = None,
        phone = None,
        mobile = None,
        utrYesNo = Some(false),
        utr = None,
        ninoYesNo = Some(false),
        nino = None,
        worksReferenceYesNo = Some(false),
        worksReference = None,
        verificationNumber = None
      )

      val ua = emptyUserAnswers
        .set(TypeOfSubcontractorPage, TypeOfSubcontractor.Individualorsoletrader)
        .success
        .value
        .set(CisIdQuery, "cis-001")
        .success
        .value
        .set(AmendSubbieResourceRefQuery, 99999L)
        .success
        .value
        .set(OriginalIndividualAnswersQuery, original)
        .success
        .value
        .set(IndividualNamesOptionsPage, Set(IndividualNamesOptions.SubcontractorName))
        .success
        .value
        .set(SubcontractorNamePage, SubcontractorName("Jane", None, "Smith"))
        .success
        .value
        .set(SubAddressYesNoPage, false)
        .success
        .value
        .set(AddIndividualContactMethodsYesNoPage, false)
        .success
        .value
        .set(UniqueTaxpayerReferenceYesNoPage, false)
        .success
        .value
        .set(NationalInsuranceNumberYesNoPage, false)
        .success
        .value
        .set(WorksReferenceNumberYesNoPage, false)
        .success
        .value

      service.amendSubcontractorEvent(ua)

      val detail = captureDetail()
      (detail \ "typeOfSubcontractor").as[String] mustBe "soletrader"
      (detail \ "cisId").as[String] mustBe "cis-001"
      (detail \ "subbieResourceRef").as[Long] mustBe 99999L
      (detail \ "originalDetails" \ "firstName").as[String] mustBe "John"
      (detail \ "updatedDetails" \ "firstName").as[String] mustBe "Jane"
      (detail \ "originalDetails" \ "surname").toOption mustBe None
      (detail \ "updatedDetails" \ "surname").toOption mustBe None
    }

    "must omit subbieResourceRef from the event when AmendSubbieResourceRefQuery is not set" in {
      val ua = emptyUserAnswers
        .set(TypeOfSubcontractorPage, TypeOfSubcontractor.Individualorsoletrader)
        .success
        .value

      service.amendSubcontractorEvent(ua)

      val detail = captureDetail()
      (detail \ "subbieResourceRef").toOption mustBe None
    }

    "must send a company amend event for a limited company" in {
      val ua = emptyUserAnswers
        .set(TypeOfSubcontractorPage, TypeOfSubcontractor.Limitedcompany)
        .success
        .value

      service.amendSubcontractorEvent(ua)

      val detail = captureDetail()
      (detail \ "typeOfSubcontractor").as[String] mustBe "company"
    }

    "must send a partnership amend event for a partnership" in {
      val ua = emptyUserAnswers
        .set(TypeOfSubcontractorPage, TypeOfSubcontractor.Partnership)
        .success
        .value

      service.amendSubcontractorEvent(ua)

      val detail = captureDetail()
      (detail \ "typeOfSubcontractor").as[String] mustBe "partnership"
    }

    "must send a trust amend event for a trust" in {
      val ua = emptyUserAnswers
        .set(TypeOfSubcontractorPage, TypeOfSubcontractor.Trust)
        .success
        .value

      service.amendSubcontractorEvent(ua)

      val detail = captureDetail()
      (detail \ "typeOfSubcontractor").as[String] mustBe "trust"
    }
  }
}
