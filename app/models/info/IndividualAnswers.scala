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

package models.info

import models.TypeOfSubcontractor
import models.add.SubcontractorName
import models.address.Address
import models.add.IndividualContactMethodOptions
import play.api.libs.json.{Json, OFormat}

case class IndividualAnswers(
  subcontractorType: TypeOfSubcontractor,
  showVerificationDetails: Boolean,
  usesTradingName: Option[Boolean],
  tradingName: Option[String],
  subcontractorName: Option[SubcontractorName],
  addressYesNo: Option[Boolean],
  address: Option[Address],
  individualContactMethodsYesNo: Option[Boolean],
  individualContactMethod: Set[IndividualContactMethodOptions],
  email: Option[String],
  phone: Option[String],
  mobile: Option[String],
  utrYesNo: Option[Boolean],
  utr: Option[String],
  ninoYesNo: Option[Boolean],
  nino: Option[String],
  worksReferenceYesNo: Option[Boolean],
  worksReference: Option[String],
  verificationNumber: Option[String]
)

object IndividualAnswers {
  implicit val format: OFormat[IndividualAnswers] =
    Json.format[IndividualAnswers]
}
