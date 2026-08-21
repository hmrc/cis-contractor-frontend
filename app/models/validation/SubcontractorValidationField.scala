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

import play.api.libs.json.{
  Format,
  JsError,
  JsResult,
  JsString,
  JsSuccess,
  JsValue,
  Reads,
  Writes
}

sealed trait SubcontractorValidationField {
  def value: String
}

object SubcontractorValidationField {

  case object EmailAddress
    extends SubcontractorValidationField {
    override val value: String = "emailAddress"
  }

  case object PhoneNumber
    extends SubcontractorValidationField {
    override val value: String = "phoneNumber"
  }

  case object MobilePhoneNumber
    extends SubcontractorValidationField {
    override val value: String = "mobilePhoneNumber"
  }

  case object AddressLine1
    extends SubcontractorValidationField {
    override val value: String = "addressLine1"
  }

  case object AddressLine2
    extends SubcontractorValidationField {
    override val value: String = "addressLine2"
  }

  case object AddressLine3
    extends SubcontractorValidationField {
    override val value: String = "addressLine3"
  }

  case object AddressLine4
    extends SubcontractorValidationField {
    override val value: String = "addressLine4"
  }

  case object Postcode
    extends SubcontractorValidationField {
    override val value: String = "postcode"
  }

  case object Country
    extends SubcontractorValidationField {
    override val value: String = "country"
  }

  val values: List[SubcontractorValidationField] =
    List(
      EmailAddress,
      PhoneNumber,
      MobilePhoneNumber,
      AddressLine1,
      AddressLine2,
      AddressLine3,
      AddressLine4,
      Postcode,
      Country
    )

  private def fromString(
                          value: String
                        ): JsResult[SubcontractorValidationField] =
    values
      .find(_.value == value)
      .fold[JsResult[SubcontractorValidationField]](
        JsError(
          s"Unknown subcontractor validation field: $value"
        )
      )(JsSuccess(_))

  implicit val format
  : Format[SubcontractorValidationField] =
    Format(
      Reads {
        case JsString(value) =>
          fromString(value)

        case _ =>
          JsError(
            "Subcontractor validation field must be a string"
          )
      },
      Writes { field =>
        JsString(field.value)
      }
    )
}
