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

import base.SpecBase
import models.TypeOfSubcontractor.{Individualorsoletrader, Limitedcompany, Partnership, Trust}
import models.add.SubcontractorName
import models.address.{Address, Country}
import models.amend.OriginalIndividualAnswers
import models.amend.company.OriginalCompanyAnswers
import models.amend.partnership.OriginalPartnershipAnswers
import models.amend.trust.OriginalTrustAnswers
import models.contact.ContactMethodOptions.{Email, Mobile, Phone}
import models.response.SubcontractorResponse
import pages.add.*
import pages.add.company.*
import pages.add.partnership.*
import pages.add.trust.*
import pages.amend.ShowVerificationDetailsPage
import queries.*

class AmendSubcontractorPopulatorSpec extends SpecBase {

  private val cisId             = "INST-123"
  private val subbieResourceRef = 1001L

  private val expectedAddress =
    Address(
      addressLine1 = "12 Harbor View Road",
      addressLine2 = Some("Amity Island"),
      addressLine3 = Some("Bodmin"),
      addressLine4 = Some("Cornwall"),
      postcode = Some("PL31 2HL"),
      country = Some(
        Country(
          code = None,
          name = Some("England")
        )
      )
    )

  private val baseSubcontractor =
    SubcontractorResponse(
      subcontractorId = 1L,
      utr = Some("1123456789"),
      pageVisited = Some(2),
      partnerUtr = Some("2234567890"),
      crn = Some("12345678"),
      firstName = Some("John"),
      nino = Some("AA123456A"),
      secondName = Some("Middle"),
      surname = Some("Smith"),
      partnershipTradingName = Some("Test Partnership"),
      tradingName = Some("Test Trading Name"),
      subcontractorType = Some("soletrader"),
      addressLine1 = Some("12 Harbor View Road"),
      addressLine2 = Some("Amity Island"),
      addressLine3 = Some("Bodmin"),
      addressLine4 = Some("Cornwall"),
      country = Some("England"),
      postcode = Some("PL31 2HL"),
      emailAddress = Some("test@example.com"),
      phoneNumber = Some("02070000000"),
      mobilePhoneNumber = Some("07123456789"),
      worksReferenceNumber = Some("XLS345-MM"),
      createDate = None,
      lastUpdate = None,
      subbieResourceRef = Some(subbieResourceRef),
      matched = Some("Y"),
      autoVerified = Some("N"),
      verified = Some("Y"),
      verificationNumber = Some("V1234567890"),
      taxTreatment = Some("gross"),
      verificationDate = None,
      version = Some(3),
      updatedTaxTreatment = None,
      lastMonthlyReturnDate = None,
      pendingVerifications = Some(0)
    )

  "AmendSubcontractorPopulator" - {

    "IndividualPopulator" - {

      "must populate individual user answers" in {
        val result =
          AmendSubcontractorPopulator.IndividualPopulator
            .populate(emptyUserAnswers, cisId, baseSubcontractor)

        result.isSuccess mustBe true

        val answers = result.get

        answers.get(TypeOfSubcontractorPage).value mustBe Individualorsoletrader
        answers.get(SubTradingNameYesNoPage).value mustBe true
        answers.get(TradingNameOfSubcontractorPage).value mustBe "Test Trading Name"

        answers.get(SubcontractorNamePage).value mustBe SubcontractorName(
          firstName = "John",
          middleName = Some("Middle"),
          lastName = "Smith"
        )

        answers.get(SubAddressYesNoPage).value mustBe true
        answers.get(AddressOfSubcontractorPage).value mustBe expectedAddress

        answers.get(AddIndividualContactMethodsYesNoPage).value mustBe true
        answers.get(IndividualContactMethodOptionsPage).value mustBe Set(Email, Phone, Mobile)
        answers.get(IndividualEmailAddressPage).value mustBe "test@example.com"
        answers.get(IndividualPhoneNumberPage).value mustBe "02070000000"
        answers.get(IndividualMobileNumberPage).value mustBe "07123456789"

        answers.get(UniqueTaxpayerReferenceYesNoPage).value mustBe true
        answers.get(SubcontractorsUniqueTaxpayerReferencePage).value mustBe "1123456789"

        answers.get(NationalInsuranceNumberYesNoPage).value mustBe true
        answers.get(SubNationalInsuranceNumberPage).value mustBe "AA123456A"

        answers.get(WorksReferenceNumberYesNoPage).value mustBe true
        answers.get(WorksReferenceNumberPage).value mustBe "XLS345-MM"

        answers.get(ShowVerificationDetailsPage).value mustBe true
        answers.get(CisIdQuery).value mustBe cisId

        answers.get(OriginalIndividualAnswersQuery).value mustBe OriginalIndividualAnswers(
          usesTradingName = Some(true),
          tradingName = Some("Test Trading Name"),
          subcontractorName = Some(
            SubcontractorName(
              firstName = "John",
              middleName = Some("Middle"),
              lastName = "Smith"
            )
          ),
          address = Some(expectedAddress),
          individualContactMethod = Some(Set(Email, Phone, Mobile)),
          email = Some("test@example.com"),
          phone = Some("02070000000"),
          mobile = Some("07123456789"),
          utr = Some("1123456789"),
          nino = Some("AA123456A"),
          worksReference = Some("XLS345-MM")
        )
      }

      "must set trading name answer to false when trading name is missing" in {
        val subcontractor =
          baseSubcontractor.copy(
            tradingName = None
          )

        val answers =
          AmendSubcontractorPopulator.IndividualPopulator
            .populate(emptyUserAnswers, cisId, subcontractor)
            .get

        answers.get(SubTradingNameYesNoPage).value mustBe false
        answers.get(TradingNameOfSubcontractorPage) mustBe None

        answers.get(OriginalIndividualAnswersQuery).value.usesTradingName mustBe Some(false)
        answers.get(OriginalIndividualAnswersQuery).value.tradingName mustBe None
      }

      "must not populate subcontractor name when first name or surname is missing" in {
        val subcontractor =
          baseSubcontractor.copy(
            firstName = None,
            surname = Some("Smith")
          )

        val answers =
          AmendSubcontractorPopulator.IndividualPopulator
            .populate(emptyUserAnswers, cisId, subcontractor)
            .get

        answers.get(SubcontractorNamePage) mustBe None
        answers.get(OriginalIndividualAnswersQuery).value.subcontractorName mustBe None
      }
    }

    "CompanyPopulator" - {

      "must populate company user answers" in {
        val subcontractor =
          baseSubcontractor.copy(
            subcontractorType = Some("company")
          )

        val result =
          AmendSubcontractorPopulator.CompanyPopulator
            .populate(emptyUserAnswers, cisId, subcontractor)

        result.isSuccess mustBe true

        val answers = result.get

        answers.get(TypeOfSubcontractorPage).value mustBe Limitedcompany
        answers.get(CompanyNamePage).value mustBe "Test Trading Name"

        answers.get(CompanyAddressYesNoPage).value mustBe true
        answers.get(CompanyAddressPage).value mustBe expectedAddress

        answers.get(AddCompanyContactMethodsYesNoPage).value mustBe true
        answers.get(CompanyContactMethodOptionsPage).value mustBe Set(Email, Phone, Mobile)
        answers.get(CompanyEmailAddressPage).value mustBe "test@example.com"
        answers.get(CompanyPhoneNumberPage).value mustBe "02070000000"
        answers.get(CompanyMobileNumberPage).value mustBe "07123456789"

        answers.get(CompanyUtrYesNoPage).value mustBe true
        answers.get(CompanyUtrPage).value mustBe "1123456789"

        answers.get(CompanyCrnYesNoPage).value mustBe true
        answers.get(CompanyCrnPage).value mustBe "12345678"

        answers.get(CompanyWorksReferenceYesNoPage).value mustBe true
        answers.get(CompanyWorksReferencePage).value mustBe "XLS345-MM"

        answers.get(ShowVerificationDetailsPage).value mustBe true
        answers.get(CisIdQuery).value mustBe cisId

        answers.get(OriginalCompanyAnswersQuery).value mustBe OriginalCompanyAnswers(
          companyName = Some("Test Trading Name"),
          addressYesNo = Some(true),
          address = Some(expectedAddress),
          companyContactMethodsYesNo = Some(true),
          companyContactMethod = Set(Email, Phone, Mobile),
          email = Some("test@example.com"),
          phone = Some("02070000000"),
          mobile = Some("07123456789"),
          crnYesNo = Some(true),
          crn = Some("12345678"),
          utrYesNo = Some(true),
          utr = Some("1123456789"),
          worksReferenceYesNo = Some(true),
          worksReference = Some("XLS345-MM"),
          verificationNumber = Some("V1234567890")
        )
      }

      "must set optional company answers to false when values are missing" in {
        val subcontractor =
          baseSubcontractor.copy(
            tradingName = None,
            utr = None,
            crn = None,
            worksReferenceNumber = None
          )

        val answers =
          AmendSubcontractorPopulator.CompanyPopulator
            .populate(emptyUserAnswers, cisId, subcontractor)
            .get

        answers.get(CompanyNamePage) mustBe None

        answers.get(CompanyUtrYesNoPage).value mustBe false
        answers.get(CompanyUtrPage) mustBe None

        answers.get(CompanyCrnYesNoPage).value mustBe false
        answers.get(CompanyCrnPage) mustBe None

        answers.get(CompanyWorksReferenceYesNoPage).value mustBe false
        answers.get(CompanyWorksReferencePage) mustBe None

        val original = answers.get(OriginalCompanyAnswersQuery).value

        original.companyName mustBe None
        original.utrYesNo mustBe Some(false)
        original.utr mustBe None
        original.crnYesNo mustBe Some(false)
        original.crn mustBe None
        original.worksReferenceYesNo mustBe Some(false)
        original.worksReference mustBe None
      }
    }

    "PartnershipPopulator" - {

      "must populate partnership user answers" in {
        val subcontractor =
          baseSubcontractor.copy(
            subcontractorType = Some("partnership")
          )

        val result =
          AmendSubcontractorPopulator.PartnershipPopulator
            .populate(emptyUserAnswers, cisId, subcontractor)

        result.isSuccess mustBe true

        val answers = result.get

        answers.get(TypeOfSubcontractorPage).value mustBe Partnership
        answers.get(PartnershipNamePage).value mustBe "Test Partnership"

        answers.get(PartnershipAddressYesNoPage).value mustBe true
        answers.get(PartnershipAddressPage).value mustBe expectedAddress

        answers.get(AddPartnershipContactMethodsYesNoPage).value mustBe true
        answers.get(PartnershipContactMethodOptionsPage).value mustBe Set(Email, Phone, Mobile)
        answers.get(PartnershipEmailAddressPage).value mustBe "test@example.com"
        answers.get(PartnershipPhoneNumberPage).value mustBe "02070000000"
        answers.get(PartnershipMobileNumberPage).value mustBe "07123456789"

        answers.get(PartnershipHasUtrYesNoPage).value mustBe true
        answers.get(PartnershipUniqueTaxpayerReferencePage).value mustBe "1123456789"

        answers.get(PartnershipNominatedPartnerNamePage).value mustBe "Test Trading Name"

        answers.get(PartnershipNominatedPartnerUtrYesNoPage).value mustBe true
        answers.get(PartnershipNominatedPartnerUtrPage).value mustBe "2234567890"

        answers.get(PartnershipNominatedPartnerNinoYesNoPage).value mustBe true
        answers.get(PartnershipNominatedPartnerNinoPage).value mustBe "AA123456A"

        answers.get(PartnershipNominatedPartnerCrnYesNoPage).value mustBe true
        answers.get(PartnershipNominatedPartnerCrnPage).value mustBe "12345678"

        answers.get(PartnershipWorksReferenceNumberYesNoPage).value mustBe true
        answers.get(PartnershipWorksReferenceNumberPage).value mustBe "XLS345-MM"

        answers.get(ShowVerificationDetailsPage).value mustBe true
        answers.get(CisIdQuery).value mustBe cisId

        answers.get(OriginalPartnershipAnswersQuery).value mustBe OriginalPartnershipAnswers(
          partnershipName = Some("Test Partnership"),
          addressYesNo = Some(true),
          address = Some(expectedAddress),
          partnershipContactMethodsYesNo = Some(true),
          partnershipContactMethodOptions = Set(Email, Phone, Mobile),
          email = Some("test@example.com"),
          phone = Some("02070000000"),
          mobile = Some("07123456789"),
          hasUtrYesNo = Some(true),
          utr = Some("1123456789"),
          nominatedPartnerName = Some("Test Trading Name"),
          nominatedPartnerUtrYesNo = Some(true),
          nominatedPartnerUtr = Some("2234567890"),
          nominatedPartnerNinoYesNo = Some(true),
          nominatedPartnerNino = Some("AA123456A"),
          nominatedPartnerCrnYesNo = Some(true),
          nominatedPartnerCrn = Some("12345678"),
          nominatedPartnerWorksReferenceYesNo = Some(true),
          nominatedPartnerWorksReference = Some("XLS345-MM"),
          verificationNumber = Some("V1234567890")
        )
      }

    }

    "TrustPopulator" - {

      "must populate trust user answers" in {
        val subcontractor =
          baseSubcontractor.copy(
            subcontractorType = Some("trust"),
            tradingName = Some("Test Trust"),
            partnershipTradingName = None
          )

        val result =
          AmendSubcontractorPopulator.TrustPopulator
            .populate(emptyUserAnswers, cisId, subcontractor)

        result.isSuccess mustBe true

        val answers = result.get

        answers.get(TypeOfSubcontractorPage).value mustBe Trust
        answers.get(TrustNamePage).value mustBe "Test Trust"

        answers.get(TrustAddressYesNoPage).value mustBe true
        answers.get(TrustAddressPage).value mustBe expectedAddress

        answers.get(AddTrustContactMethodsYesNoPage).value mustBe true
        answers.get(TrustContactMethodOptionsPage).value mustBe Set(Email, Phone, Mobile)
        answers.get(TrustEmailAddressPage).value mustBe "test@example.com"
        answers.get(TrustPhoneNumberPage).value mustBe "02070000000"
        answers.get(TrustMobileNumberPage).value mustBe "07123456789"

        answers.get(TrustUtrYesNoPage).value mustBe true
        answers.get(TrustUtrPage).value mustBe "1123456789"

        answers.get(TrustWorksReferenceYesNoPage).value mustBe true
        answers.get(TrustWorksReferencePage).value mustBe "XLS345-MM"

        answers.get(ShowVerificationDetailsPage).value mustBe true
        answers.get(CisIdQuery).value mustBe cisId

        answers.get(OriginalTrustAnswersQuery).value mustBe OriginalTrustAnswers(
          trustName = Some("Test Trust"),
          addressYesNo = Some(true),
          address = Some(expectedAddress),
          trustContactMethodsYesNo = Some(true),
          trustContactMethod = Set(Email, Phone, Mobile),
          email = Some("test@example.com"),
          phone = Some("02070000000"),
          mobile = Some("07123456789"),
          utrYesNo = Some(true),
          utr = Some("1123456789"),
          worksReferenceYesNo = Some(true),
          worksReference = Some("XLS345-MM"),
          verificationNumber = Some("V1234567890")
        )
      }

      "must use partnershipTradingName as trust name when tradingName is missing" in {
        val subcontractor =
          baseSubcontractor.copy(
            tradingName = None,
            partnershipTradingName = Some("Fallback Trust")
          )

        val answers =
          AmendSubcontractorPopulator.TrustPopulator
            .populate(emptyUserAnswers, cisId, subcontractor)
            .get

        answers.get(TrustNamePage).value mustBe "Fallback Trust"
        answers.get(OriginalTrustAnswersQuery).value.trustName mustBe Some("Fallback Trust")
      }

      "must not populate trust name when both name fields are missing" in {
        val subcontractor =
          baseSubcontractor.copy(
            tradingName = None,
            partnershipTradingName = None
          )

        val answers =
          AmendSubcontractorPopulator.TrustPopulator
            .populate(emptyUserAnswers, cisId, subcontractor)
            .get

        answers.get(TrustNamePage) mustBe None
        answers.get(OriginalTrustAnswersQuery).value.trustName mustBe None
      }
    }

    "shared helper behaviour" - {

      "must set address answer to false and leave address pages empty when address line 1 is missing" in {
        val subcontractor =
          baseSubcontractor.copy(
            addressLine1 = None,
            addressLine2 = None,
            addressLine3 = None,
            addressLine4 = None,
            postcode = None,
            country = None
          )

        val answers =
          AmendSubcontractorPopulator.TrustPopulator
            .populate(emptyUserAnswers, cisId, subcontractor)
            .get

        answers.get(TrustAddressYesNoPage).value mustBe false
        answers.get(TrustAddressPage) mustBe None

        val original = answers.get(OriginalTrustAnswersQuery).value

        original.addressYesNo mustBe Some(false)
        original.address mustBe None
      }

      "must create country with no country code when country name is returned" in {
        val answers =
          AmendSubcontractorPopulator.TrustPopulator
            .populate(emptyUserAnswers, cisId, baseSubcontractor)
            .get

        val country =
          answers
            .get(TrustAddressPage)
            .value
            .country
            .value

        country.code mustBe None
        country.name mustBe Some("England")
      }

      "must set contact method answer to false and leave contact pages empty when no contact details are returned" in {
        val subcontractor =
          baseSubcontractor.copy(
            emailAddress = None,
            phoneNumber = None,
            mobilePhoneNumber = None
          )

        val answers =
          AmendSubcontractorPopulator.TrustPopulator
            .populate(emptyUserAnswers, cisId, subcontractor)
            .get

        answers.get(AddTrustContactMethodsYesNoPage).value mustBe false
        answers.get(TrustContactMethodOptionsPage) mustBe None
        answers.get(TrustEmailAddressPage) mustBe None
        answers.get(TrustPhoneNumberPage) mustBe None
        answers.get(TrustMobileNumberPage) mustBe None

        val original = answers.get(OriginalTrustAnswersQuery).value

        original.trustContactMethodsYesNo mustBe Some(false)
        original.trustContactMethod mustBe Set.empty
        original.email mustBe None
        original.phone mustBe None
        original.mobile mustBe None
      }

      "must show verification details when subcontractor is verified" in {
        val subcontractor =
          baseSubcontractor.copy(
            verified = Some("Y"),
            pendingVerifications = Some(0)
          )

        val answers =
          AmendSubcontractorPopulator.TrustPopulator
            .populate(emptyUserAnswers, cisId, subcontractor)
            .get

        answers.get(ShowVerificationDetailsPage).value mustBe true
      }

      "must show verification details when subcontractor has pending verification" in {
        val subcontractor =
          baseSubcontractor.copy(
            verified = Some("N"),
            pendingVerifications = Some(1)
          )

        val answers =
          AmendSubcontractorPopulator.TrustPopulator
            .populate(emptyUserAnswers, cisId, subcontractor)
            .get

        answers.get(ShowVerificationDetailsPage).value mustBe true
      }

      "must hide verification details when subcontractor is not verified and has no pending verification" in {
        val subcontractor =
          baseSubcontractor.copy(
            verified = Some("N"),
            pendingVerifications = Some(0)
          )

        val answers =
          AmendSubcontractorPopulator.TrustPopulator
            .populate(emptyUserAnswers, cisId, subcontractor)
            .get

        answers.get(ShowVerificationDetailsPage).value mustBe false
      }
    }
  }
}
