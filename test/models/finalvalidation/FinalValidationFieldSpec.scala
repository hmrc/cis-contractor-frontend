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

package models.finalvalidation

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.{JsError, JsString, Json}

class FinalValidationFieldSpec extends AnyFreeSpec with Matchers {

  import FinalValidationField.*

  "FinalValidationField" - {

    "must have the correct keys" in {

      TradingName.key mustBe "tradingName"
      PartnershipTradingName.key mustBe "partnershipTradingName"
      Utr.key mustBe "utr"
      PartnerUtr.key mustBe "partnerUtr"
      Crn.key mustBe "crn"
      FirstName.key mustBe "firstName"
      SecondName.key mustBe "secondName"
      Surname.key mustBe "surname"
      Nino.key mustBe "nino"
      WorkReferenceNumber.key mustBe "workReferenceNumber"
      AddressLine1.key mustBe "addressLine1"
      AddressLine2.key mustBe "addressLine2"
      AddressLine3.key mustBe "addressLine3"
      AddressLine4.key mustBe "addressLine4"
      Country.key mustBe "country"
      PostCode.key mustBe "postCode"
      EmailAddress.key mustBe "emailAddress"
      PhoneNumber.key mustBe "phoneNumber"
      MobilePhoneNumber.key mustBe "mobilePhoneNumber"
    }

    "values" - {

      "must contain all Final Validation fields" in {

        values mustBe Seq(
          TradingName,
          PartnershipTradingName,
          Utr,
          PartnerUtr,
          Crn,
          FirstName,
          SecondName,
          Surname,
          Nino,
          WorkReferenceNumber,
          AddressLine1,
          AddressLine2,
          AddressLine3,
          AddressLine4,
          Country,
          PostCode,
          EmailAddress,
          PhoneNumber,
          MobilePhoneNumber
        )
      }
    }

    "fromKey" - {

      "must return the correct field for every valid key" in {

        values.foreach { field =>
          fromKey(field.key) mustBe Some(field)
        }
      }

      "must return None for an unknown key" in {

        fromKey("unknown") mustBe None
      }
    }

    "JSON format" - {

      "must write a FinalValidationField as its key" in {

        Json.toJson[FinalValidationField](Utr) mustBe JsString("utr")
      }

      "must read a valid FinalValidationField key" in {

        Json
          .fromJson[FinalValidationField](JsString("utr"))
          .get mustBe Utr
      }

      "must round trip every FinalValidationField" in {

        values.foreach { field =>
          Json
            .toJson[FinalValidationField](field)
            .as[FinalValidationField] mustBe field
        }
      }

      "must fail to read an unknown FinalValidationField key" in {

        Json
          .fromJson[FinalValidationField](JsString("unknown")) mustBe
          JsError("Unknown FinalValidationField key: unknown")
      }

      "must fail to read a non-string value" in {

        Json
          .fromJson[FinalValidationField](Json.obj("key" -> "utr")) mustBe
          JsError("Expected a string for FinalValidationField")
      }
    }
  }
}
