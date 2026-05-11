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


import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsSuccess, Json}

class ModifyVerificationsRequestSpec extends AnyWordSpec with Matchers {

  "CreateVerifications" should {

    "serialise and deserialise correctly" in {
      val model = CreateVerifications(
        verificationBatchResourceRef = 12345L,
        verificationResourceReferences = Seq(111L, 222L)
      )

      val json = Json.toJson(model)

      json mustBe Json.obj(
        "verificationBatchResourceRef" -> 12345L,
        "verificationResourceReferences" -> Json.arr(111L, 222L)
      )

      Json.fromJson[CreateVerifications](json) mustBe JsSuccess(model)
    }
  }

  "DeleteVerifications" should {

    "serialise and deserialise correctly" in {
      val model = DeleteVerifications(
        verificationResourceReferences = Seq(333L, 444L)
      )

      val json = Json.toJson(model)

      json mustBe Json.obj(
        "verificationResourceReferences" -> Json.arr(333L, 444L)
      )

      Json.fromJson[DeleteVerifications](json) mustBe JsSuccess(model)
    }
  }

  "ModifyVerificationsRequest" should {

    "serialise and deserialise correctly when both create + delete are present" in {
      val model = ModifyVerificationsRequest(
        instanceId = "INST-123",
        deleteVerifications = Some(DeleteVerifications(Seq(10L, 20L))),
        createVerifications = Some(CreateVerifications(verificationBatchResourceRef = 999L, verificationResourceReferences = Seq(30L)))
      )

      val json = Json.toJson(model)

      json mustBe Json.obj(
        "instanceId" -> "INST-123",
        "deleteVerifications" -> Json.obj(
          "verificationResourceReferences" -> Json.arr(10L, 20L)
        ),
        "createVerifications" -> Json.obj(
          "verificationBatchResourceRef" -> 999L,
          "verificationResourceReferences" -> Json.arr(30L)
        )
      )

      Json.fromJson[ModifyVerificationsRequest](json) mustBe JsSuccess(model)
    }

    "serialise and deserialise correctly when only create is present" in {
      val model = ModifyVerificationsRequest(
        instanceId = "INST-123",
        deleteVerifications = None,
        createVerifications = Some(CreateVerifications(verificationBatchResourceRef = 999L, verificationResourceReferences = Seq(30L, 40L)))
      )

      val json = Json.toJson(model)

      json mustBe Json.obj(
        "instanceId" -> "INST-123",
        "createVerifications" -> Json.obj(
          "verificationBatchResourceRef" -> 999L,
          "verificationResourceReferences" -> Json.arr(30L, 40L)
        )
      )

      Json.fromJson[ModifyVerificationsRequest](json) mustBe JsSuccess(model)
    }

    "serialise and deserialise correctly when only delete is present" in {
      val model = ModifyVerificationsRequest(
        instanceId = "INST-123",
        deleteVerifications = Some(DeleteVerifications(Seq(10L))),
        createVerifications = None
      )

      val json = Json.toJson(model)

      json mustBe Json.obj(
        "instanceId" -> "INST-123",
        "deleteVerifications" -> Json.obj(
          "verificationResourceReferences" -> Json.arr(10L)
        )
      )

      Json.fromJson[ModifyVerificationsRequest](json) mustBe JsSuccess(model)
    }

    "serialise and deserialise correctly when both create + delete are None (still valid JSON)" in {
      val model = ModifyVerificationsRequest(
        instanceId = "INST-123",
        deleteVerifications = None,
        createVerifications = None
      )

      val json = Json.toJson(model)

      json mustBe Json.obj(
        "instanceId" -> "INST-123"
      )

      Json.fromJson[ModifyVerificationsRequest](json) mustBe JsSuccess(model)
    }
  }
}