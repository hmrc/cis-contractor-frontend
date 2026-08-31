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

import models.address.Address
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{JsObject, Json, OWrites, __}

trait AuditEvent {
  val auditType: String
}

private def diffDetails(original: JsObject, updated: JsObject): (JsObject, JsObject) = {
  val changedKeys = updated.keys.filter(k => (original \ k).toOption != (updated \ k).toOption)
  val origDiff    = JsObject(changedKeys.flatMap(k => (original \ k).toOption.map(k -> _)).toSeq)
  val updDiff     = JsObject(changedKeys.flatMap(k => (updated \ k).toOption.map(k -> _)).toSeq)
  (origDiff, updDiff)
}

case class AddSubcontractorAuditEventModel(
  cisId: Option[String],
  typeOfSubcontractor: String,
  subTradingNameYesNo: Option[Boolean],
  tradingNameOfSubcontractor: Option[String],
  subAddressYesNo: Option[Boolean],
  addressOfSubcontractor: Option[Address],
  addIndividualContactMethodsYesNo: Option[Boolean],
  individualContactMethodOptions: Option[Seq[String]],
  individualEmailAddress: Option[String],
  individualPhoneNumber: Option[String],
  individualMobileNumber: Option[String],
  uniqueTaxpayerReferenceYesNo: Option[Boolean],
  subcontractorsUniqueTaxpayerReference: Option[String],
  nationalInsuranceNumberYesNo: Option[Boolean],
  subNationalInsuranceNumber: Option[String],
  worksReferenceNumberYesNo: Option[Boolean],
  worksReferenceNumber: Option[String]
) extends AuditEvent {
  override val auditType: String = "addSubcontractor"
}

object AddSubcontractorAuditEventModel {
  implicit val writes: OWrites[AddSubcontractorAuditEventModel] = (
    (__ \ "cisId").writeNullable[String] and
      (__ \ "typeOfSubcontractor").write[String] and
      (__ \ "subTradingNameYesNo").writeNullable[Boolean] and
      (__ \ "tradingNameOfSubcontractor").writeNullable[String] and
      (__ \ "subAddressYesNo").writeNullable[Boolean] and
      (__ \ "addressOfSubcontractor").writeNullable[Address] and
      (__ \ "addIndividualContactMethodsYesNo").writeNullable[Boolean] and
      (__ \ "individualContactMethodOptions").writeNullable[Seq[String]] and
      (__ \ "individualEmailAddress").writeNullable[String] and
      (__ \ "individualPhoneNumber").writeNullable[String] and
      (__ \ "individualMobileNumber").writeNullable[String] and
      (__ \ "uniqueTaxpayerReferenceYesNo").writeNullable[Boolean] and
      (__ \ "subcontractorsUniqueTaxpayerReference").writeNullable[String] and
      (__ \ "nationalInsuranceNumberYesNo").writeNullable[Boolean] and
      (__ \ "subNationalInsuranceNumber").writeNullable[String] and
      (__ \ "worksReferenceNumberYesNo").writeNullable[Boolean] and
      (__ \ "worksReferenceNumber").writeNullable[String]
  )(Tuple.fromProductTyped(_))
}

case class AddCompanySubcontractorAuditEventModel(
  cisId: Option[String],
  typeOfSubcontractor: String,
  companyName: Option[String],
  companyAddressYesNo: Option[Boolean],
  companyAddress: Option[Address],
  addCompanyContactMethodsYesNo: Option[Boolean],
  companyContactMethodOptions: Option[Seq[String]],
  companyEmailAddress: Option[String],
  companyPhoneNumber: Option[String],
  companyMobileNumber: Option[String],
  companyUtrYesNo: Option[Boolean],
  companyUtr: Option[String],
  companyCrnYesNo: Option[Boolean],
  companyCrn: Option[String],
  companyWorksReferenceYesNo: Option[Boolean],
  companyWorksReference: Option[String]
) extends AuditEvent {
  override val auditType: String = "addSubcontractor"
}

object AddCompanySubcontractorAuditEventModel {
  implicit val writes: OWrites[AddCompanySubcontractorAuditEventModel] = (
    (__ \ "cisId").writeNullable[String] and
      (__ \ "typeOfSubcontractor").write[String] and
      (__ \ "companyName").writeNullable[String] and
      (__ \ "companyAddressYesNo").writeNullable[Boolean] and
      (__ \ "companyAddress").writeNullable[Address] and
      (__ \ "addCompanyContactMethodsYesNo").writeNullable[Boolean] and
      (__ \ "companyContactMethodOptions").writeNullable[Seq[String]] and
      (__ \ "companyEmailAddress").writeNullable[String] and
      (__ \ "companyPhoneNumber").writeNullable[String] and
      (__ \ "companyMobileNumber").writeNullable[String] and
      (__ \ "companyUtrYesNo").writeNullable[Boolean] and
      (__ \ "companyUtr").writeNullable[String] and
      (__ \ "companyCrnYesNo").writeNullable[Boolean] and
      (__ \ "companyCrn").writeNullable[String] and
      (__ \ "companyWorksReferenceYesNo").writeNullable[Boolean] and
      (__ \ "companyWorksReference").writeNullable[String]
  )(Tuple.fromProductTyped(_))
}

case class AddPartnershipSubcontractorAuditEventModel(
  cisId: Option[String],
  typeOfSubcontractor: String,
  partnershipName: Option[String],
  partnershipAddressYesNo: Option[Boolean],
  partnershipAddress: Option[Address],
  addPartnershipContactMethodsYesNo: Option[Boolean],
  partnershipContactMethodOptions: Option[Seq[String]],
  partnershipEmailAddress: Option[String],
  partnershipPhoneNumber: Option[String],
  partnershipMobileNumber: Option[String],
  partnershipHasUtrYesNo: Option[Boolean],
  partnershipUniqueTaxpayerReference: Option[String],
  partnershipNominatedPartnerName: Option[String],
  partnershipNominatedPartnerUtrYesNo: Option[Boolean],
  partnershipNominatedPartnerUtr: Option[String],
  partnershipNominatedPartnerNinoYesNo: Option[Boolean],
  nominatedPartnerNationalInsuranceNumber: Option[String],
  partnershipNominatedPartnerCrnYesNo: Option[Boolean],
  nominatedPartnerCompanyRegistrationNumber: Option[String],
  partnershipWorksReferenceNumberYesNo: Option[Boolean],
  partnershipWorksReference: Option[String]
) extends AuditEvent {
  override val auditType: String = "addSubcontractor"
}

object AddPartnershipSubcontractorAuditEventModel {
  implicit val writes: OWrites[AddPartnershipSubcontractorAuditEventModel] = (
    (__ \ "cisId").writeNullable[String] and
      (__ \ "typeOfSubcontractor").write[String] and
      (__ \ "partnershipName").writeNullable[String] and
      (__ \ "partnershipAddressYesNo").writeNullable[Boolean] and
      (__ \ "partnershipAddress").writeNullable[Address] and
      (__ \ "addPartnershipContactMethodsYesNo").writeNullable[Boolean] and
      (__ \ "partnershipContactMethodOptions").writeNullable[Seq[String]] and
      (__ \ "partnershipEmailAddress").writeNullable[String] and
      (__ \ "partnershipPhoneNumber").writeNullable[String] and
      (__ \ "partnershipMobileNumber").writeNullable[String] and
      (__ \ "partnershipHasUtrYesNo").writeNullable[Boolean] and
      (__ \ "partnershipUniqueTaxpayerReference").writeNullable[String] and
      (__ \ "partnershipNominatedPartnerName").writeNullable[String] and
      (__ \ "partnershipNominatedPartnerUtrYesNo").writeNullable[Boolean] and
      (__ \ "partnershipNominatedPartnerUtr").writeNullable[String] and
      (__ \ "partnershipNominatedPartnerNinoYesNo").writeNullable[Boolean] and
      (__ \ "nominatedPartnerNationalInsuranceNumber").writeNullable[String] and
      (__ \ "partnershipNominatedPartnerCrnYesNo").writeNullable[Boolean] and
      (__ \ "nominatedPartnerCompanyRegistrationNumber").writeNullable[String] and
      (__ \ "partnershipWorksReferenceNumberYesNo").writeNullable[Boolean] and
      (__ \ "partnershipWorksReference").writeNullable[String]
  )(Tuple.fromProductTyped(_))
}

case class AddTrustSubcontractorAuditEventModel(
  cisId: Option[String],
  typeOfSubcontractor: String,
  trustName: Option[String],
  trustAddressYesNo: Option[Boolean],
  trustAddress: Option[Address],
  addTrustContactMethodsYesNo: Option[Boolean],
  trustContactMethodOptions: Option[Seq[String]],
  trustEmailAddress: Option[String],
  trustPhoneNumber: Option[String],
  trustMobileNumber: Option[String],
  trustUtrYesNo: Option[Boolean],
  trustUtr: Option[String],
  trustWorksReferenceYesNo: Option[Boolean],
  trustWorksReference: Option[String]
) extends AuditEvent {
  override val auditType: String = "addSubcontractor"
}

object AddTrustSubcontractorAuditEventModel {
  implicit val writes: OWrites[AddTrustSubcontractorAuditEventModel] = (
    (__ \ "cisId").writeNullable[String] and
      (__ \ "typeOfSubcontractor").write[String] and
      (__ \ "trustName").writeNullable[String] and
      (__ \ "trustAddressYesNo").writeNullable[Boolean] and
      (__ \ "trustAddress").writeNullable[Address] and
      (__ \ "addTrustContactMethodsYesNo").writeNullable[Boolean] and
      (__ \ "trustContactMethodOptions").writeNullable[Seq[String]] and
      (__ \ "trustEmailAddress").writeNullable[String] and
      (__ \ "trustPhoneNumber").writeNullable[String] and
      (__ \ "trustMobileNumber").writeNullable[String] and
      (__ \ "trustUtrYesNo").writeNullable[Boolean] and
      (__ \ "trustUtr").writeNullable[String] and
      (__ \ "trustWorksReferenceYesNo").writeNullable[Boolean] and
      (__ \ "trustWorksReference").writeNullable[String]
  )(Tuple.fromProductTyped(_))
}

case class IndividualSubcontractorDetails(
  subTradingNameYesNo: Option[Boolean],
  tradingNameOfSubcontractor: Option[String],
  subAddressYesNo: Option[Boolean],
  addressOfSubcontractor: Option[Address],
  addIndividualContactMethodsYesNo: Option[Boolean],
  individualContactMethodOptions: Option[Seq[String]],
  individualEmailAddress: Option[String],
  individualPhoneNumber: Option[String],
  individualMobileNumber: Option[String],
  uniqueTaxpayerReferenceYesNo: Option[Boolean],
  subcontractorsUniqueTaxpayerReference: Option[String],
  nationalInsuranceNumberYesNo: Option[Boolean],
  subNationalInsuranceNumber: Option[String],
  worksReferenceNumberYesNo: Option[Boolean],
  worksReferenceNumber: Option[String]
)

object IndividualSubcontractorDetails {
  implicit val writes: OWrites[IndividualSubcontractorDetails] = (
    (__ \ "subTradingNameYesNo").writeNullable[Boolean] and
      (__ \ "tradingNameOfSubcontractor").writeNullable[String] and
      (__ \ "subAddressYesNo").writeNullable[Boolean] and
      (__ \ "addressOfSubcontractor").writeNullable[Address] and
      (__ \ "addIndividualContactMethodsYesNo").writeNullable[Boolean] and
      (__ \ "individualContactMethodOptions").writeNullable[Seq[String]] and
      (__ \ "individualEmailAddress").writeNullable[String] and
      (__ \ "individualPhoneNumber").writeNullable[String] and
      (__ \ "individualMobileNumber").writeNullable[String] and
      (__ \ "uniqueTaxpayerReferenceYesNo").writeNullable[Boolean] and
      (__ \ "subcontractorsUniqueTaxpayerReference").writeNullable[String] and
      (__ \ "nationalInsuranceNumberYesNo").writeNullable[Boolean] and
      (__ \ "subNationalInsuranceNumber").writeNullable[String] and
      (__ \ "worksReferenceNumberYesNo").writeNullable[Boolean] and
      (__ \ "worksReferenceNumber").writeNullable[String]
  )(Tuple.fromProductTyped(_))
}

case class AmendSubcontractorAuditEventModel(
  cisId: Option[String],
  subbieResourceRef: Option[Int],
  typeOfSubcontractor: String,
  originalDetails: Option[IndividualSubcontractorDetails],
  updatedDetails: IndividualSubcontractorDetails
) extends AuditEvent {
  override val auditType: String = "amendSubcontractor"
}

object AmendSubcontractorAuditEventModel {
  implicit val writes: OWrites[AmendSubcontractorAuditEventModel] = OWrites { model =>
    val updatedJson                = Json.toJson(model.updatedDetails).as[JsObject]
    val (origDiffOpt, updatedDiff) = model.originalDetails match {
      case Some(orig) =>
        val (o, u) = diffDetails(Json.toJson(orig).as[JsObject], updatedJson)
        (Some(o), u)
      case None       => (None, updatedJson)
    }
    val base                       = Json.obj("typeOfSubcontractor" -> model.typeOfSubcontractor) ++
      origDiffOpt.fold(Json.obj())(o => Json.obj("originalDetails" -> o)) ++
      Json.obj("updatedDetails" -> updatedDiff)
    val withCisId                  = model.cisId.fold(base)(id => Json.obj("cisId" -> id) ++ base)
    model.subbieResourceRef.fold(withCisId)(ref => withCisId ++ Json.obj("subbieResourceRef" -> ref))
  }
}

case class CompanySubcontractorDetails(
  companyName: Option[String],
  companyAddressYesNo: Option[Boolean],
  companyAddress: Option[Address],
  addCompanyContactMethodsYesNo: Option[Boolean],
  companyContactMethodOptions: Option[Seq[String]],
  companyEmailAddress: Option[String],
  companyPhoneNumber: Option[String],
  companyMobileNumber: Option[String],
  companyUtrYesNo: Option[Boolean],
  companyUtr: Option[String],
  companyCrnYesNo: Option[Boolean],
  companyCrn: Option[String],
  companyWorksReferenceYesNo: Option[Boolean],
  companyWorksReference: Option[String]
)

object CompanySubcontractorDetails {
  implicit val writes: OWrites[CompanySubcontractorDetails] = (
    (__ \ "companyName").writeNullable[String] and
      (__ \ "companyAddressYesNo").writeNullable[Boolean] and
      (__ \ "companyAddress").writeNullable[Address] and
      (__ \ "addCompanyContactMethodsYesNo").writeNullable[Boolean] and
      (__ \ "companyContactMethodOptions").writeNullable[Seq[String]] and
      (__ \ "companyEmailAddress").writeNullable[String] and
      (__ \ "companyPhoneNumber").writeNullable[String] and
      (__ \ "companyMobileNumber").writeNullable[String] and
      (__ \ "companyUtrYesNo").writeNullable[Boolean] and
      (__ \ "companyUtr").writeNullable[String] and
      (__ \ "companyCrnYesNo").writeNullable[Boolean] and
      (__ \ "companyCrn").writeNullable[String] and
      (__ \ "companyWorksReferenceYesNo").writeNullable[Boolean] and
      (__ \ "companyWorksReference").writeNullable[String]
  )(Tuple.fromProductTyped(_))
}

case class AmendCompanySubcontractorAuditEventModel(
  cisId: Option[String],
  subbieResourceRef: Option[Int],
  typeOfSubcontractor: String,
  originalDetails: Option[CompanySubcontractorDetails],
  updatedDetails: CompanySubcontractorDetails
) extends AuditEvent {
  override val auditType: String = "amendSubcontractor"
}

object AmendCompanySubcontractorAuditEventModel {
  implicit val writes: OWrites[AmendCompanySubcontractorAuditEventModel] = OWrites { model =>
    val updatedJson                = Json.toJson(model.updatedDetails).as[JsObject]
    val (origDiffOpt, updatedDiff) = model.originalDetails match {
      case Some(orig) =>
        val (o, u) = diffDetails(Json.toJson(orig).as[JsObject], updatedJson)
        (Some(o), u)
      case None       => (None, updatedJson)
    }
    val base                       = Json.obj("typeOfSubcontractor" -> model.typeOfSubcontractor) ++
      origDiffOpt.fold(Json.obj())(o => Json.obj("originalDetails" -> o)) ++
      Json.obj("updatedDetails" -> updatedDiff)
    val withCisId                  = model.cisId.fold(base)(id => Json.obj("cisId" -> id) ++ base)
    model.subbieResourceRef.fold(withCisId)(ref => withCisId ++ Json.obj("subbieResourceRef" -> ref))
  }
}

case class PartnershipSubcontractorDetails(
  partnershipName: Option[String],
  partnershipAddressYesNo: Option[Boolean],
  partnershipAddress: Option[Address],
  addPartnershipContactMethodsYesNo: Option[Boolean],
  partnershipContactMethodOptions: Option[Seq[String]],
  partnershipEmailAddress: Option[String],
  partnershipPhoneNumber: Option[String],
  partnershipMobileNumber: Option[String],
  partnershipHasUtrYesNo: Option[Boolean],
  partnershipUniqueTaxpayerReference: Option[String],
  partnershipNominatedPartnerName: Option[String],
  partnershipNominatedPartnerUtrYesNo: Option[Boolean],
  partnershipNominatedPartnerUtr: Option[String],
  partnershipNominatedPartnerNinoYesNo: Option[Boolean],
  nominatedPartnerNationalInsuranceNumber: Option[String],
  partnershipNominatedPartnerCrnYesNo: Option[Boolean],
  nominatedPartnerCompanyRegistrationNumber: Option[String],
  partnershipWorksReferenceNumberYesNo: Option[Boolean],
  partnershipWorksReference: Option[String]
)

object PartnershipSubcontractorDetails {
  implicit val writes: OWrites[PartnershipSubcontractorDetails] = (
    (__ \ "partnershipName").writeNullable[String] and
      (__ \ "partnershipAddressYesNo").writeNullable[Boolean] and
      (__ \ "partnershipAddress").writeNullable[Address] and
      (__ \ "addPartnershipContactMethodsYesNo").writeNullable[Boolean] and
      (__ \ "partnershipContactMethodOptions").writeNullable[Seq[String]] and
      (__ \ "partnershipEmailAddress").writeNullable[String] and
      (__ \ "partnershipPhoneNumber").writeNullable[String] and
      (__ \ "partnershipMobileNumber").writeNullable[String] and
      (__ \ "partnershipHasUtrYesNo").writeNullable[Boolean] and
      (__ \ "partnershipUniqueTaxpayerReference").writeNullable[String] and
      (__ \ "partnershipNominatedPartnerName").writeNullable[String] and
      (__ \ "partnershipNominatedPartnerUtrYesNo").writeNullable[Boolean] and
      (__ \ "partnershipNominatedPartnerUtr").writeNullable[String] and
      (__ \ "partnershipNominatedPartnerNinoYesNo").writeNullable[Boolean] and
      (__ \ "nominatedPartnerNationalInsuranceNumber").writeNullable[String] and
      (__ \ "partnershipNominatedPartnerCrnYesNo").writeNullable[Boolean] and
      (__ \ "nominatedPartnerCompanyRegistrationNumber").writeNullable[String] and
      (__ \ "partnershipWorksReferenceNumberYesNo").writeNullable[Boolean] and
      (__ \ "partnershipWorksReference").writeNullable[String]
  )(Tuple.fromProductTyped(_))
}

case class AmendPartnershipSubcontractorAuditEventModel(
  cisId: Option[String],
  subbieResourceRef: Option[Int],
  typeOfSubcontractor: String,
  originalDetails: Option[PartnershipSubcontractorDetails],
  updatedDetails: PartnershipSubcontractorDetails
) extends AuditEvent {
  override val auditType: String = "amendSubcontractor"
}

object AmendPartnershipSubcontractorAuditEventModel {
  implicit val writes: OWrites[AmendPartnershipSubcontractorAuditEventModel] = OWrites { model =>
    val updatedJson                = Json.toJson(model.updatedDetails).as[JsObject]
    val (origDiffOpt, updatedDiff) = model.originalDetails match {
      case Some(orig) =>
        val (o, u) = diffDetails(Json.toJson(orig).as[JsObject], updatedJson)
        (Some(o), u)
      case None       => (None, updatedJson)
    }
    val base                       = Json.obj("typeOfSubcontractor" -> model.typeOfSubcontractor) ++
      origDiffOpt.fold(Json.obj())(o => Json.obj("originalDetails" -> o)) ++
      Json.obj("updatedDetails" -> updatedDiff)
    val withCisId                  = model.cisId.fold(base)(id => Json.obj("cisId" -> id) ++ base)
    model.subbieResourceRef.fold(withCisId)(ref => withCisId ++ Json.obj("subbieResourceRef" -> ref))
  }
}

case class TrustSubcontractorDetails(
  trustName: Option[String],
  trustAddressYesNo: Option[Boolean],
  trustAddress: Option[Address],
  addTrustContactMethodsYesNo: Option[Boolean],
  trustContactMethodOptions: Option[Seq[String]],
  trustEmailAddress: Option[String],
  trustPhoneNumber: Option[String],
  trustMobileNumber: Option[String],
  trustUtrYesNo: Option[Boolean],
  trustUtr: Option[String],
  trustWorksReferenceYesNo: Option[Boolean],
  trustWorksReference: Option[String]
)

object TrustSubcontractorDetails {
  implicit val writes: OWrites[TrustSubcontractorDetails] = (
    (__ \ "trustName").writeNullable[String] and
      (__ \ "trustAddressYesNo").writeNullable[Boolean] and
      (__ \ "trustAddress").writeNullable[Address] and
      (__ \ "addTrustContactMethodsYesNo").writeNullable[Boolean] and
      (__ \ "trustContactMethodOptions").writeNullable[Seq[String]] and
      (__ \ "trustEmailAddress").writeNullable[String] and
      (__ \ "trustPhoneNumber").writeNullable[String] and
      (__ \ "trustMobileNumber").writeNullable[String] and
      (__ \ "trustUtrYesNo").writeNullable[Boolean] and
      (__ \ "trustUtr").writeNullable[String] and
      (__ \ "trustWorksReferenceYesNo").writeNullable[Boolean] and
      (__ \ "trustWorksReference").writeNullable[String]
  )(Tuple.fromProductTyped(_))
}

case class AmendTrustSubcontractorAuditEventModel(
  cisId: Option[String],
  subbieResourceRef: Option[Int],
  typeOfSubcontractor: String,
  originalDetails: Option[TrustSubcontractorDetails],
  updatedDetails: TrustSubcontractorDetails
) extends AuditEvent {
  override val auditType: String = "amendSubcontractor"
}

object AmendTrustSubcontractorAuditEventModel {
  implicit val writes: OWrites[AmendTrustSubcontractorAuditEventModel] = OWrites { model =>
    val updatedJson                = Json.toJson(model.updatedDetails).as[JsObject]
    val (origDiffOpt, updatedDiff) = model.originalDetails match {
      case Some(orig) =>
        val (o, u) = diffDetails(Json.toJson(orig).as[JsObject], updatedJson)
        (Some(o), u)
      case None       => (None, updatedJson)
    }
    val base                       = Json.obj("typeOfSubcontractor" -> model.typeOfSubcontractor) ++
      origDiffOpt.fold(Json.obj())(o => Json.obj("originalDetails" -> o)) ++
      Json.obj("updatedDetails" -> updatedDiff)
    val withCisId                  = model.cisId.fold(base)(id => Json.obj("cisId" -> id) ++ base)
    model.subbieResourceRef.fold(withCisId)(ref => withCisId ++ Json.obj("subbieResourceRef" -> ref))
  }
}
