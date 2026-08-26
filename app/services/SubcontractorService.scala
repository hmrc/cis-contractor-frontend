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
import models.address.Address
import models.requests.CreateAndUpdateSubcontractorPayload.{CompanyPayload, IndividualOrSoleTraderPayload, PartnershipPayload, TrustPayload}
import models.response.GetSubcontractorResponse
import pages.add.*
import pages.add.partnership.*
import pages.add.company.*
import pages.add.trust.*
import play.api.Logging
import queries.{CisIdQuery, OriginalCompanyAnswersQuery, OriginalIndividualAnswersQuery, OriginalPartnershipAnswersQuery, OriginalTrustAnswersQuery}
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

  private def partnershipPayloadFromUserAnswers(
    cisId: String,
    subcontractorType: TypeOfSubcontractor,
    userAnswers: UserAnswers
  ): PartnershipPayload = {
    val address = addressFields(
      currentAddress = userAnswers.get(PartnershipAddressPage),
      originalAddress = userAnswers.get(OriginalPartnershipAnswersQuery).flatMap(_.address)
    )

    PartnershipPayload(
      cisId = cisId,
      subcontractorType = subcontractorType,
      utr = userAnswers.get(PartnershipUniqueTaxpayerReferencePage),
      partnerUtr = userAnswers.get(PartnershipNominatedPartnerUtrPage),
      partnershipTradingName = userAnswers.get(PartnershipNamePage),
      partnerTradingName = userAnswers.get(PartnershipNominatedPartnerNamePage),
      partnerNino = userAnswers.get(PartnershipNominatedPartnerNinoPage),
      partnerCrn = userAnswers.get(PartnershipNominatedPartnerCrnPage),
      addressLine1 = address.addressLine1,
      addressLine2 = address.addressLine2,
      city = address.city,
      county = address.county,
      postcode = address.postcode,
      country = address.country,
      emailAddress = userAnswers.get(PartnershipEmailAddressPage),
      phoneNumber = userAnswers.get(PartnershipPhoneNumberPage),
      mobilePhoneNumber = userAnswers.get(PartnershipMobileNumberPage),
      worksReferenceNumber = userAnswers.get(PartnershipWorksReferenceNumberPage)
    )
  }

  private def individualOrSoleTraderPayloadFromUserAnswers(
    cisId: String,
    subcontractorType: TypeOfSubcontractor,
    userAnswers: UserAnswers
  ): IndividualOrSoleTraderPayload = {
    val address = addressFields(
      currentAddress = userAnswers.get(AddressOfSubcontractorPage),
      originalAddress = userAnswers.get(OriginalIndividualAnswersQuery).flatMap(_.address)
    )

    IndividualOrSoleTraderPayload(
      cisId = cisId,
      subcontractorType = subcontractorType,
      firstName = userAnswers.get(SubcontractorNamePage).map(_.firstName),
      secondName = userAnswers.get(SubcontractorNamePage).flatMap(_.middleName),
      surname = userAnswers.get(SubcontractorNamePage).map(_.lastName),
      tradingName = userAnswers.get(TradingNameOfSubcontractorPage),
      addressLine1 = address.addressLine1,
      addressLine2 = address.addressLine2,
      city = address.city,
      county = address.county,
      postcode = address.postcode,
      country = address.country,
      nino = userAnswers.get(SubNationalInsuranceNumberPage),
      utr = userAnswers.get(SubcontractorsUniqueTaxpayerReferencePage),
      worksReferenceNumber = userAnswers.get(WorksReferenceNumberPage),
      emailAddress = userAnswers.get(IndividualEmailAddressPage),
      phoneNumber = userAnswers.get(IndividualPhoneNumberPage),
      mobilePhoneNumber = userAnswers.get(IndividualMobileNumberPage)
    )
  }

  private def companyPayloadFromUserAnswers(
    cisId: String,
    subcontractorType: TypeOfSubcontractor,
    userAnswers: UserAnswers
  ): CompanyPayload = {
    val address = addressFields(
      currentAddress = userAnswers.get(CompanyAddressPage),
      originalAddress = userAnswers.get(OriginalCompanyAnswersQuery).flatMap(_.address)
    )

    CompanyPayload(
      cisId = cisId,
      subcontractorType = subcontractorType,
      utr = userAnswers.get(CompanyUtrPage),
      crn = userAnswers.get(CompanyCrnPage),
      tradingName = userAnswers.get(CompanyNamePage),
      addressLine1 = address.addressLine1,
      addressLine2 = address.addressLine2,
      city = address.city,
      county = address.county,
      postcode = address.postcode,
      country = address.country,
      emailAddress = userAnswers.get(CompanyEmailAddressPage),
      phoneNumber = userAnswers.get(CompanyPhoneNumberPage),
      mobilePhoneNumber = userAnswers.get(CompanyMobileNumberPage),
      worksReferenceNumber = userAnswers.get(CompanyWorksReferencePage)
    )
  }

  private def trustPayloadFromUserAnswers(
    cisId: String,
    subcontractorType: TypeOfSubcontractor,
    userAnswers: UserAnswers
  ): TrustPayload = {
    val address = addressFields(
      currentAddress = userAnswers.get(TrustAddressPage),
      originalAddress = userAnswers.get(OriginalTrustAnswersQuery).flatMap(_.address)
    )

    TrustPayload(
      cisId = cisId,
      subcontractorType = subcontractorType,
      trustTradingName = userAnswers.get(TrustNamePage),
      utr = userAnswers.get(TrustUtrPage),
      addressLine1 = address.addressLine1,
      addressLine2 = address.addressLine2,
      city = address.city,
      county = address.county,
      postcode = address.postcode,
      country = address.country,
      emailAddress = userAnswers.get(TrustEmailAddressPage),
      phoneNumber = userAnswers.get(TrustPhoneNumberPage),
      mobilePhoneNumber = userAnswers.get(TrustMobileNumberPage),
      worksReferenceNumber = userAnswers.get(TrustWorksReferencePage)
    )
  }

  private case class AddressFields(
    addressLine1: Option[String],
    addressLine2: Option[String],
    city: Option[String],
    county: Option[String],
    postcode: Option[String],
    country: Option[String]
  )

  private def addressFields(
    currentAddress: Option[Address],
    originalAddress: Option[Address]
  ): AddressFields = {
    def field(currentValue: Option[String], originalValue: Option[String]): Option[String] =
      currentValue.orElse(originalValue.map(_ => ""))

    AddressFields(
      addressLine1 = currentAddress.map(_.addressLine1).orElse(originalAddress.map(_ => "")),
      addressLine2 = field(currentAddress.flatMap(_.addressLine2), originalAddress.flatMap(_.addressLine2)),
      city = field(currentAddress.flatMap(_.addressLine3), originalAddress.flatMap(_.addressLine3)),
      county = field(currentAddress.flatMap(_.addressLine4), originalAddress.flatMap(_.addressLine4)),
      postcode = field(currentAddress.flatMap(_.postcode), originalAddress.flatMap(_.postcode)),
      country =
        field(currentAddress.flatMap(_.country).flatMap(_.name), originalAddress.flatMap(_.country).flatMap(_.name))
    )
  }

  def getSubcontractor(
    cisId: String,
    subbieResourceRef: Long
  )(implicit hc: HeaderCarrier): Future[GetSubcontractorResponse] =
    cisConnector.getSubcontractor(cisId = cisId, subbieResourceRef = subbieResourceRef)
}
