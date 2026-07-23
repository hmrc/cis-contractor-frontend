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

package models.amend.trust

import models.add.trust.TrustContactMethodOptions
import models.address.Address
import play.api.libs.json.{Json, OFormat}

case class OriginalTrustAnswers(
  trustName: Option[String],
  addressYesNo: Option[Boolean],
  address: Option[Address],
  trustContactMethodsYesNo: Option[Boolean],
  trustContactMethod: Set[TrustContactMethodOptions],
  email: Option[String],
  phone: Option[String],
  mobile: Option[String],
  utrYesNo: Option[Boolean],
  utr: Option[String],
  worksReferenceYesNo: Option[Boolean],
  worksReference: Option[String],
  verificationNumber: Option[String],
  isVerified: Option[Boolean]
)

object OriginalTrustAnswers extends models.Enumerable.Implicits {
  implicit val format: OFormat[OriginalTrustAnswers] = Json.format[OriginalTrustAnswers]
}
