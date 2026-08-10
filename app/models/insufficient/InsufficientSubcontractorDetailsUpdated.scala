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

package models.insufficient

import play.api.libs.functional.syntax.*
import play.api.libs.json.*

case class InsufficientSubcontractorDetailsUpdated(
  subcontractorName: InsufficientSubcontractorName,
  updates: Seq[InsufficientSubcontractorUpdate],
  returnTo: String = InsufficientSubcontractorDetailsUpdatedReturnTo.CannotVerifyAllSubcontractors
)

object InsufficientSubcontractorDetailsUpdatedReturnTo {

  val ReviewUnmatchedSubcontractors: String =
    "reviewUnmatchedSubcontractors"

  val YourSubcontractors: String =
    "yourSubcontractors"

  val CannotVerifyAllSubcontractors: String =
    "cannotVerifyAllSubcontractors"
}

object InsufficientSubcontractorDetailsUpdated {

  implicit val reads: Reads[InsufficientSubcontractorDetailsUpdated] = (
    (__ \ "subcontractorName").read[InsufficientSubcontractorName] and
      (__ \ "updates").read[Seq[InsufficientSubcontractorUpdate]] and
      (__ \ "returnTo").readWithDefault(
        InsufficientSubcontractorDetailsUpdatedReturnTo.CannotVerifyAllSubcontractors
      )
  )(InsufficientSubcontractorDetailsUpdated.apply)

  implicit val writes: OWrites[InsufficientSubcontractorDetailsUpdated] =
    Json.writes[InsufficientSubcontractorDetailsUpdated]

  implicit val format: OFormat[InsufficientSubcontractorDetailsUpdated] =
    OFormat(reads, writes)
}

case class InsufficientSubcontractorName(
  firstName: Option[String],
  lastName: Option[String]
) {
  def displayName: String =
    Seq(firstName, lastName).flatten.mkString(" ")
}

object InsufficientSubcontractorName {
  implicit val format: OFormat[InsufficientSubcontractorName] =
    Json.format[InsufficientSubcontractorName]
}

case class InsufficientSubcontractorUpdate(
  detail: String,
  previous: Option[String],
  updated: Option[String],
  missingValueKey: String = "insufficientSubcontractorDetailsUpdated.noneProvided"
)

object InsufficientSubcontractorUpdate {

  implicit val reads: Reads[InsufficientSubcontractorUpdate] = (
    (__ \ "detail").read[String] and
      (__ \ "previous").readNullable[String] and
      (__ \ "updated").readNullable[String] and
      (__ \ "missingValueKey").readWithDefault(
        "insufficientSubcontractorDetailsUpdated.noneProvided"
      )
  )(InsufficientSubcontractorUpdate.apply)

  implicit val writes: OWrites[InsufficientSubcontractorUpdate] =
    Json.writes[InsufficientSubcontractorUpdate]

  implicit val format: OFormat[InsufficientSubcontractorUpdate] =
    OFormat(reads, writes)
}
