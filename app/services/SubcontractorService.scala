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
import models.contact.ContactMethodOptions
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

    val contactMethodsYesNo =
      userAnswers.get(AddCompanyContactMethodsYesNoPage)

    val contactMethods =
      userAnswers.get(CompanyContactMethodOptionsPage)

    existing.copy(
      utr = updatedOptionalField(
        userAnswers.get(CompanyUtrPage),
        existing.utr,
        userAnswers.get(CompanyUtrYesNoPage).contains(false)
      ),
      crn = updatedOptionalField(
        userAnswers.get(CompanyCrnPage),
        existing.crn,
        userAnswers.get(CompanyCrnYesNoPage).contains(false)
      ),
      tradingName = userAnswers.get(CompanyNamePage).orElse(existing.tradingName),
      addressLine1 =
        updatedAddressField(address.map(_.addressLine1), existing.addressLine1, removeAddress, address.isDefined),
      addressLine2 =
        updatedAddressField(address.flatMap(_.addressLine2), existing.addressLine2, removeAddress, address.isDefined),
      addressLine3 =
        updatedAddressField(address.flatMap(_.addressLine3), existing.addressLine3, removeAddress, address.isDefined),
      addressLine4 =
        updatedAddressField(address.flatMap(_.addressLine4), existing.addressLine4, removeAddress, address.isDefined),
      country =
        updatedAddressField(address.flatMap(_.country).flatMap(_.name), existing.country, removeAddress, address.isDefined),
      postcode = updatedAddressField(address.flatMap(_.postcode), existing.postcode, removeAddress, address.isDefined),
      emailAddress =
        updatedContactField(
          userAnswers.get(CompanyEmailAddressPage),
          existing.emailAddress,
          contactMethodsYesNo,
          contactMethods,
          ContactMethodOptions.Email
        ),
      phoneNumber =
        updatedContactField(
          userAnswers.get(CompanyPhoneNumberPage),
          existing.phoneNumber,
          contactMethodsYesNo,
          contactMethods,
          ContactMethodOptions.Phone
        ),
      mobilePhoneNumber =
        updatedContactField(
          userAnswers.get(CompanyMobileNumberPage),
          existing.mobilePhoneNumber,
          contactMethodsYesNo,
          contactMethods,
          ContactMethodOptions.Mobile
        ),
      worksReferenceNumber =
        updatedOptionalField(
          userAnswers.get(CompanyWorksReferencePage),
          existing.worksReferenceNumber,
          userAnswers.get(CompanyWorksReferenceYesNoPage).contains(false)
        )
    )
  }

  private def updatePartnership(existing: SubcontractorRequest, userAnswers: UserAnswers): SubcontractorRequest = {
    val address       = userAnswers.get(PartnershipAddressPage)
    val removeAddress =
      userAnswers.get(PartnershipAddressYesNoPage).contains(false)

    val contactMethodsYesNo =
      userAnswers.get(AddPartnershipContactMethodsYesNoPage)

    val contactMethods =
      userAnswers.get(PartnershipContactMethodOptionsPage)

    existing.copy(
      utr = updatedOptionalField(
        userAnswers.get(PartnershipUniqueTaxpayerReferencePage),
        existing.utr,
        userAnswers.get(PartnershipHasUtrYesNoPage).contains(false)
      ),
      partnerUtr = updatedOptionalField(
        userAnswers.get(PartnershipNominatedPartnerUtrPage),
        existing.partnerUtr,
        userAnswers.get(PartnershipNominatedPartnerUtrYesNoPage).contains(false)
      ),
      partnershipTradingName = userAnswers.get(PartnershipNamePage).orElse(existing.partnershipTradingName),
      tradingName = userAnswers.get(PartnershipNominatedPartnerNamePage).orElse(existing.tradingName),
      nino = updatedOptionalField(
        userAnswers.get(PartnershipNominatedPartnerNinoPage),
        existing.nino,
        userAnswers.get(PartnershipNominatedPartnerNinoYesNoPage).contains(false)
      ),
      crn = updatedOptionalField(
        userAnswers.get(PartnershipNominatedPartnerCrnPage),
        existing.crn,
        userAnswers.get(PartnershipNominatedPartnerCrnYesNoPage).contains(false)
      ),
      addressLine1 =
        updatedAddressField(address.map(_.addressLine1), existing.addressLine1, removeAddress, address.isDefined),
      addressLine2 =
        updatedAddressField(address.flatMap(_.addressLine2), existing.addressLine2, removeAddress, address.isDefined),
      addressLine3 =
        updatedAddressField(address.flatMap(_.addressLine3), existing.addressLine3, removeAddress, address.isDefined),
      addressLine4 =
        updatedAddressField(address.flatMap(_.addressLine4), existing.addressLine4, removeAddress, address.isDefined),
      country =
        updatedAddressField(address.flatMap(_.country).flatMap(_.name), existing.country, removeAddress, address.isDefined),
      postcode = updatedAddressField(address.flatMap(_.postcode), existing.postcode, removeAddress, address.isDefined),
      emailAddress =
        updatedContactField(
          userAnswers.get(PartnershipEmailAddressPage),
          existing.emailAddress,
          contactMethodsYesNo,
          contactMethods,
          ContactMethodOptions.Email
        ),
      phoneNumber =
        updatedContactField(
          userAnswers.get(PartnershipPhoneNumberPage),
          existing.phoneNumber,
          contactMethodsYesNo,
          contactMethods,
          ContactMethodOptions.Phone
        ),
      mobilePhoneNumber =
        updatedContactField(
          userAnswers.get(PartnershipMobileNumberPage),
          existing.mobilePhoneNumber,
          contactMethodsYesNo,
          contactMethods,
          ContactMethodOptions.Mobile
        ),
      worksReferenceNumber = updatedOptionalField(
        userAnswers.get(PartnershipWorksReferenceNumberPage),
        existing.worksReferenceNumber,
        userAnswers.get(PartnershipWorksReferenceNumberYesNoPage).contains(false)
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

    val contactMethodsYesNo =
      userAnswers.get(AddTrustContactMethodsYesNoPage)

    val contactMethods =
      userAnswers.get(TrustContactMethodOptionsPage)

    existing.copy(
      utr = updatedOptionalField(
        userAnswers.get(TrustUtrPage),
        existing.utr,
        userAnswers.get(TrustUtrYesNoPage).contains(false)
      ),
      tradingName = userAnswers.get(TrustNamePage).orElse(existing.tradingName),
      addressLine1 =
        updatedAddressField(address.map(_.addressLine1), existing.addressLine1, removeAddress, address.isDefined),
      addressLine2 =
        updatedAddressField(address.flatMap(_.addressLine2), existing.addressLine2, removeAddress, address.isDefined),
      addressLine3 =
        updatedAddressField(address.flatMap(_.addressLine3), existing.addressLine3, removeAddress, address.isDefined),
      addressLine4 =
        updatedAddressField(address.flatMap(_.addressLine4), existing.addressLine4, removeAddress, address.isDefined),
      country =
        updatedAddressField(address.flatMap(_.country).flatMap(_.name), existing.country, removeAddress, address.isDefined),
      postcode = updatedAddressField(address.flatMap(_.postcode), existing.postcode, removeAddress, address.isDefined),
      emailAddress =
        updatedContactField(
          userAnswers.get(TrustEmailAddressPage),
          existing.emailAddress,
          contactMethodsYesNo,
          contactMethods,
          ContactMethodOptions.Email
        ),
      phoneNumber =
        updatedContactField(
          userAnswers.get(TrustPhoneNumberPage),
          existing.phoneNumber,
          contactMethodsYesNo,
          contactMethods,
          ContactMethodOptions.Phone
        ),
      mobilePhoneNumber =
        updatedContactField(
          userAnswers.get(TrustMobileNumberPage),
          existing.mobilePhoneNumber,
          contactMethodsYesNo,
          contactMethods,
          ContactMethodOptions.Mobile
        ),
      worksReferenceNumber =
        updatedOptionalField(
          userAnswers.get(TrustWorksReferencePage),
          existing.worksReferenceNumber,
          userAnswers.get(TrustWorksReferenceYesNoPage).contains(false)
        )
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

    val contactMethodsYesNo =
      userAnswers.get(AddIndividualContactMethodsYesNoPage)

    val contactMethods =
      userAnswers.get(IndividualContactMethodOptionsPage)

    existing.copy(
      firstName = updatedGroupedField(name.map(_.firstName), existing.firstName, removeName, name.isDefined),
      secondName = updatedGroupedField(name.flatMap(_.middleName), existing.secondName, removeName, name.isDefined),
      surname = updatedGroupedField(name.map(_.lastName), existing.surname, removeName, name.isDefined),
      tradingName =
        updatedOptionalField(
          userAnswers.get(TradingNameOfSubcontractorPage),
          existing.tradingName,
          userAnswers.get(SubTradingNameYesNoPage).contains(false)
        ),
      nino =
        updatedOptionalField(
          userAnswers.get(SubNationalInsuranceNumberPage),
          existing.nino,
          userAnswers.get(NationalInsuranceNumberYesNoPage).contains(false)
        ),
      utr =
        updatedOptionalField(
          userAnswers.get(SubcontractorsUniqueTaxpayerReferencePage),
          existing.utr,
          userAnswers.get(UniqueTaxpayerReferenceYesNoPage).contains(false)
        ),
      addressLine1 =
        updatedAddressField(address.map(_.addressLine1), existing.addressLine1, removeAddress, address.isDefined),
      addressLine2 =
        updatedAddressField(address.flatMap(_.addressLine2), existing.addressLine2, removeAddress, address.isDefined),
      addressLine3 =
        updatedAddressField(address.flatMap(_.addressLine3), existing.addressLine3, removeAddress, address.isDefined),
      addressLine4 =
        updatedAddressField(address.flatMap(_.addressLine4), existing.addressLine4, removeAddress, address.isDefined),
      country =
        updatedAddressField(address.flatMap(_.country).flatMap(_.name), existing.country, removeAddress, address.isDefined),
      postcode = updatedAddressField(address.flatMap(_.postcode), existing.postcode, removeAddress, address.isDefined),
      emailAddress =
        updatedContactField(
          userAnswers.get(IndividualEmailAddressPage),
          existing.emailAddress,
          contactMethodsYesNo,
          contactMethods,
          ContactMethodOptions.Email
        ),
      phoneNumber =
        updatedContactField(
          userAnswers.get(IndividualPhoneNumberPage),
          existing.phoneNumber,
          contactMethodsYesNo,
          contactMethods,
          ContactMethodOptions.Phone
        ),
      mobilePhoneNumber =
        updatedContactField(
          userAnswers.get(IndividualMobileNumberPage),
          existing.mobilePhoneNumber,
          contactMethodsYesNo,
          contactMethods,
          ContactMethodOptions.Mobile
        ),
      worksReferenceNumber =
        updatedOptionalField(
          userAnswers.get(WorksReferenceNumberPage),
          existing.worksReferenceNumber,
          userAnswers.get(WorksReferenceNumberYesNoPage).contains(false)
        )
    )
  }

  private def updatedGroupedField(
    amended: Option[String],
    existing: Option[String],
    removeGroup: Boolean,
    groupAmended: Boolean
  ): Option[String] =
    if (removeGroup) Some("")
    else if (groupAmended) amended.orElse(existing.map(_ => ""))
    else existing

  private def updatedOptionalField(
    amended: Option[String],
    existing: Option[String],
    removeField: Boolean
  ): Option[String] =
    if (removeField) Some("") else amended.orElse(existing)

  private def updatedContactField(
    amended: Option[String],
    existing: Option[String],
    contactMethodsYesNo: Option[Boolean],
    contactMethods: Option[Set[ContactMethodOptions]],
    contactMethod: ContactMethodOptions
  ): Option[String] =
    contactMethodsYesNo match {
      case Some(false) =>
        Some("")
      case Some(true)  =>
        contactMethods match {
          case Some(selectedMethods) if selectedMethods.contains(contactMethod) =>
            amended.orElse(existing.map(_ => ""))
          case Some(_)                                                          =>
            existing.map(_ => "")
          case None                                                             =>
            existing
        }
      case None        =>
        amended.orElse(existing)
    }

  private def updatedAddressField(
    amended: Option[String],
    existing: Option[String],
    removeAddress: Boolean,
    addressAmended: Boolean
  ): Option[String] =
    if (removeAddress) Some("")
    else if (addressAmended) amended.orElse(existing.map(_ => ""))
    else existing

}
