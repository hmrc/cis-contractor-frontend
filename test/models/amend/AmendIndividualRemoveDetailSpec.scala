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

package models.amend

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class AmendIndividualRemoveDetailSpec extends AnyWordSpec with Matchers {
  "AmendIndividualRemoveDetail" should {

    "have the correct keys" in {

      AmendIndividualRemoveDetail.TradingName.key shouldBe
        "trading-name"

      AmendIndividualRemoveDetail.SubcontractorName.key shouldBe
        "subcontractor-name"

      AmendIndividualRemoveDetail.Address.key shouldBe
        "address"

      AmendIndividualRemoveDetail.ContactDetails.key shouldBe
        "contact-details"

      AmendIndividualRemoveDetail.Utr.key shouldBe
        "utr"

      AmendIndividualRemoveDetail.NationalInsuranceNumber.key shouldBe
        "national-insurance-number"

      AmendIndividualRemoveDetail.WorksReferenceNumber.key shouldBe
        "works-reference-number"
    }

    "have the correct message keys" in {

      AmendIndividualRemoveDetail.TradingName.messageKey shouldBe
        "amendIndividualRemoveDetailYesNo.detail.trading-name"

      AmendIndividualRemoveDetail.SubcontractorName.messageKey shouldBe
        "amendIndividualRemoveDetailYesNo.detail.subcontractor-name"

      AmendIndividualRemoveDetail.Address.messageKey shouldBe
        "amendIndividualRemoveDetailYesNo.detail.address"

      AmendIndividualRemoveDetail.ContactDetails.messageKey shouldBe
        "amendIndividualRemoveDetailYesNo.detail.contact-details"

      AmendIndividualRemoveDetail.Utr.messageKey shouldBe
        "amendIndividualRemoveDetailYesNo.detail.utr"

      AmendIndividualRemoveDetail.NationalInsuranceNumber.messageKey shouldBe
        "amendIndividualRemoveDetailYesNo.detail.national-insurance-number"

      AmendIndividualRemoveDetail.WorksReferenceNumber.messageKey shouldBe
        "amendIndividualRemoveDetailYesNo.detail.works-reference-number"
    }

    "contain all supported detail types in values" in {

      AmendIndividualRemoveDetail.values should contain theSameElementsInOrderAs Seq(
        AmendIndividualRemoveDetail.TradingName,
        AmendIndividualRemoveDetail.SubcontractorName,
        AmendIndividualRemoveDetail.Address,
        AmendIndividualRemoveDetail.ContactDetails,
        AmendIndividualRemoveDetail.Utr,
        AmendIndividualRemoveDetail.NationalInsuranceNumber,
        AmendIndividualRemoveDetail.WorksReferenceNumber
      )
    }

    "return the correct detail when fromKey is given a valid key" in {

      AmendIndividualRemoveDetail.fromKey("trading-name") shouldBe
        Some(AmendIndividualRemoveDetail.TradingName)

      AmendIndividualRemoveDetail.fromKey("subcontractor-name") shouldBe
        Some(AmendIndividualRemoveDetail.SubcontractorName)

      AmendIndividualRemoveDetail.fromKey("address") shouldBe
        Some(AmendIndividualRemoveDetail.Address)

      AmendIndividualRemoveDetail.fromKey("contact-details") shouldBe
        Some(AmendIndividualRemoveDetail.ContactDetails)

      AmendIndividualRemoveDetail.fromKey("utr") shouldBe
        Some(AmendIndividualRemoveDetail.Utr)

      AmendIndividualRemoveDetail.fromKey("national-insurance-number") shouldBe
        Some(AmendIndividualRemoveDetail.NationalInsuranceNumber)

      AmendIndividualRemoveDetail.fromKey("works-reference-number") shouldBe
        Some(AmendIndividualRemoveDetail.WorksReferenceNumber)
    }

    "return None when fromKey is given an invalid key" in {

      AmendIndividualRemoveDetail.fromKey("invalid") shouldBe None
    }

    "return None when fromKey is given an empty key" in {

      AmendIndividualRemoveDetail.fromKey("") shouldBe None
    }

    "return None when fromKey is given a key with different casing" in {

      AmendIndividualRemoveDetail.fromKey("Address") shouldBe None
    }
  }
}
