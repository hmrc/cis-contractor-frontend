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

package models.amend.company

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class AmendCompanyRemoveDetailSpec extends AnyWordSpec with Matchers {

  "AmendCompanyRemoveDetail" should {

    "have the correct keys" in {

      AmendCompanyRemoveDetail.Address.key shouldBe
        "address"

      AmendCompanyRemoveDetail.ContactDetails.key shouldBe
        "contact-details"

      AmendCompanyRemoveDetail.Utr.key shouldBe
        "utr"

      AmendCompanyRemoveDetail.CompanyRegistrationNumber.key shouldBe
        "company-registration-number"

      AmendCompanyRemoveDetail.WorksReferenceNumber.key shouldBe
        "works-reference-number"
    }

    "have the correct message keys" in {

      AmendCompanyRemoveDetail.Address.messageKey shouldBe
        "amendCompanyRemoveDetailYesNo.detail.address"

      AmendCompanyRemoveDetail.ContactDetails.messageKey shouldBe
        "amendCompanyRemoveDetailYesNo.detail.contactDetails"

      AmendCompanyRemoveDetail.Utr.messageKey shouldBe
        "amendCompanyRemoveDetailYesNo.detail.utr"

      AmendCompanyRemoveDetail.CompanyRegistrationNumber.messageKey shouldBe
        "amendCompanyRemoveDetailYesNo.detail.companyRegistrationNumber"

      AmendCompanyRemoveDetail.WorksReferenceNumber.messageKey shouldBe
        "amendCompanyRemoveDetailYesNo.detail.worksReferenceNumber"
    }

    "contain all supported detail types in values" in {

      AmendCompanyRemoveDetail.values should contain theSameElementsInOrderAs Seq(
        AmendCompanyRemoveDetail.Address,
        AmendCompanyRemoveDetail.ContactDetails,
        AmendCompanyRemoveDetail.Utr,
        AmendCompanyRemoveDetail.CompanyRegistrationNumber,
        AmendCompanyRemoveDetail.WorksReferenceNumber
      )
    }

    "return the correct detail when fromKey is given a valid key" in {

      AmendCompanyRemoveDetail.fromKey("address") shouldBe
        Some(AmendCompanyRemoveDetail.Address)

      AmendCompanyRemoveDetail.fromKey("contact-details") shouldBe
        Some(AmendCompanyRemoveDetail.ContactDetails)

      AmendCompanyRemoveDetail.fromKey("utr") shouldBe
        Some(AmendCompanyRemoveDetail.Utr)

      AmendCompanyRemoveDetail.fromKey("company-registration-number") shouldBe
        Some(AmendCompanyRemoveDetail.CompanyRegistrationNumber)

      AmendCompanyRemoveDetail.fromKey("works-reference-number") shouldBe
        Some(AmendCompanyRemoveDetail.WorksReferenceNumber)
    }

    "return None when fromKey is given an invalid key" in {

      AmendCompanyRemoveDetail.fromKey("invalid") shouldBe None
    }

    "return None when fromKey is given an empty key" in {

      AmendCompanyRemoveDetail.fromKey("") shouldBe None
    }

    "return None when fromKey is given a key with different casing" in {

      AmendCompanyRemoveDetail.fromKey("Address") shouldBe None
    }
  }
}
