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

package models.unmatched

import play.api.libs.functional.syntax.*
import play.api.libs.json.{JsError, JsString, Json, OFormat, OWrites, Reads, Writes, __}

case class UnmatchedSubcontractorDetailsUpdated(
  subcontractorName: UnmatchedSubcontractorName,
  updates: Seq[UnmatchedSubcontractorUpdate],
  returnTo: UnmatchedSubcontractorDetailsUpdatedReturnTo =
    UnmatchedSubcontractorDetailsUpdatedReturnTo.CannotVerifyAllSubcontractors
)

sealed trait UnmatchedSubcontractorDetailsUpdatedReturnTo

object UnmatchedSubcontractorDetailsUpdatedReturnTo {

  case object ReviewUnmatchedSubcontractors extends UnmatchedSubcontractorDetailsUpdatedReturnTo
  case object YourSubcontractors extends UnmatchedSubcontractorDetailsUpdatedReturnTo
  case object CannotVerifyAllSubcontractors extends UnmatchedSubcontractorDetailsUpdatedReturnTo

  implicit val reads: Reads[UnmatchedSubcontractorDetailsUpdatedReturnTo] =
    Reads.of[String].flatMap {
      case "reviewUnmatchedSubcontractors" => Reads.pure(ReviewUnmatchedSubcontractors)
      case "yourSubcontractors"            => Reads.pure(YourSubcontractors)
      case "cannotVerifyAllSubcontractors" => Reads.pure(CannotVerifyAllSubcontractors)
      case _                               => Reads(_ => JsError("Unknown returnTo value"))
    }

  implicit val writes: Writes[UnmatchedSubcontractorDetailsUpdatedReturnTo] =
    Writes {
      case ReviewUnmatchedSubcontractors => JsString("reviewUnmatchedSubcontractors")
      case YourSubcontractors            => JsString("yourSubcontractors")
      case CannotVerifyAllSubcontractors => JsString("cannotVerifyAllSubcontractors")
    }
}

object UnmatchedSubcontractorDetailsUpdated {

  implicit val reads: Reads[UnmatchedSubcontractorDetailsUpdated] = (
    (__ \ "subcontractorName").read[UnmatchedSubcontractorName] and
      (__ \ "updates").read[Seq[UnmatchedSubcontractorUpdate]] and
      (__ \ "returnTo").readWithDefault(
        UnmatchedSubcontractorDetailsUpdatedReturnTo.CannotVerifyAllSubcontractors
      )
  )(UnmatchedSubcontractorDetailsUpdated.apply)

  implicit val writes: OWrites[UnmatchedSubcontractorDetailsUpdated] =
    Json.writes[UnmatchedSubcontractorDetailsUpdated]

  implicit val format: OFormat[UnmatchedSubcontractorDetailsUpdated] =
    OFormat(reads, writes)
}

case class UnmatchedSubcontractorName(
  firstName: Option[String],
  lastName: Option[String]
) {
  def displayName: String =
    Seq(firstName, lastName).flatten.mkString(" ")
}

object UnmatchedSubcontractorName {
  implicit val format: OFormat[UnmatchedSubcontractorName] =
    Json.format[UnmatchedSubcontractorName]
}

case class UnmatchedSubcontractorUpdate(
  detail: String,
  previous: Option[String],
  updated: Option[String],
  nonSelectedKey: String = "unmatched.unmatchedSubcontractorDetailsUpdated.noneSelected",
  missingValueKey: String = "unmatched.unmatchedSubcontractorDetailsUpdated.noneProvided"
)

object UnmatchedSubcontractorUpdate {

  implicit val reads: Reads[UnmatchedSubcontractorUpdate] = (
    (__ \ "detail").read[String] and
      (__ \ "previous").readNullable[String] and
      (__ \ "updated").readNullable[String] and
      (__ \ "nonSelectedKey").readWithDefault(
        "unmatched.unmatchedSubcontractorDetailsUpdated.noneSelected"
      ) and
      (__ \ "missingValueKey").readWithDefault(
        "unmatched.unmatchedSubcontractorDetailsUpdated.noneProvided"
      )
  )(UnmatchedSubcontractorUpdate.apply)

  implicit val writes: OWrites[UnmatchedSubcontractorUpdate] =
    Json.writes[UnmatchedSubcontractorUpdate]

  implicit val format: OFormat[UnmatchedSubcontractorUpdate] =
    OFormat(reads, writes)
}
