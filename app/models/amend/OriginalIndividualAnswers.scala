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

package models.amend

import models.add.SubcontractorName
import models.address.Address
import models.add.IndividualContactMethodOptions
import play.api.libs.json.{Json, OFormat}

case class OriginalIndividualAnswers(
  usesTradingName: Option[Boolean],
  tradingName: Option[String],
  subcontractorName: Option[SubcontractorName],
  address: Option[Address],
  individualContactMethod: Option[Set[IndividualContactMethodOptions]],
  email: Option[String],
  phone: Option[String],
  mobile: Option[String],
  utr: Option[String],
  nino: Option[String],
  worksReference: Option[String]
)

object OriginalIndividualAnswers extends models.Enumerable.Implicits {
  implicit val format: OFormat[OriginalIndividualAnswers] = Json.format[OriginalIndividualAnswers]
}
