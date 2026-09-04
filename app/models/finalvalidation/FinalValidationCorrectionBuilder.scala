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
class FinalValidationCorrectionBuilder @Inject() {

  def build(
    userAnswers: UserAnswers,
    payload: FinalValidationHandoffPayload
  ): Try[FinalValidationCorrection] =

    userAnswers.get(TypeOfSubcontractorPage) match {
      case Some(subcontractorType) =>
        buildPatch(subcontractorType, userAnswers, payload.changeTarget)
          .map { patch =>
            FinalValidationCorrection(
              subcontractorId = payload.subcontractorId,
              changeTarget = payload.changeTarget,
              patch = patch
            )
          }

      case None =>
        Failure(new RuntimeException("Type of subcontractor not found"))
    }

  private def buildPatch(
    subcontractorType: TypeOfSubcontractor,
    userAnswers: UserAnswers,
    target: FinalValidationChangeTarget
  ): Try[FinalValidationSubcontractorPatch] =
    (subcontractorType, target) match {

      // Individual or Sole Trader
      case (Individualorsoletrader, SubcontractorName) =>
        update(userAnswers, SubcontractorNamePage) { name =>
          FinalValidationSubcontractorPatch(
            firstName = Some(name.firstName),
            secondName = name.middleName,
            surname = Some(name.lastName)
          )
        }

      case (Individualorsoletrader, TradingName) =>
        update(userAnswers, TradingNameOfSubcontractorPage) {
          value => FinalValidationSubcontractorPatch(tradingName = Some(value))
        }

      case (Individualorsoletrader, AddressYesNo) =>
        clearOnNoOrUpdateOnYes(
          userAnswers,
          SubAddressYesNoPage,
          AddressOfSubcontractorPage
        ) { address =>
          addressPatch(address)
        }

      case (Individualorsoletrader, AddressTarget) =>
        updateAddress(userAnswers, AddressOfSubcontractorPage)

      case (Individualorsoletrader, ContactDetailsYesNo) =>
        clearContactsOnNoOrUpdateOnYes(
          userAnswers,
          AddIndividualContactMethodsYesNoPage,
          IndividualEmailAddressPage,
          IndividualPhoneNumberPage,
          IndividualMobileNumberPage
        )

      case (Individualorsoletrader, EmailAddress) =>
        update(userAnswers, IndividualEmailAddressPage) {
          value => FinalValidationSubcontractorPatch(emailAddress = Some(value))
        }

      case (Individualorsoletrader, PhoneNumber) =>
        update(userAnswers, IndividualPhoneNumberPage) {
          value => FinalValidationSubcontractorPatch(phoneNumber = Some(value))
        }

      case (Individualorsoletrader, MobilePhoneNumber) =>
        update(userAnswers, IndividualMobileNumberPage) {
          value => FinalValidationSubcontractorPatch(mobilePhoneNumber = Some(value))
        }

      case (Individualorsoletrader, UtrYesNo) =>
        clearOnNoOrUpdateOnYes(
          userAnswers,
          UniqueTaxpayerReferenceYesNoPage,
          SubcontractorsUniqueTaxpayerReferencePage
        ) { value =>
          FinalValidationSubcontractorPatch(utr = Some(value))
        }

      case (Individualorsoletrader, Utr) =>
        update(userAnswers, SubcontractorsUniqueTaxpayerReferencePage) {
          value => FinalValidationSubcontractorPatch(utr = Some(value))
        }

      case (Individualorsoletrader, NinoYesNo) =>
        clearOnNoOrUpdateOnYes(
          userAnswers,
          NationalInsuranceNumberYesNoPage,
          SubNationalInsuranceNumberPage
        ) { value =>
          FinalValidationSubcontractorPatch(nino = Some(value))
        }

      case (Individualorsoletrader, Nino) =>
        update(userAnswers, SubNationalInsuranceNumberPage) {
          value => FinalValidationSubcontractorPatch(nino = Some(value))
        }

      case (Individualorsoletrader, WorksReferenceNumberYesNo) =>
        clearOnNoOrUpdateOnYes(
          userAnswers,
          WorksReferenceNumberYesNoPage,
          WorksReferenceNumberPage
        ) { value =>
          FinalValidationSubcontractorPatch(worksReferenceNumber = Some(value))
        }

      case (Individualorsoletrader, WorksReferenceNumber) =>
        update(userAnswers, WorksReferenceNumberPage) {
          value => FinalValidationSubcontractorPatch(worksReferenceNumber = Some(value))
        }


      // Company
      case (Limitedcompany, TradingName) =>
        update(userAnswers, CompanyNamePage) {
          value => FinalValidationSubcontractorPatch(tradingName = Some(value))
        }

      case (Limitedcompany, AddressYesNo) =>
        clearOnNoOrUpdateOnYes(
          userAnswers,
          CompanyAddressYesNoPage,
          CompanyAddressPage
        )(addressPatch)

      case (Limitedcompany, AddressTarget) =>
        updateAddress(userAnswers, CompanyAddressPage)

      case (Limitedcompany, ContactDetailsYesNo) =>
        clearContactsOnNoOrUpdateOnYes(
          userAnswers,
          AddCompanyContactMethodsYesNoPage,
          CompanyEmailAddressPage,
          CompanyPhoneNumberPage,
          CompanyMobileNumberPage
        )

      case (Limitedcompany, EmailAddress) =>
        update(userAnswers, CompanyEmailAddressPage) {
          value => FinalValidationSubcontractorPatch(emailAddress = Some(value))
        }

      case (Limitedcompany, PhoneNumber) =>
        update(userAnswers, CompanyPhoneNumberPage) {
          value => FinalValidationSubcontractorPatch(phoneNumber = Some(value))
        }

      case (Limitedcompany, MobilePhoneNumber) =>
        update(userAnswers, CompanyMobileNumberPage) {
          value => FinalValidationSubcontractorPatch(mobilePhoneNumber = Some(value))
        }

      case (Limitedcompany, UtrYesNo) =>
        clearOnNoOrUpdateOnYes(
          userAnswers,
          CompanyUtrYesNoPage,
          CompanyUtrPage
        ) { value =>
          FinalValidationSubcontractorPatch(utr = Some(value))
        }

      case (Limitedcompany, Utr) =>
        update(userAnswers, CompanyUtrPage) {
          value => FinalValidationSubcontractorPatch(utr = Some(value))
        }

      case (Limitedcompany, CrnYesNo) =>
        clearOnNoOrUpdateOnYes(
          userAnswers,
          CompanyCrnYesNoPage,
          CompanyCrnPage
        ) { value =>
          FinalValidationSubcontractorPatch(crn = Some(value))
        }

      case (Limitedcompany, Crn) =>
        update(userAnswers, CompanyCrnPage) {
          value => FinalValidationSubcontractorPatch(crn = Some(value))
        }

      case (Limitedcompany, WorksReferenceNumberYesNo) =>
        clearOnNoOrUpdateOnYes(
          userAnswers,
          CompanyWorksReferenceYesNoPage,
          CompanyWorksReferencePage
        ) { value =>
          FinalValidationSubcontractorPatch(worksReferenceNumber = Some(value))
        }

      case (Limitedcompany, WorksReferenceNumber) =>
        update(userAnswers, CompanyWorksReferencePage) {
          value => FinalValidationSubcontractorPatch(worksReferenceNumber = Some(value))
        }


      // Trust
      case (Trust, TradingName) =>
        update(userAnswers, TrustNamePage) {
          value => FinalValidationSubcontractorPatch(tradingName = Some(value))
        }

      case (Trust, AddressYesNo) =>
        clearOnNoOrUpdateOnYes(
          userAnswers,
          TrustAddressYesNoPage,
          TrustAddressPage
        )(addressPatch)

      case (Trust, AddressTarget) =>
        updateAddress(userAnswers, TrustAddressPage)

      case (Trust, ContactDetailsYesNo) =>
        clearContactsOnNoOrUpdateOnYes(
          userAnswers,
          AddTrustContactMethodsYesNoPage,
          TrustEmailAddressPage,
          TrustPhoneNumberPage,
          TrustMobileNumberPage
        )

      case (Trust, EmailAddress) =>
        update(userAnswers, TrustEmailAddressPage) {
          value => FinalValidationSubcontractorPatch(emailAddress = Some(value))
        }

      case (Trust, PhoneNumber) =>
        update(userAnswers, TrustPhoneNumberPage) {
          value => FinalValidationSubcontractorPatch(phoneNumber = Some(value))
        }

      case (Trust, MobilePhoneNumber) =>
        update(userAnswers, TrustMobileNumberPage) {
          value => FinalValidationSubcontractorPatch(mobilePhoneNumber = Some(value))
        }

      case (Trust, UtrYesNo) =>
        clearOnNoOrUpdateOnYes(
          userAnswers,
          TrustUtrYesNoPage,
          TrustUtrPage
        ) { value =>
          FinalValidationSubcontractorPatch(utr = Some(value))
        }

      case (Trust, Utr) =>
        update(userAnswers, TrustUtrPage) {
          value => FinalValidationSubcontractorPatch(utr = Some(value))
        }

      case (Trust, WorksReferenceNumberYesNo) =>
        clearOnNoOrUpdateOnYes(
          userAnswers,
          TrustWorksReferenceYesNoPage,
          TrustWorksReferencePage
        ) { value =>
          FinalValidationSubcontractorPatch(worksReferenceNumber = Some(value))
        }

      case (Trust, WorksReferenceNumber) =>
        update(userAnswers, TrustWorksReferencePage) {
          value => FinalValidationSubcontractorPatch(worksReferenceNumber = Some(value))
        }


      // Partnership
      case (Partnership, PartnershipTradingName) =>
        update(userAnswers, PartnershipNamePage) {
          value => FinalValidationSubcontractorPatch(partnershipTradingName = Some(value))
        }

      case (Partnership, TradingName) =>
        update(userAnswers, PartnershipNominatedPartnerNamePage) {
          value => FinalValidationSubcontractorPatch(tradingName = Some(value))
        }

      case (Partnership, AddressYesNo) =>
        clearOnNoOrUpdateOnYes(
          userAnswers,
          PartnershipAddressYesNoPage,
          PartnershipAddressPage
        )(addressPatch)

      case (Partnership, AddressTarget) =>
        updateAddress(userAnswers, PartnershipAddressPage)

      case (Partnership, ContactDetailsYesNo) =>
        clearContactsOnNoOrUpdateOnYes(
          userAnswers,
          AddPartnershipContactMethodsYesNoPage,
          PartnershipEmailAddressPage,
          PartnershipPhoneNumberPage,
          PartnershipMobileNumberPage
        )

      case (Partnership, EmailAddress) =>
        update(userAnswers, PartnershipEmailAddressPage) {
          value => FinalValidationSubcontractorPatch(emailAddress = Some(value))
        }

      case (Partnership, PhoneNumber) =>
        update(userAnswers, PartnershipPhoneNumberPage) {
          value => FinalValidationSubcontractorPatch(phoneNumber = Some(value))
        }

      case (Partnership, MobilePhoneNumber) =>
        update(userAnswers, PartnershipMobileNumberPage) {
          value => FinalValidationSubcontractorPatch(mobilePhoneNumber = Some(value))
        }

      // Partnership  UTR
      case (Partnership, UtrYesNo) =>
        clearOnNoOrUpdateOnYes(
          userAnswers,
          PartnershipHasUtrYesNoPage,
          PartnershipUniqueTaxpayerReferencePage
        ) { value =>
          FinalValidationSubcontractorPatch(utr = Some(value))
        }

      case (Partnership, Utr) =>
        update(userAnswers, PartnershipUniqueTaxpayerReferencePage) {
          value => FinalValidationSubcontractorPatch(utr = Some(value))
        }

      // Nominated partner  UTR
      case (Partnership, PartnerUtrYesNo) =>
        clearOnNoOrUpdateOnYes(
          userAnswers,
          PartnershipNominatedPartnerUtrYesNoPage,
          PartnershipNominatedPartnerUtrPage
        ) { value =>
          FinalValidationSubcontractorPatch(partnerUtr = Some(value))
        }

      case (Partnership, PartnerUtr) =>
        update(userAnswers, PartnershipNominatedPartnerUtrPage) {
          value => FinalValidationSubcontractorPatch(partnerUtr = Some(value))
        }

      case (Partnership, NinoYesNo) =>
        clearOnNoOrUpdateOnYes(
          userAnswers,
          PartnershipNominatedPartnerNinoYesNoPage,
          PartnershipNominatedPartnerNinoPage
        ) { value =>
          FinalValidationSubcontractorPatch(nino = Some(value))
        }

      case (Partnership, Nino) =>
        update(userAnswers, PartnershipNominatedPartnerNinoPage) {
          value => FinalValidationSubcontractorPatch(nino = Some(value))
        }

      case (Partnership, CrnYesNo) =>
        clearOnNoOrUpdateOnYes(
          userAnswers,
          PartnershipNominatedPartnerCrnYesNoPage,
          PartnershipNominatedPartnerCrnPage
        ) { value =>
          FinalValidationSubcontractorPatch(crn = Some(value))
        }

      case (Partnership, Crn) =>
        update(userAnswers, PartnershipNominatedPartnerCrnPage) {
          value => FinalValidationSubcontractorPatch(crn = Some(value))
        }

      case (Partnership, WorksReferenceNumberYesNo) =>
        clearOnNoOrUpdateOnYes(
          userAnswers,
          PartnershipWorksReferenceNumberYesNoPage,
          PartnershipWorksReferenceNumberPage
        ) { value =>
          FinalValidationSubcontractorPatch(worksReferenceNumber = Some(value))
        }

      case (Partnership, WorksReferenceNumber) =>
        update(userAnswers, PartnershipWorksReferenceNumberPage) {
          value => FinalValidationSubcontractorPatch(worksReferenceNumber = Some(value))
        }

      case _ =>
        Failure(
          new RuntimeException(
            s"Unsupported combination of subcontractor type: $subcontractorType " +
              s"and change target: $target"
          )
        )
    }

  private def answer[A: Reads](
    userAnswers: UserAnswers,
    page: QuestionPage[A]
  )(toPatch: A => FinalValidationSubcontractorPatch): Try[FinalValidationSubcontractorPatch] =
    userAnswers.get(page) match {
      case Some(value) =>
        Success(toPatch(value))

      case None =>
        Failure(
          new RuntimeException(
            s"${page.toString} not found"
          )
        )
    }

  private def update[A: Reads](
    userAnswers: UserAnswers,
    page: QuestionPage[A]
  )(toPatch: A => FinalValidationSubcontractorPatch): Try[FinalValidationSubcontractorPatch] =
    answer(userAnswers, page)(toPatch)

  private def clearOnNoOrUpdateOnYes[A: Reads](
    userAnswers: UserAnswers,
    yesNoPage: QuestionPage[Boolean],
    valuePage: QuestionPage[A]
  )(toPatch: A => FinalValidationSubcontractorPatch): Try[FinalValidationSubcontractorPatch] =
    userAnswers.get(yesNoPage) match {
      case Some(true) =>
        update(userAnswers, valuePage)(toPatch)

      case Some(false) =>
        Success(FinalValidationSubcontractorPatch())

      case None =>
        Failure(new RuntimeException(s"${yesNoPage.toString} not found"))
    }

  private def updateAddress(
    userAnswers: UserAnswers,
    page: QuestionPage[Address]
  ): Try[FinalValidationSubcontractorPatch] =
    update(userAnswers, page)(addressPatch)

  private def clearContactsOnNoOrUpdateOnYes(
    userAnswers: UserAnswers,
    yesNoPage: QuestionPage[Boolean],
    emailPage: QuestionPage[String],
    phonePage: QuestionPage[String],
    mobilePage: QuestionPage[String]
  ): Try[FinalValidationSubcontractorPatch] =
    userAnswers.get(yesNoPage) match {
      case Some(true) =>
        Success(
          FinalValidationSubcontractorPatch(
            emailAddress = userAnswers.get(emailPage),
            phoneNumber = userAnswers.get(phonePage),
            mobilePhoneNumber = userAnswers.get(mobilePage)
          )
        )

      case Some(false) =>
        Success(FinalValidationSubcontractorPatch())

      case None =>
        Failure(new RuntimeException(s"${yesNoPage.toString} not found"))
    }

  private def addressPatch(
    address: Address
  ): FinalValidationSubcontractorPatch =
    FinalValidationSubcontractorPatch(
      addressLine1 = Some(address.addressLine1),
      addressLine2 = address.addressLine2,
      addressLine3 = address.addressLine3,
      addressLine4 = address.addressLine4,
      country = address.country.flatMap(_.name),
      postcode = address.postcode
    )
}
