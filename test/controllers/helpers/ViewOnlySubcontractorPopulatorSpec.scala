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
import models.TypeOfSubcontractor
import models.TypeOfSubcontractor.{Individualorsoletrader, Limitedcompany, Partnership, Trust}
import models.address.{Address, Country}
import models.contact.ContactMethodOptions
import models.response.SubcontractorResponse
import org.scalatest.matchers.must.Matchers
import queries.{ViewOnlyCompanyAnswersQuery, ViewOnlyIndividualAnswersQuery, ViewOnlyPartnershipAnswersQuery, ViewOnlyTrustAnswersQuery}

class ViewOnlySubcontractorPopulatorSpec extends SpecBase with Matchers {

  private val subcontractor =
    SubcontractorResponse(
      subcontractorId = 1L,
      utr = Some("11111111"),
      pageVisited = Some(1),
      partnerUtr = Some("22222222"),
      crn = Some("12345678"),
      firstName = Some("John"),
      nino = Some("AB123456C"),
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
      emailAddress = Some("test@test.com"),
      phoneNumber = Some("02070000000"),
      mobilePhoneNumber = Some("07123456789"),
      worksReferenceNumber = Some("WRN-11"),
      createDate = None,
      lastUpdate = None,
      subbieResourceRef = Some(123L),
      matched = Some("matched"),
      autoVerified = Some("Y"),
      verified = Some("Y"),
      verificationNumber = Some("VRN123456"),
      taxTreatment = None,
      verificationDate = None,
      version = Some(1),
      updatedTaxTreatment = None,
      lastMonthlyReturnDate = None,
      pendingVerifications = Some(0)
    )

  private val address =
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

  "ViewOnlySubcontractorPopulator.populate" - {

    "must populate ViewOnlyIndividualAnswers for an individual subcontractor" in {

      val result =
        ViewOnlySubcontractorPopulator.populate(
          emptyUserAnswers,
          Individualorsoletrader,
          subcontractor
        )

      result.isSuccess mustBe true

      val answers =
        result.get
          .get(ViewOnlyIndividualAnswersQuery)
          .value

      answers.subcontractorType mustBe Individualorsoletrader
      answers.showVerificationDetails mustBe true

      answers.usesTradingName mustBe Some(true)
      answers.tradingName mustBe Some("Test Trading Name")

      answers.subcontractorName.value.firstName mustBe "John"
      answers.subcontractorName.value.middleName mustBe Some("Middle")
      answers.subcontractorName.value.lastName mustBe "Smith"

      answers.addressYesNo mustBe Some(true)
      answers.address mustBe Some(address)

      answers.individualContactMethodsYesNo mustBe Some(true)

      answers.individualContactMethod mustBe Set(
        ContactMethodOptions.Email,
        ContactMethodOptions.Phone,
        ContactMethodOptions.Mobile
      )

      answers.email mustBe Some("test@test.com")
      answers.phone mustBe Some("02070000000")
      answers.mobile mustBe Some("07123456789")

      answers.utrYesNo mustBe Some(true)
      answers.utr mustBe Some("11111111")

      answers.ninoYesNo mustBe Some(true)
      answers.nino mustBe Some("AB123456C")

      answers.worksReferenceYesNo mustBe Some(true)
      answers.worksReference mustBe Some("WRN-11")

      answers.verificationNumber mustBe Some("VRN123456")
    }

    "must populate ViewOnlyCompanyAnswers for a limited company" in {

      val companyResponse =
        subcontractor.copy(
          subcontractorType = Some("company")
        )

      val result =
        ViewOnlySubcontractorPopulator.populate(
          emptyUserAnswers,
          Limitedcompany,
          companyResponse
        )

      result.isSuccess mustBe true

      val answers =
        result.get
          .get(ViewOnlyCompanyAnswersQuery)
          .value

      answers.subcontractorType mustBe Limitedcompany
      answers.showVerificationDetails mustBe true

      answers.companyName mustBe Some("Test Trading Name")

      answers.addressYesNo mustBe Some(true)
      answers.address mustBe Some(address)

      answers.companyContactMethodsYesNo mustBe Some(true)

      answers.companyContactMethod mustBe Set(
        ContactMethodOptions.Email,
        ContactMethodOptions.Phone,
        ContactMethodOptions.Mobile
      )

      answers.email mustBe Some("test@test.com")
      answers.phone mustBe Some("02070000000")
      answers.mobile mustBe Some("07123456789")

      answers.crnYesNo mustBe Some(true)
      answers.crn mustBe Some("12345678")

      answers.utrYesNo mustBe Some(true)
      answers.utr mustBe Some("11111111")

      answers.worksReferenceYesNo mustBe Some(true)
      answers.worksReference mustBe Some("WRN-11")

      answers.verificationNumber mustBe Some("VRN123456")
    }

    "must populate ViewOnlyPartnershipAnswers for a partnership" in {

      val partnershipResponse =
        subcontractor.copy(
          subcontractorType = Some("partnership")
        )

      val result =
        ViewOnlySubcontractorPopulator.populate(
          emptyUserAnswers,
          Partnership,
          partnershipResponse
        )

      result.isSuccess mustBe true

      val answers =
        result.get
          .get(ViewOnlyPartnershipAnswersQuery)
          .value

      answers.subcontractorType mustBe Partnership
      answers.showVerificationDetails mustBe true

      answers.partnershipName mustBe Some("Test Partnership")

      answers.addressYesNo mustBe Some(true)
      answers.address mustBe Some(address)

      answers.partnershipContactMethodsYesNo mustBe Some(true)

      answers.partnershipContactMethodOptions mustBe Set(
        ContactMethodOptions.Email,
        ContactMethodOptions.Phone,
        ContactMethodOptions.Mobile
      )

      answers.email mustBe Some("test@test.com")
      answers.phone mustBe Some("02070000000")
      answers.mobile mustBe Some("07123456789")

      answers.hasUtrYesNo mustBe Some(true)
      answers.utr mustBe Some("11111111")

      answers.nominatedPartnerName mustBe Some("Test Trading Name")

      answers.nominatedPartnerUtrYesNo mustBe Some(true)
      answers.nominatedPartnerUtr mustBe Some("22222222")

      answers.nominatedPartnerNinoYesNo mustBe Some(true)
      answers.nominatedPartnerNino mustBe Some("AB123456C")

      answers.nominatedPartnerCrnYesNo mustBe Some(true)
      answers.nominatedPartnerCrn mustBe Some("12345678")

      answers.nominatedPartnerWorksReferenceYesNo mustBe Some(true)
      answers.nominatedPartnerWorksReference mustBe Some("WRN-11")

      answers.verificationNumber mustBe Some("VRN123456")
    }

    "must populate ViewOnlyTrustAnswers for a trust" in {

      val trustResponse =
        subcontractor.copy(
          subcontractorType = Some("trust")
        )

      val result =
        ViewOnlySubcontractorPopulator.populate(
          emptyUserAnswers,
          Trust,
          trustResponse
        )

      result.isSuccess mustBe true

      val answers =
        result.get
          .get(ViewOnlyTrustAnswersQuery)
          .value

      answers.subcontractorType mustBe Trust
      answers.showVerificationDetails mustBe true

      answers.trustName mustBe Some("Test Trading Name")

      answers.addressYesNo mustBe Some(true)
      answers.address mustBe Some(address)

      answers.trustContactMethodsYesNo mustBe Some(true)

      answers.trustContactMethod mustBe Set(
        ContactMethodOptions.Email,
        ContactMethodOptions.Phone,
        ContactMethodOptions.Mobile
      )

      answers.email mustBe Some("test@test.com")
      answers.phone mustBe Some("02070000000")
      answers.mobile mustBe Some("07123456789")

      answers.utrYesNo mustBe Some(true)
      answers.utr mustBe Some("11111111")

      answers.worksReferenceYesNo mustBe Some(true)
      answers.worksReference mustBe Some("WRN-11")

      answers.verificationNumber mustBe Some("VRN123456")
    }

    "must use partnership trading name as trust name when trading name is missing" in {

      val trustResponse =
        subcontractor.copy(
          subcontractorType = Some("trust"),
          tradingName = None,
          partnershipTradingName = Some("Partnership Trust Name")
        )

      val result =
        ViewOnlySubcontractorPopulator.populate(
          emptyUserAnswers,
          Trust,
          trustResponse
        )

      result.isSuccess mustBe true

      val answers =
        result.get
          .get(ViewOnlyTrustAnswersQuery)
          .value

      answers.trustName mustBe Some("Partnership Trust Name")
    }

    "must use trading name as trust name when both trading name and partnership trading name are present" in {

      val trustResponse =
        subcontractor.copy(
          subcontractorType = Some("trust"),
          tradingName = Some("Trust Trading Name"),
          partnershipTradingName = Some("Partnership Trust Name")
        )

      val result =
        ViewOnlySubcontractorPopulator.populate(
          emptyUserAnswers,
          Trust,
          trustResponse
        )

      result.isSuccess mustBe true

      val answers =
        result.get
          .get(ViewOnlyTrustAnswersQuery)
          .value

      answers.trustName mustBe Some("Trust Trading Name")
    }

    "must set contact methods to empty when no contact details are present" in {

      val response =
        subcontractor.copy(
          emailAddress = None,
          phoneNumber = None,
          mobilePhoneNumber = None
        )

      val result =
        ViewOnlySubcontractorPopulator.populate(
          emptyUserAnswers,
          Individualorsoletrader,
          response
        )

      result.isSuccess mustBe true

      val answers =
        result.get
          .get(ViewOnlyIndividualAnswersQuery)
          .value

      answers.individualContactMethodsYesNo mustBe Some(false)
      answers.individualContactMethod mustBe empty
    }

    "must set address to None when address line 1 is missing" in {

      val response =
        subcontractor.copy(
          addressLine1 = None,
          addressLine2 = None,
          addressLine3 = None,
          addressLine4 = None,
          postcode = None,
          country = None
        )

      val result =
        ViewOnlySubcontractorPopulator.populate(
          emptyUserAnswers,
          Individualorsoletrader,
          response
        )

      result.isSuccess mustBe true

      val answers =
        result.get
          .get(ViewOnlyIndividualAnswersQuery)
          .value

      answers.addressYesNo mustBe Some(false)
      answers.address mustBe None
    }

    "must set UTR, NINO and works reference yes-no values to false when values are missing" in {

      val response =
        subcontractor.copy(
          utr = None,
          nino = None,
          worksReferenceNumber = None
        )

      val result =
        ViewOnlySubcontractorPopulator.populate(
          emptyUserAnswers,
          Individualorsoletrader,
          response
        )

      result.isSuccess mustBe true

      val answers =
        result.get
          .get(ViewOnlyIndividualAnswersQuery)
          .value

      answers.utrYesNo mustBe Some(false)
      answers.utr mustBe None

      answers.ninoYesNo mustBe Some(false)
      answers.nino mustBe None

      answers.worksReferenceYesNo mustBe Some(false)
      answers.worksReference mustBe None
    }

    "must populate verification details when the subcontractor is verified" in {

      val verifiedResponse =
        subcontractor.copy(
          verified = Some("Y"),
          autoVerified = Some("Y"),
          verificationNumber = Some("VRN123456")
        )

      val result =
        ViewOnlySubcontractorPopulator.populate(
          emptyUserAnswers,
          Limitedcompany,
          verifiedResponse
        )

      result.isSuccess mustBe true

      val answers =
        result.get
          .get(ViewOnlyCompanyAnswersQuery)
          .value

      answers.showVerificationDetails mustBe true
      answers.verificationNumber mustBe Some("VRN123456")
    }

    "must not show verification details when the subcontractor is not verified" in {

      val unverifiedResponse =
        subcontractor.copy(
          verified = None,
          autoVerified = None,
          verificationNumber = None
        )

      val result =
        ViewOnlySubcontractorPopulator.populate(
          emptyUserAnswers,
          Limitedcompany,
          unverifiedResponse
        )

      result.isSuccess mustBe true

      val answers =
        result.get
          .get(ViewOnlyCompanyAnswersQuery)
          .value

      answers.showVerificationDetails mustBe false
      answers.verificationNumber mustBe None
    }
  }
}
