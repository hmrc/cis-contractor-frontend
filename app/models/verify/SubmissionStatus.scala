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

package models.verify

import play.api.libs.json.*

sealed trait SubmissionStatus {
  def value: String
}
object SubmissionStatus {

  sealed abstract class KnownStatus(
    override val value: String
  ) extends SubmissionStatus

  case object STARTED extends KnownStatus("STARTED")
  case object PENDING extends KnownStatus("PENDING")
  case object ACCEPTED extends KnownStatus("ACCEPTED")
  case object TIMED_OUT extends KnownStatus("TIMED_OUT")
  case object SUBMITTED extends KnownStatus("SUBMITTED")
  case object SUBMITTED_NO_RECEIPT extends KnownStatus("SUBMITTED_NO_RECEIPT")
  case object DEPARTMENTAL_ERROR extends KnownStatus("DEPARTMENTAL_ERROR")
  case object FATAL_ERROR extends KnownStatus("FATAL_ERROR")
  case object SEND_ERROR extends KnownStatus("SEND_ERROR")

  final case class Unknown(value: String) extends SubmissionStatus

  private val knownStatuses: Seq[KnownStatus] =
    Seq(
      STARTED,
      PENDING,
      ACCEPTED,
      TIMED_OUT,
      SUBMITTED,
      SUBMITTED_NO_RECEIPT,
      DEPARTMENTAL_ERROR,
      FATAL_ERROR,
      SEND_ERROR
    )

  def fromString(value: String): SubmissionStatus =
    knownStatuses
      .find(_.value == value)
      .getOrElse(Unknown(value))

  implicit val format: Format[SubmissionStatus] =
    Format(
      Reads.StringReads.map(fromString),
      Writes(status => JsString(status.value))
    )
}
