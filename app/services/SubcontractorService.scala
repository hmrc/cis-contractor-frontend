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
import models.{TypeOfSubcontractor, UserAnswers}
import models.TypeOfSubcontractor.{Individualorsoletrader, Limitedcompany, Partnership, Trust}
import models.requests.CreateAndUpdateSubcontractorPayload.{CompanyPayload, IndividualOrSoleTraderPayload, PartnershipPayload, TrustPayload}
import models.response.*
import pages.add.*
import pages.add.partnership.*
import pages.add.company.*
import pages.add.trust.*
import play.api.Logging
import uk.gov.hmrc.http.HeaderCarrier
import models.requests.{SubcontractorRequest, UpdateSubcontractorRequest}
import queries.{AmendSubbieResourceRefQuery, CisIdQuery, OriginalSubcontractorQuery}

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

  private def partnershipPayloadFromUserAnswers(
    cisId: String,
    subcontractorType: TypeOfSubcontractor,
    userAnswers: UserAnswers
  ): PartnershipPayload =
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
      city = userAnswers.get(PartnershipAddressPage).flatMap(_.addressLine3),
      county = userAnswers.get(PartnershipAddressPage).flatMap(_.addressLine4),
      postcode = userAnswers.get(PartnershipAddressPage).flatMap(_.postcode),
      country = userAnswers.get(PartnershipAddressPage).flatMap(_.country).flatMap(_.name),
      emailAddress = userAnswers.get(PartnershipEmailAddressPage),
      phoneNumber = userAnswers.get(PartnershipPhoneNumberPage),
      mobilePhoneNumber = userAnswers.get(PartnershipMobileNumberPage),
      worksReferenceNumber = userAnswers.get(PartnershipWorksReferenceNumberPage)
    )

  private def individualOrSoleTraderPayloadFromUserAnswers(
    cisId: String,
    subcontractorType: TypeOfSubcontractor,
    userAnswers: UserAnswers
  ): IndividualOrSoleTraderPayload =
    IndividualOrSoleTraderPayload(
      cisId = cisId,
      subcontractorType = subcontractorType,
      firstName = userAnswers.get(SubcontractorNamePage).map(_.firstName),
      secondName = userAnswers.get(SubcontractorNamePage).flatMap(_.middleName),
      surname = userAnswers.get(SubcontractorNamePage).map(_.lastName),
      tradingName = userAnswers.get(TradingNameOfSubcontractorPage),
      addressLine1 = userAnswers.get(AddressOfSubcontractorPage).map(_.addressLine1),
      addressLine2 = userAnswers.get(AddressOfSubcontractorPage).flatMap(_.addressLine2),
      city = userAnswers.get(AddressOfSubcontractorPage).flatMap(_.addressLine3),
      county = userAnswers.get(AddressOfSubcontractorPage).flatMap(_.addressLine4),
      postcode = userAnswers.get(AddressOfSubcontractorPage).flatMap(_.postcode),
      country = userAnswers.get(AddressOfSubcontractorPage).flatMap(_.country).flatMap(_.name),
      nino = userAnswers.get(SubNationalInsuranceNumberPage),
      utr = userAnswers.get(SubcontractorsUniqueTaxpayerReferencePage),
      worksReferenceNumber = userAnswers.get(WorksReferenceNumberPage),
      emailAddress = userAnswers.get(IndividualEmailAddressPage),
      phoneNumber = userAnswers.get(IndividualPhoneNumberPage),
      mobilePhoneNumber = userAnswers.get(IndividualMobileNumberPage)
    )

  private def companyPayloadFromUserAnswers(
    cisId: String,
    subcontractorType: TypeOfSubcontractor,
    userAnswers: UserAnswers
  ): CompanyPayload =
    CompanyPayload(
      cisId = cisId,
      subcontractorType = subcontractorType,
      utr = userAnswers.get(CompanyUtrPage),
      crn = userAnswers.get(CompanyCrnPage),
      tradingName = userAnswers.get(CompanyNamePage),
      addressLine1 = userAnswers.get(CompanyAddressPage).map(_.addressLine1),
      addressLine2 = userAnswers.get(CompanyAddressPage).flatMap(_.addressLine2),
      city = userAnswers.get(CompanyAddressPage).flatMap(_.addressLine3),
      county = userAnswers.get(CompanyAddressPage).flatMap(_.addressLine4),
      postcode = userAnswers.get(CompanyAddressPage).flatMap(_.postcode),
      country = userAnswers.get(CompanyAddressPage).flatMap(_.country).flatMap(_.name),
      emailAddress = userAnswers.get(CompanyEmailAddressPage),
      phoneNumber = userAnswers.get(CompanyPhoneNumberPage),
      mobilePhoneNumber = userAnswers.get(CompanyMobileNumberPage),
      worksReferenceNumber = userAnswers.get(CompanyWorksReferencePage)
    )

  private def trustPayloadFromUserAnswers(
    cisId: String,
    subcontractorType: TypeOfSubcontractor,
    userAnswers: UserAnswers
  ): TrustPayload =
    TrustPayload(
      cisId = cisId,
      subcontractorType = subcontractorType,
      trustTradingName = userAnswers.get(TrustNamePage),
      utr = userAnswers.get(TrustUtrPage),
      addressLine1 = userAnswers.get(TrustAddressPage).map(_.addressLine1),
      addressLine2 = userAnswers.get(TrustAddressPage).flatMap(_.addressLine2),
      city = userAnswers.get(TrustAddressPage).flatMap(_.addressLine3),
      county = userAnswers.get(TrustAddressPage).flatMap(_.addressLine4),
      postcode = userAnswers.get(TrustAddressPage).flatMap(_.postcode),
      country = userAnswers.get(TrustAddressPage).flatMap(_.country).flatMap(_.name),
      emailAddress = userAnswers.get(TrustEmailAddressPage),
      phoneNumber = userAnswers.get(TrustPhoneNumberPage),
      mobilePhoneNumber = userAnswers.get(TrustMobileNumberPage),
      worksReferenceNumber = userAnswers.get(TrustWorksReferencePage)
    )

  def getSubcontractor(
    cisId: String,
    subbieResourceRef: Long
  )(implicit hc: HeaderCarrier): Future[GetSubcontractorResponse] =
    cisConnector.getSubcontractor(cisId = cisId, subbieResourceRef = subbieResourceRef)

  def updateSubcontractor(
    userAnswers: UserAnswers,
    subbieResourceRef: Option[Long] = None
  )(implicit hc: HeaderCarrier): Future[Unit] =
    for {
      cisId             <- getCisId(userAnswers)
      subcontractorType <- getSubcontractorType(userAnswers)
      original          <- getOriginalSubcontractor(userAnswers)
      _                 <- validateAmendSubbieResourceRef(userAnswers, original, subbieResourceRef)

      subcontractor =
        updateSubcontractorFromUserAnswers(
          original = original,
          subcontractorType = subcontractorType,
          userAnswers = userAnswers
        )

      response <-
        cisConnector.updateSubcontractor(
          UpdateSubcontractorRequest(
            cisId = cisId,
            subcontractor = subcontractor
          )
        )
    } yield response

  private def validateAmendSubbieResourceRef(
    userAnswers: UserAnswers,
    original: SubcontractorResponse,
    submittedSubbieResourceRef: Option[Long]
  ): Future[Unit] =
    submittedSubbieResourceRef match {
      case Some(expectedRef) =>
        val storedRef   = userAnswers.get(AmendSubbieResourceRefQuery)
        val originalRef = original.subbieResourceRef

        if (storedRef.contains(expectedRef) && originalRef.contains(expectedRef)) {
          Future.successful(())
        } else {
          Future.failed(
            new RuntimeException(
              s"Stale amend session for subbieResourceRef=$expectedRef"
            )
          )
        }

      case None =>
        Future.successful(())
    }

  private def getOriginalSubcontractor(
    userAnswers: UserAnswers
  ): Future[SubcontractorResponse] =
    userAnswers.get(OriginalSubcontractorQuery) match {
      case Some(subcontractor) =>
        Future.successful(subcontractor)

      case None =>
        Future.failed(
          new RuntimeException(
            "OriginalSubcontractorQuery not found in session data"
          )
        )
    }

  private def toSubcontractorRequest(
    subcontractor: SubcontractorResponse
  ): SubcontractorRequest =
    SubcontractorRequest(
      subcontractorId = subcontractor.subcontractorId,
      utr = subcontractor.utr,
      pageVisited = subcontractor.pageVisited,
      partnerUtr = subcontractor.partnerUtr,
      crn = subcontractor.crn,
      firstName = subcontractor.firstName,
      nino = subcontractor.nino,
      secondName = subcontractor.secondName,
      surname = subcontractor.surname,
      partnershipTradingName = subcontractor.partnershipTradingName,
      tradingName = subcontractor.tradingName,
      subcontractorType = subcontractor.subcontractorType,
      addressLine1 = subcontractor.addressLine1,
      addressLine2 = subcontractor.addressLine2,
      addressLine3 = subcontractor.addressLine3,
      addressLine4 = subcontractor.addressLine4,
      country = subcontractor.country,
      postcode = subcontractor.postcode,
      emailAddress = subcontractor.emailAddress,
      phoneNumber = subcontractor.phoneNumber,
      mobilePhoneNumber = subcontractor.mobilePhoneNumber,
      worksReferenceNumber = subcontractor.worksReferenceNumber,
      createDate = subcontractor.createDate,
      lastUpdate = subcontractor.lastUpdate,
      subbieResourceRef = subcontractor.subbieResourceRef,
      matched = subcontractor.matched,
      autoVerified = subcontractor.autoVerified,
      verified = subcontractor.verified,
      verificationNumber = subcontractor.verificationNumber,
      taxTreatment = subcontractor.taxTreatment,
      verificationDate = subcontractor.verificationDate,
      version = subcontractor.version,
      updatedTaxTreatment = subcontractor.updatedTaxTreatment,
      lastMonthlyReturnDate = subcontractor.lastMonthlyReturnDate,
      pendingVerifications = subcontractor.pendingVerifications
    )

  private def updateSubcontractorFromUserAnswers(
    original: SubcontractorResponse,
    subcontractorType: TypeOfSubcontractor,
    userAnswers: UserAnswers
  ): SubcontractorRequest = {

    val existing =
      toSubcontractorRequest(original)

    subcontractorType match {
      case Individualorsoletrader =>
        updateIndividual(
          existing,
          userAnswers
        )

      case Limitedcompany =>
        updateCompany(
          existing,
          userAnswers
        )

      case Partnership =>
        updatePartnership(
          existing,
          userAnswers
        )

      case Trust =>
        updateTrust(
          existing,
          userAnswers
        )
    }
  }

  private def updateCompany(
    existing: SubcontractorRequest,
    userAnswers: UserAnswers
  ): SubcontractorRequest = {

    val address =
      userAnswers.get(CompanyAddressPage)

    val removeAddress =
      userAnswers.get(CompanyAddressYesNoPage).contains(false)

    existing.copy(
      utr = userAnswers.get(CompanyUtrPage),
      crn = userAnswers.get(CompanyCrnPage),
      tradingName = userAnswers.get(CompanyNamePage),
      addressLine1 = updatedAddressField(address.map(_.addressLine1), existing.addressLine1, removeAddress),
      addressLine2 = updatedAddressField(address.flatMap(_.addressLine2), existing.addressLine2, removeAddress),
      addressLine3 = updatedAddressField(address.flatMap(_.addressLine3), existing.addressLine3, removeAddress),
      addressLine4 = updatedAddressField(address.flatMap(_.addressLine4), existing.addressLine4, removeAddress),
      country = updatedAddressField(address.flatMap(_.country).flatMap(_.name), existing.country, removeAddress),
      postcode = updatedAddressField(address.flatMap(_.postcode), existing.postcode, removeAddress),
      emailAddress = userAnswers.get(CompanyEmailAddressPage),
      phoneNumber = userAnswers.get(CompanyPhoneNumberPage),
      mobilePhoneNumber = userAnswers.get(CompanyMobileNumberPage),
      worksReferenceNumber = userAnswers.get(CompanyWorksReferencePage)
    )
  }

  private def updatePartnership(existing: SubcontractorRequest, userAnswers: UserAnswers): SubcontractorRequest = {
    val address       = userAnswers.get(PartnershipAddressPage)
    val removeAddress =
      userAnswers.get(PartnershipAddressYesNoPage).contains(false)

    existing.copy(
      utr = userAnswers.get(PartnershipUniqueTaxpayerReferencePage),
      partnerUtr = userAnswers.get(PartnershipNominatedPartnerUtrPage),
      partnershipTradingName = userAnswers.get(PartnershipNamePage),
      tradingName = userAnswers.get(PartnershipNominatedPartnerNamePage),
      nino = userAnswers.get(PartnershipNominatedPartnerNinoPage),
      crn = userAnswers.get(PartnershipNominatedPartnerCrnPage),
      addressLine1 = updatedAddressField(address.map(_.addressLine1), existing.addressLine1, removeAddress),
      addressLine2 = updatedAddressField(address.flatMap(_.addressLine2), existing.addressLine2, removeAddress),
      addressLine3 = updatedAddressField(address.flatMap(_.addressLine3), existing.addressLine3, removeAddress),
      addressLine4 = updatedAddressField(address.flatMap(_.addressLine4), existing.addressLine4, removeAddress),
      country = address
        .flatMap(_.country)
        .flatMap(_.name)
        .orElse(if (removeAddress) None else existing.country),
      postcode = updatedAddressField(address.flatMap(_.postcode), existing.postcode, removeAddress),
      emailAddress = userAnswers.get(
        PartnershipEmailAddressPage
      ),
      phoneNumber = userAnswers.get(
        PartnershipPhoneNumberPage
      ),
      mobilePhoneNumber = userAnswers.get(
        PartnershipMobileNumberPage
      ),
      worksReferenceNumber = userAnswers.get(
        PartnershipWorksReferenceNumberPage
      )
    )
  }

  private def updateTrust(
    existing: SubcontractorRequest,
    userAnswers: UserAnswers
  ): SubcontractorRequest = {

    val address =
      userAnswers.get(TrustAddressPage)

    val removeAddress =
      userAnswers.get(TrustAddressYesNoPage).contains(false)

    existing.copy(
      utr = userAnswers.get(TrustUtrPage),
      tradingName = userAnswers.get(TrustNamePage),
      addressLine1 = updatedAddressField(address.map(_.addressLine1), existing.addressLine1, removeAddress),
      addressLine2 = updatedAddressField(address.flatMap(_.addressLine2), existing.addressLine2, removeAddress),
      addressLine3 = updatedAddressField(address.flatMap(_.addressLine3), existing.addressLine3, removeAddress),
      addressLine4 = updatedAddressField(address.flatMap(_.addressLine4), existing.addressLine4, removeAddress),
      country = address
        .flatMap(_.country)
        .flatMap(_.name)
        .orElse(if (removeAddress) None else existing.country),
      postcode = updatedAddressField(address.flatMap(_.postcode), existing.postcode, removeAddress),
      emailAddress = userAnswers.get(TrustEmailAddressPage),
      phoneNumber = userAnswers.get(TrustPhoneNumberPage),
      mobilePhoneNumber = userAnswers.get(TrustMobileNumberPage),
      worksReferenceNumber = userAnswers.get(TrustWorksReferencePage)
    )
  }

  private def updateIndividual(
    existing: SubcontractorRequest,
    userAnswers: UserAnswers
  ): SubcontractorRequest = {

    val name =
      userAnswers.get(SubcontractorNamePage)

    val address =
      userAnswers.get(AddressOfSubcontractorPage)

    val removeName =
      name.isEmpty && userAnswers.get(SubTradingNameYesNoPage).contains(true)

    val removeAddress =
      userAnswers.get(SubAddressYesNoPage).contains(false)

    existing.copy(
      firstName = if (removeName) None else name.map(_.firstName).orElse(existing.firstName),
      secondName = if (removeName) None else name.fold(existing.secondName)(_.middleName),
      surname = if (removeName) None else name.map(_.lastName).orElse(existing.surname),
      tradingName = userAnswers.get(
        TradingNameOfSubcontractorPage
      ),
      nino = userAnswers.get(
        SubNationalInsuranceNumberPage
      ),
      utr = userAnswers.get(
        SubcontractorsUniqueTaxpayerReferencePage
      ),
      addressLine1 = updatedAddressField(address.map(_.addressLine1), existing.addressLine1, removeAddress),
      addressLine2 = updatedAddressField(address.flatMap(_.addressLine2), existing.addressLine2, removeAddress),
      addressLine3 = updatedAddressField(address.flatMap(_.addressLine3), existing.addressLine3, removeAddress),
      addressLine4 = updatedAddressField(address.flatMap(_.addressLine4), existing.addressLine4, removeAddress),
      country = address
        .flatMap(_.country)
        .flatMap(_.name)
        .orElse(if (removeAddress) None else existing.country),
      postcode = updatedAddressField(address.flatMap(_.postcode), existing.postcode, removeAddress),
      emailAddress = userAnswers.get(
        IndividualEmailAddressPage
      ),
      phoneNumber = userAnswers.get(
        IndividualPhoneNumberPage
      ),
      mobilePhoneNumber = userAnswers.get(
        IndividualMobileNumberPage
      ),
      worksReferenceNumber = userAnswers.get(
        WorksReferenceNumberPage
      )
    )
  }

  private def updatedAddressField(
    amended: Option[String],
    existing: Option[String],
    removeAddress: Boolean
  ): Option[String] =
    if (removeAddress) None else amended.orElse(existing)

}
