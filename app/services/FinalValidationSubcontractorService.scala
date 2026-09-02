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
import pages.finalvalidation.FinalValidationChangeTargetPage
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
      subcontractor     <- getFinalValidationSubcontractor(response)
      subcontractorType <- getFinalValidationSubcontractorType(subcontractor)
      withCisId         <- userAnswers.set(CisIdQuery, instanceId)
      withType          <- withCisId.set(TypeOfSubcontractorPage, subcontractorType)
      withName          <- populateFinalValidationName(withType, subcontractorType, subcontractor)
      withTarget        <- withName.set(FinalValidationChangeTargetPage, changeTarget)
      result            <- populateFinalValidationTarget(withTarget, subcontractorType, subcontractor, changeTarget)
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
            setStringOrRemove(userAnswers, TradingNameOfSubcontractorPage, subcontractor.tradingName)
          case Limitedcompany | Trust =>
            Success(userAnswers)
          case Partnership =>
            setStringOrRemove(userAnswers, PartnershipNominatedPartnerNamePage, subcontractor.tradingName)
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
        val (yesNoPage, valuePage) = utrPages(subcontractorType)
        populateStringYesNoPair(
          userAnswers = userAnswers,
          yesNoPage = yesNoPage,
          valuePage = valuePage,
          value = subcontractor.utr,
          forceYes = false
        )
      case Utr =>
        val (yesNoPage, valuePage) = utrPages(subcontractorType)
        populateStringYesNoPair(
          userAnswers = userAnswers,
          yesNoPage = yesNoPage,
          valuePage = valuePage,
          value = subcontractor.utr,
          forceYes = true
        )
      case PartnerUtrYesNo if subcontractorType == Partnership =>
        populateStringYesNoPair(
          userAnswers = userAnswers,
          yesNoPage = PartnershipNominatedPartnerUtrYesNoPage,
          valuePage = PartnershipNominatedPartnerUtrPage,
          value = subcontractor.partnerUtr,
          forceYes = true
        )
      case PartnerUtr if subcontractorType == Partnership =>
        populateStringYesNoPair(
          userAnswers = userAnswers,
          yesNoPage = PartnershipNominatedPartnerUtrYesNoPage,
          valuePage = PartnershipNominatedPartnerUtrPage,
          value = subcontractor.partnerUtr,
          forceYes = false
        )
      case NinoYesNo =>
        ninoPages(subcontractorType).flatMap { case (yesNoPage, valuePage) =>
          populateStringYesNoPair(
            userAnswers = userAnswers,
            yesNoPage = yesNoPage,
            valuePage = valuePage,
            value = subcontractor.nino,
            forceYes = false
          )
        }
      case Nino =>
        ninoPages(subcontractorType).flatMap { case (yesNoPage, valuePage) =>
          populateStringYesNoPair(
            userAnswers = userAnswers,
            yesNoPage = yesNoPage,
            valuePage = valuePage,
            value = subcontractor.nino,
            forceYes = true
          )
        }
      case CrnYesNo =>
        crnPages(subcontractorType).flatMap { case (yesNoPage, valuePage) =>
          populateStringYesNoPair(
            userAnswers = userAnswers,
            yesNoPage = yesNoPage,
            valuePage = valuePage,
            value = subcontractor.crn,
            forceYes = false
          )
        }
      case Crn =>
        crnPages(subcontractorType).flatMap { case (yesNoPage, valuePage) =>
          populateStringYesNoPair(
            userAnswers = userAnswers,
            yesNoPage = yesNoPage,
            valuePage = valuePage,
            value = subcontractor.crn,
            forceYes = true
          )
        }
      case WorksReferenceNumberYesNo =>
        val (yesNoPage, valuePage) = worksReferenceNumberPages(subcontractorType)
        populateStringYesNoPair(
          userAnswers = userAnswers,
          yesNoPage = yesNoPage,
          valuePage = valuePage,
          value = subcontractor.worksReferenceNumber,
          forceYes = false
        )
      case WorksReferenceNumber =>
        val (yesNoPage, valuePage) = worksReferenceNumberPages(subcontractorType)
        populateStringYesNoPair(
          userAnswers = userAnswers,
          yesNoPage = yesNoPage,
          valuePage = valuePage,
          value = subcontractor.worksReferenceNumber,
          forceYes = true
        )
      case _ =>
        Failure(new RuntimeException(s"Unexpected target for identifier population: $target"))
    }

  private def populateStringYesNoPair(
    userAnswers: UserAnswers,
    yesNoPage: QuestionPage[Boolean],
    valuePage: QuestionPage[String],
    value: Option[String],
    forceYes: Boolean
  ): Try[UserAnswers] =
    for {
      withYesNo <- userAnswers.set(yesNoPage, if (forceYes) true else hasValue(value))
      result    <- setStringOrRemove(withYesNo, valuePage, value)
    } yield result
  
  private def populateAddressTarget(
    userAnswers: UserAnswers,
    subcontractorType: TypeOfSubcontractor,
    subcontractor: SubcontractorResponse,
    target: FinalValidationChangeTarget
  ): Try[UserAnswers] = {
    val (yesNoPage, addressPage) = addressPages(subcontractorType)
    val yesNoValue = target match {
      case AddressYesNo  => hasAddress(subcontractor)
      case AddressTarget => true
      case _             => return Failure(new RuntimeException(s"Unexpected target for address population: $target"))
    }
    
    for {
      withYesNo <- userAnswers.set(yesNoPage, yesNoValue)
      result    <- setAddressOrRemove(withYesNo, addressPage, toAddress(subcontractor))
    } yield result
    
  }

  private def populateContactTarget(
    userAnswers: UserAnswers,
    subcontractorType: TypeOfSubcontractor,
    subcontractor: SubcontractorResponse,
    target: FinalValidationChangeTarget
  ): Try[UserAnswers] = {
    val (yesNoPage, emailPage, phonePage, mobilePage) = contactPages(subcontractorType)
    val yesNoValue =
      target match {
        case ContactDetailsYesNo => hasAnyContact(subcontractor)
        case EmailAddress | PhoneNumber | MobilePhoneNumber => true
        case _ => return Failure(new RuntimeException(s"Unexpected target for contact population: $target"))
      }
      
    for {
      withYesNo <- userAnswers.set(yesNoPage, yesNoValue)
      withEmail <- setStringOrRemove(withYesNo, emailPage, subcontractor.emailAddress)
      withPhone <- setStringOrRemove(withEmail, phonePage, subcontractor.phoneNumber)
      result    <- setStringOrRemove(withPhone, mobilePage, subcontractor.mobilePhoneNumber)
    } yield result
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

  private def setStringOrRemove(
    userAnswers: UserAnswers,
    page: QuestionPage[String],
    value: Option[String]
  ): Try[UserAnswers] =
    nonBlank(value) match {
      case Some(answer) =>
        userAnswers.set(page, answer)
      case None =>
        userAnswers.remove(page)
    }

  private def setAddressOrRemove(
    userAnswers: UserAnswers,
    page: QuestionPage[Address],
    address: Option[Address]
  ): Try[UserAnswers] =
    address match {
      case Some(address) =>
        userAnswers.set(page, address)
      case None =>
        userAnswers.remove(page)
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
