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

class VerifyCisIdQuerySpec extends AnyFreeSpec with Matchers with SpecBase {

  "VerifyCisIdQuery" - {

    "must nest the path under verify" in {
      VerifyCisIdQuery("uuid-1").path mustEqual (JsPath \ "verify" \ "uuid-1" \ "cisId")
    }

    "must set and get a CIS ID under the uuid" in {
      val ua = emptyUserAnswers
        .set(VerifyCisIdQuery("uuid-1"), "CIS-12345")
        .success
        .value

      ua.data mustBe Json.obj(
        "verify" -> Json.obj(
          "uuid-1" -> Json.obj(
            "cisId" -> "CIS-12345"
          )
        )
      )
      ua.get(VerifyCisIdQuery("uuid-1")) mustBe Some("CIS-12345")
      ua.get(CisIdQuery) mustBe None
    }
  }
}
