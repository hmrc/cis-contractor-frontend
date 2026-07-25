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

import models.address.{Address, Country}
import models.contact.ContactMethodOptions
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.Json

class OriginalPartnershipAnswersSpec extends AnyWordSpec with Matchers {

  "OriginalPartnershipAnswers" should {

    "serialise and deserialise correctly" in {

      val model = OriginalPartnershipAnswers(
        partnershipName = Some("ABC Partnership"),
        addressYesNo = Some(true),
        address = Some(
          Address(
            addressLine1 = "1 High Street",
            addressLine2 = Some("Suite 2"),
            addressLine3 = Some("Leeds"),
            postcode = Some("LS1 1AA"),
            country = Some(Country(Some("GB"), Some("United Kingdom"))),
            addressValidated = true
          )
        ),
        partnershipContactMethodsYesNo = Some(true),
        partnershipContactMethodOptions = Some(
          Set(
            ContactMethodOptions.Email,
            ContactMethodOptions.Phone
          )
        ),
        email = Some("test@test.com"),
        phone = Some("01234567890"),
        mobile = Some("07123456789"),
        hasUtrYesNo = Some(true),
        utr = Some("1234567890"),
        nominatedPartnerName = Some("John Smith"),
        nominatedPartnerUtrYesNo = Some(true),
        nominatedPartnerUtr = Some("0987654321"),
        nominatedPartnerNinoYesNo = Some(true),
        nominatedPartnerNino = Some("AA123456A"),
        nominatedPartnerCrnYesNo = Some(true),
        nominatedPartnerCrn = Some("12345678"),
        nominatedPartnerWorksReferenceYesNo = Some(true),
        nominatedPartnerWorksReference = Some("123/AB456")
      )

      Json.fromJson[OriginalPartnershipAnswers](Json.toJson(model)).get shouldBe model
    }

    "serialise and deserialise correctly when all optional fields are empty" in {

      val model = OriginalPartnershipAnswers(
        partnershipName = None,
        addressYesNo = None,
        address = None,
        partnershipContactMethodsYesNo = None,
        partnershipContactMethodOptions = None,
        email = None,
        phone = None,
        mobile = None,
        hasUtrYesNo = None,
        utr = None,
        nominatedPartnerName = None,
        nominatedPartnerUtrYesNo = None,
        nominatedPartnerUtr = None,
        nominatedPartnerNinoYesNo = None,
        nominatedPartnerNino = None,
        nominatedPartnerCrnYesNo = None,
        nominatedPartnerCrn = None,
        nominatedPartnerWorksReferenceYesNo = None,
        nominatedPartnerWorksReference = None
      )

      Json.fromJson[OriginalPartnershipAnswers](Json.toJson(model)).get shouldBe model
    }
  }
}
