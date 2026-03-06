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

package models.requests

import models.add.TypeOfSubcontractor
import models.requests.CreateAndUpdateSubcontractorPayload.{IndividualOrSoleTraderPayload, PartnershipPayload}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.Json

class CreateAndUpdateSubcontractorPayloadSpec extends AnyWordSpec with Matchers {

  "IndividualOrSoleTraderPayload JSON format" should {

    val cisId = "10"

    "round-trip (writes -> reads) with all fields populated" in {
      val model = IndividualOrSoleTraderPayload(
        cisId = cisId,
        subcontractorType = TypeOfSubcontractor.Individualorsoletrader,
        firstName = Some("firstName"),
        secondName = Some("secondName"),
        surname = Some("surname"),
        tradingName = Some("trading name"),
        addressLine1 = Some("addressLine1"),
        addressLine2 = Some("addressLine2"),
        city = Some("city"),
        county = Some("county"),
        country = Some("country"),
        postcode = Some("post code"),
        nino = Some("AA123456A"),
        utr = Some("1234567890"),
        worksReferenceNumber = Some("WRN-001"),
        emailAddress = Some("hello@hmrc.co.uk"),
        phoneNumber = Some("0123456789")
      )

      val js = Json.toJson(model)
      js.as[IndividualOrSoleTraderPayload] mustBe model
    }

    "parse minimal JSON with only required fields" in {
      val json =
        Json.parse(
          """
            |{
            |  "cisId": "10",
            |  "subcontractorType": "soletrader"
            |}
          """.stripMargin
        )

      val parsed = json.as[IndividualOrSoleTraderPayload]
      parsed.cisId mustBe cisId
      parsed.subcontractorType mustBe TypeOfSubcontractor.Individualorsoletrader
    }

    "fail to parse when a required field is missing" in {
      val jsonMissing =
        Json.parse(
          """
            |{
            |  "cisId": "10",
            |  "nino": "AA123456A"
            |}
          """.stripMargin
        )

      jsonMissing.validate[IndividualOrSoleTraderPayload].isError mustBe true
    }
  }

  "PartnershipPayload JSON format" should {

    val cisId = "11"

    "round-trip (writes -> reads) with all fields populated" in {
      val model = PartnershipPayload(
        cisId = cisId,
        subcontractorType = TypeOfSubcontractor.Partnership,
        utr = Some("1234567890"),
        partnerUtr = Some("0987654321"),
        crn = Some("AC012345"),
        firstName = None,
        secondName = None,
        surname = None,
        nino = Some("AA123456A"),
        partnershipTradingName = Some("Test Partnership Ltd"),
        tradingName = Some("Nominated Partner Name"),
        addressLine1 = Some("addressLine1"),
        addressLine2 = Some("addressLine2"),
        city = Some("city"),
        county = Some("county"),
        country = Some("country"),
        postcode = Some("NE1 1AA"),
        emailAddress = Some("hello@hmrc.co.uk"),
        phoneNumber = Some("0123456789"),
        mobilePhoneNumber = Some("07123456789"),
        worksReferenceNumber = Some("WRN-002")
      )

      val js = Json.toJson(model)
      js.as[PartnershipPayload] mustBe model
    }

    "parse minimal JSON with only required fields" in {
      val json =
        Json.parse(
          """
            |{
            |  "cisId": "11",
            |  "subcontractorType": "partnership"
            |}
          """.stripMargin
        )

      val parsed = json.as[PartnershipPayload]
      parsed.cisId mustBe cisId
      parsed.subcontractorType mustBe TypeOfSubcontractor.Partnership
    }

    "fail to parse when a required field is missing" in {
      val jsonMissing =
        Json.parse(
          """
            |{
            |  "subcontractorType": "partnership",
            |  "utr": "1234567890"
            |}
          """.stripMargin
        )

      jsonMissing.validate[PartnershipPayload].isError mustBe true
    }
  }
}
