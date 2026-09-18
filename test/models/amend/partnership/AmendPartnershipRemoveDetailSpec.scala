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

package models.amend.partnership

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class AmendPartnershipRemoveDetailSpec extends AnyWordSpec with Matchers {

  "AmendPartnershipRemoveDetail" should {

    "have the correct keys" in {

      AmendPartnershipRemoveDetail.Address.key shouldBe
        "address"

      AmendPartnershipRemoveDetail.ContactDetails.key shouldBe
        "contact-details"

      AmendPartnershipRemoveDetail.Utr.key shouldBe
        "utr"

      AmendPartnershipRemoveDetail.WorksReferenceNumber.key shouldBe
        "works-reference-number"

      AmendPartnershipRemoveDetail.NominatedPartnerUtr.key shouldBe
        "nominated-partner-utr"

      AmendPartnershipRemoveDetail.NominatedPartnerNino.key shouldBe
        "nominated-partner-nino"

      AmendPartnershipRemoveDetail.NominatedPartnerCompanyRegistrationNumber.key shouldBe
        "nominated-partner-company-registration-number"
    }

    "have the correct message keys" in {

      AmendPartnershipRemoveDetail.Address.messageKey shouldBe
        "amendPartnershipRemoveDetailYesNo.detail.address"

      AmendPartnershipRemoveDetail.ContactDetails.messageKey shouldBe
        "amendPartnershipRemoveDetailYesNo.detail.contactDetails"

      AmendPartnershipRemoveDetail.Utr.messageKey shouldBe
        "amendPartnershipRemoveDetailYesNo.detail.utr"

      AmendPartnershipRemoveDetail.WorksReferenceNumber.messageKey shouldBe
        "amendPartnershipRemoveDetailYesNo.detail.worksReferenceNumber"

      AmendPartnershipRemoveDetail.NominatedPartnerUtr.messageKey shouldBe
        "amendPartnershipRemoveDetailYesNo.detail.nominatedPartnerUtr"

      AmendPartnershipRemoveDetail.NominatedPartnerNino.messageKey shouldBe
        "amendPartnershipRemoveDetailYesNo.detail.nominatedPartnerNino"

      AmendPartnershipRemoveDetail.NominatedPartnerCompanyRegistrationNumber.messageKey shouldBe
        "amendPartnershipRemoveDetailYesNo.detail.nominatedPartnerCompanyRegistrationNumber"
    }

    "identify nominated partner details correctly" in {

      AmendPartnershipRemoveDetail.Address.isNominatedPartnerDetail              shouldBe false
      AmendPartnershipRemoveDetail.ContactDetails.isNominatedPartnerDetail       shouldBe false
      AmendPartnershipRemoveDetail.Utr.isNominatedPartnerDetail                  shouldBe false
      AmendPartnershipRemoveDetail.WorksReferenceNumber.isNominatedPartnerDetail shouldBe false

      AmendPartnershipRemoveDetail.NominatedPartnerUtr.isNominatedPartnerDetail                       shouldBe true
      AmendPartnershipRemoveDetail.NominatedPartnerNino.isNominatedPartnerDetail                      shouldBe true
      AmendPartnershipRemoveDetail.NominatedPartnerCompanyRegistrationNumber.isNominatedPartnerDetail shouldBe true
    }

    "contain all supported detail types in values" in {

      AmendPartnershipRemoveDetail.values should contain theSameElementsInOrderAs Seq(
        AmendPartnershipRemoveDetail.Address,
        AmendPartnershipRemoveDetail.ContactDetails,
        AmendPartnershipRemoveDetail.Utr,
        AmendPartnershipRemoveDetail.WorksReferenceNumber,
        AmendPartnershipRemoveDetail.NominatedPartnerUtr,
        AmendPartnershipRemoveDetail.NominatedPartnerNino,
        AmendPartnershipRemoveDetail.NominatedPartnerCompanyRegistrationNumber
      )
    }

    "return the correct detail when fromKey is given a valid key" in {

      AmendPartnershipRemoveDetail.fromKey("address") shouldBe
        Some(AmendPartnershipRemoveDetail.Address)

      AmendPartnershipRemoveDetail.fromKey("contact-details") shouldBe
        Some(AmendPartnershipRemoveDetail.ContactDetails)

      AmendPartnershipRemoveDetail.fromKey("utr") shouldBe
        Some(AmendPartnershipRemoveDetail.Utr)

      AmendPartnershipRemoveDetail.fromKey("works-reference-number") shouldBe
        Some(AmendPartnershipRemoveDetail.WorksReferenceNumber)

      AmendPartnershipRemoveDetail.fromKey("nominated-partner-utr") shouldBe
        Some(AmendPartnershipRemoveDetail.NominatedPartnerUtr)

      AmendPartnershipRemoveDetail.fromKey("nominated-partner-nino") shouldBe
        Some(AmendPartnershipRemoveDetail.NominatedPartnerNino)

      AmendPartnershipRemoveDetail.fromKey(
        "nominated-partner-company-registration-number"
      ) shouldBe
        Some(
          AmendPartnershipRemoveDetail.NominatedPartnerCompanyRegistrationNumber
        )
    }

    "return None when fromKey is given an invalid key" in {

      AmendPartnershipRemoveDetail.fromKey("invalid") shouldBe None
    }

    "return None when fromKey is given an empty key" in {

      AmendPartnershipRemoveDetail.fromKey("") shouldBe None
    }

    "return None when fromKey is given a key with different casing" in {

      AmendPartnershipRemoveDetail.fromKey("Address") shouldBe None
    }
  }
}
