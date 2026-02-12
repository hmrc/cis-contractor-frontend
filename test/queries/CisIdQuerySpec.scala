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

package queries

import base.SpecBase
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.{JsPath, Json}

class CisIdQuerySpec extends AnyFreeSpec with Matchers with SpecBase {

  "CisIdQuery" - {

    "must have the correct JSON path" in {
      CisIdQuery.path mustEqual (JsPath \ "cisId")
    }

    "must set a CIS ID value" in {
      val ua = emptyUserAnswers
        .set(CisIdQuery, "CIS-12345")
        .success
        .value

      ua.data mustBe Json.obj(
        "cisId" -> "CIS-12345"
      )
    }

    "must get a CIS ID value" in {
      val ua = emptyUserAnswers
        .set(CisIdQuery, "CIS-67890")
        .success
        .value

      ua.get(CisIdQuery) mustBe Some("CIS-67890")
    }

    "must return None when value is not set" in {
      emptyUserAnswers.get(CisIdQuery) mustBe None
    }
  }
}
