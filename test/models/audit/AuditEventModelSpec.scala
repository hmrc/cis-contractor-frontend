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

package models.audit

import base.SpecBase
import models.address.{Address, Country}
import play.api.libs.json.{JsObject, Json}

class AuditEventModelSpec extends SpecBase {

  private val address = Address(
    addressLine1 = "4 Other Place",
    addressLine2 = Some("Some District"),
    addressLine3 = Some("Anytown"),
    postcode = Some("ZZ1 1ZZ"),
    country = Some(Country(Some("GB"), Some("United Kingdom"))),
    addressValidated = true
  )

  "AddSubcontractorAuditEventModel" - {

    "must serialise with only the mandatory field when all optionals are absent" in {
      val model = AddSubcontractorAuditEventModel(
        cisId = None,
        typeOfSubcontractor = "soletrader",
        firstName = None,
        middleName = None,
        surname = None,
        subTradingNameYesNo = None,
        tradingNameOfSubcontractor = None,
        subAddressYesNo = None,
        addressOfSubcontractor = None,
        addIndividualContactMethodsYesNo = None,
        individualContactMethodOptions = None,
        individualEmailAddress = None,
        individualPhoneNumber = None,
        individualMobileNumber = None,
        uniqueTaxpayerReferenceYesNo = None,
        subcontractorsUniqueTaxpayerReference = None,
        nationalInsuranceNumberYesNo = None,
        subNationalInsuranceNumber = None,
        worksReferenceNumberYesNo = None,
        worksReferenceNumber = None
      )
      Json.toJson(model) mustEqual Json.obj("typeOfSubcontractor" -> "soletrader")
    }

    "must serialise all fields when all are present" in {
      val model = AddSubcontractorAuditEventModel(
        cisId = Some("1"),
        typeOfSubcontractor = "soletrader",
        firstName = Some("John"),
        middleName = Some("Paul"),
        surname = Some("Smith"),
        subTradingNameYesNo = Some(true),
        tradingNameOfSubcontractor = Some("TradingName"),
        subAddressYesNo = Some(true),
        addressOfSubcontractor = Some(address),
        addIndividualContactMethodsYesNo = Some(true),
        individualContactMethodOptions = Some(Seq("email", "phone", "mobile")),
        individualEmailAddress = Some("test@test.com"),
        individualPhoneNumber = Some("+447960141611"),
        individualMobileNumber = Some("01912170507"),
        uniqueTaxpayerReferenceYesNo = Some(true),
        subcontractorsUniqueTaxpayerReference = Some("1111122222"),
        nationalInsuranceNumberYesNo = Some(true),
        subNationalInsuranceNumber = Some("NH112233D"),
        worksReferenceNumberYesNo = Some(true),
        worksReferenceNumber = Some("WORKREF-001")
      )
      Json.toJson(model) mustEqual Json.obj(
        "cisId"                                 -> "1",
        "typeOfSubcontractor"                   -> "soletrader",
        "firstName"                             -> "John",
        "middleName"                            -> "Paul",
        "surname"                               -> "Smith",
        "subTradingNameYesNo"                   -> true,
        "tradingNameOfSubcontractor"            -> "TradingName",
        "subAddressYesNo"                       -> true,
        "addressOfSubcontractor"                -> Json.toJson(address),
        "addIndividualContactMethodsYesNo"      -> true,
        "individualContactMethodOptions"        -> Json.arr("email", "phone", "mobile"),
        "individualEmailAddress"                -> "test@test.com",
        "individualPhoneNumber"                 -> "+447960141611",
        "individualMobileNumber"                -> "01912170507",
        "uniqueTaxpayerReferenceYesNo"          -> true,
        "subcontractorsUniqueTaxpayerReference" -> "1111122222",
        "nationalInsuranceNumberYesNo"          -> true,
        "subNationalInsuranceNumber"            -> "NH112233D",
        "worksReferenceNumberYesNo"             -> true,
        "worksReferenceNumber"                  -> "WORKREF-001"
      )
    }

    "must have auditType addSubcontractor" in {
      AddSubcontractorAuditEventModel(
        cisId = None,
        typeOfSubcontractor = "soletrader",
        firstName = None,
        middleName = None,
        surname = None,
        subTradingNameYesNo = None,
        tradingNameOfSubcontractor = None,
        subAddressYesNo = None,
        addressOfSubcontractor = None,
        addIndividualContactMethodsYesNo = None,
        individualContactMethodOptions = None,
        individualEmailAddress = None,
        individualPhoneNumber = None,
        individualMobileNumber = None,
        uniqueTaxpayerReferenceYesNo = None,
        subcontractorsUniqueTaxpayerReference = None,
        nationalInsuranceNumberYesNo = None,
        subNationalInsuranceNumber = None,
        worksReferenceNumberYesNo = None,
        worksReferenceNumber = None
      ).auditType mustBe "addSubcontractor"
    }
  }

  "AddCompanySubcontractorAuditEventModel" - {

    "must serialise with only the mandatory field when all optionals are absent" in {
      val model = AddCompanySubcontractorAuditEventModel(
        cisId = None,
        typeOfSubcontractor = "company",
        companyName = None,
        companyAddressYesNo = None,
        companyAddress = None,
        addCompanyContactMethodsYesNo = None,
        companyContactMethodOptions = None,
        companyEmailAddress = None,
        companyPhoneNumber = None,
        companyMobileNumber = None,
        companyUtrYesNo = None,
        companyUtr = None,
        companyCrnYesNo = None,
        companyCrn = None,
        companyWorksReferenceYesNo = None,
        companyWorksReference = None
      )
      Json.toJson(model) mustEqual Json.obj("typeOfSubcontractor" -> "company")
    }

    "must serialise all fields when all are present" in {
      val model = AddCompanySubcontractorAuditEventModel(
        cisId = Some("1"),
        typeOfSubcontractor = "company",
        companyName = Some("Test Co Ltd"),
        companyAddressYesNo = Some(true),
        companyAddress = Some(address),
        addCompanyContactMethodsYesNo = Some(true),
        companyContactMethodOptions = Some(Seq("email")),
        companyEmailAddress = Some("company@test.com"),
        companyPhoneNumber = Some("01912170507"),
        companyMobileNumber = Some("+447960141611"),
        companyUtrYesNo = Some(true),
        companyUtr = Some("1111122222"),
        companyCrnYesNo = Some(true),
        companyCrn = Some("12345678"),
        companyWorksReferenceYesNo = Some(true),
        companyWorksReference = Some("WORKREF-002")
      )
      Json.toJson(model) mustEqual Json.obj(
        "cisId"                         -> "1",
        "typeOfSubcontractor"           -> "company",
        "companyName"                   -> "Test Co Ltd",
        "companyAddressYesNo"           -> true,
        "companyAddress"                -> Json.toJson(address),
        "addCompanyContactMethodsYesNo" -> true,
        "companyContactMethodOptions"   -> Json.arr("email"),
        "companyEmailAddress"           -> "company@test.com",
        "companyPhoneNumber"            -> "01912170507",
        "companyMobileNumber"           -> "+447960141611",
        "companyUtrYesNo"               -> true,
        "companyUtr"                    -> "1111122222",
        "companyCrnYesNo"               -> true,
        "companyCrn"                    -> "12345678",
        "companyWorksReferenceYesNo"    -> true,
        "companyWorksReference"         -> "WORKREF-002"
      )
    }
  }

  "AddPartnershipSubcontractorAuditEventModel" - {

    "must serialise with only the mandatory field when all optionals are absent" in {
      val model = AddPartnershipSubcontractorAuditEventModel(
        cisId = None,
        typeOfSubcontractor = "partnership",
        partnershipName = None,
        partnershipAddressYesNo = None,
        partnershipAddress = None,
        addPartnershipContactMethodsYesNo = None,
        partnershipContactMethodOptions = None,
        partnershipEmailAddress = None,
        partnershipPhoneNumber = None,
        partnershipMobileNumber = None,
        partnershipHasUtrYesNo = None,
        partnershipUniqueTaxpayerReference = None,
        partnershipNominatedPartnerName = None,
        partnershipNominatedPartnerUtrYesNo = None,
        partnershipNominatedPartnerUtr = None,
        partnershipNominatedPartnerNinoYesNo = None,
        nominatedPartnerNationalInsuranceNumber = None,
        partnershipNominatedPartnerCrnYesNo = None,
        nominatedPartnerCompanyRegistrationNumber = None,
        partnershipWorksReferenceNumberYesNo = None,
        partnershipWorksReference = None
      )
      Json.toJson(model) mustEqual Json.obj("typeOfSubcontractor" -> "partnership")
    }

    "must serialise all fields when all are present" in {
      val model = AddPartnershipSubcontractorAuditEventModel(
        cisId = Some("1"),
        typeOfSubcontractor = "partnership",
        partnershipName = Some("Test Partnership"),
        partnershipAddressYesNo = Some(true),
        partnershipAddress = Some(address),
        addPartnershipContactMethodsYesNo = Some(true),
        partnershipContactMethodOptions = Some(Seq("email")),
        partnershipEmailAddress = Some("partnership@test.com"),
        partnershipPhoneNumber = Some("01912170507"),
        partnershipMobileNumber = Some("+447960141611"),
        partnershipHasUtrYesNo = Some(true),
        partnershipUniqueTaxpayerReference = Some("1111122222"),
        partnershipNominatedPartnerName = Some("Nominated Partner"),
        partnershipNominatedPartnerUtrYesNo = Some(true),
        partnershipNominatedPartnerUtr = Some("2222233333"),
        partnershipNominatedPartnerNinoYesNo = Some(true),
        nominatedPartnerNationalInsuranceNumber = Some("NH112233D"),
        partnershipNominatedPartnerCrnYesNo = Some(true),
        nominatedPartnerCompanyRegistrationNumber = Some("12345678"),
        partnershipWorksReferenceNumberYesNo = Some(true),
        partnershipWorksReference = Some("WORKREF-003")
      )
      Json.toJson(model) mustEqual Json.obj(
        "cisId"                                     -> "1",
        "typeOfSubcontractor"                       -> "partnership",
        "partnershipName"                           -> "Test Partnership",
        "partnershipAddressYesNo"                   -> true,
        "partnershipAddress"                        -> Json.toJson(address),
        "addPartnershipContactMethodsYesNo"         -> true,
        "partnershipContactMethodOptions"           -> Json.arr("email"),
        "partnershipEmailAddress"                   -> "partnership@test.com",
        "partnershipPhoneNumber"                    -> "01912170507",
        "partnershipMobileNumber"                   -> "+447960141611",
        "partnershipHasUtrYesNo"                    -> true,
        "partnershipUniqueTaxpayerReference"        -> "1111122222",
        "partnershipNominatedPartnerName"           -> "Nominated Partner",
        "partnershipNominatedPartnerUtrYesNo"       -> true,
        "partnershipNominatedPartnerUtr"            -> "2222233333",
        "partnershipNominatedPartnerNinoYesNo"      -> true,
        "nominatedPartnerNationalInsuranceNumber"   -> "NH112233D",
        "partnershipNominatedPartnerCrnYesNo"       -> true,
        "nominatedPartnerCompanyRegistrationNumber" -> "12345678",
        "partnershipWorksReferenceNumberYesNo"      -> true,
        "partnershipWorksReference"                 -> "WORKREF-003"
      )
    }
  }

  private val baseIndividualDetails = IndividualSubcontractorDetails(
    firstName = Some("John"),
    middleName = Some("Paul"),
    surname = Some("Smith"),
    subTradingNameYesNo = Some(false),
    tradingNameOfSubcontractor = None,
    subAddressYesNo = Some(true),
    addressOfSubcontractor = Some(address),
    addIndividualContactMethodsYesNo = Some(true),
    individualContactMethodOptions = Some(Seq("email", "phone", "mobile")),
    individualEmailAddress = Some("sub@example.com"),
    individualPhoneNumber = Some("01234567890"),
    individualMobileNumber = Some("07123456789"),
    uniqueTaxpayerReferenceYesNo = Some(true),
    subcontractorsUniqueTaxpayerReference = Some("2736707626"),
    nationalInsuranceNumberYesNo = Some(true),
    subNationalInsuranceNumber = Some("AA123456C"),
    worksReferenceNumberYesNo = Some(true),
    worksReferenceNumber = Some("WR-123")
  )

  private val baseCompanyDetails = CompanySubcontractorDetails(
    companyName = Some("Test Co Ltd"),
    companyAddressYesNo = Some(true),
    companyAddress = Some(address),
    addCompanyContactMethodsYesNo = Some(true),
    companyContactMethodOptions = Some(Seq("email")),
    companyEmailAddress = Some("company@example.com"),
    companyPhoneNumber = Some("01234567890"),
    companyMobileNumber = Some("07123456789"),
    companyUtrYesNo = Some(true),
    companyUtr = Some("1111122222"),
    companyCrnYesNo = Some(true),
    companyCrn = Some("12345678"),
    companyWorksReferenceYesNo = Some(true),
    companyWorksReference = Some("WR-456")
  )

  private val basePartnershipDetails = PartnershipSubcontractorDetails(
    partnershipName = Some("Test Partnership"),
    partnershipAddressYesNo = Some(true),
    partnershipAddress = Some(address),
    addPartnershipContactMethodsYesNo = Some(true),
    partnershipContactMethodOptions = Some(Seq("email")),
    partnershipEmailAddress = Some("partnership@example.com"),
    partnershipPhoneNumber = Some("01234567890"),
    partnershipMobileNumber = Some("07123456789"),
    partnershipHasUtrYesNo = Some(true),
    partnershipUniqueTaxpayerReference = Some("1111122222"),
    partnershipNominatedPartnerName = Some("Nominated Partner"),
    partnershipNominatedPartnerUtrYesNo = Some(true),
    partnershipNominatedPartnerUtr = Some("2222233333"),
    partnershipNominatedPartnerNinoYesNo = Some(true),
    nominatedPartnerNationalInsuranceNumber = Some("NH112233D"),
    partnershipNominatedPartnerCrnYesNo = Some(true),
    nominatedPartnerCompanyRegistrationNumber = Some("12345678"),
    partnershipWorksReferenceNumberYesNo = Some(true),
    partnershipWorksReference = Some("WR-789")
  )

  private val baseTrustDetails = TrustSubcontractorDetails(
    trustName = Some("Test Trust"),
    trustAddressYesNo = Some(true),
    trustAddress = Some(address),
    addTrustContactMethodsYesNo = Some(true),
    trustContactMethodOptions = Some(Seq("email")),
    trustEmailAddress = Some("trust@example.com"),
    trustPhoneNumber = Some("01234567890"),
    trustMobileNumber = Some("07123456789"),
    trustUtrYesNo = Some(true),
    trustUtr = Some("1111122222"),
    trustWorksReferenceYesNo = Some(true),
    trustWorksReference = Some("WR-012")
  )

  "AmendSubcontractorAuditEventModel" - {

    "must only emit the changed field in each section when one field differs" in {
      val updated = baseIndividualDetails.copy(individualMobileNumber = Some("07123456999"))
      val model   = AmendSubcontractorAuditEventModel(
        cisId = Some("1"),
        subbieResourceRef = None,
        typeOfSubcontractor = "soletrader",
        originalDetails = Some(baseIndividualDetails),
        updatedDetails = updated
      )
      val json    = Json.toJson(model)
      (json \ "originalDetails").as[JsObject] mustEqual Json.obj("individualMobileNumber" -> "07123456789")
      (json \ "updatedDetails").as[JsObject] mustEqual Json.obj("individualMobileNumber" -> "07123456999")
    }

    "must emit all changed fields when multiple values differ" in {
      val updated = baseIndividualDetails.copy(
        individualEmailAddress = Some("new@example.com"),
        worksReferenceNumber = Some("WR-999")
      )
      val model   = AmendSubcontractorAuditEventModel(
        cisId = Some("1"),
        subbieResourceRef = None,
        typeOfSubcontractor = "soletrader",
        originalDetails = Some(baseIndividualDetails),
        updatedDetails = updated
      )
      val json    = Json.toJson(model)
      (json \ "originalDetails").as[JsObject].keys mustBe Set("individualEmailAddress", "worksReferenceNumber")
      (json \ "updatedDetails").as[JsObject].keys mustBe Set("individualEmailAddress", "worksReferenceNumber")
      (json \ "originalDetails" \ "individualEmailAddress").as[String] mustBe "sub@example.com"
      (json \ "updatedDetails" \ "individualEmailAddress").as[String] mustBe "new@example.com"
      (json \ "originalDetails" \ "worksReferenceNumber").as[String] mustBe "WR-123"
      (json \ "updatedDetails" \ "worksReferenceNumber").as[String] mustBe "WR-999"
    }

    "must include original value in originalDetails when a field is removed in the update" in {
      val updated = baseIndividualDetails.copy(worksReferenceNumberYesNo = Some(false), worksReferenceNumber = None)
      val model   = AmendSubcontractorAuditEventModel(
        cisId = Some("1"),
        subbieResourceRef = None,
        typeOfSubcontractor = "soletrader",
        originalDetails = Some(baseIndividualDetails),
        updatedDetails = updated
      )
      val json    = Json.toJson(model)
      (json \ "originalDetails" \ "worksReferenceNumberYesNo").as[Boolean] mustBe true
      (json \ "originalDetails" \ "worksReferenceNumber").as[String] mustBe "WR-123"
      (json \ "updatedDetails" \ "worksReferenceNumberYesNo").as[Boolean] mustBe false
      (json \ "updatedDetails" \ "worksReferenceNumber").toOption mustBe None
    }

    "must omit originalDetails and emit full updatedDetails when originalDetails is absent" in {
      val model = AmendSubcontractorAuditEventModel(
        cisId = Some("1"),
        subbieResourceRef = None,
        typeOfSubcontractor = "soletrader",
        originalDetails = None,
        updatedDetails = baseIndividualDetails
      )
      val json  = Json.toJson(model)
      (json \ "originalDetails").toOption mustBe None
      (json \ "updatedDetails").as[JsObject] mustEqual Json.toJson(baseIndividualDetails).as[JsObject]
    }

    "must omit cisId when absent" in {
      val model = AmendSubcontractorAuditEventModel(
        cisId = None,
        subbieResourceRef = None,
        typeOfSubcontractor = "soletrader",
        originalDetails = None,
        updatedDetails = baseIndividualDetails
      )
      (Json.toJson(model) \ "cisId").toOption mustBe None
    }

    "must include subbieResourceRef in JSON when present" in {
      val model = AmendSubcontractorAuditEventModel(
        cisId = Some("1"),
        subbieResourceRef = Some(42),
        typeOfSubcontractor = "soletrader",
        originalDetails = None,
        updatedDetails = baseIndividualDetails
      )
      (Json.toJson(model) \ "subbieResourceRef").as[Int] mustBe 42
    }

    "must omit subbieResourceRef when absent" in {
      val model = AmendSubcontractorAuditEventModel(
        cisId = None,
        subbieResourceRef = None,
        typeOfSubcontractor = "soletrader",
        originalDetails = None,
        updatedDetails = baseIndividualDetails
      )
      (Json.toJson(model) \ "subbieResourceRef").toOption mustBe None
    }

    "must have auditType amendSubcontractor" in {
      AmendSubcontractorAuditEventModel(
        cisId = None,
        subbieResourceRef = None,
        typeOfSubcontractor = "soletrader",
        originalDetails = None,
        updatedDetails = baseIndividualDetails
      ).auditType mustBe "amendSubcontractor"
    }
  }

  "AmendCompanySubcontractorAuditEventModel" - {

    "must only emit the changed field in each section when one field differs" in {
      val updated = baseCompanyDetails.copy(companyEmailAddress = Some("new@example.com"))
      val model   = AmendCompanySubcontractorAuditEventModel(
        cisId = Some("1"),
        subbieResourceRef = None,
        typeOfSubcontractor = "company",
        originalDetails = Some(baseCompanyDetails),
        updatedDetails = updated
      )
      val json    = Json.toJson(model)
      (json \ "originalDetails").as[JsObject] mustEqual Json.obj("companyEmailAddress" -> "company@example.com")
      (json \ "updatedDetails").as[JsObject] mustEqual Json.obj("companyEmailAddress" -> "new@example.com")
    }

    "must omit originalDetails and emit full updatedDetails when originalDetails is absent" in {
      val model = AmendCompanySubcontractorAuditEventModel(
        cisId = None,
        subbieResourceRef = None,
        typeOfSubcontractor = "company",
        originalDetails = None,
        updatedDetails = baseCompanyDetails
      )
      val json  = Json.toJson(model)
      (json \ "originalDetails").toOption mustBe None
      (json \ "updatedDetails").as[JsObject] mustEqual Json.toJson(baseCompanyDetails).as[JsObject]
    }

    "must include subbieResourceRef in JSON when present" in {
      val model = AmendCompanySubcontractorAuditEventModel(
        cisId = None,
        subbieResourceRef = Some(7),
        typeOfSubcontractor = "company",
        originalDetails = None,
        updatedDetails = baseCompanyDetails
      )
      (Json.toJson(model) \ "subbieResourceRef").as[Int] mustBe 7
    }

    "must have auditType amendSubcontractor" in {
      AmendCompanySubcontractorAuditEventModel(
        cisId = None,
        subbieResourceRef = None,
        typeOfSubcontractor = "company",
        originalDetails = None,
        updatedDetails = baseCompanyDetails
      ).auditType mustBe "amendSubcontractor"
    }
  }

  "AmendPartnershipSubcontractorAuditEventModel" - {

    "must only emit the changed field in each section when one field differs" in {
      val updated = basePartnershipDetails.copy(partnershipEmailAddress = Some("new@example.com"))
      val model   = AmendPartnershipSubcontractorAuditEventModel(
        cisId = Some("1"),
        subbieResourceRef = None,
        typeOfSubcontractor = "partnership",
        originalDetails = Some(basePartnershipDetails),
        updatedDetails = updated
      )
      val json    = Json.toJson(model)
      (json \ "originalDetails").as[JsObject] mustEqual Json.obj(
        "partnershipEmailAddress" -> "partnership@example.com"
      )
      (json \ "updatedDetails").as[JsObject] mustEqual Json.obj("partnershipEmailAddress" -> "new@example.com")
    }

    "must omit originalDetails and emit full updatedDetails when originalDetails is absent" in {
      val model = AmendPartnershipSubcontractorAuditEventModel(
        cisId = None,
        subbieResourceRef = None,
        typeOfSubcontractor = "partnership",
        originalDetails = None,
        updatedDetails = basePartnershipDetails
      )
      val json  = Json.toJson(model)
      (json \ "originalDetails").toOption mustBe None
      (json \ "updatedDetails").as[JsObject] mustEqual Json.toJson(basePartnershipDetails).as[JsObject]
    }

    "must include subbieResourceRef in JSON when present" in {
      val model = AmendPartnershipSubcontractorAuditEventModel(
        cisId = None,
        subbieResourceRef = Some(3),
        typeOfSubcontractor = "partnership",
        originalDetails = None,
        updatedDetails = basePartnershipDetails
      )
      (Json.toJson(model) \ "subbieResourceRef").as[Int] mustBe 3
    }

    "must have auditType amendSubcontractor" in {
      AmendPartnershipSubcontractorAuditEventModel(
        cisId = None,
        subbieResourceRef = None,
        typeOfSubcontractor = "partnership",
        originalDetails = None,
        updatedDetails = basePartnershipDetails
      ).auditType mustBe "amendSubcontractor"
    }
  }

  "AmendTrustSubcontractorAuditEventModel" - {

    "must only emit the changed field in each section when one field differs" in {
      val updated = baseTrustDetails.copy(trustEmailAddress = Some("new@example.com"))
      val model   = AmendTrustSubcontractorAuditEventModel(
        cisId = Some("1"),
        subbieResourceRef = None,
        typeOfSubcontractor = "trust",
        originalDetails = Some(baseTrustDetails),
        updatedDetails = updated
      )
      val json    = Json.toJson(model)
      (json \ "originalDetails").as[JsObject] mustEqual Json.obj("trustEmailAddress" -> "trust@example.com")
      (json \ "updatedDetails").as[JsObject] mustEqual Json.obj("trustEmailAddress" -> "new@example.com")
    }

    "must omit originalDetails and emit full updatedDetails when originalDetails is absent" in {
      val model = AmendTrustSubcontractorAuditEventModel(
        cisId = None,
        subbieResourceRef = None,
        typeOfSubcontractor = "trust",
        originalDetails = None,
        updatedDetails = baseTrustDetails
      )
      val json  = Json.toJson(model)
      (json \ "originalDetails").toOption mustBe None
      (json \ "updatedDetails").as[JsObject] mustEqual Json.toJson(baseTrustDetails).as[JsObject]
    }

    "must include subbieResourceRef in JSON when present" in {
      val model = AmendTrustSubcontractorAuditEventModel(
        cisId = None,
        subbieResourceRef = Some(99),
        typeOfSubcontractor = "trust",
        originalDetails = None,
        updatedDetails = baseTrustDetails
      )
      (Json.toJson(model) \ "subbieResourceRef").as[Int] mustBe 99
    }

    "must have auditType amendSubcontractor" in {
      AmendTrustSubcontractorAuditEventModel(
        cisId = None,
        subbieResourceRef = None,
        typeOfSubcontractor = "trust",
        originalDetails = None,
        updatedDetails = baseTrustDetails
      ).auditType mustBe "amendSubcontractor"
    }
  }

  "AddTrustSubcontractorAuditEventModel" - {

    "must serialise with only the mandatory field when all optionals are absent" in {
      val model = AddTrustSubcontractorAuditEventModel(
        cisId = None,
        typeOfSubcontractor = "trust",
        trustName = None,
        trustAddressYesNo = None,
        trustAddress = None,
        addTrustContactMethodsYesNo = None,
        trustContactMethodOptions = None,
        trustEmailAddress = None,
        trustPhoneNumber = None,
        trustMobileNumber = None,
        trustUtrYesNo = None,
        trustUtr = None,
        trustWorksReferenceYesNo = None,
        trustWorksReference = None
      )
      Json.toJson(model) mustEqual Json.obj("typeOfSubcontractor" -> "trust")
    }

    "must serialise all fields when all are present" in {
      val model = AddTrustSubcontractorAuditEventModel(
        cisId = Some("1"),
        typeOfSubcontractor = "trust",
        trustName = Some("Test Trust"),
        trustAddressYesNo = Some(true),
        trustAddress = Some(address),
        addTrustContactMethodsYesNo = Some(true),
        trustContactMethodOptions = Some(Seq("email")),
        trustEmailAddress = Some("trust@test.com"),
        trustPhoneNumber = Some("01912170507"),
        trustMobileNumber = Some("+447960141611"),
        trustUtrYesNo = Some(true),
        trustUtr = Some("1111122222"),
        trustWorksReferenceYesNo = Some(true),
        trustWorksReference = Some("WORKREF-004")
      )
      Json.toJson(model) mustEqual Json.obj(
        "cisId"                       -> "1",
        "typeOfSubcontractor"         -> "trust",
        "trustName"                   -> "Test Trust",
        "trustAddressYesNo"           -> true,
        "trustAddress"                -> Json.toJson(address),
        "addTrustContactMethodsYesNo" -> true,
        "trustContactMethodOptions"   -> Json.arr("email"),
        "trustEmailAddress"           -> "trust@test.com",
        "trustPhoneNumber"            -> "01912170507",
        "trustMobileNumber"           -> "+447960141611",
        "trustUtrYesNo"               -> true,
        "trustUtr"                    -> "1111122222",
        "trustWorksReferenceYesNo"    -> true,
        "trustWorksReference"         -> "WORKREF-004"
      )
    }
  }
}
