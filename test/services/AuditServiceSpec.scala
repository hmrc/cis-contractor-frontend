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
import models.address.{Address, Country}
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
      (detail \ "subTradingNameYesNo").toOption mustBe None
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
        .set(SubTradingNameYesNoPage, true)
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

      service.addSubcontractorEvent(ua)

      val detail = captureDetail()
      (detail \ "typeOfSubcontractor").as[String] mustBe "soletrader"
      (detail \ "subTradingNameYesNo").as[Boolean] mustBe true
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
}
