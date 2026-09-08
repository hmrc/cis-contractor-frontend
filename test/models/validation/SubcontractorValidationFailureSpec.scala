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

import models.validation.SubcontractorValidationField.{AddressLine1, EmailAddress, Postcode}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.Json

class SubcontractorValidationFailureSpec extends AnyWordSpec with Matchers {

  "SubcontractorValidationFailure JSON format" must {

    "write all failed fields for a subcontractor" in {
      val failure =
        SubcontractorValidationFailure(
          subcontractorId = 101L,
          failedFields = List(
            FieldValidationFailure(
              field = EmailAddress,
              value = Some("invalid@email")
            ),
            FieldValidationFailure(
              field = Postcode,
              value = Some("INVALID123")
            )
          )
        )

      Json.toJson(failure) mustBe
        Json.obj(
          "subcontractorId" -> 101L,
          "failedFields"    -> Json.arr(
            Json.obj(
              "field" -> "emailAddress",
              "value" -> "invalid@email"
            ),
            Json.obj(
              "field" -> "postcode",
              "value" -> "INVALID123"
            )
          )
        )
    }

    "round-trip multiple failed fields for a subcontractor" in {
      val failure =
        SubcontractorValidationFailure(
          subcontractorId = 101L,
          failedFields = List(
            FieldValidationFailure(
              field = EmailAddress,
              value = Some("invalid@email")
            ),
            FieldValidationFailure(
              field = Postcode,
              value = Some("INVALID123")
            ),
            FieldValidationFailure(
              field = AddressLine1,
              value = None
            )
          )
        )

      Json
        .toJson(failure)
        .validate[SubcontractorValidationFailure]
        .get mustBe failure
    }

    "round-trip an empty failed-fields list" in {
      val failure =
        SubcontractorValidationFailure(
          subcontractorId = 101L,
          failedFields = Nil
        )

      Json
        .toJson(failure)
        .validate[SubcontractorValidationFailure]
        .get mustBe failure
    }

    "round-trip multiple subcontractor failures" in {
      val failures =
        List(
          SubcontractorValidationFailure(
            subcontractorId = 101L,
            failedFields = List(
              FieldValidationFailure(
                field = EmailAddress,
                value = Some("invalid@email")
              )
            )
          ),
          SubcontractorValidationFailure(
            subcontractorId = 102L,
            failedFields = List(
              FieldValidationFailure(
                field = AddressLine1,
                value = None
              )
            )
          )
        )

      Json
        .toJson(failures)
        .validate[
          List[SubcontractorValidationFailure]
        ]
        .get mustBe failures
    }

    "fail to read when subcontractorId is missing" in {
      val json =
        Json.obj(
          "failedFields" -> Json.arr()
        )

      json
        .validate[SubcontractorValidationFailure]
        .isError mustBe true
    }

    "fail to read when failedFields is missing" in {
      val json =
        Json.obj(
          "subcontractorId" -> 101L
        )

      json
        .validate[SubcontractorValidationFailure]
        .isError mustBe true
    }
  }

  "SubcontractorValidationFailure.merge" must {

    "combine failed fields for the same subcontractor" in {
      SubcontractorValidationFailure.merge(
        List(
          SubcontractorValidationFailure(
            subcontractorId = 101L,
            failedFields = List(
              FieldValidationFailure(
                field = EmailAddress,
                value = Some("invalid@email")
              )
            )
          )
        ),
        List(
          SubcontractorValidationFailure(
            subcontractorId = 101L,
            failedFields = List(
              FieldValidationFailure(
                field = AddressLine1,
                value = None
              )
            )
          )
        )
      ) mustBe
        List(
          SubcontractorValidationFailure(
            subcontractorId = 101L,
            failedFields = List(
              FieldValidationFailure(
                field = EmailAddress,
                value = Some("invalid@email")
              ),
              FieldValidationFailure(
                field = AddressLine1,
                value = None
              )
            )
          )
        )
    }

    "keep separate subcontractors as separate entries" in {
      SubcontractorValidationFailure.merge(
        List(
          SubcontractorValidationFailure(
            subcontractorId = 101L,
            failedFields = List(
              FieldValidationFailure(
                field = EmailAddress,
                value = Some("invalid@email")
              )
            )
          )
        ),
        List(
          SubcontractorValidationFailure(
            subcontractorId = 102L,
            failedFields = List(
              FieldValidationFailure(
                field = AddressLine1,
                value = None
              )
            )
          )
        )
      ) mustBe
        List(
          SubcontractorValidationFailure(
            subcontractorId = 101L,
            failedFields = List(
              FieldValidationFailure(
                field = EmailAddress,
                value = Some("invalid@email")
              )
            )
          ),
          SubcontractorValidationFailure(
            subcontractorId = 102L,
            failedFields = List(
              FieldValidationFailure(
                field = AddressLine1,
                value = None
              )
            )
          )
        )
    }

    "return an empty list when there are no failures" in {
      SubcontractorValidationFailure.merge(Nil, Nil) mustBe Nil
    }
  }
}
