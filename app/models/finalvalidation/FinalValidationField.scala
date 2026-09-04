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

import play.api.libs.json.*

sealed trait FinalValidationField {
  def key: String
}

object FinalValidationField {

  case object TradingName extends FinalValidationField {
    override val key: String = "tradingName"
  }

  case object PartnershipTradingName extends FinalValidationField {
    override val key: String = "partnershipTradingName"
  }

  case object Utr extends FinalValidationField {
    override val key: String = "utr"
  }

  case object PartnerUtr extends FinalValidationField {
    override val key: String = "partnerUtr"
  }

  case object Crn extends FinalValidationField {
    override val key: String = "crn"
  }

  case object FirstName extends FinalValidationField {
    override val key: String = "firstName"
  }

  case object SecondName extends FinalValidationField {
    override val key: String = "secondName"
  }

  case object Surname extends FinalValidationField {
    override val key: String = "surname"
  }

  case object Nino extends FinalValidationField {
    override val key: String = "nino"
  }

  case object WorkReferenceNumber extends FinalValidationField {
    override val key: String = "workReferenceNumber"
  }

  case object AddressLine1 extends FinalValidationField {
    override val key: String = "addressLine1"
  }

  case object AddressLine2 extends FinalValidationField {
    override val key: String = "addressLine2"
  }

  case object AddressLine3 extends FinalValidationField {
    override val key: String = "addressLine3"
  }

  case object AddressLine4 extends FinalValidationField {
    override val key: String = "addressLine4"
  }

  case object Country extends FinalValidationField {
    override val key: String = "country"
  }

  case object PostCode extends FinalValidationField {
    override val key: String = "postCode"
  }

  case object EmailAddress extends FinalValidationField {
    override val key: String = "emailAddress"
  }

  case object PhoneNumber extends FinalValidationField {
    override val key: String = "phoneNumber"
  }

  case object MobilePhoneNumber extends FinalValidationField {
    override val key: String = "mobilePhoneNumber"
  }

  val values: Seq[FinalValidationField] = Seq(
    TradingName,
    PartnershipTradingName,
    Utr,
    PartnerUtr,
    Crn,
    FirstName,
    SecondName,
    Surname,
    Nino,
    WorkReferenceNumber,
    AddressLine1,
    AddressLine2,
    AddressLine3,
    AddressLine4,
    Country,
    PostCode,
    EmailAddress,
    PhoneNumber,
    MobilePhoneNumber
  )

  def fromKey(key: String): Option[FinalValidationField] = values.find(_.key == key)

  given Format[FinalValidationField] = new Format[FinalValidationField] {
    override def writes(o: FinalValidationField): JsValue = JsString(o.key)

    override def reads(json: JsValue): JsResult[FinalValidationField] = json match {
      case JsString(key) =>
        fromKey(key) match {
          case Some(field) => JsSuccess(field)
          case None        => JsError(s"Unknown FinalValidationField key: $key")
        }
      case _             => JsError("Expected a string for FinalValidationField")
    }
  }
}
