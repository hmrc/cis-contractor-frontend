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
import models.add.SubcontractorName
import models.address.{Address, Country}
import models.contact.ContactMethodOptions
import models.response.SubcontractorResponse
import org.scalatest.matchers.must.Matchers

class SubcontractorPopulatorUtilsSpec extends SpecBase with Matchers {

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
      autoVerified = Some("true"),
      verified = Some("true"),
      verificationNumber = Some("VRN123456"),
      taxTreatment = None,
      verificationDate = None,
      version = Some(1),
      updatedTaxTreatment = None,
      lastMonthlyReturnDate = None,
      pendingVerifications = Some(0)
    )

  "SubcontractorPopulatorUtils.toAddress" - {

    "must return an address when address line 1 is present" in {

      SubcontractorPopulatorUtils.toAddress(subcontractor) mustBe Some(
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
      )
    }

    "must return None when address line 1 is missing" in {

      val response =
        subcontractor.copy(
          addressLine1 = None
        )

      SubcontractorPopulatorUtils.toAddress(response) mustBe None
    }

    "must preserve optional address fields when address line 1 is present" in {

      val response =
        subcontractor.copy(
          addressLine2 = None,
          addressLine3 = None,
          addressLine4 = None,
          postcode = None,
          country = None
        )

      SubcontractorPopulatorUtils.toAddress(response) mustBe Some(
        Address(
          addressLine1 = "12 Harbor View Road",
          addressLine2 = None,
          addressLine3 = None,
          addressLine4 = None,
          postcode = None,
          country = None
        )
      )
    }
  }

  "SubcontractorPopulatorUtils.addressFieldsExist" - {

    "must return true when subcontractor has address" in {
      SubcontractorPopulatorUtils.addressFieldsExist(subcontractor) mustBe true
    }

    "must return false when subcontractor do not has address" in {

      val response =
        subcontractor.copy(
          addressLine1 = None,
          addressLine2 = None,
          addressLine3 = None,
          addressLine4 = None,
          postcode = None,
          country = None
        )

      SubcontractorPopulatorUtils.addressFieldsExist(response) mustBe false
    }

    "must return false when address line 1 is missing" in {
      val response =
        subcontractor.copy(
          addressLine1 = None
        )

      SubcontractorPopulatorUtils.addressFieldsExist(response) mustBe false
    }
  }

  "SubcontractorPopulatorUtils.contactMethods" - {

    "must return all available contact methods" in {

      SubcontractorPopulatorUtils.contactMethods(
        subcontractor
      ) mustBe Set(
        ContactMethodOptions.Email,
        ContactMethodOptions.Phone,
        ContactMethodOptions.Mobile
      )
    }

    "must return only the available contact methods" in {

      val response =
        subcontractor.copy(
          emailAddress = Some("test@test.com"),
          phoneNumber = None,
          mobilePhoneNumber = None
        )

      SubcontractorPopulatorUtils.contactMethods(
        response
      ) mustBe Set(
        ContactMethodOptions.Email
      )
    }

    "must return an empty set when no contact details are present" in {

      val response =
        subcontractor.copy(
          emailAddress = None,
          phoneNumber = None,
          mobilePhoneNumber = None
        )

      SubcontractorPopulatorUtils.contactMethods(
        response
      ) mustBe empty
    }
  }

  "SubcontractorPopulatorUtils.individualName" - {

    "must return the subcontractor name when first name and surname are present" in {

      SubcontractorPopulatorUtils.individualName(
        subcontractor
      ) mustBe Some(
        SubcontractorName(
          firstName = "John",
          middleName = Some("Middle"),
          lastName = "Smith"
        )
      )
    }

    "must return the subcontractor name without a middle name when second name is missing" in {

      val response =
        subcontractor.copy(
          secondName = None
        )

      SubcontractorPopulatorUtils.individualName(
        response
      ) mustBe Some(
        SubcontractorName(
          firstName = "John",
          middleName = None,
          lastName = "Smith"
        )
      )
    }

    "must return the subcontractor name without a first name when first name is missing" in {

      val response =
        subcontractor.copy(
          firstName = None
        )

      SubcontractorPopulatorUtils.individualName(
        response
      ) mustBe Some(SubcontractorName("", Some("Middle"), "Smith"))
    }

    "must return the subcontractor name without a surname when surname is missing" in {

      val response =
        subcontractor.copy(
          surname = None
        )

      SubcontractorPopulatorUtils.individualName(
        response
      ) mustBe Some(SubcontractorName("John", Some("Middle"), ""))
    }

    "must return the subcontractor name with middle name only when both first name and surname are missing" in {

      val response =
        subcontractor.copy(
          firstName = None,
          surname = None
        )

      SubcontractorPopulatorUtils.individualName(
        response
      ) mustBe Some(SubcontractorName("", Some("Middle"), ""))
    }
  }

  "SubcontractorPopulatorUtils.hasTradingName" - {

    "must return true when trading name is present" in {

      SubcontractorPopulatorUtils.hasTradingName(
        subcontractor
      ) mustBe true
    }

    "must return false when trading name is missing" in {

      val response =
        subcontractor.copy(
          tradingName = None
        )

      SubcontractorPopulatorUtils.hasTradingName(
        response
      ) mustBe false
    }

    "must return false when trading name is empty" in {

      val response =
        subcontractor.copy(
          tradingName = Some("")
        )

      SubcontractorPopulatorUtils.hasTradingName(
        response
      ) mustBe false
    }

    "must return false when trading name contains only whitespace" in {

      val response =
        subcontractor.copy(
          tradingName = Some("   ")
        )

      SubcontractorPopulatorUtils.hasTradingName(
        response
      ) mustBe false
    }

    "must return true when trading name contains non-whitespace characters" in {

      val response =
        subcontractor.copy(
          tradingName = Some("  Test Trading Name  ")
        )

      SubcontractorPopulatorUtils.hasTradingName(
        response
      ) mustBe true
    }
  }
}
