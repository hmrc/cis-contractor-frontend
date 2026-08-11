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

package models

import play.api.i18n.Messages
import play.api.libs.json.{JsObject, Json, OFormat, Reads, Writes}

import java.time.LocalDateTime

case class SubcontractorCurrentVerification(
  subcontractorId: Long,
  subbieResourceRef: Option[Long],
  firstName: Option[String],
  secondName: Option[String],
  surname: Option[String],
  tradingName: Option[String],
  utr: Option[String],
  nino: Option[String],
  crn: Option[String],
  partnerUtr: Option[String],
  partnershipTradingName: Option[String],
  subcontractorType: Option[String],
  addressLine1: Option[String],
  addressLine2: Option[String],
  addressLine3: Option[String],
  addressLine4: Option[String],
  country: Option[String],
  postcode: Option[String],
  emailAddress: Option[String],
  phoneNumber: Option[String],
  mobilePhoneNumber: Option[String],
  worksReferenceNumber: Option[String],
  matched: Option[String],
  autoVerified: Option[String],
  verified: Option[String],
  verificationNumber: Option[String],
  taxTreatment: Option[String],
  verificationDate: Option[LocalDateTime],
  version: Option[Int],
  updatedTaxTreatment: Option[String],
  lastMonthlyReturnDate: Option[LocalDateTime],
  pendingVerifications: Option[Int]
) {
  def displayName(implicit messages: Messages): String =
    name.getOrElse(messages("verify.noName"))

  private def name: Option[String] = {
    val first              = firstName.map(_.trim).filter(_.nonEmpty)
    val surnameValue       = surname.map(_.trim).filter(_.nonEmpty)
    val trading            = tradingName.map(_.trim).filter(_.nonEmpty)
    val partnershipTrading = partnershipTradingName.map(_.trim).filter(_.nonEmpty)

    val individualName =
      surnameValue.map(s => first.map(f => s"$s, $f").getOrElse(s))

    partnershipTrading.orElse(trading).orElse(individualName)
  }
}

object SubcontractorCurrentVerification {
  given format: OFormat[SubcontractorCurrentVerification] = Json.format[SubcontractorCurrentVerification]
}
