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
import models.add.{IndividualContactMethodOptions, SubcontractorName}
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
import queries.*

import scala.util.Try

object AmendSubcontractorPopulator {

  object IndividualPopulator {
    def populate(
      userAnswers: UserAnswers,
      cisId: String,
      subcontractor: SubcontractorResponse
    ): Try[UserAnswers] = {
      val address         = SubcontractorPopulatorUtils.toAddress(subcontractor)
      val methods         = SubcontractorPopulatorUtils.contactMethods(subcontractor)
      val name            = SubcontractorPopulatorUtils.individualName(subcontractor)
      val usesTradingName = SubcontractorPopulatorUtils.usesTradingName(subcontractor)

      val original = originalAnswers(
        subcontractor = subcontractor,
        address = address,
        methods = methods,
        name = name,
        usesTradingName = usesTradingName
      )

      for {
        updated <- userAnswers.set(TypeOfSubcontractorPage, Individualorsoletrader)
        updated <- updated.set(SubTradingNameYesNoPage, usesTradingName)
        updated <- setOptional(updated, TradingNameOfSubcontractorPage, subcontractor.tradingName)
        updated <- setOptional(updated, SubcontractorNamePage, name)
        updated <- updated.set(SubAddressYesNoPage, address.isDefined)
        updated <- setOptional(updated, AddressOfSubcontractorPage, address)
        updated <- updated.set(AddIndividualContactMethodsYesNoPage, methods.nonEmpty)
        updated <- if (methods.nonEmpty) { updated.set(IndividualContactMethodOptionsPage, methods) }
                   else { Try(updated) }
        updated <- setOptional(updated, IndividualEmailAddressPage, subcontractor.emailAddress)
        updated <- setOptional(updated, IndividualPhoneNumberPage, subcontractor.phoneNumber)
        updated <- setOptional(updated, IndividualMobileNumberPage, subcontractor.mobilePhoneNumber)
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

    private def originalAnswers(
      subcontractor: SubcontractorResponse,
      address: Option[Address],
      methods: Set[IndividualContactMethodOptions],
      name: Option[SubcontractorName],
      usesTradingName: Boolean
    ): OriginalIndividualAnswers =
      OriginalIndividualAnswers(
        usesTradingName = Some(usesTradingName),
        tradingName = subcontractor.tradingName,
        subcontractorName = name,
        addressYesNo = Some(address.isDefined),
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
      subcontractor: SubcontractorResponse
    ): Try[UserAnswers] = {

      val address =
        SubcontractorPopulatorUtils.toAddress(subcontractor)

      val methods =
        SubcontractorPopulatorUtils.contactMethods(subcontractor)

      val original =
        originalAnswers(
          subcontractor = subcontractor,
          address = address,
          methods = methods
        )

      for {
        updated <- userAnswers.set(TypeOfSubcontractorPage, Limitedcompany)
        updated <- setOptional(updated, CompanyNamePage, subcontractor.tradingName)
        updated <- updated.set(CompanyAddressYesNoPage, address.isDefined)
        updated <- setOptional(updated, CompanyAddressPage, address)
        updated <- updated.set(AddCompanyContactMethodsYesNoPage, methods.nonEmpty)
        updated <- if (methods.nonEmpty) updated.set(CompanyContactMethodOptionsPage, methods) else Try(updated)
        updated <- setOptional(updated, CompanyEmailAddressPage, subcontractor.emailAddress)
        updated <- setOptional(updated, CompanyPhoneNumberPage, subcontractor.phoneNumber)
        updated <- setOptional(updated, CompanyMobileNumberPage, subcontractor.mobilePhoneNumber)
        updated <- updated.set(CompanyUtrYesNoPage, subcontractor.utr.isDefined)
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

    private def originalAnswers(
      subcontractor: SubcontractorResponse,
      address: Option[Address],
      methods: Set[ContactMethodOptions]
    ): OriginalCompanyAnswers =
      OriginalCompanyAnswers(
        companyName = subcontractor.tradingName,
        addressYesNo = Some(address.isDefined),
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
      subcontractor: SubcontractorResponse
    ): Try[UserAnswers] = {

      val address =
        SubcontractorPopulatorUtils.toAddress(subcontractor)

      val methods =
        SubcontractorPopulatorUtils.contactMethods(subcontractor)

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

      for {
        updated <- userAnswers.set(TypeOfSubcontractorPage, Trust)
        updated <- setOptional(updated, TrustNamePage, trustName)
        updated <- updated.set(TrustAddressYesNoPage, address.isDefined)
        updated <- setOptional(updated, TrustAddressPage, address)
        updated <- updated.set(AddTrustContactMethodsYesNoPage, methods.nonEmpty)
        updated <- if (methods.nonEmpty) updated.set(TrustContactMethodOptionsPage, methods) else Try(updated)
        updated <- setOptional(updated, TrustEmailAddressPage, subcontractor.emailAddress)
        updated <- setOptional(updated, TrustPhoneNumberPage, subcontractor.phoneNumber)
        updated <- setOptional(updated, TrustMobileNumberPage, subcontractor.mobilePhoneNumber)
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

    private def originalAnswers(
      subcontractor: SubcontractorResponse,
      address: Option[Address],
      methods: Set[ContactMethodOptions],
      trustName: Option[String]
    ): OriginalTrustAnswers =
      OriginalTrustAnswers(
        trustName = trustName,
        addressYesNo = Some(address.isDefined),
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
      subcontractor: SubcontractorResponse
    ): Try[UserAnswers] = {
      val address =
        SubcontractorPopulatorUtils.toAddress(subcontractor)

      val methods              =
        SubcontractorPopulatorUtils.contactMethods(subcontractor)
      val nominatedPartnerName = subcontractor.tradingName
      val partnershipName      = subcontractor.partnershipTradingName

      val original = originalAnswers(
        subcontractor = subcontractor,
        address = address,
        methods = methods,
        partnershipName = partnershipName,
        nominatedPartnerName = nominatedPartnerName
      )

      for {
        updated <- userAnswers.set(TypeOfSubcontractorPage, Partnership)
        updated <- setOptional(updated, PartnershipNamePage, partnershipName)
        updated <- updated.set(PartnershipAddressYesNoPage, address.isDefined)
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
        updated <- updated.set(PartnershipWorksReferenceNumberYesNoPage, subcontractor.worksReferenceNumber.isDefined)
        updated <- setOptional(updated, PartnershipWorksReferenceNumberPage, subcontractor.worksReferenceNumber)
        updated <- updated.set(CisIdQuery, cisId)
        updated <- updated.set(OriginalPartnershipAnswersQuery, original)
      } yield updated
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
        addressYesNo = Some(address.isDefined),
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
}
