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

import com.google.inject.{Inject, Singleton}
import models.{JourneyNamespace, TypeOfSubcontractor, UserAnswers}
import models.add.IndividualNamesOptions
import models.audit.*
import models.contact.ContactMethodOptions
import pages.add.*
import pages.add.company.*
import pages.add.partnership.*
import pages.add.trust.*
import play.api.libs.json.{Json, OWrites, Writes}
import play.api.mvc.Request
import queries.*
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.AuditExtensions
import uk.gov.hmrc.play.audit.http.connector.*
import uk.gov.hmrc.play.audit.model.ExtendedDataEvent

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AuditService @Inject() (
  auditConnector: AuditConnector
)(implicit ec: ExecutionContext) {

  private val auditSource: String = "cis-contractor-frontend"

  def sendEvent[A <: AuditEventModel](
    auditEvent: A
  )(implicit hc: HeaderCarrier, writes: Writes[A], request: Request[?]): Future[AuditResult] = {
    val extendedDataEvent = ExtendedDataEvent(
      auditSource = auditSource,
      auditType = auditEvent.auditType,
      detail = Json.toJson(auditEvent),
      tags = AuditExtensions.auditHeaderCarrier(hc).toAuditTags()
    )

    auditConnector.sendExtendedEvent(extendedDataEvent)
  }

  def amendSubcontractorEvent(userAnswers: UserAnswers)(implicit hc: HeaderCarrier): Unit =
    userAnswers.get(TypeOfSubcontractorPage) match {
      case Some(TypeOfSubcontractor.Limitedcompany) => send(buildAmendCompanyModel(userAnswers))
      case Some(TypeOfSubcontractor.Partnership)    => send(buildAmendPartnershipModel(userAnswers))
      case Some(TypeOfSubcontractor.Trust)          => send(buildAmendTrustModel(userAnswers))
      case _                                        => send(buildAmendIndividualModel(userAnswers))
    }

  def addSubcontractorEvent(userAnswers: UserAnswers)(implicit hc: HeaderCarrier): Unit =
    userAnswers.get(TypeOfSubcontractorPage) match {
      case Some(TypeOfSubcontractor.Limitedcompany) => send(buildCompanyModel(userAnswers))
      case Some(TypeOfSubcontractor.Partnership)    => send(buildPartnershipModel(userAnswers))
      case Some(TypeOfSubcontractor.Trust)          => send(buildTrustModel(userAnswers))
      case _                                        => send(buildIndividualModel(userAnswers))
    }

  def addSubcontractorEvent(
    userAnswers: UserAnswers,
    id: String
  )(implicit hc: HeaderCarrier): Unit = {
    val namespace = JourneyNamespace.AddSubcontractor

    userAnswers.get(TypeOfSubcontractorPage(id, namespace)) match {
      case Some(TypeOfSubcontractor.Limitedcompany) => send(buildCompanyModel(userAnswers, id))
      case Some(TypeOfSubcontractor.Partnership) => send(buildPartnershipModel(userAnswers, id))
      case Some(TypeOfSubcontractor.Trust) => send(buildTrustModel(userAnswers, id))
      case Some(TypeOfSubcontractor.Individualorsoletrader) => send(buildIndividualModel(userAnswers, id))
      case None => throw new IllegalStateException("TypeOfSubcontractorPage not found in scoped session data")
    }
  }

  private def send[A <: AuditEvent](event: A)(implicit writes: OWrites[A], hc: HeaderCarrier): Unit =
    auditConnector.sendExplicitAudit(event.auditType, Json.toJson(event))

  private def buildIndividualModel(ua: UserAnswers): AddSubcontractorAuditEventModel = {
    val namesOpts   = ua.get(IndividualNamesOptionsPage)
    val contactOpts = ua.get(IndividualContactMethodOptionsPage)
    AddSubcontractorAuditEventModel(
      cisId = ua.get(CisIdQuery),
      typeOfSubcontractor = ua.get(TypeOfSubcontractorPage).fold("")(_.toString),
      subcontractorNameSelected = namesOpts.map(_.contains(IndividualNamesOptions.SubcontractorName)),
      tradingNameSelected = namesOpts.map(_.contains(IndividualNamesOptions.TradingName)),
      firstName = ua.get(SubcontractorNamePage).map(_.firstName),
      middleName = ua.get(SubcontractorNamePage).flatMap(_.middleName),
      surname = ua.get(SubcontractorNamePage).map(_.lastName),
      tradingNameOfSubcontractor = ua.get(TradingNameOfSubcontractorPage),
      subAddressYesNo = ua.get(SubAddressYesNoPage),
      addressOfSubcontractor = ua.get(AddressOfSubcontractorPage),
      addIndividualContactMethodsYesNo = ua.get(AddIndividualContactMethodsYesNoPage),
      individualEmailContactMethod = contactOpts.map(_.contains(ContactMethodOptions.Email)),
      individualPhoneContactMethod = contactOpts.map(_.contains(ContactMethodOptions.Phone)),
      individualMobileContactMethod = contactOpts.map(_.contains(ContactMethodOptions.Mobile)),
      individualEmailAddress = ua.get(IndividualEmailAddressPage),
      individualPhoneNumber = ua.get(IndividualPhoneNumberPage),
      individualMobileNumber = ua.get(IndividualMobileNumberPage),
      uniqueTaxpayerReferenceYesNo = ua.get(UniqueTaxpayerReferenceYesNoPage),
      subcontractorsUniqueTaxpayerReference = ua.get(SubcontractorsUniqueTaxpayerReferencePage),
      nationalInsuranceNumberYesNo = ua.get(NationalInsuranceNumberYesNoPage),
      subNationalInsuranceNumber = ua.get(SubNationalInsuranceNumberPage),
      worksReferenceNumberYesNo = ua.get(WorksReferenceNumberYesNoPage),
      worksReferenceNumber = ua.get(WorksReferenceNumberPage)
    )
  }

  private def buildCompanyModel(ua: UserAnswers): AddCompanySubcontractorAuditEventModel = {
    val contactOpts = ua.get(CompanyContactMethodOptionsPage)
    AddCompanySubcontractorAuditEventModel(
      cisId = ua.get(CisIdQuery),
      typeOfSubcontractor = ua.get(TypeOfSubcontractorPage).fold("")(_.toString),
      companyName = ua.get(CompanyNamePage),
      companyAddressYesNo = ua.get(CompanyAddressYesNoPage),
      companyAddress = ua.get(CompanyAddressPage),
      addCompanyContactMethodsYesNo = ua.get(AddCompanyContactMethodsYesNoPage),
      companyEmailContactMethod = contactOpts.map(_.contains(ContactMethodOptions.Email)),
      companyPhoneContactMethod = contactOpts.map(_.contains(ContactMethodOptions.Phone)),
      companyMobileContactMethod = contactOpts.map(_.contains(ContactMethodOptions.Mobile)),
      companyEmailAddress = ua.get(CompanyEmailAddressPage),
      companyPhoneNumber = ua.get(CompanyPhoneNumberPage),
      companyMobileNumber = ua.get(CompanyMobileNumberPage),
      companyUtrYesNo = ua.get(CompanyUtrYesNoPage),
      companyUtr = ua.get(CompanyUtrPage),
      companyCrnYesNo = ua.get(CompanyCrnYesNoPage),
      companyCrn = ua.get(CompanyCrnPage),
      companyWorksReferenceYesNo = ua.get(CompanyWorksReferenceYesNoPage),
      companyWorksReference = ua.get(CompanyWorksReferencePage)
    )
  }

  private def buildPartnershipModel(ua: UserAnswers): AddPartnershipSubcontractorAuditEventModel = {
    val contactOpts = ua.get(PartnershipContactMethodOptionsPage)

    AddPartnershipSubcontractorAuditEventModel(
      cisId = ua.get(CisIdQuery),
      typeOfSubcontractor = ua.get(TypeOfSubcontractorPage).fold("")(_.toString),
      partnershipName = ua.get(PartnershipNamePage),
      partnershipAddressYesNo = ua.get(PartnershipAddressYesNoPage),
      partnershipAddress = ua.get(PartnershipAddressPage),
      addPartnershipContactMethodsYesNo = ua.get(AddPartnershipContactMethodsYesNoPage),
      partnershipEmailContactMethod = contactOpts.map(_.contains(ContactMethodOptions.Email)),
      partnershipPhoneContactMethod = contactOpts.map(_.contains(ContactMethodOptions.Phone)),
      partnershipMobileContactMethod = contactOpts.map(_.contains(ContactMethodOptions.Mobile)),
      partnershipEmailAddress = ua.get(PartnershipEmailAddressPage),
      partnershipPhoneNumber = ua.get(PartnershipPhoneNumberPage),
      partnershipMobileNumber = ua.get(PartnershipMobileNumberPage),
      partnershipHasUtrYesNo = ua.get(PartnershipHasUtrYesNoPage),
      partnershipUniqueTaxpayerReference = ua.get(PartnershipUniqueTaxpayerReferencePage),
      partnershipNominatedPartnerName = ua.get(PartnershipNominatedPartnerNamePage),
      partnershipNominatedPartnerUtrYesNo = ua.get(PartnershipNominatedPartnerUtrYesNoPage),
      partnershipNominatedPartnerUtr = ua.get(PartnershipNominatedPartnerUtrPage),
      partnershipNominatedPartnerNinoYesNo = ua.get(PartnershipNominatedPartnerNinoYesNoPage),
      nominatedPartnerNationalInsuranceNumber = ua.get(PartnershipNominatedPartnerNinoPage),
      partnershipNominatedPartnerCrnYesNo = ua.get(PartnershipNominatedPartnerCrnYesNoPage),
      nominatedPartnerCompanyRegistrationNumber = ua.get(PartnershipNominatedPartnerCrnPage),
      partnershipWorksReferenceNumberYesNo = ua.get(PartnershipWorksReferenceNumberYesNoPage),
      partnershipWorksReference = ua.get(PartnershipWorksReferenceNumberPage)
    )
  }

  private def buildTrustModel(ua: UserAnswers): AddTrustSubcontractorAuditEventModel = {
    val contactOpts = ua.get(TrustContactMethodOptionsPage)
    AddTrustSubcontractorAuditEventModel(
      cisId = ua.get(CisIdQuery),
      typeOfSubcontractor = ua.get(TypeOfSubcontractorPage).fold("")(_.toString),
      trustName = ua.get(TrustNamePage),
      trustAddressYesNo = ua.get(TrustAddressYesNoPage),
      trustAddress = ua.get(TrustAddressPage),
      addTrustContactMethodsYesNo = ua.get(AddTrustContactMethodsYesNoPage),
      trustEmailContactMethod = contactOpts.map(_.contains(ContactMethodOptions.Email)),
      trustPhoneContactMethod = contactOpts.map(_.contains(ContactMethodOptions.Phone)),
      trustMobileContactMethod = contactOpts.map(_.contains(ContactMethodOptions.Mobile)),
      trustEmailAddress = ua.get(TrustEmailAddressPage),
      trustPhoneNumber = ua.get(TrustPhoneNumberPage),
      trustMobileNumber = ua.get(TrustMobileNumberPage),
      trustUtrYesNo = ua.get(TrustUtrYesNoPage),
      trustUtr = ua.get(TrustUtrPage),
      trustWorksReferenceYesNo = ua.get(TrustWorksReferenceYesNoPage),
      trustWorksReference = ua.get(TrustWorksReferencePage)
    )
  }

  // Multi-tab
  private def buildIndividualModel(
    ua: UserAnswers,
    id: String
  ): AddSubcontractorAuditEventModel = {
    val namespace = JourneyNamespace.AddSubcontractor
    val namesOpts = ua.get(IndividualNamesOptionsPage(id, namespace))
    val contactOpts = ua.get(IndividualContactMethodOptionsPage(id, namespace))

    AddSubcontractorAuditEventModel(
      cisId = ua.get(CisIdQuery(id, namespace)),
      typeOfSubcontractor = ua.get(TypeOfSubcontractorPage(id, namespace)).fold("")(_.toString),
      subcontractorNameSelected = namesOpts.map(_.contains(IndividualNamesOptions.SubcontractorName)),
      tradingNameSelected = namesOpts.map(_.contains(IndividualNamesOptions.TradingName)),
      firstName = ua.get(SubcontractorNamePage(id, namespace)).map(_.firstName),
      middleName = ua.get(SubcontractorNamePage(id, namespace)).flatMap(_.middleName),
      surname = ua.get(SubcontractorNamePage(id, namespace)).map(_.lastName),
      tradingNameOfSubcontractor = ua.get(TradingNameOfSubcontractorPage(id, namespace)),
      subAddressYesNo = ua.get(SubAddressYesNoPage(id, namespace)),
      addressOfSubcontractor = ua.get(AddressOfSubcontractorPage(id, namespace)),
      addIndividualContactMethodsYesNo = ua.get(AddIndividualContactMethodsYesNoPage(id, namespace)),
      individualEmailContactMethod = contactOpts.map(_.contains(ContactMethodOptions.Email)),
      individualPhoneContactMethod = contactOpts.map(_.contains(ContactMethodOptions.Phone)),
      individualMobileContactMethod = contactOpts.map(_.contains(ContactMethodOptions.Mobile)),
      individualEmailAddress = ua.get(IndividualEmailAddressPage(id, namespace)),
      individualPhoneNumber = ua.get(IndividualPhoneNumberPage(id, namespace)),
      individualMobileNumber = ua.get(IndividualMobileNumberPage(id, namespace)),
      uniqueTaxpayerReferenceYesNo = ua.get(UniqueTaxpayerReferenceYesNoPage(id, namespace)),
      subcontractorsUniqueTaxpayerReference = ua.get(SubcontractorsUniqueTaxpayerReferencePage(id, namespace)),
      nationalInsuranceNumberYesNo = ua.get(NationalInsuranceNumberYesNoPage(id, namespace)),
      subNationalInsuranceNumber = ua.get(SubNationalInsuranceNumberPage(id, namespace)),
      worksReferenceNumberYesNo = ua.get(WorksReferenceNumberYesNoPage(id, namespace)),
      worksReferenceNumber = ua.get(WorksReferenceNumberPage(id, namespace))
    )
  }

  private def buildCompanyModel(
    ua: UserAnswers,
    id: String
  ): AddCompanySubcontractorAuditEventModel = {
    val namespace = JourneyNamespace.AddSubcontractor
    val contactOpts = ua.get(CompanyContactMethodOptionsPage(id, namespace))

    AddCompanySubcontractorAuditEventModel(
      cisId = ua.get(CisIdQuery(id, namespace)),
      typeOfSubcontractor = ua.get(TypeOfSubcontractorPage(id, namespace)).fold("")(_.toString),
      companyName = ua.get(CompanyNamePage(id, namespace)),
      companyAddressYesNo = ua.get(CompanyAddressYesNoPage(id, namespace)),
      companyAddress = ua.get(CompanyAddressPage(id, namespace)),
      addCompanyContactMethodsYesNo = ua.get(AddCompanyContactMethodsYesNoPage(id, namespace)),
      companyEmailContactMethod = contactOpts.map(_.contains(ContactMethodOptions.Email)),
      companyPhoneContactMethod = contactOpts.map(_.contains(ContactMethodOptions.Phone)),
      companyMobileContactMethod = contactOpts.map(_.contains(ContactMethodOptions.Mobile)),
      companyEmailAddress = ua.get(CompanyEmailAddressPage(id, namespace)),
      companyPhoneNumber = ua.get(CompanyPhoneNumberPage(id, namespace)),
      companyMobileNumber = ua.get(CompanyMobileNumberPage(id, namespace)),
      companyUtrYesNo = ua.get(CompanyUtrYesNoPage(id, namespace)),
      companyUtr = ua.get(CompanyUtrPage(id, namespace)),
      companyCrnYesNo = ua.get(CompanyCrnYesNoPage(id, namespace)),
      companyCrn = ua.get(CompanyCrnPage(id, namespace)),
      companyWorksReferenceYesNo = ua.get(CompanyWorksReferenceYesNoPage(id, namespace)),
      companyWorksReference = ua.get(CompanyWorksReferencePage(id, namespace))
    )
  }

  private def buildPartnershipModel(
    ua: UserAnswers,
    id: String
  ): AddPartnershipSubcontractorAuditEventModel = {
    val namespace = JourneyNamespace.AddSubcontractor
    val contactOpts = ua.get(PartnershipContactMethodOptionsPage(id, namespace))

    AddPartnershipSubcontractorAuditEventModel(
      cisId = ua.get(CisIdQuery(id, namespace)),
      typeOfSubcontractor = ua.get(TypeOfSubcontractorPage(id, namespace)).fold("")(_.toString),
      partnershipName = ua.get(PartnershipNamePage(id, namespace)),
      partnershipAddressYesNo = ua.get(PartnershipAddressYesNoPage(id, namespace)),
      partnershipAddress = ua.get(PartnershipAddressPage(id, namespace)),
      addPartnershipContactMethodsYesNo = ua.get(AddPartnershipContactMethodsYesNoPage(id, namespace)),
      partnershipEmailContactMethod = contactOpts.map(_.contains(ContactMethodOptions.Email)),
      partnershipPhoneContactMethod = contactOpts.map(_.contains(ContactMethodOptions.Phone)),
      partnershipMobileContactMethod = contactOpts.map(_.contains(ContactMethodOptions.Mobile)),
      partnershipEmailAddress = ua.get(PartnershipEmailAddressPage(id, namespace)),
      partnershipPhoneNumber = ua.get(PartnershipPhoneNumberPage(id, namespace)),
      partnershipMobileNumber = ua.get(PartnershipMobileNumberPage(id, namespace)),
      partnershipHasUtrYesNo = ua.get(PartnershipHasUtrYesNoPage(id, namespace)),
      partnershipUniqueTaxpayerReference = ua.get(PartnershipUniqueTaxpayerReferencePage(id, namespace)),
      partnershipNominatedPartnerName = ua.get(PartnershipNominatedPartnerNamePage(id, namespace)),
      partnershipNominatedPartnerUtrYesNo = ua.get(PartnershipNominatedPartnerUtrYesNoPage(id, namespace)),
      partnershipNominatedPartnerUtr = ua.get(PartnershipNominatedPartnerUtrPage(id, namespace)),
      partnershipNominatedPartnerNinoYesNo = ua.get(PartnershipNominatedPartnerNinoYesNoPage(id, namespace)),
      nominatedPartnerNationalInsuranceNumber = ua.get(PartnershipNominatedPartnerNinoPage(id, namespace)),
      partnershipNominatedPartnerCrnYesNo = ua.get(PartnershipNominatedPartnerCrnYesNoPage(id, namespace)),
      nominatedPartnerCompanyRegistrationNumber = ua.get(PartnershipNominatedPartnerCrnPage(id, namespace)),
      partnershipWorksReferenceNumberYesNo = ua.get(PartnershipWorksReferenceNumberYesNoPage(id, namespace)),
      partnershipWorksReference = ua.get(PartnershipWorksReferenceNumberPage(id, namespace))
    )
  }

  private def buildTrustModel(
    ua: UserAnswers,
    id: String
  ): AddTrustSubcontractorAuditEventModel = {
    val namespace = JourneyNamespace.AddSubcontractor
    val contactOpts = ua.get(TrustContactMethodOptionsPage(id, namespace))

    AddTrustSubcontractorAuditEventModel(
      cisId = ua.get(CisIdQuery(id, namespace)),
      typeOfSubcontractor = ua.get(TypeOfSubcontractorPage(id, namespace)).fold("")(_.toString),
      trustName = ua.get(TrustNamePage(id, namespace)),
      trustAddressYesNo = ua.get(TrustAddressYesNoPage(id, namespace)),
      trustAddress = ua.get(TrustAddressPage(id, namespace)),
      addTrustContactMethodsYesNo = ua.get(AddTrustContactMethodsYesNoPage(id, namespace)),
      trustEmailContactMethod = contactOpts.map(_.contains(ContactMethodOptions.Email)),
      trustPhoneContactMethod = contactOpts.map(_.contains(ContactMethodOptions.Phone)),
      trustMobileContactMethod = contactOpts.map(_.contains(ContactMethodOptions.Mobile)),
      trustEmailAddress = ua.get(TrustEmailAddressPage(id, namespace)),
      trustPhoneNumber = ua.get(TrustPhoneNumberPage(id, namespace)),
      trustMobileNumber = ua.get(TrustMobileNumberPage(id, namespace)),
      trustUtrYesNo = ua.get(TrustUtrYesNoPage(id, namespace)),
      trustUtr = ua.get(TrustUtrPage(id, namespace)),
      trustWorksReferenceYesNo = ua.get(TrustWorksReferenceYesNoPage(id, namespace)),
      trustWorksReference = ua.get(TrustWorksReferencePage(id, namespace))
    )
  }

  private def buildAmendIndividualModel(ua: UserAnswers): AmendSubcontractorAuditEventModel = {
    val namesOpts   = ua.get(IndividualNamesOptionsPage)
    val contactOpts = ua.get(IndividualContactMethodOptionsPage)
    AmendSubcontractorAuditEventModel(
      cisId = ua.get(CisIdQuery),
      subcontractorResourceRef = ua.get(AmendSubbieResourceRefQuery),
      typeOfSubcontractor = ua.get(TypeOfSubcontractorPage).fold("")(_.toString),
      updatedDetails = IndividualSubcontractorDetails(
        subcontractorNameSelected = namesOpts.map(_.contains(IndividualNamesOptions.SubcontractorName)),
        tradingNameSelected = namesOpts.map(_.contains(IndividualNamesOptions.TradingName)),
        firstName = ua.get(SubcontractorNamePage).map(_.firstName),
        middleName = ua.get(SubcontractorNamePage).flatMap(_.middleName),
        surname = ua.get(SubcontractorNamePage).map(_.lastName),
        tradingNameOfSubcontractor = ua.get(TradingNameOfSubcontractorPage),
        subAddressYesNo = ua.get(SubAddressYesNoPage),
        addressOfSubcontractor = ua.get(AddressOfSubcontractorPage),
        addIndividualContactMethodsYesNo = ua.get(AddIndividualContactMethodsYesNoPage),
        individualEmailContactMethod = contactOpts.map(_.contains(ContactMethodOptions.Email)),
        individualPhoneContactMethod = contactOpts.map(_.contains(ContactMethodOptions.Phone)),
        individualMobileContactMethod = contactOpts.map(_.contains(ContactMethodOptions.Mobile)),
        individualEmailAddress = ua.get(IndividualEmailAddressPage),
        individualPhoneNumber = ua.get(IndividualPhoneNumberPage),
        individualMobileNumber = ua.get(IndividualMobileNumberPage),
        uniqueTaxpayerReferenceYesNo = ua.get(UniqueTaxpayerReferenceYesNoPage),
        subcontractorsUniqueTaxpayerReference = ua.get(SubcontractorsUniqueTaxpayerReferencePage),
        nationalInsuranceNumberYesNo = ua.get(NationalInsuranceNumberYesNoPage),
        subNationalInsuranceNumber = ua.get(SubNationalInsuranceNumberPage),
        worksReferenceNumberYesNo = ua.get(WorksReferenceNumberYesNoPage),
        worksReferenceNumber = ua.get(WorksReferenceNumberPage)
      )
    )
  }

  private def buildAmendCompanyModel(ua: UserAnswers): AmendCompanySubcontractorAuditEventModel = {
    val contactOpts = ua.get(CompanyContactMethodOptionsPage)
    AmendCompanySubcontractorAuditEventModel(
      cisId = ua.get(CisIdQuery),
      subcontractorResourceRef = ua.get(AmendSubbieResourceRefQuery),
      typeOfSubcontractor = ua.get(TypeOfSubcontractorPage).fold("")(_.toString),
      updatedDetails = CompanySubcontractorDetails(
        companyName = ua.get(CompanyNamePage),
        companyAddressYesNo = ua.get(CompanyAddressYesNoPage),
        companyAddress = ua.get(CompanyAddressPage),
        addCompanyContactMethodsYesNo = ua.get(AddCompanyContactMethodsYesNoPage),
        companyEmailContactMethod = contactOpts.map(_.contains(ContactMethodOptions.Email)),
        companyPhoneContactMethod = contactOpts.map(_.contains(ContactMethodOptions.Phone)),
        companyMobileContactMethod = contactOpts.map(_.contains(ContactMethodOptions.Mobile)),
        companyEmailAddress = ua.get(CompanyEmailAddressPage),
        companyPhoneNumber = ua.get(CompanyPhoneNumberPage),
        companyMobileNumber = ua.get(CompanyMobileNumberPage),
        companyUtrYesNo = ua.get(CompanyUtrYesNoPage),
        companyUtr = ua.get(CompanyUtrPage),
        companyCrnYesNo = ua.get(CompanyCrnYesNoPage),
        companyCrn = ua.get(CompanyCrnPage),
        companyWorksReferenceYesNo = ua.get(CompanyWorksReferenceYesNoPage),
        companyWorksReference = ua.get(CompanyWorksReferencePage)
      )
    )
  }

  private def buildAmendPartnershipModel(ua: UserAnswers): AmendPartnershipSubcontractorAuditEventModel = {
    val contactOpts = ua.get(PartnershipContactMethodOptionsPage)
    AmendPartnershipSubcontractorAuditEventModel(
      cisId = ua.get(CisIdQuery),
      subcontractorResourceRef = ua.get(AmendSubbieResourceRefQuery),
      typeOfSubcontractor = ua.get(TypeOfSubcontractorPage).fold("")(_.toString),
      updatedDetails = PartnershipSubcontractorDetails(
        partnershipName = ua.get(PartnershipNamePage),
        partnershipAddressYesNo = ua.get(PartnershipAddressYesNoPage),
        partnershipAddress = ua.get(PartnershipAddressPage),
        addPartnershipContactMethodsYesNo = ua.get(AddPartnershipContactMethodsYesNoPage),
        partnershipEmailContactMethod = contactOpts.map(_.contains(ContactMethodOptions.Email)),
        partnershipPhoneContactMethod = contactOpts.map(_.contains(ContactMethodOptions.Phone)),
        partnershipMobileContactMethod = contactOpts.map(_.contains(ContactMethodOptions.Mobile)),
        partnershipEmailAddress = ua.get(PartnershipEmailAddressPage),
        partnershipPhoneNumber = ua.get(PartnershipPhoneNumberPage),
        partnershipMobileNumber = ua.get(PartnershipMobileNumberPage),
        partnershipHasUtrYesNo = ua.get(PartnershipHasUtrYesNoPage),
        partnershipUniqueTaxpayerReference = ua.get(PartnershipUniqueTaxpayerReferencePage),
        partnershipNominatedPartnerName = ua.get(PartnershipNominatedPartnerNamePage),
        partnershipNominatedPartnerUtrYesNo = ua.get(PartnershipNominatedPartnerUtrYesNoPage),
        partnershipNominatedPartnerUtr = ua.get(PartnershipNominatedPartnerUtrPage),
        partnershipNominatedPartnerNinoYesNo = ua.get(PartnershipNominatedPartnerNinoYesNoPage),
        nominatedPartnerNationalInsuranceNumber = ua.get(PartnershipNominatedPartnerNinoPage),
        partnershipNominatedPartnerCrnYesNo = ua.get(PartnershipNominatedPartnerCrnYesNoPage),
        nominatedPartnerCompanyRegistrationNumber = ua.get(PartnershipNominatedPartnerCrnPage),
        partnershipWorksReferenceNumberYesNo = ua.get(PartnershipWorksReferenceNumberYesNoPage),
        partnershipWorksReference = ua.get(PartnershipWorksReferenceNumberPage)
      )
    )
  }

  private def buildAmendTrustModel(ua: UserAnswers): AmendTrustSubcontractorAuditEventModel = {
    val contactOpts = ua.get(TrustContactMethodOptionsPage)
    AmendTrustSubcontractorAuditEventModel(
      cisId = ua.get(CisIdQuery),
      subcontractorResourceRef = ua.get(AmendSubbieResourceRefQuery),
      typeOfSubcontractor = ua.get(TypeOfSubcontractorPage).fold("")(_.toString),
      updatedDetails = TrustSubcontractorDetails(
        trustName = ua.get(TrustNamePage),
        trustAddressYesNo = ua.get(TrustAddressYesNoPage),
        trustAddress = ua.get(TrustAddressPage),
        addTrustContactMethodsYesNo = ua.get(AddTrustContactMethodsYesNoPage),
        trustEmailContactMethod = contactOpts.map(_.contains(ContactMethodOptions.Email)),
        trustPhoneContactMethod = contactOpts.map(_.contains(ContactMethodOptions.Phone)),
        trustMobileContactMethod = contactOpts.map(_.contains(ContactMethodOptions.Mobile)),
        trustEmailAddress = ua.get(TrustEmailAddressPage),
        trustPhoneNumber = ua.get(TrustPhoneNumberPage),
        trustMobileNumber = ua.get(TrustMobileNumberPage),
        trustUtrYesNo = ua.get(TrustUtrYesNoPage),
        trustUtr = ua.get(TrustUtrPage),
        trustWorksReferenceYesNo = ua.get(TrustWorksReferenceYesNoPage),
        trustWorksReference = ua.get(TrustWorksReferencePage)
      )
    )
  }

}
