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
import models.UserAnswers
import models.add.SubcontractorName
import models.address.{Address, Country}
import models.amend.OriginalIndividualAnswers
import models.amend.company.OriginalCompanyAnswers
import models.amend.trust.OriginalTrustAnswers
import models.contact.ContactMethodOptions
import pages.add.*
import pages.add.company.*
import pages.add.trust.*
import queries.{OriginalCompanyAnswersQuery, OriginalIndividualAnswersQuery, OriginalTrustAnswersQuery}

class AmendmentHelperSpec extends SpecBase {

  private val address =
    Address(
      addressLine1 = "12 High Street",
      addressLine2 = Some("Line 2"),
      addressLine3 = None,
      addressLine4 = None,
      postcode = Some("AB1 2CD"),
      country = Some(Country(None, Some("England")))
    )

  private val original =
    OriginalCompanyAnswers(
      companyName = Some("ABC Ltd"),
      addressYesNo = Some(true),
      address = Some(address),
      companyContactMethodsYesNo = Some(true),
      companyContactMethod = Set(ContactMethodOptions.Email),
      email = Some("test@test.com"),
      phone = Some("0123456789"),
      mobile = Some("07123456789"),
      crnYesNo = Some(true),
      crn = Some("12345678"),
      utrYesNo = Some(true),
      utr = Some("1111111111"),
      worksReferenceYesNo = Some(true),
      worksReference = Some("WRN123"),
      verificationNumber = Some("VRN123")
    )

  private val userAnswers =
    emptyUserAnswers
      .set(OriginalCompanyAnswersQuery, original)
      .success
      .value
      .set(CompanyNamePage, "ABC Ltd")
      .success
      .value
      .set(CompanyAddressYesNoPage, true)
      .success
      .value
      .set(CompanyAddressPage, address)
      .success
      .value
      .set(AddCompanyContactMethodsYesNoPage, true)
      .success
      .value
      .set(CompanyContactMethodOptionsPage, Set(ContactMethodOptions.Email))
      .success
      .value
      .set(CompanyEmailAddressPage, "test@test.com")
      .success
      .value
      .set(CompanyPhoneNumberPage, "0123456789")
      .success
      .value
      .set(CompanyMobileNumberPage, "07123456789")
      .success
      .value
      .set(CompanyUtrYesNoPage, true)
      .success
      .value
      .set(CompanyUtrPage, "1111111111")
      .success
      .value
      .set(CompanyCrnYesNoPage, true)
      .success
      .value
      .set(CompanyCrnPage, "12345678")
      .success
      .value
      .set(CompanyWorksReferenceYesNoPage, true)
      .success
      .value
      .set(CompanyWorksReferencePage, "WRN123")
      .success
      .value

  private val originalTrust =
    OriginalTrustAnswers(
      trustName = Some("ABC Trust"),
      addressYesNo = Some(true),
      address = Some(address),
      trustContactMethodsYesNo = Some(true),
      trustContactMethod = Set(ContactMethodOptions.Email),
      email = Some("test@test.com"),
      phone = Some("0123456789"),
      mobile = Some("07123456789"),
      utrYesNo = Some(true),
      utr = Some("1111111111"),
      worksReferenceYesNo = Some(true),
      worksReference = Some("WRN123"),
      verificationNumber = Some("VRN123")
    )

  private val trustUserAnswers: UserAnswers =
    emptyUserAnswers
      .set(OriginalTrustAnswersQuery, originalTrust)
      .success
      .value
      .set(TrustNamePage, "ABC Trust")
      .success
      .value
      .set(TrustAddressYesNoPage, true)
      .success
      .value
      .set(TrustAddressPage, address)
      .success
      .value
      .set(AddTrustContactMethodsYesNoPage, true)
      .success
      .value
      .set(TrustContactMethodOptionsPage, Set(ContactMethodOptions.Email))
      .success
      .value
      .set(TrustEmailAddressPage, "test@test.com")
      .success
      .value
      .set(TrustPhoneNumberPage, "0123456789")
      .success
      .value
      .set(TrustMobileNumberPage, "07123456789")
      .success
      .value
      .set(TrustUtrYesNoPage, true)
      .success
      .value
      .set(TrustUtrPage, "1111111111")
      .success
      .value
      .set(TrustWorksReferenceYesNoPage, true)
      .success
      .value
      .set(TrustWorksReferencePage, "WRN123")
      .success
      .value

  private val originalIndividual =
    OriginalIndividualAnswers(
      usesTradingName = Some(false),
      tradingName = None,
      subcontractorName = Some(
        SubcontractorName(
          firstName = "John",
          middleName = None,
          lastName = "Smith"
        )
      ),
      addressYesNo = Some(true),
      address = Some(address),
      individualContactMethodsYesNo = Some(true),
      individualContactMethod = Set(ContactMethodOptions.Email),
      email = Some("test@test.com"),
      phone = Some("0123456789"),
      mobile = Some("07123456789"),
      utrYesNo = Some(true),
      utr = Some("1111111111"),
      ninoYesNo = Some(false),
      nino = None,
      worksReferenceYesNo = Some(true),
      worksReference = Some("WRN123"),
      verificationNumber = Some("VRN123")
    )

  private val individualUserAnswers =
    emptyUserAnswers
      .set(OriginalIndividualAnswersQuery, originalIndividual)
      .success
      .value
      .set(SubTradingNameYesNoPage, false)
      .success
      .value
      .set(
        SubcontractorNamePage,
        SubcontractorName(
          firstName = "John",
          middleName = None,
          lastName = "Smith"
        )
      )
      .success
      .value
      .set(SubAddressYesNoPage, true)
      .success
      .value
      .set(AddressOfSubcontractorPage, address)
      .success
      .value
      .set(AddIndividualContactMethodsYesNoPage, true)
      .success
      .value
      .set(IndividualContactMethodOptionsPage, Set(ContactMethodOptions.Email))
      .success
      .value
      .set(IndividualEmailAddressPage, "test@test.com")
      .success
      .value
      .set(IndividualPhoneNumberPage, "0123456789")
      .success
      .value
      .set(IndividualMobileNumberPage, "07123456789")
      .success
      .value
      .set(UniqueTaxpayerReferenceYesNoPage, true)
      .success
      .value
      .set(SubcontractorsUniqueTaxpayerReferencePage, "1111111111")
      .success
      .value
      .set(NationalInsuranceNumberYesNoPage, false)
      .success
      .value
      .set(WorksReferenceNumberYesNoPage, true)
      .success
      .value
      .set(WorksReferenceNumberPage, "WRN123")
      .success
      .value
  "AmendmentHelper" - {

    "companyHasChanges" - {

      "must return false when there are no original answers" in {
        AmendmentHelper.companyHasChanges(emptyUserAnswers) mustBe false
      }

      "must return false when no fields have changed" in {
        AmendmentHelper.companyHasChanges(userAnswers) mustBe false
      }

      "must return true when a field has changed" in {
        val updated =
          userAnswers
            .set(CompanyNamePage, "XYZ Ltd")
            .success
            .value

        AmendmentHelper.companyHasChanges(updated) mustBe true
      }
    }

    "trustHasChanges" - {

      "must return false when there are no original answers" in {
        AmendmentHelper.trustHasChanges(emptyUserAnswers) mustBe false
      }

      "must return false when no fields have changed" in {
        AmendmentHelper.trustHasChanges(trustUserAnswers) mustBe false
      }

      "must return true when a field has changed" in {
        val updated =
          trustUserAnswers
            .set(TrustNamePage, "XYZ Trust")
            .success
            .value

        AmendmentHelper.trustHasChanges(updated) mustBe true
      }
    }

    "individualHasChanges" - {

      "must return false when there are no original answers" in {
        AmendmentHelper.individualHasChanges(emptyUserAnswers) mustBe false
      }

      "must return false when no fields have changed" in {
        AmendmentHelper.individualHasChanges(individualUserAnswers) mustBe false
      }

      "must return true when a field has changed" in {
        val updated =
          individualUserAnswers
            .set(SubcontractorsUniqueTaxpayerReferencePage, "2222222222")
            .success
            .value

        AmendmentHelper.individualHasChanges(updated) mustBe true
      }
    }
  }

}
