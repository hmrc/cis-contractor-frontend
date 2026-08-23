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
import models.TypeOfSubcontractor
import models.UserAnswers
import models.audit.*
import models.contact.ContactMethodOptions
import pages.add.*
import pages.add.company.*
import pages.add.partnership.*
import pages.add.trust.*
import play.api.libs.json.{Json, OWrites}
import queries.CisIdQuery
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditConnector

import scala.concurrent.ExecutionContext

@Singleton
class AuditService @Inject() (
  auditConnector: AuditConnector
)(implicit ec: ExecutionContext) {

  def addSubcontractorEvent(userAnswers: UserAnswers)(implicit hc: HeaderCarrier): Unit =
    userAnswers.get(TypeOfSubcontractorPage) match {
      case Some(TypeOfSubcontractor.Limitedcompany) => send(buildCompanyModel(userAnswers))
      case Some(TypeOfSubcontractor.Partnership)    => send(buildPartnershipModel(userAnswers))
      case Some(TypeOfSubcontractor.Trust)          => send(buildTrustModel(userAnswers))
      case _                                        => send(buildIndividualModel(userAnswers))
    }

  private def send[A <: AuditEvent](event: A)(implicit writes: OWrites[A], hc: HeaderCarrier): Unit =
    auditConnector.sendExplicitAudit(event.auditType, Json.toJson(event))

  private def buildIndividualModel(ua: UserAnswers): AddSubcontractorAuditEventModel =
    AddSubcontractorAuditEventModel(
      cisId = ua.get(CisIdQuery),
      typeOfSubcontractor = ua.get(TypeOfSubcontractorPage).fold("")(_.toString),
      subTradingNameYesNo = ua.get(SubTradingNameYesNoPage),
      tradingNameOfSubcontractor = ua.get(TradingNameOfSubcontractorPage),
      subAddressYesNo = ua.get(SubAddressYesNoPage),
      addressOfSubcontractor = ua.get(AddressOfSubcontractorPage),
      addIndividualContactMethodsYesNo = ua.get(AddIndividualContactMethodsYesNoPage),
      individualContactMethodOptions = ua
        .get(IndividualContactMethodOptionsPage)
        .map(opts => ContactMethodOptions.ordered(opts).map(_.toString)),
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

  private def buildCompanyModel(ua: UserAnswers): AddCompanySubcontractorAuditEventModel =
    AddCompanySubcontractorAuditEventModel(
      cisId = ua.get(CisIdQuery),
      typeOfSubcontractor = ua.get(TypeOfSubcontractorPage).fold("")(_.toString),
      companyName = ua.get(CompanyNamePage),
      companyAddressYesNo = ua.get(CompanyAddressYesNoPage),
      companyAddress = ua.get(CompanyAddressPage),
      addCompanyContactMethodsYesNo = ua.get(AddCompanyContactMethodsYesNoPage),
      companyContactMethodOptions = ua
        .get(CompanyContactMethodOptionsPage)
        .map(opts => ContactMethodOptions.ordered(opts).map(_.toString)),
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

  private def buildPartnershipModel(ua: UserAnswers): AddPartnershipSubcontractorAuditEventModel =
    AddPartnershipSubcontractorAuditEventModel(
      cisId = ua.get(CisIdQuery),
      typeOfSubcontractor = ua.get(TypeOfSubcontractorPage).fold("")(_.toString),
      partnershipName = ua.get(PartnershipNamePage),
      partnershipAddressYesNo = ua.get(PartnershipAddressYesNoPage),
      partnershipAddress = ua.get(PartnershipAddressPage),
      addPartnershipContactMethodsYesNo = ua.get(AddPartnershipContactMethodsYesNoPage),
      partnershipContactMethodOptions = ua
        .get(PartnershipContactMethodOptionsPage)
        .map(opts => ContactMethodOptions.ordered(opts).map(_.toString)),
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

  private def buildTrustModel(ua: UserAnswers): AddTrustSubcontractorAuditEventModel =
    AddTrustSubcontractorAuditEventModel(
      cisId = ua.get(CisIdQuery),
      typeOfSubcontractor = ua.get(TypeOfSubcontractorPage).fold("")(_.toString),
      trustName = ua.get(TrustNamePage),
      trustAddressYesNo = ua.get(TrustAddressYesNoPage),
      trustAddress = ua.get(TrustAddressPage),
      addTrustContactMethodsYesNo = ua.get(AddTrustContactMethodsYesNoPage),
      trustContactMethodOptions = ua
        .get(TrustContactMethodOptionsPage)
        .map(opts => ContactMethodOptions.ordered(opts).map(_.toString)),
      trustEmailAddress = ua.get(TrustEmailAddressPage),
      trustPhoneNumber = ua.get(TrustPhoneNumberPage),
      trustMobileNumber = ua.get(TrustMobileNumberPage),
      trustUtrYesNo = ua.get(TrustUtrYesNoPage),
      trustUtr = ua.get(TrustUtrPage),
      trustWorksReferenceYesNo = ua.get(TrustWorksReferenceYesNoPage),
      trustWorksReference = ua.get(TrustWorksReferencePage)
    )
}
