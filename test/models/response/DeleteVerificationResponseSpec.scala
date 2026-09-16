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

package models.response

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsSuccess, Json}

class DeleteVerificationResponseSpec extends AnyWordSpec with Matchers {

  "DeleteVerificationResponse" should {

    "serialise and deserialise correctly" in {
      val model = DeleteVerificationResponse(
        verificationsCounter = Some(2L)
      )

      val json = Json.toJson(model)

      json mustBe Json.obj(
        "verificationsCounter" -> 2L
      )

      Json.fromJson[DeleteVerificationResponse](json) mustBe JsSuccess(model)
    }

    "serialise and deserialise correctly when the counter is absent" in {
      val model = DeleteVerificationResponse(
        verificationsCounter = None
      )

      val json = Json.toJson(model)

      json mustBe Json.obj()

      Json.fromJson[DeleteVerificationResponse](json) mustBe JsSuccess(model)
    }
  }
}
