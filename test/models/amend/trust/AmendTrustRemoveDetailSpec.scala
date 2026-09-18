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

package models.amend.trust

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class AmendTrustRemoveDetailSpec extends AnyWordSpec with Matchers {

  "AmendTrustRemoveDetail" should {

    "have the correct keys" in {

      AmendTrustRemoveDetail.Address.key shouldBe
        "address"

      AmendTrustRemoveDetail.ContactDetails.key shouldBe
        "contact-details"

      AmendTrustRemoveDetail.Utr.key shouldBe
        "utr"

      AmendTrustRemoveDetail.WorksReferenceNumber.key shouldBe
        "works-reference-number"
    }

    "have the correct message keys" in {

      AmendTrustRemoveDetail.Address.messageKey shouldBe
        "amendTrustRemoveDetailYesNo.detail.address"

      AmendTrustRemoveDetail.ContactDetails.messageKey shouldBe
        "amendTrustRemoveDetailYesNo.detail.contactDetails"

      AmendTrustRemoveDetail.Utr.messageKey shouldBe
        "amendTrustRemoveDetailYesNo.detail.utr"

      AmendTrustRemoveDetail.WorksReferenceNumber.messageKey shouldBe
        "amendTrustRemoveDetailYesNo.detail.worksReferenceNumber"
    }

    "contain all supported detail types in values" in {

      AmendTrustRemoveDetail.values should contain theSameElementsInOrderAs Seq(
        AmendTrustRemoveDetail.Address,
        AmendTrustRemoveDetail.ContactDetails,
        AmendTrustRemoveDetail.Utr,
        AmendTrustRemoveDetail.WorksReferenceNumber
      )
    }

    "return the correct detail when fromKey is given a valid key" in {

      AmendTrustRemoveDetail.fromKey("address") shouldBe
        Some(AmendTrustRemoveDetail.Address)

      AmendTrustRemoveDetail.fromKey("contact-details") shouldBe
        Some(AmendTrustRemoveDetail.ContactDetails)

      AmendTrustRemoveDetail.fromKey("utr") shouldBe
        Some(AmendTrustRemoveDetail.Utr)

      AmendTrustRemoveDetail.fromKey("works-reference-number") shouldBe
        Some(AmendTrustRemoveDetail.WorksReferenceNumber)
    }

    "return None when fromKey is given an invalid key" in {

      AmendTrustRemoveDetail.fromKey("invalid") shouldBe None
    }

    "return None when fromKey is given an empty key" in {

      AmendTrustRemoveDetail.fromKey("") shouldBe None
    }

    "return None when fromKey is given a key with different casing" in {

      AmendTrustRemoveDetail.fromKey("Address") shouldBe None
    }
  }
}
