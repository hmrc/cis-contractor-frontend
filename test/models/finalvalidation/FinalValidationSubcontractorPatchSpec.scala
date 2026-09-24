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

import base.SpecBase
import play.api.libs.json.Json

class FinalValidationSubcontractorPatchSpec extends SpecBase {

  "FinalValidationSubcontractorPatch" - {

    "must round trip through JSON" in {

      val patch =
        FinalValidationSubcontractorPatch(
          utr = Some("1234567890"),
          partnerUtr = Some("0987654321"),
          crn = Some("12345678"),
          firstName = Some("John"),
          secondName = Some("Paul"),
          surname = Some("Smith"),
          partnershipTradingName = Some("Partnership Trading"),
          tradingName = Some("Smith Trading"),
          nino = Some("AB123456C"),
          worksReferenceNumber = Some("WRN123"),
          addressLine1 = Some("1 Test Street"),
          addressLine2 = Some("Test Area"),
          addressLine3 = Some("Test Town"),
          addressLine4 = Some("Test County"),
          country = Some("GB"),
          postcode = Some("AA1 1AA"),
          emailAddress = Some("john@example.com"),
          phoneNumber = Some("01234567890"),
          mobilePhoneNumber = Some("07123456789")
        )

      Json
        .toJson(patch)
        .as[FinalValidationSubcontractorPatch] mustBe patch
    }

    "must round trip through JSON when all values are empty" in {

      val patch =
        FinalValidationSubcontractorPatch()

      Json
        .toJson(patch)
        .as[FinalValidationSubcontractorPatch] mustBe patch
    }

    "must write the expected JSON" in {

      val patch =
        FinalValidationSubcontractorPatch(
          utr = Some("1234567890"),
          tradingName = Some("Smith Trading")
        )

      Json.toJson(patch) mustBe Json.obj(
        "utr"         -> "1234567890",
        "tradingName" -> "Smith Trading"
      )
    }
  }
}
