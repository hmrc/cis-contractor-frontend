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

package controllers.helpers

import controllers.amend.AmendControllerUtils.{setOptional, shouldShowVerificationDetails}
import models.TypeOfSubcontractor.{Individualorsoletrader, Limitedcompany, Partnership, Trust}
import models.UserAnswers
import models.add.{IndividualContactMethodOptions, IndividualNamesOptions, SubcontractorName}
import models.address.Address
import models.amend.OriginalIndividualAnswers
import models.amend.company.OriginalCompanyAnswers
import models.amend.partnership.OriginalPartnershipAnswers
import models.amend.trust.OriginalTrustAnswers
import models.contact.ContactMethodOptions
import models.response.SubcontractorResponse
import pages.add.*
import pages.add.company.*
import pages.add.partnership.*
import pages.add.trust.*
import pages.amend.ShowVerificationDetailsPage
import play.api.libs.json.Writes
import queries.*

import scala.util.Try

object AmendSubcontractorPopulator {

  object IndividualPopulator {
    def populate(
      userAnswers: UserAnswers,
      cisId: String,
      subcontractor: SubcontractorResponse,
      subJourneyId: Option[String] = None
    ): Try[UserAnswers] = {
      val address    = SubcontractorPopulatorUtils.toAddress(subcontractor)
      val methods    = SubcontractorPopulatorUtils.contactMethods(subcontractor)
      val hasAddress = SubcontractorPopulatorUtils.addressFieldsExist(subcontractor)

      val name = SubcontractorPopulatorUtils.individualName(subcontractor)

      val hasName: Boolean                                    = name.isDefined
      val hasTradingName: Boolean                             = SubcontractorPopulatorUtils.hasTradingName(subcontractor)
      val individualNamesOptions: Set[IndividualNamesOptions] =
        SubcontractorPopulatorUtils.individualNamesOptions(hasName, hasTradingName)

      val original = originalAnswers(
        subcontractor = subcontractor,
        address = address,
        methods = methods,
        individualNamesOptions = individualNamesOptions,
        name = name
      )

      subJourneyId match {
        case Some(id) =>
          for {
            updated <- userAnswers.set(
                         TypeOfSubcontractorPage(id),
                         Individualorsoletrader
                       ) // TODO DTR-9463: Do I need to update page with amend path?
            updated <- updated.set(IndividualNamesOptionsPage(id), individualNamesOptions)
            updated <- setOptional(updated, TradingNameOfSubcontractorPage(id), subcontractor.tradingName)
            updated <- setOptional(updated, SubcontractorNamePage(id), name)
            updated <- updated.set(SubAddressYesNoPage(id), hasAddress)
            updated <- setOptional(updated, AddressOfSubcontractorPage(id), address)
            updated <- updated.set(AddIndividualContactMethodsYesNoPage(id), methods.nonEmpty)
            updated <- if (methods.nonEmpty) {
                         updated.set(IndividualContactMethodOptionsPage(id), methods)
                       } else {
                         Try(updated)
                       }
            updated <- setOptional(updated, IndividualEmailAddressPage(id), subcontractor.emailAddress)
            updated <- setOptional(updated, IndividualPhoneNumberPage(id), subcontractor.phoneNumber)
            updated <- setOptional(updated, IndividualMobileNumberPage(id), subcontractor.mobilePhoneNumber)
            updated <- updated.set(
                         OriginalSubcontractorQuery,
                         subcontractor
                       ) // TODO DTR-9463: Do I need to update query with id?
            updated <- setOptionalQuery(
                         updated,
                         AmendSubbieResourceRefQuery,
                         subcontractor.subbieResourceRef
                       ) // TODO DTR-9463: Do I need to update query with id?
            updated <- updated.set(UniqueTaxpayerReferenceYesNoPage(id), subcontractor.utr.isDefined)
            updated <- setOptional(updated, SubcontractorsUniqueTaxpayerReferencePage(id), subcontractor.utr)
            updated <- updated.set(NationalInsuranceNumberYesNoPage(id), subcontractor.nino.isDefined)
            updated <- setOptional(updated, SubNationalInsuranceNumberPage(id), subcontractor.nino)
            updated <- updated.set(WorksReferenceNumberYesNoPage(id), subcontractor.worksReferenceNumber.isDefined)
            updated <- setOptional(updated, WorksReferenceNumberPage(id), subcontractor.worksReferenceNumber)
            updated <- updated.set(ShowVerificationDetailsPage(id), shouldShowVerificationDetails(subcontractor))
            updated <- updated.set(CisIdQuery(id), cisId) // TODO DTR-9463: Do I need to update query with amend path?
            updated <-
              updated.set(OriginalIndividualAnswersQuery, original) // TODO DTR-9463: Do I need to update query with id?
          } yield updated
        case None     =>
          for {
            updated <- userAnswers.set(TypeOfSubcontractorPage, Individualorsoletrader)
            updated <- updated.set(IndividualNamesOptionsPage, individualNamesOptions)
            updated <- setOptional(updated, TradingNameOfSubcontractorPage, subcontractor.tradingName)
            updated <- setOptional(updated, SubcontractorNamePage, name)
            updated <- updated.set(SubAddressYesNoPage, hasAddress)
            updated <- setOptional(updated, AddressOfSubcontractorPage, address)
            updated <- updated.set(AddIndividualContactMethodsYesNoPage, methods.nonEmpty)
            updated <- if (methods.nonEmpty) {
                         updated.set(IndividualContactMethodOptionsPage, methods)
                       } else {
                         Try(updated)
                       }
            updated <- setOptional(updated, IndividualEmailAddressPage, subcontractor.emailAddress)
            updated <- setOptional(updated, IndividualPhoneNumberPage, subcontractor.phoneNumber)
            updated <- setOptional(updated, IndividualMobileNumberPage, subcontractor.mobilePhoneNumber)
            updated <- updated.set(OriginalSubcontractorQuery, subcontractor)
            updated <- setOptionalQuery(updated, AmendSubbieResourceRefQuery, subcontractor.subbieResourceRef)
            updated <- updated.set(UniqueTaxpayerReferenceYesNoPage, subcontractor.utr.isDefined)
            updated <- setOptional(updated, SubcontractorsUniqueTaxpayerReferencePage, subcontractor.utr)
            updated <- updated.set(NationalInsuranceNumberYesNoPage, subcontractor.nino.isDefined)
            updated <- setOptional(updated, SubNationalInsuranceNumberPage, subcontractor.nino)
            updated <- updated.set(WorksReferenceNumberYesNoPage, subcontractor.worksReferenceNumber.isDefined)
            updated <- setOptional(updated, WorksReferenceNumberPage, subcontractor.worksReferenceNumber)
            updated <- updated.set(ShowVerificationDetailsPage, shouldShowVerificationDetails(subcontractor))
            updated <- updated.set(CisIdQuery, cisId)
            updated <- updated.set(OriginalIndividualAnswersQuery, original)
          } yield updated
      }
    }

    private def originalAnswers(
      subcontractor: SubcontractorResponse,
      address: Option[Address],
      methods: Set[IndividualContactMethodOptions],
      individualNamesOptions: Set[IndividualNamesOptions],
      name: Option[SubcontractorName]
    ): OriginalIndividualAnswers =
      OriginalIndividualAnswers(
        individualNamesOptions = individualNamesOptions,
        tradingName = subcontractor.tradingName,
        subcontractorName = name,
        addressYesNo = Some(SubcontractorPopulatorUtils.addressFieldsExist(subcontractor)),
        address = address,
        individualContactMethodsYesNo = Some(methods.nonEmpty),
        individualContactMethod = methods,
        email = subcontractor.emailAddress,
        phone = subcontractor.phoneNumber,
        mobile = subcontractor.mobilePhoneNumber,
        utrYesNo = Some(subcontractor.utr.isDefined),
        utr = subcontractor.utr,
        ninoYesNo = Some(subcontractor.nino.isDefined),
        nino = subcontractor.nino,
        worksReferenceYesNo = Some(subcontractor.worksReferenceNumber.isDefined),
        worksReference = subcontractor.worksReferenceNumber,
        verificationNumber = subcontractor.verificationNumber
      )
  }

  object CompanyPopulator {
    def populate(
      userAnswers: UserAnswers,
      cisId: String,
      subcontractor: SubcontractorResponse,
      subJourneyId: Option[String] = None
    ): Try[UserAnswers] = {

      val address    = SubcontractorPopulatorUtils.toAddress(subcontractor)
      val methods    = SubcontractorPopulatorUtils.contactMethods(subcontractor)
      val hasAddress = SubcontractorPopulatorUtils.addressFieldsExist(subcontractor)

      val original =
        originalAnswers(
          subcontractor = subcontractor,
          address = address,
          methods = methods
        )
      subJourneyId match {
        case Some(id) =>
          for {
            updated <- userAnswers.set(TypeOfSubcontractorPage(id), Limitedcompany)
            updated <- setOptional(updated, CompanyNamePage(id), subcontractor.tradingName)
            updated <- updated.set(CompanyAddressYesNoPage(id), hasAddress)
            updated <- setOptional(updated, CompanyAddressPage(id), address)
            updated <- updated.set(AddCompanyContactMethodsYesNoPage(id), methods.nonEmpty)
            updated <- if (methods.nonEmpty) updated.set(CompanyContactMethodOptionsPage(id), methods) else Try(updated)
            updated <- setOptional(updated, CompanyEmailAddressPage(id), subcontractor.emailAddress)
            updated <- setOptional(updated, CompanyPhoneNumberPage(id), subcontractor.phoneNumber)
            updated <- setOptional(updated, CompanyMobileNumberPage(id), subcontractor.mobilePhoneNumber)
            updated <- updated.set(CompanyUtrYesNoPage(id), subcontractor.utr.isDefined)
            updated <- updated.set(
                         OriginalSubcontractorQuery,
                         subcontractor
                       ) // TODO DTR-9463: Do I need to update query with id?
            updated <- setOptionalQuery(
                         updated,
                         AmendSubbieResourceRefQuery,
                         subcontractor.subbieResourceRef
                       ) // TODO DTR-9463: Do I need to update query with id?
            updated <- setOptional(updated, CompanyUtrPage(id), subcontractor.utr)
            updated <- updated.set(CompanyCrnYesNoPage(id), subcontractor.crn.isDefined)
            updated <- setOptional(updated, CompanyCrnPage(id), subcontractor.crn)
            updated <- updated.set(
                         CompanyWorksReferenceYesNoPage(id),
                         subcontractor.worksReferenceNumber.isDefined
                       )
            updated <- setOptional(
                         updated,
                         CompanyWorksReferencePage(id),
                         subcontractor.worksReferenceNumber
                       )
            updated <- updated.set(
                         ShowVerificationDetailsPage(id),
                         shouldShowVerificationDetails(subcontractor)
                       )
            updated <- updated.set(CisIdQuery(id), cisId) // TODO DTR-9463: Do I need to update query amend?
            updated <-
              updated.set(OriginalCompanyAnswersQuery, original) // TODO DTR-9463: Do I need to update query with id?
          } yield updated
        case None     =>
          for {
            updated <- userAnswers.set(TypeOfSubcontractorPage, Limitedcompany)
            updated <- setOptional(updated, CompanyNamePage, subcontractor.tradingName)
            updated <- updated.set(CompanyAddressYesNoPage, hasAddress)
            updated <- setOptional(updated, CompanyAddressPage, address)
            updated <- updated.set(AddCompanyContactMethodsYesNoPage, methods.nonEmpty)
            updated <- if (methods.nonEmpty) updated.set(CompanyContactMethodOptionsPage, methods) else Try(updated)
            updated <- setOptional(updated, CompanyEmailAddressPage, subcontractor.emailAddress)
            updated <- setOptional(updated, CompanyPhoneNumberPage, subcontractor.phoneNumber)
            updated <- setOptional(updated, CompanyMobileNumberPage, subcontractor.mobilePhoneNumber)
            updated <- updated.set(CompanyUtrYesNoPage, subcontractor.utr.isDefined)
            updated <- updated.set(OriginalSubcontractorQuery, subcontractor)
            updated <- setOptionalQuery(updated, AmendSubbieResourceRefQuery, subcontractor.subbieResourceRef)
            updated <- setOptional(updated, CompanyUtrPage, subcontractor.utr)
            updated <- updated.set(CompanyCrnYesNoPage, subcontractor.crn.isDefined)
            updated <- setOptional(updated, CompanyCrnPage, subcontractor.crn)
            updated <- updated.set(
                         CompanyWorksReferenceYesNoPage,
                         subcontractor.worksReferenceNumber.isDefined
                       )
            updated <- setOptional(
                         updated,
                         CompanyWorksReferencePage,
                         subcontractor.worksReferenceNumber
                       )
            updated <- updated.set(
                         ShowVerificationDetailsPage,
                         shouldShowVerificationDetails(subcontractor)
                       )
            updated <- updated.set(CisIdQuery, cisId)
            updated <- updated.set(OriginalCompanyAnswersQuery, original)
          } yield updated
      }
    }

    private def originalAnswers(
      subcontractor: SubcontractorResponse,
      address: Option[Address],
      methods: Set[ContactMethodOptions]
    ): OriginalCompanyAnswers =
      OriginalCompanyAnswers(
        companyName = subcontractor.tradingName,
        addressYesNo = Some(SubcontractorPopulatorUtils.addressFieldsExist(subcontractor)),
        address = address,
        companyContactMethodsYesNo = Some(methods.nonEmpty),
        companyContactMethod = methods,
        email = subcontractor.emailAddress,
        phone = subcontractor.phoneNumber,
        mobile = subcontractor.mobilePhoneNumber,
        crnYesNo = Some(subcontractor.crn.isDefined),
        crn = subcontractor.crn,
        utrYesNo = Some(subcontractor.utr.isDefined),
        utr = subcontractor.utr,
        worksReferenceYesNo = Some(subcontractor.worksReferenceNumber.isDefined),
        worksReference = subcontractor.worksReferenceNumber,
        verificationNumber = subcontractor.verificationNumber
      )

  }

  object TrustPopulator {
    def populate(
      userAnswers: UserAnswers,
      cisId: String,
      subcontractor: SubcontractorResponse,
      subJourneyId: Option[String] = None
    ): Try[UserAnswers] = {

      val address    = SubcontractorPopulatorUtils.toAddress(subcontractor)
      val methods    = SubcontractorPopulatorUtils.contactMethods(subcontractor)
      val hasAddress = SubcontractorPopulatorUtils.addressFieldsExist(subcontractor)

      val trustName =
        subcontractor.tradingName.orElse(
          subcontractor.partnershipTradingName
        )

      val original = originalAnswers(
        subcontractor = subcontractor,
        address = address,
        methods = methods,
        trustName = trustName
      )
      subJourneyId match {
        case Some(id) =>
          for { // TODO DTR-9463: Do I need to update Trusts with amend path?
            updated <- userAnswers.set(TypeOfSubcontractorPage(id), Trust)
            updated <- setOptional(updated, TrustNamePage(id), trustName)
            updated <- updated.set(TrustAddressYesNoPage(id), hasAddress)
            updated <- setOptional(updated, TrustAddressPage(id), address)
            updated <- updated.set(AddTrustContactMethodsYesNoPage(id), methods.nonEmpty)
            updated <- if (methods.nonEmpty) updated.set(TrustContactMethodOptionsPage(id), methods) else Try(updated)
            updated <- setOptional(updated, TrustEmailAddressPage(id), subcontractor.emailAddress)
            updated <- setOptional(updated, TrustPhoneNumberPage(id), subcontractor.phoneNumber)
            updated <- setOptional(updated, TrustMobileNumberPage(id), subcontractor.mobilePhoneNumber)
            updated <- updated.set(
                         OriginalSubcontractorQuery,
                         subcontractor
                       ) // TODO DTR-9463: Do I need to update query with id?
            updated <- setOptionalQuery(
                         updated,
                         AmendSubbieResourceRefQuery,
                         subcontractor.subbieResourceRef
                       ) // TODO DTR-9463: Do I need to update query with id?
            updated <- updated.set(TrustUtrYesNoPage(id), subcontractor.utr.isDefined)
            updated <- setOptional(updated, TrustUtrPage(id), subcontractor.utr)
            updated <- updated.set(TrustWorksReferenceYesNoPage(id), subcontractor.worksReferenceNumber.isDefined)
            updated <- setOptional(updated, TrustWorksReferencePage(id), subcontractor.worksReferenceNumber)
            updated <- updated.set(
                         ShowVerificationDetailsPage(id),
                         shouldShowVerificationDetails(subcontractor)
                       )
            updated <- updated.set(CisIdQuery(id), cisId) // TODO DTR-9463: Do I need to update query with amend?
            updated <-
              updated.set(OriginalTrustAnswersQuery, original) // TODO DTR-9463: Do I need to update query with id?
          } yield updated
        case none     =>
          for {
            updated <- userAnswers.set(TypeOfSubcontractorPage, Trust)
            updated <- setOptional(updated, TrustNamePage, trustName)
            updated <- updated.set(TrustAddressYesNoPage, hasAddress)
            updated <- setOptional(updated, TrustAddressPage, address)
            updated <- updated.set(AddTrustContactMethodsYesNoPage, methods.nonEmpty)
            updated <- if (methods.nonEmpty) updated.set(TrustContactMethodOptionsPage, methods) else Try(updated)
            updated <- setOptional(updated, TrustEmailAddressPage, subcontractor.emailAddress)
            updated <- setOptional(updated, TrustPhoneNumberPage, subcontractor.phoneNumber)
            updated <- setOptional(updated, TrustMobileNumberPage, subcontractor.mobilePhoneNumber)
            updated <- updated.set(OriginalSubcontractorQuery, subcontractor)
            updated <- setOptionalQuery(updated, AmendSubbieResourceRefQuery, subcontractor.subbieResourceRef)
            updated <- updated.set(TrustUtrYesNoPage, subcontractor.utr.isDefined)
            updated <- setOptional(updated, TrustUtrPage, subcontractor.utr)
            updated <- updated.set(TrustWorksReferenceYesNoPage, subcontractor.worksReferenceNumber.isDefined)
            updated <- setOptional(updated, TrustWorksReferencePage, subcontractor.worksReferenceNumber)
            updated <- updated.set(
                         ShowVerificationDetailsPage,
                         shouldShowVerificationDetails(subcontractor)
                       )
            updated <- updated.set(CisIdQuery, cisId)
            updated <- updated.set(OriginalTrustAnswersQuery, original)
          } yield updated
      }
    }

    private def originalAnswers(
      subcontractor: SubcontractorResponse,
      address: Option[Address],
      methods: Set[ContactMethodOptions],
      trustName: Option[String]
    ): OriginalTrustAnswers =
      OriginalTrustAnswers(
        trustName = trustName,
        addressYesNo = Some(SubcontractorPopulatorUtils.addressFieldsExist(subcontractor)),
        address = address,
        trustContactMethodsYesNo = Some(methods.nonEmpty),
        trustContactMethod = methods,
        email = subcontractor.emailAddress,
        phone = subcontractor.phoneNumber,
        mobile = subcontractor.mobilePhoneNumber,
        utrYesNo = Some(subcontractor.utr.isDefined),
        utr = subcontractor.utr,
        worksReferenceYesNo = Some(subcontractor.worksReferenceNumber.isDefined),
        worksReference = subcontractor.worksReferenceNumber,
        verificationNumber = subcontractor.verificationNumber
      )
  }

  object PartnershipPopulator {
    def populate(
      userAnswers: UserAnswers,
      cisId: String,
      subcontractor: SubcontractorResponse,
      subJourneyId: Option[String] = None
    ): Try[UserAnswers] = {
      val address              = SubcontractorPopulatorUtils.toAddress(subcontractor)
      val hasAddress           = SubcontractorPopulatorUtils.addressFieldsExist(subcontractor)
      val methods              = SubcontractorPopulatorUtils.contactMethods(subcontractor)
      val nominatedPartnerName = subcontractor.tradingName
      val partnershipName      = subcontractor.partnershipTradingName

      val original = originalAnswers(
        subcontractor = subcontractor,
        address = address,
        methods = methods,
        partnershipName = partnershipName,
        nominatedPartnerName = nominatedPartnerName
      )

      subJourneyId match {
        case Some(id) =>
          for { // TODO DTR-9463: Do I need to update Partnerships with amend path?
            updated <- userAnswers.set(TypeOfSubcontractorPage(id), Partnership)
            updated <- setOptional(updated, PartnershipNamePage(id), partnershipName)
            updated <- updated.set(PartnershipAddressYesNoPage(id), hasAddress)
            updated <- setOptional(updated, PartnershipAddressPage(id), address)
            updated <- updated.set(AddPartnershipContactMethodsYesNoPage(id), methods.nonEmpty)
            updated <-
              if (methods.nonEmpty) updated.set(PartnershipContactMethodOptionsPage(id), methods) else Try(updated)
            updated <- setOptional(updated, PartnershipEmailAddressPage(id), subcontractor.emailAddress)
            updated <- setOptional(updated, PartnershipPhoneNumberPage(id), subcontractor.phoneNumber)
            updated <- setOptional(updated, PartnershipMobileNumberPage(id), subcontractor.mobilePhoneNumber)
            updated <- updated.set(PartnershipHasUtrYesNoPage(id), subcontractor.utr.isDefined)
            updated <- setOptional(updated, PartnershipUniqueTaxpayerReferencePage(id), subcontractor.utr)
            updated <- setOptional(
                         updated,
                         PartnershipNominatedPartnerNamePage(id),
                         nominatedPartnerName
                       )
            updated <- updated.set(PartnershipNominatedPartnerUtrYesNoPage(id), subcontractor.partnerUtr.isDefined)
            updated <- setOptional(updated, PartnershipNominatedPartnerUtrPage(id), subcontractor.partnerUtr)
            updated <- updated.set(ShowVerificationDetailsPage(id), shouldShowVerificationDetails(subcontractor))
            updated <- updated.set(PartnershipNominatedPartnerNinoYesNoPage(id), subcontractor.nino.isDefined)
            updated <- setOptional(updated, PartnershipNominatedPartnerNinoPage(id), subcontractor.nino)
            updated <- updated.set(PartnershipNominatedPartnerCrnYesNoPage(id), subcontractor.crn.isDefined)
            updated <- setOptional(updated, PartnershipNominatedPartnerCrnPage(id), subcontractor.crn)
            updated <- updated.set(
                         OriginalSubcontractorQuery,
                         subcontractor
                       ) // TODO DTR-9463: Do I need to update query with id?
            updated <- setOptionalQuery(
                         updated,
                         AmendSubbieResourceRefQuery,
                         subcontractor.subbieResourceRef
                       ) // TODO DTR-9463: Do I need to update query with id?
            updated <-
              updated.set(PartnershipWorksReferenceNumberYesNoPage(id), subcontractor.worksReferenceNumber.isDefined)
            updated <- setOptional(updated, PartnershipWorksReferenceNumberPage(id), subcontractor.worksReferenceNumber)
            updated <- updated.set(CisIdQuery(id), cisId) // TODO DTR-9463: Do I need to update query with amend?
            updated <- updated.set(
                         OriginalPartnershipAnswersQuery,
                         original
                       ) // TODO DTR-9463: Do I need to update query with id?
          } yield updated
        case None     =>
          for {
            updated <- userAnswers.set(TypeOfSubcontractorPage, Partnership)
            updated <- setOptional(updated, PartnershipNamePage, partnershipName)
            updated <- updated.set(PartnershipAddressYesNoPage, hasAddress)
            updated <- setOptional(updated, PartnershipAddressPage, address)
            updated <- updated.set(AddPartnershipContactMethodsYesNoPage, methods.nonEmpty)
            updated <- if (methods.nonEmpty) updated.set(PartnershipContactMethodOptionsPage, methods) else Try(updated)
            updated <- setOptional(updated, PartnershipEmailAddressPage, subcontractor.emailAddress)
            updated <- setOptional(updated, PartnershipPhoneNumberPage, subcontractor.phoneNumber)
            updated <- setOptional(updated, PartnershipMobileNumberPage, subcontractor.mobilePhoneNumber)
            updated <- updated.set(PartnershipHasUtrYesNoPage, subcontractor.utr.isDefined)
            updated <- setOptional(updated, PartnershipUniqueTaxpayerReferencePage, subcontractor.utr)
            updated <- setOptional(
                         updated,
                         PartnershipNominatedPartnerNamePage,
                         nominatedPartnerName
                       )
            updated <- updated.set(PartnershipNominatedPartnerUtrYesNoPage, subcontractor.partnerUtr.isDefined)
            updated <- setOptional(updated, PartnershipNominatedPartnerUtrPage, subcontractor.partnerUtr)
            updated <- updated.set(ShowVerificationDetailsPage, shouldShowVerificationDetails(subcontractor))
            updated <- updated.set(PartnershipNominatedPartnerNinoYesNoPage, subcontractor.nino.isDefined)
            updated <- setOptional(updated, PartnershipNominatedPartnerNinoPage, subcontractor.nino)
            updated <- updated.set(PartnershipNominatedPartnerCrnYesNoPage, subcontractor.crn.isDefined)
            updated <- setOptional(updated, PartnershipNominatedPartnerCrnPage, subcontractor.crn)
            updated <- updated.set(OriginalSubcontractorQuery, subcontractor)
            updated <- setOptionalQuery(updated, AmendSubbieResourceRefQuery, subcontractor.subbieResourceRef)
            updated <-
              updated.set(PartnershipWorksReferenceNumberYesNoPage, subcontractor.worksReferenceNumber.isDefined)
            updated <- setOptional(updated, PartnershipWorksReferenceNumberPage, subcontractor.worksReferenceNumber)
            updated <- updated.set(CisIdQuery, cisId)
            updated <- updated.set(OriginalPartnershipAnswersQuery, original)
          } yield updated
      }
    }

    private def originalAnswers(
      subcontractor: SubcontractorResponse,
      address: Option[Address],
      methods: Set[ContactMethodOptions],
      partnershipName: Option[String],
      nominatedPartnerName: Option[String]
    ): OriginalPartnershipAnswers =
      OriginalPartnershipAnswers(
        partnershipName = partnershipName,
        addressYesNo = Some(SubcontractorPopulatorUtils.addressFieldsExist(subcontractor)),
        address = address,
        partnershipContactMethodsYesNo = Some(methods.nonEmpty),
        partnershipContactMethodOptions = methods,
        email = subcontractor.emailAddress,
        phone = subcontractor.phoneNumber,
        mobile = subcontractor.mobilePhoneNumber,
        hasUtrYesNo = Some(subcontractor.utr.isDefined),
        utr = subcontractor.utr,
        nominatedPartnerName = nominatedPartnerName,
        nominatedPartnerUtrYesNo = Some(subcontractor.partnerUtr.isDefined),
        nominatedPartnerUtr = subcontractor.partnerUtr,
        nominatedPartnerNinoYesNo = Some(subcontractor.nino.isDefined),
        nominatedPartnerNino = subcontractor.nino,
        nominatedPartnerCrnYesNo = Some(subcontractor.crn.isDefined),
        nominatedPartnerCrn = subcontractor.crn,
        nominatedPartnerWorksReferenceYesNo = Some(subcontractor.worksReferenceNumber.isDefined),
        nominatedPartnerWorksReference = subcontractor.worksReferenceNumber,
        verificationNumber = subcontractor.verificationNumber
      )
  }

  private def setOptionalQuery[A: Writes](
    userAnswers: UserAnswers,
    query: Settable[A],
    value: Option[A]
  ): Try[UserAnswers] =
    value.fold(Try(userAnswers)) { answer =>
      userAnswers.set(query, answer)
    }
}
