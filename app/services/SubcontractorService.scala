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

import connectors.ConstructionIndustrySchemeConnector
import models.UserAnswers
import models.add.TypeOfSubcontractor.{Individualorsoletrader, Limitedcompany, Partnership, Trust}
import models.add.TypeOfSubcontractor
import models.contact.ContactOptions
import models.requests.CreateAndUpdateSubcontractorPayload.{CompanyPayload, IndividualOrSoleTraderPayload, PartnershipPayload, TrustPayload}
import pages.add.*
import pages.add.partnership.*
import pages.add.company.*
import pages.add.trust.*
import play.api.Logging
import queries.CisIdQuery
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SubcontractorService @Inject() (
  cisConnector: ConstructionIndustrySchemeConnector
)(implicit ec: ExecutionContext)
    extends Logging {

  def createAndUpdateSubcontractor(userAnswers: UserAnswers)(implicit hc: HeaderCarrier): Future[Unit] =
    for {
      cisId             <- getCisId(userAnswers)
      subcontractorType <- getSubcontractorType(userAnswers)

      payload = {
        subcontractorType match {
          case Individualorsoletrader =>
            individualOrSoleTraderPayloadFromUserAnswers(cisId, subcontractorType, userAnswers)

          case Partnership =>
            partnershipPayloadFromUserAnswers(cisId, subcontractorType, userAnswers)

          case Limitedcompany =>
            companyPayloadFromUserAnswers(cisId, subcontractorType, userAnswers)

          case Trust =>
            trustPayloadFromUserAnswers(cisId, subcontractorType, userAnswers)
        }
      }

      _ <- cisConnector.createAndUpdateSubcontractor(payload)
    } yield ()

  def isDuplicateUTR(userAnswers: UserAnswers, utr: String)(implicit hc: HeaderCarrier): Future[Boolean] =
    for {
      cisId   <- getCisId(userAnswers)
      utrList <- cisConnector.getSubcontractorUTRs(cisId.toString)
    } yield utrList.subcontractorUTRs.contains(utr)

  private def getCisId(userAnswers: UserAnswers): Future[String] =
    userAnswers.get(CisIdQuery) match {
      case Some(cisId) => Future.successful(cisId)
      case None        => Future.failed(new RuntimeException("CisIdQuery not found in session data"))
    }

  private def getSubcontractorType(userAnswers: UserAnswers): Future[TypeOfSubcontractor] =
    userAnswers.get(TypeOfSubcontractorPage) match {
      case Some(t) => Future.successful(t)
      case None    => Future.failed(new RuntimeException("TypeOfSubcontractorPage not found in session data"))
    }

  private case class ContactDetails(email: Option[String], phone: Option[String], mobile: Option[String])

  private def partnershipContactDetailsFromUserAnswers(userAnswers: UserAnswers): ContactDetails =
    userAnswers.get(PartnershipChooseContactDetailsPage) match {
      case Some(ContactOptions.Email)     => ContactDetails(userAnswers.get(PartnershipEmailAddressPage), None, None)
      case Some(ContactOptions.Phone)     => ContactDetails(None, userAnswers.get(PartnershipPhoneNumberPage), None)
      case Some(ContactOptions.Mobile)    => ContactDetails(None, None, userAnswers.get(PartnershipMobileNumberPage))
      case Some(ContactOptions.NoDetails) => ContactDetails(None, None, None)
      case _                              => ContactDetails(None, None, None)
    }

  private def partnershipPayloadFromUserAnswers(
    cisId: String,
    subcontractorType: TypeOfSubcontractor,
    userAnswers: UserAnswers
  ): PartnershipPayload = {

    val contactDetails = partnershipContactDetailsFromUserAnswers(userAnswers)

    PartnershipPayload(
      cisId = cisId,
      subcontractorType = subcontractorType,
      utr = userAnswers.get(PartnershipUniqueTaxpayerReferencePage),
      partnerUtr = userAnswers.get(PartnershipNominatedPartnerUtrPage),
      partnershipTradingName = userAnswers.get(PartnershipNamePage),
      partnerTradingName = userAnswers.get(PartnershipNominatedPartnerNamePage),
      partnerNino = userAnswers.get(PartnershipNominatedPartnerNinoPage),
      partnerCrn = userAnswers.get(PartnershipNominatedPartnerCrnPage),
      addressLine1 = userAnswers.get(PartnershipAddressPage).map(_.addressLine1),
      addressLine2 = userAnswers.get(PartnershipAddressPage).flatMap(_.addressLine2),
      city = userAnswers.get(PartnershipAddressPage).map(_.addressLine3),
      county = userAnswers.get(PartnershipAddressPage).flatMap(_.addressLine4),
      postcode = userAnswers.get(PartnershipAddressPage).map(_.postalCode),
      country = userAnswers.get(PartnershipAddressPage).map(_.country),
      emailAddress = contactDetails.email,
      phoneNumber = contactDetails.phone,
      mobilePhoneNumber = contactDetails.mobile,
      worksReferenceNumber = userAnswers.get(PartnershipWorksReferenceNumberPage)
    )
  }

  private def individualContactDetailsFromUserAnswers(userAnswers: UserAnswers): ContactDetails =
    userAnswers.get(IndividualChooseContactDetailsPage) match {
      case Some(ContactOptions.Email)     => ContactDetails(userAnswers.get(IndividualEmailAddressPage), None, None)
      case Some(ContactOptions.Phone)     => ContactDetails(None, userAnswers.get(IndividualPhoneNumberPage), None)
      case Some(ContactOptions.Mobile)    => ContactDetails(None, None, userAnswers.get(IndividualMobileNumberPage))
      case Some(ContactOptions.NoDetails) => ContactDetails(None, None, None)
      case _                              => ContactDetails(None, None, None)
    }

  private def individualOrSoleTraderPayloadFromUserAnswers(
    cisId: String,
    subcontractorType: TypeOfSubcontractor,
    userAnswers: UserAnswers
  ): IndividualOrSoleTraderPayload = {

    val contactDetails = individualContactDetailsFromUserAnswers(userAnswers)

    IndividualOrSoleTraderPayload(
      cisId = cisId,
      subcontractorType = subcontractorType,
      firstName = userAnswers.get(SubcontractorNamePage).map(_.firstName),
      secondName = userAnswers.get(SubcontractorNamePage).flatMap(_.middleName),
      surname = userAnswers.get(SubcontractorNamePage).map(_.lastName),
      tradingName = userAnswers.get(TradingNameOfSubcontractorPage),
      addressLine1 = userAnswers.get(AddressOfSubcontractorPage).map(_.addressLine1),
      addressLine2 = userAnswers.get(AddressOfSubcontractorPage).flatMap(_.addressLine2),
      city = userAnswers.get(AddressOfSubcontractorPage).map(_.addressLine3),
      county = userAnswers.get(AddressOfSubcontractorPage).flatMap(_.addressLine4),
      postcode = userAnswers.get(AddressOfSubcontractorPage).map(_.postalCode),
      country = userAnswers.get(AddressOfSubcontractorPage).map(_.country),
      emailAddress = contactDetails.email,
      phoneNumber = contactDetails.phone,
      mobilePhoneNumber = contactDetails.mobile,
      nino = userAnswers.get(SubNationalInsuranceNumberPage),
      utr = userAnswers.get(SubcontractorsUniqueTaxpayerReferencePage),
      worksReferenceNumber = userAnswers.get(WorksReferenceNumberPage)
    )
  }

  private def companyContactDetailsFromUserAnswers(userAnswers: UserAnswers): ContactDetails =
    userAnswers.get(CompanyContactOptionsPage) match {
      case Some(ContactOptions.Email)     => ContactDetails(userAnswers.get(CompanyEmailAddressPage), None, None)
      case Some(ContactOptions.Phone)     => ContactDetails(None, userAnswers.get(CompanyPhoneNumberPage), None)
      case Some(ContactOptions.Mobile)    => ContactDetails(None, None, userAnswers.get(CompanyMobileNumberPage))
      case Some(ContactOptions.NoDetails) => ContactDetails(None, None, None)
      case _                              => ContactDetails(None, None, None)
    }

  private def companyPayloadFromUserAnswers(
    cisId: String,
    subcontractorType: TypeOfSubcontractor,
    userAnswers: UserAnswers
  ): CompanyPayload = {
    val contactDetails = companyContactDetailsFromUserAnswers(userAnswers)

    CompanyPayload(
      cisId = cisId,
      subcontractorType = subcontractorType,
      utr = userAnswers.get(CompanyUtrPage),
      crn = userAnswers.get(CompanyCrnPage),
      tradingName = userAnswers.get(CompanyNamePage),
      addressLine1 = userAnswers.get(CompanyAddressPage).map(_.addressLine1),
      addressLine2 = userAnswers.get(CompanyAddressPage).flatMap(_.addressLine2),
      city = userAnswers.get(CompanyAddressPage).map(_.addressLine3),
      county = userAnswers.get(CompanyAddressPage).flatMap(_.addressLine4),
      postcode = userAnswers.get(CompanyAddressPage).map(_.postalCode),
      country = userAnswers.get(CompanyAddressPage).map(_.country),
      emailAddress = contactDetails.email,
      phoneNumber = contactDetails.phone,
      mobilePhoneNumber = contactDetails.mobile,
      worksReferenceNumber = userAnswers.get(CompanyWorksReferencePage)
    )
  }

  private def trustContactDetailsFromUserAnswers(userAnswers: UserAnswers): ContactDetails =
    userAnswers.get(TrustContactOptionsPage) match {
      case Some(ContactOptions.Email)     => ContactDetails(userAnswers.get(TrustEmailAddressPage), None, None)
      case Some(ContactOptions.Phone)     => ContactDetails(None, userAnswers.get(TrustPhoneNumberPage), None)
      case Some(ContactOptions.Mobile)    => ContactDetails(None, None, userAnswers.get(TrustMobileNumberPage))
      case Some(ContactOptions.NoDetails) => ContactDetails(None, None, None)
      case _                              => ContactDetails(None, None, None)
    }

  private def trustPayloadFromUserAnswers(
    cisId: String,
    subcontractorType: TypeOfSubcontractor,
    userAnswers: UserAnswers
  ): TrustPayload = {
    val contactDetails = trustContactDetailsFromUserAnswers(userAnswers)

    TrustPayload(
      cisId = cisId,
      subcontractorType = subcontractorType,
      trustTradingName = userAnswers.get(TrustNamePage),
      utr = userAnswers.get(TrustUtrPage),
      addressLine1 = userAnswers.get(TrustAddressPage).map(_.addressLine1),
      addressLine2 = userAnswers.get(TrustAddressPage).flatMap(_.addressLine2),
      city = userAnswers.get(TrustAddressPage).map(_.addressLine3),
      county = userAnswers.get(TrustAddressPage).flatMap(_.addressLine4),
      postcode = userAnswers.get(TrustAddressPage).map(_.postalCode),
      country = userAnswers.get(TrustAddressPage).map(_.country),
      emailAddress = contactDetails.email,
      phoneNumber = contactDetails.phone,
      mobilePhoneNumber = contactDetails.mobile,
      worksReferenceNumber = userAnswers.get(TrustWorksReferencePage)
    )
  }
}
