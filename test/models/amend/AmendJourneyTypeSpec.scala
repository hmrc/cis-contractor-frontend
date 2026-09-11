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

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsString, Json}

class AmendJourneyTypeSpec extends AnyWordSpec with Matchers {

  "AmendJourneyType.fromString" should {

    "return Standard for standard" in {
      AmendJourneyType.fromString("standard") mustBe
        Some(AmendJourneyType.Standard)
    }

    "return InsufficientInfo for insufficient" in {
      AmendJourneyType.fromString("insufficient") mustBe
        Some(AmendJourneyType.InsufficientInfo)
    }

    "return UnmatchedInfo for unmatched" in {
      AmendJourneyType.fromString("unmatched") mustBe
        Some(AmendJourneyType.UnmatchedInfo)
    }

    "return None for an unknown value" in {
      AmendJourneyType.fromString("unknown") mustBe None
    }

    "return None for an empty value" in {
      AmendJourneyType.fromString("") mustBe None
    }
  }

  "AmendJourneyType.routeValue" should {

    "return standard for Standard" in {
      AmendJourneyType.Standard.routeValue mustBe
        "standard"
    }

    "return insufficient for InsufficientInfo" in {
      AmendJourneyType.InsufficientInfo.routeValue mustBe
        "insufficient"
    }

    "return unmatched for UnmatchedInfo" in {
      AmendJourneyType.UnmatchedInfo.routeValue mustBe
        "unmatched"
    }
  }

  "AmendJourneyType writes" should {

    "write Standard" in {
      Json.toJson(
        AmendJourneyType.Standard: AmendJourneyType
      ) mustBe JsString("Standard")
    }

    "write InsufficientInfo" in {
      Json.toJson(
        AmendJourneyType.InsufficientInfo: AmendJourneyType
      ) mustBe JsString("InsufficientInfo")
    }

    "write UnmatchedInfo" in {
      Json.toJson(
        AmendJourneyType.UnmatchedInfo: AmendJourneyType
      ) mustBe JsString("UnmatchedInfo")
    }
  }

  "AmendJourneyType reads" should {

    "read Standard" in {
      JsString("Standard")
        .as[AmendJourneyType] mustBe
        AmendJourneyType.Standard
    }

    "read InsufficientInfo" in {
      JsString("InsufficientInfo")
        .as[AmendJourneyType] mustBe
        AmendJourneyType.InsufficientInfo
    }

    "read UnmatchedInfo" in {
      JsString("UnmatchedInfo")
        .as[AmendJourneyType] mustBe
        AmendJourneyType.UnmatchedInfo
    }

    "fail for an invalid value" in {
      JsString("Invalid")
        .validate[AmendJourneyType]
        .isError mustBe true
    }

    "fail for a non-string value" in {
      Json
        .obj(
          "journeyType" -> "Standard"
        )
        .validate[AmendJourneyType]
        .isError mustBe true
    }
  }
}
