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

package models.subcontractor

import models.add.TypeOfSubcontractor
import play.api.libs.json.{Json, OFormat}

final case class CreateAndUpdateSubcontractorRequest(
  instanceId: Int,
  subcontractorType: TypeOfSubcontractor,
  firstName: Option[String] = None,
  secondName: Option[String] = None,
  surname: Option[String] = None,
  tradingName: Option[String] = None,
  addressLine1: Option[String] = None,
  addressLine2: Option[String] = None,
  addressLine3: Option[String] = None,
  addressLine4: Option[String] = None,
  country: Option[String] = None,
  postcode: Option[String] = None,
  nino: Option[String] = None,
  utr: Option[String] = None,
  worksReferenceNumber: Option[String] = None,
  emailAddress: Option[String] = None,
  phoneNumber: Option[String] = None
)

object CreateAndUpdateSubcontractorRequest {
  implicit val format: OFormat[CreateAndUpdateSubcontractorRequest] = Json.format[CreateAndUpdateSubcontractorRequest]
}
