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

package utils

import base.SpecBase
import models.add.*
import models.contact.ContactOptions
import org.scalatest.freespec.AnyFreeSpec
import utils.SubcontractorCleanup.*
import pages.add.*
import pages.add.partnership.*
import pages.add.company.*
import pages.add.trust.*

class SubcontractorCleanupSpec extends SpecBase {

  val taxpayerReference = "5860920998"

  val nationalInsuranceNumber = "AA123456A"

  val worksReferenceNumber = "1234567-AB"

  val crn = "AC012345"

  val utr = "5860920998"

  val email = "abc@bbb.com"

  val name = "John"

  val address = InternationalAddress(
    addressLine1 = "value 1",
    addressLine2 = Some("value 2"),
    addressLine3 = "value 3",
    addressLine4 = Some("value 4"),
    postalCode = "NX1 1AA",
    country = "United Kingdom"
  )

  val phoneNumber = "01234567"

  val subcontractorName = SubcontractorName(
    firstName = "John",
    middleName = Some("Paul"),
    lastName = "Smith"
  )

  ".removeIndividualSoleTraderSubcontractor" - {

    "remove all IndividualSoleTraderSubcontractor journey answers" in {

      val userAnswers =
        emptyUserAnswers
          .set(AddressOfSubcontractorPage, address)
          .success
          .value
          .set(IndividualChooseContactDetailsPage, ContactOptions.Email)
          .success
          .value
          .set(IndividualEmailAddressPage, email)
          .success
          .value
          .set(IndividualMobileNumberPage, phoneNumber)
          .success
          .value
          .set(IndividualPhoneNumberPage, phoneNumber)
          .success
          .value
          .set(NationalInsuranceNumberYesNoPage, true)
          .success
          .value
          .set(SubAddressYesNoPage, true)
          .success
          .value
          .set(SubcontractorNamePage, subcontractorName)
          .success
          .value
          .set(SubcontractorsUniqueTaxpayerReferencePage, taxpayerReference)
          .success
          .value
          .set(SubNationalInsuranceNumberPage, nationalInsuranceNumber)
          .success
          .value
          .set(SubTradingNameYesNoPage, true)
          .success
          .value
          .set(TradingNameOfSubcontractorPage, name)
          .success
          .value
          .set(UniqueTaxpayerReferenceYesNoPage, true)
          .success
          .value
          .set(WorksReferenceNumberPage, worksReferenceNumber)
          .success
          .value
          .set(WorksReferenceNumberYesNoPage, true)
          .success
          .value

      val result = removeIndividualSoleTraderSubcontractor(userAnswers).success.value

      result.get(AddressOfSubcontractorPage) mustBe None
      result.get(IndividualChooseContactDetailsPage) mustBe None
      result.get(IndividualEmailAddressPage) mustBe None
      result.get(IndividualMobileNumberPage) mustBe None
      result.get(IndividualPhoneNumberPage) mustBe None
      result.get(NationalInsuranceNumberYesNoPage) mustBe None
      result.get(SubAddressYesNoPage) mustBe None
      result.get(SubcontractorNamePage) mustBe None
      result.get(SubcontractorsUniqueTaxpayerReferencePage) mustBe None
      result.get(SubNationalInsuranceNumberPage) mustBe None
      result.get(SubTradingNameYesNoPage) mustBe None
      result.get(TradingNameOfSubcontractorPage) mustBe None
      result.get(UniqueTaxpayerReferenceYesNoPage) mustBe None
      result.get(WorksReferenceNumberPage) mustBe None
      result.get(WorksReferenceNumberYesNoPage) mustBe None
    }
  }

  ".removeLimitedCompanySubcontractor" - {

    "remove all LimitedCompanySubcontractor journey answers" in {

      val userAnswers =
        emptyUserAnswers
          .set(CompanyAddressPage, address)
          .success
          .value
          .set(CompanyAddressYesNoPage, true)
          .success
          .value
          .set(CompanyContactOptionsPage, ContactOptions.Email)
          .success
          .value
          .set(CompanyCrnPage, crn)
          .success
          .value
          .set(CompanyCrnYesNoPage, true)
          .success
          .value
          .set(CompanyEmailAddressPage, email)
          .success
          .value
          .set(CompanyMobileNumberPage, phoneNumber)
          .success
          .value
          .set(CompanyNamePage, name)
          .success
          .value
          .set(CompanyPhoneNumberPage, phoneNumber)
          .success
          .value
          .set(CompanyUtrPage, taxpayerReference)
          .success
          .value
          .set(CompanyUtrYesNoPage, true)
          .success
          .value
          .set(CompanyWorksReferencePage, worksReferenceNumber)
          .success
          .value
          .set(CompanyWorksReferenceYesNoPage, true)
          .success
          .value

      val result = removeLimitedCompanySubcontractor(userAnswers).success.value

      result.get(CompanyAddressPage) mustBe None
      result.get(CompanyAddressYesNoPage) mustBe None
      result.get(CompanyContactOptionsPage) mustBe None
      result.get(CompanyCrnPage) mustBe None
      result.get(CompanyCrnYesNoPage) mustBe None
      result.get(CompanyEmailAddressPage) mustBe None
      result.get(CompanyMobileNumberPage) mustBe None
      result.get(CompanyNamePage) mustBe None
      result.get(CompanyPhoneNumberPage) mustBe None
      result.get(CompanyUtrPage) mustBe None
      result.get(CompanyUtrYesNoPage) mustBe None
      result.get(CompanyWorksReferencePage) mustBe None
      result.get(CompanyWorksReferenceYesNoPage) mustBe None
    }
  }

  ".removePartnershipSubcontractor" - {

    "remove all PartnershipSubcontractor journey answers" in {

      val userAnswers =
        emptyUserAnswers
          .set(PartnershipAddressPage, address)
          .success
          .value
          .set(PartnershipAddressYesNoPage, true)
          .success
          .value
          .set(PartnershipChooseContactDetailsPage, ContactOptions.Email)
          .success
          .value
          .set(PartnershipEmailAddressPage, email)
          .success
          .value
          .set(PartnershipHasUtrYesNoPage, true)
          .success
          .value
          .set(PartnershipMobileNumberPage, phoneNumber)
          .success
          .value
          .set(PartnershipNamePage, name)
          .success
          .value
          .set(PartnershipNominatedPartnerCrnPage, crn)
          .success
          .value
          .set(PartnershipNominatedPartnerCrnYesNoPage, true)
          .success
          .value
          .set(PartnershipNominatedPartnerNamePage, "John")
          .success
          .value
          .set(PartnershipNominatedPartnerNinoPage, nationalInsuranceNumber)
          .success
          .value
          .set(PartnershipNominatedPartnerNinoYesNoPage, true)
          .success
          .value
          .set(PartnershipNominatedPartnerUtrPage, utr)
          .success
          .value
          .set(PartnershipNominatedPartnerUtrYesNoPage, true)
          .success
          .value
          .set(PartnershipPhoneNumberPage, phoneNumber)
          .success
          .value
          .set(PartnershipUniqueTaxpayerReferencePage, taxpayerReference)
          .success
          .value
          .set(PartnershipWorksReferenceNumberPage, worksReferenceNumber)
          .success
          .value
          .set(PartnershipWorksReferenceNumberYesNoPage, true)
          .success
          .value

      val result = removePartnershipSubcontractor(userAnswers).success.value

      result.get(PartnershipAddressPage) mustBe None
      result.get(PartnershipAddressYesNoPage) mustBe None
      result.get(PartnershipChooseContactDetailsPage) mustBe None
      result.get(PartnershipEmailAddressPage) mustBe None
      result.get(PartnershipHasUtrYesNoPage) mustBe None
      result.get(PartnershipMobileNumberPage) mustBe None
      result.get(PartnershipNamePage) mustBe None
      result.get(PartnershipNominatedPartnerCrnPage) mustBe None
      result.get(PartnershipNominatedPartnerCrnYesNoPage) mustBe None
      result.get(PartnershipNominatedPartnerNamePage) mustBe None
      result.get(PartnershipNominatedPartnerNinoPage) mustBe None
      result.get(PartnershipNominatedPartnerNinoYesNoPage) mustBe None
      result.get(PartnershipNominatedPartnerUtrPage) mustBe None
      result.get(PartnershipNominatedPartnerUtrYesNoPage) mustBe None
      result.get(PartnershipPhoneNumberPage) mustBe None
      result.get(PartnershipUniqueTaxpayerReferencePage) mustBe None
      result.get(PartnershipWorksReferenceNumberPage) mustBe None
      result.get(PartnershipWorksReferenceNumberYesNoPage) mustBe None
    }
  }

  ".removeTrustSubcontractor" - {

    "remove all TrustSubcontractor journey answers" in {

      val userAnswers =
        emptyUserAnswers
          .set(TrustAddressPage, address)
          .success
          .value
          .set(TrustAddressYesNoPage, true)
          .success
          .value
          .set(TrustContactOptionsPage, ContactOptions.Email)
          .success
          .value
          .set(TrustEmailAddressPage, email)
          .success
          .value
          .set(TrustMobileNumberPage, phoneNumber)
          .success
          .value
          .set(TrustNamePage, name)
          .success
          .value
          .set(TrustPhoneNumberPage, phoneNumber)
          .success
          .value
          .set(TrustUtrPage, utr)
          .success
          .value
          .set(TrustUtrYesNoPage, true)
          .success
          .value
          .set(TrustWorksReferencePage, worksReferenceNumber)
          .success
          .value
          .set(TrustWorksReferenceYesNoPage, true)
          .success
          .value

      val result = removeTrustSubcontractor(userAnswers).success.value

      result.get(TrustAddressPage) mustBe None
      result.get(TrustAddressYesNoPage) mustBe None
      result.get(TrustContactOptionsPage) mustBe None
      result.get(TrustEmailAddressPage) mustBe None
      result.get(TrustMobileNumberPage) mustBe None
      result.get(TrustNamePage) mustBe None
      result.get(TrustPhoneNumberPage) mustBe None
      result.get(TrustUtrPage) mustBe None
      result.get(TrustUtrYesNoPage) mustBe None
      result.get(TrustWorksReferencePage) mustBe None
      result.get(TrustWorksReferenceYesNoPage) mustBe None
    }
  }

  ".removeAllSubcontractor" - {

    "remove all subcontractor related journey answers" in {

      val userAnswers =
        emptyUserAnswers
          .set(TypeOfSubcontractorPage, TypeOfSubcontractor.Individualorsoletrader)
          .success
          .value
          .set(AddressOfSubcontractorPage, address)
          .success
          .value
          .set(IndividualChooseContactDetailsPage, ContactOptions.Email)
          .success
          .value
          .set(IndividualEmailAddressPage, email)
          .success
          .value
          .set(IndividualMobileNumberPage, phoneNumber)
          .success
          .value
          .set(IndividualPhoneNumberPage, phoneNumber)
          .success
          .value
          .set(NationalInsuranceNumberYesNoPage, true)
          .success
          .value
          .set(SubAddressYesNoPage, true)
          .success
          .value
          .set(SubcontractorNamePage, subcontractorName)
          .success
          .value
          .set(SubcontractorsUniqueTaxpayerReferencePage, taxpayerReference)
          .success
          .value
          .set(SubNationalInsuranceNumberPage, nationalInsuranceNumber)
          .success
          .value
          .set(SubTradingNameYesNoPage, true)
          .success
          .value
          .set(TradingNameOfSubcontractorPage, name)
          .success
          .value
          .set(UniqueTaxpayerReferenceYesNoPage, true)
          .success
          .value
          .set(WorksReferenceNumberPage, worksReferenceNumber)
          .success
          .value
          .set(WorksReferenceNumberYesNoPage, true)
          .success
          .value
          .set(CompanyAddressPage, address)
          .success
          .value
          .set(CompanyAddressYesNoPage, true)
          .success
          .value
          .set(CompanyContactOptionsPage, ContactOptions.Email)
          .success
          .value
          .set(CompanyCrnPage, crn)
          .success
          .value
          .set(CompanyCrnYesNoPage, true)
          .success
          .value
          .set(CompanyEmailAddressPage, email)
          .success
          .value
          .set(CompanyMobileNumberPage, phoneNumber)
          .success
          .value
          .set(CompanyNamePage, name)
          .success
          .value
          .set(CompanyPhoneNumberPage, phoneNumber)
          .success
          .value
          .set(CompanyUtrPage, taxpayerReference)
          .success
          .value
          .set(CompanyUtrYesNoPage, true)
          .success
          .value
          .set(CompanyWorksReferencePage, worksReferenceNumber)
          .success
          .value
          .set(CompanyWorksReferenceYesNoPage, true)
          .success
          .value
          .set(PartnershipAddressPage, address)
          .success
          .value
          .set(PartnershipAddressYesNoPage, true)
          .success
          .value
          .set(PartnershipChooseContactDetailsPage, ContactOptions.Email)
          .success
          .value
          .set(PartnershipEmailAddressPage, email)
          .success
          .value
          .set(PartnershipHasUtrYesNoPage, true)
          .success
          .value
          .set(PartnershipMobileNumberPage, phoneNumber)
          .success
          .value
          .set(PartnershipNamePage, name)
          .success
          .value
          .set(PartnershipNominatedPartnerCrnPage, crn)
          .success
          .value
          .set(PartnershipNominatedPartnerCrnYesNoPage, true)
          .success
          .value
          .set(PartnershipNominatedPartnerNamePage, "John")
          .success
          .value
          .set(PartnershipNominatedPartnerNinoPage, nationalInsuranceNumber)
          .success
          .value
          .set(PartnershipNominatedPartnerNinoYesNoPage, true)
          .success
          .value
          .set(PartnershipNominatedPartnerUtrPage, utr)
          .success
          .value
          .set(PartnershipNominatedPartnerUtrYesNoPage, true)
          .success
          .value
          .set(PartnershipPhoneNumberPage, phoneNumber)
          .success
          .value
          .set(PartnershipUniqueTaxpayerReferencePage, taxpayerReference)
          .success
          .value
          .set(PartnershipWorksReferenceNumberPage, worksReferenceNumber)
          .success
          .value
          .set(PartnershipWorksReferenceNumberYesNoPage, true)
          .success
          .value
          .set(TrustAddressPage, address)
          .success
          .value
          .set(TrustAddressYesNoPage, true)
          .success
          .value
          .set(TrustContactOptionsPage, ContactOptions.Email)
          .success
          .value
          .set(TrustEmailAddressPage, email)
          .success
          .value
          .set(TrustMobileNumberPage, phoneNumber)
          .success
          .value
          .set(TrustNamePage, name)
          .success
          .value
          .set(TrustPhoneNumberPage, phoneNumber)
          .success
          .value
          .set(TrustUtrPage, utr)
          .success
          .value
          .set(TrustUtrYesNoPage, true)
          .success
          .value
          .set(TrustWorksReferencePage, worksReferenceNumber)
          .success
          .value
          .set(TrustWorksReferenceYesNoPage, true)
          .success
          .value

      val result = removeAllSubcontractor(userAnswers).success.value

      result.get(TypeOfSubcontractorPage) mustBe None

      result.get(AddressOfSubcontractorPage) mustBe None
      result.get(IndividualChooseContactDetailsPage) mustBe None
      result.get(IndividualEmailAddressPage) mustBe None
      result.get(IndividualMobileNumberPage) mustBe None
      result.get(IndividualPhoneNumberPage) mustBe None
      result.get(NationalInsuranceNumberYesNoPage) mustBe None
      result.get(SubAddressYesNoPage) mustBe None
      result.get(SubcontractorNamePage) mustBe None
      result.get(SubcontractorsUniqueTaxpayerReferencePage) mustBe None
      result.get(SubNationalInsuranceNumberPage) mustBe None
      result.get(SubTradingNameYesNoPage) mustBe None
      result.get(TradingNameOfSubcontractorPage) mustBe None
      result.get(UniqueTaxpayerReferenceYesNoPage) mustBe None
      result.get(WorksReferenceNumberPage) mustBe None
      result.get(WorksReferenceNumberYesNoPage) mustBe None

      result.get(CompanyAddressPage) mustBe None
      result.get(CompanyAddressYesNoPage) mustBe None
      result.get(CompanyContactOptionsPage) mustBe None
      result.get(CompanyCrnPage) mustBe None
      result.get(CompanyCrnYesNoPage) mustBe None
      result.get(CompanyEmailAddressPage) mustBe None
      result.get(CompanyMobileNumberPage) mustBe None
      result.get(CompanyNamePage) mustBe None
      result.get(CompanyPhoneNumberPage) mustBe None
      result.get(CompanyUtrPage) mustBe None
      result.get(CompanyUtrYesNoPage) mustBe None
      result.get(CompanyWorksReferencePage) mustBe None
      result.get(CompanyWorksReferenceYesNoPage) mustBe None

      result.get(PartnershipAddressPage) mustBe None
      result.get(PartnershipAddressYesNoPage) mustBe None
      result.get(PartnershipChooseContactDetailsPage) mustBe None
      result.get(PartnershipEmailAddressPage) mustBe None
      result.get(PartnershipHasUtrYesNoPage) mustBe None
      result.get(PartnershipMobileNumberPage) mustBe None
      result.get(PartnershipNamePage) mustBe None
      result.get(PartnershipNominatedPartnerCrnPage) mustBe None
      result.get(PartnershipNominatedPartnerCrnYesNoPage) mustBe None
      result.get(PartnershipNominatedPartnerNamePage) mustBe None
      result.get(PartnershipNominatedPartnerNinoPage) mustBe None
      result.get(PartnershipNominatedPartnerNinoYesNoPage) mustBe None
      result.get(PartnershipNominatedPartnerUtrPage) mustBe None
      result.get(PartnershipNominatedPartnerUtrYesNoPage) mustBe None
      result.get(PartnershipPhoneNumberPage) mustBe None
      result.get(PartnershipUniqueTaxpayerReferencePage) mustBe None
      result.get(PartnershipWorksReferenceNumberPage) mustBe None
      result.get(PartnershipWorksReferenceNumberYesNoPage) mustBe None

      result.get(TrustAddressPage) mustBe None
      result.get(TrustAddressYesNoPage) mustBe None
      result.get(TrustContactOptionsPage) mustBe None
      result.get(TrustEmailAddressPage) mustBe None
      result.get(TrustMobileNumberPage) mustBe None
      result.get(TrustNamePage) mustBe None
      result.get(TrustPhoneNumberPage) mustBe None
      result.get(TrustUtrPage) mustBe None
      result.get(TrustUtrYesNoPage) mustBe None
      result.get(TrustWorksReferencePage) mustBe None
      result.get(TrustWorksReferenceYesNoPage) mustBe None
    }
  }
}
