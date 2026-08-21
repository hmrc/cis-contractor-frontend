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

package models.validation

import models.validation.SubcontractorValidationField.{
  AddressLine1,
  EmailAddress
}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{
  JsNull,
  Json
}

class FieldValidationFailureSpec
  extends AnyWordSpec
    with Matchers {

  "FieldValidationFailure JSON format" must {

    "write a failed field and its current value" in {
      val failure =
        FieldValidationFailure(
          field = EmailAddress,
          value = Some("invalid@email")
        )

      Json.toJson(failure) mustBe
        Json.obj(
          "field" -> "emailAddress",
          "value" -> "invalid@email"
        )
    }

    "round-trip a failed field containing a value" in {
      val failure =
        FieldValidationFailure(
          field = EmailAddress,
          value = Some("invalid@email")
        )

      Json
        .toJson(failure)
        .validate[FieldValidationFailure]
        .get mustBe failure
    }

    "round-trip a failed field with no value" in {
      val failure =
        FieldValidationFailure(
          field = AddressLine1,
          value = None
        )

      Json
        .toJson(failure)
        .validate[FieldValidationFailure]
        .get mustBe failure
    }

    "read a missing value as None" in {
      val json =
        Json.obj(
          "field" -> "addressLine1"
        )

      json
        .validate[FieldValidationFailure]
        .get mustBe
        FieldValidationFailure(
          field = AddressLine1,
          value = None
        )
    }

    "read a null value as None" in {
      val json =
        Json.obj(
          "field" -> "addressLine1",
          "value" -> JsNull
        )

      json
        .validate[FieldValidationFailure]
        .get mustBe
        FieldValidationFailure(
          field = AddressLine1,
          value = None
        )
    }

    "fail to read an unsupported field" in {
      val json =
        Json.obj(
          "field" -> "unsupportedField",
          "value" -> "invalid value"
        )

      json
        .validate[FieldValidationFailure]
        .isError mustBe true
    }
  }
}
