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
import models.TypeOfSubcontractor
import models.TypeOfSubcontractor.*
import models.UserAnswers
import models.add.SubcontractorName as AddSubcontractorName
import models.address.{Address, Country}
import models.finalvalidation.FinalValidationChangeTarget
import models.finalvalidation.FinalValidationChangeTarget.{Address as AddressTarget, SubcontractorName as SubcontractorNameTarget, *}
import models.finalvalidation.FinalValidationUpdateSubcontractorRequest
import models.response.{GetSubcontractorResponse, SubcontractorResponse}
import pages.QuestionPage
import pages.add.*
import pages.add.company.*
import pages.add.partnership.*
import pages.add.trust.*
import queries.CisIdQuery
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

@Singleton
class FinalValidationSubcontractorService @Inject() (
  cisConnector: ConstructionIndustrySchemeConnector
) {

  def updateSubcontractorForFinalValidation(
    request: FinalValidationUpdateSubcontractorRequest
  )(implicit hc: HeaderCarrier): Future[Unit] =
    cisConnector.updateSubcontractorForFinalValidation(request)
  
  def populateFinalValidationUserAnswers(
    userAnswers: UserAnswers,
    instanceId: String,
    response: GetSubcontractorResponse,
    changeTarget: FinalValidationChangeTarget
  ): Try[UserAnswers] =
    for {
      subcontractor <- getFinalValidationSubcontractor(response)
      subcontractorType <- getFinalValidationSubcontractorType(subcontractor)
      withCisId <- userAnswers.set(CisIdQuery, instanceId)
      withType <- withCisId.set(TypeOfSubcontractorPage, subcontractorType)
      withName <- populateFinalValidationName(withType, subcontractorType, subcontractor)
      result <- populateFinalValidationTarget(withName, subcontractorType, subcontractor, changeTarget)
    } yield result

  private def getFinalValidationSubcontractor(response: GetSubcontractorResponse): Try[SubcontractorResponse] =
    response.subcontractor match {
      case Some(subcontractor) =>
        Success(subcontractor)
      case None =>
        Failure(new RuntimeException("Subcontractor not found in GetSubcontractorResponse"))
    }

  private def populateFinalValidationName(
    userAnswers: UserAnswers,
    subcontractorType: TypeOfSubcontractor,
    subcontractor: SubcontractorResponse
  ): Try[UserAnswers] =
    subcontractorType match {
      case Individualorsoletrader =>
        userAnswers.set(
          SubcontractorNamePage,
          AddSubcontractorName(
            firstName = subcontractor.firstName.getOrElse(""),
            middleName = subcontractor.secondName,
            lastName = subcontractor.surname.getOrElse("")
          )
        )

      case Limitedcompany =>
        userAnswers.set(CompanyNamePage, subcontractor.tradingName.getOrElse(""))

      case Trust =>
        userAnswers.set(TrustNamePage, subcontractor.tradingName.getOrElse(""))

      case Partnership =>
        userAnswers.set(PartnershipNamePage, subcontractor.partnershipTradingName.getOrElse(""))
    }

  private def populateFinalValidationTarget(
    userAnswers: UserAnswers,
    subcontractorType: TypeOfSubcontractor,
    subcontractor: SubcontractorResponse,
    target: FinalValidationChangeTarget
  ): Try[UserAnswers] =
    target match {
      case SubcontractorNameTarget | TradingName | PartnershipTradingName =>
        populateNameTarget(userAnswers, subcontractorType, subcontractor, target)

      case UtrYesNo | Utr | PartnerUtrYesNo | PartnerUtr | NinoYesNo | Nino | CrnYesNo | Crn | WorksReferenceNumberYesNo | WorksReferenceNumber =>
        populateIdentifierTarget(userAnswers, subcontractorType, subcontractor, target)

      case AddressYesNo | AddressTarget =>
        populateAddressTarget(userAnswers, subcontractorType, subcontractor, target)

      case ContactDetailsYesNo | EmailAddress | PhoneNumber | MobilePhoneNumber =>
        populateContactTarget(userAnswers, subcontractorType, subcontractor, target)
    }

  private def populateNameTarget(
    userAnswers: UserAnswers,
    subcontractorType: TypeOfSubcontractor,
    subcontractor: SubcontractorResponse,
    target: FinalValidationChangeTarget
  ): Try[UserAnswers] =
    target match {
      case SubcontractorNameTarget | PartnershipTradingName =>
        Success(userAnswers)

      case TradingName =>
        subcontractorType match {
          case Individualorsoletrader =>
            setStringIfPresent(userAnswers, TradingNameOfSubcontractorPage, subcontractor.tradingName)
          case Limitedcompany | Trust =>
            Success(userAnswers)
          case Partnership =>
            setStringIfPresent(userAnswers, PartnershipNominatedPartnerNamePage, subcontractor.tradingName)
        }

      case _ =>
        Failure(new RuntimeException(s"Unexpected target for name population: $target"))
    }

  private def populateIdentifierTarget(
    userAnswers: UserAnswers,
    subcontractorType: TypeOfSubcontractor,
    subcontractor: SubcontractorResponse,
    target: FinalValidationChangeTarget
  ): Try[UserAnswers] =
    target match {
      case UtrYesNo =>
        val (yesNoPage, _) = utrPages(subcontractorType)
        userAnswers.set(yesNoPage, hasValue(subcontractor.utr))
      case Utr =>
        val (_, valuePage) = utrPages(subcontractorType)
        setStringIfPresent(userAnswers, valuePage, subcontractor.utr)
      case PartnerUtrYesNo if subcontractorType == Partnership =>
        userAnswers.set(PartnershipNominatedPartnerUtrYesNoPage, hasValue(subcontractor.partnerUtr))
      case PartnerUtr if subcontractorType == Partnership =>
        setStringIfPresent(userAnswers, PartnershipNominatedPartnerUtrPage, subcontractor.partnerUtr)
      case NinoYesNo =>
        ninoPages(subcontractorType).flatMap { case (yesNoPage, _) =>
          userAnswers.set(yesNoPage, hasValue(subcontractor.nino))
        }
      case Nino =>
        ninoPages(subcontractorType).flatMap { case (_, valuePage) =>
          setStringIfPresent(userAnswers, valuePage, subcontractor.nino)
        }
      case CrnYesNo =>
        crnPages(subcontractorType).flatMap { case (yesNoPage, _) =>
          userAnswers.set(yesNoPage, hasValue(subcontractor.crn))
        }
      case Crn =>
        crnPages(subcontractorType).flatMap { case (_, valuePage) =>
          setStringIfPresent(userAnswers, valuePage, subcontractor.crn)
        }
      case WorksReferenceNumberYesNo =>
        val (yesNoPage, _) = worksReferenceNumberPages(subcontractorType)
        userAnswers.set(yesNoPage, hasValue(subcontractor.worksReferenceNumber))
      case WorksReferenceNumber =>
        val (_, valuePage) = worksReferenceNumberPages(subcontractorType)
        setStringIfPresent(userAnswers, valuePage, subcontractor.worksReferenceNumber)
      case _ =>
        Failure(new RuntimeException(s"Unexpected target for identifier population: $target"))
    }

  private def populateAddressTarget(
    userAnswers: UserAnswers,
    subcontractorType: TypeOfSubcontractor,
    subcontractor: SubcontractorResponse,
    target: FinalValidationChangeTarget
  ): Try[UserAnswers] = {
    val (yesNoPage, addressPage) = addressPages(subcontractorType)
    target match {
      case AddressYesNo =>
        userAnswers.set(yesNoPage, hasAddress(subcontractor))
      case AddressTarget =>
        setAddressIfPresent(userAnswers, addressPage, toAddress(subcontractor))
      case _ =>
        Failure(new RuntimeException(s"Unexpected target for address population: $target"))
    }
  }

  private def populateContactTarget(
    userAnswers: UserAnswers,
    subcontractorType: TypeOfSubcontractor,
    subcontractor: SubcontractorResponse,
    target: FinalValidationChangeTarget
  ): Try[UserAnswers] = {
    val (yesNoPage, emailPage, phonePage, mobilePage) = contactPages(subcontractorType)

    target match {
      case ContactDetailsYesNo =>
        userAnswers.set(yesNoPage, hasAnyContact(subcontractor))
      case EmailAddress =>
        setStringIfPresent(userAnswers, emailPage, subcontractor.emailAddress)
      case PhoneNumber =>
        setStringIfPresent(userAnswers, phonePage, subcontractor.phoneNumber)
      case MobilePhoneNumber =>
        setStringIfPresent(userAnswers, mobilePage, subcontractor.mobilePhoneNumber)
      case _ =>
        Failure(new RuntimeException(s"Unexpected target for contact population: $target"))
    }
  }

  private def utrPages(subcontractorType: TypeOfSubcontractor): (QuestionPage[Boolean], QuestionPage[String]) =
    subcontractorType match {
      case Individualorsoletrader => (UniqueTaxpayerReferenceYesNoPage, SubcontractorsUniqueTaxpayerReferencePage)
      case Limitedcompany => (CompanyUtrYesNoPage, CompanyUtrPage)
      case Partnership => (PartnershipHasUtrYesNoPage, PartnershipUniqueTaxpayerReferencePage)
      case Trust => (TrustUtrYesNoPage, TrustUtrPage)
    }

  private def ninoPages(subcontractorType: TypeOfSubcontractor): Try[(QuestionPage[Boolean], QuestionPage[String])] =
    subcontractorType match {
      case Individualorsoletrader => Success((NationalInsuranceNumberYesNoPage, SubNationalInsuranceNumberPage))
      case Partnership => Success((PartnershipNominatedPartnerNinoYesNoPage, PartnershipNominatedPartnerNinoPage))
      case other => Failure(new RuntimeException(s"NINO not applicable for subocntactor type: $other"))
    }

  private def crnPages(subcontractorType: TypeOfSubcontractor): Try[(QuestionPage[Boolean], QuestionPage[String])] =
    subcontractorType match {
      case Limitedcompany => Success((CompanyCrnYesNoPage, CompanyCrnPage))
      case Partnership => Success((PartnershipNominatedPartnerCrnYesNoPage, PartnershipNominatedPartnerCrnPage))
      case other => Failure(new RuntimeException(s"CRN not applicable for subcontractor type: $other"))
    }

  private def worksReferenceNumberPages(subcontractorType: TypeOfSubcontractor): (QuestionPage[Boolean], QuestionPage[String]) =
    subcontractorType match {
      case Individualorsoletrader => (WorksReferenceNumberYesNoPage, WorksReferenceNumberPage)
      case Limitedcompany => (CompanyWorksReferenceYesNoPage, CompanyWorksReferencePage)
      case Partnership => (PartnershipWorksReferenceNumberYesNoPage, PartnershipWorksReferenceNumberPage)
      case Trust => (TrustWorksReferenceYesNoPage, TrustWorksReferencePage)
    }

  private def addressPages(subcontractorType: TypeOfSubcontractor): (QuestionPage[Boolean], QuestionPage[models.address.Address]) =
    subcontractorType match {
      case Individualorsoletrader => (SubAddressYesNoPage, AddressOfSubcontractorPage)
      case Limitedcompany => (CompanyAddressYesNoPage, CompanyAddressPage)
      case Partnership => (PartnershipAddressYesNoPage, PartnershipAddressPage)
      case Trust => (TrustAddressYesNoPage, TrustAddressPage)
    }

  private def contactPages(subcontractorType: TypeOfSubcontractor): (
    QuestionPage[Boolean], QuestionPage[String], QuestionPage[String], QuestionPage[String]
    ) =
    subcontractorType match {
      case Individualorsoletrader =>
        (AddIndividualContactMethodsYesNoPage, IndividualEmailAddressPage, IndividualPhoneNumberPage, IndividualMobileNumberPage)
      case Limitedcompany =>
        (AddCompanyContactMethodsYesNoPage, CompanyEmailAddressPage, CompanyPhoneNumberPage, CompanyMobileNumberPage)
      case Partnership =>
        (AddPartnershipContactMethodsYesNoPage, PartnershipEmailAddressPage, PartnershipPhoneNumberPage, PartnershipMobileNumberPage)
      case Trust =>
        (AddTrustContactMethodsYesNoPage, TrustEmailAddressPage, TrustPhoneNumberPage, TrustMobileNumberPage)
    }

  private def hasAddress(subcontractor: SubcontractorResponse): Boolean =
    hasValue(subcontractor.addressLine1)
  
  private def toAddress(
    subcontractor: SubcontractorResponse
  ): Option[Address] =
    if (!hasAddress(subcontractor)) {
      None
    } else {
      Some(
        Address(
          addressLine1 = nonBlank(subcontractor.addressLine1).getOrElse(""),
          addressLine2 = nonBlank(subcontractor.addressLine2),
          addressLine3 = nonBlank(subcontractor.addressLine3),
          addressLine4 = nonBlank(subcontractor.addressLine4),
          postcode = nonBlank(subcontractor.postcode),
          country = nonBlank(subcontractor.country).map { name =>
            Country(code = None, name = Some(name))
          }
        )
      )
    }

  private def setStringIfPresent(
    userAnswers: UserAnswers,
    page: QuestionPage[String],
    value: Option[String]
  ): Try[UserAnswers] =
    nonBlank(value) match {
      case Some(answer) =>
        userAnswers.set(page, answer)
      case None =>
        Success(userAnswers)
    }

  private def setAddressIfPresent(
    userAnswers: UserAnswers,
    page: QuestionPage[Address],
    address: Option[Address]
  ): Try[UserAnswers] =
    address match {
      case Some(address) =>
        userAnswers.set(page, address)
      case None =>
        Success(userAnswers)
    }

  private def nonBlank(value: Option[String]): Option[String] =
    value.map(_.trim).filter(_.nonEmpty)

  private def hasValue(value: Option[String]): Boolean =
    nonBlank(value).isDefined

  private def hasAnyContact(subcontractor: SubcontractorResponse): Boolean =
    Seq(
      subcontractor.emailAddress,
      subcontractor.phoneNumber,
      subcontractor.mobilePhoneNumber
    ).exists(hasValue)

  private def getFinalValidationSubcontractorType(
    subcontractor: SubcontractorResponse
  ): Try[TypeOfSubcontractor] =
    subcontractor.subcontractorType.flatMap(TypeOfSubcontractor.fromString) match {
      case Some(subcontractorType) =>
        Success(subcontractorType)
      case None =>
        Failure(new RuntimeException(s"Unsupported subcontractor type: ${subcontractor.subcontractorType}"))
    }

}
