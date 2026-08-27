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

import models.add.{IndividualNamesOptions, SubcontractorName}
import models.address.{Address, Country}
import models.contact.ContactMethodOptions
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsSuccess, Json}

class OriginalIndividualAnswersSpec extends AnyWordSpec with Matchers {

  private val address =
    Address(
      addressLine1 = "12 Harbour Road",
      addressLine2 = Some("Area"),
      addressLine3 = Some("Town"),
      addressLine4 = Some("County"),
      postcode = Some("NE1 1AA"),
      country = Some(
        Country(
          code = None,
          name = Some("England")
        )
      )
    )

  private val model =
    OriginalIndividualAnswers(
      individualNamesOptions = Set(IndividualNamesOptions.SubcontractorName),
      tradingName = None,
      subcontractorName = Some(
        SubcontractorName(
          firstName = "John",
          middleName = Some("A"),
          lastName = "Smith"
        )
      ),
      addressYesNo = Some(true),
      address = Some(address),
      individualContactMethodsYesNo = Some(true),
      individualContactMethod = Set(ContactMethodOptions.Email),
      email = Some("john@test.com"),
      phone = Some("01234567890"),
      mobile = Some("07123456789"),
      utrYesNo = Some(true),
      utr = Some("1234567890"),
      ninoYesNo = Some(true),
      nino = Some("AB123456C"),
      worksReferenceYesNo = Some(true),
      worksReference = Some("WRN123"),
      verificationNumber = Some("VRN123")
    )

  "OriginalIndividualAnswers" should {

    "serialise to JSON" in {

      val json = Json.toJson(model)

      json.validate[OriginalIndividualAnswers] shouldBe JsSuccess(model)
    }

    "deserialise from JSON" in {

      val json = Json.toJson(model)

      Json.fromJson[OriginalIndividualAnswers](json) shouldBe JsSuccess(model)
    }
  }
}
