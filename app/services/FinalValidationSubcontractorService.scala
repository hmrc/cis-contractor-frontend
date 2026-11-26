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

import models.{JourneyNamespace, TypeOfSubcontractor, UserAnswers}
import models.TypeOfSubcontractor.*
import models.add.{IndividualNamesOptions, SubcontractorName as AddSubcontractorName}
import models.address.{Address, Country}
import models.finalvalidation.{FinalValidationChangeTarget, FinalValidationDraftSubcontractor, FinalValidationSubcontractorDetails}
import models.finalvalidation.FinalValidationChangeTarget.{Address as AddressTarget, SubcontractorName as SubcontractorNameTarget, *}
import pages.QuestionPage
import pages.add.*
import pages.add.company.*
import pages.add.partnership.*
import pages.add.trust.*
import pages.finalvalidation.{FinalValidationBaseUtrPage, FinalValidationChangeTargetPage}
import queries.CisIdQuery

import javax.inject.{Inject, Singleton}
import scala.util.{Failure, Success, Try}

@Singleton
class FinalValidationSubcontractorService @Inject() {

  private val namespace = JourneyNamespace.SubcontractorFinalValidation

  def populateFinalValidationUserAnswers(
    userAnswers: UserAnswers,
    instanceId: String,
    subcontractor: FinalValidationDraftSubcontractor,
    changeTarget: FinalValidationChangeTarget
  ): Try[UserAnswers] =
    val details = subcontractor.proposed

    for {
      subcontractorType <- getFinalValidationSubcontractorType(subcontractor)
      withCisId         <- userAnswers.set(CisIdQuery, instanceId)
      withType          <- withCisId.set(TypeOfSubcontractorPage, subcontractorType)
      withName          <- populateFinalValidationName(withType, subcontractorType, details)
      withTarget        <- withName.set(FinalValidationChangeTargetPage, changeTarget)
      withBaseUtr       <- populateBaseUtr(withTarget, subcontractor)
      result            <- populateFinalValidationTarget(withBaseUtr, subcontractorType, details, changeTarget)
    } yield result

  private def populateBaseUtr(
    userAnswers: UserAnswers,
    subcontractor: FinalValidationDraftSubcontractor
  ): Try[UserAnswers] =
    subcontractor.base.utr match {
      case Some(utr) =>
        userAnswers.set(FinalValidationBaseUtrPage, utr)

      case None =>
        userAnswers.remove(FinalValidationBaseUtrPage)
    }

  private def populateFinalValidationName(
    userAnswers: UserAnswers,
    subcontractorType: TypeOfSubcontractor,
    details: FinalValidationSubcontractorDetails
  ): Try[UserAnswers] =
    subcontractorType match {
      case Individualorsoletrader =>
        userAnswers.set(
          SubcontractorNamePage,
          AddSubcontractorName(
            firstName = details.firstName.getOrElse(""),
            middleName = details.secondName,
            lastName = details.surname.getOrElse("")
          )
        )

      case Limitedcompany =>
        userAnswers.set(CompanyNamePage, details.tradingName.getOrElse(""))

      case Trust =>
        userAnswers.set(TrustNamePage, details.tradingName.getOrElse(""))

      case Partnership =>
        for {
          withPartnershipName <- userAnswers.set(PartnershipNamePage, details.partnershipTradingName.getOrElse(""))
          result              <- withPartnershipName.set(
                                   PartnershipNominatedPartnerNamePage,
                                   details.tradingName.getOrElse("")
                                 )
        } yield result
    }

  private def populateFinalValidationTarget(
    userAnswers: UserAnswers,
    subcontractorType: TypeOfSubcontractor,
    details: FinalValidationSubcontractorDetails,
    target: FinalValidationChangeTarget
  ): Try[UserAnswers] =
    target match {
      case Names =>
        subcontractorType match {
          case Individualorsoletrader =>
            populateIndividualNamesOptions(userAnswers, details)
          case _                      =>
            Failure(
              new RuntimeException(
                s"Names not applicable for subcontractor type: $subcontractorType"
              )
            )
        }

      case SubcontractorNameTarget | TradingName | PartnershipTradingName =>
        populateNameTarget(userAnswers, subcontractorType, details, target)

      case UtrYesNo | Utr | PartnerUtrYesNo | PartnerUtr | NinoYesNo | Nino | CrnYesNo | Crn |
          WorksReferenceNumberYesNo | WorksReferenceNumber =>
        populateIdentifierTarget(userAnswers, subcontractorType, details, target)

      case AddressYesNo | AddressTarget =>
        populateAddressTarget(userAnswers, subcontractorType, details, target)

      case ContactDetailsYesNo | EmailAddress | PhoneNumber | MobilePhoneNumber =>
        populateContactTarget(userAnswers, subcontractorType, details, target)
    }

  private def populateNameTarget(
    userAnswers: UserAnswers,
    subcontractorType: TypeOfSubcontractor,
    details: FinalValidationSubcontractorDetails,
    target: FinalValidationChangeTarget
  ): Try[UserAnswers] =
    target match {
      case SubcontractorNameTarget | PartnershipTradingName =>
        Success(userAnswers)

      case TradingName =>
        subcontractorType match {
          case Individualorsoletrader =>
            setStringOrRemove(userAnswers, TradingNameOfSubcontractorPage, details.tradingName)
          case Limitedcompany | Trust =>
            Success(userAnswers)
          case Partnership            =>
            setStringOrRemove(userAnswers, PartnershipNominatedPartnerNamePage, details.tradingName)
        }

      case _ =>
        Failure(new RuntimeException(s"Unexpected target for name population: $target"))
    }

  private def populateIdentifierTarget(
    userAnswers: UserAnswers,
    subcontractorType: TypeOfSubcontractor,
    details: FinalValidationSubcontractorDetails,
    target: FinalValidationChangeTarget
  ): Try[UserAnswers] =
    target match {
      case UtrYesNo                                            =>
        val (yesNoPage, valuePage) = utrPages(subcontractorType)
        populateStringYesNoPair(
          userAnswers = userAnswers,
          yesNoPage = yesNoPage,
          valuePage = valuePage,
          value = details.utr,
          forceYes = false
        )
      case Utr                                                 =>
        val (yesNoPage, valuePage) = utrPages(subcontractorType)
        populateStringYesNoPair(
          userAnswers = userAnswers,
          yesNoPage = yesNoPage,
          valuePage = valuePage,
          value = details.utr,
          forceYes = true
        )
      case PartnerUtrYesNo if subcontractorType == Partnership =>
        populateStringYesNoPair(
          userAnswers = userAnswers,
          yesNoPage = PartnershipNominatedPartnerUtrYesNoPage,
          valuePage = PartnershipNominatedPartnerUtrPage,
          value = details.partnerUtr,
          forceYes = false
        )
      case PartnerUtr if subcontractorType == Partnership      =>
        populateStringYesNoPair(
          userAnswers = userAnswers,
          yesNoPage = PartnershipNominatedPartnerUtrYesNoPage,
          valuePage = PartnershipNominatedPartnerUtrPage,
          value = details.partnerUtr,
          forceYes = true
        )
      case NinoYesNo                                           =>
        ninoPages(subcontractorType).flatMap { case (yesNoPage, valuePage) =>
          populateStringYesNoPair(
            userAnswers = userAnswers,
            yesNoPage = yesNoPage,
            valuePage = valuePage,
            value = details.nino,
            forceYes = false
          )
        }
      case Nino                                                =>
        ninoPages(subcontractorType).flatMap { case (yesNoPage, valuePage) =>
          populateStringYesNoPair(
            userAnswers = userAnswers,
            yesNoPage = yesNoPage,
            valuePage = valuePage,
            value = details.nino,
            forceYes = true
          )
        }
      case CrnYesNo                                            =>
        crnPages(subcontractorType).flatMap { case (yesNoPage, valuePage) =>
          populateStringYesNoPair(
            userAnswers = userAnswers,
            yesNoPage = yesNoPage,
            valuePage = valuePage,
            value = details.crn,
            forceYes = false
          )
        }
      case Crn                                                 =>
        crnPages(subcontractorType).flatMap { case (yesNoPage, valuePage) =>
          populateStringYesNoPair(
            userAnswers = userAnswers,
            yesNoPage = yesNoPage,
            valuePage = valuePage,
            value = details.crn,
            forceYes = true
          )
        }
      case WorksReferenceNumberYesNo                           =>
        val (yesNoPage, valuePage) = worksReferenceNumberPages(subcontractorType)
        populateStringYesNoPair(
          userAnswers = userAnswers,
          yesNoPage = yesNoPage,
          valuePage = valuePage,
          value = details.worksReferenceNumber,
          forceYes = false
        )
      case WorksReferenceNumber                                =>
        val (yesNoPage, valuePage) = worksReferenceNumberPages(subcontractorType)
        populateStringYesNoPair(
          userAnswers = userAnswers,
          yesNoPage = yesNoPage,
          valuePage = valuePage,
          value = details.worksReferenceNumber,
          forceYes = true
        )
      case _                                                   =>
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
    details: FinalValidationSubcontractorDetails,
    target: FinalValidationChangeTarget
  ): Try[UserAnswers] = {
    val (yesNoPage, addressPage) = addressPages(subcontractorType)
    val yesNoValue               = target match {
      case AddressYesNo  => hasAddress(details)
      case AddressTarget => true
      case _             => return Failure(new RuntimeException(s"Unexpected target for address population: $target"))
    }

    for {
      withYesNo <- userAnswers.set(yesNoPage, yesNoValue)
      result    <- setAddressOrRemove(withYesNo, addressPage, toAddress(details))
    } yield result

  }

  private def populateContactTarget(
    userAnswers: UserAnswers,
    subcontractorType: TypeOfSubcontractor,
    details: FinalValidationSubcontractorDetails,
    target: FinalValidationChangeTarget
  ): Try[UserAnswers] = {
    val (yesNoPage, emailPage, phonePage, mobilePage) = contactPages(subcontractorType)
    val yesNoValue                                    =
      target match {
        case ContactDetailsYesNo                            => hasAnyContact(details)
        case EmailAddress | PhoneNumber | MobilePhoneNumber => true
        case _                                              => return Failure(new RuntimeException(s"Unexpected target for contact population: $target"))
      }

    for {
      withYesNo <- userAnswers.set(yesNoPage, yesNoValue)
      withEmail <- setStringOrRemove(withYesNo, emailPage, details.emailAddress)
      withPhone <- setStringOrRemove(withEmail, phonePage, details.phoneNumber)
      result    <- setStringOrRemove(withPhone, mobilePage, details.mobilePhoneNumber)
    } yield result
  }

  private def utrPages(subcontractorType: TypeOfSubcontractor): (QuestionPage[Boolean], QuestionPage[String]) =
    subcontractorType match {
      case Individualorsoletrader => (UniqueTaxpayerReferenceYesNoPage, SubcontractorsUniqueTaxpayerReferencePage)
      case Limitedcompany         => (CompanyUtrYesNoPage, CompanyUtrPage)
      case Partnership            => (PartnershipHasUtrYesNoPage, PartnershipUniqueTaxpayerReferencePage)
      case Trust                  => (TrustUtrYesNoPage, TrustUtrPage)
    }

  private def ninoPages(subcontractorType: TypeOfSubcontractor): Try[(QuestionPage[Boolean], QuestionPage[String])] =
    subcontractorType match {
      case Individualorsoletrader => Success((NationalInsuranceNumberYesNoPage, SubNationalInsuranceNumberPage))
      case Partnership            => Success((PartnershipNominatedPartnerNinoYesNoPage, PartnershipNominatedPartnerNinoPage))
      case other                  => Failure(new RuntimeException(s"NINO not applicable for subocntactor type: $other"))
    }

  private def crnPages(subcontractorType: TypeOfSubcontractor): Try[(QuestionPage[Boolean], QuestionPage[String])] =
    subcontractorType match {
      case Limitedcompany => Success((CompanyCrnYesNoPage, CompanyCrnPage))
      case Partnership    => Success((PartnershipNominatedPartnerCrnYesNoPage, PartnershipNominatedPartnerCrnPage))
      case other          => Failure(new RuntimeException(s"CRN not applicable for subcontractor type: $other"))
    }

  private def worksReferenceNumberPages(
    subcontractorType: TypeOfSubcontractor
  ): (QuestionPage[Boolean], QuestionPage[String]) =
    subcontractorType match {
      case Individualorsoletrader => (WorksReferenceNumberYesNoPage, WorksReferenceNumberPage)
      case Limitedcompany         => (CompanyWorksReferenceYesNoPage, CompanyWorksReferencePage)
      case Partnership            => (PartnershipWorksReferenceNumberYesNoPage, PartnershipWorksReferenceNumberPage)
      case Trust                  => (TrustWorksReferenceYesNoPage, TrustWorksReferencePage)
    }

  private def addressPages(
    subcontractorType: TypeOfSubcontractor
  ): (QuestionPage[Boolean], QuestionPage[models.address.Address]) =
    subcontractorType match {
      case Individualorsoletrader => (SubAddressYesNoPage, AddressOfSubcontractorPage)
      case Limitedcompany         => (CompanyAddressYesNoPage, CompanyAddressPage)
      case Partnership            => (PartnershipAddressYesNoPage, PartnershipAddressPage)
      case Trust                  => (TrustAddressYesNoPage, TrustAddressPage)
    }

  private def contactPages(subcontractorType: TypeOfSubcontractor): (
    QuestionPage[Boolean],
    QuestionPage[String],
    QuestionPage[String],
    QuestionPage[String]
  ) =
    subcontractorType match {
      case Individualorsoletrader =>
        (
          AddIndividualContactMethodsYesNoPage,
          IndividualEmailAddressPage,
          IndividualPhoneNumberPage,
          IndividualMobileNumberPage
        )
      case Limitedcompany         =>
        (AddCompanyContactMethodsYesNoPage, CompanyEmailAddressPage, CompanyPhoneNumberPage, CompanyMobileNumberPage)
      case Partnership            =>
        (
          AddPartnershipContactMethodsYesNoPage,
          PartnershipEmailAddressPage,
          PartnershipPhoneNumberPage,
          PartnershipMobileNumberPage
        )
      case Trust                  =>
        (AddTrustContactMethodsYesNoPage, TrustEmailAddressPage, TrustPhoneNumberPage, TrustMobileNumberPage)
    }

  private def hasAddress(details: FinalValidationSubcontractorDetails): Boolean =
    hasValue(details.addressLine1)

  private def toAddress(
    details: FinalValidationSubcontractorDetails
  ): Option[Address] =
    if (!hasAddress(details)) {
      None
    } else {
      Some(
        Address(
          addressLine1 = nonBlank(details.addressLine1).getOrElse(""),
          addressLine2 = nonBlank(details.addressLine2),
          addressLine3 = nonBlank(details.addressLine3),
          addressLine4 = nonBlank(details.addressLine4),
          postcode = nonBlank(details.postcode),
          country = nonBlank(details.country).map { name =>
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
      case None         =>
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
      case None          =>
        userAnswers.remove(page)
    }

  private def nonBlank(value: Option[String]): Option[String] =
    value.map(_.trim).filter(_.nonEmpty)

  private def hasValue(value: Option[String]): Boolean =
    nonBlank(value).isDefined

  private def hasAnyContact(details: FinalValidationSubcontractorDetails): Boolean =
    Seq(
      details.emailAddress,
      details.phoneNumber,
      details.mobilePhoneNumber
    ).exists(hasValue)

  private def getFinalValidationSubcontractorType(
    subcontractor: FinalValidationDraftSubcontractor
  ): Try[TypeOfSubcontractor] =
    subcontractor.subcontractorType.flatMap(TypeOfSubcontractor.fromString) match {
      case Some(subcontractorType) =>
        Success(subcontractorType)
      case None                    =>
        Failure(new RuntimeException(s"Unsupported subcontractor type: ${subcontractor.subcontractorType}"))
    }

  private def populateIndividualNamesOptions(
    userAnswers: UserAnswers,
    details: FinalValidationSubcontractorDetails
  ): Try[UserAnswers] = {

    val hasSubcontractorName =
      Seq(
        details.firstName,
        details.secondName,
        details.surname
      ).flatten
        .exists(_.trim.nonEmpty)

    val hasTradingName =
      details.tradingName
        .exists(_.trim.nonEmpty)

    val selected =
      Set.empty[IndividualNamesOptions] ++
        Option.when(hasSubcontractorName)(
          IndividualNamesOptions.SubcontractorName
        ) ++
        Option.when(hasTradingName)(
          IndividualNamesOptions.TradingName
        )

    for {
      withNames <- userAnswers.set(
                     IndividualNamesOptionsPage,
                     selected
                   )
      result    <- setStringOrRemove(
                     withNames,
                     TradingNameOfSubcontractorPage,
                     details.tradingName
                   )
    } yield result
  }

  // Multi-tab
  def populateFinalValidationUserAnswers(
    userAnswers: UserAnswers,
    instanceId: String,
    subcontractor: FinalValidationDraftSubcontractor,
    changeTarget: FinalValidationChangeTarget,
    id: String
  ): Try[UserAnswers] = {
    val details = subcontractor.proposed

    for {
      subcontractorType <- getFinalValidationSubcontractorType(subcontractor)
      withCisId         <- userAnswers.set(CisIdQuery(id, namespace), instanceId)
      withType          <- withCisId.set(TypeOfSubcontractorPage(id, namespace), subcontractorType)
      withName          <- populateFinalValidationName(withType, subcontractorType, details, id)
      withTarget        <- withName.set(FinalValidationChangeTargetPage(id), changeTarget)
      withBaseUtr       <- populateBaseUtr(withTarget, subcontractor, id)
      result            <- populateFinalValidationTarget(
                             withBaseUtr,
                             subcontractorType,
                             details,
                             changeTarget,
                             id
                           )
    } yield result
  }

  private def populateBaseUtr(
    userAnswers: UserAnswers,
    subcontractor: FinalValidationDraftSubcontractor,
    id: String
  ): Try[UserAnswers] =
    subcontractor.base.utr match {
      case Some(utr) => userAnswers.set(FinalValidationBaseUtrPage(id), utr)
      case None      => userAnswers.remove(FinalValidationBaseUtrPage(id))
    }

  private def populateFinalValidationName(
    userAnswers: UserAnswers,
    subcontractorType: TypeOfSubcontractor,
    details: FinalValidationSubcontractorDetails,
    id: String
  ): Try[UserAnswers] =
    subcontractorType match {
      case Individualorsoletrader =>
        userAnswers.set(
          SubcontractorNamePage(id, namespace),
          AddSubcontractorName(
            firstName = details.firstName.getOrElse(""),
            middleName = details.secondName,
            lastName = details.surname.getOrElse("")
          )
        )

      case Limitedcompany =>
        userAnswers.set(CompanyNamePage(id, namespace), details.tradingName.getOrElse(""))

      case Trust =>
        userAnswers.set(TrustNamePage(id, namespace), details.tradingName.getOrElse(""))

      case Partnership =>
        for {
          withPartnershipName <- userAnswers.set(
                                   PartnershipNamePage(id, namespace),
                                   details.partnershipTradingName.getOrElse("")
                                 )
          result              <- withPartnershipName.set(
                                   PartnershipNominatedPartnerNamePage(id, namespace),
                                   details.tradingName.getOrElse("")
                                 )
        } yield result
    }

  private def populateFinalValidationTarget(
    userAnswers: UserAnswers,
    subcontractorType: TypeOfSubcontractor,
    details: FinalValidationSubcontractorDetails,
    target: FinalValidationChangeTarget,
    id: String
  ): Try[UserAnswers] =
    target match {
      case Names =>
        subcontractorType match {
          case Individualorsoletrader =>
            populateIndividualNamesOptions(userAnswers, details, id)

          case _ =>
            Failure(new RuntimeException(s"Names not applicable for subcontractor type: $subcontractorType"))
        }

      case SubcontractorNameTarget | TradingName | PartnershipTradingName =>
        populateNameTarget(userAnswers, subcontractorType, details, target, id)

      case UtrYesNo | Utr | PartnerUtrYesNo | PartnerUtr | NinoYesNo | Nino | CrnYesNo | Crn |
          WorksReferenceNumberYesNo | WorksReferenceNumber =>
        populateIdentifierTarget(userAnswers, subcontractorType, details, target, id)

      case AddressYesNo | AddressTarget =>
        populateAddressTarget(userAnswers, subcontractorType, details, target, id)

      case ContactDetailsYesNo | EmailAddress | PhoneNumber | MobilePhoneNumber =>
        populateContactTarget(userAnswers, subcontractorType, details, target, id)
    }

  private def populateNameTarget(
    userAnswers: UserAnswers,
    subcontractorType: TypeOfSubcontractor,
    details: FinalValidationSubcontractorDetails,
    target: FinalValidationChangeTarget,
    id: String
  ): Try[UserAnswers] =
    target match {
      case SubcontractorNameTarget | PartnershipTradingName =>
        Success(userAnswers)

      case TradingName =>
        subcontractorType match {
          case Individualorsoletrader =>
            setStringOrRemove(
              userAnswers,
              TradingNameOfSubcontractorPage(id, namespace),
              details.tradingName
            )

          case Limitedcompany | Trust =>
            Success(userAnswers)

          case Partnership =>
            setStringOrRemove(
              userAnswers,
              PartnershipNominatedPartnerNamePage(id, namespace),
              details.tradingName
            )
        }

      case _ =>
        Failure(new RuntimeException(s"Unexpected target for name population: $target"))
    }

  private def populateIdentifierTarget(
    userAnswers: UserAnswers,
    subcontractorType: TypeOfSubcontractor,
    details: FinalValidationSubcontractorDetails,
    target: FinalValidationChangeTarget,
    id: String
  ): Try[UserAnswers] =
    target match {
      case UtrYesNo | Utr =>
        val (yesNoPage, valuePage) = utrPages(subcontractorType, id)

        populateStringYesNoPair(
          userAnswers = userAnswers,
          yesNoPage = yesNoPage,
          valuePage = valuePage,
          value = details.utr,
          forceYes = target == Utr
        )

      case PartnerUtrYesNo | PartnerUtr if subcontractorType == Partnership =>
        populateStringYesNoPair(
          userAnswers = userAnswers,
          yesNoPage = PartnershipNominatedPartnerUtrYesNoPage(id, namespace),
          valuePage = PartnershipNominatedPartnerUtrPage(id, namespace),
          value = details.partnerUtr,
          forceYes = target == PartnerUtr
        )

      case NinoYesNo | Nino =>
        ninoPages(subcontractorType, id).flatMap { case (yesNoPage, valuePage) =>
          populateStringYesNoPair(
            userAnswers = userAnswers,
            yesNoPage = yesNoPage,
            valuePage = valuePage,
            value = details.nino,
            forceYes = target == Nino
          )
        }

      case CrnYesNo | Crn =>
        crnPages(subcontractorType, id).flatMap { case (yesNoPage, valuePage) =>
          populateStringYesNoPair(
            userAnswers = userAnswers,
            yesNoPage = yesNoPage,
            valuePage = valuePage,
            value = details.crn,
            forceYes = target == Crn
          )
        }

      case WorksReferenceNumberYesNo | WorksReferenceNumber =>
        val (yesNoPage, valuePage) = worksReferenceNumberPages(subcontractorType, id)

        populateStringYesNoPair(
          userAnswers = userAnswers,
          yesNoPage = yesNoPage,
          valuePage = valuePage,
          value = details.worksReferenceNumber,
          forceYes = target == WorksReferenceNumber
        )

      case _ =>
        Failure(new RuntimeException(s"Unexpected target for identifier population: $target"))
    }

  private def populateAddressTarget(
    userAnswers: UserAnswers,
    subcontractorType: TypeOfSubcontractor,
    details: FinalValidationSubcontractorDetails,
    target: FinalValidationChangeTarget,
    id: String
  ): Try[UserAnswers] = {
    val (yesNoPage, addressPage) = addressPages(subcontractorType, id)

    val yesNoValue = target match {
      case AddressYesNo  => hasAddress(details)
      case AddressTarget => true
      case _             => return Failure(new RuntimeException(s"Unexpected target for address population: $target"))
    }

    for {
      withYesNo <- userAnswers.set(yesNoPage, yesNoValue)
      result    <- setAddressOrRemove(withYesNo, addressPage, toAddress(details))
    } yield result
  }

  private def populateContactTarget(
    userAnswers: UserAnswers,
    subcontractorType: TypeOfSubcontractor,
    details: FinalValidationSubcontractorDetails,
    target: FinalValidationChangeTarget,
    id: String
  ): Try[UserAnswers] = {
    val (yesNoPage, emailPage, phonePage, mobilePage) =
      contactPages(subcontractorType, id)

    val yesNoValue = target match {
      case ContactDetailsYesNo =>
        hasAnyContact(details)

      case EmailAddress | PhoneNumber | MobilePhoneNumber =>
        true

      case _ =>
        return Failure(new RuntimeException(s"Unexpected target for contact population: $target"))
    }

    for {
      withYesNo <- userAnswers.set(yesNoPage, yesNoValue)
      withEmail <- setStringOrRemove(withYesNo, emailPage, details.emailAddress)
      withPhone <- setStringOrRemove(withEmail, phonePage, details.phoneNumber)
      result    <- setStringOrRemove(withPhone, mobilePage, details.mobilePhoneNumber)
    } yield result
  }

  private def utrPages(
    subcontractorType: TypeOfSubcontractor,
    id: String
  ): (QuestionPage[Boolean], QuestionPage[String]) =
    subcontractorType match {
      case Individualorsoletrader =>
        (UniqueTaxpayerReferenceYesNoPage(id, namespace), SubcontractorsUniqueTaxpayerReferencePage(id, namespace))

      case Limitedcompany =>
        (CompanyUtrYesNoPage(id, namespace), CompanyUtrPage(id, namespace))

      case Partnership =>
        (PartnershipHasUtrYesNoPage(id, namespace), PartnershipUniqueTaxpayerReferencePage(id, namespace))

      case Trust =>
        (TrustUtrYesNoPage(id, namespace), TrustUtrPage(id, namespace))
    }

  private def ninoPages(
    subcontractorType: TypeOfSubcontractor,
    id: String
  ): Try[(QuestionPage[Boolean], QuestionPage[String])] =
    subcontractorType match {
      case Individualorsoletrader =>
        Success(
          (
            NationalInsuranceNumberYesNoPage(id, namespace),
            SubNationalInsuranceNumberPage(id, namespace)
          )
        )

      case Partnership =>
        Success(
          (
            PartnershipNominatedPartnerNinoYesNoPage(id, namespace),
            PartnershipNominatedPartnerNinoPage(id, namespace)
          )
        )

      case other =>
        Failure(new RuntimeException(s"NINO not applicable for subcontractor type: $other"))
    }

  private def crnPages(
    subcontractorType: TypeOfSubcontractor,
    id: String
  ): Try[(QuestionPage[Boolean], QuestionPage[String])] =
    subcontractorType match {
      case Limitedcompany =>
        Success(
          (
            CompanyCrnYesNoPage(id, namespace),
            CompanyCrnPage(id, namespace)
          )
        )

      case Partnership =>
        Success(
          (
            PartnershipNominatedPartnerCrnYesNoPage(id, namespace),
            PartnershipNominatedPartnerCrnPage(id, namespace)
          )
        )

      case other =>
        Failure(new RuntimeException(s"CRN not applicable for subcontractor type: $other"))
    }

  private def worksReferenceNumberPages(
    subcontractorType: TypeOfSubcontractor,
    id: String
  ): (QuestionPage[Boolean], QuestionPage[String]) =
    subcontractorType match {
      case Individualorsoletrader =>
        (
          WorksReferenceNumberYesNoPage(id, namespace),
          WorksReferenceNumberPage(id, namespace)
        )

      case Limitedcompany =>
        (
          CompanyWorksReferenceYesNoPage(id, namespace),
          CompanyWorksReferencePage(id, namespace)
        )

      case Partnership =>
        (
          PartnershipWorksReferenceNumberYesNoPage(id, namespace),
          PartnershipWorksReferenceNumberPage(id, namespace)
        )

      case Trust =>
        (
          TrustWorksReferenceYesNoPage(id, namespace),
          TrustWorksReferencePage(id, namespace)
        )
    }

  private def addressPages(
    subcontractorType: TypeOfSubcontractor,
    id: String
  ): (QuestionPage[Boolean], QuestionPage[Address]) =
    subcontractorType match {
      case Individualorsoletrader =>
        (
          SubAddressYesNoPage(id, namespace),
          AddressOfSubcontractorPage(id, namespace)
        )

      case Limitedcompany =>
        (
          CompanyAddressYesNoPage(id, namespace),
          CompanyAddressPage(id, namespace)
        )

      case Partnership =>
        (
          PartnershipAddressYesNoPage(id, namespace),
          PartnershipAddressPage(id, namespace)
        )

      case Trust =>
        (
          TrustAddressYesNoPage(id, namespace),
          TrustAddressPage(id, namespace)
        )
    }

  private def contactPages(
    subcontractorType: TypeOfSubcontractor,
    id: String
  ): (QuestionPage[Boolean], QuestionPage[String], QuestionPage[String], QuestionPage[String]) =
    subcontractorType match {
      case Individualorsoletrader =>
        (
          AddIndividualContactMethodsYesNoPage(id, namespace),
          IndividualEmailAddressPage(id, namespace),
          IndividualPhoneNumberPage(id, namespace),
          IndividualMobileNumberPage(id, namespace)
        )

      case Limitedcompany =>
        (
          AddCompanyContactMethodsYesNoPage(id, namespace),
          CompanyEmailAddressPage(id, namespace),
          CompanyPhoneNumberPage(id, namespace),
          CompanyMobileNumberPage(id, namespace)
        )

      case Partnership =>
        (
          AddPartnershipContactMethodsYesNoPage(id, namespace),
          PartnershipEmailAddressPage(id, namespace),
          PartnershipPhoneNumberPage(id, namespace),
          PartnershipMobileNumberPage(id, namespace)
        )

      case Trust =>
        (
          AddTrustContactMethodsYesNoPage(id, namespace),
          TrustEmailAddressPage(id, namespace),
          TrustPhoneNumberPage(id, namespace),
          TrustMobileNumberPage(id, namespace)
        )
    }

  private def populateIndividualNamesOptions(
    userAnswers: UserAnswers,
    details: FinalValidationSubcontractorDetails,
    id: String
  ): Try[UserAnswers] = {

    val hasSubcontractorName =
      Seq(
        details.firstName,
        details.secondName,
        details.surname
      ).flatten
        .exists(_.trim.nonEmpty)

    val hasTradingName =
      details.tradingName
        .exists(_.trim.nonEmpty)

    val selected =
      Set.empty[IndividualNamesOptions] ++
        Option.when(hasSubcontractorName)(
          IndividualNamesOptions.SubcontractorName
        ) ++
        Option.when(hasTradingName)(
          IndividualNamesOptions.TradingName
        )

    for {
      withNames <- userAnswers.set(
                     IndividualNamesOptionsPage(id, namespace),
                     selected
                   )
      result    <- setStringOrRemove(
                     withNames,
                     TradingNameOfSubcontractorPage(id, namespace),
                     details.tradingName
                   )
    } yield result
  }
}
