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
import play.api.libs.json.*

case class UnmatchedSubcontractorDetailsUpdated(
                                                    subcontractorName: UnmatchedSubcontractorName,
                                                    updates: Seq[UnmatchedSubcontractorUpdate],
                                                    returnTo: String = UnmatchedSubcontractorDetailsUpdatedReturnTo.CannotVerifyAllSubcontractors
                                                  )

object UnmatchedSubcontractorDetailsUpdatedReturnTo {

  val ReviewUnmatchedSubcontractors: String =
    "reviewUnmatchedSubcontractors"

  val YourSubcontractors: String =
    "yourSubcontractors"

  val CannotVerifyAllSubcontractors: String =
    "cannotVerifyAllSubcontractors"
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
                                            missingValueKey: String = "unmatchedSubcontractorDetailsUpdated.noneProvided"
                                          )

object UnmatchedSubcontractorUpdate {

  implicit val reads: Reads[UnmatchedSubcontractorUpdate] = (
    (__ \ "detail").read[String] and
      (__ \ "previous").readNullable[String] and
      (__ \ "updated").readNullable[String] and
      (__ \ "missingValueKey").readWithDefault(
        "insufficientSubcontractorDetailsUpdated.noneProvided"
      )
    )(UnmatchedSubcontractorUpdate.apply)

  implicit val writes: OWrites[UnmatchedSubcontractorUpdate] =
    Json.writes[UnmatchedSubcontractorUpdate]

  implicit val format: OFormat[UnmatchedSubcontractorUpdate] =
    OFormat(reads, writes)
}