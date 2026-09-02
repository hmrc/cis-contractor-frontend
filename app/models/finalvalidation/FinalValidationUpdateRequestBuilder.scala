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

package models.finalvalidation

import models.UserAnswers
import models.address.Address
import models.finalvalidation.FinalValidationChangeTarget.{Address as AddressTarget, *}
import pages.QuestionPage
import pages.add.*
import pages.add.company.*
import pages.add.partnership.*
import pages.add.trust.*
import play.api.libs.json.Reads
import models.TypeOfSubcontractor
import models.TypeOfSubcontractor.*

import javax.inject.{Inject, Singleton}
import scala.util.{Failure, Success, Try}


@Singleton
class FinalValidationUpdateRequestBuilder @Inject() {

  def build(
    userAnswers: UserAnswers,
    payload: FinalValidationHandoffPayload
  ): Try[Option[FinalValidationUpdateSubcontractorRequest]] =

    userAnswers.get(TypeOfSubcontractorPage) match {
      case Some(subcontractorType) =>
        buildFor(subcontractorType, userAnswers, payload)
      case None =>
        Failure(new RuntimeException("Type of subcontractor not found"))
    }

  private def buildFor(
    subcontractorType: TypeOfSubcontractor,
    userAnswers: UserAnswers,
    payload: FinalValidationHandoffPayload
  ): Try[Option[FinalValidationUpdateSubcontractorRequest]] =
    (subcontractorType, payload.changeTarget) match {
      // Individual or Sole Trader
      case (Individualorsoletrader, SubcontractorName) =>
        update(userAnswers, SubcontractorNamePage, payload) { name =>
          FinalValidationSubcontractorPatch(
            firstName = Some(name.firstName),
            secondName  = name.middleName,
            surname = Some(name.lastName)
          )
        }

      case (Individualorsoletrader, TradingName) =>
        update(userAnswers, TradingNameOfSubcontractorPage, payload) {
          value => FinalValidationSubcontractorPatch(tradingName = Some(value))
        }

      case (Individualorsoletrader, AddressYesNo) =>
        clearOnNoOrUpdateOnYes(
          userAnswers,
          SubAddressYesNoPage,
          AddressOfSubcontractorPage,
          payload
        ) { address =>
          addressPatch(address)
        }

      case (Individualorsoletrader, AddressTarget) =>
        updateAddress(userAnswers, AddressOfSubcontractorPage, payload)

      case (Individualorsoletrader, ContactDetailsYesNo) =>
        clearContactsOnNoOrUpdateOnYes(
          userAnswers,
          AddIndividualContactMethodsYesNoPage,
          IndividualEmailAddressPage,
          IndividualPhoneNumberPage,
          IndividualMobileNumberPage,
          payload
        )

      case (Individualorsoletrader, EmailAddress) =>
        update(userAnswers, IndividualEmailAddressPage, payload) {
          value => FinalValidationSubcontractorPatch(emailAddress = Some(value))
        }

      case (Individualorsoletrader, PhoneNumber) =>
        update(userAnswers, IndividualPhoneNumberPage, payload) {
          value => FinalValidationSubcontractorPatch(phoneNumber = Some(value))
        }

      case (Individualorsoletrader, MobilePhoneNumber) =>
        update(userAnswers, IndividualMobileNumberPage, payload) {
          value => FinalValidationSubcontractorPatch(mobilePhoneNumber = Some(value))
        }

      case (Individualorsoletrader, UtrYesNo) =>
        clearOnNoOrUpdateOnYes(
          userAnswers,
          UniqueTaxpayerReferenceYesNoPage,
          SubcontractorsUniqueTaxpayerReferencePage,
          payload
        ) { value =>
          FinalValidationSubcontractorPatch(utr = Some(value))
        }

      case (Individualorsoletrader, Utr) =>
        update(userAnswers, SubcontractorsUniqueTaxpayerReferencePage, payload) {
          value => FinalValidationSubcontractorPatch(utr = Some(value))
        }

      case (Individualorsoletrader, NinoYesNo) =>
        clearOnNoOrUpdateOnYes(
          userAnswers,
          NationalInsuranceNumberYesNoPage,
          SubNationalInsuranceNumberPage,
          payload
        ) { value =>
          FinalValidationSubcontractorPatch(nino = Some(value))
        }

      case (Individualorsoletrader, Nino) =>
        update(userAnswers, SubNationalInsuranceNumberPage, payload) {
          value => FinalValidationSubcontractorPatch(nino = Some(value))
        }

      case (Individualorsoletrader, WorksReferenceNumberYesNo) =>
        clearOnNoOrUpdateOnYes(
          userAnswers,
          WorksReferenceNumberYesNoPage,
          WorksReferenceNumberPage,
          payload
        ) { value =>
          FinalValidationSubcontractorPatch(worksReferenceNumber = Some(value))
        }

      case (Individualorsoletrader, WorksReferenceNumber) =>
        update(userAnswers, WorksReferenceNumberPage, payload) {
          value => FinalValidationSubcontractorPatch(worksReferenceNumber = Some(value))
        }
        
      // Company
      case (Limitedcompany, TradingName) =>  
        update(userAnswers, CompanyNamePage, payload) {
          value => FinalValidationSubcontractorPatch(tradingName = Some(value))
        }

      case (Limitedcompany, AddressYesNo) => 
        clearOnNoOrUpdateOnYes(
          userAnswers,
          CompanyAddressYesNoPage,
          CompanyAddressPage,
          payload
        )(addressPatch)

      case (Limitedcompany, AddressTarget) => 
        updateAddress(userAnswers, CompanyAddressPage, payload)

      case (Limitedcompany, ContactDetailsYesNo) => 
        clearContactsOnNoOrUpdateOnYes(
          userAnswers,
          AddCompanyContactMethodsYesNoPage,
          CompanyEmailAddressPage,
          CompanyPhoneNumberPage,
          CompanyMobileNumberPage,
          payload
        )

      case (Limitedcompany, EmailAddress) => 
        update(userAnswers, CompanyEmailAddressPage, payload) {
          value => FinalValidationSubcontractorPatch(emailAddress = Some(value))
        }

      case (Limitedcompany, PhoneNumber) => 
        update(userAnswers, CompanyPhoneNumberPage, payload) {
          value => FinalValidationSubcontractorPatch(phoneNumber = Some(value))
        }

      case (Limitedcompany, MobilePhoneNumber) => 
        update(userAnswers, CompanyMobileNumberPage, payload) {
          value => FinalValidationSubcontractorPatch(mobilePhoneNumber = Some(value))
        }

      case (Limitedcompany, UtrYesNo) => 
        clearOnNoOrUpdateOnYes(
          userAnswers,
          CompanyUtrYesNoPage,
          CompanyUtrPage,
          payload
        ){ value =>
          FinalValidationSubcontractorPatch(utr = Some(value))
        }

      case (Limitedcompany, Utr) => 
        update(userAnswers, CompanyUtrPage, payload) {
          value => FinalValidationSubcontractorPatch(utr = Some(value))
        }
        
      case (Limitedcompany, CrnYesNo) => 
        clearOnNoOrUpdateOnYes(
          userAnswers,
          CompanyCrnYesNoPage,
          CompanyCrnPage,
          payload
        ) { value =>
          FinalValidationSubcontractorPatch(crn = Some(value))
        }

      case (Limitedcompany, Crn) => 
        update(userAnswers, CompanyCrnPage, payload) {
          value => FinalValidationSubcontractorPatch(crn = Some(value))
        }

      case (Limitedcompany, WorksReferenceNumberYesNo) => 
        clearOnNoOrUpdateOnYes(
          userAnswers,
          CompanyWorksReferenceYesNoPage,
          CompanyWorksReferencePage,
          payload
        ) { value =>
          FinalValidationSubcontractorPatch(worksReferenceNumber = Some(value))
        }

      case (Limitedcompany, WorksReferenceNumber) => 
        update(userAnswers, CompanyWorksReferencePage, payload) {
          value => FinalValidationSubcontractorPatch(worksReferenceNumber = Some(value))
        }
      
      // Trust
      case (Trust, TradingName) =>
        update(userAnswers, TrustNamePage, payload) {
          value => FinalValidationSubcontractorPatch(tradingName = Some(value))
        }
            
      case (Trust, AddressYesNo) =>
        clearOnNoOrUpdateOnYes(
          userAnswers,
          TrustAddressYesNoPage,
          TrustAddressPage,
          payload
        )(addressPatch)
        
      case (Trust, AddressTarget) =>
        updateAddress(userAnswers, TrustAddressPage, payload)
        
      case (Trust, ContactDetailsYesNo) =>
        clearContactsOnNoOrUpdateOnYes(
          userAnswers,
          AddTrustContactMethodsYesNoPage,
          TrustEmailAddressPage,
          TrustPhoneNumberPage,
          TrustMobileNumberPage,
          payload
        )
        
      case (Trust, EmailAddress) =>
        update(userAnswers, TrustEmailAddressPage, payload) {
          value => FinalValidationSubcontractorPatch(emailAddress = Some(value))
          }
            
      case (Trust, PhoneNumber) =>  
        update(userAnswers, TrustPhoneNumberPage, payload) {
          value => FinalValidationSubcontractorPatch(phoneNumber = Some(value))
        }
        
      case (Trust, MobilePhoneNumber) =>
        update(userAnswers, TrustMobileNumberPage, payload) {
          value => FinalValidationSubcontractorPatch(mobilePhoneNumber = Some(value))
        }
            
      case (Trust, UtrYesNo) =>
        clearOnNoOrUpdateOnYes(
          userAnswers,
          TrustUtrYesNoPage,
          TrustUtrPage,
          payload
        ) { value =>
          FinalValidationSubcontractorPatch(utr = Some(value))
        }
        
      case (Trust, Utr) =>
        update(userAnswers, TrustUtrPage, payload) {
          value => FinalValidationSubcontractorPatch(utr = Some(value))
        }
            
      case (Trust, WorksReferenceNumberYesNo) =>
        clearOnNoOrUpdateOnYes(
          userAnswers,
          TrustWorksReferenceYesNoPage,
          TrustWorksReferencePage,
          payload
        ) { value =>
          FinalValidationSubcontractorPatch(worksReferenceNumber = Some(value))
        }
        
      case (Trust, WorksReferenceNumber) =>
        update(userAnswers, TrustWorksReferencePage, payload) {
          value => FinalValidationSubcontractorPatch(worksReferenceNumber = Some(value))
        }
            
      // Partnership
      case (Partnership, PartnershipTradingName) =>
        update(userAnswers, PartnershipNamePage, payload) {
          value => FinalValidationSubcontractorPatch(partnershipTradingName = Some(value))
        }
      
      case (Partnership, TradingName) =>
        update(userAnswers, PartnershipNominatedPartnerNamePage, payload) {
          value => FinalValidationSubcontractorPatch(tradingName = Some(value))
        }
        
      case (Partnership, AddressYesNo) =>
        clearOnNoOrUpdateOnYes(
          userAnswers,
          PartnershipAddressYesNoPage,
          PartnershipAddressPage,
          payload
        )(addressPatch)
        
      case (Partnership, AddressTarget) =>
        updateAddress(userAnswers, PartnershipAddressPage, payload)
        
      case (Partnership, ContactDetailsYesNo) =>
        clearContactsOnNoOrUpdateOnYes(
          userAnswers,
          AddPartnershipContactMethodsYesNoPage,
          PartnershipEmailAddressPage,
          PartnershipPhoneNumberPage,
          PartnershipMobileNumberPage,
          payload
        )
        
      case (Partnership, EmailAddress) =>
        update(userAnswers, PartnershipEmailAddressPage, payload) {
          value => FinalValidationSubcontractorPatch(emailAddress = Some(value))
        }
            
      case (Partnership, PhoneNumber) =>
        update(userAnswers, PartnershipPhoneNumberPage, payload) {
          value => FinalValidationSubcontractorPatch(phoneNumber = Some(value))
        }
        
      case (Partnership, MobilePhoneNumber) =>
        update(userAnswers, PartnershipMobileNumberPage, payload) {
          value => FinalValidationSubcontractorPatch(mobilePhoneNumber = Some(value))
        }
      
      // Partnership  UTR  
      case (Partnership, UtrYesNo) =>
        clearOnNoOrUpdateOnYes(
          userAnswers,
          PartnershipHasUtrYesNoPage,
          PartnershipUniqueTaxpayerReferencePage,
          payload
        ) { value =>
          FinalValidationSubcontractorPatch(utr = Some(value))
        }
        
      case (Partnership, Utr) =>
        update(userAnswers, PartnershipUniqueTaxpayerReferencePage, payload) {
          value => FinalValidationSubcontractorPatch(utr = Some(value))
        }
            
      // Nominated partner  UTR
      case (Partnership, PartnerUtrYesNo) =>
        clearOnNoOrUpdateOnYes(
          userAnswers,
          PartnershipNominatedPartnerUtrYesNoPage,
          PartnershipNominatedPartnerUtrPage,
          payload
        ) { value =>
          FinalValidationSubcontractorPatch(partnerUtr = Some(value))
        }
        
      case (Partnership, PartnerUtr) =>
        update(userAnswers, PartnershipNominatedPartnerUtrPage, payload) {
          value => FinalValidationSubcontractorPatch(partnerUtr = Some(value))
        }
            
      case (Partnership, NinoYesNo) =>
        clearOnNoOrUpdateOnYes(
          userAnswers,
          PartnershipNominatedPartnerNinoYesNoPage,
          PartnershipNominatedPartnerNinoPage,
          payload
        ) { value =>
          FinalValidationSubcontractorPatch(nino = Some(value))
        }
        
      case (Partnership, Nino) =>
        update(userAnswers, PartnershipNominatedPartnerNinoPage, payload) {
          value => FinalValidationSubcontractorPatch(nino = Some(value))
        }
            
      case (Partnership, CrnYesNo) =>
        clearOnNoOrUpdateOnYes(
          userAnswers,
          PartnershipNominatedPartnerCrnYesNoPage,
          PartnershipNominatedPartnerCrnPage,
          payload
        ) { value =>
          FinalValidationSubcontractorPatch(crn = Some(value))
        }

      case (Partnership, Crn) =>
        update(userAnswers, PartnershipNominatedPartnerCrnPage, payload) {
          value => FinalValidationSubcontractorPatch(crn = Some(value))
        }

      case (Partnership, WorksReferenceNumberYesNo) =>
        clearOnNoOrUpdateOnYes(
          userAnswers,
          PartnershipWorksReferenceNumberYesNoPage,
          PartnershipWorksReferenceNumberPage,
          payload
        ) { value =>
          FinalValidationSubcontractorPatch(worksReferenceNumber = Some(value))
        }

      case (Partnership, WorksReferenceNumber) =>
        update(userAnswers, PartnershipWorksReferenceNumberPage, payload) {
          value => FinalValidationSubcontractorPatch(worksReferenceNumber = Some(value))
        }

      case _ =>
        Failure(new RuntimeException(s"Unsupported combination of subcontractor type: $subcontractorType " +
          s"and change target: ${payload.changeTarget}"))
    }

  private def addressPatch(address: Address): FinalValidationSubcontractorPatch =
    FinalValidationSubcontractorPatch(
      addressLine1 = Some(address.addressLine1),
      addressLine2 = address.addressLine2,
      addressLine3 = address.addressLine3,
      addressLine4 = address.addressLine4,
      country      = address.country.flatMap(_.name),
      postcode     = address.postcode
    )

  private def update[A: Reads](
    userAnswers: UserAnswers,
    page: QuestionPage[A],
    payload: FinalValidationHandoffPayload
  )(toPatch: A => FinalValidationSubcontractorPatch): Try[Option[FinalValidationUpdateSubcontractorRequest]] =
    userAnswers.get(page) match {
      case Some(answer) =>
        Success(Some(request(payload, toPatch(answer))))
      case None =>
        Failure(new RuntimeException(s"${page.toString} not found"))
    }

  private def clearOnNoOrUpdateOnYes[A: Reads](
    userAnswers: UserAnswers,
    yesNoPage: QuestionPage[Boolean],
    valuePage: QuestionPage[A],
    payload: FinalValidationHandoffPayload
  )(toPatch: A => FinalValidationSubcontractorPatch): Try[Option[FinalValidationUpdateSubcontractorRequest]] =
    userAnswers.get(yesNoPage) match {
      case Some(false) =>
        Success(Some(request(payload, FinalValidationSubcontractorPatch())))
      case Some(true)  =>
        update(userAnswers, valuePage, payload)(toPatch)
      case None        =>
        Failure(new RuntimeException(s"${yesNoPage.toString} is required but not found"))
    }

  private def clearContactsOnNoOrUpdateOnYes(
    userAnswers: UserAnswers,
    yesNoPage: QuestionPage[Boolean],
    emailPage: QuestionPage[String],
    phonePage: QuestionPage[String],
    mobilePage: QuestionPage[String],
    payload: FinalValidationHandoffPayload
  ): Try[Option[FinalValidationUpdateSubcontractorRequest]] =
    userAnswers.get(yesNoPage) match {
      case Some(false) =>
        Success(Some(request(payload, FinalValidationSubcontractorPatch())))

      case Some(true)  =>
        Success(Some(request(payload, FinalValidationSubcontractorPatch(
          emailAddress = userAnswers.get(emailPage),
          phoneNumber = userAnswers.get(phonePage),
          mobilePhoneNumber = userAnswers.get(mobilePage)
        ))))

      case None        =>
        Failure(new RuntimeException(s"${yesNoPage.toString} is required but not found"))
    }


  private def updateAddress(
    userAnswers: UserAnswers,
    page: QuestionPage[Address],
    payload: FinalValidationHandoffPayload
  ): Try[Option[FinalValidationUpdateSubcontractorRequest]] =
    update(userAnswers, page, payload)(addressPatch)

  private def request(
    payload: FinalValidationHandoffPayload,
    patch: FinalValidationSubcontractorPatch
  ): FinalValidationUpdateSubcontractorRequest =
    FinalValidationUpdateSubcontractorRequest(
      instanceId = payload.instanceId,
      subcontractorId = payload.subcontractorId,
      subbieResourceRef = payload.subbieResourceRef,
      changeTarget = payload.changeTarget.key,
      patch = patch
    )

}
