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

import models.address.{Address, Country}
import models.contact.ContactMethodOptions
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.Json

class OriginalTrustAnswersSpec extends AnyWordSpec with Matchers {

  "OriginalTrustAnswers" should {

    "serialise and deserialise correctly" in {

      val model = OriginalTrustAnswers(
        trustName = Some("Test Trust"),
        addressYesNo = Some(true),
        address = Some(
          Address(
            addressLine1 = "12 Harbor View Road",
            addressLine2 = Some("Amity Island"),
            addressLine3 = Some("Bodmin"),
            addressLine4 = Some("Cornwall"),
            postcode = Some("PL31 2HL"),
            country = Some(Country(None, Some("England"))),
            addressValidated = true
          )
        ),
        trustContactMethodsYesNo = Some(true),
        trustContactMethod = Set(
          ContactMethodOptions.Email,
          ContactMethodOptions.Phone
        ),
        email = Some("test@example.com"),
        phone = Some("01234567890"),
        mobile = Some("07123456789"),
        utrYesNo = Some(true),
        utr = Some("7777777777"),
        worksReferenceYesNo = Some(true),
        worksReference = Some("XLS345-MM")
      )

      Json.fromJson[OriginalTrustAnswers](Json.toJson(model)).get shouldBe model
    }

    "serialise and deserialise correctly when all optional fields are empty" in {

      val model = OriginalTrustAnswers(
        trustName = None,
        addressYesNo = None,
        address = None,
        trustContactMethodsYesNo = None,
        trustContactMethod = Set.empty,
        email = None,
        phone = None,
        mobile = None,
        utrYesNo = None,
        utr = None,
        worksReferenceYesNo = None,
        worksReference = None
      )

      Json.fromJson[OriginalTrustAnswers](Json.toJson(model)).get shouldBe model
    }
  }
}
